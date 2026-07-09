# UI pass round 9 — 3-agent sanity sweep over the uncommitted diff + 7 fixes (2026-07-09)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 40)`
Source: before Peetsa re-tests the whole HUD Studio + create-screen arc by hand, an adversarial sanity
sweep was run over the entire uncommitted client-UI diff (rounds 7 + 8: Tape pin fix, Aurora theme +
speed slider, 8-slot presets + clipboard export/import, independent zone/biome/coords text sizes, and the
Classic-atlas `CLASSIC_ATLAS_SCALE` shrink). Goal: catch what a mechanical build + a by-hand playtest
would miss, so the live pass isn't spent rediscovering things a diff read could have found. All fixes
below are already in the working tree; nothing here needs another code decision from Peetsa.

## Scope isolation (unchanged from rounds 7-8)

Client UI only. The diff touches exactly `client/CompassHud.java`, `client/CompassHudConfig.java`,
`client/LatitudeConfig.java`, `client/LatitudeHudStudioScreen.java`,
`client/CompassHudPreset.java` (new), `client/CompassHudPresetSlots.java` (new), and
`client/create/LatitudeCreateWorldScreen.java`, plus the two binder files. ZERO `world/`, `terrain/`, or
`mixin/` files touched; same worldgen args as TEST 30-39; C-2/C-3 terrain untouched.

## Sweep methodology — three parallel review agents over the same diff

The uncommitted diff was reviewed by three independent agents, each with a different lens, none seeing the
others' findings:

1. **Adversarial UI bug hunt (Opus).** Assume the diff is broken; try to make it crash, NPE, divide by
   zero, corrupt config, mis-render, or drift out of sync with a sibling code path. Highest-severity lens.
2. **Contradiction / duplication / staleness (Sonnet).** Look for a doc/registry/index/comment that now
   disagrees with the code, a superseded value left behind, or two records of the same fact that diverge.
3. **Clean / efficient code review (Sonnet).** Reuse, simplification, dead code, and correctness of the
   kind that isn't a crash but is a latent trap or an avoidable inconsistency.

## What the adversarial hunt POSITIVELY CLEARED (no fix needed)

The Opus hunt found no critical or high-severity crash and explicitly cleared the parts most likely to
bite, so these are on record as checked rather than assumed:

- **Import null-safety.** `CompassHudPreset.fromJson` catches everything and returns null on empty or
  garbage clipboard content; the Import button no-ops (`if (p != null)`) rather than throwing. Pasting
  junk cannot crash the Studio.
- **Aurora divide-by-zero.** Guarded twice: `sanitize()` clamps `rainbowCycleSeconds` to `[10, 40]`, and
  `CompassHud.rainbowColors()` independently floors the period at `1000ms` (`Math.max(1000L, ...)`) before
  dividing. A hand-edited config can't reach a zero-period modulo.
- **Reflection copy (`copyAllInstanceFields`).** Skips statics; there are no transient fields to lose; and
  `sanitize()` runs on the snapshot first, so a reflected copy can't smuggle an out-of-range value in.
- **Gson round-trip of a preset saved before a field existed.** A slot serialized without one of the new
  fields deserializes to the type default, and `sanitize()` normalizes it — degrades to defaults, no throw.
- **All 4 new config fields clamped, ranges matching sliders exactly.** `rainbowCycleSeconds` slider
  10-40 == clamp 10-40; `zone/biome/coordsTextScale` slider 0.5-3.0 == clamp 0.5-3.0. No slider can set a
  value `sanitize()` would then reject.
- **Tape pin offset not double-applied.** The new `(diameter - contentH) / 2` box→content offset in
  `drawPinMarkers()` is applied once, ANALOG-only, and is a no-op for every look but Tape (where
  `contentH == diameter` elsewhere).
- **Tab reindex consistent.** Adding the Presets tab shifted `TAB_GENERAL` 3→4 and set `TAB_PRESETS = 3`;
  every downstream `activeTab ==` comparison uses the constants, so the General tab still renders.
