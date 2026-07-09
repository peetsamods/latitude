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
