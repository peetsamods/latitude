# Phase 5 — Boundary Experience: plan & run log (2026-07-09)

`status: IN PROGRESS — B-0/B-1 (23908d7a) + B-2 incl. runtime gates complete (pushed ba9b1099);
B-3-P1 polar approach done (4daf45d5); B-3-P2 hemisphere titles + warning language done (dev+18-tests+
sweeper green, reviewer PASSED); NEXT: stage TEST jar → B-4 live look`

## B-2 pass log (2026-07-09)

Developer (Opus): both fixes per the amended design. New pure helper `core/geo/EdgeOceanRamp.java`
(START=0.10 / FULL=0.70 / MAX_SHARE=0.94 — cap deliberately <1.0 so the border is a frayed island coast,
not a solid ring); new flags `latitude.boundaryV2.enabled` + `latitude.terrainV2.floorSightedVeto`
(default false, build.gradle-forwarded); GeoSummary gains trailing additive `projectionEdgeXOnly01`
(X-only edgeB — poles untouched); Fix-1 in BOTH pick() copies AFTER the raised-land veto, gated on
BOUNDARY_V2 + geoV2 summary + terrainBiasActivelyBiasing + oceanStrengthRatio≠0, frayed via ValueNoise2D
salt "edge_ocn" (~900-block provinces at R7500, floor 256); Fix-2 flag-ternary in BOTH mirror-veto
copies (skipPreview arm → previewFloorHeight/OCEAN_FLOOR_WG; !skipPreview untouched). 4 GeoSummary
constructor call-sites across 2 test files gained the trailing neutral arg (arity-only).
Test-writer (Sonnet): `EdgeOceanRampTest` 13/13 green (containment invariant incl. negative/NaN-class
inputs, monotonicity over 200 samples, 0.94 cap, strict-< fray boundary, formula-derived anchor).
Sweeper (Opus): **ACCEPT-WITH-NOTES, zero defects** — amendment compliance verified (X-only edgeB;
insertion after the raised-land veto both copies; boundaryV2-on+terrainV2-off cannot fire), flag-off
byte-identity structural (lazy ternary; current live config inert), twins character-identical, salt
unique across ~45, Art VI clean, build.gradle keys exact. Notes: "4 test files" = 4 call-sites in 2
files; Fix-1's river comment wording imprecise (edge-river behavior intentionally parked for B-4).
Runtime gates remaining: terrain-aware atlas proof (flag-off identity + flag-on edge targets re-derived
at the proof radius) + empirical OCEAN_FLOOR_WG-sees-carve check — the director's next step.

## B-3-P1 pass log (2026-07-09) — polar approach (dev+tests+sweeper+re-sweep green, reviewer pending)

Developer (Opus): new pure helper `core/PolarHazardWindow.java` — hazard [87,90]: progress=(deg-87)/3,
slowness 0..II / weakness 0..I integer tiers, freezeTicks 0..140 (vanilla freeze-death threshold),
miningFatigue >=~88 deg, blindness >=~89 deg; ambient [85,90]: snowCount 2..30 FIXED per-tick budget,
fogIntensity 0..1. OLD window was stage-stepped from ~84.6 deg (progress 0.94 x 4 rungs); NEW = hazard-free
below 87 (more explorable pole) + continuous bite to 90. Server leg: borderUxTick re-driven by the helper,
private PolarStage enum removed. Client leg: `if (client.isPaused()) return;` guards ALL particle paths
(the anti-backlog HARD REQ); ambient snow hoisted out of enableWarningParticles (always-on atmosphere,
sandstorm-haze precedent); EW storm particles keep their config gate. Ordering achieved: warn text ~84.6 ->
snow 85 -> hazard 87 -> lethal 90 (atmosphere first, then danger).
Test-writer (Sonnet): `PolarHazardWindowTest` 21/21 (exact-87.0 boundary = bitwise 0, monotonic over 1200
samples, endpoint/clamp/tier assertions).
Sweeper (Opus): ACCEPT-WITH-NOTES + ONE real find — the fog re-point fed DEAD code (polar screen fog never
rendered anywhere, pre-existing; all severity plumbing was uncalled). Fix-up (same dev, resumed): NEW
`client/PolarWhiteoutOverlayHud.java` (first-ever visible polar whiteout: full-screen fill on the proven
EwSandstormOverlayHud precedent, alpha=min(0.90, intensity^2*0.90), tint 238/242/248, guards incl. F1 +
surfaceOk so no cave whiteout) + one call in InGameHudMixin after the EW haze + comments corrected
(computePoleFogEnd explicitly UNCONSUMED). Re-sweep (Opus, delta-scoped): **ACCEPT-WITH-NOTES** — claim
"no prior visible polar fog" CONFIRMED; mixin guard identical to proven EW path; alpha bounded <=229 (text
legible); both hemispheres via |lat|; comments accurate. Notes for B-4 eyeball: cold-corner EW-haze +
polar-white COMPOUND (aesthetic call); dead severity fields flagged for a cleanup slice (not removed).
compile + full suite green (21 new tests stay green).

## B-3-P2 pass log (2026-07-09) — hemisphere titles + warning language (dev+18-tests+sweeper green, reviewer pending)

Developer (Opus) recon: the N/S equator title WAS live but fired through the ZONE title's single display
slot (the exact collision B-3c targeted) and even self-suppressed while a zone title showed. Implemented:
new pure `core/HemisphereCrossing.java` (axis-agnostic sideOf/evaluate/composeLines; DEAD_ZONE=64,
MAX_STEP=256 teleport guard, COOLDOWN 15s — constants carried from the old N/S logic) + new dedicated
`client/HemisphereTitleOverlay.java` (ONE shared window, two slots, N/S-first stacking, fixed anchor
-40 independent of the draggable zone title, reuses the extracted ZoneEnterTitleOverlay.drawTitleLineAt
for identical styling) + GlobeWarningOverlay rewired per-axis + InGameHudMixin renders it after the zone
title + disconnect reset. E/W = prime-meridian (centerX) crossing, "Eastern/Western Hemisphere"; 0,0
same-window = single stacked two-line title by construction. Warning-language pass: exactly ONE string
tightened (POLE_WARN_1 "You should consider turning back." -> "Consider turning back."); other six judged
already good. Test-writer (Sonnet): HemisphereCrossingTest 18/18 (fire-on-cross, dead-zone wobble never
fires, no-fire-without-stable-side, teleport guard incl. ==256 boundary, deterministic cooldown re-arm,
composeLines ordering/null-drop, translation-invariance proving axis-agnosticism); suite 111/111.
Sweeper (Opus): **ACCEPT-WITH-NOTES, zero defects** — old-fires ⊆ new-fires (nothing regressed, formerly
zone-suppressed crossings now fire into their own channel); 0,0 stacked-single-title traced same-tick;
zone-slot collision structurally dead (only the real zone title calls ZoneEnterTitleOverlay.trigger);
drawTitleLineAt extraction byte-equivalent; antimeridian teleport-back can NEVER fake a prime-meridian
crossing (never crosses centerX + 256 guard). B-4 eyeball notes: late-joining second line skips its
fade-in (cosmetic); a zone title dragged to ~center-40 could visually neighbor the hemisphere block.

## B-3-P3 pass log (2026-07-09) — polar warning ladder re-anchor (dev+7-tests+sweeper green, reviewer pending)

Developer (Opus): re-anchored the 4 `LatitudeMath.POLAR_STAGE_{1,2,3,LETHAL}_PROGRESS` constants
0.940/0.970/0.990/0.995 → **0.9444/0.9667/0.9889/0.9967** (= 85/87/89/89.7°) to align the warning ladder
with P1's window, and rewrote the 4 polar warning strings: WARN_1 "Snow begins to fall. The cold is
setting in — consider turning back." (the snow-onset line, 85°), WARN_2 "The cold seeps in. Your movements
are slowing." (87°, as slowness starts), DANGER "DANGER! Lethal cold ahead. Turn back now." (89°, RED+BOLD),
LETHAL "The cold is freezing you." (89.7°, RED+BOLD — replaces the dishonest "The cold overwhelms you." that
fired before death at 90°). New `LatitudeMathHazardStageTest` (7 tests, boundary + monotonic-no-skip);
suite 118/118. Hazard MECHANICS (PolarHazardWindow, degree-keyed) untouched.

