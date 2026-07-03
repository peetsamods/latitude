# HUD Studio title styling (RGB/rainbow/case/spacing) + Rainbow Text + polish fixes (2026-07-03)

`status: active` · `scope: rendering` · `date: 2026-07-03` · `evidence id: 20260703-hud-studio-title-rainbow-polish`

Follows directly from `hud-studio-custom-theme-20260703.md` (commit `90a5fbbe`). Three commits on
`port/canonical-26.2-pivot`, all local, not pushed.

## What shipped

### `9000dd49` — Title RGB/preset/rainbow colors + text case + General tab additions

- New `LatitudeConfig` enums `TitleColorPreset` (WHITE/GOLD/RED/CYAN/GREEN/CUSTOM/RAINBOW) and `TitleCaseMode`
  (NORMAL/UPPERCASE/LOWERCASE/MOCKING), plus fields `zoneEnterTitleColorPreset`, `zoneEnterTitleRgb`,
  `zoneEnterTitleCase`. `LatitudeConfig` uses a dual-representation GSON pattern (public static fields the game
  actually reads, private `...Value` fields GSON (de)serializes, manually copied in both `load()` branches and
  `save()`) with **no `fresh()`-style factory** (unlike `CompassHudConfig`) — the new fields were threaded
  through all of that plumbing plus a `sanitize()` null-guard.
- `RainbowText` gained an alpha-aware overload (`drawCentered(..., int alpha)`) so Rainbow-styled titles keep
  their existing fade-in/out; the original 5-arg overload now just delegates with `alpha=0xFF`.
- `ZoneEnterTitleOverlay`: found `render()` (real gameplay) and `renderStaticAt()` (HUD Studio preview) were
  independently duplicated with zero shared code. Refactored both onto one shared `drawStyledTitle`/`applyCase`
  pair. Deleted two confirmed-zero-caller dead overloads (`renderStatic`, the 4-arg `renderStaticAt`) — verified
  via repo-wide grep that only `render()` (from `InGameHudMixin`) and the 6-arg `renderStaticAt()` (from
  `LatitudeHudStudioScreen`) had any callers at all.
- HUD Studio's Title tab gained: Zone Title on/off, Title Duration, Show Degrees on Title (all previously
  reachable only from the separate pause-menu Settings screen), a Title Color preset cycle button, a Custom RGB
  picker group (shown only when preset=CUSTOM), and a Title Case cycle button. General tab gained a Title
  Draggable toggle — `LatitudeConfig.zoneEnterTitleDraggable` already existed and was already read by the drag
  handler, but had zero UI anywhere until this commit.
- Both hardcoded-defaults copies (`LatitudeSettingsScreen.applyDefaults(LatitudeConfig)`,
  `LatitudeHudStudioScreen.resetHudDefaults()`) updated with matching literal values for the new fields.
- **Found, not fixed:** `ZoneEntryNotifier.java` + `ui/ZoneTitleOverlay.java` are a completely unreachable
  parallel title-notification system — `ZoneEntryNotifier.onZoneEntered` has zero callers, and even its own
  target (`ZoneTitleOverlay.show`) leads to a `render()` that also has zero callers. Two config fields
  (`zoneEntryNotifyMode`, `showLatitudeDegrees`) only feed this dead path and are themselves inert despite
  having live-looking Settings-screen widgets. Spun off as background task `task_7003cfac` — not yet resolved
  at time of writing.

### `0600661e` — Title Normal case, letter spacing, tab strip overflow

- **Bug: "Normal" title case was visually identical to "UPPERCASE."** Root cause: the source string was forced
  `.toUpperCase()` *before* `applyCase()` ever ran, in three independent places —
  `LatitudeHudStudioScreen.zoneTitleWord()` (HUD Studio's own preview sample text),
  `GlobeWarningOverlay.buildZoneEnterTitle()` (the real zone-enter title's text), and the hardcoded
  `"NORTHERN HEMISPHERE"`/`"SOUTHERN HEMISPHERE"` string literals (the real hemisphere-crossing title). All
  three were changed to natural case (e.g. "Tropics", "Northern Hemisphere"), leaving `ZoneEnterTitleOverlay
  .applyCase()` as the single place that decides final casing.
- **New: Letter Spacing slider** (-4 to +16px, `LatitudeConfig.zoneEnterTitleLetterSpacing`) on the Title tab.
  Implementing this required replacing `ZoneEnterTitleOverlay`'s single `ctx.centeredText`/`RainbowText
  .drawCentered` call with a new shared `drawSpacedText()` per-character loop, used uniformly for *both* the
  solid-color and Rainbow paths (so there is exactly one styling code path, not a branch that could silently
  diverge). `RainbowText` gained a `paletteColor(int)` accessor (bare `0xRRGGBB`, cycling) so the spaced-draw
  loop could reuse its exact palette/order without duplicating it.
- **Fix: "Placement" tab label overflowed its button's borders.** Widened the HUD Studio sidebar 180→208px and
  added a `TAB_LABEL_SCALE = 0.85f` pose-matrix scale-down for tab-strip label text (combining both fixes Peetsa
  suggested: wider panel + smaller letters).

### `61d51782` — Rainbow Text for compass/zone/biome/coords

- New `CompassHudConfig.textRainbow` boolean toggle (Compass tab, "Rainbow Text") overrides the existing Text
  Color preset/RGB sliders with a per-letter rainbow cycle. Investigation found `CompassHud.java` already
  funnels every zone/biome/coords text draw (analog-attached line + all 3 detached labels: zone, biome, coords)
  through one shared `drawText()` helper — editing that single helper, plus `renderDigitalAt()`'s separate
  per-line loop for digital-mode text, covered every render path with one addition (`drawRainbowLeftAligned()`,
  reusing `RainbowText.paletteColor()`). Preserves the existing Text Opacity fade by extracting the alpha byte
  from `cfg.textArgb()` before overriding the color. Both `applyDefaults(CompassHudConfig)` copies synced.

## Proof (green, every commit)

- `JAVA_HOME=<temurin-25> ./gradlew compileJava` → `BUILD SUCCESSFUL` after each commit.
- `JAVA_HOME=<temurin-25> ./gradlew build` → `BUILD SUCCESSFUL` (full production jar).
- `JAVA_HOME=<temurin-25> ./gradlew runBiomePreview` → headless Atlas `biomes.txt` diffed byte-identical
  against the immediately-prior commit's baseline after each round (only the `durationMs` timing metadata line
  differs) — every change in this arc is pure client-UI/HUD, zero worldgen touch.
- Jars staged incrementally into the Modrinth profile `LATITUDE 26.2` as `TEST 18.jar` → `TEST 19.jar` →
  `TEST 20.jar`, each sha256-verified against the freshly built jar before handoff.

## Residual / next

- `task_7003cfac` (dead `ZoneEntryNotifier`/`ui.ZoneTitleOverlay` chain + 2 inert config fields) — investigation
  spun off, not yet resolved as of this doc.
- No further live-test feedback captured yet for this specific arc at time of writing (in progress).
