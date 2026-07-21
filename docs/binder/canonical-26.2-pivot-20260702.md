# Canonical 26.2 pivot (2026-07-02)

`status: GREEN — compileJava + deterministic headless Atlas proven on 26.2` · `scope: Phase -2 (Version Truth) + Phase -1 (Canonical 26.2 Pivot) only`

> **Point-in-time note (added 2026-07-06, Fable 5 audit Slice A):** this running log ends at TEST 20 /
> HEAD `61d51782` (2026-07-03). Everything after — GeoAuthority/ClimateAuthority Phases 0-3, the Biome
> Consumer slice, Phase 4 (terrain wrapper), TEST 21-26, and the Fable 5 audit — is recorded in
> `index.md`'s dated sections and `fable5-overhaul-audit-report-20260706.md`, not here.

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

`model: Opus 4.8, high effort` · `outcome: Phase -1 code-complete; all headless gates GREEN; only live client pass remains`

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
- **Full production `build`: GREEN.** `./gradlew build` produces `build/libs/latitude-2.0-beta.1+26.2.jar`
  (1.49 MB) plus the sources jar. This is more than `compileJava` — it runs the Loom remap step that produces
  the shippable (intermediary-mapped) jar.
- **Production mixin-apply structure verified against the known-good jar.** The 26.2 jar declares
  `"refmap": "globe.refmap.json"` in `globe.mixins.json` but contains **no** standalone `globe.refmap.json` —
  and this is **identical** to the live-proven `latitude-1.4.1-beta.2+26.1.2.jar` (the jar that passed the full
  live closeout in `docs/release/current-gates.json`). The project uses modern Loom's remap-at-build-time
  (tiny-remapper mixin extension rewrites mixin bytecode references straight to intermediary in the output jar),
  so the standalone refmap file is unnecessary and its absence is expected, not a defect. The dev-run warning
  *"Reference map globe.refmap.json could not be read… ignore in a development environment"* is the normal dev
  message. Conclusion: mixins will apply in the Modrinth (production) runtime the same way they do in the
  shipped 26.1.2 jar.

### Frozen-river vegetal stripping: investigated, NOT a regression, deliberately untouched

`BiomeFeatureStripping.java:71` reflects `getMethod("getFeatures")`. The headless log warns
`NoSuchMethodException: BiomeGenerationSettings.getFeatures()`. First read (Sonnet pass) called this a 26.2
regression. **It is not.** Verified against the 26.1.2 merged deobf jar: 26.1.2's `BiomeGenerationSettings`
already exposed **`features()`**, not `getFeatures()` — the `getFeatures` name has not existed in these mappings
since before 26.1.2. So this reflection threw `NoSuchMethodException` on 26.1.2 **too**, and the frozen-river
VEGETAL_DECORATION scrub has **removed nothing on any version** (also confirmed by the inner
`instanceof List<?>` check, which can never match a `HolderSet` — `HolderSet extends Iterable`, not `List`). No
evidence of `removed>0` exists anywhere in the repo.