- **Classic atlas scale safe.** `CLASSIC_ATLAS_SCALE` is applied after the shared budget math and the
  result is re-floored (`Math.max(18, radius)`), so it can't produce a sub-minimum radius; Mercator's path
  reads nothing new.

## Fixes applied (all now in the tree)

### 1. [HIGH] Reset HUD skipped all 4 new fields — `LatitudeHudStudioScreen.applyDefaults`

`applyDefaults` (the full "Reset HUD" that copies every field from a `fresh` config into the live one) is
commented as copying every field, but it silently skipped the four fields this diff added
(`rainbowCycleSeconds`, `zoneTextScale`, `biomeTextScale`, `coordsTextScale`). The narrower Reset
Compass / Reset Labels buttons — hand-maintained field lists in the same file — *were* updated in round 7,
which is exactly what makes the gap dangerous: it looks done. Symptom would have been a full HUD reset
that leaves a cranked-up biome text size or a running Aurora speed untouched, contradicting the button's
promise. Fix: added the four missing `cfg.X = fresh.X` lines to `applyDefaults`.
**Why it mattered:** the reflection preset path (round 7) is drift-proof, but Reset HUD is a *separate*,
hand-maintained mirror of the same field set, and it rotted the moment a field was added. See L27.

### 2. [MEDIUM] Preset apply sanitized disk but not live title fields — `LatitudeConfig` + `CompassHudPreset.applyToLive`

`applyToLive()` sanitized the compass half (`compass.sanitize()`) but wrote the Title tab's numeric fields
(title scale / seconds / letter-spacing) straight into the LIVE config with no clamp. An imported or
hand-edited preset with e.g. `title.scale = 50` would render a broken title live until a restart, because
`saveCurrent()` only sanitizes the DISK copy, not the on-screen state. Fix: added
`LatitudeConfig.sanitizeLive()` — implemented as `applyFrom(captureTo())`, reusing
`LatitudeConfigData.sanitize()` as the single source of truth so there's no second clamp list to
maintain — and `applyToLive()` calls it in its title branch before `saveCurrent()`.
**Why it mattered:** a save-path sanitize protects the file the game reads next launch but leaves *this*
session's on-screen state broken. Untrusted external input (a shared preset blob) has to be clamped on the
LIVE state, not just on the way to disk. See L28.

### 3. [MEDIUM] Zone/Biome text-size sliders stayed live when the label was OFF — `LatitudeHudStudioScreen.updateSidebarVisibility`

The new Zone/Biome Text Size sliders stayed visible and interactive even with "Display Zone/Biome in HUD"
OFF, bypassing the Labels tab's own hide-when-off convention that `wZoneFollow`/`wBiomeFollow` already
follow. Fix: added `setVisible(wZoneTextScale, sidebarVisible && cfg.displayZoneInHud)` and the biome
equivalent to `updateSidebarVisibility()`, so the size control appears only when its label does.
**Why it mattered:** an interactive control for a label that isn't shown is a confusing dead widget and
breaks the tab's established gating pattern.

### 4. [MEDIUM] index.md round-8 entry contradicted its own registry row — `docs/binder/index.md`

index.md's round-8 entry still documented the superseded `CLASSIC_ATLAS_SCALE = 0.62f` / TEST 37, while
the evidence-registry row added in the same diff said `0.82f` / TEST 39 — two records of the same fact
disagreeing. Fixed index.md to the `0.82f` / TEST 39 chain. **Verified this pass:** the round-8 index
entry now reads `0.82f` (happy medium between 1.0-too-big/clipping and 0.62-too-small/bunched) and
`TEST 39.jar` STAGED, matching registry row `20260708-ui-pass-round8-fixes`.
**Why it mattered:** indexing discipline — the index and registry are the same fact's two homes and must
not diverge in the same commit that creates them.

### 5. [LOW] Biome-attached-alone drew with the wrong scale — `CompassHud.attachedZoneScale`

