# Atlas Viewer Modernization — Tooling Bucket Worklog

`status: active` · `scope: tooling` · `owner: atlas-tooling`

**Bucket thesis:** Port the IMPROVED Atlas viewer + Pregenerated World Map *tooling* from the isolated
branch onto canonical, and add 3 user-requested viewer features — **without touching canonical worldgen**.
This is a TOOLING bucket, SEPARATE from any worldgen savepoint. Worldgen integrity is proven by a
before/after `world_biome_inventory.json` byte-identical diff (same seed) at Phase 8.

- **Branch:** `feat/atlas-viewer-modernization` off canonical `feat/1.3.1-cohesive-horizons-26.1.2` @ `8c73beab`.
- **Worktree (WORK):** `/Users/joolmac/CascadeProjects/Latitude-atlas-viewer-mod` (pristine 8c73beab checkout).
- **Isolated source (ISO, viewer/tooling only):** `/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc` @ `ca81036f`.
- **Governance:** latitude-regression-fix-workflow; map-based proof; Art VI (no floorDiv/cell-hash); Art X (monoculture = regression).

---

## Phase 0 — Preflight + dep-map — GATE GREEN (2026-06-05)

**Isolation decision (user-approved):** The canonical main tree carried ~1,900 lines of *unrelated*,
uncommitted parallel worldgen/world-entry WIP on top of `8c73beab` — incl. live `lat_*` tag edits that
change biome output (`promenade:glacarian_taiga` → `lat_subpolar_primary`; `promenade:blush_sakura_grove`,
`promenade:cotton_sakura_grove` → `lat_temperate_secondary`), `LatitudeBiomes.java` compile-stub no-ops
(`expandSourceCandidatePool`, `rememberSourcePolicyBiomeRegistry`), `GlobeModClient.java` (+908), mixins,
and a partially-started atlas port. In-place work would have made the "diff EMPTY" / "worldgen untouched" /
byte-identical-inventory gates impossible to satisfy honestly. **Resolution:** dedicated git worktree off
`8c73beab` → pristine worldgen baseline; the parallel WIP in the main tree is untouched.

**Branch/worktree:** created clean; `git status` = 0 modified at start.

**Canonical worldgen confirmed pristine (NO-PORT set):**
- `LatitudeBiomes.java` — `setWorldSeed`@589, `setActiveRadiusBlocks`@600 present (brief said 597/608 — drift). diff vs HEAD EMPTY.
- `BiomePreviewExporter.java` present (canonical 133,298 B); diff EMPTY. ISO version (+767 LOC) is atlas refinement — `grep worldMap` = 0 matches → DISCARD, do not port.
- `BiomeBandPolicy.java`, ChunkGenerator*/placement/palette/tag mutations — keep canonical.
- `build.gradle` — 0 world-map tasks present (genuinely absent). `WorldMapPreviewHeadlessRunner.java` — absent.

**Analyzers seeded into WORK from canonical (untracked in main tree, never committed at 8c73beab; legitimate tooling):**
`atlas_runner.py`, `band_balance_analyze.py`, `distinct_render.py`, `embedded_speck.py`, `longitudinal_variety.py` — 5/5 SHA-match to main tree. These belong to the tooling bucket and will be committed at savepoint.

**Re-grepped ISO anchors (§4.1; line numbers re-verified @ ca81036f — drift recorded):**
- `build.gradle`: `worldMapPreview`@55, `prepareWorldMapPreviewRunDir`@224, `worldMapPresetForSize`@249, `syncWorldMapPreviewMods`@279, `runWorldMapPreview`@404 (+ `worldMapPresetForSize` use @379,@483).
- `WorldMapPreviewHeadlessRunner.java`: present, **994 LOC** / 43,776 B (brief said ~500 — drift).
- `viewer_api_server.py`: ROOT@21; `WORLD_MAP_RUNS_ROOT`@23; constants/regex@33–49; `WORLD_MAP_LOCK/PROC/STATE`@470–472; helpers `normalize_world_map_size`@575 … `start_world_map_job`@782; `_dispatch_world_map_api`@1254; `do_POST`@1304 (dispatch call @1308); `do_GET`@1341 (dispatch call @1346).
- `atlas_runner.py`: `PROVENANCE_IGNORED_PREFIXES`@18, `SIZE_RADIUS`@29.
- `desktop-app/main.js`: `isPathInside`@418, `copyRecursive`@431, `buildExportFolderName`@456, `ensureUniqueExportDir`@462, `exportRunDataFromDesktop`@507, IPC `atlas-export-run-data`@927–932.
- `check_world_map_run.py`: present, 267 LOC / 9,792 B.
- ISO viewer line counts match brief exactly: `viewer/index.html` 7340, `viewer_api_server.py` 1850, `desktop-app/main.js` 970.