Because the feature has always been inert, the behavior-preserving action for the pivot is to **leave it
exactly as-is** (no-op → no-op; the log warning is identical to 26.1.2's). *Fixing* it to actually strip
vegetation would be a NEW, never-shipped worldgen change (frozen rivers losing plants for the first time),
which would violate the pivot's "no worldgen behavior changes" mandate. If working frozen-river vegetal
stripping is actually desired, that is a **new feature request for Peetsa**, not a pivot repair — file it
separately.

### Remaining before release-ready (separate lanes, NOT this slice)

1. **Live 26.2 client pass.** The screen/camera/render-target/HUD changes and all client mixins
   (`InGameHudMixin`, `LatitudeCreateWorldScreen`, etc.) are compile-proven only — a headless server run does
   not load client mixins. Needs a live 26.2 client pass (separate proof type, LESSONS L4). This is the single
   item that genuinely requires Peetsa at the keyboard.
2. **No cross-version byte-identical flag-off diff.** Vanilla worldgen itself changed 26.1.2→26.2 (speleothem
   system, etc.), so a byte-identical cross-version diff is not meaningful; the determinism + banded-distribution
   sanity above stand in as the worldgen-unchanged evidence.

### Verdict

Phase -1 is **code-complete and GREEN for every headlessly-verifiable gate**: compile, full production build,
production mixin-apply structure (matches the shipped 26.1.2 jar), headless Atlas, determinism. The one flagged
"worldgen regression" turned out to be a pre-existing inert feature, correctly left untouched. The only thing
left is the live in-game client pass, which only Peetsa can drive. Local commits only; no tag, no push, no
Modrinth profile staging performed (staging offered pending Peetsa's go-ahead).

---

## New-biome handling + profile staging (2026-07-02, same day)

`trigger: Peetsa flagged that 26.2 introduces new biomes — "make sure those are able to load in."`

**26.2 added exactly one biome: `minecraft:sulfur_caves`** (a cave biome from the "Chaos Cubed" update; none
removed). Diffed the 26.1.2 vs 26.2 merged deobf biome registries to confirm.

**Bug found and fixed (commit `292c7ed3`).** Latitude classifies each biome as cave-vs-surface via a **hardcoded
list** of `{lush_caves, dripstone_caves, deep_dark}` in three places. `sulfur_caves` was not in it, so Latitude
misclassified it as a *surface* biome and overwrote it with a latitude-band pick — meaning **sulfur_caves would
never have generated underground**. Added it to all three cave lists so it is preserved exactly like the other
cave biomes:
- `ChunkGeneratorPopulateBiomesMixin.isCaveBiome`
- `LatitudeBiomeSource.isCaveBiome`
- `LatitudeBiomes.SURFACE_CAVE_DENYLIST`

Proof: compileJava green; headless Atlas green; the **surface** atlas is byte-identical before/after (the fix is
surgical — only underground caves); vanilla `OverworldBiomeBuilder` confirms `sulfur_caves` is placed in the
overworld; it is now handled identically to `dripstone_caves`, which the atlas biome inventory confirms is
preserved and reachable. The atlas inventory doesn't positively list `sulfur_caves` — but it also doesn't list
`lush_caves` or `deep_dark` (both known-working cave biomes), because that sampling only catches common caves at
its Y, so its absence is not evidence of failure. **Final visible confirmation is a live
`/locate biome minecraft:sulfur_caves`** in the client test.

Note: this cave-list fragility is worth a future refactor (there is no vanilla `#is_cave` biome tag — `is_overworld`
lumps surface and cave biomes together — so a hardcoded list is currently the only clean option; revisit if
Mojang adds a cave tag or a new cave biome lands again).

**Profile staged (Peetsa authorized).** Built jar `latitude-2.0-beta.1+26.2.jar`
(sha256 `a9f0ce4e455ffc95f577f6b72ae73da23ff34876af21dd734bafd7145cb2ce55`) staged into the Modrinth profile
**`LATITUDE 26.2`** as **`TEST 1.jar`** (profile already had the matching `fabric-api-0.154.0+26.2.jar`). Staged
sha256 verified identical to the build output. Ready for the live client pass.

### Live test checklist (the one remaining gate — Peetsa at the keyboard)

Launch `LATITUDE 26.2` via the Modrinth App (never the Mojang launcher). Confirm:
1. Title screen loads; create a new Latitude world (world-creation screen renders + works — client mixins).
2. World enters; HUD/compass renders; latitude warnings/overlays behave.
3. Menus/screens open and close (settings, HUD studio) — the screen-management repairs.
4. `/locate biome minecraft:sulfur_caves` returns a hit (new-biome generation), and digging there shows it.
5. General biome feel across bands looks right; no crashes on load/soak/save-quit.

---

## Live client crash #1: fixed, plus a full client-mixin audit (2026-07-02, same day)

`trigger: Peetsa's first live launch of TEST 1.jar crashed at startup.`

### The crash

```
java.lang.RuntimeException: Mixin transformation of net.minecraft.client.gui.Gui failed
Caused by: MixinApplyError: Mixin [...InGameHudMixin...] FAILED during APPLY
Caused by: InvalidInjectionException: Critical injection failure: @Inject annotation on
globe$renderEwHazeBeforeHotbar could not find any targets matching
'extractHotbarAndDecorations' in net/minecraft/client/gui/Gui. No refMap loaded.
```

Full report: `crash-2026-07-02_16.59.16-client.txt` (Modrinth profile `LATITUDE 26.2`).

### Root cause

26.2 moved the entire HUD-element extraction method family off `Gui` and onto a **new class**,
`net.minecraft.client.gui.Hud` (`Gui` now just holds a `hud` field and delegates:
`Gui.extractRenderState(DeltaTracker, boolean, boolean)` internally calls
`hud.extractRenderState(GuiGraphicsExtractor, DeltaTracker)`). Verified by `javap` diff: every method name and
signature in the family — `extractHotbarAndDecorations`, `extractRenderState(GuiGraphicsExtractor, DeltaTracker)`,
`extractCrosshair`, `extractEffects`, `extractItemHotbar`, etc. — is **identical**, just moved wholesale to `Hud`.
This is exactly the class of bug the plan warned about: mixin injection targets are strings resolved at
class-load time, not checked by `compileJava`, and `Gui`/`Hud` are client-only classes a headless server run
never loads — so nothing in the entire prior proof suite could have caught this.

**Fix:** retarget `@Mixin(Gui.class)` → `@Mixin(Hud.class)` in `InGameHudMixin`. No other change (commit
`2133372e`).

### Full client-mixin audit (to avoid a second crash cycle)

Per Peetsa's standing instruction to finish the whole phase before another live test, every **registered**
client mixin (per `globe.mixins.json`'s `client` list — 11 total) was manually checked against the real 26.2
jar via `javap`, not just the one that crashed:

| Mixin | Target | 26.2 verdict |
|---|---|---|
| `CreateWorldScreenMixin` | `CreateWorldScreen.init()` | exists, matches |
| `CreateWorldScreenInitRedirectMixin` | `CreateWorldScreen.init()` | exists, matches |
| `WorldCreatorMixin` | `WorldCreationUiState.updatePresetLists()` + shadowed fields | exists, matches |
| `WorldRendererWorldBorderMixin` | `WorldBorderRenderer.render(WorldBorderRenderState, Vec3, double, double)` | exists, matches |
| `SystemDetailsHardwareProbeMixin` | `SystemReport` (class only, no injectors) | exists |
| `MinecraftClientStartIntegratedMixin` | `Minecraft.doWorldLoad(LevelStorageAccess, PackRepository, WorldStem, Optional<GameRules>, boolean)` | exists, matches |
| `InGameHudMixin` | `Hud.extractHotbarAndDecorations`, `Hud.extractRenderState` | **fixed this pass** (was `Gui`) |
| `LatitudeLoadingClientTickMixin` (2nd class in `LevelLoadingScreenLatitudeOverlayMixin.java`) | `Minecraft.tick()` | exists, matches |
| `LevelLoadingScreenLatitudeOverlayMixin` | `LevelLoadingScreen.extractRenderState(GuiGraphicsExtractor,int,int,float)`, `.onClose()`, `.tick()` | exists, matches |
| `FogRendererEwMixin` | `FogRenderer.applyFog` (`@ModifyVariable`, `require=0` both) | **target method GONE** — fails safe (no crash), feature inert (see below) |
| `compat.sodium.RenderSectionManagerVisibilityMixin` | `@Pseudo` target on a Sodium class string | Sodium not installed in this profile — mixin is a designed no-op, N/A |

Four **unregistered** (dead) client mixins were also found and correctly ignored, since they're not in
`globe.mixins.json`'s `client` list and therefore never load: `CreateWorldScreenShowMixin` (also has an
unrelated pre-existing bug — a Yarn-style `MinecraftClient` descriptor string that could never have resolved on
any Mojang-mapped version; harmless only because it's dead), `EwStormWallRendererMixin`, `FogUniformPackerClampMixin`,
`FogRendererMixin`.

### New follow-up flagged: east-west fog tightening is silently inert on 26.2

26.2 restructured fog rendering: the old `FogRenderer.applyFog(...)` local-variable-based method is gone,
replaced by `setupFog(Camera, int, DeltaTracker, float, ClientLevel) -> FogData` plus a new
`FogEnvironment`/`FogMode` abstraction. `FogRendererEwMixin`'s two `@ModifyVariable` injectors target the
removed method with `require = 0` (explicitly permissive), so this **fails safe — no crash** — but the
east-west fog-tightening visual feature does nothing on 26.2 right now. This is a real design task (understand
the new `FogData`/`FogEnvironment` shape and find the right hook), not a rename, so it was **not** attempted in
this crash-fix pass. Flagged for its own card.

### What this does NOT change

The sulfur_caves fix, the worldgen mixin repairs, compileJava, full build, and headless Atlas all re-verified
green and **byte-identical** to the pre-crash-fix baseline after this change (this crash and its fix are 100%
client-side; no worldgen path was touched).

### Re-staged for live retest

Rebuilt jar (sha256 `982dd4238014161e7946812eb3087a6d0827320b1fd9b01213211a7ad405851b`), staged as **`TEST 2.jar`**
in the `LATITUDE 26.2` Modrinth profile, stale `TEST 1.jar` removed, staged sha256 verified identical to the
build. Did **not** attempt to launch the client myself (`runClient` would open a real graphical window on
Peetsa's screen from a background process — inappropriate to trigger unprompted, and is exactly the live-test
step reserved for her).

---

## Live test #2: create-screen works, plus scroll/atlas polish (2026-07-02, same day)

`trigger: TEST 2.jar launched clean — custom LATITUDE New World screen renders and creates worlds. Polish feedback followed.`

**The crash fix held.** TEST 2 launched to the title screen, the custom `LatitudeCreateWorldScreen` renders, and
world creation works — confirming the `InGameHudMixin`→`Hud` retarget and all the other client-mixin repairs
apply correctly live. That closes the "client-side repairs are compile-proven only" gap for everything except
the two still-flagged items (fog-tightening inert; a full scenic soak).

Peetsa's polish feedback on the New World screen, and what was done:

1. **Panels 2 & 3 scrolled "jerkily/snappy" at larger GUI scale; panel 1 was buttery smooth.** Root cause
   found: the interactive widgets in panels 2/3 (`ZoneRowWidget` rows, stepper `Button`s) are
   `addRenderableWidget`'d and extracted by `super.extractRenderState()` *outside* the panels' scissor blocks, so
   `updateRightLayout`/`positionSettingsStepper` hard-hide each one the instant it isn't 100% inside the viewport
   → they pop in/out. Panel 1 barely scrolls, so it never triggers. **Fix (Peetsa chose the low-risk option via
   AskUserQuestion): smooth-scroll glide.** Added float `*ScrollDisplay` that eases toward the integer `*Scroll`
   target each frame (`advanceScrollAnimation`, framerate-independent ease-out); all panel content + widgets +
   scrollbar thumbs now position from the eased display. The visibility toggle still exists but the offset moves
   continuously, so it's one smooth transition instead of a jarring jump. Commit `91eb6764`. Client-only, zero
   worldgen impact.
2. **Atlas "too small."** Raised `previewDiscFill` for every world size (e.g. TINY 0.52→0.68). Still graded by
   world size (conveys relative scale). Safe because `renderPlanispherePreview`'s `while (radius >= 18)` fit loop
   shrinks the radius until the composition fits — an over-large request can't overflow.
3. **Deferred (subjective, need live iteration):** atlas horizontal **centering** (the globe+labels composition
   is centered, which necessarily offsets the globe left by half the label column; centering the globe alone
   risks the latitude labels overflowing into the scrollbar gutter — needs Peetsa to judge direction) and the
   **right/lower frame-border angularity** (in `LatitudePlanisphereRenderer.drawAtlasFrame`).

Re-staged: rebuilt jar sha256 `7c51fc01c1515fbfa6124097ff958ce405d204d2a03b91c5156dc643fd70a9e2` as
**`TEST 3.jar`** (stale `TEST 2.jar` removed, staged sha verified). These are visual changes — smoothness and
atlas size need Peetsa's live eyes to confirm; the scroll pop-vs-glide and atlas centering may take a round or
two since they can't be verified headlessly.

---

## /latdev commands restored for testers (2026-07-02, same day)

`trigger: Peetsa: "all of the /latdev commands are absent in this version."`

**Not a 26.2 regression.** By design the full `/latdev` (`dev.LatitudeDevCommand`, with the seam auditor + PNG
exporter) only registers in a dev environment, and the shippable subset (`LatitudeDevCommands` — `here`,
`probe`, `tpband <band> [center|low|high]`, `tpedge <west|east> [frac]`) was gated behind an opt-in
`-Dlatitude.devCommands=true` JVM flag that the Modrinth profile didn't carry. So the staged jar simply never
registered them.

**Peetsa chose (AskUserQuestion): bake into beta builds.** `LatitudeDevCommands.registerIfEnabled` now
auto-registers the subset when the mod version is a pre-release (contains beta/alpha/rc/pre/snapshot), stays off
in a dev env (full command owns `/latdev` there) and off for stable releases, with `-Dlatitude.devCommands=true/
false` as an explicit override either way (commit `47c6c5ad`). Commands still require cheats/op. Built jar
version is `2.0-beta.1+26.2` → auto-enabled. Staged as **`TEST 4.jar`**. The subset covers the live-testing set;
the heavy tools (seam audit, PNG export) stay dev/headless-only intentionally.

Note: this couldn't be verified headlessly — the `runBiomePreview` server IS a dev environment, so it takes the
full-command path, not the subset. Confidence rests on the compile + the version-string check (`2.0-beta.1`
contains "beta") + the standard Fabric `getModContainer("globe")` metadata lookup. Peetsa's live `/latdev help`
is the confirmation.

---

## Live test #3 feedback: atlas border, scroll clipping, tabbed heading (2026-07-02)

`trigger: Peetsa screen recording (couldn't open it — macOS Desktop TCC blocks the sandboxed shell — worked from her written description + the actual texture).`

1. **Atlas frame border wrong on right/bottom (top/left fine, changes per size).** Root cause found by
   extracting the texture: `map_background.png` is **64×64**, but `drawAtlasFrame` passed `128, 128` as the
   texture size to the 1:1-mapping `blit` overload — so the frame mapped texels 1:1 into a claimed-128 space
   that was really 64px, and the right/bottom of the frame sampled *past* the real texture (garbage that shifted
   with each atlas dimension). Fixed with the region-blit overload (dest size separate from source region) to
   stretch the full 64×64 texture cleanly over the frame box. Commit `18a82995`.
2. **Scroll "snap" persisted** despite the smooth-glide, because (as Peetsa nailed it) panels 2/3 show "no half
   sentence" — the glide moved the offset but the widgets still popped whole. Fixed for panel 2: `ZoneRowWidget`
   visibility gate is now INTERSECT and `extractWidgetRenderState` scissors each row to the panel viewport, so
   rows slide and clip into "half rows" at the edge like panel 1's text. Rows are click-active only while fully
   visible.
3. **Redundant "Spawn Zone" title in tabbed mode** removed (the tab strip labels the pane) and its reserved
   height zeroed so content moves up; three-column mode unchanged.

**Deferred: panel 3 (Rules) still pops** — its 14 stepper/toggle widgets are vanilla `Button`s that can't
self-clip like the inner-class `ZoneRowWidget`; clipping them needs a `Button` subclass across all 14, so it was
held until the panel-2 clip technique is confirmed good live (validate the low-risk case before replicating a
riskier change blind). Staged **`TEST 5.jar`** (sha `d8c01ed0…`). All three changes are client-only and
visual — need Peetsa's live eyes; the atlas-border fix is high-confidence (clear texture-size bug), the
row-clipping and heading are best-reasoned-but-unverified.

---

## Live test #4 feedback: Rules panel clipped scrolling confirmed good, extend it (2026-07-03)

`trigger: Peetsa confirmed the panel-2 clip technique looks right live — go ahead and give panel 3 the same treatment.`

Panel 2's technique validated the deferred item above, so it was replicated for the Rules panel's 14 vanilla
`Button`s. 26.2 sealed `Button`'s own rendering (`extractWidgetRenderState` is final, `extractContents`
package-private), so a self-clipping `Button` subclass — the approach that worked for `ZoneRowWidget` — isn't
possible here. Instead, the fix follows vanilla's own `AbstractSelectionList` pattern: the 14 buttons are added
via `addWidget` (get input/focus/narration, but are NOT auto-rendered by `super`) and drawn manually by
`renderSettingsScrollWidgets` *inside* the Rules panel's scissor, so they clip at the viewport edges exactly
like the panel's labels already did. Visibility gate switched from fully-contained to INTERSECT so partial
buttons render (and take clicks) while fully-off-viewport ones don't; buttons still receive input through the
screen's existing `super.mouseClicked/Dragged/Released` children dispatch, and render pixel-identical to vanilla
(same `extractRenderState` call, just invoked manually at the right time). Commit `b1e9a68b`, staged
**`TEST 12.jar`**. Client-only, needed Peetsa's live eyes to confirm every Rules button (steppers, toggles, Game
Rules, HUD Studio) still scrolls, clips, and responds correctly — the Settings screen and HUD Studio's own lists
still popped at this point and would get the same treatment once this was confirmed good.

## Live test #5 feedback: World Shape relocation, compass N scaling (2026-07-03)

`trigger: Peetsa: "I'd like the world shape (1:1 vs 2:1) to be on this panel/tab, above world size... Do you see how the 'N' overtakes the whole compass when it is scaled down?"`

1. **World Shape (Mercator 2:1 / Legacy 1:1) moved from the Rules panel to the World panel**, positioned above
   World Size — reads more naturally there as a "what kind of world is this" property than buried in Rules.
   Moved wholesale, not duplicated: stepper creation, layout math (new `worldShapeFieldY` row between Seed and
   World Size, pushing everything below down one row), visibility/active gating (still greyed out for
   non-Latitude world types), tabbed-mode visibility (toggles with the World tab now, not Rules), and rendering
   all moved together. Rules panel's row count dropped from 9 to 8 accordingly.
2. **Analog compass "N" glyph now scales with the compass radius** instead of a fixed vanilla-font size. At
   small analog sizes (the slider goes down to radius 8) the unscaled letter was as tall as the whole disc.
   Uses the same `pose().pushMatrix()/translate()/scale()/popMatrix()` pattern already established in
   `LatitudeSettingsScreen.drawScaledCenteredText`: `scale = clamp(radius / 24.0, 0.4, 1.0)` — 24 was the
   pre-2.0 default radius where the unscaled letter looked proportionate, with a 0.4x floor so it stays visible
   at the smallest compass. Still anchored just under the north tick (which already scales with radius).

Commit `a9bd94ee`, staged **`TEST 13.jar`**. Verified: `compileJava` + `build` green; headless Atlas
byte-identical (both changes are client-only, no worldgen impact).

## HUD Studio prominence + rainbow lettering (2026-07-03)

`trigger: Peetsa: "I want HUD Studio to be more prominent... perhaps it should be the first button... change the letter colors to rainbow."`

New shared `RainbowText` utility (`com.example.globe.client`): draws text with each non-space character cycling
through a 7-color ROYGBIV-ish palette tuned to stay legible on the mod's dark panels. Used by both the Latitude
Settings screen and the create-screen Rules panel so the palette/logic lives in exactly one place — deliberately
avoiding the "three independent copies of the compass defaults" drift bug fixed earlier this session. Mechanism:
vanilla `Button` forces its own label white and 26.2 sealed its render pipeline, so a button's text can't be
recolored directly — both HUD Studio buttons get a blank `Component` message, and `RainbowText.drawCentered`
paints the real "HUD Studio" lettering on top right after the button renders (still inside the Rules panel's
scissor on the create screen, so it clips consistently with everything else there). Reordering: Settings screen
moves HUD Studio's entry to the very top of its list (was last); create-screen Rules panel moves its row to be
first, shifting World Type and everything below down by one row. Removes the earlier gold-frame decoration on
the Settings screen (superseded by the rainbow text). Commit `b3f06bfa`, staged **`TEST 14.jar`**. Verified:
`compileJava` + `build` green; headless Atlas byte-identical.

## Rules panel double-layout fix + HUD Studio reposition (2026-07-03)

`trigger: Peetsa: "On world creation menu third panel, we have like two layers that are scrolling. Also, the HUD studio should be under 'Starting Compass'."`

**"Two layers scrolling" root cause**, found by comparing all three panels' layout-call structure: World and
Spawn Zone each call their own layout function exactly once per frame (top of `extractRenderState`,
unconditionally). Rules was the *only* panel that called its layout function a **second time**, redundantly,
inside its own tab-conditional render block — `updateSettingsLayout()` at the top of `extractRenderState`
already computes and applies every widget's position/visibility for the current frame regardless of active tab,
so the second call was recomputing and re-applying (via `.setRectangle` on every button) the exact same thing a
second time per frame. Fixed by removing the redundant call, matching how World/Spawn Zone already worked. Also
repositioned HUD Studio's row to sit right after "Starting Compass" instead of first, on this screen only (the
pause-menu Settings screen keeps HUD Studio first, unchanged). Commit `53ccd98a`, staged **`TEST 15.jar`**.
Verified: `compileJava` + `build` green; headless Atlas byte-identical.

