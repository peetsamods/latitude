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
| bug blaster investigation and closure | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/fix-history.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/fix-history.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/biomes-coverage.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/biomes-coverage.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/release/checklist.md) | compatibility | 2026-06-04 | active | 2026-06-18 |
| compatibility and migration status | [/Users/joolmac/CascadeProjects/Latitude (Globe)/tmp/audits/MIGRATION_STATUS.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/tmp/audits/MIGRATION_STATUS.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/tmp/audits/26-1-mixin-cluster-research](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/tmp/audits/26-1-mixin-cluster-research), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-184901/mod-present-atlas-summary.txt](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-184901/mod-present-atlas-summary.txt) | migration, compatibility | 2026-06-18 | active | 2026-07-02 |
| release and handoff readiness | [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/current-readiness-audit.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/current-readiness-audit.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/current-gates.json](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/current-gates.json), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/live-proof-runbook.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/live-proof-runbook.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/CHANGELOG.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/CHANGELOG.md) | [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/26.1.2-canonical-roadmap.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/26.1.2-canonical-roadmap.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/lat-readiness-nonlive-status](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/lat-readiness-nonlive-status), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/lat-post140-findings-classifier](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/lat-post140-findings-classifier), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/mc-wait-shot](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tools/mc-wait-shot), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856) | release | 2026-06-21 | active | 2026-07-05 |
| live proof and runtime evidence | [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/evidence-registry.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/evidence-registry.md) | /tmp evidence payload folders listed in registry entries; [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/release/checklist.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-live-20260618-211834/live/visual-audit-notes.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-live-20260618-211834/live/visual-audit-notes.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-final-status.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-final-status.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-status.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/live-parity-status.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/screen-lock-session-state.txt](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/atlas-worldsize-parity-20260619-081729/live/screen-lock-session-state.txt), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/itty-terrain-render-bug-20260620-072856), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/code-red-itty-palm-performance-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/code-red-itty-palm-performance-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/post-140-findings-review-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-profile-stage-clean-proof-settings-20260620.md), [/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md](/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/docs/binder/e09-itty-headless-atlas-live-lock-20260621.md) | release, worldgen, compatibility, rendering, migration | 2026-06-21 | active | 2026-07-05 |

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
