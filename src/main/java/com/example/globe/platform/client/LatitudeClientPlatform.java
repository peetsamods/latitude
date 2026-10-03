package com.example.globe.platform.client;

import com.example.globe.platform.LatitudePlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

/** Static entry point to {@link LatitudeClientPlatformService}. */
public final class LatitudeClientPlatform {
    private static final String[] IMPLEMENTATIONS = {
            "com.example.globe.platform.fabric.FabricLatitudeClientPlatform",
            "com.example.globe.platform.neoforge.NeoForgeLatitudeClientPlatform"
    };

    private LatitudeClientPlatform() {
    }

    public static LatitudeClientPlatformService service() {
        return Holder.SERVICE;
    }

    public static void onClientStarted(Consumer<Minecraft> listener) {
        service().onClientStarted(listener);
    }

    public static void onClientTickEnd(Consumer<Minecraft> listener) {
        service().onClientTickEnd(listener);
    }

    public static void onClientJoin(Consumer<Minecraft> listener) {
        service().onClientJoin(listener);
    }

    public static void onClientDisconnect(Consumer<Minecraft> listener) {
        service().onClientDisconnect(listener);
    }

    public static <T extends CustomPacketPayload> void onClientPayload(
            CustomPacketPayload.Type<T> type, LatitudeClientPlatformService.ClientPayloadHandler<T> handler) {
        service().onClientPayload(type, handler);
    }

    public static KeyMapping registerKeyMapping(KeyMapping mapping) {
        return service().registerKeyMapping(mapping);
    }

    private static final class Holder {
        private static final LatitudeClientPlatformService SERVICE =
                LatitudePlatform.loadImplementation(IMPLEMENTATIONS, LatitudeClientPlatformService.class);
    }
}
