# Stony Peaks / Treeline / Desert Follow-up

`status: active` · `scope: worldgen` · `date: 2026-06-21`

## Context

Julia manually retested the current `1.4.1-beta.2+26.1.2` profile and reported three worldgen concerns:

- `/locate biome minecraft:desert` led to a dark-forest scene in the saved world.
- Stony peaks appeared as a low, broad stone section rather than a higher, more rugged mountain.
- The tree line should return to the older Y160-170-ish visual threshold.

The live-coordinate evidence in `tmp/live-coordinate-probe-20260621/coordinate-proof-summary.json` confirms that the reported desert locate target `[3773,136,3236]` was saved as `minecraft:dark_forest` through the column, while the stony target `[4431,110,3740]` was saved as `minecraft:stony_peaks` with top stone around Y101.

## First Source Slice

The first implemented slice restores the historical treeline baseline and tightens live temperate-mountain authority:

- `TREE_LINE_Y` restored from `180` to `168`.
- `TREE_LINE_FADE_BAND` restored from `16` to `28`.
- Temperate mountain-family promotion now requires a higher live terrain gate (`seaLevel + 56`) plus either ruggedness (`WINDSWEPT_RUGGED_THRESH + WINDSWEPT_RUGGED_HYST`) or the vanilla mountain-noise signal.
- Headless Atlas/source sampling without terrain inputs still uses the existing mountain-noise fallback, so representation maps do not swing simply because live terrain authority became stricter.

The historical treeline source is commit `18f2629f0519595bb4e7c82c8f6ee62982727deb`, where `TREE_LINE_Y = 168` and `TREE_LINE_FADE_BAND = 28`. No source-history evidence was found for an older `ALPINE_ROCK_Y = 172`; alpine rock remains at Y184 in this slice.

## Proof

Commands:

```bash
python3 tools/check-biome-tuning-policy.py
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain compileJava
git diff --check
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain runBiomePreview --args="--latdevBiomePng --seed=214214684415956679 --size=small --radius=7500 --step=64 --y=110 --layers=biome,stats,audit --emitBiomeIndex=true --bundle=true --out=/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/biome-tuning-20260621/after-small-y110"
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain -Dlatdev.locateBoundary="enabled=true;seed=214214684415956679;radius=7500;x=3773;y=136;z=3236;saved=minecraft:dark_forest;out=/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/biome-tuning-20260621/desert-boundary-current-head" runBiomePreview
python3 tools/check-desert-boundary-proof.py /Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/biome-tuning-20260621/desert-boundary-current-head/proof.txt
shasum -a 256 /Users/joolmac/Library/Application\ Support/ModrinthApp/profiles/Lat\ 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar
unzip -p /Users/joolmac/Library/Application\ Support/ModrinthApp/profiles/Lat\ 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar META-INF/MANIFEST.MF
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain build
shasum -a 256 build/libs/latitude-1.4.1-beta.2+26.1.2.jar
unzip -p build/libs/latitude-1.4.1-beta.2+26.1.2.jar META-INF/MANIFEST.MF
pgrep -fl "Minecraft|fabric|net.minecraft|java"
lsof /Users/joolmac/Library/Application\ Support/ModrinthApp/profiles/Lat\ 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar
cp -p /Users/joolmac/Library/Application\ Support/ModrinthApp/profiles/Lat\ 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar /Users/joolmac/Library/Application\ Support/ModrinthApp/profiles/Lat\ 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar.pre-76c5b020-20260621-160420
cp -p build/libs/latitude-1.4.1-beta.2+26.1.2.jar /Users/joolmac/Library/Application\ Support/ModrinthApp/profiles/Lat\ 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar
shasum -a 256 /Users/joolmac/Library/Application\ Support/ModrinthApp/profiles/Lat\ 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar
unzip -p /Users/joolmac/Library/Application\ Support/ModrinthApp/profiles/Lat\ 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar META-INF/MANIFEST.MF
mc-chat /worldborder get
mc-chat /locate biome minecraft:desert
mc-chat /tp @s 4052.5 100 3156.5 0 60
mc-chat /tp @s 4052.5 86.0 3156.5 0 60
python3 saved chunk biome probe for `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2/saves/Alternate`
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain -Platdev.preview.levelName=latdev-desert-214214684415956679 -Platdev.preview.levelSeed=214214684415956679 -Platdev.preview.levelType=globe:globe_regular -Dlatdev.locateBoundary="enabled=true;radius=7500;x=4052;y=86;z=3156;saved=minecraft:dark_forest;out=/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/biome-tuning-20260621/desert-boundary-globe-regular-live-seed-20260621-163309" runBiomePreview
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain -Platdev.preview.levelName=latdev-desert-fix-20260621164017 -Platdev.preview.levelSeed=214214684415956679 -Platdev.preview.levelType=globe:globe_regular -Dlatitude.debugWorldgenPath=true -Dlatdev.locateBoundary="enabled=true;radius=7500;x=4052;y=86;z=3156;saved=minecraft:dark_forest;target=minecraft:desert;startx=3773;starty=136;startz=3236;searchradius=6400;horizontalstep=32;verticalstep=64;out=/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/biome-tuning-20260621/desert-locate-proof-after-serverlevel-wrap-20260621-164017" runBiomePreview
python3 tools/check-desert-boundary-proof.py /Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/biome-tuning-20260621/desert-locate-proof-after-serverlevel-wrap-20260621-164017/proof.txt
python3 tools/check-biome-tuning-policy.py
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain --max-workers=1 compileJava
git diff --check
```

