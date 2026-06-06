# Latitude Atlas WARM_DRY Balance Report - 2026-06-05

Status: partial, visual proof pending

> **Superseded for canonical 1.4 by [`tropical-rebalance-report-20260605.md`](./tropical-rebalance-report-20260605.md) (evidence `20260605-tropical-rebalance`).** That isolated-worktree WARM_DRY candidate (`20260604-202134`) was never merged to `feat/1.3.1-cohesive-horizons-26.1.2`. The canonical 1.4 rebalance instead fixed the root cause (latitude-independent moisture in `ProvinceAuthority.classifyWarm`) directly, then restored a believable desert+badlands mix and equatorial variety. This report is retained for history.

## Scope

This report consolidates the Atlas work from the isolated worktree:

`/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc`

The canonical Latitude checkout is used here only to record binder state. The behavior work and generated Atlas evidence remain in the isolated worktree.

Primary threads referenced:

- `019e9059-66f7-7920-8334-6c2d762e4a1a`: Atlas API startup smoke proving the dirty-source auto-update gate.
- `019e94d1-efac-78b1-8a28-c7fd2f3e1702`: WARM_DRY desert/badlands allocation and latest no-browser candidate proof.

## Goal

The product goal is to keep the Cohesive Horizons Atlas visually humid and coherent in tropical/subtropical regions while preserving legible arid WARM_DRY pockets. The current target is not "make the tropics dry again." The target is:

- humid tropical/subtropical bands remain cohesive;
- Old Growth Pine Taiga does not appear as stray sampled speckles or islands;
- WARM_DRY output contains visually meaningful desert and badlands, not almost-all badlands and not all desert;
- evidence remains isolated, reproducible, and separated from canonical savepoint state until Julia approves visual proof.

## Startup And Viewer Infrastructure Context

The Atlas API startup gate was proven before the WARM_DRY visual work continued. In thread `019e9059-66f7-7920-8334-6c2d762e4a1a`, the API server was launched only long enough to query `/api/repo-status`.

Key result:

- `/api/repo-status` returned `blocked=true`, `updated=false`, branch `codex/atlas-world-map-p0-e19fc1cc`, commit `ca81036f`.
- `GIT_TRACE` showed only branch/head/status commands and no `fetch`, `merge`, or reexec.
- The API smoke proved the startup dirty-source gate blocks auto-update before fetch.
- The server was stopped after proof.
- The isolated worktree was found to lack `docs/binder`, so binder updates must be recorded from canonical Latitude unless a separate binder-backfill slice is authorized for the isolated branch.

Evidence:

- `/tmp/latitude-atlas-api-smoke-20260604T015953Z-session/git-trace.log`
- rollout summary: `/Users/joolmac/.codex/memories/rollout_summaries/2026-06-04T01-57-18-bozU-latitude_atlas_api_startup_smoke_blocked_repo_status.md`

## Worldgen/Atlas Work Completed Before WARM_DRY Rebalance

The no-browser Atlas proof chain addressed humid-tropical dry leakage and Old Growth Pine Taiga before the WARM_DRY visual balance issue was raised.

Important run ladder:

- `20260604-170703`: WARM_MEDIUM humid-diversion candidate reduced humid-tag-to-savanna leakage while keeping WARM_DRY dry stable.
- `20260604-171539`: late WARM_MEDIUM jungle-family rewrite guard removed 41 invalid late rewrites that were happening at `u=0` tropical-core samples.
- `20260604-172705`: Atlas exporter support-pass guard removed 7 post-sampling support rewrites from preserved WARM_MEDIUM `sparse_jungle`.
- `20260604-182048`: Old Growth Pine Taiga provenance showed the remaining 14 pixels were real direct Latitude selection output, not provider drift or support drift.
- `20260604-182610`: removing only `minecraft:old_growth_pine_taiga` from `globe:lat_temperate_secondary` removed Old Growth Pine Taiga from sampled output.

Run `20260604-182610` authority:

