package net.blay09.mods.waystones.client.gui.screen;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.WarpPlateBlock;
import net.blay09.mods.waystones.menu.WaystoneModifierMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WaystoneModifierScreen extends AbstractContainerScreen<WaystoneModifierMenu> {

    private static final ResourceLocation WARP_PLATE_GUI_TEXTURES = ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "textures/gui/menu/waystone_modifiers.png");

    public WaystoneModifierScreen(WaystoneModifierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageHeight = 196;
        inventoryLabelY = 93;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int x, int y) {
        guiGraphics.blit(RenderType::guiTextured, WARP_PLATE_GUI_TEXTURES, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        super.renderLabels(guiGraphics, x, y);

        Component galacticName = WarpPlateBlock.getGalacticName(menu.getWaystone());
        int width = font.width(galacticName);
        guiGraphics.drawString(font, galacticName, imageWidth - width - 5, 5, 0xFFFFFFFF);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
