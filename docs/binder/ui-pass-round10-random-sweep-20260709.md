# Create-screen round 10 — animated "Random" spawn-zone sweep on the Atlas (2026-07-09)

`status: FIXED IN SOURCE, awaiting live look (TEST 41)`
Source: Peetsa — when the spawn zone is set to **Random**, the atlas currently just shows every band in the
flat unselected wash (no single zone is highlighted, since Random picks one at generation time). She wanted
Random to feel special: start colored in the middle, then have the bands "strobe/cycle outward in opposing
directions" — a colorful double-scrolling flourish.

## What it does

Only when the spawn zone is Random (`selectedBand == null` in `LatitudePlanisphereRenderer.renderCompact`):
a single glow **pulse** starts at the equator and travels outward through the latitude bands to the poles.
Because every band is already drawn mirrored north + south, one advancing pulse reads as **two** lit
regions moving apart — the "double scroll in opposing directions." As the pulse passes a band, that band's
own native climate color brightens and turns nearly opaque (the same ~1.30× brightening + high-alpha the
*selected* band uses); away from the pulse it settles back to the normal muted wash. A gold crest line
rides the pulse front (mirrored N+S), echoing the gold edge a selected zone gets, easing in/out via a sine
envelope so it doesn't blink at the loop seam. When the front runs past the pole the polar band fades, then
the loop restarts back at the equator — matching "initially colored in the middle, cycle outward."

Non-Random rendering is **byte-identical** — the selected-band pop, the muted others, and the static gold
outline are all untouched; the animation lives entirely in the `selectedBand == null` branch.

## Implementation

`src/main/java/com/example/globe/client/create/LatitudePlanisphereRenderer.java` only:
- The band-fill loop gains a `randomSweep` branch computing a Gaussian glow per band from the distance
  between the band's center latitude and the advancing pulse front (`frontDeg`).
- `frontDeg = phase * POLE_FADE_DEG`, `phase` from `System.currentTimeMillis() % RANDOM_SWEEP_PERIOD_MS`
  (the same wall-clock idiom as the Aurora compass theme; the create screen redraws every frame so it
  animates smoothly).
- Three tuning constants, all cosmetic and safe to change: `RANDOM_SWEEP_PERIOD_MS = 3200` (one full
  equator→pole loop), `RANDOM_SWEEP_SIGMA_DEG = 16.0` (how many bands are lit at once — wider = softer
  overlap), `POLE_FADE_DEG = 108.0` (front travels past 90° so the polar band fades before the wrap).
- The gold crest line replaces the old "none for Random" branch of the selected-band gold block.

## Follow-up (same day): fade in at the equator restart, not a pop

Peetsa liked the look but wanted the loop seam softened: when the pulse wraps back to the equator it
shouldn't POP a fully-lit band into existence. Added a `sweepEnv` envelope that eases the whole pulse
intensity IN at the equator restart and OUT past the pole, via a `SWEEP_FADE_FRAC = 0.22` fraction of the
loop and a `smoothstep()` helper (`sweepEnv = smoothstep(0, FRAC, phase) * smoothstep(0, FRAC, 1-phase)`,
multiplied into every band's glow). Peak (env == 1) holds across the middle of the sweep, so only the very
start/end of the loop are eased — the traveling pulse itself is unchanged. `SWEEP_FADE_FRAC` is a fourth
cosmetic knob (0 = old hard pop).

Peetsa had also tried some experimental constant values in her copy (period 800 / sigma 200 / pole-fade
250, which flattens the traveling pulse into a uniform all-band glow); on her call the shipped values were
restored to the original **period 3200 / sigma 16 / pole-fade 108** (the distinct outward pulse she liked),
with the new fade-in on top.

## Verification

- `compileJava` + pure-JVM suite green; `./gradlew clean build` green.
- `TEST 42.jar` staged (SHA-256 `04c1cd781aa106667adf35ffb33cf071641f94ea1cf4ca4629394a2f2a3b5a3c`),
  superseding `TEST 41.jar` (the pre-fade-in build) → `TEST 40.jar`.
- NOT live-verified yet: select Random on the create screen for a Latitude world and confirm the outward
  double-scroll reads well at both Classic (1:1 square) and Mercator (2:1) shapes, and that picking an
  actual zone still shows the normal static highlight. Purely cosmetic; the three constants above are the
  tuning knobs if the pace/width wants adjusting.
- Worldgen isolation: one client render file touched; zero `world/`, `terrain/`, `mixin/` changes. NOT
  committed yet (new subjective visual — held for Peetsa's live look before committing).
