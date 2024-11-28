package net.blay09.mods.waystones.block;

import com.mojang.serialization.MapCodec;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.block.entity.ModBlockEntities;
import net.blay09.mods.waystones.block.entity.WarpPlateBlockEntity;
import net.blay09.mods.waystones.core.WaystoneProxy;
import net.blay09.mods.waystones.tag.ModItemTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class WarpPlateBlock extends WaystoneBlockBase {

    public static final MapCodec<WarpPlateBlock> CODEC = simpleCodec(WarpPlateBlock::new);

    public static int getColorForBlock(@Nullable BlockAndTintGetter view, BlockPos pos) {
        if (view == null || !(view.getBlockEntity(pos) instanceof WarpPlateBlockEntity warpPlate)) {
            return 0xFFFFFFFF;
        }

        final var name = getGalacticIdentifier(warpPlate.getWaystone());
        final var color = getColorForName(name).getColor();
        return color != null ? color : 0xFFFFFFFF;
    }

    public enum WarpPlateStatus implements StringRepresentable {
        EMPTY,
        IDLE,
        ATTUNING,
        WARPING,
        WARPING_INVALID,
        LOCKED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final Style GALACTIC_STYLE = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("minecraft", "alt"));

    private static final VoxelShape SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
            box(3.0, 1.0, 3.0, 13.0, 2.0, 13.0)
    ).optimize();

    public static final EnumProperty<WarpPlateStatus> STATUS = EnumProperty.create("status", WarpPlateStatus.class);

    public WarpPlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(STATUS, WarpPlateStatus.EMPTY));
    }

    @Override
    protected boolean canSilkTouch() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATUS);
    }

    @Override
    public void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
        if (entity.getX() >= pos.getX() && entity.getX() < pos.getX() + 1
                && entity.getY() >= pos.getY() && entity.getY() < pos.getY() + 1
                && entity.getZ() >= pos.getZ() && entity.getZ() < pos.getZ() + 1
                && !world.isClientSide) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof WarpPlateBlockEntity) {
                ((WarpPlateBlockEntity) tileEntity).onEntityCollision(entity);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        final var status = state.getValue(STATUS);
        if (status == WarpPlateStatus.WARPING) {
            for (int i = 0; i < 50; i++) {
                world.addParticle(ParticleTypes.CRIMSON_SPORE,
                        pos.getX() + Math.random(),
                        pos.getY() + Math.random() * 2,
                        pos.getZ() + Math.random(),
                        0f,
                        0f,
                        0f);
                world.addParticle(ParticleTypes.PORTAL,
                        pos.getX() + Math.random(),
                        pos.getY() + Math.random() * 2,
                        pos.getZ() + Math.random(),
                        0f,
                        0f,
                        0f);
            }
        } else if (status == WarpPlateStatus.WARPING_INVALID) {
            for (int i = 0; i < 10; i++) {
                world.addParticle(ParticleTypes.SMOKE, pos.getX() + Math.random(), pos.getY(), pos.getZ() + Math.random(), 0f, 0.01f, 0f);
            }
        } else if(status == WarpPlateStatus.ATTUNING) {
            for (int i = 0; i < 10; i++) {
                world.addParticle(ParticleTypes.WARPED_SPORE, pos.getX() + Math.random(), pos.getY(), pos.getZ() + Math.random(), 0f, 0f, 0f);
            }
            for (int i = 0; i < 10; i++) {
                world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + Math.random(), pos.getY(), pos.getZ() + Math.random(), 0f, 0f, 0f);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WarpPlateBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (itemStack.is(ModItemTags.WARP_SHARDS)) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof WarpPlateBlockEntity warpPlate) {
                final var existing = warpPlate.getShardItem();
                if (existing.isEmpty()) {
                    warpPlate.setShardItem(player.getAbilities().instabuild ? itemStack.copy().split(1) : itemStack.split(1));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof WarpPlateBlockEntity warpPlate) {
                final var itemStack = warpPlate.getShardItem();
                if (!itemStack.isEmpty()) {
                    final var itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                    level.addFreshEntity(itemEntity);
                    warpPlate.setShardItem(ItemStack.EMPTY);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof WarpPlateBlockEntity warpPlate) {
            warpPlate.getSettingsMenuProvider().ifPresent(it -> Balm.getNetworking().openGui(player, it));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void addWaystoneNameToTooltip(List<Component> tooltip, WaystoneProxy waystone) {
        tooltip.add(getGalacticName(waystone));
    }

    public static ChatFormatting getColorForName(String name) {
        int colorIndex = Math.abs(name.hashCode()) % 15;
        ChatFormatting textFormatting = ChatFormatting.getById(colorIndex);
        if (textFormatting == ChatFormatting.GRAY) {
            return ChatFormatting.LIGHT_PURPLE;
        } else if (textFormatting == ChatFormatting.DARK_GRAY) {
            return ChatFormatting.DARK_PURPLE;
        } else if (textFormatting == ChatFormatting.BLACK) {
            return ChatFormatting.GOLD;
        }
        return textFormatting != null ? textFormatting : ChatFormatting.GRAY;
    }

    public static String getGalacticIdentifier(Waystone waystone) {
        final var intermediate = waystone.getWaystoneUid().toString().replaceAll("[0-9\\-]", "");
        return intermediate.substring(0, Math.min(8, intermediate.length()));
    }

    public static Component getGalacticName(Waystone waystone) {
        final var name = getGalacticIdentifier(waystone);
        return Component.literal(name).withStyle(WarpPlateBlock.getColorForName(name)).withStyle(GALACTIC_STYLE);
    }

    @Override
    protected boolean shouldOpenMenuWhenPlaced() {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        final var tickingBlockEntityType = ModBlockEntities.warpPlate.get();
        if (tickingBlockEntityType == null) {
            return null;
        }
        return world.isClientSide ? null : createTickerHelper(type,
                tickingBlockEntityType,
                (level, pos, state2, blockEntity) -> blockEntity.serverTick());
    }
}
