package com.example.globe.mixin;

import com.example.globe.core.LatitudeV2Flags;
import com.example.globe.core.PolarVegetationFade;
import com.example.globe.world.LatitudeBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.BlockColumnFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Polar small-vegetation fade (the maintainer 2026-07-10; {@code latitude.polarVegetationFade.enabled}).
 *
 * <p>Thins surface vegetation toward the pole so the extreme-polar cap reads as bare snow/ice rather
 * than grass/ferns/flowers/sugarcane at 84-86deg. 26.1 removed {@code RandomPatchFeature}; in 26.2 the
 * small-vegetation placements that used to flow through it are re-typed as {@code minecraft:simple_block}
 * ({@link SimpleBlockFeature} -- grass, ferns, single flowers, wildflower, bushes, leaf litter, dead bush)
 * and {@code minecraft:block_column} ({@link BlockColumnFeature} -- sugarcane). This guard reuses the exact
 * interception mechanism the tree guards use ({@code @Inject} HEAD-cancellable on
 * {@code Feature.place(...)} reading the placement origin), just pointed at those two
 * feature classes.
 *
 * <p><b>26.3 port note.</b> 26.3 retired {@code FeaturePlaceContext} and the whole
 * {@code Feature<C extends FeatureConfiguration>} generic: a feature is now an unparameterised
 * {@code Feature} that carries its own configuration as record components and is placed through
 * {@code place(WorldGenLevel, ChunkGenerator, RandomSource, BlockPos)}. So the level/origin that used to
 * come off the context are plain parameters now, and the firefly-bush config read goes through the target
 * instance itself ({@code SimpleBlockFeature.toPlace()}) rather than {@code context.config()}. Same three
 * gates, same short-circuit order, same flag-off byte-identity.
 *
 * <p>Trees are deliberately NOT included -- {@link TreeLineVegetationGuardMixin} and
 * {@link ExtremePolarVegetationGuardMixin} ({@code TreeFeature}) already own tree suppression.
 *
 * <p>Flag-off is byte-identical: the first line returns before touching anything. The latitude ramp +
 * coherent fray live in {@link LatitudeBiomes#polarVegetationFadeStrips(int, int)} (pure math in
 * {@link com.example.globe.core.PolarVegetationFade}), which also returns "keep" for any non-globe world
 * and for every column below the fade onset.
 *
 * <p><b>Collateral guards (short-circuit order):</b> both feature classes also back placements this fade
 * must NOT touch, so the strip decision is fenced by three gates, cheapest first:
 * <ol>
 *   <li><b>flag</b> -- first statement; flag-off returns before reading anything (byte-identity).</li>
 *   <li><b>dimension</b> -- overworld only. {@link SimpleBlockFeature} also backs nether patches
 *       (crimson_roots, patch_fire, sulfur_pool...); we bail before any latitude/heightmap work when the
 *       feature is generating in a non-overworld dimension.</li>
 *   <li><b>latitude + fray</b> -- {@link LatitudeBiomes#polarVegetationFadeStrips(int, int)}: pure
 *       arithmetic + coherent noise. Returns "keep" (and pays NO heightmap lookup) for every column below
 *       the 76deg onset, so sub-polar columns short-circuit here.</li>
 *   <li><b>surface proximity</b> -- only reached for a would-strip polar column. Both classes ALSO back
 *       lush-cave features (moss / pale moss / spore blossom / small+big dripleaf / cave vines+glow
 *       berries / cave mushrooms) which generate far below the surface; comparing the placement origin Y
 *       against the {@code WORLD_SURFACE_WG} heightmap ({@link PolarVegetationFade#nearSurface(int, int)},
 *       {@link PolarVegetationFade#SURFACE_MARGIN}) keeps the fade to the surface layer so an under-cap
 *       lush cave is never stripped bare.</li>
 * </ol>
 * {@code require = 1}: both
 * {@code place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z}
 * descriptors are present in the 26.3-rc-2 merged jar (verified with javap: both records implement
 * {@code Feature} and declare that method), so a future remap that drops either target fails loud instead
 * of silently no-opping the guard.
 */
@Mixin({SimpleBlockFeature.class, BlockColumnFeature.class})
public class PolarVegetationFadeGuardMixin {

    @Inject(
            method = "place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void globe$polarVegetationFade(WorldGenLevel level,
                                           ChunkGenerator generator,
                                           RandomSource random,
                                           BlockPos origin,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!LatitudeV2Flags.POLAR_VEGETATION_FADE_ENABLED) {
            return; // flag-off: byte-identical, nothing read
        }
        // Overworld-only: SimpleBlockFeature also backs nether patches. Cheap early short-circuit.
        if (level.getLevel().dimension() != Level.OVERWORLD) {
            return;
        }
        // S11(c) FIREFLY BUSH BAN (owner-flagged twice): firefly_bush specifically is banned OUTRIGHT from
        // 50 deg (SUBPOLAR onset) -- far equatorward of the general 76/82 fade. Identified by the SimpleBlock
        // config's placed STATE, sampled with a THROWAWAY per-position random so the worldgen RNG sequence is
        // never consumed (firefly uses a simple provider, which ignores the random entirely). Rides this same
        // guard + the veg-fade flag family (default ON -- the ban must ship live, not behind the default-off
        // barrens flag); every non-firefly placement falls through to the ordinary fade below.
        if ((Object) this instanceof SimpleBlockFeature simpleFeature
                && LatitudeBiomes.fireflyBanApplies(origin.getX(), origin.getZ())) {
            BlockState toPlace = simpleFeature.toPlace().value()
                    .getState(level, RandomSource.create(origin.asLong()), origin);
            if (toPlace.getBlock() == Blocks.FIREFLY_BUSH) {
                cir.setReturnValue(false);
                return;
            }
        }
        // Latitude + coherent fray. Pays no heightmap lookup below the 76deg onset (returns false there).
        if (!LatitudeBiomes.polarVegetationFadeStrips(origin.getX(), origin.getZ())) {
            return;
        }
        // Would-strip polar column: only mow placements AT THE LOCAL SURFACE so under-cap lush caves stay.
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        if (PolarVegetationFade.nearSurface(origin.getY(), surfaceY)) {
            cir.setReturnValue(false);
        }
    }
}
