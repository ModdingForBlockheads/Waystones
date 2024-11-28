package net.blay09.mods.waystones.datagen;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.ModBlocks;
import net.blay09.mods.waystones.block.WarpPlateBlock;
import net.blay09.mods.waystones.block.WaystoneBlockBase;
import net.blay09.mods.waystones.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.Optional;

import static net.minecraft.data.models.BlockModelGenerators.createHorizontalFacingDispatch;

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

        final var sharestoneTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("waystones", "item/sharestone")), Optional.empty());
        for (final var sharestone : ModBlocks.sharestones) {
            itemModelGenerator.generateFlatItem(sharestone.asItem(), sharestoneTemplate);
        }

        final var portstoneTemplate = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath("waystones", "item/portstone")), Optional.empty());
        for (final var portstone : ModBlocks.portstones) {
            itemModelGenerator.generateFlatItem(portstone.asItem(), portstoneTemplate);
        }
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
        blockStateModelGenerator.skipAutoItemBlock(block);
    }

    private void createSharestone(BlockModelGenerators blockStateModelGenerator, Block block) {
        final var topModelLocation = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/sharestone_top");
        final var bottomModelLocation = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/sharestone_bottom");
        final var generator = MultiVariantGenerator.multiVariant(block)
                .with(createHorizontalFacingDispatch())
                .with(PropertyDispatch.property(WaystoneBlockBase.HALF)
                        .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                        .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, topModelLocation)));
        blockStateModelGenerator.blockStateOutput.accept(generator);
        blockStateModelGenerator.skipAutoItemBlock(block);
    }

    private void createPortstone(BlockModelGenerators blockStateModelGenerator, Block block) {
        final var topModelLocation = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/portstone_top");
        final var bottomModelLocation = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "block/portstone_bottom");
        final var generator = MultiVariantGenerator.multiVariant(block)
                .with(createHorizontalFacingDispatch())
                .with(PropertyDispatch.property(WaystoneBlockBase.HALF)
                        .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                        .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, topModelLocation)));
        blockStateModelGenerator.blockStateOutput.accept(generator);
        blockStateModelGenerator.skipAutoItemBlock(block);
    }

}
