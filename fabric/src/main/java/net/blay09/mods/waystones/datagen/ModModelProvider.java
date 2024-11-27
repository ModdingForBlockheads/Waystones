package net.blay09.mods.waystones.datagen;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.*;
import net.blay09.mods.waystones.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import static net.minecraft.client.data.models.BlockModelGenerators.createHorizontalFacingDispatch;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.warpPlate)
                .with(PropertyDispatch.property(WarpPlateBlock.STATUS)
                        .select(WarpPlateBlock.WarpPlateStatus.EMPTY, Variant.variant().with(VariantProperties.MODEL, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/warp_plate_empty")))
                        .select(WarpPlateBlock.WarpPlateStatus.IDLE, Variant.variant().with(VariantProperties.MODEL, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/warp_plate")))
                        .select(WarpPlateBlock.WarpPlateStatus.ATTUNING, Variant.variant().with(VariantProperties.MODEL, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/warp_plate")))
                        .select(WarpPlateBlock.WarpPlateStatus.WARPING, Variant.variant().with(VariantProperties.MODEL, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/warp_plate")))
                        .select(WarpPlateBlock.WarpPlateStatus.WARPING_INVALID, Variant.variant().with(VariantProperties.MODEL, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/warp_plate")))
                        .select(WarpPlateBlock.WarpPlateStatus.LOCKED, Variant.variant().with(VariantProperties.MODEL, ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/warp_plate_locked")))
                ));
        blockStateModelGenerator.registerSimpleTintedItemModel(ModBlocks.warpPlate, ModelLocationUtils.getModelLocation(ModBlocks.warpPlate.asItem()), new Constant(0xffc456bd));
        createDoubleBlockWaystone(blockStateModelGenerator, ModBlocks.waystone);
        createDoubleBlockWaystone(blockStateModelGenerator, ModBlocks.sandyWaystone);
        createDoubleBlockWaystone(blockStateModelGenerator, ModBlocks.mossyWaystone);
        createDoubleBlockWaystone(blockStateModelGenerator, ModBlocks.deepslateWaystone);
        createDoubleBlockWaystone(blockStateModelGenerator, ModBlocks.blackstoneWaystone);
        createDoubleBlockWaystone(blockStateModelGenerator, ModBlocks.endStoneWaystone);
        for (final var portstone : ModBlocks.portstones) {
            createPortstone(blockStateModelGenerator, portstone);
        }
        for (final var sharestone : ModBlocks.sharestones) {
            createSharestone(blockStateModelGenerator, sharestone);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(ModItems.warpDust, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.dormantShard, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.attunedShard, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.crumblingAttunedShard, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.deepslateShard, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.warpStone, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.warpScroll, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.returnScroll, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.boundScroll, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.blankScroll, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    private void createDoubleBlockWaystone(BlockModelGenerators blockStateModelGenerator, Block block) {
        createDoubleBlockWaystone(blockStateModelGenerator, block, block);
    }

    private void createDoubleBlockWaystone(BlockModelGenerators blockStateModelGenerator, Block block, Block modelBlock) {
        final var topModelLocation = ModelLocationUtils.getModelLocation(modelBlock, "_top");
        final var bottomModelLocation = ModelLocationUtils.getModelLocation(modelBlock, "_bottom");
        final var generator = MultiVariantGenerator.multiVariant(block)
                .with(createHorizontalFacingDispatch())
                .with(PropertyDispatch.property(WaystoneBlockBase.HALF)
                        .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                        .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, topModelLocation)));
        blockStateModelGenerator.blockStateOutput.accept(generator);
        blockStateModelGenerator.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block.asItem()));
    }

    private void createSharestone(BlockModelGenerators blockStateModelGenerator, SharestoneBlock block) {
        final var topModelLocation = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/sharestone_top");
        final var bottomModelLocation = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/sharestone_bottom");
        final var generator = MultiVariantGenerator.multiVariant(block)
                .with(createHorizontalFacingDispatch())
                .with(PropertyDispatch.property(WaystoneBlockBase.HALF)
                        .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                        .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, topModelLocation)));
        blockStateModelGenerator.blockStateOutput.accept(generator);
        blockStateModelGenerator.registerSimpleTintedItemModel(block, ModelLocationUtils.getModelLocation(block.asItem()), new Constant(block.getColor().getTextColor()));
    }

    private void createPortstone(BlockModelGenerators blockStateModelGenerator, PortstoneBlock block) {
        final var topModelLocation = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/portstone_top");
        final var bottomModelLocation = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/portstone_bottom");
        final var generator = MultiVariantGenerator.multiVariant(block)
                .with(createHorizontalFacingDispatch())
                .with(PropertyDispatch.property(WaystoneBlockBase.HALF)
                        .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                        .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, topModelLocation)));
        blockStateModelGenerator.blockStateOutput.accept(generator);
        if(block.getColor() != null) {
            blockStateModelGenerator.registerSimpleTintedItemModel(block, ModelLocationUtils.getModelLocation(block.asItem()), new Constant(block.getColor().getTextColor()));
        } else {
            blockStateModelGenerator.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block.asItem()));
        }
    }

}
