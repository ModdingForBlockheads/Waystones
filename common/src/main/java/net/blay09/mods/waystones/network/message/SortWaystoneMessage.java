package net.blay09.mods.waystones.network.message;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SortWaystoneMessage implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<SortWaystoneMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, "sort_waystone"));

    public static final UUID SORT_FIRST = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID SORT_LAST = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    private final UUID waystoneUid;
    private final UUID otherWaystoneUid;

    public SortWaystoneMessage(UUID waystoneUid, UUID otherWaystoneUid) {
        this.waystoneUid = waystoneUid;
        this.otherWaystoneUid = otherWaystoneUid;
    }

    public static void encode(FriendlyByteBuf buf, SortWaystoneMessage message) {
        buf.writeUUID(message.waystoneUid);
        buf.writeUUID(message.otherWaystoneUid);
    }

    public static SortWaystoneMessage decode(FriendlyByteBuf buf) {
        final var waystoneUid = buf.readUUID();
        final var otherWaystoneUid = buf.readUUID();
        return new SortWaystoneMessage(waystoneUid, otherWaystoneUid);
    }

    public static void handle(ServerPlayer player, SortWaystoneMessage message) {
        if (player == null) {
            return;
        }

        if (message.otherWaystoneUid.equals(SORT_FIRST)) {
            PlayerWaystoneManager.sortWaystoneAsFirst(player, message.waystoneUid);
        } else if (message.otherWaystoneUid.equals(SORT_LAST)) {
            PlayerWaystoneManager.sortWaystoneAsLast(player, message.waystoneUid);
        } else {
            PlayerWaystoneManager.sortWaystoneSwap(player, message.waystoneUid, message.otherWaystoneUid);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
