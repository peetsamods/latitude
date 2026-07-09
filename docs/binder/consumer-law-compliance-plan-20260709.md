# Consumer law-compliance slice — plan & working record (2026-07-09)

`status: IN PROGRESS — passes 0 (scoping+baselines), 1 (P1-A), 2 (P1-B) complete; pass 3 (map proof) pending`
Authorized by Peetsa 2026-07-09 ("pull up the P1-A/P1-B bug diagnosis and scope the slice"). This is the
prerequisite slice gating any Phase-5 `biomeConsumerV2` flip, per
`fable5-biome-geography-audit-20260707.md` §6. Run under the tiered multi-agent workflow (AGENTS.md):
Architect/director (main loop, writes no code), Developer (Opus), Test-writer (Sonnet), Sweeper (Opus,
adversarial per pass), Reviewer (read-only, docs-vs-actions). Codex CLI not installed — skipped.

## The two bugs (from the audit + this session's recon)

- **P1-A — the reroll undoes the tropical dry law.** `applyClimateCompatReroll`
  (`LatitudeBiomes.java:10015-10059`, both overloads; called at `:3677`/`:4325` as the very LAST pick step)
  repaints purely from the raw `ClimateClass` — `HOT_DESERT`/`COOL_DESERT` classes repaint into
  desert/badlands at ANY latitude. The tropical dry LAW (`demoteEquatorialBadlands`/`demoteEquatorialDesert`
  inside `applyFinalSavannaClimateClamp`, `:10137-10142`, ramp constants `BADLANDS/DESERT_LAT_RAMP_LOW_DEG
  = 23.5` at `:2556/:2569`) runs ~175 lines earlier — so the reroll re-introduces the exact biomes the law
  scrubbed (audit: 0.02% → 2.01% dry share at 0-20°, ~432 cells).
- **P1-B — flat-polar repaint on warm-band mountains.** ClimateAuthority's `alpineStep`
  (`ClimateAuthority.java:183-186,342-352`; `alt = mountainIntent01 × ALT_GAIN(0.85)`, threshold
  `ALPINE_ALT=0.45`, gain `K_ALT=0.60`) demotes warm classes to `COLD_STEPPE` from a terrain-BLIND intent
  proxy; `COLD_STEPPE.vanillaFamily()` = `snowy_plains`/`windswept_gravelly_hills` (flat-polar, no altitude
  family) — so over-cooled warm-band mountain columns become snowy_plains at 10-38° (~233 cells).

## Architect scope rulings

