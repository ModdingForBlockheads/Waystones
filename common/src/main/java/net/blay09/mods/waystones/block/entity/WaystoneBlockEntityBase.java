package net.blay09.mods.waystones.block.entity;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.block.entity.CustomRenderBoundingBox;
import net.blay09.mods.balm.api.block.entity.OnLoadHandler;
import net.blay09.mods.balm.api.container.BalmContainerProvider;
import net.blay09.mods.balm.api.container.DefaultContainer;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.api.error.WaystoneEditError;
import net.blay09.mods.waystones.block.WaystoneBlock;
import net.blay09.mods.waystones.block.WaystoneBlockBase;
import net.blay09.mods.waystones.component.ModComponents;
import net.blay09.mods.waystones.core.*;
import net.blay09.mods.waystones.item.ModItems;
import net.blay09.mods.waystones.menu.WaystoneEditMenu;
import net.blay09.mods.waystones.menu.WaystoneModifierMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class WaystoneBlockEntityBase extends BalmBlockEntity implements OnLoadHandler, CustomRenderBoundingBox, BalmContainerProvider {

    protected final DefaultContainer container = new DefaultContainer(5) {
        @Override
        public int getMaxStackSize(ItemStack itemStack) {
            if (itemStack.is(ModItems.dormantShard)) {
                return 1;
            }

            return super.getMaxStackSize(itemStack);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack itemStack) {
            if (itemStack.is(ModItems.dormantShard)) {
                return slot == 0;
            }

            return super.canPlaceItem(slot, itemStack);
        }

        @Override
        public void setChanged() {
            onInventoryChanged();
        }
    };

    protected void onInventoryChanged() {
    }

    private Waystone waystone = InvalidWaystone.INSTANCE;
    private UUID waystoneUid;
    private boolean shouldNotInitialize;
    private boolean silkTouched;

    public WaystoneBlockEntityBase(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("Items", container.serialize(provider));

        if (waystone.isValid()) {
            tag.put("UUID", NbtUtils.createUUID(waystone.getWaystoneUid()));
        } else if (waystoneUid != null) {
            tag.put("UUID", NbtUtils.createUUID(waystoneUid));
        }
    }

    @Override
    public void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        if (compound.contains("Items")) {
            container.deserialize(compound.getCompound("Items"), provider);
        }

        if (compound.contains("UUID", Tag.TAG_INT_ARRAY)) {
            waystoneUid = NbtUtils.loadUUID(Objects.requireNonNull(compound.get("UUID")));
        }

        if (compound.contains("Waystone", Tag.TAG_COMPOUND)) {
            var syncedWaystone = WaystoneImpl.read(compound.getCompound("Waystone"), provider);
            WaystoneManagerImpl.get(null).updateWaystone(syncedWaystone);
            waystone = new WaystoneProxy(null, syncedWaystone.getWaystoneUid());
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        final var waystoneUidComponent = input.get(ModComponents.waystone.get());
        if (waystoneUidComponent != null) {
            waystoneUid = waystoneUidComponent;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        builder.set(ModComponents.waystone.get(), waystone.isValid() ? waystone.getWaystoneUid() : waystoneUid);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        tag.put("Waystone", WaystoneImpl.write(getWaystone(), new CompoundTag(), level.registryAccess()));
    }

    @Override
    public void onLoad() {
        Waystone backingWaystone = waystone;
        if (waystone instanceof WaystoneProxy) {
            backingWaystone = ((WaystoneProxy) waystone).getBackingWaystone();
        }
        if (backingWaystone instanceof WaystoneImpl && level != null) {
            ((WaystoneImpl) backingWaystone).setDimension(level.dimension());
            ((WaystoneImpl) backingWaystone).setPos(worldPosition);
        }
        sync();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                worldPosition.getX() + 1,
                worldPosition.getY() + 2,
                worldPosition.getZ() + 1);
    }

    public Waystone getWaystone() {
        if (!waystone.isValid() && level != null && !level.isClientSide && !shouldNotInitialize) {
            if (waystoneUid != null) {
                waystone = new WaystoneProxy(level.getServer(), waystoneUid);
            }

            if (!waystone.isValid()) {
                BlockState state = getBlockState();
                if (state.getBlock() instanceof WaystoneBlockBase) {
                    DoubleBlockHalf half = state.hasProperty(WaystoneBlockBase.HALF) ? state.getValue(WaystoneBlockBase.HALF) : DoubleBlockHalf.LOWER;
                    WaystoneOrigin origin = state.hasProperty(WaystoneBlockBase.ORIGIN) ? state.getValue(WaystoneBlockBase.ORIGIN) : WaystoneOrigin.UNKNOWN;
                    if (half == DoubleBlockHalf.LOWER) {
                        initializeWaystone((ServerLevelAccessor) Objects.requireNonNull(level), null, origin);
                    } else if (half == DoubleBlockHalf.UPPER) {
                        BlockEntity blockEntity = level.getBlockEntity(worldPosition.below());
                        if (blockEntity instanceof WaystoneBlockEntityBase) {
                            initializeFromBase(((WaystoneBlockEntityBase) blockEntity));
                        }
                    }
                }
            }

            if (waystone.isValid()) {
                waystoneUid = waystone.getWaystoneUid();
                sync();
            }
        }

        return waystone;
    }

    protected abstract ResourceLocation getWaystoneType();

    public void initializeWaystone(ServerLevelAccessor world, @Nullable LivingEntity player, WaystoneOrigin origin) {
        WaystoneImpl waystone = new WaystoneImpl(getWaystoneType(),
                UUID.randomUUID(),
                world.getLevel().dimension(),
                worldPosition,
                origin,
                player != null ? player.getUUID() : null);
        WaystoneManagerImpl.get(world.getServer()).addWaystone(waystone);
        this.waystone = waystone;
        setChanged();
        sync();
    }

    public void initializeFromExisting(ServerLevelAccessor world, WaystoneImpl existingWaystone, ItemStack itemStack) {
        waystone = existingWaystone;
        existingWaystone.setDimension(world.getLevel().dimension());
        existingWaystone.setPos(worldPosition);
        existingWaystone.setTransient(false);
        WaystoneManagerImpl.get(world.getServer()).updateWaystone(waystone);
        setChanged();
        sync();
    }

    public void initializeFromBase(WaystoneBlockEntityBase tileEntity) {
        waystone = tileEntity.getWaystone();
        setChanged();
        sync();
    }

    public void uninitializeWaystone() {
        if (waystone.isValid()) {
            WaystoneManagerImpl.get(level.getServer()).removeWaystone(waystone);
            PlayerWaystoneManager.removeKnownWaystone(level.getServer(), waystone);
            WaystoneSyncManager.sendWaystoneRemovalToAll(level.getServer(), waystone, true);
        }

        waystone = InvalidWaystone.INSTANCE;
        shouldNotInitialize = true;

        DoubleBlockHalf half = getBlockState().getValue(WaystoneBlock.HALF);
        BlockPos otherPos = half == DoubleBlockHalf.UPPER ? worldPosition.below() : worldPosition.above();
        BlockEntity blockEntity = Objects.requireNonNull(level).getBlockEntity(otherPos);
        if (blockEntity instanceof WaystoneBlockEntityBase) {
            WaystoneBlockEntityBase waystoneTile = (WaystoneBlockEntityBase) blockEntity;
            waystoneTile.waystone = InvalidWaystone.INSTANCE;
            waystoneTile.shouldNotInitialize = true;
        }

        setChanged();
        sync();
    }

    public void setSilkTouched(boolean silkTouched) {
        this.silkTouched = silkTouched;
    }

    public boolean isSilkTouched() {
        return silkTouched;
    }

    public Optional<MenuProvider> getSelectionMenuProvider() {
        return Optional.empty();
    }

    public abstract Component getName();

    public Optional<MenuProvider> getSettingsMenuProvider() {
        return Optional.of(new BalmMenuProvider<WaystoneEditMenu.Data>() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.waystones.waystone_settings", getName());
            }

            @Override
            public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
                final var error = WaystonePermissionManager.mayEditWaystone(player, player.level(), getWaystone());
                return new WaystoneEditMenu(i,
                        getWaystone(),
                        WaystoneBlockEntityBase.this,
                        playerInventory,
                        getModifierCount(),
                        error.map(WaystoneEditError::getTranslationKey).map(Component::translatable).orElse(null));
            }

            @Override
            public WaystoneEditMenu.Data getScreenOpeningData(ServerPlayer serverPlayer) {
                final var error = WaystonePermissionManager.mayEditWaystone(serverPlayer, serverPlayer.level(), getWaystone());
                return new WaystoneEditMenu.Data(worldPosition, getWaystone(), getModifierCount(), error.map(WaystoneEditError::getTranslationKey).map(Component::translatable));
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, WaystoneEditMenu.Data> getScreenStreamCodec() {
                return WaystoneEditMenu.STREAM_CODEC;
            }
        });
    }

    public Optional<MenuProvider> getModifierMenuProvider() {
        return Optional.of(new BalmMenuProvider<BlockPos>() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.waystones.waystone_modifiers");
            }

            @Override
            public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
                return new WaystoneModifierMenu(i, WaystoneBlockEntityBase.this, playerInventory);
            }

            @Override
            public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
                return worldPosition;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, BlockPos> getScreenStreamCodec() {
                return BlockPos.STREAM_CODEC.cast();
            }
        });
    }

    public Collection<? extends Waystone> getAuxiliaryTargets() {
        final var result = new ArrayList<Waystone>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            final var item = container.getItem(i);
            WaystonesAPI.getBoundWaystone(null, item).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    public void applyModifierEffects(Entity entity) {
        int fireSeconds = 0;
        int poisonSeconds = 0;
        int blindSeconds = 0;
        int featherFallSeconds = 0;
        int fireResistanceSeconds = 0;
        int witherSeconds = 0;
        int potency = 1;
        List<ItemStack> curativeItems = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.getItem() == Items.BLAZE_POWDER) {
                fireSeconds += itemStack.getCount();
            } else if (itemStack.getItem() == Items.POISONOUS_POTATO) {
                poisonSeconds += itemStack.getCount();
            } else if (itemStack.getItem() == Items.INK_SAC) {
                blindSeconds += itemStack.getCount();
            } else if (itemStack.getItem() == Items.MILK_BUCKET || itemStack.getItem() == Items.HONEY_BLOCK) {
                curativeItems.add(itemStack);
            } else if (itemStack.getItem() == Items.DIAMOND) {
                potency = Math.min(4, potency + itemStack.getCount());
            } else if (itemStack.getItem() == Items.FEATHER) {
                featherFallSeconds = Math.min(8, featherFallSeconds + itemStack.getCount());
            } else if (itemStack.getItem() == Items.MAGMA_CREAM) {
                fireResistanceSeconds = Math.min(8, fireResistanceSeconds + itemStack.getCount());
            } else if (itemStack.getItem() == Items.WITHER_ROSE) {
                witherSeconds += itemStack.getCount();
            }
        }

        if (entity instanceof LivingEntity) {
            if (fireSeconds > 0) {
                entity.setRemainingFireTicks(fireSeconds * 20);
            }
            if (poisonSeconds > 0) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.POISON, poisonSeconds * 20, potency));
            }
            if (blindSeconds > 0) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindSeconds * 20, potency));
            }
            if (featherFallSeconds > 0) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, featherFallSeconds * 20, potency));
            }
            if (fireResistanceSeconds > 0) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, fireResistanceSeconds * 20, potency));
            }
            if (witherSeconds > 0) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WITHER, witherSeconds * 20, potency));
            }
            for (ItemStack curativeItem : curativeItems) {
                Balm.getHooks().curePotionEffects((LivingEntity) entity, curativeItem);
            }
        }
    }

    private int getModifierCount() {
        // TODO I'm sorry, Future Blay will create a proper system for these modifiers (I promise)
        var modifiers = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.getItem() == Items.BLAZE_POWDER) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.POISONOUS_POTATO) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.INK_SAC) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.MILK_BUCKET || itemStack.getItem() == Items.HONEY_BLOCK) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.DIAMOND) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.FEATHER) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.MAGMA_CREAM) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.WITHER_ROSE) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.QUARTZ) {
                modifiers += 1;
            } else if (itemStack.getItem() == Items.SPIDER_EYE) {
                modifiers += 1;
            }
        }
        return modifiers;
    }
}
