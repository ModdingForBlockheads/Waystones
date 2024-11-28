package net.blay09.mods.waystones.tag;

import net.blay09.mods.waystones.Waystones;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomeTags {
    public static final TagKey<Biome> HAS_STRUCTURE_WAYSTONE = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "has_structure/waystone"));
    public static final TagKey<Biome> HAS_STRUCTURE_MOSSY_WAYSTONE = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "has_structure/mossy_waystone"));
    public static final TagKey<Biome> HAS_STRUCTURE_SANDY_WAYSTONE = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "has_structure/sandy_waystone"));
    public static final TagKey<Biome> HAS_STRUCTURE_BLACKSTONE_WAYSTONE = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "has_structure/blackstone_waystone"));
    public static final TagKey<Biome> HAS_STRUCTURE_END_STONE_WAYSTONE = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "has_structure/end_stone_waystone"));
}
