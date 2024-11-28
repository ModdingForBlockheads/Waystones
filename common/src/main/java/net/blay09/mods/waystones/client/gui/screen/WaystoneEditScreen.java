package net.blay09.mods.waystones.client.gui.screen;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.client.gui.widget.WaystoneVisbilityButton;
import net.blay09.mods.waystones.core.WaystoneVisibilities;
import net.blay09.mods.waystones.menu.WaystoneEditMenu;
import net.blay09.mods.waystones.network.message.EditWaystoneMessage;
import net.blay09.mods.waystones.network.message.RequestManageWaystoneModifiersMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class WaystoneEditScreen extends AbstractContainerScreen<WaystoneEditMenu> {

    private EditBox textField;
    private WaystoneVisbilityButton visibilityButton;
    private ImageButton modifierButton;
    private Button saveButton;

    public WaystoneEditScreen(WaystoneEditMenu container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        imageHeight = 210;
        titleLabelY = 44;
    }

    @Override
    public void init() {
        super.init();
        final var waystone = menu.getWaystone();
        var oldText = waystone.getName().getString();
        if (textField != null) {
            oldText = textField.getValue();
        }
        var oldVisibility = waystone.getVisibility();
        if (visibilityButton != null) {
            oldVisibility = visibilityButton.getVisibility();
        }

        var y = topPos + titleLabelY + 16;

        final var error = menu.getError();
        if (error != null) {
            y += 9;
        }

        textField = new EditBox(Minecraft.getInstance().font, leftPos, y, 176, 20, textField, Component.empty());
        textField.setMaxLength(128);
        textField.setValue(oldText);
        textField.setEditable(menu.canEdit());
        addRenderableWidget(textField);
        if (menu.canEdit() && oldText.isEmpty()) {
            setInitialFocus(textField);
        }
        y += 28;

        final var visibilityOptions = WaystoneVisibilities.getVisibilityOptions(Minecraft.getInstance().player, waystone);
        visibilityButton = new WaystoneVisbilityButton(leftPos, y, oldVisibility, visibilityOptions, menu.canEdit());
        visibilityButton.active = menu.canEdit() && visibilityOptions.size() > 1;
        addRenderableWidget(visibilityButton);
        y += 24;

        final var modifierSprites = new WidgetSprites(
                ResourceLocation.withDefaultNamespace("waystones/modifier_button"),
                ResourceLocation.withDefaultNamespace("waystones/modifier_button_highlighted"));
        modifierButton = new ImageButton(20,
                20,
                modifierSprites,
                (button) -> {
                    Balm.getNetworking()
                            .sendToServer(new EditWaystoneMessage(menu.getWaystone().getWaystoneUid(), textField.getValue(), visibilityButton.getVisibility()));
                    Balm.getNetworking().sendToServer(new RequestManageWaystoneModifiersMessage(menu.getWaystone().getPos()));
                },
                Component.literal("gui.waystones.waystone_settings.manage_modifiers"));
        modifierButton.setPosition(leftPos, y);
        addRenderableWidget(modifierButton);
        y += 24;

        saveButton = Button.builder(menu.canEdit() ? Component.translatable("gui.waystones.waystone_settings.save") : Component.translatable(
                        "gui.waystones.waystone_settings.close"), it -> onClose())
                .pos(leftPos + 176 / 2 - 50, y)
                .size(100, 20)
                .build();
        addRenderableWidget(saveButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.canEdit() && textField.isMouseOver(mouseX, mouseY) && button == 1) {
            textField.setValue("");
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textField.keyPressed(keyCode, scanCode, modifiers) || textField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                this.onClose();
            }

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        renderTooltip(guiGraphics, mouseX, mouseY);

        if (textField != null && textField.getValue().isEmpty()) {
            guiGraphics.drawString(Minecraft.getInstance().font,
                    Component.translatable("waystones.untitled_waystone"),
                    textField.getX() + 4,
                    textField.getY() + 6,
                    0x808080);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(font, title, 176 / 2, titleLabelY, 0xFFFFFFFF);
        final var error = menu.getError();
        if (error != null) {
            guiGraphics.drawCenteredString(font, error, 176 / 2, titleLabelY + 12, ChatFormatting.RED.getColor());
        }
        guiGraphics.drawString(font,
                Component.translatable("gui.waystones.waystone_settings.visibility." + visibilityButton.getVisibility().name().toLowerCase(Locale.ROOT)),
                24,
                visibilityButton.getY() - topPos + 6,
                0xFFFFFFFF,
                true);
        final var modifiersComponent = menu.getModifierCount() > 0
                ? Component.translatable("gui.waystones.waystone_settings.modifiers_active", menu.getModifierCount())
                : Component.translatable(
                "gui.waystones.waystone_settings.no_modifiers_active");
        guiGraphics.drawString(font,
                modifiersComponent,
                24,
                modifierButton.getY() - topPos + 6,
                menu.getModifierCount() > 0 ? 0xFf55Ff55 : 0xfFaAaAaA,
                true);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
    }

    @Override
    public void onClose() {
        if (textField != null && visibilityButton != null) {
            Balm.getNetworking()
                    .sendToServer(new EditWaystoneMessage(menu.getWaystone().getWaystoneUid(), textField.getValue(), visibilityButton.getVisibility()));
        }

        super.onClose();
    }

}