Results:

- Policy check: pass.
- Compile: pass.
- `git diff --check`: pass.
- Atlas sanity on seed `214214684415956679`, SMALL, Y110: pass as a distribution guard. Inventory remained aligned with the earlier headless run: `minecraft:desert` ~4.288%, `minecraft:dark_forest` ~0.873%, `minecraft:stony_peaks` ~0.184%.
- Current-source desert boundary proof at `[3773,136,3236]`: pass. `wrapped_source_biome=minecraft:dark_forest`, `source_equivalent_biome=minecraft:dark_forest`, and `populate_equivalent_biome=minecraft:dark_forest`, matching the saved profile biome from the earlier chunk probe.
- Profile/build jar preflight at that point: staged jar SHA was still `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458` and its manifest commit was `e5d092ca7f09a397afc413137f62ea409566e1e7`, while the source checkout was HEAD `3d719aff`.
- Fresh local build jar proof: `build/libs/latitude-1.4.1-beta.2+26.1.2.jar` is SHA `76c5b020eb9570f6b7498f823cc054dd0058f0453f477226a5fa6316f4f4e933` with manifest commit `3d719aff4c1f33be64daa44bc9a0b803d719ad84` and `Build-Dirty: true`.
- Profile staging proof: no matching Minecraft/Java process was running and `lsof` showed the profile jar was not open. The prior active profile jar was backed up to `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar.pre-76c5b020-20260621-160420`, then the fresh source-HEAD jar was copied into the active profile. Post-stage active profile SHA is `76c5b020eb9570f6b7498f823cc054dd0058f0453f477226a5fa6316f4f4e933` with manifest commit `3d719aff4c1f33be64daa44bc9a0b803d719ad84`. Evidence: `tmp/biome-tuning-20260621/profile-stage-fresh-head-20260621-160420`.
- Fresh staged live desert proof: exact Java-owned `Minecraft* 26.1.2 - Singleplayer` window loaded `Alternate` through Modrinth on staged SHA `76c5b020...`; the log confirms `[LAT][BUILD]` commit `3d719aff`, seed `214214684415956679`, `isGlobe=true`, and 15000-block border. `/worldborder get` returned 15000. `/locate biome minecraft:desert` returned `[4052, 86, 3156]`, but screenshots at `[4052.5,100,3156.5]` and exact locate Y `[4052.5,86,3156.5]` both show HUD `Biome: minecraft:dark_forest`. Saved chunk proof for `[4052,86,3156]` reports `saved_biome_at_y=minecraft:dark_forest` and `distinct_saved_biomes_in_column=[minecraft:dark_forest]`. Evidence: `tmp/biome-tuning-20260621/live-fresh-head-desert-20260621-160920`.
- Fresh coordinate source-boundary rerun on a seeded `globe:globe_regular` headless world: pass. Evidence: `tmp/biome-tuning-20260621/desert-boundary-globe-regular-live-seed-20260621-163309/proof.txt`. The old locate coordinate is exactly the mismatch boundary: `raw_source_biome=minecraft:desert`, while `wrapped_source_biome`, `populate_equivalent_biome`, `live_biome`, and `saved_profile_biome` are all `minecraft:dark_forest`.
- Gate debug proof: pass. Evidence: `tmp/biome-tuning-20260621/desert-boundary-globe-gatefields-20260621-163748/proof.txt` and adjacent debug logs. Once the world is running, `settings_accessor_ready=true`, `stable_globe_overworld_regular=true`, `active_radius=7500`, and `should_apply_latitude_worldgen=true`, but the generator still exposes `biome_source_class=net.minecraft.world.level.biome.MultiNoiseBiomeSource` to vanilla locate callers.
- Source fix: `ServerLevelFindClosestBiomeMixin` now redirects `ServerLevel.findClosestBiome3d(...)` so Globe worlds search through `LatitudeBiomeSource` instead of the raw vanilla source. Non-Globe worlds keep the raw source.
- Post-fix seeded locate proof: pass. Evidence: `tmp/biome-tuning-20260621/desert-locate-proof-after-serverlevel-wrap-20260621-164017/proof.txt`. The old bad coordinate remains correctly dark forest in final Latitude truth, and the simulated desert locate search returns `[3293,136,2692]` where `locate_result_holder=minecraft:desert`, `locate_result_wrapped_source_biome=minecraft:desert`, `locate_result_populate_equivalent_biome=minecraft:desert`, `locate_result_live_biome=minecraft:desert`, and `locate_result_final_matches_target=true`.
- Post-fix validation: `tools/check-desert-boundary-proof.py`, `tools/check-biome-tuning-policy.py`, `compileJava`, and `git diff --check` all pass.

