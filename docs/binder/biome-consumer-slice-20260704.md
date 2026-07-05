# Biome Consumer slice — land climate-reroll ready to ship; ocean swap walled off pending Phase 4

`date: 2026-07-04` · `branch: port/canonical-26.2-pivot` · `scope: Biome Consumer slice per docs/LATITUDE_2_0_OVERHAUL.md`
`model: Sonnet, medium effort (integration work on an already-locked design, per model-effort-strategy)`

Per the kickoff prompt's own instruction: *"Do not consider this slice done from automated proof alone."*
The proof gate surfaced a real land-fraction finding (below) before any live pass. **Resolution (Peetsa,
2026-07-04): wait for Phase 4 before enabling the ocean-authority swap** (option 1 of 3). Implemented as a
structural split rather than a documentation-only warning: the risky half now requires its own explicit
flag, so the safe half (land climate-compat reroll) can ship today without carrying the risky half along
by accident.

## Integration-boundary decision

Per the working card's request to "decide and implement the integration boundary... state which and why":

1. **New dedicated flag**: `latitude.biomeConsumerV2.enabled` (default `false`), added to
   `LatitudeV2Flags`. NOT reusing `geoV2`/`climateV2` — those flags have an established, tested
   contract from Phases 2-3 ("computed and discarded, zero biome change"); overloading them to also
   mean "and drives biomes" would silently break that contract. The consumer flag depends on its
   authority's flag also being on (e.g. if `geoV2` is off, there is no `GeoSummary` to consume no
   matter what the consumer flag says).
2. **Ocean/land authority — gated behind its OWN sub-flag** (2026-07-04, post-finding):
   `GeoAuthority.isOceanIntent()` CAN replace the coarse per-cell `OceanDistanceField` threshold as the
   `oceanAuthority` boolean in both `LatitudeBiomes.pick()` overloads, but only when BOTH
   `latitude.biomeConsumerV2.enabled` AND the new `latitude.biomeConsumerV2.oceanAuthority.enabled`
   (also default `false`) are on. This is the actual Phase 2 fix (coherent continents, a dominant ocean
   basin) reaching live worldgen instead of only the offline proof tool — but per the land-fraction
   finding below, it is walled off behind its own flag rather than a code-comment warning, so it cannot
   be enabled by accident while the consumer flag alone is flipped on for the (proven-safe) land reroll.
   The existing veto (`oceanAuthority=false` when real terrain height is clearly above sea level) is
   left in place unchanged, regardless of which authority produced `oceanAuthority=true` — real
   generated terrain height is ground truth and should win over any macro-geography heuristic.
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

- `core/LatitudeV2Flags.BIOME_CONSUMER_V2_ENABLED` + `BIOME_CONSUMER_V2_OCEAN_AUTHORITY_ENABLED` (both
  new, both default `false`, independently required for the ocean-authority swap specifically).
- `LatitudeBiomes.java`: the no-op wiring blocks from Phase 0/2/3 now capture `geoV2Summary`/
  `climateV2Summary` into locals (still only computed when their own flag is on — zero cost change
  when off); the ocean-authority override (now gated on both consumer flags); `applyClimateCompatReroll`
  (Registry + Collection overloads, gated on the consumer flag alone) + `climateFamilyMismatch` + a
  coherent variant-index helper, mirrored in both `pick()` overloads at the same points the existing
  cascade already has analogous "final law" checks.
- Two new unit tests (`biomeConsumerV2DefaultsToDisabled`, `biomeConsumerV2OceanAuthorityDefaultsToDisabled`).

## Proof gate

