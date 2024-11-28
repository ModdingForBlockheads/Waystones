package net.blay09.mods.waystones.core;

import net.blay09.mods.waystones.api.*;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WaystoneImpl implements Waystone, MutableWaystone {

    public static final StreamCodec<RegistryFriendlyByteBuf, Waystone> STREAM_CODEC = StreamCodec.of(WaystoneImpl::write, WaystoneImpl::read);
    public static final StreamCodec<RegistryFriendlyByteBuf, Collection<Waystone>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new));

    private final ResourceLocation waystoneType;
    private final UUID waystoneUid;
    private final WaystoneOrigin origin;

    private boolean isTransient;
    private ResourceKey<Level> dimension;
    private BlockPos pos;

    private Component name = Component.empty();
    private WaystoneVisibility visibility;

    private UUID ownerUid;

    public WaystoneImpl(ResourceLocation waystoneType, UUID waystoneUid, ResourceKey<Level> dimension, BlockPos pos, WaystoneOrigin origin, @Nullable UUID ownerUid) {
        this.waystoneType = waystoneType;
        this.waystoneUid = waystoneUid;
        this.dimension = dimension;
        this.pos = pos;
        this.origin = origin;
        this.ownerUid = ownerUid;
        this.visibility = WaystoneVisibility.fromWaystoneType(waystoneType);
    }

    @Override
    public UUID getWaystoneUid() {
        return waystoneUid;
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void setName(Component name) {
        this.name = name;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    @Override
    public WaystoneOrigin getOrigin() {
        return origin;
    }

    @Override
    public WaystoneVisibility getVisibility() {
        return visibility;
    }

    @Override
    public void setVisibility(WaystoneVisibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public boolean isOwner(Player player) {
        return ownerUid == null || player.getGameProfile().getId().equals(ownerUid) || player.getAbilities().instabuild;
    }

    @Override
    public void setOwnerUid(@Nullable UUID ownerUid) {
        this.ownerUid = ownerUid;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public UUID getOwnerUid() {
        return ownerUid;
    }

    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public ResourceLocation getWaystoneType() {
        return waystoneType;
    }

    @Override
    public boolean isValidInLevel(ServerLevel level) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlockTags.IS_TELEPORT_TARGET);
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    public static List<Waystone> readList(RegistryFriendlyByteBuf buf) {
        int size = buf.readShort();
        List<Waystone> waystones = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            waystones.add(read(buf));
        }
        return waystones;
    }

    public static Waystone read(RegistryFriendlyByteBuf buf) {
        final var waystoneUid = buf.readUUID();
        final var waystoneType = buf.readResourceLocation();
        final var name = ComponentSerialization.STREAM_CODEC.decode(buf);
        final var visibility = buf.readEnum(WaystoneVisibility.class);
        final var dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(250)));
        final var pos = buf.readBlockPos();
        final var origin = buf.readEnum(WaystoneOrigin.class);

        final var waystone = new WaystoneImpl(waystoneType, waystoneUid, dimension, pos, origin, null);
        waystone.setName(name);
        waystone.setVisibility(visibility);
        return waystone;
    }

    private static BlockPos readLegacyBlockPos(CompoundTag compound) {
        return new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z"));
    }

    public static Waystone read(CompoundTag compound, HolderLookup.Provider provider) {
        final var waystoneUid = NbtUtils.loadUUID(Objects.requireNonNull(compound.get("WaystoneUid")));
        final var legacyName = compound.getString("Name");
        final var name = compound.contains("NameV2")
                ? Component.Serializer.fromJson(compound.getString("NameV2"), provider)
                : Component.literal(legacyName);
        final var dimensionType = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(compound.getString("World")));
        final var pos = NbtUtils.readBlockPos(compound, "BlockPos").orElseGet(() -> readLegacyBlockPos(compound.getCompound("BlockPos")));
        final var legacyWasGenerated = compound.getBoolean("WasGenerated");
        var origin = legacyWasGenerated ? WaystoneOrigin.WILDERNESS : WaystoneOrigin.UNKNOWN;
        if (compound.contains("Origin")) {
            try {
                origin = WaystoneOrigin.valueOf(compound.getString("Origin"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        final var ownerUid = compound.contains("OwnerUid") ? NbtUtils.loadUUID(Objects.requireNonNull(compound.get("OwnerUid"))) : null;
        final var waystoneType = compound.contains("Type") ? ResourceLocation.parse(compound.getString("Type")) : WaystoneTypes.WAYSTONE;
        final var isTransient = compound.contains("Transient") && compound.getBoolean("Transient");
        final var waystone = new WaystoneImpl(waystoneType, waystoneUid, dimensionType, pos, origin, ownerUid);
        waystone.setName(name);
        waystone.setTransient(isTransient);
        if (compound.contains("Visibility")) {
            waystone.setVisibility(WaystoneVisibility.valueOf(compound.getString("Visibility")));
        } else {
            waystone.setVisibility(compound.getBoolean("IsGlobal") ? WaystoneVisibility.GLOBAL : WaystoneVisibility.ACTIVATION);
        }
        return waystone;
    }

    public static void writeList(RegistryFriendlyByteBuf buf, Collection<Waystone> waystones) {
        buf.writeShort(waystones.size());
        for (Waystone waystone : waystones) {
            write(buf, waystone);
        }
    }

    public static void write(RegistryFriendlyByteBuf buf, Waystone waystone) {
        buf.writeUUID(waystone.getWaystoneUid());
        buf.writeResourceLocation(waystone.getWaystoneType());
        ComponentSerialization.STREAM_CODEC.encode(buf, waystone.getName());
        buf.writeEnum(waystone.getVisibility());
        buf.writeResourceLocation(waystone.getDimension().location());
        buf.writeBlockPos(waystone.getPos());
        buf.writeEnum(waystone.getOrigin());
    }

    public static CompoundTag write(Waystone waystone, CompoundTag compound, HolderLookup.Provider provider) {
        compound.put("WaystoneUid", NbtUtils.createUUID(waystone.getWaystoneUid()));
        compound.putString("Type", waystone.getWaystoneType().toString());
        compound.putString("NameV2", Component.Serializer.toJson(waystone.getName(), provider));
        compound.putString("World", waystone.getDimension().location().toString());
        compound.putBoolean("Transient", waystone.isTransient());
        compound.put("BlockPos", NbtUtils.writeBlockPos(waystone.getPos()));
        compound.putString("Origin", waystone.getOrigin().name());
        if (waystone.getOwnerUid() != null) {
            compound.put("OwnerUid", NbtUtils.createUUID(waystone.getOwnerUid()));
        }
        compound.putString("Visibility", waystone.getVisibility().name());
        return compound;
    }
}
