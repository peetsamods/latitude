# Phase 5 — Boundary Experience: plan & run log (2026-07-09)

`status: IN PROGRESS — slice B-0 (scoping + edge truth) underway`
Authorized by Peetsa 2026-07-09 ("Proceed with Phase 5 using workflow"). Objective per
`docs/LATITUDE_2_0_OVERHAUL.md` §Phase 5: make the world edge intentional and less wall-like WITHOUT
claiming seamless topology — bias the projection edge toward ocean/ice/storm geography, adjust warning
language/visuals, live visual proof after atlas green. Prerequisite satisfied: consumer law-compliance
slice complete (`9f21b3cb`). E-W wrapping is SCRAPPED history (2026-06-24, fully reverted; carry only its
honest-framing rule: no false wrap promise). Hard stops in force, especially: edge fixes go through
SHARED AUTHORITY logic, never one-off biome clamps; flag-off output must stay byte-identical.

Tiered workflow per AGENTS.md (architect = main loop, never codes; Developer Opus; Test-writer Sonnet;
Sweeper Opus adversarial per pass; Reviewer read-only docs-vs-actions). This doc is the shared RUN LOG.

## Current truth (recon + measurement, 2026-07-09)

**Measured edge composition (A′ atlas, live config, R7500 Mercator):**
- Z edges (poles): ALREADY intentional — 67% ice/snow land + 25% frozen ocean. Little to do.
- X edges (east/west): NOT intentional — 46-54% ordinary land (savanna 18-22%, jungle, taiga),
  statistically identical to the interior (45-47%); ocean share at the edge (32%) ≈ interior (30%).
  A player flying east hits savanna guillotined by the world border.

**Code truth (recon):**
- `GeoAuthority.contEdgedAt` ALREADY suppresses continentality at edges: `-EDGE_STR(1.30)·smoothstep(0.80,1,|x|/xR)`
  and `-POLE_STR(0.75)·smoothstep(0.92,1,|z|/zR)` — always-on under geoV2, NOT latitude-aware (same ocean
  push at every latitude; no ice/storm variant). `GeoSummary.projectionEdgeSuitability01` is computed per
  column and CONSUMED BY NOTHING — a ready-made hook.
- **THE CENTRAL PUZZLE (B-0 must answer before any design):** the authority's edge-ocean push does NOT
  reach the visible map — despite land01≈0 in the outer band, the biome layer still paints ~50% land
  there. Suspects: the pre-2.0 biome machinery (OceanDistanceField/provinces) doesn't consult geoV2;
  the C-2 waterline/mirror vetoes may be excluded or blind in the edge band; terrain carve behavior at
  the extreme edge. Diagnose with existing atlas artifacts (continentalness/height/biome PNGs) — no new
  runs needed for diagnosis.
- Physical edge: X = real vanilla WorldBorder at xRadius·2 (Mercator: sized to the WIDE axis — the square
  border sits far outside the Z extent). Z = NO hard stop; escalating hazard effects
  (IMPAIR→HOSTILE→WHITEOUT→LETHAL) from `POLAR_START_FRAC·zRadius`; PolarCapScrubber exists but disabled.
- Boundary UX inventory: LIVE = EwSandstormOverlayHud (screen haze, climate-aware tan/whiteout),
  GlobeWarningOverlay (escalating text, climate-aware), warning particles, zone titles. DEAD = the 3D
  EwStormWallRenderer (26.1-era mixin never registered on 26.2; render call commented out).
