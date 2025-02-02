package net.blay09.mods.waystones.menu;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.blay09.mods.waystones.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WaystoneModifierMenu extends AbstractContainerMenu {

    private final WaystoneBlockEntityBase blockEntity;

    public WaystoneModifierMenu(int windowId, WaystoneBlockEntityBase warpPlate, Inventory playerInventory) {
        super(ModMenus.waystoneModifiers.get(), windowId);
        this.blockEntity = warpPlate;

        addSlot(new WaystoneModifierSlot(warpPlate.getContainer(), 0, 80, 45));
        addSlot(new WaystoneModifierSlot(warpPlate.getContainer(), 1, 80, 17));
        addSlot(new WaystoneModifierSlot(warpPlate.getContainer(), 2, 108, 45));
        addSlot(new WaystoneModifierSlot(warpPlate.getContainer(), 3, 80, 73));
        addSlot(new WaystoneModifierSlot(warpPlate.getContainer(), 4, 52, 45));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        for (int j = 0; j < 9; ++j) {
            addSlot(new Slot(playerInventory, j, 8 + j * 18, 162));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(blockEntity, player);
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
            }
            else {
                if (!getSlot(0).hasItem()) {
                    if (!this.moveItemStackTo(slotStack.split(1), 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else {
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

    public Waystone getWaystone() {
        return blockEntity.getWaystone();
    }
}
