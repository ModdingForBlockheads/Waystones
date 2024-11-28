package net.blay09.mods.waystones.api;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.waystones.api.requirement.*;
import net.blay09.mods.waystones.api.error.WaystoneTeleportError;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class WaystonesAPI {

    public static InternalMethods __internalMethods;

    public static Either<WaystoneTeleportContext, WaystoneTeleportError> createDefaultTeleportContext(Entity entity, Waystone waystone, Consumer<WaystoneTeleportContext> init) {
        return __internalMethods.createDefaultTeleportContext(entity, waystone, init);
    }

    public static Either<WaystoneTeleportContext, WaystoneTeleportError> createCustomTeleportContext(Entity entity, Waystone waystone) {
        return __internalMethods.createCustomTeleportContext(entity, waystone);
    }

    public static WaystoneTeleportContext createUnboundTeleportContext(Entity entity, Waystone waystone) {
        return __internalMethods.createUnboundTeleportContext(entity, waystone);
    }

    public static WaystoneTeleportContext createUnboundTeleportContext(Entity entity) {
        return __internalMethods.createUnboundTeleportContext(entity);
    }

    public static Either<List<Entity>, WaystoneTeleportError> tryTeleport(WaystoneTeleportContext context) {
        return __internalMethods.tryTeleport(context);
    }

    public static Either<List<Entity>, WaystoneTeleportError> forceTeleport(WaystoneTeleportContext context) {
        return __internalMethods.forceTeleport(context);
    }

    public static Optional<Waystone> getWaystoneAt(ServerLevel level, BlockPos pos) {
        return __internalMethods.getWaystoneAt(level, pos);
    }

    /**
     * @param level only used to access getServer() when on server, does not have to match the waystone's actual level
     */
    public static Optional<Waystone> getWaystone(Level level, UUID uuid) {
        return __internalMethods.getWaystone(level, uuid);
    }

    public static boolean isWaystoneActivated(Player player, Waystone waystone) {
        return __internalMethods.isWaystoneActivated(player, waystone);
    }

    public static Collection<Waystone> getActivatedWaystones(Player player) {
        return __internalMethods.getActivatedWaystones(player);
    }

    public static Optional<Waystone> getNearestWaystone(Player player) {
        return __internalMethods.getNearestWaystone(player);
    }

    public static Optional<Waystone> placeWaystone(Level level, BlockPos pos, WaystoneStyle style) {
        return __internalMethods.placeWaystone(level, pos, style);
    }

    public static Optional<Waystone> placeSharestone(Level level, BlockPos pos, @Nullable DyeColor color) {
        return __internalMethods.placeSharestone(level, pos, color);
    }

    public static Optional<Waystone> placeWarpPlate(Level level, BlockPos pos) {
        return __internalMethods.placeWarpPlate(level, pos);
    }

    public static Optional<Waystone> getBoundWaystone(@Nullable Player player, ItemStack itemStack) {
        return __internalMethods.getBoundWaystone(player, itemStack);
    }

    public static void setBoundWaystone(ItemStack itemStack, Waystone waystone) {
        __internalMethods.setBoundWaystone(itemStack, waystone);
    }

    public static ItemStack createAttunedShard(Waystone warpPlate) {
        return __internalMethods.createAttunedShard(warpPlate);
    }

    public static ItemStack createBoundScroll(Waystone waystone) {
        return __internalMethods.createBoundScroll(waystone);
    }

    public static WarpRequirement resolveRequirements(WaystoneTeleportContext context) {
        return __internalMethods.resolveRequirements(context);
    }

    public static void registerRequirementType(RequirementType<?> requirementType) {
        __internalMethods.registerRequirementType(requirementType);
    }

    public static void registerRequirementModifier(RequirementFunction<?, ?> requirementModifier) {
        __internalMethods.registerRequirementModifier(requirementModifier);
    }

    public static void registerVariableResolver(VariableResolver variableResolver) {
        __internalMethods.registerVariableResolver(variableResolver);
    }

    public static void registerConditionPredicate(ConditionResolver<?> conditionResolver) {
        __internalMethods.registerConditionResolver(conditionResolver);
    }

    public static void registerParameterSerializer(ParameterSerializer<?> parameterSerializer) {
        __internalMethods.registerParameterSerializer(parameterSerializer);
    }

    public static void activateWaystone(ServerPlayer player, Waystone waystone) {
        __internalMethods.activateWaystone(player, waystone);
    }

    public static void deactivateWaystone(ServerPlayer player, Waystone waystone) {
        __internalMethods.deactivateWaystone(player, waystone);
    }
}
