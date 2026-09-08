package com.example.globe.world;

/**
 * The per-chunk home of the "decorated under the fixed feature index" marker.
 *
 * <p>The donor line kept this marker as a persistent Fabric chunk attachment. The 1.20 range is
 * served by one jar, and Fabric API has no attachment API on 1.20.2 and 1.20.3, so the marker is
 * carried by every {@code ChunkAccess} through a mixin that implements this interface and is
 * saved as the chunk tag {@link #DECORATED_UNDER_FIXED_INDEX_KEY} by a {@code ChunkSerializer}
 * mixin — the same id the donor's attachment used (maintainer ruling, 2026-09-08).</p>
 *
 * <p>Three operations, because two of them must not look alike: marking a chunk is a change
 * that has to reach the save, so it flags the chunk unsaved; restoring the marker from a save or
 * carrying it across a chunk's promotion is not a change, so it must not.</p>
 */
public interface LatitudeChunkMarker {

    /** The chunk NBT key the marker is saved under; the donor line's attachment id. */
    String DECORATED_UNDER_FIXED_INDEX_KEY = "globe:retrofit_decorated";

    /** Whether this chunk was decorated under the fixed index. */
    boolean globe$isDecoratedUnderFixedIndex();

    /** Marks this chunk as decorated under the fixed index and flags it unsaved if that changed. */
    void globe$markDecoratedUnderFixedIndex();

    /** Restores the marker as read from a save or carried from a promoted chunk; never dirties. */
    void globe$loadDecoratedUnderFixedIndex(boolean decorated);
}
