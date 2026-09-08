package com.example.globe.mixin;

import com.example.globe.world.LatitudeChunkMarker;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * An {@code ImposterProtoChunk} is a fully loaded chunk standing in for a proto chunk during a
 * neighbour's generation. It owns no state of its own, so the retrofit marker is read from and
 * written to the chunk it wraps — exactly as its block, biome and unsaved state already are.
 */
@Mixin(ImposterProtoChunk.class)
public abstract class ImposterProtoChunkRetrofitMarkerMixin implements LatitudeChunkMarker {

    @Shadow
    @Final
    private LevelChunk wrapped;

    @Override
    public boolean globe$isDecoratedUnderFixedIndex() {
        return ((LatitudeChunkMarker) this.wrapped).globe$isDecoratedUnderFixedIndex();
    }

    @Override
    public void globe$markDecoratedUnderFixedIndex() {
        ((LatitudeChunkMarker) this.wrapped).globe$markDecoratedUnderFixedIndex();
    }

    @Override
    public void globe$loadDecoratedUnderFixedIndex(boolean decorated) {
        ((LatitudeChunkMarker) this.wrapped).globe$loadDecoratedUnderFixedIndex(decorated);
    }
}
