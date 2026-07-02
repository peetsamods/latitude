# Arid Belt Dry Mosaic Assessment - 2026-07-02

`status: parked future pass` · `scope: worldgen` · `branch: feat/custom-biome-expansion-26.1.2` · `HEAD: f86944cb`

## Summary

The current subtropical arid belt is legal and coherent, but visually risks reading repetitive. The target future pass is a **dry mosaic**: keep the 26-35 degree subtropical belt clearly dry and Earth-like, while increasing visible variety through scrub, dryland, wasteland/steppe, oasis, canyon/mesa, rocky arid accents, and dry grassland shoulders from installed biome-provider mods.

This note is a read-only assessment and planning capture. It is not an implementation, release gate movement, or proof-complete savepoint.

## Current Assessment

- Current Atlas generation: `/Users/joolmac/Desktop/LatitudeAtlasExport_20260702-064708_20260702-065928.zip`.
- Root assessed: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`.
- Branch / HEAD: `feat/custom-biome-expansion-26.1.2` / `f86944cb`.
- Band legality is already strong: strict band-correctness proof from the assessed run reported tropical hard-arid at `0.00%`, subtropical hard-arid at about `28.27%`, and temperate hard-arid at about `0.16%`.
- The distribution problem is representation, not belt legality. Hard-arid output is heavily Minecraft-vanilla, with `minecraft:desert` carrying most hard-arid cells and custom arid providers mostly cameo-level.
- Subtropical land is still dominated by the savanna/grass-dry plus desert/badlands families, which supports the Earth-like arid-belt goal but may read repetitive unless that identity is deliberately desired.

## Provider Inventory Notes

Installed or observed local providers relevant to this assessment:

- BoP is present in headless and live/provider contexts. Useful dry candidates include `biomesoplenty:dryland`, `biomesoplenty:wasteland`, `biomesoplenty:wasteland_steppe`, `biomesoplenty:rocky_shrubland`, `biomesoplenty:scrubland`, `biomesoplenty:shrubland`, `biomesoplenty:lush_desert`, plus `biomesoplenty:prairie` and `biomesoplenty:lush_savanna` as dry-transition shoulder biomes.
- Terralith is present and has multiple dry/arid candidates. Useful candidates include existing desert/canyon/oasis entries plus `terralith:hot_shrubland`, `terralith:shrubland`, `terralith:steppe`, `terralith:arid_highlands`, `terralith:savanna_badlands`, `terralith:ashen_savanna`, `terralith:fractured_savanna`, `terralith:savanna_slopes`, `terralith:bryce_canyon`, `terralith:white_mesa`, `terralith:warped_mesa`, and `terralith:red_oasis`.
- CliffTree has useful arid candidates such as `clifftree:shrubland`, `clifftree:oasis`, `clifftree:coniferous_badlands`, and possibly `clifftree:desert_cliff`, but future work must verify whether the same CliffTree jar is present in the exact proof/live profile and resolve the current tag-script discrepancy around `desert_cliff`.
- Promenade did not show a clean arid-belt candidate. Exclude it from this pass unless Julia explicitly wants a more fantasy/wet-forest look, which would be a different target than dry mosaic.

Avoid cold or wrong-band candidates such as `biomesoplenty:cold_desert`, `terralith:cold_shrubland`, `terralith:rocky_shrubland`, `terralith:snowy_badlands`, wet/tropical canyon/jungle entries, CliffTree tundra/bog/sky/cave entries, and Promenade fantasy forests.

## Implementation Direction

Recommended future work should be staged and conservative:

1. Add a focused arid-belt metrics tool first, before source behavior changes. It should report hard-arid share, dry-transition share, top exact IDs, provider share, north/south symmetry, and starved intended IDs from atlas `biome_ids.png` and palette files.
2. Stage 1 should tune the normal subtropical ladder and biome tags, not global climate authority. Align normal `pickTropicalGradient` step 1 with the documented NoSwamp `LAT_ARID_*` route, and promote carefully chosen provider IDs out of accent-only starvation where appropriate.
3. Do not first change belt width, `ProvinceAuthority`, `TROPICAL_WET_BIAS`, or the hard tropical/poleward clamps. The belt is legal; the problem is representation inside the legal belt.
4. Stage 2 only if custom arid IDs remain starved after Stage 1: add a narrow approved-custom arid survival path for WARM_DRY behavior, while keeping tropical and poleward demotion guards authoritative.

## Acceptance Shape For A Future Pass

Suggested proof targets:

- `band_correctness_check.py --strict` remains green.
- Tropical hard-arid stays `<=0.5%`.
- Temperate hard-arid stays `<=1%`.
- Subtropical hard-arid stays in the `20-35%` range.
- 23.5-26 degree fringe hard-arid stays around `2-12%`.
- Each 26-35 degree core slice stays around `20-45%`.
- Top hard-arid exact ID is `<=65%` of hard-arid cells.
- At least 6 hard-arid IDs reach `>=0.5%`.
- At least 2 non-Minecraft hard-arid providers reach `>=2%` each when present in the proof inventory.
- Hard-arid plus dry-transition remains the dominant subtropical identity, roughly `65-95%` of subtropical land.
- No single subtropical land biome exceeds `30%`, and vanilla `minecraft:savanna` stays `<=25%`.
- `cohesion_representation.py` should not show worse confetti, monoculture, or new starved intended IDs.

Suggested seed matrix:

- Primary seed: `2591890304012655616`, small/R7500, step16.
- Cross-seeds: `214214684415956679` and `220220260619002`, at least step64 first, then step16 if results are close.

## Boundaries

- This is a future pass idea, not the active TEST 1 punch-list unless Julia explicitly reprioritizes.
- Do not turn this into release authorization, savepoint closure, live proof, upload, tag, push, or public claim.
- Keep live/profile provider inventory separate from headless provider inventory. Do not count CliffTree toward acceptance unless the proof/live profile actually includes it.
- If this becomes active work, re-read the current dirty diff first; `LatitudeBiomes.java` was already modified when this note was captured.

## Sources Used

- Julia chat request and selected concern about dry-savanna domination.
- Read-only Codex and subagent assessment on 2026-07-02.
- Current Atlas export: `/Users/joolmac/Desktop/LatitudeAtlasExport_20260702-064708_20260702-065928.zip`.
- Local binder context including `arid-belt-earthlike-20260625.md`, `overrep-analysis-20260622.md`, `overrep-enforcement-warm-medium-20260622.md`, and `worldgen-regression-prevention-20260625.md`.
- Read-only Notion context: Latitude Hub, Release Hard Gate, and Bug & Regression Summary, used only to confirm release boundaries and canon.
