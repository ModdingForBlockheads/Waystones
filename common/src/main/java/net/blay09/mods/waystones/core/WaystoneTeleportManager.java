package net.blay09.mods.waystones.core;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.*;
import net.blay09.mods.waystones.api.error.WaystoneTeleportError;
import net.blay09.mods.waystones.api.event.WaystoneTeleportEvent;
import net.blay09.mods.waystones.block.WaystoneBlock;
import net.blay09.mods.waystones.block.entity.WarpPlateBlockEntity;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.config.WaystonesConfigData;
import net.blay09.mods.waystones.network.message.TeleportEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class WaystoneTeleportManager {

    public static Collection<? extends Entity> findPets(Entity entity) {
        return entity.level().getEntitiesOfClass(TamableAnimal.class, new AABB(entity.blockPosition()).inflate(10),
                pet -> entity.getUUID().equals(pet.getOwnerUUID()) && !pet.isOrderedToSit() && !pet.isLeashed() && !WaystonePermissionManager.isEntityDeniedTeleports(pet)
        );
    }

    public static List<Mob> findLeashedAnimals(Entity player) {
        return player.level().getEntitiesOfClass(Mob.class, new AABB(player.blockPosition()).inflate(10),
                e -> player.equals(e.getLeashHolder())
        );
    }

    public static Either<List<Entity>, WaystoneTeleportError> doTeleport(WaystoneTeleportContext context) {
        final var server = context.getEntity().getServer();
        if (server == null) {
            return Either.right(new WaystoneTeleportError.NotOnServer());
        }

        return resolveDestination(server, context.getTargetWaystone()).flatMap(it -> doTeleport(context, it));
    }

    public static Either<List<Entity>, WaystoneTeleportError> doTeleport(WaystoneTeleportContext context, TeleportDestination destination) {
        final var sourceLevel = (ServerLevel) context.getEntity().level();
        List<Entity> teleportedEntities = teleportEntityAndAttached(context.getEntity(), context, destination);
        context.getAdditionalEntities()
                .forEach(additionalEntity -> teleportedEntities.addAll(teleportEntityAndAttached(additionalEntity, context, destination)));

        final var sourcePos = context.getEntity().blockPosition();
        final var targetLevel = (ServerLevel) destination.level();
        final var targetPos = BlockPos.containing(destination.location());

        BlockEntity targetTileEntity = targetLevel.getBlockEntity(targetPos);
        if (targetTileEntity instanceof WarpPlateBlockEntity warpPlate) {
            teleportedEntities.forEach(warpPlate::markEntityForCooldown);
        }

        if (context.playsSound()) {
            sourceLevel.playSound(context.getEntity(), sourcePos, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.01f, 1f);
            targetLevel.playSound(null, targetPos, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.05f, 1f);
        }

        if (context.playsEffect()) {
            teleportedEntities.forEach(additionalEntity -> Balm.getNetworking().sendToTracking(sourceLevel, sourcePos, new TeleportEffectMessage(sourcePos)));
            Balm.getNetworking().sendToTracking(targetLevel, targetPos, new TeleportEffectMessage(targetPos));
        }

        if (targetTileEntity instanceof WaystoneBlockEntityBase waystoneBlockEntity) {
            teleportedEntities.forEach(waystoneBlockEntity::applyModifierEffects);
        }

        return Either.left(teleportedEntities);
    }

    private static List<Entity> teleportEntityAndAttached(Entity entity, WaystoneTeleportContext context, TeleportDestination destination) {
        final var teleportedEntities = new ArrayList<Entity>();

        final var targetLevel = (ServerLevel) destination.level();
        final var targetLocation = destination.location();
        final var targetDirection = destination.direction();

        final var mount = entity.getVehicle();
        Entity teleportedMount = null;
        if (mount != null) {
            teleportedMount = teleportEntity(mount, targetLevel, targetLocation, targetDirection);
            teleportedEntities.add(teleportedMount);
        }

        final List<Mob> leashedEntities = context.getLeashedEntities();
        final List<Entity> teleportedLeashedEntities = new ArrayList<>();
        leashedEntities.forEach(leashedEntity -> {
            Entity teleportedLeashedEntity = teleportEntity(leashedEntity, targetLevel, targetLocation, targetDirection);
            teleportedEntities.add(teleportedLeashedEntity);
            teleportedLeashedEntities.add(teleportedLeashedEntity);
        });

        final var teleportedEntity = teleportEntity(entity, targetLevel, targetLocation, targetDirection);
        teleportedEntities.add(teleportedEntity);

        // We have to update the leashedToEntity in case the player was cloned during dimensional teleport
        teleportedLeashedEntities.forEach(teleportedLeashedEntity -> {
            if (teleportedLeashedEntity instanceof Mob teleportedLeashedMob) {
                teleportedLeashedMob.setLeashedTo(teleportedEntity, true);
            }
        });

        if (teleportedMount != null) {
            // TODO We do not remount currently. It causes weird sync issues and it seems that Vanilla does not do it either.
            //      Would have to look further at what point it's safe to remount without triggering movement correction.
        }

        return teleportedEntities;
    }

    private static Entity teleportEntity(Entity entity, ServerLevel targetWorld, Vec3 targetPos3d, Direction direction) {
        float yaw = direction.toYRot();
        double x = targetPos3d.x;
        double y = targetPos3d.y;
        double z = targetPos3d.z;
        if (entity instanceof ServerPlayer) {
            ChunkPos chunkPos = new ChunkPos(BlockPos.containing(x, y, z));
            targetWorld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, entity.getId());
            entity.stopRiding();
            if (((ServerPlayer) entity).isSleeping()) {
                ((ServerPlayer) entity).stopSleepInBed(true, true);
            }

            if (targetWorld == entity.level()) {
                ((ServerPlayer) entity).connection.teleport(x, y, z, yaw, entity.getXRot());
            } else {
                entity.teleportTo(targetWorld, x, y, z, Set.of(), yaw, entity.getXRot(), false);
            }

            entity.setYHeadRot(yaw);
        } else {
            float pitch = Mth.clamp(entity.getXRot(), -90.0F, 90.0F);
            if (targetWorld == entity.level()) {
                entity.moveTo(x, y, z, yaw, pitch);
                entity.setYHeadRot(yaw);
            } else {
                entity.unRide();
                Entity oldEntity = entity;
                entity = entity.getType().create(targetWorld, EntitySpawnReason.DIMENSION_TRAVEL);
                if (entity == null) {
                    return oldEntity;
                }

                entity.restoreFrom(oldEntity);
                entity.moveTo(x, y, z, yaw, pitch);
                entity.setYHeadRot(yaw);
                oldEntity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                targetWorld.addDuringTeleport(entity);
            }
        }

        if (!(entity instanceof LivingEntity) || !((LivingEntity) entity).isFallFlying()) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0, 1));
            entity.setOnGround(true);
        }

        if (entity instanceof PathfinderMob) {
            ((PathfinderMob) entity).getNavigation().stop();
        }

        sendHackySyncPacketsAfterTeleport(entity);

        return entity;
    }

    private static Either<TeleportDestination, WaystoneTeleportError> resolveDestination(MinecraftServer server, Waystone waystone) {
        final var level = server.getLevel(waystone.getDimension());
        if (level == null) {
            return Either.right(new WaystoneTeleportError.InvalidDimension(waystone.getDimension()));
        }

        final var pos = waystone.getPos();
        final var state = level.getBlockState(pos);
        var direction = state.hasProperty(WaystoneBlock.FACING) ? state.getValue(WaystoneBlock.FACING) : Direction.NORTH;

        // Use a list to keep order intact - it might check one direction twice, but no one cares
        final var directionCandidates = Lists.newArrayList(direction, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH);
        for (Direction candidate : directionCandidates) {
            BlockPos offsetPos = pos.relative(candidate);
            BlockPos offsetPosUp = offsetPos.above();
            if (level.getBlockState(offsetPos).isSuffocating(level, offsetPos) || level.getBlockState(offsetPosUp).isSuffocating(level, offsetPosUp)) {
                continue;
            }

            direction = candidate;
            break;
        }

        final var waystoneType = waystone.getWaystoneType();
        final var shouldOffsetFacing = !(waystoneType.equals(WaystoneTypes.WARP_PLATE));
        final var targetPos = shouldOffsetFacing ? pos.relative(direction) : pos;
        final var location = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        return Either.left(new TeleportDestination(level, location, direction));
    }

    private static void sendHackySyncPacketsAfterTeleport(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            // No idea why this is still needed since we're using the same code as /tp. Maybe /tp is broken too for interdimensional travel.
            player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        }
    }

    public static Either<List<Entity>, WaystoneTeleportError> tryTeleport(WaystoneTeleportContext context) {
        WaystoneTeleportEvent.Pre event = new WaystoneTeleportEvent.Pre(context);
        Balm.getEvents().fireEvent(event);
        if (event.isCanceled()) {
            return Either.right(new WaystoneTeleportError.CancelledByEvent());
        }

        final var entity = context.getEntity();

        if (!context.getLeashedEntities().isEmpty()) {
            if (WaystonesConfig.getActive().teleports.transportLeashed == WaystonesConfigData.TransportMobs.DISABLED) {
                return Either.right(new WaystoneTeleportError.LeashedWarpDenied());
            }

            for (final var leashedEntity : context.getLeashedEntities()) {
                if (WaystonePermissionManager.isEntityDeniedTeleports(leashedEntity)) {
                    return Either.right(new WaystoneTeleportError.SpecificLeashedWarpDenied(leashedEntity));
                }
            }

            if (context.isDimensionalTeleport() && WaystonesConfig.getActive().teleports.transportLeashed == WaystonesConfigData.TransportMobs.SAME_DIMENSION) {
                return Either.right(new WaystoneTeleportError.LeashedDimensionalWarpDenied());
            }
        }

        if (entity instanceof Player player && !context.getRequirements().canAfford(player) && !player.getAbilities().instabuild) {
            return Either.right(new WaystoneTeleportError.NotEnoughXp());
        }

        if (entity instanceof Player player) {
            context.getRequirements().consume(player);
        }

        return doTeleport(context).ifLeft(teleportedEntities -> Balm.getEvents().fireEvent(new WaystoneTeleportEvent.Post(context, teleportedEntities)));
    }

}
