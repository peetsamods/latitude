package com.example.globe.mixin;

import com.example.globe.GlobeMod;
import com.example.globe.core.LatitudeV2Flags;
import com.example.globe.core.PolarBarrensBand;
import com.example.globe.util.LatitudeBands;
import com.example.globe.world.LatitudeBiomeResolver;
import com.example.globe.world.LatitudeBiomes;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class ChunkGeneratorPopulateBiomesMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("LatitudeBiomes");

    @Unique
    private static final boolean FIX_SURFACE_CAVE_BIOMES =
            Boolean.parseBoolean(System.getProperty("latitude.fixSurfaceCaveBiomes", "true"));

    @Unique
    private static final int MAX_CAVE_BIOME_Y =
            Integer.getInteger("latitude.maxCaveBiomeY", 96);

    @Unique
    private static final int HARD_DECK_SURFACE_Y =
            Integer.getInteger("latitude.hardDeckSurfaceY", 20);

    @Unique
    private static final int CAVE_SURFACE_MARGIN_BLOCKS =
            Integer.getInteger("latitude.caveSurfaceMarginBlocks", 8);

    @Unique
    private static final boolean DEBUG_CAVE_CLAMP =
            Boolean.getBoolean("latitude.debugCaveClamp");

    @Unique
    private static final boolean DEBUG_CAVE_DECK =
            Boolean.getBoolean("latitude.debugCaveDeck");

    @Unique
    private static final boolean DEBUG_WORLDGEN_PATH =
            Boolean.getBoolean("latitude.debugWorldgenPath");

    @Unique
    private static final boolean DEBUG_BIOME_PICK =
            Boolean.getBoolean("latitude.debugBiomePick");

    @Unique
    private static final int PICK_FAILURE_LOG_LIMIT = 256;

    @Unique
    private static final java.util.concurrent.atomic.AtomicBoolean DEBUG_POPULATE_GATE_REJECT_LOGGED =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Unique
    private static final java.util.concurrent.atomic.AtomicBoolean DEBUG_POPULATE_NO_CONTEXT_LOGGED =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Unique
    private static final java.util.concurrent.atomic.AtomicBoolean DEBUG_WORLDGEN_PATH_ONCE_LOGGED =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Unique
    private static final java.util.concurrent.atomic.AtomicInteger DEBUG_POPBIO_LOG_COUNT =
            new java.util.concurrent.atomic.AtomicInteger(0);

    @Unique
    private static final String GLOBE_SETTINGS_CHECKED =
            "globe:overworld|globe:overworld_xsmall|globe:overworld_small|globe:overworld_regular|globe:overworld_large|globe:overworld_massive";

    // Only apply Latitude to your globe overworld settings (keeps Nether/End sane).
    @Unique
    private static final Identifier GLOBE_SETTINGS_ID = Identifier.fromNamespaceAndPath("globe", "overworld");

    @Unique
    private static final Identifier GLOBE_SETTINGS_XSMALL_ID = Identifier.fromNamespaceAndPath("globe", "overworld_xsmall");

    @Unique
    private static final Identifier GLOBE_SETTINGS_SMALL_ID = Identifier.fromNamespaceAndPath("globe", "overworld_small");

    @Unique
    private static final Identifier GLOBE_SETTINGS_REGULAR_ID = Identifier.fromNamespaceAndPath("globe", "overworld_regular");

    @Unique
    private static final Identifier GLOBE_SETTINGS_LARGE_ID = Identifier.fromNamespaceAndPath("globe", "overworld_large");

    @Unique
    private static final Identifier GLOBE_SETTINGS_MASSIVE_ID = Identifier.fromNamespaceAndPath("globe", "overworld_massive");

    @Unique
    private static final Identifier LUSH_CAVES_ID = Identifier.fromNamespaceAndPath("minecraft", "lush_caves");

    @Unique
    private static final Identifier DRIPSTONE_CAVES_ID = Identifier.fromNamespaceAndPath("minecraft", "dripstone_caves");

    @Unique
    private static final Identifier DEEP_DARK_ID = Identifier.fromNamespaceAndPath("minecraft", "deep_dark");

    @Unique
    private static final Identifier SULFUR_CAVES_ID = Identifier.fromNamespaceAndPath("minecraft", "sulfur_caves");

    // B-9 P2 KEYSTONE (owner flight TEST 113, 2026-07-19: the polar underground read as "generic dark
    // stone"): the globe:glacial_caves underground biome id, resolved once per chunk (see the swap in the
    // resolver below for the full law).
    @Unique
    private static final Identifier GLACIAL_CAVES_BIOME_ID = Identifier.fromNamespaceAndPath("globe", "glacial_caves");

    @Unique
    private static final java.util.concurrent.atomic.AtomicBoolean GLACIAL_CAVES_MISSING_WARNED =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Unique
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_KEY =
            ResourceKey.create(Registries.NOISE_SETTINGS, GLOBE_SETTINGS_ID);

    @Unique
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_XSMALL_KEY =
            ResourceKey.create(Registries.NOISE_SETTINGS, GLOBE_SETTINGS_XSMALL_ID);

    @Unique
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_SMALL_KEY =
            ResourceKey.create(Registries.NOISE_SETTINGS, GLOBE_SETTINGS_SMALL_ID);

    @Unique
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_REGULAR_KEY =
            ResourceKey.create(Registries.NOISE_SETTINGS, GLOBE_SETTINGS_REGULAR_ID);

    @Unique
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_LARGE_KEY =
            ResourceKey.create(Registries.NOISE_SETTINGS, GLOBE_SETTINGS_LARGE_ID);

    @Unique
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_MASSIVE_KEY =
            ResourceKey.create(Registries.NOISE_SETTINGS, GLOBE_SETTINGS_MASSIVE_ID);

    @Unique
    private static final Long2LongOpenHashMap DEBUG_WORLDGEN_CHUNKS = new Long2LongOpenHashMap();

    @Unique
    private static final Long2LongOpenHashMap DEBUG_PICK_FAIL_COLUMNS = new Long2LongOpenHashMap();

    static {
        DEBUG_WORLDGEN_CHUNKS.defaultReturnValue(Long.MIN_VALUE);
        DEBUG_PICK_FAIL_COLUMNS.defaultReturnValue(Long.MIN_VALUE);
    }

    @Shadow
    public abstract boolean stable(ResourceKey<NoiseGeneratorSettings> settings);

    @Unique
    private boolean globe$isAnyGlobeSettings() {
        return GlobeMod.shouldApplyLatitudeWorldgen((NoiseBasedChunkGenerator) (Object) this);
    }

    @Unique
    private int globe$borderRadiusBlocks() {
        return GlobeMod.borderRadiusForNoiseGenerator((NoiseBasedChunkGenerator) (Object) this);
    }

    @Unique
    private static void globe$logPopBio(String phase, String message) {
        if (!DEBUG_WORLDGEN_PATH) {
            return;
        }
        if (DEBUG_POPBIO_LOG_COUNT.getAndIncrement() >= 20) {
            return;
        }
        LOGGER.info("[LAT][POPBIO][{}] {}", phase, message);
    }

    /**
     * Wrap the resolver Minecraft 26.3 has already prepared for this chunk.
     * This is Latitude's final chunk-biome owner, so a mapping mismatch must fail during startup
     * instead of silently generating a fresh world without Latitude geography.
     */
    @Inject(method = "decorateBiomeResolver", at = @At("RETURN"), cancellable = true, require = 1)
    private void globe$wrapBiomeSupplier(
            Blender blender,
            ChunkAccess chunk,
            BiomeResolver originalSupplier,
            CallbackInfoReturnable<BiomeResolver> cir) {
        var pos = chunk.getPos();
        globe$logPopBio("ENTER", "chunk=" + pos.x() + "," + pos.z() + " settings=" + globe$matchedSettingsLabel());
        // Gate: only apply to your globe overworld settings.
        if (!this.globe$isAnyGlobeSettings()) {
            globe$logPopBio("FALLBACK", "settings=" + globe$matchedSettingsLabel() + " action=vanilla populateBiomes");
            if (DEBUG_WORLDGEN_PATH && DEBUG_POPULATE_GATE_REJECT_LOGGED.compareAndSet(false, true)) {
                LOGGER.info("[Latitude] populateBiomes gate reject: settings not Globe preset checked={} matched={} action=falling back to vanilla populateBiomes",
                        GLOBE_SETTINGS_CHECKED, globe$matchedSettingsLabel());
            }
            return;
        }

        Registry<Biome> biomes = LatitudeBiomes.activeBiomeRegistryOrNull();
        RandomState noiseConfig = LatitudeBiomes.activeRandomStateOrNull();
        Climate.Sampler sampler = LatitudeBiomes.activeClimateSamplerOrNull();
        if (biomes == null || noiseConfig == null || sampler == null) {
            globe$logPopBio("FALLBACK", "settings=" + globe$matchedSettingsLabel()
                    + " action=vanilla populateBiomes reason=missing_bound_context");
            if (DEBUG_WORLDGEN_PATH && DEBUG_POPULATE_NO_CONTEXT_LOGGED.compareAndSet(false, true)) {
                LOGGER.info("[Latitude] populateBiomes gate reject: bound resolver context unavailable settings={} action=falling back to vanilla populateBiomes",
                        globe$matchedSettingsLabel());
            }
            return;
        }

        LatitudeBiomes.rememberSourcePolicyBiomeRegistry(biomes);
        // B-9 P2 KEYSTONE: resolve the globe:glacial_caves holder once per chunk (one map get). Missing
        // from the registry (broken datapack) degrades to no-swap with a one-time warn rather than
        // crashing worldgen -- the biome JSON schema test and the boot-time datapack parse gate own that
        // failure class (the carver-holder degrade precedent).
        Holder<Biome> glacialCavesResolved = null;
        if (LatitudeV2Flags.GLACIAL_CAVES_V1_ENABLED) {
            glacialCavesResolved = biomes.get(GLACIAL_CAVES_BIOME_ID).orElse(null);
            if (glacialCavesResolved == null && GLACIAL_CAVES_MISSING_WARNED.compareAndSet(false, true)) {
                LOGGER.warn("[Latitude] B-9 glacial_caves biome missing from the biome registry (id={}) "
                        + "action=skipping the underground biome swap", GLACIAL_CAVES_BIOME_ID);
            }
        }
        final Holder<Biome> glacialCaves = glacialCavesResolved;
        int borderRadiusBlocks = this.globe$borderRadiusBlocks();
        NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator)(Object) this;
        Long2LongOpenHashMap surfaceYCache = new Long2LongOpenHashMap();
        surfaceYCache.defaultReturnValue(Long.MIN_VALUE);
        Long2ObjectOpenHashMap<Holder<Biome>> columnPickCache = new Long2ObjectOpenHashMap<>();
        Long2ObjectOpenHashMap<Holder<Biome>> columnPickBase = new Long2ObjectOpenHashMap<>();
        Long2ObjectOpenHashMap<Holder<Biome>> columnBaseCache = new Long2ObjectOpenHashMap<>();
        // B-9 P2: per-column memoization of the glacial-caves blend+ocean decision (1 = swap, 0 = no) --
        // one region-noise sample per column instead of one per deep quart cell.
        Long2IntOpenHashMap glacialColumnCache = new Long2IntOpenHashMap();
        glacialColumnCache.defaultReturnValue(Integer.MIN_VALUE);
        logWorldgenPathOnce(chunk, borderRadiusBlocks, globe$matchedSettingsLabel());
        BiomeResolver sourceSupplier = cir.getReturnValue() != null
                ? cir.getReturnValue()
                : originalSupplier;

        BiomeResolver wrapped = new LatitudeBiomeResolver(sourceSupplier, (delegate, x, y, z) -> {
            globe$logPopBio("LATITUDE_RESOLVER", "chunk=" + pos.x() + "," + pos.z() + " noise=" + x + "," + y + "," + z);
            // x/z are "noise biome coords" (4-block). Convert to block coords for your latitude math.
            int blockX = (x << 2) + 2;
            int blockZ = (z << 2) + 2;
            int blockY = (y << 2) + 2;
            long colKey = (((long) x) << 32) ^ (z & 0xFFFF_FFFFL);

            Holder<Biome> current = delegate.getNoiseBiome(x, y, z);
            Holder<Biome> base = columnBaseCache.get(colKey);
            if (base == null) {
                base = delegate.getNoiseBiome(
                        x, LatitudeBiomes.SURFACE_CLASSIFY_Y >> 2, z);
                columnBaseCache.put(colKey, base);
            }
            boolean caveCurrent = isCaveBiome(biomes, current);

            if (blockY > HARD_DECK_SURFACE_Y && isCaveBiome(biomes, base)) {
                Holder<Biome> plains = biomes.get(Identifier.fromNamespaceAndPath("minecraft", "plains")).orElse(null);
                if (DEBUG_CAVE_DECK) {
                    LOGGER.info("[LAT_CAVE_DECK] replaced {} at blockY={} x={} z={}",
                            biomeId(biomes, base), blockY, blockX, blockZ);
                }
                if (plains != null) {
                    base = plains;
                }
            }

            int surfaceY = Integer.MIN_VALUE;
            boolean nearSurface = false;
            boolean tooHigh = false;
            boolean deepDarkIllegal = false;
            if (FIX_SURFACE_CAVE_BIOMES && caveCurrent) {
                surfaceY = resolveSurfaceY(generator, noiseConfig, chunk, blockX, blockZ, surfaceYCache);
                nearSurface = blockY >= (surfaceY - CAVE_SURFACE_MARGIN_BLOCKS);
                tooHigh = blockY > MAX_CAVE_BIOME_Y;
                deepDarkIllegal = isDeepDark(biomes, current) && blockY > -16;
                if (nearSurface || tooHigh || deepDarkIllegal) {
                    Holder<Biome> replacement = pickSurfaceReplacement(
                            biomes, base, blockX, blockZ, blockY, borderRadiusBlocks, sampler,
                            generator, noiseConfig, chunk);
                    if (DEBUG_CAVE_CLAMP) {
                        LOGGER.info("[Latitude] Clamped {} at x={} y={} z={} (surfaceY={} margin={} maxY={} deepDarkIllegal={}) -> {}",
                                biomeId(biomes, current), blockX, blockY, blockZ,
                                surfaceY, CAVE_SURFACE_MARGIN_BLOCKS, MAX_CAVE_BIOME_Y,
                                deepDarkIllegal, biomeId(biomes, replacement));
                    }
                    return replacement;
                }
            }
            // B-9 P2 KEYSTONE: beneath glacial-blend LAND columns, every quart cell below the fixed
            // LatitudeBiomes.GLACIAL_CAVES_CEILING_Y (48) line resolves to globe:glacial_caves. Ordering is
            // deliberate: AFTER the near-surface/too-high/deep-dark cave clamps (their outputs stay
            // bitwise-unchanged) and BEFORE the deep-cave pass-through and every pick() path, so deep
            // dripstone/sulfur/lush cells and deep non-cave cells all take the glacial identity while ALL
            // surface and upper quarts (blockY >= 48) keep today's pick order exactly -- the surface-quart
            // identity pin, enforced by isBelowGlacialCaveCeiling. The column decision is the EXACT shared
            // UNDERGROUND BLEND law the crevasse/tunnel carvers and the /latdev locator ride (S28:
            // glacialCaveColumnApplies -> glacialBlendColumnApplies, a wide 78-86 deg band on ONE 640-block
            // coherent region field, deliberately NOT the 64-block surface barrens fray, so the underground
            // identity onsets equatorward of the surface barrens and blends in gradually). Populate runs
            // PRE-NOISE, so this is pure lat/Y math -- no heightmaps, no block reads. Ocean-family columns
            // are excluded from the SAME memoized per-column source sample `base` the resolver classifies
            // everything else with: the sacred sea keeps its vanilla underground. deep_dark cells pass
            // through untouched (ancient cities and sculk are biome-tied; "the underground stays alive"
            // means reskin-plus, never stripping a vanilla underground landmark). Flag-off, or a missing
            // registry entry, is byte-identical: glacialCaves stays null and this block never runs.
            if (glacialCaves != null && LatitudeBiomes.isBelowGlacialCaveCeiling(blockY)) {
                int glacialDecision = glacialColumnCache.get(colKey);
                if (glacialDecision == Integer.MIN_VALUE) {
                    glacialDecision = LatitudeBiomes.glacialCaveColumnApplies(
                            blockX, blockZ, borderRadiusBlocks, base.is(BiomeTags.IS_OCEAN)) ? 1 : 0;
                    glacialColumnCache.put(colKey, glacialDecision);
                }
                if (glacialDecision == 1 && !isDeepDark(biomes, current)) {
                    return glacialCaves;
                }
            }

            // S11(a) LUSH-CAVE VETO: a legitimate DEEP cave cell (past the near-surface/too-high clamps
            // above) resolving to lush_caves inside the polar barrens band remaps to the COLUMN'S SURFACE
            // biome -- lush reads tropical, and dripstone/sulfur/deep_dark deliberately pass through.
            // Band-gated on the core onset, not the fray (PolarBarrensBand.vetoesLushCaveCell documents
            // why). The remap is the SAME per-column memoized surface pick the surface cells get -- no
            // second surface decision, the pick derives its own surface column -- so the cave column
            // inherits exactly the biome above it (fray-aware: polar_barrens or snowy_plains). Runs AFTER
            // the glacial keystone, which owns the same cells when armed.
            if (caveCurrent && PolarBarrensBand.vetoesLushCaveCell(
                    LatitudeV2Flags.POLAR_BARRENS_ENABLED,
                    globe$isBiomeId(biomes, current, LUSH_CAVES_ID),
                    borderRadiusBlocks > 0 ? Math.abs((double) blockZ) * 90.0 / borderRadiusBlocks : Double.NaN)) {
                Holder<Biome> cachedSurfacePick = columnPickCache.get(colKey);
                if (cachedSurfacePick != null && columnPickBase.get(colKey) == base) {
                    return cachedSurfacePick;
                }
                Holder<Biome> surfacePick = globe$pickOrNull(
                        biomes, base, blockX, blockZ, blockY, borderRadiusBlocks,
                        sampler, generator, noiseConfig, chunk);
                if (surfacePick == null) {
                    surfacePick = pickSafeFallback(biomes, blockZ);
                }
                columnPickCache.put(colKey, surfacePick);
                columnPickBase.put(colKey, base);
                return surfacePick;
            }
            if (caveCurrent) {
                return LatitudeBiomes.caveCoverageOverride(biomes, current, blockX, blockY, blockZ);
            }

            // Latitude's non-cave selection is column-based: caller Y is used only for a debug
            // trace, while every physical decision uses the sampled surface column. Reuse one
            // result down the full non-cave column instead of repeating the full climate/pool
            // selection for every quart-Y cell.
            Holder<Biome> cachedPick = columnPickCache.get(colKey);
            if (cachedPick != null && columnPickBase.get(colKey) == base) {
                return cachedPick;
            }
            Holder<Biome> picked = globe$pickOrNull(
                    biomes, base, blockX, blockZ, blockY, borderRadiusBlocks,
                    sampler, generator, noiseConfig, chunk);
            if (picked == null) {
                picked = pickSafeFallback(biomes, blockZ);
            }
            columnPickCache.put(colKey, picked);
            columnPickBase.put(colKey, base);
            return picked;
        });

        globe$logPopBio("ENTER", "installing Latitude resolver chunk=" + pos.x() + "," + pos.z() + " radius=" + borderRadiusBlocks);
        cir.setReturnValue(wrapped);
    }

    @Unique
    private static Holder<Biome> globe$pickOrNull(
            Registry<Biome> biomes, Holder<Biome> base,
            int blockX, int blockZ, int blockY, int borderRadiusBlocks,
            Climate.Sampler sampler, NoiseBasedChunkGenerator generator,
            RandomState noiseConfig, ChunkAccess chunk) {
        Holder<Biome> picked = null;
        try {
            picked = LatitudeBiomes.pick(
                    biomes, base, blockX, blockZ, blockY, borderRadiusBlocks,
                    sampler, "MIXIN", generator, noiseConfig, chunk);
        } catch (RuntimeException t) {
            globe$logPopBio("ERROR", t.getClass().getSimpleName() + ": " + t.getMessage());
            logPickFailOnce(blockX, blockZ, "exception", t.toString());
            if (DEBUG_BIOME_PICK) {
                LOGGER.debug("[Latitude] Biome pick exception", t);
            }
        }
        if (picked == null) {
            logPickFailOnce(blockX, blockZ, "null", null);
            if (DEBUG_BIOME_PICK) {
                LOGGER.debug("[Latitude] Biome pick returned null at x={} z={}", blockX, blockZ);
            }
        }
        return picked;
    }

    @Unique
    private static boolean isCaveBiome(Registry<Biome> biomes, Holder<Biome> entry) {
        if (entry.is(ConventionalBiomeTags.IS_CAVE)
                || entry.is(ConventionalBiomeTags.IS_UNDERGROUND)) {
            return true;
        }
        Identifier actual = biomes.getKey(entry.value());
        if (actual == null) {
            actual = entry.unwrapKey().map(key -> key.identifier()).orElse(null);
        }
        if (actual == null) {
            return false;
        }
        return actual.equals(LUSH_CAVES_ID)
                || actual.equals(DRIPSTONE_CAVES_ID)
                || actual.equals(DEEP_DARK_ID)
                || actual.equals(SULFUR_CAVES_ID);
    }

    @Unique
    private static Holder<Biome> pickSurfaceReplacement(Registry<Biome> biomes, Holder<Biome> base,
                                                                int blockX, int blockZ, int blockY, int borderRadiusBlocks,
                                                                Climate.Sampler sampler,
                                                                NoiseBasedChunkGenerator generator, RandomState noiseConfig, ChunkAccess heightView) {
        Holder<Biome> pick;
        try {
            pick = LatitudeBiomes.pick(biomes, base, blockX, blockZ, blockY, borderRadiusBlocks, sampler, "CAVE_CLAMP",
                    generator, noiseConfig, heightView);
        } catch (RuntimeException t) {
            pick = null;
            logPickFailOnce(blockX, blockZ, "clamp_exception", t.toString());
            if (DEBUG_BIOME_PICK) {
                LOGGER.debug("[Latitude] Clamp pick exception", t);
            }
        }
        if (pick == null) {
            logPickFailOnce(blockX, blockZ, "clamp_null", null);
            pick = pickSafeFallback(biomes, blockZ);
        }
        if (!isCaveBiome(biomes, pick)) {
            return pick;
        }
        if (base != null && !isCaveBiome(biomes, base)) {
            return base;
        }
        return pickSafeFallback(biomes, blockZ);
    }

    @Unique
    private static int resolveSurfaceY(NoiseBasedChunkGenerator generator, RandomState noiseConfig, ChunkAccess heightView,
                                       int blockX, int blockZ, Long2LongOpenHashMap surfaceYCache) {
        long key = (((long) blockX) << 32) ^ (blockZ & 0xFFFF_FFFFL);
        long cached = surfaceYCache.get(key);
        if (cached != Long.MIN_VALUE) {
            return (int) cached;
        }

        int surfaceY;
        if (generator == null || noiseConfig == null || heightView == null) {
            surfaceY = HARD_DECK_SURFACE_Y;
        } else {
            surfaceY = generator.getBaseHeight(blockX, blockZ, Heightmap.Types.WORLD_SURFACE_WG, heightView, noiseConfig);
        }
        surfaceYCache.put(key, surfaceY);
        return surfaceY;
    }

    @Unique
    private static boolean globe$isBiomeId(Registry<Biome> biomes, Holder<Biome> entry, Identifier id) {
        Identifier actual = biomes.getKey(entry.value());
        if (actual == null) {
            actual = entry.unwrapKey().map(key -> key.identifier()).orElse(null);
        }
        return id.equals(actual);
    }

    @Unique
    private static boolean isDeepDark(Registry<Biome> biomes, Holder<Biome> entry) {
        Identifier actual = biomes.getKey(entry.value());
        if (actual == null) {
            actual = entry.unwrapKey().map(key -> key.identifier()).orElse(null);
        }
        return DEEP_DARK_ID.equals(actual);
    }

    @Unique
    private static void logWorldgenPathOnce(ChunkAccess chunk, int borderRadiusBlocks, String settingsLabel) {
        if (!DEBUG_WORLDGEN_PATH) {
            return;
        }
        if (!DEBUG_WORLDGEN_PATH_ONCE_LOGGED.compareAndSet(false, true)) {
            return;
        }
        LOGGER.info("[Latitude] Worldgen path active: overriding populateBiomes settings={} checked={} chunk={} radius={} writing=true",
                settingsLabel, GLOBE_SETTINGS_CHECKED, chunk.getPos(), borderRadiusBlocks);
    }

    @Unique
    private String globe$matchedSettingsLabel() {
        if (this.stable(GLOBE_SETTINGS_KEY)) {
            return "overworld";
        }
        if (this.stable(GLOBE_SETTINGS_XSMALL_KEY)) {
            return "overworld_xsmall";
        }
        if (this.stable(GLOBE_SETTINGS_SMALL_KEY)) {
            return "overworld_small";
        }
        if (this.stable(GLOBE_SETTINGS_REGULAR_KEY)) {
            return "overworld_regular";
        }
        if (this.stable(GLOBE_SETTINGS_LARGE_KEY)) {
            return "overworld_large";
        }
        if (this.stable(GLOBE_SETTINGS_MASSIVE_KEY)) {
            return "overworld_massive";
        }
        if (GlobeMod.shouldApplyLatitudeWorldgen((NoiseBasedChunkGenerator) (Object) this)) {
            return "inline_globe";
        }
        return "unknown";
    }

    @Unique
    private static void logPickFailOnce(int blockX, int blockZ, String reason, String detail) {
        long key = (((long) blockX) << 32) ^ (blockZ & 0xFFFF_FFFFL);
        synchronized (DEBUG_PICK_FAIL_COLUMNS) {
            if (DEBUG_PICK_FAIL_COLUMNS.size() >= PICK_FAILURE_LOG_LIMIT) {
                return;
            }
            if (DEBUG_PICK_FAIL_COLUMNS.putIfAbsent(key, System.nanoTime()) != Long.MIN_VALUE) {
                return;
            }
        }
        if (detail != null) {
            LOGGER.warn("[LAT_PICK_FAIL] x={} z={} reason={} detail={}", blockX, blockZ, reason, detail);
        } else {
            LOGGER.warn("[LAT_PICK_FAIL] x={} z={} reason={}", blockX, blockZ, reason);
        }
    }

    @Unique
    private static Holder<Biome> pickLatitudeFallback(Registry<Biome> biomes, Holder<Biome> base,
                                                             int blockX, int blockZ, int borderRadiusBlocks) {
        int radius = Math.max(1, borderRadiusBlocks);
        LatitudeBands.Band band = LatitudeBands.fromAbsoluteLatitudeDeg(Math.abs((double) blockZ) * 90.0 / radius);
        return switch (band) {
            case SUBPOLAR, POLAR -> pickFallback(biomes, base, "minecraft:snowy_plains", "minecraft:taiga", "minecraft:snowy_taiga");
            case TEMPERATE -> pickFallback(biomes, base, "minecraft:plains", "minecraft:forest", "minecraft:birch_forest");
            case SUBTROPICAL -> pickFallback(biomes, base, "minecraft:savanna", "minecraft:sparse_jungle", "minecraft:jungle");
            case TROPICAL -> pickFallback(biomes, base, "minecraft:jungle", "minecraft:savanna", "minecraft:plains");
        };
    }

    @Unique
    private static Holder<Biome> pickSafeFallback(Registry<Biome> biomes, int blockZ) {
        boolean farNorth = Math.abs(blockZ) > 8000;
        Identifier id = Identifier.fromNamespaceAndPath("minecraft", farNorth ? "snowy_plains" : "plains");
        Holder<Biome> entry = biomes.get(id).orElse(null);
        if (entry != null) {
            return entry;
        }
        return biomes.get(Identifier.fromNamespaceAndPath("minecraft", "plains")).orElse(null);
    }

    @Unique
    private static Holder<Biome> pickFallback(Registry<Biome> biomes, Holder<Biome> base, String... ids) {
        for (String id : ids) {
            Holder<Biome> entry = biomes.get(Identifier.parse(id)).orElse(null);
            if (entry != null) {
                return entry;
            }
        }
        return base != null ? base : biomes.get(Identifier.fromNamespaceAndPath("minecraft", "plains")).orElse(null);
    }

    @Unique
    private static boolean isBiomeId(Registry<Biome> biomes, Holder<Biome> entry, String id) {
        Identifier target = Identifier.parse(id);
        Identifier actual = biomes.getKey(entry.value());
        if (actual != null) {
            return actual.equals(target);
        }
        return entry.unwrapKey().map(key -> key.identifier().equals(target)).orElse(false);
    }

    @Unique
    private static String biomeId(Registry<Biome> biomes, Holder<Biome> entry) {
        Identifier actual = biomes.getKey(entry.value());
        if (actual != null) {
            return actual.toString();
        }
        return entry.unwrapKey().map(key -> key.identifier().toString()).orElse("?");
    }
}
