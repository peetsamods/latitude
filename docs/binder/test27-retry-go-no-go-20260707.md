# TEST 27 retry (r=0 recipe) — terrain go/no-go note (2026-07-07)

`status: LIVE GREEN on the armed r=0 leg — Phase 4 closeable modulo two enumerated residual checks`
Session: Peetsa, TEST 27.jar (SHA `fbfb7a2b…`), typed pinned seed, Mercator UI-Regular, args
geoV2+terrainV2 S=0.4 + **oceanStrengthRatio=0.0**. Evidence: `~/Documents/Test 27-5.pdf` (5 annotated
screenshots + Spark), Spark `spark.lucko.me/5CaSlz7SgV` (02:56 PM, 4 ms interval, **all-thread**),
headless cross-sections at the reported F3 coordinates (scratchpad `test27-5/`). This is the roadmap's
"terrain go/no-go decision note" deliverable for Phase 4.

## Checklist results

| Script item | Result |
|---|---|
| 1. Sodium boot | **GREEN — P1-4 CLOSED LIVE.** No crash with Sodium 0.9.0 through a full session (renderer active in every F3). |
| 2. Spawn sanity | **GREEN.** Green meadow, flowers, sheep, Y=102. (Spawn still landed in the projection edge band at x=-17202 — TEST 27 predates the C-2 spawn fix — but at r=0 that band is untouched vanilla terrain, so it is benign. C-2 `29d43749` excludes the band from spawn search going forward.) |
| 3. Coastline believability | **GREEN.** The calibration coast reads as graded, terraced slopes out of the water — no cliff-wall, no slab, no hollows ("believable?" massif at -3315/-3620 confirmed non-artifacted). |
| 4. R7 warm-band snow glance | **GREEN.** 24.8° highlands at the pre-located coordinate: bare alpine stone above Y168, NO snow — the taper's structural guarantee confirmed by eyeball. |
| 5. Spark all-thread @ S=0.4 | **GREEN — P1-3's armed leg CLOSED.** All-thread capture (C2ME/ForkJoin/IO workers all present): TPS 20.01/19.88/19.96, MSPT median 7.97 ms, 95%ile 64.5 ms, max 261 ms over 1m03s of profiling. Chunk generation at S=0.4 is healthy. (Machine RAM still tight at 91% physical — context, not blocker.) |
| 6. Classic same-seed world B (edge parity + teardown/re-arm log) | **SKIPPED** — residual. |
| 7. S=0 control capture | **SKIPPED** — residual (the armed capture is unambiguous on its own; the control tightens the perf claim but no anomaly needs explaining). |

## New findings from the session (both verified headlessly at the exact F3 coordinates)

1. **"Harsh seam between subtropical desert to temperate plains" (-2815/-3906, 35.2°):** ZERO
   terrain-bias involvement — bias is +0 across the whole cross-section (land01=0 there, r=0). This is
   the pre-existing 35° BAND EDGE: band boundaries are jitter-wiggled lines but still one-biome-to-another
   flips, and desert→plains is the highest-contrast pair possible. Would look identical flag-off.
   Backlog candidate (biome-side, pre-existing class): an ecotone strip / boundary fray at band edges,
   cousin of the arid-ladder fray (`ff713f57`).
2. **"Wall" (-2771/-3810, 34.3°):** MOSTLY pre-existing — VANILLA terrain steps 94→81→73 naturally across
   that line; the bias adds +5 only on the west shoulder, exactly where `land01` flips 1.0→0.0 in <50
   blocks (the known sharp island edge east of the calibration ramp). Verdict: minor sharpening at r=0.
   Backlog note (geography-side): sharp land01 edges translate to terrain shoulders under bias; consider
   edge softening or a slope limiter before high-strength configs.
3. **Note for the bathymetry era:** the "believable?" desert massif sits on land01=0.000 — geography
   wants it UNDERWATER. Under C-2 r=1 it carves to sea. The r=0 recipe deliberately leaves old-map land
   standing; the alignment arrives with the next jar.

## Terrain-config clarification (added same day, after Peetsa asked "Terralith is not installed — does
## that change anything?")

**No Terralith anywhere in this test chain — and that makes the record BETTER, not worse.** Verified
directly: the live `LATITUDE 26.2` profile's mods contain no terrain mod, AND the headless dev server
loads only fabric-api modules + Latitude (44 mods, datapacks `vanilla,…,globe`) — so every headless
measurement and every live observation in Phase 4 ran on IDENTICAL, PURE-VANILLA `minecraft:overworld`
noise (which is exactly why headless numbers matched live blocks throughout). Corrections and rules:
- Earlier "Terralith"/"vanilla-Terralith" attributions in this doc and adjacent records (the wall's
  stepping, the coast terracing, the slab-bracket "noise config" hedge) describe VANILLA behavior — the
  Terralith phrasing was inherited from the 26.1.2-era profile, which did carry Terralith.
- **Calibration scope rule: every Phase-4 number (S=0.4, the 0.09→0.10 slab bracket, the taper window,
  C-2's K_DEPTH/shelf constants) is calibrated for PURE-VANILLA density.** If Terralith or any
  noise-settings datapack is ever added to the 26.2 profile, re-run the Slice C/C-2 sweep matrix
  (~5 min headless) BEFORE trusting any of these values — design residual R4's anticipated
  recalibration, now with its concrete trigger.
- Alignment note: the vanilla-first hard rule (2.0 must work vanilla-only; packs are enrichment) is
  satisfied by construction for Phase 4's entire proof chain — the pack-present configuration is
  untested-but-additive, the intended shape.

## Decision

**GO, with two enumerated residuals.** The r=0 recipe (S=0.4) is live-confirmed: believable coasts, no
artifacts of any class, healthy all-thread performance, no wrapper-caused warm snow, Sodium coexistence.
Phase 4's exit criterion (a calibrated, live-verified strength + this note) is satisfied for the
land-lift half of the feature; the two skipped checks (Classic world-B parity incl. the teardown/re-arm
log, and the S=0 control capture) are LOW-RISK residuals folded into the next live session rather than
blockers — both take ~10 minutes total and the next session is already needed for TEST 28 (UI pass) and
the r=1 bathymetry look.

**Recommended sequencing (Peetsa decides):** TEST 28 = the UI session's held jar (`5651ac0a…`, worldgen
identical to TEST 27) for the 9-point UI checklist; TEST 29 = fresh build with C-2 for the bathymetry
live look (r=1: carved oceans, continental shelves, ~a third of interior old-map land converting to
proper ocean where geography says so) + the two residual checks + the consumer law-compliance slice
remains the gate before any Phase-5 consumer flip.
