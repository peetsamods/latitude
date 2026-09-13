package com.example.globe.mixin.terrain;

import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for {@link RandomState}'s otherwise-final {@code router} field, so the Phase 4 terrain-bias
 * hook can replace the world's {@link NoiseRouter} with one whose {@code finalDensity} is wrapped by
 * {@code GeoTerrainBiasFunction}. Verified against the 26.3-rc-2 jar: {@code RandomState} still declares
 * {@code private final NoiseRouter router;}, but 26.3 REMOVED the public {@code router()} getter, so both
 * the read and the write now go through this accessor -- a {@code @Mutable @Accessor} for the write, and
 * the plain {@code @Accessor} for the read that used to be the getter.
 *
 * <p>Only the router field is exposed; every other {@code RandomState} field is left untouched. The
 * mutation site is gated by a real positive globe check (see {@link RandomStateRouterTerrainMixin}); on
 * non-globe worlds this accessor is never invoked.
 */
@Mixin(RandomState.class)
public interface RandomStateAccessor {

    @Accessor("router")
    NoiseRouter globe$getRouter();

    @Accessor("router")
    @Mutable
    void globe$setRouter(NoiseRouter router);
}
