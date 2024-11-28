package net.blay09.mods.waystones.core;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.BalmEnvironment;
import net.blay09.mods.waystones.api.*;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.api.event.WaystoneActivatedEvent;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.blay09.mods.waystones.config.InventoryButtonMode;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.config.WaystonesConfigData;
import net.blay09.mods.waystones.worldgen.namegen.NameGenerationMode;
import net.blay09.mods.waystones.worldgen.namegen.NameGeneratorManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerWaystoneManager {

    private static final IPlayerWaystoneData persistentPlayerWaystoneData = new PersistentPlayerWaystoneData();
    private static final IPlayerWaystoneData inMemoryPlayerWaystoneData = new InMemoryPlayerWaystoneData();

    public static boolean isWaystoneActivated(Player player, Waystone waystone) {
        return getPlayerWaystoneData(player.level()).isWaystoneActivated(player, waystone);
    }

    public static void activateWaystone(Player player, Waystone waystone) {
        if (!waystone.hasName() && waystone instanceof MutableWaystone && waystone.wasGenerated()) {
            NameGenerationMode nameGenerationMode = WaystonesConfig.getActive().worldGen.nameGenerationMode;
            final var name = NameGeneratorManager.get(player.getServer()).getName(player.level(), waystone, player.level().random, nameGenerationMode);
            ((MutableWaystone) waystone).setName(name);
        }

        if (!waystone.hasOwner() && waystone instanceof MutableWaystone mutableWaystone) {
            mutableWaystone.setOwnerUid(player.getUUID());
            mutableWaystone.setVisibility(WaystonesConfig.getActive().general.defaultVisibility);
        }

        if (player.getServer() != null) {
            WaystoneManagerImpl.get(player.getServer()).setDirty();
        }

        if (!isWaystoneActivated(player, waystone) && waystone.getWaystoneType().equals(WaystoneTypes.WAYSTONE)) {
            getPlayerWaystoneData(player.level()).activateWaystone(player, waystone);

            Balm.getEvents().fireEvent(new WaystoneActivatedEvent(player, waystone));
        }
    }

    public static Optional<Waystone> getInventoryButtonTarget(Player player) {
        InventoryButtonMode inventoryButtonMode = WaystonesConfig.getActive().getInventoryButtonMode();
        if (inventoryButtonMode.isReturnToNearest()) {
            return PlayerWaystoneManager.getNearestWaystone(player);
        } else if (inventoryButtonMode.hasNamedTarget()) {
            return WaystoneManagerImpl.get(player.getServer()).findWaystoneByName(inventoryButtonMode.getNamedTarget());
        }

        return Optional.empty();
    }

    public static void deactivateWaystone(Player player, Waystone waystone) {
        getPlayerWaystoneData(player.level()).deactivateWaystone(player, waystone);
    }

    public static Map<ResourceLocation, Long> getCooldowns(Player player) {
        return getPlayerWaystoneData(player.level()).getCooldowns(player);
    }

    public static void resetCooldowns(Player player) {
        getPlayerWaystoneData(player.level()).resetCooldowns(player);
    }

    public static long getCooldownUntil(Player player, ResourceLocation key) {
        return getPlayerWaystoneData(player.level()).getCooldownUntil(player, key);
    }

    public static long getCooldownMillisLeft(Player player, ResourceLocation key) {
        long cooldownUntil = getCooldownUntil(player, key);
        return Math.max(0, cooldownUntil - System.currentTimeMillis());
    }

    public static void setCooldownUntil(Player player, ResourceLocation key, long timestamp) {
        getPlayerWaystoneData(player.level()).setCooldownUntil(player, key, timestamp);
    }

    public static Optional<Waystone> getNearestWaystone(Player player) {
        return getPlayerWaystoneData(player.level()).getWaystones(player).stream()
                .filter(it -> it.getDimension() == player.level().dimension())
                .min((first, second) -> {
                    double firstDist = first.getPos().distToCenterSqr(player.getX(), player.getY(), player.getZ());
                    double secondDist = second.getPos().distToCenterSqr(player.getX(), player.getY(), player.getZ());
                    return (int) Math.round(firstDist) - (int) Math.round(secondDist);
                });
    }

    public static Collection<Waystone> getActivatedWaystones(Player player) {
        return getPlayerWaystoneData(player.level()).getWaystones(player);
    }

    public static IPlayerWaystoneData getPlayerWaystoneData(@Nullable Level world) {
        return world == null || world.isClientSide ? inMemoryPlayerWaystoneData : persistentPlayerWaystoneData;
    }

    public static IPlayerWaystoneData getPlayerWaystoneData(BalmEnvironment side) {
        return side.isClient() ? inMemoryPlayerWaystoneData : persistentPlayerWaystoneData;
    }

    public static List<UUID> getSortingIndex(Player player) {
        return getPlayerWaystoneData(player.level()).getSortingIndex(player);
    }

    public static List<UUID> ensureSortingIndex(Player player, Collection<Waystone> waystones) {
        return getPlayerWaystoneData(player.level()).ensureSortingIndex(player, waystones);
    }

    public static void sortWaystoneAsFirst(Player player, UUID waystoneUid) {
        getPlayerWaystoneData(player.level()).sortWaystoneAsFirst(player, waystoneUid);
    }

    public static void sortWaystoneAsLast(Player player, UUID waystoneUid) {
        getPlayerWaystoneData(player.level()).sortWaystoneAsLast(player, waystoneUid);
    }

    public static void sortWaystoneSwap(Player player, UUID waystoneUid, UUID otherWaystoneUid) {
        getPlayerWaystoneData(player.level()).sortWaystoneSwap(player, waystoneUid, otherWaystoneUid);
    }

    public static void activeWaystoneForEveryone(@Nullable MinecraftServer server, Waystone waystone) {
        if (server == null) {
            return;
        }

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            if (!isWaystoneActivated(player, waystone)) {
                activateWaystone(player, waystone);
            }
        }
    }

    public static void removeKnownWaystone(@Nullable MinecraftServer server, Waystone waystone) {
        if (server == null) {
            return;
        }

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            deactivateWaystone(player, waystone);
            WaystoneSyncManager.sendActivatedWaystones(player);
        }
    }

    public static Collection<Waystone> getTargetsForPlayer(Player player) {
        return PlayerWaystoneManager.getActivatedWaystones(player);
    }

    public static Collection<Waystone> getTargetsForItem(Player player, ItemStack itemStack) {
        return PlayerWaystoneManager.getActivatedWaystones(player);
    }

    public static Collection<Waystone> getTargetsForWaystone(Player player, Waystone waystone) {
        final var result = getTargetsForWaystoneType(player, waystone.getWaystoneType());

        final var blockEntity = player.level().getBlockEntity(waystone.getPos());
        if (blockEntity instanceof WaystoneBlockEntityBase waystoneBlockEntity) {
            result.addAll(waystoneBlockEntity.getAuxiliaryTargets());
        }

        return result;
    }

    public static Collection<Waystone> getTargetsForWaystoneType(Player player, ResourceLocation waystoneType) {
        final var result = new ArrayList<Waystone>();
        if (WaystoneTypes.isSharestone(waystoneType)) {
            result.addAll(WaystoneManagerImpl.get(player.getServer()).getWaystonesByType(waystoneType).toList());
        } else {
            result.addAll(PlayerWaystoneManager.getActivatedWaystones(player));
        }

        return result;
    }

    public static Collection<Waystone> getTargetsForInventoryButton(ServerPlayer player) {
        return PlayerWaystoneManager.getActivatedWaystones(player);
    }
}
