# S37 icicle + atmosphere -- GlobeMod.java wiring TODO (Dressing Crew, 2026-07-23)

The Dressing Crew's brief forbids editing `GlobeMod.java` directly. This note is the
"one clearly-marked TODO" in its place.

## Required edit

In `com.example.globe.GlobeMod#onInitialize()`, alongside the other unconditional
content registrations (`PolarOutfitting.register()`, `PowderCrevasseRoofFeature.register()`,
`GlobeParticles.register()`), add:

```java
// S37 (Dressing Crew): register globe:icicle (reshaded pointed-dripstone clone) UNCONDITIONALLY,
// before registry freeze -- same registry-consistency law as the registrations above.
com.example.globe.world.IcicleBlocks.register();
```

No flag gates this call -- `globe:icicle` is pure decorative content (a block + BlockItem), not
gameplay behaviour, so it follows the same unconditional pattern as `PolarOutfitting`/
`PowderCrevasseRoofFeature`/`GlobeParticles` rather than a `LatitudeV2Flags` entry.

## Everything else needed no GlobeMod.java touch

* World generation (`globe:icicle_cluster`, the pale-moss features) is pure JSON, wired into
  `data/globe/worldgen/biome/glacial_caves.json`'s `features` array -- no registry call needed.
* Assets (blockstates/models/textures/lang/loot table) are pure resources.
* The pale-moss atmosphere uses vanilla blocks (`minecraft:pale_hanging_moss`,
  `minecraft:pale_moss_carpet`) only -- no new Java registration at all.