## Biome display + full HUD detachability (2026-07-03)

`trigger: Peetsa: "put an option to put the biome in the compass HUD... they should be detachable and moved around at will. All the HUD elements should be allowed to snap-to grid or free move."`

New `CompassHudConfig` fields: `displayBiomeInHud`, `biomeFollowsCompass`, `biomeHAnchor`/`VAnchor`,
`biomeOffsetX`/`Y` (mirrors the existing zone/band fields exactly), `biomeBeforeZone` (order toggle for when
both zone and biome are attached to the compass line), and `coordsFollowsCompass`/`coordsHAnchor`/`VAnchor`/
`coordsOffsetX`/`Y` — coords (lat/lon) were previously always fused to the compass with no detach concept at
all; now they get the same follow/detach/anchor/offset shape. `CompassHud.java`: made
`BiomeSamplerTools.biomeDisplayName` public and reused it (no duplicate name-formatting logic); added
`biomeLabel`/`sampleBiome` mirroring `zoneLabel`/`sampleZone`; added `attachedZoneBiomeLive`/`Sample` +
`joinOrdered` to combine zone+biome into one order-respecting "attached text" segment, replacing ~9 duplicate
`zoneFollowsCompass ? sampleZone(cfg) : null` call sites with one source of truth; added `coordsText`/
`coordsSample` canonical formatters; added `computeBiomeBounds`/`renderDetachedBiome` and
`computeCoordsBounds`/`renderDetachedCoords` mirroring the proven zone pattern; fixed two now-stale
`cfg.displayZoneInHud && cfg.zoneFollowsCompass` guards that would have wrongly hidden biome-only text.
`LatitudeHudStudioScreen.java`: new `DragElement.BIOME`/`COORDS` with matching hit-test/drag/release handling;
new sidebar controls (Display Biome in HUD, Biome Placement, Zone/Biome Order, Coords Placement) plus a
**Dragging control exposing the previously-unexposed `LatitudeConfig.hudSnapEnabled`/`hudSnapPixels`** — this
config already existed and was already wired into every drag handler, but had zero UI, so players could never
turn "snap only" off. `applyDefaults(CompassHudConfig)`/`resetHudDefaults()` updated with the new fields
(avoiding the "independent copies of defaults drift apart" bug fixed earlier). Full review pass confirmed
`LatitudeHudAdjustScreen.java` was dead code (never instantiated) — intentionally left untouched at this point
(deleted in the next round below). Commit `0d65a0a7`, staged **`TEST 16.jar`**. Verified: `compileJava` + full
`build` green; headless Atlas byte-identical.

