package net.blay09.mods.waystones.config;

import net.blay09.mods.balm.api.config.*;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.api.WaystoneVisibility;
import net.blay09.mods.waystones.worldgen.namegen.NameGenerationMode;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

@Config(Waystones.MOD_ID)
public class WaystonesConfigData implements BalmConfigData {

    public enum TransportMobs {
        ENABLED,
        SAME_DIMENSION,
        DISABLED
    }

    public enum VillageWaystoneGeneration {
        DISABLED,
        REGULAR,
        FREQUENT
    }

    public General general = new General();
    public Teleports teleports = new Teleports();
    public InventoryButton inventoryButton = new InventoryButton();
    public WorldGen worldGen = new WorldGen();
    public Client client = new Client();
    public Compatibility compatibility = new Compatibility();

    public static class General {

        @Synced
        @Comment("List of waystone origins that should prevent others from editing. PLAYER is special in that it allows only edits by the owner of the waystone.")
        @ExpectedType(WaystoneOrigin.class)
        public Set<WaystoneOrigin> restrictedWaystones = Set.of(WaystoneOrigin.PLAYER);

        @Synced
        @Comment("Set to \"GLOBAL\" and change allowedVisibilities to have newly placed or found waystones be global by default.")
        public WaystoneVisibility defaultVisibility = WaystoneVisibility.ACTIVATION;

        @Synced
        @Comment("Add \"GLOBAL\" to allow every player to create global waystones.")
        @ExpectedType(WaystoneVisibility.class)
        public Set<WaystoneVisibility> allowedVisibilities = Set.of();

        @Synced
        @Comment("The time in ticks that it takes to use a warp stone. This is the charge-up time when holding right-click.")
        public int warpStoneUseTime = 32;

        @Synced
        @Comment("The time in ticks that it takes to use a warp plate. This is the time the player has to stand on top for.")
        public int warpPlateUseTime = 15;

        @Synced
        @Comment("The time in ticks it takes to use a scroll. This is the charge-up time when holding right-click.")
        public int scrollUseTime = 32;
    }

    public static class Teleports {
        @Synced
        @Comment("Set to false to simply disable all xp costs. See warpRequirements for more fine-grained control.")
        public boolean enableCosts = true;

        @Synced
        @Comment("Set to false to simply disable all cooldowns. See warpRequirements for more fine-grained control.")
        public boolean enableCooldowns = true;

        @Synced
        @ExpectedType(String.class)
        @Comment("List of warp requirements with comma-separated parameters in parentheses. Conditions can be defined as comma-separated list in square brackets. Will be applied in order.")
        public List<String> warpRequirements = List.of(
                "[is_not_interdimensional] scaled_add_xp_cost(distance, 0.01)",
                "[is_interdimensional] add_xp_cost(27)",
                "[source_is_warp_plate] multiply_xp_cost(0)",
                "[target_is_global] multiply_xp_cost(0)",
                "min_xp_cost(0)",
                "max_xp_cost(27)",
                "[source_is_inventory_button] add_cooldown(inventory_button, 300)");

        @Synced
        @Comment("Set to ENABLED to have nearby pets teleport with you. Set to SAME_DIMENSION to have nearby pets teleport with you only if you're not changing dimensions. Set to DISABLED to disable.")
        public TransportMobs transportPets = TransportMobs.DISABLED;

        @Synced
        @Comment("Set to ENABLED to have leashed mobs teleport with you. Set to SAME_DIMENSION to have leashed mobs teleport with you only if you're not changing dimensions. Set to DISABLED to disable.")
        public TransportMobs transportLeashed = TransportMobs.ENABLED;

        @Comment("List of entities that cannot be teleported, either as pet, leashed, or on warp plates.")
        @ExpectedType(ResourceLocation.class)
        public Set<ResourceLocation> entityDenyList = Set.of(ResourceLocation.withDefaultNamespace("wither"));
    }

