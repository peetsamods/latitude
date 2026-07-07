# Fable 5 Overhaul Audit — Report (2026-07-06)

`status: final` · charter: `fable5-overhaul-audit-kickoff-20260706.md` · audited at HEAD `d87537f8`
(branch `port/canonical-26.2-pivot`; this report's own commit advances HEAD past that)

Audit shape: 9 parallel read-only lanes (4 deep/Opus, 5 inventory/Sonnet) + a lane-10 code map + 17
director-run headless measurement runs + 2 adversarial refuters (both attacking the audit's own biggest
findings) + an independent synthesis critic (Fable). Every load-bearing claim below was verified against
raw code/docs/git by at least two independent readers (a lane plus the director, or a lane plus a
refuter); claim labels follow the charter taxonomy. Full lane reports and raw measurement JSONs live in
the session scratchpad; everything load-bearing is IN this report (the scratchpad is ephemeral).

---

## 1. Executive Verdict

**NO — Phase 4 cannot close, and Phase 5 must not start yet.**

Not because the wrapper is broken: install coverage, the re-wrap contract, and the engage mechanism are
MECHANICAL-PROVEN and live-corroborated at stress strengths. It is because **Phase 4's own exit criterion
— a calibrated usable strength plus a go/no-go note — is unsatisfiable on the current formula**:

- **P0-1 (refutation-hardened, both axes): the Y-uniform bias has an EMPTY usable strength window.**
  Below S≈0.10 the ground moves ≤±1 block (invisible). At S=0.10, a detached stone slab appears at
  Y256–319 over deep land — on every seed tested (3/3), at the identical bracket (bias 0.0225→0.025
  density units), because the limiting quantity is a near-constant high-altitude density margin of the
  vanilla/Terralith noise config, not local terrain. Even at S=1.0 the ground rises only ~13 blocks while
  the sky is long since solid. The asymmetric escape (`oceanStrengthRatio`) was probed and closed: land
  stays untouched, but the sea floor deepens only 2–6 blocks (waterline pinned) while the deep-ocean
  column hollows into a lava-floored void at Y−64..−55. `HEADLESS-ONLY` (19 runs, 3 seeds, structural
  block stacks) + `LIVE-CONFIRMED` corroboration (the S=0.2 "enormous block" screenshot; the S=1.0
  floating landmass). **A Y-aware taper in the bias formula is a prerequisite for calibration, not a
  tuning option.**
- **P0-2: the standing calibration handoff is a trap.** The binder hands a before/after coordinate
  (`x=9600, z=3372`) derived from a standalone `GeoAuthority(seed=0)` scan — but a live world whose seed
  is literally 0 NEVER builds a real GeoAuthority (`rebuildGeoAuthority` declines at seed 0), so that
  coastline exists in no live world. Following the handoff verbatim reproduces the "nothing at any
  strength" symptom of the just-fixed install bug — or worse, silently shows a PRIOR world's geography
  (P1-1). `CONTRADICTORY` + `DOC-GAP`, verified at `phase4-terrain-wrapper-20260705.md:196-200`,
  `GlobeMod.java:309`, `LatitudeBiomes.java:628`.

**Blocking gate set (smallest sufficient):** G1 = Y-taper slice green headlessly (non-empty window, no
slab/lava, composition tripwires). G2 = state/observability batch landed (stale-provider reset + one-shot
warns; replay probe green). G3 = exactly ONE scripted live session ending in the written go/no-go note.
Details in §7; everything else is scheduled, not blocking.

## 2. Current Truth Table

| Root | Branch / HEAD / tag | State | Docs truth |
|---|---|---|---|
| `Latitude-2.0-26.2-pivot` (ACTIVE) | `port/canonical-26.2-pivot` @ `d87537f8` (audit charter; report commit advances this), tag at `30502e80` = `save/phase4-live-pass-docs-20260706`, ahead of origin 15+ | Dirty: `run-headless/server.properties` ONLY — and its diff is currently NOT timestamp-only (level-seed/type from this audit's own measurement runs; never stage it) | Binder + registry current through 2026-07-06 EXCEPT the P0-2/P0-3 items and the §8 repair map |
| `Latitude (Globe)` (history/front door) | `main` @ `cfd7d115`, ahead 3, clean, no tag | clean | `docs/HANDOFF.md` banner redirects to the 26.1.2 worktree as "active" — itself stale (active line is the pivot); body is pre-2.0 present-tense (P0-3) |
| `Latitude-custom-biome-expansion-26.1.2` | (2.0-beta.1 line, superseded by pivot) | — | Self-scoped correctly (historical; verified by Lane 5) |
| `Latitude-port-1.4.0-1.21.11` | — | Substantial uncommitted, never-pushed WIP (6 modified files + 2 untracked docs) | Existence recorded per Peetsa's instruction; content NOT inspected (out of audit scope) |
| Ports 1.20.1 / 1.21.1 | — | — | Untouched this audit |

Live config of record (today's testing): `geoV2=on`, `terrainV2=on`, `biomeConsumerV2=off` — geography
drives TERRAIN but not biome selection; deliberate and code-confirmed (`LatitudeBiomes.java:2990/:3552/
:3679/:4173` all gated off). All V2 flags are `static final`, read once per JVM (`core/LatitudeV2Flags.java:63-72`).

## 3. Top Risks (deduplicated; likelihood × severity × proof-cost)

### P0 — block Phase 4 closure
1. **Empty usable strength window** (§1; `lane-10` measurements + refute-B). Smallest next proof: none —
   existence is settled; the next proof belongs to the taper slice (G1).
2. **Calibration-handoff trap + live-record seed ambiguity** (§1; synthesis C1). The registry's live rows
   (`:123/:124`) pair "seed 0" with armed terrainV2 — behaviorally impossible as written; the S=10.0 rift
   world's actual seed is recorded nowhere (blank-seed random is the likely reading; a stale-provider
   session is the alarming one). Smallest next proof: re-derive the coordinate at the pinned standard
   seed via the same standalone scan (minutes, no Minecraft; this session's post-correction scan already
   found `x=-3300, z=-3636` at seed `2591890304012655616` — Slice A re-derives to confirm before amending)
   + annotate both rows; henceforth record actual seed + same-JVM world history in armed live evidence.
3. **Front doors misleading-current-truth** (Lane 5, director-verified lines): `docs/LATITUDE_2_0_
   OVERHAUL.md:873-999` Phase-4 kickoff section has zero status annotation (reads as not-yet-executed;
   tail still frames the live pass as future); main-root `docs/HANDOFF.md` redirects to a stale target and
   its body (73%) is unguarded 26.1.2 present-tense ("Immediate next: Continue candidate hardening…",
   line 77). Blocks savepoint progression per the project's own `update-contract.md`. `STALE`+`DOC-GAP`.

### P1 — fix/prove before or during the one live gate
1. **Stale-REAL provider cross-world leak** (Lanes 1+3 converged; refute-A CONFIRMED, mechanism
   sharpened): on world switch, world B's own load re-seeds the provider with world A's seed
   (`setGlobeShape`/`setRadius` fire rebuilds while `WORLD_SEED` still holds A's; `GlobeMod.java:304,
   312-313`) and `setWorldSeed(0)` then declines (`LatitudeBiomes.java:628` has no else). The per-call
   safety check passes on the wrong world's real provider (`GeoTerrainBiasFunction.java:143` tests only
   `instanceof`). Bounds: only seed-0/zero-radius GLOBE world B after a real-seed A in one JVM; vanilla
   worlds immune; real-seed B self-heals. P0-2's protocol prescribes exactly the trigger config.
   Fix shape: ~1-line reset on SERVER_STOPPED (or else-branch reset) + refute-A's single-JVM setter-order
   replay probe. `MECHANICAL-PROVEN` structure, live repro pending.
2. **Zero-observability compound** (four silent modes, one theme — no surface in game, logs, or tooling
   can say whether the bias is active, inert, or stale-wrong): seed-0 decline silent
   (`LatitudeBiomes.java:628`); `terrainV2`-on+`geoV2`-off silently inert (`TerrainRouterWrapping.java:171`);
   `compute()`'s catch-all fully silent AND re-invokes the delegate (uncaught second throw if the delegate
   threw; `GeoTerrainBiasFunction.java:182-185`); Sodium degrade silent (dead `latitude$lastLogMs` field,
   `RenderSectionManagerVisibilityMixin.java:17`; Sodium wholly undeclared in `fabric.mod.json`). Fix
   shape: Slice B one-shot latched warns (the codebase's own `debugAlpine` CAS idiom); the `/latdev
   terrainHere` diagnostic stays DESIGN-ONLY this pass (`../design/live-terrain-diagnostics-design-20260706.md`).
3. **Perf story unverified**: the ColumnMemo fix (`0be832f2`) is correct-by-inspection and not dangerous,
   but no profiling has ever run at S>0 (the Phase 4 Spark baseline at S=0 short-circuits before the
   memo). Proof: one all-thread Spark capture at S>0 vs S=0 vs wrapper-off, piggybacked on the live gate.
   `UNVERIFIED`.
4. **Sodium crash fix not live-confirmed** (registry row `20260706-sodium-client-crash-fix`'s own open
   action). Proof: the live gate boots with the actual Sodium jar.
5. **Tooling manufactures untraceable evidence**: `atlas_runner.py:222` hardcodes `"emitHeight": False`
   into `run_manifest.json` regardless of reality (a real run dir contains `step32_height.png` while its
   manifest says false + `seed: '0'`); no `-D` flag config is recorded anywhere in the bundle; no
   lock/pgrep preflight exists (the concurrent-run crawl already burned a session). This is the same
   disease as P0-2, in the tooling. `MECHANICAL-PROVEN` with live example.

### P2 — scheduled, not blocking
1. Composition absurdity set (drowned-land — the ocean-veto is one-directional, `LatitudeBiomes.java:
   2995-3003`; lifted-ocean; R7 subtropical snow; spawn mismatch) — **unreachable at sub-flip movement on
   the current formula**; re-ranked to the taper slice's acceptance tripwires (Lane 6's composed
   3-assertion probe is the gate). `PLAUSIBLE-RISK`, contingent.
2. blockX/blockZ absoluteness residual — substantially closed by the measurement ladder (formula-exact at
   S>0 through vanilla cell machinery, column-differentiated by land01); one assertion rides in the taper
   re-sweep. Downgraded from Lane 2's P1.
3. Ocean-authority consumer path lacks a provider-realness check (`LatitudeBiomes.java:2990-2993` area) —
   reopens the L17 class if `biomeConsumerV2`+`oceanAuthority` ever flip while a provider is stale/NoOp;
   must close before Phase 5+ flips those flags.
4. ThreadLocal memo pins one dead GeoAuthority per worker thread between worlds (bounded, self-healing,
   low-tens-of-KB; `GeoTerrainBiasFunction.java:93`).
5. Tropical step boundaries 1↔2 and 2↔3 retain the exact zero-blend cliff the `ff713f57` fray fix
   repaired at 0↔1 (fray gated to `baseStep==0`, `LatitudeBiomes.java:2804`) — visual salience unmeasured.
6. Dead/confusable client-code batch: unreachable `ZoneEntryNotifier`/`ui.ZoneTitleOverlay` pair (still
   open `task_7003cfac`), `OverlayProof` (fake "PROOF OVERLAY LIVE" string, zero callers), `GlobeModMenu`
   stub, `EwSandstormOverlayRenderer` vs live `EwSandstormOverlayHud` naming collision, 5 unregistered
   mixin files — plus a deliberate decision needed on the 90-file orphaned `data/globe/worldgen/
   density_function/` JSON tree (verified fully orphaned; delete or label).
7. Remaining doc staleness (P1-severity content, P2 urgency): `current-state-handoff-20260701.md` +
   `test1-live-findings-20260701.md` self-label as active resume pointers but froze 41+ commits ago;
   `canonical-26.2-pivot-20260702.md` + `VERSION_MATRIX.md` frozen at TEST 20; the binder index's own
   freshness table has 4/6 rows past due while marked `active` (violates `update-contract.md`).
8. **No per-world persistence of strength/formula version** (synthesis-critic discovery M1 — all lanes
   missed it): `LatitudeWorldState` persists radius/shape but nothing terrainV2; the same world reopened
   under a different strength (or the post-taper formula) generates new chunks under a different terrain
   law → permanent chunk-boundary shear. Immediate discipline: fresh world per config; after the taper
   lands, ALL pre-taper armed test worlds are invalid for armed testing. Future: persist + warn (noted in
   the diagnostics design).
9. Hygiene notes: `server.properties` is git-tracked and real settings have ridden in it before (commit
   `1fe61223`) — never stage it in unrelated commits; Art VI bound on the fray amplitude (per-block hash
   is the project's documented-forbidden idiom, tolerated at amplitude 0.08 — record the bound).

## 4. Blind Spots Found (by category)

- **Live proof:** the mechanical gate + 6-lens sweeper could not reach the world-load ordering window
  (L18, structural); the S=0 Spark baseline could not see engaged-wrapper cost; NO proof leg ever covered
  an already-marginal high-altitude column — the slab was invisible to every prior gate (the harness's
  density ladder stops at Y=100 while the slab lives at Y256-319; only `getBaseHeight`/structural stacks
  see it, and nothing asserted on them).
- **Performance:** memo efficacy reasoned, never measured; no all-thread capture exists at S>0.
- **Flags/state lifecycle:** no teardown hook for ANY worldgen static (providers, seed, radius, shape);
  "one JVM = one world" was silently assumed by a prior sweeper disposition and is false (dev, tests,
  world-switching); flag realness ≠ state liveness recurred (L17 class) in the ocean-authority path.
- **Obsolete code:** nothing re-triggerable (E/W teleport wrap and ocean-seam sink NEVER EXISTED in this
  repo — they lived in the 26.1.2 worktree; director-verified), but a live E-W fog/border/render-distance
  hazard system is easily confused with the scrapped wrap, and the inert-confusable batch (P2-6) invites
  false trails.
- **Docs:** the front doors lied (P0-3); the live-pass rows under-specify the one variable (actual seed +
  JVM world history) the two biggest state findings hinge on (P0-2); resume pointers point backwards.
- **Tooling:** proof bundles don't self-describe (P1-5); no contention preflight; no Classic-vs-Mercator
  comparison support; seed-0 warnings nowhere; height export is emit-only-nobody-reads (`ATLAS-BLIND`).
- **Compatibility:** exactly one third-party-target mixin exists (Sodium) — fragility contained but
  silent and undeclared; the require=1 default means any future third-party target is a crash-on-update.
- **Design assumptions:** the Y-uniform bias implicitly assumed the density field's response to a
  constant offset is surface-local — false above land (near-zero high-altitude margin ⇒ slab) and inert
  below the sea (waterline pinning ⇒ no sink without voids). The formula needs altitude awareness (G1).

## 5. Obsolete/Problematic Code Resurgence Report

Classification per charter (Lane 4, spot-verified): **IMPOSSIBLE** — E/W teleport wrap, ocean-seam sink
(never in this repo), amplitude-wrapper-as-code (zero artifacts; concept-confusion risk only).
**INERT-BUT-CONFUSING** — 90-file orphaned DF JSON tree (verified zero references from active noise
settings), `ZoneEntryNotifier`/`ZoneTitleOverlay`, `OverlayProof`, `GlobeModMenu`, `EwSandstormOverlay
Renderer`, 5 unregistered mixins. **RE-TRIGGERABLE** — none found. **CURRENTLY-ACTIVE** (correctly, but
watch for confusion) — the E-W fog/border hazard system (distinct from the scrapped wrap), `OceanDistance
Field` (50+ call sites; its parked replacement is double-flag-gated and accidental-enable was REFUTED),
and the Phase 4 wrapper itself. Inert-but-dangerous-to-trust: the orphaned DF tree and `OverlayProof`'s
fake proof string are the two most likely to mislead a future worker or agent.

## 6. Phase 4 Go/No-Go

**Mechanically closed only — NOT live-closed.** The wrapper mechanism is proven (install coverage across
all `RandomState` call sites, per-call provider check, mapChildren re-wrap contract, bounds contract) and
live-corroborated at stress strengths. The exit criterion is unsatisfiable on the current formula (P0-1),
the prescribed calibration protocol misfires (P0-2), and cross-world state hygiene (P1-1) plus
observability (P1-2) would contaminate any live evidence gathered meanwhile. **Exact gates before
Phase 5: G1 (taper slice green headlessly), G2 (state/observability batch + replay probe), G3 (one
scripted live session → written go/no-go note).** Phase 5 ("Boundary Experience") tunes the same
edge/pole surface Phase 4's wrapper sinks (design R1) — starting it against an uncalibratable baseline
compounds two unproven edge effects.

## 7. Strategic Path Forward (five bounded slices; fix-all-then-test-once)

- **Slice A — Truth restore (docs only).** Objective: front doors and handoffs stop lying; P0-2
  corrections recorded. Work: §8's repair map; re-derive the calibration coordinate at the pinned
  standard seed (standalone scan, no MC) and amend the binder handoff to "create the world by TYPING this
  seed"; annotate registry rows `:123/:124` with the actual-seed ambiguity. Forbidden: any code.
  Proof gate: grep assertions. Stop: mapped repairs applied, nothing more.
- **Slice B — State + observability hardening (small code batch).** Objective: stale-real leak dead;
  every silent inert/failure mode gets one latched log line. Work: provider/seed/radius reset on
  SERVER_STOPPED (or else-branch reset); one-shot warns (seed-0 decline, terrainV2-on+geoV2-off); latched
  warn in `compute()`'s catch with `base` computed before the risky section (kills the delegate
  re-invoke); wire the Sodium mixin's dead log field into a skip log. Forbidden: formula/tuning/biome
  logic. Proof gate: S=0 byte-identity green; refute-A's setter-order replay probe asserts world B gets
  NoOp; warn lines observable headlessly. Stop: any byte-identity diff → halt.
- **Slice C — Y-taper prerequisite (the core slice).** Objective: make a usable strength window EXIST,
  proven headlessly before anyone launches a client. Work: Y-aware taper at the single bias-add line
  (`GeoTerrainBiasFunction.java:181`; the column memo stays valid — it caches Y-independent land01);
  minimal harness extension (caller X/Z probes + Y sampling to 320, per the diagnostics design §4);
  fix-or-assert the one-directional ocean veto if terrain now crosses the waterline. Forbidden: consumer/
  ocean-authority flips; atlas pipeline; any live run. Proof gate: re-run this audit's full sweep matrix
  (3 seeds × S ladder + r probes) — no slab at any S ≤ chosen max, no lava voids at r ≤ 30, ground
  response ≥ a named target (e.g. 3–8 blocks) at some S, Lane 6's three composition tripwires green,
  coordinate-absoluteness assertion green. Stop: window still empty post-taper → STOP and redesign; do
  not tune forward.
- **Slice D — Tooling truthfulness (parallel; needs its own authorization — tooling was design-only this
  pass).** Manifest records actual sysprops + real emitHeight; `biomes.txt` echoes V2 flags; pgrep/
  session.lock preflight. Proof gate: one smoke run whose manifest matches its invocation.
- **Slice E — THE one live gate (the only manual session).** Fresh JVM; Sodium installed; world created
  by TYPING the pinned real seed; Mercator regular at Slice C's calibrated strength. Checklist only:
  (1) Sodium boot no-crash; (2) coastline believability at the re-derived coordinate; (3) R7
  subtropical-peak snow check at headless-pre-located coords; (4) spawn sanity; (5) second world —
  Classic, same seed: edge-ring parity + live exercise of the post-fix world-switch path; (6) all-thread
  Spark during a pregen burst at S>0 vs S=0. Forbidden: strength dialing on a reused world (P2-8); any
  fix-as-you-go edits. Proof gate: the roadmap's terrain go/no-go note, written and indexed. Stop: any
  tripwire fails → back to Slice C; else **Phase 4 CLOSED**.

Sequencing: A ∥ B (tiny) → C → D when authorized → E once. Total manual burden: one ~45-minute session.

## 8. Documentation Repair Map (map only — nothing edited this pass)

| Doc | Wrong/missing truth | Repair |
|---|---|---|
| `docs/LATITUDE_2_0_OVERHAUL.md:873-999` | Phase-4 kickoff section reads as not-yet-run | Prepend a dated STATUS block: executed 2026-07-05/06; mechanically complete; live pass found+fixed install-timing (`95bca16c`) + perf (`0be832f2`); calibration BLOCKED on empty-window finding → this report |
| `Latitude (Globe)/docs/HANDOFF.md` | Banner redirects to 26.1.2 worktree as active; body is pre-2.0 present-tense; "Immediate next" prescribes dead work (line 77) | Re-point banner through to the pivot root + this binder; demote body under an explicit HISTORICAL guard |
| `docs/binder/phase4-terrain-wrapper-20260705.md:196-200` | Calibration coordinate derived at seed 0 — no live world can have it | Amend with the re-derived standard-seed coordinate + "TYPE the seed" protocol + a pointer to this report's P0-2 |
| `docs/binder/evidence-registry.md` rows `:123/:124` | Record "seed 0" live observations with armed terrainV2 (behaviorally impossible as written); actual seed unrecorded | Append-only annotation row(s) noting the ambiguity; new discipline: record actual seed + same-JVM world history in armed live evidence |
| `current-state-handoff-20260701.md`, `test1-live-findings-20260701.md` | Self-labeled active resume pointers, frozen pre-pivot | Supersession banners pointing at the pivot front door |
| `canonical-26.2-pivot-20260702.md`, `docs/porting/VERSION_MATRIX.md` | Frozen at TEST 20 / HEAD `61d51782` | Refresh or banner as point-in-time |
| `docs/binder/index.md` topic table | 4/6 rows past `Next review due` while marked `active` | Flip to `stale` or re-review per `update-contract.md` |
| Cross-session memory (director-owned) | Carries the seed-0 coordinate as "the handoff" + stale alpine constants (180/184 vs code's 168) | Fix both memory entries (owned by the director, done in the same pass as this report) |

## 9. Subagent Appendix

- **Wave 1 (9 read-only lanes):** L1 Phase-4 proof-gap (Opus), L2 terrain/perf (Opus), L3 flag/state
  (Opus), L6 composition (Opus), L4 obsolete/resurgence, L5 docs coherence, L7 tooling/evidence, L8
  client/compat, L10 tooling map (Sonnet). All returned evidence-first reports; none rejected outright.
- **Director verification:** every P0/P1 code claim spot-checked at the raw file (catch-all, rebuild
  guard, stop handler, fray gate, veto, both doc P0s, constants, manifest line, Sodium mixin, `1fe61223`).
  Corrections issued: L8's "fires every call" phrasing (wraps every call; fires on throw — plus the new
  delegate-re-invoke nuance); the director's OWN lane prompts fed stale alpine constants from memory
  (168 in code, not 180/184) — lanes rightly trusted code.
- **Director measurements (Lane 10):** 17 sequenced foreground terrainProof runs (flip ladder 0.05–1.0,
  3-seed replication, S=0.09 bracket tightening, r=10/30 asymmetric probes), structural-stack analysis.
- **Wave 2 (adversarial refuters, Opus):** refute-A (stale provider) — CONFIRMED, could not refute,
  mechanism sharpened (B re-seeds with A's seed), scenario matrix + replay probe designed. refute-B
  (empty window) — CONFIRMED in corrected form (detached slab, not sky-fill; ground under-response;
  gradient-variance caveat scoped; r-axis probes requested → run → closed with lava-void evidence).
- **Wave 3 (synthesis critic, Fable):** adopted in full — C1 (seed-0 record contradiction + handoff trap,
  NEW), C2 (blockX/Z downgrade), C3 (absurdity set re-ranked to taper tripwires; L1's calibration-only
  live gate retired), C4 (seed-0-silence tier + attribution fix), dedup pass (4 convergence groups
  counted once), M1–M4 completeness findings (per-world strength persistence gap being the standout).
- Rejected/retired en route: L1's recommended live gate as originally scoped (superseded by the
  empty-window result); L2's blockX/Z P1 tier; L7's P0 tier for seed-0 silence; the director's own
  pre-refutation "sky-fill" framing and single-bracket cross-seed claim (tightened by extra runs).
- Cost shape: ~2.1M subagent tokens across 12 agents + 19 director-run headless measurements.

## 10. Plain-English Note to Julia

The wrapper works — that part is real and proven. But the knob you were asked to tune has no good
setting: below ~0.1 it does almost nothing you can see, and at 0.1 it doesn't make mountains — it makes
a solid stone shelf in the sky (your "enormous block" photo was exactly that, and it happens on every
seed at the same value, so it was never going to be tuned around). Pushing the ocean side harder instead
just carves lava-floored caverns under the seabed. So the honest answer to "what strength should I use?"
is: none exists yet. The fix is a known, small change (make the push fade with altitude), and we can
prove it works headlessly before you ever launch the game again.

Two traps were also disarmed on paper before they cost you a session: the coordinate you were handed for
before/after testing only exists on seed-0 worlds — which are exactly the worlds where the whole system
stays off (that's also why some of yesterday's records say "seed 0" while showing terrain effects, which
can't both be true — most likely those worlds actually had random seeds); and if you open a real world
and then a seed-0 world in the same game session, the second world silently borrows the first one's
geography. Both fixes are tiny. The plan ahead is three small proven-at-the-desk batches (docs truth,
state+logging, the altitude fade), then ONE scripted ~45-minute live session with a typed seed and a
short checklist — and that session, not vibes, decides whether Phase 4 is closed and Phase 5 begins.
