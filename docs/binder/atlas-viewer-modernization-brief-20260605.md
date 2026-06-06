# SUPER HANDOFF — Atlas Viewer Modernization (ultracode all-nighter)

**Date:** 2026-06-05 · **Author:** design/research pass · **Audience:** a fresh implementing agent with NO prior context
**Target branch (to create):** `feat/atlas-viewer-modernization` off canonical `feat/1.3.1-cohesive-horizons-26.1.2` @ `8c73beab`
**Governance:** operate under `latitude-regression-fix-workflow`; map-based proof only; binder (`docs/binder/`) + Notion logging; one logical bucket per savepoint.

---

## 1. MISSION + NON-NEGOTIABLES

**Mission:** Bring the IMPROVED Atlas viewer + Pregenerated World Map feature onto canonical by **porting the atlas-tooling bucket** (`tools/atlas/*` + world-map gradle tasks + the world-map Java exporter), then add **3 user-requested viewer features** in the same pass. Canonical worldgen is FIXED and savepointed — it must remain byte-for-byte untouched.

**Non-negotiables:**
1. **Port tooling, NOT worldgen.** Copy only `tools/atlas/*`, the world-map gradle tasks, and `WorldMapPreviewHeadlessRunner.java`. **Never** bring over `LatitudeBiomes.java`, `BiomePreviewExporter.java`, `BiomeBandPolicy.java`, `ChunkGenerator*` mixins, or any biome-placement/palette/tag mutation from the isolated branch.
2. **New branch off canonical.** `git checkout -b feat/atlas-viewer-modernization` from `8c73beab`. Do all work there.
3. **Do NOT `git merge` the isolated branch.** A merge drags broken worldgen + forces a `LatitudeBiomes.java` conflict. Port files by **copy / surgical insertion**, file-by-file, from the isolated worktree path.
4. **latitude-regression-fix-workflow** is LAW (Constitution + Logging Contract + Architecture Ref). Art VI: no floorDiv/cell-hash; map-based proof. Art X: monoculture = regression.
5. **Binder + Notion logging.** Log this as a TOOLING bucket. Record evidence in `docs/binder/evidence-registry.md` + `docs/binder/index.md`; mirror to Notion.
6. **Compile / smoke / savepoint as a TOOLING bucket SEPARATE from worldgen.** The savepoint commit message + binder entry must state explicitly: "tooling only; worldgen untouched; biome distribution unchanged." Prove worldgen integrity with a before/after `world_biome_inventory.json` diff (must be identical for same seed).

---

## 2. CURRENT STATE + PROVENANCE

**CANONICAL (target):** `/Users/joolmac/CascadeProjects/Latitude (Globe)`, branch `feat/1.3.1-cohesive-horizons-26.1.2` @ `8c73beab`.
- Worldgen FIXED & savepointed (v1.4 "Cohesive Horizons": tropical jungle ~71%, equator arid ~17%, confetti tiny-share ~7%).
- Atlas viewer is the OLDER/smaller version. Analyzers ALREADY present in `tools/atlas/`: `band_balance_analyze.py` (per-band family ratios + a `DELTAS (baseline -> candidate)` path), `longitudinal_variety.py`, `distinct_render.py` (offline golden-ratio distinct PNG, keyed on volatile index), `embedded_speck.py` (TRUE-confetti = small component fully enclosed by ONE other biome). `atlas_runner.py`, `viewer_api_server.py`, `viewer/index.html`, `desktop-app/`, `palette_authority.json` present.
- **VERIFIED THIS PASS:** canonical `build.gradle` has **NO** `worldMapPreview` / `runWorldMapPreview` / `worldMapPresetForSize` (the world-map tasks are genuinely absent). `src/main/java/com/example/globe/dev/` has **NO** `WorldMapPreviewHeadlessRunner.java`. `LatitudeBiomes.java` **already has** `setWorldSeed(long)` (line 597) and `setActiveRadiusBlocks(int)` (line 608) — so the exporter's call sites have their hooks; no worldgen edit needed.
- Uncommitted working-tree state: desktop-app config now canonical + a worktree-guard added (uncommitted). Many `run-headless/world/*` deletions are present in the dirty tree — **do not commit these incidentally**; scope the savepoint to tooling files only.

