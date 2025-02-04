package net.blay09.mods.waystones.block.entity;

import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.menu.ModMenus;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.Optional;

public class WaystoneBlockEntity extends WaystoneBlockEntityBase {

    public WaystoneBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.waystone.get(), blockPos, blockState);
    }

    @Override
    protected ResourceLocation getWaystoneType() {
        return WaystoneTypes.WAYSTONE;
    }

    @Override
    public Component getName() {
        return Component.translatable("container.waystones.waystone");
    }

    @Override
    public Optional<MenuProvider> getSelectionMenuProvider() {
        return Optional.of(new BalmMenuProvider<WaystoneSelectionMenu.Data>() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.waystones.waystone_selection");
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                final var waystones = PlayerWaystoneManager.getTargetsForWaystone(player, getWaystone());
                PlayerWaystoneManager.ensureSortingIndex(player, waystones);
                return new WaystoneSelectionMenu(ModMenus.waystoneSelection.get(), getWaystone(), windowId, waystones, Collections.emptySet());
            }

            @Override
            public WaystoneSelectionMenu.Data getScreenOpeningData(ServerPlayer serverPlayer) {
                return new WaystoneSelectionMenu.Data(getWaystone(), PlayerWaystoneManager.getTargetsForWaystone(serverPlayer, getWaystone()));
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, WaystoneSelectionMenu.Data> getScreenStreamCodec() {
                return WaystoneSelectionMenu.STREAM_CODEC;
            }
        });
    }

}