**Sweeper (Opus): ACCEPT-WITH-NOTES, zero defects.** Architect-priority findings:
- **SHARED-CONSTANT coupling (HONEST DISCLOSURE — corrects the earlier "EW out of scope / unchanged"
  framing):** these 4 constants are ALSO read by the E/W storm axis (`hazardStageIndexEW` = EW storm text +
  `stormIntensityForStage` particles) and the EW spawn safe-margin (`GlobeMod.java:693` uses STAGE_1). So
  re-anchoring N/S DID nudge EW: LEVEL_1 storm onset +66 blocks outward, LEVEL_2 whiteout onset ~50 blocks
  INWARD (safer), stormCritical +25 blocks, spawn cap +66 blocks outward (still min()-capped, spawn↔hazard
  gap stays exactly 0.08·xRadius=1200 blocks so spawn is NEVER inside a hazard band). Every shift ≤0.44% of (largest = STAGE_1, +66 blocks;
  X-radius, no EW warning inverts, no particle tier vanishes, terrain/biome byte-identical. **Architect
  decision: KEEP-SHARED** — correctness does not require splitting; the EW values were themselves arbitrary
  fractions, this is a sub-1% re-tune of a tuning choice, the one meaningful shift is in the safer
  direction. RECORDED-NOT-FIXED: the constants couple N/S-latitude warnings to EW-border-fraction storm
  tuning; the day either axis needs INDEPENDENT tuning, split them into separate sets (cheap then, and the
  shared coupling is the only blocker).
- **Rounding (NOTE, harmless):** the 4-dp literals aren't bit-exact vs deg/90 — WARN_2/DANGER/LETHAL lag
  their mechanic by fractions of a single block (~0.003°), WARN_1 leads by ~0.004°. Sub-block, imperceptible,
  no stage skipped. Optional future polish: express as exact `85.0/90.0`-style fractions (self-documenting,
  flips the sub-block lags to leads). Not fixed — genuinely cosmetic.
Ordering CLEAN (strict monotonic; each warning fires at/just-before its mechanic). Strings CLEAN (honest,
short, RED+BOLD retained). Mechanics independence CLEAN. New test correctly pins the new boundaries.

## B-4 round 3 — Peetsa's TEST 52 verdict + new ideas (2026-07-10)

**LOVES the terrain generation** ("WOW"). Punch list: (1) loading-screen dims say 20k x 20k on a Wide
world (should be 40k x 20k); (2) small hemisphere whisper should fade IN and out, translucent + italic
(currently pops in stark white via the vanilla action bar); (3) NEW FEATURE REQUEST: /latdev "survey" /
"what am I looking at" — explains the region's geology/geography in plain language from the authority
fields (why this massif, why this desert); future player-facing item (binoculars/survey kit) noted for
an official update; (4) F1 hide-HUD must hide the compass/labels too; (5) snap-to-grid NOT lost —
answered: it's the Labels tab's "Dragging" toggle; (6) IDEA SAVED: LATITUDE ACHIEVEMENTS (World
Traveler = all hemispheres, Nomad = keeps moving, etc.) — exploration incentives, future phase;
(7) polar VEGETATION should thin/vanish at high latitude (grass/sugarcane/flowers at 84-86 deg feels
wrong) — worldgen, flag-gated; (8) sun STILL visible at 86 deg (storm ramp too shallow — ambient 0.2
at 86 barely dents vanilla sun; steepen so full storm by ~87-88); (9) freeze PULSING bug — hearts
flash blue/red rapidly, damage never lands: the 10-tick throttled setTicksFrozen(max) sawtooths against
vanilla's 2/tick decay, so the frozen state oscillates and never sustains the 140 damage threshold —
needs per-tick maintenance; (10) wants a real BLIZZARD by warning level 2 (faster fall, sideways drive,
heavier density — current reads as regular snowfall); (11) E/W: Peetsa proposed the "teleport-evator"
MIRROR-BAND wrap — reserve the last ~5 deg as terrain generated as a COPY of the opposite edge, teleport
player inside identical terrain = seam invisible by construction. Assessment: this IS the classical
periodic-wrap technique and fixes the scrapped attempt's terrain-mismatch failure by construction;
remaining hard parts = cloud-layer snap on teleport, map mods, multiplayer sightlines. DECISION: ship
B-5 fog-passage first (machinery reusable), then prototype the mirror-band as a B-6 "invisible upgrade"
that can retire the prompt if it works. Storm warnings at the EW border retire whenever wrap ships.

### B-4 round 3 pass log (2026-07-10) — three parallel crews, all swept, committed 06404a9b/08dbc15e/68fb7289

**Pass 1 — /latdev survey (06404a9b).** Dev (Opus): pure `core/GeoSurveyNarrator` (terrain story from
plate/mountain/shelf signals, climate story from rain-shadow/windward/fetch/continentality, latitude-belt
line, traveler's note from prevailing wind + coast distance; zero MC imports) + `/latdev survey`
subcommand reusing the LIVE GeoAuthority instance and reconstructing `new ClimateAuthority(geo)`.
Sweeper (Opus): **ACCEPT-WITH-NOTES, zero defects** — the reconstruction is PROVABLY identical to the
live climate path (same (seed,zRadius,xRadius) triple, single-arg ClimateAuthority derives everything,
no static mutable state); NoOp/console/edge-of-world paths safe; rule branches mutually exclusive; no
jargon leaks. Notes: flat-column rain-shadow phrasing is geographically valid; geoV2-only gate
intentional. 8 scenario tests.

**Pass 2 — pole feel + UI fixes (08dbc15e).** Dev (Opus), 6 items:
(1) FREEZE PULSING root-caused — the 10-tick setTicksFrozen re-apply sawtoothed against vanilla's
2/tick thaw, so hearts flickered and the fully-frozen threshold (140) never sustained → freeze is now
maintained EVERY server tick via `steadyFreezeTicks = freezeTicks + 3`; sweeper verified against the
real tick order (END_SERVER_TICK set → −2 decay → 141 ≥ 140 still frozen at damage check; margin 1
would have failed). Old throttled call removed (no double-apply); max() never fights powder snow;
creative/spectator exempted (follow-up guard after sweep note — no steady blue vignette while test-flying).
(2) BLIZZARD ≥87°: wind 0.09→0.43 + fall 0.04→0.15 scaled by blizzardDrive + second dense pass
(≤60/tick fixed budget, isPaused guard — anti-backlog law intact).
(3) STORM SKY steepened: stormLevel=(deg−85)/2.5 → fully overcast by 87.5° (was 0.2 at 86°); real
weather never reduced (only lifts).
(4) Loading dims label shape-aware: Wide Regular now "40,000 × 20,000" (matches create screen z*4 × z*2).
(5) Whispers off the vanilla action bar → new `LatitudeWhisperOverlay`: fade-in/hold/fade-out,
translucent ≤70%, italic; reset on world switch; sole action-bar writer replaced (grep-proven).
(6) F1 now hides compass/labels/titles/whispers but KEEPS atmosphere (fog/haze = weather, not HUD);
sweeper decompiled Hud.class and proved no double-render on either branch (hidden path draws atmosphere
exactly once via the TAIL, visible path via the HEAD injection). Sweeper: **ACCEPT-WITH-NOTES** (vignette
note fixed in-pass; whisper per-frame alloc = cosmetic). +8 PolarHazardWindow tests.

**Pass 3 — polar vegetation fade (68fb7289).** Dev (Opus): new flag `latitude.polarVegetationFade.enabled`
(default OFF, build.gradle-forwarded). 26.2 re-typed the old RandomPatch small vegetation into
simple_block/block_column features (verified vs the merged jar — sugarcane = block_column, flows through);
new `PolarVegetationFadeGuardMixin` on both classes (HEAD-cancellable place, the proven tree-guard
pattern), keep-chance smoothstep 1.0@78° → 0@86° (half at 82°), frayed on coherent ValueNoise2D salt
"polveg" (48-block blobs, Art VI — thinning, not a ring). Trees untouched (tree-line guards own them).
Sweeper (Opus): **ACCEPT-WITH-NOTES + 1 MAJOR flag-on find** — no Y gate meant lush caves UNDER the
polar cap would be stripped of moss/dripleaf/glow berries, and Nether simple_block features (crimson
roots, patch_fire, sulfur pools) were exposed at far-Z. Fix-up (fresh Opus): 4 short-circuit gates in
order (flag → overworld-only → latitude+fray → surface proximity WORLD_SURFACE_WG margin 5, with a
4–6-margin guard test so it can't drift into cave territory), require 0→1 (remap breakage now loud),
redundant max() dropped. 16 pure tests. Salt uniqueness verified across all 48 codebase salts.
Flag-off byte-identity structural (first-statement return; new helper called only by the mixin).
PROOF NOTE: the atlas renders biomes, not decoration features — flag-on proof is an in-game eyeball
at 80–86° (sparse → bare, blobby edge), flag-off needs no atlas (no existing code path altered).

**Suite: 173/173.** Remaining round-3 items answered without code: #5 snap-to-grid never lost (Labels
tab "Dragging" toggle); #6 achievements idea saved; #10 mirror-band wrap assessed (classical periodic
wrap, fixes the scrapped attempt's terrain mismatch by construction; hard parts cloud-snap/map-mods/
multiplayer sightlines; sequenced as B-6 candidate after B-5). All commits HELD UNPUSHED pending
Peetsa approval.

### TEST 53 staged (2026-07-10)

`TEST 53.jar` staged in the Modrinth `LATITUDE 26.2` profile, SHA-256 `417fab10679ee779…`, built from
3f48633a (all three round-3 passes + docs). Replaces TEST 52. STAGING TRAP LOGGED: this project has NO
`remapJar` task — `./gradlew remapJar` fails instantly and a masked failure (`; ls` after it) made a
stale 15:20 jar look freshly built; the staged jar was byte-identical to TEST 52 (caught by SHA match +
missing new classes). Correct build = `./gradlew build -x test`; ALWAYS verify a staged jar by grepping
`unzip -l` for a class added in the round. Live-look notes for Peetsa: vegetation fade needs JVM arg
`-Dlatitude.polarVegetationFade.enabled=true` (default-off); everything else is on by default.

### UI round 13 (2026-07-10) — Peetsa's 7-item UI/UX list, audit-driven, committed 2da8c065/56628039/885b3da4/748605cb

While Peetsa flew TEST 53 he filed 7 UI/UX asks. Audit first: `docs/binder/latitude-ui-ux-audit-20260710.md`
(24 findings, 3 CRITICAL — 13-state blind color cycle, buried snap-to-grid, no HUD Studio front door;
committed da495ce0). Then four swept passes:
**Pass 1 (2da8c065)**: selected-zone letter-wave bounce + tab shimmer (quieter than the atlas crest);
rainbow text unified onto pure FlowingGradient (continuous hue, all 4 renderers match; discrete palette
removed); HUD Studio Cancel (constructor snapshot of BOTH config singletons, restored AND re-saved on
Cancel since the screen live-saves; preset undo/redo memory cleared — sweeper find fixed in-pass).
**Rename (56628039)**: shapes now "Wide 2:1"/"Square 1:1" everywhere player-facing (persistence proven
display-independent); loading line gains the shape word ("Regular · Wide 2:1 · 40,000 × 20,000 ·
tropical start") — closes Peetsa's two live reports.
**Pass A (885b3da4)**: 6 blind CycleButtons → SwatchDropdown modal picker (all options visible, chips,
keyboard nav, no click fall-through — sweeper traced routing); conditional controls roll out/in on a
cumulative eased-height layout (UiEase, identical at rest); NEW Reduce Motion setting (snapshot-covered);
grid-snap surfaced as a lit/dim canvas icon beside undo/redo + "Grid Snap" rename.
**Pass B (748605cb)**: create-screen options rail → code-drawn illuminated icon+label rows (compass rose,
'/' chip, village house, chest that opens lit with gold sparkles, monitor+rainbow wordmark, scroll);
accessible pattern icon+text+tooltip+On/Off+sound; backing writes byte-identical; audit H5 silent
mouse-select fixed. Suite 183/183. Commit order note: A before B (B reads reduceMotion).
STILL OPEN from the list: HUD Studio pause-menu front door (audit C3, queued Pass C), reduce-motion
wiring into create-screen bounce/shimmer/crest + zone-title/Aurora anims (field exists, consumers
partially wired), item 7's broader audit MEDIUM/POLISH backlog.

### UI round 13 follow-ups (2026-07-10) — live regression + delight passes (fca6a5f1, 350be841)

Peetsa (flying TEST 54, loved the round: "everything is beautiful"): (a) Random row missed the letter
ripple — fixed (same wave constants, aurora colors kept); (b) requested LATITUDE wordmark glow +
sparkles — breathing bloom + 4 deterministic twinkle motes; (c) SCREENSHOT REGRESSION: 885b3da4's eased
layout staircased the Presets tab (cursor advanced per WIDGET; same-line Load|Save|× and undo|redo
exploded into stacked rows) + reveal popped at the 0.85 threshold. LESSON: the prior sweep verified the
telescoping only for single-widget rows — the fix pass re-verified per-tab against git ground truth
(885b3da4^), extracted pure GroupRowLayout with a staircase regression-pin test, made mid-roll rows
scissor-slide continuously, and restored disabled greying (pre-existing bug: dead undo/empty slots were
clickable). (d) Rainbow/Aurora parity (Peetsa): title Rainbow = STATIC ROYGBIV, new appended Aurora
preset = flowing (by-name persistence, saved values unshifted), compass toggle renamed "Aurora Text" —
Aurora=flowing / Rainbow=static everywhere. (e) Reduce Motion now covers the ENTIRE create screen +
atlas planisphere (title/bounce/shimmer/crest/sweep/icon anims → static equivalents). Suite 188/188
(+GroupRowLayoutTest, static-gradient determinism tests). TEST 55 staging next.

### UI round 14 (2026-07-10) — digital compass glow-up, Studio fixes, GUI-scale hardening (ec9050b8..bf6f5e22)

Peetsa (flying TEST 57): digital compass "ugly, old, un-cute"; wants accessibility dropdown; grid glyph
"more griddy"; Compass Look tooltip wrong (= the audit's tooltip-leak note made user-visible); Disc vs
Ring indistinguishable; Color Cycle Speed direction backwards + wants a rightward speed ripple + lightning
bolt; assign a GUI-scale parity check. Delivered in 6 commits, all swept:
**ec9050b8** digital = themed rounded chip (ring underline / muted frame / needle-tinted direction token,
Aurora flows automatically); Disc-vs-Ring root cause = 0.50 inner alpha over dark faces → default 0.85;
NEW fresh default Rose+Sunset (5 default changes, migration-safety per-field verified — analogLook is
4 days old so pre-07-07 configs would adopt ROSE: accepted PRE-RELEASE ONLY, disclosed in-code, sanitize
fallbacks aligned). **d4f10cba** Studio: tooltip leak fixed at the source (off-screen cursor for
underlying passes while a picker is open); SpeedSlider left=slow/right=fast (storage unchanged), gold
crest drifting right + hand-drawn bolt, tooltip rewritten; grid glyph on exact integer thirds + node
dots; NEW AccessibilityMode enum {STANDARD, HIGH_CONTRAST, COLORBLIND_FRIENDLY} (General tab, Cancel-
covered, HUD-side application specced in javadoc = follow-up); scheme picker + Aurora speed + Custom
RGB unlocked for Digital style. **50ee7dc7** GUI-scale parity audit (0 CRITICAL / 3 HIGH — compass +
create screen already fraction-anchored/clamped; doc has a 5-min live checklist). Fix lanes:
**fdf9bccb** (A) zone-title render-clamp = un-strandable at any scale, titles fit-to-width, warnings
word-wrap stacking upward, pure OverlayLayout + 11 tests; **11f95e13** (B) Studio narrow layout —
threshold 636, Done|Cancel re-anchors clear of the card, sidebar shrinks to 150 floor keeping a 140px
preview strip, >=636 pixel-identical (test-pinned), HudStudioLayout + 8 tests; **bf6f5e22** (C)
scaledUi/compactUi no-op stubs deleted, 106 sites inlined token-verified, behavior-neutral.
Suite 208/208. TEST 58 next. Follow-ups queued: HUD-side accessibility application (CompassHud),
editor-preview clamp parity (WYSIWYG at edges), audit M5 atlas-vanish placeholder, Math.max residue.

### UI round 15 (2026-07-11) — accessibility modes for real + title styling overhaul (6c4d09bb/50fb00c8/9a833b1b)

Peetsa: "no meaningful change with the accessibility options at all" (correct — only Studio chrome ever
responded) + title wishlist (black outline w/ configurable color, drop-shadow toggle, diffuse glow,
off-white-outlined default, rainbow fade-in shimmer). Three passes, one sweep, one identity-deviation
fix: NEW pure core/ui/AccessibilityPalette (ONE rulebook, 15 tests — HC floors text/bg opacity + lifts
dim greys toward white; CB remaps only red/green-RELIANT signals via isRedReliant, gold provably passes)
applied across CompassHud (backing disc + rim, opaque digital plate, cyan needle under CB), create
screen (all leaf text helpers, icon plates), RulesIcons, AND the Studio itself (card/tabs/dropdown
rows/open list/glyphs — the screen he flips the dropdown on responds instantly). Titles: NEW pure
TitleStyle — 8-dir outline at constant 1 screen-px (1/scale in the scaled pose), explicit drop-shadow
toggle, 3-ring diffuse glow, one-sweep fade-in Gaussian shimmer for RAINBOW/AURORA (reduceMotion-gated);
NEW default = black outline + OFF_WHITE 0xF3ECDD fill, shadow off (pre-release migration disclosed
in-code; existing saves keep their fill color, adopt outline). Studio Title tab: outline toggle +
conditional RGB picker + shadow/glow toggles, preview live; hemisphere titles inherit w/ threaded fade.
Sweeper: ACCEPT-WITH-NOTES — STANDARD identity verified at 40+ sites, ONE deviation caught (reduce-motion
icon glow 0.85→0.9) and restored in-pass. Suite 228/228. TEST 60 staged next. NOTE FOR PEETSA'S FLIGHT:
his existing config keeps WHITE fill but gains the outline and loses the hard shadow (new keys) — the
Title tab toggles restore any prior look.

### TEST 61 staged + FIRST DIRECTOR-DRIVEN LIVE VERIFICATION via Modrinth (2026-07-11)

Peetsa directed live checks through the Modrinth App. Established a WORKING live-control lane from this
session: Modrinth App driven via computer-use (Play/Stop; stuck-"Stopping" cleared via the instance
chip), game window captured via `screencapture -l` + `tools/mc-window`, input via CGEvent helpers
(scratchpad mc-click/mc-scroll wrapping tools/mc-window geometry; ESC unreliable in bespoke screens —
click Done/Cancel instead; helper Swift compiles can exceed short Bash timeouts — use >=90s and
one-step-per-call). Window moves to the built-in display fullscreen sometimes — re-raise via System
Events AXRaise (mc-focus aborts when the window is on another Space; raise directly).

VERIFIED LIVE ON TEST 61 (SHA 4054de87..., ghost fix + glimmer commits d4eaea4e/0aa3d840):
- GHOST FIX CONFIRMED: HUD Studio round-trip from the Rules panel then scroll — ONE coherent layer,
  steppers + icon rows each rendered once, scroll-locked (the TEST 60 bug is gone).
- Header-band clip: all three panel headings clean in three-column mode.
- Icon rows: lit compass rose (On), dim '/' (Off), monitor+rainbow HUD Studio, CENTERED house roof,
  closed dim chest (Off), scroll glyph — all live.
- Side-anchored tooltips: Commands tooltip opened LEFT of the rail per design.
- Wide 2:1 + "40,000 x 20,000 blocks" dims label; LATITUDE wordmark glow + twinkle sparkles.
- Selected-zone brightness lift (Temperate name gold + brighter subtitle).
- HUD Studio: narrow-mode Done|Cancel right-anchor at small window; full-width layout normal at
  fullscreen; Rose/Sunset/0.85 fresh defaults; Look as dropdown; griddy snap icon.
- Title RAINBOW preset renders as STATIC ROYGBIV gradient in the Studio preview (parity fix live).
NOT yet eyeballed live: glimmer motion (needs an actual zone crossing), accessibility dropdown flip,
freeze/blizzard/pole work (needs in-world flight — Peetsa's). Game left running at the world-select
screen. TEST 61 = TEST 60 + ghost fix + color-aware title glimmer (suite 230/230).

### Compass text default matches title off-white (2026-07-11, f38108d3)

Peetsa: "Change the default compass text to the same gentle warm white of the titles." Fresh-config
`CompassHudConfig.textRgb` changed 0xFFFFFF -> 0xF3ECDD (LatitudeConfigData.OFF_WHITE_RGB, the same
constant the title's OFF_WHITE preset uses) -- one shared warm-white across compass and titles.
Existing saved configs keep their own textRgb untouched (present key, no migration). Studio's Text
Color dropdown gained an honest "Off-White" entry (name/rgb/title mappings) instead of mislabeling the
new default "White". Verified the constant is actually compiled in via javap disassembly (int 15985885
= 0xF3ECDD present in the CompassHudConfig constructor). Suite 230/230. TEST 62 staged SHA d6f63fcd...

### Title defaults refined: ALL CAPS, outline removed (2026-07-11, 8fa6fcff)

Peetsa flew with the outline and decided against it: "make the default titles ALL CAPS. Remove the
outline b/c I don't think I like it." zoneEnterTitleCase fresh default NORMAL->UPPERCASE;
zoneEnterTitleOutline fresh default true->false (toggle + RGB picker remain in the Studio for anyone
who wants it). Fixed the two duplicated hardcoded literals in the Studio's Reset Title / Reset HUD
handlers to match (same class of bug the compass pass caught earlier -- these duplicate-default-site
drifts keep recurring, worth a dedicated grep next cleanup pass). Existing saved configs untouched
(both keys always existed). Suite 230/230. TEST 63 staged SHA 4e56c0e5...

### Polar particles respect vanilla's Particles setting (2026-07-11, 6cf7b602)

Peetsa (performance): decreasing vanilla's Particles option should decrease the pole snow/blizzard
particles too, so it can't slow anyone's game down. Vanilla only has THREE tiers (verified against the
mapped jar: ALL/DECREASED/MINIMAL -- no "off"), so MINIMAL is the floor (thin but still snowy). New pure
core/ParticleDensity (FULL=1.0/DECREASED=0.5/MINIMAL=0.15, 6 tests) scales the polar ambient-snow +
blizzard second-pass budget, read fresh every spawn-tick from the live vanilla option via an exhaustive
switch expression (a future MC particle tier would fail to COMPILE, not silently mis-map). Anti-backlog
law untouched -- sweeper re-verified isPaused/spawnTick/no-new-state/no-per-tick-allocation from scratch.
E/W border-storm particles are a separate system, deliberately untouched. Suite 236/236. TEST 64 staged
SHA 33165fe5...

### Studio contrast plate, bright glimmer, /latdev title (2026-07-11, 68a0ee2d/2c5b35f6/b0c6f2f3)

Peetsa's flight-2 feedback, three parallel passes: (1) HUD Studio title preview now sits on a neutral
contrast plate (sized/positioned to exactly track the real un-fitted/un-clamped renderStaticAt geometry)
so outline/shadow/glow are always evaluable regardless of the world behind the editor. (2) Bright glimmer
-- ROOT-CAUSED a real bug along the way: the old multiplicative brighten() was near-invisible on
saturated colors (pure red got literally zero visible glimmer at any amplitude, since 255*x clamps and
0*x stays 0). Switched to lerp-toward-white (channel + (255-channel)*boost, no clamping needed by
construction), amplitude 0.34->0.85 -- new regression test pins primaries actually changing, would have
failed under the old code. (3) /latdev title -- genuine CLIENT command (Fabric's ClientCommands API,
not the server-side /latdev tree, since the title overlay is client-only rendering) fires the real zone
title on demand, bypassing the anti-spam banding but leaving every real-crossing state field untouched
(sweeper traced each one). All three swept together for cross-pass interaction (glimmer is color-only,
never affects geometry, so the plate padding holds regardless). Suite 237/237. TEST 65 next.

### /latdev un-shadowed + Studio title round + loading wave (2026-07-11, 14a081f1/bf6cc543/0f8c90d0)

LIVE REGRESSION FIXED: the new client-side /latdev title root shadowed EVERY server /latdev subcommand
(Fabric intercepts any line whose root the client knows; parse failures surface instead of forwarding —
the earlier pass's "falls through" claim was FALSE, disproven from fabric-command-api-v2 3.1.0 bytecode).
Fix = greedy passthrough forwarding via a direct ServerboundChatCommandPacket (vanilla's unsigned branch;
the polite sendCommand() path would re-enter the interceptor and recurse infinitely). LESSON: client
command roots that share a server root REQUIRE passthrough; claims about framework dispatch behavior
must be verified from bytecode, not docs-folklore — the original sweep accepted the claim unverified.
Also: title-preview plate → Title-tab-only photoshop CHECKERBOARD (misleading-background complaint);
preset OVERWRITE now confirms (Overwrite/Keep It, empty slots instant); Outline Thickness slider (1-4px,
precomputed Chebyshev stamp sets); Glow Intensity slider (0.2-2.0, 0.5 halo cap); gentle glow now the
fresh default (ON at 0.75 — Peetsa's config keeps its explicit off until toggled/Reset, disclosed);
loading-screen summary line = gentle circular word-illumination wave (1.5s/element, 30% lift, seamless
wrap, reduceMotion=static, 9 tests). Suite 249/249. TEST 66 staged next.

### Glimmer visible on white + loading Sunset/wave (2026-07-11, bc352dbf/1f4d7cb0)

TWO more near-white-headroom fixes (this bug keeps recurring in two forms — brighten-toward-white is
blind to near-white fills, brighten-multiply was blind to saturated fills). (1) TITLE glimmer was
invisible on the OFF_WHITE default → replaced brighten-toward-white with a SHINE-SWEEP (dim baseline
0.75 + bright white-pop crest 0.70), proven exact no-op when not glimmering so no resting dim; dead
GLIMMER_AMPLITUDE/glimmerBoost removed. (2) LOADING screen: compass recolored to Sunset (amber/coral/
plum, sourced from CompassHud DialColors) + 8-point Rose star; the word-illumination wave had the SAME
invisible-on-warm-white bug → LoadingWave.shade (rest-dim 0.75 + gentle pop 0.40, separators dimmed to
match) so words visibly rest-dim and light up in turn. LESSON (write to LESSONS): "brightness effect
invisible on X-colored fills" is now a KNOWN recurring trap — any brighten-toward-a-target effect is
blind to fills already near that target; the fix is always relative contrast (dim the surroundings),
never a bigger amplitude. Suite 259/259. TEST 67 staged. NOTE: brighten() now has no live caller (kept
as a tested primitive).

### Loading needle legibility + faded-shadow/spacing defaults (2026-07-11, 2868b9a8/4e5c7eba)

Peetsa flight items: (1) loading compass needle was lost against the amber rose (amber tail+dot = same
amber as the star) → tail off-white, dot plum, rose star muted rose-brown so the coral needle dominates.
(2) default depth = FADED DROP SHADOW (directional down-right low-alpha stamps, distinct from the
omnidirectional glow and the hard vanilla shadow; fill shadow-arg hard-false = no double-shadow) instead
of the glow halo; glow off by default (still available). (3) default letter spacing 0→1. All four
default sites updated; sweep caught + fixed two: Reset Title now resets letter spacing AND its
scale/seconds corrected 1.6/4.0→1.8/6.0 (never matched the real defaults — pre-existing bug). Existing
configs keep their explicit values (fresh/Reset only). Suite 260/260. TEST 68 staged.

### Title flicker root-caused: tick-clock animation (2026-07-11, 9db7aeac) + art-direction review parked

Peetsa: title "glitching out — delay, pop in, disappear, reappear" + glimmer "like a rubberband." ONE
disease: visual animation on getGameTime(), which freezes during chunk-gen stalls (exactly when titles
fire) and REGRESSES on integrated servers (client time snapped to authoritative) — frozen alpha, jump-
pops, non-monotonic vanish/reappear; glimmer crest = 14 integer-tick hops. FIX: whole title family
(zone/hemisphere/whisper) now animates on wall-clock ms captured at trigger (durations byte-identical,
ticks x 50); plus a third real pop found en route (baseline dim snapping 0.75->1.0 at sweep end) eased
with a sin(pi x progress) arch — sweeper proved first/last letters get identical shine. LESSON: never
drive per-frame visuals from the tick clock; wall-clock is the family idiom (LoadingWave was already
right). PARKED FOR PEETSA: art-direction review (title-art-direction-review-20260711.md, committed
8888abd8) — headline finding CONFLICTS with his outline removal (off-white on snow = 1.11:1, invisible;
review wants the 1px outline back ON by default); + gold hairline rule, two-line lockup, eased motion,
per-zone accent recs. His call pending. Suite 263/263. TEST 69 staged.

### APPROVED title moment shipped: C v2 glimmer + lockup (2026-07-11, 6c305cb7)

NEW WORKFLOW PRECEDENT — mock-first approval: built an interactive HTML artifact (title-glimmer-mock,
claude.ai artifact 2091b90e) with the current shine + 3 candidate choreographies on 4 backdrops; Peetsa
picked C, gave 3 notes (no pre-flicks, slower/more dramatic, melt-don't-snap); round-2 mock (C v2) at
the same URL; approved "It's beautiful!" — THEN implemented the approved ms values 1:1 as a contract
(sweep verified every boundary independently). Choreography: appear 350ms → hero sweep 900ms (pop .85,
sin-arch dim) → bloom .65 + 2% swell 250ms → cubic melt 850ms → rest. Plus creative-director rec D:
two-line lockup (name full-scale, degrees .55x beneath, split only on a regex-anchored degrees token —
hemisphere titles unsplittable). Swell folded into scale pre-clamp; plate/hitbox/clamp share one
measure(). Bounce feel fix same round (2868b9a8-adjacent: sub-pixel glide + 0.95s period, TEST 70).
Mock-first + approved-values-as-contract worked extremely well — reuse for future signature moments.
Suite 266/266. TEST 71 staged.

### Five-crew round: box KILLED, copacetic sweep, CD copy pass, understudy debut (2026-07-11, 29a37881..230a0a4c)

Peetsa's four asks ran as four parallel crews + fixes. (1) THE OCCLUDING BOX (third report) finally
root-caused: NOT a fill — the Rules rail's scroll-clip started at panelTop+36 while the heading ends at
+18, leaving an 18px invisible shelf that erased scrolling content; the earlier heading fix rewired the
OTHER two panels but the rail kept its own +36 constant, and investigation #1 wrongly blamed the tooltip.
LESSON: "no such box exists" conclusions require enumerating every clip rect, not just fills. (2)
Copacetic sweep: SHIP-SAFE; 3 default-drift LOWs fixed (Reset Title offsetY/-40 + color/case/rgb; preset
outline/caseMode stale) + a 4th found in cross-check. (3) CD loading-copy verdicts implemented: phrases
52->54 (14 cut, 6 rewrites, +8 navigation, +8 GEOLOGY lines per Peetsa: rain shadows/windward/island
arcs/faults), featured tail 34; whisper offset 34->42 (CD's delegated final call). (4) CREATIVE
UNDERSTUDY ROLE DEBUT (locked into the workflow, .claude/agents/creative-understudy.md): 25 pitches ->
director triage -> mechanical fixes shipped (zone-word unification onto LatitudeBands.
displayNameForZoneKey, x->×, failsafe tone, EW-haze log gate); PITCHED TO PEETSA (pending): SpawnZoneScreen
restyle SWING, vignette-synced warnings, Ginormous flavor symmetry, World Type tooltip; zone-copy
consolidation pass noted (SpawnZoneScreen range literals duplicated, marked for it). Suite 266/266.
TEST 72 staged.

### Round close: fossil deleted, vignette shipped, glyph home (2026-07-11, bd3a7f68/343a69b4/fbdf3a53)

Peetsa's dead-code hunch CONFIRMED: SpawnZoneScreen was a 1.1.x fossil (opener flag never set true,
trigger payload never sent, join handler comments the legacy path retired) — DELETED with its client
plumbing; orphaned senderless c2s payload + server receiver assessed LOW (idempotent/tag-locked/
self-only) and earmarked for the dead-code cleanup slice. World Type stepper got its newcomer tooltip.
Vignette pulse (understudy SWING, approved): DANGER/LETHAL warnings now punctuate the atmosphere —
edge-dark pulse synced to the text (0.25/0.40 peaks, lethal 0.08 honest linger), wall-clock,
reduceMotion static, F1-hides with the text, whiteout→vignette→text order verified. GlyphDraw = the
one home for the scaled-glyph trick (3 prior reinventions cited; pure code-motion, pixel-verified).
Ginormous keeps its "!" (director design verdict: deliberate crescendo). Suite 272/272. TEST 73 staged.

### TEST 75 feedback round: smear fix, honest cold timing, the cap goes bare (2026-07-12, 5ec0d475/6678d367/35ba69e8)

Wind bed LOCKED IN (Peetsa: "epic"). Three fixes off his polar flight: (1) warning SMEAR root-caused --
the fill's vanilla drop shadow stacked on the keyline (9th dark stamp, 1px off); fill is shadow-free
under a keyline now, and ALL FOUR tiers are keylined (white 85/87 lines were illegible on snow). Copy:
89.7 -> "Hypothermia sets in." (qualifier dropped), 87 -> "Blizzard conditions worsening. Turn back
while you can." (honesty: slowness moved). (2) COLD TIMING: hazard onset 87 -> 88.5, severe AT 90
(freeze crosses damage threshold ~89.96); ambient untouched (snow/wind 85, storm sky 85-87.5) and
blizzardDrive decoupled onto its own 87 ramp so the storm still LOOKS like it builds before it bites;
warning-ladder degrees unmoved (EW KEEP-SHARED). (3) FOLIAGE at 88 deg (flowers/sugarcane/FIREFLY
BUSHES) = the polar vegetation fade shipped default-OFF; vanilla 26.2 decorates snowy_plains/frozen_river
themselves, so the fade is mandatory for a bare cap. Default flipped TRUE (both sites), ONSET/FULL now
-D-tunable (onsetDeg/fullDeg). Sweeper ACCEPT-WITH-NOTES, 5 LOW all resolved in-pass (2 stale comments,
1 test-default coupling, doc reconcile = this entry; informational: new chunks in old saves get a soft
fray-noise seam at the cap -- intended). Suite 295/295. TEST 76 staged.

### The pole gets its voice + the loading screen its nameplate (2026-07-11, d5b40d29/d7444a02)

Peetsa approved the full CD slate ("Sound yes... yes on all of your other recs") + his warning register.
WIND BED shipped (highest-impact CD rec): vanilla wind-rush loop, Weather slider, 0.8*((|lat|-85)/5)^2
envelope (whisper 87 -> howl 90), 0.5-deg hysteresis, single-instance lifecycle sweeper-walked, re-arm
cooldown for the muted-channel churn nit. WARNINGS in Peetsa's blizzard-conditions voice (DANGER = his
line verbatim); near-black keyline on the two red lines (whiteout washout fixed); LETHAL differentiated
(1.15x + ember warmth pulse in the vignette, DANGER stays cold). LOADING SCREEN v2: shared
LatitudeWordmark helper (create screen verbatim-extracted, unified brand gold 0xE8B64A wordmark-only),
loading screen draws the real wordmark 1.4x/2 sparkles, specs line gilded kind-gold/size-white through
the wave, F9 hint to the mechanics zone, gold-tinted graticule. OPEN for Peetsa: screen-wide gold
convergence; gild the size word? Suite 291/291. TEST 75 staged.

## B-4 polish round 2 (2026-07-10; dev+sweeper green; committed, HELD unpushed; TEST 52)

Peetsa's second live round, five fixes: (1) per-SIDE hemisphere titles — each hemisphere gets its FULL
title once per visit-episode, repeats SMALL, leave-band resets (his design, adopted). (2) E/W title
never fired — ROOT CAUSE: the teleport guard measured steps against the HELD dead-zone reference, so
walked meridian crossings on Wide 2:1 (1 deg ~111 blocks) read as >256-block teleports and were
suppressed; fixed with a separate RAW per-sample guard reference + regression test documenting the old
bug. (3) Pole warnings now EPISODIC: each tier (85/87/89/89.7) fires once per incursion, ~10 s display
with fade in/out, all tiers re-arm only below 84 deg (pure PolarWarningEpisode + 7 tests; persistent
draw removed; pole episode owns the line over EW storm during its window — intentional). (4) STORM SKY:
client-only mixin lifts ClientLevel rain level with the 85-90 ambient ramp — vanilla itself greys the
sky, hides sun/moon, renders REAL snowfall; registered client-only, instanceof guard makes server
leakage structurally impossible (sweeper-verified; note: storm AMBIENT SOUND also comes free — Peetsa
to judge by ear). (5) Snow mystery SOLVED WITH EVIDENCE: counts were always correct (33/64/80 at
87/89/90) — pure visibility (white flakes vs white fog); vanilla snowfall now carries density,
SNOW_MAX_COUNT 80->30 (subtle texture), evidence log behind -Dlatitude.debugPolarSnow. Suite 141/141.
Sweeper: ACCEPT-WITH-NOTES zero defects (dead computeWarningState flagged for a cleanup slice).

## World-shape decisions (2026-07-10, Peetsa)

- RENAME agreed: player-facing shape names become **"Square 1:1"** and **"Wide 2:1"** (drop "Mercator";
  "Legacy" rejected as pejorative). Small UI/label pass, to be scheduled (create screen + anywhere the
  shape is named to players). Internal code/flag names unchanged.
- **1.5:1 third shape REJECTED for now** — a third aspect multiplies every edge/pole/atlas test surface;
  revisit only if Wide still feels sprawly AFTER Phase 5B lands.
- **Phase 5B "Moisture-Driven Biome Selection (Longitude Matters)" QUEUED** in the overhaul roadmap
  (before Phase 6): the real answer to "is 2:1 too wide" is making longitude meaningful, not shrinking
  the world. Emergent per-seed hemisphere personalities via the existing Fetch & Lift fields; never
  scripted. Prereq (consumer law-compliance) already complete.

## TEST 49 staged + B-5 worktree opened (2026-07-10)

TEST 49.jar staged (SHA 868dee21...) = all three B-4 fix packages (title+pole feel e2d915fa, ocean labels
09c1e40a, zone-title anti-spam b6c829d7) for Peetsa's fly-test. All HELD UNPUSHED pending approval.
B-5 (Hemisphere Passage) work moved to a SEPARATE worktree `/Users/joolmac/CascadeProjects/Latitude-b5-
hemisphere-passage` on branch `phase5-b5-hemisphere-passage` (off b6c829d7), so title-drama adjustments
from testing can land on port/canonical-26.2-pivot without B-5 WIP in the way. Recon in flight there.

## B-4 LIVE LOOK — Peetsa's verdict (2026-07-09, TEST 48)

**GOOD:** generation with both new flags OFF "looks awesome" (live byte-identity confirmed by eyeball).
**FLAGS-ON looked "really cursed"** — consistent with the recorded open finding (the floor-sighted
relabel over-floods the interior; calibration required before that flag can ever default on).
**Punch list (Peetsa's order):**
1. Village in ocean (cursed placement).
2. Pole approach: fog ramps then WHAM full blindness with no warning — wants smooth increasing fog only;
   snow-particle increase NOT visible; sky stays sunny/blue — wants stormy look.
3. E/W borders still storm-warnings — wants the Hemisphere Passage (B-5) BUILT (green-lit now).
4. Ocean depth status question (answered: base carve live, C-4 trenches designed-not-built).
5. Dripstone caverns inside underwater trenches — origin?
6. Wrong biomes in ocean: river, plains, etc.
Title UX request: hemisphere titles must not spam when straddling a line — full title ONCE; while within
3° of the line, re-crossings show only a small unobtrusive message; big title re-arms after leaving the
3° band. Videos to be frame-dissected. Items 1/5/6 = one investigation (flooded-land label family +
carve-depth interactions). Items 2 + title UX = client polish pass. Item 3 = B-5 build.
## B-4 polish pass 1 (2026-07-09) — title anti-spam + pole feel (dev+sweeper+fix-up green; committed, HELD unpushed)

Peetsa's live-feedback fixes. (A) TITLE ANTI-SPAM: HemisphereCrossing gains evaluateBanded (FULL once per
approach; re-cross within a 3-deg band → SMALL action-bar message via gui.hud.setOverlayMessage; FULL
re-arms only after leaving the band; bands latitudeRadius/30 and halfSize/60, FLOORED at dead-zone+32
after the sweeper found xsmall-Classic bandX 62.5 < deadzone 64 made SMALL unreachable). +3 tests.
(B) POLE FEEL: Blindness effect REMOVED entirely (ramping whiteout carries vision loss); whiteout alpha
curve intensity^2 → pow(intensity,0.65) (visible from ~85.5 deg, no WHAM); two-color storm lerp
grey-blue(92,108,132)→white (dims the sunny sky); snow budget 30→80, envelope 16, wind drift ±0.09 —
fixed budget + isPaused guard untouched (anti-backlog law). Suite 120/120. Sweeper: ACCEPT-WITH-NOTES
(R1 fixed same pass; R2 fast-crossing re-arm = by-design, cooldown-throttled; fog exponent + storm color
are eyeball picks pending Peetsa). REVIEWER SKIPPED this pass (session budget) — sweeper + fix-up
verified; flag for a catch-up docs-vs-actions review next session. HELD UNPUSHED pending Peetsa approval.

## B-4 polish pass 3 — zone-title anti-spam (2026-07-10; dev+7-tests+sweeper green; committed, HELD unpushed)

Peetsa: apply the hemisphere band anti-spam to the climate-ZONE titles too, so building/walking a band
edge (temperate↔subtropical etc.) doesn't spam the full title. `canonicalTitleZoneKey` is a PURE climate
band (|lat|-derived, boundaries 23.5/35/50/66.5°, no hemisphere/longitude). New pure
`core/ZoneTitleBanding.java` (Fire{NONE,FULL,SMALL} + evaluate + nearestBoundaryDistanceDeg); GlobeWarningOverlay
gains a single `zoneFullArmed` flag (latitude axis only): FULL on a genuine new-zone entry ONCE, SMALL
action-bar note on re-cross while within 3° of the boundary, FULL re-arms once settled >3° deep. Reuses
the hemisphere action-bar (renamed showActionBarMessage — hemisphere behavior byte-identical). Coarse
throttle + zoneEnterTitleEnabled gate kept. New ZoneTitleBandingTest 7/7; suite 134/134 (verified by
director directly, not just agent claim). NOTE — a duplicate developer collision occurred (a stalled
resume actually completed while a fresh dev was launched); resolved cleanly (only one version on disk,
the fresh dup killed before writing). Sweeper: **ACCEPT-WITH-NOTES, zero defects** — the feared
"re-arm never reachable at small radius" bug does NOT occur (3° band = latRadius/30 = 125 blocks at
xsmall, dominates the 96 floor; reachable inside the narrowest zone subtropical 11.5°); legitimate
first-entries preserved (FULL cannot fire twice / SMALL cannot fire without a real zone change). Cosmetic:
small message uses natural case (matches the pre-existing hemisphere small-message behavior). HELD UNPUSHED.

## B-4 polish pass 2 — carve-aware ocean labels (2026-07-09; dev+6-tests+sweeper green; committed, HELD unpushed)

Implements ocean-label-investigation-20260709.md behind flag `latitude.terrainV2.carveAwareLabels`
(default off): public static carveTargetYOrMax (pure, thread-safe, +Infinity fallback = structurally
inert without an active carve); label hook in BOTH pick() twins after all vetoes, NOT behind terrain
inputs (fires in the input-less SOURCE path = the village-eligibility fix); rivers convert when sunk
(IS_RIVER && !carveAwareOcean); cave-clamp measures min(estimate, carveTarget) (trench dripstone gets
clamped); StructureBiomeMatchGuardMixin cancels land-only structures over carved sea (belt+suspenders).
New CarveAwareLabelsTest 6/6 + flag default test; suite 127/127. Sweeper: **ACCEPT-WITH-NOTES, zero
defects** (flag-off byte-identity airtight incl. live config; twins identical; SOURCE-path reachability
proven — zero returns before the hook; union semantics with B-2/C-2 vetoes sane in all 8 flag combos;
cave-clamp min() self-defusing at coasts). Noted R1/R2: structure guard rides terrainBiasActivelyBiasing
only (consistent w/ existing pattern) and tests start-chunk center only (cancel-only belt+suspenders;
pick()-side fix is primary). GATE BEFORE ANY DEFAULT FLIP: terrain-aware atlas flag-on run — village
eligibility over carved sea spot-check + land fraction ≈ geo intent (~39%).

## B-4 video evidence (2026-07-09)

Peetsa's 13:55 recording frame-dissected (167 frames, 19 curated + manifest in session scratchpad
b4curated/). Confirms: village-on-water at 7N 9W (3 frames); polar warnings firing at the new anchors;
no title spam observed at the 0,0 crossing in-video (anti-spam shipped anyway per Peetsa's concern).
TWO NEW ANOMALIES for next docket: (i) f0035 ~2:55 — bright ORANGE terrain patch at 83N among ice
spikes (wrong-colored terrain, undiagnosed); (ii) f0165 ~13:45 — deep RED/PINK sky near 180E (possibly
sunset/time-command artifact — commands visible in toasts — needs a second look before calling it a bug).

## B-2 runtime gates (2026-07-09 — RESOLVED, push authorized ba9b1099)

- **Gate 1 GREEN**: flag-off plain atlas @ `94bed4ac` (run `20260709-123627`) = byte-identical to A′
  `20260709-113845` (0/110,215). Both new flags are provably inert off.
- **Gate 2 INCONCLUSIVE — confounded, honestly recorded**: flag-ON terrain-aware run `20260709-124004`
  (37 min) nominally hits the X-edge target (edge land 46%→0.7%; frayed latitude-correct oceans, frozen
  at poles, warm at equator; pole edge = frozen ocean at the rim) BUT the diff vs the PLAIN flag-off run
  changed 54.6% of the whole map and interior land collapsed to 8.9% — THREE variables changed at once
  (atlasTerrainAware sampler mode + boundaryV2 + floorSightedVeto), so the mass conversion cannot be
  attributed. Hypotheses: (a) terrain-aware mode alone activates the C-2 mirror veto atlas-wide (its
  designed behavior finally visible), (b) the OCEAN_FLOOR_WG estimator over-fires under the wrapper
  (L24 class), (c) fix-2's effect. **Run E in flight**: terrain-aware + BOTH new flags OFF — E vs D
  isolates the two flags; E vs C isolates the sampler mode. If E already shows the interior collapse,
  the collapse is pre-existing C-2-veto/estimator behavior surfaced by the atlas mode, NOT this slice's
  regression — but then the flag-attribution diff (D vs E) must still show fix-1 confined to the edge
  band and fix-2's conversions floor-justified. NO PUSH until attribution is clean.
- **Gate 2 RESOLVED (runs C=20260709-123627, D=20260709-124004, E=20260709-131833)**: E vs C (flags off
  both, sampler mode isolated) = 54.54% changed -> the mass conversion is ENTIRELY pre-existing C-2
  mirror-veto behavior surfaced by terrain-aware sampling, NOT this slice. D vs E (flags isolated,
  terrain-aware both) = **22 cells (0.020%)**, all land->latitude-correct ocean at 35-66.5 deg (the edge
  band's residual after the veto already converted most flooded edge columns). Edge targets met in the
  terrain-aware view (X-edge land 46%->0.7%, frayed frozen/warm oceans by latitude, poles' rim frozen
  ocean). ATTRIBUTION CLEAN -> push authorized (all new behavior flag-gated + inert off, gate 1
  byte-identity green).
- **OPEN FINDING (pre-existing, surfaced today, gates any floorSightedVeto default-flip)**: in
  terrain-aware view the C-2 veto + OCEAN_FLOOR_WG converts the interior to ~77% ocean vs GeoAuthority's
  ~39%-land intent -- the floor estimator plausibly over-reads the carve (L24 class). Fix-2 stays
  default-off until this is calibrated (a future slice: compare OCEAN_FLOOR_WG vs the density ladder on
  carved columns). NOT a B-2 regression: D-vs-E proves our flags did not cause it.
- NOTE: fix-2's live (skipPreview) branch is NOT exercised by the terrain-aware atlas (which uses the
  !skipPreview arm) -- its live behavior is code-identical to the proven previewFloorHeight source, but
  its MAP-WIDE live effect (~the E-vs-C picture on new chunks) is exactly why it has its own flag and a
  B-4 live decision.
Authorized by Peetsa 2026-07-09 ("Proceed with Phase 5 using workflow"). Objective per
`docs/LATITUDE_2_0_OVERHAUL.md` §Phase 5: make the world edge intentional and less wall-like WITHOUT
claiming seamless topology — bias the projection edge toward ocean/ice/storm geography, adjust warning
language/visuals, live visual proof after atlas green. Prerequisite satisfied: consumer law-compliance
slice complete (`9f21b3cb`). E-W wrapping is SCRAPPED history (2026-06-24, fully reverted; carry only its
honest-framing rule: no false wrap promise). Hard stops in force, especially: edge fixes go through
SHARED AUTHORITY logic, never one-off biome clamps; flag-off output must stay byte-identical.

Tiered workflow per AGENTS.md (architect = main loop, never codes; Developer Opus; Test-writer Sonnet;
Sweeper Opus adversarial per pass; Reviewer read-only docs-vs-actions). This doc is the shared RUN LOG.

## Current truth (recon + measurement, 2026-07-09)

**Measured edge composition (A′ atlas, live config, R7500 Mercator):**
- Z edges (poles): ALREADY intentional — 67% ice/snow land + 25% frozen ocean. Little to do.
- X edges (east/west): NOT intentional — 46-54% ordinary land (savanna 18-22%, jungle, taiga),
  statistically identical to the interior (45-47%); ocean share at the edge (32%) ≈ interior (30%).
  A player flying east hits savanna guillotined by the world border.

**Code truth (recon):**
- `GeoAuthority.contEdgedAt` ALREADY suppresses continentality at edges: `-EDGE_STR(1.30)·smoothstep(0.80,1,|x|/xR)`
  and `-POLE_STR(0.75)·smoothstep(0.92,1,|z|/zR)` — always-on under geoV2, NOT latitude-aware (same ocean
  push at every latitude; no ice/storm variant). `GeoSummary.projectionEdgeSuitability01` is computed per
  column and CONSUMED BY NOTHING — a ready-made hook.
- **THE CENTRAL PUZZLE (B-0 must answer before any design):** the authority's edge-ocean push does NOT
  reach the visible map — despite land01≈0 in the outer band, the biome layer still paints ~50% land
  there. Suspects: the pre-2.0 biome machinery (OceanDistanceField/provinces) doesn't consult geoV2;
  the C-2 waterline/mirror vetoes may be excluded or blind in the edge band; terrain carve behavior at
  the extreme edge. Diagnose with existing atlas artifacts (continentalness/height/biome PNGs) — no new
  runs needed for diagnosis.
- Physical edge: X = real vanilla WorldBorder at xRadius·2 (Mercator: sized to the WIDE axis — the square
  border sits far outside the Z extent). Z = NO hard stop; escalating hazard effects
  (IMPAIR→HOSTILE→WHITEOUT→LETHAL) from `POLAR_START_FRAC·zRadius`; PolarCapScrubber exists but disabled.
- Boundary UX inventory: LIVE = EwSandstormOverlayHud (screen haze, climate-aware tan/whiteout),
  GlobeWarningOverlay (escalating text, climate-aware), warning particles, zone titles. DEAD = the 3D
  EwStormWallRenderer (26.1-era mixin never registered on 26.2; render call commented out).
- Two overlapping spawn-safety nets exist (C-2's 0.80·xR clamp + older clampSpawnAwayFromEwWarning) —
  don't add a third.

## Slices

| Slice | Who | Content | Gate |
|---|---|---|---|
| B-0 | Architect | Edge-truth diagnosis: WHY doesn't the authority's edge ocean reach the visible map? Distance-graded X/Z edge bins incl. ice category; land01-vs-height-vs-biome cross-check from existing artifacts | Written mechanism diagnosis in this doc |
| B-1 | Architect designs, Sweeper adversarially reviews | Design: latitude-aware EDGE INTENT through shared authority (warm/temperate → ocean, polar → ice shelf; storm = UX layer, not biome), consuming/extending the existing edge terms + the unused projectionEdgeSuitability01 hook; NEW FLAG (house LatitudeV2Flags pattern) so flag-off is byte-identical incl. the current live config | Design section approved in this doc |
| B-2 | Developer(Opus) → Test-writer(Sonnet) → Sweeper(Opus) → Reviewer | Implement geography leg | compile+suite; flag-off atlas byte-identity; flag-on edge acceptance (targets set in B-1 from B-0 numbers); commit |
| B-3 | Developer(Opus) → Sweeper → Reviewer | UX leg: (a) tighter continuous POLE hazard window (Peetsa: 87-90°, smooth slowness+freeze ramp, not stair-steps); (b) warning-language pass; (c) HEMISPHERE TITLES — E/W title at the prime meridian + keep N/S equator title + the 0°,0° non-overlap rule (spec below); (d) decide/revive the 3D storm wall on 26.2 (client-only) | compile+suite; commit; live eyeball queued |
| B-4 | Peetsa live | Fly the east edge (equator/temperate/pole) + cross the equator and the prime meridian on a fresh flag-on world; judge the boundary experience, hazard window, hemisphere titles, storm/haze visuals | Written live verdict; only then default-flip discussion |
| B-5 (proposed) | Architect design → sweeper → dev/test/sweeper/review | HEMISPHERE PASSAGE at the E/W world edge (±180° antimeridian): fog-advisory prompt → consensual blur-through → arrival title. Peetsa's reopening of the E/W crossing — DIFFERENT design from the SCRAPPED seamless-wrap (see note). Builds on the B-2 ocean shore | design approved; then normal impl passes + live |

Push cadence: commit per green pass; push after B-2's atlas gate (geography) and after B-3 (client).

## Peetsa's B-3/B-5 design intents (2026-07-09 — recorded, not yet scheduled to a dev pass)

**Poles — tight continuous hazard window (B-3a).** Onset ~87° (≈0.967·zRadius), full lethal at 90°, so the
2-3° window `[87,90]` is the only HAZARDOUS band and the rest of the polar region stays explorable. Replace
the current stage-STEPPED ladder with CONTINUOUS scaling across the window: slowness amplifier and
freeze-tick rate both ramp smoothly with progress→90° (progress = clamp01((|lat|-87)/3)). Reuses the
existing `LatitudeMath.hazardProgressZ`/`borderUxTick` plumbing (recon item 2/3) — retune constants +
make the effect magnitude a function of progress rather than a stage index. Open Q for B-4: keep the
whiteout screen effect at the deep end, or let freeze damage carry it.

**Poles — persistent snow + fog ramp (B-3b), Peetsa's request, with the anti-backlog guardrail.** Snow
begins ~85° (BEFORE the 87° hazard onset — atmosphere first, then danger) and ramps particle density up
to VERY heavy by 90°, with increasing fog toward the edge (share the existing `computePoleWhiteoutFactor`
screen-fog path). Extend the EXISTING `GlobeModClient.polarCapClientTick` (which is already built the
right way: `END_CLIENT_TICK`-driven, a CAPPED per-tick count, NO time accumulator / NO
`System.currentTimeMillis`). Move the onset out to ~85° and make the per-tick count a smooth function of a
85→90° progress ramp (uncap the current `count>6` clamp to a higher heavy-snow ceiling near 90°, still a
FIXED per-tick budget).
- **THE BUG PEETSA HIT BEFORE ("particles store up while paused, then dump the whole backlog on
  resume"):** two known anti-patterns cause it — (a) spawning off wall-clock time so resume fires a
  catch-up burst; (b) spawning WHILE PAUSED (the client tick keeps firing but the particle engine doesn't
  step them, so they pile at the spawn point and all animate on unpause). The current code already avoids
  (a). **HARD REQUIREMENT for B-3b: add an explicit `if (client.isPaused()) return;` guard at the top of
  the spawn tick** to kill (b), and keep the fixed-budget-per-tick / no-accumulator rule. Paused → spawns
  nothing → resumes clean; the few flakes already airborne just freeze/resume (not a dump). Never
  reintroduce a wall-clock or "how many do I owe since last spawn" accumulator for particle cadence.

**Polar warning ladder re-anchor (B-3-P3, 2026-07-09).** The four polar warning MESSAGES + their trigger
thresholds still sit on the PRE-B-3 stage ladder (`LatitudeMath.POLAR_STAGE_{1,2,3,LETHAL}_PROGRESS =
0.940/0.970/0.990/0.995` → 84.6°/87.3°/89.1°/89.55°), which no longer aligns with P1's new window
(snow 85° / hazard 87° / blindness ~89° / lethal 90°). Peetsa: refine the messages. Re-anchor the 4 stage
constants to the new milestones (progress = deg/90) and refresh the wording, esp. the dishonest LETHAL line
("The cold overwhelms you." fires at 89.55° while death is at 90°):
- WARN_1 → **85°** (0.9444), coincides with snow onset → an atmospheric "the snow begins / cold sets in"
  line (this IS the snow-onset message Peetsa asked for; no 5th tier needed).
- WARN_2 → **87°** (0.9667), coincides with the hazard onset (slowness begins) → "the cold seeps in,
  movement slowing".
- DANGER → **89°** (0.9889), ~blindness onset → the actionable red "lethal cold ahead, turn back NOW"
  (keeps ~1° lead before real freeze damage at 90°).
- LETHAL → **~89.7°** (0.9967) → present-continuous, honest "the cold is freezing you" (fires only when
  freeze ticks are genuinely near-max, not prematurely final).
These constants also drive warning-PARTICLE intensity (`polarIntensityForStage`) — re-anchoring aligns the
whole warning system (text + particles) to the new window, desirable. Hazard EFFECTS are P1's
`PolarHazardWindow` (independent, untouched). EW warnings out of scope (already refined) — DESIGN-TIME ASSUMPTION, CORRECTED by the B-3-P3 pass log above: the shared stage constants DID nudge EW ≤0.44% of X-radius (accepted, KEEP-SHARED). Client-only, no
worldgen, no thresholds on the hazard mechanics themselves — only the WARNING display ladder.

**Hemisphere titles (B-3c).** Geography: longitude runs -180..+180; **two** meridians flip the E/W
hemisphere — the **prime meridian (0°, interior)** and the **antimeridian (±180°, the world edge)**.
Requirements:
- KEEP the existing N/S hemisphere title at the **equator (lat 0)** crossing — verify it still fires
  (recon: GlobeWarningOverlay drives hemisphere-crossing titles today; confirm the N/S one is live).
- ADD an E/W hemisphere title at the **prime-meridian (lon 0)** crossing during normal interior play —
  announces the hemisphere being ENTERED ("EASTERN HEMISPHERE" / "WESTERN HEMISPHERE"). The Longitude
  readout already flips sign at lon 0, so this is a clean trigger.
- **0°,0° NON-OVERLAP RULE (hard requirement):** crossing the equator and the prime meridian on the same
  tick (or within the same title display window) must NOT render two single-line titles on top of each
  other. Architect ruling for the dev: a SINGLE shared hemisphere-title channel — if both crossings are
  active within the window, render ONE stacked two-line title (e.g. "NORTHERN HEMISPHERE" /
  "EASTERN HEMISPHERE"); otherwise an independent single-line title. De-dupe so the intersection can never
  double-fire. This is distinct from the zone-enter title (which has its own draggable placement in HUD
  Studio) — hemisphere titles are their own channel; make sure they don't collide with the zone title
  either.
- The B-5 passage's ARRIVAL title (at the ±180 edge) is the SAME E/W hemisphere concept — share the
  channel/renderer, just a different trigger (edge passage vs interior meridian).

**B-5 Hemisphere Passage (E/W edge crossing) — proposed slice.** Player nears the ±180 world edge →
increasingly opaque fog → "Heavy fog advisory — pass through?" yes/no prompt. NO: gentle turn + soft
push-back current away from the border (NOT a hard camera yank — feels janky). YES: blur/opaque-fog
transition (which also masks destination-chunk preload) → teleport to the mirrored X → fog thins →
"WESTERN/EASTERN HEMISPHERE" arrival title. Anti-spam: band-state HYSTERESIS — the prompt arms once on
ENTERING the fog band and cannot re-arm until the player has left it (>~200 blocks inland), so sliding
N/S along the edge never re-prompts. **NOT a retry of the SCRAPPED seamless wrap** (2026-06-24): that
attempt tried to make the crossing INVISIBLE and died on cloud-snap/ocean-mismatch/fake-carve at the
seam; this design makes the crossing EXPLICIT and CONSENSUAL (framed, not denied), and the B-2 boundary
ocean gives both shores the open water the old ocean-seam plan never had. Feasibility/UX design is its
own slice AFTER B-3/B-4 (judge the new edge live first). Recommended sequence: B-3 → B-4 → B-5.

## Open decisions (Peetsa's, queued for the B-4 live session)
1. Flip `biomeConsumerV2` default-ON? (Its law gate is cleared; flip is a Phase-5-era decision, separable
   from the boundary work. Recommend eyeballing consumer-on in the same B-4 world.)
2. Storm wall 3D: revive vs retire-for-good (haze+warnings may be enough — B-3 presents both options).
3. Z-edge hard stop: today a determined player can tank the debuffs and walk past the pole — accept
   (soft lore boundary) or harden? (Peetsa leans toward the tight 87-90° hazard window as the answer —
   see B-3a; a hard wall may be unnecessary if the freeze window is punishing enough.)
4. B-5 Hemisphere Passage: build it (explicit fog-gated E/W crossing) — Peetsa wants it; schedule after
   the B-4 live look confirms the new edge feels right to build a passage onto.

## Pass log (appended as slices complete)

- **B-0 opened (2026-07-09)**: recon complete (edge suppression terms, unused projectionEdgeSuitability01
  hook, dead storm wall, live haze/warnings, physical-edge split X-border/Z-hazard, wrap post-mortem
  reusables=none). Edge composition measured from A′ (numbers above).
- **B-0 DIAGNOSIS COMPLETE (2026-07-09, code-trace with file:line evidence)**: the edge-ocean intent IS
  expressed in 3D but not in biome labels, via a two-mechanism stack:
  1. **Atlas overstates the problem**: biomes.png runs terrain-BLIND by default (`ATLAS_SAMPLER` context,
     null noiseConfig/heightView → the C-2 sunk-land mirror veto's guard never passes). The carve itself
     (`carveCeilYOrInfinity`, gated only on land01/shelf01) DOES fire at the edge — land01≈0 → carve
     target ~Y39 → flooded. **Live, the outer band is already water over a ~Y39 seabed**, not dry savanna.
  2. **Live biome labels are still wrong**: the mirror veto's cheap (`skipPreview`, MIXIN/live) branch
     reads the FLUID-INCLUSIVE `WORLD_SURFACE_WG` (flooded columns read 63 → veto never fires); C-2's own
     floor-sighted fix (`previewFloorHeight`/OCEAN_FLOOR_WG) was wired only into the `!skipPreview`
     harness branch. So live shows ocean water tagged savanna/jungle (wrong identity), and C-2's veto was
     never live-verified (Phase 4 stopped pre-Slice-E).
  Candidates evaluated: (1) consume `projectionEdgeSuitability01` as a flag-gated latitude-aware edge
  ocean authority in pick() — RECOMMENDED (terrain-blind → identical on atlas + live); (2) floor-sight the
  live veto branch — correct C-2 completion, helps everywhere, invisible to the default atlas; (3) global
  BIOME_CONSUMER_V2_OCEAN_AUTHORITY — mis-scoped (known land-collapse to ~13%), rejected.

## B-1 design (architect, 2026-07-09 — under adversarial review)

Two bounded fixes, one flag:

**Fix 1 — edge intent (the Phase-5 core).** New flag `latitude.boundaryV2.enabled` (house LatitudeV2Flags
pattern, default false). In `pick()`, when the flag is on AND geoV2 is live: consume
`GeoSummary.projectionEdgeSuitability01` — when it crosses a threshold, set `oceanAuthority = true` for
the column. The EXISTING latitude-correct ocean-family logic then does the rest (C-2 already proved ocean
family follows latitude: frozen oceans at the poles — the "ice" edge comes free). The boundary must be
FRAYED, not a ring: gate through the same coherent keep-noise pattern the demote gates use (Art VI — no
straight lines), ramping with edgeSuit so ocean share rises smoothly toward the border. Threshold/ramp
chosen so the outer ~0.90·xRadius+ is ocean-dominated while everything with edgeSuit≈0 is untouched.
Shared authority end to end: GeoAuthority signal → pick()'s existing oceanAuthority seam; no biome clamps.

**Fix 2 — floor-sight the live mirror veto (C-2 completion).** In the veto's `skipPreview` branch, replace
the fluid-inclusive `columnDecisionY` height source with an `OCEAN_FLOOR_WG`-based floor estimate (the same
source C-2's `previewFloorHeight` already trusts). No new flag: it completes C-2's documented intent under
C-2's existing gates (`terrainBiasActivelyBiasing`); flag-off (all-V2-off) remains byte-identical because
the veto only runs while biasing. Fixes wrong-identity water everywhere (interior floating-land + edge).

**B-2 acceptance (targets from B-0 numbers, measured on atlas runs with `-Dlatitude.atlasTerrainAware=true`
where fix-2 visibility is needed, plain atlas for fix-1):**
- Flag-off (boundaryV2 off, live-config flags): atlas byte-identical to A′ `20260709-113845`.
- Flag-on: X-edge outer 3 cells land-biome share ≤10% (from 46-48%), ocean+ice ≥85%, frozen-family
  dominating at polar rows; cells with edgeSuit≈0 byte-identical to flag-off (change confined to the band).
- Pure-JVM: new tests for the edge-intent threshold/ramp helper (pure core) + existing suite green.
- Hard stops honored: no clamps, no wrap claims, flag discipline.

### B-1 amendments (sweeper APPROVE-WITH-CHANGES, 2026-07-09 — binding on B-2)

1. **Fix-1 consumes the X-only `edgeB` term, NOT `projectionEdgeSuitability01`** (= max(edgeB, poleB) —
   the poleB component would convert the already-good icy pole LAND shelf into frozen ocean, a regression;
   D1). Expose edgeB as its own GeoSummary field (additive).
2. **Fix-1 gates on `terrainBiasActivelyBiasing()` (same predicate + oceanStrengthRatio check the C-2
   mirror uses) and inserts AFTER the raised-land veto** — otherwise the fluid-inclusive raised-land veto
   clobbers it live (WORLD_SURFACE_WG reads 63 ≥ seaLevel on flooded columns) while the terrain-blind
   atlas shows it working: atlas≠live, the exact failure class this phase exists to kill (D2). With the
   gate, boundaryV2-on + terrainV2-off can never paint ocean labels on dry land. The "plain atlas proves
   fix-1" framing is DROPPED — both fixes are proven with `-Dlatitude.atlasTerrainAware=true` runs (and
   targets re-derived at whatever radius that mode can afford) + the B-4 live look.
3. **Fix-2 gets its own sub-flag** (`latitude.terrainV2.floorSightedVeto`, default false — the
   oceanAuthority sub-flag precedent): it is honestly a MAP-WIDE change to the current live config
   (~30% of sampled columns flip to their C-2-intended ocean identity, with chunk-boundary discontinuities
   in existing worlds), so it must be independently switchable at B-4 (R1). Applied to BOTH pick-path
   mirror copies. AND its premise — that `OCEAN_FLOOR_WG` sees the terrainV2 carve in the live MIXIN
   context — must be empirically confirmed (harness/live), not assumed (R2, L24 class).
4. Noted, accepted, stated: the outer band becoming ocean-dominated IS a (frayed-approach) ocean moat —
   that is the intentional Phase-5 outcome, not an accident (Art X reviewed); edge rivers stay rivers
   (cosmetic, revisit at B-4); edge villages vanish with the land (structures follow biome); spawn is
   already excluded from the band by the C-2 clamp — add no third net.
