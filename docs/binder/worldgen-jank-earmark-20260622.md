# 1.4.1 — EARMARKED worldgen/terrain jank (deferred, not being fixed this slice)

`status: earmarked` · `scope: worldgen` · `date: 2026-06-22` · raised by Peetsa with screenshots + `~/Documents/deserts.pdf`

Peetsa: "Things seem … not great overall." These are LOGGED for a dedicated worldgen pass, not fixed
now (this slice is the GUI build). Evidence: the 5 in-game screenshots in chat + `deserts.pdf` (a
collage of desert terrain — massive high-amplitude sand dune-mountains, deep sand ravines, rivers
cutting through, at high elevation).

## 1. mushroom_fields splotch — ✅ FIXED 2026-06-22 (mushroom island override now gated to genuine open ocean: `oceanDistanceBlocks==0`; inland deep-ocean pockets on high terrain no longer mushroom-ify; see `worldgen-quickfixes-20260622.md`)
- Screenshots 1–2: a patch of `minecraft:mushroom_fields` appearing inland on rocky/raised terrain
  beside a small pool. Mushroom fields is a rare special vanilla biome that should not splotch in
  like this (and historically MC restricts it to isolated ocean islands).
- Likely a biome-placement/gate issue (mushroom_fields leaking into the land pool or a band),
  related to the confetti/pool work but distinct. **Fix idea:** gate `minecraft:mushroom_fields`
  out of the normal land pools (or restrict to genuine isolated-island placement), provable via
  headless atlas (share should drop to ~0 inland).

## 2. Coastal amplitude — raised "tectonic" beaches instead of smooth descent to sea
- Screenshots 3–5: beaches/coasts render as raised, terraced sand cliffs — "as if every beach is a
  tectonic plate and the land is pushed upward" — instead of a gentle slope from grass → beach → sea.
- This is the **terrain-amplitude** problem: coastal transitions are too dramatic/stepped.

## 3. Deserts at high elevation with dramatic terrain + deep sand ravines — unrealistic
- `deserts.pdf` + screenshots: deserts form as big high-relief sand massifs with ravines/rivers
  cut into them, at elevation. Real deserts are mostly low-relief (ergs/regs/flats); high dramatic
  sand mountains with incised canyons are not how sandy deserts form.
- Same **amplitude** root as #2 — the live terrain noise (vanilla/Terralith `minecraft:overworld/*`
  density functions) is too amplified for the warm-dry biomes, and Latitude currently can't tune it.

## Amplitude investigation (2026-06-22) — confirmed DF-level, not data-tunable
Traced the terrain path on 26.1.2: `globe.json` world_preset sets the overworld `settings` to
`globe:overworld`, and `globe:overworld`'s `noise_router` (`initial_density`/`final_density`) delegates
to **`minecraft:overworld/sloped_cheese`** + `minecraft:overworld/caves/*` — i.e. the VANILLA terrain
DFs, which **Terralith overrides**. The globe's own `data/globe/worldgen/density_function/base_terrain.json`
(etc.) are **orphaned/unused**. So the coastal "reefs" + dramatic high deserts are Terralith/vanilla
terrain shape, and amplitude is NOT tunable via globe data. Options (all non-trivial, real tradeoffs):
1. **Runtime DF wrapper** — intercept/dampen `minecraft:overworld/sloped_cheese` at runtime (roadmap's
   approach; biggest new system; keeps Terralith terrain but tames it; per-biome amplitude bias possible).
2. **Redirect `globe:overworld` to the globe's own DFs** (`globe:base_terrain`/`sloped_cheese`) — globe
   controls terrain + amplitude (data-only-ish), but **LOSES Terralith terrain** (major product decision)
   and the globe DFs are abandoned/untuned.
3. **Datapack override of `minecraft:overworld/sloped_cheese`** — global amplitude knob, but conflicts
   with Terralith by load order (whoever loads last wins) — fragile.
4. The outer `final_density` `mul 0.64`/`squeeze` in `globe:overworld` IS globe-controlled — a crude
   GLOBAL flatten knob, but it flattens ALL terrain (over-corrects) and needs heavy live visual tuning.
**Recommendation: option 1 (runtime DF wrapper) as a dedicated, scoped project — too big + tradeoff-heavy
to fire off blindly; needs Peetsa's design decision (esp. keep-vs-replace Terralith terrain).**

## Root-cause link (known)
- #2 and #3 are the **amplitude problem** already on record: the globe `terrain_spline` density-function
  graph is DEAD (live terrain = vanilla/Terralith DFs), so amplitude is NOT tunable via globe data —
  it needs a runtime DF wrapper (the "Altitude" amplitude knob in the roadmap). See the
  `amplified-terrain-small-worlds` and `altitude-mod-roadmap` notes. Worse on smaller worlds.