    public static class InventoryButton {
        @Synced
        @Comment("Set to 'NONE' for no inventory button. Set to 'NEAREST' for an inventory button that teleports to the nearest waystone. Set to 'ANY' for an inventory button that opens the waystone selection menu. Set to a waystone name for an inventory button that teleports to a specifically named waystone.")
        public String inventoryButton = "";

        @Comment("The x position of the inventory button in the inventory.")
        @Synced
        public int inventoryButtonX = 58;

        @Comment("The y position of the inventory button in the inventory.")
        @Synced
        public int inventoryButtonY = 60;

        @Comment("The y position of the inventory button in the creative menu.")
        @Synced
        public int creativeInventoryButtonX = 88;

        @Comment("The y position of the inventory button in the creative menu.")
        @Synced
        public int creativeInventoryButtonY = 33;
    }

    public static class WorldGen {
        @Comment("Set to 'DEFAULT' to only generate the normally textured waystones. Set to 'MOSSY' or 'SANDY' to generate all as that variant. Set to 'BIOME' to make the style depend on the biome it is generated in.")
        public WorldGenStyle wildWaystoneStyle = WorldGenStyle.BIOME;

        @Comment("Approximate chunk distance between wild waystones being generated. Set to 0 to disable generation.")
        public int chunksBetweenWildWaystones = 25;

        @Comment("List of dimensions that wild waystones are allowed to spawn in. If left empty, all dimensions except those in wildWaystonesDimensionDenyList are used.")
        @ExpectedType(ResourceLocation.class)
        public Set<ResourceLocation> wildWaystonesDimensionAllowList = Set.of(ResourceLocation.withDefaultNamespace("overworld"), ResourceLocation.withDefaultNamespace("the_nether"), ResourceLocation.withDefaultNamespace("the_end"));

        @Comment("List of dimensions that wild waystones are not allowed to spawn in. Only used if wildWaystonesDimensionAllowList is empty.")
        @ExpectedType(ResourceLocation.class)
        public Set<ResourceLocation> wildWaystonesDimensionDenyList = Set.of();

        @Comment("Set to 'PRESET_FIRST' to first use names from the nameGenerationPresets. Set to 'PRESET_ONLY' to use only those custom names. Set to 'MIXED' to have some waystones use custom names, and others random names.")
        public NameGenerationMode nameGenerationMode = NameGenerationMode.PRESET_FIRST;

        @Comment("The template to use when generating new names. Supported placeholders are {Biome} (english biome name) and {MrPork} (the default name generator).")
        public String nameGenerationTemplate = "{MrPork}";

        @Comment("These names will be used for the PRESET name generation mode. See the nameGenerationMode option for more info.")
        @ExpectedType(String.class)
        public List<String> nameGenerationPresets = List.of();

        @Comment("Set to REGULAR to have waystones spawn in some villages. Set to FREQUENT to have waystones spawn in most villages. Set to DISABLED to disable waystone generation in villages. Waystones will only spawn in vanilla or supported villages.")
        public VillageWaystoneGeneration spawnInVillages = VillageWaystoneGeneration.REGULAR;
    }

    public static class Compatibility {
        @Comment("If enabled, JourneyMap waypoints will be created for each activated waystone.")
        public boolean journeyMap = true;

        @Comment("If enabled, JourneyMap waypoints will only be created if the mod 'JourneyMap Integration' is not installed")
        public boolean preferJourneyMapIntegrationMod = true;

        @Comment("If enabled, Waystones will add markers for waystones and sharestones to BlueMap.")
        public boolean blueMap = true;

        @Comment("If enabled, Waystones will add markers for waystones and sharestones to Dynmap.")
        public boolean dynmap = true;
    }

    public static class Client {
        @Comment("If enabled, the text overlay on waystones will no longer always render at full brightness.")
        public boolean disableTextGlow = false;
    }

    public InventoryButtonMode getInventoryButtonMode() {
        return new InventoryButtonMode(inventoryButton.inventoryButton);
    }
}
