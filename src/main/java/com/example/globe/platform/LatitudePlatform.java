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
 * Static entry point to the loader seam. Each loader jar contains exactly one implementation class,
 * found by name on first use, so the seam works before any loader entrypoint has run (mixin static
 * initializers can reach it that early).
 */
public final class LatitudePlatform {
    private static final String[] IMPLEMENTATIONS = {
            "com.example.globe.platform.fabric.FabricLatitudePlatform",
            "com.example.globe.platform.neoforge.NeoForgeLatitudePlatform"
    };

    private LatitudePlatform() {
    }

    public static LatitudePlatformService service() {
        return Holder.SERVICE;
    }

    public static String loaderName() {
        return service().loaderName();
    }

    public static String loaderModId() {
        return service().loaderModId();
    }

    public static boolean isDevelopmentEnvironment() {
        return service().isDevelopmentEnvironment();
    }

    public static boolean isModLoaded(String modId) {
        return service().isModLoaded(modId);
    }

    public static Path configDir() {
        return service().configDir();
    }

    public static Optional<String> modVersion(String modId) {
        return service().modVersion(modId);
    }

    public static boolean modFlag(String modId, String key) {
        return service().modFlag(modId, key);
    }

    public static java.util.List<Path> modSourcePaths(String modId) {
        return service().modSourcePaths(modId);
    }

    public static Optional<Path> findModFile(String modId, String path) {
        return service().findModFile(modId, path);
    }

    public static java.util.List<String> loadedModIds() {
        return service().loadedModIds();
    }

    public static String[] launchArguments() {
        return service().launchArguments();
    }

    public static <T> void register(Registry<T> registry, Identifier id, T value) {
        service().register(registry, id, value);
    }

    public static void onServerStarted(Consumer<MinecraftServer> listener) {
        service().onServerStarted(listener);
    }

    public static void onServerStopping(Consumer<MinecraftServer> listener) {
        service().onServerStopping(listener);
    }

    public static void onServerStopped(Consumer<MinecraftServer> listener) {
        service().onServerStopped(listener);
    }

    public static void onServerTickEnd(Consumer<MinecraftServer> listener) {
        service().onServerTickEnd(listener);
    }

    public static void onServerLevelLoad(BiConsumer<MinecraftServer, ServerLevel> listener) {
        service().onServerLevelLoad(listener);
    }

    public static void onPlayerJoin(BiConsumer<ServerPlayer, MinecraftServer> listener) {
        service().onPlayerJoin(listener);
    }

    public static void onPlayerDisconnect(BiConsumer<ServerPlayer, MinecraftServer> listener) {
        service().onPlayerDisconnect(listener);
    }

    public static void onRegisterCommands(LatitudeCommandRegistrar registrar) {
        service().onRegisterCommands(registrar);
    }

    public static <T extends CustomPacketPayload> void registerClientboundPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        service().registerClientboundPayload(type, codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        service().registerServerboundPayload(type, codec);
    }

    public static <T extends CustomPacketPayload> void onServerPayload(
            CustomPacketPayload.Type<T> type, LatitudePlatformService.ServerPayloadHandler<T> handler) {
        service().onServerPayload(type, handler);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        service().sendToPlayer(player, payload);
    }

    /** Instantiates the first implementation class present in this jar. */
    public static <S> S loadImplementation(String[] classNames, Class<S> serviceType) {
        ClassLoader loader = LatitudePlatform.class.getClassLoader();
        for (String className : classNames) {
            Class<?> type;
            try {
                type = Class.forName(className, true, loader);
            } catch (ClassNotFoundException absent) {
                continue;
            }
            try {
                return serviceType.cast(type.getDeclaredConstructor().newInstance());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Latitude could not start loader seam " + className, e);
            }
        }
        throw new IllegalStateException("Latitude found no " + serviceType.getSimpleName()
                + " implementation for this mod loader");
    }

    private static final class Holder {
        private static final LatitudePlatformService SERVICE =
                loadImplementation(IMPLEMENTATIONS, LatitudePlatformService.class);
    }
}
