# Create-screen round 8 — Classic (1:1) atlas oversized/clipping, Mercator untouched (2026-07-08)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 37 candidate)`
Source: Peetsa — the atlas preview on the create-world screen clips off the panel even at "Tiny" world
size when the Classic (1:1 square) world shape is selected, and "Itty Bitty" still looks very large. Asked
specifically to shrink the 1:1 rendering only, leaving the 2:1 Mercator atlas exactly as it is.

## Root cause

The atlas's radius is computed per frame from the available panel space, then a fit-shrink loop verifies
the full composition (globe box + latitude labels + caption) actually fits before drawing (never a raw
clip — an oversized request gets resized down first). The width side of that budget uses a
`widthDivisor`: `2.0 * MERCATOR_ASPECT` (= 4.0) for Mercator, `2.0` for Classic — correct in principle,
since a Mercator globe is twice as wide as tall for the same radius and a Classic globe is square. But the
practical effect: for the *same* panel, Classic's width budget comes out roughly **double** Mercator's,
which in practice made Classic almost always **height-bound** (the height budget, identical for both
shapes, becomes the smaller of the two and wins the `min()`). That meant:

- `previewDiscFill`'s per-world-size grading (0.70 at Itty Bitty ramping to 0.99 at Ginormous) barely
  mattered for Classic — even Itty Bitty rendered close to the full height ceiling, reading as much larger
  than intended.
- With so little width headroom actually being used, there was little margin left once the real
  composition math (label width, padding, frame border) was subtracted — enough to clip at Tiny in
  practice.

Mercator doesn't have this problem because its stricter 4.0 width divisor keeps it reliably width-bound,
where `previewDiscFill` has real room to operate.

## Fix

Added `CLASSIC_ATLAS_SCALE = 0.62f`, applied to the computed radius **only when the shape isn't
Mercator** — right after the existing shared width/height-budget + disc-fill math, before the fit-shrink
loop runs. Mercator's code path reads nothing new and is byte-identical. This directly shrinks every
Classic world size (including Itty Bitty) and gives the fit-shrink loop far more headroom before it would
ever need to hit its floor, fixing the Tiny-size clipping too.

## Finding 2 (follow-up, same day): 0.62 was too aggressive — bunched sizes together

Peetsa: after the 0.62 shrink, Ginormous now reads about like Small/Regular and Itty Bitty isn't much
smaller either. Cause: the multiplier scales every world size down by the SAME fraction, so it shrinks the
*absolute pixel gap* between sizes right along with the overall size, not just the overall size — a
strong shrink compresses the whole size range into a visually narrow band. Raised `CLASSIC_ATLAS_SCALE`
from `0.62f` to **`0.82f`** — a happy medium between the original `1.0` (too big, clipped at Tiny) and
`0.62` (too small, sizes indistinguishable). No other logic changed.

## Verification

- `compileJava` + pure-JVM suite (`cleanTest test`) green.
- `./gradlew clean build` green; `TEST 39.jar` staged (SHA-256
  `84f6c803da4297d812831722d58c3b8122c78b7df8a14d014e8eaf62b7ec558a`), superseding `TEST 38.jar` (which
  superseded `TEST 37.jar`, the first 0.62-scale attempt, which superseded `TEST 36.jar`).
- NOT live-verified yet: cycle all 6 world sizes with Classic selected (no clipping, sensible relative
  sizing from Itty Bitty through Ginormous) and separately confirm Mercator's atlas is visually unchanged
  from TEST 36 at the same sizes.
- Worldgen isolation: only `client/create/LatitudeCreateWorldScreen.java` touched (one new constant + one
  conditional multiplier at the radius computation). Zero `world/`, `terrain/`, `mixin/` changes.
