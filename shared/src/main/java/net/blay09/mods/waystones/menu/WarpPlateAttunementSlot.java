package net.blay09.mods.waystones.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class WarpPlateAttunementSlot extends Slot {
    private final Supplier<Boolean> mayPickupFunc;

    public WarpPlateAttunementSlot(Container container, int slot, int x, int y, Supplier<Boolean> mayPickupFunc) {
        super(container, slot, x, y);
        this.mayPickupFunc = mayPickupFunc;
    }

    @Override
    public boolean mayPickup(Player player) {
        return mayPickupFunc.get() && super.mayPickup(player);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (this.getContainerSlot() == 0) {
            return 1;
        }
        return stack.getMaxStackSize();
    }
}
