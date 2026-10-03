package com.example.globe.platform.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

/** Client-side half of the loader seam; never loaded on a dedicated server. */
public interface LatitudeClientPlatformService {
    void onClientStarted(Consumer<Minecraft> listener);

    void onClientTickEnd(Consumer<Minecraft> listener);

    /** The client has joined a world and the play connection is up. */
    void onClientJoin(Consumer<Minecraft> listener);

    void onClientDisconnect(Consumer<Minecraft> listener);

    <T extends CustomPacketPayload> void onClientPayload(
            CustomPacketPayload.Type<T> type, ClientPayloadHandler<T> handler);

    /** Lists a key mapping in the Controls screen and returns it. */
    KeyMapping registerKeyMapping(KeyMapping mapping);

    @FunctionalInterface
    interface ClientPayloadHandler<T> {
        void handle(T payload, Minecraft client);
    }
}
