package net.blay09.mods.waystones.menu;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.blay09.mods.waystones.core.WaystoneImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WaystoneEditMenu extends AbstractContainerMenu {

    public record Data(BlockPos pos, Waystone waystone, int modifierCount, Optional<Component> error) {
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneEditMenu.Data> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.cast(),
            WaystoneEditMenu.Data::pos,
            WaystoneImpl.STREAM_CODEC,
            WaystoneEditMenu.Data::waystone,
            ByteBufCodecs.INT,
            WaystoneEditMenu.Data::modifierCount,
            ComponentSerialization.OPTIONAL_STREAM_CODEC,
            WaystoneEditMenu.Data::error,
            WaystoneEditMenu.Data::new);

    private final Waystone waystone;
    private final WaystoneBlockEntityBase blockEntity;
    private final int modifierCount;
    private final Component error;

    public WaystoneEditMenu(int windowId, Waystone waystone, WaystoneBlockEntityBase blockEntity, Inventory playerInventory, int modifierCount, Component error) {
        super(ModMenus.waystoneSettings.get(), windowId);
        this.waystone = waystone;
        this.blockEntity = blockEntity;
        this.modifierCount = modifierCount;
        this.error = error;
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

    public int getModifierCount() {
        return modifierCount;
    }

    public boolean canEdit() {
        return error == null;
    }

    @Nullable
    public Component getError() {
        return error;
    }
}
