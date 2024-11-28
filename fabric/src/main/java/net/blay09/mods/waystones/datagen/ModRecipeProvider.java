package net.blay09.mods.waystones.datagen;

import net.blay09.mods.balm.api.tag.BalmItemTags;
import net.blay09.mods.waystones.block.ModBlocks;
import net.blay09.mods.waystones.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.DECORATIONS, ModBlocks.waystone)
                        .pattern(" S ")
                        .pattern("SWS")
                        .pattern("OOO")
                        .define('S', Blocks.STONE_BRICKS)
                        .define('W', ModItems.warpStone)
                        .define('O', Blocks.OBSIDIAN)
                        .unlockedBy("has_warp_stone", has(ModItems.warpStone))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.sandyWaystone)
                        .pattern(" S ")
                        .pattern("SWS")
                        .pattern("OOO")
                        .define('S', Blocks.CHISELED_SANDSTONE)
                        .define('W', ModItems.warpStone)
                        .define('O', Blocks.OBSIDIAN)
                        .unlockedBy("has_warp_stone", has(ModItems.warpStone))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.mossyWaystone)
                        .pattern(" S ")
                        .pattern("SWS")
                        .pattern("OOO")
                        .define('S', Blocks.MOSSY_STONE_BRICKS)
                        .define('W', ModItems.warpStone)
                        .define('O', Blocks.OBSIDIAN)
                        .unlockedBy("has_warp_stone", has(ModItems.warpStone))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.deepslateWaystone)
                        .pattern(" S ")
                        .pattern("SWS")
                        .pattern("OOO")
                        .define('S', Blocks.DEEPSLATE)
                        .define('W', ModItems.warpStone)
                        .define('O', Blocks.OBSIDIAN)
                        .unlockedBy("has_warp_stone", has(ModItems.warpStone))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.blackstoneWaystone)
                        .pattern(" S ")
                        .pattern("SWS")
                        .pattern("OOO")
                        .define('S', Blocks.BLACKSTONE)
                        .define('W', ModItems.warpStone)
                        .define('O', Blocks.OBSIDIAN)
                        .unlockedBy("has_warp_stone", has(ModItems.warpStone))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.endStoneWaystone)
                        .pattern(" S ")
                        .pattern("SWS")
                        .pattern("OOO")
                        .define('S', Blocks.END_STONE_BRICKS)
                        .define('W', ModItems.warpStone)
                        .define('O', Blocks.OBSIDIAN)
                        .unlockedBy("has_warp_stone", has(ModItems.warpStone))
                        .save(exporter);

                shapeless(RecipeCategory.DECORATIONS, ModBlocks.mossyWaystone)
                        .requires(ModBlocks.waystone)
                        .requires(Blocks.VINE, 3)
                        .unlockedBy("has_waystone", has(ModBlocks.waystone))
                        .save(exporter, "waystones:mossy_waystone_from_vines");

                shapeless(RecipeCategory.DECORATIONS, ModBlocks.mossyWaystone)
                        .requires(ModBlocks.waystone)
                        .requires(Blocks.MOSS_BLOCK, 3)
                        .unlockedBy("has_waystone", has(ModBlocks.waystone))
                        .save(exporter, "waystones:mossy_waystone_from_moss_blocks");

                shaped(RecipeCategory.DECORATIONS, ModBlocks.warpPlate)
                        .pattern("SWS")
                        .pattern("WFW")
                        .pattern("SWS")
                        .define('F', ModItems.dormantShard)
                        .define('W', ModItems.warpDust)
                        .define('S', Blocks.STONE_BRICKS)
                        .unlockedBy("has_warp_dust", has(ModItems.warpDust))
                        .save(exporter);

                shapeless(RecipeCategory.DECORATIONS, ModItems.dormantShard, 1)
                        .requires(ModItems.warpDust, 2)
                        .requires(Items.FLINT)
                        .unlockedBy("has_warp_dust", has(ModItems.warpDust))
                        .save(exporter);

                shapeless(RecipeCategory.DECORATIONS, ModItems.deepslateShard, 1)
                        .requires(Items.DEEPSLATE)
                        .requires(Items.FLINT)
                        .unlockedBy("has_deepslate", has(Items.DEEPSLATE))
                        .save(exporter);

                createPortstoneRecipe(exporter, DyeColor.WHITE, BalmItemTags.WHITE_DYES);
                createPortstoneRecipe(exporter, DyeColor.ORANGE, BalmItemTags.ORANGE_DYES);
                createPortstoneRecipe(exporter, DyeColor.MAGENTA, BalmItemTags.MAGENTA_DYES);
                createPortstoneRecipe(exporter, DyeColor.LIGHT_BLUE, BalmItemTags.LIGHT_BLUE_DYES);
                createPortstoneRecipe(exporter, DyeColor.YELLOW, BalmItemTags.YELLOW_DYES);
                createPortstoneRecipe(exporter, DyeColor.LIME, BalmItemTags.LIME_DYES);
                createPortstoneRecipe(exporter, DyeColor.PINK, BalmItemTags.PINK_DYES);
                createPortstoneRecipe(exporter, DyeColor.GRAY, BalmItemTags.GRAY_DYES);
                createPortstoneRecipe(exporter, DyeColor.LIGHT_GRAY, BalmItemTags.LIGHT_GRAY_DYES);
                createPortstoneRecipe(exporter, DyeColor.CYAN, BalmItemTags.CYAN_DYES);
                createPortstoneRecipe(exporter, DyeColor.PURPLE, BalmItemTags.PURPLE_DYES);
                createPortstoneRecipe(exporter, DyeColor.BLUE, BalmItemTags.BLUE_DYES);
                createPortstoneRecipe(exporter, DyeColor.BROWN, BalmItemTags.BROWN_DYES);
                createPortstoneRecipe(exporter, DyeColor.GREEN, BalmItemTags.GREEN_DYES);
                createPortstoneRecipe(exporter, DyeColor.RED, BalmItemTags.RED_DYES);
                createPortstoneRecipe(exporter, DyeColor.BLACK, BalmItemTags.BLACK_DYES);

                createSharestoneRecipe(exporter, DyeColor.ORANGE, BalmItemTags.ORANGE_DYES);
                createSharestoneRecipe(exporter, DyeColor.MAGENTA, BalmItemTags.MAGENTA_DYES);
                createSharestoneRecipe(exporter, DyeColor.LIGHT_BLUE, BalmItemTags.LIGHT_BLUE_DYES);
                createSharestoneRecipe(exporter, DyeColor.YELLOW, BalmItemTags.YELLOW_DYES);
                createSharestoneRecipe(exporter, DyeColor.LIME, BalmItemTags.LIME_DYES);
                createSharestoneRecipe(exporter, DyeColor.PINK, BalmItemTags.PINK_DYES);
                createSharestoneRecipe(exporter, DyeColor.GRAY, BalmItemTags.GRAY_DYES);
                createSharestoneRecipe(exporter, DyeColor.LIGHT_GRAY, BalmItemTags.LIGHT_GRAY_DYES);
                createSharestoneRecipe(exporter, DyeColor.CYAN, BalmItemTags.CYAN_DYES);
                createSharestoneRecipe(exporter, DyeColor.PURPLE, BalmItemTags.PURPLE_DYES);
                createSharestoneRecipe(exporter, DyeColor.BLUE, BalmItemTags.BLUE_DYES);
                createSharestoneRecipe(exporter, DyeColor.BROWN, BalmItemTags.BROWN_DYES);
                createSharestoneRecipe(exporter, DyeColor.GREEN, BalmItemTags.GREEN_DYES);
                createSharestoneRecipe(exporter, DyeColor.RED, BalmItemTags.RED_DYES);
                createSharestoneRecipe(exporter, DyeColor.BLACK, BalmItemTags.BLACK_DYES);

                shaped(RecipeCategory.DECORATIONS, ModItems.warpStone)
                        .pattern("DED")
                        .pattern("EGE")
                        .pattern("DED")
                        .define('G', BalmItemTags.EMERALDS)
                        .define('E', Items.ENDER_PEARL)
                        .define('D', Items.AMETHYST_SHARD)
                        .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModItems.warpScroll, 3)
                        .pattern("GDG")
                        .pattern("GEG")
                        .pattern("PPP")
                        .define('D', Items.INK_SAC)
                        .define('G', BalmItemTags.GOLD_NUGGETS)
                        .define('E', Items.ENDER_PEARL)
                        .define('P', Items.PAPER)
                        .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModItems.blankScroll, 3)
                        .pattern("GFG")
                        .pattern("PPP")
                        .define('F', Items.FEATHER)
                        .define('G', BalmItemTags.GOLD_NUGGETS)
                        .define('P', Items.PAPER)
                        .unlockedBy("has_paper", has(Items.PAPER))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModItems.returnScroll, 3)
                        .pattern("GEG")
                        .pattern("PPP")
                        .define('E', Items.INK_SAC)
                        .define('G', BalmItemTags.GOLD_NUGGETS)
                        .define('P', Items.PAPER)
                        .unlockedBy("has_paper", has(Items.PAPER))
                        .save(exporter);

                shapeless(RecipeCategory.DECORATIONS, ModItems.warpDust, 4)
                        .requires(Items.ENDER_PEARL)
                        .requires(Items.AMETHYST_SHARD)
                        .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                        .save(exporter);
            }

            private void createSharestoneRecipe(RecipeOutput exporter, DyeColor color, TagKey<Item> dyeTag) {
                final var sharestone = ModBlocks.getSharestone(color);
                if (sharestone == null) {
                    return;
                }

                shaped(RecipeCategory.DECORATIONS, sharestone)
                        .pattern("SSS")
                        .pattern("DWD")
                        .pattern("OOO")
                        .define('S', Blocks.STONE_BRICKS)
                        .define('W', ModItems.warpStone)
                        .define('O', Blocks.OBSIDIAN)
                        .define('D', dyeTag)
                        .unlockedBy("has_warp_stone", has(ModItems.warpStone))
                        .save(exporter);
            }

            private void createPortstoneRecipe(RecipeOutput exporter, DyeColor color, TagKey<Item> dyeTag) {
                final var portstone = ModBlocks.getPortstone(color);
                if (portstone == null) {
                    return;
                }

                shaped(RecipeCategory.DECORATIONS, portstone)
                        .pattern("DSD")
                        .pattern("SWS")
                        .pattern("BBB")
                        .define('S', Blocks.STONE_BRICKS)
                        .define('W', ModItems.warpStone)
                        .define('B', Blocks.POLISHED_ANDESITE)
                        .define('D', dyeTag)
                        .unlockedBy("has_warp_stone", has(ModItems.warpStone))
                        .save(exporter);
            }

        };
    }

    @Override
    public String getName() {
        return "waystones";
    }
}