## Registry-Backed Locate Closure

The first live retest of the plain wrapper was still red: staged SHA `cbc509f3680d485889fff33f5338a2de61aad4612f9410ce11b45faf0853538c` made `/locate biome minecraft:desert` return `[4596,80,3092]`, but HUD proof at that coordinate showed `minecraft:badlands`.

The final source slice made locate use the registry-backed `LatitudeBiomeSource` path so the biome holder returned by vanilla locate matches the final Latitude biome returned by populate/live sampling. Headless proof in `tmp/biome-tuning-20260621/desert-locate-proof-live-start-after-registry-locate-20260621-1658/proof.txt` passes with `locate_result_final_matches_target=true`.

Fresh build/stage/live proof:

- Built jar SHA: `af1579b2e7f885ace1567e7400fd94cf0e958e160201edaccca020b2b1c6231c`
- Profile staging evidence: `tmp/biome-tuning-20260621/profile-stage-registry-locate-20260621-170341/profile-stage.log`
- Live proof evidence: `tmp/biome-tuning-20260621/live-registry-locate-after-stage-20260621-1703/desert-live-proof-summary.md`
- `/locate biome minecraft:desert` returned `[5272,75,2353]`.
- HUD screenshots at `[5272,100,2353]` and exact returned Y `[5272,75,2353]` both show `Biome: minecraft:desert`.
- Saved chunk proof reports both points as `minecraft:desert`, with `distinct_saved_biomes_in_column=[minecraft:desert]`.

Julia's supplied screenshot from the same run shows palm fronds visible and is preserved as `tmp/biome-tuning-20260621/live-registry-locate-after-stage-20260621-1703/user-supplied-palm-fronds-visible.png`. Treat that as supportive visual evidence, not a standalone palm/decoration gate closure.

## Stony Peaks And Treeline Closure

Stony-peaks proof is green for this slice:

- Current-source probe at the old low saved stony coordinate `[4431,110,3740]` resolves as `minecraft:forest` in wrapped/source-equivalent/populate-equivalent/live truth, not as stony peaks.
- Current-source locate for `minecraft:stony_peaks` from that area returns `[1583,170,4028]`.
- Live HUD proof at `[1583,170,4028]` shows `Biome: minecraft:stony_peaks`.
- Julia accepted the corrected live angle as good stony-peaks evidence. The screenshot is preserved as `tmp/biome-tuning-20260621/live-registry-locate-after-stage-20260621-1703/user-supplied-stony-peaks-good-angle.png`.
- Saved surface sampling around the live stony scene reports top solid Y95..Y159, range 64, mostly `minecraft:stone` with calcite/grass/dirt accents. That is much higher and more rugged than the earlier low broad top-stone-around-Y101 section.

