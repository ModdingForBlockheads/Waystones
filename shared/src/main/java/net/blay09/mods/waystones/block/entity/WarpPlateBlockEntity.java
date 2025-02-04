package net.blay09.mods.waystones.block.entity;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.container.ContainerUtils;
import net.blay09.mods.balm.api.container.ImplementedContainer;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.IMutableWaystone;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.block.WarpPlateBlock;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.*;
import net.blay09.mods.waystones.menu.WarpPlateContainer;
import net.blay09.mods.waystones.recipe.ModRecipes;
import net.blay09.mods.waystones.recipe.WarpPlateRecipe;
import net.blay09.mods.waystones.tag.ModItemTags;
import net.blay09.mods.waystones.worldgen.namegen.NameGenerationMode;
import net.blay09.mods.waystones.worldgen.namegen.NameGenerator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class WarpPlateBlockEntity extends WaystoneBlockEntityBase implements ImplementedContainer {

    private static final Logger logger = LoggerFactory.getLogger(WarpPlateBlockEntity.class);

    private final WeakHashMap<Entity, Integer> ticksPassedPerEntity = new WeakHashMap<>();

    private final Random random = new Random();
    private final ContainerData dataAccess;

    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);

    private int attunementTicks;
    private boolean readyForAttunement;
    private boolean completedFirstAttunement;
    private int lastAttunementSlot;

    public WarpPlateBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.warpPlate.get(), blockPos, blockState);
        dataAccess = new ContainerData() {
            @Override
            public int get(int i) {
                if (i == 0) {
                    return attunementTicks;
                } else if (i == 1) {
                    return getMaxAttunementTicks();
                } else if (i == 2) {
                    return isCompletedFirstAttunement() ? 1 : 0;
                }
                return 0;
            }

            @Override
            public void set(int i, int j) {
                if (i == 0) {
                    attunementTicks = j;
                } else if (i == 1) {
                    // ignored
                } else if (i == 2) {
                    // ignored
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        if (!completedFirstAttunement) {
            return ItemStack.EMPTY;
        }
        return ImplementedContainer.super.removeItem(slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (!completedFirstAttunement) {
            return ItemStack.EMPTY;
        }

        return ImplementedContainer.super.removeItemNoUpdate(slot);
    }

    @Override
    public void initializeFromExisting(ServerLevelAccessor world, Waystone existingWaystone, ItemStack itemStack) {
        super.initializeFromExisting(world, existingWaystone, itemStack);

        CompoundTag tag = itemStack.getTag();
        completedFirstAttunement = tag != null && tag.getBoolean("CompletedFirstAttunement");

        if (!completedFirstAttunement) {
            initializeInventory(world);
        }
    }

    @Override
    public void initializeWaystone(ServerLevelAccessor world, @Nullable LivingEntity player, WaystoneOrigin origin) {
        super.initializeWaystone(world, player, origin);

        // Warp Plates generate a name on placement always
        IWaystone waystone = getWaystone();
        if (waystone instanceof IMutableWaystone) {
            String name = NameGenerator.get(world.getServer()).getName(waystone, world.getRandom(), NameGenerationMode.RANDOM_ONLY);
            ((IMutableWaystone) waystone).setName(name);
        }

        WaystoneSyncManager.sendWaystoneUpdateToAll(world.getServer(), waystone);

        initializeInventory(world);
    }

    private void initializeInventory(ServerLevelAccessor levelAccessor) {
        WarpPlateRecipe initializingRecipe = levelAccessor.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.warpPlateRecipeType)
                .stream()
                .filter(recipe -> recipe.getId().getNamespace().equals(Waystones.MOD_ID) && recipe.getId().getPath().equals("attuned_shard"))
                .findFirst()
                .orElse(null);
        if (initializingRecipe == null) {
            logger.error("Failed to find Warp Plate recipe for initial attunement");
            completedFirstAttunement = true;
            return;
        }

        for (int i = 0; i < 5; i++) {
            final var ingredient = initializingRecipe.getIngredients().get(i);
            final var ingredientItems = ingredient.getItems();
            final var ingredientItem = ingredientItems.length > 0 ? ingredientItems[0] : ItemStack.EMPTY;
            setItem(i, ingredientItem.copy());
        }
    }

    @Override
    protected ResourceLocation getWaystoneType() {
        return WaystoneTypes.WARP_PLATE;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        ContainerHelper.saveAllItems(tag, items);
        tag.putBoolean("ReadyForAttunement", readyForAttunement);
        tag.putBoolean("CompletedFirstAttunement", completedFirstAttunement);
        tag.putInt("LastAttunementSlot", lastAttunementSlot);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        ContainerHelper.loadAllItems(compound, items);
        readyForAttunement = compound.getBoolean("ReadyForAttunement");
        completedFirstAttunement = compound.getBoolean("CompletedFirstAttunement");
        lastAttunementSlot = compound.getInt("LastAttunementSlot");
    }

    @Override
    public BalmMenuProvider getMenuProvider() {
        return new BalmMenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.waystones.warp_plate");
            }

            @Override
            public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
                WarpPlateBlockEntity.this.markReadyForAttunement();
                return new WarpPlateContainer(i, playerInventory, getWaystone(), WarpPlateBlockEntity.this, dataAccess);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                Waystone.write(buf, getWaystone());
            }
        };
    }

    @Override
    public MenuProvider getSettingsMenuProvider() {
        return null;
    }

    public void onEntityCollision(Entity entity) {
        Integer ticksPassed = ticksPassedPerEntity.putIfAbsent(entity, 0);
        if (ticksPassed == null || ticksPassed != -1) {
            final var status = getTargetWaystone().filter(IWaystone::isValid)
                    .map(it -> WarpPlateBlock.WarpPlateStatus.ACTIVE)
                    .orElse(WarpPlateBlock.WarpPlateStatus.INVALID);
            level.setBlock(worldPosition, getBlockState()
                    .setValue(WarpPlateBlock.ACTIVE, true)
                    .setValue(WarpPlateBlock.STATUS, status), 3);
        }
    }

    private boolean isEntityOnWarpPlate(Entity entity) {
        return entity.getX() >= worldPosition.getX() && entity.getX() < worldPosition.getX() + 1
                && entity.getY() >= worldPosition.getY() && entity.getY() < worldPosition.getY() + 1
                && entity.getZ() >= worldPosition.getZ() && entity.getZ() < worldPosition.getZ() + 1;
    }

    public void serverTick() {
        WarpPlateRecipe recipe = trySelectRecipe();
        if (recipe != null) {
            attunementTicks++;

            if (attunementTicks >= getMaxAttunementTicks()) {
                attunementTicks = 0;
                ItemStack attunedShard = recipe.assemble(this, RegistryAccess.EMPTY);
                WaystonesAPI.setBoundWaystone(attunedShard, getWaystone());
                ItemStack centerStack = getItem(0);
                if (centerStack.getCount() > 1) {
                    centerStack = centerStack.copyWithCount(centerStack.getCount() - 1);
                    if (!Minecraft.getInstance().player.getInventory().add(centerStack)) {
                        Minecraft.getInstance().player.drop(centerStack, false);
                    }
                }
                setItem(0, attunedShard);
                for (int i = 1; i <= 4; i++) {
                    getItem(i).shrink(1);
                }
                this.completedFirstAttunement = true;
            }
        } else {
            attunementTicks = 0;
        }

        if (getBlockState().getValue(WarpPlateBlock.STATUS) != WarpPlateBlock.WarpPlateStatus.IDLE) {
            AABB boundsAbove = new AABB(worldPosition.getX(),
                    worldPosition.getY(),
                    worldPosition.getZ(),
                    worldPosition.getX() + 1,
                    worldPosition.getY() + 1,
                    worldPosition.getZ() + 1);
            List<Entity> entities = level.getEntities((Entity) null, boundsAbove, EntitySelector.ENTITY_STILL_ALIVE);
            if (entities.isEmpty()) {
                level.setBlock(worldPosition, getBlockState()
                        .setValue(WarpPlateBlock.ACTIVE, false)
                        .setValue(WarpPlateBlock.STATUS, WarpPlateBlock.WarpPlateStatus.IDLE), 3);
                ticksPassedPerEntity.clear();
            }
        }

        final var useTime = getWarpPlateUseTime();
        Iterator<Map.Entry<Entity, Integer>> iterator = ticksPassedPerEntity.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, Integer> entry = iterator.next();
            Entity entity = entry.getKey();
            Integer ticksPassed = entry.getValue();
            if (!entity.isAlive() || !isEntityOnWarpPlate(entity)) {
                iterator.remove();
            } else if (ticksPassed > useTime) {
                ItemStack targetAttunementStack = getTargetAttunementStack();
                IWaystone targetWaystone = WaystonesAPI.getBoundWaystone(targetAttunementStack).orElse(null);
                if (targetWaystone != null && targetWaystone.isValid()) {
                    teleportToWarpPlate(entity, targetWaystone, targetAttunementStack);
                }

                if (entity instanceof Player) {
                    if (targetWaystone == null) {
                        var chatComponent = Component.translatable("chat.waystones.warp_plate_has_no_target");
                        chatComponent.withStyle(ChatFormatting.DARK_RED);
                        ((Player) entity).displayClientMessage(chatComponent, true);
                    } else if (!targetWaystone.isValid()) {
                        var chatComponent = Component.translatable("chat.waystones.warp_plate_has_invalid_target");
                        chatComponent.withStyle(ChatFormatting.DARK_RED);
                        ((Player) entity).displayClientMessage(chatComponent, true);
                    }
                }

                iterator.remove();
            } else if (ticksPassed != -1) {
                entry.setValue(ticksPassed + 1);
            }
        }
    }

    private int getWarpPlateUseTime() {
        float useTimeMultiplier = 1;
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack itemStack = getItem(i);
            if (itemStack.getItem() == Items.AMETHYST_SHARD) {
                useTimeMultiplier -= 0.016f * itemStack.getCount();
            } else if (itemStack.getItem() == Items.SLIME_BALL) {
                useTimeMultiplier += 0.016f * itemStack.getCount();
            }
        }

        int configuredUseTime = WaystonesConfig.getActive().cooldowns.warpPlateUseTime;
        return Mth.clamp((int) (configuredUseTime * useTimeMultiplier), 1, configuredUseTime * 2);
    }

    private void teleportToWarpPlate(Entity entity, IWaystone targetWaystone, ItemStack targetAttunementStack) {
        WaystonesAPI.createDefaultTeleportContext(entity, targetWaystone, WarpMode.WARP_PLATE, getWaystone())
                .flatMap(ctx -> {
                    ctx.setWarpItem(targetAttunementStack);
                    ctx.setConsumesWarpItem(targetAttunementStack.is(ModItemTags.SINGLE_USE_WARP_SHARDS));
                    return PlayerWaystoneManager.tryTeleport(ctx);
                })
                .ifRight(PlayerWaystoneManager.informRejectedTeleport(entity))
                .ifLeft(entities -> entities.forEach(this::applyWarpPlateEffects))
                .left();
    }

    private void applyWarpPlateEffects(Entity entity) {
        int fireSeconds = 0;
        int poisonSeconds = 0;
        int blindSeconds = 0;
        int featherFallSeconds = 0;
        int fireResistanceSeconds = 0;
        int witherSeconds = 0;
        int potency = 1;
        List<ItemStack> curativeItems = new ArrayList<>();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack itemStack = getItem(i);
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
                entity.setSecondsOnFire(fireSeconds);
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

    @Nullable
    private WarpPlateRecipe trySelectRecipe() {
        if (!readyForAttunement || level == null) {
            return null;
        }
        if (getItem(0).getCount() > 1) {
            return null; //prevents crafting when more than 1 ingredient is present
        }

        return level.getRecipeManager().getRecipeFor(ModRecipes.warpPlateRecipeType, this, level).orElse(null);
    }

    public ItemStack getTargetAttunementStack() {
        boolean shouldRoundRobin = false;
        boolean shouldPrioritizeSingleUseShards = false;
        List<ItemStack> attunedShards = new ArrayList<>();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack itemStack = getItem(i);
            if (itemStack.is(ModItemTags.WARP_SHARDS)) {
                IWaystone waystoneAttunedTo = WaystonesAPI.getBoundWaystone(itemStack).orElse(null);
                if (waystoneAttunedTo != null && !waystoneAttunedTo.getWaystoneUid().equals(getWaystone().getWaystoneUid())) {
                    attunedShards.add(itemStack);
                }
            } else if (itemStack.getItem() == Items.QUARTZ) {
                shouldRoundRobin = true;
            } else if (itemStack.getItem() == Items.SPIDER_EYE) {
                shouldPrioritizeSingleUseShards = true;
            }
        }
        if (shouldPrioritizeSingleUseShards && attunedShards.stream().anyMatch(stack -> stack.is(ModItemTags.SINGLE_USE_WARP_SHARDS))) {
            attunedShards.removeIf(stack -> !stack.is(ModItemTags.SINGLE_USE_WARP_SHARDS));
        }

        if (!attunedShards.isEmpty()) {
            lastAttunementSlot = (lastAttunementSlot + 1) % attunedShards.size();
            return shouldRoundRobin ? attunedShards.get(lastAttunementSlot) : attunedShards.get(random.nextInt(attunedShards.size()));
        }

        return ItemStack.EMPTY;
    }

    public Optional<IWaystone> getTargetWaystone() {
        return WaystonesAPI.getBoundWaystone(getTargetAttunementStack());
    }

    public int getMaxAttunementTicks() {
        return 30;
    }

    public void markReadyForAttunement() {
        readyForAttunement = true;
    }

    public void markEntityForCooldown(Entity entity) {
        ticksPassedPerEntity.put(entity, -1);
    }

    public boolean isCompletedFirstAttunement() {
        return completedFirstAttunement;
    }

    public ContainerData getContainerData() {
        return dataAccess;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0 && !getItem(0).isEmpty()) {
            return false; //prevents hoppers to add items in an occupied center slot
        }
        return ImplementedContainer.super.canPlaceItem(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
}
