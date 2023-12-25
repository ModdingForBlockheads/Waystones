package net.blay09.mods.waystones.api;

import net.blay09.mods.waystones.api.cost.Cost;
import net.blay09.mods.waystones.core.WarpMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IWaystoneTeleportContext {
    Entity getEntity();

    IWaystone getTargetWaystone();

    TeleportDestination getDestination();

    void setDestination(TeleportDestination destination);

    List<Mob> getLeashedEntities();

    List<Entity> getAdditionalEntities();

    void addAdditionalEntity(Entity additionalEntity);

    Optional<IWaystone> getFromWaystone();

    void setFromWaystone(@Nullable IWaystone fromWaystone);

    ItemStack getWarpItem();

    void setWarpItem(ItemStack warpItem);

    boolean isDimensionalTeleport();

    Cost getExperienceCost();

    void setExperienceCost(Cost cost);

    void setCooldown(int cooldown);

    int getCooldown();

    WarpMode getWarpMode();

    void setWarpMode(WarpMode warpMode);

    boolean playsSound();

    void setPlaysSound(boolean playsSound);

    boolean playsEffect();

    void setPlaysEffect(boolean playsEffect);

    default boolean consumesWarpItem() {
        return getWarpMode() != null && getWarpMode().consumesItem();
    };

    default void setConsumesWarpItem(boolean consumesWarpItem) { }

}
