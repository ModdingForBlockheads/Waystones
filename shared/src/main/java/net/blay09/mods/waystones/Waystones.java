package net.blay09.mods.waystones;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.block.ModBlocks;
import net.blay09.mods.waystones.block.entity.ModBlockEntities;
import net.blay09.mods.waystones.command.ModCommands;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.cost.CostRegistry;
import net.blay09.mods.waystones.handler.ModEventHandlers;
import net.blay09.mods.waystones.item.ModItems;
import net.blay09.mods.waystones.menu.ModMenus;
import net.blay09.mods.waystones.network.ModNetworking;
import net.blay09.mods.waystones.recipe.ModRecipes;
import net.blay09.mods.waystones.stats.ModStats;
import net.blay09.mods.waystones.worldgen.ModWorldGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Waystones {

    public static final Logger logger = LoggerFactory.getLogger(Waystones.class);

    public static final String MOD_ID = "waystones";

    public static void initialize() {
        WaystonesAPI.__internalMethods = new InternalMethodsImpl();
        CostRegistry.registerDefaults();

        WaystonesConfig.initialize();
        ModStats.initialize();
        ModEventHandlers.initialize();
        ModNetworking.initialize(Balm.getNetworking());
        ModBlocks.initialize(Balm.getBlocks());
        ModBlockEntities.initialize(Balm.getBlockEntities());
        ModItems.initialize(Balm.getItems());
        ModMenus.initialize(Balm.getMenus());
        ModWorldGen.initialize(Balm.getWorldGen());
        ModRecipes.initialize(Balm.getRecipes());
        ModCommands.initialize(Balm.getCommands());

        if (WaystonesConfig.getActive().compatibility.blueMapIntegration) {
            Balm.initializeIfLoaded("bluemap", "net.blay09.mods.waystones.compat.BlueMapIntegration");
        }

        if (WaystonesConfig.getActive().compatibility.dynmapIntegration) {
            Balm.initializeIfLoaded("dynmap", "net.blay09.mods.waystones.compat.DynmapIntegration");
        }
    }
}