**ISOLATED (source of viewer improvements):** `/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc`, branch `codex/atlas-world-map-p0-e19fc1cc` @ `ca81036f`.
- Has the IMPROVED viewer + "Pregenerated World Map" feature, but **OLD/BROKEN worldgen** (must NOT come over).
- All isolated line numbers in this brief are @ `ca81036f`. Re-grep on arrival; do not trust line numbers blindly.

**Viewer file deltas (canonical → isolated):**
- `tools/atlas/viewer/index.html`: 5722 → 7340 lines
- `tools/atlas/viewer_api_server.py`: 1016 → 1850 lines
- `tools/atlas/desktop-app/main.js`: 914 → 970 lines

**Atlas data format (raw run dir):** `biome_ids.png` (per-pixel biome INDEX in RED channel), `biome_palette.json` (index→biome_id), `palette_authority.json` (biome_id→hex), `world_biome_inventory.json` (per-biome counts/shares), `bands.png` (geometric latitude bands), `land_bands.png` (resolved band; seam palette tropical `#7A4A28` / subtropical `#F5A623` / temperate `#3FAF5A` / subpolar `#3D7FC7` / polar `#B7C8E5`), `legend.json`, `biomes.txt` (radiusBlocks/stepBlocks). Bands: tropical 0–23.5, subtropical 23.5–35, temperate 35–50, subpolar 50–66.5, polar 66.5–90. `latitude = |blockZ|/radius*90`. Provider stack: minecraft + biomesoplenty + terralith + promenade (~78 biomes).

---

## 3. PHASE PLAN (ultracode all-nighter)

Each phase = deliverables + an acceptance GATE. Do not advance past a red gate.

### Phase 0 — Preflight + dep-map (S)
- **Do:** `git checkout -b feat/atlas-viewer-modernization`. Confirm clean tooling scope (stash/ignore the `run-headless/world/*` deletions). Open both repos. Re-grep isolated for the exact line ranges in §4.1 (line numbers drift). `grep "worldMap" isolated/BiomePreviewExporter.java` → expect 0 matches (confirms its +767 LOC is atlas refinement, not world-map; we DISCARD it anyway).
- **Gate:** branch created off `8c73beab`; isolated paths confirmed; canonical worldgen files enumerated as NO-PORT (see §4.1 "Keep Canonical").

### Phase 1 — Port: Java + Gradle baseline (M)
- **Do:** Copy `WorldMapPreviewHeadlessRunner.java` verbatim into `src/main/java/com/example/globe/dev/`. Add the 5 gradle blocks (§4.1). Register the runner's `ServerLifecycleEvents.SERVER_STARTED` hook (confirm it self-registers or wire it in `GlobeMod`/dev init the same way `BiomePreviewHeadlessRunner` is).
- **Gate (compile):** `./gradlew compileJava` green; `./gradlew build --dry-run` parses all new tasks. `LatitudeBiomes.java` git-diff EMPTY.

### Phase 2 — Port: Python backend (M)
- **Do:** Surgically insert world-map routes/handlers/constants into `viewer_api_server.py` (§4.1). Add `check_world_map_run.py` verbatim. Update `atlas_runner.py` (`SIZE_RADIUS`, `PROVENANCE_IGNORED_PREFIXES`).
- **Gate:** `python3 -m py_compile` clean on all three; `GET /api/runs` and `GET /api/world-map/jobs` both return (empty OK).

### Phase 3 — Port: Viewer frontend (M)
- **Do:** Replace `tools/atlas/viewer/index.html` with isolated (7340 lines). Extend `desktop-app/main.js` with `exportRunDataFromDesktop()` + IPC handler. Verify script imports (Plotly/Tauri), `/api/...` paths, `preload.js` path.
- **Gate (smoke):** viewer loads; Atlas mode lists existing runs + renders map; World Map tab toggles; `./gradlew runWorldMapPreview --latdevWorldMap --job=smoke --size=small --maxtiles=4` writes `run-headless-worldmap/latdev/world-map-runs/smoke/job_state.json`; viewer lists the job + coverage SVG.

