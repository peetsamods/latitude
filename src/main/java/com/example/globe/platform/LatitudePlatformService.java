package com.example.globe.platform;

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

/**
 * Everything Latitude's common code needs from a mod loader. One implementation ships per loader
 * jar; shared code reaches it only through {@link LatitudePlatform}.
 *
 * <p>Event methods register a listener immediately and keep the loader's own ordering, so a Fabric
 * build calls exactly the Fabric event it called before this seam existed.
 */
public interface LatitudePlatformService {
    String loaderName();

    /** Mod id the loader itself reports under ("fabricloader" / "neoforge"). */
    String loaderModId();

    boolean isDevelopmentEnvironment();

    boolean isModLoaded(String modId);

    Path configDir();

    /** The mod's version as the loader reports it, without a "v" prefix. */
    Optional<String> modVersion(String modId);

    /** A boolean flag from the mod's loader metadata (Fabric custom value / NeoForge mod property). */
    boolean modFlag(String modId, String key);

    /** The jar or classes directories a mod was loaded from. */
    java.util.List<Path> modSourcePaths(String modId);

    /** A file inside the mod's own jar or classes directory. */
    Optional<Path> findModFile(String modId, String path);

    /** Ids of every loaded mod, in loader order. */
    java.util.List<String> loadedModIds();

    /** Program arguments the game was launched with. */
    String[] launchArguments();

    <T> void register(Registry<T> registry, Identifier id, T value);

    void onServerStarted(Consumer<MinecraftServer> listener);

    void onServerStopping(Consumer<MinecraftServer> listener);

    void onServerStopped(Consumer<MinecraftServer> listener);

    void onServerTickEnd(Consumer<MinecraftServer> listener);

    /** Fires for every server level as it loads, before the overworld's initial spawn is chosen. */
    void onServerLevelLoad(BiConsumer<MinecraftServer, ServerLevel> listener);

    void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener);

    void onPlayerDisconnect(BiConsumer<ServerPlayer, MinecraftServer> listener);

    void onRegisterCommands(LatitudeCommandRegistrar registrar);

    <T extends CustomPacketPayload> void registerClientboundPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec);

    <T extends CustomPacketPayload> void registerServerboundPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec);

    <T extends CustomPacketPayload> void onServerPayload(
            CustomPacketPayload.Type<T> type, ServerPayloadHandler<T> handler);

    /** Sends to one player. On NeoForge, players whose client lacks the channel are skipped. */
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    @FunctionalInterface
    interface ServerPayloadHandler<T> {
        void handle(T payload, ServerPlayer player, MinecraftServer server);
    }
}
