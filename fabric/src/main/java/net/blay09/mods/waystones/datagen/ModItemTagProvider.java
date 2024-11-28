package net.blay09.mods.waystones.datagen;

import net.blay09.mods.waystones.block.ModBlocks;
import net.blay09.mods.waystones.item.ModItems;
import net.blay09.mods.waystones.tag.ModItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider<Item> {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ITEM, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        getOrCreateTagBuilder(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("enchantable/durability"))).add(ModItems.warpStone);
        getOrCreateTagBuilder(ModItemTags.SCROLLS).add(ModItems.warpScroll, ModItems.returnScroll, ModItems.boundScroll, ModItems.blankScroll);
        getOrCreateTagBuilder(ModItemTags.WARP_SCROLLS).add(ModItems.warpScroll);
        getOrCreateTagBuilder(ModItemTags.RETURN_SCROLLS).add(ModItems.returnScroll);
        getOrCreateTagBuilder(ModItemTags.BOUND_SCROLLS).add(ModItems.boundScroll);
        getOrCreateTagBuilder(ModItemTags.WARP_STONES).add(ModItems.warpStone);
        getOrCreateTagBuilder(ModItemTags.WARP_SHARDS).add(ModItems.attunedShard, ModItems.crumblingAttunedShard, ModItems.dormantShard, ModItems.deepslateShard);
        getOrCreateTagBuilder(ModItemTags.SINGLE_USE_WARP_SHARDS).add(ModItems.crumblingAttunedShard);
        getOrCreateTagBuilder(ModItemTags.WAYSTONES).add(ModBlocks.waystone.asItem(),
                ModBlocks.mossyWaystone.asItem(),
                ModBlocks.sandyWaystone.asItem(),
                ModBlocks.deepslateWaystone.asItem(),
                ModBlocks.blackstoneWaystone.asItem(),
                ModBlocks.endStoneWaystone.asItem());
        FabricTagProvider<Item>.FabricTagBuilder sharestonesTag = getOrCreateTagBuilder(ModItemTags.SHARESTONES);
        for (Block sharestone : ModBlocks.sharestones) {
            sharestonesTag.add(sharestone.asItem());
        }
    }
}
