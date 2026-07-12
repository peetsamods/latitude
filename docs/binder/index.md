# Latitude Documentation Binder

`status: active`

## Purpose
Provide one canonical map for Latitude documentation, record-keeping, and proof evidence links.

## Start here for Latitude 2.0 overhaul work
- `docs/LATITUDE_2_0_OVERHAUL.md` — comprehensive project overhaul plan, Minecraft `26.2` canonical-pivot strategy, portability spine, projected-planet world-shape decision, GeoAuthority/ClimateAuthority contracts, roadmap, proof gates, and Fable 5 handoff.
- `docs/binder/longitude-earthlike-world-overhaul-20260702.md` — dated research/hardening log behind the overhaul plan.
- `docs/porting/PORTING.md` — porting front door for the `26.2` pivot and future Minecraft versions.
- `docs/porting/VERSION_MATRIX.md` — current version target matrix.
- `docs/porting/PORTABILITY_ARCHITECTURE.md` — target core/adapters/mixins split for easier future ports.

## Global freshness policy
- A binder entry is `stale` when it exceeds the stale thresholds in `update-contract.md`.
- Stale sections block savepoint/release progression until refreshed with valid evidence.
- Canonical docs are never replaced; they are deprecated with `supersedes` in the evidence registry.

## Canonical topic map (single source of truth)

| Evidence topic | Canonical source-of-truth doc | Allowed sibling sources | Default scope | Last reviewed | Freshness status | Next review due |
| --- | --- | --- | --- | --- | --- | --- |
| worldgen policy and biome behavior | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/binder/tropical-rebalance-report-20260605.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/binder/tropical-rebalance-report-20260605.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/biomes-coverage.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/biomes-coverage.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/binder/atlas-warm-dry-balance-report-20260605.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/binder/atlas-warm-dry-balance-report-20260605.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/26.1.2-canonical-roadmap.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/26.1.2-canonical-roadmap.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/analysis/atlas_worldsize_analysis.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/analysis/atlas_worldsize_analysis.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-final-status.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-final-status.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856/worldgen-spawn-stall-fix-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856/worldgen-spawn-stall-fix-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856/existing-load-overlay-and-high-column-terrain-fix-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856/existing-load-overlay-and-high-column-terrain-fix-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/code-red-itty-palm-performance-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/code-red-itty-palm-performance-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/alpine-snowline-lower-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/alpine-snowline-lower-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/overrep-analysis-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/overrep-analysis-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/worldgen-jank-earmark-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/worldgen-jank-earmark-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/worldgen-correctness-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/worldgen-correctness-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/worldgen-quickfixes-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/worldgen-quickfixes-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/overrep-polar-enrichment-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/overrep-polar-enrichment-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/overrep-enforcement-warm-medium-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/overrep-enforcement-warm-medium-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/amplitude-df-wrapper-design-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/amplitude-df-wrapper-design-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/worldgen-regression-prevention-20260625.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/worldgen-regression-prevention-20260625.md) | worldgen | 2026-07-02 | active | 2026-07-16 |
| rendering and HUD invariant behavior | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/first-load-message.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/first-load-message.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/fix-history.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/fix-history.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-live-20260618-211834/live/visual-audit-notes.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-live-20260618-211834/live/visual-audit-notes.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856/existing-load-overlay-and-high-column-terrain-fix-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856/existing-load-overlay-and-high-column-terrain-fix-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/code-red-itty-palm-performance-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/code-red-itty-palm-performance-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/compass-themes-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/compass-themes-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/world-creation-bookui-impl-spec-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/world-creation-bookui-impl-spec-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/gui-bookui-compass-bonuschest-20260622.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/gui-bookui-compass-bonuschest-20260622.md), [/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot/docs/binder/hud-studio-custom-theme-20260703.md](/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot/docs/binder/hud-studio-custom-theme-20260703.md), [/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot/docs/binder/hud-studio-title-rainbow-and-polish-20260703.md](/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot/docs/binder/hud-studio-title-rainbow-and-polish-20260703.md), [/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot/docs/binder/canonical-26.2-pivot-20260702.md](/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot/docs/binder/canonical-26.2-pivot-20260702.md) | rendering | 2026-07-03 | active | 2026-07-17 |
| bug blaster investigation and closure | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/fix-history.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/fix-history.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/biomes-coverage.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/biomes-coverage.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/release/checklist.md) | compatibility | 2026-06-04 | stale (flipped 2026-07-06 per update-contract: 18 days past due) | 2026-06-18 |
| compatibility and migration status | [/Users/joolmac/CascadeProjects/Latitude (Globe)/tmp/audits/MIGRATION_STATUS.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/tmp/audits/MIGRATION_STATUS.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/tmp/audits/26-1-mixin-cluster-research](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/tmp/audits/26-1-mixin-cluster-research), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-184901/mod-present-atlas-summary.txt](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-184901/mod-present-atlas-summary.txt) | migration, compatibility | 2026-06-18 | stale (flipped 2026-07-06 per update-contract: past due) | 2026-07-02 |
| release and handoff readiness | [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/current-readiness-audit.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/current-readiness-audit.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/current-gates.json](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/current-gates.json), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/live-proof-runbook.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/live-proof-runbook.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/CHANGELOG.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/CHANGELOG.md) | [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/26.1.2-canonical-roadmap.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/26.1.2-canonical-roadmap.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/lat-readiness-nonlive-status](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/lat-readiness-nonlive-status), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/lat-post140-findings-classifier](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/lat-post140-findings-classifier), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/mc-wait-shot](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/mc-wait-shot), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856) | release | 2026-06-21 | stale (flipped 2026-07-06 per update-contract: past due) | 2026-07-05 |
| live proof and runtime evidence | [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/evidence-registry.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/evidence-registry.md) | /tmp evidence payload folders listed in registry entries; [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-live-20260618-211834/live/visual-audit-notes.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-live-20260618-211834/live/visual-audit-notes.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-final-status.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-final-status.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-status.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-status.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/screen-lock-session-state.txt](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/screen-lock-session-state.txt), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/code-red-itty-palm-performance-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/code-red-itty-palm-performance-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md) | release, worldgen, compatibility, rendering, migration | 2026-06-21 | stale (flipped 2026-07-06 per update-contract: past due) | 2026-07-05 |

## 2026-07-01 additions (2.0 "Longitude" line)
These dated notes extend the topic map above (rendering+HUD, release, worldgen) and are the current resume
surface for the 2.0 line. Recorded 2026-07-01 during the binder/handoff/lessons doc-sync pass; see the
2026-07-01 row in `evidence-registry.md`.
- `current-state-handoff-20260701.md` — **start-here / resume pointer** for the 2.0 line (release scope);
  the feature branch's handoff until it reconciles with `main`'s `docs/HANDOFF.md`.
- `atlas-worldshape-longitude-20260701.md` — shipped 2.0 UI features (rendering scope): Longitude compass
  reading (commit `9a6e4e41`) + Atlas / World Shape Mercator-vs-Legacy toggle (commit `30db22fc`).
- `test1-live-findings-20260701.md` — **open punch-list** (rendering + create-UI + worldgen + perf scope):
  11 findings from Peetsa's TEST 1 live test; the next work gate. Two items blocked on Notion re-auth.
- `bug-catcher-20260701.md` — adversarial find→verify review (6 dimensions) run before Peetsa's TEST 6 retest;
  caught + fixed 2 confirmed bugs (existing-save Mercator-flip save-corruption; recreate drops world name).
- `atlas-createscreen-iteration-20260701.md` — atlas + create-screen live-feedback loop (TEST 7→10): continents,
  real vanilla map-texture frame + mapping notes, band contrast, centering, Vanilla-type grey-out. A4 layout
  reorg still open.
- `spark-profile-analysis-20260701.md` — decoded Peetsa's `.sparkprofile` (schema-less protobuf parse, no
  browser/proto tooling needed). Confirms a real ~3-minute TPS collapse, but the capture only sampled "Server
  thread" — the actual chunk-gen worker pool (where Latitude/Terralith run) is invisible in it. Needs a re-profile
  with `--thread *`.

## 2026-07-02 additions (future pass parking lot)
- `../LATITUDE_2_0_OVERHAUL.md` — **canonical Latitude 2.0 overhaul front door**: 26.2 pivot, portability,
  projected-planet world-shape direction, measurement-first GeoAuthority/ClimateAuthority roadmap, stop rules,
  and generalized execution handoff.
