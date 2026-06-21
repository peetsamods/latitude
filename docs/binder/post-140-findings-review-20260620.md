# Post-1.4.0 Findings Review - 2026-06-20

`status: partial`

## Trigger
Julia asked for review and hardening of a pasted findings report comparing current work against the GitHub-only `v1.4.0+26.1.2` release line.

## Source Truth
- GitHub release tag: `v1.4.0+26.1.2` -> commit `2deab50e691d228457ce67572c222214946e19bd`
- Report comparison commit: `e039b427` was the GitHub-release docs marker after the tagged release.
- Front-door checkout reviewed by the pasted report: `/Users/joolmac/CascadeProjects/Latitude (Globe)`, `main`, `fe660b50`
- Active candidate checkout: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`, `feat/custom-biome-expansion-26.1.2`, `e5d092ca`, tag `save/clifftree-expansion-26.1.2`
- Active staged Modrinth profile jar: `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar`
- Current active jar SHA-256: `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`
- Prior live-tested code-red jar SHA-256: `d51eace9e517db5e53c8754e581e44b49ef68a6778b0f367ee60c8eefa5df073`, preserved as `latitude-1.4.1-beta.2+26.1.2.jar.pre-e09-stage-20260620-220852.bak`
- Current proof profile settings: `renderDistance=16`, `simulationDistance=8`; previous `32/12` preserved in `options.txt.pre-e09-stage-20260620-220852.bak`

## Findings Matrix

| Finding | Candidate status | Evidence | Release impact |
| --- | --- | --- | --- |
| Missing custom-biome feature retention after decoration indexing | real in front-door `main`; fixed/present in active candidate | `globe.mixins.json` registers `ChunkGeneratorGenerateFeaturesBiomeSetMixin`; source has `featuresPerStep` expansion and `retainAll` redirect; profile jar contains the class and mixin entry | Not a candidate blocker. It is a front-door/main drift hazard until candidate work is merged/saved through the normal Julia-owned path. |
| Latitude tag pools collapsed to mostly vanilla-only lists | real in front-door `main`; fixed/present in active candidate | Candidate/profile jar includes populated `lat_arid_accent`, `lat_temperate_secondary`, `lat_subpolar_primary`, and `lat_tropics_secondary` with optional BoP/Terralith/Promenade/BYG/Terrestria/Wilder Wild/Traverse/CliffTree IDs where applicable | Not a candidate blocker after the current staged jar. |
| Equatorial anti-arid safeguards removed | real in front-door `main`; fixed/present in active candidate | Candidate `ProvinceAuthority` has the tropical wet-bias ramp; `LatitudeBiomes` has `demoteEquatorialBadlands` and `demoteEquatorialDesert` in both final savanna-clamp overloads | Not a candidate blocker. |
| Polar sanitize hard-remaps forest/taiga/cherry to `ice_spikes` | real in front-door `main`; fixed/present in active candidate | Candidate has `keepPolarIceSpike` cap and maps invalid polar forest/taiga/cherry picks to `polarSnowyBase`, currently `minecraft:snowy_plains`, not unconditional `ice_spikes` | Not a candidate blocker. |
| Chunk lag/open performance issue | real and still partial on active candidate | Current `e09ea003...` fresh SMALL run reached playable terrain and later went quiet, but logged three early `Can't keep up` warnings around 2.0-2.7s; thread dump sampled `LatitudeBiomes.previewHeight -> surfaceDecisionY -> pick` through `NoiseBasedChunkGenerator.globe$populateBiomes` while the machine was under high memory/load pressure and a second Java client was running | Candidate remains partial until a clean single-client movement soak is green or explicitly deferred. |
| Existing-world vanilla loading / void render transition | fixed for the prior existing-save proof; current-SHA rerun still needed | `d51eace9...` latest log shows `integrated-world loading overlay activated`, bespoke overlay first render, `first safe playable tick`, then `loading screen closed`; screenshot `live-itty-inworld-after-entry.png` shows terrain | Same-source `e09ea003...` is now staged, but live proof on that exact jar is still open. |
| Invisible but pickable Promenade palm leaves | fixed mechanistically; direct palm-scene visual proof still useful | Promenade registered zero-alpha tint `0x007DB22E`; Latitude now registers opaque `0xFF7DB22E` for four Promenade palm leaf/deco blocks at `ClientLifecycleEvents.CLIENT_STARTED`; `d51eace9...` latest log confirms compat applied to 4 blocks | Mechanism fixed. Keep one direct palm-canopy screenshot in the next scenic pass on current `e09ea003...`. |
| Shutdown/save hang | real on older run; green for current fresh SMALL proof | Crash report `crash-2026-06-20_21.23.54-client.txt` belongs to a prior jar/run. Current `e09ea003...` fresh SMALL proof saved and returned to title; latest log shows `Stopping server`, `Saving worlds`, and all dimensions saved, with no new crash report | Shutdown is green for the current fresh SMALL run, but keep performance/clean movement soak partial. |
| Fresh `New Expedition` smoke on current SHA | green for SMALL, not Itty | Exact Java-window proof and latest log show `globe 1.4.1-beta.2+26.1.2`, `size=SMALL`, bespoke overlay first render, `first safe playable tick`, overlay clear, rendered terrain screenshots, and no crash | Closes current-SHA fresh SMALL smoke. Does not close Itty-specific proof because the world name said Itty but lifecycle size was SMALL. |
| Itty Atlas biome diversity on current SHA | green for non-live Atlas; live proof still open | Seed `220220260619002`, radius `3750`, step `64`, 74 discovered biomes across `minecraft:43`, `biomesoplenty:17`, `terralith:11`, `promenade:3`, with all five placement bands represented; temperate shoulder sample had `warmDry=0` | Helps answer the smaller-world diversity question and does not support a one-biome/one-provider collapse for the tested seed. It does not close live chunk loading, rendering, or performance because Atlas sampling skips the live `previewHeight()` path. |
| Readiness/provenance drift | fixed in this docs pass | `docs/release/checklist.md`, `current-readiness-audit.md`, `current-gates.json`, router checklist, roadmap, runbook, and `tools/lat-readiness-nonlive-status` now point to `e09ea003...` as the active staged profile/runtime jar and preserve `d51eace9...` as historical proof/backup | Documentation/tooling no longer points at stale `063edb38...` as the active candidate and no longer treats mutable `build/libs` output as unstaged. |

