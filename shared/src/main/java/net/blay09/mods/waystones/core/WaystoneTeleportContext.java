package net.blay09.mods.waystones.core;

import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.IWaystoneTeleportContext;
import net.blay09.mods.waystones.api.TeleportDestination;
import net.blay09.mods.waystones.api.cost.Cost;
import net.blay09.mods.waystones.cost.NoCost;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaystoneTeleportContext implements IWaystoneTeleportContext {
    private final Entity entity;
    private final IWaystone targetWaystone;

    private final List<Entity> additionalEntities = new ArrayList<>();
    private final List<Mob> leashedEntities = new ArrayList<>();

    private TeleportDestination destination;
    private IWaystone fromWaystone;

    private WarpMode warpMode = WarpMode.CUSTOM;
    private ItemStack warpItem = ItemStack.EMPTY;
    @Nullable
    private Boolean consumesWarpItem; // nullable for now so we can fall back to legacy warp mode implementation

    private Cost xpCost = NoCost.INSTANCE;

    private int cooldown;
    private boolean playsSound = true;
    private boolean playsEffect = true;

    public WaystoneTeleportContext(Entity entity, IWaystone targetWaystone, TeleportDestination destination) {
        this.entity = entity;
        this.targetWaystone = targetWaystone;
        this.destination = destination;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public IWaystone getTargetWaystone() {
        return targetWaystone;
    }

    @Override
    public TeleportDestination getDestination() {
        return destination;
    }

    @Override
    public void setDestination(TeleportDestination destination) {
        this.destination = destination;
    }

    @Override
    public List<Mob> getLeashedEntities() {
        return leashedEntities;
    }

    @Override
    public List<Entity> getAdditionalEntities() {
        return additionalEntities;
    }

    @Override
    public void addAdditionalEntity(Entity additionalEntity) {
        this.additionalEntities.add(additionalEntity);
    }

    @Override
    @Nullable
    public Optional<IWaystone> getFromWaystone() {
        return Optional.ofNullable(fromWaystone);
    }

    @Override
    public void setFromWaystone(@Nullable IWaystone fromWaystone) {
        this.fromWaystone = fromWaystone;
    }

    @Override
    public ItemStack getWarpItem() {
        return warpItem;
    }

    @Override
    public void setWarpItem(ItemStack warpItem) {
        this.warpItem = warpItem;
    }

    @Override
    public boolean isDimensionalTeleport() {
        return targetWaystone.getDimension() != entity.level().dimension();
    }

    @Override
    public Cost getExperienceCost() {
        return xpCost;
    }

    @Override
    public void setExperienceCost(Cost cost) {
        this.xpCost = cost;
    }

    @Override
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public WarpMode getWarpMode() {
        return warpMode;
    }

    @Override
    public void setWarpMode(WarpMode warpMode) {
        this.warpMode = warpMode;
    }

    @Override
    public boolean playsSound() {
        return playsSound;
    }

    @Override
    public void setPlaysSound(boolean playsSound) {
        this.playsSound = playsSound;
    }

    @Override
    public boolean playsEffect() {
        return playsEffect;
    }

    @Override
    public void setPlaysEffect(boolean playsEffect) {
        this.playsEffect = playsEffect;
    }

    @Override
    public boolean consumesWarpItem() {
        return this.consumesWarpItem == null ? getWarpMode().consumesItem() : this.consumesWarpItem;
    }

    @Override
    public void setConsumesWarpItem(boolean consumesWarpItem) {
        this.consumesWarpItem = consumesWarpItem;
    }
}
