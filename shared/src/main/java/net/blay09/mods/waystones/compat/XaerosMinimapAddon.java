package net.blay09.mods.waystones.compat;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.KnownWaystonesEvent;
import net.blay09.mods.waystones.config.WaystonesConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import xaero.common.XaeroMinimapSession;
import xaero.common.core.IXaeroMinimapClientPlayNetHandler;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.common.settings.ModSettings;
import xaero.minimap.XaeroMinimap;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class XaerosMinimapAddon {
  public static String invalid = "invalid";
  private static boolean invalidWarned = false;
  private static String setName;
  private static String setNameInitiated;

  public static void initialize() {
    if (WaystonesConfig.getActive().displayWaystonesOnXaeros()) {
      Balm.getEvents().onEvent(KnownWaystonesEvent.class, XaerosMinimapAddon::onKnownWaystones);
    }
  }

  public static WaypointsManager getWaypointsManager() {
    Minecraft mc = Minecraft.getInstance();
    XaeroMinimapSession session = ((IXaeroMinimapClientPlayNetHandler) mc.player.connection)
        .getXaero_minimapSession();
    return session.getWaypointsManager();
  }

  public static void makeWaypoint(BlockPos pos, String name, WaypointSet set) {
    if (name.equals(invalid)) {
      // inform player about bad waystone name once per session
      if (!invalidWarned) {
        Minecraft mc = Minecraft.getInstance();
        Player entity = mc.player;
        TranslatableComponent chatComponent = new TranslatableComponent("chat.waystones.invalid_name_xaeros");
        chatComponent.withStyle(ChatFormatting.DARK_RED);
        entity.displayClientMessage(chatComponent, true);
        invalidWarned = true;
      }
      return;
    }
    Waypoint instant = new Waypoint(pos.getX(), pos.getY() + 2, pos.getZ(), name,
        name.substring(0, 1), (int) (Math.random() * (double) ModSettings.ENCHANT_COLORS.length), 0, false);
    set.getList().add(instant);
  }

  public static void addKnownWaypoints(KnownWaystonesEvent event) {
    WaypointsManager wm = getWaypointsManager();
    // addSet resets any existing set with setName
    wm.getCurrentWorld().addSet(setName);
    // prevent selected set from toggling back to Waystones on each event
    if (setNameInitiated != setName && WaystonesConfig.getActive().waystonesSetDefaultXaeros()) {
      setNameInitiated = setName;
      wm.getCurrentWorld().setCurrent(setName);
    }

    WaypointSet set = wm.getCurrentWorld().getSets().get(setName);
    // add waystones to set
    for (IWaystone waystone : event.getWaystones()) {
      try {
        makeWaypoint(waystone.getPos(), waystone.hasName() ? waystone.getName() : "TEMP NAME", set);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {
      XaeroMinimap.instance.getSettings().saveWaypoints(wm.getCurrentWorld());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void onKnownWaystones(KnownWaystonesEvent event) {
    if (!Compat.isXaerosMinimapInstalled) {
      return;
    }
    setName = WaystonesConfig.getActive().waystonesSetNameXaeros();
    WaypointsManager wm = getWaypointsManager();
    // if world is not loaded yet, wait
    int timeout = wm.getCurrentWorld() == null ? 500 : 0;
    CompletableFuture.delayedExecutor(timeout, TimeUnit.MILLISECONDS).execute(() -> {
      // if world is still not loaded, try again
      if (wm.getCurrentWorld() == null) {
        onKnownWaystones(event);
      } else {
        addKnownWaypoints(event);
      }
    });
  }
}