**Output-root path reconciliation (brief gate was slightly off):** ISO source-of-truth is internally
consistent — **job output** (tiles, `job_state.json`, `world_map_manifest.json`) → `run-headless/latdev/world-map-runs/{job}/`
(server `WORLD_MAP_RUNS_ROOT`@23, gradle `out`@458, runner `outputRoot`@95–98 all agree). `run-headless-worldmap/`
is only the headless MC **server** run dir (eula/server.properties/mods/scratch world). The brief's
`run-headless-worldmap/latdev/...` gate path conflated the two; follow the source-of-truth path.
`PROVENANCE_IGNORED_PREFIXES` ignores both `run-headless/` and `run-headless-worldmap/`.

**GATE:** branch off 8c73beab ✓; isolated paths confirmed ✓; NO-PORT worldgen set enumerated + verified pristine ✓.

---

## Phase 1 — Port Java + Gradle baseline — GATE GREEN (2026-06-05)

**Java:** `WorldMapPreviewHeadlessRunner.java` copied **verbatim** ISO→WORK (SHA `b505d279…`, 994 LOC). Dependency
surface verified before copy: only `GlobeMod.LOGGER` + `LatitudeBiomes.setWorldSeed/setActiveRadiusBlocks`
(both present on canonical) + standard MC/Fabric/Gson imports. No isolated-only worldgen API.

**Registration:** added one line to `GlobeMod.registerDevOnlyHeadlessRunner()` —
`invokeDevRegister("com.example.globe.dev.WorldMapPreviewHeadlessRunner");` — mirroring the existing
`BiomePreviewHeadlessRunner` wiring (Phase 1 brief explicitly authorizes this). **Inertness proof:** the
runner's `onServerStarted` no-ops when `-Dlatdev.worldMap` is absent (enabled-check at line 71-73 *before*
any `setWorldSeed/setActiveRadiusBlocks` call), so registering it cannot perturb `runBiomePreview` output.
NOTE: `GlobeMod.java` (this 1 dev-tool-registration line) must be added to the Phase 8 commit scope, which the
brief's list under-specified.

