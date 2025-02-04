package net.blay09.mods.waystones.menu;

import net.blay09.mods.waystones.api.IWaystone;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WarpPlateContainer extends AbstractContainerMenu {

    private final IWaystone waystone;
    private final Container container;
    private final ContainerData containerData;

    public WarpPlateContainer(int windowId, Inventory playerInventory, IWaystone waystone) {
        this(windowId, playerInventory, waystone, new SimpleContainer(5), new SimpleContainerData(3));
    }

    public WarpPlateContainer(int windowId, Inventory playerInventory, IWaystone waystone, Container container, ContainerData containerData) {
        super(ModMenus.warpPlate.get(), windowId);
        this.waystone = waystone;
        this.container = container;
        this.containerData = containerData;

        checkContainerDataCount(containerData, 1);

        addSlot(new WarpPlateAttunementSlot(container, 0, 80, 45, this::isCompletedFirstAttunement));
        addSlot(new WarpPlateAttunementSlot(container, 1, 80, 17, this::isCompletedFirstAttunement));
        addSlot(new WarpPlateAttunementSlot(container, 2, 108, 45, this::isCompletedFirstAttunement));
        addSlot(new WarpPlateAttunementSlot(container, 3, 80, 73, this::isCompletedFirstAttunement));
        addSlot(new WarpPlateAttunementSlot(container, 4, 52, 45, this::isCompletedFirstAttunement));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        for (int j = 0; j < 9; ++j) {
            addSlot(new Slot(playerInventory, j, 8 + j * 18, 162));
        }

        addDataSlots(containerData);
    }

    private boolean isCompletedFirstAttunement() {
        return containerData.get(2) == 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    public float getAttunementProgress() {
        return containerData.get(0) / (float) containerData.get(1);
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

    public IWaystone getWaystone() {
        return waystone;
    }
}
