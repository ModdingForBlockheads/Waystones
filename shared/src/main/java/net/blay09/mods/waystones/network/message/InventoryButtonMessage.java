package net.blay09.mods.waystones.network.message;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.config.InventoryButtonMode;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.Waystone;
import net.blay09.mods.waystones.core.WaystoneTeleportManager;
import net.blay09.mods.waystones.menu.ModMenus;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WarpMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Optional;

public class InventoryButtonMessage {

    public static void encode(InventoryButtonMessage message, FriendlyByteBuf buf) {
    }

    public static InventoryButtonMessage decode(FriendlyByteBuf buf) {
        return new InventoryButtonMessage();
    }

    public static void handle(ServerPlayer player, InventoryButtonMessage message) {
        InventoryButtonMode inventoryButtonMode = WaystonesConfig.getActive().getInventoryButtonMode();
        if (!inventoryButtonMode.isEnabled()) {
            return;
        }

        if (player == null) {
            return;
        }

        // Reset cooldown if player is in creative mode
        if (player.getAbilities().instabuild) {
            PlayerWaystoneManager.setInventoryButtonCooldownUntil(player, 0);
        }

        if (!PlayerWaystoneManager.canUseInventoryButton(player)) {
            return;
        }

        IWaystone waystone = PlayerWaystoneManager.getInventoryButtonTarget(player);
        if (waystone != null) {
            WaystoneTeleportManager.tryTeleportToWaystone(player, waystone, WarpMode.INVENTORY_BUTTON, null);
        } else if (inventoryButtonMode.isReturnToAny()) {
            final var waystones = PlayerWaystoneManager.getTargetsForInventoryButton(player);
            PlayerWaystoneManager.ensureSortingIndex(player, waystones);
            final BalmMenuProvider containerProvider = new BalmMenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("container.waystones.waystone_selection");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                    return new WaystoneSelectionMenu(ModMenus.inventorySelection.get(), WarpMode.INVENTORY_BUTTON, null, windowId, waystones);
                }

                @Override
                public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                    Waystone.writeList(buf, waystones);
                }
            };
            Balm.getNetworking().openGui(player, containerProvider);
        }
    }

}
