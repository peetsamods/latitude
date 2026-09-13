package com.example.globe.client.create;

import org.jetbrains.annotations.Nullable;

/** Carries source-world identity across vanilla's Re-Create screen construction. */
public interface RecreatedWorldPresetCarrier {
    void globe$setRecreatedWorldPresetId(@Nullable String presetId);

    @Nullable
    String globe$getRecreatedWorldPresetId();

    /**
     * The source world's persisted globe shape id, or {@code null} when it was never stamped.
     *
     * <p>Carried separately from the preset id because the two axes are persisted separately: the preset
     * id encodes the world's SIZE, while the shape (Wide 2:1 / Square 1:1) rides its own save field. A
     * Re-Create that carried only the preset would restore the size and silently reset the shape.</p>
     */
    void globe$setRecreatedGlobeShapeId(@Nullable String globeShapeId);

    @Nullable
    String globe$getRecreatedGlobeShapeId();
}
