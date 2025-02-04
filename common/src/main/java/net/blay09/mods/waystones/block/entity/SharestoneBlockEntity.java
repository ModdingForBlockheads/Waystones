package net.blay09.mods.waystones.block.entity;

import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.block.SharestoneBlock;
import net.blay09.mods.waystones.core.*;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.menu.ModMenus;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;

public class SharestoneBlockEntity extends WaystoneBlockEntityBase {

    public SharestoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.sharestone.get(), pos, state);
    }

    @Override
    protected ResourceLocation getWaystoneType() {
        return WaystoneTypes.getSharestone(((SharestoneBlock) getBlockState().getBlock()).getColor())
                .orElse(WaystoneTypes.WAYSTONE); // fallback to regular waystone if invalid
    }

    @Override
    public void initializeWaystone(ServerLevelAccessor world, @Nullable LivingEntity player, WaystoneOrigin origin) {
        super.initializeWaystone(world, player, origin);

        WaystoneSyncManager.sendWaystoneUpdateToAll(world.getServer(), getWaystone());
    }

    @Override
    public Component getName() {
        return Component.translatable("container.waystones.sharestone");
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
                final var fromWaystone = getWaystone();
                final var waystones = PlayerWaystoneManager.getTargetsForWaystone(player, fromWaystone);
                PlayerWaystoneManager.ensureSortingIndex(player, waystones);
                return new WaystoneSelectionMenu(ModMenus.sharestoneSelection.get(), fromWaystone, windowId, waystones, Collections.emptySet());
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
