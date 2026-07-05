# Sweeper audit #2: Phases 0-3 comprehensive sweep — triage + fixes (all 27 findings dispositioned)

`date: 2026-07-05` · `branch: port/canonical-26.2-pivot` · `scope: docs/binder/sweeper-audit-standing-practice-20260705.md`'s launched sweep
`model: Sonnet, medium effort (triage + fixes on already-locked designs, per model-effort-strategy); sweeper agents Opus/high per the standing-practice doc`

## Trigger

Per Peetsa's instruction to keep sweeping for holes/blind spots while other work continues, sweeper audit
#2 (background workflow `wf_9c9dbfa8-fec`, 6 lenses, 36 agents) covered everything Phases 0-3 that sweeper
audit #1 (Biome Consumer bug scope) did not already fix: GeoAuthority's own internal decision cascades,
the Phase 0 Platform Adapter shells, `tools/atlas/geography_analyzer.py`'s math and test coverage,
GeoAuthority's documented invariants under adversarial inputs, and ClimateAuthority's probe-gathering
side. Returned 30 raw findings, **27 surviving independent verification** (CONFIRMED or PLAUSIBLE).

## Headline fix: `geography_analyzer.py`'s "largest area-weighted share" was measuring the wrong component

**Found independently by 7 of the 27 survivors across nearly every lens** (findings #1, #2, #3, #4, #9,
#20, #24) — the strongest possible signal this was real. `component_shares()` sorts its component list
by **raw cell count** (`np.argsort(-raw_bins)`). `largest_share()` then reused `components[0]` — the
raw-largest component — for BOTH the raw metric (correct: raw-largest genuinely has the max raw share)
AND the weighted metric (wrong: the raw-largest component is not necessarily the weighted-largest one
under cosine-latitude area weighting). On a fixture with a raw-large, low-weight polar blob vs. a
raw-small, high-weight equatorial blob, this reported the polar blob's ~17% weighted share as "largest
area-weighted share" when the true largest (the equatorial blob) is ~74% — understating dominance by
4-5x. This is the Phase 1 Measurement Harness's core trust metric, used to validate every GeoAuthority
proof number cited across this whole overhaul.

**Fix:** `largest_share()` now separately ranks the raw metric (`components[0]`, unchanged) and the
weighted metric (`max(components, key=lambda c: c["weighted_sum"])`). `bridge_sensitivity` was confirmed
unaffected — it only reads the `["raw"]` sub-field. New test `LargestWeightedShareTest` (a polar-blob vs.
equatorial-blob fixture, computed from real `row_weights()` output, not hand-picked constants) closes the
coverage gap finding #6 named (every prior fixture happened to have raw-largest == weighted-largest).

## Other fixes

- **Findings #5/#20 (unknown/out-of-palette biome indices silently counted as land):**
  `family_grid()`'s LUT fell through to `"land"` for any palette index with no entry, and separately
  `np.clip` folded any raw pixel value above the palette's max index onto max index's own family — both
  silently inflating land share with no warning. Fixed: both cases now map to a distinct `"unknown"`
  family (excluded from land/ocean/river/coast), and `analyze()`'s report gained an `unknown_pixels`
  count/fraction, surfaced as a WARNING line in the text report. Two new tests (in-range palette gap,
  out-of-range pixel value).
- **Finding #7 (`noRainforestInDesert` tests an unreachable-class scenario):** the test sat at phi=25
  (SUBTROPICAL band), where `TROPICAL_RAINFOREST`/`TROPICAL_MONSOON` are structurally unreachable
  regardless of precipitation — the assertion held by band alone, never exercising the dry-tropical-
  interior gate it claims to (the same "test passes for the wrong reason" shape LESSONS L14 warns about).
  Moved to phi=15 (TROPICAL band), where the assertion is now meaningful.
- **Finding #8 (`ClimateSummary.currentModifier01` is SIGNED despite the `*01` naming convention meaning
  [0,1] everywhere else in the record):** renamed to `currentModifierSigned` before any live consumer
  exists (the cheapest possible time to fix a naming footgun) — `ClimateSummaryTest` and
  `PORTABILITY_ARCHITECTURE.md` updated to match.
- **Findings #11/#18 (`seasonality()`'s `bigContinent` gate had a dead `continentId() != -1` tautology):**
  removed — no live or proof path ever attaches a `GeoIdTable`, so this clause was always true for land
  and never actually distinguished anything; `cont in [0.15,0.70]` is the real, now-sole filter.
- **Findings #12/#25 (`GeoAuthority.warpAmp` missing the `Math.max(1,...)` floor its 7 sibling scales all
  have):** added — prevents the domain warp from silently collapsing to identity at `zRadius<=8`
  (unreachable at any canonical size, but a real consistency/robustness gap).
- **Findings #16/#23 (`GeoAuthorityTest.idNamespacesDisjoint` can only ever pass, by design):**
  `sample()` deliberately re-applies a parity mask after any id-table lookup, so continent/basin ids are
  disjoint by construction regardless of whether the underlying labeling is correct — real, intentional
  code (worth testing on its own), but not evidence the id-table/labeling itself is right. Test comment
  corrected to say precisely what it verifies, and to point at `continentIdStableWithinBlob` (same test
  class) as the test that actually covers labeling correctness.
- **Finding #14 (dead calm-wind fallback branch):** kept (it's a legitimate defensive floor against a
  future `ZONAL_FLOOR` change), comment added explaining why it's currently unreachable.
