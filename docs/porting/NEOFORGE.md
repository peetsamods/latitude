# Latitude on NeoForge

Latitude 1.5 builds for Fabric and NeoForge from one source tree. The Fabric build is the root Gradle
project and is unchanged in shape; the NeoForge build is a second, standalone Gradle build in
`neoforge/` that compiles the same shared sources.

## Layout

| Path | Contents | Built by |
| --- | --- | --- |
| `src/main/java` | All gameplay, worldgen, client and mixin code | both |
| `src/main/java/com/example/globe/platform` | The loader seam: `LatitudePlatform` / `LatitudePlatformService` and the client pair in `platform/client` | both |
| `src/main/java/com/example/globe/platform/fabric` | Fabric implementation of the seam and the Fabric entrypoints | Fabric only |
| `neoforge/src/main/java/com/example/globe/platform/neoforge` | NeoForge implementation of the seam and the `@Mod` entrypoints | NeoForge only |
| `src/main/resources` | Assets, data, `globe.mixins.json` | both (`fabric.mod.json` is Fabric only) |
| `neoforge/src/main/resources/META-INF/neoforge.mods.toml` | NeoForge mod metadata | NeoForge only |

Mod identity (version, group, Minecraft version) lives only in the root `gradle.properties`; the NeoForge
build reads it from there. `neoforge/gradle.properties` holds only the NeoForge version and the version
ranges written into `neoforge.mods.toml`.

## The one rule

Shared code never imports `net.fabricmc` or `net.neoforged`. When shared code needs something from the
loader (an event, a registry entry, a network payload, the config directory), add a method to
`LatitudePlatformService` (or `LatitudeClientPlatformService`), implement it in both
`platform/fabric` and `neoforge/.../platform/neoforge`, and call it through `LatitudePlatform`.

The Fabric implementation is a direct pass-through to the Fabric API, so the Fabric jar behaves exactly
as it did before the seam existed. Commands are handed over through `tools.LatitudeCommandRegistrar`
so the seam never names Brigadier (see the brigadier containment rule in
`tools/verify_phase6_dev_tooling.py`).

## Building

```
./gradlew build                 # Fabric jar: build/libs/latitude-<version>.jar
./gradlew -p neoforge build     # NeoForge jar: neoforge/build/libs/latitude-neoforge-<version>.jar
```

`./gradlew check` remains the Fabric proof gate. The NeoForge build excludes the same development
packages from its release jar as the Fabric build does.

## Where the loaders differ

- **HUD haze below the hotbar.** NeoForge replaces the vanilla hotbar method with GUI layers, so
  `InGameHudMixin`'s hotbar injector is `require = 0` and the NeoForge client registers the same
  `HotbarHazeLayer` just below `VanillaGuiLayers.HOTBAR`. Mod layers carry no hide-HUD gate, so the
  NeoForge layer checks it itself. `latitudeMixinTargetVerify` still checks the Fabric target at build
  time.
- **Registry entries** (the riparian placement modifier type) are recorded by the seam and registered
  inside NeoForge's `RegisterEvent`; NeoForge freezes registries outside it.
- **Network payloads** are registered as optional channels, so a client without Latitude can still join,
  as on Fabric. The NeoForge seam only sends a payload to a client that has the channel.
- **Level load** fires for client levels on NeoForge too; the seam forwards server levels only, matching
  Fabric's `ServerLevelEvents.LOAD`.
- **Player join** fires at the end of login on NeoForge rather than part-way through.
- **Client disconnect.** NeoForge fires `ClientPlayerNetworkEvent.LoggingOut` from every
  `Minecraft.disconnect()`, including the clean-up before a new world opens. The seam forwards only
  logouts that had a play connection, matching Fabric; otherwise the loading overlay is cleared the
  moment a new world starts.
- **Promenade** has no NeoForge build, so its palm-leaf tint compatibility only ever activates on Fabric.

## Proving a NeoForge build

1. `./gradlew check` and `./gradlew -p neoforge build` both succeed.
2. Headless Atlas on NeoForge, from a clean run directory (no world, a pre-written `server.properties`):

   ```
   ./gradlew -p neoforge runBiomePreview \
     -Platdev.preview.runDir=build/headless-runs/<name> \
     "-Platdev.biomePng=enabled=true;seed=<seed>;radius=<r>;step=<s>;y=64;layers=biome,stats,audit;emitbiomeindex=true;out=<dir>"
   ```

   Compare `biome_ids.png` with a Fabric run of the same recipe by biome name (palette indices are
   assigned in discovery order and can differ between runs).
3. Dev client smoke: `./gradlew -p neoforge runClient`. In a development run the auto-create-world
   probe opens Latitude's create-world screen, creates a world, waits for the loading overlay to
   clear, records spawn diagnostics and closes the game (`[LAT][CWPATH]` lines). A brand-new run
   directory first shows Minecraft's accessibility onboarding screen, which the probe does not
   dismiss; set `onboardAccessibility:false` in `neoforge/run/options.txt` first.
4. Live play in a NeoForge profile, with the partner mods the release supports.