- `model-effort-strategy-20260702.md` — which model/reasoning-effort tier per overhaul phase, and a precise
  answer to "can a future thread self-adjust model as it goes" (partially: can delegate bounded sub-tasks via
  Agent/Workflow's `model` param, cannot change its own ambient model without `/model`). Fable explicitly
  excluded from the default rotation on cost/cadence grounds.
- `future-pass-ideas.md` — parked ideas Julia wants future agents to notice without making them active scope.
- `arid-belt-dry-mosaic-assessment-20260702.md` — complete read-only assessment and future implementation
  direction for reintroducing custom-biome variety into the subtropical arid belt while preserving band law.
- `longitude-earthlike-world-overhaul-20260702.md` — full read-only Latitude 2.0 plan for a projected 2:1
  earthlike world: cohesive continents/oceans first, then wind, ocean, rain-shadow, current, and monsoon climate
  authority; explicitly retires E/W wrap and ocean-seam terrain sink as the 2.0 centerpiece.

## 2026-07-03 additions (HUD Studio custom theme + dead-screen removal)
- `hud-studio-custom-theme-20260703.md` — HUD Studio tabbed redesign (Compass/Placement/Title/General),
  new 12th `CUSTOM` analog compass theme with RGB/ARGB color pickers, and removal of the confirmed-dead
  `LatitudeHudAdjustScreen.java` (superseded by `LatitudeHudStudioScreen`, zero call sites). Current
  canonical theme count of record (12) — the 2026-06-22 compass-theme docs are left unedited as
  point-in-time evidence for the 6-theme/11-total slice shipped that day.

## 2026-07-03 additions (title RGB/rainbow/case styling + Rainbow Text + polish fixes)
- `hud-studio-title-rainbow-and-polish-20260703.md` — follow-on to the custom-theme redesign above: zone-enter
  title gets the same styling depth as the compass (6 color presets + Custom RGB + **Rainbow**, plus a letter-
  case selector including a "Mocking" alternating-case option, plus a letter-spacing/kerning slider); a
  **Rainbow Text** toggle added for the compass/zone/biome/coords readout too; General tab gained a Title
  Draggable toggle; Title tab gained direct access to Zone Title on/off, duration, and degree-display settings
  previously reachable only from the separate Settings screen. Also fixes a live-test-caught bug where "Normal"
  title case was silently identical to "UPPERCASE" (source text was force-uppercased in 3 places before the
  case transform ever ran) and a tab-strip label ("Placement") that overflowed its button. **Found but not
  fixed:** a completely unreachable parallel title-notification system (`ZoneEntryNotifier`/`ui.ZoneTitleOverlay`,
  zero callers on both the trigger and render sides) — flagged as background task `task_7003cfac`, still open.
- `canonical-26.2-pivot-20260702.md` — the running pivot log was ~15 commits stale (stopped at `TEST 5.jar`);
  brought current through `TEST 20.jar` / HEAD `61d51782` with a full narrative of every round in between
  (Rules panel clip extension, World Shape relocation + compass N-scaling, HUD Studio promotion + rainbow
  lettering, Rules panel double-layout fix, biome display + full HUD detachability, then the tabbed/RGB
  redesign and title-styling arc summarized above).
- `docs/porting/VERSION_MATRIX.md` — the "live 26.2 client pass" item was still listed as the one open blocker;
  updated to reflect it as done (proven across ~20 staged test jars) and pointed at the two docs above for detail.

## 2026-07-03 additions (GeoAuthority/ClimateAuthority phases 0-3)
Overhaul-plan phases, all landed on `port/canonical-26.2-pivot`, all behind disabled flags, all with a
flag-off byte-identical proof against `save/canonical-26.2-baseline`. See `evidence-registry.md` rows
`20260703-phase0-portability-foundation` through `20260703-phase3-climateauthority`.
- `phase0-portability-foundation-20260703.md` — no-op `GeoSummary`/`ClimateSummary` core-layer scaffolding
  + adapter shells behind `latitude.geoV2.enabled`/`latitude.climateV2.enabled` (both default false).
- `phase1-measurement-harness-20260703.md` — `tools/atlas/geography_analyzer.py`; independently
  reproduced the "current red" (~63-64% land, ~95%-share landmass, no dominant ocean basin).
- `phase2-geoauthority-20260703.md` (design: `../design/geoauthority-design-20260703.md`) —
  "Inverted-Plate Continentality" macro-geography, locked via a 4-design adversarial judge panel.
  Measured vs the red on the same seed: land 63%→39%, largest continent-of-world ~60%→~30%, dominant
  ocean basin <10%→99.6%.
- `phase3-climateauthority-20260703.md` (design: `../design/climateauthority-design-20260703.md`) —
  "Fetch & Lift" moisture-transport climate model, same judge-panel discipline; 14/14 acceptance rows
  executed as JVM tests; Earth-like distribution on real geography (subtropical arid belt ~60% desert,
  ice caps 82% at the poles).
- `../design/atlas-geography-overlay-plan-20260703.md` — Atlas overlay plan (design only, deferred to
  Phase 2/3 consumer work).
- Both authorities are pure Core Logic (zero Minecraft imports) and pack-independent by construction —
  see memory `vanilla-first-overhaul-constraint`.

## 2026-07-04 addition (Biome Consumer slice -- land reroll shippable, ocean swap walled off)
- `biome-consumer-slice-20260704.md` — GeoAuthority can replace `OceanDistanceField` as the land/ocean
  authority and ClimateAuthority rerolls clear climate/biome-family mismatches, behind
  `latitude.biomeConsumerV2.enabled` (default false, kept separate from `geoV2`/`climateV2` on purpose).
  Flag-off byte-identical (16/16); vanilla-only run resolves real vanilla biomes with zero fallback.
  **Real finding, RESOLVED via a flag split:** with the ocean-authority swap active, live land fraction
  collapsed to ~13% (GeoAuthority alone calibrated ~39%) because `pick()`'s existing
  `base.is(IS_OCEAN) || oceanAuthority` union now compounds two largely-independent noise fields instead
  of the old, highly-overlapping ODF-vs-terrain pair; confirmed via a `-Dlatitude.geoV2.seaLevel=0.0`
  diagnostic (barely moved the number) that this is a terrain-integration gap (Phase 4), not a
  GeoAuthority miscalibration. **Peetsa's decision: wait for Phase 4.** The ocean-authority swap now
  requires its own additional `latitude.biomeConsumerV2.oceanAuthority.enabled` flag (still default
  false) so it can't be enabled by accident; with only the original consumer flag on, land fraction is
  confirmed back at the pre-existing 63.14% baseline (ocean swap fully inert) while the safe climate
  reroll is active. The land-reroll half is ready to enable; the ocean half stays parked.
  See `evidence-registry.md` row `20260704-biome-consumer-slice`.

## 2026-07-05 addition (live-session finding + sweeper audit + Phase 4 prep research)
- **LIVE FINDING (2026-07-05): the "safe" land climate-reroll was NOT actually safe.** First live
  in-game session with `latitude.biomeConsumerV2.enabled=true` showed "Snowy Taiga" next to jungle at
  18°N. Root-caused: `ClimateAuthority.classifyBase`'s TROPICAL-band case requires `temperature01>=
  T_WARM`; a tropical column cooled below that by GeoAuthority's terrain-decoupled altitude proxy falls
  through every case to the generic `HUMID_CONTINENTAL` default, alpine-stepped to `BOREAL`. The unit
  test for this exact scenario passed anyway (the fallthrough default coincidentally produces the same
  label the intended path would have) — recorded as `docs/LESSONS.md` L14 in the main worktree, alongside
  L13 (same root pattern: a new independent signal breaking something that assumed correlation, this
  time GeoAuthority's altitude intent vs. real terrain, showing up a second time in a different
  subsystem).
- `phase4-prep-research-20260705.md` — Phase 4's own document-review step, done ahead of any kickoff
  prompt: the prior E-W ocean-seam wrapping attempt was fully built, live-tested, and explicitly scrapped
  by Peetsa (closed, do not revive); Latitude's own terrain density functions are orphaned dead code
  (live terrain is 100% vanilla/Terralith); the existing amplitude-wrapper design
  (`amplitude-df-wrapper-design-20260622.md`) is a related-but-different, still-unimplemented piece of
  work (Y-only dampening, not position-dependent geography integration); height export exists but is
  disabled by default and unvalidated; and a ~3-minute generation freeze from 2026-07-01 was never
  conclusively root-caused (main-thread-only Spark capture, confounded by 99.71% system memory use at
  the time) — any Phase 4 proof needs an all-thread Spark capture on an unloaded machine.
- A sweeper-audit workflow (adversarial find + independently-verify + synthesize, Opus) was run against
  the Biome Consumer bug scope to find any OTHER instances of the same failure classes before trusting
  the slice again; see the binder note this produces once triaged, and `evidence-registry.md`.
- `biome-consumer-sweeper-fixes-20260705.md` — triage + fix of all 30 sweeper findings above.
  `ClimateAuthority.classifyBase` rewritten as a compile-time-exhaustive `switch` over `LatitudeBand`
  (root cause of the live bug); ocean altitude-cooling zeroed; `climateFamilyMismatch` made symmetric;
  the climate-compat reroll relocated to run after all downstream land-law gates; a dead constant
  removed; a live current-gate/design-doc mismatch fixed; design doc corrected on 3 points. 5 new tests
  (44 total), flag-off byte-identical re-confirmed (20/22 artifacts exact, 2 diffs are timestamp/duration
  bookkeeping only). Every finding individually dispositioned — fixed, test-pinned as intentional
  behavior, or documented-and-deferred to Phase 4 / a dedicated future slice with a stated reason. See
  `evidence-registry.md` row `20260705-biome-consumer-sweeper-fixes`.
- `sweeper-audit-standing-practice-20260705.md` — the sweeper find→verify→synthesize pattern is now a
  documented standing practice (`.claude/workflows/latitude-sweeper-audit.js`, the canonical
  lens/schema template — confirmed NOT resolvable by `Workflow({name, args})` in this environment, so
  always copy-and-hardcode into `script`/`scriptPath` instead). Sweeper audit #2 (Phases 0-3
  comprehensive sweep, excluding the already-fixed classifyBase/climateFamilyMismatch/
  applyClimateCompatReroll scope) launched as background workflow `wf_9c9dbfa8-fec`. See
  `evidence-registry.md` row `20260705-sweeper-standing-practice`.
- `sweeper-audit-2-phases-0-3-20260705.md` — sweep #2 results + triage: 30 raw findings, 27 survived
  verification. Headline: `geography_analyzer.py`'s `largest_share()` reused the raw-largest
  component's weight for its "largest area-weighted share" metric instead of ranking weighted-share
  independently (found by 7/27 survivors across nearly every lens; understated dominance 4-5x) — fixed.
  Also fixed: silently-land-classified unknown/out-of-palette biome indices; an unreachable-class test;
  a misleadingly-named signed field (renamed before any consumer existed); a dead tautological
  seasonality clause; a missing radius-scale floor; a tautological test's misleading docstring; 2
  design-doc inaccuracies. 6 lower-severity findings documented as residual risks and deliberately
  deferred (Phase-4-scoped, pre-existing mod-wide convention, or a currently-unconsumed field) rather
  than fixed. Both sweeper audits are now fully triaged/resolved. See `evidence-registry.md` row
  `20260705-sweeper-audit-2-phases-0-3`.
- **Phase 4 (Terrain Integration Spike) kickoff prompt drafted** in `docs/LATITUDE_2_0_OVERHAUL.md`
  ("Kickoff Slice Prompt (Phase 4 only)"), built on `phase4-prep-research-20260705.md`. Closes off the
  scrapped ocean-seam teleport-loop approach explicitly; distinguishes this phase's position-dependent
  terrain wrapper from the separate, still-open Y-only amplitude wrapper; requires height-export
  validation + an all-thread Spark before/after capture (not repeating the 2026-07-01 ambiguous
  capture) as prerequisites; proposes flag `latitude.terrainV2.enabled`; states plainly this phase is
  live-verification-only. See `evidence-registry.md` row `20260705-phase4-kickoff-prompt`.

## 2026-07-05 addition (Phase 4 kickoff -- terrain wrapper design locked + adversarially reviewed)
- **Phase 4 (Terrain Integration Spike) execution started.** A judge-panel design workflow's 3-candidate
  fan-out failed outright (all 3 hit a structured-output retry-cap); the synthesis step recovered solo by
  decompiling the real `minecraft-merged-deobf-26.2.jar` and producing a fact-grounded r1 design. Because
  the fan-out failure meant no genuine adversarial diversity had occurred, a SEPARATE 4-lens adversarial
  review + independent-verify pass was run against the locked r1 design and found 2 high/critical CONFIRMED
  defects a plausible, well-cited single-author design still missed: a unit-mismatch bug (wrapping both
  `finalDensity` and `preliminarySurfaceLevel` with one additive term when they are in incommensurable
  units) built on a false premise (spawn/heightmap does not actually read `preliminarySurfaceLevel`), and a
  gating method (`GlobeMod.isGlobeWorld(RandomState)`) that does not exist and cannot be implemented as
  described. See `docs/design/terrain-wrapper-design-20260705.md` (revision r2, locked) for the corrected
  design, its `preliminarySurfaceLevel`→`finalDensity`-only fix, the replaced globe gate, a corrected
  smoothness claim (Lipschitz-on-the-block-lattice, not "C-infinity" -- `land01` is `Math.round`-snapped),
  a new vanilla-only proof leg, and a 6-item residual-risk register (R1-R6) disclosing what is accepted
  rather than fixed (notably: `land01` is itself edge/pole-coupled, so the bias legitimately sinks a
  terrain ring near world edges/poles -- intended-direction, not a hard cliff, but honestly disclosed
  rather than assumed away). See `evidence-registry.md` row `20260705-phase4-terrain-wrapper-design` and
  `docs/LESSONS.md` L16 (main worktree) for the generalized lesson: verifying that individual facts a
  design cites are true is not the same as verifying the reasoning that combines them, and a failed
  workflow fan-out that a later step "recovers" from solo needs a genuine separate adversarial pass, not
  just acceptance of a plausible-looking final result.

## 2026-07-05 addition (Phase 4 execution -- implementation, mechanical proof, sweeper audit, fixes)
- **Phase 4 (Terrain Integration Spike) is mechanically proof-complete; the live pass is still pending.**
  Full execution log: `phase4-terrain-wrapper-20260705.md`. Implementation (`GeoTerrainBiasFunction`,
  `TerrainRouterWrapping`, the `ChunkMap`-relocated mixin, 3 new flags) landed on the locked r2 design.
  The mechanical proof harness's own structural check caught and fixed a real defect (a default
  `mapChildren` broke installed-but-strength=0 byte-identity in pipeline structure, closing design residual
  R5) and surfaced a K-scaling calibration finding for the live pass (R4: strength=1.0 at the starting K
  produced a floating landmass on vanilla density). A subsequent 6-lens sweeper audit (23/26 findings
  confirmed -- an unusually high rate) found the harness's initial "34/34 PASS" claim was **not
  trustworthy** (its own non-globe isolation leg never validly ran) and one **critical** defect: the
  install gate checked only a boolean flag as proof `GeoAuthority`'s field was live, missing that a seed-0
  world leaves the field a NEUTRAL no-op regardless of the flag -- which would have silently biased the
  **entire world** toward ocean. Both are fixed and the full mechanical proof gate was genuinely re-run
  (not trusted from a summary) with raw evidence spot-checked directly. See `LESSONS.md` L16 (the design-
  review lesson: verifying facts a design cites is not verifying its reasoning) and L17 (the fix-round
  lesson: a flag being "on" doesn't mean the state it represents is actually live) in the main worktree,
  and `evidence-registry.md` rows `20260705-phase4-terrain-wrapper-design` and
  `20260705-phase4-terrain-wrapper-execution`.
- **Next step:** Peetsa's live pass (terrain amplitude is live-only verifiable -- the atlas cannot see
  height at all). Checklist in `phase4-terrain-wrapper-20260705.md`, notably a LOW starting strength (not
  1.0) and a specific check for anachronistic snow on a lifted subtropical peak (design residual R7).

## 2026-07-06 addition (Phase 4 live pass: install-timing bug + perf fix + first live confirmation; two
unrelated same-day fixes)
- **Phase 4's mechanically-proof-complete state hid a critical bug, found on Peetsa's very first live
  create-world test:** the terrain wrapper never installed on ANY freshly created world, any seed, any
  strength -- both the full mechanical proof gate and the 6-lens sweeper audit passed clean because neither
  could structurally reach the exact world-load ordering window where the bug lived (the dev/atlas proof
  path always builds its `RandomState` on an already-fully-loaded world, so `GEO_V2_PROVIDER` is already
  real by the time it's checked there; real gameplay's `ChunkMap`-constructor path is not). Fixed by moving
  the NoOp-provider safety check from a one-time install-time snapshot into a per-call check in
  `GeoTerrainBiasFunction.compute()`. A second real defect (chunk-generation performance regression from
  redundantly re-sampling `GeoAuthority` once per vertical noise cell instead of once per column) was found
  and fixed once the wrapper started actually installing. After both fixes, Peetsa confirmed the mechanism
  genuinely works live (a dramatic land/ocean rift at a deliberate stress-test strength of 10.0). See
  `phase4-terrain-wrapper-20260705.md`'s "Live pass findings (2026-07-06)" section, design doc residuals
  R8/R9 (both closed) and R4's update, `LESSONS.md` L18 and L19 (main worktree), and `evidence-registry.md`
  row `20260706-phase4-terrain-live-pass-findings`. Strength calibration is the open item; a fixed,
  reproducible test coordinate (computed directly from `GeoAuthority`, independent of the currently-
  unrelated displayed biome map) was handed to Peetsa for that.
- **Unrelated same-day fix:** a client crash on every singleplayer world load with Sodium 0.9.0+mc26.2
  installed, caused by an E-W section-culling compat mixin targeting a Sodium internal method that version
  no longer has. Fixed by making that one injection tolerant of a missing target (`require = 0`) instead of
  crashing the whole client. See `evidence-registry.md` row `20260706-sodium-client-crash-fix`.
- **Unrelated same-day fix:** a visually-wrong, dead-straight arid/savanna boundary cutting across
  mountains, initially suspected as a seed-0 noise degeneracy (hypothesis tested and refuted via a same-
  config atlas run at a different seed). Real cause: `pickTropicalGradient`'s subtropical composition
  ladder picks a discrete step via `floor(tJitter*4)` with zero blending between the arid and savanna
  steps. Fixed by widening the existing step-dither band specifically at that boundary; verified
  composition-neutral (arid/savanna aggregate shares essentially unchanged) with a real, measured increase
  in boundary fraying (8.6%->12.7% of edges). See `evidence-registry.md` row
  `20260706-arid-savanna-boundary-fray`.
- Also: `CLAUDE.md` gained guidance on multi-step sequential background pipelines (never delegate to a
  subagent; the orchestrator sequences each state-changing step itself) and on shared, lockable headless
  world-gen resources, after a subagent's before/after regression-proof attempt piled up concurrent
  processes against the same `run-headless/world` directory.

## 2026-07-06 addition (Fable 5 overhaul audit — kickoff charter)
- `fable5-overhaul-audit-kickoff-20260706.md` — standing charter for a deep, adversarial, read-first audit
  of the whole 2.0 overhaul (and adjacent code/docs) before any Phase 5 go decision, prompted by Phase 4
  shipping two live-only bugs through a full mechanical proof gate + sweeper audit, plus the brand-new
  headless-measured cliff-edge strength threshold (`baseHeight` 92→320 world-ceiling flip between
  strength 0.05 and 0.1 at one mountain-peak column while flat/coastal columns don't move). Base draft by
  GPT-5.5 (supplied by Peetsa), corrected + expanded by Fable 5 against fresh-verified repo state; per
  Peetsa's decision the audit executes in the authoring session with Fable 5 as director over read-only
  subagent lanes (10 lanes; the new Lane 10 owns cliff-edge characterization and a DESIGN-ONLY live
  `/latdev terrainHere` diagnostic spec). Deliverables: `fable5-overhaul-audit-report-20260706.md` +
  `../design/live-terrain-diagnostics-design-20260706.md`. See `evidence-registry.md` row
  `20260706-fable5-audit-kickoff`.

## 2026-07-06 addition (Fable 5 overhaul audit — REPORT: Phase 4 NO-GO, Y-taper prerequisite)
- `fable5-overhaul-audit-report-20260706.md` — **the audit's final 10-section report; START HERE for
  anything Phase-4/Phase-5.** Executive verdict: Phase 4 is mechanically closed but CANNOT live-close and
  Phase 5 must not start — the Y-uniform bias formula has a refutation-hardened EMPTY usable strength
  window (ground ≤±1 block below S≈0.10; a detached stone slab at Y256-319 appears at S=0.10 on 3/3 seeds
  — the live "enormous block"; the asymmetric ocean-ratio escape closes with lava-floored voids under the
  seabed), so strength calibration as scoped is unsatisfiable and a Y-aware taper is a PREREQUISITE (P0-1).
  Also P0: the binder's own calibration handoff is a trap (coordinate derived at seed 0, which no live
  world can arm; live registry rows pair "seed 0" with armed terrainV2 — impossible as written) and the
  two front-door docs are misleading-current-truth. P1s: refutation-CONFIRMED stale-REAL provider
  cross-world leak (world B re-seeds with world A's seed before setWorldSeed(0) declines); a
  zero-observability compound (silent seed-0 decline / silent nonsense flag combos / silent compute()
  catch / silent Sodium degrade); perf fix never profiled; Sodium fix not live-confirmed; proof bundles
  don't record their own config. Path forward: 5 bounded slices (A docs-truth, B state+observability, C
  Y-taper w/ headless proof gate, D tooling truthfulness, E ONE scripted ~45-min live session → written
  go/no-go). Blocking gates G1/G2/G3. 12 subagents + 19 director-run headless measurements; every P0/P1
  spot-checked at raw code by the director + two adversarial refuters + an independent synthesis critic.
- `../design/live-terrain-diagnostics-design-20260706.md` — DESIGN-ONLY (per Peetsa) implementation-ready
  spec for `/latdev terrainHere`: land01/bias/install-gate/provider-seed-match at the player's column, a
  wrapped-vs-strength-0-baseline density ladder (baseline arithmetically recovered from one live sample —
  no second launch), a ceiling scan with a slab-flip proximity warning derived from the measured
  0.0225-0.025 flip bracket, and a loud STALE? marker that would have made both the install-timing bug and
  the stale-provider hazard visible at a glance. Plus harness extensions (caller X/Z probes, Y-to-320
  sampling) and the 3 adjacent one-line observability fixes to bundle. See `evidence-registry.md` row
  `20260706-fable5-audit-report`.

## 2026-07-06 addition (Slice A — truth restore, docs only)
Executes the audit report's Slice A. All corrections are dated banners/blocks — no history rewritten,
no code touched. See `evidence-registry.md` row `20260706-fable5-slice-a-truth-restore`.
- **Calibration coordinate re-derived at the pinned seed** (standalone scanner re-run 2026-07-06):
  TYPE seed `2591890304012655616`, Mercator regular, fly to `x=-3300, z=-3636` (~32.7°S) — `land01` ramps
  0.10→0.99 west-to-east over ~560 blocks; stay west of x≈-2840 (abrupt island edge beyond). Correction
  block added to `phase4-terrain-wrapper-20260705.md`, which also RETIRES that doc's "find the strength
  value" next-step (empty-window finding: no value exists pre-taper).
- **Seed-0 live-record ambiguity annotated** (audit P0-2): registry rows `20260706-arid-savanna-boundary-fray`
  and `20260706-phase4-terrain-live-pass-findings` record "seed 0" alongside armed-terrainV2 observations,
  which is behaviorally impossible as written (literal seed-0 worlds never arm GeoAuthority); the actual
  world seeds/world-load history were not recorded. Those rows stay as-written (append-only law); the
  annotation lives in row `20260706-fable5-slice-a-truth-restore`. New discipline: armed live evidence must
  record the actual typed seed + same-JVM world-load history.
- **Front doors corrected:** `../LATITUDE_2_0_OVERHAUL.md` Phase-4 kickoff section now carries an EXECUTED
  status block (was reading as not-yet-run); main-root `docs/HANDOFF.md` (Latitude (Globe)) re-pointed at
  the pivot as the active line with its 26.1.2 body explicitly fenced historical; LESSONS L20 added
  (mid-session corrections must reach every durable record same-pass).
- **Stale resume surfaces bannered:** `current-state-handoff-20260701.md` (superseded),
  `test1-live-findings-20260701.md` (historical, no longer the work gate),
  `canonical-26.2-pivot-20260702.md` (point-in-time, ends at TEST 20), `../porting/VERSION_MATRIX.md`
  (2026-07-06 status update; 26.1.2 branch row demoted to prior-era reference).
- **Topic-map freshness enforced:** the 4 overdue rows above flipped `active`→`stale` per
  `update-contract.md`'s own rule (they block savepoint/release-class progression until re-reviewed).

## 2026-07-06 addition (Slice B — state + observability hardening, ALL GATES GREEN)
Executes the audit report's Slice B. See `evidence-registry.md` row `20260706-fable5-slice-b-state-observability`.
- **Stale-provider leak (audit P1-1) killed two ways:** the rebuild decline paths now explicitly reset the
  V2 providers to NoOp (an inert world reads NEUTRAL, never a prior world's geography), and a new
  SERVER_STOPPED teardown clears providers + seed + radius. Proven by a new harness replay-probe leg that
  replays the exact seed-0 world-B load order and asserts the provider ends NoOp (pass=true).
- **Every silent mode now speaks once** (audit P1-2): seed-0/zero-radius INERT warn (fires from
  `setWorldSeed`, the load sequence's final setter); terrainV2-on+geoV2-off never-installs warn;
  INSTALLED-but-not-ready info; first-bias ENGAGED info; compute() bias-failure warn (catch restructured so
  the delegate's own failures propagate unwrapped — no more double-invoke); Sodium-degrade warn via a
  client-init reflection check. All observed in real headless logs.
- **Proof gates:** compile+test green; armed-S=0 vs flag-off terrainProof reports byte-identical (all data
  fields); S=0.05 values pre-vs-post change byte-identical; replay probe green. NB: the probe's first run
  silently skipped because build.gradle's forward list lacked the new property (the L17 forwarding class,
  caught by the gate itself) — forwarding added.

## 2026-07-07 addition (Slice C — Y-aware taper: THE USABLE STRENGTH WINDOW NOW EXISTS, all gates green)
Executes the audit report's core slice. See `evidence-registry.md` row `20260707-fable5-slice-c-y-taper`.
- **The taper** (smoothstep envelope: zero ≤Y=-32, full across [0,96], zero ≥Y=160) killed both measured
  artifact classes: NO detached ceiling slab at any strength up to 1.0 on 3/3 seeds (pre-taper: slab at
  S=0.10), NO lava voids under oceans at ratio up to 30 (pre-taper: voids from ~0.075 ocean push).
- **A real window exists:** land ground +3/+5/+6 blocks at S=0.3/0.4/0.5 (+13 at 1.0), coasts grade
  (land-side +4, mid-ramp 0, ocean-side −1 at S=0.4 on the calibration coastline), formula exact at all
  16 sampled Y. **Named live-gate candidate: S=0.4.**
- **Drowned-land fixed, not asserted:** the coherence tripwire caught 1/81 columns (jungle sunk below sea,
  still land-biome) — the audit-predicted one-directional-veto gap; a mirror veto now flips sunk
  geography-ocean land columns to ocean, gated so flag-off and armed-S=0 are PROVEN byte-identical
  (zero biome/height diffs on the grid). S=0.4 composition shift: 6/81 columns, all sensible.
- **Preset naming trap caught by the gate:** `globe:globe_regular` = UI-size SMALL (7500);
  UI-size REGULAR = `globe:globe_large` (10000). The Slice-A calibration coordinate is valid for the
  UI-Regular worlds Peetsa actually creates; headless verification of live-Regular claims must boot
  `globe_large`. (First probe run read land01=0 everywhere — the absoluteness gate caught it.)
- Harness gained caller X/Z probes, a Y ladder to 319, and the 81-column coherence grid — the
  live-diagnostics design's §4 instrument gaps are now closed for the headless side.

## 2026-07-07 addition (biome geography audit — "is it broken?" answered with exact-ID evidence)
- `fable5-biome-geography-audit-20260707.md` — Peetsa's question ("do rain shadows, windward/leeward,
  subtropical transitions, desert/plains boundaries, coast/interior logic make sense, or is Latitude
  broken/not wired?") answered per config. **Verdict: not broken.** The authorities' fields are genuinely
  earthlike (textbook belts, real Sahara-belt minimum, coast→interior drying, working rain-shadow
  transect, strong tropical-highland cooling) — but in the LIVE config none of the moisture logic reaches
  the visible map (NOT WIRED; the map is the lawful pre-2.0 band/province machinery, tropical dry share
  0.02%). With the consumer ON (measured for the first time with the flags actually forwarded): 1.76% of
  cells change — roughly half are climate-correct corrections the stripe logic can't make, and half are
  two CONFIRMED consumer bugs: P1-A (reroll undoes the tropical dry law at 10-20°, 0.02%→2.01%) and P1-B
  (kAlt over-cool + flat-polar repaint = snowy_plains on warm-band mountains, the L14 class). A/B/C
  sequencing unaffected; a new "consumer law-compliance" slice is prerequisite to any Phase-5 consumer
  flip. Also fixed in-pass: climateV2/biomeConsumerV2 were never forwarded to the headless JVM (the L17
  forwarding class, second catch today — the first "consumer-on" atlas silently ran consumer-off and
  diffed 0.00%). See `evidence-registry.md` row `20260707-fable5-biome-geography-audit`.

## 2026-07-07 addition (RECOVERY CHECKPOINT — A/B/C green, stopped before D/E; START HERE to resume)
- `fable5-recovery-checkpoint-20260707.md` — the stop-gate record for this recovery pass: Slice A (docs
  truth), Slice B (stale-provider kill + observability), Slice C (Y-taper — no slab, no lava voids, real
  ground response, S=0.4 named candidate) all GREEN with their proof gates; biome geography audit
  delivered (2 CONFIRMED consumer bugs queued as a pre-Phase-5-flip slice; live config unaffected). Audit
  gates G1+G2 satisfied, G3 (the ONE live session) not run. Phase 4 NOT claimed closed; Phase 5 NOT
  recommended. Awaiting Peetsa: Slice E, Slice D, consumer law-compliance. Four in-pass gate catches
  recorded (2× flag-forwarding, the globe_regular≠UI-Regular preset trap, a silent malformed-flag zero).
  See `evidence-registry.md` row `20260707-fable5-recovery-checkpoint`.

## 2026-07-07 addition (Slice D — tooling truthfulness, gates green)
- Proof bundles now self-describe and contention is un-stumble-into-able: `run_manifest.json` records the
  actual `--sysprop` config + honest emitHeight (was hardcoded False); `run_flags.json` sidecar (next to
  biomes.txt, both exporter call sites) echoes every LatitudeV2Flags value — sidecar not in-file, so
  flag-off byte-identity diffs of biomes.txt keep working; `atlas_runner` preflight aborts on a live
  runBiomePreview process or a genuinely HELD session.lock. Smoke `20260707-094823` green. See
  `evidence-registry.md` row `20260707-fable5-slice-d-tooling-truthfulness`.

## 2026-07-07 addition (Slice E READY — TEST 27 staged, scripted live session waiting on Peetsa)
- `fable5-slice-e-live-script-20260707.md` — **Peetsa's run sheet for the one session that closes
  Phase 4.** TEST 27.jar staged in `LATITUDE 26.2` (SHA `fbfb7a2b…`, Sodium 0.9.0 present); JVM args
  `-Dlatitude.geoV2.enabled=true -Dlatitude.terrainV2.enabled=true -Dlatitude.terrainV2.strength=0.4`;
  TYPE seed `2591890304012655616`; 9 steps (~45-60 min): Sodium boot, Mercator-Regular world, the
  calibration-coastline flight, R7 glance at pre-located warm-band columns, Spark all-thread capture at
  S=0.4, Classic same-seed second world (teardown/re-arm log check), fresh-world S=0 control capture.
  See `evidence-registry.md` row `20260707-fable5-slice-e-prep`.

## 2026-07-07 addition (bespoke-UI audit + HUD overhaul design — PROPOSED, awaiting Peetsa)
- `ui-audit-20260707.md` — evaluation of every Latitude-owned UI surface. Both live complaints
  root-caused to ONE layout primitive (content-width-coupled anchoring: `anchoredX` consumes a box width
  that includes the biome text — the dial moves because the box re-centers; the Studio lies because its
  previews/offsets are computed against fixed SAMPLE text widths). Plus hidden problems (GUI-scale-baked
  placements, per-pixel disc fill, stale cross-dimension statics, boss-bar collision, F9→legacy-Settings
  split with dup fields, unversioned configs, 8 dead config fields, no seed-0 guard on the create screen,
  dead classes) and an explicit what-works list (create screen + loading overlay healthy; Studio preview
  already renders the real draw path).
- `../design/hud-layout-overhaul-design-20260707.md` — the proposed fix: **pin + grow** layout model
  (anchor is a point; alignment decides growth; satellites pin to compass edges; scale-independent
  storage + versioned migration), truthful Studio (Longest-text preview default, pin crosshairs, one drag
  model, show-mode honesty), F9→Studio consolidation + config hygiene, render hygiene, and create-screen
  refinements (seed-0 guard first among them). See `evidence-registry.md` row
  `20260707-ui-audit-and-overhaul-design`. **r2 (same day, direction APPROVED):** adds the non-clipping
  HOTBAR DOCK (the historical clipping bug found precisely: analog attach centered the dial ON the hotbar,
  `CompassHud.java:589-592`, disabled in `4778a5ed`; the dock pin computes past offhand/attack-indicator
  and grows only away from the hotbar, with a deterministic fit ladder), five COMPASS LOOKS
  (disc/ring/rose/tape/minimal, texture-atlas rendered → resource-pack reskinnable + kills the per-pixel
  fills), element-centric STUDIO IA v2 (F9→Studio, one page per element, one save model), and a deeper
  create/loading refinement list. Slices U-A..U-E. Row `20260707-ui-overhaul-design-r2`.
- `ui-overhaul-implementation-20260707.md` — **IMPLEMENTED same day (approval: "I absolutely love all
  of these ideas and approve their implementations")**: U-A `e904d731` pin+grow core + hotbar dock +
  migration, U-B `b34b30a6` truthful Studio + element tabs + F9→Studio (LatitudeSettingsScreen deleted),
  U-C `764e188e` versioned single-source config (10 dead fields pruned, directionMode surfaced),
  U-D `6b11c849` five compass looks + span-batched dial + change-driven HUD strings + world-switch
  resets, U-E `db93644e` seed guard/affordances + truthful loading stages + Chartroom pass + dead-code
  deletion. Gates: 54/54 pure-JVM; worldgen identity by diff-scope construction. Jar
  `latitude-2.0-beta.1+26.2.jar` SHA `5651ac0a...c56f2` **built + HELD** (TEST 27 session in flight);
  stages as TEST 28. Remaining gate: Peetsa's ONE live UI pass (checklist in the doc). Row
  `20260707-ui-overhaul-implementation`.

## 2026-07-07 addition (TEST 27 live findings — Slice E attempt #1 aborted, diagnosed, one-arg retry ready)
- `test27-live-findings-20260707.md` — Peetsa's aborted first Slice-E run, fully diagnosed with headless
  reproduction at his exact F3 coordinates. Headline: **negative bias HOLLOWS terrain** (uniform −0.1
  density shatters marginal underground: spawn column [-64..98] solid → 4 fragments + 63 void blocks —
  the "massive broken cavern"; the spawn even sits in the projection edge band at land01=0.000 via the
  biome-driven spawn decoupling Lane 1 predicted). Rain-shadow question answered: NOT WIRED in this
  config (the model believes what Peetsa expects; the map can't hear it until the consumer law-compliance
  slice). Shelf absence = smoothstep dead-zone + unconsumed shelf01 (design item). Chunk lag
  environment-confounded (99.6% RAM / 83% swap / server-thread-only capture). **Retry recipe (verified
  headlessly): same TEST 27 jar + `-Dlatitude.terrainV2.oceanStrengthRatio=0.0`** — ocean/edge columns
  byte-identical to unbiased, land still lifts. Slice C-2 (bathymetry regime) + spawn fix pending
  authorization. See `evidence-registry.md` row `20260707-test27-live-findings`.

## 2026-07-07 addition (Slice C-2 — bathymetry: oceans carve instead of shatter, and they FLOOD)
- Registry row `20260707-fable5-slice-c2-bathymetry`. The additive negative bias is gone: ocean-intent
  columns now carve to a geography-prescribed floor (shelf01 apron = real continental shelves; min()
  semantics = hollowing and slabs structurally impossible), and the new `preliminarySurfaceLevel`
  companion wrapper (unit-correct block-Y clamp — not L16's rejected additive term) makes carved seas
  flood properly instead of forming air pockets + perched aquifers. The mirror veto gained solid-floor
  sight (its own new tripwire caught 26/81 land biomes floating over carved water — the fluid-inclusive
  heightmap blinded it once flooding worked) and an r≠0 gate (protects the r=0 recipe); 24/26 convert to
  latitude-appropriate oceans, 2 beach-rule residuals ACCEPTED-RISK P2. Spawn search now excludes the
  projection edge band when biasing. Harness tripwires upgraded (gapBlocks/solidRanges + floorHeight).
  All gates green incl. r=0-recipe exactness — Peetsa's in-flight TEST 27 retry is untouched. r=1
  bathymetry (28/81 interior columns converting to proper oceans) is the NEXT live candidate, after the
  current retry reports.

## 2026-07-07 addition (TEST 27 retry: LIVE GREEN — the Phase-4 terrain go/no-go note exists)
- `test27-retry-go-no-go-20260707.md` — the r=0 recipe live pass: Sodium boot (P1-4 closed), green
  spawn, believable graded coasts, no wrapper snow, healthy ALL-THREAD Spark at S=0.4 (P1-3 armed leg
  closed: TPS 20, med 7.97 ms). **GO with two ~10-minute residuals** (Classic world-B parity + S=0
  control capture), folded into the next session. The "harsh seam" (35.2°) is pure pre-existing
  band-edge contrast (zero bias involvement, verified); the "wall" (34.3°) is Terralith stepping +5
  bias at the sharp land01 island edge. Sequencing recommendation: TEST 28 = held UI jar, TEST 29 =
  fresh build with C-2 for the r=1 bathymetry look. See row `20260707-test27-retry-go`.

## 2026-07-07 addition (TEST 28 staged — UI pass + worldgen residuals; bathymetry is args-only next)
- Registry row `20260707-test28-staging`: TEST 28 = fresh HEAD build (`e453665d…`), superseding the held
  `5651ac0a…` (identical UI code + C-2 inert-at-r=0 by gate proof). Session = the UI 9-point checklist
  (`ui-overhaul-implementation-20260707.md`) + Classic world-B parity/teardown-log residual + optional
  S=0 control capture; JVM args unchanged from the live-green retry. r=1 bathymetry = args-only on this
  jar afterwards.

## 2026-07-07 addition (TEST 29 staged — UI round-1 fixes jar; session = the r=1 bathymetry look)
- Registry row `20260707-test29-staging`: `TEST 29.jar` = the `af752929` build (SHA `2f7360c1…`),
  TEST 28 removed. Args change is ONE knob: `oceanStrengthRatio=1.0`; fresh world, same pinned seed
  recommended. Expectations + the not-fixed phantom-ocean caveat in
  `test28-deep-ocean-decoupling-20260707.md`; UI re-check matrix in `ui-pass-round1-fixes-20260707.md`.

## 2026-07-09 addition (Ocean-label family investigation — villages/dripstone/rivers in ocean)
- `ocean-label-investigation-20260709.md` — Peetsa's B-4 punch-list items 1/5/6 root-caused (all
  CONFIRMED): structure path has no terrain inputs so flooded labels feed vanilla village placement;
  carve exposes untouched vanilla 3D cave biomes and the cave-clamp measures the waterline not the floor;
  rivers are hard-excluded from the relabel (7,469 cells byte-invariant, 67% inside ocean). FIX SHAPE:
  one shared pure oracle `carveCeilYOrInfinity` (the carve's own target) drives the relabel in ALL
  contexts + a floor-aware cave clamp + river inclusion — one flag, default off, atlas-proven. Row shares
  `20260709-phase5-boundary-experience`.

## 2026-07-09 addition (Phase 5 — Boundary Experience: plan + run log)
- `phase5-boundary-experience-plan-20260709.md` — Phase 5 opened per Peetsa ("Proceed with Phase 5 using
  workflow"). Measured current red from the A′ atlas: poles already intentional (67% ice/snow) but the
  X (east/west) edge is 46-54% ordinary land — statistically identical to the interior, zero ocean bias
  on the VISIBLE map, despite GeoAuthority already suppressing continentality hard past 0.80·xRadius
  (EDGE_STR=1.30) and exposing an UNCONSUMED projectionEdgeSuitability01 hook. Central B-0 puzzle: why
  the authority's edge-ocean intent never reaches the visible biome layer (mirror-veto gating / carve
  behavior / atlas-context blindness — code-trace in flight). Slices B-0 truth → B-1 design (latitude-aware
  edge intent, own flag, shared authority — never clamps) → B-2 geography impl → B-3 UX (dead 3D storm
  wall decision + warning language) → B-4 Peetsa live look. NB: bundle height layer is L24-unreliable
  under terrainV2 (WORLD_SURFACE_WG lies on carved columns) — height axis of edge cross-checks needs the
  density ladder, not the atlas PNG. Row `20260709-phase5-boundary-experience`.

## 2026-07-09 addition (Consumer law-compliance slice — plan + working record)
- `consumer-law-compliance-plan-20260709.md` — the Phase-5-gating slice, authorized by Peetsa: fix P1-A
  (reroll undoes the tropical dry law — make `applyClimateCompatReroll` law-aware IN PLACE via the same
  demote predicates, reroll stays LAST) and P1-B (kAlt over-cool + snowy_plains-on-warm-mountains — fixed
  at the CONSUMER using the already-exposed `altitudeCooling01` + real `preview.centerHeight`; altitude
  families grove/snowy_slopes, never snowy_plains; ClimateAuthority untouched). Tiered workflow
  (AGENTS.md): Architect/Developer(Opus)/Test-writer(Sonnet)/Sweeper(Opus)/Reviewer. Proof: pre-fix
  baselines pinned from a pristine worktree; post-fix config-1 byte-identity + config-2 acceptance diff
  (dry 0-20° ≤0.1% w/ 25.5° corrections retained; zero snowy_plains <45°; T1-T3 green). Push only after
  map proof. Row `20260709-consumer-law-compliance`.

## 2026-07-11 addition (zone-title art-direction & readability review — READ-ONLY, PROPOSED)
- `title-art-direction-review-20260711.md` — art-director creative/readability pass over the zone-enter
  title (the "SUBPOLAR 66°N" signature moment), grounded in the real draw path (`ZoneEnterTitleOverlay.java`,
  `core/ui/TitleStyle.java`, shipped defaults) and Peetsa's 2026-07-10 screenshots. Headline (quantified with
  contrast ratios): the default off-white fill `0xF3ECDD` is near-invisible on bright backgrounds — **1.11:1
  on snow/whiteout** (the exact POLAR/SUBPOLAR bands where the signature fires hardest), 1.27–1.59:1 on bright
  sky — because the faded directional drop shadow is a depth cue, not a legibility tool. Fix is nearly free: the
  1–4 px dark keyline already exists in code but ships **OFF**; a 1 px dark outline scores 11–20:1 against every
  bright background AND 17.9:1 against the ivory fill itself, so keyline+light-fill cover opposite background
  brightnesses = readable everywhere. 6 recommendations (S/M/L sized, each with a plain-language "what Peetsa
  would see"): **A** outline ON by default (the hero, S), **B** keep drop shadow, **C** hairline gold rule
  `0xE8B64A` under the title to tie it to the loading-screen LATITUDE wordmark / create screen (S–M),
  **D** two-line lockup (zone name big / degrees small, M), **E** eased fades + shine-start delayed to tick ~8
  + 3% set-down (S), **F** future per-zone band-tint on the accent rule only (M). Ends with a full "Title v2"
  default recipe. Single highest-impact change: default the 1 px dark outline ON. PROPOSED, awaiting Peetsa.
  No `src/` changes; doc-only.

## 2026-07-10 addition (UI/UX + accessibility audit — bespoke menus, PROPOSED)
- `latitude-ui-ux-audit-20260710.md` — read-only accessibility/UX pass over every Latitude-owned
  screen (create-world, HUD Studio, SpawnZoneScreen, in-game HUD surface) for Peetsa's "menu-hell →
  so easy!" goal. 24 findings (3 CRITICAL / 7 HIGH / 8 MEDIUM / 6 POLISH), each grounded in a named
  principle (Nielsen heuristics, WCAG contrast/target-size/motion) with `file:line` citations and an
  S/M/L sized fix. Top hits: the 13-state "Color Scheme" cycle is un-navigable
  (`LatitudeHudStudioScreen.java:295-305`); snap-to-grid is buried under a "Dragging" cycle on the
  Labels tab (`:577-586`, Peetsa couldn't find it); HUD Studio has no in-game front door but F9
  (`GlobeModClient.java:184-193`); toggles collapse the layout with no transition (motion-sensitivity,
  `:1382-1442`); no Cancel/discard in the Studio (`:860-866`). Ends with an accessible icon+label+
  tooltip+lit-state+sound pattern for Peetsa's iconography sidebar (cites the `drawButtonGlyph` 1.9×
  scaled-draw precedent, `:965-983`) and a 3-pass sequence — A (HUD Studio) ∥ B (create-screen
  iconography) parallel-safe by file, C (reduce-motion + cross-cutting) sequential after. Complements
  the layout-primitive lens in `ui-audit-20260707.md` + `../design/hud-layout-overhaul-design-20260707.md`.
  PROPOSED, awaiting Peetsa. No `src/` changes; doc-only.

## 2026-07-10 addition (GUI-scale parity audit — bespoke screens + HUD overlays, PROPOSED)
- `latitude-gui-scale-parity-audit-20260710.md` — read-only pass answering Peetsa's "does a higher/lower
  GUI ratio break everything?" Anchored on the fact that MC's GUI Scale changes the *effective
  resolution* (floor 320×240, 480×270 at scale 4/1080p) rather than rescaling widgets. 12 findings
  (0 CRITICAL / 3 HIGH / 5 MEDIUM / 4 POLISH), each with `file:line` + S/M/L fix. Headline: the create
  screen and compass are well-defended (create drops to a tabbed layout below 530 gui-px and scroll-clips
  every pane; compass/labels are fraction-anchored + re-clamped every frame — the reference pattern).
  Real bugs: the zone-enter title stores an *absolute* pixel offset and is never re-clamped at render, so
  it drifts off-screen after a scale change (`ZoneEnterTitleOverlay.java:79-81`, `LatitudeConfigData.java:68-72`);
  HUD Studio's sidebar is a fixed 208 px with no narrow fallback and its centered Done/Cancel underlaps it
  (`LatitudeHudStudioScreen.java:57, 973-984`); `scaledUi()/compactUi()` are no-op stubs and `isCompact()`
  is dead code (`LatitudeCreateWorldScreen.java:671-677, 388-390`). SwatchDropdown up-flip/scroll and the
  bounded/ellipsize text helpers are confirmed scale-safe. Ends with a 3-lane parallel fix plan (A overlays
  ∥ B HUD-Studio ∥ C create-screen, all file-disjoint) and a 5-minute live GUI-scale test checklist.
  PROPOSED, awaiting Peetsa. No `src/` changes; doc-only.

## 2026-07-09 addition (Round 12 — undo/redo icons + selected-band left→right glow)
- `ui-pass-round12-undo-redo-selband-sweep-20260709.md` — two requests off the TEST 43 screenshot. (1)
  Replaced round 11's verbose "Undo Last Load" text button with a compact `↶`/`↷` icon PAIR, extending the
  one-shot undo into a one-level undo↔redo toggle (`CompassHudPreset` gains `redoSnapshot` + `redoLastLoad()`
  + `hasRedo()`; fresh Load/Import arms Undo & clears Redo; each button disabled when its stack is empty).
  (2) A picked Atlas band now shimmers with a Gaussian glow crest sweeping LEFT→RIGHT across its own
  highlighted color — the round-10 Random idea turned on its side — via new
  `LatitudePlanisphereRenderer.fillSelectedGlowStrip()` (column-slice strip, floored at SELECTED_BASE_GLOW=0.5
  so it always reads selected, rising to the full pop at the crest; reuses the Random bright/alpha mapping +
  SWEEP_FADE_FRAC seam-fade). NOTE: ↶/↷ are U+21B6/U+21B7 — if they box out on some setup, swap to ↺/↻.
  FOLLOW-UP (TEST 45): Peetsa found the selected shimmer too subtle and the icons too small — made the glow
  BOLDER (base-glow 0.5→0.35, bright gain →0.45 peaking ~1.45×, alpha →0xFF) and drew the ↶/↷ glyphs
  manually scaled ~1.9× on empty-labelled buttons (`drawButtonGlyph`, after super.extractRenderState) since
  MC's unicode font renders them tiny at button-message size. FOLLOW-UP 2 (TEST 46): when a specific zone is
  picked, the OTHER bands now render fully TRANSPARENT (map graphic shows through) instead of a muted color
  wash — only the selected band colors in; graticule + gold edges still draw; Random unchanged. compile+
  suite+build green; `TEST 46.jar` STAGED (SHA `aeba2d73…`), supersedes TEST 45→44→43.
  Row `20260709-ui-pass-round12-undo-redo-selband-sweep`.

## 2026-07-09 addition (HUD Studio round 11 — "Undo Last Load" in the Presets tab) [SUPERSEDED by round 12]
- `ui-pass-round11-preset-undo-20260709.md` — Peetsa wants to recover from an accidental preset Load/Import.
  New "Undo Last Load" button, disabled when there's nothing to undo, restores the HUD to exactly what it
  was right before the last Load-a-slot or Import action — one level, consumed on use. Snapshot is taken
  automatically at the TOP of `CompassHudPreset.applyToLive()` (the single choke point BOTH Load and Import
  already pass through), not at each button — the same single-source-of-truth reasoning round 9's sweep
  used to fix the "hand-maintained sibling list rots" bug (L27), so no future caller can forget to arm it.
  Guarded against the restore itself re-arming the undo point. compile+suite+build green; `TEST 43.jar`
  STAGED (SHA `25993594…`), supersedes TEST 42. Row `20260709-ui-pass-round11-preset-undo`.

## 2026-07-09 addition (Create-screen round 10 — animated "Random" spawn-zone Atlas sweep)
- `ui-pass-round10-random-sweep-20260709.md` — Peetsa wanted the Random spawn zone to feel special on the
  atlas: start colored in the middle, bands "strobe/cycle outward in opposing directions," a colorful
  double scroll. Implemented in `LatitudePlanisphereRenderer.renderCompact` for the `selectedBand == null`
  (Random) case only: a Gaussian glow PULSE starts at the equator and travels outward to the poles; since
  every band is drawn mirrored N+S, one pulse reads as two lit regions moving apart. Each band brightens in
  its own native climate color as the pulse passes (same pop the selected band uses), a gold crest line
  rides the front (sine-eased so no seam blink), and the loop restarts at the equator after the polar band
  fades past 90°. Wall-clock driven (System.currentTimeMillis, same as Aurora). Non-Random rendering
  byte-identical. Cosmetic tuning constants (period 3200ms / sigma 16° / pole-fade 108°). FOLLOW-UP:
  Peetsa wanted the loop seam softened — added a `sweepEnv` smoothstep envelope (SWEEP_FADE_FRAC=0.22) that
  eases the pulse IN at the equator restart and OUT past the pole instead of popping. compile+suite+build
  green; `TEST 42.jar` STAGED (SHA `04c1cd78…`), supersedes TEST 41→40. NOT committed (held for live look).
  Row `20260709-ui-pass-round10-random-sweep`.

## 2026-07-09 addition (UI pass round 9 — 3-agent sanity sweep over the uncommitted diff + 7 fixes)
- `ui-pass-round9-sanity-sweep-20260709.md` — before Peetsa re-tests the whole HUD Studio + create-screen
  arc by hand, an adversarial sanity sweep ran three independent review agents over the ENTIRE uncommitted
  rounds 7+8 diff: (1) adversarial UI bug hunt [Opus], (2) contradiction/duplication/staleness [Sonnet],
  (3) clean/efficient code review [Sonnet]. The Opus hunt found NO critical/high crash and positively
  cleared import null-safety, Aurora divide-by-zero (guarded twice), the reflection field copy, Gson
  round-trip of a preset saved before a field existed, all 4 new clamped fields (slider ranges == clamps),
  the Tape pin offset (not double-applied), the tab reindex, and the atlas scale. SEVEN fixes applied,
  now in the tree: [HIGH] `LatitudeHudStudioScreen.applyDefaults` (Reset HUD) silently skipped all 4 new
  fields (rainbowCycleSeconds/zone/biome/coordsTextScale) even though the narrower Reset Compass/Labels
  were updated — the hand-maintained mirror rotted while the reflection preset path didn't (L27);
  [MEDIUM] `CompassHudPreset.applyToLive` wrote title numerics to the LIVE config unclamped (saveCurrent
  only sanitizes disk) — new `LatitudeConfig.sanitizeLive()` (= applyFrom(captureTo()), reuses
  LatitudeConfigData.sanitize) called before saveCurrent (L28); [MEDIUM] Zone/Biome Text Size sliders
  stayed live when the label was OFF — gated in `updateSidebarVisibility`; [MEDIUM] index.md round-8 entry
  still said 0.62f/TEST 37, contradicting its own registry row — corrected to 0.82f/TEST 39; [LOW]
  biome-attached-alone drew with zoneTextScale (biomeTextScale a no-op) — new
  `CompassHud.attachedZoneScale` helper; [LOW] scaled-up attached text overflowed its hitbox/border
  vertically (width scaled, height not) — new `CompassHud.attachedTextLineHeight` used in both
  computeAnalogBounds and renderAnalogAt; [LOW] `CompassHudPresetSlots.isOccupied` counted a corrupt
  (compass==null) slot as occupied — now requires p != null && p.compass != null. Deferred/flagged (not
  bugs to fix now): themeShort() default-fallback future-proofing; a PRE-EXISTING TitleFields-vs-Reset
  Title default-literal drift (Peetsa to pick canonical); presetFormatVersion written-but-never-read stub;
  the documented zone+biome-both-attached fused-line scope boundary. compile+suite+build green; `TEST
  40.jar` STAGED (SHA `283a3bb5…`), supersedes TEST 39. Row `20260709-ui-pass-round9-sanity-sweep`.

## 2026-07-08 addition (Create-screen round 8 — Classic (1:1) atlas oversized/clipping, Mercator untouched)
- `ui-pass-round8-fixes-20260708.md` — Peetsa: the atlas clips off the panel at Tiny world size on the
  Classic (1:1 square) shape, and Itty Bitty still looks very large; fix the 1:1 only, leave 2:1 Mercator
  alone. ROOT CAUSE: the atlas radius fit-budget's `widthDivisor` (2.0 Classic vs 4.0 Mercator, since a
  Mercator globe is 2x as wide as tall for the same radius) means Classic's width budget comes out roughly
  DOUBLE Mercator's for the same panel — in practice that made Classic almost always height-bound (same
  height budget for both shapes wins the `min()`), so `previewDiscFill`'s per-size grading barely mattered
  (even Itty Bitty rendered near the height ceiling) and there was little margin left once real label/
  padding/frame math was subtracted, clipping at Tiny. FIX: new `CLASSIC_ATLAS_SCALE` applied to the
  radius ONLY when shape isn't Mercator, after the shared budget math, before the existing fit-shrink
  loop — Mercator's code path reads nothing new, byte-identical. First try `0.62f` overshot (bunched the
  world sizes together — a flat multiplier shrinks the pixel GAP between sizes along with overall size);
  raised to **`0.82f`** (happy medium between 1.0-too-big/clipping and 0.62-too-small/bunched).
  compile+suite+build green; `TEST 39.jar` STAGED (SHA `84f6c803…`), supersedes TEST 38 (which superseded
  the 0.62 TEST 37 attempt, TEST 36 before that). Row `20260708-ui-pass-round8-fixes`.

## 2026-07-08 addition (HUD Studio round 7 — Tape pin fix, Rainbow theme, presets/export, per-label text size)
- `ui-pass-round7-fixes-20260708.md` — (1) fixed the Tape-only pin-marker bug Peetsa's screenshot
  confirmed ("+ crosshairs" floating above the tape strip): a recurrence of LESSONS L23 (content-vs-box
  mismatch) in `drawPinMarkers()`, which the original L23 pass never touched — same
  `(diameter - contentH) / 2` offset applied elsewhere now applied there too, ANALOG-only, no-op for
  every look but Tape. (2) added a `RAINBOW` analog color-scheme theme (time-based hue rotation via
  `System.currentTimeMillis()`, ring/needle opposite on the wheel) — inserted mid-enum before `CUSTOM`
  (safe: no ordinal dependencies anywhere, Gson persists by name), auto-appears in the existing Color
  Scheme cycle button. (3) new "Presets" tab: 8 numbered save/load/clear slots + Export/Import-to-clipboard,
  backed by new `CompassHudPreset`/`CompassHudPresetSlots` classes (single snapshot shape shared by both
  features; reflection-based field copy so new config fields can't be silently missed). (4) three new
  Labels-tab sliders (Zone/Biome/Coords Text Size, 0.5-3.0, default 1.0) — full independent control when
  detached or (coords) always; when zone+biome BOTH ride attached to the compass they still share one
  fused line/size (documented scope boundary, sidestepped by detaching either one). compile+suite green;
  `TEST 35.jar` STAGED (SHA `08b3da58…`), supersedes TEST 34. Row `20260708-ui-pass-round7-fixes`.

## 2026-07-08 addition (HUD Studio round 6 — real CycleButton tooltip-wipe bug found + fixed)
- `ui-pass-round6-fixes-20260708.md` — (1) restored "opacity" wording in the three tooltips round 5
  changed to "see-through" (Peetsa: not jargon). (2) A REAL, confirmed bug: "after you click on a button
  and go back and hover over it, the tooltip is gone and will not come back." Decompiled the 26.2
  `CycleButton` class and found the exact mechanism — it has its OWN internal tooltip mechanism (a private
  `tooltipSupplier` field + an auto-called `updateTooltip()` that fires on EVERY value change, no
  null-check) that this codebase never populates (tooltips are attached externally via `setTooltip`
  after the builder chain) — so the FIRST click on ANY of the Studio's ~30 dropdown-style controls
  silently overwrites its tooltip with `null`, permanently. Fixed by patching `tooltipSupplier` via
  reflection inside the one shared `tooltip()` helper (CycleButton exposes no post-construction setter)
  to a constant supplier returning the same `Tooltip` — correct behavior since every Studio tooltip is
  static text, not value-dependent. Falls back gracefully if the field is ever renamed upstream. (3) Title
  Case tooltip already lists Normal correctly (from the prior correction commit `30a5f7fd`) — confirmed,
  no further change needed. compile+suite green; zero worldgen files touched. `TEST 34.jar` STAGED (SHA
  `f656dc5b…`), supersedes TEST 33. Row `20260708-ui-pass-round6-fixes`.

## 2026-07-08 addition (HUD Studio round 5 — Title tab bugs, tooltip pass, Tape legibility floor)
- `ui-pass-round5-fixes-20260708.md` — (1) "Show Degrees" did nothing: the title preview's RENDER call
  site duplicated the sample-title logic inline instead of calling the already-existing shared
  `studioPreviewTitle()` helper (used only by the drag hit-test), so the one place that mattered never
  checked the flag. Fixed by deleting the duplicate and having the shared helper (now flag-aware) serve
  both. (2) Title Case "duplicate" — **CORRECTED same session**: NORMAL's no-op transform was
  indistinguishable from UPPERCASE because the Studio's no-world SAMPLE title was hardcoded all-caps —
  exactly the scenario Peetsa tests from. The diagnosis was right; removing `NORMAL` entirely was NOT
  what Peetsa wanted (he values "Tropical" natural-case and asked for it back immediately). Reverted the
  removal in full and fixed the actual bug instead: `studioPreviewTitle()`'s no-world fallback is now
  natural-case ("Tropics 12°S", not "TROPICS 12°S"), matching the live in-world title — Normal and
  Uppercase now genuinely differ everywhere, no option removed. See registry row
  `20260708-ui-pass-round5-correction`. (3) Tooltip pass: stripped "(Folded in from
  the old F9 Settings screen.)" from both occurrences; rewrote ~15 jargon-first tooltips into plain
  sentences; the two Zone/Biome Text Grow tooltips Peetsa flagged directly were rebuilt with concrete
  examples instead of internal "pin" vocabulary. (4) Tape look illegible at small Analog Size: gave Tape
  its own 32-unit floor at the single existing `analogDiameter()` choke point (not a new width/box split —
  avoids the L23 class of bug just documented), plus the Studio slider's own minimum raises to match so
  the displayed number is never a lie. compile+suite green; zero worldgen files touched. `TEST 33.jar`
  STAGED (SHA `b18c50ed…`, corrected build — the removal-era `17f528d2…` was never tested live), supersedes
  TEST 32. Row
  `20260708-ui-pass-round5-fixes`.

## 2026-07-08 addition (create-screen round 4 — atlas label misalignment fixed; "Ginormous!" flourish)
- `ui-pass-round4-fixes-20260708.md` — Peetsa: atlas latitude LABELS don't line up with the atlas's own
  gridlines, worst at Itty Bitty ("0° almost at the North Pole"), better at larger sizes. Root cause:
  `computePreviewLabelYs()`'s overflow recovery SLID THE WHOLE COLUMN UP uniformly (including the 0°
  label, which starts at its exactly-correct true position) whenever the de-collision cascade pushed the
  last label past the bottom — at small radii the true gaps are smaller than the comfort gap, so the
  cascade + its corrective shift both fire hard, dragging 0° far from its own gridline. Confirmed the
  true-position formula already matches `LatitudePlanisphereRenderer.renderCompact()`'s band-line math
  exactly — the bug was purely in the recovery step, not a label/gridline formula mismatch. FIX: never
  move the 0° label (the column's one guaranteed-correct anchor); when overflow would occur, compress the
  cascade's GAPS toward a bare no-overlap floor instead of shifting the column — 0° stays pinned, no
  overlap ever, and interior labels only compromise when truly necessary (hand-verified at several
  simulated radii). Also: "Ginormous" world size now renders italicized as "Ginormous!" (Peetsa's ask;
  every other size name unaffected). compile+pure-JVM suite green; zero worldgen files touched. `TEST
  32.jar` STAGED (SHA `c7cdce7c…`), supersedes TEST 31. Row `20260708-ui-pass-round4-fixes`.

## 2026-07-08 addition (legacy binder docs retroactively indexed — closing pre-Fable-arc indexing gaps)
- A sweep for orphaned binder docs (not referenced in index or registry) found five, all pre-dating the
  Fable-5 arc; indexed here for discoverability (no new registry evidence rows fabricated — these record
  their own prior proof):
  - `arid-belt-earthlike-20260625.md` — subtropical arid belt (badlands/desert/mesa) Earth-like design +
    anti-regression spec; staged jar `d39de6a2` (2026-06-25 worldgen work).
  - `atlas-viewer-modernization-brief-20260605.md` — super-handoff brief for the Atlas Viewer
    modernization all-nighter (dev-only tooling; see memory `atlas-viewer-modernization`).
  - `headless-live-test-20260630.md` — what a headless run can vs can't stand in for vs an in-game
    eyeball; staged jar `d8657778` (2026-06-30 snow-line/world-creation validation).
  - `headless-verification-playbook.md` — the standing methodology doc for proving worldgen behavior
    headlessly (agent can't reliably pilot the GLFW client); cross-agent (Codex/Claude).
  - `version-bump-2.0-20260701.md` — the 1.4.1-beta.2 → 2.0-beta.1 rename decision (Peetsa, 2026-07-01;
    see memory `latitude-versioning-convention`).

## 2026-07-08 addition (documentation catch-up — LESSONS L21-L25 + C-4 bathymetric-relief design)
- **LESSONS L21-L25** landed in the MAIN worktree `docs/LESSONS.md` (commit `0fa11aa6` on `Latitude
  (Globe)` main; per L10 lessons live in that one surface). L21 = changing one layer of a multi-map
  system exposes the other layers' latent mismatches (terrain carve revealed the un-wired biome
  decoupling; reads-as-regression-but-isn't). L22 = grade a boundary-triggered transform's ONSET not just
  its magnitude (the carve-onset wall), and grade the clamp TARGET never the clamped OUTPUT (the
  density-blend hollowing dead end). L23 = when one value becomes two (content-box vs layout-box) re-audit
  every consumer's contract; equal-case call sites hide the bug (Tape-only Studio dock). L24 = vanilla
  estimators (getBaseHeight/_WG) misreport under a custom density wrapper; audit against the ground-truth
  field (nearly a false "oceans got shallower" report). L25 = a framework focused/active flag can be
  sticky; track the interaction lifecycle yourself (stuck transparency checkerboard).
- `docs/design/bathymetric-relief-c4-design-20260708.md` — DESIGN ONLY (not authorized): deeper oceans +
  earth-y trenches. Key finding: the geology already exists — `GeoSummary` exposes `coastDistanceBlocks`
  (abyssal profile) and `islandArc01` = `seamProx·typeContrast·arcN` (subduction/arc-trench signal), both
  already populated by GeoAuthority, so C-4 is a carve DEPTH-formula change consuming existing signals,
  zero geology-engine change. ~83-block trenches fit under the Y-64 floor. Constraints from earned lessons
  baked in (grade trench walls per L22, deep-flood gate, density-ladder audit per L24, r=0 identity +
  gap-delta gates, live knobs per L17). Recommended first step = MAP where trenches fall on Peetsa's seed
  (add islandArc01/seamProx to the coherence probe) before carving. Row `20260708-c4-bathymetric-design`.

## 2026-07-08 addition (Ocean depth anchor — "shallower than vanilla?" measured; answer: no, it deepens)
- `ocean-depth-anchor-20260708.md` — Peetsa asked whether the ocean is shallower than vanilla. Measured
  on his seed (r=0==vanilla vs r=1): where geography wants ocean, VANILLA mostly put land (median floor
  at sea level, up to Y139); the carve pulls it to open water, and `min()` semantics mean it can only
  deepen or equal a column, never shallow it. What reads as "shallow" is two intended things: the
  deliberately-shallow continental-shelf apron near shore, and carved basins settling to a uniform ~24
  blocks (max depth = strength 0.4 × K_DEPTH 60) instead of vanilla's abyssal trenches — a TUNABLE knob
  (`K_DEPTH_BLOCKS`, ocean-only), not a design floor. Two secondary findings: (A) tooling — getBaseHeight
  OCEAN_FLOOR_WG mis-estimates deeply-carved columns as high (Y94-118) though the density ladder proves
  them carved below Y40; metric artifact, world is correct; trust the density ladder on carved columns.
  (B) hypothesis — that same _WG estimate feeding structure placement is a plausible mechanism for
  "ships in a forest"; UNVERIFIED, belongs with the biome/structure-side work. Row `20260708-ocean-depth-anchor`.

## 2026-07-08 addition (Slice C-3 LIVE GREEN — the wall is gone; continental shelves confirmed in-game)
- Registry row `20260708-c3-live-green` (and a LIVE CONFIRMATION block prepended to
  `fable5-slice-c3-grip-20260707.md`). Peetsa flew the TEST 30 r=1 world across the same 38-46°S /
  131-137°E coast as the TEST 29 wall: it's GONE — every coastline now grades into water as a
  continental shelf (terraced shallows, the old massif is an island sloping into the sea, carved
  deep-ocean floor + kelp). C-3 live-closed. His three biome complaints (Forest-over-water,
  shipwreck-in-forest, Meadow-at-waterline) are all correctly seen and all the SAME known issue — the
  TEST 28 three-map decoupling (biome layer on the pre-carve map; consumer not wired) — NOT a C-3
  regression; fix path is the already-queued consumer law-compliance slice + interim label-side
  candidates. Terrain half of Phase 4 is now live-verified on both legs (land-lift TEST 27, carve/shelf
  TEST 30); remaining worldgen work is all biome-side.

## 2026-07-08 addition (TEST 30 UI pass round 3 — three bugs, three independent root causes)
- `ui-pass-round3-fixes-20260708.md` — Peetsa's screen recording of a TEST 30 HUD Studio + create-screen
  pass, reviewed frame-by-frame at native resolution before any code change. (1) Atlas position
  "bouncing" when cycling world size: `computeSizeLabelBottom()` reserved space using only the
  SELECTED size's wrapped description line count, so `leftPreviewTopY` (and the "ATLAS" heading with
  it) moved whenever a different size's description wrapped to a different line count — fixed by
  reserving the worst-case line count across all sizes (same pattern as the compass HUD's
  reservedTextWidth). (2) Tape + Attach-to-Hotbar rendering off-screen in the Studio preview only
  (correct live in-game): the Studio-preview render call site fed `computeAnalogBounds()`'s
  content-true hitbox `y` into `renderAnalogAt`, which expects the DIAL-BOX top-left — double-applying
  round 2's content-vs-box adjustment for looks where they differ (Tape only; every other look has
  content==box so the bug was invisible elsewhere). Fixed by computing the actual box position at that
  call site the same way the live path does; the now-dead `computePreviewBounds()` was deleted.
  (3) Transparency checkerboard "locked on" after the first slider touch: `transparencyAdjustActive()`
  checked `isFocused()`, which is STICKY in vanilla (persists past mouseUp until another widget is
  focused) — fixed by having `FloatSlider` track its own click→release lifecycle explicitly
  (`isDragging()`), dropping `isFocused()` entirely. All three: compile + pure-JVM suite green; zero
  worldgen files touched (C-3 untouched). `TEST 31.jar` STAGED (SHA `1c589a15…`), supersedes TEST 30.
  Row `20260708-ui-pass-round3-fixes`.

## 2026-07-07 addition (Slice C-3 grade-the-grip GREEN + TEST 30 staged — the wall fix)
- `fable5-slice-c3-grip-20260707.md` — Peetsa authorized the wall fix same night. SHIPPED DESIGN: the
  carve CEILING descends from Y160 (taper top) to the C-2 depth target across `smoothstep(|d|/gripWidth)`
  — pure min() semantics preserved. DEAD END recorded: the first attempt blended DENSITIES and the
  gap-delta tripwire caught it re-creating the TEST 27 hollowing class at partial grip (14950: gaps 0→8)
  — density blending in the carve regime is rejected, don't retry. Knob `-Dlatitude.terrainV2.gripWidth`
  (default 0.8 measured on the wall transect; 0.4 too tight on steep gradients; 0 = legacy). Gates 5/5:
  r=0 byte-identity, wall-transect grading (80 stands where C-2 planed to 60), open-ocean bit-identity,
  0 lowered floors / 0 flips on the 81-grid, no gap/fragment regressions. `TEST 30.jar` STAGED (SHA
  `13a5cd19…`, = UI round 2 + C-3); fresh world required. Row `20260707-fable5-slice-c3-grip`.

## 2026-07-07 addition (TEST 29 — first r=1 live look: the wall = carve-onset cliff; UI round 2 landed)
- `test29-r1-look-and-ui-round2-20260707.md` — TEST 29 ran **r=1 live** (log-verified). The "cursed
  giant wall" at 40°S 135°E is DIAGNOSED via headless transect: a real geography coastline (land01
  1→0 over ~300 blocks; NOT the edge band) where the carve ceiling's INSTANT grip planes tall old-map
  hills to sea level — the go/no-go's "sharp-edge shoulder" residual promoted to a 20-40-block cliff
  at r=1. Fix candidate "grade the grip" (lerp the ceiling in over a coastal ramp; monotone-safe)
  awaits authorization. Img-2 shelf terraces = system working as designed. UI round 2 ALL LANDED:
  atlas top-aligned, Random rainbow-italic when selected, LATITUDE letterpress wordmark, checkerboard
  only-while-adjusting + content-sized (also fixes tape grab confusion + docked preview overhang),
  snap→Labels, preview text defaults LIVE, drag-undocks-the-dock, Reset Labels/Title tracked, and an
  always-on create-click zone-selection log line (the Random-created-Temperate mystery is evidence-
  bounded: selection was false at click; wiring verified correct). Jar `a97ea5c8…` HELD → TEST 30 on
  Peetsa's word. Row `20260707-test29-r1-ui-round2`.

## 2026-07-07 addition (TEST 28 "geography looks off" — three-map ocean decoupling, measured)
- `test28-deep-ocean-decoupling-20260707.md` — Peetsa's "Deep Ocean over knee-deep water / tree in
  water / unsunken wreck / water level off?" report, diagnosed with log ground truth + headless probes
  at his exact columns. NOT a water-level bug and NOT C-2: the world runs r=0 (log-verified) and the
  area is the pre-existing THREE-MAP decoupling — vanilla terrain (seabed) vs legacy ODF labels
  ("Deep Ocean", gravel dressing, seam features/structures) vs GeoAuthority (says land01=1.0 = solid
  CONTINENT at all four probed columns). Grid quantification: 40/81 aligned; 24/81 double-land over
  geo-ocean (r=1 carves these — verified 33 carve + 21 labels realign); **8/81 = his phantom-ocean
  class, bit-identical at r=1** (both C-2 vetoes require isOceanIntent) + 3/81 phantom-ocean-on-dry
  → post-r=1 THE dominant remaining artifact class (~14%). Fix candidates enumerated for
  authorization (narrow land01 label veto; depth-honest deep-vs-regular family); full fix = consumer
  flip behind law-compliance. Preset-radius map extended: globe=15000/UI-Large. Row
  `20260707-test28-geo-decoupling`.

## 2026-07-07 addition (TEST 28 UI pass round 1 — 5 bugs fixed, 2 create-screen requests landed)
- `ui-pass-round1-fixes-20260707.md` — Peetsa's mid-pass findings, all root-caused in code and fixed:
  tape-docked-too-high/too-large (dock ladder docked TAPE's phantom diameter box; now docks the look's
  TRUE content height — the strip sits ON the hotbar row), L-toggle leaving Direction Format + Compass
  Look visible (hand-list vs tracker split; blanket pass now; Reset Compass was tracked by NEITHER and
  also didn't scroll — fixed), Direction Format inert on analog (only the digital line ever rendered it;
  TAPE labels now honor it incl. degrees, and the row only exists where it has an effect), analog-size ×
  dock strangeness (same content-true fix + content-true Studio border/hitbox/drag + ghost hotbar for
  docked analog previews), Reset-lands-mid-screen (U-D's 15% boss-bar nudge in `fresh()` reverted to the
  classic top-center default). Create screen: atlas now centers the MAP not the map+labels composition;
  new sixth spawn-zone row **Random** (rolled to a concrete band at create time, logged; planisphere
  renders highlight-free via null-tolerant `renderCompact`). Compile + pure-JVM suite green; zero
  worldgen files touched. Live re-test = TEST 29 candidate (specific matrix in the doc). Row
  `20260707-ui-pass-round1-fixes`.

## Binder sections
- `future-pass-ideas.md`: parked Julia ideas that are not active implementation scope yet.
- `evidence-registry.md`: append-only list of proof and savepoint evidence.
- `evidence-record-schema.md`: required evidence-row contract and enums.
- `evidence-schema.json`: machine-readable schema for evidence rows.
- `update-contract.md`: triggers, stale debt handling, and savepoint gate directives.
- `notion-latitude-source-map.md`: Notion-to-binder source map and current gap ledger.

## Historical 1.4.1 Candidate Notes (superseded — see [[latitude-versioning-convention]] for the 2.0-beta.1 rename; this section is pinned to savepoint `c9da0f93`, well behind current HEAD)
- `stony-treeline-desert-followup-20260621.md`: active worldgen follow-up; first source slice restores the historical Y168 tree line/fade 28 and tightens live temperate-mountain authority. Fresh staged SHA `76c5b020...` exact-window live proof reproduced the desert locate mismatch at `[4052,86,3156]`; seeded proof showed vanilla raw source said desert while Latitude wrapped/populated truth said dark forest. Final registry-backed locate wrapper staged as SHA `af1579b2...` is live-green: `/locate biome minecraft:desert` returned `[5272,75,2353]`, HUD proof and saved chunk proof both report `minecraft:desert`, and Julia's same-run screenshot shows visible palm fronds as supportive visual evidence. Stony peaks are visually green from the accepted `[1583,170,4028]` live angle, with saved-surface sampling showing a rugged mostly-stone Y95..Y159 spread. Treeline constants are green: `TREE_LINE_Y=168`, `TREE_LINE_FADE_BAND=28`.
- `c9da0f93` / `save/biome-tuning-followup-26.1.2`: local savepoint for the biome-tuning follow-up. Fresh savepoint rebuild staged into `Lat 1.4+26.1.2` as SHA `1f50c5954cef3c91de1b071e78172ad6940a41abe5bace3e6febde5ac449a477` with manifest commit `c9da0f93029f7f16c50a7bc89eb766c576a85b48`; evidence lives in `tmp/closeout-1.4-20260621/rebuild-c9da0f93-175345` and `tmp/closeout-1.4-20260621/profile-stage-c9da0f93-175416`.
- `tmp/closeout-1.4-20260621/final-live-launch-blocker-181416`: historical c9 launch blocker. Superseded after Julia reopened Modrinth and the c9 candidate launched.
- `tmp/closeout-1.4-20260621/final-live-c9-192419`: c9 final live cruise. Green for exact Java window, c9 build line, existing Globe load, 20,000 border, command control, desert locate/HUD, short clean movement soak, and save/quit.
- `tmp/closeout-1.4-20260621/stony-followup-c9-195009`: c9 stony follow-up. Fresh `/locate biome minecraft:stony_peaks` returned `[1452,170,4201]`; settled HUD reads `minecraft:stony_peaks`; `/execute if biome ~ ~ ~ minecraft:stony_peaks run say stony_peaks_here` passed; save/quit and final no-Java process check are green.
- `tools/lat-post140-findings-classifier`: repeatable read-only classifier for the pasted post-1.4.0 findings; prior proof passed on staged `e09ea003...`, and a final refresh against `1f50c595...` is pending before publication.
- `e09-itty-headless-atlas-live-lock-20260621.md`: historical `e09ea003...` Itty headless Atlas diversity proof and earlier locked-session live gate; Julia's 2026-06-21 manual retest later closed loading-screen/chunk-loading symptoms, while scenic/palm/decoration/performance remain open on the c9 candidate.
- `e09-profile-stage-clean-proof-settings-20260620.md`: historical `e09ea003...` profile staging and proof settings.
- `e09-fresh-small-live-smoke-shutdown-20260620.md`: historical `e09ea003...` exact-window fresh SMALL smoke and save/quit proof; superseded for release closeout by the c9/`1f50c595...` final live cruise.
- Final release cruise plus stony follow-up are live-green on staged SHA `1f50c595...`; branch/tag push, upload, publication, cleanup, and porting remain Julia-owned manual gates.

## Evidence ownership
- Project-wide owner: /Users/joolmac
- Scope owner per topic:
  - worldgen: gameplay/worldgen owner
  - rendering: client-rendering owner
  - compatibility: porting/interop owner
  - release: release coordinator
  - migration: migration engineer

## Stale status interpretation
- `active` means within policy windows and usable for savepoint/release checks.
- `stale` means the section must be refreshed before any new proof-complete savepoint.
- `superseded` means replaced by a newer evidence entry.
- `retired` means archival only, not used for current decisioning.

## Entrypoint rule
All binder-related updates must be reflected in `evidence-registry.md` and the relevant topic row in `index.md`.
