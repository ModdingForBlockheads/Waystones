package net.blay09.mods.waystones.datagen;

import net.blay09.mods.waystones.tag.ModBiomeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagProvider extends FabricTagProvider<Biome> {
    public ModBiomeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BIOME, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        final var hasMossyWaystones = getOrCreateTagBuilder(ModBiomeTags.HAS_STRUCTURE_MOSSY_WAYSTONE);
        hasMossyWaystones.add(Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.MUSHROOM_FIELDS);
        hasMossyWaystones.addOptionalTag(BiomeTags.IS_JUNGLE);
        getOrCreateTagBuilder(ModBiomeTags.HAS_STRUCTURE_SANDY_WAYSTONE).add(Biomes.DESERT);
        getOrCreateTagBuilder(ModBiomeTags.HAS_STRUCTURE_BLACKSTONE_WAYSTONE).addOptionalTag(BiomeTags.IS_NETHER);
        getOrCreateTagBuilder(ModBiomeTags.HAS_STRUCTURE_END_STONE_WAYSTONE).addOptionalTag(BiomeTags.IS_END);

        final var hasWaystones = getOrCreateTagBuilder(ModBiomeTags.HAS_STRUCTURE_WAYSTONE);
        hasWaystones.add(
                Biomes.PLAINS,
                Biomes.SUNFLOWER_PLAINS,
                Biomes.SNOWY_PLAINS,
                Biomes.ICE_SPIKES,
                Biomes.FOREST,
                Biomes.FLOWER_FOREST,
                Biomes.BIRCH_FOREST,
                Biomes.DARK_FOREST,
                Biomes.OLD_GROWTH_BIRCH_FOREST,
                Biomes.OLD_GROWTH_PINE_TAIGA,
                Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                Biomes.TAIGA,
                Biomes.SNOWY_TAIGA,
                Biomes.SAVANNA,
                Biomes.SAVANNA_PLATEAU,
                Biomes.WINDSWEPT_HILLS,
                Biomes.WINDSWEPT_GRAVELLY_HILLS,
                Biomes.WINDSWEPT_FOREST,
                Biomes.WINDSWEPT_SAVANNA,
                Biomes.JUNGLE,
                Biomes.SPARSE_JUNGLE,
                Biomes.BAMBOO_JUNGLE,
                Biomes.BADLANDS,
                Biomes.ERODED_BADLANDS,
                Biomes.WOODED_BADLANDS,
                Biomes.MEADOW,
                Biomes.CHERRY_GROVE,
                Biomes.GROVE,
                Biomes.SNOWY_SLOPES,
                Biomes.FROZEN_PEAKS,
                Biomes.JAGGED_PEAKS,
                Biomes.STONY_PEAKS,
                Biomes.RIVER,
                Biomes.FROZEN_RIVER,
                Biomes.BEACH,
                Biomes.SNOWY_BEACH,
                Biomes.STONY_SHORE,
                Biomes.WARM_OCEAN,
                Biomes.LUKEWARM_OCEAN,
                Biomes.DEEP_LUKEWARM_OCEAN,
                Biomes.OCEAN,
                Biomes.DEEP_OCEAN,
                Biomes.COLD_OCEAN,
                Biomes.DEEP_COLD_OCEAN,
                Biomes.FROZEN_OCEAN,
                Biomes.DEEP_FROZEN_OCEAN,
                Biomes.DRIPSTONE_CAVES,
                Biomes.LUSH_CAVES,
                Biomes.DEEP_DARK
        );
    }

}
