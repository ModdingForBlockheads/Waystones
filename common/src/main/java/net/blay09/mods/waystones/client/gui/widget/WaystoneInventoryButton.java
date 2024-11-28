package net.blay09.mods.waystones.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.balm.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.waystones.api.TeleportFlags;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.blay09.mods.waystones.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class WaystoneInventoryButton extends Button {

    private static final ResourceLocation INVENTORY_BUTTON_SPRITE = ResourceLocation.withDefaultNamespace("waystones/inventory_button");

    private final AbstractContainerScreen<?> parentScreen;
    private final ItemStack iconItem;
    private final ItemStack iconItemHovered;
    private final Supplier<Boolean> visiblePredicate;
    private final Supplier<Integer> xPosition;
    private final Supplier<Integer> yPosition;

    private final WarpRequirement warpRequirement;

    public WaystoneInventoryButton(AbstractContainerScreen<?> parentScreen, OnPress pressable, Supplier<Boolean> visiblePredicate, Supplier<Integer> xPosition, Supplier<Integer> yPosition) {
        super(0, 0, 16, 16, Component.empty(), pressable, Button.DEFAULT_NARRATION);
        this.parentScreen = parentScreen;
        this.visiblePredicate = visiblePredicate;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.iconItem = new ItemStack(ModItems.boundScroll);
        this.iconItemHovered = new ItemStack(ModItems.warpScroll);

        final var player = Minecraft.getInstance().player;
        warpRequirement = WaystonesAPI.resolveRequirements(WaystonesAPI.createUnboundTeleportContext(player).addFlag(TeleportFlags.INVENTORY_BUTTON));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        visible = visiblePredicate.get();
        if (visible) {
            setX(((AbstractContainerScreenAccessor) parentScreen).getLeftPos() + xPosition.get());
            setY(((AbstractContainerScreenAccessor) parentScreen).getTopPos() + yPosition.get());
            isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;

            final var player = Minecraft.getInstance().player;
            if (warpRequirement.canAfford(player)) {
                ItemStack icon = isHovered ? iconItemHovered : iconItem;
                guiGraphics.renderItem(icon, getX(), getY());
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, icon, getX(), getY());
            } else {
                RenderSystem.enableBlend();
                guiGraphics.blitSprite(RenderType::guiTextured, INVENTORY_BUTTON_SPRITE, getX(), getY(), 16, 16, 0x80FFFFFF);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                RenderSystem.disableBlend();
            }
        }
    }
}
