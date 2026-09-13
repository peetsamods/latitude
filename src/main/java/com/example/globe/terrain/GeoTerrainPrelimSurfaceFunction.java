package com.example.globe.terrain;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/**
 * Slice C-2 companion wrapper for {@code NoiseRouter.preliminarySurfaceLevel} (field #11).
 *
 * <p><b>Why #11 is wrapped now after the locked design deliberately refused to.</b> The r1 design was
 * rejected for adding the same DENSITY-unit bias term to #11 (a block-Y-unit field) — a unit mismatch,
 * and correctly retired (LESSONS L16). The bathymetry regime changes the calculus: its carve target IS a
 * block-Y level, so {@code min(prelim, ceilY)} is unit-correct — and it is REQUIRED, not optional: the
 * first bathymetry gate run showed carved sea cavities not flooding (air pockets + perched aquifers under
 * the ocean), because the aquifer/fluid system distinguishes "open to the sea" from "underground cave"
 * via the preliminary surface, which still reported the old, uncarved height. Aligning #11 with the carve
 * ceiling makes the fluid system treat carved ocean as ocean.
 *
 * <p>The land side passes through untouched (the additive lift is small and density-unit; the historical
 * unit-mismatch rejection stands for that side). No carve (S==0, r==0, NoOp provider, land column) means
 * exact pass-through, preserving the S=0 byte-identity contract. The carve ceiling comes from
 * {@link GeoTerrainBiasFunction#carveCeilYOrInfinity}, the single source shared with the finalDensity
 * wrapper, so the two fields can never disagree about the target; the shared per-thread column memo makes
 * the second consumer effectively free.
 */
public final class GeoTerrainPrelimSurfaceFunction implements DensityFunction {

    private final DensityFunction delegate;

    private final MapCodec<GeoTerrainPrelimSurfaceFunction> codec = MapCodec.unit(this);

    public GeoTerrainPrelimSurfaceFunction(DensityFunction delegate) {
        this.delegate = delegate;
    }

    /**
     * The clamp itself. 26.3 replaced {@code compute(FunctionContext)} with a compile-then-sample contract,
     * so the delegate's value and the column arrive as plain arguments; the delegate is evaluated outside
     * this try (see {@link Sampler#sampleValue}), mirroring GeoTerrainBiasFunction, so its own failures
     * propagate exactly as they would unwrapped.
     */
    private double globe$clamped(double base, int blockX, int blockZ) {
        try {
            // Slice C-3 note: the helper's ceiling is already grip-graded, so this stays C-2's pure
            // min() clamp and the two wrappers keep reading the SAME effective ceiling per column.
            double ceilY = GeoTerrainBiasFunction.carveCeilYOrInfinity(blockX, blockZ);
            return Double.isInfinite(ceilY) ? base : Math.min(base, ceilY);
        } catch (Throwable t) {
            GeoTerrainBiasFunction.logBiasFailureOnce(t);
            return base;
        }
    }

    @Override
    public DensitySampler compileSampler(CompileContext compileContext) {
        return new Sampler(delegate.compileSampler(compileContext));
    }

    /** Compiled form; the clamp is a pure per-cell function so volume sampling is the naive walk. */
    private final class Sampler implements DensitySampler {

        private final DensitySampler base;

        private Sampler(DensitySampler base) {
            this.base = base;
        }

        @Override
        public float sampleValue(SamplerContext context, int x, int y, int z) {
            float raw = base.sampleValue(context, x, y, z);
            return (float) globe$clamped(raw, x, z);
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer buffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, buffer, volume, this);
        }
    }

    /**
     * min: the clamp can pull the reported surface down to {@code SEA_LEVEL - maxDepth} when it can bind
     * (same condition as the finalDensity wrapper's clamp regime). max: min() never raises, so the
     * delegate's ceiling stands.
     */
    @Override
    public Interval range() {
        Interval base = delegate.range();
        double maxDepth = Math.abs(com.example.globe.core.LatitudeV2Flags.TERRAIN_V2_STRENGTH)
                * Math.abs(com.example.globe.core.LatitudeV2Flags.TERRAIN_V2_OCEAN_STRENGTH_RATIO) * 60.0;
        float min = maxDepth > 0.0 ? (float) Math.min(base.min(), 63.0 - maxDepth) : base.min();
        return Interval.of(min, base.max());
    }

    /** The clamp is a column (X/Z) decision applied at every Y, so the node varies on all three axes. */
    @Override
    public int domainAxes() {
        return DensityFunction.ALL_AXES;
    }

    @Override
    public MapCodec<? extends DensityFunction> codec() {
        return codec;
    }

    /** Same structural contract as GeoTerrainBiasFunction.rewriteChildren (design §9-R5): rewrap the child. */
    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new GeoTerrainPrelimSurfaceFunction(rule.rewrite(delegate));
    }
}
