package com.example.globe.mixin;

import com.example.globe.world.LatitudeChunkMarker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Persists the retrofit marker with the chunk: written as the chunk tag
 * {@link LatitudeChunkMarker#DECORATED_UNDER_FIXED_INDEX_KEY} when a marked chunk is saved and
 * restored onto the chunk when that tag is read back. This is the persistence the donor line got
 * from a Fabric persistent attachment; both {@code read} and {@code write} carry the same
 * signature on 1.20.1 through 1.20.4, so one arm serves the whole range.
 */
@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerRetrofitMarkerMixin {

    @Inject(
            method = "write(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At("RETURN"))
    private static void globe$writeRetrofitMarker(ServerLevel level, ChunkAccess chunk,
                                                  CallbackInfoReturnable<CompoundTag> cir) {
        if (((LatitudeChunkMarker) chunk).globe$isDecoratedUnderFixedIndex()) {
            cir.getReturnValue().putBoolean(LatitudeChunkMarker.DECORATED_UNDER_FIXED_INDEX_KEY, true);
        }
    }

    @Inject(
            method = "read(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/ai/village/poi/PoiManager;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/chunk/ProtoChunk;",
            at = @At("RETURN"))
    private static void globe$readRetrofitMarker(ServerLevel level, PoiManager poiManager, ChunkPos pos,
                                                 CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
        if (tag.getBoolean(LatitudeChunkMarker.DECORATED_UNDER_FIXED_INDEX_KEY)) {
            // The value read is a ProtoChunk, or an ImposterProtoChunk around a LevelChunk when the
            // save held a full chunk; the imposter forwards the marker to the chunk it wraps.
            ((LatitudeChunkMarker) cir.getReturnValue()).globe$loadDecoratedUnderFixedIndex(true);
        }
    }
}
