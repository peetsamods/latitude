package com.example.globe.platform.neoforge;

import com.example.globe.platform.client.LatitudeClientPlatformService;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** NeoForge side of the client seam. */
public final class NeoForgeLatitudeClientPlatform implements LatitudeClientPlatformService {
    private static final List<Consumer<RegisterClientPayloadHandlersEvent>> PAYLOAD_HANDLERS = new ArrayList<>();
    private static final List<KeyMapping> KEY_MAPPINGS = new ArrayList<>();

    /** Connects the deferred client registrations to this mod's event bus. */
    public static void bind(IEventBus modBus) {
        modBus.addListener(RegisterClientPayloadHandlersEvent.class,
                event -> PAYLOAD_HANDLERS.forEach(registration -> registration.accept(event)));
        modBus.addListener(RegisterKeyMappingsEvent.class, NeoForgeLatitudeClientPlatform::onRegisterKeyMappings);
    }

    @Override
    public void onClientStarted(Consumer<Minecraft> listener) {
        NeoForge.EVENT_BUS.addListener(ClientStartedEvent.class, event -> listener.accept(event.getClient()));
    }

    @Override
    public void onClientTickEnd(Consumer<Minecraft> listener) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> listener.accept(Minecraft.getInstance()));
    }

    @Override
    public void onClientJoin(Consumer<Minecraft> listener) {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class,
                event -> listener.accept(Minecraft.getInstance()));
    }

    @Override
    public void onClientDisconnect(Consumer<Minecraft> listener) {
        // NeoForge fires LoggingOut from every Minecraft.disconnect(), including the clean-up run before a
        // new world opens, when no play connection exists. Fabric's DISCONNECT fires only when a real play
        // connection closes, so only forward a logout that had a connection.
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
            if (event.getConnection() != null) {
                listener.accept(Minecraft.getInstance());
            }
        });
    }

    @Override
    public <T extends CustomPacketPayload> void onClientPayload(
            CustomPacketPayload.Type<T> type, ClientPayloadHandler<T> handler) {
        PAYLOAD_HANDLERS.add(event -> event.register(type,
                (payload, context) -> handler.handle(payload, Minecraft.getInstance())));
    }

    @Override
    public KeyMapping registerKeyMapping(KeyMapping mapping) {
        KEY_MAPPINGS.add(mapping);
        return mapping;
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        Set<KeyMapping.Category> categories = new LinkedHashSet<>();
        for (KeyMapping mapping : KEY_MAPPINGS) {
            categories.add(mapping.getCategory());
        }
        for (KeyMapping.Category category : categories) {
            try {
                event.registerCategory(category);
            } catch (IllegalArgumentException alreadyRegistered) {
                // Another registration in this event already added the category.
            }
        }
        KEY_MAPPINGS.forEach(event::register);
    }
}
