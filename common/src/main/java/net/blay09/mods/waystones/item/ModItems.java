package net.blay09.mods.waystones.item;

import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.item.BalmItems;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ModItems {
    public static DeferredObject<CreativeModeTab> creativeModeTab;

    public static Item returnScroll;
    public static Item blankScroll;
    public static Item boundScroll;
    public static Item warpScroll;
    public static Item warpStone;
    public static Item warpDust;
    public static Item dormantShard;
    public static Item attunedShard;
    public static Item deepslateShard;
    public static Item crumblingAttunedShard;

    public static void initialize(BalmItems items) {
        items.registerItem((identifier) -> returnScroll = new ReturnScrollItem(defaultProperties(identifier)), id("return_scroll"));
        items.registerItem((identifier) -> blankScroll = new BlankScrollItem(defaultProperties(identifier)), id("blank_scroll"));
        items.registerItem((identifier) -> boundScroll = new BoundScrollItem(defaultProperties(identifier)), id("bound_scroll"), null);
        items.registerItem((identifier) -> warpScroll = new WarpScrollItem(defaultProperties(identifier)), id("warp_scroll"));
        items.registerItem((identifier) -> warpStone = new WarpStoneItem(defaultProperties(identifier)), id("warp_stone"));
        items.registerItem((identifier) -> warpDust = new WarpDustItem(defaultProperties(identifier)), id("warp_dust"));
        items.registerItem((identifier) -> dormantShard = new ShardItem(defaultProperties(identifier)), id("dormant_shard"));
        items.registerItem((identifier) -> attunedShard = new AttunedShardItem(defaultProperties(identifier)), id("attuned_shard"), null);
        items.registerItem((identifier) -> deepslateShard = new ShardItem(defaultProperties(identifier)), id("deepslate_shard"), null);
        items.registerItem((identifier) -> crumblingAttunedShard = new CrumblingAttunedShardItem(defaultProperties(identifier)), id("crumbling_attuned_shard"), null);

        creativeModeTab = items.registerCreativeModeTab(() -> new ItemStack(ModBlocks.waystone), id("waystones"));

        items.setCreativeModeTabSorting(id("waystones"), new Comparator<>() {
            private static final String[] patternStrings = new String[]{
                    "waystone",
                    "white_portstone",
                    "red_sharestone",
                    "warp_plate",
                    "blank_scroll",
                    "return_scroll",
                    "warp_scroll",
                    "warp_stone",
                    "warp_dust",
                    "dormant_shard",
                    "deepslate_shard",
                    ".+_waystone",
                    ".+_sharestone",
                    ".+_portstone",
                    "attuned_shard",
                    "crumbling_attuned_shard",
            };

            private static final Map<String, Integer> indexMap = new HashMap<>();
            private static final Map<Pattern, Integer> patternIndexMap = new HashMap<>();

            static {
                for (int i = 0; i < patternStrings.length; i++) {
                    final var patternString = patternStrings[i];
                    indexMap.put(patternString, i);
                    patternIndexMap.put(Pattern.compile(patternString), i);
                }
            }

            private static int getIndex(String name) {
                final var index = indexMap.get(name);
                if (index != null) {
                    return index;
                }

                for (var entry : patternIndexMap.entrySet()) {
                    if (entry.getKey().matcher(name).matches()) {
                        return entry.getValue();
                    }
                }

                return -1;
            }

            @Override
            public int compare(ItemLike o1, ItemLike o2) {
                final var id1 = BuiltInRegistries.ITEM.getKey(o1.asItem());
                final var id2 = BuiltInRegistries.ITEM.getKey(o2.asItem());
                final var name1 = id1.getPath();
                final var name2 = id2.getPath();
                final var index1 = getIndex(name1);
                final var index2 = getIndex(name2);
                if (index1 != -1 && index2 != -1) {
                    return Integer.compare(index1, index2);
                } else if (index1 != -1) {
                    return -1;
                } else if (index2 != -1) {
                    return 1;
                }

                return name1.compareTo(name2);
            }
        });
    }

    private static Item.Properties defaultProperties(ResourceLocation identifier) {
        return new Item.Properties().setId(itemId(identifier));
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, name);
    }

    private static ResourceKey<Item> itemId(ResourceLocation identifier) {
        return ResourceKey.create(Registries.ITEM, identifier);
    }
}
