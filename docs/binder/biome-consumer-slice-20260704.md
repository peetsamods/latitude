# Biome Consumer slice — implemented, proven safe off, NOT ready for a live pass

`date: 2026-07-04` · `branch: port/canonical-26.2-pivot` · `scope: Biome Consumer slice per docs/LATITUDE_2_0_OVERHAUL.md`
`model: Sonnet, medium effort (integration work on an already-locked design, per model-effort-strategy)`

Per the kickoff prompt's own instruction: *"Do not consider this slice done from automated proof alone."*
This note ends with a real finding that needs Peetsa's decision, not a "ready" declaration.

## Integration-boundary decision

Per the working card's request to "decide and implement the integration boundary... state which and why":

1. **New dedicated flag**: `latitude.biomeConsumerV2.enabled` (default `false`), added to
   `LatitudeV2Flags`. NOT reusing `geoV2`/`climateV2` — those flags have an established, tested
   contract from Phases 2-3 ("computed and discarded, zero biome change"); overloading them to also
   mean "and drives biomes" would silently break that contract. The consumer flag depends on its
   authority's flag also being on (e.g. if `geoV2` is off, there is no `GeoSummary` to consume no
   matter what the consumer flag says).
2. **Ocean/land authority**: `GeoAuthority.isOceanIntent()` replaces the coarse per-cell
   `OceanDistanceField` threshold as the `oceanAuthority` boolean in both `LatitudeBiomes.pick()`
   overloads, when the consumer flag is on. This is the actual Phase 2 fix (coherent continents, a
   dominant ocean basin) reaching live worldgen instead of only the offline proof tool. The existing
   veto (`oceanAuthority=false` when real terrain height is clearly above sea level) is left in place
   unchanged, regardless of which authority produced `oceanAuthority=true` — real generated terrain
   height is ground truth and should win over any macro-geography heuristic.
3. **Land climate-compatibility reroll**: after the existing province/band cascade picks a biome,
   `ClimateAuthority.climateClass()` is checked for a **clear structural mismatch** (frozen biome in a
   hot climate, jungle in a desert climate, desert/snow in a rainforest climate) via
   `climateFamilyMismatch()`, and only rerolls to a member of `ClimateClass.vanillaFamily()` on a real
   mismatch — everything the existing cascade already gets right is left untouched. This is
   deliberately the SAME shape as the project's own existing "law" idiom (`applyFinalSavannaClimateClamp`,
   the offline `band_correctness_check.py`), just live instead of offline. Variant selection within a
   family uses coherent `ValueNoise2D` (Art VI: no per-block dither/cell-hash).
4. **Ocean biome selection (which ocean biome, given ocean) was deliberately left untouched.** The
   existing `oceanByLatitudeBandOrBase` picker is already band-driven, tag-pool-aware, and
   vanilla-safe (fallback chains to `minecraft:ocean`/`deep_ocean`/etc.). Replacing it with a
   `ClimateClass` ocean-subclass mapping would have discarded that pack-enrichment machinery for no
   proven benefit this slice — scope kept narrow per "integrate alongside, don't rewrite wholesale."

## What was built

- `core/LatitudeV2Flags.BIOME_CONSUMER_V2_ENABLED` (new).
- `LatitudeBiomes.java`: the no-op wiring blocks from Phase 0/2/3 now capture `geoV2Summary`/
  `climateV2Summary` into locals (still only computed when their own flag is on — zero cost change
  when off); the ocean-authority override; `applyClimateCompatReroll` (Registry + Collection
  overloads) + `climateFamilyMismatch` + a coherent variant-index helper, mirrored in both `pick()`
  overloads at the same points the existing cascade already has analogous "final law" checks.
- One new unit test (`biomeConsumerV2DefaultsToDisabled`).

## Proof gate

**1. Mechanical (green):**
- `compileJava` green; 38/38 unit tests green.
- **Flag-off byte-identical**: headless exact-ID atlas (seed 42424242, radius 2000→10000, step 64),
  all three v2 flags at their disabled defaults — SHA-256 identical across all 16 artifacts vs
  `save/canonical-26.2-baseline`. The Hard Stop ("flag-off output differs") is clear.
- **Vanilla-only, consumer flag on**: same seed/size as the Phase 2/3 proofs (2591890304012655616,
  small, step16), `-Dlatitude.geoV2.enabled=true -Dlatitude.climateV2.enabled=true
  -Dlatitude.biomeConsumerV2.enabled=true`, **zero datapacks loaded**. No exceptions, no crash.
  `world_biome_inventory.json`: 44 biomes, 100% `minecraft:` namespace, zero unresolved/fallback
  markers — every reachable combination resolved to a real vanilla biome.

