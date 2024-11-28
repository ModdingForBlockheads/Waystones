package net.blay09.mods.waystones.worldgen;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.event.server.ServerReloadedEvent;
import net.blay09.mods.balm.api.event.server.ServerStartedEvent;
import net.blay09.mods.balm.api.world.BalmWorldGen;
import net.blay09.mods.balm.api.world.BiomePredicate;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.block.ModBlocks;
import net.blay09.mods.waystones.block.WaystoneBlock;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.config.WaystonesConfigData;
import net.blay09.mods.waystones.config.WorldGenStyle;
import net.blay09.mods.waystones.mixin.StructureTemplatePoolAccessor;
import net.blay09.mods.waystones.tag.ModBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModWorldGen {
    private static final ResourceLocation waystone = ResourceLocation.fromNamespaceAndPath("waystones", "waystone");
    private static final ResourceLocation mossyWaystone = ResourceLocation.fromNamespaceAndPath("waystones", "mossy_waystone");
    private static final ResourceLocation sandyWaystone = ResourceLocation.fromNamespaceAndPath("waystones", "sandy_waystone");
    private static final ResourceLocation blackstoneWaystone = ResourceLocation.fromNamespaceAndPath("waystones", "blackstone_waystone");
    private static final ResourceLocation deepslateWaystone = ResourceLocation.fromNamespaceAndPath("waystones", "deepslate_waystone");
    private static final ResourceLocation endStoneWaystone = ResourceLocation.fromNamespaceAndPath("waystones", "end_stone_waystone");
    private static final ResourceLocation villageWaystoneStructure = ResourceLocation.fromNamespaceAndPath("waystones", "village/common/waystone");
    private static final ResourceLocation desertVillageWaystoneStructure = ResourceLocation.fromNamespaceAndPath("waystones", "village/desert/waystone");
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST,
            ResourceLocation.fromNamespaceAndPath("minecraft", "empty"));

    public static DeferredObject<PlacementModifierType<WaystonePlacement>> waystonePlacement;

    public static void initialize(BalmWorldGen worldGen) {
        worldGen.registerFeature(waystone, () -> new WaystoneFeature(NoneFeatureConfiguration.CODEC, ModBlocks.waystone.defaultBlockState()));
        worldGen.registerFeature(mossyWaystone, () -> new WaystoneFeature(NoneFeatureConfiguration.CODEC, ModBlocks.mossyWaystone.defaultBlockState()));
        worldGen.registerFeature(sandyWaystone, () -> new WaystoneFeature(NoneFeatureConfiguration.CODEC, ModBlocks.sandyWaystone.defaultBlockState()));
        worldGen.registerFeature(blackstoneWaystone,
                () -> new WaystoneFeature(NoneFeatureConfiguration.CODEC, ModBlocks.blackstoneWaystone.defaultBlockState()));
        worldGen.registerFeature(deepslateWaystone, () -> new WaystoneFeature(NoneFeatureConfiguration.CODEC, ModBlocks.deepslateWaystone.defaultBlockState()));
        worldGen.registerFeature(endStoneWaystone, () -> new WaystoneFeature(NoneFeatureConfiguration.CODEC, ModBlocks.endStoneWaystone.defaultBlockState()));

        waystonePlacement = worldGen.registerPlacementModifier(id("waystone"), () -> () -> WaystonePlacement.CODEC);

        worldGen.addFeatureToBiomes(matchesTag(ModBiomeTags.HAS_STRUCTURE_SANDY_WAYSTONE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                getWaystoneFeature(WorldGenStyle.SANDY));
        worldGen.addFeatureToBiomes(matchesTag(ModBiomeTags.HAS_STRUCTURE_MOSSY_WAYSTONE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                getWaystoneFeature(WorldGenStyle.MOSSY));
        worldGen.addFeatureToBiomes(matchesTag(ModBiomeTags.HAS_STRUCTURE_BLACKSTONE_WAYSTONE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                getWaystoneFeature(WorldGenStyle.BLACKSTONE));
        worldGen.addFeatureToBiomes(matchesTag(ModBiomeTags.HAS_STRUCTURE_END_STONE_WAYSTONE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                getWaystoneFeature(WorldGenStyle.END_STONE));
        worldGen.addFeatureToBiomes(matchesTag(ModBiomeTags.HAS_STRUCTURE_WAYSTONE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                getWaystoneFeature(WorldGenStyle.DEFAULT));

        worldGen.registerPoiType(id("wild_waystone"), () -> new PoiType(gatherWaystonesOfOrigin(WaystoneOrigin.WILDERNESS), 1, 1));
        worldGen.registerPoiType(id("village_waystone"), () -> new PoiType(gatherWaystonesOfOrigin(WaystoneOrigin.VILLAGE), 1, 1));

        Balm.getEvents().onEvent(ServerStartedEvent.class, event -> setupDynamicRegistries(event.getServer().registryAccess()));
        Balm.getEvents().onEvent(ServerReloadedEvent.class, event -> setupDynamicRegistries(event.getServer().registryAccess()));
    }

    private static Set<BlockState> gatherWaystonesOfOrigin(WaystoneOrigin origin) {
        final var sourceBlocks = List.of(ModBlocks.waystone,
                ModBlocks.sandyWaystone,
                ModBlocks.mossyWaystone,
                ModBlocks.blackstoneWaystone,
                ModBlocks.endStoneWaystone);
        return sourceBlocks.stream()
                .flatMap(it -> it.getStateDefinition().getPossibleStates().stream())
                .filter(it -> it.getValue(WaystoneBlock.ORIGIN) == origin)
                .collect(Collectors.toSet());
    }

    private static BiomePredicate matchesTag(TagKey<Biome> tag) {
        return (resourceLocation, biome) -> biome.is(tag);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, name);
    }

    private static ResourceLocation getWaystoneFeature(WorldGenStyle biomeWorldGenStyle) {
        WorldGenStyle worldGenStyle = WaystonesConfig.getActive().worldGen.wildWaystoneStyle;
        return switch (worldGenStyle) {
            case MOSSY -> mossyWaystone;
            case SANDY -> sandyWaystone;
            case BLACKSTONE -> blackstoneWaystone;
            case DEEPSLATE -> deepslateWaystone;
            case END_STONE -> endStoneWaystone;
            case BIOME -> switch (biomeWorldGenStyle) {
                case SANDY -> sandyWaystone;
                case MOSSY -> mossyWaystone;
                case BLACKSTONE -> blackstoneWaystone;
                case DEEPSLATE -> deepslateWaystone;
                case END_STONE -> endStoneWaystone;
                default -> waystone;
            };
            default -> waystone;
        };
    }

    public static void setupDynamicRegistries(RegistryAccess registryAccess) {
        if (WaystonesConfig.getActive().worldGen.spawnInVillages != WaystonesConfigData.VillageWaystoneGeneration.DISABLED) {
            // Add Waystone to Vanilla Villages.
            addWaystoneStructureToVillageConfig(registryAccess, "village/plains/houses", villageWaystoneStructure, 1);
            addWaystoneStructureToVillageConfig(registryAccess, "village/snowy/houses", villageWaystoneStructure, 1);
            addWaystoneStructureToVillageConfig(registryAccess, "village/savanna/houses", villageWaystoneStructure, 1);
            addWaystoneStructureToVillageConfig(registryAccess, "village/desert/houses", desertVillageWaystoneStructure, 1);
            addWaystoneStructureToVillageConfig(registryAccess, "village/taiga/houses", villageWaystoneStructure, 1);
        }
    }

    private static void addWaystoneStructureToVillageConfig(RegistryAccess registryAccess, String villagePiece, ResourceLocation waystoneStructure, int weight) {
        Holder<StructureProcessorList> emptyProcessorList = registryAccess.lookupOrThrow(Registries.PROCESSOR_LIST)
                .getOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        LegacySinglePoolElement piece = StructurePoolElement.legacy(waystoneStructure.toString(), emptyProcessorList)
                .apply(StructureTemplatePool.Projection.RIGID);
        if (piece instanceof WaystoneStructurePoolElement element) {
            element.waystones$setIsWaystone(true);
        }
        StructureTemplatePool pool = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL)
                .getOptional(ResourceLocation.withDefaultNamespace(villagePiece))
                .orElse(null);
        if (pool != null) {
            var poolAccessor = (StructureTemplatePoolAccessor) pool;
            // pretty sure this can be an immutable list (when datapacked) so gotta make a copy to be safe.
            final var listOfPieces = new ObjectArrayList<>(poolAccessor.getTemplates());
            for (int i = 0; i < weight; i++) {
                listOfPieces.add(piece);
            }
            poolAccessor.setTemplates(listOfPieces);

            List<Pair<StructurePoolElement, Integer>> listOfWeightedPieces = new ArrayList<>(poolAccessor.getRawTemplates());
            listOfWeightedPieces.add(new Pair<>(piece, weight));
            poolAccessor.setRawTemplates(listOfWeightedPieces);
        }
    }
}