- Desert realism additionally wants warm-dry biomes biased toward LOW relief specifically (a
  per-biome/per-province amplitude bias once the DF wrapper exists), plus the #3-overrep Java
  enforcement work (`overrep-analysis-20260622.md`) since desert/badlands placement is hardcoded.

## General biome jank
- Catch-all: the above plus residual placement oddities. Revisit after the amplitude DF wrapper +
  the #3 Java enforcement slice land, then re-audit via headless atlas + a live cruise.

## 4. Tropical badlands = LAW VIOLATION — ✅ FIXED 2026-06-22 (map-proven, tropical 0.00% arid; see `worldgen-correctness-20260622.md`)
- Peetsa spawned into the tropical band → "vast badland". The long-standing LAW: **no badlands/deserts
  in tropical** (Earth geography). Confirmed by the headless matrix (tropical band: savanna 21.8% +
  desert 17.7% arid leak) and now a live spawn.
- ROOT CAUSE = the #3 hardcoded-Java leak (`overrep-analysis-20260622.md`): `pickOpenTropicalFallback`
  (~L7281) returns literal `minecraft:savanna`/`minecraft:desert`; `pickAridRegionFallback` returns
  vanilla badlands/desert; `enforceWarmProvinceFamily` coerces to vanilla arid. These run in the
  tropical/WARM path independent of tags. **Fix = enforce the law in the tropical fallback/enforcement
  path (Java): tropical must never resolve to badlands/desert** (route to a humid/dry-but-non-arid
  alternative). Higher priority than the amplitude items — it's a rule violation, not polish.

## 5. Forest "crater" terrain anomaly (image 4)
- A huge bowl/crater of stone where `minecraft:forest` should be — biome reads forest but terrain is a
  massive sink. Investigate: likely amplitude (#2/#3 root) interacting with a Terralith/worldgen
  carver or a province-height seam. Earmark + reproduce.

## 6. Bonus chest for Latitude worlds — ✅ FIXED 2026-06-22 (placeLatitudeBonusChest at the zone spawn; live test pending; see `gui-bookui-compass-bonuschest-20260622.md`)
- I enabled the bonus-chest toggle for Latitude worlds (UI gate removed; flag rides `goh.options()`),
  but Peetsa couldn't find the chest. The Latitude custom spawn logic likely bypasses vanilla
  bonus-chest placement (it places at the resolved spawn during spawn-finding, which Latitude overrides).
  Toggle ≠ generation. NEEDS: verify whether the chest places at the Latitude spawn; if not, place it
  explicitly at the Latitude spawn point. Reopened, not closed.

## 7. Snow line too HIGH for the terrain — ✅ ADDRESSED 2026-06-22 (ALPINE_ROCK_Y 184→168; live eyeball pending; see `worldgen-correctness-20260622.md`)
- Live: meadow peaks top ~Y176-181, clouds at Y190, alpine snow floor `ALPINE_ROCK_Y=184` → snow
  essentially never triggers. My 1.4.1 change only lowered onsets WITHIN the ≥184 zone, so it doesn't
  help peaks below 184. **Revisit: lower the whole alpine band (`ALPINE_ROCK_Y` ~165-170 + onsets) so
  snow catches real peak heights, keeping latitude grading + warm-creep safety.** (Supersedes part of
  `alpine-snowline-lower-20260622.md` — the onset shape is right, the absolute floor is too high.)

## 8. Vegetation in the snow — ✅ FIXED 2026-06-22
- Grass/flowers were poking through alpine snow. Root: `alpineSurfaceKind` carved out "meadow"
  (unchanged grass_block) cells even ABOVE the snow line, and those grass_blocks (+ vanilla snow
  layers) hosted vegetation. FIX: the meadow carve-out now applies ONLY below the snow line; the snow
  zone is always snow/stone (no grass_block) so grass/flowers have nothing to grow on there. Surface-
  level fix (no feature mixin — `RandomPatchFeature` doesn't exist in 26.1 mojmap). See `worldgen-quickfixes-20260622.md`.

## 9. latitude:alpine biome representation (snowy summit reads as "sunflower_plains") — EARMARKED (not done)
- Peetsa's idea: a snowy/rocky summit still reports its base biome (e.g. sunflower_plains). Introduce
  `latitude:alpine` (or globe:alpine) representation so high alpine surfaces read as alpine. Design +
  representation work (custom biome vs HUD/naming layer) — scope later.

## Disposition
DEFERRED — own worldgen pass, gated on: (a) runtime density-function amplitude wrapper (biggest
blocker, roadmap item), (b) the #3 Java enforcement/remap slice, (c) a mushroom_fields land-pool gate.
None block the 1.4.1 GUI work. Art II (canonical-first) + map-proof apply when picked up.
