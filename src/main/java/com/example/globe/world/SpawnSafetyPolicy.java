package com.example.globe.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Dependency-free policy for initial-spawn coordinates and hazardous ground.
 */
public final class SpawnSafetyPolicy {
    public static final int FALLBACK_STEP_BLOCKS = 256;
    public static final int FALLBACK_MAX_RINGS = 1;
    public static final int SPAWN_PREPARATION_NEIGHBOR_RADIUS_CHUNKS = 1;
    public static final int COMPACT_PROBE_STEP_BLOCKS = 32;
    public static final int COMPACT_PROBE_X_HALF_SPAN_BLOCKS = 256;
    public static final int COMPACT_PROBE_Z_HALF_SPAN_BLOCKS = 64;
    /** Adjacent lattices along the latitude row share one edge column. */
    public static final int COMPACT_LATTICE_STRIDE_BLOCKS = COMPACT_PROBE_X_HALF_SPAN_BLOCKS * 2;
    /** Chunk-free probes per lattice; the row-wide search classifies at most this many per lattice. */
    public static final int MAX_COMPACT_PROBES = 85;
    public static final int MAX_VALIDATION_CANDIDATES = 9;

    public record FallbackCandidate(int x, int z) {
    }

    /**
     * @param probes all unique chunk-free probes that were classified
     * @param validationCandidates the ordered cohort production may terrain-validate
     * @param landCandidateCount how many of the cohort's candidates classified as land
     * @param landProbeCount how many of all probes classified as land
     * @param terrainOnlyFallback true when classification was unavailable and the cohort is central
     */
    public record CompactCohort(
            List<FallbackCandidate> probes,
            List<FallbackCandidate> validationCandidates,
            int landCandidateCount,
            int landProbeCount,
            boolean terrainOnlyFallback) {
        public CompactCohort {
            probes = List.copyOf(probes);
            validationCandidates = List.copyOf(validationCandidates);
        }
    }

    private record WindowChoice(
            List<FallbackCandidate> candidates,
            int landCount,
            long centerDistanceSquared,
            int centerX,
            int centerZ) {
    }

    private SpawnSafetyPolicy() {
    }

    /**
     * Returns the largest absolute X that is both inside the terrain-search margin and outside
     * Latitude's east/west warning zone. Sampling inside this bound avoids moving a validated
     * spawn to a different, unvalidated terrain column afterward.
     */
    public static int safeSearchMaxAbsX(
            int borderHalf,
            int terrainMargin,
            int warningDistance,
            int warningPadding) {
        int terrainLimit = Math.max(0, borderHalf - Math.max(0, terrainMargin));
        int warningLimit = Math.max(
                0,
                borderHalf - Math.max(0, warningDistance) - Math.max(0, warningPadding));
        return Math.min(terrainLimit, warningLimit);
    }

    /**
     * Produces a finite, deterministic search around the requested latitude. Every coordinate is
     * already inside both the terrain margin and the east/west warning margin; production must
     * still validate the actual terrain column before accepting it.
     */
    public static List<FallbackCandidate> safeFallbackCandidates(
            int borderHalf,
            int targetZ,
            int terrainMargin,
            int warningDistance,
            int warningPadding,
            int stepBlocks,
            int maxRings) {
        int maxAbsX = safeSearchMaxAbsX(
                borderHalf,
                terrainMargin,
                warningDistance,
                warningPadding);
        int maxAbsZ = Math.max(0, borderHalf - Math.max(0, terrainMargin));
        int centerZ = Math.max(-maxAbsZ, Math.min(maxAbsZ, targetZ));
        int step = Math.max(1, stepBlocks);
        int rings = Math.max(0, maxRings);

        LinkedHashSet<FallbackCandidate> candidates = new LinkedHashSet<>();
        candidates.add(new FallbackCandidate(0, centerZ));
        for (int ring = 1; ring <= rings; ring++) {
            long rawOffset = (long) ring * step;
            int xOffset = (int) Math.min(maxAbsX, rawOffset);
            int northZ = (int) Math.min(maxAbsZ, (long) centerZ + rawOffset);
            int southZ = (int) Math.max(-maxAbsZ, (long) centerZ - rawOffset);

            candidates.add(new FallbackCandidate(xOffset, centerZ));
            candidates.add(new FallbackCandidate(-xOffset, centerZ));
            candidates.add(new FallbackCandidate(0, northZ));
            candidates.add(new FallbackCandidate(0, southZ));
            candidates.add(new FallbackCandidate(xOffset, northZ));
            candidates.add(new FallbackCandidate(-xOffset, northZ));
            candidates.add(new FallbackCandidate(xOffset, southZ));
            candidates.add(new FallbackCandidate(-xOffset, southZ));
        }
        return List.copyOf(candidates);
    }

