package net.blay09.mods.waystones.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.ModBlocks;
import net.blay09.mods.waystones.block.WaystoneBlock;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntity;
import net.blay09.mods.waystones.client.ModRenderers;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.Objects;

public class WaystoneRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {

    private static final Material MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.withDefaultNamespace("waystone_overlays/waystone_active"));

    private final SharestoneModel model;

    public WaystoneRenderer(BlockEntityRendererProvider.Context context) {
        model = new SharestoneModel(context.bakeLayer(ModRenderers.waystoneModel));
    }

    @Override
    public void render(WaystoneBlockEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = tileEntity.getBlockState();
        if (state.getValue(WaystoneBlock.HALF) != DoubleBlockHalf.LOWER) {
            return;
        }

        float angle = state.getValue(WaystoneBlock.FACING).toYRot();
        matrixStack.pushPose();
        matrixStack.translate(0.5f, 0f, 0.5f);
        matrixStack.mulPose(Axis.YP.rotationDegrees(angle));
        matrixStack.mulPose(Axis.XN.rotationDegrees(180f));
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        Player player = Minecraft.getInstance().player;
        boolean isActivated = PlayerWaystoneManager.isWaystoneActivated(Objects.requireNonNull(player), tileEntity.getWaystone());
        if (isActivated) {
            matrixStack.scale(1.05f, 1.05f, 1.05f);
            VertexConsumer vertexBuilder = MATERIAL.buffer(buffer, RenderType::entityCutout);
            int light = WaystonesConfig.getActive().client.disableTextGlow ? combinedLightIn : 15728880;
            int overlay = WaystonesConfig.getActive().client.disableTextGlow ? combinedOverlayIn : OverlayTexture.NO_OVERLAY;
            int color = 0xFFFFFFFF;
            if (state.getBlock() == ModBlocks.endStoneWaystone) {
                color = 0xFF7200FF;
            } else if (state.getBlock() == ModBlocks.blackstoneWaystone) {
                color = 0xFF993333;
            }

            model.renderToBuffer(matrixStack, vertexBuilder, light, overlay, color);
        }
        matrixStack.popPose();
    }
}