**1. Mechanical (green):**
- `compileJava` green; 40/40 unit tests green (after the flag split).
- **Flag-off byte-identical**: headless exact-ID atlas (seed 42424242, radius 2000→10000, step 64), all
  four v2 flags at their disabled defaults — SHA-256 identical across all 16 artifacts vs
  `save/canonical-26.2-baseline`, re-confirmed after the flag split. The Hard Stop ("flag-off output
  differs") is clear.
- **Vanilla-only, consumer flag on (pre-split investigation)**: same seed/size as the Phase 2/3 proofs
  (2591890304012655616, small, step16), all three original flags on, **zero datapacks loaded**. No
  exceptions, no crash. `world_biome_inventory.json`: 44 biomes, 100% `minecraft:` namespace, zero
  unresolved/fallback markers — every reachable combination resolved to a real vanilla biome. (This run
  is what surfaced the land-fraction finding below.)
- **Vanilla-only, RECOMMENDED SHIP CONFIGURATION (post-split)**: `latitude.geoV2.enabled=true
  latitude.climateV2.enabled=true latitude.biomeConsumerV2.enabled=true` with the new
  `biomeConsumerV2.oceanAuthority.enabled` left at its default `false` — same seed/size, zero
  datapacks. Land fraction measured at **63.14%**, matching the pre-existing baseline exactly (the
  ocean-authority swap is confirmed fully walled off). 48 biomes in inventory, 100% vanilla, zero
  unresolved markers, sane top-biome distribution. This is the configuration Peetsa can enable today.
- **Adversarial code review** (general-purpose agent, read-only, independent of the implementer) of the
  flag-split diff: confirmed by full-file grep that there are exactly 2 `oceanAuthority` declarations and
  exactly 2 conditional overrides (one pair per `pick()` overload), both requiring
  `BIOME_CONSUMER_V2_ENABLED && BIOME_CONSUMER_V2_OCEAN_AUTHORITY_ENABLED`; confirmed the land-reroll
  call sites require only the original flag; confirmed no other file in the repo references
  `isOceanIntent`/`oceanAuthority`/`BIOME_CONSUMER_V2` with a stale single-flag assumption; confirmed the
  new flag's system-property name matches everywhere it's referenced. Found two minor doc-completeness
  gaps (a since-fixed stale line in `index.md`, and two in-code comments not mentioning the new sub-flag)
  — both patched. No dead code, logic errors, or missed call sites.

**2. Live in-game session: still not done.** The recommended ship configuration is now proven safe by
every mechanical measure available (byte-identical off, land fraction back to baseline on); a live
session is Peetsa's call on timing, not blocked by an unresolved finding anymore.

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

## What this slice deliberately did NOT do

- Did not touch ocean-biome-family selection (kept the existing band/tag-pool picker).
- Did not attempt a terrain-height fix (Phase 4 territory, explicitly forbidden this slice).
- Did not recalibrate `SEA_LEVEL` or any other constant to paper over the land-fraction finding.
- Did not enable the ocean-authority swap by default, or leave it reachable via the same flag as the
  safe land-reroll piece — it required its own dedicated flag precisely because of the finding above.
- Did not tag or push. Local commits only.

## Decision made (Peetsa, 2026-07-04): wait for Phase 4

Of the three options originally laid out (wait for Phase 4 / accept the land-fraction hit / redesign the
ocean composition logic), Peetsa chose to wait for Phase 4. Implemented as a structural flag split (see
above) rather than just picking option 1 as a documentation convention — the point of splitting the flag
is that "wait for Phase 4" is now enforced by the code, not by a comment someone could ignore. The
ocean-authority code is written, tested (offline), and ready to re-enable once Phase 4 makes real terrain
height correlate with GeoAuthority's geography (or if a future redesign of the ocean composition logic is
chosen instead) — nothing about it needs to be re-derived, just re-flagged and re-proven live.

**Ready to ship now**: `latitude.biomeConsumerV2.enabled=true` (with `geoV2.enabled` and
`climateV2.enabled` also true) gives the land climate-compat reroll only — proven flag-off-byte-identical
when off, and proven to preserve the existing land/ocean baseline (63.14%, matching pre-existing behavior
exactly) when on. `latitude.biomeConsumerV2.oceanAuthority.enabled` stays off by default and undocumented
as a "just flip it" option — re-enabling it should be its own decision, informed by Phase 4's outcome or
a deliberate redesign, not a rediscovery of this same finding.
