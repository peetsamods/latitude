# Ocean depth — is it shallower than vanilla? (measured, 2026-07-08)

`status: ANSWERED with numbers — carve DEEPENS (never shallows) vs vanilla; two secondary findings recorded`
Trigger: Peetsa on the TEST 30 r=1 world — "the ocean seems shallower than vanilla... am I imagining
things? is this intentional?" Measured directly on her seed (`2591890304012655616`, globe_large /
radius 10000, Mercator), r=0 (== vanilla ocean side, byte-identical by the C-2 gate) vs r=1 carve.

## Headline: the carve does NOT make oceans shallower than vanilla

Sea level is a normal Y=63. On the 81-column coherence grid, 37 columns are ocean-INTENT (land01 < 0.5).
Where geography wants ocean, **vanilla mostly put LAND**: the median vanilla floor at those columns is
right at sea level, ranging up to Y139 (a vanilla mountain standing where the globe wants sea). The
carve pulls those down to open water. By density truth (the finalDensity ladder, i.e. the field that
actually places blocks), every fully-ocean-intent column (land01≈0) is carved to air/water from the
surface down past Y40 — deep ocean, no remnant land.

The carve's clamp is `min(base, ceil)`: it can only remove terrain above the target, never add it. So
by construction it can only make a given column **deeper or equal** to vanilla, never shallower.

## So what is Peetsa actually seeing? Two real, intended things

1. **The continental-shelf apron is deliberately shallow near shore.** `shelfApron(shelf01)` keeps
   the carve to as little as 20% of full depth right at the coastline, widening to full depth offshore
   — that IS the terraced shallows she liked the look of. Flying coastlines, most of the water in frame
   is this intentional shelf zone.
2. **Carved basins settle to a uniform moderate depth, not vanilla's abyssal trenches.** Max carve
   depth at the current calibration is `|S·r| · K_DEPTH_BLOCKS = 0.4 × 60 = 24 blocks` (floor ~Y39).
   Vanilla's deepest oceans go ~30-40+. So new oceans read as a consistent ~24-deep sea rather than a
   dramatic drop-off. This is a **tunable knob, not a design floor** — deepen by raising `K_DEPTH_BLOCKS`
   (ocean-only; doesn't touch land lift) or the strength. Not changed here; flagged for Peetsa's call.

## Secondary finding A (tooling): OCEAN_FLOOR_WG height ESTIMATE misreads deeply-carved columns

The coherence probe's `generator.getBaseHeight(..., OCEAN_FLOOR_WG)` returned Y94-118 for ~10
ocean-intent columns whose finalDensity ladder proves they're carved to air/water below Y40 (true floor
< Y39). The density field is correct; vanilla's worldgen HEIGHT ESTIMATOR (`iterateNoiseColumn`, cell
-resolution) mis-estimates the column height under the carve's sharp `CEIL_FLOOR = -0.5` clamp. This is
a metric artifact — it does NOT change generated blocks (the world is correct, matching the video's deep
blue + kelp floors). Recorded so future depth audits trust the density ladder, not getBaseHeight_WG,
on carved columns.

## Secondary finding B (hypothesis, links to "ships in a forest"): _WG heightmap → structure placement

If Latitude's structures place off the same `*_WG` estimate that A shows is wrong on carved columns, a
shipwreck could be positioned by a phantom-high floor estimate and end up stranded above the real
carved seabed / on adjacent coast — a plausible mechanism for Peetsa's "ships in a forest". UNVERIFIED;
it belongs with the biome/structure-side alignment work (the consumer law-compliance slice / the TEST 28
coastline-label candidates), not the terrain carve. Do not treat as confirmed until a structure-origin
probe demonstrates it.

## Evidence
- Headless: `scratchpad/depth-r0.json` (vanilla stand-in), `depth-r1.json` (carve), `depth-ladder-r1.json`
  (density ladders at 0,2750 / -1375,4125 / -5500,0 / 4125,0). r=0-as-vanilla is justified by the C-2
  gate's proven r=0 ocean byte-identity.
- The terrainV2-OFF run produces no probes (harness only runs its suite when the wrapper installs) — use
  the r=0 recipe as the vanilla ocean stand-in, not terrainV2=false.