- **Finding #27 (`LatitudeV2Flags`' static-final flags can't be flipped by `System.setProperty` after
  class-load):** correct and intentional for the zero-cost shipping path; added a javadoc warning so a
  future test author doesn't write a flag-on test that silently exercises the flag-off path.
- **Findings #21/#22 (design-doc inaccuracies):** `climateauthority-design-20260703.md`'s cross-JVM-parity
  residual risk omitted `Math.hypot` (same 1-ulp hazard class as cos/pow) — corrected.
  `geoauthority-design-20260703.md`'s "seed-variance-free land fraction" claim contradicted the binder's
  own measured 13-point seed spread (31.1-44.3%) — corrected to describe what rank-normalization actually
  guarantees (a bounded, well-distributed *bias term*, not a fixed land fraction).

## Documented, deferred (with reasons) — not fixed this pass

| # | One-line | Why deferred |
|---|---|---|
| 10 | JUnit `GeoAuthorityTest` land/ocean metrics are raw-pixel, not area-weighted like `geography_analyzer.py` | PLAUSIBLE (not CONFIRMED); reconciling needs either porting area-weighting into Java or re-deriving JUnit thresholds under it — real work, not a triage-sized fix. Documented as geoauthority-design residual risk 8 |
| 13 | `seed==0` treated as an uninitialized sentinel (GeoAuthority/ClimateAuthority share this with the pre-existing ProvinceAuthority convention) | Pre-existing, mod-wide convention predating Phase 2/3 — out of this design's scope to change unilaterally. Documented as residual risk 7 |
| 15 | GeoAuthority plate-bias defaults to neutral (0.5) for probe columns landing beyond the world's populated plate-cell range | Edge-of-world-only, deterministic, low severity — real fix needs widening `buildPlateNorm`'s padding to the max possible probe reach. Documented as residual risk 6, flagged for a robustness pass alongside Phase 4 |
| 17 | `seasonality()` falls through to generic `SEASONAL` for near-equator TROPICAL columns with high continentality/short fetch | Confirmed but currently harmless — zero consumers read `seasonalityClass` yet (same latent-field status as sweep #1 finding #30). Documented as climateauthority-design residual risk 6, flagged for whichever future phase starts consuming the field |
| 19 | `coastDistanceBlocks` linearizes on the dominant FBM octave only, so it over/under-estimates true distance-to-coast near plate seams/edge-pole ramps | Latent — no live consumer yet implements the `|coastDistanceBlocks| > coastBand` reliability gate this risk itself prescribes. Documented as geoauthority-design residual risk 2 addendum |
| 26 | Dominant-ocean-basin invariant may break at extreme non-2:1 aspect ratios (PLAUSIBLE) | Not reachable at either currently-shipped world shape (Classic 1:1, Mercator 2:1) — speculative robustness for an unsupported configuration. Documented as geoauthority-design residual risk 5 addendum |

## Proof gate

- `compileJava` / `test` (Java): green, 43/43 tests (no regressions; `LargestWeightedShareTest`/
  `UnknownPixelTest` are new Python tests, not counted here).
- `python3 -m unittest test_geography_analyzer`: green, 22/22 tests (19 pre-existing + 3 new).
- Flag-off byte-identical: **PASS**. Headless exact-ID atlas (seed 42424242, `--size small`, step 64)
  generated at current HEAD (working tree with all fixes above) and separately at the
  `save/canonical-26.2-baseline` tag (temporary worktree, removed after). 22 artifacts in both runs;
  20/22 SHA-256 identical (every worldgen-content file: biome IDs, bands, continentalness, humidity,
  temperature, biome inventory, seam crops, legends). The only 2 diffs are per-invocation bookkeeping
  (`run_manifest.json`'s timestamp/commit fields; `step64_biomes.txt`'s `durationMs=` line) — not
  biome/geography data. Consistent with the sweep #1 fix round's proof (same file set, same 2 harmless
  diffs).
- Live in-game re-test: not yet done — bundled with the sweep #1 fix round's pending live session,
  Peetsa's call on timing.

## Adversarial verification of this fix batch itself

Per the standing sweeper practice, an independent Opus reviewer (given no access to this note, only the
raw diff) was tasked with adversarially checking the fixes above for correctness, not just re-checking
the original findings. It hand-traced the `family_grid()` indexing math across all 4 cases (valid entry,
in-range palette gap, out-of-range value, empty-palette degenerate case), the `largest_share()` tie-break
behavior, and — most importantly — independently re-derived that the `bigContinent` tautology removal is
safe by checking every `ClimateAuthority` construction site in the repo for a `GeoIdTable` attachment
(none exists) rather than trusting the claim. 6 of 7 checklist items came back clean.

**One real gap it found:** the `currentModifier01` → `currentModifierSigned` rename was complete in Java
(zero remaining references) but left **3 stale doc references** to the old name — `docs/
LATITUDE_2_0_OVERHAUL.md`'s field contract (the twin of the `PORTABILITY_ARCHITECTURE.md` list this
round DID update), `docs/binder/longitude-earthlike-world-overhaul-20260702.md`'s field list, and
`climateauthority-design-20260703.md`'s own field-heading (the same file this round edited for an
unrelated reason, missed on this line). All 3 fixed immediately once found. This is exactly why the
project's absolute indexing/consistency discipline treats doc drift as a real defect, not a nitpick, and
exactly the value of running an adversarial check on your own fixes, not just on the original findings.

## What's next

- Draft the Phase 4 (Terrain Integration Spike) kickoff prompt, now that both sweeper audits are
  triaged/resolved — this was the explicit gate for starting Phase 4 per the 2026-07-05 session's plan.
