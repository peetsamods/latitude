# 1.4.1 worldgen quick fixes — mushroom splotch + vegetation in snow

`status: active` · `scope: worldgen` · `date: 2026-06-22` · staged jar SHA `80dc88f54eddcd4e6f1f`

## A. mushroom_fields splotch → restricted to genuine open ocean
- Symptom: `minecraft:mushroom_fields` appearing inland on high/rocky terrain beside small water.
- Cause: `mushroomIslandOverride` fired on any deep-ocean BIOME pick (1/2000 chunks). When deep-ocean
  is classified inland (land-like continentalness + amplitude → high rocky terrain), it got
  mushroom-ified → the "splotch."
- Fix (`LatitudeBiomes.java`): added `isGenuineOpenOcean(x,z,sampler)` = `oceanDistanceBlocks(...) <= 0`
  (the column is a true ocean cell by continentalness). The override now requires `isDeepOcean &&
  isGenuineOpenOcean`. Threaded the `Climate.Sampler` (already in scope in both `pick` overloads) into
  both `mushroomIslandOverride` overloads + call sites. Null sampler (atlas fast-path) → false → no
  override (safe default; mushroom islands just don't show in the headless atlas). Real ocean mushroom
  islands still generate; inland deep-ocean pockets never do.

## B. Vegetation in the snow → no grass_block in the snow zone
- Symptom: grass/flowers poking through alpine snow.
- Cause: `alpineSurfaceKind` carved out "meadow" (unchanged grass_block) cells even above the snow
  line; those grass_blocks (often under a vanilla snow layer) hosted vegetation.
- Fix (`LatitudeBiomes.alpineSurfaceKind`): the snow check runs first now — at/above the band snow
  onset the cell is ALWAYS snow (no meadow carve-out), so there's no grass_block in the snow zone for
  grass/flowers to grow on. The fading meadow shelf lives strictly BELOW the snow line. Extracted
  `alpineSnowMinY(blockZ,radius)` (shared by the cap). NOTE: a feature-level guard was attempted first
  but `RandomPatchFeature` does not exist in 26.1 mojmap (confirms the `ExtremePolarVegetationGuardMixin`
  comment) — the surface-level fix is cleaner and mapping-independent.

## Proof / status
- `check-biome-tuning-policy.py` + `check_tree_line_port.sh` pass; compileJava + build green; staged.
- Both are altitude/continentalness-gated and seed-specific-rare (mushroom) so the headless atlas
  can't cleanly show them → **live eyeball by Peetsa** is the confirmation (no inland mushroom splotch;
  bare snow caps with no grass). Art II: port to canonical + version chain.
