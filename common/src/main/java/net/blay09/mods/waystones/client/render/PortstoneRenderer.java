package net.blay09.mods.waystones.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.blay09.mods.waystones.block.PortstoneBlock;
import net.blay09.mods.waystones.block.entity.PortstoneBlockEntity;
import net.blay09.mods.waystones.client.ModRenderers;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class PortstoneRenderer implements BlockEntityRenderer<PortstoneBlockEntity> {
    private static final Material MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("waystone_overlays/portstone"));
    private static ItemStack warpStoneItem;

    private final PortstoneModel model;

    public PortstoneRenderer(BlockEntityRendererProvider.Context context) {
        model = new PortstoneModel(context.bakeLayer(ModRenderers.portstoneModel));
    }

    @Override
    public void render(PortstoneBlockEntity tileEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        final var level = tileEntity.getLevel();
        final var state = tileEntity.getBlockState();
        if (level == null || state.getValue(PortstoneBlock.HALF) != DoubleBlockHalf.LOWER) {
            return;
        }
        final var facing = state.getValue(PortstoneBlock.FACING);

        if (warpStoneItem == null) {
            warpStoneItem = new ItemStack(ModItems.warpStone);
            level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.UNBREAKING).ifPresent(it -> warpStoneItem.enchant(it, 1));
        }

        DyeColor color = ((PortstoneBlock) state.getBlock()).getColor();
        if (color != null) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));
            poseStack.mulPose(Axis.XN.rotationDegrees(180f));
            poseStack.translate(0f, -2f, 0f);
            float scale = 1.01f;
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.scale(scale, scale, scale);

            VertexConsumer vertexBuilder = MATERIAL.buffer(buffer, RenderType::entityCutout);
            int light = WaystonesConfig.getActive().client.disableTextGlow ? combinedLightIn : 15728880;
            int overlay = WaystonesConfig.getActive().client.disableTextGlow ? combinedOverlayIn : OverlayTexture.NO_OVERLAY;
            model.renderToBuffer(poseStack, vertexBuilder, light, overlay, color.getTextureDiffuseColor());
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 1f, 0.5f);
        poseStack.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));
        poseStack.translate(0f, 0f, 0.15f);
        poseStack.mulPose(Axis.XN.rotationDegrees(25f));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(0.03125f, 0f, 0f);
        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(warpStoneItem, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, poseStack, buffer, level, 0);
        poseStack.popPose();
    }
}
