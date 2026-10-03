package com.example.globe.platform.fabric;

import com.example.globe.platform.LatitudePlatformService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import com.example.globe.tools.LatitudeCommandRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Fabric side of the loader seam: every method is a direct pass-through to the Fabric API. */
public final class FabricLatitudePlatform implements LatitudePlatformService {
    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public String loaderModId() {
        return "fabricloader";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Optional<String> modVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString());
    }

    @Override
    public boolean modFlag(String modId, String key) {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modId);
        if (container.isEmpty() || !container.get().getMetadata().containsCustomValue(key)) {
            return false;
        }
        CustomValue value = container.get().getMetadata().getCustomValue(key);
        return value != null
                && value.getType() == CustomValue.CvType.BOOLEAN
                && value.getAsBoolean();
    }

    @Override
    public java.util.List<Path> modSourcePaths(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getOrigin().getPaths())
                .orElse(java.util.List.of());
    }

    @Override
    public Optional<Path> findModFile(String modId, String path) {
        return FabricLoader.getInstance().getModContainer(modId).flatMap(container -> container.findPath(path));
    }

    @Override
    public java.util.List<String> loadedModIds() {
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ids.add(mod.getMetadata().getId());
        }
        return ids;
    }

    @Override
    public String[] launchArguments() {
        return FabricLoader.getInstance().getLaunchArguments(true);
    }

    @Override
    public <T> void register(Registry<T> registry, Identifier id, T value) {
        Registry.register(registry, id, value);
    }

    @Override
    public void onServerStarted(Consumer<MinecraftServer> listener) {
        ServerLifecycleEvents.SERVER_STARTED.register(listener::accept);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> listener) {
        ServerLifecycleEvents.SERVER_STOPPING.register(listener::accept);
    }

    @Override
    public void onServerStopped(Consumer<MinecraftServer> listener) {
        ServerLifecycleEvents.SERVER_STOPPED.register(listener::accept);
    }

    @Override
    public void onServerTickEnd(Consumer<MinecraftServer> listener) {
        ServerTickEvents.END_SERVER_TICK.register(listener::accept);
    }

    @Override
    public void onServerLevelLoad(BiConsumer<MinecraftServer, ServerLevel> listener) {
        ServerLevelEvents.LOAD.register(listener::accept);
    }

    @Override
    public void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> listener.accept(handler.player, server));
    }

    @Override
    public void onPlayerDisconnect(BiConsumer<ServerPlayer, MinecraftServer> listener) {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> listener.accept(handler.player, server));
    }

    @Override
    public void onRegisterCommands(LatitudeCommandRegistrar registrar) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registrar.register(dispatcher));
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientboundPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerboundPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
    }

    @Override
    public <T extends CustomPacketPayload> void onServerPayload(
            CustomPacketPayload.Type<T> type, ServerPayloadHandler<T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(type,
                (payload, context) -> handler.handle(payload, context.player(), context.server()));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
