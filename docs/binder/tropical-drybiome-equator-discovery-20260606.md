# Latitude — Tropical Dry-Biome Equator Discovery — 2026-06-06

`status: active` · scope: `worldgen` · result: `discovery` (metric built, fix NOT yet done) · evidence id: `20260606-tropical-drybiome-equator-discovery`

## Finding
A new exact sub-latitude metric (`tools/atlas/sublatitude_dry_wet.py`) on Julia's live world (Modrinth profile "Latitude 1.4", world "Testy is Besty", **seed 2533348776566713405**, small/R7500/step16) shows the deep equator carries dry biomes Earth would not put there:

```
 lat band | wetJung savanna  des/bl    DRY
   0-5    |  58.4%   19.9%   21.2%   41.1%   (badlands 8.8%, desert 12.4% at 0-5deg)
   5-10   |  49.1%   34.8%   15.8%   50.6%   (badlands ~0%)
  10-15   |  38.0%   30.8%   30.6%   61.4%
  15-20   |  34.5%   25.4%   39.5%   64.9%
  20-24   |  27.2%   32.1%   39.5%   71.7%
```
Wet→dry gradient direction is correct. But **badlands at 0–5° (8.8%)** is the clearest realism gap (MC badlands = American-SW ~35°N subtropical landform; Earth's equator has none), and **desert at 0–5° (12.4%)** is borderline-high (Earth deep equator is rainforest). **Savanna throughout is Earth-true (keep).**

## Method
`biome_ids.png` red channel = biome INDEX; `biome_palette.json` maps index→id (exact, no color collision — do NOT use natural `biomes.png`, colors collide; this is why an earlier natural-color attempt read savanna=0%). Bin LAND by `|lat| = |z|/radius*90`, categorize wet_jungle / savanna / desert_badlands. Top-biome-per-band confirmed via `tmp/tropical-drybiome-equator-discovery-c2e4dd84/topbiomes_per_band.py`.

## Decision / next
Julia wants a worldgen overhaul to push badlands (and tighten desert) out of 0–10°, keep savanna. Full task spec, code levers (`badlandsProvinceAuthorityHitModern` needs a latitude gate; `ProvinceAuthority.classifyWarm` wet-bias for desert), and proof method recorded in memory `v1p4-tropical-drybiome-equator-overhaul`. Handed to a new thread. NOT yet implemented. Validate the badlands-at-0–5° pattern on a 2nd seed before tuning (this is one ocean-heavy seed).
