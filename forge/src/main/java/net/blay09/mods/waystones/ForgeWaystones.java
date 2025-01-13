package net.blay09.mods.waystones;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.forge.ForgeLoadContext;
import net.blay09.mods.waystones.client.WaystonesClient;
import net.blay09.mods.waystones.compat.Compat;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

@Mod(Waystones.MOD_ID)
public class ForgeWaystones {

    private static final Logger logger = LoggerFactory.getLogger(ForgeWaystones.class);

    public ForgeWaystones(FMLJavaModLoadingContext context) {
        final var loadContext = new ForgeLoadContext(context.getModEventBus());
        Balm.initialize(Waystones.MOD_ID, loadContext, Waystones::initialize);
        if (FMLEnvironment.dist.isClient()) {
            BalmClient.initialize(Waystones.MOD_ID, loadContext, WaystonesClient::initialize);
        }

        Balm.initializeIfLoaded(Compat.THEONEPROBE, "net.blay09.mods.waystones.compat.TheOneProbeIntegration");

        // TODO would be nice if we could use Balm.initializeIfLoaded here, but it might run too late at the moment)
        if (Balm.isModLoaded("repurposed_structures")) {
            try {
                Class.forName("net.blay09.mods.waystones.compat.RepurposedStructuresIntegration").getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException e) {
                logger.error("Failed to load Repurposed Structures integration", e);
            }
        }
    }
}