    /**
     * Uses chunk-free biome probes to select one compact 3x3 terrain-validation cohort. Each
     * deterministic probe lattice spans X +/-256 and Z +/-64 around a centre on the requested
     * latitude in 32-block steps. Coordinates are clamped and deduplicated inside the existing
     * terrain and warning bounds before the callback is invoked.
     *
     * <p>The first lattice is centred on x=0. If it holds no window whose nine probes all classify
     * as land, the next lattices out along the same latitude row are classified, east and west
     * in step, until one does or the row is exhausted. The nearest fully-land window wins; when
     * the whole row has none, the land-richest window nearest the centre does. A row the
     * classifier sees as ocean end to end was the usual reason the nine terrain probes all failed
     * and the world fell back to vanilla's equator spawn.
     */
    public static CompactCohort compactValidationCohort(
            int borderHalf,
            int targetZ,
            int terrainMargin,
            int warningDistance,
            int warningPadding,
            BiPredicate<Integer, Integer> isLandProbe) {
        if (isLandProbe == null) {
            return centralCompactValidationCohort(
                    borderHalf, targetZ, terrainMargin, warningDistance, warningPadding);
        }

        int maxAbsX = safeSearchMaxAbsX(
                borderHalf,
                terrainMargin,
                warningDistance,
                warningPadding);
        int maxAbsZ = Math.max(0, borderHalf - Math.max(0, terrainMargin));
        int centerZ = clamp(targetZ, -maxAbsZ, maxAbsZ);

        LinkedHashSet<FallbackCandidate> uniqueProbes = new LinkedHashSet<>();
        Map<FallbackCandidate, Boolean> landByProbe = new LinkedHashMap<>();
        WindowChoice best = null;
        int ringsPerSide = compactRingsPerSide(maxAbsX);
        try {
            for (int ring = 0; ring <= ringsPerSide; ring++) {
                int[] centres = ring == 0
                        ? new int[] {0}
                        : new int[] {ring * COMPACT_LATTICE_STRIDE_BLOCKS, -ring * COMPACT_LATTICE_STRIDE_BLOCKS};
                for (int centerX : centres) {
                    FallbackCandidate[][] lattice = compactLattice(
                            centerX, centerZ, maxAbsX, maxAbsZ, uniqueProbes);
                    for (FallbackCandidate[] column : lattice) {
                        for (FallbackCandidate probe : column) {
                            if (!landByProbe.containsKey(probe)) {
                                landByProbe.put(probe, isLandProbe.test(probe.x(), probe.z()));
                            }
                        }
                    }
                    WindowChoice latticeBest = bestWindow(lattice, landByProbe, centerZ);
                    if (isBetterWindow(latticeBest, best)) {
                        best = latticeBest;
                    }
                }
                if (best != null && best.landCount() == MAX_VALIDATION_CANDIDATES) {
                    break;
                }
            }
        } catch (RuntimeException classificationFailure) {
            return centralCompactValidationCohort(
                    borderHalf, targetZ, terrainMargin, warningDistance, warningPadding);
        }

        if (best == null) {
            return centralCompactValidationCohort(
                    borderHalf, targetZ, terrainMargin, warningDistance, warningPadding);
        }

        List<FallbackCandidate> ordered = new ArrayList<>(best.candidates());
        ordered.sort(Comparator
                .comparing((FallbackCandidate candidate) ->
                        !Boolean.TRUE.equals(landByProbe.get(candidate)))
                .thenComparingLong(candidate -> distanceSquared(candidate, 0, centerZ))
                .thenComparingInt(FallbackCandidate::x)
                .thenComparingInt(FallbackCandidate::z));
        if (ordered.size() > MAX_VALIDATION_CANDIDATES) {
            ordered = new ArrayList<>(ordered.subList(0, MAX_VALIDATION_CANDIDATES));
        }
        int orderedLandCount = 0;
        for (FallbackCandidate candidate : ordered) {
            if (Boolean.TRUE.equals(landByProbe.get(candidate))) {
                orderedLandCount++;
            }
        }
        int landProbeCount = 0;
        for (Boolean land : landByProbe.values()) {
            if (Boolean.TRUE.equals(land)) {
                landProbeCount++;
            }
        }
        return new CompactCohort(
                List.copyOf(uniqueProbes),
                ordered,
                orderedLandCount,
                landProbeCount,
                false);
    }

