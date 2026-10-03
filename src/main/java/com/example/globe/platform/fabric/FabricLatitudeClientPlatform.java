package com.example.globe.platform.fabric;

import com.example.globe.platform.client.LatitudeClientPlatformService;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

/** Fabric side of the client seam: every method is a direct pass-through to the Fabric API. */
public final class FabricLatitudeClientPlatform implements LatitudeClientPlatformService {
    @Override
    public void onClientStarted(Consumer<Minecraft> listener) {
        ClientLifecycleEvents.CLIENT_STARTED.register(listener::accept);
    }

    @Override
    public void onClientTickEnd(Consumer<Minecraft> listener) {
        ClientTickEvents.END_CLIENT_TICK.register(listener::accept);
    }

    @Override
    public void onClientJoin(Consumer<Minecraft> listener) {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> listener.accept(client));
    }

    @Override
    public void onClientDisconnect(Consumer<Minecraft> listener) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> listener.accept(client));
    }

    @Override
    public <T extends CustomPacketPayload> void onClientPayload(
            CustomPacketPayload.Type<T> type, ClientPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type,
                (payload, context) -> handler.handle(payload, context.client()));
    }

    @Override
    public KeyMapping registerKeyMapping(KeyMapping mapping) {
        return KeyMappingHelper.registerKeyMapping(mapping);
    }
}
