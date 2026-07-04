# Phase 3 (ClimateAuthority Prototype) — earthlike climate behind an opt-in flag

`date: 2026-07-03` · `branch: port/canonical-26.2-pivot` · `scope: Phase 3, per docs/LATITUDE_2_0_OVERHAUL.md`
`model: Opus 4.8 (the second from-scratch model phase, per model-effort-strategy)`

Objective (met): make climate explainable, shared, and earthlike enough to guide biome families — a
shared `ClimateSummary`, not clamps in `pick()` — built and MEASURED behind an opt-in flag, with
flag-off worldgen byte-identical.

## Design

Locked via a 4-design adversarial judge-panel (17 agents). Winner: **"Fetch & Lift" moisture-transport-
first** — latitude sets only temperature + wind direction, precipitation is transported via bounded
upwind ocean-fetch. Full spec in `docs/design/climateauthority-design-20260703.md`. The synthesizer
built a numeric harness and iterated the constants until all 14 acceptance rows + invariants passed.

## What was built

- `core/climate/ClimateClass` (vanilla-first Köppen taxonomy, one vanilla family per class),
  `SeasonalityClass`, `ClimateAuthorityParams` (radius-relative, `-Dlatitude.climateV2.*` overrides),
  `ClimateAuthority` (the model — `sample()` gathers ≤9 bounded GeoAuthority probes, `assemble()` is the
  pure test-driven derivation of every ClimateSummary field).
- `adapter/climate/ClimateAuthorityProvider` — real provider; interface refined from Phase 0's
  `(latitudeDeg, band)` to `(blockX, blockZ)` (parallel to GeoSummaryProvider). Wired flag-gated in
  `LatitudeBiomes` (`CLIMATE_V2_PROVIDER`, rebuilt on seed/radius/shape change only when the flag is on).
  Consumes a dedicated GeoAuthority; the summary is computed-and-discarded (does not drive biomes yet).
- `dev/ClimateAtlas` (+ `climateAtlas` Gradle task) — pure-Java offline proof tool, loads no mods
  (vanilla-only measurement), rasterizes temperature/precip/climateClass + a per-band class histogram.

## Proof

- **compileJava**: green.
- **Unit tests**: 37/37 green (13 new ClimateAuthority incl. the full **14-row acceptance table**, the
  bounded-orography control, the same-latitude geography split, no-desert-in-wet / no-rainforest-in-dry,
  temperature-falls-to-poles, field range, wind-non-degenerate, current basin-relative + hemisphere-
  symmetric, vanilla completeness, polar-freezes, determinism; + 24 prior).
- **Flag-off byte-identical**: headless exact-ID atlas (seed 42424242, step 64) SHA-256 identical across
  all **16** artifacts to `save/canonical-26.2-baseline`. Clears the Hard Stop.
- **Both flags on (geoV2+climateV2) inert on biomes**: same run yields a byte-identical `biome_ids.png`
  — both consumers discard their summaries, and both authorities construct cleanly in the headless
  runtime (no crash).

## Acceptance measured on real geography (`climateAtlas`, seed 2591890304012655616, small)

Per-band land climateClass distribution — Earth-like:

| band | dominant classes |
|---|---|
| tropical | RAINFOREST 42% + SAVANNA 35% (+ ~20% alpine-cooled cold classes — see finding) |
| subtropical | HOT_DESERT 43% + COOL_DESERT 18% = **the arid belt (~60% desert)**; HUMID_SUBTROPICAL / MEDITERRANEAN present |
| temperate | HUMID_CONTINENTAL 24% vs TEMPERATE_OCEANIC 22% (**interior/coast split**) |
| subpolar | BOREAL 41% (taiga-dominated) + tundra/ice |
| polar | ICE_CAP 82% + TUNDRA 13% (poles freeze) |

## Findings / decisions during implementation

- **Interface refinement**: Phase 0's `ClimateSummaryProvider.summarize(latitudeDeg, band)` → `(blockX,
  blockZ)`. The real model derives latitude/band and consumes GeoAuthority geography from the coords, so
  the coordinate signature (matching GeoSummaryProvider) is the right shape. Updated the no-op impl + the
  Phase 0 provider test accordingly.
- **Aggressive alpine cooling (residual risk #1) confirmed on real geography**: ~20% of tropical land
  reads as boreal/tundra/steppe because `mountainIntent01` drives `altitudeCooling`, and GeoAuthority
  marks a fair amount of terrain with mountain intent. Earth-plausible in direction (mountains are cooler)
  but stronger than Earth in magnitude. Left at the calibrated default; tunable via
  `-Dlatitude.climateV2.kAlt` (Peetsa to eyeball live). The real fix is Phase 4 terrain height, which
  retires the intent proxy — noted as the reason rain-shadow/orography stay diagnostic.

## What Phase 3 deliberately did NOT do

- Did not drive biome selection from ClimateAuthority (consumer discards the summary). The biome-consumer
  slice — mapping `climateClass` → vanilla families first, then optional pack enrichment
  ([[vanilla-first-overhaul-constraint]]) — is a later slice with its own working card and a vanilla-only
  proof alongside a pack-present proof.
- Hydrology and real orographic truth remain out of scope (Phase 4 terrain).
- Did not tag or push. Local commit only.

## Next

Phase 4 (Terrain Integration Spike) or the biome-consumer slice. Phase 4 is the higher-risk one (a
documented prior ocean-sink seam failure) and would retire the mountain/rain-shadow intent proxies with
real height; the biome-consumer slice is what makes GeoAuthority+ClimateAuthority visible *in the world*.
Recommend the biome-consumer slice first (lower risk, higher visible payoff), with both authorities' flags
turned on to drive vanilla-first biome families.
