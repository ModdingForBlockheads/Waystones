package net.blay09.mods.waystones.block.entity;

import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.waystones.core.Waystone;
import net.blay09.mods.waystones.menu.ModMenus;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.blay09.mods.waystones.menu.WaystoneSettingsMenu;
import net.blay09.mods.waystones.core.WarpMode;
import net.blay09.mods.waystones.core.WaystoneTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class WaystoneBlockEntity extends WaystoneBlockEntityBase {

    public WaystoneBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.waystone.get(), blockPos, blockState);
    }

    @Override
    protected ResourceLocation getWaystoneType() {
        return WaystoneTypes.WAYSTONE;
    }

    @Override
    public BalmMenuProvider getMenuProvider() {
        return new BalmMenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.waystones.waystone_selection");
            }

            @Override
            public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                return WaystoneSelectionMenu.createWaystoneSelection(i, playerEntity, WarpMode.WAYSTONE_TO_WAYSTONE, getWaystone());
            }

            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                buf.writeByte(WarpMode.WAYSTONE_TO_WAYSTONE.ordinal());
                Waystone.write(buf, getWaystone());
            }
        };
    }

    @Override
    public BalmMenuProvider getSettingsMenuProvider() {
        return new BalmMenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.waystones.waystone_settings");
            }

            @Override
            public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                return new WaystoneSettingsMenu(ModMenus.waystoneSettings.get(), getWaystone(), i);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                Waystone.write(buf, getWaystone());
            }
        };
    }

}