## Proof Commands
- `tools/lat-post140-findings-classifier` -> `CLASSIFIER STATUS: PASS-PARTIAL`; proves the four concrete pasted-report source findings are fixed/present in both current source and active profile jar SHA `e09ea003...`, while live performance/visual gates remain partial/open.
- `git rev-parse --show-toplevel`; `git status -sb`; `git branch --show-current`; `git rev-parse --short HEAD`; `git tag --points-at HEAD`
- `git rev-list -n 1 v1.4.0+26.1.2`; `git ls-tree -r --name-only <rev> -- <paths>`
- `rg -n "ChunkGeneratorGenerateFeaturesBiomeSetMixin|featuresPerStep|retainAll|wet-bias|demoteEquatorial|keepPolarIceSpike|polarSnowyBase" src/main/java src/main/resources/globe.mixins.json`
- `unzip -p <profile-jar> globe.mixins.json`
- `unzip -p <profile-jar> data/globe/tags/worldgen/biome/<tag>.json`
- `javap -classpath <profile-jar> -p com.example.globe.mixin.ChunkGeneratorGenerateFeaturesBiomeSetMixin`
- `rg -n "integrated-world loading overlay activated|first safe playable tick|Can't keep up|Promenade palm" <latest.log>`
- `find <profile>/crash-reports -type f -newer <profile-jar>`
- `./gradlew --no-daemon --console plain runBiomePreview --args="--seed 220220260619002 --size itty --step 64 --emitBiomeIndex true --bundle --out <dir>"`

## Current Verdict
The pasted report is materially valid against the `Latitude (Globe)` front-door `main` checkout at `fe660b50`, but it is not evidence that the active `1.4.1-beta.2+26.1.2` candidate jar is missing those custom-biome/worldgen safeguards. The active candidate/profile jar `e09ea003...` contains the relevant custom-retention mixin, expanded biome tags, equatorial anti-arid gates, polar ice-spike cap, existing-save overlay/render fixes, and Promenade palm tint fix.

The release candidate remains partial. Current-SHA fresh SMALL smoke, save/quit shutdown, headless Itty Atlas diversity/no-collapse evidence, and repeatable post-1.4.0 source-finding classification are green on `e09ea003...`, but performance is still partial and Itty-specific live proof, scenic delta, direct palm-canopy visual proof, and a clean single-client movement soak remain open. The 2026-06-21 live continuation reached exact Java title-window proof, then the macOS session locked before safe gameplay proof could continue.

## Next Gate
When the screen is unlocked, run a clean single-client live proof on the current `Lat 1.4+26.1.2` profile jar SHA `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`: Itty-size fresh/existing load, representative scenic delta including a palm scene if available, and non-teleport movement soak/performance review. The SMALL fresh smoke and save/quit proof are already recorded in `docs/binder/e09-fresh-small-live-smoke-shutdown-20260620.md`; headless Itty diversity evidence is recorded in `docs/binder/e09-itty-headless-atlas-live-lock-20260621.md`.
