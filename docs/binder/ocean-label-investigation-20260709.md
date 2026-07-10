# Ocean-label family investigation — villages/dripstone/rivers in ocean (2026-07-09)

`status: INVESTIGATION COMPLETE — fix slice authorized by Peetsa's B-4 punch list; flag-gated build next`
From Peetsa's TEST 48 live look (flags off). Investigator: Opus, read-only, code + the two existing
20260709 atlas runs (113845 terrain-blind / 131833 terrain-aware). HEAD a1af3b63.

## Measured (110,215 cells, cross-tab blind→aware)
- land→ocean (flooded-label family): **52,285 cells (47.4%)**; land fraction 63.3%→13.1% vs ~39% geo
  intent (the known estimator over-flood).
- **Rivers NEVER relabeled: 7,469 in both runs, byte-identical; 4,979 (67%) fully surrounded by ocean.**

## Root causes (all CONFIRMED)
1. **VILLAGE IN OCEAN**: structure eligibility is vanilla, keyed on the biome source. LatitudeBiomeSource
   repaints via pick() context "SOURCE" (LatitudeBiomeSource.java:105-108) — which has NO terrain inputs
   (generator/noiseConfig/heightView null), so BOTH sunk-land vetoes (LatitudeBiomes.java:3100, :3155)
   never run there. Flooded columns keep their savanna/plains label → vanilla places villages. The
   existing floorSightedVeto flag CANNOT fix this (it sits behind hasPreviewTerrainInputs). Village start
   Y comes from fluid-inclusive/phantom-high estimators → generates in/on the water.
2. **DRIPSTONE IN TRENCHES**: the wrapper wraps only finalDensity + prelim surface
   (TerrainRouterWrapping.java:220-236); vanilla depth() is untouched, so 3D cave biomes stay where they
   were — the carve strips the covering rock and EXPOSES them. The surface-cave clamp
   (ChunkGeneratorPopulateBiomesMixin.java:303-320) measures "near surface" with WORLD_SURFACE_WG
   (reads waterline Y63) so trench-floor dripstone (~Y45) is judged deep → not clamped.
3. **RIVERS IN OPEN OCEAN**: the mirror veto explicitly excludes rivers (`!IS_RIVER`,
   LatitudeBiomes.java:3148) and the river branch (:3176) returns before the ocean branch — rivers can
   never convert, even where fully sunk.

## Fix shape (one shared signal, three touches)
**The honest oracle is `GeoTerrainBiasFunction.carveCeilYOrInfinity(x,z)` (:165-216)** — the carve's own
pure analytic target: no generator, no estimator, +Infinity on land-intent, shelfApron keeps coasts
shallow. Relabel a column ocean iff `carveCeilY < seaLevel - 2`. This: (a) works in the input-less
SOURCE path → villages stop being eligible over carved sea; (b) restores land fraction toward ~39%
intent (replaces the over-flooding getBaseHeight(OCEAN_FLOOR_WG) approach); (c) unifies atlas/live/
SOURCE (same pure function everywhere — no atlas≠live gap).
- Touch 1 (labels+villages): carve-aware ocean relabel usable in ALL pick() contexts; replaces the
  floorSightedVeto estimator branch. Belt+suspenders: extend StructureBiomeMatchGuardMixin to cancel
  land structures where carveCeilY < seaLevel-2.
- Touch 2 (dripstone): cave-clamp's resolveSurfaceY measures against the carved solid floor
  (carveCeilY-aware) instead of WORLD_SURFACE_WG.
- Touch 3 (rivers): drop the !IS_RIVER exclusion / gate the river branch on carveCeilY so sunk rivers
  convert like any flooded land.
All behind ONE new flag (house pattern), default off; atlas proof (flag-off byte-identity + flag-on
land-fraction ≈ geo intent + village-eligibility spot-check) before any default flip. Full evidence
index in the session task transcript; key files cited inline above.
