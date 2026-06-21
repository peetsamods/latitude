# e09 Itty Headless Atlas And Live Lock Note - 2026-06-21

`status: current-partial`

## Trigger
The post-1.4.0 hardening lane needed current-SHA Itty evidence after the `e09ea003...` profile jar was staged. Exact live proof was attempted, but the macOS session locked before gameplay could be driven safely.

## Candidate Truth
- Root: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`
- Branch/HEAD: `feat/custom-biome-expansion-26.1.2` / `e5d092ca`
- Tag at HEAD: `save/clifftree-expansion-26.1.2`
- Profile: `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2`
- Active profile jar: `mods/latitude-1.4.1-beta.2+26.1.2.jar`
- Active profile/local build SHA-256: `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`
- Active profile jar count: exactly one loadable `latitude*.jar`

Evidence: `tmp/post-140-hardening-continuation-20260620-220852/live-continue-20260621/preflight-profile-truth-current.txt`.

## Live Control Result
Before the display lock, the exact-window helper identified a Java-owned game window:

```text
owner=java title=Minecraft* 26.1.2
```

An exact screenshot showed the title screen, not the vanilla Launcher. After one Singleplayer-targeting UI action, exact-window capture failed because the macOS session locked. Current lock evidence shows `CGSSessionScreenIsLocked=Yes`, so live proof must not continue until Julia unlocks the screen and exact-window capture succeeds again.

Evidence:
- `tmp/post-140-hardening-continuation-20260620-220852/live-continue-20260621/window-identity-initial.txt`
- `tmp/post-140-hardening-continuation-20260620-220852/live-continue-20260621/initial-exact-window.png`
- `tmp/post-140-hardening-continuation-20260620-220852/live-continue-20260621/locked-session-capture-attempt.txt`
- `tmp/post-140-hardening-continuation-20260620-220852/live-continue-20260621/lock-state-current.txt`

## Headless Itty Atlas Result
A current-SHA Itty headless Atlas run was completed for seed `220220260619002`, radius `3750`, at both `step128` and denser `step64`. These runs are non-live evidence only: the Atlas sampler intentionally uses the atlas fast path and skips live `previewHeight()` calls, so this does not close the live chunk/loading/performance gate.

The denser `step64` run produced:
- 74 discovered biomes.
- Providers: `minecraft:43`, `biomesoplenty:17`, `terralith:11`, `promenade:3`.
- Placement bands: `polar:8`, `subpolar:18`, `temperate:27`, `subtropical:14`, `tropical:7`.
- No one-biome or one-provider collapse.
- Temperate shoulder rows showed forest/birch/dark-forest/river/ocean composition and `warmDry=0` in the sampled shoulder window.

Evidence:
- Atlas output: `tmp/post-140-hardening-continuation-20260620-220852/live-continue-20260621/headless-itty-atlas-step64/seed_220220260619002/Run_e5d092ca/R3750/step64/`
- Summary: `tmp/post-140-hardening-continuation-20260620-220852/live-continue-20260621/headless-itty-step64-inventory-summary.txt`
- Image SHA-256: `7cc8c7c5a3a3916f71efd17fc52f4e8e0c4f89a0578058ae1ce0772f0834aebb`
- Summary SHA-256: `bb46b2024ffd185c53f41598651f14d543e8e0c915bc703234d93e45ba1c452f`

## What This Proves
The current `e09ea003...` candidate does not show an Itty-scale Atlas biome diversity collapse for the tested seed. The smaller-world warning can still be true in the sense that smaller worlds sample fewer total chunks and therefore may naturally show less total biome variety, but the current headless evidence does not support a release-blocking "Itty becomes one biome/one pack" failure.

## What This Does Not Prove
This does not prove that live Itty loading/rendering is green. It also does not prove the palm-canopy visual gate, scenic delta, or clean movement soak. Those still require exact-window live proof on the same profile jar SHA, ideally with only the Latitude Java client running.

## Next Gate
When the screen is unlocked, resume with exact-window capture first. Accept live proof only if the targeted window is still Java-owned `Minecraft* 26.1.2`, the profile jar SHA remains `e09ea003...`, and the in-game run closes Itty load/terrain, scenic delta, direct palm visual, and clean movement soak/performance without vanilla Launcher contamination.
