package com.example.globe.mixin;

import com.example.globe.world.LatitudeChunkMarker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A chunk is decorated while it is still a {@code ProtoChunk}; the {@code LevelChunk} the game
 * keeps is built from that proto chunk afterwards, and the marker has to cross that promotion or
 * every freshly generated chunk would look bare to a retrofit armed later.
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkRetrofitMarkerMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V",
            at = @At("TAIL"))
    private void globe$carryRetrofitMarkerFromProtoChunk(ServerLevel level, ProtoChunk protoChunk,
                                                         LevelChunk.PostLoadProcessor postLoad,
                                                         CallbackInfo ci) {
        ((LatitudeChunkMarker) (Object) this).globe$loadDecoratedUnderFixedIndex(
                ((LatitudeChunkMarker) protoChunk).globe$isDecoratedUnderFixedIndex());
    }
}
