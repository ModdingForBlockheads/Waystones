package net.blay09.mods.waystones.menu;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.blay09.mods.waystones.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WaystoneModifierMenu extends AbstractContainerMenu {

    private final Container container;
    private final Waystone waystone;

    public WaystoneModifierMenu(int windowId, Inventory playerInventory, Waystone waystone) {
        this(windowId, playerInventory, waystone, new SimpleContainer(5));
    }

    public WaystoneModifierMenu(int windowId, Inventory playerInventory, Waystone waystone, Container container) {
        super(ModMenus.waystoneModifiers.get(), windowId);
        this.container = container;
        this.waystone = waystone;

        addSlot(new WaystoneModifierSlot(container, 0, 80, 45));
        addSlot(new WaystoneModifierSlot(container, 1, 80, 17));
        addSlot(new WaystoneModifierSlot(container, 2, 108, 45));
        addSlot(new WaystoneModifierSlot(container, 3, 80, 73));
        addSlot(new WaystoneModifierSlot(container, 4, 52, 45));

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
        return container.stillValid(player);
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
        return waystone;
    }
}