Treeline proof is green for staged-jar constants:

- `tools/check-biome-tuning-policy.py` passes.
- `javap` against the staged profile jar SHA `af1579b2...` confirms `TREE_LINE_Y=168` and `TREE_LINE_FADE_BAND=28`, with `ALPINE_ROCK_Y=184` and `ALPINE_ROCK_FADE=14`.
- Atlas compare found the usable historical treeline baseline at commit `18f2629f0519595bb4e7c82c8f6ee62982727deb`, also `168/28`. The public `v1.4.0+26.1.2` tag has no explicit treeline constants.

Combined proof summary: `tmp/biome-tuning-20260621/live-registry-locate-after-stage-20260621-1703/stony-treeline-proof-summary.md`.

## Savepoint Rebuild And Profile Stage

After Julia accepted the biome-tuning savepoint, the local branch was committed and tagged:

- Commit: `c9da0f93029f7f16c50a7bc89eb766c576a85b48` (`fix: close biome tuning follow-up`)
- Tag: `save/biome-tuning-followup-26.1.2`

The previously staged profile jar SHA `af1579b2...` carried pre-savepoint manifest commit `3d719aff...`. A fresh jar was rebuilt from `c9da0f93` and staged into `Lat 1.4+26.1.2`:

- Fresh build/profile SHA: `1f50c5954cef3c91de1b071e78172ad6940a41abe5bace3e6febde5ac449a477`
- Manifest: `Git-Commit=c9da0f93029f7f16c50a7bc89eb766c576a85b48`, `Build-Time=2026-06-21T21:53:47Z`, `Build-Dirty=true`
- Rebuild evidence: `tmp/closeout-1.4-20260621/rebuild-c9da0f93-175345`
- Profile stage evidence: `tmp/closeout-1.4-20260621/profile-stage-c9da0f93-175416/profile-stage.log`
- Backup of the pre-savepoint profile jar: `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar.pre-c9da0f93-20260621-175417`

The next release gate is one bounded final live cruise on SHA `1f50c595...`; do not treat the older `af1579b2...` profile proof as final release-candidate cruise evidence.

## Final Live Cruise Launch Blocker

The 2026-06-21 final live cruise attempt on the c9 savepoint candidate did not reach Minecraft. This is a launcher/control blocker, not a new Latitude worldgen red:

- The active profile jar still proves SHA `1f50c5954cef3c91de1b071e78172ad6940a41abe5bace3e6febde5ac449a477` and manifest commit `c9da0f93029f7f16c50a7bc89eb766c576a85b48`.
- No `Minecraft*` Java client process or window was present.
- Computer Use returned `cgWindowNotFound` for `Modrinth App`.
- macOS reported the Modrinth App process with no windows.
- Modrinth launcher log `session_20260621_181416.log` contains `theseus::state: Attempted to get state before it is initialized`.
- A reversible removal of `app-window-state.json` did not make Modrinth recreate a visible window. The file was restored and reset to a visible 1400x900 rectangle.

Evidence: `tmp/closeout-1.4-20260621/final-live-launch-blocker-181416`.

The next live slice starts by restoring a usable Modrinth/profile launch path. Do not use a direct token-bearing launch helper unless Julia explicitly authorizes that last-resort route.

## Final Live C9 Partial Cruise

Julia reopened Modrinth, superseding the launcher blocker. A bounded c9 live cruise ran on staged profile SHA `1f50c5954cef3c91de1b071e78172ad6940a41abe5bace3e6febde5ac449a477`.

Green:

- Exact Java-owned `Minecraft* 26.1.2` window proof.
- Live `[LAT][BUILD]` line for commit `c9da0f93029f7f16c50a7bc89eb766c576a85b48`.
- Existing `New World` load reached `isGlobe=true`, radius `10000`, border diameter `20000`, first safe playable tick, and normal loading-screen closure.
- `/worldborder get` reported `20000 block(s) wide`.
- `/locate biome minecraft:desert` returned `[-896,220,-1312]`; settled HUD proof at `-895.5,100,-1311.5` shows `Biome: minecraft:desert`.
- A 20-second non-teleport movement soak produced zero new warnings/errors/crashes.
- Save/quit returned to title and logged all dimensions saved.

