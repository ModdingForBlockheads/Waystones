package net.blay09.mods.waystones.client.gui.widget;

import net.blay09.mods.waystones.api.WaystoneVisibility;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WaystoneVisbilityButton extends Button implements ITooltipProvider {

    private final WidgetSprites ACTIVATION_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("waystones/visibility_button_activation"),
            ResourceLocation.withDefaultNamespace("waystones/visibility_button_activation_highlighted"));
    private final WidgetSprites GLOBAL_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("waystones/visibility_button_global"),
            ResourceLocation.withDefaultNamespace("waystones/visibility_button_global_highlighted"));
    private final WidgetSprites SHARD_ONLY_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("waystones/visibility_button_shard_only"),
            ResourceLocation.withDefaultNamespace("waystones/visibility_button_shard_only_highlighted"));
    private final WidgetSprites SHARESTONE_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("waystones/visibility_button_sharestone"),
            ResourceLocation.withDefaultNamespace("waystones/visibility_button_sharestone_highlighted"));

    private final List<WaystoneVisibility> options;
    private final boolean canEdit;
    private WaystoneVisibility visibility;

    public WaystoneVisbilityButton(int x, int y, WaystoneVisibility visibility, List<WaystoneVisibility> options, boolean canEdit) {
        super(x, y, 18, 18, Component.empty(), button -> {
        }, Button.DEFAULT_NARRATION);
        this.options = options;
        this.visibility = visibility;
        this.canEdit = canEdit;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        final var sprite = getSprites().get(this.isActive(), this.isHoveredOrFocused());
        guiGraphics.blitSprite(RenderType::guiTextured, sprite, getX(), getY(), 20, 20);
    }

    @Override
    public boolean shouldShowTooltip() {
        return isHovered;
    }

    @Override
    public List<Component> getTooltipComponents() {
        final var visibilityValueComponent = Component.translatable("tooltip.waystones.visibility." + visibility.name().toLowerCase(Locale.ROOT))
                .withStyle(ChatFormatting.WHITE);
        final var result = new ArrayList<Component>();
        result.add(Component.translatable("tooltip.waystones.visibility", visibilityValueComponent).withStyle(ChatFormatting.YELLOW));
        if (!canEdit) {
            result.add(Component.translatable("tooltip.waystones.edit_restricted").withStyle(ChatFormatting.RED));
        }
        return result;
    }

    public WaystoneVisibility getVisibility() {
        return visibility;
    }

    private WidgetSprites getSprites() {
        return switch (visibility) {
            case ACTIVATION -> ACTIVATION_SPRITES;
            case GLOBAL -> GLOBAL_SPRITES;
            case SHARD_ONLY -> SHARD_ONLY_SPRITES;
            case ORANGE_SHARESTONE, GRAY_SHARESTONE, LIGHT_GRAY_SHARESTONE, BLACK_SHARESTONE, RED_SHARESTONE, GREEN_SHARESTONE, BROWN_SHARESTONE,
                 BLUE_SHARESTONE, PURPLE_SHARESTONE, CYAN_SHARESTONE, PINK_SHARESTONE, LIME_SHARESTONE, YELLOW_SHARESTONE, LIGHT_BLUE_SHARESTONE,
                 MAGENTA_SHARESTONE -> SHARESTONE_SPRITES;
        };
    }

    @Override
    public void onPress() {
        if (canEdit) {
            final var index = options.indexOf(visibility);
            visibility = options.get((index + 1) % options.size());
        }
    }
}
