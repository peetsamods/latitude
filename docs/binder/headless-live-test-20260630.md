# Headless "live test" — what's automatable vs. eyeball-only (2026-06-30)

`status: active` · `scope: validation, snow-line, world-creation` · `staged jar: d8657778`

Round of automated verification standing in for an in-game live test (agent can't reliably pilot the GLFW
client — synthetic input is flaky, and the Modrinth-App-only HARD RULE means the "fly around and look" test
needs Peetsa at the keyboard). The goal here was to push as much as possible into headless, repeatable checks
and to be explicit about the residue that genuinely needs a human.

## Results

| Item | Method | Verdict |
|------|--------|---------|
| Bonus-chest fix (launcher carries the flag) | source verify | PASS — `WorldOptions(seed, generateStructures, bonusChest)` at `LatitudeWorldLauncher.java:234`; the dropped-flag regression is fixed |
| Generate-structures toggle | source verify | PASS — `LatitudeCreateWorldScreen` field defaults true, toggles, plumbs to `beginExpedition` |
| "Expedition"→"World" wording | source verify | FIXED 2 leftovers the earlier pass missed: header subtitle `"New Expedition"`→`"New World"` (`:1087`) and size desc `"The standard expedition…"`→`"The standard world…"` (`:98`). Remaining `beginExpedition` hits are method names / log lines (not user-visible). |
| Plains-on-steep gate | terrain-aware atlas | PASS — temperate plains 19.7%→8.3%, hills/meadow 0%→12.6% |
| Arid belt / badlands / frozen_river | atlas + band_correctness_check | PASS (see [[arid-belt-earthlike-20260625]]) |
| **Snow line lowered** | **NEW headless alpine audit** | **PASS** (below) |

## NEW reusable check — headless alpine snow audit

A dev-only mode in `BiomePreviewHeadlessRunner` (trigger `-Dlatdev.alpineAudit=true`, paired with
`-Dlatdev.biomePng=disabled`) boots a REAL globe world and evaluates the **actual compiled**
`LatitudeBiomes.alpineSurfaceKind(x,y,z,radius)` across a latitude×altitude grid — proving the snow-onset
logic, not a re-derivation of it. Report → `run-headless/latdev/alpine-snow-audit.txt`.

Re-run:
```
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain \
  -Platdev.preview.levelName=latdev-alpine-audit -Platdev.preview.levelSeed=2591890304012655616 \
  -Platdev.preview.levelType=globe:globe_small \
  -Dlatdev.alpineAudit=true -Dlatdev.biomePng=disabled runBiomePreview
```

Result (radius 5000), 50%-snow onset Y per band — exactly tracks the compiled `alpineSnowMinY` constants:

```
band          onsetY  snow%@Y[166 170 174 178 182 186 190]  belowY168
TROPICAL        never    0   0   0   0   0   0   0           clean   (warm-creep safe)
SUBTROPICAL       182    0   0   0   0  50 100 100           clean
TEMPERATE         174    0   0  50 100 100 100 100           clean   <- THE FIX (peaks Y176-185 now cap)
SUBPOLAR          170    0  59 100 100 100 100 100           clean
POLAR             169    0  85 100 100 100 100 100           clean
```

All 11 assertions PASS (tropical never snows at any Y incl. 200; every band clean below the Y168 floor; each
band's majority-snow onset within ±a few blocks of its constant — the spread is the intended ±4 ValueNoise warp).

Code: dev-only, EXCLUDED from the shipped jar (build.gradle strips `com/example/globe/dev/`; verified 0 dev
classes in the release jar). No shipped worldgen logic changed.

## Honest residue — still needs Peetsa in-game (Modrinth App)
- **Snow on real peaks:** the audit proves the *decision* (temperate terrain ≥Y174 → snow). Whether terrain
  actually reaches that near a given spawn is Terralith-terrain-dependent; the near-spawn small-world sample
  topped out at Y88–110 (no alpine peak), so snow painting on a real peak wasn't *shown* (only the logic). Peaks
  ~Y176-185 were seen on the earlier live cruise. → eyeball on a world with mountains.
- **Bonus chest actually at spawn:** the client-creation flow (the fixed path) only runs in the real client;
  a dedicated server has no bonus-chest field to force. Source-proven, in-game-unconfirmed.
- **Subjective feel:** Mercator reading as wider; snow line looking natural; world-creation screen wording/layout.

See the live-smoke checklist (`docs/release/live-smoke-checklist.md`) for the manual set.
