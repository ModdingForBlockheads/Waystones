package net.blay09.mods.waystones.handler;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.LivingDamageEvent;
import net.blay09.mods.balm.api.event.PlayerLoginEvent;
import net.blay09.mods.balm.api.event.UseBlockEvent;
import net.blay09.mods.waystones.api.event.WaystoneActivatedEvent;

public class ModEventHandlers {
    public static void initialize() {
        Balm.getEvents().onEvent(PlayerLoginEvent.class, LoginHandler::onPlayerLogin);
        Balm.getEvents().onEvent(LivingDamageEvent.class, WarpDamageResetHandler::onDamage);
        Balm.getEvents().onEvent(WaystoneActivatedEvent.class, WaystoneActivationStatHandler::onWaystoneActivated);
        Balm.getEvents().onEvent(UseBlockEvent.class, WaystoneDebugHandler::onWaystoneUsed);
    }
}
