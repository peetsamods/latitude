# HUD Studio pass round 5 — Title tab, tooltip pass, Tape legibility (2026-07-08)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 34 candidate)`
Source: Peetsa on the HUD Studio's Title and Compass tabs, plus a full tooltip readability request.

## Finding 1: "Show Degrees" does nothing in the Title tab

**Root cause:** the Studio's title preview built its sample string (`zoneWord + " " + degText`) inline at
the render call site, unconditionally concatenating the degree text — it never checked
`LatitudeConfig.showZoneBaseDegreesOnTitle` at all. A SEPARATE, already-existing helper
(`studioPreviewTitle()`, explicitly commented "shared by the render path and the drag hit-test") *did*
exist and *was* used by the drag hit-test, but the render path had its own duplicated copy of the same
logic instead of calling it — so the one place that mattered (what you actually see) never consulted the
flag.

**Fix:** deleted the duplicated inline copy; the render path now calls `studioPreviewTitle()` directly,
and that shared function now honors the flag itself (both in the live-world branch and its no-world
fallback string). Toggling degrees now visibly changes the preview, and the drag hitbox can never drift
from what's drawn again (single source of truth — the same class of fix as the Round-3 Studio-preview
coordinate bug).

## Finding 2: Title Case has a "duplicate" — Normal and Uppercase look identical

**Root cause:** `applyCase()`'s NORMAL branch is a genuine no-op (returns the text unchanged) and
UPPERCASE genuinely differs — but the Studio's no-world SAMPLE title (used whenever the Studio is opened
from the create-world screen, before any world exists — Peetsa's exact usage) was hardcoded as
`"TROPICS 12°S"`, already all-caps. NORMAL applied to an already-uppercase string is indistinguishable
from UPPERCASE applied to it — a real, reproducible duplicate in exactly the scenario being tested.

**CORRECTED same session:** the first fix removed `NORMAL` from `TitleCaseMode` entirely, on the reading
that Peetsa wanted the option gone. He clarified immediately after: he wants `NORMAL` kept — it's the
"Tropical" natural-case look, genuinely distinct from `UPPERCASE`'s "TROPICAL" on any real zone name, and
he values it. The removal was reverted in full (enum restored to `{NORMAL, UPPERCASE, LOWERCASE,
MOCKING}`, default back to `NORMAL`, all five consumers reverted, the now-moot regression test deleted).

**The actual fix:** the diagnosis was correct, the response to it wasn't — the real bug was narrower than
"delete the option." `studioPreviewTitle()`'s no-world fallback strings are now natural case
(`"Tropics 12°S"` / `"Tropics"`, not `"TROPICS 12°S"` / `"TROPICS"`), matching the live in-world
`zoneTitleWord()` (which was already natural-case). With that one casing fix, `NORMAL` and `UPPERCASE`
now visibly differ in every context, including the exact no-world Studio preview Peetsa was testing from
— no option needed to disappear.

## Finding 3: Tooltip cleanup

- Removed "(Folded in from the old F9 Settings screen.)" from both tooltips that had it (Show HUD, Warning
  Messages in the General tab) — internal implementation trivia, not useful to a player.
- Full readability pass over every Studio tooltip. Left the already-clear ones alone; rewrote roughly 15
  that led with jargon (`opacity`, slash-separated field lists like "compass/zone/biome/coords", "letter
  casing", "steps inward stages") into plain sentences, several with concrete examples.
- The two **Zone/Biome Text Grow** tooltips Peetsa flagged directly were rebuilt from scratch: they used
  to explain the mechanism in Pin & Grow's own internal vocabulary ("extends from its pin") without ever
  saying what a "pin" is. Now: *"If the zone name changes length (like 'Tropics' vs 'Subtropics'), pick
  which side the label grows from — left, right, or evenly on both sides. Where you've placed the label
  on screen never moves, only which direction the text stretches from it."*

## Finding 4: Tape look gets illegibly narrow at small Analog Size

**Root cause:** the Tape look's readability depends on its WIDTH — heading labels (N/NE/E…) spread across
`usableHalf = radius − 3`, and the label text itself also shrinks (floors at 55% scale). Both shrink
together as the shared Analog Size slider (16–72) approaches its minimum, compounding into illegible
"squish" at the bottom of the range — exactly Peetsa's description ("shrinks inward and gets narrower...
at its narrowest you can't read any of the numbers").

**Fix:** gave Tape its own legibility floor (32 — the smallest size already validated live with no
complaints in earlier rounds) applied at the single existing `analogDiameter()` choke point every
consumer (render, dock, hitbox, drag) already reads from — not a second width-vs-box split, avoiding the
exact class of bug just written up as LESSONS L23. The Studio's "Analog Size" slider's own minimum bound
also raises to 32 specifically when Compass Look is Tape, so the displayed/draggable number always
matches what's actually rendered (never a lie) — every other look keeps the full 16–72 range unchanged.

## Verification

- `compileJava` + pure-JVM suite (`cleanTest test`) green (the `NORMAL`-default assertions in
  `LatitudeConfigDataTest` are back to asserting `NORMAL`; the transient "removed" regression test was
  deleted along with the removal it was guarding).
- NOT live-verified yet (visual/UI class of bug): Show Degrees toggling the Title preview; Title Case
  cycling through all FOUR options (Normal/UPPERCASE/lowercase/mOcKiNg) with Normal now genuinely reading
  as "Tropics" (not "TROPICS") even from the no-world create-screen Studio; Tape's Analog Size slider
  bottoming out at 32 (not 16) and staying legible there; every rewritten tooltip reads clearly at a
  glance.
- Worldgen isolation by diff scope: only `client/LatitudeHudStudioScreen.java`, `client/CompassHud.java`,
  `client/CompassDialRenderer.java`, `client/ZoneEnterTitleOverlay.java`,
  `core/config/LatitudeConfigData.java`, plus the one test file. Zero `world/`, `terrain/`, `mixin/`
  changes — C-3 and the TEST 30 args remain untouched.

`TEST 33.jar` staged (SHA-256 `b18c50ed3bd63cf284be6e886b8498dd90100b86c960d6e5d30e9660bc113de9` —
replaces the SHA `17f528d2…` build that briefly removed Normal; same TEST 33 name since that build was
never actually tested live), superseding `TEST 32.jar`. Same worldgen args as TEST 30/31/32.
