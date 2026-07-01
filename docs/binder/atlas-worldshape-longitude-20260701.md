# 2.0 UI features: Longitude compass reading + Atlas / World Shape toggle (2026-07-01)

`status: shipped (source-green, live-eyeball pending)` · `scope: rendering, HUD, create-world UI` · `branch: feat/custom-biome-expansion-26.1.2`

Two user-facing features landed this session that previously had **no binder note** (the gap that
prompted the 2026-07-01 doc-sync pass). Both are compiled green and staged in the Modrinth profile as
`TEST 1.jar`; both still need a live in-game eyeball. See [[latitude-versioning-convention]] and the
`version-bump-2.0-20260701.md` note for the surrounding 2.0 rename.

---

## 1. Longitude reading on the compass (commit `9a6e4e41`)

Adds a real **longitude** reading alongside the existing latitude reading on the compass HUD, togglable in
the F9 settings screen (same toggle surface as the other compass readouts).

**Convention (must stay consistent with vanilla F3 and `/latdev tpedge`):**
- West = `−X`, East = `+X`.
- Measured against `halfSize(border)` — the **X radius**, which is shape-correct in both Classic and
  Mercator (Mercator's X radius is 2× the Z radius). `0°` at center X, `180°` at the E/W border.

**Code:**
- `src/main/java/com/example/globe/util/LatitudeMath.java` — new `hemisphereEW(border, x)`,
  `longitudeDegrees(border, x)`, `formatLongitudeDeg(border, x)` (added right after `formatLatitudeDeg`).
- `src/main/java/com/example/globe/client/LatitudeMath.java` — client shim `formatLongitudeDeg(playerX, border)`
  (note: this shim's arg order is reversed vs. the util class — a known, pre-existing wart in this codebase's
  two-`LatitudeMath` split).
- `src/main/java/com/example/globe/client/CompassHud.java` — lat/lon readouts renamed to `LatLon` variants;
  new `joinLatLon(lat, lon)` (returns `"lat, lon"`, null-safe), `longitudeText(...)`; digital call site now
  renders `joinLatLon(latitudeText(...), longitudeText(...))`.

---

## 2. Atlas / World Shape toggle — Mercator 2:1 vs Legacy 1:1 (commit `30db22fc`)

Re-adds the Classic/Mercator choice at world-creation time (an earlier explicit decision had removed it in
favor of Mercator-only; reversed per direction this session). The CLASSIC runtime was already fully correct
end-to-end (border sizing, spawn X radius, latitude/pole math, dev commands all already branch on
`GlobeShape`) — the **only** missing piece was the create-screen front door, so this change is 100% "add a
UI + plumbing path," zero "repair broken runtime." See [[mercator-world-type]].

**Plumbing (create-time shape → persisted world state):**
- `GlobePending.pendingGlobeShape` (String, `null` = no explicit choice) — mirrors the existing
  `pendingGlobeRadius`/spawnZone/startWithCompass singleton pattern. `globe_shape` lives in
  `LatitudeWorldState` (SavedData), not a vanilla `WorldOptions` field, so it needs the pending-singleton
  path, not direct parameter threading.
- `LatitudeWorldLauncher.beginExpedition(...)` — new `GlobeShape` param; written to
  `GlobePending.pendingGlobeShape` in the async launch block; rolled back to `null` in the failure branch.
- `GlobeMod.initLatitudeBiomesForWorld(...)` — the force-point that used to unconditionally stamp every new
  world `"mercator"` now prefers an explicit `pendingShape` when present, else the same mercator default —
  gated behind the **exact same** `gameTime<100 && currently-classic` condition as before, so existing saves
  are provably untouched.

**Create-screen UI (`LatitudeCreateWorldScreen.java`):**
- New "World Shape" stepper on the Rules rail (mirrors the World Type prev/next idiom). Options
  `Mercator 2:1` / `Legacy 1:1`, defaulting to index 0 = Mercator so anyone who ignores the toggle sees no
  behavior change.
- `worldDimsLabel()` (renamed from `mercatorDimsLabel()`) branches: Mercator shows E-W `4z` × N-S `2z`;
  Legacy shows a true square `2z × 2z` — both derived from the same border radius the real world border uses
  (satisfies "the atlas should agree with the size of the world").
- `beginExpedition(...)` call now passes `currentWorldShape()`.

**Atlas preview (`LatitudePlanisphereRenderer.java` + create screen):**
- The preview draws a **rectangle** for Mercator and (currently) a **circle** for Legacy, reusing the same
  band-fill / selected-band-highlight logic; only the fill primitive differs
  (`fillRect`/`fillBandStripRect`/`drawLatitudeLineRect` vs. the chord-masked circle primitives). The
  selected spawn zone already highlights as a brightened stripe + gold edge lines; that logic was ported to
  the rectangle case.
- The display label `"PLANISPHERE"` was renamed to `"ATLAS"` (a circle-specific name no longer fits once the
  preview can be a rectangle). The class/method identifiers (`LatitudePlanisphereRenderer`,
  `renderPlanispherePreview`) were deliberately left un-renamed — zero behavior change either way; a cosmetic
  follow-up.

**Classic byte-identical guarantee for existing saves:** preserved. Nothing in `LatitudeWorldState`'s
codec/defaults, `LatitudeBiomes`'s `GlobeShape` enum / `getActiveXRadiusBlocks`, or `GlobeMod.setGlobeBorder`
branching was touched.

---

## Known issues found in TEST 1 live testing (2026-07-01)

TEST 1 confirmed the toggle **works** end-to-end, but live testing surfaced polish/correctness gaps in the
just-shipped UI (and adjacent worldgen). All are catalogued with repro + code pointers + suggested priority in
**`test1-live-findings-20260701.md`** — including: the Legacy atlas should render as a **square, not a
circle**; the atlas looks "like a flag" and needs a more map-like treatment; latitude labels are cramped; the
left/right control layout is under question; and "Re-create world" does not copy the source seed. Do not
consider these two features "done" until that punch-list is worked and a live eyeball passes.
