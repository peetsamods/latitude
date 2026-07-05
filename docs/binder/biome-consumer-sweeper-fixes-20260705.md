# Biome Consumer live-bug fix + sweeper audit #1 triage (all 30 findings dispositioned)

`date: 2026-07-05` · `branch: port/canonical-26.2-pivot` · `scope: fixes for docs/binder/biome-consumer-slice-20260704.md`'s live finding, per the row `20260705-live-finding-classify-fallthrough` in `evidence-registry.md`
`model: Sonnet, medium effort (bug fix + test-strengthening on an already-locked design, per model-effort-strategy)`

## Trigger

Peetsa's first live session with `latitude.biomeConsumerV2.enabled=true` (the config this project had just
recommended as "safe to enable today") showed Snowy Taiga next to jungle at 18°N and windswept
gravelly hills with mountain goats at sea level in the tropics. Root cause and the durable lesson are
recorded in `docs/LESSONS.md` L14 (main worktree). Per Peetsa's instruction, a "sweeper" adversarial
audit (Opus, 36 agents: 6 lenses find + independent skeptical verify) was run over the full Biome
Consumer bug scope before trusting a patch to the reported symptom alone — it returned **30 raw
findings, all 30 surviving independent verification** (CONFIRMED or PLAUSIBLE). This note is the triage
and fix of all 30.

## The core fix: `ClimateAuthority.classifyBase` rewritten as an exhaustive switch

