package net.blay09.mods.waystones.menu;

import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.menu.BalmMenus;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.core.WarpMode;
import net.blay09.mods.waystones.core.Waystone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ModMenus {
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> waystoneSelection;
    public static DeferredObject<MenuType<WaystoneSelectionMenu>> sharestoneSelection;
    public static DeferredObject<MenuType<WarpPlateContainer>> warpPlate;
    public static DeferredObject<MenuType<WaystoneSettingsMenu>> waystoneSettings;

    public static void initialize(BalmMenus menus) {
        waystoneSelection = menus.registerMenu(id("waystone_selection"), (syncId, inventory, buf) -> {
            final var warpMode = WarpMode.values[buf.readByte()];
            IWaystone fromWaystone = null;
            if (warpMode == WarpMode.WAYSTONE_TO_WAYSTONE) {
                fromWaystone = Waystone.read(buf);
            }

            return WaystoneSelectionMenu.createWaystoneSelection(syncId, inventory.player, warpMode, fromWaystone);
        });

        sharestoneSelection = menus.registerMenu(id("sharestone_selection"), (syncId, inventory, buf) -> {
            final var fromWaystone = Waystone.read(buf);
            final var count = buf.readShort();
            final var waystones = new ArrayList<IWaystone>(count);
            for (int i = 0; i < count; i++) {
                waystones.add(Waystone.read(buf));
            }
            return new WaystoneSelectionMenu(ModMenus.sharestoneSelection.get(), WarpMode.SHARESTONE_TO_SHARESTONE, fromWaystone, syncId, waystones);
        });

        warpPlate = menus.registerMenu(id("warp_plate"), (windowId, inv, data) -> {
            final var waystone = Waystone.read(data);
            return new WarpPlateContainer(windowId, inv, waystone);
        });

        waystoneSettings = menus.registerMenu(id("waystone_settings"), (windowId, inv, data) -> {
            final var waystone = Waystone.read(data);
            return new WaystoneSettingsMenu(waystoneSettings.get(), waystone, windowId);
        });
    }

    @NotNull
    private static ResourceLocation id(String name) {
        return new ResourceLocation(Waystones.MOD_ID, name);
    }

}
