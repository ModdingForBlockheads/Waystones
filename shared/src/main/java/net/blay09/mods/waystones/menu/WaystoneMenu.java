package net.blay09.mods.waystones.menu;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.api.WaystoneVisibility;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.WaystonePermissionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WaystoneMenu extends AbstractContainerMenu {

    private final Player player;
    private final Waystone waystone;
    private final WaystoneBlockEntityBase blockEntity;
    private final ContainerData containerData;
    private final boolean canEdit;

    public WaystoneMenu(int windowId, Waystone waystone, WaystoneBlockEntityBase blockEntity, ContainerData containerData, Inventory playerInventory, boolean canEdit) {
        super(ModMenus.waystoneSettings.get(), windowId);
        this.player = playerInventory.player;
        this.waystone = waystone;
        this.blockEntity = blockEntity;
        this.containerData = containerData;
        this.canEdit = canEdit;

        blockEntity.markReadyForAttunement();

        checkContainerDataCount(containerData, 1);

        addSlot(new WarpPlateAttunementSlot(blockEntity, 0, 80, 64));
        addSlot(new WarpPlateAttunementSlot(blockEntity, 1, 80, 36));
        addSlot(new WarpPlateAttunementSlot(blockEntity, 2, 108, 64));
        addSlot(new WarpPlateAttunementSlot(blockEntity, 3, 80, 92));
        addSlot(new WarpPlateAttunementSlot(blockEntity, 4, 52, 64));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 128 + i * 18));
            }
        }

        for (int j = 0; j < 9; ++j) {
            addSlot(new Slot(playerInventory, j, 8 + j * 18, 186));
        }

        addDataSlots(containerData);
    }

    public float getAttunementProgress() {
        return containerData.get(0) / (float) blockEntity.getMaxAttunementTicks();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            if (index < 5) {
                if (!this.moveItemStackTo(slotStack, 5, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!getSlot(0).hasItem()) {
                    if (!this.moveItemStackTo(slotStack.split(1), 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(slotStack, 1, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = blockEntity.getBlockPos();
        return player.distanceToSqr((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= 64;
    }

    public Waystone getWaystone() {
        return waystone;
    }

    public List<WaystoneVisibility> getVisibilityOptions() {
        final var result = new ArrayList<WaystoneVisibility>();
        final var baseVisibility = WaystoneVisibility.fromWaystoneType(waystone.getWaystoneType());
        result.add(baseVisibility);
        if (baseVisibility == WaystoneVisibility.ACTIVATION) {
            if (WaystonePermissionManager.isAllowedVisibility(WaystoneVisibility.GLOBAL) || WaystonePermissionManager.skipsPermissions(player)) {
                result.add(WaystoneVisibility.GLOBAL);
            }
        }
        return result;
    }

    public boolean canEdit() {
        return canEdit;
    }
}
