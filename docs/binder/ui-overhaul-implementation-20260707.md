# UI Overhaul Implementation — U-A..U-E landed, jar built + HELD (2026-07-07)

`status: IMPLEMENTED — all five approved slices committed; mechanical gates green; the ONE live UI pass
is the remaining gate and belongs to Peetsa. The UI jar is BUILT but deliberately NOT staged into the
Modrinth profile: the parallel worldgen fork's Slice E live session runs on TEST 27 and staging would
replace that jar mid-test.` · design: `../design/hud-layout-overhaul-design-20260707.md` (APPROVED r3) ·
evaluation: `ui-audit-20260707.md` · registry row: `20260707-ui-overhaul-implementation`

## What shipped, by slice (each = one commit on `port/canonical-26.2-pivot`)

| Slice | Commit | One-line |
|---|---|---|
| U-A | `e904d731` | Pin & Grow layout core + non-clipping hotbar dock + fractional-pin migration |
| U-B | `b34b30a6` | Truthful Studio (LONGEST preview text, pin crosshairs, real title hitbox), element-centric tabs, F9→Studio, `LatitudeSettingsScreen` deleted |
| U-C | `764e188e` | Config hygiene: `LatitudeConfigData` pure DTO (versioned, single default source, legacy `...Value` keys via alternates), 10 dead fields pruned, dead `ZoneEntryNotifier`+`ui/ZoneTitleOverlay` deleted, `directionMode` surfaced |
| U-D | `6b11c849` | Five compass looks (DISC/RING/ROSE/TAPE/MINIMAL) in `CompassDialRenderer`, span-batched drawing (~2·d fills vs πr² per-pixel), resource-pack texture override, change-driven HUD string caching, world-switch resets, boss-bar default nudge |
| U-E | `db93644e` | Seed dice/copy + seed-0 guard at launch, presetId+radius launch log + `GlobeWorldSize` trap doc, loading world-summary + truthful gate-bound stage labels + fail-safe chat notice + parchment frame, SpawnZoneScreen restyle, dead classes out (`OverlayProof`, `GlobeModMenu`, `EwSandstormOverlayRenderer`, old planisphere `render()` 450→219 lines, `globe$displayProgress`) |

Both live complaints from the audit are structurally dead, not tuned around: the dial is placed by its
OWN box (text extends away from it; width can never move the pin), and the Studio measures the same
strings the game draws (LONGEST source by default, title hitbox measured via
`ZoneEnterTitleOverlay.styledWidth` on the exact preview string).

## Gates (all green, 2026-07-07)

- **Compile + pure-JVM suite**: 54/54 across all slices (`HudLayoutMathTest` 6 probes incl. pin
  invariance under width sweeps and dock-never-overlaps across screenW 320–3840 × both hands × both
  attack-indicator modes; `LatitudeConfigDataTest` 5 round-trip/sanitize probes; pre-existing geo/climate
  suites untouched).
- **Worldgen identity**: U-A passed a value-level S=0 identity check directly. U-B..U-E hold **by
  construction**: `git diff e904d731..HEAD` touches only `client/`, `core/config/`, `core/ui/`,
  `mixin/client/`, tests, docs, and one test-scope gson dependency in `build.gradle` — zero worldgen
  sources. (Deliberate exception that is creation-input, not generation math: a typed seed parsing to 0
  is substituted at launch, loudly logged — seed 0 is the sentinel that disables GeoAuthority.)
- **Jar**: `build/libs/latitude-2.0-beta.1+26.2.jar`, SHA-256
  `5651ac0a68825f875003707028c2ca504cafec8098b5f1bfc435cb18149c56f2`. **HELD — do not stage until the
  TEST 27 worldgen session concludes.** When staged it becomes TEST 28 per the naming convention.

## Live UI pass checklist (Peetsa, one session, after TEST 27's session closes)

1. **Fresh config**: compass appears below where boss bars stack (the ~15%-down default), analog DISC,
   nothing shifted when a long biome name (e.g. Windswept Gravelly Hills) scrolls under you with biome
   display ON — the original complaint.
2. **Migration**: an existing pre-2.0 config keeps its day-one placement on first load (one-time pixel→
   fraction conversion), then survives GUI-scale changes.
3. **Attach to Hotbar** (both compass styles): docks right of hotbar; toggle left-handed mode and the
   hotbar attack indicator — never clips, steps BESIDE→STACKED→LIFTED as the window narrows.
4. **Studio truthfulness**: what you place with LONGEST preview text is exactly where live text sits;
   pin crosshairs make the pin/grow model visible; title grab box matches the drawn letters at any
   scale/spacing/case.
5. **Looks**: cycle DISC/RING/ROSE/TAPE/MINIMAL at several sizes/themes; TAPE reads correctly while
   turning (heading window ±60°).
6. **F9** opens the Studio directly; Show HUD mode + Warning Messages live in General.
7. **Create screen**: dice rerolls seed, copy copies it, typing seed `0` still creates a working
   Latitude world (log shows the substitution); world-shape/size flow unchanged.
8. **Loading screen**: parchment-framed card, world summary line, stage labels progress truthfully
   ("Shaping the world..." → "Placing you on the map..." → "Laying out the nearby land..." → "Painting
   the horizon..."); spawn-zone picker shows title/colors/tooltips.
9. **World switch**: leave to menu, enter another world — no stale zone title flash, no stale compass
   presence, HUD strings correct immediately.

## Residuals (documented, deliberate)

- Analog dock SHRUNK rung still collapses to STACKED metrics (dial-scale plumbing; revisit if LIFTED
  triggers too often on very narrow screens).
- Studio look thumbnails skipped — the look cycle + full-size live preview covers selection.
- No explicit reduced-motion gate on the loading needle/phrase fades (no vanilla flag to bind to;
  motions are gentle and short).
- Chartroom visual language applied to loading + spawn picker + tokens; the create screen already wore
  the parchment identity (its 06-22 structure is untouched by decision).
- `EwSandstormOverlayHud` (live) retained; only the never-called `EwSandstormOverlayRenderer` deleted.
