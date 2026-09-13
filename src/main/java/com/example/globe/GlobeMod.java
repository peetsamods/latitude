package com.example.globe;

import net.fabricmc.api.ModInitializer;
import com.example.globe.world.LatitudeBiomes;
import com.example.globe.world.LatitudeBiomeSource;
import com.example.globe.world.LatitudeWorldState;
import com.example.globe.core.PassageAxis;
import com.example.globe.world.BiomeSelectionProfile;
import com.example.globe.world.VanillaBiomeRepresentationProfile;
import com.example.globe.world.CaveBiomeRepresentationProfile;
import com.example.globe.world.SpawnSafetyPolicy;
import com.example.globe.world.WorldgenGeneratorAuthorityPolicy;
import com.example.globe.util.BiomeSamplerTools;
import com.example.globe.util.BiomeSamplerTools.SamplerTemplate;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.lang.reflect.Method;

import java.io.InputStream;

public class GlobeMod implements ModInitializer {
    public static final String MOD_ID = "globe";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String SPAWN_CHOSEN_TAG = "globe_spawn_chosen";
    // Duplicates vanilla's own join logging once per player join; opt-in only (maintainer ruling, 2026-08-18).
    private static final boolean DEBUG_JOIN = Boolean.getBoolean("latitude.debugJoin");

    public static final int BORDER_RADIUS = 7500;
    public static final int POLE_BAND_START_ABS_Z = 12000;
    private static int activePoleBandStartAbsZ = POLE_BAND_START_ABS_Z;
    public static final int POLE_WARNING_DISTANCE_BLOCKS = 256;
    public static final int POLE_LETHAL_DISTANCE_BLOCKS = 96;
    public static final int POLE_LETHAL_WARNING_DISTANCE = POLE_WARNING_DISTANCE_BLOCKS;
    public static final int EFFECT_REFRESH_TICKS = 20;
    private static final int EW_WARNING_DISTANCE_BLOCKS = 500;
    private static final int EW_SPAWN_PADDING_BLOCKS = 64;
    // The E/W spawn safety margin is a FRACTION of the X radius: the sandstorm onset scales with world
    // width, so a fixed block margin fails on every size (it left spawn AT the onset on a Tiny world and
    // deep inside the storm on a large one).
    private static final double EW_SPAWN_SAFE_MARGIN_FRAC = 0.08;
    private static final long SPAWN_SALT = 0x7A3E21B5D4C1F7A9L;
    // B-5/B-7 Hemisphere Passage: players who answered a prompt "turn back" and are owed a ONE-SHOT nudge back
    // toward center, applied (and cleared) on the next server tick in {@link #borderUxTick}. B-7 (A3): axis-keyed
    // -- the value is which edge the decline was for (EW pushes back along X toward center; POLE pushes
    // equatorward along Z). Thread-confined to the server thread: the C2S receiver schedules onto it via
    // {@code context.server().execute(...)}, and END_SERVER_TICK runs on it too, so a plain HashMap needs no
    // synchronization.
    private static final java.util.Map<java.util.UUID, PassageAxis> PENDING_PASSAGE_TURN_BACK = new java.util.HashMap<>();
    // B-5 turn-back nudge strength (blocks/tick added toward center). Gentle single impulse, then the player
    // is free -- a per-tick push would fight input (the vetoed "hard yank"). Live-tunable feel is P3's call.
    private static final double PASSAGE_TURN_BACK_IMPULSE = 0.45;

    // B-7 S2: the Wide-world pole hard-stop clamp bookkeeping. POLE_CLAMP_LAST_CONTACT_TICK rate-limits the
    // "presentable contact" chime/action-bar (~2s, tick-counter, never wall-clock); POLE_CLAMP_LOGGED gates the
    // once-per-session server log. Both thread-confined to the server thread and cleared on disconnect. (The
    // clamp MATH -- engagement epsilon, clamped Z, outward-velocity kill -- is the pure core PoleHardStop.)
    private static final java.util.Map<java.util.UUID, Long> POLE_CLAMP_LAST_CONTACT_TICK = new java.util.HashMap<>();
    private static final java.util.Set<java.util.UUID> POLE_CLAMP_LOGGED = new java.util.HashSet<>();
    // B-7 S1 cold-protection: the four armor slots consulted for freeze-immune-wearable pieces (leather by
    // default, datapack-extensible). A full set of freeze-immune pieces here negates freeze DAMAGE.
    private static final EquipmentSlot[] COLD_ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    // B-7 S5(c): post-pole-crossing cold-grace end tick per player (PoleCrossingGrace.graceUntil), stamped on a
    // successful pole crossing so a low-health crosser cannot die inside the arrival-curtain ceremony. Read in
    // borderUxTick; suppresses BOTH cold-damage bands + the F3 frost cue while active; NOT wired into the S6
    // heal lock. Thread-confined to the server thread; cleared on disconnect; stale entries lazily dropped.
    private static final java.util.Map<java.util.UUID, Long> POLE_CROSS_COLD_GRACE_UNTIL = new java.util.HashMap<>();
    // B-7 S9 (arrival pre-warm, TEST 99 freeze mitigation): the per-player ticketed antipodal anchor -- the
    // target block column the current PORTAL radius ticket is centered on + the last refresh tick. ONE entry
    // per player, present ONLY while they are inside the pole prompt band (worst case: N players camped at the
    // prompt line = N 5x5-chunk tickets -- see PoleArrivalPrewarm's budget javadoc). Thread-confined to the
    // server thread; dropped on band-exit/crossing/disconnect; the PORTAL ticket's own 300-tick timeout is the
    // cleanup backstop if a drop is ever missed.
    private static final java.util.Map<java.util.UUID, PolePrewarmAnchor> POLE_PREWARM = new java.util.HashMap<>();

    /** S9: one player's active pre-warm ticket anchor (target block column) + last ticket-refresh tick. */
    private record PolePrewarmAnchor(int targetX, int targetZ, long lastRefreshTick) {
    }
    // B-7 S6: cached warmth-scan verdicts (PolarWarmth 9x5x9 box scan is 405 block reads -- cached per player
    // on a WARMTH_RESCAN_TICKS cadence so heal-time checks and the per-tick cue share one scan). Value = the
    // game-time tick the verdict was computed at + the verdict. Thread-confined; cleared on disconnect.
    private static final java.util.Map<java.util.UUID, long[]> POLE_WARMTH_CACHE = new java.util.HashMap<>();
    private static final long WARMTH_RESCAN_TICKS = 20L;
    // The S6 warmth-scan box half-extents: a ~4-block box around the player (9 x 5 x 9).
    private static final int WARMTH_SCAN_RADIUS_XZ = 4;
    private static final int WARMTH_SCAN_RADIUS_Y = 2;
    // P1 sweep HIGH-2: server-side per-player cross cooldown (idempotency guard). Two cross=true payloads
    // arriving in the same tick would otherwise BOTH pass validation -- the mirror preserves border distance,
    // so after the first teleport the second re-validates true and bounces the player straight back (double
    // ring-load, double S2C). The server cannot see client arm state, so this guard must live here: last
    // SUCCESSFUL cross game-time per UUID; a cross within the window is rejected+logged. 60 ticks (3s):
    // comfortably longer than any duplicate-send/lag burst, and irrelevant to a legitimate re-cross (the
    // client re-arm needs a 564-block walk-out, minutes not seconds). Thread-confined to the server thread
    // like PENDING_PASSAGE_TURN_BACK; entries cleared on disconnect.
    private static final java.util.Map<java.util.UUID, Long> PASSAGE_LAST_CROSS_TICK = new java.util.HashMap<>();
    private static final long PASSAGE_CROSS_COOLDOWN_TICKS = 60L;
    private static final Set<String> PROVIDER_PROFILE_WARNINGS = ConcurrentHashMap.newKeySet();
    private static volatile boolean pendingInitialBonusChest;

    public static final int POLE_START = 12000; // Legacy constant, use activePoleBandStartAbsZ for dynamic logic

    private enum PolarStage {
        NONE,
        UNEASE,
        IMPAIR,
        HOSTILE,
        WHITEOUT,
        LETHAL,
        HOPELESS
    }

    private static final Identifier GLOBE_SETTINGS_ID = Identifier.fromNamespaceAndPath(MOD_ID, "overworld");
    private static final Identifier GLOBE_SETTINGS_XSMALL_ID = Identifier.fromNamespaceAndPath(MOD_ID, "overworld_xsmall");
    private static final Identifier GLOBE_SETTINGS_SMALL_ID = Identifier.fromNamespaceAndPath(MOD_ID, "overworld_small");
    private static final Identifier GLOBE_SETTINGS_REGULAR_ID = Identifier.fromNamespaceAndPath(MOD_ID, "overworld_regular");

