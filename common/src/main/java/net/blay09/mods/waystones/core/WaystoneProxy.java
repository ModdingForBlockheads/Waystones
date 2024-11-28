package net.blay09.mods.waystones.core;

import net.blay09.mods.waystones.api.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WaystoneProxy implements Waystone, MutableWaystone {

    private final MinecraftServer server;
    private final UUID waystoneUid;
    private Waystone backingWaystone;

    public WaystoneProxy(@Nullable MinecraftServer server, UUID waystoneUid) {
        this.server = server;
        this.waystoneUid = waystoneUid;
    }

    @Override
    public boolean isValidInLevel(ServerLevel level) {
        return getBackingWaystone().isValidInLevel(level);
    }

    @Override
    public boolean isTransient() {
        return getBackingWaystone().isTransient();
    }

    @Override
    public boolean isValid() {
        return WaystoneManagerImpl.get(server).getWaystoneById(waystoneUid).isPresent();
    }

    public Waystone getBackingWaystone() {
        if (backingWaystone == null) {
            backingWaystone = WaystoneManagerImpl.get(server).getWaystoneById(waystoneUid).orElse(InvalidWaystone.INSTANCE);
        }

        return backingWaystone;
    }

    @Override
    public UUID getOwnerUid() {
        return getBackingWaystone().getOwnerUid();
    }

    @Override
    public UUID getWaystoneUid() {
        return waystoneUid;
    }

    @Override
    public Component getName() {
        return getBackingWaystone().getName();
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return getBackingWaystone().getDimension();
    }

    @Override
    public boolean wasGenerated() {
        return getBackingWaystone().wasGenerated();
    }

    @Override
    public WaystoneOrigin getOrigin() {
        return getBackingWaystone().getOrigin();
    }

    @Override
    public boolean isOwner(Player player) {
        return getBackingWaystone().isOwner(player);
    }

    @Override
    public BlockPos getPos() {
        return getBackingWaystone().getPos();
    }

    @Override
    public ResourceLocation getWaystoneType() {
        return getBackingWaystone().getWaystoneType();
    }

    @Override
    public void setName(Component name) {
        Waystone backingWaystone = getBackingWaystone();
        if (backingWaystone instanceof MutableWaystone) {
            ((MutableWaystone) backingWaystone).setName(name);
        }
    }

    @Override
    public void setDimension(ResourceKey<Level> dimension) {
        Waystone backingWaystone = getBackingWaystone();
        if (backingWaystone instanceof MutableWaystone) {
            ((MutableWaystone) backingWaystone).setDimension(dimension);
        }
    }

    @Override
    public void setPos(BlockPos pos) {
        Waystone backingWaystone = getBackingWaystone();
        if (backingWaystone instanceof MutableWaystone) {
            ((MutableWaystone) backingWaystone).setPos(pos);
        }
    }

    @Override
    public void setOwnerUid(UUID ownerUid) {
        Waystone backingWaystone = getBackingWaystone();
        if (backingWaystone instanceof MutableWaystone) {
            ((MutableWaystone) backingWaystone).setOwnerUid(ownerUid);
        }
    }

    @Override
    public void setTransient(boolean isTransient) {
        Waystone backingWaystone = getBackingWaystone();
        if (backingWaystone instanceof MutableWaystone) {
            ((MutableWaystone) backingWaystone).setTransient(isTransient);
        }
    }

    @Override
    public WaystoneVisibility getVisibility() {
        return getBackingWaystone().getVisibility();
    }

    @Override
    public void setVisibility(WaystoneVisibility visibility) {
        Waystone backingWaystone = getBackingWaystone();
        if (backingWaystone instanceof MutableWaystone) {
            ((MutableWaystone) backingWaystone).setVisibility(visibility);
        }
    }
}
