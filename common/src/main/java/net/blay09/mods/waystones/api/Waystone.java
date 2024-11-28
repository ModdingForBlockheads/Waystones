package net.blay09.mods.waystones.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Waystone {
    UUID getWaystoneUid();

    Component getName();

    ResourceKey<Level> getDimension();

    default boolean wasGenerated() {
        return getOrigin() == WaystoneOrigin.VILLAGE || getOrigin() == WaystoneOrigin.WILDERNESS || getOrigin() == WaystoneOrigin.DUNGEON;
    }

    WaystoneOrigin getOrigin();

    boolean isOwner(Player player);

    BlockPos getPos();

    boolean isValid();

    @Nullable
    UUID getOwnerUid();

    ResourceLocation getWaystoneType();

    default boolean hasName() {
        return !getName().getString().isEmpty();
    }

    default boolean hasOwner() {
        return getOwnerUid() != null;
    }

    default boolean isValidInLevel(ServerLevel level) {
        return false;
    }

    boolean isTransient();

    WaystoneVisibility getVisibility();
}
