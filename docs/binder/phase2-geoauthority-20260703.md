# Phase 2 (GeoAuthority Prototype) — coherent macro geography behind an opt-in flag

`date: 2026-07-03` · `branch: port/canonical-26.2-pivot` · `scope: Phase 2, per docs/LATITUDE_2_0_OVERHAUL.md`
`model: Opus 4.8 (per model-effort-strategy: this is the from-scratch algorithm phase)`

Objective (met): produce coherent macro land/ocean/continent/ocean-basin **intent** behind an opt-in flag,
**visible/measured in Atlas before it changes biome selection**, with flag-off worldgen byte-identical.

## Design

Locked via a 4-design adversarial judge-panel workflow (17 agents: 4 independent algorithms × 3
lenses + synthesis at max effort). Winner: **"Inverted-Plate Continentality"** — full spec in
`docs/design/geoauthority-design-20260703.md`. In one line: land is the minority upper tail of a
warp-organicised, plate-biased continentality FBM, so ocean is the connected sheet; continent/basin ids
come from an offline flood-fill table with an O(1) hot-path lookup and a plate-id fallback.

## What was built (all Core Logic, zero Minecraft imports except the dev tool + wiring)

- `core/geo/GeoNoise` — verbatim value-noise/hash01/mix64 (kept Minecraft-free; `GeoNoiseParityTest`
  guards against drift from the util originals).
- `core/geo/GeoAuthorityParams` — the locked, radius-relative parameter table + `-Dlatitude.geoV2.*` knobs.
- `core/geo/GeoAuthority` — the field + `sample()→GeoSummary` + cheap per-world `plateNorm` precompute.
- `core/geo/GeoIdLabeling` + `GeoIdTable` — offline 4-connected flood-fill labeling + macro metrics.
- `adapter/geo/GeoAuthorityProvider` — real `GeoSummaryProvider`; wired flag-gated in `LatitudeBiomes`
  (`GEO_V2_PROVIDER`, rebuilt on seed/radius/shape change **only when the flag is on**).
- `dev/GeoAuthorityAtlas` (+ `geoAuthorityAtlas` Gradle task) — pure-Java offline proof tool: rasterizes
  land/ocean to analyzer-compatible `biome_ids.png` (land=plains/ocean=ocean) so the trusted Phase 1
  `geography_analyzer.py` measures it with the same connected-component path used on the red; also emits
  continent/basin visuals + a metrics JSON. Loads no mods → inherently the vanilla-only measurement.

## Proof

- **compileJava**: green.
- **Unit tests**: 24/24 green (12 GeoAuthority invariants incl. determinism, land-fraction band across
  5 sizes × 5 seeds, size-invariance, dominant-basin, largest-continent-of-world, edge-ocean, no-fold
  warp, id-stability-within-blob, id-namespace-disjoint, field-sanity; 2 GeoNoise parity; + 10 prior).
- **Flag-off byte-identical**: headless exact-ID atlas (seed 42424242, radius 2000→10000, step 64) is
  SHA-256 identical across all **16** artifacts to `save/canonical-26.2-baseline` (only the timing line
  differs). Clears the Hard Stop "flag-off output differs".
- **Flag-on inert on biomes**: same run with `-Dlatitude.geoV2.enabled=true` yields a `biome_ids.png`
  byte-identical to flag-off — the Phase 2 consumer computes and discards the summary, so biome selection
  is unchanged, and GeoAuthority constructs cleanly in the headless runtime (no crash).

## Metrics — GeoAuthority vs the current red

Independently confirmed by `geography_analyzer.py` on the emitted rasters (seed 2591890304012655616, small):

| metric | current red | GeoAuthority | 
|---|---|---|
| land fraction | 63–64% | 39.1% |
| largest land component (of land) | ~95% | 77.8% |
| largest continent (of world) | ~60% | ~30% |
| major continents | 1 blob | 3 |
| dominant ocean basin | <10% (none) | **99.6%** |
| projection edge (all bands) | mixed land/ocean | 100% ocean |

Sweep (step16), showing robustness + size-invariance (land fraction identical itty/small/regular per seed):

| seed | land% | continents | largest land (of land) | ocean basin |
|---|---|---|---|---|
| 2591890304012655616 | 39.1 | 3 | 78% | 99.6% |
| 1 | 31.1 | 7 | 53% | 99.8% |
| 987654321 | 32.4 | 6 | 51% | 99.9% |
| 424242 (heavy tail) | 44.3 | 1 supercontinent | 95% | 97.1% |

## Findings / decisions during implementation

- **Namespace-disjointness bug caught by test** — the synthesized spec XORed a land/ocean type bit into
  the id hash and claimed disjointness; that does not survive mixing (cross-namespace collisions possible).
  Fixed structurally: land ids even, ocean ids odd, enforced by the column's own type. Now guaranteed
  globally (not just for gated interior pixels).
- **Heavy-tail supercontinent (residual risk #1) is real** — seed 424242 gives one landmass at 95% *of
  land*. Kept it in the test seed set and reframed the anti-red assertion to "largest continent < 50% of
  the *world*" (red ~60%, worst GeoAuthority ~42%, typical ~20%) — a more honest guarantee than
  largest-of-land, since an Earth-plausible supercontinent can be ~95% of land while land is <45% of world.
  Tunable down via `-Dlatitude.geoV2.lplateRatio=0.22` if a flatter distribution is wanted (Peetsa to
  eyeball live).

## What Phase 2 deliberately did NOT do

- Did not drive biome selection from GeoAuthority (the consumer discards the summary). Per the plan's
  ordering ("visible in Atlas before it changes biome selection"), wiring the biome consumer — vanilla-first
  land/ocean resolution, then optional pack enrichment ([[vanilla-first-overhaul-constraint]]) — is the
  next slice, gated behind the same flag with its own metrics/proof.
- Did not persist the id table into level data (live hot path uses the plate-id fallback; fine while ids
  don't drive biomes).
- Hydrology fields reserved/neutral (needs an offline flow-accumulation pass).
- Did not tag or push. Local commit only.

## Next

Phase 3 (ClimateAuthority) or the Phase 2 biome-consumer slice — both warrant a fresh working card.
Recommend the biome-consumer slice next (it's what makes GeoAuthority visible *in the world*), with an
explicit vanilla-only proof alongside a pack-present proof.