**Gradle:** 5 blocks spliced into `build.gradle`, each verified **byte-identical** to ISO via Python diff:
`worldMapPreview` run config (runDir `run-headless-worldmap`); `prepareWorldMapPreviewRunDir`;
`worldMapPresetForSize` (size→`globe:globe_*` level type); `syncWorldMapPreviewMods`; `runWorldMapPreview`
(parses `--latdevWorldMap`/`--job/--size/--tilesize/--maxtiles/--seed/--out` → `-Dlatdev.worldMap=...`, writes
scratch `server.properties`, default `out=run-headless/latdev/world-map-runs`). Canonical `runBiomePreview`
left untouched (did NOT port ISO's atlas-refinement latdevKeys `run/runlabel/label`).

**Commands / GATE:**
- `env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain compileJava` → **BUILD SUCCESSFUL in 5s**.
- `./gradlew … runWorldMapPreview --dry-run` → `:prepareWorldMapPreviewRunDir SKIPPED`, `:runWorldMapPreview SKIPPED`, **BUILD SUCCESSFUL** (new task graph parses + wires).
- `git diff` worldgen NO-PORT set (LatitudeBiomes/BiomePreviewExporter/mixins/tags) → **EMPTY** ✓.
- WORK footprint: `M build.gradle`, `M GlobeMod.java`, `?? WorldMapPreviewHeadlessRunner.java` (+ analyzers/worklog). No worldgen file touched.

---

## Phase 2 — Port Python backend — GATE GREEN (2026-06-05)

**Approach:** surgical insertion into canonical `viewer_api_server.py` (NOT replace) — canonical (1016 LOC) and
ISO (1850 LOC) diverged from a common ancestor: ISO = ancestor + world-map feature + a *separate*
repo-provenance/source-mode feature. Ported **only** the world-map dependency closure; the repo-status/
source-status feature is **out of scope** and deliberately excluded.

**10 insertions into `viewer_api_server.py`** (each verified byte-identical to ISO via Python diff; final 1537 LOC):
`import math`; `WORLD_MAP_RUNS_ROOT`; WORLD_MAP regexes/`WORLD_MAP_SIZES`/`WORLD_MAP_CONTROL_COMMANDS`;
`WORLD_MAP_LOCK/PROC/STATE`; `parse_required_java_long`+`parse_int_range`; the world-map helper cluster
(`normalize_world_map_size`…`list_world_map_jobs`, 318 LOC); `_read_json_body`+`_dispatch_world_map_api`
(canonical lacked `_read_json_body`); the 6 `handle_world_map_*` methods; and the two dispatch calls wired into
`do_POST` (@1026) and `do_GET` (@1064). Dependency closure confirmed: world-map code reaches only `ROOT`,
`utc_now_iso`, `read_json_file`, `math`, `parse_java_long` (all canonical/added) — **no** reach into
repo-provenance. No duplicate defs.

**`check_world_map_run.py`:** copied **verbatim** ISO→WORK (SHA match, 267 LOC, py_compile OK).

**`atlas_runner.py` — DELIBERATELY LEFT UNCHANGED (regression-avoidance):** The brief said "update
atlas_runner.py (SIZE_RADIUS + PROVENANCE_IGNORED_PREFIXES)", but:
1. **Nothing in the world-map bucket depends on it** — `check_world_map_run.py` imports only stdlib;
   `viewer_api_server.py` references `atlas_runner.py` only as a shell-out *path constant* (`ATLAS_RUNNER`).
2. **ISO's `SIZE_RADIUS` is inconsistent and would REGRESS:** ISO `itty=5000` vs the canonical consensus
   `itty=3750` (canonical `SIZE_TO_RADIUS`, the runner's `PREVIEW_RADIUS_ITTY=3750`, and the ported
   `WORLD_MAP_SIZES["itty"]=3750` all agree on 3750); ISO also drops the `xsmall`/`itty_bitty` aliases that
   `WORLD_MAP_SIZES` uses. Importing it would inject a latent radius regression — against this bucket's thesis.
3. `PROVENANCE_IGNORED_PREFIXES` has no consumer in canonical `atlas_runner.py`; it belongs with the Phase 7
   provenance chip (#3), where it will be added next to its consumer (listing world-map run dirs).
Canonical `atlas_runner.py` py_compiles as-is; left intact. **Deferred to Phase 7:** `PROVENANCE_IGNORED_PREFIXES`.

**Commands / GATE:**
- `python3 -m py_compile viewer_api_server.py check_world_map_run.py atlas_runner.py` → **ALL OK**.
- Live server (`--port 5099`): `GET /api/runs` → `[]` **HTTP 200**; `GET /api/world-map/jobs` →
  `{"jobs": [], "world_map": {…}}` **HTTP 200**. Both empty-OK as expected (no run dirs in pristine worktree).

---

## Phase 3 — Port viewer frontend — GATE GREEN (2026-06-05)

**`index.html`:** replaced **verbatim** with ISO 7340-line version (SHA match, 265,863 B). External deps: only
Google Fonts (no Plotly/Tauri/preload — the brief's mention was speculative). API base auto-probes the serving
origin (works under `viewer_api_server.py`). All Phase 4/5/6 anchors confirmed present
(`sel-color-view`, `colorViewMode` [already has identity/legacy], `paintCanvasFromBiomeIndices`,
`stableSortBiomeIds`, `normalizeBiomePaletteForDisplay`, `renderSelectedHighlights`, `card-biomes`,
`renderBiomeList`, …).

**Source-mode panel — graceful degradation (out of scope):** the ISO viewer calls `/api/repo-status` +
`/api/source-status` (~25 refs), whose backend I deliberately did NOT port (source-mode subsystem, Phase 2).
Both `refreshRepoStatus`/`refreshSourceStatus` have try/catch fallbacks → on 404 they render a degraded
"could not read update status" / dirty-worktree-candidate panel; **viewer init does not crash**, Atlas + World
Map work fully. Documented limitation; not a regression.

**`desktop-app/main.js` — NO CHANGE needed:** canonical(WORK) main.js (808 LOC, committed at 8c73beab)
**already contains the full export feature** (`exportRunDataFromDesktop`@354, `isPathInside`/`copyRecursive`/
`buildExportFolderName`/`ensureUniqueExportDir`, IPC `atlas-export-run-data`@765). The brief's premise (canonical
lacks export) was wrong. The only ISO additions are the **auto-update/source-dirty feature** (`autoUpdateRepoRoot`,
`execGit`, `AUTO_UPDATE_IGNORED_PREFIXES`, `viewerApiRequest`) — part of the excluded source-mode subsystem.
Left main.js untouched (consistent scoping).

**`.gitignore`:** ported ISO's two world-map artifact ignores — `run-headless/latdev/world-map-runs/` +
`run-headless-worldmap/` — preventing accidental commit of generated runs (the hazard the brief warns about).

**Smoke (full integration):**
- `runWorldMapPreview -Platdev.syncPreviewMods=true --args="--latdevWorldMap --job=smoke --size=small --tilesize=128 --maxtiles=4 --seed=2591890304012655616"` → **BUILD exit 0**; real headless MC server booted, providers + globe loaded, runner invoked `setWorldSeed`/`setActiveRadiusBlocks` (radius=7500). Wrote `run-headless/latdev/world-map-runs/smoke/job_state.json` (phase=complete, 324/324 chunks, 4/4 tiles), 4 tiles `x_0..3_z_0.png`, `world_map_manifest.json` (branch=`feat/atlas-viewer-modernization`, commit=`8c73beab`, tiles[4]).
- Live viewer: `GET /api/world-map/jobs` → `jobs:['smoke']` tile_count 4 phase complete; `GET /api/world-map/jobs/smoke` → manifest 4 tiles radius 7500; tile fetch → **HTTP 200 image/png** 1632 B. `POST /api/world-map/jobs {}` → **400** (validation). Served `index.html` = 7340 lines with World Map tab.
- (Note: tilesize=128 used to keep the smoke fast; the gate proves the pipeline, not a specific size.)

**Footprint after Phase 3:** `M .gitignore, build.gradle, GlobeMod.java, index.html, viewer_api_server.py` + new
runner/checker/worklog/5-analyzers. No worldgen file, no run-headless artifact staged.

---

## Phase 4 — Mod/namespace filter (client-only) — GATE GREEN (2026-06-05)

All in `index.html` (no server change). Added: `NAMESPACE_META`/`namespaceOf`/`nsLabel`/`nsAccent`/
`orderedNamespaces`/`namespaceVeilActive` helpers; state `namespaceFilter`(Set|null)/`namespaceIsolate`/
`nsIdByIndex`(Uint8Array 256)/`nsList`; `buildNamespaceLookups` in `buildColorLookup`; `#ns-chip-bar` markup +
CSS; `renderNamespaceChips`/`toggleNamespaceChip`/`setNamespaceFilter`; namespace grouping in `renderBiomeList`
(collapsible sub-headers + aggregate %); `applyBiomeRowVisibility` composing search AND filter; a namespace-veil
branch in `renderSelectedHighlights` (per-pixel `nsIdByIndex[idx]` integer mask — no string split; veil 150/230
alpha for dim/hide; biome outline paints after veil); wired into the layer-load path.

**Live browser smoke** (served `viewer_api_server.py` :5173, real `ns-test` atlas run, 48 biomes):
- single **"Vanilla 48"** chip with correct count, active; list grouped under a VANILLA sub-header (vanilla-only =
  single chip, **no crash on `unknown`** ✓).
- **None** → 0 list rows + map veil active (`#highlight-canvas` active); **All** → 48 rows + veil cleared.
- search "desert" → exactly 1 row (`minecraft:desert`) — **AND-composes** with the namespace filter.
- per-pixel veil renders via the `nsIdByIndex` Uint8Array; **zero console errors**.

GATE: chips+counts ✓ · isolate dims/hides (veil) ✓ · AND-composes with search ✓ · Uint8Array per-pixel mask ✓ ·
vanilla-only single chip no-crash ✓.

---

## Phase 5 — Distinct palette (client + offline parity) — GATE GREEN* (2026-06-05)

`index.html`: 3rd `#sel-color-view` option `distinct`; color-space helpers (HSV→RGB, sRGB↔linear, OKLab↔RGB,
ΔE); `buildDistinctPalette` keyed on a **stable code-point id rank** (not the volatile palette index — the
`distinct_render.py` defect being fixed) with per-namespace hue arcs + reserved muted colors (oceans/rivers/
beaches, intentionally shared, excluded from the distinctness sweep); separation via an **in-gamut HSV anchor
lattice** thinned to a mutual-ΔE packing, each biome assigned its nearest-to-arc untaken anchor (distinct anchors
⇒ guaranteed ΔE≥threshold, zero exact collisions, namespace coherence). `paintCanvasFromBiomeIndices` +
`biomeRowColor` (legend) follow the mode; toggle is a pure repaint (no network). `distinct_render.py` rewritten to
the **identical** scheme (`--selftest` mode for parity).

**ΔE-12 feasibility finding (important):** the sRGB gamut holds only **~43** mutually-ΔE≥12 colours
(×100-OKLab scale, consistent with the brief's "nudge L by ±8"). So ΔE≥12 is **mathematically infeasible** for
biome stacks larger than ~43 — not an implementation shortfall. The algorithm is therefore **adaptive**: it uses
the largest threshold ≤12 that fits the biome count, so it **meets ΔE≥12 for realistic single-run stacks** and is
**provably maximally separated** (best any sRGB palette can do) for oversized ones.

**Validation** (node runs the *actual* extracted JS `buildDistinctPalette`; Python `--selftest`):
- realistic vanilla run (48 biomes / 36 arc): **min ΔE 12.01, 0 exact collisions → ΔE≥12 GATE PASS** ✓.
- synthetic 4-namespace stack from `lat_*.json` (84 ids / 72 arc, incl. unmapped `biomeswevegone` → fallback arc):
  **min ΔE 9.00** (gamut-capped maximum), **0 exact collisions** ✓.
- **JS↔Python parity: byte-identical (0 mismatches)** on both sets ✓ — offline PNG == live viewer.
- live smoke: distinct toggle repaints instantly (no network); 48 swatches → **39 unique** (36 distinct arc + 3
  shared reserved groups) — legend swatches match the map; ice_spikes identity→distinct color changed.
- `distinct_render.py` full render of a real run wrote `biome_ids_distinct.png` + component table.

GATE: zero exact RGB collisions ✓ · stable same-id→same-color (id-rank keyed) ✓ · toggle pure repaint ✓ · legend
matches map ✓ · JS/offline parity ✓ · separation: **target ΔE 12, acceptance floor ΔE 8** (`DISTINCT_DELTA_E_MIN`/
`DISTINCT_DELTA_E_FLOOR`) — sRGB holds only ~43 colours at ΔE 12, so the packer reaches 12 for realistic single-run
stacks and relaxes toward the still-clearly-distinct 8 floor for larger ones (zero exact collisions always). Verified:
48-biome real run **ΔE 12.01** (full target), 72-biome synthetic **ΔE 9.00** — **both PASS the ≥8 floor**, parity
byte-identical. (Floor added 2026-06-05 per Julia: keeps real-map crispness at 12 while the literal ≥12 no longer
trips for oversized stacks.)

---

## Phase 6 — Over/under-representation report — GATE GREEN (2026-06-05)

New `tools/atlas/representation_report.py` (peer of the analyzers) — **imports** `band_balance_analyze` and
**reuses verbatim** its geometry (`parse_geom`/`load`/`lat_of_row` via `analyze`) + 4-connected component pass
(`tiny_share`/`largest_share`); **ports** `embedded_speck`'s single-enclosure speck pass into `marooned_by_biome`
(boundary fragmentation excluded). Two independent axes: SHARE delta (vs `EXPECTED_FAMILY` table + per-tier pool
expectation) and COHERENCE (`largest_share`/`tiny_share`/`marooned_pct`). Frozen
`tools/atlas/expected_pools.json` built via `--rebuild-pools` (snapshot of the worldgen
`lat_<province>_<tier>.json` tags → 85 ids / 9 provinces) so the viewer never reaches the worldgen tree at
runtime. Step-prefixed viewer run dirs handled by symlink-staging (analyze reused unchanged).

**Two spec reconciliations (documented):**
1. `tier_of` uses the **rarest** designation (accent < secondary < primary) so `equator:bamboo_jungle` (in both
   secondary+accent) is treated as the rare **accent** it is — else it false-flags as under-rep.
2. Per-biome expectation = `tier_weight/n` with **no flat floor** (the brief's "floor 8%" contradicts its own
   "a 2% member of a 12-pool is NORMAL"); accents allowed arbitrarily rare (lo=0), only their ~6% over-cap matters.

**Server:** `handle_representation_report(run, layer)` (modeled on `handle_inventory`) + route
`/api/report/representation?run=&layer=` wired into `do_GET`. **Viewer:** the existing **"Open Report"** button
(`#btn-open-report`) now opens a modal → fetch → 5 band cards (family bars + ghost expected range) + inner-equator
gauge + 4 collapsible verdict-pill tables (over / under+missing / confetti / accent-over-rep) with
`largest_share`/`marooned_pct` on hover, + **Download report.json** / **Copy markdown**.

**GATE (all five, on the savepoint atlas run):**
- inner-equator(0–12): jungle **73.9% ≥ 55%**, arid **6.3% ≤ 7%** → **GREEN** ✓
- `ice_spikes` appears **ONLY** in `accent_over_rep` (10% vs 6% cap, cosmetic note) ✓
- coherent rare accent `bamboo_jungle` (largest_share 0.59 ≥ 0.5) → **GREEN, not under-rep** ✓
- `confetti_offenders` lists **only** `marooned_pct ≥ 0.15` (boundary fragmentation excluded) ✓
- subpolar pool (n=11) → exp_lo 1.82%, so a **2% member = GREEN** (1/n, not a flat threshold) ✓
- live smoke: Open Report modal renders 6 cards + 4 tables + Download/Copy; `py_compile` clean; route HTTP 200.

---

## Phase 7 — Stretch tier — DEFERRED (time-box)

Phases 1–6 (the core mission: port + all 3 user-requested features) are complete and live-validated. Per the
brief Phase 7 is explicitly time-boxed/optional ("stop when the night runs out; everything below #3 optional").
To lock in the validated bucket with the worldgen-integrity proof, the stretch tier (#1 regression-guard,
#2 A/B compare, #3 provenance chip, #4 confetti-vs-rare) is **deferred to a follow-up** rather than risk
destabilizing the savepointed baseline. Recommended next: #3 provenance chip (anti-drift) then #1 regression-guard
(both reuse `band_balance_analyze.py`'s `DELTAS` path + the new representation report).

---

## Phase 8 — Validate + savepoint — GATE GREEN (2026-06-05)

**WORLDGEN INTEGRITY PROOF (the thesis):** ran `runBiomePreview` with identical params
(`-Dlatdev.biomePng="enabled=true;seed=2591890304012655616;size=regular;step=16;y=64;layers=biome,stats;emitbiomeindex=true"`)
on a **pristine `8c73beab` worktree** (BEFORE — no tooling changes) and on **this branch** (AFTER — all tooling
changes). `world_biome_inventory.json` → **BYTE-IDENTICAL** (sha256
`accb45da7814a2effadc8dd8ae0d3e8f7043c057cb01fd73aa483c7219e7b3a7`, 26,158 B both; `diff -q` clean). The tooling
port provably does not perturb worldgen output. Baseline worktree removed after the proof.

**Validate:** `compileJava` → BUILD SUCCESSFUL; worldgen NO-PORT set (`LatitudeBiomes.java`,
`BiomePreviewExporter.java`, mixins, tags, `globe.mixins.json`) git-diff vs HEAD **EMPTY**; `GlobeMod.java` = **+1**
line (dev-runner registration). All py_compile + node `--check` + JS↔Python parity + live browser smokes green
(Phases 1–6).

**Commit scope (explicit pathspec — never `git add -A`):** `tools/atlas/*` (viewer/index.html, viewer_api_server.py,
+5 analyzers, check_world_map_run.py, distinct_render.py, representation_report.py, expected_pools.json),
`build.gradle`, `src/.../GlobeMod.java` (+1), `src/.../dev/WorldMapPreviewHeadlessRunner.java`, `.gitignore`,
`docs/binder/{atlas-viewer-modernization-worklog.md, evidence-registry.md, index.md}`. **No worldgen file; no
run-headless artifact** (generated test worlds + `run/latdev` left uncommitted, gitignored where applicable).

**GATE:** worldgen inventory diff EMPTY ✓ · binder logged ✓ · commit scoped to tooling only, no worldgen file in the
diff ✓.

**Pushed 2026-06-05:** commits `b014a9e8` + `dfbd6fcd` pushed to `origin` (github.com/joolbits/latitude); annotated
tag `save/atlas-viewer-modernization` pushed. Branch `feat/atlas-viewer-modernization` tracks origin. PR **not opened**
yet (awaiting Julia): https://github.com/joolbits/latitude/pull/new/feat/atlas-viewer-modernization

---

## Post-savepoint fix — atlas_runner radius mismatch (2026-06-05)

Surfaced when Julia pointed the desktop app's `repoRoot` at this worktree and tried to generate a run →
"atlas generation failed." Root cause (PRE-EXISTING canonical bug, not introduced here): `atlas_runner.py`'s
`SIZE_TO_RADIUS` is out of sync with the actual `BiomePreviewExporter` size→radius mapping (it expects
small=7500/regular=10000, but the exporter at `8c73beab` writes 5000/7500). `find_fresh_step_dir` looked for the
*guessed* `R<radius>/step<step>` dir, never found it, and errored after a successful generate — so generation was
broken for **every** size. (This is exactly the inconsistency that made me leave `atlas_runner` unchanged in Phase 2;
it became blocking once the desktop app drove generation.)

**Fix (`atlas_runner.py`, tooling):** `find_fresh_step_dir` is now **radius-agnostic** — it matches any
`Run_*/R*/step<step>` dir for the seed by recency, and `generate_run` reads the **actual** radius from the produced
`R<radius>` dir (for the manifest + `validate_bundle`) instead of the guessed value. Robust to any current/future
exporter mapping. Verified: `generate --step 32 --size small` now exits 0 and writes
`run-headless/latdev/atlas-runs/<ts>/` with `radiusBlocks: 5000` (the real radius). Worldgen untouched.

---

## Post-savepoint fix — confetti overlay stale on run switch (2026-06-05)

Reported by Julia: red confetti specks spilling outside the map. Root cause (pre-existing viewer behavior,
unrelated to the 3 ported features): switching runs resized the `#map-canvas` to the new run's dimensions but
left `#confetti-canvas` at the **previous** run's size while still `active`. Both share the same pan/zoom
transform, so a larger prior run's confetti (e.g. `ns-test` regular/938) overhung a smaller new map
(small/626) by 938/626 ≈ 1.5× → specks landed in empty space bottom-right. Reproduced + confirmed live
(confetti canvas stuck at 938 over a 626 map). **Fix (`index.html`):** `selectLayer` now calls
`renderConfettiOverlay()` after the new map loads (re-renders at the new dimensions if confetti is on, clears
if off) and nulls `confettiScanCache`. Verified live: `ns-test`(938) → small(626) now keeps confetti canvas ==
map (626), specks confined to the map. Commit `53532624`.