**2. Live: NOT DONE.** See the finding below — this slice is not ready for that pass yet.

## Finding: land fraction collapses to ~13% (not GeoAuthority's calibrated ~39%)

The vanilla-only consumer-on run (`docs/binder/evidence-20260704-biome-consumer/consumer-on-biomes-preview.png`)
measured by the trusted `geography_analyzer.py`:

| metric | GeoAuthority alone (Phase 2 offline proof, same seed) | Live consumer-on (this slice) |
|---|---|---|
| land fraction | 39.1% | **12.7%** |
| largest ocean basin | 99.6% | 98.4% (still coherent) |
| land components | 33 | 399 (visually still reads as real continents, not confetti — see image) |

**Root cause, mechanically understood, not a code bug:** `LatitudeBiomes.pick()` has always decided
ocean as `base.is(BiomeTags.IS_OCEAN) || oceanAuthority` — a **union**, not a replacement. `base` reflects
vanilla/Terralith's own terrain-height-driven noise (an entirely separate field from GeoAuthority's
continentality field, since Phase 4 terrain integration hasn't happened yet). Previously `oceanAuthority`
came from `OceanDistanceField`, which measures **the same underlying terrain continentalness noise** that
`base` already reflects — so the two terms overlapped heavily and the union stayed close to either one
(~36% water, the original red). GeoAuthority's ocean intent is computed from an **independent** noise
field, so it barely overlaps with `base`'s natural ocean — the union of two largely-independent ~60%/~36%
water fields lands far higher than either (observed 87.3% water / 12.7% land).

**Diagnostic ruling out a GeoAuthority-side fix:** flooring `-Dlatitude.geoV2.seaLevel=0.0` (GeoAuthority's
own maximum-land setting) only moved land 12.7% → 13.9% at the same seed/step32. GeoAuthority's own ocean
share is not the bottleneck — `base.is(BiomeTags.IS_OCEAN)` (vanilla/Terralith terrain, not yet informed by
GeoAuthority) is now the binding term. This confirms the mechanism above rather than a miscalibration.

**Qualitative read (from the attached image):** the *shape* of the geography is right — real coherent
continents with clean coastlines, a single dominant connected ocean, no confetti/fragmentation. The
problem is purely *how much* land there is, not *how it's arranged*. This is squarely the "too little
playable land" problem the Phase 2 roadmap entry names in its own "Potential problems" list — now
materialized live rather than theoretical.

**Why this isn't fixed in this slice:** the real fix is Phase 4 (making actual terrain height correlate
with GeoAuthority's macro-geography, so the union stops compounding two independent fields). Attempting a
workaround now would mean either (a) forcing land onto terrain that's naturally generated as deep water
(visually/gameplay broken without real height integration — floating dry biome over an ocean floor), or
(b) tuning a constant to paper over a structural interaction (exactly the "fix visual ugliness with a
one-off clamp instead of shared authority logic" pattern the roadmap's own Hard Stops forbid). Neither is
in scope for this slice.

## What Phase Biome-Consumer deliberately did NOT do

- Did not touch ocean-biome-family selection (kept the existing band/tag-pool picker).
- Did not attempt a terrain-height fix (Phase 4 territory, explicitly forbidden this slice).
- Did not recalibrate `SEA_LEVEL` or any other constant to paper over the land-fraction finding.
- Did not run a live in-game session — the finding above means this isn't ready for one yet.
- Did not tag or push. Local commits only.

## Decision needed from Peetsa before this is "ready"

Three options, not mutually exclusive:
1. **Wait for Phase 4** (terrain integration) before enabling the ocean-authority swap at all — ship
   only the land climate-compat reroll piece behind the consumer flag in the meantime (that piece has
   no land-fraction side effect and is separately provable/safe).
2. **Accept the land-fraction hit for now** and treat "small, coherent continents in a big ocean" as an
   acceptable interim look — playable, just sparser than GeoAuthority's own calibration intends — while
   Phase 4 is scheduled.
3. **Change the ocean composition logic** so GeoAuthority is more authoritative in both directions (not
   just promote-to-ocean) — a real design change with its own risk (dry biome on naturally-generated
   water terrain) that would need its own working card and probably its own live-tuning pass.

This binder note and the code are ready to review; the slice is **not** ready to hand to a live session
until one of the above is chosen.
