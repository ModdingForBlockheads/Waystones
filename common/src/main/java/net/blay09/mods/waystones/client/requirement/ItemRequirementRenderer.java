package net.blay09.mods.waystones.client.requirement;

import net.blay09.mods.waystones.requirement.ItemRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class ItemRequirementRenderer implements RequirementRenderer<ItemRequirement> {
    @Override
    public void renderWidget(Player player, ItemRequirement requirement, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, int x, int y) {
        final var font = Minecraft.getInstance().font;
        // TODO if (!requirement.canAfford(player)) {
        // TODO     guiGraphics.setColor(1f, 1f, 1f, 0.5f);
        // TODO }
        guiGraphics.renderItem(requirement.getItemStack(), x, y, 16, 16);
        guiGraphics.renderItemDecorations(font, requirement.getItemStack(), x, y, requirement.getCount() > 1 ? String.valueOf(requirement.getCount()) : null);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