- runtime/provider authority stayed valid: `globe:globe_xsmall`, `biomesoplenty:68`, `minecraft:65`, `terrablender:1`, non-minecraft total `69`;
- tropical/subtropical land-only dry residual: `77 / 1556 = 4.95%`;
- humid tropical/subtropical coverage: `1276 / 1556 = 82.01%`;
- dry residual was all WARM_DRY: `minecraft:badlands=70`, `minecraft:desert=7`;
- Old Growth Pine Taiga went from 14 pixels across 9 components to 0 pixels and 0 components;
- the 14 former Old Growth Pine Taiga pixels became `minecraft:old_growth_birch_forest` 11 times and `minecraft:taiga` 3 times;
- sampled palette count changed `54 -> 53`; inventory count changed `55 -> 54`; removed entry was only `minecraft:old_growth_pine_taiga`.

This state was better for humidity and Old Growth Pine Taiga, but the WARM_DRY product metric was incomplete.

## Problem

Julia observed that the visual output had effectively no desert. The artifact numbers supported that concern:

- baseline `20260604-182610` dry total: `77`;
- badlands: `70`;
- desert: `7`;
- badlands share: `90.91%`;
- desert share: `9.09%`.

The old acceptance metric, "low dry residual," was therefore misleading. It proved that humid leakage was mostly fixed, but it did not prove that the remaining dry WARM_DRY output looked like a believable arid mix.

The new product gate is:

- desert must be visibly present;
- badlands must also remain visibly present;
- neither desert nor badlands should consume nearly all sampled WARM_DRY output unless visual proof explicitly accepts that look.

## Attempts And Outcomes

### Attempt 1: Prefer Desert In WARM_DRY Fallback

Patch:

- `src/main/java/com/example/globe/world/LatitudeBiomes.java`
- WARM_DRY registry fallback was changed to try `minecraft:desert` before `minecraft:badlands`.

Proof run:

- `20260604-192555`
- `compileJava` passed.
- One no-browser BOP-backed Atlas smoke passed.

Result:

- dry total: `77`;
- desert: `77`;
- badlands family: `0`;
- humid tropical/subtropical total stayed `1276`;
- Old Growth Pine Taiga stayed `0`.

Visual status:

- Existing raw bundle was registered into the local viewer path.
- Viewer screenshot: `/tmp/latitude-atlas-viewer-smoke-20260604-192555.png`.
- Julia rejected the result because there were no badlands.

Classification:

- Numeric humid and taiga gates stayed green.
- WARM_DRY balance was red because the sample overcorrected to all desert.

### Attempt 2: Route Final Clamp Through Existing Arid Region Fallback

Patch:

- WARM_DRY final savanna climate clamp rewrites were routed through existing `pickAridRegionFallback(...)` instead of direct WARM_DRY family enforcement.

Proof run:

- `20260604-201317`
- `compileJava` passed.
- One no-browser BOP-backed Atlas smoke passed.

Result:

- dry total: `77`;
- desert: `77`;
- badlands family: `0`;
- humid tropical/subtropical total stayed `1276`;
- Old Growth Pine Taiga stayed `0`.

Classification:

- Failed for WARM_DRY balance.
- Existing badlands authority was too narrow for this seed/sample, so the final-clamp rewrite set still defaulted entirely to desert.

### Attempt 3: Final-Clamp-Only Badlands Retention Mask

Patch:

- Added a final-clamp-only WARM_DRY chooser in `LatitudeBiomes.java`.
- Existing dry picks are preserved.
- Non-dry WARM_DRY final-clamp rewrites use coarse `BADLANDS_OUTSIDE_PROVINCE_SALT` noise at scale `1024` with threshold `0.45`.
- Mask hits choose badlands variants; misses choose desert.
- WARM_WET, WARM_MEDIUM, support pass, provider jars, Old Growth Pine Taiga tags, viewer/API/desktop code, and unrelated biome tags were not touched in this slice.

Proof run:

- `20260604-202134`
- `git diff --check -- src/main/java/com/example/globe/world/LatitudeBiomes.java` passed.
- `JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain compileJava` passed.
- `JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain runBiomePreview --args='--seed 2591890304012655616 --size itty --radius 5000 --step 128 --emitBiomeIndex true --bundle --run 20260604-202134'` passed.

Result:

- dry total: `77`;
- desert: `42`;
- badlands family: `35`;
- badlands: `32`;
- wooded badlands: `2`;
- eroded badlands: `1`;
- desert share: `54.55%`;
- badlands-family share: `45.45%`;
- humid tropical/subtropical total stayed `1276`;
- Old Growth Pine Taiga stayed `0`;
- sampled palette contains both desert and badlands family;
- sampled palette does not contain Old Growth Pine Taiga.

Candidate gate:

- desert present: pass;
- badlands-family present: pass;
- desert/badlands balance: pass numerically;
- not all-desert regression: pass;
- not badlands-dominant baseline: pass;
- humid total stable vs `20260604-182610` and `20260604-192555`: pass;
- Old Growth Pine Taiga absent: pass.

Classification:

- The latest candidate fixes the numeric desert/badlands balance problem.
- It is still partial because it has not had viewer visual proof.

## Current State

Current best no-browser candidate:

- Run: `20260604-202134`
- Root: `/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc`
- Branch: `codex/atlas-world-map-p0-e19fc1cc`
- HEAD: `ca81036f`
- Tag at HEAD: `save/atlas-exporter-dry-edge-cleanup-proof`
- Tree: dirty WIP/evidence preserved, no staged files during proof.

Evidence:

- `/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc/tmp/warm-dry-badlands-retention-20260604-202134.log`
- `/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc/tmp/warm-dry-badlands-retention-20260604-202134-compare.json`
- `/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc/run/latdev/atlas/seed_2591890304012655616/Run_20260604-202134/R5000/step128`
- `/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc/run-headless/atlas-preview-20260604-202134`

Files changed in the latest source candidate:

- `/Users/joolmac/.config/superpowers/worktrees/Latitude (Globe)/atlas-world-map-p0-e19fc1cc/src/main/java/com/example/globe/world/LatitudeBiomes.java`

Files intentionally untouched in the latest candidate:

- canonical Latitude source files;
- provider jars;
- viewer/API/desktop code;
- WARM_WET behavior;
- WARM_MEDIUM behavior;
- support pass behavior;
- Old Growth Pine Taiga tag membership;
- browser/desktop/API proof surfaces;
- staging, commit, tag, push, cleanup/reset/stash/delete/restore.

## Known Problems And Open Risks

1. Visual proof is still missing for `20260604-202134`.

The latest candidate is numerically balanced, but the actual viewer look may still be wrong. The next proof must inspect the existing run, not generate a new one.

2. The candidate is dirty-worktree evidence, not savepoint-ready state.

The isolated worktree contains broad dirty WIP and generated artifacts. The WARM_DRY candidate must not be treated as committed doctrine until visual proof and savepoint-prep guard pass.

3. Binder docs are absent in the isolated Atlas worktree.

Canonical binder records can track the work, but the isolated branch itself does not currently carry `docs/binder`. That remains a workflow/reporting precondition for future isolated proof lanes if binder availability inside the branch becomes required.

4. The prior all-desert visual proof is red and should not be savepointed.

Run `20260604-192555` proved desert visibility but removed badlands entirely. It is evidence for the failure mode, not a candidate to preserve.

## Decision

Do not savepoint yet.

Treat the current state as:

- humid tropics/subtropics: green enough to preserve unless visual proof contradicts it;
- Old Growth Pine Taiga removal: green enough to preserve unless visual proof contradicts it;
- WARM_DRY desert/badlands balance: numerically green on `20260604-202134`, visually unproven.

## Next Recommended Slice

Run a viewer-only smoke of existing run `20260604-202134`.

Strict boundary for that slice:

- no generation/export/smoke rerun;
- no source/resource edits;
- no provider jar changes;
- launch only the minimal local Atlas viewer/API needed to inspect existing run `20260604-202134`;
- confirm desert and badlands are both visually legible;
- confirm humid tropics/subtropics remain cohesive;
- confirm Old Growth Pine Taiga remains visually absent;
- capture concise screenshot/log evidence;
- stop any viewer/API process started;
- stop before cleanup/reset/stash/delete/restore/savepoint/stage/commit/tag/push.

If visual green, the next step after that should be savepoint-prep/guard, gated on Julia approval.

If visual red, the next step should be exactly one narrow WARM_DRY visual/provenance candidate, not a broad climate retune.
