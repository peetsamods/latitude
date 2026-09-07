package com.example.globe;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class GlobeNet {
    private static boolean registered;

    private GlobeNet() {
    }

    /**
     * Claims the three channel ids.
     *
     * <p>On this Minecraft line the id is claimed by {@code PacketType.create}, which runs inside
     * each payload record's static initialiser rather than in a separate registry call. Naming the
     * three constants here is what forces those initialisers, so every id is still claimed at
     * exactly the point in start-up this method was always called from.
     */
    public static void registerPayloads() {
        if (registered) {
            return;
        }
        registered = GlobeStatePayload.ID != null
                && OpenSpawnPickerPayload.ID != null
                && SetSpawnPickerPayload.ID != null;
    }

    /**
     * The channel id carries a version suffix because this payload gained a field.
     *
     * <p>Beta 1 shipped the same id with a single boolean. Had the id stayed put, a Beta 2 client
     * reading a Beta 1 server would run off the end of the buffer looking for the band string, and
     * the mismatch would surface as a disconnect rather than a missing label. An unrecognised id is
     * discarded by vanilla instead, so a mixed pair now simply loses the loading-screen zone name
     * and keeps playing.
     */
    public record GlobeStatePayload(boolean isGlobe, String loadingBandId) implements FabricPacket {
        public static final PacketType<GlobeStatePayload> ID = PacketType.create(
                new ResourceLocation("globe", "s2c_globe_state_v2"), GlobeStatePayload::read);

        private static GlobeStatePayload read(FriendlyByteBuf buf) {
            boolean isGlobe = buf.readBoolean();
            String loadingBandId = buf.readUtf();
            return new GlobeStatePayload(isGlobe, loadingBandId);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeBoolean(isGlobe);
            buf.writeUtf(loadingBandId);
        }

        @Override
        public PacketType<?> getType() {
            return ID;
        }
    }

    public record OpenSpawnPickerPayload(boolean open) implements FabricPacket {
        public static final PacketType<OpenSpawnPickerPayload> ID = PacketType.create(
                new ResourceLocation("globe", "s2c_open_spawn_picker"), OpenSpawnPickerPayload::read);

        private static OpenSpawnPickerPayload read(FriendlyByteBuf buf) {
            return new OpenSpawnPickerPayload(buf.readBoolean());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeBoolean(open);
        }

        @Override
        public PacketType<?> getType() {
            return ID;
        }
    }

    public record SetSpawnPickerPayload(String zoneId) implements FabricPacket {
        public static final PacketType<SetSpawnPickerPayload> ID = PacketType.create(
                new ResourceLocation("globe", "c2s_set_spawn_picker"), SetSpawnPickerPayload::read);

        private static SetSpawnPickerPayload read(FriendlyByteBuf buf) {
            return new SetSpawnPickerPayload(buf.readUtf());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(zoneId);
        }

        @Override
        public PacketType<?> getType() {
            return ID;
        }
    }
}