    private static final Identifier GLOBE_SETTINGS_LARGE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "overworld_large");
    private static final Identifier GLOBE_SETTINGS_MASSIVE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "overworld_massive");

    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_KEY = ResourceKey.create(net.minecraft.core.registries.Registries.NOISE_SETTINGS, GLOBE_SETTINGS_ID);
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_XSMALL_KEY = ResourceKey.create(net.minecraft.core.registries.Registries.NOISE_SETTINGS, GLOBE_SETTINGS_XSMALL_ID);
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_SMALL_KEY = ResourceKey.create(net.minecraft.core.registries.Registries.NOISE_SETTINGS, GLOBE_SETTINGS_SMALL_ID);
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_REGULAR_KEY = ResourceKey.create(net.minecraft.core.registries.Registries.NOISE_SETTINGS, GLOBE_SETTINGS_REGULAR_ID);

    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_LARGE_KEY = ResourceKey.create(net.minecraft.core.registries.Registries.NOISE_SETTINGS, GLOBE_SETTINGS_LARGE_ID);
    private static final ResourceKey<NoiseGeneratorSettings> GLOBE_SETTINGS_MASSIVE_KEY = ResourceKey.create(net.minecraft.core.registries.Registries.NOISE_SETTINGS, GLOBE_SETTINGS_MASSIVE_ID);

    /** Exact overworld generator for the currently loaded Latitude server. */
    private static volatile NoiseBasedChunkGenerator activeLatitudeOverworldGenerator;

    @Override
    public void onInitialize() {
        LOGGER.info("{} initialized. Use the globe:globe world preset for deterministic terrain.", MOD_ID);

        logBuildMetadata("server");

        com.example.globe.compat.LatitudeTerraBlenderBridge.install();

        // Phase 5 S25b: register the custom worldgen Feature type (globe:powder_crevasse_roof, the
        // powder-snow roofed crevasse trap) UNCONDITIONALLY, before registry freeze -- a datapack feature
        // referencing an unregistered type would hard-fail at load. All BEHAVIOUR is flag-gated inside it.
        com.example.globe.world.PowderCrevasseRoofFeature.register();
        // S37: globe:icicle (vanilla speleothem rooted on packed ice) + its item. Unconditional (registry
        // consistency law); generation is biome-JSON-scoped to glacial caves.
        com.example.globe.world.IcicleBlocks.register();
        // Phase 5 S27: the custom particle type (globe:frost_glint), registered unconditionally before
        // registry freeze; the client render factory lives in GlobeModClient and spawning is flag-gated.
        com.example.globe.content.GlobeParticles.register();

        // Phase 5 B-10 (Polar Outfitting): register the mod's first Java content objects
        // (globe:polar_hood + globe:cold_protection) UNCONDITIONALLY, before registry freeze -- registries
        // must be consistent across sessions; all BEHAVIOUR is flag-gated (POLAR_OUTFITTING_ENABLED).
        com.example.globe.content.PolarOutfitting.register();

        // Placement modifier behind Latitude's lush desert riverbanks. Registered unconditionally
        // so the placed-feature JSON still parses when the banks themselves are switched off.
        com.example.globe.world.feature.LatitudeRiparianBanks.registerPlacementType();

        GlobeNet.registerPayloads();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Shipping operator commands: unconditional and directly linked. Unlike the dev tree,
            // com.example.globe.tools is packaged into release artifacts, so a missing class is a
            // build error worth surfacing rather than something to swallow reflectively.
            com.example.globe.tools.LatitudeToolsCommand.register(dispatcher);
            registerDevOnlyCommand(dispatcher);
        });

        // Initialize province authority at world-load time, before spawn-chunk generation fires
        // for brand-new worlds. SERVER_STARTED fires too late (after spawn chunks are pregenerated).
        ServerLevelEvents.LOAD.register(GlobeMod::initLatitudeBiomesForWorld);
        ServerLifecycleEvents.SERVER_STARTED.register(GlobeMod::applyWorldBorder);
        registerDevOnlyHeadlessRunner();
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            activeLatitudeOverworldGenerator = null;
            pendingInitialBonusChest = false;
            LatitudeBiomes.clearWorldgenContext();
            // Slice B (audit P1-1): tear down the V2 worldgen statics so a subsequent world in this JVM can
            // never inherit this world's GeoAuthority/ClimateAuthority. The seed-0/zero-radius decline path
            // in rebuild* historically never overwrote them.
            LatitudeBiomes.resetWorldgenStateForServerStop();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerLevel overworld = server.overworld();
            if (overworld == null) {
                return;
            }

            boolean isGlobe = isGlobeOverworld(overworld);
            if (DEBUG_JOIN) {
                LOGGER.info("JOIN: player={}, isGlobeOverworld={}", handler.player.getName().getString(), isGlobe);
            }
            LatitudeWorldState worldState = isGlobe ? LatitudeWorldState.get(overworld) : null;
            if (worldState != null) {
                // A fresh integrated world can cross the client disconnect boundary before its
                // first regular tick. Snapshot the actual spawn band now so both the initial
                // loading overlay and the next world-list row have authoritative saved data.
                recordLastKnownBand(overworld, overworld.getWorldBorder(), handler.player);
            }
            String loadingBandId = worldState == null
                    ? ""
                    : worldState.getLastKnownBandId().orElse("");
            int latitudeZRadius = isGlobe ? LatitudeBiomes.getActiveRadiusBlocks() : 0;
            int intendedXRadius = isGlobe ? LatitudeBiomes.getActiveXRadiusBlocks() : 0;
            ServerPlayNetworking.send(handler.player,
                    new GlobeNet.GlobeStatePayload(isGlobe, latitudeZRadius, intendedXRadius, loadingBandId));
            boolean isBrandNewWorld = overworld.getGameTime() < 100L;
            boolean spawnAlreadyChosen = handler.player.entityTags().contains(SPAWN_CHOSEN_TAG);

            if (isGlobe && pendingInitialBonusChest) {
                // The biome-correct initial coordinate is only a suggestion. Minecraft 26.2's
                // PrepareSpawnTask resolves and loads the actual safe player position before JOIN,
                // so placing the optional chest here avoids a synchronous remote chunk request.
                pendingInitialBonusChest = false;
                placeLatitudeBonusChest(overworld, handler.player.blockPosition());
            }

            if (!server.isDedicatedServer()) {
                GlobePending.consume();
            }

            boolean startWithCompass = !server.isDedicatedServer() && GlobePending.startWithCompass;
            if (isGlobe && !server.isDedicatedServer() && !StartCompass.hasReceived(handler.player)) {
                if (!startWithCompass) {
                    StartCompass.markReceived(handler.player);
                } else if (hasCompassAnywhere(handler.player)) {
                    StartCompass.markReceived(handler.player);
                } else {
                    boolean given = handler.player.addItem(new ItemStack(Items.COMPASS));
                    if (given) {
                        StartCompass.markReceived(handler.player);
                    }
                }
            }

            if (isGlobe && !spawnAlreadyChosen && !worldState.isSpawnPickerDismissed() && isBrandNewWorld) {
                // Initial creation either set a terrain-validated Latitude spawn or intentionally
                // delegated to vanilla's safe-spawn routine. Never repeat a synchronous globe scan
                // on the first player join: that can strand the client on the loading overlay.
                LOGGER.info("[Latitude] Suppressing legacy post-load spawn relocation; retaining the initial safe spawn");
                worldState.setSpawnPickerDismissed(true);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(GlobeNet.SetSpawnPickerPayload.ID, (payload, context) -> {
            context.server().execute(() -> applySpawnChoice(context.player(), payload.zoneId()));
        });

        // B-5/B-7 Hemisphere Passage: the client's answer to a crossing prompt. Never trusted -- the handler
        // re-derives the authoritative distance to that edge on the server thread.
        ServerPlayNetworking.registerGlobalReceiver(GlobeNet.PassageAnswerPayload.ID, (payload, context) -> {
            context.server().execute(() -> handlePassageAnswer(context.player(), payload.cross(), payload.axis()));
        });

        ServerTickEvents.END_SERVER_TICK.register(GlobeMod::borderUxTick);

        // Authoritative last-known-band capture at quit time, closing the gap the periodic tick in
        // borderUxTick leaves if a player quits within moments of crossing a band boundary.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            ServerLevel overworld = server.overworld();
            if (player == null || overworld == null || player.level() != overworld || !isGlobeOverworld(overworld)) {
                return;
            }
            recordLastKnownBand(overworld, overworld.getWorldBorder(), player);
        });

        // B-5/B-7: drop any pending turn-back nudge, cross-cooldown, pole-clamp, cold-grace, warmth-cache and
        // S9 pre-warm bookkeeping for a leaver (the pre-warm drop also releases the chunk ticket).
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            java.util.UUID uuid = handler.player.getUUID();
            PENDING_PASSAGE_TURN_BACK.remove(uuid);
            PASSAGE_LAST_CROSS_TICK.remove(uuid);
            POLE_CLAMP_LAST_CONTACT_TICK.remove(uuid);
            POLE_CLAMP_LOGGED.remove(uuid);
            POLE_CROSS_COLD_GRACE_UNTIL.remove(uuid);
            POLE_WARMTH_CACHE.remove(uuid);
            ServerLevel overworld = server.overworld();
            if (overworld != null) {
                dropPolePrewarm(overworld, uuid);
            }
        });
    }

    private static void registerDevOnlyCommand(Object dispatcher) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        invokeDevRegister("com.example.globe.dev.LatitudeDevCommand", dispatcher);
        invokeDevRegister("com.example.globe.dev.LatitudeDevCommands", dispatcher);
    }

    private static volatile Method devTickGreenMarkersMethod;
    private static volatile boolean devTickGreenMarkersMissing;

    /**
     * The 2.0 dev tree's green-marker tick lives in the release-excluded {@code dev} package; resolve it
     * reflectively so a public artifact (which omits {@code dev/}) never references the class.
     */
    private static void devTickGreenMarkers(ServerLevel overworld, long worldTime) {
        if (devTickGreenMarkersMissing) {
            return;
        }
        try {
            Method method = devTickGreenMarkersMethod;
            if (method == null) {
                method = Class.forName("com.example.globe.dev.LatitudeDevCommands")
                        .getMethod("tickGreenMarkers", ServerLevel.class, long.class);
                devTickGreenMarkersMethod = method;
            }
            method.invoke(null, overworld, worldTime);
        } catch (ClassNotFoundException | NoSuchMethodException absent) {
            devTickGreenMarkersMissing = true;
        } catch (ReflectiveOperationException | RuntimeException failure) {
            devTickGreenMarkersMissing = true;
            LOGGER.warn("[Latitude] dev green-marker tick disabled after a failure: {}", failure.toString());
        }
    }

    private static void registerDevOnlyHeadlessRunner() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        invokeDevRegister("com.example.globe.dev.BiomePreviewHeadlessRunner");
    }

    private static void invokeDevRegister(String className, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            Method registerMethod = null;
            for (Method method : clazz.getMethods()) {
                if (!method.getName().equals("register") || method.getParameterCount() != args.length) {
                    continue;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                boolean matches = true;
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (args[i] != null && !parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    registerMethod = method;
                    break;
                }
            }
            if (registerMethod == null) {
                throw new NoSuchMethodException("No compatible register method found");
            }
            registerMethod.invoke(null, args);
        } catch (ClassNotFoundException e) {
            LOGGER.debug("[latdev] Skipping missing dev class {}", className);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("[latdev] Failed to invoke {}.register", className, e);
        }
    }

    /**
     * Fires at world-load time — before Minecraft pre-generates spawn chunks for new worlds.
     * Seeds {@link LatitudeBiomes} with the world seed and radius so that province authority
     * is non-null when the first worldgen call arrives.
     *
     * <p>Only acts on the Globe overworld; other dimensions are ignored.
     */
    private static void initLatitudeBiomesForWorld(MinecraftServer server, ServerLevel world) {
        if (world != server.overworld()) {
            return;
        }

        int pendingRadius = GlobePending.pendingGlobeRadius;
        GlobePending.pendingGlobeRadius = 0;

        // The create screen's own launch is the primary authority on whether this is a Latitude
        // world, exactly as on the shipped 26.1x line: record the pending radius BEFORE consulting
        // the generator. isGlobeOverworld() then recognises the world through the persisted state.
        // The generator-holder check must NOT gate this: lithostitched legitimately swaps a patched
        // Direct settings holder into the live generator to inject dependent mods' worldgen
        // (CliffTree on 26.1.x), which breaks holder-key recognition while leaving the terrain
        // exactly ours. The launcher already verified the baked generator was a bound globe:
        // reference before launch, so the only thing still worth failing on is gross substitution —
        // a generator that is not even a NoiseBasedChunkGenerator.
        if (pendingRadius > 0 && world.getGameTime() < 100L
                && !(world.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator)) {
            throw new IllegalStateException(
                    "Latitude created this world, but the loaded overworld generator was replaced "
                    + "by a non-noise generator (" 
                    + world.getChunkSource().getGenerator().getClass().getName()
                    + "). Another world generation mod took over the overworld during world "
                    + "creation; the world would save as vanilla terrain and Latitude would stay "
                    + "disabled in it. Remove the conflicting world generation mod and create the "
                    + "world again. This world save is not a Latitude world and can be deleted.");
        }
        LatitudeWorldState earlyState = pendingRadius > 0 && world.getGameTime() < 100L
                ? LatitudeWorldState.get(world)
                : LatitudeWorldState.getIfPresent(world);
        if (earlyState != null && earlyState.getGlobeRadius() <= 0
                && pendingRadius > 0 && world.getGameTime() < 100L) {
            earlyState.setGlobeRadius(pendingRadius);
            LOGGER.info("[Latitude] Recorded Globe world radius {} ahead of generator recognition "
                    + "(create-screen authority)", pendingRadius);
        }

        if (!isGlobeOverworld(world)) {
            activeLatitudeOverworldGenerator = null;
            LatitudeBiomes.clearWorldgenContext();
            return;
        }
        LatitudeWorldState worldState = LatitudeWorldState.get(world);
        long seed = server.getWorldGenSettings().options().seed();
        net.minecraft.core.Registry<Biome> biomeRegistry =
                world.registryAccess().lookupOrThrow(Registries.BIOME);
        java.util.List<String> activeBiomeIds = biomeRegistry.keySet().stream()
                .map(Identifier::toString).toList();
        // Provider-ticket profile capture. pendingRadius > 0 was the only trigger here, which made
        // the capture a create-screen-only event: a dedicated server (or a vanilla create flow that
        // selects the globe preset without Latitude's screen) never has a pending radius, so its
        // worlds ran profile-less forever — MODERN_1_3 policy instead of V4, and every
        // ledger-routed provider biome silently absent (proven: a 390k-sample atlas placed all 26
        // tagged BoP biomes and zero of the 14 ledger-only ones). The capture now fires for ANY
        // fresh globe overworld inside the creation window, using the create screen's radius when
        // it exists and the settings-derived radius otherwise.
        //
        // The creation window (gameTime < 100) is what keeps existing worlds untouched: a world
        // created before this fix has lived past the window, stays profile-less, and keeps its
        // frozen legacy placement — capturing later would make new chunks disagree with old ones.
        //
        // -Dlatitude.providerProfileCapture.disable=true suppresses only the NEW non-create-screen
        // path. It exists for cross-version atlas parity: a pre-fix reference build can never
        // capture headlessly, so a like-for-like diff against one needs this build to abstain too.
        boolean creationWindow = world.getGameTime() < 100L;
        boolean clientCreated = pendingRadius > 0;
        boolean dedicatedCaptureDisabled = Boolean.getBoolean("latitude.providerProfileCapture.disable");
        if (worldState.getProviderTicketProfile().isEmpty() && creationWindow
                && (clientCreated || !dedicatedCaptureDisabled)) {
            int captureRadius = clientCreated ? pendingRadius : borderRadiusForGlobeOverworld(world);
            BiomeSelectionProfile profile = BiomeSelectionProfile.capture(activeBiomeIds);
            worldState.setProviderTicketProfile(profile);
            worldState.setVanillaRepresentationProfile(
                    VanillaBiomeRepresentationProfile.capture(captureRadius, seed, profile));
            worldState.setCaveRepresentationProfile(
                    CaveBiomeRepresentationProfile.capture(captureRadius, profile));
            worldState.setWorldgenPolicy(
                    LatitudeWorldState.WorldgenPolicyVersion.PROVIDER_TICKET_V4_CAVE_COVERAGE);
            worldState.markContentRosterCurrent();
            if (!clientCreated && worldState.getGlobeRadius() <= 0) {
                // Same state-based recognition resilience 68716f22 gave client-created worlds:
                // captureRadius is borderRadiusForGlobeOverworld's own settings-derived answer
                // (state was empty), so persisting it is a fixpoint — and after this first load,
                // recognition no longer depends on the generator's settings holder surviving
                // in-place mutation by provider mods.
                worldState.setGlobeRadius(captureRadius);
            }
            LOGGER.info("[Latitude] Recorded Globe world: border radius {} ({})", captureRadius,
                    clientCreated ? "from create-world selection" : "fresh dedicated/vanilla-created world");
        }
        if (worldState.tryUpgradeContentRoster(activeBiomeIds)) {
            LOGGER.info("[Latitude] Added current Minecraft biome content to new, unexplored terrain in this existing world.");
        }
        BiomeSelectionProfile runtimeProviderProfile =
                worldState.getRuntimeProviderTicketProfile(activeBiomeIds).orElse(null);
        ChunkGenerator generator = world.getChunkSource().getGenerator();
        activeLatitudeOverworldGenerator = generator instanceof NoiseBasedChunkGenerator noise
                ? noise
                : null;
        int radius = borderRadiusForGlobeOverworld(world);
        warnForProviderProfileDrift(world, worldState);
        RandomState randomState = world.getChunkSource().randomState();
        Climate.Sampler sampler = randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        LatitudeBiomes.activateWorldgenContext(radius, seed, worldState.getWorldgenPolicy(),
                runtimeProviderProfile,
                worldState.getVanillaRepresentationProfile().orElse(null),
                worldState.getCaveRepresentationProfile().orElse(null),
                sampler,
                donorBiomeSource(generator),
                generator.getSeaLevel(),
                biomeRegistry,
                randomState,
                generator instanceof NoiseBasedChunkGenerator terrainGenerator ? terrainGenerator : null,
                world);
        LOGGER.info("[Latitude] Early init: province authority seeded before spawn-chunk generation (seed={} radius={})", seed, radius);
        setGlobeBorder(world, radius);
    }

    private static void applyWorldBorder(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return;
        }

        if (!isGlobeOverworld(overworld)) {
            return;
        }

        ChunkGenerator generator = overworld.getChunkSource().getGenerator();
        activeLatitudeOverworldGenerator = generator instanceof NoiseBasedChunkGenerator noise
                ? noise
                : null;

        LatitudeWorldState worldState = LatitudeWorldState.get(overworld);

        int borderRadiusBlocks = borderRadiusForGlobeOverworld(overworld);
        long seed = overworld.getServer().getWorldGenSettings().options().seed();
        net.minecraft.core.Registry<Biome> biomeRegistry =
                overworld.registryAccess().lookupOrThrow(Registries.BIOME);
        java.util.List<String> activeBiomeIds = biomeRegistry.keySet().stream()
                .map(Identifier::toString).toList();
        if (worldState.tryUpgradeContentRoster(activeBiomeIds)) {
            LOGGER.info("[Latitude] Added current Minecraft biome content to new, unexplored terrain in this existing world.");
        }
        BiomeSelectionProfile runtimeProviderProfile =
                worldState.getRuntimeProviderTicketProfile(activeBiomeIds).orElse(null);
        warnForProviderProfileDrift(overworld, worldState);
        RandomState randomState = overworld.getChunkSource().randomState();
        Climate.Sampler sampler = randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        LatitudeBiomes.activateWorldgenContext(borderRadiusBlocks, seed, worldState.getWorldgenPolicy(),
                runtimeProviderProfile,
                worldState.getVanillaRepresentationProfile().orElse(null),
                worldState.getCaveRepresentationProfile().orElse(null),
                sampler,
                donorBiomeSource(generator),
                generator.getSeaLevel(),
                biomeRegistry,
                randomState,
                generator instanceof NoiseBasedChunkGenerator terrainGenerator ? terrainGenerator : null,
                overworld);

        setGlobeBorder(overworld, borderRadiusBlocks);
    }

    private static BiomeSource donorBiomeSource(ChunkGenerator generator) {
        BiomeSource source = generator.getBiomeSource();
        return source instanceof LatitudeBiomeSource latitude ? latitude.original() : source;
    }

    private static void warnForProviderProfileDrift(ServerLevel world, LatitudeWorldState state) {
        if (!LatitudeWorldState.isProviderTicketPolicy(state.getWorldgenPolicy())) return;
        Optional<BiomeSelectionProfile> profile = state.getProviderTicketProfile();
        if (profile.isEmpty()) {
            if (PROVIDER_PROFILE_WARNINGS.add("missing-profile:" + world.getServer().getWorldData().getLevelName())) {
                LOGGER.warn("[Latitude] Provider-ticket world has no valid birth profile; new terrain will use only non-provider fallback. Removing biome mods can still leave saved chunks unreadable.");
            }
            return;
        }
        java.util.List<String> activeIds = world.registryAccess().lookupOrThrow(Registries.BIOME).keySet().stream()
                .map(Identifier::toString).toList();
        java.util.List<String> missing = profile.get().missingIds(activeIds);
        if (!missing.isEmpty() && PROVIDER_PROFILE_WARNINGS.add(profile.get().encode() + missing)) {
            LOGGER.warn("[Latitude] Provider-ticket birth profile is missing {} locked biome IDs: {}. No new provider is substituted; removing biome mods can still leave saved chunks unreadable.", missing.size(), missing);
        }
    }

    private static void setGlobeBorder(ServerLevel overworld, int borderRadiusBlocks) {
        WorldBorder border = overworld.getWorldBorder();
        // borderRadiusBlocks is the Z (latitude) radius, e.g. 3750 / 5000 / 7500.
        int zRadius = borderRadiusBlocks;
        // Mercator worlds are 2:1: the playable X extent is ASPECT * the Z radius. Minecraft's WorldBorder
        // is square-only, so we size the square border to the WIDER (X) axis; the N/S poles stay interior
        // at |Z| = zRadius (enforced by the pole hazard band), and the E-W storm wall lands at the X edge.
        int xRadius = LatitudeBiomes.isMercator()
                ? (int) Math.round(zRadius * LatitudeBiomes.MERCATOR_ASPECT)
                : zRadius;
        double diameter = xRadius * 2.0;
        border.setCenter(0.0, 0.0);
        // DEFINITIVELY clear any persisted / in-flight lerp (TEST 86 finding). ServerLevel.getWorldBorder()
        // has already run ensureInitialized (so a level.dat BorderLerp* is installed as a MovingArea by now);
        // setSize replaces it with a StaticArea and broadcasts a size-change packet, snapping every client off
        // the lerp. The per-tick borderUxTick guard (enforceGlobeBorderSnapped) re-snaps if anything -- a
        // startup ordering race, or a mid-session /worldborder lerp -- moves it afterward. Belt + suspenders.
        border.setSize(diameter);
        if (border.getSize() != diameter) { // paranoia: a listener/extent that didn't take -- force once more.
            border.setSize(diameter);
        }

        // Latitude authority stays the Z radius (poles at |Z| = zRadius, NOT at the X border edge).
        LatitudeBiomes.setRadius(zRadius);
        LatitudeBiomes.setActiveRadiusBlocks(zRadius);
        // Tell the latitude math the Z radius so HUD/zone/pole calcs divide Z by zRadius (not the X border half).
        com.example.globe.util.LatitudeMath.setLatitudeZRadius(zRadius);
        // ...and the INTENDED X radius, so the server's E/W-edge feature geometry (passage cross re-validation)
        // reads the same anchor the client does -- never the live (possibly lerping) border half.
        com.example.globe.util.LatitudeMath.setIntendedXRadius(xRadius);
        LOGGER.info("[Latitude] Radius Sync: shape={} zRadius={} xRadius={} borderDiameter={} ACTIVE_RADIUS_BLOCKS={}",
                LatitudeBiomes.getGlobeShape(), zRadius, xRadius, diameter, LatitudeBiomes.getActiveRadiusBlocks());

        // Pole hazard band starts at a fraction of the Z radius (the geographic pole), interior to the border.
        activePoleBandStartAbsZ = (int) Math.round(zRadius * com.example.globe.util.LatitudeMath.POLAR_START_FRAC);

        GlobeMod.LOGGER.info("[Latitude] WorldBorder set: shape={} zRadius={} xRadius={} diameter={} center=0,0 polarStart={}",
                LatitudeBiomes.getGlobeShape(), zRadius, xRadius, diameter, activePoleBandStartAbsZ);
    }

    /**
     * Keep the globe world border snapped to the mod's intended diameter, killing any persisted or mid-session
     * lerp. Cheap: reads {@code getActiveXRadiusBlocks()} (a static) and only calls {@code setSize} on an
     * actual mismatch (> 1 block), so a healthy border is a no-op compare each tick. When it fires, {@code
     * setSize} installs a StaticArea and broadcasts the correction, so any active interpolation stops within a
     * tick on the server AND on every client. Falls back to the live border half if the intended X radius is
     * not yet known (never forces a bogus size).
     */
    private static void enforceGlobeBorderSnapped(WorldBorder border) {
        if (border == null) {
            return;
        }
        int xRadius = LatitudeBiomes.getActiveXRadiusBlocks();
        if (xRadius <= 0) {
            return; // radius not resolved yet -- do not force a size (leave vanilla behavior).
        }
        double diameter = xRadius * 2.0;
        if (Math.abs(border.getSize() - diameter) > 1.0) {
            LOGGER.info("[Latitude] Border drift detected (size={} intended={}); re-snapping (killing lerp)",
                    border.getSize(), diameter);
            border.setSize(diameter);
        }
    }
    private static void borderUxTick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return;
        }

        if (!isGlobeOverworld(overworld)) {
            return;
        }

        long worldTime = overworld.getGameTime();
        // B-4 round 3 item 1: the MobEffects (slowness/weakness/mining fatigue) stay on the cheap 10-tick
        // cadence (each is a 40-tick effect, so re-applying every 10 ticks is ample), but the FREEZE ticks
        // must be maintained EVERY server tick -- see below.
        boolean effectsTick = (worldTime % 10L) == 0L;

        WorldBorder border = overworld.getWorldBorder();
        // TEST 86 guard: keep the globe border SNAPPED to the intended diameter. Catches (a) a persisted
        // level.dat lerp that survived the startup snap for any ordering reason, and (b) a mid-session vanilla
        // /worldborder <size> <time> lerp -- both would otherwise slide the physical wall (and, pre-redesign,
        // every feature line). Only acts on an ACTUAL mismatch, so the common case is a cheap compare and no
        // packet; when it does fire, setSize snaps the server border and broadcasts the correction to clients.
        enforceGlobeBorderSnapped(border);

        // S32: /latdev markGlacial's lingering green markers (60 s re-emit; no-op when no scan has run).
        devTickGreenMarkers(overworld, worldTime);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() != overworld) {
                continue;
            }

            // Keep the persisted "last known band" fresh for every player, regardless of polar status, so
            // the loading screen and the world-selection list can show it the next time this save is opened.
            // Deliberately ahead of every hazard/edge continue: it is a plain bookkeeping write, not a
            // polar concern.
            if (effectsTick) {
                recordLastKnownBand(overworld, border, player);
            }

            // B-5/B-7: a pending "turn back" gets its ONE-SHOT nudge back toward center here (an EDGE concern,
            // not a latitude one, so it runs BEFORE the HAZARD_ONSET_DEG continue). The helper reads the axis,
            // gates on the matching feature flag, and skips creative/spectator.
            if (!PENDING_PASSAGE_TURN_BACK.isEmpty()) {
                applyPendingPassageTurnBack(player, border);
            }

            // B-7 S2: the Wide-world pole hard stop -- a server-side motion clamp at the |z| = zRadius pole line
            // (Classic worlds already wall it with the vanilla square border). Pure movement logic: it clamps
            // position/velocity back to the line and is NOT a teleport wrap and NOT worldgen. Flag-gated so
            // flag-off leaves the Wide pole the unmarked endless plain it is today. [P3 fix] Vanilla-border
            // parity: the wall stops creative too; ONLY spectator passes (PoleHardStop.exemptFromClamp).
            // S9: the arrival PRE-WARM ticket rides the same flag gate (Wide AND Classic -- unlike the clamp).
            if (com.example.globe.core.LatitudeV2Flags.POLE_PASSAGE_V2_ENABLED) {
                applyPoleHardStop(player, border, worldTime);
                tickPolePrewarm(player, overworld, border, worldTime);
            }

            double latDeg = com.example.globe.util.LatitudeMath.absLatDegExact(border, player.getZ());
            boolean unaffected = player.isCreative() || player.isSpectator();

            // S35: the S30 scripted snow-collapse event is RETIRED (owner verbatim: solid snow covers were
            // "the mistake"; and its falling blocks BACKFILLED the very shaft they revealed). The trap is now
            // pure vanilla powder physics -- the cover itself sinks the victim; nothing to tick here.

            // B-7 S7 (POLAR IMMERSION): the SINGLE cold-evaluation latitude. In water (isInWater -- false in a
            // boat, the free story-true exemption) the existing curves are evaluated at |lat|+3 capped at 90
            // ("polar water is three degrees colder than the air"), so the open 82-85 liquid sea bites like 85
            // land and under-ice swimming at 87+ reads like the lethal core. EVERY cold-band read below
            // (frostbite applies/interval/amount, the F3 cue, the 87.5 lethal gate, hazardProgress -> frost
            // visual + slowness staging + lethal damage) uses effLatDeg, so damage, cue and blue hearts shift
            // together (the honesty law); LivingEntityFreezeDamageMixin's gate (isInPolarFreezeDamageBand)
            // applies the SAME shift so vanilla's auto-freeze can never double-dip an immersed player.
            boolean inWater = !unaffected && player.isInWater();
            double effLatDeg = com.example.globe.core.PolarImmersion.effectiveLatDeg(latDeg, inWater);

            // B-7 S4/S5(c)/S6: the cold-pause + heal-lock inputs, computed once per tick for survival players
            // in the cold zone. The DAMAGE zone reads the S7 effective latitude (a swimmer at 82 is in the
            // band); the S6 heal-lock zone stays RAW latitude (S7 leaves the lock untouched -- immersion is not
            // shelter, and this local must mirror isPolarHealLocked exactly). S4: genuine shelter (raw sky
            // light <= 3 at the eye, ColdShelter) pauses cold DAMAGE -- unless IMMERSED (S7: water conducts
            // cold; walls do not help in the sea -- PolarImmersion.coldDamagePaused owns the rule). S5(c): the
            // post-crossing grace pauses it for the arrival-curtain window (grace > immersion). S6: sheltered
            // without a warmth source freezes WOUNDS (heal lock, enforced at the LivingEntity.heal chokepoint
            // by LivingEntityHealLockMixin via isPolarHealLocked) and HOLDS the F3 frost cue. Short-circuit
            // order: zone -> grace -> shelter -> warmth scan (the 405-block scan still runs only for sheltered
            // RAW-zone polar players, cached ~20t in POLE_WARMTH_CACHE -- S7 does not widen the S6 scan).
            boolean inColdZone = !unaffected
                    && effLatDeg >= com.example.globe.core.PolarHazardWindow.FROSTBITE_ONSET_DEG;
            boolean inRawColdZone = !unaffected
                    && latDeg >= com.example.globe.core.PolarHazardWindow.FROSTBITE_ONSET_DEG;
            boolean coldSheltered = false;
            boolean coldGrace = false;
            boolean healLocked = false;
            if (inColdZone) {
                coldGrace = isPoleCrossColdGraceActive(player, worldTime);
                coldSheltered = isColdSheltered(overworld, player);
                if (inRawColdZone) {
                    boolean nearWarmth = coldSheltered && isNearWarmthCached(overworld, player, worldTime);
                    healLocked = com.example.globe.core.PolarWounds.healLocked(true, coldSheltered, nearWarmth);
                }
            }
            boolean coldDamagePaused =
                    com.example.globe.core.PolarImmersion.coldDamagePaused(coldSheltered, inWater, coldGrace);

            // S37 F1: PONDED WATER CAUSES FREEZING (the maintainer: "the water that has ponded is not causing freezing
            // damage -- hearts are red"). DIRECT water-contact chill: raises ticksFrozen itself, exactly like
            // standing in vanilla powder snow, just faster (+3..+6/t vs vanilla's -2/t decay; PolarWaterChill).
            // Raw |lat| (NOT the S7-shifted effective latitude) is the input -- see the law's javadoc. Gated on
            // canFreeze() so any leather piece exempts (vanilla's own powder gate, javap-verified); isInWater()
            // is false in a boat and in mere rain. SWEEP REQUIRED-FIX 1 (S37): sits AFTER coldDamagePaused and
            // honors it -- during the sacred post-crossing grace curtain (or any pause) the chill must not
            // build toward vanilla's own auto-freeze damage below the 87.5 suppression band. NOTE (sweep 2):
            // an 82+ swimmer is ALREADY frostbite-band via the S7 +3 shift, so the chill's vanilla freeze
            // damage STACKS with S7 frostbite -- deliberate ("polar water is colder than polar air"), owner
            // confirms the harshness at flight.
            if (inWater && player.canFreeze() && !coldDamagePaused
                    && latDeg >= com.example.globe.core.PolarWaterChill.ONSET_LAT_DEG) {
                player.setTicksFrozen(com.example.globe.core.PolarWaterChill.nextTicksFrozen(
                        player.getTicksFrozen(), latDeg, player.getTicksRequiredToFreeze()));
            }

            // B-7 S3: the FROSTBITE band [85,88) -- gentle freeze damage equatorward of the lethal core, handing
            // off EXACTLY at 88 deg to the untouched [88,90] curve (both bands evaluated at the S7 effective
            // latitude). ColdProtection scales the amount at the SAME single computed-amount point as the
            // lethal core, so a full freeze-immune set negates it (the leather drysuit, S7); a zeroed amount
            // skips the hurt call. S4/S5(c)/S7: paused per PolarImmersion.coldDamagePaused.
            boolean frostbiteBiting = inColdZone && !coldDamagePaused
                    && com.example.globe.core.PolarHazardWindow.appliesFrostbiteDamage(effLatDeg);
            if (frostbiteBiting) {
                int fInterval = com.example.globe.core.PolarHazardWindow.frostbiteIntervalTicks(effLatDeg);
                if (worldTime % (long) fInterval == 0L) {
                    float fAmount = com.example.globe.core.PolarHazardWindow.frostbiteDamageAmount(effLatDeg)
                            * (float) com.example.globe.core.ColdProtection.damageMultiplier(coldProtectionPieceCount(player));
                    if (fAmount > 0.0f) {
                        player.hurtServer(overworld, overworld.damageSources().freeze(), fAmount);
                    }
                }
            }

            // B-7 F3 (+S6 amendment): the frostbite FROST-CUE floor -- no silent damage. Active iff frostbite is
            // actually biting this tick OR the S6 heal-lock holds (frozen wounds LOOK frozen); paused with the
            // damage otherwise (PolarWounds.frostCueActive is the single rule). Reads the SAME S7 effective
            // latitude as the bite, so cue and damage cannot drift. FLOOR semantics: only ever RAISES
            // ticksFrozen (vanilla powder-snow freezing and the lethal path's own frost visual are never
            // decreased). Below the (effective) 87.5 hazard onset this is the only writer; in [87.5,88) the
            // lethal frost-visual writer below composites via max().
            int frostCueFloor = com.example.globe.core.PolarWounds.frostCueActive(frostbiteBiting, healLocked)
                    ? com.example.globe.core.PolarHazardWindow.frostbiteFrostCueTicks(effLatDeg)
                    : 0;
            if (frostCueFloor > 0
                    && effLatDeg < com.example.globe.core.PolarHazardWindow.HAZARD_ONSET_DEG
                    && player.getTicksFrozen() < frostCueFloor) {
                player.setTicksFrozen(frostCueFloor);
            }

            // B-3a: the LETHAL polar hazard core [87.5,90], evaluated at the S7 effective latitude (an immersed
            // 85-deg swimmer reads 88 -- the lethal onset; a dry 85-deg walker is untouched). The curve itself
            // is UNCHANGED by B-7 (frost visual from 87.5, freeze DAMAGE from ~88 intensifying to a lethal
            // pole, slowness/weakness/mining fatigue); the B-7 prompt-zone survival numbers depend on that.
            if (effLatDeg < com.example.globe.core.PolarHazardWindow.HAZARD_ONSET_DEG) {
                continue;
            }
            double progress = com.example.globe.core.PolarHazardWindow.hazardProgress(effLatDeg);

            // FROST VISUAL every tick. frostVisualTicks builds 0 -> 140 across 87.5 -> ~88 and holds at/above
            // 140 to the pole, so it CROSSES vanilla's fully-frozen threshold at the ~88 deg damage onset --
            // the HUD hearts tint blue exactly when freeze damage starts (TEST 77), driven off this set value.
            // Vanilla's OWN fixed 1 HP/40-tick freeze auto-damage, which also keys off 140, is cancelled at its
            // aiStep source for in-band players by LivingEntityFreezeDamageMixin, so the scaled damage below
            // stays the SOLE freeze-damage source (no double dip). Set EVERY tick because vanilla decays
            // ticksFrozen ~2/tick when the player isn't in powder snow; this END_SERVER_TICK write is the last
            // writer each tick (the entity-tracker broadcast reads it before aiStep's decay), so the client
            // reliably sees our value and the blue hearts don't flicker.
            if (!unaffected) {
                // F3 composite: never set BELOW the active frostbite cue floor (relevant only in [87.5,88),
                // where the lethal ramp is still climbing 0->140 and the cue floor is at ~117-140; max() keeps
                // the frost monotone across the hand-off instead of popping down at 87.5).
                player.setTicksFrozen(Math.max(
                        com.example.globe.core.PolarHazardWindow.frostVisualTicks(progress), frostCueFloor));
            }

            // Scaled freeze DAMAGE: begins ~88 deg and gets worse toward the pole. The interval shrinks
            // (60 -> 10 ticks) and the amount grows (1 -> 3 HP) with latitude, applied on the server's own
            // cadence (worldTime % interval) -- stateless, no per-player accumulator. Same freeze damage
            // type/death as vanilla; only the timing is ours.
            // S4/S5(c): the lethal core pauses while genuinely sheltered or inside the post-crossing grace
            // window -- same pause law as the frostbite band (one rule, one story: walls stop the bleeding).
            if (!unaffected && !coldDamagePaused
                    && com.example.globe.core.PolarHazardWindow.appliesFreezeDamage(progress)) {
                int interval = com.example.globe.core.PolarHazardWindow.freezeDamageIntervalTicks(progress);
                if (worldTime % (long) interval == 0L) {
                    // B-7 S1: scale the lethal-core amount by the cold-protection multiplier at this single
                    // computed-amount point (interval unchanged, per the design). Full freeze-immune set -> 0.
                    float amount = com.example.globe.core.PolarHazardWindow.freezeDamageAmount(progress)
                            * (float) com.example.globe.core.ColdProtection.damageMultiplier(coldProtectionPieceCount(player));
                    if (amount > 0.0f) {
                        player.hurtServer(overworld, overworld.damageSources().freeze(), amount);
                    }
                }
            }

            if (!effectsTick) {
                continue;
            }

            int duration = 40;
            boolean ambient = true;
            boolean showParticles = false;
            boolean showIcon = false;

            int slowAmp = com.example.globe.core.PolarHazardWindow.slownessAmplifier(progress);
            int weakAmp = com.example.globe.core.PolarHazardWindow.weaknessAmplifier(progress);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, slowAmp, ambient, showParticles, showIcon));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weakAmp, ambient, showParticles, showIcon));
            if (com.example.globe.core.PolarHazardWindow.appliesMiningFatigue(progress)) {
                player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, 0, ambient, showParticles, showIcon));
            }
            // B-4: Blindness REMOVED from the polar hazard. The old hard blindness snap ("WHAM full
            // blindness with no warning") is replaced by the smooth ramping whiteout overlay
            // (PolarWhiteoutOverlayHud), which carries vision loss continuously. Freeze/slowness/weakness
            // stay as-is.
        }
    }


    /**
     * Persists the latitude band a player currently occupies, so the loading screen and the
     * world-selection list can show it the next time this save is opened. Called on a throttled
     * tick cadence while playing and once more, authoritatively, on disconnect.
     */
    private static void recordLastKnownBand(ServerLevel overworld, WorldBorder border, ServerPlayer player) {
        double absLatDeg = com.example.globe.util.LatitudeMath.absLatDegExact(border, player.getZ());
        com.example.globe.util.LatitudeBands.Band band = com.example.globe.util.LatitudeBands.fromAbsoluteLatitudeDeg(absLatDeg);
        LatitudeWorldState.get(overworld).setLastKnownBandId(band.id());
    }

    /**
     * B-5/B-7 Hemisphere Passage: handle a player's answer to a crossing prompt, on the server thread, ROUTED
     * BY AXIS. NEVER trusts the client -- re-derives the authoritative distance to that edge and rejects
     * anything not in the prompt band. Guards (in order): the axis's feature flag on ({@code PASSAGE_V2_ENABLED}
     * for EW, {@code POLE_PASSAGE_V2_ENABLED} for POLE); player alive + not removed; in the globe overworld;
     * within the prompt band for that axis; the axis surface gate (EW: not deep-underground; POLE: not
     * in-water/no-sky, S2 groundwork); the SHARED per-player cross cooldown ({@link #PASSAGE_CROSS_COOLDOWN_TICKS},
     * HIGH-2 idempotency). Creative/spectator MAY cross, but are EXEMPT from the turn-back push.
     */
    private static void handlePassageAnswer(ServerPlayer player, boolean cross, PassageAxis axis) {
        boolean flagOn = axis == PassageAxis.POLE
                ? com.example.globe.core.LatitudeV2Flags.POLE_PASSAGE_V2_ENABLED
                : com.example.globe.core.LatitudeV2Flags.PASSAGE_V2_ENABLED;
        if (!flagOn) {
            return; // this axis is off: every answer ignored, live behavior untouched.
        }
        if (player == null || player.isRemoved() || !player.isAlive()) {
            return; // no-op cleanly if the player died/left between answering and this tick.
        }
        if (!(player.level() instanceof ServerLevel world)
                || world != world.getServer().overworld()
                || !isGlobeOverworld(world)) {
            return; // must be standing in the globe overworld.
        }
        WorldBorder answerBorder = world.getWorldBorder();
        // Distance + prompt line for THIS axis, both anchored to the mod's synced intended radius (never the
        // live/lerping border half), so client and server agree even mid-lerp. serverAcceptsCross adds slack.
        double distToEdge;
        double promptAt;
        if (axis == PassageAxis.POLE) {
            int zRadius = LatitudeBiomes.getActiveRadiusBlocks();
            if (zRadius <= 0) {
                zRadius = (int) Math.round(com.example.globe.util.LatitudeMath.latitudeRadius(answerBorder));
            }
            distToEdge = com.example.globe.core.PoleGeometry.distanceToPole(zRadius, answerBorder.getCenterZ(), player.getZ());
            promptAt = com.example.globe.core.PoleGeometry.resolve(zRadius).promptDist();
        } else {
            distToEdge = distanceToEwEdgeBlocks(answerBorder, player.getX());
            double xRadiusIntended = com.example.globe.util.LatitudeMath.intendedXRadius(answerBorder);
            promptAt = com.example.globe.core.EdgeGeometry.resolve(xRadiusIntended).promptDist();
        }
        if (!com.example.globe.core.HemispherePassage.serverAcceptsCross(distToEdge, promptAt)) {
            LOGGER.warn("[Latitude][Passage] Rejected {} answer from {} cross={} distToEdge={} promptAt={} (not in prompt band; possible spoof)",
                    axis, player.getName().getString(), cross, distToEdge, promptAt);
            return;
        }
        // Surface gate (belt-and-suspenders; the client also freezes the prompt). EW rejects a deep-underground
        // sender; POLE rejects an in-water OR no-sky sender (S2 groundwork) -- an under-ice swimmer or cave
        // explorer must not cross; the pole crossing is a surface act.
        if (axis == PassageAxis.POLE) {
            if (isInWaterOrNoSky(world, player)) {
                LOGGER.warn("[Latitude][PolePassage] Rejected answer from {} cross={} (in-water/no-sky; pole crossing is surface-only)",
                        player.getName().getString(), cross);
                return;
            }
        } else if (isDeepUnderground(world, player)) {
            LOGGER.warn("[Latitude][Passage] Rejected answer from {} cross={} (underground; passage is surface-only)",
                    player.getName().getString(), cross);
            return;
        }
        if (cross) {
            // HIGH-2 idempotency guard (SHARED across axes): reject a repeat cross inside the cooldown window.
            long now = world.getGameTime();
            Long lastCross = PASSAGE_LAST_CROSS_TICK.get(player.getUUID());
            if (lastCross != null && (now - lastCross) < PASSAGE_CROSS_COOLDOWN_TICKS) {
                LOGGER.warn("[Latitude][Passage] Rejected repeat {} cross from {} within cooldown ({} of {} ticks)",
                        axis, player.getName().getString(), now - lastCross, PASSAGE_CROSS_COOLDOWN_TICKS);
                return;
            }
            BlockPos arrival = axis == PassageAxis.POLE
                    ? HemispherePassageService.crossPole(player)
                    : HemispherePassageService.crossHemisphere(player);
            if (arrival != null) {
                PASSAGE_LAST_CROSS_TICK.put(player.getUUID(), now);
                if (axis == PassageAxis.POLE) {
                    // S5(c)/S16(c): stamp the one-shot post-crossing cold grace -- the arrival now lands ON the
                    // pole line (90 deg, ~6 HP/s raw) behind the opaque arrival curtain, so cold damage (both
                    // bands) + the F3 cue are suppressed for the ceremony window only, then the blizzard owns
                    // them for the 90->88 escape trek. Re-stamped only by a NEW successful crossing.
                    POLE_CROSS_COLD_GRACE_UNTIL.put(player.getUUID(),
                            com.example.globe.core.PoleCrossingGrace.graceUntil(now));
                    // S9: drop the pre-warm ticket -- the crossing's own synchronous 3x3 FULL load has just
                    // taken over at this exact target (the ticket did its job getting the terrain generated
                    // beforehand). If the arriver lingers in the far band, next tick re-arms a fresh ticket
                    // for their RETURN target as ordinary in-band behavior.
                    dropPolePrewarm(world, player.getUUID());
                }
                // Per-player S2C ONLY (never broadcast). P2 consumes it for the arrival ceremony + to seed the
                // right client arm disarmed-in-band. Carries the axis and the arrival (X,Z).
                ServerPlayNetworking.send(player, new GlobeNet.PassageArrivalPayload(axis, arrival.getX(), arrival.getZ()));
            }
        } else {
            if (player.isCreative() || player.isSpectator()) {
                return; // exempt from push-back.
            }
            PENDING_PASSAGE_TURN_BACK.put(player.getUUID(), axis); // one-shot nudge, applied next tick.
        }
    }

    /**
     * Apply (and consume) a pending B-5/B-7 turn-back nudge for {@code player}: a SINGLE gentle velocity impulse
     * back toward center on the DECLINED axis (EW: toward center-X; POLE: equatorward toward center-Z), then the
     * player is free. A per-tick push would fight input (the vetoed hard yank), so this fires exactly once. Gates
     * on the declined axis's feature flag and skips creative/spectator (belt + suspenders; also filtered at
     * enqueue time).
     */
    private static void applyPendingPassageTurnBack(ServerPlayer player, WorldBorder border) {
        PassageAxis axis = PENDING_PASSAGE_TURN_BACK.remove(player.getUUID());
        if (axis == null) {
            return;
        }
        boolean flagOn = axis == PassageAxis.POLE
                ? com.example.globe.core.LatitudeV2Flags.POLE_PASSAGE_V2_ENABLED
                : com.example.globe.core.LatitudeV2Flags.PASSAGE_V2_ENABLED;
        if (!flagOn) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        net.minecraft.world.phys.Vec3 v = player.getDeltaMovement();
        if (axis == PassageAxis.POLE) {
            double centerZ = border.getCenterZ();
            double dir = player.getZ() >= centerZ ? -1.0 : 1.0; // steer equatorward (toward center Z).
            player.setDeltaMovement(v.x, v.y, v.z + dir * PASSAGE_TURN_BACK_IMPULSE);
        } else {
            double centerX = border.getCenterX();
            double dir = player.getX() >= centerX ? -1.0 : 1.0; // steer back toward center X.
            player.setDeltaMovement(v.x + dir * PASSAGE_TURN_BACK_IMPULSE, v.y, v.z);
        }
        player.needsSync = true; // force the velocity to broadcast to the client (vanilla knockback idiom).
    }

    /**
     * B-7 S1 cold protection: count how many of the player's four armor slots ({@link #COLD_ARMOR_SLOTS}) carry
     * an item in the vanilla {@code freeze_immune_wearables} tag (leather by default, datapack-extensible per the
     * vanilla-first law). The thin MC shim feeding {@link com.example.globe.core.ColdProtection}; 0..4.
     */
    private static int coldProtectionPieceCount(ServerPlayer player) {
        int count = 0;
        for (EquipmentSlot slot : COLD_ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.is(holder -> holder.is(ItemTags.FREEZE_IMMUNE_WEARABLES))) {
                count++;
            }
        }
        return count;
    }

    /**
     * B-7 S2/item 8: the pole surface gate -- true if the player is in water OR has no sky overhead. Broader than
     * {@link #isDeepUnderground} (the EW gate) by design: an under-ice swimmer (in water) and a cave explorer (no
     * sky) must BOTH be denied the pole crossing (the belt-and-suspenders sibling of B-5's underground reject;
     * the primary client-side suppression is P2's). Binary {@code canSeeSky} is CORRECT here -- prompt gates want
     * conservative-deny -- unlike the S4 shelter rule, which needs the graded {@link #isColdSheltered} read.
     */
    private static boolean isInWaterOrNoSky(ServerLevel world, ServerPlayer player) {
        return player.isInWater() || !world.canSeeSky(player.blockPosition().above());
    }

    /**
     * B-7 S4 shelter shim: RAW SKY LIGHT at the player's eye position, fed to the pure
     * {@link com.example.globe.core.ColdShelter} classifier (sheltered iff {@code <= 3}). Raw sky light is
     * graded and trap-proof where binary {@code canSeeSky} is not: the single-overhead-log trap reads ~11-13
     * (side-lit, still exposed) while a sealed hut/cave/snow burrow reads 0-2 -- and it is time-of-day
     * independent (night darkness lives in skyDarken, not the stored light).
     */
    private static boolean isColdSheltered(ServerLevel world, ServerPlayer player) {
        BlockPos eye = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        int rawSkyLight = world.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(eye);
        return com.example.globe.core.ColdShelter.isSheltered(rawSkyLight);
    }

    /** B-7 S5(c) shim: is the post-pole-crossing cold-grace window still open for this player? Lazily drops the
     *  map entry once it lapses (the one-shot can only be re-opened by a NEW successful crossing). */
    private static boolean isPoleCrossColdGraceActive(ServerPlayer player, long worldTime) {
        Long until = POLE_CROSS_COLD_GRACE_UNTIL.get(player.getUUID());
        if (until == null) {
            return false;
        }
        if (!com.example.globe.core.PoleCrossingGrace.isActive(until, worldTime)) {
            POLE_CROSS_COLD_GRACE_UNTIL.remove(player.getUUID());
            return false;
        }
        return true;
    }

    /**
     * B-7 S6 warmth shim: is a {@link com.example.globe.core.PolarWarmth} source within the ~4-block box
     * (9x5x9) around the player? The 405-block scan is cached per player for {@link #WARMTH_RESCAN_TICKS}
     * (~1 s) so the per-tick cold section and heal-time lock checks share one scan; callers only invoke this
     * for SHELTERED polar players (the short-circuit order zone -&gt; shelter -&gt; scan), so the cost is a
     * campfire-hut nicety, not a world tax.
     */
    private static boolean isNearWarmthCached(ServerLevel world, ServerPlayer player, long worldTime) {
        long[] cached = POLE_WARMTH_CACHE.get(player.getUUID());
        if (cached != null && (worldTime - cached[0]) < WARMTH_RESCAN_TICKS && worldTime >= cached[0]) {
            return cached[1] != 0L;
        }
        boolean warm = scanForWarmth(world, player.blockPosition());
        POLE_WARMTH_CACHE.put(player.getUUID(), new long[]{worldTime, warm ? 1L : 0L});
        return warm;
    }

    /** The raw S6 box scan: feed every block state in the 9x5x9 box through the pure
     *  {@link com.example.globe.core.PolarWarmth#isWarmBlock} classifier (registry id + LIT; blocks without a
     *  LIT property pass {@code lit=true}, which the classifier ignores for anything outside the warm set). */
    private static boolean scanForWarmth(ServerLevel world, BlockPos center) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy = -WARMTH_SCAN_RADIUS_Y; dy <= WARMTH_SCAN_RADIUS_Y; dy++) {
            for (int dx = -WARMTH_SCAN_RADIUS_XZ; dx <= WARMTH_SCAN_RADIUS_XZ; dx++) {
                for (int dz = -WARMTH_SCAN_RADIUS_XZ; dz <= WARMTH_SCAN_RADIUS_XZ; dz++) {
                    m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = world.getBlockState(m);
                    if (state.isAir()) {
                        continue;
                    }
                    Identifier id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    boolean lit = !state.hasProperty(BlockStateProperties.LIT)
                            || state.getValue(BlockStateProperties.LIT);
                    if (com.example.globe.core.PolarWarmth.isWarmBlock(id.getNamespace(), id.getPath(), lit)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * B-7 S6 (FROZEN WOUNDS): the heal-lock gate, called by {@code LivingEntityHealLockMixin} at the single
     * {@code LivingEntity.heal} chokepoint. TRUE cancels the heal. Pure predicate applied AT HEAL TIME -- no
     * cumulative pool, no persistent state ({@link com.example.globe.core.PolarWounds#healLocked}): locked iff
     * a survival/adventure player in a globe overworld is inside the polar cold zone
     * ({@code |lat| >= 85}), genuinely sheltered (S4 {@link #isColdSheltered}), and NOT near a
     * {@link com.example.globe.core.PolarWarmth} source ("only the warmth of a fire mends them"). Everything
     * else -- other mobs, other dimensions, non-globe worlds, sub-85 latitudes, exposed players, players by a
     * fire -- heals 100% vanilla. The S5 post-crossing grace is deliberately NOT consulted (grace suppresses
     * damage + cue only; an arriving crosser is exposed, so the lock is off anyway). Accepted v1 edges:
     * golden-apple ABSORPTION is not a heal() and passes; dev setHealth writes bypass heal().
     */
    public static boolean isPolarHealLocked(net.minecraft.world.entity.LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (!(player.level() instanceof ServerLevel level) || !isGlobeOverworld(level)) {
            return false;
        }
        double latDeg = com.example.globe.util.LatitudeMath.absLatDegExact(level.getWorldBorder(), player.getZ());
        boolean inColdZone = latDeg >= com.example.globe.core.PolarHazardWindow.FROSTBITE_ONSET_DEG;
        if (!inColdZone) {
            return false;
        }
        boolean sheltered = isColdSheltered(level, player);
        if (!sheltered) {
            return false;
        }
        boolean nearWarmth = isNearWarmthCached(level, player, level.getGameTime());
        return com.example.globe.core.PolarWounds.healLocked(true, true, nearWarmth);
    }

    /**
     * B-7 S2: the Wide-world pole hard stop. On a Wide (Mercator) world the square vanilla border is sized to the
     * WIDER X axis, so the pole line at {@code |z| = zRadius} is interior and unwalled -- an endless death plain.
     * This clamps a player's position/velocity back to that line (a lightweight position-correction packet --
     * NOT an entity teleport, NOT a wrap, NOT worldgen). Classic worlds already wall the pole with the vanilla
     * border, so this no-ops there. [P3 fix 2026-07-14] Gamemode rule = TRUE vanilla-border parity
     * ({@link com.example.globe.core.PoleHardStop#exemptFromClamp}): the wall stops survival, adventure AND
     * CREATIVE (the vanilla border stops creative flight too -- the owner flew past 90 in creative on TEST 97
     * and rightly found "no wall" wrong); only SPECTATOR passes. Creative gets the same clamp + contact
     * presentation; the crossing/prompt stays available to creative unchanged. Skips the tick a crossing fired
     * for this player (the crossing owns the position that tick). Fires the presentable rate-limited contact
     * event on engagement.
     */
    private static void applyPoleHardStop(ServerPlayer player, WorldBorder border, long worldTime) {
        if (!LatitudeBiomes.isMercator()) {
            return; // Classic: the vanilla square border already walls |z| = zRadius.
        }
        if (com.example.globe.core.PoleHardStop.exemptFromClamp(player.isCreative(), player.isSpectator())) {
            return; // ONLY spectator no-clips past the line (vanilla-border parity; creative is clamped).
        }
        Long lastCross = PASSAGE_LAST_CROSS_TICK.get(player.getUUID());
        if (lastCross != null && lastCross == worldTime) {
            return; // a crossing fired this tick -- it owns the position; don't fight it.
        }
        int zRadius = LatitudeBiomes.getActiveRadiusBlocks();
        // Pure engagement decision (PoleHardStop owns epsilon/clampedZ/outward-sign; zRadius <= 0 never engages).
        com.example.globe.core.PoleHardStop.Decision d = com.example.globe.core.PoleHardStop.evaluate(
                player.getZ(), border.getCenterZ(), zRadius, com.example.globe.core.PoleHardStop.CLAMP_EPSILON);
        if (!d.engaged()) {
            return;
        }
        // F1 (sweep MEDIUM): a MOUNTED player must not ride through the wall -- the vehicle re-derives the
        // passenger from its own un-clamped position, so correcting only the player is a no-op while riding.
        // Parity + simplicity: dismount first (the same discipline the crossing uses), then clamp the player;
        // and kill the vehicle's own outward-Z momentum under the SAME pure law so the horse/boat stops at the
        // wall beside its rider instead of coasting past it. (Vanilla's border blocks vehicles physically;
        // dismount-at-the-line is our equivalent -- documented parity choice.)
        if (player.isPassenger()) {
            net.minecraft.world.entity.Entity vehicle = player.getVehicle();
            player.stopRiding();
            if (vehicle != null) {
                net.minecraft.world.phys.Vec3 vv = vehicle.getDeltaMovement();
                vehicle.setDeltaMovement(vv.x, vv.y,
                        com.example.globe.core.PoleHardStop.killOutwardZ(vv.z, d.outwardSign()));
            }
        }
        // Kill the player's OUTWARD Z velocity (keep inward/lateral motion) so momentum toward the pole dies.
        net.minecraft.world.phys.Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(v.x, v.y, com.example.globe.core.PoleHardStop.killOutwardZ(v.z, d.outwardSign()));
        // Snap the position back to the line via a position-correction packet (vanilla-border-like rubber-band).
        player.connection.teleport(player.getX(), player.getY(), d.clampedZ(), player.getYRot(), player.getXRot());
        player.needsSync = true;
        emitPoleClampContact(player, d.clampedZ(), worldTime);
    }

    /**
     * The presentable pole-clamp contact event (owner amendment 2026-07-13): so an invisible wall never confuses
     * a swimmer or spelunker. Rate-limited to ~2 s per player by TICK COUNTER (never wall-clock,
     * {@link com.example.globe.core.PoleClampContact}); plays a frost/glass chime at the contact point and sends
     * an action-bar line whose wording is chosen by whether the player is in water. Logs the FIRST contact once
     * per session. Richer presentation (particles / a keyline plane) is P2's.
     */
    private static void emitPoleClampContact(ServerPlayer player, double contactZ, long worldTime) {
        long last = POLE_CLAMP_LAST_CONTACT_TICK.getOrDefault(player.getUUID(),
                com.example.globe.core.PoleClampContact.NEVER);
        if (!com.example.globe.core.PoleClampContact.shouldEmit(last, worldTime,
                com.example.globe.core.PoleClampContact.CONTACT_COOLDOWN_TICKS)) {
            return;
        }
        POLE_CLAMP_LAST_CONTACT_TICK.put(player.getUUID(), worldTime);
        boolean inWater = player.isInWater();
        ServerLevel world = (ServerLevel) player.level();
        world.playSound(null, player.getX(), player.getY(), contactZ,
                SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 0.7f, 0.8f);
        player.sendSystemMessage(Component.literal(com.example.globe.core.PoleClampContact.message(inWater)), true);
        if (POLE_CLAMP_LOGGED.add(player.getUUID())) {
            LOGGER.info("[Latitude][PolePassage] Pole hard-stop first contact for {} at z={} (inWater={})",
                    player.getName().getString(), contactZ, inWater);
        }
    }

    /**
     * B-7 S9 (arrival pre-warm, TEST 99 freeze mitigation): while a could-cross player (survival/adventure/
     * creative -- spectator excluded like the prompt) is inside the pole PROMPT BAND
     * ({@code distanceToPole <= promptAt + 32}, Wide AND Classic), keep a {@code TicketType.PORTAL} radius-2
     * (5x5 chunk) load ticket alive at their CURRENT antipodal arrival target, so the landing terrain is
     * generated BEFORE they answer the prompt -- killing the virgin-antipode generation cliff that stalled
     * TEST 99 (60+ tick server debt + a JourneyMap fullmap open = client hard-stall) and the answer-time
     * density-compile burst ("loadedTeleportChunks=9" right at the cross).
     *
     * <p><b>Idiom.</b> The target is computed by the SAME code path the crossing itself uses
     * ({@link HemispherePassageService#poleArrivalTarget} -- pure arithmetic, no chunk access, never a
     * re-derived copy). The ticket is the 26.2 {@code ServerChunkCache.addTicketWithRadius(PORTAL, pos, 2)}
     * -- PORTAL is vanilla's own "pre-load the far side of a portal" type, timeout 300 ticks, so it is
     * TIMEOUT-STYLE: we re-add it every {@link com.example.globe.core.PoleArrivalPrewarm#REFRESH_CADENCE_TICKS}
     * (~3 s) while in-band rather than holding a permanent ticket, and any missed drop self-expires within
     * 15 s. This ASYNC ticket replaces nothing at answer time: the crossing's own synchronous 3x3 FULL ring
     * ({@code placeSafeY}) still runs -- it just finds the chunks already generated.
     *
     * <p><b>Pure vs shim.</b> Every DECISION (band membership incl. the 32-block margin, gamemode
     * eligibility, the 64-block re-anchor drift, the refresh cadence) is the pure, tested
     * {@link com.example.globe.core.PoleArrivalPrewarm}; this method is the thin shim that reads
     * position/gamemode, computes the target, and add/removes the ticket.
     *
     * <p><b>Drops:</b> band-exit or gamemode-ineligibility (here), a successful crossing
     * ({@code handlePassageAnswer} -- the crossing's own load takes over), disconnect (the connection
     * handler), and an unresolved radius. <b>EW is exempt by design</b>: its arrival is the same-|X| mirror
     * column at the player's own Z -- terrain at the identical border distance they are already standing at,
     * never a virgin antipode at this scale, so pre-warming it would ticket chunks the approach already loads.
     */
    private static void tickPolePrewarm(ServerPlayer player, ServerLevel world, WorldBorder border, long worldTime) {
        java.util.UUID uuid = player.getUUID();
        int zRadius = LatitudeBiomes.getActiveRadiusBlocks();
        if (zRadius <= 0) {
            dropPolePrewarm(world, uuid);
            return; // radius not resolved -- no geometry to anchor on.
        }
        double distToPole = com.example.globe.core.PoleGeometry.distanceToPole(zRadius, border.getCenterZ(), player.getZ());
        double promptAt = com.example.globe.core.PoleGeometry.resolve(zRadius).promptDist();
        if (!com.example.globe.core.PoleArrivalPrewarm.eligibleGameMode(player.isCreative(), player.isSpectator())
                || !com.example.globe.core.PoleArrivalPrewarm.inPrewarmBand(distToPole, promptAt)) {
            dropPolePrewarm(world, uuid);
            return;
        }
        HemispherePassageService.PoleArrivalTarget target =
                HemispherePassageService.poleArrivalTarget(world, player.getX(), player.getZ());
        PolePrewarmAnchor prev = POLE_PREWARM.get(uuid);
        if (prev == null) {
            addPolePrewarmTicket(world, target.baseX(), target.targetZ());
            POLE_PREWARM.put(uuid, new PolePrewarmAnchor(target.baseX(), target.targetZ(), worldTime));
            // The flight-recorder line (one per band-entry): prove pre-warm from a live log.
            if (Boolean.getBoolean("latitude.debugPassage")) {
                LOGGER.info("[LatPassage] EVENT axis=POLE prewarm ARM player={} anchor=({},{}) dist={} promptAt={}",
                        player.getName().getString(), target.baseX(), target.targetZ(),
                        String.format(java.util.Locale.ROOT, "%.1f", distToPole),
                        String.format(java.util.Locale.ROOT, "%.1f", promptAt));
            }
            return;
        }
        if (com.example.globe.core.PoleArrivalPrewarm.needsReanchor(
                prev.targetX(), prev.targetZ(), target.baseX(), target.targetZ())) {
            // Drift re-anchor: release the stale square, ticket the new one.
            removePolePrewarmTicket(world, prev.targetX(), prev.targetZ());
            addPolePrewarmTicket(world, target.baseX(), target.targetZ());
            POLE_PREWARM.put(uuid, new PolePrewarmAnchor(target.baseX(), target.targetZ(), worldTime));
            return;
        }
        if (com.example.globe.core.PoleArrivalPrewarm.shouldRefresh(prev.lastRefreshTick(), worldTime)) {
            // Same anchor: re-add on the slow cadence to reset PORTAL's 300-tick timeout (timeout-style keep-alive).
            addPolePrewarmTicket(world, prev.targetX(), prev.targetZ());
            POLE_PREWARM.put(uuid, new PolePrewarmAnchor(prev.targetX(), prev.targetZ(), worldTime));
        }
    }

    /** S9 drop: forget the anchor and release its ticket (best-effort -- PORTAL's 300-tick timeout is the
     *  backstop if the remove ever misses, e.g. after a same-position re-add landed in the same window). */
    private static void dropPolePrewarm(ServerLevel world, java.util.UUID uuid) {
        PolePrewarmAnchor prev = POLE_PREWARM.remove(uuid);
        if (prev != null) {
            removePolePrewarmTicket(world, prev.targetX(), prev.targetZ());
        }
    }

    private static void addPolePrewarmTicket(ServerLevel world, int blockX, int blockZ) {
        world.getChunkSource().addTicketWithRadius(TicketType.PORTAL,
                new ChunkPos(Math.floorDiv(blockX, 16), Math.floorDiv(blockZ, 16)),
                com.example.globe.core.PoleArrivalPrewarm.TICKET_RADIUS_CHUNKS);
    }

    private static void removePolePrewarmTicket(ServerLevel world, int blockX, int blockZ) {
        world.getChunkSource().removeTicketWithRadius(TicketType.PORTAL,
                new ChunkPos(Math.floorDiv(blockX, 16), Math.floorDiv(blockZ, 16)),
                com.example.globe.core.PoleArrivalPrewarm.TICKET_RADIUS_CHUNKS);
    }

    /** Server-side blocks to the nearest E/W world-border edge ({@code >= 0}), anchored to the mod's INTENDED
     *  X radius (not the live/lerping border half) so the anti-exploit re-validation matches the client UI,
     *  which now anchors on the same intended radius. */
    private static double distanceToEwEdgeBlocks(WorldBorder border, double x) {
        return com.example.globe.util.LatitudeMath.distanceToEwEdgeIntended(border, x);
    }

    /**
     * B-5 item 2 (surface-only passage): server-derived "deep underground" -- genuinely below the surface AND
     * with no sky overhead. Mirrors {@code GlobeClientState.isDeepUnderground} exactly (the same
     * {@code PolarExposure.isBelowSurface} depth cut the client + enclosure sampler use, AND-ed with a real
     * {@code canSeeSky} check), so the server never trusts a stale client prompt sent from a cave.
     */
    private static boolean isDeepUnderground(ServerLevel world, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        if (!com.example.globe.core.PolarExposure.isBelowSurface(pos.getY(), world.getSeaLevel())) {
            return false;
        }
        return !world.canSeeSky(pos.above());
    }

    /**
     * True iff {@code entity} is a survival/adventure {@link ServerPlayer} standing in a globe overworld at
     * or beyond the polar HAZARD onset -- i.e. exactly the band where {@link #borderUxTick} drives our own
     * latitude-scaled freeze damage (and holds {@code ticksFrozen} at/above vanilla's fully-frozen threshold
     * so the blue hearts show, TEST 77). This is the SOLE gate {@code LivingEntityFreezeDamageMixin} uses to
     * suppress vanilla's own fixed 1 HP/40-tick auto-freeze damage, keeping our curve the ONLY freeze-damage
     * source in-band while real powder snow / non-globe / non-band / creative / spectator play stays 100%
     * vanilla. Mirrors {@link #borderUxTick}'s own in-band test (isGlobeOverworld + absLatDegExact &gt;=
     * {@link com.example.globe.core.PolarHazardWindow#HAZARD_ONSET_DEG} + not creative/spectator) so the two
     * can never drift apart.
     *
     * <p><b>Accepted niche (B-7 F5): powder-snow double-dip in the frostbite band {@code [85,87.5)}.</b> This
     * gate starts at the HAZARD onset (87.5), so vanilla's own powder-snow auto-freeze damage is NOT suppressed
     * in the frostbite band -- a player standing fully-buried IN powder snow there can take vanilla's
     * 1 HP/40t on top of the S3 frostbite nibble. Accepted as-is: it requires deliberate burial in powder snow
     * (not mere polar presence), reads as "buried in powder snow hurts extra" (true), and widening this gate to
     * 85 would silently disable REAL powder-snow freezing across the whole frostbite band -- a worse lie.
     */
    public static boolean isInPolarFreezeDamageBand(net.minecraft.world.entity.LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (!(player.level() instanceof ServerLevel level) || !isGlobeOverworld(level)) {
            return false;
        }
        double latDeg = com.example.globe.util.LatitudeMath.absLatDegExact(level.getWorldBorder(), player.getZ());
        // S7: the SAME immersion shift borderUxTick's chokepoint applies -- an immersed swimmer whose
        // EFFECTIVE latitude is in the lethal band (e.g. raw 85 in water = 88) gets our curve, so vanilla's
        // fixed auto-freeze (which keys off the 140 ticksFrozen our shifted frost visual now crosses) must be
        // suppressed for exactly the same population. Raw-lat here with an immersed 85-deg swimmer would
        // double-dip; the mirror discipline in this javadoc is the whole point of this method.
        double effLatDeg = com.example.globe.core.PolarImmersion.effectiveLatDeg(latDeg, player.isInWater());
        return effLatDeg >= com.example.globe.core.PolarHazardWindow.HAZARD_ONSET_DEG;
    }

    /**
     * Solar Tilt functional layer (P1) — the SINGLE server-side consumer of the {@link com.example.globe.core.SolarTilt}
     * evaluator. Both mob-rule mixins ({@code MonsterSolarSpawnMixin} for the polar-night dark-spawn / midnight-sun
     * spawn veto, {@code MobSolarSunBurnMixin} for undead sun-burn) funnel through here so the "effective sun" they
     * obey can NEVER drift from what the sky (P2) will draw — the §8 one-evaluator law.
     *
     * <p>Reads the vanilla-synced clock ({@link ServerLevel#getOverworldClockTime()} — sweep-verified real,
     * survived the 26.2 WorldClock rework) and the position's signed latitude ({@code -degreesFromZ}, north = +φ),
     * derives δ(day) and the current hour angle, and returns which 24-hour polar regime the column is in on the
     * winter/summer side today. Gated: returns {@link com.example.globe.core.SolarTilt.FunctionalBand#NONE} unless
     * the master flag is on AND this is a globe overworld. The {@code functionalMinDeg} floor (A2; default 60 since TEST 114, was 74.5) is
     * applied inside {@code SolarTilt.functionalBand}. Zero persistent state (§8d): stateless per check.
     */
    public static com.example.globe.core.SolarTilt.FunctionalBand solarFunctionalBand(ServerLevel level, double blockZ) {
        if (!com.example.globe.core.LatitudeV2Flags.SOLAR_TILT_V2_ENABLED) {
            return com.example.globe.core.SolarTilt.FunctionalBand.NONE;
        }
        if (level == null || !isGlobeOverworld(level)) {
            return com.example.globe.core.SolarTilt.FunctionalBand.NONE;
        }
        WorldBorder border = level.getWorldBorder();
        double signedLatDeg = -com.example.globe.util.LatitudeMath.degreesFromZ(border, blockZ);
        double day = com.example.globe.core.SolarTilt.dayCount(level.getOverworldClockTime());
        double delta = com.example.globe.core.SolarTilt.deltaDeg(day,
                com.example.globe.core.LatitudeV2Flags.SOLAR_TILT_DELTA_MAX_DEG,
                com.example.globe.core.LatitudeV2Flags.SOLAR_TILT_YEAR_LENGTH_DAYS,
                com.example.globe.core.LatitudeV2Flags.SOLAR_TILT_FROZEN_PHASE_DEG);
        return com.example.globe.core.SolarTilt.functionalBand(signedLatDeg, delta,
                com.example.globe.core.LatitudeV2Flags.SOLAR_TILT_FUNCTIONAL_MIN_DEG);
    }

    public static boolean isGlobeOverworld(ServerLevel world) {
        LatitudeWorldState existingState = LatitudeWorldState.getIfPresent(world);
        if (existingState != null && existingState.getGlobeRadius() > 0) {
            return true;
        }
        ChunkGenerator gen = world.getChunkSource().getGenerator();
        if (!(gen instanceof NoiseBasedChunkGenerator noise)) return false;

        return isGlobeNoiseGenerator(noise);
    }

    public static boolean isGlobeNoiseGenerator(NoiseBasedChunkGenerator noise) {
        return noise != null && (noise.stable(GLOBE_SETTINGS_KEY)
                || noise.stable(GLOBE_SETTINGS_XSMALL_KEY)
                || noise.stable(GLOBE_SETTINGS_SMALL_KEY)
                || noise.stable(GLOBE_SETTINGS_REGULAR_KEY)
                || noise.stable(GLOBE_SETTINGS_LARGE_KEY)
                || noise.stable(GLOBE_SETTINGS_MASSIVE_KEY));
    }

    public static boolean shouldApplyLatitudeWorldgen(NoiseBasedChunkGenerator noise) {
        return WorldgenGeneratorAuthorityPolicy.shouldApply(
                isGlobeNoiseGenerator(noise),
                LatitudeBiomes.hasActiveWorldgenAuthority(),
                noise,
                activeLatitudeOverworldGenerator);
    }

    private static int borderRadiusForGlobeOverworld(ServerLevel world) {
        LatitudeWorldState existingState = LatitudeWorldState.getIfPresent(world);
        int persisted = existingState != null ? existingState.getGlobeRadius() : 0;
        if (persisted > 0) {
            return persisted;
        }
        ChunkGenerator gen = world.getChunkSource().getGenerator();
        if (!(gen instanceof NoiseBasedChunkGenerator noise)) return BORDER_RADIUS;
        return borderRadiusForNoiseGenerator(noise);
    }

    public static int borderRadiusForNoiseGenerator(NoiseBasedChunkGenerator noise) {
        if (noise == null) return BORDER_RADIUS;
        if (noise.stable(GLOBE_SETTINGS_KEY)) return 15000;
        if (noise.stable(GLOBE_SETTINGS_XSMALL_KEY)) return 3750;
        if (noise.stable(GLOBE_SETTINGS_SMALL_KEY)) return 5000;
        if (noise.stable(GLOBE_SETTINGS_REGULAR_KEY)) return BORDER_RADIUS;
        if (noise.stable(GLOBE_SETTINGS_LARGE_KEY)) return 10000;
        if (noise.stable(GLOBE_SETTINGS_MASSIVE_KEY)) return 20000;
        if (WorldgenGeneratorAuthorityPolicy.isExactActiveOverworld(
                LatitudeBiomes.hasActiveWorldgenAuthority(),
                noise,
                activeLatitudeOverworldGenerator)) {
            return LatitudeBiomes.getActiveRadiusBlocks();
        }

        return BORDER_RADIUS;
    }

    public static boolean trySetInitialLatitudeSpawn(ServerLevel world,
                                                     ServerLevelData levelData,
                                                     boolean generateBonusChest,
                                                     boolean debugWorld,
                                                     LevelLoadListener loadListener) {
        if (world == null || levelData == null || debugWorld || !isGlobeOverworld(world)) {
            return false;
        }
        String pendingZone = GlobePending.peek();
        if (pendingZone == null) {
            return false;
        }

        try {
            SpawnChoice spawnChoice = resolveInitialSpawnChoice(world, pendingZone);
            BlockPos spawnPos = spawnChoice.pos();
            if (loadListener != null) {
                loadListener.start(LevelLoadListener.Stage.PREPARE_GLOBAL_SPAWN, 0);
                loadListener.updateFocus(world.dimension(), ChunkPos.containing(spawnPos));
            }
            levelData.setSpawn(LevelData.RespawnData.of(world.dimension(), spawnPos, 0.0f, 0.0f));
            LatitudeWorldState.get(world).setSpawnPickerDismissed(true);
            pendingInitialBonusChest = false;
            if (generateBonusChest) {
                if (spawnChoice.terrainValidated()) {
                    placeLatitudeBonusChest(world, spawnPos);
                } else {
                    pendingInitialBonusChest = true;
                }
            }
            LOGGER.info("[Latitude] Early initial spawn set before player-spawn pregen: zone={} x={} y={} z={} radius={} terrainValidated={} bonusChest={} bonusChestDeferred={}",
                    spawnChoice.zoneId(), spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                    spawnChoice.radius(), spawnChoice.terrainValidated(), generateBonusChest,
                    pendingInitialBonusChest);
            if (loadListener != null) {
                loadListener.finish(LevelLoadListener.Stage.PREPARE_GLOBAL_SPAWN);
            }
            return true;
        } catch (RuntimeException e) {
            // No unchecked Latitude coordinate is returned. Vanilla now performs its own normal
            // safe-spawn selection, and the first join must not retry the expensive globe search.
            LatitudeWorldState.get(world).setSpawnPickerDismissed(true);
            LOGGER.warn("[Latitude] Immediate terrain-validated initial spawn unavailable; delegating to vanilla safe spawn without post-join relocation", e);
            return false;
        }
    }

    /**
     * Place the vanilla bonus chest at the Latitude globe spawn. We cancel vanilla setInitialSpawn
     * (which would place it at the vanilla-computed spawn), so this re-creates that placement at our
     * zone-based spawn. Mirrors vanilla MinecraftServer.setInitialSpawn's bonus-chest block.
     */
    private static void placeLatitudeBonusChest(ServerLevel world, BlockPos spawnPos) {
        try {
            // Ensure the spawn chunk is loaded so placement actually writes (vanilla's
            // getSpawnHeight loads it; our spawn path may not have).
            world.getChunk(spawnPos);

            // Vanilla's BONUS_CHEST feature is deliberately NOT used here. It shuffles every column
            // in the origin's chunk and places at the first MOTION_BLOCKING_NO_LEAVES position that
            // is air — which over water is the block *above the surface*, so in a wet spawn chunk the
            // chest ends up floating on the water and every torch fails canSurvive. Our spawn column
            // is already validated dry, sturdy land, so anchor the chest to it instead of re-rolling.
            BlockPos chestPos = findBonusChestSite(world, spawnPos);
            if (chestPos == null) {
                LOGGER.warn("[Latitude] No dry bonus-chest site near globe spawn x={} z={}; skipping chest",
                        spawnPos.getX(), spawnPos.getZ());
                return;
            }

            world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            RandomizableContainer.setBlockEntityLootTable(
                    world, world.getRandom(), chestPos, BuiltInLootTables.SPAWN_BONUS_CHEST);

            BlockState torch = Blocks.TORCH.defaultBlockState();
            int torches = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos torchPos = chestPos.relative(direction);
                if (torch.canSurvive(world, torchPos) && world.getBlockState(torchPos).isAir()) {
                    world.setBlock(torchPos, torch, 2);
                    torches++;
                }
            }
            LOGGER.info("[Latitude] Placed bonus chest at globe spawn x={} y={} z={} torches={}",
                    chestPos.getX(), chestPos.getY(), chestPos.getZ(), torches);
        } catch (Throwable t) {
            LOGGER.warn("[Latitude] Failed to place bonus chest at globe spawn (continuing without)", t);
        }
    }

    /**
     * Nearest dry, solid-supported column to the spawn that can host the chest without burying the
     * player's own spawn block. Prefers a site where the torches can actually stand, so the chest
     * reads as a deliberate supply drop rather than something washed up.
     */
    private static BlockPos findBonusChestSite(ServerLevel world, BlockPos spawnPos) {
        BlockPos fallback = null;
        for (int radius = 1; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    BlockPos candidate = world.getHeightmapPos(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            new BlockPos(spawnPos.getX() + dx, world.getMinY(), spawnPos.getZ() + dz));
                    if (!isDryChestSite(world, candidate)) {
                        continue;
                    }
                    if (countSurvivableTorchSides(world, candidate) >= 2) {
                        return candidate;
                    }
                    if (fallback == null) {
                        fallback = candidate;
                    }
                }
            }
        }
        return fallback;
    }

    private static boolean isDryChestSite(ServerLevel world, BlockPos pos) {
        if (!world.getBlockState(pos).isAir() || !world.getFluidState(pos).isEmpty()) {
            return false;
        }
        BlockPos support = pos.below();
        return world.getFluidState(support).isEmpty()
                && world.getBlockState(support).isFaceSturdy(world, support, Direction.UP);
    }

    private static int countSurvivableTorchSides(ServerLevel world, BlockPos chestPos) {
        BlockState torch = Blocks.TORCH.defaultBlockState();
        int count = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos torchPos = chestPos.relative(direction);
            if (torch.canSurvive(world, torchPos) && world.getBlockState(torchPos).isAir()) {
                count++;
            }
        }
        return count;
    }

    private static void applySpawnChoice(ServerPlayer player, String id) {
        if (player.entityTags().contains(SPAWN_CHOSEN_TAG)) {
            return;
        }

        if (Boolean.getBoolean("latitude.disableSpawnTeleport")) {
            // DEBUG ONLY: avoid join hitch while diagnosing spawn teleport.
            LOGGER.info("Spawn teleport disabled by latitude.disableSpawnTeleport (debug only).");
            return;
        }

        ServerLevel world = (ServerLevel) player.level();
        if (!isGlobeOverworld(world)) {
            return;
        }

        SpawnChoice spawnChoice;
        try {
            spawnChoice = resolveSpawnChoice(world, id);
        } catch (RuntimeException e) {
            LOGGER.error(
                    "[Latitude] No terrain-validated spawn was available for zone={}; keeping the player's current safe position",
                    id,
                    e);
            return;
        }
        LOGGER.info("Applying spawn choice: player={}, zoneId={}", player.getName().getString(), spawnChoice.zoneId());

        BlockPos clampedSpawnPos = spawnChoice.pos();
        world.setRespawnData(LevelData.RespawnData.of(world.dimension(), clampedSpawnPos, 0.0f, 0.0f));

        BlockPos teleportPos = clampedSpawnPos;
        player.teleportTo(world, teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5, EnumSet.noneOf(Relative.class), player.getYRot(), player.getXRot(), true);
        player.setDeltaMovement(0.0, 0.0, 0.0);
        player.fallDistance = 0.0F;
        player.addTag(SPAWN_CHOSEN_TAG);
        LatitudeWorldState.get(world).setSpawnPickerDismissed(true);
    }

    private static SpawnChoice resolveInitialSpawnChoice(ServerLevel world, String id) {
        return resolveSpawnChoice(
                world,
                id,
                SpawnSafetyPolicy.INITIAL_SPAWN_TERRAIN_VALIDATION_BUDGET,
                false,
                true);
    }

    private static SpawnChoice resolveSpawnChoice(ServerLevel world, String id) {
        return resolveSpawnChoice(world, id, Integer.MAX_VALUE, true, true);
    }

    private static SpawnChoice resolveSpawnChoice(
            ServerLevel world,
            String id,
            int terrainValidationBudget,
            boolean prepareTeleportNeighbors,
            boolean allowTerrainFallback) {
        String zoneId = id;
        long seed = world.getServer().getWorldGenSettings().options().seed();
        if (zoneId != null && zoneId.equals("RANDOM")) {
            zoneId = resolveSpawnZoneId(zoneId, seed);
            LOGGER.info("Resolved RANDOM spawn zone: seed={}, chosen={}", seed, zoneId);
        }

        if (zoneId == null) {
            zoneId = "TEMPERATE";
        }

        int radius = LatitudeBiomes.getActiveRadiusBlocks();
        if (radius <= 0) {
            WorldBorder border = world.getWorldBorder();
            radius = (int) Math.round(com.example.globe.util.LatitudeMath.halfSize(border));
        }

        double v = hash01(seed, 1, 0, SPAWN_SALT);

        double spawnAbsLatFrac = com.example.globe.util.LatitudeMath.spawnFracForZoneKey(zoneId);
        int z = (int) Math.round(radius * spawnAbsLatFrac);
        if (v < 0.5) {
            z = -z;
        }

        int warnStartZ = Math.max(0, radius - POLE_WARNING_DISTANCE_BLOCKS);
        int maxAbsZ = Math.max(0, warnStartZ - 500);
        z = Mth.clamp(z, -maxAbsZ, maxAbsZ);

        int targetZ = z;
        ResolvedSpawn resolvedSpawn;
        try {
            SamplerTemplate template = BiomeSamplerTools.createTemplate(world);
            RandomState noiseConfig = RandomState.create(
                    template.noiseParameters(), seed, template.settings().value());
            Climate.Sampler sampler = noiseConfig.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
            resolvedSpawn = findLandSpawn(
                    world,
                    template,
                    sampler,
                    noiseConfig,
                    radius,
                    targetZ,
                    seed,
                    terrainValidationBudget,
                    prepareTeleportNeighbors);
        } catch (Exception e) {
            LOGGER.warn("[Latitude] Biome probe failed, using fallback spawn", e);
            resolvedSpawn = null;
        }

        if (resolvedSpawn == null && allowTerrainFallback) {
            LOGGER.warn(
                    "[Latitude] Could not find a validated biome-targeted spawn for zone={} targetZ={}; trying bounded terrain-safe fallback columns.",
                    zoneId,
                    targetZ);
            BlockPos fallback = findSafeFallbackSpawn(world, radius, targetZ, prepareTeleportNeighbors);
            if (fallback != null) {
                resolvedSpawn = new ResolvedSpawn(fallback, true);
            }
        }
        if (resolvedSpawn == null) {
            throw new IllegalStateException(
                    "No terrain-validated Latitude spawn was available for zone="
                            + zoneId
                            + " targetZ="
                            + targetZ);
        }

        int xRadius = LatitudeBiomes.getActiveXRadiusBlocks() > 0
                ? LatitudeBiomes.getActiveXRadiusBlocks()
                : radius;
        return new SpawnChoice(
                zoneId,
                clampSpawnAwayFromEwWarning(resolvedSpawn.pos(), xRadius),
                radius,
                resolvedSpawn.terrainValidated());
    }

    /**
     * Keep spawn a safe FRACTION inside the E/W sandstorm onset, not just a fixed block margin -- the onset
     * scales with world width, so a fixed margin fails on every size (it left spawn AT the onset on a Tiny
     * world and deep inside the storm on a large one). Takes the more restrictive of the fractional-safe cap
     * and the legacy fixed cap, and only ever moves X.
     */
    private static BlockPos clampSpawnAwayFromEwWarning(BlockPos spawnPos, int xRadiusBlocks) {
        if (spawnPos == null || xRadiusBlocks <= 0) {
            return spawnPos;
        }
        int absX = Math.abs(spawnPos.getX());
        double safeFrac = Math.max(0.0,
                com.example.globe.util.LatitudeMath.POLAR_STAGE_1_PROGRESS - EW_SPAWN_SAFE_MARGIN_FRAC);
        int fractionalSafe = (int) Math.round(safeFrac * xRadiusBlocks);
        int fixedSafe = Math.max(0, xRadiusBlocks - EW_WARNING_DISTANCE_BLOCKS - EW_SPAWN_PADDING_BLOCKS);
        int safeMaxAbsX = Math.min(fractionalSafe, fixedSafe);
        if (absX <= safeMaxAbsX) {
            return spawnPos;
        }
        int clampedX = spawnPos.getX() >= 0 ? safeMaxAbsX : -safeMaxAbsX;
        return new BlockPos(clampedX, spawnPos.getY(), spawnPos.getZ());
    }

    public static void logBuildMetadata(String side) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(MOD_ID);
        String version = mod.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?");
        String commit = "?";
        String branch = "?";
        String time = "?";
        String dirty = "?";

        if (mod.isPresent()) {
            try (InputStream is = mod.get().findPath("META-INF/MANIFEST.MF").map(path -> {
                try {
                    return java.nio.file.Files.newInputStream(path);
                } catch (Exception e) {
                    return null;
                }
            }).orElse(null)) {
                if (is != null) {
                    Manifest mf = new Manifest(is);
                    Attributes attrs = mf.getMainAttributes();
                    commit = Optional.ofNullable(attrs.getValue("Git-Commit")).orElse(commit);
                    branch = Optional.ofNullable(attrs.getValue("Git-Branch")).orElse(branch);
                    time = Optional.ofNullable(attrs.getValue("Build-Time")).orElse(time);
                    dirty = Optional.ofNullable(attrs.getValue("Build-Dirty")).orElse(dirty);
                }
            } catch (Exception ignored) {
            }
        }

        // A dev run has no packaged manifest, so every field above stays "?" and the marker cannot
        // name the source it was built from. Loom passes the same identity as system properties;
        // a release jar carries the manifest and never sees them, so the manifest still wins.
        commit = devBuildProperty(commit, "latitude.dev.gitCommit");
        branch = devBuildProperty(branch, "latitude.dev.gitBranch");
        dirty = devBuildProperty(dirty, "latitude.dev.buildDirty");
        time = devBuildProperty(time, "latitude.dev.buildTime");

        LOGGER.info("[LAT][BUILD] side={} version={} commit={} branch={} dirty={} time={}", side, version, commit, branch, dirty, time);
    }

    private static String devBuildProperty(String current, String key) {
        if (!"?".equals(current)) {
            return current;
        }
        String value = System.getProperty(key);
        return value == null || value.isBlank() ? current : value.trim();
    }

    private static ResolvedSpawn findLandSpawn(ServerLevel world, SamplerTemplate template,
                                               Climate.Sampler sampler,
                                               RandomState noiseConfig,
                                               int borderHalf, int targetZ, long seed,
                                               int terrainValidationBudget,
                                               boolean prepareTeleportNeighbors) {
        final int margin = 320;
        int searchMaxAbsX = SpawnSafetyPolicy.safeSearchMaxAbsX(
                borderHalf,
                margin,
                EW_WARNING_DISTANCE_BLOCKS,
                EW_SPAWN_PADDING_BLOCKS);
        // Slice C-2 (TEST 27 finding 1b): when the Phase 4 terrain bias is actively shaping terrain, never
        // even SEARCH for spawn inside the projection edge band (|x| >= EDGE_START * xRadius = the outer
        // 20%). Geography deliberately ramps that band to ocean-intent, so its biased terrain is carved --
        // but the land test below is BIOME-driven (the old map still paints land biomes there while
        // biomeConsumerV2 is off), which is exactly how the first live run spawned at |x| = 0.86 * xRadius
        // inside a shattered column. The post-hoc clamp below (~0.9 * xRadius) is not tight enough on its own.
        if (LatitudeBiomes.terrainBiasActivelyBiasing()) {
            int xRadius = LatitudeBiomes.getActiveXRadiusBlocks() > 0
                    ? LatitudeBiomes.getActiveXRadiusBlocks()
                    : borderHalf;
            int edgeSafeX = (int) Math.floor(
                    com.example.globe.core.geo.GeoAuthorityParams.EDGE_START * xRadius) - 64;
            searchMaxAbsX = Math.max(0, Math.min(searchMaxAbsX, edgeSafeX));
        }
        final int maxAbsX = searchMaxAbsX;
        final int maxAbsZ = Math.max(0, borderHalf - margin);

        final int samplesPerPass = 16;
        final int zJitter = 96;

        // Size-invariance: active radius is source of truth, borderHalf is fallback only.
        int radiusBlocks = LatitudeBiomes.getActiveRadiusBlocks();
        if (radiusBlocks <= 0) radiusBlocks = borderHalf;
        int classifyY = LatitudeBiomes.SURFACE_CLASSIFY_Y;
        LatitudeBiomeSource painted = paintedSpawnView(world, template, noiseConfig, radiusBlocks);

        LatitudeBiomes.setWorldSeed(seed);

        RandomSource rng = RandomSource.create(seed ^ 0x9E3779B97F4A7C15L ^ (long) targetZ);
        int terrainValidationAttempts = 0;

        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < samplesPerPass; i++) {
                int x = rng.nextIntBetweenInclusive(-maxAbsX, maxAbsX);
                int z = pass == 0
                        ? targetZ
                        : Mth.clamp(
                                targetZ + rng.nextIntBetweenInclusive(-zJitter, zJitter),
                                -maxAbsZ,
                                maxAbsZ);

                if (!isLandBiome(painted, sampler, x, z, classifyY)) {
                    continue;
                }

                if (terrainValidationBudget <= 0) {
                    // This is a biome-correct suggestion, not a claimed safe surface. Minecraft
                    // 26.2's asynchronous PrepareSpawnTask performs the final collision/liquid
                    // search around it before the player is created. No remote chunk is loaded here.
                    return new ResolvedSpawn(
                            new BlockPos(x, world.getSeaLevel() + 1, z),
                            false);
                }
                if (terrainValidationAttempts >= Math.max(0, terrainValidationBudget)) {
                    return null;
                }
                terrainValidationAttempts++;
                BlockPos candidate = placeSafeY(world, x, z, prepareTeleportNeighbors);
                if (candidate != null) {
                    return new ResolvedSpawn(candidate, true);
                }
            }
        }
        return null;
    }

    /**
     * Bounded last-resort search used after both a normal no-candidate result and a biome-probe
     * exception. It may relax the requested biome family, but never the physical safety checks.
     */
    private static BlockPos findSafeFallbackSpawn(
            ServerLevel world,
            int borderHalf,
            int targetZ,
            boolean prepareTeleportNeighbors) {
        final int terrainMargin = 320;
        for (SpawnSafetyPolicy.FallbackCandidate candidate :
                SpawnSafetyPolicy.safeFallbackCandidates(
                        borderHalf,
                        targetZ,
                        terrainMargin,
                        EW_WARNING_DISTANCE_BLOCKS,
                        EW_SPAWN_PADDING_BLOCKS,
                        SpawnSafetyPolicy.FALLBACK_STEP_BLOCKS,
                        SpawnSafetyPolicy.FALLBACK_MAX_RINGS)) {
            try {
                BlockPos validated = placeSafeY(
                        world,
                        candidate.x(),
                        candidate.z(),
                        prepareTeleportNeighbors);
                if (validated != null) {
                    return validated;
                }
            } catch (RuntimeException e) {
                LOGGER.warn(
                        "[Latitude] Safe fallback terrain probe failed at x={} z={}; continuing bounded search.",
                        candidate.x(),
                        candidate.z(),
                        e);
            }
        }
        return null;
    }

    /**
     * The spawn search judges a candidate column through the same painted, terrain-aware biome
     * view that chunk population and structure siting use, so "land" here means the biome the
     * player will actually stand in. A bare pick without terrain evidence disagreed with that view
     * at coasts and on raised ground, where the painter turns a donor ocean label into land or a
     * low land label into ocean, so the search could accept a column that generates as water.
     */
    private static LatitudeBiomeSource paintedSpawnView(ServerLevel world,
                                                        SamplerTemplate template,
                                                        RandomState noiseConfig,
                                                        int radiusBlocks) {
        ChunkGenerator generator = world.getChunkSource().getGenerator();
        if (!(generator instanceof NoiseBasedChunkGenerator terrainGenerator)) {
            throw new IllegalStateException("Spawn search requires a NoiseChunkGenerator");
        }
        return LatitudeBiomeSource.forLocate(
                template.baseSource(),
                template.biomeRegistry(),
                radiusBlocks,
                terrainGenerator,
                noiseConfig,
                world);
    }

    /**
     * Pure biome probe — no chunk generation. Returns true if the painted biome at
     * (blockX, blockZ) is land (not ocean or river).
     */
    private static boolean isLandBiome(LatitudeBiomeSource painted,
                                        Climate.Sampler sampler,
                                        int blockX, int blockZ,
                                        int classifyY) {
        Holder<Biome> resolved = painted.getNoiseBiome(
                Math.floorDiv(blockX, 4),
                Math.floorDiv(classifyY, 4),
                Math.floorDiv(blockZ, 4),
                sampler);

        // Tag-based checks — safe against substring false positives
        return !resolved.is(BiomeTags.IS_OCEAN) && !resolved.is(BiomeTags.IS_RIVER);
    }

    /**
     * Generates exactly one candidate chunk to get a safe spawn Y via heightmap. Only after the
     * column passes every safety check does it prepare the eight neighboring teleport chunks.
     * Returns a valid spawn BlockPos, or null if the terrain fails validation.
     */
    /** Package entry for the hemisphere-passage service; the validator itself stays private. */
    static BlockPos placeSafeYForPassage(ServerLevel world, int x, int z, boolean prepareTeleportNeighbors) {
        return placeSafeY(world, x, z, prepareTeleportNeighbors);
    }

    private static BlockPos placeSafeY(
            ServerLevel world,
            int x,
            int z,
            boolean prepareTeleportNeighbors) {
        loadSpawnTargetChunk(world, x, z);

        BlockPos spawn = world.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(x, world.getMinY(), z));
        BlockPos ground = spawn.below();

        // Same validation as the old tryLandAt
        if (!world.getFluidState(spawn).isEmpty()) return null;
        if (!world.getFluidState(spawn.above()).isEmpty()) return null;
        if (!world.getBlockState(spawn).isAir()) return null;
        if (!world.getBlockState(spawn.above()).isAir()) return null;
        if (!world.getFluidState(ground).isEmpty()) return null;
        BlockState groundState = world.getBlockState(ground);
        Identifier groundBlockId = BuiltInRegistries.BLOCK.getKey(groundState.getBlock());
        if (groundBlockId == null
                || SpawnSafetyPolicy.isDangerousSurfaceId(groundBlockId.toString())) {
            return null;
        }
        if (!groundState.isFaceSturdy(world, ground, Direction.UP)) return null;

        int loadedNeighborChunks = prepareTeleportNeighbors
                ? loadSpawnTargetNeighborRing(world, x, z)
                : 0;
        LOGGER.info("[Latitude] Prepared spawn target surface: x={} y={} z={} loadedTeleportChunks={}",
                x, spawn.getY(), z, loadedNeighborChunks + 1);
        return spawn;
    }

    private static void loadSpawnTargetChunk(ServerLevel world, int x, int z) {
        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);
        world.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    private static int loadSpawnTargetNeighborRing(ServerLevel world, int x, int z) {
        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);
        int loadedChunks = 0;
        int radius = SpawnSafetyPolicy.SPAWN_PREPARATION_NEIGHBOR_RADIUS_CHUNKS;
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                world.getChunkSource().getChunk(chunkX + dx, chunkZ + dz, ChunkStatus.FULL, true);
                loadedChunks++;
            }
        }
        return loadedChunks;
    }

    private record ResolvedSpawn(BlockPos pos, boolean terrainValidated) {
    }

    private record SpawnChoice(String zoneId, BlockPos pos, int radius, boolean terrainValidated) {
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double hash01(long seed, int x, int z, long salt) {
        long h = seed ^ salt;
        h ^= (long) x * 0x9E3779B97F4A7C15L;
        h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        h ^= (h >>> 27);
        h *= 0x3C79AC492BA7B653L;
        h ^= (h >>> 33);
        return ((h >>> 11) * (1.0 / (1L << 53)));
    }

    private static String resolveSpawnZoneId(String selected, long seed) {
        if (selected == null || !selected.equals("RANDOM")) {
            return selected;
        }

        String[] options = {"EQUATOR", "TROPICAL", "SUBTROPICAL", "TEMPERATE", "SUBPOLAR", "POLAR"};
        long mixed = seed ^ 0x9E3779B97F4A7C15L;
        int idx = Math.floorMod(mixed, options.length);
        return options[idx];
    }

    private static boolean hasCompassAnywhere(ServerPlayer player) {
        if (player == null) return false;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (containsCompass(inv.getItem(i), 0)) return true;
        }
        return false;
    }

    private static boolean containsCompass(ItemStack stack, int depth) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(Items.COMPASS)) return true;

        if (depth >= 6) return false;

        if (stack.is(Items.BUNDLE)) {
            BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
            if (contents != null) {
                for (var inside : contents.items()) {
                    if (containsCompass(inside.create(), depth + 1)) return true;
                }
            }
        }

        return false;
    }
}
