# Adversarial bug-catcher pass — 2026-07-01 (post-TEST 5)

`status: done (2 fixed in TEST 6, 1 dimension re-running)` · `scope: rendering, create-UI, worldgen` · `branch: feat/custom-biome-expansion-26.1.2`

Peetsa asked for an adversarial "bug-catcher" before another manual test pass. Ran a 6-dimension find→verify
workflow (each finding independently re-checked by a skeptic reading the real code) over the code changed this
session. Run `wf_ce159931-002`.

## Dimensions
create-ui · worldgen-cache-guards · border-overlay · loading-messages · atlas-compass-math · integration-regression.

## Result: 4 raw findings → **2 confirmed**, 1 dismissed, 1 dimension errored.

### ✅ Fixed — CONFIRMED #1 (save corruption, medium): existing Classic/legacy save flips to Mercator on load
`GlobeMod.java` shape-stamp + `LatitudeWorldState.java` codec. `globe_shape` used `optionalFieldOf("globe_shape",
"classic")`, so an ABSENT field (pre-2.0 / legacy save) deserialized to the concrete `"classic"` — no unset
sentinel, indistinguishable from an explicitly-Legacy world. The guard `gameTime<100 && "classic".equals(...)`
was thus satisfied by every legacy save; any existing SQUARE world loaded with `gameTime<100` (created then
force-quit within ~5s) got silently re-stamped `mercator` and persisted → border grew from `2*zRadius` to
`4*zRadius`, shifting spawn/longitude/E-W-wall math onto square terrain. The "existing saves provably untouched"
claim (from the Atlas-toggle commit `30db22fc`) was **false**.
**Fix** (`4a73791a`, TEST 6): `globe_shape` now has a real unset sentinel (Optional, no default; field stays
null, distinct from "classic"), mirroring the `worldgen_policy` pattern. Stamping gated on `pendingGlobeShape`
(only ever set by the bespoke create screen — the sole Globe-creation path, Mercator by default) AND
`hasGlobeShape()==false`. The `gameTime<100` heuristic dropped for shape. Legacy saves stay Classic; stamped
worlds are never re-stamped.

### ✅ Fixed — CONFIRMED #2 (low): Re-create world drops the source name
`LatitudeCreateWorldScreen.openLoaded` + the two redirect mixins. The seed fix (`12f53f0a`) threaded
`getUiState().getSeed()` but not the parallel `getName()`, so `probeSetWorldInputs` always got `worldName=null`
on recreate → world created as the hardcoded `"New World"`.
**Fix** (`018e4d0e`, TEST 6): thread `getUiState().getName()` through `openLoaded` (both mixins) into
`probeSetWorldInputs` (no-ops on blank, so fresh create still shows the default).

### ✗ Dismissed (correctly): `phraseSeedIdx==0` conflates unset sentinel with index 0
`LevelLoadingScreenLatitudeOverlayMixin`. Verifier confirmed `PHRASES.length==52`, `FEATURED_PHRASE_COUNT==19`,
so `pickSeedIndex()` returns `33 + rand(19)` ∈ [33,51] — **never 0** with the current array. The `>0` "unset"
check is safe today. (Latent fragility only if PHRASES ever shrinks below 19 or the featured block reaches index
0; not a live bug.)

### ✓ Re-ran border-overlay dimension (original crashed on an API drop): CLEAN — no runtime defects
Standalone re-review confirmed: all three EW `String.format` templates have exactly two `%s` (no format-mismatch
crash); every `degreesFromZ` path is null-safe because `LatitudeMath.halfSize(null|0)` returns 1.0 (no NPE / no
divide-by-zero / no NaN, even on very early load before the Z-radius sync); the 66.5° band boundary classifies
correctly (`>= 66.5` → POLAR); and the `cold` band computation is byte-identical across the warning TEXT
(`ewIsColdBand`), the PARTICLES (`ewStormClientTick`), and the HAZE (`EwSandstormOverlayHud`) — so they can never
disagree. Only smell: a provably-**unreachable** fallback `return` at `GlobeClientState.java:230` (dead code, no
runtime risk) — left as-is (not worth a rebuild; clean up opportunistically on the next border touch).

## Not-a-bug notes worth keeping
The **worldgen per-column cache** (the C3 lag fix) and the **C1/C2 village guards** drew adversarial scrutiny in
their dimensions and produced no confirmed findings — the "does pick() depend on blockY only via biomeY" and
"base-verify vs cave-deck override" concerns were checked and held. See `test1-live-findings-20260701.md`.
