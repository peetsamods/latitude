# TEST 28 "geography looks off" — the three-map ocean decoupling, measured (2026-07-07)

`status: DIAGNOSED (no fix applied — assessment only; fix candidates enumerated for authorization)`
Source: Peetsa's 4 screenshots from the TEST 28 world ("Fable Test 28", **Large**/radius 15000,
Mercator, pinned seed `2591890304012655616`, Temperate spawn) around x≈-8550, z≈6700-7150 (~42°S):
"Deep Ocean" label over knee-deep water, barren gray shoal-flats at the waterline, a tree standing
IN the water, a shipwreck sitting above the surface — "I think the water level is off."

## Ground truth first (log-verified, not assumed)

`latest.log` for the session: `Phase 4 terrain bias installed/ENGAGED (strength=0.4,
oceanStrengthRatio=0.0)` — **this world is the r=0 recipe**. No bathymetry carving is active;
ocean-side terrain is byte-identical vanilla. `biomeConsumerV2` and
`biomeConsumerV2.oceanAuthority` are both OFF (defaults; not in the args), so visible ocean
placement is the LEGACY OceanDistanceField model. (Also confirmed working live: the Slice-B
teardown latch fired on server stop at 16:43.)

## The answer: water level is fine — the world is running three disagreeing land/ocean maps

Sea level is Y=63 everywhere (visible as the dead-flat waterline in his shots). What he found is
the documented pre-existing DECOUPLING, now conspicuous because he explored a big water body:

1. **Vanilla terrain** decides where water physically is and how deep (the seabed).
2. **Legacy ODF biome map** decides the LABELS ("Deep Ocean"), the surface dressing (ocean-biome
   surface rules = gravel/stone, no grass → the barren gray pancakes on awash shoals), boundary
   features (the tree in water = land-biome feature at a seam column whose vanilla surface is
   submerged), and structures (the wreck placed on a floor at ~Y60 breaches the surface).
3. **GeoAuthority (V2)** — at r=0 only lifts land-intent terrain (+≤5 blocks at S=0.4).

Headless probes at his EXACT columns (same seed/preset/shape/args; scratchpad `test28geo/`):
**`land01 = 1.0` at all four** — V2 geography calls his whole area solid CONTINENT, while the
legacy map labels it Deep Ocean and vanilla terrain makes it shallow sea (solid surface Y40-64).
Maximum three-way disagreement, one column, three answers.

## Quantified world-wide (81-column coherence grid, this seed)

| Class (geo / label / terrain) | Count | Meaning |
|---|---|---|
| land / land / dry | 28/81 | aligned land |
| ocean / land / dry | 24/81 | "double-land over geo-ocean" — the `believable?`-massif class; **r=1 carves these to real ocean** |
| ocean / ocean / wet | 12/81 | aligned ocean |
| **land / OCEAN / wet** | **8/81** | **HIS CASE — phantom-ocean labels on V2-land shoals; r=1 does NOT touch these** |
| ocean / LAND / wet | 4/81 | drowned-land (mirror veto's class, r≠0) |
| land / OCEAN / dry | 3/81 | phantom-ocean label on dry land |
| land / land / WET | 2/81 | flooded land seams (tree-in-water cousins) |

Verified r=1 preview on the same grid: **33 geo-ocean columns carve deeper** and **21 biome labels
realign** (mirror veto + carve side). His four columns are **bit-identical at r=1** (land01=1.0 →
land branch; the carve and both C-2 vetoes require `isOceanIntent()`).

## What this means for the roadmap

- The r=1 bathymetry leg (already queued) fixes the LARGEST class (24/81 double-land → real
  oceans) and is unaffected by this finding — still the right next worldgen look.
- After r=1, the dominant REMAINING artifact class is exactly what Peetsa photographed:
  **legacy phantom-ocean labels over V2-land** (11/81 ≈ 14% of columns): shallow "Deep Ocean",
  gray barren shoals, surface-breaching wrecks. No currently-live mechanism removes it: the
  raised-land veto only fires when the floor is at/above sea level (his floors are 1-15 blocks
  under), and full ocean-authority is gated behind the consumer (with the DOCUMENTED land-fraction
  collapse interaction, LatitudeBiomes.java ~L3081 comment).
- **Fix candidates (need Peetsa's authorization; NOT implemented):**
  - **(b) Narrow label-side veto:** extend the existing raised-land veto's doctrine — demote
    legacy ocean-authority where geoV2 `land01` is decisively land (e.g. ≥0.8). Kills the phantom
    class only; can only ADD land (no collapse risk in the L3081 direction), but shifts visible
    land fraction → needs an atlas re-measure gate.
  - **(c) Depth-honest ocean family:** pick deep vs regular ocean by REAL floor depth
    (`previewFloorHeight`), so "Deep Ocean" can never label knee-deep water even where the ocean
    label itself stays. Small, cosmetic-honest, veto-independent.
  - Both are consumer-era alignment brought forward as a bounded slice; the full fix remains the
    consumer flip behind the law-compliance slice.

## Evidence

- Live: Peetsa's 4 screenshots (minimap "Deep Ocean" at the probed coords) + profile `latest.log`.
- Headless: `scratchpad/test28geo/r0.json` + `r1.json` (two runs, fresh world dir each, foreground,
  preset `globe:globe`=15000 — NOTE the preset map: globe=15000/UI-Large, globe_large=10000/
  UI-Regular, globe_regular=7500/UI-Small, globe_small=5000, globe_xsmall=3750, globe_massive=20000).
- Classification sums 81/81; his-case sample rows and the r0↔r1 delta counts reproduced in the
  registry row.
