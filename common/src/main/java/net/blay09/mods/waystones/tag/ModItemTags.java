package net.blay09.mods.waystones.tag;

import net.blay09.mods.waystones.Waystones;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> SCROLLS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "scrolls"));
    public static final TagKey<Item> BOUND_SCROLLS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "bound_scrolls"));
    public static final TagKey<Item> RETURN_SCROLLS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "return_scrolls"));
    public static final TagKey<Item> WARP_SCROLLS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "warp_scrolls"));
    public static final TagKey<Item> WARP_STONES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "warp_stones"));
    public static final TagKey<Item> WARP_SHARDS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "warp_shards"));
    public static final TagKey<Item> SINGLE_USE_WARP_SHARDS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "single_use_warp_shards"));
    public static final TagKey<Item> WAYSTONES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "waystones"));
    public static final TagKey<Item> SHARESTONES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "sharestones"));
    public static final TagKey<Item> PORTSTONES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "portstones"));
}
