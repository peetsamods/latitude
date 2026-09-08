package com.example.globe.dev;

import com.example.globe.GlobeMod;
import com.example.globe.GlobePending;
import com.example.globe.util.LatitudeMath;
import com.example.globe.world.LatitudeBiomes;
import com.example.globe.world.SpawnSafetyPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dev-only headless job that reproduces and censuses Latitude's initial-spawn choice.
 *
 * <p>Lives in the excluded {@code com.example.globe.dev} package, so nothing here ships.
 * Armed by {@code -Dlatdev.spawnProbe=enabled=true;zone=SUBTROPICAL;zones=...;seeds=1-40;out=...}
 * and reached only through {@link BiomePreviewHeadlessRunner#register()} in a development
 * environment.
 *
 * <p>Three things happen, in order, on the server thread once the world is up:
 * <ol>
 *   <li>{@code zone=} was pushed into {@link GlobePending} at mod init, so the dedicated server
 *       ran the real create-world initial-spawn path (the mixin on {@code setInitialSpawn}). The
 *       job reports where the world's spawn ended up and which zone that latitude belongs to.</li>
 *   <li>For every zone in {@code zones=} the real resolver is run again on this world's seed,
 *       loading at most the nine cohort chunks each time, and the outcome recorded.</li>
 *   <li>For every seed in {@code seeds=} and every zone, only the chunk-free plan is built, so the
 *       cohort policy can be censused across many seeds without generating a world per seed.</li>
 * </ol>
 * The per-column rejection reasons are production log lines; this job only frames them.
 */
public final class SpawnProbeHeadlessJob {
    static final String PROP_KEY = "latdev.spawnProbe";
    private static final String TAG = "[latdev][spawn-probe]";
    private static final List<String> ALL_ZONES =
            List.of("EQUATOR", "TROPICAL", "SUBTROPICAL", "TEMPERATE", "SUBPOLAR", "POLAR");

    private SpawnProbeHeadlessJob() {
    }

    record Config(boolean enabled, String pendingZone, List<String> zones, List<Long> seeds, Path outDir) {
    }

    static Config parseConfig() {
        String prop = System.getProperty(PROP_KEY, "");
        if (prop == null || prop.isBlank()) {
            return new Config(false, null, List.of(), List.of(), null);
        }
        Map<String, String> kv = new LinkedHashMap<>();
        for (String part : prop.split(";")) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            kv.put(part.substring(0, eq).trim().toLowerCase(Locale.ROOT), part.substring(eq + 1).trim());
        }
        boolean enabled = !"false".equalsIgnoreCase(kv.getOrDefault("enabled", "true"));
        String pendingZone = normalizeZone(kv.get("zone"));
        List<String> zones = new ArrayList<>();
        String zonesRaw = kv.get("zones");
        if (zonesRaw == null || zonesRaw.isBlank()) {
            zones.addAll(ALL_ZONES);
        } else {
            for (String zone : zonesRaw.split(",")) {
                String normalized = normalizeZone(zone);
                if (normalized != null && !zones.contains(normalized)) {
                    zones.add(normalized);
                }
            }
        }
        List<Long> seeds = parseSeeds(kv.get("seeds"));
        Path outDir = kv.containsKey("out") && !kv.get("out").isBlank()
                ? Path.of(kv.get("out")).toAbsolutePath().normalize()
                : null;
        return new Config(enabled, pendingZone, List.copyOf(zones), seeds, outDir);
    }

    /**
     * Mod-init hook: the create screen normally pushes the chosen zone into {@link GlobePending}
     * before the integrated server loads the world. A dedicated server has no screen, so the job
     * pushes it here, before {@code MinecraftServer.createLevels} runs {@code setInitialSpawn}.
     */
    static void armPendingZone() {
        Config config = parseConfig();
        if (!config.enabled() || config.pendingZone() == null) {
            return;
        }
        GlobePending.set(config.pendingZone());
        GlobeMod.LOGGER.info("{} armed pending initial-spawn zone={}", TAG, config.pendingZone());
    }

    static void runAndStop(MinecraftServer server, Config config) {
        ServerLevel world = server.overworld();
        if (world == null) {
            GlobeMod.LOGGER.error("{} no overworld available; stopping server", TAG);
            server.halt(false);
            return;
        }
        long worldSeed = world.getSeed();
        try {
            int radius = LatitudeBiomes.getActiveRadiusBlocks();
            List<String> report = new ArrayList<>();

            BlockPos worldSpawn = world.getSharedSpawnPos();
            String spawnZone = radius > 0
                    ? LatitudeMath.zoneForRadius(radius, worldSpawn.getZ()).name()
                    : "?";
            String worldLine = String.format(Locale.ROOT,
                    "world spawn: seed=%d radius=%d requestedZone=%s x=%d y=%d z=%d zoneAtSpawn=%s",
                    worldSeed, radius, config.pendingZone(), worldSpawn.getX(), worldSpawn.getY(),
                    worldSpawn.getZ(), spawnZone);
            GlobeMod.LOGGER.info("{} {}", TAG, worldLine);
            report.add(worldLine);
            report.add("");
            report.add("validated census (this world's seed, real chunk loads, at most nine per zone)");
            report.add("zone\ttargetZ\tlandProbes\tcohortLand\toutcome\tx\ty\tz\tzoneAtResult");

            for (String zone : config.zones()) {
                GlobeMod.InitialSpawnPlan plan = GlobeMod.planInitialSpawn(world, zone, worldSeed);
                SpawnSafetyPolicy.CompactCohort cohort = plan.cohort();
                GlobeMod.LOGGER.info("{} validate zone={} seed={} radius={} targetZ={} landProbes={}/{} cohortLand={}/{}",
                        TAG, plan.zoneId(), worldSeed, plan.radius(), plan.targetZ(),
                        cohort.landProbeCount(), cohort.probes().size(),
                        cohort.landCandidateCount(), cohort.validationCandidates().size());
                String outcome;
                String coords = "\t\t";
                String zoneAtResult = "";
                try {
                    BlockPos pos = GlobeMod.probeInitialSpawn(world, zone);
                    outcome = "PLACED";
                    coords = pos.getX() + "\t" + pos.getY() + "\t" + pos.getZ();
                    zoneAtResult = plan.radius() > 0
                            ? LatitudeMath.zoneForRadius(plan.radius(), pos.getZ()).name()
                            : "?";
                } catch (GlobeMod.InitialSpawnSelectionException exhausted) {
                    outcome = "EXHAUSTED";
                }
                GlobeMod.LOGGER.info("{} result zone={} outcome={} {}", TAG, plan.zoneId(), outcome,
                        coords.replace('\t', ' ').trim());
                report.add(String.format(Locale.ROOT, "%s\t%d\t%d/%d\t%d/%d\t%s\t%s\t%s",
                        plan.zoneId(), plan.targetZ(),
                        cohort.landProbeCount(), cohort.probes().size(),
                        cohort.landCandidateCount(), cohort.validationCandidates().size(),
                        outcome, coords, zoneAtResult));
            }

            List<String> census = new ArrayList<>();
            if (!config.seeds().isEmpty()) {
                census.add("seed,zone,targetZ,landProbes,totalProbes,cohortLand,cohortSize,terrainOnly");
                long started = System.nanoTime();
                for (long seed : config.seeds()) {
                    for (String zone : config.zones()) {
                        GlobeMod.InitialSpawnPlan plan = GlobeMod.planInitialSpawn(world, zone, seed);
                        SpawnSafetyPolicy.CompactCohort cohort = plan.cohort();
                        census.add(String.format(Locale.ROOT, "%d,%s,%d,%d,%d,%d,%d,%s",
                                seed, plan.zoneId(), plan.targetZ(),
                                cohort.landProbeCount(), cohort.probes().size(),
                                cohort.landCandidateCount(), cohort.validationCandidates().size(),
                                cohort.terrainOnlyFallback()));
                    }
                }
                long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
                GlobeMod.LOGGER.info("{} chunk-free census: seeds={} zones={} rows={} durationMs={}",
                        TAG, config.seeds().size(), config.zones().size(), census.size() - 1, elapsedMs);
            }

            Path outDir = config.outDir() != null
                    ? config.outDir()
                    : server.getServerDirectory().toAbsolutePath().normalize().resolve("latdev");
            Files.createDirectories(outDir);
            Path reportFile = outDir.resolve(String.format(Locale.ROOT, "spawn-probe_seed%d_R%d.txt", worldSeed, radius));
            Files.writeString(reportFile, String.join("\n", report) + "\n", StandardCharsets.UTF_8);
            GlobeMod.LOGGER.info("{} wrote {}", TAG, reportFile);
            if (!census.isEmpty()) {
                Path censusFile = outDir.resolve(String.format(Locale.ROOT, "spawn-census_R%d.csv", radius));
                Files.writeString(censusFile, String.join("\n", census) + "\n", StandardCharsets.UTF_8);
                GlobeMod.LOGGER.info("{} wrote {}", TAG, censusFile);
            }
        } catch (Throwable t) {
            GlobeMod.LOGGER.error("{} spawn probe failed", TAG, t);
        } finally {
            LatitudeBiomes.setWorldSeed(worldSeed);
            GlobeMod.LOGGER.info("{} stopping server", TAG);
            server.halt(false);
        }
    }

    private static String normalizeZone(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String zone = raw.trim().toUpperCase(Locale.ROOT);
        return zone.equals("RANDOM") || ALL_ZONES.contains(zone) ? zone : null;
    }

    private static List<Long> parseSeeds(String raw) {
        List<Long> seeds = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        for (String token : raw.split(",")) {
            String part = token.trim();
            if (part.isEmpty()) {
                continue;
            }
            int dash = part.indexOf('-', 1);
            try {
                if (dash > 0) {
                    long from = Long.parseLong(part.substring(0, dash).trim());
                    long to = Long.parseLong(part.substring(dash + 1).trim());
                    for (long seed = Math.min(from, to); seed <= Math.max(from, to); seed++) {
                        seeds.add(seed);
                    }
                } else {
                    seeds.add(Long.parseLong(part));
                }
            } catch (NumberFormatException ignored) {
                GlobeMod.LOGGER.warn("{} ignoring unparseable seed token '{}'", TAG, part);
            }
        }
        return List.copyOf(seeds);
    }
}
