# e09 Fresh SMALL Live Smoke And Shutdown Proof - 2026-06-20

`status: partial-current-sha-proof`

## Trigger
Continuation of the post-`v1.4.0+26.1.2` hardening lane after staging the current `1.4.1-beta.2+26.1.2` profile jar SHA `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`.

## Candidate Truth
- Root: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`
- Branch/HEAD: `feat/custom-biome-expansion-26.1.2` / `e5d092ca`
- Tag at HEAD: `save/clifftree-expansion-26.1.2`
- Profile: `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2`
- Active profile jar: `mods/latitude-1.4.1-beta.2+26.1.2.jar`
- Active profile jar SHA-256: `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`
- Manifest build time: `2026-06-21T01:59:22Z`, commit `e5d092ca7f09a397afc413137f62ea409566e1e7`, branch `feat/custom-biome-expansion-26.1.2`, dirty `true`
- Proof settings: `renderDistance=16`, `simulationDistance=8`

## What Is Green
- Exact-window route was proven against Java-owned `Minecraft* 26.1.2`, not the vanilla Launcher.
- The live process for PID `26974` used game directory `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2`; a separate Slabbed `26.2` Java client was also running and is not accepted as Latitude evidence.
- Fresh `New Expedition` reached a playable Latitude world on the current staged jar.
- Log order shows the bespoke overlay activated and first rendered before playable entry, then cleared only after `first safe playable tick`.
- Screenshot evidence shows rendered terrain, HUD/compass, and biome overlay after entry, not the earlier void symptom.
- Save and Quit returned to the title screen; latest log records server stop and all dimensions saved; no new crash report appeared after the save/quit proof.

## Important Correction
The created world was named `Codex E09 Itty Smoke`, but the lifecycle log says `size=SMALL`, not Itty. Treat this as current-SHA fresh SMALL smoke proof. It is not Itty-size proof and does not close the Itty-specific scenic/terrain/performance gate.

## Remaining Risk
Performance is still partial. The live run logged three early `Can't keep up` warnings after entry:

- `2147ms / 42 ticks`
- `2713ms / 54 ticks`
- `2030ms / 40 ticks`

The later static wait produced no new log lines and CPU settled, but the machine was under heavy pressure and another Minecraft Java client was running. Thread sampling showed worker activity in `LatitudeBiomes.previewHeight -> surfaceDecisionY -> pick` through `NoiseBasedChunkGenerator.globe$populateBiomes`, which is the current high-column terrain authority path rather than the older `previewTerrain` recursion. Do not call the performance gate green from this run.

## Evidence
- Evidence folder: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/post-140-hardening-continuation-20260620-220852`
- Profile jar proof: `profile-jar-after-e09-stage.sha256`, `profile-jar-after-e09-stage-manifest.txt`
- Exact-window proof: `live-java-gamedirs-sanitized.txt`, `live-java-window-list.txt`, `live-e09-windowid-39054-direct.png`, `live-e09-title-menu-after-activation.png`
- Fresh-world proof: `live-e09-after-create-new-world-click.png`, `live-e09-new-expedition-current.png`, `live-e09-new-expedition-after-210s-static.png`, `live-e09-fresh-small-clean-log-lines.txt`
- Performance evidence: `live-e09-log-review-after-static-soak.txt`, `live-e09-thread-dump-after-static-soak.txt`, `live-e09-heap-info-after-static-soak.txt`, `live-e09-vm-stat-after-static-soak.txt`, `live-e09-top-after-static-soak.txt`
- Shutdown proof: `live-e09-before-save-quit-menu.png`, `live-e09-savequit-log-final.txt`, `live-e09-crash-reports-after-savequit.txt`, `live-e09-after-savequit-final.png`

## Next Gate
Run a clean single-client current-SHA proof focused on the still-open gates: Itty-size fresh/existing load, representative scenic delta, direct palm-canopy visual proof, and a normal movement soak with no unacceptable stalls. Do not delete/regenerate chunks, savepoint, tag, push, upload, or publish without Julia authorization.
