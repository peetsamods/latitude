# Latitude

A globe-style world + latitude-based biome bands with a customizable compass HUD and warnings.

## Current version

This checkout builds Latitude `1.5.2-beta+26.2` for Minecraft **26.2** on **Fabric**,
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

- Fabric Loader (Minecraft 26.2)
- Fabric API

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
./gradlew clean build
```

(Windows: `gradlew.bat clean build`.)

The release jar to upload is in:

- `build/libs/` (the remapped main jar, not `-dev` / `-sources`)

### Native NeoForge build

The root build above produces the Fabric artifact. To build the native NeoForge artifact instead:

```bash
./gradlew -p neoforge build
```

The NeoForge main and sources JARs are written to `neoforge/build/libs/`.

## Support

ko-fi.com/peetsa
