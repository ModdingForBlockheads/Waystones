package net.blay09.mods.waystones.menu;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.menu.BalmMenuFactory;
import net.blay09.mods.balm.api.menu.BalmMenus;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.TeleportFlags;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.core.WaystoneImpl;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ModMenus {

    public record ItemInitiatedWaystoneMenuData(Collection<Waystone> waystones, ItemStack itemStack) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemInitiatedWaystoneMenuData> STREAM_CODEC = StreamCodec.composite(
                WaystoneImpl.LIST_STREAM_CODEC,
                ItemInitiatedWaystoneMenuData::waystones,
                ItemStack.STREAM_CODEC,
                ItemInitiatedWaystoneMenuData::itemStack,
                ItemInitiatedWaystoneMenuData::new
        );
    }

    private static final BalmMenus menus = Balm.getMenus();
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> waystoneSelection = menus.registerMenu(id("waystone_selection"),
            new BalmMenuFactory<WaystoneSelectionMenu, WaystoneSelectionMenu.Data>() {
                @Override
                public WaystoneSelectionMenu create(int windowId, Inventory inventory, WaystoneSelectionMenu.Data data) {
                    return new WaystoneSelectionMenu(ModMenus.waystoneSelection.get(),
                            data.fromWaystone(),
                            windowId,
                            data.waystones(),
                            Collections.emptySet());
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, WaystoneSelectionMenu.Data> getStreamCodec() {
                    return WaystoneSelectionMenu.STREAM_CODEC;
                }
            });
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> warpScrollSelection = menus.registerMenu(id("warp_scroll_selection"),
            new BalmMenuFactory<WaystoneSelectionMenu, ItemInitiatedWaystoneMenuData>() {
                @Override
                public WaystoneSelectionMenu create(int windowId, Inventory inventory, ItemInitiatedWaystoneMenuData data) {
                    return new WaystoneSelectionMenu(ModMenus.warpScrollSelection.get(), null, windowId, data.waystones(), Collections.emptySet())
                            .withWarpItem(data.itemStack());
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, ItemInitiatedWaystoneMenuData> getStreamCodec() {
                    return ItemInitiatedWaystoneMenuData.STREAM_CODEC;
                }
            });
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> warpStoneSelection = menus.registerMenu(id("warp_stone_selection"),
            new BalmMenuFactory<WaystoneSelectionMenu, ItemInitiatedWaystoneMenuData>() {
                @Override
                public WaystoneSelectionMenu create(int windowId, Inventory inventory, ItemInitiatedWaystoneMenuData data) {
                    return new WaystoneSelectionMenu(ModMenus.warpStoneSelection.get(), null, windowId, data.waystones(), Collections.emptySet())
                            .withWarpItem(data.itemStack());
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, ItemInitiatedWaystoneMenuData> getStreamCodec() {
                    return ItemInitiatedWaystoneMenuData.STREAM_CODEC;
                }
            });
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> portstoneSelection = menus.registerMenu(id("portstone_selection"),
            new BalmMenuFactory<WaystoneSelectionMenu, Collection<Waystone>>() {
                @Override
                public WaystoneSelectionMenu create(int windowId, Inventory inventory, Collection<Waystone> waystones) {
                    return new WaystoneSelectionMenu(ModMenus.portstoneSelection.get(), null, windowId, waystones, Collections.emptySet());
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, Collection<Waystone>> getStreamCodec() {
                    return WaystoneImpl.LIST_STREAM_CODEC;
                }
            });
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> inventorySelection = menus.registerMenu(id("inventory_selection"),
            new BalmMenuFactory<WaystoneSelectionMenu, Collection<Waystone>>() {
                @Override
                public WaystoneSelectionMenu create(int windowId, Inventory inventory, Collection<Waystone> waystones) {
                    return new WaystoneSelectionMenu(ModMenus.inventorySelection.get(), null, windowId, waystones, Set.of(TeleportFlags.INVENTORY_BUTTON));
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, Collection<Waystone>> getStreamCodec() {
                    return WaystoneImpl.LIST_STREAM_CODEC;
                }
            });
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> adminSelection = menus.registerMenu(id("admin_selection"),
            new BalmMenuFactory<WaystoneSelectionMenu, Collection<Waystone>>() {
                @Override
                public WaystoneSelectionMenu create(int windowId, Inventory inventory, Collection<Waystone> waystones) {
                    return new WaystoneSelectionMenu(ModMenus.adminSelection.get(), null, windowId, waystones, Set.of(TeleportFlags.ADMIN));
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, Collection<Waystone>> getStreamCodec() {
                    return WaystoneImpl.LIST_STREAM_CODEC;
                }
            });
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> sharestoneSelection = menus.registerMenu(id("sharestone_selection"),
            new BalmMenuFactory<WaystoneSelectionMenu, WaystoneSelectionMenu.Data>() {
                @Override
                public WaystoneSelectionMenu create(int windowId, Inventory inventory, WaystoneSelectionMenu.Data data) {
                    return new WaystoneSelectionMenu(ModMenus.sharestoneSelection.get(),
                            data.fromWaystone(),
                            windowId,
                            data.waystones(),
                            Collections.emptySet());
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, WaystoneSelectionMenu.Data> getStreamCodec() {
                    return WaystoneSelectionMenu.STREAM_CODEC;
                }
            });
    public static DeferredObject<MenuType<WaystoneModifierMenu>> waystoneModifiers = menus.registerMenu(id("waystone_modifiers"),
            new BalmMenuFactory<WaystoneModifierMenu, Waystone>() {
                @Override
                public WaystoneModifierMenu create(int windowId, Inventory inventory, Waystone waystone) {
                    return new WaystoneModifierMenu(windowId, inventory, waystone);
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, Waystone> getStreamCodec() {
                    return WaystoneImpl.STREAM_CODEC;
                }
            });
    public static DeferredObject<MenuType<WaystoneEditMenu>> waystoneSettings = menus.registerMenu(id("waystone"),
            new BalmMenuFactory<WaystoneEditMenu, WaystoneEditMenu.Data>() {
                @Override
                public WaystoneEditMenu create(int windowId, Inventory inventory, WaystoneEditMenu.Data data) {
                    return new WaystoneEditMenu(windowId, data.waystone(), data.modifierCount(), data.error().orElse(null));
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, WaystoneEditMenu.Data> getStreamCodec() {
                    return WaystoneEditMenu.STREAM_CODEC;
                }
            });

    public static void initialize() {
    }

    @NotNull
    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, name);
    }

}
