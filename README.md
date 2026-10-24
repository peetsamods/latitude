# Latitude

A globe-style world + latitude-based biome bands with a customizable compass HUD and warnings.

## Current version

This checkout builds Latitude `1.5.3+26.1-neoforge` for Minecraft **26.1.2** on **NeoForge**,
using Java 25. See [the changelog](CHANGELOG.md) for this version's changes and
[version naming](docs/versioning.md) for the shared release contract.

## Features

- **Latitude biome bands** via biome tags under `globe:lat_*`.
- **Warning overlays** (e.g. pole / edge warnings).
- **Compass HUD**
  - Toggle compass: `,` (comma)
  - Open HUD Studio: `F9`
  - Reassign either shortcut in Minecraft's Controls menu
  - Fully configurable: anchors, offsets, scale, background alpha, colors, show modes, direction modes

## Design Spec / Release Gate
See: docs/design-spec.md

## Guardrails / Workflows
- Savepoint Autopilot: `.windsurf/workflows/latitude-savepoint-autopilot.md`

## Dependencies

- Minecraft 26.1–26.1.2
- Fabric Loader 0.17.3 or newer
- Fabric API
- Java 25 or newer

## Biome tag integration

This mod selects biomes for latitude bands via a family of `globe:lat_*` biome tags — separate
primary/secondary/accent tiers per latitude band, plus dedicated tags for rivers, beaches, oceans,
and wetlands. See `src/main/resources/data/globe/tags/worldgen/biome/` for the current full set.

Biome mods can integrate by adding their biomes into the relevant tags.

## Config

Compass HUD configuration is stored in:

- `config/globe_compass_hud.json`

## Building

```bash
./gradlew -p neoforge build
```

(Windows: `gradlew.bat -p neoforge build`.)

The native NeoForge runtime JAR is:

- `neoforge/build/libs/latitude-neoforge-1.5.3+26.1-neoforge.jar`

Do not upload development or sources JARs as the playable release file.
The root project also supports the Fabric compatibility build used for shared-source checks.

## Support

ko-fi.com/peetsa
