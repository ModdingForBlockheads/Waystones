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

public interface InternalMethods {

    Either<WaystoneTeleportContext, WaystoneTeleportError> createDefaultTeleportContext(Entity entity, Waystone waystone, Consumer<WaystoneTeleportContext> init);

    Either<WaystoneTeleportContext, WaystoneTeleportError> createCustomTeleportContext(Entity entity, Waystone waystone);

    WaystoneTeleportContext createUnboundTeleportContext(Entity entity, Waystone waystone);

    WaystoneTeleportContext createUnboundTeleportContext(Entity entity);

    Either<List<Entity>, WaystoneTeleportError> tryTeleport(WaystoneTeleportContext context);

    Either<List<Entity>, WaystoneTeleportError> forceTeleport(WaystoneTeleportContext context);

    Optional<Waystone> getWaystoneAt(Level level, BlockPos pos);

    Optional<Waystone> getWaystone(Level level, UUID uuid);

    ItemStack createAttunedShard(Waystone warpPlate);

    ItemStack createBoundScroll(Waystone waystone);

    Optional<Waystone> placeWaystone(Level level, BlockPos pos, WaystoneStyle style);

    Optional<Waystone> placeSharestone(Level level, BlockPos pos, DyeColor color);

    Optional<Waystone> placeWarpPlate(Level level, BlockPos pos);

    Optional<Waystone> getBoundWaystone(Player player, ItemStack itemStack);

    void setBoundWaystone(ItemStack itemStack, @Nullable Waystone waystone);

    WarpRequirement resolveRequirements(WaystoneTeleportContext context);

    void registerRequirementType(RequirementType<?> requirementType);

    void registerRequirementModifier(RequirementFunction<?, ?> requirementModifier);

    void registerVariableResolver(VariableResolver variableResolver);

    void registerConditionResolver(ConditionResolver<?> conditionResolver);

    void registerParameterSerializer(ParameterSerializer<?> parameterSerializer);

    boolean isWaystoneActivated(Player player, Waystone waystone);

    Collection<Waystone> getActivatedWaystones(Player player);

    Optional<Waystone> getNearestWaystone(Player player);

    void activateWaystone(ServerPlayer player, Waystone waystone);

    void deactivateWaystone(ServerPlayer player, Waystone waystone);
}