When the zone label is detached but the biome rides attached to the compass, the attached string was drawn
with `zoneTextScale`, making `biomeTextScale` a silent no-op in that specific case. Fix: added a shared
helper `CompassHud.attachedZoneScale(cfg)` — zone wins when it's part of the attached string (covers both
the documented "zone+biome fused share one size" case AND the zone-only case), and `biomeTextScale` is
used when ONLY biome is attached. Used in both `analogAttachedTextWidth` (width budget) and
`renderAnalogAt` (the actual draw) so the measurement and the render agree.
**Why it mattered:** a slider that does nothing in a reachable configuration reads as a bug even though
nothing crashes.

### 6. [LOW] Scaled-up attached text overflowed its own hitbox/border vertically — `CompassHud.attachedTextLineHeight`

For the analog-ATTACHED coords/zone text, the width was scaled by the text-size sliders but the height
still used the unscaled font `lineHeight`, so scaled-up text spilled past its drag hitbox and the Studio
preview border vertically. Fix: added `CompassHud.attachedTextLineHeight()` (the tallest present segment's
*scaled* height) and used it in BOTH `computeAnalogBounds` (the hitbox) and `renderAnalogAt`'s preview
border, so the two stay in sync by construction.
**Why it mattered:** same class as the L23 Tape bug — a box that claims content must be measured with the
same math the content is drawn with, or the grabbable region and the visible region drift apart.

### 7. [LOW] A corrupt preset slot counted as occupied — `CompassHudPresetSlots.isOccupied`

`isOccupied()` returned true for a corrupt slot (non-null preset object but `compass == null`), which
enabled the Load and Clear buttons for a "look" that applies nothing. Fix: `isOccupied()` now requires
`p != null && p.compass != null`, so a structurally-empty slot is treated as empty and its Load/Clear stay
disabled.
**Why it mattered:** a Load button that appears active but applies nothing is a silent no-op the user
can't distinguish from a real load.

## Known-minor items deliberately NOT fixed (deferred / flagged, not bugs to fix now)

- **`CompassHudPresetSlots.themeShort()` has a string-switch with a default fallback.** A future new
  `AnalogCompassTheme` would show its raw enum name in a slot summary rather than failing to compile the
  way the exhaustive `themeLabel()` would. Low. Noted as a future-proofing follow-up, not fixed this pass.
- **`CompassHudPreset.TitleFields` default literals (scale 1.8 / seconds 6.0) diverge from the Studio's
  "Reset Title" defaults (scale 1.6 / seconds 4.0).** The preset defaults match `LatitudeConfigData`'s own
  field initializers; the Reset Title button's numbers are the ones that differ. This is a PRE-EXISTING
  drift this session did not create. Flagged for Peetsa to decide which is canonical (Reset Title is the
  likely-stale one); behavior deliberately left unchanged so nothing shifts under him mid-arc.
- **`CompassHudPreset.presetFormatVersion` is written but never read.** An intentional forward-compat stub;
  no version-check / migration code consumes it yet. Noted so a future reader doesn't assume migration
  logic exists.
- **Zone + biome BOTH attached to the compass render as one fused line sharing `zoneTextScale`.** This is
  the documented feature scope boundary from round 7 (splitting the fused line means rewriting the shared
  text-wrap and drag-hitbox math). Detaching either label gives independent sizing immediately. Unchanged.

## Verification

- `compileJava` + full pure-JVM test suite (`cleanTest test`) green.
- `./gradlew clean build` green.
- `TEST 40.jar` staged in the `LATITUDE 26.2` Modrinth profile, SHA-256
  `283a3bb5a88ffcae92ebfd4b496b32f1cffe427d1b967e2389f2913bab1da9ab` (verified source↔staged),
  superseding `TEST 39.jar`. Same worldgen args as TEST 30-39; C-2/C-3 terrain untouched.
- NOT live-verified yet (these are the live re-test items): Reset HUD now also resets Aurora speed +
  all three text sizes; an imported preset with an extreme title scale renders sane immediately (no
  restart); Zone/Biome Text Size sliders hide when their label is OFF; biome-attached-alone honors
  `biomeTextScale`; scaled-up attached text stays inside its drag hitbox and preview border; a corrupt
  slot's Load/Clear stay disabled; plus the full round 7 + 8 re-check matrices in
  `ui-pass-round7-fixes-20260708.md` and `ui-pass-round8-fixes-20260708.md`.
