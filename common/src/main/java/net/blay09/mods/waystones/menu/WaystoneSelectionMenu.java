package net.blay09.mods.waystones.menu;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.core.WaystoneImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public class WaystoneSelectionMenu extends AbstractContainerMenu {

    public record Data(Waystone fromWaystone, Collection<Waystone> waystones) {
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
            WaystoneImpl.STREAM_CODEC,
            Data::fromWaystone,
            WaystoneImpl.LIST_STREAM_CODEC,
            Data::waystones,
            Data::new);

    private final Waystone fromWaystone;
    private final Collection<Waystone> waystones;
    private final Set<ResourceLocation> flags;
    private Consumer<WaystoneTeleportContext> postTeleportHandler = it -> {};
    private ItemStack warpItem = ItemStack.EMPTY;

    public WaystoneSelectionMenu(MenuType<WaystoneSelectionMenu> type, @Nullable Waystone fromWaystone, int windowId, Collection<Waystone> waystones, Set<ResourceLocation> flags) {
        super(type, windowId);
        this.fromWaystone = fromWaystone;
        this.waystones = waystones;
        this.flags = flags;
    }

    public WaystoneSelectionMenu withWarpItem(ItemStack warpItem) {
        this.warpItem = warpItem;
        return this;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (fromWaystone != null) {
            BlockPos pos = fromWaystone.getPos();
            return player.distanceToSqr((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= 64;
        }

        return true;
    }

    @Nullable
    public Waystone getWaystoneFrom() {
        return fromWaystone;
    }

    public ItemStack getWarpItem() {
        return warpItem;
    }

    public Collection<Waystone> getWaystones() {
        return waystones;
    }

    public Set<ResourceLocation> getFlags() {
        return flags;
    }

    public Consumer<WaystoneTeleportContext> getPostTeleportHandler() {
        return postTeleportHandler;
    }

    public WaystoneSelectionMenu setPostTeleportHandler(Consumer<WaystoneTeleportContext> postTeleportHandler) {
        this.postTeleportHandler = postTeleportHandler;
        return this;
    }
}