Caveats:

- `tools/lat-cruise-gate` stayed pending only on `Worldgen path active`; source shows that log line is gated behind `-Dlatitude.debugWorldgenPath=true`, which the release-profile JVM did not use.
- Desert locate took `18128 ms` and logged a large `Can't keep up` warning; chunk-load teleports also logged short `Can't keep up` warnings. The later movement soak itself was clean.
- Current-world `/locate biome minecraft:stony_peaks` returned `[1590,170,4112]`, but settled HUD at the coordinate read `promenade:cotton_sakura_grove`. The view contains a rugged high mountain nearby, and prior stony proof remains supportive, but this current live cruise is not a clean stony locate/HUD green.

Evidence: `tmp/closeout-1.4-20260621/final-live-c9-192419`.

Next gate: Julia decides whether the prior accepted stony visual proof plus current rugged-mountain view is enough, or opens one narrow stony locate/HUD follow-up before release push/publication.

## C9 Stony Follow-up Green

Julia chose the narrow follow-up. A fresh live pass on the staged c9 profile SHA `1f50c5954cef3c91de1b071e78172ad6940a41abe5bace3e6febde5ac449a477` launched through Modrinth and proved the exact Java-owned `Minecraft* 26.1.2` window.

Green:

- Existing `New World` loaded as Globe with `JOIN: player=Peetsa, isGlobeOverworld=true`, `S2C globe state: isGlobe=true`, 20,000-block border, first safe playable tick, and normal loading-screen close.
- Fresh `/locate biome minecraft:stony_peaks` returned `[1452,170,4201]`.
- `/tp @s 1452 170 4201 0 60` settled with HUD `Biome: minecraft:stony_peaks`.
- The screenshot shows a rugged mostly-stone/calcite mountain scene.
- `/execute if biome ~ ~ ~ minecraft:stony_peaks run say stony_peaks_here` emitted `[Peetsa] stony_peaks_here`, proving the player-position biome server-side.
- Save and Quit to Title logged player/world/chunk saves and `ThreadedAnvilChunkStorage: All dimensions are saved`; final process check reported no `java` process.

Evidence: `tmp/closeout-1.4-20260621/stony-followup-c9-195009`.

Result: the c9 final live cruise no longer has an open stony locate/HUD blocker. Branch/tag push, upload, publication, cleanup, and porting remain separate Julia-owned release gates.

## Limits And Next Slice

This slice closes the desert-locate mismatch for the current staged dirty-source candidate. It narrows the issue:

- Deserts are globally present in headless Atlas on the same seed.
- The exact reported target `[3773,136,3236]` is dark forest in saved chunks and in current-source SOURCE/populate-equivalent proof.
- The video/profile jar was not a fresh HEAD artifact; it was stamped `e5d092ca`, two commits behind source HEAD `3d719aff`.
- The active profile was staged to source-HEAD jar SHA `76c5b020...`; live proof against that jar reproduced the mismatch at a new returned coordinate `[4052,86,3156]`.
- Seeded headless proof showed the mechanism: vanilla/raw source at `[4052,86,3156]` is desert, while Latitude-wrapped/populated/live/saved truth is dark forest.
- The final registry-backed source fix makes locate search use Latitude-wrapped biome truth, and live proof on staged SHA `af1579b2...` returns an actual final desert coordinate instead of the stale raw-source coordinate.

The next gate is not a desert-representation edit. The desert locate bug, stony-peaks visual concern, and staged-jar treeline constants are green for this dirty-source candidate.

Atlas distribution currently shows vanilla desert around 4.3% and dark forest under 1%, so the video/live symptom is not evidence of global desert underrepresentation or global dark-forest overrepresentation.

Remaining risk is ordinary release-readiness/release-ops risk: public version identity, branch/tag push, upload, publication, cleanup, and porting are not authorized by this proof.

This slice does not delete/regenerate chunks, push, upload, or publish.