The reported bug (and findings #1-#12, #14, #15) all trace to one root cause: the old `classifyBase`
gated its TROPICAL and SUBTROPICAL productive branches on `T >= T_WARM (0.60)`. A column cooled into
`[T_BOREAL_HI=0.42, T_WARM=0.60)` — reachable from altitude cooling alone, no unusual precipitation —
matched no explicit case and fell through to a band-blind default: `HUMID_CONTINENTAL` for TROPICAL
(alpine-stepped to `BOREAL` — the confirmed 18°N defect), or `COLD_STEPPE` (snowy_plains) for
SUBTROPICAL, **unmasked** since that gap is reachable below the alpine-step threshold (findings #2,
#3, #6, #10, #13, #20).

**Fix:** `classifyBase` (`ClimateAuthority.java`) now `switch`es exhaustively over the 5-value
`LatitudeBand` enum for every `T >= T_BOREAL_HI` column — a future 6th band is a compile error, not a
silent fallthrough. Temperature only distinguishes hot-vs-cool *within* a band's own branch, matching
how the tropical productive block already worked for rainforest-vs-savanna. A cooled column now
classifies via its own band's normal logic, then `alpineStep` (unchanged) demotes it one tier — the
smooth, monotone behavior the design always intended (see the corrected residual-risk-1 in
`docs/design/climateauthority-design-20260703.md`).

`classifyBase` was also changed from `private` to package-private, matching the file's existing
`bump()`/`currentModifierFor()` convention, so `ClimateAuthorityTest` can call it directly with exact
`(T, P, band, seasonality)` combinations instead of reverse-engineering geography that hits an exact
`(T,P)` target through the full `assemble()` pipeline — this is what makes the new exhaustiveness test
(below) possible.

## Other production fixes

- **Finding #19 (ocean altitude-cooling):** `mountainIntent01` is a land-oriented orographic INTENT
  signal (GeoAuthority has no real submarine-terrain concept), but an ocean column near a plate boundary
  could still carry a nonzero value, and `assemble()`'s `alt` term applied it uniformly — crashing
  equatorial ocean temperature toward `OCEAN_FROZEN` with no physical basis. Fixed: `alt` is zeroed for
  ocean columns.
- **Findings #13, #14, #17, #20 (`climateFamilyMismatch` asymmetric coverage):** the reroll law had no
  guard against frozen/desert picks under warm-temperate classes, and `default -> false` silently
  admitted anything under `HUMID_CONTINENTAL`/`TEMPERATE_OCEANIC`/etc. — exactly the classes the
  fallthrough bug produced, so the "safety net" didn't catch its own root cause's output. Fixed: every
  non-ocean `ClimateClass` now has an explicit, symmetric rule (cold rejects jungle/desert/savanna, hot
  desert rejects jungle/cold, rainforest rejects desert/cold, warm-temperate forest classes reject
  cold/desert).
- **Finding #16 (reroll ordering):** `applyClimateCompatReroll` ran immediately after
  `postFinalSavannaClamp`, but ~9 more downstream land-law gates (alpine-output clamp, jungle/taiga
  survival gates, dry-warm identity, wetland survival, etc.) ran *after* it in both `pick()` overloads —
  any of them could silently undo the reroll's correction. Fixed: relocated the reroll call to run last,
  immediately before each overload's final `debugPick`/`return`, in both the Registry and Collection
  variants.
- **Finding #24 (dead `T_COOL`):** `ClimateAuthorityParams.T_COOL=0.48` from the original design
  synthesis was declared but never consumed by `classifyBase`. Removed.
- **Finding #29 (`GYRE_HI`/`DRIFT_HI` mismatch):** `currentModifierFor`'s warm-boundary drift term
  (`bump(a, DRIFT_LO=40, DRIFT_HI=62)`) is documented as extending the warm current "up the
  mid-latitudes" to 62°, but the only live caller (`sample()`) gated the whole current computation on
  `a < GYRE_HI (55)`, so `a` in `[55,62)` always got `curr=0` live even though unit tests exercised
  `currentModifierFor` directly at those latitudes and passed. Fixed: `sample()` now gates on
  `a < DRIFT_HI`, so the documented drift band is actually reachable in production.

## Test-suite strengthening (`ClimateAuthorityTest.java`, 39 → 44 tests)

- **Row 14 corrected**, not just re-passed: it used to assert `BOREAL`, which was the *coincidental*
  output of the fallthrough bug, not the design's actual intent ("ALPINE step-down from rainforest," per
  the row's own original comment). The new, correct output is `TEMPERATE_OCEANIC` —
  `alpineStep(TROPICAL_RAINFOREST)` — which is what the design comment described all along; the code
  just never produced it until now (finding #21, #5, #9, #11, #15).
- **New: `subtropicalCooledGapNoLongerFallsToColdSteppe`** — direct `classifyBase` regression for the
  SUBTROPICAL cool-wet case (findings #2, #3, #6): pre-fix this silently produced `COLD_STEPPE`
  (snowy_plains) with no alpine flag, unmasked at the exact `(T,P)` used.
- **New: `oceanColumnNearMountainIntentStaysWarmAtEquator`** — regression for finding #19.
- **New: `classifyBaseNeverFallsThroughAcrossFullTPBandGrid`** — the actual "exhaustiveness" test per
  LESSONS L14's own requirement ("spot-check proof beyond aggregate metrics"): calls `classifyBase`
  directly across every `LatitudeBand` × a representative T/P grid spanning every threshold tier, and
  asserts a TROPICAL/SUBTROPICAL column with `T >= T_BOREAL_HI` never resolves to a purely-arctic class
  — the exact shape of the pre-fix bug. A future change that reintroduces a silent gap now fails this
  test loudly instead of only showing up as a plausible-looking wrong biome in play.
- **New: `subpolarBandBoundaryIsTemperatureBlindByDesign`** — pins finding #22 (see disposition below)
  as documented, intentional behavior rather than leaving the phi=50 discontinuity untested.

## Design-doc corrections (`docs/design/climateauthority-design-20260703.md`)

- **Finding #27:** the doc said altitude cooling weight is `K_ALT=0.60`; the code pre-scales
  `mountainIntent01` by `ALT_GAIN=0.85` first, so the true effective coefficient is `0.51`, and the true
  alpine-step threshold in `mountainIntent01` terms is `0.45/0.85≈0.529`, not the raw `ALPINE_ALT=0.45`.
  Doc corrected with both numbers.
- **Finding #28:** the doc's residual risk 2 said orographic signals are "advisory... must not [be]
  promote[d] to hard terrain truth until Phase 4," which reads as covering the ALPINE step too — but the
  alpine step *is* a hard, unconditional `climateClass` rewrite today. Doc corrected to call this out
  explicitly as the one place a diagnostic-grade intent signal does hard-override the class, and as
  still-open (same terrain-decoupling risk as finding #18, not yet fixed — needs Phase 4 real terrain).
- **Finding #23:** the doc's residual risk 1 described the tropical-highland cold classes as a smooth,
  monotone alpine-cooling effect; the pre-fix code's actual discontinuous fallthrough jump contradicted
  that framing. Now moot — the `classifyBase` rewrite makes the doc's original claim true again; residual
  risk 1 was updated to record the fix and its own history rather than silently going stale.

## Disposition of all 30 sweeper findings

| # | One-line | Disposition |
|---|---|---|
| 1,2,3,4,6,7,8,10,12 | classifyBase fallthrough (TROPICAL/SUBTROPICAL T-gap), various framings | **Fixed** — exhaustive switch rewrite |
| 5,9,11,15 | row14 / acceptance table proves nothing beyond the coincidental label | **Fixed** — row14 corrected + 3 new regression tests |
| 13,14,17,20 | `climateFamilyMismatch` asymmetric / missing coverage | **Fixed** — symmetric rule per class |
| 16 | reroll runs before ~9 downstream land-law gates that can undo it | **Fixed** — relocated to run last in both `pick()` overloads |
| 18 | `mountainIntent01` terrain-decoupled for temperature AND alpine step (not just lift/shadow) | **Documented, deferred to Phase 4** — real fix needs real terrain height; doc corrected (residual risk 2) to stop implying only lift/shadow is advisory |
| 19 | ocean altitude-cooling crashes equatorial ocean toward frozen | **Fixed** — `alt` zeroed for ocean columns |
| 21 | row14 comment mischaracterizes the mechanism | **Fixed** — comment + assertion corrected |
| 22 | SUBPOLAR/TEMPERATE phi=50 boundary is temperature-blind | **Pinned as intentional, tested** — every SUBPOLAR input hits an explicit case (not a fallthrough default); added a boundary regression test documenting current behavior rather than changing band semantics unilaterally |
| 23,27,28 | design-doc inaccuracies | **Fixed** — doc corrected (see above) |
| 24 | dead `T_COOL` constant | **Fixed** — removed |
| 25 | ocean-authority veto is one-sided (land-over-terrain has no inverse check) | **Documented, deferred to Phase 4** — the whole ocean-authority swap this belongs to is already flagged off pending Phase 4 terrain integration (Peetsa's 2026-07-04 decision); noted in `phase4-prep-research-20260705.md`'s scope for that spike |
| 26 | `isSnowyVariant` substring match (`"snow"`/`"ice"`/`"frozen"`) is broad enough to false-positive on an unusually-named custom-pack biome | **Documented, deferred** — this is a pre-existing helper used at 13+ call sites across the whole cascade, predating this slice; narrowing it is a cross-cutting change well beyond "fix the Biome Consumer bug scope," and the sweeper's own confidence caveat is low (depends on a pack shipping such a name). Flagged for a dedicated future slice, not fixed here |
| 29 | `GYRE_HI`/`DRIFT_HI` gate mismatch makes the documented 55-62° warm-drift band dead in production | **Fixed** — one-line gate change (`sample()` now uses `DRIFT_HI`) |
| 30 | `seasonalityClass` computed independently of the alpine-cooled final class, producing internally-contradictory stored values (e.g. "monsoon tundra") | **Documented, deferred** — currently latent (grep confirms zero consumers of `seasonalityClass` in `world/`); recomputing seasonality post-alpine-step is a real design question for whichever future phase starts consuming the field, not a live bug today. Flagged for that phase's kickoff |

## Proof gate

- `compileJava`: green.
- `test`: 44/44 green (`ClimateAuthorityTest` 17/17, up from 14 acceptance rows + 5 invariants before this
  pass; full suite unaffected elsewhere).
- Flag-off byte-identical: **PASS**. Headless exact-ID atlas (seed 42424242, `--size small` / radius
  7500, step 64) generated at current HEAD (working tree with all fixes above, all v2 flags at their
  disabled defaults) and separately at the `save/canonical-26.2-baseline` tag (temporary worktree,
  removed after) via `tools/atlas/atlas_runner.py generate`. 22 artifacts present in both runs; SHA-256
  identical on 20/22 (every worldgen-content file: biome IDs, bands, continentalness, humidity,
  temperature, biome inventory, seam crops, legends). The only 2 diffs are per-invocation bookkeeping
  (`run_manifest.json`'s timestamp/commit fields; `step64_biomes.txt`'s `durationMs=` line) — not
  biome/geography data. Confirms the four production fixes above are unreachable when all v2 flags are
  off, as the flag-gated-development discipline requires.
- Live in-game re-test: not yet done — Peetsa's call on timing, same as the original slice.

## What's next

- Sweeper audit #2: a comprehensive pass over Phases 0-3 (not just the Biome Consumer scope), per
  Peetsa's instruction to keep sweeping for holes/blind spots/ambiguity while other work continues.
- Establish the sweeper workflow as a standing, reusable practice (not just a one-off script per bug).
- Draft the actual Phase 4 kickoff prompt once sweep #2 is triaged.
