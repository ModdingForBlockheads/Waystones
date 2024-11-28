package net.blay09.mods.waystones.item;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.*;
import net.blay09.mods.waystones.api.trait.IAttunementItem;
import net.blay09.mods.waystones.api.trait.IFOVOnUse;
import net.blay09.mods.waystones.api.trait.IResetUseOnDamage;
import net.blay09.mods.waystones.component.ModComponents;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.WaystoneProxy;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BoundScrollItem extends ScrollItemBase implements IResetUseOnDamage, IFOVOnUse, IAttunementItem {

    public BoundScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity entity) {
        return WaystonesConfig.getActive().general.scrollUseTime;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof ServerPlayer player) {
            final var boundTo = getWaystoneAttunedTo(player.getServer(), player, stack);
            boundTo.ifPresent(targetWaystone -> WaystonesAPI.createDefaultTeleportContext(player, targetWaystone, it -> it.setWarpItem(stack))
                    .mapLeft(WaystonesAPI::tryTeleport)
                    .ifLeft(it -> stack.consume(1, player))
            );
        }

        return stack;
    }

    @Override
    public Optional<Waystone> getWaystoneAttunedTo(MinecraftServer server, Player player, ItemStack itemStack) {
        return Optional.ofNullable(itemStack.get(ModComponents.attunement.get())).map(attunement -> new WaystoneProxy(server, attunement));
    }

    @Override
    public void setWaystoneAttunedTo(ItemStack itemStack, @Nullable Waystone waystone) {
        if (waystone != null) {
            itemStack.set(ModComponents.attunement.get(), waystone.getWaystoneUid());
        } else {
            itemStack.remove(ModComponents.attunement.get());
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        final var player = Balm.getProxy().getClientPlayer();
        if (player == null) {
            return;
        }

        final var boundTo = getWaystoneAttunedTo(player.getServer(), player, itemStack);
        boundTo.ifPresent(it -> {
            final var boundToValueComponent = it.getName().copy().withStyle(ChatFormatting.AQUA);
            list.add(Component.translatable("tooltip.waystones.bound_to", boundToValueComponent).withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}