---

## RGB color pickers + tabbed HUD Studio redesign, then a full title-styling + polish arc (2026-07-03)

`trigger: Peetsa: "It looks absolutely beautiful!! Please apply the RGB sliders to the titles also..." (after "I need the RGB picker now for text colors/compass custom color scheme").`

This was a 4-commit arc, each fully verified (`compileJava` + `build` + headless `runBiomePreview`
byte-identical) and staged as its own test jar. Full technical detail for each lives in its own dated doc; this
entry is the continuity summary tying them into the pivot's running log.

1. **`90a5fbbe` — RGB color pickers + tabbed HUD Studio redesign** (staged `TEST 17.jar`). New 12th `CUSTOM`
   analog compass theme with independent RGB pickers (3 sliders + live swatch) for face/ring/muted/needle;
   an RGB picker for HUD text color; a previously-unexposed text-opacity slider. HUD Studio restructured from
   a flat scrolling list gated by a "Target: Compass/Title/Both" button into **4 tabs** (Compass/Placement/
   Title/General) with the same themed-card look (bordered panel, gold accents, heading) as the Settings and
   World Creation screens — widgets are now constructed per-tab (mirroring the pre-existing analog/digital
   branching) rather than always-constructed-with-visibility-toggle. Also rolls in the removal of
   `LatitudeHudAdjustScreen.java` (confirmed dead, zero call sites, flagged in the prior round). Full detail:
   `docs/binder/hud-studio-custom-theme-20260703.md`.