1. **The reroll stays LAST.** Its position after all 9 downstream laws is itself a prior fix
   (sweeper-audit finding #16, in-code comment `:3669-3676`). The fix makes the reroll law-aware IN PLACE.
2. **P1-A**: before accepting a desert- or badlands-family repaint, consult the SAME law predicates the
   base path uses (`shouldDemoteEquatorialBadlands`/`shouldDemoteEquatorialDesert`, same blockX/blockZ) —
   if the law would demote that biome at this column, skip the repaint (keep the existing pick). No
   constant duplication; both overloads updated identically. Poleward-arid gating is a conscious NON-goal
   (audit found no poleward bug; smallest fix).
3. **P1-B is fixed at the CONSUMER, not in ClimateAuthority.** ClimateAuthority is locked Phase-3 pure
   core (its 14-row acceptance test must not move); it exposes `altitudeCooling01` in every
   `ClimateSummary`, and REAL terrain height (`preview.centerHeight`, the Phase-4 wrapper's actual height)
   is already in scope at the reroll call site. The consumer distinguishes "cold because mountain-cooled"
   (altitudeCooling01 high, low latitude) from "cold because polar" and (i) vetoes cold repaints where the
   REAL terrain isn't actually elevated, (ii) routes genuine cooled-mountain repaints to altitude families
   (grove/snowy_slopes), never snowy_plains. Tempering kAlt itself inside ClimateAuthority = Phase-5-era
   design item, out of scope.
4. **Flag-gating is absolute**: every behavior change lives inside the existing
   `BIOME_CONSUMER_V2_ENABLED && climateV2Summary != null` gate. The live config (consumer OFF) must be
   byte-identical pre/post — proven by atlas, not asserted.

## Pass structure & proof protocol

| Pass | Who | Content | Gate |
|---|---|---|---|
| 0 | Architect | Recon + this plan + pre-fix baselines pinned | Baselines A (config-1) + B (config-2) generated from a pristine worktree at the pre-fix commit |
| 1 | Developer(Opus) → Test-writer(Sonnet) → Sweeper(Opus) → Reviewer | P1-A | compile + pure-JVM suite green; sweeper + reviewer verdicts; local commit |
| 2 | same crew | P1-B | same; local commit |
| 3 | Architect (runs all atlases personally, sequenced, never concurrent) | Map proof | A′ (config-1 post-fix) byte-identical to A; B′ (config-2 post-fix) vs A′ diff: dry share 0-20° ≤0.1% with the 25.5° corrections retained; zero snowy_plains <45°; TerrainProofHarness T1-T3 green; `band_correctness_check.py` pass |

Atlas recipe (matches the audit): `tools/atlas/atlas_runner.py generate --step 64 --size small`
(R7500) `--seed 2591890304012655616`; config-2 adds `--sysprop latitude.climateV2.enabled=true --sysprop
latitude.biomeConsumerV2.enabled=true`; both configs carry the live terrain flags (geoV2+terrainV2 S=0.4).
Push to origin only after Pass 3 is green (map-based proof is law).

## Pass log (appended as passes complete)

- **Pass 0 (2026-07-09)**: recon complete (code map with file:line for both bugs, law gates, flag plumbing,
  test idioms, atlas tooling — sole code briefing for the developer). Plan committed. Baselines: see
  registry row `20260709-consumer-law-compliance` as runs land.
- **Pass 0 baselines (2026-07-09)**: A (consumer OFF) run `20260709-110329`, B (consumer ON, pre-fix) run
  `20260709-110752`, both from the pristine worktree pinned at `01ba2de5` (~3 min each, R7500 step64,
  seed 2591890304012655616, flags verified in run manifests). `band_correctness_check`: A **PASS**,
  B **FAIL** (tropical arid 2.17% vs 0.50% max) — the P1-A signature reproduced on demand. Exact-ID diff
  (`atlas_diff.py`, session scratchpad; measures the same three acceptance metrics Pass 3 will use):
  980/110,215 cells change (0.889%); **dry share 0-20° 0.027% → 1.681%** (P1-A, ~350 jungle→arid cells);
  **snowy_plains <45° 0 → 174** (P1-B, plus savanna→windswept_gravelly_hills COLD_STEPPE siblings);
  keep-these corrections present (~88 desert→forest/flower_forest cells at 24-35°).
- **Pass 1 — P1-A (2026-07-09)**: Developer (Opus) made the reroll law-aware in place: new private helper
  `rerollCandidateViolatesEquatorialAridLaw(candidate, blockX, blockZ)` — candidate is desert/badlands
  family AND either `shouldDemoteEquatorialBadlands` OR `shouldDemoteEquatorialDesert` fires → both
  `applyClimateCompatReroll` overloads skip the repaint and keep the already-lawful pick. Direct predicate
  reuse (their signatures already fit); no constant duplication — veto and law share the identical
  ramp+keep-noise and cannot drift. Reroll stays LAST. compile + pure-JVM suite green. Test-writer ruling:
  engages in Pass 2 (Pass 1's change is entirely in MC-heavy LatitudeBiomes, atlas-proven by convention;
  no registry-bootstrap test infra invented mid-slice). Sweeper (Opus, adversarial, 9 probes):
  **ACCEPT-WITH-NOTES** — all probes clean (predicate purity, full family coverage incl. eroded_badlands,
  only HOT/COOL_DESERT classes affected, twins symmetric, flag-gating structural, exception paths correct);
  2 non-blocking notes recorded: (i) first-vetoed-member early-return can conservatively under-correct in
  the 23.5-27° salt-divergence belt (safe direction; future refinement could continue the loop),
  (ii) a pathological cold-pick-in-tropical-HOT_DESERT case keeps the cold pick — a PRE-EXISTING frozen-law
  gap, not this pass's regression.
- **Pass 2 — P1-B (2026-07-09)**: Developer (Opus), two layers. (B, the hard guarantee) new pure-core
  `ClimateClass.alpineFamily()` — COLD_STEPPE/TUNDRA→(grove, snowy_slopes), ICE_CAP→(frozen_peaks,
  snowy_slopes), all others fall through to vanillaFamily; `rerollFamilyFor` routes every cold-class
  repaint below `SNOWY_PLAINS_MIN_LAT_DEG = 45.0` (cites the audit acceptance) through alpineFamily —
  snowy_plains structurally unreachable from the reroll below 45° regardless of why the column classified
  cold. (A, the realism layer) `rerollColdAltitudeVetoed`: cold class below 45° + `altitudeCooling01 ≥
  ClimateAuthorityParams.ALPINE_ALT` (the exact 0.45 anchor alpineStep itself fires on) + real terrain NOT
  genuinely elevated (reuses temperateMountainTerrainAuthority's own test: centerHeight ≥ seaLevel+25 or
  robustDelta ≥ 8 — no new magic numbers) → skip the repaint, keep the lawful pick. Conservative fallback:
  when preview terrain is synthetic (`realTerrainKnown=false`, which includes LIVE worldgen contexts under
  the default skip-preview sysprops), the veto dominates — live consumer-on play keeps base picks on
  warm-band mountains (alpine SNOW still applies via the elevation-driven surface mixin); only
  terrain-probed contexts exercise the grove/snowy_slopes branch. ClimateAuthority/kAlt/alpineStep
  UNTOUCHED (locked); both overloads exact twins; both call sites still flag-gated. compile + full suite
  green. Test-writer (Sonnet): new pure-JVM `ClimateClassAlpineFamilyTest` (5/5 green) — key invariant
  asserted over ALL enum values (no class's alpineFamily may contain snowy_plains) + a pin on the original
  vanillaFamily data so accidental edits fail loudly. Sweeper (Opus, 9 probes): **ACCEPT-WITH-NOTES** —
  all probes clean (additivity, no import/class-load hazard since ALPINE_ALT is compile-time-inlined,
  sparse-registry fallback returns the pick not vanillaFamily so nothing leaks, P1-A untouched, twins,
  gating, latitude helper character-faithful to the law idiom, hot path arithmetic-only). RISK-1 (moderate,
  by design): the code guarantee is REPAINT-scoped; the map-scoped acceptance ("zero snowy_plains <45° in
  the final consumer-ON map") additionally requires the BASE path to contribute none — baseline A already
  measured base-path snowy_plains <45° = 0 on this seed (all 174 pre-fix cells were reroll-injected), so
  Pass 3's atlas closes RISK-1; do not declare acceptance from code inspection alone. Sweeper note 2:
  registry-level absence is delivered by the Pass-3 atlas metric (LatitudeBiomes is atlas-proven by repo
  convention), not JUnit.
