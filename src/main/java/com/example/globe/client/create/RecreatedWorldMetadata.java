package com.example.globe.client.create;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;

/**
 * Reads fields out of Latitude's persisted per-world save state directly from disk, before the
 * world is loaded — used both to recover Latitude's identity before vanilla normalizes Re-Create
 * to Normal, and to show save-time information (like the player's last known climate zone) on
 * screens that list saves without starting a server.
 */
public final class RecreatedWorldMetadata {
    private static final Path LATITUDE_STATE = Path.of(
            "dimensions", "minecraft", "overworld", "data", "globe", "latitude_world_state.dat");

    private RecreatedWorldMetadata() {
    }

    /** Both persisted fields the client reads at world open, from a single pass over the state file. */
    public record LatitudeState(@Nullable String presetId, @Nullable String lastKnownBandId) {
    }

    /** Reads the state file once; null when the world carries no Latitude state at all. */
    @Nullable
    public static LatitudeState read(Path worldRoot) throws IOException {
        CompoundTag data = readLatitudeStateData(worldRoot);
        if (data == null) {
            return null;
        }
        return new LatitudeState(
                RecreatedWorldTypePolicy.presetIdForRadius(data.getIntOr("globe_radius", 0)),
                data.getString("last_known_band").orElse(null));
    }

    @Nullable
    public static String latitudePresetId(Path worldRoot) throws IOException {
        CompoundTag data = readLatitudeStateData(worldRoot);
        if (data == null) {
            return null;
        }
        int radius = data.getIntOr("globe_radius", 0);
        return RecreatedWorldTypePolicy.presetIdForRadius(radius);
    }

    /** Canonical id (e.g. "temperate") of the band a player was last known to occupy, if recorded. */
    @Nullable
    public static String lastKnownBandId(Path worldRoot) throws IOException {
        CompoundTag data = readLatitudeStateData(worldRoot);
        if (data == null) {
            return null;
        }
        return data.getString("last_known_band").orElse(null);
    }

    @Nullable
    private static CompoundTag readLatitudeStateData(Path worldRoot) throws IOException {
        if (worldRoot == null) {
            return null;
        }
        Path statePath = worldRoot.resolve(LATITUDE_STATE);
        if (!Files.isRegularFile(statePath)) {
            return null;
        }
        CompoundTag root = NbtIo.readCompressed(statePath, NbtAccounter.unlimitedHeap());
        return root.getCompoundOrEmpty("data");
    }
}
