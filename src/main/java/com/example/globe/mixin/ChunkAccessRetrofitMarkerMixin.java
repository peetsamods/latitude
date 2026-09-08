package com.example.globe.mixin;

import com.example.globe.world.LatitudeChunkMarker;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Gives every chunk a home for the retrofit marker — the 1.20-range stand-in for the donor line's
 * persistent chunk attachment (see {@link LatitudeChunkMarker}).
 *
 * <p>The field is volatile because it is written on a worldgen thread, as the chunk is decorated,
 * and read on the server thread by the opt-in retrofit and on save.</p>
 */
@Mixin(ChunkAccess.class)
public abstract class ChunkAccessRetrofitMarkerMixin implements LatitudeChunkMarker {

    @Unique
    private volatile boolean globe$decoratedUnderFixedIndex;

    @Override
    public boolean globe$isDecoratedUnderFixedIndex() {
        return this.globe$decoratedUnderFixedIndex;
    }

    @Override
    public void globe$markDecoratedUnderFixedIndex() {
        if (!this.globe$decoratedUnderFixedIndex) {
            this.globe$decoratedUnderFixedIndex = true;
            // A persistent attachment flagged its chunk unsaved when set; the marker does the same,
            // so the tag reaches the region file with the next save of this chunk.
            ((ChunkAccess) (Object) this).setUnsaved(true);
        }
    }

    @Override
    public void globe$loadDecoratedUnderFixedIndex(boolean decorated) {
        this.globe$decoratedUnderFixedIndex = decorated;
    }
}
