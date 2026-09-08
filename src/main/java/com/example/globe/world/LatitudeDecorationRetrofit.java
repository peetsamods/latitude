package com.example.globe.world;

import com.example.globe.GlobeMod;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Opt-in retrofit for worlds whose already-generated chunks predate the ledger-decoration fix.
 *
 * <p>Before the fix, a biome admitted only through the provider-ticket ledger (no {@code lat_*}
 * policy-tag membership) generated with terrain and biome identity but none of its own features:
 * vanilla's decoration narrowed the chunk's biome set against the raw source's
 * {@code possibleBiomes} and consulted a feature index that had never seen the biome. The visible
 * result is a bare biome — the maintainer's Overgrown Greens. Chunks generated after the fix are correct;
 * chunks generated before it stay bare forever, because decoration is a generation-time,
 * once-per-chunk event.
 *
 * <p>This engine re-runs exactly what was dropped and nothing else: for a chunk whose stored
 * section palettes contain a retrofit-eligible biome, it replays that biome's
 * {@code VEGETAL_DECORATION} placed features through the normal runtime placement pipeline
 * ({@link PlacedFeature#placeWithBiomeCheck} — the same operation vanilla's {@code /place} command
 * performs). Safety comes from the features' own machinery, not from this class: every vegetal
 * feature carries biome and survivability filters, so placements self-limit to columns whose
 * stored biome matches and to positions that are genuinely plantable (air over valid ground).
 * Columns of neighboring biomes already declined these features at generation time via the same
 * filters, so the replay cannot double anything that already ran. Only the vegetal step is
 * replayed: shared underground content (ores, springs, carvers) ran at generation through the
 * chunk's other biomes and does not need repair.
 *
 * <p>Eligibility is deliberately historical: a biome qualifies when the provider-ticket ledger
 * routes it but none of {@link #DECORATION_POLICY_TAG_PATHS} contains it — the exact membership
 * test the pre-fix decoration guard applied. Biomes that were in those tags were protected all
 * along and never generated bare; replaying them would double-decorate. For the same reason the
 * tag list below must stay APPEND-ONLY history: removing an entry would widen retrofit onto
 * chunks that were never bare.
 *
 * <p>Handled chunks are recorded on the world state, which persists the marker with the save —
 * and every newly generated chunk is marked at decoration time by
 * {@code ChunkGeneratorGenerateFeaturesBiomeSetMixin} — so no chunk is ever processed twice, and
 * post-fix chunks are never touched at all. Work is throttled to two chunks per server tick, with
 * at most 2,048 chunks pending at once.
 *
 * <p>On a world that never captured a provider-ticket profile (created by a dedicated server
 * before that fix), enabling the retrofit also adopts a profile so NEW chunks gain the full
 * roster. That world's old chunks contain no ledger biomes to retrofit; the seam between old and
 * new regions is inherent to what is being asked for, and the command's confirmation warning says
 * so.
 */
public final class LatitudeDecorationRetrofit {

    /**
     * The decoration guard's policy-tag membership list. Shared with
     * {@code ChunkGeneratorGenerateFeaturesBiomeSetMixin} (mixin classes must not be referenced
     * from ordinary code, so the constant lives here). APPEND-ONLY: retrofit eligibility is
     * defined as "ledger-routed but absent from these tags at the time the bare chunks were
     * generated" — see the class javadoc.
     */
    public static final String[] DECORATION_POLICY_TAG_PATHS = {
            "lat_tropics_primary",
            "lat_tropics_secondary",
            "lat_tropics_accent",
            "lat_arid_primary",
            "lat_arid_secondary",
            "lat_arid_accent",
            "lat_trans_arid_tropics_1_primary",
            "lat_trans_arid_tropics_1_secondary",
            "lat_trans_arid_tropics_1_accent",
            "lat_trans_arid_tropics_2_primary",
            "lat_trans_arid_tropics_2_secondary",
            "lat_trans_arid_tropics_2_accent",
            "lat_subtropical_humid_primary",
            "lat_subtropical_humid_secondary",
            "lat_subtropical_humid_accent",
            "lat_temperate_primary",
            "lat_temperate_secondary",
            "lat_temperate_accent",
            "lat_temperate_mountain",
            "lat_subpolar_primary",
            "lat_subpolar_secondary",
            "lat_subpolar_accent",
            "lat_polar_primary",
            "lat_polar_secondary",
            "lat_polar_accent",
            "lat_ocean_tropical",
            "lat_ocean_temperate",
            "lat_ocean_subpolar",
            "lat_ocean_polar"
    };

    private static final int CHUNKS_PER_TICK = 2;
    private static final int MAX_PENDING_CHUNKS = 2048;
    private static final int ENABLE_SWEEP_RADIUS_CHUNKS = 10;
    private static final long CONFIRM_WINDOW_MS = 60_000L;
    private static final long OVERFLOW_WARNING_INTERVAL_MS = 60_000L;
    private static final long COMPLETION_INFO_INTERVAL_MS = 60_000L;

    // All mutated on the server thread only (commands, chunk-load events, end-of-tick).
    private static final ArrayDeque<Long> QUEUE = new ArrayDeque<>();
    private static final Set<Long> QUEUED = new HashSet<>();
    private static volatile long pendingConfirmDeadlineMs;
    private static final AtomicInteger CHUNKS_PROCESSED = new AtomicInteger();
    private static final AtomicInteger CHUNKS_SCANNED = new AtomicInteger();
    private static final AtomicInteger CHUNKS_DEFERRED = new AtomicInteger();
    private static final AtomicInteger CHUNKS_RETROFITTED = new AtomicInteger();
    private static final AtomicInteger FEATURES_PLACED = new AtomicInteger();
    private static volatile long lastOverflowWarningMs = Long.MIN_VALUE;
    private static volatile long lastCompletionInfoMs = Long.MIN_VALUE;
    private static volatile boolean completionSummaryPending;
    private static volatile List<ResourceLocation> eligibleCache;

    /**
     * The world state of every loaded Latitude overworld, captured on the server thread when the
     * level loads. Decoration runs on the worldgen threads, where reaching into a level's data
     * storage to find (or create) that state is not safe; marking through the already-resolved
     * instance is. Cleared as levels unload and again when the server stops.
     */
    private static final Map<ServerLevel, LatitudeWorldState> WORLD_STATES = new ConcurrentHashMap<>();

    /**
     * Phase of the level-load event in which {@link #cacheWorldState} runs. Whether an overworld is
     * Latitude's can depend on the create-screen radius, and the handler that records it (in
     * {@code GlobeMod}) sits in the default phase, registered after this class's own. Fabric runs a
     * phase in registration order, so an unordered handler here would ask before the answer exists
     * and such a world would cache nothing. Running after the whole default phase instead makes
     * the load event the moment the answer is final: nothing has to ask again on later ticks, and
     * a world Latitude did not generate is probed exactly once.
     */
    private static final ResourceLocation AFTER_RECOGNITION_PHASE =
            new ResourceLocation("globe", "retrofit_after_recognition");

    private LatitudeDecorationRetrofit() {
    }

    public static void init() {
        ServerWorldEvents.LOAD.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_RECOGNITION_PHASE);
        ServerWorldEvents.LOAD.register(AFTER_RECOGNITION_PHASE, (server, world) -> cacheWorldState(world));
        ServerWorldEvents.UNLOAD.register((server, world) -> WORLD_STATES.remove(world));
        ServerChunkEvents.CHUNK_LOAD.register(LatitudeDecorationRetrofit::onChunkLoad);
        ServerTickEvents.END_SERVER_TICK.register(LatitudeDecorationRetrofit::onEndTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            clearQueueState();
            // Unlike the queue reset above, the level cache is not touched when the retrofit is
            // merely switched off: generation-time marking has to keep working either way.
            WORLD_STATES.clear();
        });
    }

    /**
     * Captures a level's Latitude state while the server thread still owns it, so decoration can
     * mark through it later. Only a Latitude overworld is ever retrofitted, and only a Latitude
     * world has this state to begin with — caching anywhere else would write a Latitude state file
     * into a save that has none.
     */
    private static void cacheWorldState(ServerLevel world) {
        if (world == null || WORLD_STATES.containsKey(world)
                || world.dimension() != Level.OVERWORLD
                || !GlobeMod.isLatitudeOverworld(world)) {
            return;
        }
        WORLD_STATES.put(world, LatitudeWorldState.get(world));
    }

    /**
     * Marks a chunk as decorated under the fixed index; called from the decoration mixin for every
     * freshly generated chunk, and once more for every chunk the repair loop handles.
     *
     * <p>The 1.20 range has no per-chunk attachment API, so the marker lives on the world state
     * instead of on the chunk. Decoration runs on a worldgen thread, so this goes through the
     * instance {@link #cacheWorldState} already resolved rather than through the level's data
     * storage; a level with nothing cached — the window before it finished loading, and every world
     * Latitude did not generate — records nothing.</p>
     *
     * <p>Marking is deliberately unconditional rather than gated on the retrofit being armed:
     * a chunk generated correctly today has to look correct to a retrofit armed tomorrow, and the
     * only record of that is this marker.</p>
     */
    public static void markDecoratedUnderFixedIndex(LevelAccessor level, ChunkAccess chunk) {
        if (!(level instanceof ServerLevelAccessor serverLevel) || chunk == null) {
            return;
        }
        LatitudeWorldState state = WORLD_STATES.get(serverLevel.getLevel());
        if (state == null) {
            return;
        }
        state.markRetrofitted(chunk.getPos().toLong());
    }

    /** Whether this chunk was already decorated under the fixed index. */
    private static boolean isDecoratedUnderFixedIndex(ServerLevel world, ChunkPos pos) {
        LatitudeWorldState state = WORLD_STATES.get(world);
        if (state == null) {
            state = LatitudeWorldState.getIfPresent(world);
        }
        return state != null && state.isRetrofitted(pos.toLong());
    }

    public static boolean isEnabled(ServerLevel world) {
        LatitudeWorldState state = LatitudeWorldState.getIfPresent(world);
        return state != null && state.isRetrofitEnabled();
    }

    /** First step of the two-step enable; returns the warning lines to show the operator. */
    public static List<String> requestEnable(ServerLevel world) {
        // A world Latitude did not generate must never reach the adoption step: adoption persists
        // a globe radius and arms biome authority, permanently converting a vanilla save with no
        // way back. Refuse before arming the confirm window.
        if (!GlobeMod.isLatitudeOverworld(world)) {
            return List.of("This overworld was not generated by Latitude, so there is nothing to "
                    + "retrofit. Retrofit only repairs missing decoration in Latitude worlds; it "
                    + "will not convert a vanilla or third-party world into one.");
        }
        // Read-only until the operator confirms — do not create a state file just to warn.
        LatitudeWorldState state = LatitudeWorldState.getIfPresent(world);
        pendingConfirmDeadlineMs = System.currentTimeMillis() + CONFIRM_WINDOW_MS;
        boolean needsProfileAdoption = state == null || state.getProviderTicketProfile().isEmpty();
        List<String> lines = new ArrayList<>();
        lines.add("Latitude retrofit will re-run the missing plant decoration for provider biomes "
                + "that generated bare in already-explored chunks (only where the stored biome "
                + "matches; placements go into open air over valid ground).");
        if (needsProfileAdoption) {
            lines.add("This world has no provider-biome roster yet (it was created before Latitude "
                    + "captured one). Enabling will adopt a roster NOW: newly generated chunks will "
                    + "place provider biomes that older chunks do not have, so terrain generated "
                    + "before and after this switch will differ at the boundary.");
        }
        lines.add("BACK UP YOUR WORLD FIRST. This changes world data and cannot be undone by "
                + "flipping the switch back off.");
        lines.add("To proceed, run: /latitude retrofit confirm (within 60 seconds)");
        return lines;
    }

    /** Second step; performs the actual enable. Returns feedback lines. */
    public static List<String> confirmEnable(MinecraftServer server, ServerLevel world) {
        List<String> lines = new ArrayList<>();
        if (!GlobeMod.isLatitudeOverworld(world)) {
            lines.add("This overworld was not generated by Latitude; retrofit does not apply.");
            return lines;
        }
        if (System.currentTimeMillis() > pendingConfirmDeadlineMs) {
            lines.add("No pending retrofit request (or it expired). Run /latitude retrofit enable first.");
            return lines;
        }
        pendingConfirmDeadlineMs = 0L;
        LatitudeWorldState state = LatitudeWorldState.get(world);
        // Second chance at the level cache, on the server thread: a world recognised as Latitude's
        // only after it finished loading would otherwise mark nothing for the repair loop.
        cacheWorldState(world);
        if (state.getProviderTicketProfile().isEmpty()) {
            GlobeMod.adoptProviderTicketProfile(server, world, "retrofit adoption");
            lines.add("Provider-biome roster adopted: newly generated chunks will now place the "
                    + "full provider set.");
        }
        state.setRetrofitEnabled(true);
        eligibleCache = null;
        int seeded = sweepAroundPlayers(world);
        GlobeMod.LOGGER.info(
                "[Latitude] retrofit enabled: pending={}/{} deferred={} seeded={}",
                QUEUE.size(), MAX_PENDING_CHUNKS, CHUNKS_DEFERRED.get(), seeded);
        lines.add("Retrofit enabled. Already-loaded terrain near players has been queued ("
                + seeded + " chunks); other affected chunks are handled as they load.");
        return lines;
    }

    public static List<String> disable(ServerLevel world) {
        // Nothing to turn off on a world that never had state — and no reason to create one.
        LatitudeWorldState state = LatitudeWorldState.getIfPresent(world);
        if (state != null) {
            state.setRetrofitEnabled(false);
        }
        GlobeMod.LOGGER.info(
                "[Latitude] retrofit disabled: pending={}/{} processed={} scanned={} deferred={} "
                        + "retrofitted={} featuresPlaced={}",
                QUEUE.size(), MAX_PENDING_CHUNKS, CHUNKS_PROCESSED.get(), CHUNKS_SCANNED.get(),
                CHUNKS_DEFERRED.get(), CHUNKS_RETROFITTED.get(), FEATURES_PLACED.get());
        clearQueueState();
        return List.of("Retrofit disabled. Chunks already retrofitted keep their decoration; "
                + "unprocessed chunks stay as they are.");
    }

    public static List<String> status(ServerLevel world) {
        LatitudeWorldState state = LatitudeWorldState.getIfPresent(world);
        boolean enabled = state != null && state.isRetrofitEnabled();
        boolean hasProfile = state != null && state.getProviderTicketProfile().isPresent();
        return List.of(
                "Retrofit: " + (enabled ? "ENABLED" : "disabled")
                        + " | provider roster: " + (hasProfile ? "present" : "absent"),
                "pending/capacity: " + QUEUE.size() + "/" + MAX_PENDING_CHUNKS
                        + " | processed/scanned: " + CHUNKS_PROCESSED.get() + "/" + CHUNKS_SCANNED.get()
                        + " | deferred: " + CHUNKS_DEFERRED.get(),
                "retrofitted: " + CHUNKS_RETROFITTED.get()
                        + " | features placed: " + FEATURES_PLACED.get(),
                "Eligible biomes: " + eligibleSummary(world));
    }

    // ── Event plumbing ──

    private static void onChunkLoad(ServerLevel world, LevelChunk chunk) {
        if (world != world.getServer().overworld() || !isEnabled(world)) {
            return;
        }
        if (isDecoratedUnderFixedIndex(world, chunk.getPos())) {
            return;
        }
        if (!enqueue(chunk.getPos())) {
            // A duplicate is already pending; an overflow rejection remains unmarked so a later
            // chunk-load event can retry it after capacity becomes available.
            return;
        }
    }

    private static void onEndTick(MinecraftServer server) {
        if (QUEUE.isEmpty()) {
            maybeLogCompletionSummary();
            return;
        }
        ServerLevel world = server.overworld();
        if (world == null || !isEnabled(world)) {
            clearQueueState();
            return;
        }
        for (int i = 0; i < CHUNKS_PER_TICK && !QUEUE.isEmpty(); i++) {
            long packed = QUEUE.pollFirst();
            QUEUED.remove(packed);
            processChunk(world, new ChunkPos(packed));
        }
        if (QUEUE.isEmpty()) {
            maybeLogCompletionSummary();
        }
    }

    private static boolean enqueue(ChunkPos pos) {
        long packed = pos.toLong();
        if (QUEUED.contains(packed)) {
            return false;
        }
        if (QUEUE.size() >= MAX_PENDING_CHUNKS) {
            CHUNKS_DEFERRED.incrementAndGet();
            maybeWarnQueueOverflow(pos);
            return false;
        }
        QUEUED.add(packed);
        QUEUE.addLast(packed);
        completionSummaryPending = true;
        return true;
    }

    private static int sweepAroundPlayers(ServerLevel world) {
        int seeded = 0;
        for (ServerPlayer player : world.players()) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -ENABLE_SWEEP_RADIUS_CHUNKS; dx <= ENABLE_SWEEP_RADIUS_CHUNKS; dx++) {
                for (int dz = -ENABLE_SWEEP_RADIUS_CHUNKS; dz <= ENABLE_SWEEP_RADIUS_CHUNKS; dz++) {
                    ChunkAccess chunk = world.getChunkSource()
                            .getChunk(center.x + dx, center.z + dz, ChunkStatus.FULL, false);
                    if (chunk instanceof LevelChunk level
                            && !isDecoratedUnderFixedIndex(world, level.getPos())) {
                        if (enqueue(level.getPos())) {
                            seeded++;
                        }
                    }
                }
            }
        }
        return seeded;
    }

    // ── The actual work ──

    private static void processChunk(ServerLevel world, ChunkPos pos) {
        CHUNKS_PROCESSED.incrementAndGet();
        ChunkAccess access = world.getChunkSource().getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
        if (!(access instanceof LevelChunk chunk)) {
            return;
        }
        if (isDecoratedUnderFixedIndex(world, pos)) {
            return;
        }
        CHUNKS_SCANNED.incrementAndGet();
        try {
            List<Holder<Biome>> present = eligibleBiomesIn(world, chunk);
            if (!present.isEmpty()) {
                int placed = replayVegetalFeatures(world, pos, present);
                FEATURES_PLACED.addAndGet(placed);
                CHUNKS_RETROFITTED.incrementAndGet();
                GlobeMod.LOGGER.debug("[Latitude] retrofit decorated chunk {},{}: biomes={} featuresPlaced={}",
                        pos.x, pos.z,
                        present.stream().map(h -> h.unwrapKey().map(k -> k.location().toString()).orElse("?")).toList(),
                        placed);
            }
        } catch (Exception e) {
            // Never crash the server for cosmetic repair; mark handled so a poison chunk cannot
            // wedge the queue by re-entering it on every load.
            GlobeMod.LOGGER.warn("[Latitude] retrofit failed for chunk {},{} (marking handled)", pos.x, pos.z, e);
        }
        markDecoratedUnderFixedIndex(world, chunk);
        chunk.setUnsaved(true);
    }

    private static void maybeWarnQueueOverflow(ChunkPos pos) {
        long now = System.currentTimeMillis();
        if (lastOverflowWarningMs == Long.MIN_VALUE
                || now < lastOverflowWarningMs
                || now - lastOverflowWarningMs >= OVERFLOW_WARNING_INTERVAL_MS) {
            lastOverflowWarningMs = now;
            GlobeMod.LOGGER.warn(
                    "[Latitude] retrofit queue is full ({}/{}); deferred chunk {},{} will retry on a later load",
                    QUEUE.size(), MAX_PENDING_CHUNKS, pos.x, pos.z);
        }
    }

    private static void maybeLogCompletionSummary() {
        if (!completionSummaryPending) {
            return;
        }
        long now = System.currentTimeMillis();
        if (lastCompletionInfoMs != Long.MIN_VALUE
                && now >= lastCompletionInfoMs
                && now - lastCompletionInfoMs < COMPLETION_INFO_INTERVAL_MS) {
            return;
        }
        lastCompletionInfoMs = now;
        completionSummaryPending = false;
        GlobeMod.LOGGER.info(
                "[Latitude] retrofit queue complete: processed={} scanned={} deferred={} retrofitted={} "
                        + "featuresPlaced={}",
                CHUNKS_PROCESSED.get(), CHUNKS_SCANNED.get(), CHUNKS_DEFERRED.get(),
                CHUNKS_RETROFITTED.get(), FEATURES_PLACED.get());
    }

    private static void clearQueueState() {
        QUEUE.clear();
        QUEUED.clear();
        pendingConfirmDeadlineMs = 0L;
        CHUNKS_PROCESSED.set(0);
        CHUNKS_SCANNED.set(0);
        CHUNKS_DEFERRED.set(0);
        CHUNKS_RETROFITTED.set(0);
        FEATURES_PLACED.set(0);
        lastOverflowWarningMs = Long.MIN_VALUE;
        lastCompletionInfoMs = Long.MIN_VALUE;
        completionSummaryPending = false;
        eligibleCache = null;
    }

    private static List<Holder<Biome>> eligibleBiomesIn(ServerLevel world, LevelChunk chunk) {
        List<ResourceLocation> eligible = eligibleBiomeIds(world);
        if (eligible.isEmpty()) {
            return List.of();
        }
        Registry<Biome> registry = world.registryAccess().registryOrThrow(Registries.BIOME);
        Map<ResourceLocation, Holder<Biome>> found = new LinkedHashMap<>();
        for (LevelChunkSection section : chunk.getSections()) {
            section.getBiomes().getAll(holder -> holder.unwrapKey().ifPresent(key -> {
                ResourceLocation id = key.location();
                if (eligible.contains(id)) {
                    found.putIfAbsent(id, holder);
                }
            }));
        }
        if (found.isEmpty()) {
            return List.of();
        }
        // Re-resolve through the registry so downstream feature lookups use canonical holders.
        List<Holder<Biome>> out = new ArrayList<>();
        for (ResourceLocation id : found.keySet()) {
            registry.getHolder(ResourceKey.create(Registries.BIOME, id)).ifPresent(out::add);
        }
        return out;
    }

    private static int replayVegetalFeatures(ServerLevel world, ChunkPos pos, List<Holder<Biome>> biomes) {
        int step = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
        BlockPos origin = new BlockPos(pos.getMinBlockX(), world.getMinBuildHeight(), pos.getMinBlockZ());
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(world.getSeed()));
        long decorationSeed = random.setDecorationSeed(world.getSeed(), origin.getX(), origin.getZ());
        int placed = 0;
        for (Holder<Biome> biome : biomes) {
            List<HolderSet<PlacedFeature>> features = biome.value().getGenerationSettings().features();
            if (step >= features.size()) {
                continue;
            }
            int index = 0;
            for (Holder<PlacedFeature> feature : features.get(step)) {
                random.setFeatureSeed(decorationSeed, index++, step);
                try {
                    // placeWithBiomeCheck consults the STORED biome at each candidate position and
                    // only places where that biome actually carries this feature — the runtime
                    // equivalent of generation's BiomeFilter, and what keeps the replay from
                    // spilling into neighboring columns.
                    if (feature.value().placeWithBiomeCheck(
                            world, world.getChunkSource().getGenerator(), random, origin)) {
                        placed++;
                    }
                } catch (Exception e) {
                    GlobeMod.LOGGER.warn("[Latitude] retrofit feature failed in chunk {},{} ({}): {}",
                            pos.x, pos.z, feature.unwrapKey().map(k -> k.location().toString()).orElse("?"),
                            e.toString());
                }
            }
        }
        return placed;
    }

    // ── Eligibility ──

    /**
     * Every custom biome Latitude can paint: the {@link #DECORATION_POLICY_TAG_PATHS} union
     * {@link BiomeDescriptorLedger}, registry-resolved. This is the full producible set — unlike
     * {@link #eligibleBiomeIds}, which deliberately excludes tagged biomes to scope retrofit
     * replay. Shared with {@code ChunkGeneratorGenerateFeaturesBiomeSetMixin} (the decoration-index
     * protection) and {@code LatitudeBiomeSource} (the {@code /locate biome} candidate pool):
     * both need "everything Latitude could have placed here," not a generation-order-scoped
     * subset. {@code minecraft:} entries are skipped — they are always in the raw source's own
     * {@code possibleBiomes()} already; absent optional mods simply fail the registry lookup and
     * are skipped.
     */
    public static List<Holder<Biome>> allPaintableCustomBiomes(Registry<Biome> biomeRegistry) {
        Map<ResourceLocation, Holder<Biome>> out = new LinkedHashMap<>();
        for (String tagPath : DECORATION_POLICY_TAG_PATHS) {
            TagKey<Biome> tag = TagKey.create(Registries.BIOME, new ResourceLocation("globe", tagPath));
            for (Holder<Biome> holder : biomeRegistry.getTagOrEmpty(tag)) {
                holder.unwrapKey().ifPresent(key -> {
                    ResourceLocation id = key.location();
                    if (!"minecraft".equals(id.getNamespace())) {
                        out.putIfAbsent(id, holder);
                    }
                });
            }
        }
        for (BiomeDescriptorLedger.Descriptor descriptor : BiomeDescriptorLedger.descriptors()) {
            ResourceLocation id = ResourceLocation.tryParse(descriptor.biomeId());
            if (id == null || "minecraft".equals(id.getNamespace()) || out.containsKey(id)) {
                continue;
            }
            biomeRegistry.getHolder(ResourceKey.create(Registries.BIOME, id))
                    .ifPresent(holder -> out.putIfAbsent(id, holder));
        }
        return List.copyOf(out.values());
    }

    private static List<ResourceLocation> eligibleBiomeIds(ServerLevel world) {
        List<ResourceLocation> cached = eligibleCache;
        if (cached != null) {
            return cached;
        }
        Registry<Biome> registry = world.registryAccess().registryOrThrow(Registries.BIOME);
        Set<ResourceLocation> tagged = new HashSet<>();
        for (String tagPath : DECORATION_POLICY_TAG_PATHS) {
            TagKey<Biome> tag = TagKey.create(Registries.BIOME, new ResourceLocation("globe", tagPath));
            for (Holder<Biome> holder : registry.getTagOrEmpty(tag)) {
                holder.unwrapKey().ifPresent(key -> tagged.add(key.location()));
            }
        }
        List<ResourceLocation> out = new ArrayList<>();
        for (BiomeDescriptorLedger.Descriptor descriptor : BiomeDescriptorLedger.descriptors()) {
            ResourceLocation id = ResourceLocation.tryParse(descriptor.biomeId());
            if (id == null || "minecraft".equals(id.getNamespace()) || tagged.contains(id)) {
                continue;
            }
            if (registry.getHolder(ResourceKey.create(Registries.BIOME, id)).isPresent()) {
                out.add(id);
            }
        }
        cached = List.copyOf(out);
        eligibleCache = cached;
        return cached;
    }

    private static String eligibleSummary(ServerLevel world) {
        List<ResourceLocation> ids = eligibleBiomeIds(world);
        if (ids.isEmpty()) {
            return "none present (no eligible provider mods installed)";
        }
        return ids.size() + " (" + ids.stream().map(ResourceLocation::toString)
                .limit(6).reduce((a, b) -> a + ", " + b).orElse("") + (ids.size() > 6 ? ", …" : "") + ")";
    }
}
