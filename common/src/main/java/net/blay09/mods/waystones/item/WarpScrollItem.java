package net.blay09.mods.waystones.item;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.trait.IResetUseOnDamage;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WaystoneImpl;
import net.blay09.mods.waystones.menu.ModMenus;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WarpScrollItem extends ScrollItemBase implements IResetUseOnDamage {

    public WarpScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity entity) {
        return WaystonesConfig.getActive().general.scrollUseTime;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof ServerPlayer player) {
            final var waystones = PlayerWaystoneManager.getTargetsForItem(player, itemStack);
            PlayerWaystoneManager.ensureSortingIndex(player, waystones);
            Balm.getNetworking().openGui(((ServerPlayer) entity), new BalmMenuProvider<Collection<Waystone>>() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("container.waystones.waystone_selection");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
                    return new WaystoneSelectionMenu(ModMenus.warpScrollSelection.get(), null, windowId, waystones, Collections.emptySet())
                            .setPostTeleportHandler(context -> itemStack.consume(1, inventory.player));
                }


                @Override
                public Collection<Waystone> getScreenOpeningData(ServerPlayer serverPlayer) {
                    return waystones;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, Collection<Waystone>> getScreenStreamCodec() {
                    return WaystoneImpl.LIST_STREAM_CODEC;
                }
            });
        }
        return itemStack;
    }

}
