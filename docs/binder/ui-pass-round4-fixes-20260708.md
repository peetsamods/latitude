# TEST 30/31 create-screen pass round 4 — atlas label misalignment + Ginormous flourish (2026-07-08)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 33 candidate)`
Source: Peetsa on the create-world screen — the atlas latitude labels don't line up with the atlas's own
gridlines, worst at Itty Bitty ("zero degrees is almost at the North Pole"), improving as the world size
grows. Plus a small ask: italicize "Ginormous" with an exclamation mark.

## Finding: label column drifts off its own gridlines at small preview radius

**Root cause:** `computePreviewLabelYs()` placed each label at its true proportional position
(`center + radius·deg/90`), then ran a forward de-collision cascade (push a label down if it's closer
than a comfortable gap to the one above it), then — if the cascade pushed the LAST label past the
atlas's bottom edge — **slid the entire column up by the overflow amount**, uniformly, including the
FIRST label (0°).

At small preview radii (Itty Bitty, Tiny), the true proportional gaps between the six labels (0°, 23.5°,
35°, 50°, 66.5°, 90°) are smaller than the comfortable minimum gap, so the cascade pushes every label
well past its true spot — and the resulting overflow, corrected by the uniform shift, can be large enough
to drag the 0° label (which started at its exactly-correct position, matching the atlas's own equator
gridline) far above where it belongs — visually "almost at the pole." As radius grows with world size,
true gaps stop being smaller than the comfort gap, the cascade and its corrective shift stop firing, and
labels sit at their true (correct) positions — matching Peetsa's "gets more accurate as the world gets
larger."

Confirmed the true-position formula matches the atlas's own rendering: `LatitudePlanisphereRenderer
.renderCompact()` places its latitude band lines with the identical `halfH · deg/90` shape, against the
same inner radius (`radius − previewFrameBorder(radius)`) the label function receives. So label and
gridline math were never inconsistent with each other — the bug was purely the label cascade's recovery
step corrupting an otherwise-correct anchor.

**Fix:** never move the 0° label — it is the one label guaranteed to be exactly correct (offset 0 from
center) and the column's anchor. When the comfort-spaced cascade would overflow the bottom, compress the
cascade's GAPS back toward a bare no-overlap floor (each label's own height, i.e. flush-but-not-touching)
instead of shifting the whole column. This:
- keeps 0° pinned to the atlas's true equator line at every world size,
- still guarantees no two labels ever overlap (gaps never compress below the no-overlap floor),
- degrades gracefully: interior/lower labels absorb the compression first, only landing exactly at their
  true position again once there's enough radius to afford it (as observed, matching "more accurate at
  larger sizes"),
- and only in a genuinely pathological case (six labels not fitting even flush-packed, roughly requiring
  a preview radius under ~20px) accepts a small residual overflow past the bottom rather than ever moving
  the anchor — verified by hand-simulation at several radii (20px: residual accepted, honest physical
  limit; 40px: compresses to exactly fit, zero overflow, 0° still exact).

## Finding: "Ginormous" wanted a flourish

Peetsa asked for the "Ginormous" world-size name to render italicized with a trailing exclamation mark
("Ginormous!") — the theatrical treatment fitting its description ("a world that could take a lifetime to
cross"). Added a small italic-centered-text helper alongside the existing plain one, applied only when
`selectedSize == GlobeWorldSize.MASSIVE` (the enum value "Ginormous" displays for); every other size name
is unaffected.

## Verification

- `compileJava` + pure-JVM suite (`cleanTest test`) green.
- Hand-simulated the label-compression algorithm at radius 20 and 40 (see commit/PR notes) confirming:
  0° always lands at its true offset; no compressed gap ever falls below the label's own height (no
  overlap); residual overflow only appears in a radius regime well below what the real UI produces.
- NOT live-verified yet (visual UI class of bug): cycle every world size at least once and confirm the
  degree numbers sit on their gridlines at every size, especially Itty Bitty/Tiny; confirm "Ginormous!" is
  italic with the exclamation mark and doesn't get ellipsized/clipped at narrow panel widths.
- Worldgen isolation by diff scope: only `client/create/LatitudeCreateWorldScreen.java` touched. Zero
  `world/`, `terrain/`, `mixin/` changes — C-3 and the TEST 30 args are untouched.

`TEST 32.jar` staged (SHA-256 `c7cdce7c2d17b04843491bcf0aeef461584796ba9be9ce9c70ee7d0e2f7c767c`),
superseding `TEST 31.jar`. Same worldgen args as TEST 30/31.
