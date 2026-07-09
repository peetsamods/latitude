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

**Fix (per Peetsa's explicit instruction):** removed `NORMAL` from `TitleCaseMode` entirely — three
options remain: `UPPERCASE`, `LOWERCASE`, `MOCKING`. New default is `UPPERCASE` (matches the pre-existing
sample/fallback look). Five consumers updated (the enum declaration, the field default, `sanitize()`'s
null-guard default, `applyCase()`'s switch, the Studio's cycle-button label map, and "Reset Title"'s
default). Backward compatible by construction: Gson maps an unrecognized saved enum constant to `null`
(not a parse failure — confirmed by a new regression test), and `sanitize()` already null-guards to the
new default, so an existing config that saved `"NORMAL"` degrades safely on next load.

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

- `compileJava` + pure-JVM suite (`cleanTest test`) green, including a new regression test
  (`removedNormalTitleCaseDegradesToUppercaseDefault`) that actually round-trips a `"NORMAL"`-valued
  config through Gson + `sanitize()` and asserts it lands on `UPPERCASE` — proof, not just a claim in a
  comment.
- NOT live-verified yet (visual/UI class of bug): Show Degrees toggling the Title preview; Title Case
  cycling only through the three remaining options with a fresh (no-legacy) config; Tape's Analog Size
  slider bottoming out at 32 (not 16) and staying legible there; every rewritten tooltip reads clearly at
  a glance.
- Worldgen isolation by diff scope: only `client/LatitudeHudStudioScreen.java`, `client/CompassHud.java`,
  `client/CompassDialRenderer.java`, `client/ZoneEnterTitleOverlay.java`,
  `core/config/LatitudeConfigData.java`, plus the one test file. Zero `world/`, `terrain/`, `mixin/`
  changes — C-3 and the TEST 30 args remain untouched.

`TEST 33.jar` staged (SHA-256 `17f528d28db8a086fd81f72f1e9de669803b6c3df58af1667d1d0e19447aee7d`),
superseding `TEST 32.jar`. Same worldgen args as TEST 30/31/32.
