package com.example.globe.platform.neoforge;

import com.example.globe.platform.LatitudePlatformService;
import com.example.globe.tools.LatitudeCommandRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * NeoForge side of the loader seam. Game events go straight onto {@link NeoForge#EVENT_BUS}.
 * Registry entries and network payloads must be declared inside NeoForge's own registration
 * events, so those calls are recorded here and replayed when the mod bus fires them.
 */
public final class NeoForgeLatitudePlatform implements LatitudePlatformService {
    /** Bumped only when a payload's wire format changes. */
    private static final String NETWORK_VERSION = "1";

    private static final List<PendingRegistration<?>> REGISTRATIONS = new ArrayList<>();
    private static final List<Consumer<PayloadRegistrar>> CLIENTBOUND = new ArrayList<>();
    private static final Map<CustomPacketPayload.Type<?>, StreamCodec<RegistryFriendlyByteBuf, ?>> SERVERBOUND =
            new LinkedHashMap<>();
    private static final Map<CustomPacketPayload.Type<?>, ServerPayloadHandler<?>> SERVER_HANDLERS =
            new LinkedHashMap<>();

    /** Connects the deferred registrations to this mod's event bus; called from the mod constructor. */
    public static void bind(IEventBus modBus) {
        modBus.addListener(RegisterEvent.class, NeoForgeLatitudePlatform::onRegister);
        modBus.addListener(RegisterPayloadHandlersEvent.class, NeoForgeLatitudePlatform::onRegisterPayloads);
    }

    @Override
    public String loaderName() {
        return "neoforge";
    }

    @Override
    public String loaderModId() {
        return "neoforge";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.isProduction();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Optional<String> modVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString());
    }

    @Override
    public boolean modFlag(String modId, String key) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getModProperties().get(key))
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }

    @Override
    public List<Path> modSourcePaths(String modId) {
        var fileInfo = ModList.get().getModFileById(modId);
        return fileInfo == null ? List.of() : List.of(fileInfo.getFile().getFilePath());
    }

    @Override
    public Optional<Path> findModFile(String modId, String path) {
        var fileInfo = ModList.get().getModFileById(modId);
        if (fileInfo == null) {
            return Optional.empty();
        }
        Path root = fileInfo.getFile().getFilePath();
        if (Files.isDirectory(root)) {
            Path candidate = root.resolve(path);
            return Files.exists(candidate) ? Optional.of(candidate) : Optional.empty();
        }
        try {
            Path candidate = java.nio.file.FileSystems.newFileSystem(root).getPath(path);
            return Files.exists(candidate) ? Optional.of(candidate) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> loadedModIds() {
        List<String> ids = new ArrayList<>();
        ModList.get().getMods().forEach(info -> ids.add(info.getModId()));
        return ids;
    }

    @Override
    public String[] launchArguments() {
        FMLLoader loader = FMLLoader.getCurrentOrNull();
        return loader == null ? new String[0] : loader.getProgramArgs().getArguments();
    }

    @Override
    public <T> void register(Registry<T> registry, Identifier id, T value) {
        REGISTRATIONS.add(new PendingRegistration<>(registry.key(), id, value));
    }

    @Override
    public void onServerStarted(Consumer<MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> listener.accept(event.getServer()));
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, event -> listener.accept(event.getServer()));
    }

    @Override
    public void onServerStopped(Consumer<MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> listener.accept(event.getServer()));
    }

    @Override
    public void onServerTickEnd(Consumer<MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> listener.accept(event.getServer()));
    }

    @Override
    public void onServerLevelLoad(BiConsumer<MinecraftServer, ServerLevel> listener) {
        // LevelEvent.Load also fires for client levels; Fabric's ServerLevelEvents.LOAD does not.
        NeoForge.EVENT_BUS.addListener(LevelEvent.Load.class, event -> {
            if (event.getLevel() instanceof ServerLevel level) {
                listener.accept(level.getServer(), level);
            }
        });
    }

    @Override
    public void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                listener.accept(player, serverOf(player));
            }
        });
    }

    @Override
    public void onPlayerDisconnect(BiConsumer<ServerPlayer, MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                listener.accept(player, serverOf(player));
            }
        });
    }

    @Override
    public void onRegisterCommands(LatitudeCommandRegistrar registrar) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> registrar.register(event.getDispatcher()));
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientboundPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        CLIENTBOUND.add(registrar -> registrar.playToClient(type, codec));
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerboundPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        SERVERBOUND.put(type, codec);
    }

    @Override
    public <T extends CustomPacketPayload> void onServerPayload(
            CustomPacketPayload.Type<T> type, ServerPayloadHandler<T> handler) {
        SERVER_HANDLERS.put(type, handler);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        // Latitude's channels are optional, so a player without Latitude can still join; Fabric
        // tolerates that too. Only send what the client says it can read.
        if (player.connection != null && player.connection.hasChannel(payload.type())) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static MinecraftServer serverOf(ServerPlayer player) {
        return ((ServerLevel) player.level()).getServer();
    }

    private static void onRegister(RegisterEvent event) {
        for (PendingRegistration<?> pending : REGISTRATIONS) {
            pending.registerIfMatches(event);
        }
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        for (Consumer<PayloadRegistrar> clientbound : CLIENTBOUND) {
            clientbound.accept(registrar);
        }
        for (Map.Entry<CustomPacketPayload.Type<?>, StreamCodec<RegistryFriendlyByteBuf, ?>> entry : SERVERBOUND.entrySet()) {
            registerServerbound(registrar, entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> void registerServerbound(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<?> rawType,
            StreamCodec<RegistryFriendlyByteBuf, ?> rawCodec) {
        CustomPacketPayload.Type<T> type = (CustomPacketPayload.Type<T>) rawType;
        StreamCodec<RegistryFriendlyByteBuf, T> codec = (StreamCodec<RegistryFriendlyByteBuf, T>) rawCodec;
        ServerPayloadHandler<T> handler = (ServerPayloadHandler<T>) SERVER_HANDLERS.get(type);
        registrar.playToServer(type, codec, (payload, context) -> {
            if (handler != null && context.player() instanceof ServerPlayer player) {
                handler.handle(payload, player, serverOf(player));
            }
        });
    }

    private record PendingRegistration<T>(ResourceKey<? extends Registry<T>> registryKey, Identifier id, T value) {
        void registerIfMatches(RegisterEvent event) {
            if (event.getRegistryKey().equals(registryKey)) {
                event.register(registryKey, id, () -> value);
            }
        }
    }
}
