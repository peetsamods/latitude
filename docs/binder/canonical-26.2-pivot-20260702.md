# Canonical 26.2 pivot attempt (2026-07-02)

`status: blocked — Hard Stop hit during compile repair` · `scope: Phase -2 (Version Truth) + Phase -1 (Canonical 26.2 Pivot) only`

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
