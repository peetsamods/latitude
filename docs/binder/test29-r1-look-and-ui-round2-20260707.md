# TEST 29 — first r=1 bathymetry live look + UI round 2 (2026-07-07)

`status: UI fixes LANDED (jar held, SHA a97ea5c8…); worldgen wall finding DIAGNOSED, fix awaits authorization`
Source: Peetsa's TEST 29 session (jar `2f7360c1…`, world "Fable Test 29", **Regular**/10000 Mercator,
pinned seed, **r=1 args live**: `strength=0.4, oceanStrengthRatio=1.0` — log-verified installed+ENGAGED),
5 screenshots + a screen recording. This is the roadmap's first live look at Slice C-2 bathymetry.

## Worldgen: the "cursed giant wall" — carve-onset cliff (DIAGNOSED, not fixed)

At 40°S 135°E (x≈14,993, z≈4,426): a sheer, straight, ~30-block raw-stone wall running N-S for
hundreds of blocks; aquifer waterfalls + lava falls bleeding from the cut face; at its foot a
flowered, terraced green apron planed to just above sea level (still biome "Forest"), then shallow
shelf, then real deep water. Peetsa: "I think it's cursed."

**Mechanism (headless transect at her exact coordinates, same seed/preset/args — `test29wall-r1.json`):**

| x (z=4426) | land01 | solid surface Y |
|---|---|---|
| 14700 | 1.000 | 64 |
| 14800 | 1.000 | 70 |
| 14925 | 0.507 | 80 |
| 14950 | 0.374 | 60 |
| 14975 | 0.255 | 48 |
| 15025 | 0.084 | 40 |
| 15100 | 0.000 | 40 |
| 15200+ | 0.000 | below scan floor (deep carved ocean) |

`land01` ramps 1→0 over ~300 blocks (a real geography coastline — NOT the projection edge band,
which starts at |x|=16,000 here; the legacy `STORM_OVERLAY_EDGE_START=14650` overlay constant is
also unrelated). The carve design grades **depth** with ocean-ward distance (smoothstep) and shelf,
but the ceiling's **grip is instant**: the moment a column crosses to ocean-intent (d≥0), everything
above `Y* ≈ 63` is erased regardless of how tall the old-map terrain stood. Where the geo-coastline
crosses tall vanilla hills (Y~80-110), the d=0 contour becomes a planing cliff — vertical cut face,
exposed aquifers/lava, apron terraces below (the 0.08 ceiling slope makes the terracing). The apron
keeps land biomes (correct per the veto: floor ≥ sea level) so it dresses as flowered meadow.

**"Does img 2 look right?"** — YES, same system working as designed: the pale terraced flats at the
coast are the shelf-apron shallows (carved-to-shallow with beach/gravel dressing). The WALL is the
part that isn't acceptable, and it is a **C-2 design residual now live-confirmed**: the go/no-go
backlog's "sharp land01 edges become terrain shoulders" class, promoted at r=1 from a 5-block
shoulder to a 20-40-block cliff.

**Fix candidate (needs Peetsa's authorization — worldgen slice "C-3 / grade the grip"):** blend the
carve ceiling in over a coastal ramp: `density = lerp(base, min(base, ceil), grip01)` with
`grip01 = smoothstep(0, GRIP_BLOCKS, d)` (GRIP ~100-200 blocks). Monotone between `base` and the
carved value ⇒ can't create floating terrain or new solids; turns walls into steep coastal descents.
Needs the standard gates: sweep matrix re-run (vanilla-density calibration scope), gap-delta
tripwires, r=0 byte-identity, fresh-world live look.

## UI round 2 — all landed (commit this doc ships with)

| Finding (Peetsa) | Root cause | Fix |
|---|---|---|
| "Atlas still is too low" | Composition was V-CENTERED in the preview area → parked mid-panel with dead space under the ATLAS label | Top-aligned under the label; slack falls below the caption |
| "Random rainbow + italicized when selected (only when selected)" | — | Selected Random row's name renders per-letter rainbow (RainbowText palette) + italic; unselected stays muted |
| "'LATITUDE' at the top was going to look more special" | Plain one-line gold text | Wordmark: 1.5× letter-spaced gold with dark-bronze letterpress under-layer + flanking rules with diamond tips; falls back to the plain line when the header rect is too small |
| Checkerboard "distracting"; tape drag "only a specific part works"; docked preview "clips off screen… photoshop background unbalancing" | The transparency aid drew ALWAYS, as a full diameter×diameter square — visually inflating the compass, mismatching the content-true hitbox (the grabbable strip), and hanging below the docked slot | Checkerboard now draws ONLY while the Inner Transparency slider is hovered/focused, sized to the look's true content rect |
| "Snap-to-grid should be in the labels tab" | Lived in General | Dragging + Grid Size rows moved to Labels (above Reset Labels) |
| "Default look shows 89°S… doesn't reflect the title or in-game location" | Compass preview text defaulted to LONGEST (worst-case) while the title preview used the real latitude | Preview text default = LIVE in-world (samples out of world); LONGEST still cycleable |
| "Even if attached, you should be able to move it, which would unselect attach" | Docked compass wasn't draggable | Grabbing a docked compass undocks it: pin seeded at the docked spot (no jump), drag continues normally, Attach button refreshes to OFF. (The optional "lock" idea noted, not built) |
| ~~Random zone created a Temperate world with no roll log~~ **NOT A BUG — Peetsa's correction (same night): she deliberately created with Temperate; the Random screenshot was a feature demo for the rainbow request** | Wiring verified correct end-to-end anyway | The always-on `random=… selectedZone=…` create-click log line stays (pure diagnostics; costs nothing) |
| (housekeeping) | "Reset Labels"/"Reset Title" were untracked (same class as Reset Compass in round 1: no scroll, no L-toggle) | Both tracked + tooltipped |

Verification: `compileJava` + pure-JVM suite (cleanTest) green; jar `a97ea5c846a956…` built + HELD
(stages as **TEST 30** on Peetsa's word). Worldgen untouched by diff scope (client/ + core/ui docs
only — zero world/, terrain/, geo/ changes); the r=1 look continues unchanged on this jar.

## Also confirmed this session (round-1 fixes seen working live)

- Tape docked BESIDE the hotbar, ON the hotbar line, text beside it — the content-true dock fix.
- Direction-format labels render on the tape (W/NW/N visible in the recording's frames).
- Create screen: atlas horizontally centered; Random row present and selectable with the sealed-orders
  helper; dims label/dice/copy all as designed.