### Phase 4 — Feature: Mod/namespace filter (S/M) — *client-only*
- **Do:** Implement §4.2 entirely in `index.html`. No server change.
- **Gate:** chips for all 4 namespaces with correct counts; isolate dims others; AND-composes with biome search; per-pixel mask uses `nsIdByIndex` Uint8Array (no per-pixel string split); vanilla-only run = single chip, no crash on `unknown`.

### Phase 5 — Feature: Distinct palette (M) — *client-only + offline parity*
- **Do:** Implement §4.3: add `distinct` mode to `#sel-color-view`, `buildDistinctPalette()` keyed on `stableSortBiomeIds` rank with namespace hue arcs + OKLab ΔE collision sweep; legend = Biome Presence swatches follow mode. Refactor `distinct_render.py` to the same id-rank scheme for QA parity.
- **Gate:** min pairwise ΔE(OKLab) ≥ 12, zero exact RGB collisions; same biome_id → same color across two runs sharing a namespace; toggle = pure repaint (no network); legend swatches match map pixel-for-pixel.

### Phase 6 — Feature: Over/under-representation report (M/L)
- **Do:** Implement §4.4: new `tools/atlas/representation_report.py` (reuse `band_balance_analyze.py` + `embedded_speck.py` loaders/passes verbatim), frozen `expected_pools.json`, `handle_representation_report` in server (model on `handle_inventory`), `/api/report/representation`, wire the existing "Open Report" button.
- **Gate:** on canonical savepoint run: inner-equator(0–12) jungle ≥55% + arid ≤7% → GREEN; `ice_spikes` appears ONLY in `accent_over_rep` (~9% vs 6% cap); a coherent rare accent (`bamboo_jungle`, `largest_share≥0.5`) = GREEN not under-rep; confetti section only lists `marooned_pct≥0.15`; subpolar 2% member (pool n=12) = GREEN.

### Phase 7 — Stretch tier (time-boxed)
- **Do:** Per §5 priority, build the shared two-run loader + delta renderer, then #1 regression-guard, #2 A/B compare, #3 provenance chip. Stop when the night runs out; everything below #3 is optional.
- **Gate:** each stretch item ships behind its own toggle, never destabilizes the ported baseline.

### Phase 8 — Validate + savepoint (S)
- **Do:** Full compile + smoke (§6 of dep-map). **Worldgen integrity proof:** run `runBiomePreview` same seed before/after; diff `world_biome_inventory.json` → must be identical. Write binder evidence entry. Commit ONLY tooling files.
- **Gate:** worldgen inventory diff EMPTY; binder + Notion logged; commit scoped to `tools/atlas/*`, `build.gradle`, `WorldMapPreviewHeadlessRunner.java`, docs. No worldgen file in the diff.

---

## 4. EMBEDDED SPECS

### 4.1 PORT DEPENDENCY MAP

**Copy verbatim (no merge):**
```
src/main/java/com/example/globe/dev/WorldMapPreviewHeadlessRunner.java  [NEW, ~500 LOC]
tools/atlas/viewer/index.html                                          [REPLACE ENTIRE → 7340]
tools/atlas/check_world_map_run.py                                     [NEW, ~9.8 KB]
build.gradle:
   worldMapPreview               run config  (~lines 55–62 isolated)
   prepareWorldMapPreviewRunDir  task        (~224–247)
   worldMapPresetForSize()       closure     (~249–266) maps size→globe:globe_* level type
   syncWorldMapPreviewMods       task        (~279–288)
   runWorldMapPreview            task config (~404–499) parses latdev opts → JVM prop -Dlatdev.worldMap=...
```

**Merge with surgical insertion:**
```
tools/atlas/viewer_api_server.py
   WORLD_MAP_* constants/locks/global state (~22–23, 36–49)
   world_map_* helpers + list_world_map_jobs (~575–891)
   _dispatch_world_map_api (~1254–1302)
   world_map_* handlers     (~1436–1512)
   add _dispatch_world_map_api calls into do_GET/do_POST
   NEW route handle_representation_report + /api/report/representation (Phase 6)
tools/atlas/atlas_runner.py — SIZE_RADIUS mapping + PROVENANCE_IGNORED_PREFIXES
tools/atlas/desktop-app/main.js — exportRunDataFromDesktop() (~507–600) + IPC handler (~929–933)
   + helpers buildExportFolderName/ensureUniqueExportDir/copyRecursive/isPathInside
```

