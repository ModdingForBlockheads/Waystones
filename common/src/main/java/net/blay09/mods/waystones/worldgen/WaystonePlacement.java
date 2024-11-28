package net.blay09.mods.waystones.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;
import java.util.stream.Stream;

public class WaystonePlacement extends PlacementModifier {

    public static final MapCodec<WaystonePlacement> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter((placement) -> placement.heightmap)).apply(builder, WaystonePlacement::new));

    private final Heightmap.Types heightmap;

    public WaystonePlacement() {
        this(Heightmap.Types.OCEAN_FLOOR_WG);
    }

    public WaystonePlacement(Heightmap.Types heightmap) {
        this.heightmap = heightmap;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        if (isWaystoneChunk(context, pos)) {
            if (context.getLevel().getLevel().dimension() == Level.NETHER) {
                BlockPos.MutableBlockPos mutablePos = pos.mutable();
                int topMostY = context.getHeight(heightmap, pos.getX(), pos.getZ());
                mutablePos.setY(topMostY);
                BlockState stateAbove = context.getLevel().getBlockState(mutablePos);
                for (int i = mutablePos.getY(); i >= 1; i--) {
                    mutablePos.setY(mutablePos.getY() - 1);
                    BlockState state = context.getLevel().getBlockState(mutablePos);
                    if (!state.isAir() && state.getFluidState().isEmpty() && stateAbove.isAir() && !state.is(Blocks.BEDROCK)) {
                        mutablePos.setY(mutablePos.getY() + 1);
                        break;
                    }
                    stateAbove = state;
                }
                return mutablePos.getY() > 0 ? Stream.of(mutablePos) : Stream.empty();
            }

            int x = pos.getX();
            int z = pos.getZ();
            int y = context.getHeight(heightmap, x, z);
            return y > context.getMinY() ? Stream.of(new BlockPos(x, y, z)) : Stream.of();
        } else {
            return Stream.empty();
        }
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldGen.waystonePlacement.get();
    }

    private boolean isWaystoneChunk(PlacementContext world, BlockPos pos) {
        final int chunkDistance = WaystonesConfig.getActive().worldGen.chunksBetweenWildWaystones;
        if (chunkDistance == 0) {
            return false;
        }

        ResourceLocation dimension = world.getLevel().getLevel().dimension().location();
        final var dimensionAllowList = WaystonesConfig.getActive().worldGen.wildWaystonesDimensionAllowList;
        final var dimensionDenyList = WaystonesConfig.getActive().worldGen.wildWaystonesDimensionDenyList;
        if (!dimensionAllowList.isEmpty() && !dimensionAllowList.contains(dimension)) {
            return false;
        } else if (!dimensionDenyList.isEmpty() && dimensionDenyList.contains(dimension)) {
            return false;
        }

        final int maxDeviation = (int) Math.ceil(chunkDistance / 2f);
        int chunkX = pos.getX() / 16;
        int chunkZ = pos.getZ() / 16;
        int devGridX = pos.getX() / 16 * maxDeviation;
        int devGridZ = pos.getZ() / 16 * maxDeviation;
        long seed = world.getLevel().getSeed();
        Random random = new Random(seed * devGridX * devGridZ);
        int chunkOffsetX = random.nextInt(maxDeviation);
        int chunkOffsetZ = random.nextInt(maxDeviation);
        return (chunkX + chunkOffsetX) % chunkDistance == 0 && (chunkZ + chunkOffsetZ) % chunkDistance == 0;
    }
}
