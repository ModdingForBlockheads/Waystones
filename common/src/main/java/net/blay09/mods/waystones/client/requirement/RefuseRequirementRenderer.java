package net.blay09.mods.waystones.client.requirement;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.waystones.requirement.RefuseRequirement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class RefuseRequirementRenderer implements RequirementRenderer<RefuseRequirement> {

    private static final ResourceLocation CANCEL_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/cancel");

    @Override
    public void renderWidget(Player player, RefuseRequirement requirement, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, int x, int y) {
        RenderSystem.enableBlend();
        guiGraphics.blitSprite(RenderType::guiTextured, CANCEL_SPRITE, x, y, 16, 16, 0x80FFFFFF);
    }
}