**Keep canonical — NO PORT (worldgen, has isolated bugs/conflicts):**
```
src/main/java/com/example/globe/world/LatitudeBiomes.java   (already has setWorldSeed/setActiveRadiusBlocks)
src/main/java/com/example/globe/dev/BiomePreviewExporter.java  (isolated +767 LOC is atlas refinement; DISCARD)
src/main/java/com/example/globe/dev/BiomeBandPolicy.java
all ChunkGenerator*/biome-placement/palette/tag mutations
```

**World-map backend data flow:** viewer World Map form → `POST /api/world-map/jobs` (start) → `viewer_api_server` shells `runWorldMapPreview` with latdev opts → gradle sets `-Dlatdev.worldMap=enabled=true;seed=X;size=Y;tilesize=Z;job=J;maxtiles=M` → `WorldMapPreviewHeadlessRunner.onServerStarted()` parses Config, calls `LatitudeBiomes.setWorldSeed/setActiveRadiusBlocks`, runs tile loop, polls `control.json` (pause/resume/cancel), writes `tiles/z0/x_*_z_*.png`, `job_state.json` (chunks_done/total, percent, phase), `world_map_manifest.json` (seed, radius, tiles[]). Viewer polls `GET /api/world-map/jobs/{job}` + serves tiles via `/api/world-map/jobs/{job}/tiles/{z}/{file}`.
Output root: `run-headless-worldmap/latdev/world-map-runs/{job}/{job_state.json,world_map_manifest.json,tiles/z0/,scratch-world/}`.

**Risk note:** `WorldMapPreviewHeadlessRunner` only *invokes* `LatitudeBiomes` with world-map radius/seed — no method changes. Confirm in Phase 1 the runner self-registers its lifecycle hook (mirror `BiomePreviewHeadlessRunner`); if not, add the registration only.

### 4.2 MOD/NAMESPACE FILTER (client-only; `index.html` only)

`namespace = biome_id.slice(0, indexOf(':'))` (`'unknown'` if none). Source: `biome_palette.json` → already in `state.biomeByIndex[0..255]`/`state.biomeByIdMap`.
- **Helpers** near `biomeByIndex`: `namespaceOf`, `nsLabel`, `NAMESPACE_META = {minecraft:{label:'Vanilla',accent:'#9AA7B2'}, biomesoplenty:{label:'BoP',accent:'#6FBF73'}, terralith:{label:'Terralith',accent:'#C98BDB'}, promenade:{label:'Promenade',accent:'#E0A050'}}`.
- **State:** `namespaceFilter: null` (Set or null=all), `namespaceIsolate: false`. Lazily build `state.nsIdByIndex = Uint8Array(256)` + `state.nsList` (stable order: minecraft, biomesoplenty, terralith, promenade, then alpha) after palette load.
- **UI:** `#ns-chip-bar` in Biome Presence panel (`#card-biomes`), between title row and `#biome-list-body`, above search. Toggle chips per present namespace (accent dot + label + live count). "Isolate" segmented control `[Show others dimmed | Hide others]` + `All`/`None`. `All` clears filter.
- **List:** in `renderBiomeList` after `mergeBiomePresenceRows`, group rows by namespace (collapsible sub-header w/ aggregate %). Filtered-out rows `display:none` (reuse existing `biomeSearch` show/hide pattern). Namespace filter AND search compose.
- **Map:** add namespace-isolate branch in `renderSelectedHighlights` (owns `#highlight-canvas`): per pixel `ns = nsIdByIndex[biomeIndices[p] & 0xFF]`; if in selected set → `alpha=0` (true-color shows through); else neutral veil (`128,128,128, ~150`). Fold ns state into `renderKey` for cache invalidation. Biome magenta outline paints after veil.
- **Edges:** `minecraft:`→"Vanilla"; `unknown`→greyed chip; multi-select = union; keep `b.pct` as whole-world share + sub-header aggregate; optional "Dim water" toggle (default OFF, keeps coastlines as reference); empty filter → placeholder; absent namespace → no chip.

