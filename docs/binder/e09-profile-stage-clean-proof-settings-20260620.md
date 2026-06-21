# E09 Profile Stage And Clean Proof Settings - 2026-06-20

`status: partial`

## Trigger
The post-`v1.4.0+26.1.2` findings review left the release candidate partial: source/report findings were fixed in the candidate, but the active Modrinth profile still needed a clean current-SHA live proof. A fresh local verification build existed as SHA `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`.

## Candidate Truth
- Root: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`
- Branch/HEAD: `feat/custom-biome-expansion-26.1.2` / `e5d092ca7f09a397afc413137f62ea409566e1e7`
- Tag at HEAD: `save/clifftree-expansion-26.1.2`
- Active profile: `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2`
- Active profile jar: `mods/latitude-1.4.1-beta.2+26.1.2.jar`
- Active profile jar SHA-256 after staging: `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`
- Prior profile jar backup: `mods/latitude-1.4.1-beta.2+26.1.2.jar.pre-e09-stage-20260620-220852.bak`, SHA-256 `d51eace9e517db5e53c8754e581e44b49ef68a6778b0f367ee60c8eefa5df073`

## Profile Config Change
The profile was changed from render/simulation `32/12` to `16/8` for the next clean single-client performance proof. The previous options file is preserved as:

`/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2/options.txt.pre-e09-stage-20260620-220852.bak`

## Proof
- No Minecraft game client process had the profile jar open before staging.
- Exactly one loadable `latitude*.jar` remains in the profile mods directory.
- Active profile jar SHA matches the local verification build SHA `e09ea003...`.
- Active profile jar manifest still records `Git-Commit=e5d092ca7f09a397afc413137f62ea409566e1e7`, `Git-Branch=feat/custom-biome-expansion-26.1.2`, `Build-Dirty=true`, and `Build-Time=2026-06-21T01:59:22Z`.
- Profile options now record `renderDistance:16` and `simulationDistance:8`.

## Evidence
Evidence folder:

`/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/post-140-hardening-continuation-20260620-220852`

Key files:
- `profile-active-latitude-jars.txt`
- `profile-jar-after-e09-stage.sha256`
- `profile-jar-before-e09-stage.sha256`
- `profile-jar-after-e09-stage-manifest.txt`
- `profile-options-before-clean-proof-config.txt`
- `profile-options-after-clean-proof-config.txt`

## Current Verdict
This closes the local profile-staging drift for the next proof target, but it does not close live release readiness. Fresh `New Expedition`, scenic delta, clean non-teleport soak/performance, and clean shutdown remain open on SHA `e09ea003...`.

## Next Gate
Launch `Lat 1.4+26.1.2` without vanilla Launcher evidence, prove exact Java-owned `Minecraft* 26.1.2` window identity, then run the fresh smoke/scenic/soak/shutdown proof against active profile jar SHA `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`.
