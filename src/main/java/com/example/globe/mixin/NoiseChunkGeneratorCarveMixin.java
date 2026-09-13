package com.example.globe.mixin;

import com.example.globe.GlobeMod;
import com.example.globe.GlobeRegions;
import com.example.globe.core.GlacialBlend;
import com.example.globe.core.GlacialCarverLaw;
import com.example.globe.core.LatitudeV2Flags;
import com.example.globe.world.LatitudeBiomeSource;
import com.example.globe.world.LatitudeBiomes;
import com.example.globe.world.LatitudeWorldgenScope;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * The carver seam: the legacy polar-cap cancel plus the glacial carver append, both bound to the
 * 26.3 {@code NoiseBasedChunkGenerator.generateCarvers} shape.
 *
 * <h2>26.3 rebinding (maintainer ruling, 2026-09-12)</h2>
 * Three things moved under this seam between 26.2 and 26.3 and every one of them is load-bearing:
 * <ul>
 *   <li>{@code applyCarvers(WorldGenRegion, long, RandomState, BiomeManager, StructureManager,
 *       ChunkAccess)} is gone; the carving loop now lives in
 *       {@code generateCarvers(ChunkAccess, Blender, NoiseChunk, RandomState, BiomeManager,
 *       WorldGenRegion, MaterialRule)}. Its {@code WorldGenRegion} argument is NULLABLE -- vanilla
 *       branches to {@code getBiomeGenerationSettingsForCarver} when it is null -- so the append leg
 *       must tolerate a null region rather than assume one (it needs the region only to reach the
 *       registry access).</li>
 *   <li>{@code ConfiguredWorldCarver} no longer exists: carver holders are {@code Holder<WorldCarver>}
 *       and the registry key is {@link Registries#CARVER} (the datapack directory moved from
 *       {@code worldgen/configured_carver/} to {@code worldgen/carver/} to match).</li>
 *   <li>The biome a seed chunk's carver list is resolved from is reached through a
 *       {@link BiomeResolver} that vanilla builds ONCE per call from the RAW {@code biomeSource}
 *       field ({@code biomeSource.createUncachedResolver(randomState)}), not through a per-call
 *       {@code getNoiseBiome(x, y, z, sampler)}. The sea probe captures that exact resolver
 *       ({@code @Local(ordinal = 0)}) so it asks the same question of the same object, at no extra
 *       sampler cost.</li>
 * </ul>
 *
 * <h2>LEGACY LEG -- verbatim semantics, CENTER-chunk-keyed</h2>
 * On {@code stable(globe:overworld)} worlds (the legacy radius settings KEY only) with the center
 * chunk at {@code |minBlockZ + 8| >= }{@link GlobeRegions#POLAR_CAP_START}, carving is cancelled at
 * HEAD, exactly as the 26.3 line shipped it. Keeping the cancel (rather than the 2.0 line's
 * order-preserving list strip) is deliberate: the 26.3 line's cancel is the behaviour its own worlds
 * were generated against, and cancel and empty-list are block-identical anyway.
 *
 * <h2>B-9 LEG -- seed-chunk glacial-BLEND LAND append (S28)</h2>
 * When {@link LatitudeV2Flags#GLACIAL_CAVES_V1_ENABLED} is on and the SEED chunk is glacial-blend
 * LAND, the {@code globe:crevasse} + {@code globe:glacial_tunnels} holders are appended AFTER the raw
 * list. Appending preserves vanilla's per-carver seeding ({@code setLargeFeatureSeed} with
 * {@code seed + listIndex}): vanilla carvers keep indices 0..n-1 and their exact streams; the glacial
 * pair take n, n+1. Decisions, cheap-first, all deterministic:
 * <ul>
 *   <li><b>Flag + armed radius</b> ({@code LatitudeBiomes.getActiveRadiusBlocks()} is 0 on unarmed
 *       JVMs -- headless tools, non-globe sessions).</li>
 *   <li><b>Blend band (S28)</b>: {@code |lat| = |minBlockZ| * 90 / radius} at the seed chunk's MIN
 *       CORNER against {@link GlacialBlend#BLEND_ONSET_DEG} (78) as the pure-math early-out, then the
 *       EXACT shared decision the glacial-caves biome swap and the {@code /latdev} locator ride,
 *       {@link LatitudeBiomes#glacialBlendColumnApplies}.</li>
 *   <li><b>Globe OVERWORLD generator only</b>: {@code getBiomeSource() instanceof LatitudeBiomeSource}
 *       reuses the wrap gate of {@code ChunkGeneratorBiomeSourceMixin}, so the nether/end of a globe
 *       world cannot pick up glacial carvers at deep {@code |z|}.</li>
 *   <li><b>LAND probe</b>: the captured carver resolver at the seed chunk's min-corner quart with
 *       quart-Y of sea level 63 -- the surface band, so the answer reflects sea-level ocean-ness
 *       rather than a deepslate-depth cave biome. Ocean-family is {@code BiomeTags.IS_OCEAN} ONLY;
 *       rivers are land-band water.</li>
 *   <li><b>Holders</b>: resolved from {@code region.registryAccess()} per accepted seed chunk.
 *       Both-or-nothing: a missing entry skips the append with a one-time warn rather than crashing
 *       worldgen -- the JSON schema tests and the boot-time datapack parse gate own that class.</li>
 * </ul>
 *
 * <p><b>Local capture honesty:</b> the CENTER chunk and the region/randomState come from the method's
 * own arguments (each unambiguous by type); the SEED chunk pos is {@code @Local(ordinal = 1)}
 * {@link ChunkPos} and the carver resolver {@code @Local(ordinal = 0)} {@link BiomeResolver} -- the
 * 26.3 method body allocates exactly two ChunkPos locals (center then seed) and exactly one
 * BiomeResolver, verified against the 26.3-rc-2 disassembly, and mixin application fails loudly at
 * load if that shape changes. Flag-off returns vanilla's own Iterable object untouched.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseChunkGeneratorCarveMixin {
    @Unique
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_KEY = ResourceKey.create(
            Registries.NOISE_SETTINGS,
            Identifier.fromNamespaceAndPath("globe", "overworld")
    );

    @Unique
    private static final ResourceKey<WorldCarver> GLOBE_CREVASSE_KEY = ResourceKey.create(
            Registries.CARVER,
            Identifier.fromNamespaceAndPath("globe", "crevasse")
    );

    @Unique
    private static final ResourceKey<WorldCarver> GLOBE_GLACIAL_TUNNELS_KEY = ResourceKey.create(
            Registries.CARVER,
            Identifier.fromNamespaceAndPath("globe", "glacial_tunnels")
    );

    /** The sea-level probe height (design: "quart-Y at ~sea-level 63"). */
    @Unique
    private static final int GLOBE_SEA_LEVEL_PROBE_Y = 63;

    /** A carver entry is a legacy-strip target iff it is NOT ours ({@code globe:*}). Unkeyed inline
     *  holders have no namespace and therefore strip. Retained for {@link GlacialCarverLaw}'s law
     *  surface even though this line keeps the HEAD cancel for the legacy leg. */
    @Unique
    private static final Predicate<Holder<WorldCarver>> GLOBE_LEGACY_STRIP_TARGET = holder ->
            holder.unwrapKey().map(key -> !"globe".equals(key.identifier().getNamespace())).orElse(true);

    @Unique
    private static final AtomicBoolean GLOBE_MISSING_CARVER_WARNED = new AtomicBoolean(false);

    @Inject(
            method = "generateCarvers(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/levelgen/material/rule/MaterialRule;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void globe$disableCarversInPolarCap(ChunkAccess chunk,
                                                Blender blender,
                                                NoiseChunk noiseChunk,
                                                RandomState randomState,
                                                BiomeManager biomeManager,
                                                WorldGenRegion world,
                                                MaterialRule materialRule,
                                                CallbackInfo ci) {
        if (!LatitudeWorldgenScope.isActive()) {
            return;
        }
        NoiseBasedChunkGenerator self = (NoiseBasedChunkGenerator) (Object) this;
        if (!self.stable(GLOBE_SETTINGS_KEY)) {
            return;
        }

        int centerZ = chunk.getPos().getMinBlockZ() + 8;
        if (Math.abs(centerZ) >= GlobeRegions.POLAR_CAP_START) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "generateCarvers(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/levelgen/material/rule/MaterialRule;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/biome/BiomeGenerationSettings;getCarvers()Ljava/lang/Iterable;"
            )
    )
    private Iterable<Holder<WorldCarver>> globe$appendGlacialCarvers(
            BiomeGenerationSettings settings,
            Operation<Iterable<Holder<WorldCarver>>> original,
            @Local(argsOnly = true) RandomState randomState,
            @Local(argsOnly = true) WorldGenRegion region,
            @Local(ordinal = 0) BiomeResolver carverResolver,
            @Local(ordinal = 1) ChunkPos seedChunkPos) {
        Iterable<Holder<WorldCarver>> raw = original.call(settings);
        NoiseBasedChunkGenerator self = (NoiseBasedChunkGenerator) (Object) this;

        List<Holder<WorldCarver>> glacial =
                globe$glacialCarversForSeedChunk(self, region, carverResolver, seedChunkPos);
        if (glacial.isEmpty()) {
            return raw; // dormant: vanilla's own Iterable, untouched -- byte-identical.
        }
        return GlacialCarverLaw.filter(raw, false, true, glacial, GLOBE_LEGACY_STRIP_TARGET);
    }

    /**
     * The B-9 leg's per-seed-chunk decision + holder resolution (see class javadoc for each gate's
     * why). Returns the {@code [crevasse, glacial_tunnels]} pair when the seed chunk is flag-on
     * glacial-blend land on a globe overworld generator, else an empty list.
     */
    @Unique
    private List<Holder<WorldCarver>> globe$glacialCarversForSeedChunk(
            NoiseBasedChunkGenerator self, WorldGenRegion region, BiomeResolver carverResolver,
            ChunkPos seedChunkPos) {
        if (!LatitudeV2Flags.GLACIAL_CAVES_V1_ENABLED) {
            globe$debugExit("flagOff", seedChunkPos);
            return List.of();
        }
        int radius = LatitudeBiomes.getActiveRadiusBlocks();
        if (radius <= 0) {
            globe$debugExit("noRadius", seedChunkPos);
            return List.of();
        }
        int minBlockX = seedChunkPos.getMinBlockX();
        int minBlockZ = seedChunkPos.getMinBlockZ();
        double absLatDeg = Math.abs((double) minBlockZ) * 90.0 / radius;
        if (absLatDeg <= GlacialBlend.BLEND_ONSET_DEG) {
            return List.of(); // below the blend onset: pure-math exit, no region sample, no probe. (Not
                              // debug-counted: this is the whole equatorward world, it would drown the log.)
        }
        if (region == null) {
            // 26.3: generateCarvers accepts a null region (the non-region carver-settings branch). The
            // append needs registryAccess, so it declines rather than guessing. Vanilla carving is
            // untouched on that path.
            globe$debugExit("noRegion", seedChunkPos);
            return List.of();
        }
        if (!(self.getBiomeSource() instanceof LatitudeBiomeSource)) {
            globe$debugExit("notLatitudeSource", seedChunkPos);
            return List.of(); // nether/end (or a non-globe generator with a stale armed radius).
        }
        // S28 UNDERGROUND GLACIAL BLEND: the crevasse/tunnel append rides the EXACT shared blend decision
        // the glacial-caves biome swap and the /latdev locator use (LatitudeBiomes.glacialBlendColumnApplies
        // -> GlacialBlend.undergroundGlacial on the 640-block region field, wide 78-86 deg band), so none of
        // biome/crevasse/locator outruns another.
        if (!LatitudeBiomes.glacialBlendColumnApplies(minBlockX, minBlockZ, radius)) {
            globe$debugExit("blendSaysNo", seedChunkPos);
            return List.of();
        }
        Holder<Biome> seaProbe = carverResolver.getNoiseBiome(
                QuartPos.fromBlock(minBlockX),
                QuartPos.fromBlock(GLOBE_SEA_LEVEL_PROBE_Y),
                QuartPos.fromBlock(minBlockZ));
        if (seaProbe.is(BiomeTags.IS_OCEAN)) {
            globe$debugExit("seaChunk", seedChunkPos);
            return List.of(); // sea seed chunk: the frozen sea is sacred (and structurally uncarvable anyway).
        }
        Registry<WorldCarver> carvers = region.registryAccess().lookupOrThrow(Registries.CARVER);
        Optional<Holder.Reference<WorldCarver>> crevasse = carvers.get(GLOBE_CREVASSE_KEY);
        Optional<Holder.Reference<WorldCarver>> tunnels = carvers.get(GLOBE_GLACIAL_TUNNELS_KEY);
        if (crevasse.isEmpty() || tunnels.isEmpty()) {
            if (GLOBE_MISSING_CARVER_WARNED.compareAndSet(false, true)) {
                GlobeMod.LOGGER.warn(
                        "[Latitude] B-9 glacial carvers missing from the carver registry "
                                + "(crevasse present={}, glacial_tunnels present={}) action=skipping append",
                        crevasse.isPresent(), tunnels.isPresent());
            }
            return List.of();
        }
        globe$debugExit("APPEND", seedChunkPos);
        return List.of(crevasse.get(), tunnels.get());
    }

    /** S31 diagnosis instrument ({@code -Dlatitude.debugCollapse=true}): name the exit every glacial-band seed
     *  chunk takes through the append decision, so a "no crevasses live" report can be traced to its gate. */
    @Unique
    private static void globe$debugExit(String reason, ChunkPos pos) {
        if (Boolean.getBoolean("latitude.debugCollapse")) {
            GlobeMod.LOGGER.info("[LAT][CARVEGATE] {} chunk=({},{})", reason, pos.x(), pos.z());
        }
    }
}