    /**
     * Central terrain-only cohort used when biome sampler setup or classification is unavailable.
     */
    public static CompactCohort centralCompactValidationCohort(
            int borderHalf,
            int targetZ,
            int terrainMargin,
            int warningDistance,
            int warningPadding) {
        int maxAbsX = safeSearchMaxAbsX(
                borderHalf,
                terrainMargin,
                warningDistance,
                warningPadding);
        int maxAbsZ = Math.max(0, borderHalf - Math.max(0, terrainMargin));
        int centerZ = clamp(targetZ, -maxAbsZ, maxAbsZ);
        LinkedHashSet<FallbackCandidate> candidates = new LinkedHashSet<>();
        for (int xOffset = -COMPACT_PROBE_STEP_BLOCKS;
                xOffset <= COMPACT_PROBE_STEP_BLOCKS;
                xOffset += COMPACT_PROBE_STEP_BLOCKS) {
            for (int zOffset = -COMPACT_PROBE_STEP_BLOCKS;
                    zOffset <= COMPACT_PROBE_STEP_BLOCKS;
                    zOffset += COMPACT_PROBE_STEP_BLOCKS) {
                candidates.add(new FallbackCandidate(
                        clamp(xOffset, -maxAbsX, maxAbsX),
                        clamp(centerZ + zOffset, -maxAbsZ, maxAbsZ)));
            }
        }
        List<FallbackCandidate> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator
                .comparingLong((FallbackCandidate candidate) ->
                        distanceSquared(candidate, 0, centerZ))
                .thenComparingInt(FallbackCandidate::x)
                .thenComparingInt(FallbackCandidate::z));
        return new CompactCohort(List.of(), ordered, 0, 0, true);
    }

    /**
     * Applies at most nine expensive validators in deterministic cohort order and stops at the
     * first accepted result. A null validator result rejects that coordinate.
     */
    public static <T> Optional<T> firstValidatedCandidate(
            List<FallbackCandidate> candidates,
            Function<FallbackCandidate, T> validator) {
        if (candidates == null || validator == null) {
            return Optional.empty();
        }
        int limit = Math.min(MAX_VALIDATION_CANDIDATES, candidates.size());
        for (int index = 0; index < limit; index++) {
            T accepted = validator.apply(candidates.get(index));
            if (accepted != null) {
                return Optional.of(accepted);
            }
        }
        return Optional.empty();
    }

    /**
     * How many lattices the row-wide search may classify before it has covered every X inside
     * the search bound: the central one plus one per stride on each side. Multiplied by
     * {@link #MAX_COMPACT_PROBES} this bounds the chunk-free probe count of one initial spawn.
     */
    public static int compactLatticeCount(int maxAbsX) {
        return 1 + 2 * compactRingsPerSide(maxAbsX);
    }

    private static int compactRingsPerSide(int maxAbsX) {
        int reach = Math.max(0, maxAbsX) - COMPACT_PROBE_X_HALF_SPAN_BLOCKS;
        if (reach <= 0) {
            return 0;
        }
        return (int) Math.ceil(reach / (double) COMPACT_LATTICE_STRIDE_BLOCKS);
    }

    private static FallbackCandidate[][] compactLattice(
            int centerX,
            int centerZ,
            int maxAbsX,
            int maxAbsZ,
            LinkedHashSet<FallbackCandidate> uniqueProbes) {
        int xCount = COMPACT_PROBE_X_HALF_SPAN_BLOCKS * 2 / COMPACT_PROBE_STEP_BLOCKS + 1;
        int zCount = COMPACT_PROBE_Z_HALF_SPAN_BLOCKS * 2 / COMPACT_PROBE_STEP_BLOCKS + 1;
        FallbackCandidate[][] lattice = new FallbackCandidate[xCount][zCount];
        for (int xIndex = 0; xIndex < xCount; xIndex++) {
            int rawX = centerX - COMPACT_PROBE_X_HALF_SPAN_BLOCKS
                    + xIndex * COMPACT_PROBE_STEP_BLOCKS;
            int x = clamp(rawX, -maxAbsX, maxAbsX);
            for (int zIndex = 0; zIndex < zCount; zIndex++) {
                int rawZ = centerZ - COMPACT_PROBE_Z_HALF_SPAN_BLOCKS
                        + zIndex * COMPACT_PROBE_STEP_BLOCKS;
                int z = clamp(rawZ, -maxAbsZ, maxAbsZ);
                FallbackCandidate probe = new FallbackCandidate(x, z);
                lattice[xIndex][zIndex] = probe;
                uniqueProbes.add(probe);
            }
        }
        return lattice;
    }

    private static WindowChoice bestWindow(
            FallbackCandidate[][] lattice,
            Map<FallbackCandidate, Boolean> landByProbe,
            int centerZ) {
        WindowChoice best = null;
        int xCount = lattice.length;
        int zCount = lattice[0].length;
        for (int xStart = 0; xStart <= xCount - 3; xStart++) {
            for (int zStart = 0; zStart <= zCount - 3; zStart++) {
                LinkedHashSet<FallbackCandidate> window = new LinkedHashSet<>();
                for (int xOffset = 0; xOffset < 3; xOffset++) {
                    for (int zOffset = 0; zOffset < 3; zOffset++) {
                        window.add(lattice[xStart + xOffset][zStart + zOffset]);
                    }
                }
                List<FallbackCandidate> windowCandidates = List.copyOf(window);
                int landCount = 0;
                for (FallbackCandidate candidate : windowCandidates) {
                    if (Boolean.TRUE.equals(landByProbe.get(candidate))) {
                        landCount++;
                    }
                }
                FallbackCandidate windowCenter = lattice[xStart + 1][zStart + 1];
                WindowChoice choice = new WindowChoice(
                        windowCandidates,
                        landCount,
                        distanceSquared(windowCenter, 0, centerZ),
                        windowCenter.x(),
                        windowCenter.z());
                if (isBetterWindow(choice, best)) {
                    best = choice;
                }
            }
        }
        return best;
    }

    private static boolean isBetterWindow(WindowChoice candidate, WindowChoice current) {
        if (current == null) {
            return true;
        }
        if (candidate.landCount() != current.landCount()) {
            return candidate.landCount() > current.landCount();
        }
        if (candidate.centerDistanceSquared() != current.centerDistanceSquared()) {
            return candidate.centerDistanceSquared() < current.centerDistanceSquared();
        }
        if (candidate.centerX() != current.centerX()) {
            return candidate.centerX() < current.centerX();
        }
        return candidate.centerZ() < current.centerZ();
    }

    private static long distanceSquared(FallbackCandidate candidate, int centerX, int centerZ) {
        long dx = (long) candidate.x() - centerX;
        long dz = (long) candidate.z() - centerZ;
        return dx * dx + dz * dz;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    /**
     * Worst-case synchronous FULL-chunk requests when each candidate loads only its own chunk for
     * validation and the accepted candidate then loads the surrounding neighbor ring once.
     */
    public static int maximumFallbackChunkLoadCalls(
            int candidateCount,
            int preparationNeighborRadiusChunks) {
        int candidates = Math.max(0, candidateCount);
        int radius = Math.max(0, preparationNeighborRadiusChunks);
        int side = radius * 2 + 1;
        int neighborChunks = Math.max(0, side * side - 1);
        return candidates + neighborChunks;
    }

    /**
     * True for blocks that are intrinsically unsafe directly beneath a newly spawned player.
     * Unknown provider blocks fail open; production separately requires a sturdy upper face.
     */
    public static boolean isDangerousSurfaceId(String blockId) {
        if (blockId == null) {
            return false;
        }
        return switch (blockId.toLowerCase(Locale.ROOT)) {
            case "minecraft:magma_block",
                    "minecraft:cactus",
                    "minecraft:powder_snow",
                    "minecraft:campfire",
                    "minecraft:soul_campfire",
                    "minecraft:pointed_dripstone",
                    "minecraft:fire",
                    "minecraft:soul_fire",
                    "minecraft:wither_rose",
                    "minecraft:sweet_berry_bush" -> true;
            default -> false;
        };
    }
}