- Two overlapping spawn-safety nets exist (C-2's 0.80·xR clamp + older clampSpawnAwayFromEwWarning) —
  don't add a third.

## Slices

| Slice | Who | Content | Gate |
|---|---|---|---|
| B-0 | Architect | Edge-truth diagnosis: WHY doesn't the authority's edge ocean reach the visible map? Distance-graded X/Z edge bins incl. ice category; land01-vs-height-vs-biome cross-check from existing artifacts | Written mechanism diagnosis in this doc |
| B-1 | Architect designs, Sweeper adversarially reviews | Design: latitude-aware EDGE INTENT through shared authority (warm/temperate → ocean, polar → ice shelf; storm = UX layer, not biome), consuming/extending the existing edge terms + the unused projectionEdgeSuitability01 hook; NEW FLAG (house LatitudeV2Flags pattern) so flag-off is byte-identical incl. the current live config | Design section approved in this doc |
| B-2 | Developer(Opus) → Test-writer(Sonnet) → Sweeper(Opus) → Reviewer | Implement geography leg | compile+suite; flag-off atlas byte-identity; flag-on edge acceptance (targets set in B-1 from B-0 numbers); commit |
| B-3 | Developer(Opus) → Sweeper → Reviewer | UX leg: warning language pass + decide/revive the 3D storm wall on 26.2 (client-only) | compile+suite; commit; live eyeball queued |
| B-4 | Peetsa live | Fly east edge at equator/temperate/pole on a fresh flag-on world; judge the boundary experience + storm/haze visuals | Written live verdict; only then default-flip discussion |

Push cadence: commit per green pass; push after B-2's atlas gate (geography) and after B-3 (client).

## Open decisions (Peetsa's, queued for the B-4 live session)
1. Flip `biomeConsumerV2` default-ON? (Its law gate is cleared; flip is a Phase-5-era decision, separable
   from the boundary work. Recommend eyeballing consumer-on in the same B-4 world.)
2. Storm wall 3D: revive vs retire-for-good (haze+warnings may be enough — B-3 presents both options).
3. Z-edge hard stop: today a determined player can tank the debuffs and walk past the pole — accept
   (soft lore boundary) or harden?

## Pass log (appended as slices complete)

- **B-0 opened (2026-07-09)**: recon complete (edge suppression terms, unused projectionEdgeSuitability01
  hook, dead storm wall, live haze/warnings, physical-edge split X-border/Z-hazard, wrap post-mortem
  reusables=none). Edge composition measured from A′ (numbers above).
- **B-0 DIAGNOSIS COMPLETE (2026-07-09, code-trace with file:line evidence)**: the edge-ocean intent IS
  expressed in 3D but not in biome labels, via a two-mechanism stack:
  1. **Atlas overstates the problem**: biomes.png runs terrain-BLIND by default (`ATLAS_SAMPLER` context,
     null noiseConfig/heightView → the C-2 sunk-land mirror veto's guard never passes). The carve itself
     (`carveCeilYOrInfinity`, gated only on land01/shelf01) DOES fire at the edge — land01≈0 → carve
     target ~Y39 → flooded. **Live, the outer band is already water over a ~Y39 seabed**, not dry savanna.
  2. **Live biome labels are still wrong**: the mirror veto's cheap (`skipPreview`, MIXIN/live) branch
     reads the FLUID-INCLUSIVE `WORLD_SURFACE_WG` (flooded columns read 63 → veto never fires); C-2's own
     floor-sighted fix (`previewFloorHeight`/OCEAN_FLOOR_WG) was wired only into the `!skipPreview`
     harness branch. So live shows ocean water tagged savanna/jungle (wrong identity), and C-2's veto was
     never live-verified (Phase 4 stopped pre-Slice-E).
  Candidates evaluated: (1) consume `projectionEdgeSuitability01` as a flag-gated latitude-aware edge
  ocean authority in pick() — RECOMMENDED (terrain-blind → identical on atlas + live); (2) floor-sight the
  live veto branch — correct C-2 completion, helps everywhere, invisible to the default atlas; (3) global
  BIOME_CONSUMER_V2_OCEAN_AUTHORITY — mis-scoped (known land-collapse to ~13%), rejected.

## B-1 design (architect, 2026-07-09 — under adversarial review)

Two bounded fixes, one flag:

**Fix 1 — edge intent (the Phase-5 core).** New flag `latitude.boundaryV2.enabled` (house LatitudeV2Flags
pattern, default false). In `pick()`, when the flag is on AND geoV2 is live: consume
`GeoSummary.projectionEdgeSuitability01` — when it crosses a threshold, set `oceanAuthority = true` for
the column. The EXISTING latitude-correct ocean-family logic then does the rest (C-2 already proved ocean
family follows latitude: frozen oceans at the poles — the "ice" edge comes free). The boundary must be
FRAYED, not a ring: gate through the same coherent keep-noise pattern the demote gates use (Art VI — no
straight lines), ramping with edgeSuit so ocean share rises smoothly toward the border. Threshold/ramp
chosen so the outer ~0.90·xRadius+ is ocean-dominated while everything with edgeSuit≈0 is untouched.
Shared authority end to end: GeoAuthority signal → pick()'s existing oceanAuthority seam; no biome clamps.

**Fix 2 — floor-sight the live mirror veto (C-2 completion).** In the veto's `skipPreview` branch, replace
the fluid-inclusive `columnDecisionY` height source with an `OCEAN_FLOOR_WG`-based floor estimate (the same
source C-2's `previewFloorHeight` already trusts). No new flag: it completes C-2's documented intent under
C-2's existing gates (`terrainBiasActivelyBiasing`); flag-off (all-V2-off) remains byte-identical because
the veto only runs while biasing. Fixes wrong-identity water everywhere (interior floating-land + edge).

**B-2 acceptance (targets from B-0 numbers, measured on atlas runs with `-Dlatitude.atlasTerrainAware=true`
where fix-2 visibility is needed, plain atlas for fix-1):**
- Flag-off (boundaryV2 off, live-config flags): atlas byte-identical to A′ `20260709-113845`.
- Flag-on: X-edge outer 3 cells land-biome share ≤10% (from 46-48%), ocean+ice ≥85%, frozen-family
  dominating at polar rows; cells with edgeSuit≈0 byte-identical to flag-off (change confined to the band).
- Pure-JVM: new tests for the edge-intent threshold/ramp helper (pure core) + existing suite green.
- Hard stops honored: no clamps, no wrap claims, flag discipline.

### B-1 amendments (sweeper APPROVE-WITH-CHANGES, 2026-07-09 — binding on B-2)

1. **Fix-1 consumes the X-only `edgeB` term, NOT `projectionEdgeSuitability01`** (= max(edgeB, poleB) —
   the poleB component would convert the already-good icy pole LAND shelf into frozen ocean, a regression;
   D1). Expose edgeB as its own GeoSummary field (additive).
2. **Fix-1 gates on `terrainBiasActivelyBiasing()` (same predicate + oceanStrengthRatio check the C-2
   mirror uses) and inserts AFTER the raised-land veto** — otherwise the fluid-inclusive raised-land veto
   clobbers it live (WORLD_SURFACE_WG reads 63 ≥ seaLevel on flooded columns) while the terrain-blind
   atlas shows it working: atlas≠live, the exact failure class this phase exists to kill (D2). With the
   gate, boundaryV2-on + terrainV2-off can never paint ocean labels on dry land. The "plain atlas proves
   fix-1" framing is DROPPED — both fixes are proven with `-Dlatitude.atlasTerrainAware=true` runs (and
   targets re-derived at whatever radius that mode can afford) + the B-4 live look.
3. **Fix-2 gets its own sub-flag** (`latitude.terrainV2.floorSightedVeto`, default false — the
   oceanAuthority sub-flag precedent): it is honestly a MAP-WIDE change to the current live config
   (~30% of sampled columns flip to their C-2-intended ocean identity, with chunk-boundary discontinuities
   in existing worlds), so it must be independently switchable at B-4 (R1). Applied to BOTH pick-path
   mirror copies. AND its premise — that `OCEAN_FLOOR_WG` sees the terrainV2 carve in the live MIXIN
   context — must be empirically confirmed (harness/live), not assumed (R2, L24 class).
4. Noted, accepted, stated: the outer band becoming ocean-dominated IS a (frayed-approach) ocean moat —
   that is the intentional Phase-5 outcome, not an accident (Art X reviewed); edge rivers stay rivers
   (cosmetic, revisit at B-4); edge villages vanish with the land (structures follow biome); spawn is
   already excluded from the band by the C-2 clamp — add no third net.