### 4.3 DISTINCT COLOR PALETTE (client-only + offline parity)

Add 3rd option to existing `#sel-color-view` (`identity`/`legacy` → + `distinct`). Toggle = set `state.colorViewMode` + repaint `paintCanvasFromBiomeIndices()`, no refetch. Color pick:
```js
const c = state.colorViewMode==='distinct' ? b.distinctColor
        : (state.colorViewMode==='legacy' && b.legacyColor) ? b.legacyColor
        : b.color;
```
- **`buildDistinctPalette(biomeIds)`** called after `state.biomes` built, before `buildColorLookup()`. Key on **stable id rank** via `stableSortBiomeIds` (NOT volatile index — this is the `distinct_render.py` defect being fixed). Set `b.distinctColor` in `normalizeBiomePaletteForDisplay()` next to `legacyColor`.
- **Namespace hue arcs:** minecraft 20–70, biomesoplenty 90–160, terralith 200–260, promenade 280–340. Within family size `m`, local rank `k`: `h=(arcStart+(arcEnd-arcStart)*((k*0.618033)%1))/360`, `s=0.62+0.30*((k*2)%3)/2`, `v=0.70+0.25*((k*5)%4)/3`.
- **Collision sweep:** convert all to OKLab; for any pair ΔE<12, nudge later-ranked member's L by ±8 deterministically until min pairwise ΔE ≥ 12.
- **Reserved muted (override arc, excluded from arc count):** `*ocean*`→`#1C2A4A`; `river`/`frozen_river`→`#28466E`; `*beach*|*shore*|*coast*|*dune*`→`#C9B98A`.
- **Legend:** Biome Presence swatches follow `state.colorViewMode` (presence list IS the legend in distinct mode; "grouped by mod" subheader).
- **Cross-feature:** colors assigned over FULL inventory (filter never recolors, only dims). Confetti overlay draws on top of distinct base (natural pairing). Reports color-agnostic.
- **Offline parity:** refactor `distinct_render.py` `distinct_color(i,n)` to the same id-rank + arc + ΔE scheme so offline PNG == live viewer (QA single source of truth).

### 4.4 OVER/UNDER-REPRESENTATION REPORT

New `tools/atlas/representation_report.py` (peer of `band_balance_analyze.py`/`embedded_speck.py`). **Reuse verbatim** their `parse_geom`/`load`/`lat_of_row`, the 4-connected component pass (`band_balance_analyze.analyze`), and the single-enclosure speck pass (`embedded_speck.main`). Geometry MUST stay identical: `lat=|(-radius+iz*step)|/radius*90`, same BANDS edge list (Art VI).

- **Inputs:** `biome_ids.png`, `biome_palette.json`, `world_biome_inventory.json`, `biomes.txt`. Expectation tables static (below). Tag pool snapshot: frozen `tools/atlas/expected_pools.json` (one-time `--rebuild-pools` from `src/main/resources/data/globe/tags/worldgen/biome/lat_*.json`; format `band→{primary,secondary,accent}` of ids). Viewer must NOT depend on the worldgen tree at runtime.
- **EXPECTED_FAMILY[band][family]=(lo,hi)** shares of band LAND pixels (families = `band_balance_analyze.family()`):

| band | jungle | savanna | desert | badlands | taiga | leafy/other |
|---|---|---|---|---|---|---|
| tropical | 0.45–0.75 | 0.05–0.20 | 0.00–0.08 | 0.00–0.05 | 0.00 | 0.10–0.35 |
| subtropical | 0.00–0.10 | 0.10–0.30 | 0.15–0.40 | 0.05–0.20 | 0.00 | 0.10–0.40 |
| temperate | 0.00 | 0.00–0.08 | 0.00–0.05 | 0.00–0.05 | 0.05–0.20 | 0.45–0.85 |
| subpolar | 0.00 | 0.00 | 0.00 | 0.00 | 0.40–0.75 | 0.20–0.50 |
| polar | 0.00 | 0.00 | 0.00 | 0.00 | 0.05–0.25 | 0.70–0.95 |

