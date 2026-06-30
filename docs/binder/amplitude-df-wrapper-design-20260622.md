# Terrain amplitude — runtime density-function wrapper (design / scoping)

`status: design` · `scope: worldgen` · `date: 2026-06-22`
Goal (Peetsa): tame the coastal "reefs" + over-dramatic high deserts (terrain too tall/stepped) while
KEEPING Terralith's terrain. Confirmed (see `worldgen-jank-earmark-20260622.md`): `globe:overworld`'s
noise router delegates terrain to `minecraft:overworld/sloped_cheese` (Terralith-overridden), so the
only keep-Terralith lever is to wrap the density function at runtime.

## Interception point
- `RandomState.create(HolderGetter, Holder<NoiseGeneratorSettings>, long)` builds the `NoiseRouter` from
  the settings (both mapped + already used in GlobeMod). Mixin into `RandomState` (constructor TAIL, or
  a redirect on router construction); gate on the settings holder being `globe:overworld` (mirror
  `NoiseChunkGeneratorCarveMixin`'s `GLOBE_SETTINGS_KEY` check). Non-globe worlds untouched.
- Replace `router.finalDensity()` (and `initialDensityWithoutJaggedness()`, which feeds the cheap
  heightmap/spawn preview) with a wrapped DF. `NoiseRouter` is a record with ~16 DF fields — rebuild it
  via the canonical constructor copying all fields except the two we wrap (verify field order against the
  26.1.2 mapping; everything else passes through unchanged).

## Dampening approach (prototype): height-dependent density bias
Terrain height = where `finalDensity` crosses 0 (below = solid, above = air). To LOWER peaks without a
full rearchitecture, add a downward density bias that grows with height above sea level:

```
wrappedFinal = DensityFunctions.add(
    origFinal,
    DensityFunctions.mul(
        DensityFunctions.constant(-K_AMPLITUDE),
        DensityFunctions.yClampedGradient(SEA_LEVEL, HIGH_Y, 0.0, 1.0)));   // 0 at/below sea, 1 by HIGH_Y
```
- High terrain's density is pulled toward "air" → peaks settle lower → amplitude (peak height spread)
  compresses; sea level and oceans (y<=SEA_LEVEL → gradient 0) are untouched, so coastlines flatten
  from the *top down* (the "reefs" lose their tall stepped tops).
- `K_AMPLITUDE` is the single tunable knob (start ~0.05; raise = flatter). `HIGH_Y` ~200 (above which
  full bias). Pure DF math, deterministic, Art VI-clean (no floorDiv/cell-hash).
- This is the crude GLOBAL first cut. It is intentionally simple + safe + reversible (set K=0 = no-op).

## Per-biome amplitude (next iteration, after the global knob is dialed in)
Multiply the bias by a per-biome/per-province factor so e.g. DESERTS get a stronger flatten (Earth-true
low-relief ergs) than mountains. Source the factor from the band/province (latitude + ProvinceAuthority)
or a biome tag (`globe:low_relief`). Keeps mountains dramatic where wanted, flattens deserts/coasts.

## Proof = LIVE (atlas can't show terrain height)
The headless atlas is a fixed-Y biome map; it cannot show amplitude. Verification is Peetsa's live
eyeball: a temperate/desert coast should descend more gently to the sea (no tall reef steps); high
deserts should be lower-relief; mountains still present (K tuned so peaks aren't erased). Iterate K live.
Add a `-Dlatitude.debugAmplitude` log of the wrap activating + a column height sample for a quick check.

## Risks
- NoiseRouter record field order is mapping-version-sensitive — verify before constructing; a wrong field
  wrecks terrain. Keep K=0 fallback + try/catch around the wrap so a failure leaves vanilla terrain.
- Density bias ≠ exact height scaling; very high K could create floating cutoffs or flatten oceanside
  cliffs unnaturally — dial K up slowly with live screenshots.
- Heightmap/spawn use `initialDensityWithoutJaggedness`; wrap it too or spawn-finding disagrees with
  the rendered surface.
- Art II: globe-worldgen change → port to the version chain once tuned.

## First implementation step
Mixin `RandomState` (gate globe:overworld) → rebuild `NoiseRouter` with the two wrapped DFs + `K_AMPLITUDE`
constant + debug log + K=0/try-catch safety. Build, stage, Peetsa live-tunes K. Then add per-biome factor.
