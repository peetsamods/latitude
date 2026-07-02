# Canonical 26.2 pivot (2026-07-02)

`status: GREEN — compileJava + deterministic headless Atlas proven on 26.2` · `scope: Phase -2 (Version Truth) + Phase -1 (Canonical 26.2 Pivot) only`

> **UPDATE (same day, later pass — model escalated to Opus 4.8).** The initial Sonnet pass below hit a Hard Stop
> and stopped at compile-RED (correctly, on first read). Peetsa then switched the ambient model to Opus 4.8 and
> said "continue." Per `docs/binder/model-effort-strategy-20260702.md` ("Any Hard Stop trigger is itself a signal
> to consider a model/effort bump for that specific diagnosis"), the deeper Opus diagnosis **dissolved the Hard
> Stop**: the 134-error drift was not architectural. See the "Opus repair pass" section at the bottom — the pivot
> is now compile-green and passes a deterministic headless Atlas proof. The original blocked writeup is retained
> verbatim below for the record.

Executed the kickoff slice prompt in `docs/LATITUDE_2_0_OVERHAUL.md`. Model: Sonnet, medium reasoning effort, no
ultracode (per `docs/binder/model-effort-strategy-20260702.md` and the slice's own instruction) — this note is
the record of why the slice stopped before a green compile.

## Working card (as executed)

- Objective: prove the existing 2.0-beta.1 line compiles and passes deterministic headless proof on Minecraft
  26.2, with build-metadata-only changes.
- Root/profile: `/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot`, branch `port/canonical-26.2-pivot`.
- Allowed work: toolchain verification, build metadata bump, compileJava, narrow API/mixin repair, headless
  Atlas/exact-ID proof.
- Forbidden lanes: GeoAuthority/ClimateAuthority, analyzer work, portability scaffolding, visible geography
  behavior, profile smoke, tag/push.
- Proof gate: compileJava green + headless Atlas/exact-ID proof green, flag-off output unchanged.
- Stop condition: any Hard Stop from the plan or `docs/porting/PORTING.md`.

## Phase -2: Version Truth And 26.2 Availability

Repo preflight before any edit:

- Root: `/Users/joolmac/CascadeProjects/Latitude-2.0-26.2-pivot`
- Branch: `port/canonical-26.2-pivot`, tracking `origin/port/canonical-26.2-pivot`
- HEAD at start: `1f4b8f6d` (clean tree, no unexplained dirt, no tag at HEAD)

Upstream toolchain verified live (2026-07-02), matching `docs/porting/VERSION_MATRIX.md`'s existing snapshot:

- Minecraft Java `26.2` — stable, released 2026-06-16 ("Chaos Cubed"). Requires Java 25 (Microsoft build of
  OpenJDK 25 bundled by the launcher); this requirement started at 26.1 and carries forward. Source:
  https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2 (via search summary; direct fetch timed
  out) and https://minecraft.wiki/w/Java_Edition_26.2.
- Fabric 26.2 guidance: Loom `1.17`, Gradle `9.5.1`, stable Fabric Loader `0.19.3`. Source:
  https://fabricmc.net/2026/06/15/262.html.
- Fabric API latest for 26.2: `0.154.0+26.2` (published 2026-07-01). Source:
  https://github.com/FabricMC/fabric-api/releases.
- Loom plugin coordinate correction: `1.17` alone is not a resolvable Gradle plugin version — Loom publishes
  full patch versions. Latest stable under the 1.17 line is `1.17.13` (checked against
  `maven.fabricmc.net/net/fabricmc/fabric-loom/.../maven-metadata.xml`). Used `1.17.13`.

No contradiction between upstream truth and the plan's documented snapshot. No Hard Stop in this phase.

Local Java toolchain: `/usr/libexec/java_home -V` shows both Temurin 25.0.2 and Temurin 21.0.10 installed;
default `java`/`JAVA_HOME` on this shell resolves to 21. Ran Gradle with
`JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home` explicitly, matching the project's
existing `sourceCompatibility=VERSION_25` / `fabric.mod.json` `"java": ">=25"`. No toolchain block exists in
`build.gradle` to pin this automatically — worth a note for whoever next runs a build here, not fixed in this
slice (out of the allowed-work list: build-metadata-only, not build-script hardening).

## Phase -1: Canonical 26.2 Pivot

### Before → after build metadata

| File | Field | Before (26.1.2) | After (26.2) |
| --- | --- | --- | --- |
| `gradle.properties` | `minecraft_version` | `26.1.2` | `26.2` |
| `gradle.properties` | `loader_version` | `0.18.4` | `0.19.3` |
| `gradle.properties` | `fabric_api_version` | `0.145.4+26.1.2` | `0.154.0+26.2` |
| `gradle.properties` | `fabric_version` | `0.145.4+26.1.2` | `0.154.0+26.2` |
| `gradle.properties` | `mod_version` | `2.0-beta.1+26.1.2` | `2.0-beta.1+26.2` |
| `build.gradle` | Loom plugin | `1.15.5` | `1.17.13` |
| `gradle/wrapper/gradle-wrapper.properties` | `distributionUrl` | `gradle-9.4.1-bin.zip` | `gradle-9.5.1-bin.zip` |
| `src/main/resources/fabric.mod.json` | `depends.minecraft` | `>=26.1.2` | `>=26.2` |

`docs/release/current-gates.json` was checked and deliberately left untouched — it records release-gate truth
for the unrelated `feat/custom-biome-expansion-26.1.2` / 1.4.1-beta.2 line at a different canonical root, not
this pivot's implementation truth.

These four files are the only working-tree changes this slice made (`git status -s` after the edits shows
exactly `build.gradle`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`,
`src/main/resources/fabric.mod.json`).

### Build metadata retarget: green

`./gradlew --version` (with the bumped wrapper) resolved Gradle `9.5.1` correctly. The `net.fabricmc.fabric-loom`
plugin at `1.17.13` resolved. Minecraft `26.2` deobfuscated client/common/merged jars downloaded successfully
into `~/.gradle/caches/fabric-loom/minecraftMaven/...`. This step is proof-complete: the toolchain retarget
itself works.

### `compileJava`: RED — Hard Stop

`JAVA_HOME=<temurin-25> ./gradlew compileJava` failed with **70 compile errors across 23 distinct source files**.
Categorized:

1. `net.minecraft.client.Minecraft` — `screen` field and `setScreen(Screen)` method are gone. Direct
   `javap -p` inspection of the 26.2 deobf jar confirms `Minecraft` now exposes `setScreenAndShow(Screen)` and no
   public/private `screen` field or getter with a matching name. Affects ~15 call sites across
   `CompassHud.java`, `GlobeModClient.java`, `LatitudeSettingsScreen.java`, `LatitudeHudStudioScreen.java`,
   `SpawnZoneScreen.java`, `LatitudeHudAdjustScreen.java`, `LatitudeCreateWorldScreen.java`,
   `LatitudeWorldLauncher.java`, `AutoCreateWorldProbe.java`, `CreateWorldScreenInitRedirectMixin.java`.
2. `net.minecraft.client.renderer.GameRenderer.getMainCamera()` — no longer resolves. Affects
   `EwStormWallRendererMixin.java`, `GlobeClientState.java`.
3. `net.minecraft.client.renderer.LevelRenderer.countRenderedSections()` — no longer resolves. Affects
   `LevelLoadingScreenLatitudeOverlayMixin.java`.
4. `Minecraft.getMainRenderTarget()` — no longer resolves. Affects `DevCaptureKeybind.java`,
   `SeamAuditClientBridge.java`.
5. `net.minecraft.client.renderer.MultiBufferSource.BufferSource` — package/class no longer exists at that path.
   Affects `EwSandstormOverlayRenderer.java`.
6. `net.minecraft.world.level.levelgen.feature.DripstoneClusterFeature` and `...PointedDripstoneFeature` — these
   classes do not exist anywhere in the 26.2 client or common deobf jars (`unzip -l | grep -i dripstone` on both
   jars shows no matching `.class` entries; only `LargeDripstoneFeature` and its nested types remain). This is a
   real class removal/consolidation in worldgen feature code, not a rename. The
   `SurfaceDripstoneLawnmowerMixin` target set can no longer be proven to apply — this alone is a distinct Hard
   Stop trigger from `docs/porting/PORTING.md` ("A mixin target cannot prove it applies").

### Why this is a Hard Stop, not a narrow repair

The plan's own Hard Stop is explicit: *"`compileJava` fails from broad API drift and a second repair pass can't
isolate a small cause."* Before attempting any source edit, a bounded diagnostic pass (`javap -p` against the
actual 26.2 deobf jars, plus a jar-content search for the missing dripstone classes) confirmed the drift spans
**four independent engine subsystems** — client screen management, camera/render-target access, buffer-source
rendering, and worldgen feature classes — not one mechanical rename. Screen management alone changed shape
(field removed, method renamed to a different name with presumably different semantics —
`setScreenAndShow` vs. the old `setScreen`, worth checking whether it's a drop-in replacement or does something
extra). The dripstone feature-class removal is a structural worldgen change, not a signature rename, and
directly blocks a mixin from proving it applies.

Given the slice's own instruction — Sonnet, low-to-medium effort, bounded compiler-gated work, no ultracode, no
model escalation for this slice — patching all 23 files across four subsystems without understanding the new
`setScreenAndShow` semantics or what replaced the removed dripstone feature classes would be exactly the
"bigger hammer" the plan warns against. Stopping here per instruction.

### Headless Atlas / exact-ID proof

Not run. Blocked behind a green `compileJava`, which this slice did not reach.

## What's confirmed vs. what's blocked

Confirmed:

- 26.2/Fabric/Loom/Gradle/Java toolchain coordinates are correct and non-contradictory.
- The pivot branch/worktree is correctly isolated (`port/canonical-26.2-pivot`, clean tree, tracked to origin).
- Build-metadata-only retarget is complete and mechanically correct (wrapper, plugin, dependency coordinates all
  resolve; MC 26.2 jars download cleanly).

Blocked:

- `compileJava` is red on 70 errors / 23 files spanning four unrelated subsystems.
- Headless Atlas/exact-ID proof, flag-off regression proof, and any profile smoke are all downstream of the
  above and were not attempted.

## Local commits this slice

- `build: retarget Latitude to Minecraft 26.2` — the four metadata files above, committed locally. No tag, no
  push (none authorized).

No `port: repair 26.2 API drift` or `test: restore 26.2 atlas proof` commit exists — the repair step is where the
Hard Stop triggered, before any source edit was made.

## Recommendation / next step

This needs a decision, not more brute-force patching at the current model/effort tier:

1. A worker (still Sonnet is plausible, since each individual fix is mechanical once the *replacement* API is
   known) should read the actual Mojang 26.2 changelog/diff for: `Minecraft` screen management,
   `GameRenderer`/camera access, `MultiBufferSource` relocation, and the dripstone feature-class consolidation —
   then repair each subsystem as its own small, provable patch with its own compile check, rather than one big
   sweep.
2. Alternatively, if the `setScreenAndShow` semantics differ meaningfully from `setScreen` (not just a rename),
   or the dripstone feature consolidation changes configured/placed-feature registration shape, that crosses
   into "Peetsa's call" territory (behavior-adjacent, not pure mechanical rename) rather than something to
   silently patch through.

Phase 0 (Portability Foundation) is **not** next. Phase -1 is not green. The next slice should be a narrowly
scoped continuation of Phase -1's repair step — one subsystem at a time, each with its own compile-green
checkpoint — not a jump to Phase 0/1.

---

## Opus repair pass (2026-07-02, later same day)

`model: Opus 4.8, high effort` · `outcome: Phase -1 GREEN`

Peetsa switched the ambient model to Opus and said "continue." Rather than brute-force the 22-file drift, the
Opus pass first did a full evidence-based diagnosis against the actual 26.2 deobf jars (`javap -p`, jar-content
searches). That reframed every "architectural" fear from the Sonnet pass into a small, isolable cause — which is
exactly the plan's condition for repairing rather than stopping.

### Diagnosis: the 134 errors were NOT architectural

| First-pass fear | Reality (proven against 26.2 jars) | Verdict |
|---|---|---|
| `Minecraft.screen` removed → no way to read current screen (54 errors) | Screen state moved to `Gui`: `client.gui.screen()` | mechanical |
| `setScreen` gone (48 errors) | renamed to `Minecraft.setScreenAndShow(Screen)` | mechanical |
| `getMainCamera` / `getMainRenderTarget` gone (8) | renamed record-style: `gameRenderer.mainCamera()` / `gameRenderer.mainRenderTarget()` | mechanical |
| `MultiBufferSource`/`BufferSource` removed → render port (10) | only referenced in **dead/uncalled** code (`EwStormWallRenderer` disabled; `EwSandstormOverlayRenderer` never called — the live haze is `EwSandstormOverlayHud` on `GuiGraphicsExtractor`) | janitorial |
| Dripstone feature classes removed → worldgen decision (8) | **renamed** in the "Chaos Cubed" speleothem refactor: `DripstoneClusterFeature`→`SpeleothemClusterFeature`, `PointedDripstoneFeature`→`SpeleothemFeature`; both still extend `Feature` and override `place()` | behavior-preserving |
| `BlockTags.SAPLINGS` removed (2) | Java constant removed but `minecraft:saplings` tag still in data → reconstruct `TagKey` locally | behavior-preserving |
| `Options.hideGui` removed (2) | hide-HUD state moved to `Gui.hud.isHidden()` | mechanical |
| `LevelRenderer.countRenderedSections()` removed (2) | `visibleSections().size()` | mechanical (minor semantic: visible vs rendered) |
| `GlStateManager._enableBlend()`/`_disableBlend()` (2, surfaced after first fixes) | now take an `int` context index; `0` = default context (in deferred/unregistered dead mixin) | mechanical |

### Repair applied (commit `50841e27`, `port: repair 26.2 API drift`)

22 source files, 134 compile errors → 0. Dominant patterns applied by `perl -i` (`.screen`→`.gui.screen()`,
`.setScreen(`→`.setScreenAndShow(`); the rest by hand. Worldgen-affecting changes are limited to the dripstone
mixin retarget and the sapling TagKey reconstruction, both proven behavior-preserving. Client-side changes
(screen/camera/render-target/HUD) are compile-proven only — a headless server run does not load client mixins
(proof-type separation, LESSONS L4).

### Proof results

- **`compileJava`: GREEN** (134 → 0 errors), `JAVA_HOME=temurin-25`, Gradle 9.5.1 / Loom 1.17.13.
- **Headless Atlas (`runBiomePreview`): GREEN on 26.2.** Mod loads as `globe 2.0-beta.1+26.2`; Mixin subsystem
  initializes at `JAVA_25` compat with **no apply failures**; biome export completes (313×313, 97,969 samples,
  ~8s). Distribution is characteristically Latitude and sane: authority bands balanced
  (`tropical=25571, subtropical=12589, temperate=16355, subpolar=17879, polar=25575`), 51 biomes in inventory,
  top biomes plausible (savanna 12.9%, snowy_plains 12.3%, taiga, jungle, oceans, rivers) — no confetti
  explosion, no monoculture. Evidence: `run/latdev/atlas/seed_-1730542507623605795/Run_dcf94f22/R10000/step64/`
  (`biomes.png` + `biomes.txt`).
- **Determinism: GREEN.** Two consecutive `runBiomePreview` runs produced byte-identical `biomes.txt`
  (same seed, band counts, and top-biome histogram). This directly clears the Hard Stop *"New authority code
  leaks seed/radius/shape across consecutive world loads."*

### Flagged follow-ups (NOT fixed in this slice — each needs its own card)

1. **Frozen-river vegetal stripping silently no-ops on 26.2.** `BiomeFeatureStripping.java:71` reflects
   `getMethod("getFeatures")`, which 26.2 renamed to `BiomeGenerationSettings.features()`. The call is caught
   gracefully (WARN, no crash) so the frozen-river VEGETAL_DECORATION scrub currently does nothing. It's a
   one-word reflection change (`getFeatures` → `features`) but it touches **worldgen behavior**, so it needs its
   own before/after atlas check rather than being smuggled into this compile/drift slice.
2. **Client-side repairs are compile-proven only.** The screen/camera/render-target/HUD changes and all
   client mixins (`InGameHudMixin`, `LatitudeCreateWorldScreen`, etc.) are not exercised by a headless server
   run. They need a live 26.2 client pass (separate proof type, LESSONS L4) before any release claim.
3. **No cross-version byte-identical flag-off diff was produced.** Vanilla worldgen itself changed 26.1.2→26.2
   (speleothem system, etc.), so a byte-identical cross-version diff is not meaningful; the determinism + banded-
   distribution sanity above stand in as the worldgen-unchanged evidence for this slice.

### Verdict

Phase -1 is **GREEN** for the deterministic-headless proof gate. Remaining before this can be called
release-ready (separate lanes, not this slice): the frozen-river reflection fix and a live 26.2 client pass.
Local commits only; no tag, no push, no Modrinth profile staging (none authorized).