Keep tropical inner(0–12)/outer(12–23.5) split: inner jungle ≥0.55, inner arid ≤0.07 (v1.4 gate).
- **Per-band BIOME-POOL expectation (tier→ref weight):** `primary` ref≈`0.50/n_primary` floor 8%; `secondary` ref≈`0.30/n_secondary` floor 3%; `accent` ref≈`0.20/n_accent` floor 0% + **cap ≈6%**. Pool sizes: equator primary=1(`jungle`)/accent=1(`bamboo_jungle`); arid primary=2(`desert`,`badlands`); temperate primary=7; subpolar primary=12; polar primary=1/accent=1(`ice_spikes`). Per-biome expectation = `1/n` of tier, NEVER a flat global threshold (a 2% member of a 12-pool is NORMAL).
- **Metrics per biome/family:** `actual_share` (inventory + per-band pass); `expected_lo/hi` (family table A; biome `[ref*0.4, ref*2.0]` clamped to tier floor/cap); `delta` (signed dist to nearest edge, normalized `/max(hi,eps)`); coherence reads `largest_share`, `tiny_share` (from band_balance), `marooned_pct` (embedded_speck TRUE-confetti).
- **Verdict (share-delta and coherence are INDEPENDENT axes):**
```
inside[lo,hi] → GREEN
actual<lo: if accent && largest_share>=0.5 → GREEN ("rare accent, coherent"); else AMBER/RED scaled by |delta|
actual>hi: if accent → RED ("accent over-rep", ice_spikes); else AMBER/RED
orthogonal: if marooned_pct>=0.15 or (tiny_share>=0.4 && largest_share<0.25) → RED-CONFETTI
```
- **Sections (JSON `payload.sections`):** `per_band_composition` (families + inner_equator gauge), `over_represented` (actual>hi sorted by norm delta), `under_represented` (actual<lo incl. MISSING; accent-missing=AMBER), `confetti_offenders` (ONLY `marooned_pct≥0.15`; canonical pairs jungle_in_arid/taiga_in_plains/old_growth_pine_taiga_speck/sparse_jungle_in_arid; boundary fragmentation excluded), `accent_over_rep` (separate; pre-seed `ice_spikes ~9%` vs 6% cap with cosmetic note).
- **Server:** `handle_representation_report(run, layer)` modeled on `handle_inventory`; route `/api/report/representation?run=&layer=`.
- **Viewer:** wire existing "Open Report" button → fetch → 5 band cards (family stacked bar + ghost expected range + inner-equator gauge) + 4 collapsible tables (over/under+missing/confetti/accent-over-rep) w/ verdict pills + hover showing `largest_share`/`marooned_pct`. Export: "Download report.json" + "Copy markdown" (reuse inventory-export plumbing).

---

## 5. STRETCH-IDEA TIER LIST

**DO-FIRST (high value, reuses analyzers):**
1. **Regression-guard mode (M)** — run vs saved baseline, flag deltas. `band_balance_analyze.py` already emits `DELTAS (baseline→candidate)` (~line 245). Endpoint `/api/regression?run=&baseline=`; delta table w/ red/green thresholds (Art X monoculture, equator arid, confetti tiny-share); pin baseline = savepoint inventory.
2. **A/B run compare (M)** — twin synced map canvases + shared band-family ratio diff; reuse two-dir mode. Shares ~70% plumbing with #1 — **build the shared two-run loader + delta renderer once, fork presentation.**
3. **Provenance/branch chip (S)** — write `run_provenance.json` at export (git branch, short SHA, worldgen-config hash, timestamp); persistent header chip, red if commit ≠ canonical HEAD. *Directly prevents the worktree-drift bug that triggered this whole effort — do early.*
4. **Confetti-vs-rare disambiguation (S/M)** — join `embedded_speck.py` + inventory shares; classify {coherent-rare, true-confetti, healthy}; two-axis dot (share vs largest-component-fraction). Stops the parked "sparse +165 preserve-cluster" false alarm.

**IF-TIME:** 5. Band-violation heatmap overlay (M) · 6. Biome search/jump-to + `?run=&biome=` deep-link (S) · 7. Per-band composition mini-charts (S, near-free) · 8. Palette legend panel (S, mostly subsumed by §4.3 legend).

