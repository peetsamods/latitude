# Slice C-3 — grade the grip (the TEST 29 wall fix) + TEST 30 staging (2026-07-07)

`status: LIVE GREEN — the wall is gone; continental shelves confirmed in-game (TEST 30, 2026-07-08)`
Authorization: Peetsa, same night as the TEST 29 findings ("yes continue with those two decisions" =
stage TEST 30 + fix the wall). Design origin: `test29-r1-look-and-ui-round2-20260707.md` (the
carve-onset-cliff diagnosis).

## LIVE CONFIRMATION (2026-07-08, TEST 30 world)

Peetsa flew the TEST 30 r=1 world (log-verified `strength=0.4, oceanStrengthRatio=1.0`, gripWidth at
its 0.8 default; Regular/10000 Mercator) around 38-46°S / 131-137°E — the same coastal region as the
TEST 29 wall. Screen recording `~/CascadeProjects/Proofs/Screen Recording 2026-07-08 at 7.53.36 PM.mov`.
**The wall is GONE.** Every coastline now reads as a graded continental shelf: terraced sandy shallows
descending into water, the old "believable?" massif is now an island whose flanks slope into the sea,
carved deep-ocean floor with kelp forests below. No sheer stone cliff, no aquifer/lava bleed face
anywhere in the flight. Peetsa: "This is looking very continental-shelfy right?... I don't see the
weird walls. It looks good." C-3 is live-closed.

Peetsa also flagged three BIOME (not terrain) mismatches, all correctly observed and all ONE root
cause — the three-map decoupling from the TEST 28 diagnosis (`test28-deep-ocean-decoupling-20260707.md`),
NOT anything C-3 touched: (a) "forest-ocean" — a Forest biome label/surface over what carved down into
water (the flooded apron keeps its land-biome tag); (b) "ships in a forest" — shipwreck structures
place by the pre-carve ocean/biome map, so they land on now-forested carved ground; (c) "meadow at low
elevation" — Meadow is vanilla's high/mountain-grove biome, appearing at the waterline because the
terrain carved down under it while its biome tag stayed put (seen ~40°S 135°E, flower-covered slope to
the sea). The fix path is unchanged: the consumer law-compliance slice, with the interim label-side
veto + depth-honest ocean-family candidates already enumerated in the TEST 28 row. No new work implied
by this session beyond what's already queued.

---


## What changed (plain language)

The ocean carve used to seize a column at full strength the instant it crossed the geography
coastline — only the *depth* of the sea was graded with distance, not whether the carve applied at
all. Where the coastline crossed tall old-map hills, that made the sheer "cursed wall". Now the
carve's ceiling *descends* across a coastal band: right at the coastline it sits above all shaped
terrain (bites nothing), and it lowers smoothly to the C-2 depth target as you head out to sea. Tall
coastal terrain now steps down along its own contours into the water instead of being planed off.

## The design pivot the tripwires forced (evidence, not vibes)

The first implementation blended densities (`base + grip·(carved − base)`). The gap-delta gate
immediately caught it re-creating the TEST 27 hollowing class at partial grip:
`probe(14950,4426) gapBlocks 0→8, solidRanges 1→2` — marginal underground pockets sandwiched under
the graded surface. Density blending is therefore REJECTED for this regime (recorded here as a dead
end — don't retry). The shipped design grades the CEILING'S HEIGHT instead and keeps C-2's pure
`min()` semantics, which structurally cannot hollow or slab:

```
grip01  = smoothstep(|d| / gripWidth)          d = 2·land01 − 1 (ocean side)
ceilEff = CEIL_ONSET_Y + grip01 · (Y* − CEIL_ONSET_Y)     CEIL_ONSET_Y = 160 (= taper top)
density = min(base, max(CEIL_FLOOR, CEIL_SLOPE · (ceilEff − y)))
```

Both wrappers (`finalDensity` #12 and `preliminarySurfaceLevel` #11) read the SAME gripped ceiling
from the shared helper, so terrain and flooding can never disagree.

## The knob

`-Dlatitude.terrainV2.gripWidth` (default **0.8**, live-tunable like S and r):
- `0` = legacy instant grip (the wall behavior, kept as an escape hatch/diagnostic).
- `0.4` was measured TOO TIGHT: on steep coastline gradients it maps to ~50 blocks — still cliffy.
- `0.8` (default): descent spreads across the land01 0.5→0.1 band (~100-150 blocks on the wall
  coast); land01 ≤ 0.1 carves at FULL strength, so open ocean is untouched.
- Larger = gentler, wider coasts; the tradeoff is old-map land standing a bit further seaward.

## Gates (all green; evidence in scratchpad `c3-*.json`, baselines `c3-baseline-r0.json` +
## `test29wall-r1.json`)

| Gate | Result |
|---|---|
| 1. r=0 byte-identity (pre vs post, columnProbes + coherence + structural + transects + landFraction) | PASS — identical |
| 2. Wall transect (z=4426) | PASS — 14950: vanilla 80 / C-2 wall 60 / **C-3 80 (stands)**; 15000: vanilla 60 / C-2 48 / **C-3 56 (partial)**; 15025+: full carve identical |
| 3. Open-ocean columns bit-identical to C-2 r=1 | PASS (15200/15300/deep flat) |
| 4. Coherence grid vs C-2 r=1 (81 columns) | PASS — 0 floors lowered, 0 biome flips (no grid column sits in the narrow grip band at 2 km spacing) |
| 5. Gap-delta tripwires (every probe column) | PASS — no column gained gapBlocks or solidRanges (the blend version FAILED exactly here) |

Also: compile + pure-JVM suite green; L17 discipline held (the new sysprop is forwarded in
build.gradle's runBiomePreview block in the same commit that reads it).

## TEST 30 staging

`TEST 30.jar` SHA-256 `13a5cd195d941ec87d96dc39b5be103853bb617c3b0b8500570502df8bdd5371` verified
source↔staged; TEST 29 removed from the profile. Contents = UI round 2 (`9f36e295`) + this slice.
Session brief:
- **Fresh world required** (the carve boundary moved — never reopen the TEST 29 world at r=1).
- Same JVM args as TEST 29 (geoV2 + terrainV2 S=0.4 + oceanStrengthRatio=1.0); the new
  `gripWidth` knob is available for live tuning if coasts read too abrupt (up) or too mushy (down).
- Look for: coastal descents where walls were; shelf terraces unchanged; open-ocean depth unchanged;
  UI round-2 items (atlas position, wordmark, Random rainbow when selected, checkerboard behavior,
  snap in Labels, LIVE preview values, drag-undock).
- Still open, unchanged by this slice: phantom-ocean label class (TEST 28 row, needs its own call),
  consumer law-compliance before any Phase-5 flip, Classic world-B parity + S=0 Spark residuals.
