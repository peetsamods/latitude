# Fable 5 Overhaul Audit — Kickoff Charter (2026-07-06)

`status: active` — audit in progress in the authoring session

## Provenance and execution mode

- Base draft authored by GPT-5.5, supplied by Peetsa on 2026-07-06. Corrected and expanded the same day by
  Fable 5 against fresh-verified repo state (every git fact below was re-checked read-only immediately
  before this charter was committed; the charter commit itself bumps the pivot's ahead-count by one).
- Peetsa's explicit decision (AskUserQuestion, 2026-07-06): the audit runs IN the authoring session with
  Fable 5 as director — not handed to a fresh session. Subagent lanes are fresh-context by construction,
  which carries the auditor-independence intent.
- Binding scope answers from Peetsa: the debugging-tools lane is **DESIGN ONLY** (no implementation without
  separate explicit authorization); the `Latitude-port-1.4.0-1.21.11` uncommitted WIP is **flagged for
  existence only** — its content is NOT inspected.
- Deliverables land as `docs/binder/fable5-overhaul-audit-report-20260706.md` +
  `docs/design/live-terrain-diagnostics-design-20260706.md`, indexed per binder discipline
  (`index.md` + `evidence-registry.md` in the same commit).

The directive below is the GPT-5.5 draft with corrections applied in place. Where this charter contradicts
the original draft, this charter is right (each correction was verified against the repo on 2026-07-06);
where the draft's claims are kept as *verification targets*, they are marked UNVERIFIED rather than
asserted.

---

You are Fable 5 acting as director of a deep Latitude 2.0 overhaul audit for Julia.

Julia has only a narrow window to use you. Do not waste it on patching random symptoms. Your job is to
direct a rigorous, adversarial, read-first audit that finds blind spots, latent risks, stale docs, obsolete
code paths, performance traps, and strategic next steps before Latitude proceeds further.

## Mission

Audit the Latitude 2.0 overhaul and answer:

1. Is Phase 4 actually closed enough to move into Phase 5?
2. What hidden assumptions, stale docs, dead code, or obsolete paths could re-trigger old failures?
3. What performance, lifecycle, proof, compatibility, or live-client risks are still latent?
4. What documentation gaps exist across Latitude iterations/worktrees?
5. What is the strongest strategic path forward, with targeted proof gates instead of endless
   whack-a-mole testing?

You are the director. Fan out to subagents where useful. Critically assess all subagent work. Do not
accept conclusion-only reports.

## Scope And Safety

Start read-only.

Do not edit code, docs, configs, jars, profiles, worlds, tags, branches, or evidence unless Julia
explicitly authorizes a later implementation pass. The ONLY writes permitted in this pass are the audit's
own new documents (this charter, the final audit report, the live-diagnostics design doc) plus their
`index.md`/`evidence-registry.md` entries, committed as plain docs commits. Stale docs are MAPPED in the
report, never repaired in this pass.

Do not run concurrent headless worldgen jobs against the same `run-headless/world`. Before any headless
run, confirm no prior one is still alive (`ps aux | grep -iE "runBiomePreview|gradle"`).

Short measurement runs (the `-Dlatdev.terrainProof` harness class, ~10 s each) are permitted: run by the
director personally, foreground, one at a time. NO atlas-scale runs (~28 minutes each on this hardware,
per this session's own measured history) without separate explicit authorization from Julia.

Do not let subagents background long commands or use `nohup`/drivers that hide state. Long commands must
run foreground with visible output or be orchestrated centrally. Additionally, per this repo's CLAUDE.md:
never delegate a multi-step sequential pipeline with state changes between steps to a subagent — the
director sequences each state-changing step personally. Audit-lane subagents in this pass are restricted
further: read/grep/git-log only, no gradle, no java, no world-gen of any kind.

If a claim depends on live Minecraft behavior, label it as live-unverified unless actual live evidence
exists.

## Roots To Inspect

Observed state below was verified read-only on 2026-07-06 immediately before this charter was committed.
Re-verify fresh at each use; treat drift from these numbers as a finding, not an obstacle.

Primary historical/front-door root:

`/Users/joolmac/CascadeProjects/Latitude (Globe)`

Observed state (corrected from the draft, which said `f2addbfc`/ahead 2 — stale by one commit):
- branch: `main`
- HEAD: `cfd7d115` (`docs(lessons): L18 + L19`)
- ahead of origin by 3, working tree clean, no tag at HEAD
- current `docs/HANDOFF.md` may be stale/pre-2.0 relative to the active pivot — UNVERIFIED, an explicit
  Lane 5 target

Primary active 2.0 root:

`/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot`

Observed state (corrected from the draft, which predated the live-pass docs commit):
- branch: `port/canonical-26.2-pivot`
- HEAD: `30502e80` (`docs: record Phase 4 live-pass findings + today's two unrelated fixes`)
- tag at HEAD: `save/phase4-live-pass-docs-20260706`
- ahead of origin by 14
- dirty tracked file observed: `run-headless/server.properties`, timestamp-only diff (benign runtime
  artifact, an established pattern in this project — but Lane 7 should still confirm nothing else ever
  rides along with it)
- important post-Phase-4 commits, newest first:
  - `30502e80` — live-pass findings + same-day fixes documented (binder/index/registry)
  - `0be832f2` / tag `save/terrain-column-cache-perf-fix`
  - `95bca16c` / tag `save/terrain-install-timing-fix`
  - `ff713f57` — arid/savanna step-boundary fray fix
  - `f63999d5` — CLAUDE.md multi-step-pipeline + resource-contention guidance
  - `abec67b8` — Sodium 0.9.0 missing-mixin-target crash tolerance

Also inspect, as needed for iteration drift:
- `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`
- `/Users/joolmac/CascadeProjects/Latitude-port-1.4.0-1.20.1`
- `/Users/joolmac/CascadeProjects/Latitude-port-1.4.0-1.21.1`
- `/Users/joolmac/CascadeProjects/Latitude-port-1.4.0-1.21.11` — NOTE: this worktree has substantial
  uncommitted, never-pushed work-in-progress (6 modified files including `GlobeMod.java` /
  `LatitudeBiomes.java` / a dev command file, plus 2 untracked docs incl. `CODEX_CRUISE_CONTROL.md`).
  Per Peetsa: record that it exists and where, for awareness; do NOT spend audit budget inspecting its
  content.

## Required First Reads

In `/Users/joolmac/CascadeProjects/Latitude (Globe)`:
- `AGENTS.md`
- `docs/HANDOFF.md`
- `docs/LESSONS.md`

In `/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot`:
- `AGENTS.md`
- `CLAUDE.md`
- `docs/LATITUDE_2_0_OVERHAUL.md`
- `docs/binder/index.md`
- `docs/binder/evidence-registry.md`
- `docs/binder/phase4-terrain-wrapper-20260705.md`
- `docs/design/terrain-wrapper-design-20260705.md`
- `docs/binder/phase4-prep-research-20260705.md`
- `docs/binder/test1-live-findings-20260701.md`
- `docs/binder/current-state-handoff-20260701.md`
- `docs/binder/canonical-26.2-pivot-20260702.md`
- `docs/binder/model-effort-strategy-20260702.md`
- `docs/binder/sweeper-audit-standing-practice-20260705.md`
- `docs/porting/PORTING.md`
- `docs/porting/VERSION_MATRIX.md`
- `docs/porting/PORTABILITY_ARCHITECTURE.md`

## Known Context To Verify, Not Assume

Latitude 2.0 roadmap includes Phase -2, -1, 0, 1, 2, 3, 4, 5, and 6.

Phase 4 is the terrain-wrapper phase. It was mechanically completed — including a full mechanical proof
gate AND a 6-lens adversarial sweeper audit — and live testing STILL exposed two serious gaps:
- The terrain wrapper did not install in fresh live worlds because the install-time provider check ran
  before the real provider was rebuilt (`ChunkMap`-constructor mixin fires before `GlobeMod`'s
  create-world flow rebuilds `GEO_V2_PROVIDER`). Neither prior proof could reach this: the dev/atlas
  proof path always builds its `RandomState` on an already-loaded `ServerLevel`. Recorded as LESSONS L18.
- Once the wrapper engaged, `GeoAuthority.sample(x,z)` caused a real chunk-generation performance
  regression because it was re-run once per vertical noise-lattice cell instead of once per column.
  Invisible to the strength-0 Spark baseline (strength 0 short-circuits the path entirely).

Fixes landed (audit subjects, NOT settled facts — do not assume they close Phase 4):
- `95bca16c`: moved NoOp-provider safety from install-time to per-call
  (`GeoTerrainBiasFunction.compute()`).
- `0be832f2`: per-thread column memo caching `GeoAuthority.sample()`, invalidated on provider change.

Also landed the same day, unrelated to Phase 4 (both are Lane 8 / Lane 6 audit subjects respectively):
- `abec67b8`: Sodium 0.9.0+mc26.2 crashed the whole client via a compat mixin targeting a
  removed internal method (`RenderSectionManager.isSectionVisible(III)Z`); made tolerant via `require=0`
  (silent skip). Whether silent skip is acceptable is an explicit Lane 8 question.
- `ff713f57`: dead-straight arid/savanna boundary — `pickTropicalGradient`'s subtropical ladder picks a
  discrete step via `floor(tJitter*4)` with zero blending between the arid and savanna steps; fixed by
  widening the existing step-dither band at that boundary. Verified composition-neutral (arid
  0.884%→0.885%, savanna 9.590%→9.588%) with a real measured fray increase (8.6%→12.7% of boundary
  edges).

**Brand-new finding, in NO prior proof or audit (Lane 10 owns this):** direct headless measurement on
2026-07-06 (`-Dlatdev.terrainProof`, seed `2591890304012655616`, strengths 0.0/0.05/0.1/0.2) found
`baseHeight` at a real mountain-peak-adjacent land column unchanged at S=0.0 and S=0.05 (92), then jumping
to the literal world ceiling (320) at S=0.1 and staying there at S=0.2 — a cliff-edge threshold, not a
gradual response. Raw density samples at every coarse Y sampled show the bias itself IS smooth and linear
(`S × 0.25` exactly, as the formula predicts); the flip must occur above the coarse sample range,
consistent with an already-marginal high-altitude density sliver (a tall peak) tipping fully positive from
a small constant offset. Ordinary flat/coastal/ocean columns showed ZERO `baseHeight` change across the
same strengths. Status: HEADLESS-ONLY, single column, single seed — needs characterization before any
strength value can be called safe. This is also why "0.05 looked fine" while a mountain flipped: the
response is terrain-dependent.

Known proof blind spots (structural, confirmed by the above):
- Atlas/headless mechanical proof did not catch the fresh-world install-timing problem (cannot reach the
  ordering window).
- Spark baseline at strength 0 could not catch engaged-wrapper performance (strength 0 short-circuits).
- The terrainProof harness auto-selects representative columns by land01; it accepts NO caller-specified
  X/Z, so it cannot be pointed at a live-observed anomaly, and its column choice missed the marginal
  mountain-peak case that produces the cliff-edge behavior.

Known docs state (CORRECTED from the draft as of 2026-07-06 end-of-day — do not inherit the draft's
staleness claims):
- CLOSED, verified: `docs/design/terrain-wrapper-design-20260705.md` is NOT pre-implementation-stale — its
  residual-risk register has R8/R9 recorded closed and R4 updated (2026-07-06).
- CLOSED, verified: `docs/binder/phase4-terrain-wrapper-20260705.md` DOES include the live install/perf
  fixes ("Live pass findings (2026-07-06)" section).
- CLOSED, verified: the evidence registry HAS rows covering the fixes
  (`20260706-phase4-terrain-live-pass-findings`, plus `20260706-sodium-client-crash-fix` and
  `20260706-arid-savanna-boundary-fray`).
- UNVERIFIED (Lane 5 target): main root `docs/HANDOFF.md` may still point to older 26.1.2 active-line
  truth.
- UNVERIFIED (Lane 5 target): `docs/LATITUDE_2_0_OVERHAUL.md` may contain stale status around Phase 4
  kickoff/live retest (one subagent flagged a specific line this session; never independently confirmed).

Process context lanes must obey: this repo's `CLAUDE.md` gained rules on 2026-07-06 after real failures —
subagents must not background long commands (they cannot be woken); multi-step pipelines with state
changes between steps are sequenced by the director, never delegated; two headless world-gen processes
must never run against the same world directory (they contend on `session.lock` and both crawl,
indistinguishable from a hang).

## Mandatory Subagent Lanes

Assign separate subagents where possible. Use the model you deem appropriate, but default to cheaper
mechanical models for grep/inventory and high/max reasoning models for architecture/proof synthesis.

1. Phase 4 Live/Proof Gap Auditor
   - Audit whether Phase 4 is truly live-closed.
   - Focus on install timing, fresh-world provider lifecycle, mechanical proof blind spots, and what
     exact live/headless proof is still needed.
   - Determine whether Phase 5 can start safely.

2. Terrain And Performance Auditor
   - Inspect `GeoTerrainBiasFunction`, router wrapping, provider sampling, cache invalidation,
     thread-local behavior, and chunkgen hot paths.
   - Check whether the cache fix is correct, insufficient, or dangerous.
   - Identify targeted profiling proof needed, especially all-thread Spark or counters on an unloaded
     machine.

3. Flag/Gating/State Lifecycle Auditor
   - Audit `geoV2`, `climateV2`, `biomeConsumerV2`, `oceanAuthority`, `terrainV2`, strength flags,
     seed/profile differences, and NoOp provider behavior.
   - Look for flags that say "enabled" while guarded state is not actually live.
   - Look for process-global/static provider leaks across worlds or seeds.

4. Obsolete Code And Resurgence Auditor
   - Search for dead/scrapped paths that could accidentally reattach:
     - E/W teleport wrap
     - ocean seam terrain sink
     - orphaned data-pack density functions
     - amplitude-wrapper leftovers
     - unreachable title/notification overlay systems
     - old dev tooling that implies false proof
   - For each, classify: impossible, inert but confusing, re-triggerable, or currently active.

5. Docs Coherence Across Iterations Auditor
   - Compare main root, active 2.0 pivot, 26.1.2 worktree, and port roots.
   - Identify stale handoffs, stale binder entries, missing evidence registry rows, misleading "done"
     language, outdated phase state, and conflicts between docs and git.
   - Separate historical notes from current truth.
   - Includes the two UNVERIFIED doc-staleness targets named above.

6. Biome/Climate/Geo Composition Auditor
   - Revisit the failures from lessons L13/L14:
     - hidden land/ocean union assumption
     - climate/classification fallthrough
     - aggregate proof missing localized absurdity
   - Audit remaining risks in biome consumer, ocean authority, arid/savanna fray, subtropical snow,
     poles, coastlines, and altitude proxy vs actual terrain.

7. Tooling/Evidence Auditor
   - Audit atlas/headless/live proof tools.
   - Check height export, exact-ID analyzer behavior, stale `server.properties`, world locks, seed
     handling, Classic vs Mercator comparisons, and whether proof names match actual config.
   - Identify how proof should be hardened so Julia stops rediscovering the same bugs manually.

8. Client/Compatibility Auditor
   - Audit Sodium compatibility fix, missing mixin target behavior, client crash risk, and whether
     silent skip is acceptable.
   - Check UI/HUD/map/title systems for stale or unreachable code that could confuse live proof.

9. Strategic Synthesis Critic
   - After all lanes report, independently challenge the findings.
   - Look for missed interactions between lanes.
   - Rank risks by likelihood, severity, and proof cost.
   - Produce a path forward that minimizes Julia's manual testing burden.

10. Terrain Bias Threshold & Live Diagnostics Auditor (NEW — measurement plus DESIGN-ONLY tooling)
    - Own the 2026-07-06 cliff-edge finding. Characterize it across more columns, more seeds, both world
      shapes, and a finer strength ladder: is the world-ceiling flip specific to already-marginal mountain
      peaks, or does ordinary terrain also flip at higher strengths? Where does the threshold sit for a
      range of representative terrain types, to the extent it is pin-downable? Measurement mechanism: the
      existing `-Dlatdev.terrainProof` harness (short headless runs, DIRECTOR-run, foreground, sequenced —
      subagents never run these).
    - Design (do not build) the harness extension: accept explicit caller-specified X/Z probe coordinates,
      so a live-observed anomaly can be reproduced headlessly on demand.
    - Design (do not build) a live in-game diagnostic. Confirmed gap, verified read-only 2026-07-06: there
      is NO live way for a player to see `GeoAuthority.land01` at their position, the terrain density bias
      being applied there, or how close the local column is to the flip threshold — everything requires a
      headless run and reading a JSON file. Reusable in-repo patterns exist: `latitude.debugBlend`'s
      structured per-column logging, `latitude.debugAlpine`'s one-shot latch, the `/latdev here` /
      `/latdev explainHere` command registration, and `TerrainRouterWrapping.installIfArmed()`'s
      gate-check machinery. Target shape: a `/latdev terrainHere`-style command reporting, at the player's
      column: `land01`, bias direction/magnitude at the configured strength, actual `finalDensity` at a
      few Y levels around the player, a same-column strength-0 baseline computed on demand (so the delta
      is visible in chat without two game launches), and a proactive threshold-proximity warning (sample
      density across the full Y range; warn before a column becomes a world-ceiling pillar).
    - Deliverable: `docs/design/live-terrain-diagnostics-design-20260706.md`, implementation-ready (exact
      command name, exact fields shown, exact hook points into existing code) so an authorized follow-up
      session can build it without re-deriving the approach.

## Required Claim Labels

Every important claim must be tagged:

- `LIVE-CONFIRMED`
- `MECHANICAL-PROVEN`
- `HEADLESS-ONLY`
- `ATLAS-BLIND`
- `DOC-GAP`
- `STALE`
- `CONTRADICTORY`
- `UNVERIFIED`
- `PLAUSIBLE-RISK`
- `ACCEPTED-RISK`
- `BLOCKED`

Do not blur these categories.

## Subagent Return Format

Each subagent must return:

- Verdict
- Files/docs inspected
- Exact evidence with paths and line references where possible
- Commands run, if any
- Confirmed findings
- Plausible but unproven risks
- Refuted concerns
- Severity ranking
- Recommended next proof gate
- Residual uncertainty

Reject or send back any subagent answer that gives vibes without evidence.

## Final Deliverable To Julia

Produce a concise but thorough audit report with these sections:

1. Executive Verdict
   - Can Latitude move from Phase 4 to Phase 5?
   - If no, exactly what is the blocking proof gap?

2. Current Truth Table
   - Root, branch, HEAD, tag, dirty state, active docs truth, and stale docs truth.

3. Top Risks
   - P0/P1/P2 ranked list.
   - Each item must include evidence, why it matters, and the smallest next proof.

4. Blind Spots Found
   - Group by live proof, performance, flags/state lifecycle, obsolete code, docs, tooling,
     compatibility, and design assumptions.

5. Obsolete/Problematic Code Resurgence Report
   - What can re-trigger old bad behavior?
   - What is inert but dangerous because future workers may trust it?

6. Phase 4 Go/No-Go
   - Say whether Phase 4 is live-closed, mechanically closed only, or not closed.
   - Identify the exact proof gate needed before Phase 5.

7. Strategic Path Forward
   - Give 3-5 bounded slices.
   - Each slice must have objective, allowed work, forbidden lanes, proof gate, and stop condition.
   - Prefer proof-system hardening and targeted live checks over broad manual retesting.

8. Documentation Repair Map
   - List stale/missing docs and what truth each should contain.
   - Do not edit them unless Julia authorizes.

9. Subagent Appendix
   - Summarize who audited what, what you accepted, what you rejected, and why.

10. Plain-English Note To Julia
    - Explain what this means without requiring her to parse code.

## Success Standard

This audit fails if it ends with "just test everything again."

It succeeds only if it reduces the problem into targeted proof gates, identifies the highest-risk blind
spots, separates live truth from proxy proof, catches stale docs across iterations, and gives Julia a sane
path forward that does not keep recreating the same whack-a-mole loop.
