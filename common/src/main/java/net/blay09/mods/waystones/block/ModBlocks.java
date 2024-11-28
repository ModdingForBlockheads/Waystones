package net.blay09.mods.waystones.block;

import net.blay09.mods.balm.api.block.BalmBlocks;
import net.blay09.mods.waystones.Waystones;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

public class ModBlocks {

    private static final DyeColor[] portstoneColors = new DyeColor[]{
            DyeColor.WHITE,
            DyeColor.ORANGE,
            DyeColor.MAGENTA,
            DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.PINK,
            DyeColor.GRAY,
            DyeColor.LIGHT_GRAY,
            DyeColor.CYAN,
            DyeColor.PURPLE,
            DyeColor.BLUE,
            DyeColor.BROWN,
            DyeColor.GREEN,
            DyeColor.RED,
            DyeColor.BLACK
    };

    private static final DyeColor[] sharestoneColors = new DyeColor[]{
            DyeColor.ORANGE,
            DyeColor.MAGENTA,
            DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.PINK,
            DyeColor.GRAY,
            DyeColor.LIGHT_GRAY,
            DyeColor.CYAN,
            DyeColor.PURPLE,
            DyeColor.BLUE,
            DyeColor.BROWN,
            DyeColor.GREEN,
            DyeColor.RED,
            DyeColor.BLACK
    };

    public static Block waystone;
    public static Block mossyWaystone;
    public static Block sandyWaystone;
    public static Block deepslateWaystone;
    public static Block blackstoneWaystone;
    public static Block endStoneWaystone;
    public static Block warpPlate;
    public static final PortstoneBlock[] portstones = new PortstoneBlock[portstoneColors.length];
    public static final SharestoneBlock[] sharestones = new SharestoneBlock[sharestoneColors.length];

    public static void initialize(BalmBlocks blocks) {
        blocks.register((identifier) -> waystone = new WaystoneBlock(defaultProperties(identifier)), ModBlocks::itemBlock, id("waystone"));
        blocks.register((identifier) -> mossyWaystone = new WaystoneBlock(defaultProperties(identifier)), ModBlocks::itemBlock, id("mossy_waystone"));
        blocks.register((identifier) -> sandyWaystone = new WaystoneBlock(defaultProperties(identifier)), ModBlocks::itemBlock, id("sandy_waystone"));
        blocks.register((identifier) -> deepslateWaystone = new WaystoneBlock(defaultProperties(identifier).sound(SoundType.DEEPSLATE)),
                ModBlocks::itemBlock,
                id("deepslate_waystone"));
        blocks.register((identifier) -> blackstoneWaystone = new WaystoneBlock(defaultProperties(identifier)), ModBlocks::itemBlock, id("blackstone_waystone"));
        blocks.register((identifier) -> endStoneWaystone = new WaystoneBlock(defaultProperties(identifier)), ModBlocks::itemBlock, id("end_stone_waystone"));
        blocks.register((identifier) -> warpPlate = new WarpPlateBlock(defaultProperties(identifier)), ModBlocks::itemBlock, id("warp_plate"));

        for (final var color : portstoneColors) {
            blocks.register((identifier) -> portstones[color.ordinal()] = new PortstoneBlock(color, defaultProperties(identifier)),
                    ModBlocks::itemBlock,
                    id(color.getSerializedName() + "_portstone"));
        }

        for (final var color : sharestoneColors) {
            blocks.register((identifier) -> sharestones[color.ordinal() - 1] = new SharestoneBlock(color, defaultProperties(identifier)),
                    ModBlocks::itemBlock,
                    id(color.getSerializedName() + "_sharestone"));
        }
    }

    private static BlockItem itemBlock(Block block, ResourceLocation name) {
        return new BlockItem(block, defaultItemProperties(name));
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, name);
    }

    private static ResourceKey<Block> blockId(ResourceLocation identifier) {
        return ResourceKey.create(Registries.BLOCK, identifier);
    }

    private static ResourceKey<Item> itemId(ResourceLocation identifier) {
        return ResourceKey.create(Registries.ITEM, identifier);
    }

    private static BlockBehaviour.Properties defaultProperties(ResourceLocation identifier) {
        return BlockBehaviour.Properties.of().setId(blockId(identifier)).sound(SoundType.STONE).strength(5f, 2000f);
    }

    private static Item.Properties defaultItemProperties(ResourceLocation identifier) {
        return new Item.Properties().setId(itemId(identifier));
    }

    @Nullable
    public static SharestoneBlock getSharestone(DyeColor color) {
        final var index = color.ordinal() - 1; // -1 because we skip WHITE
        if (index < 0 || index >= sharestones.length) {
            return null;
        }

        return sharestones[index];
    }

    @Nullable
    public static PortstoneBlock getPortstone(DyeColor color) {
        final var index = color.ordinal();
        if (index >= portstones.length) {
            return null;
        }

        return portstones[index];
    }

}