**SKIP-FOR-NOW:** 9. "Climate realism score" (needs governance-blessed rubric — don't invent at 3am) · 10. Full shareable-link infra (only the `?run=&biome=` slice of #6 is worth it) · 11. Longitudinal-variety surfacing (no current complaint).

**Recommended night order:** shared two-run loader → #1 → #2 → #3 (early, anti-drift) → #7 (free) → #4 → then #5/#6/#8 as time allows. #1+#2+#3 alone deliver the thesis: *prove worldgen didn't regress, branch-safely.*

---

## 6. RISKS / WATCH-ITEMS

- **LatitudeBiomes.java conflict** — the #1 reason NOT to git-merge. Canonical already has the needed methods; isolated only invokes them. Keep canonical's file; confirm git-diff EMPTY at every gate.
- **World-map backend deps** — `runWorldMapPreview` is a real headless server run (Loom run config + scratch world). First smoke may be slow (~30s+). Verify `prepareWorldMapPreviewRunDir` writes `eula.txt`/`server.properties` and `syncWorldMapPreviewMods` copies the provider mods, or generation stalls. Confirm the runner self-registers its lifecycle hook.
- **7k-line `index.html`** — full-file replace is correct but verify: Plotly/Tauri script imports, `/api/...` paths, `../desktop-app/preload.js`, local CSS/theme vars survive. Features §4.2/§4.3 then edit this single file heavily — re-grep anchors, never trust isolated line numbers.
- **Palette stability** — distinct palette MUST key on `stableSortBiomeIds` rank with namespace buckets, not index; otherwise colors shuffle between runs (the `distinct_render.py` defect). Enforce ΔE≥12 + deterministic (no `Math.random`). Keep offline `.py` in lockstep.
- **Provider-stack assumptions** — tables/arcs assume minecraft+biomesoplenty+terralith+promenade (~78 biomes). Guard for vanilla-only / missing namespace (no crash, no chip). `expected_pools.json` must be a frozen snapshot so the viewer never reaches into the worldgen tag tree at runtime.
- **Scope leakage at savepoint** — dirty tree has `run-headless/world/*` deletions; commit ONLY tooling files. Prove worldgen integrity via before/after `world_biome_inventory.json` diff (must be byte-identical for same seed). Log as a TOOLING bucket, separate from any worldgen savepoint.

---

## 7. COPY-PASTE KICKOFF PROMPT

```
You are implementing the Atlas Viewer Modernization tooling bucket. Read the full brief first:
/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/binder/atlas-viewer-modernization-brief-20260605.md

NON-NEGOTIABLES: Port atlas TOOLING only — never port worldgen. Do NOT git-merge the isolated
branch. Create branch feat/atlas-viewer-modernization off canonical feat/1.3.1-cohesive-horizons-26.1.2
@ 8c73beab. Operate under latitude-regression-fix-workflow; map-based proof; log to binder
(docs/binder/) + Notion as a TOOLING bucket SEPARATE from worldgen.

CANONICAL: /Users/joolmac/CascadeProjects/Latitude (Globe)
ISOLATED (source, viewer only): /Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc @ ca81036f
Keep canonical LatitudeBiomes.java/BiomePreviewExporter.java/BiomeBandPolicy.java + all worldgen.

Execute the phase plan in order, honoring each gate:
0 Preflight+dep-map → 1 Port Java/Gradle → 2 Port Python backend → 3 Port viewer frontend →
4 Mod/namespace filter → 5 Distinct palette → 6 Over/under-rep report → 7 Stretch (regression-guard,
A/B compare, provenance chip) → 8 Validate+savepoint.

PROOF OF WORLDGEN INTEGRITY before savepoint: run runBiomePreview same seed before/after; diff
world_biome_inventory.json — must be byte-identical. Commit ONLY tools/atlas/*, build.gradle,
WorldMapPreviewHeadlessRunner.java, docs. Re-grep all isolated line numbers on arrival (they drift).

Start with Phase 0: create the branch, confirm scope, re-grep the isolated anchors in section 4.1.
```