2. **`9000dd49` — Title RGB/preset/rainbow colors + text case + General tab additions** (staged `TEST 18.jar`).
   Zone-enter title text gets the same styling depth as the compass: 6 color presets (White/Gold/Red/Cyan/
   Green) plus Custom (RGB sliders + swatch) plus **Rainbow** (per-letter cycling, reusing `RainbowText` via a
   new alpha-aware overload so the title's fade-in/out still works), plus a letter-case selector (Normal/
   UPPERCASE/lowercase/**Mocking** — alternates capitalization per letter, skipping spaces/punctuation without
   breaking the alternation). Tracing the two live render paths (`ZoneEnterTitleOverlay.render()` for real
   gameplay vs. `renderStaticAt()` for the HUD Studio preview) to apply this consistently found they were
   independently duplicated with zero shared code — refactored both onto one shared `drawStyledTitle`/
   `applyCase` helper pair, and deleted two confirmed-zero-caller dead overloads (`renderStatic`, the 4-arg
   `renderStaticAt`). Title tab also gained direct access to settings previously reachable only from the
   separate pause-menu Settings screen (Zone Title on/off, duration, whether the title shows the degree
   readout); General tab gained a Title Draggable toggle (the config field existed and was already read by the
   drag handler, but had zero UI anywhere until now). `LatitudeConfig`'s dual-representation GSON pattern
   (public static + private "Value" fields, no `fresh()`-style factory unlike `CompassHudConfig`) required the
   new fields threaded through both of `load()`'s branches, `save()`, and `sanitize()`; both hardcoded-defaults
   copies (`LatitudeSettingsScreen.applyDefaults(LatitudeConfig)`, HUD Studio's `resetHudDefaults()`) updated in
   lockstep. **Spun off a background investigation** (`task_7003cfac`, not yet resolved): `ZoneEntryNotifier`/
   `ui.ZoneTitleOverlay` turned out to be a completely unreachable parallel title-notification system — zero
   callers on both the trigger and render sides — likely a pre-`GlobeWarningOverlay` implementation that was
   superseded but never deleted.
3. **`0600661e` — Title Normal case, letter spacing, tab strip overflow fixes** (staged `TEST 19.jar`). Live
   feedback caught that "Normal" title case was silently identical to UPPERCASE: the source text was forced
   `.toUpperCase()` before `applyCase()` ever ran, in 3 places — `LatitudeHudStudioScreen.zoneTitleWord()` (HUD
   Studio preview), `GlobeWarningOverlay.buildZoneEnterTitle()` (real zone-enter title), and the hardcoded
   `"NORTHERN/SOUTHERN HEMISPHERE"` literals (real hemisphere-crossing title) — all three now use natural case
   ("Tropics", "Northern Hemisphere"), leaving `applyCase()` as the only place casing is decided. Added a Letter
   Spacing slider (-4 to +16px) to the Title tab, which required switching `ZoneEnterTitleOverlay`'s single-call
   `ctx.centeredText` draw to a per-character `drawSpacedText` loop for both the solid-color and Rainbow paths
   (one styling code path instead of a branch that could drift); `RainbowText` gained a `paletteColor(int)`
   accessor for reuse. Also fixed the "Placement" tab label overflowing its button's borders: widened the HUD
   Studio sidebar 180→208px and scaled tab labels to 0.85x.
4. **`61d51782` — Rainbow Text for compass/zone/biome/coords** (staged `TEST 20.jar`, current HEAD). New
   `CompassHudConfig.textRainbow` toggle overrides the Text Color preset/RGB sliders with a per-letter rainbow
   cycle, wired through `CompassHud`'s existing shared `drawText()` helper (already used by the analog-attached
   line and all 3 detached zone/biome/coords labels) plus `renderDigitalAt()`'s per-line loop for digital mode,
   via a new `drawRainbowLeftAligned()` reusing `RainbowText.paletteColor()` — one addition covers every place
   this text renders. Preserves the existing Text Opacity fade by carrying the alpha byte through from
   `textArgb()`. Both `applyDefaults(CompassHudConfig)` copies synced with the new field.

**Current state at the end of this arc:** HEAD `61d51782`, staged jar `TEST 20.jar` in the Modrinth profile
`LATITUDE 26.2`, all local commits (not pushed). Every round in this arc is a pure client-UI/HUD change with
zero worldgen impact, each individually confirmed via a byte-identical headless `runBiomePreview` diff against
the prior commit.
