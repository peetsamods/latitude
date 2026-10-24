# Latitude

A globe-style world + latitude-based biome bands with a customizable compass HUD and warnings.

## Current version

This checkout builds Latitude `1.5.3+1.21.11` for Minecraft **1.21.11** on **Fabric**,
using Java 21. See [the changelog](CHANGELOG.md) for this version's changes and
[version naming](docs/versioning.md) for the shared release contract.

## Features

- **Latitude biome bands** via biome tags under `globe:lat_*`.
- **Warning overlays** (e.g. pole / edge warnings).
- **Compass HUD**
  - Toggle compass: `,` (comma)
  - Open HUD Studio: `F9`
  - Reassign either shortcut in Minecraft's Controls menu
  - Fully configurable: anchors, offsets, scale, background alpha, colors, show modes, direction modes

## Design specification

See [`docs/design-spec.md`](docs/design-spec.md).

## Dependencies

- Minecraft 1.21.11
- Fabric Loader 0.17.3 or newer
- Fabric API
- Java 21 or newer

## Biome tag integration

This mod selects biomes for latitude bands via biome tags:

- `globe:lat_equator`
- `globe:lat_tropical`
- `globe:lat_temperate`
- `globe:lat_subpolar`
- `globe:lat_polar`

Biome mods can integrate by adding their biomes into these tags.

## Config

Compass HUD configuration is stored in:

- `config/globe_compass_hud.json`

## Building

```sh
./gradlew clean build
```

On Windows, use `.\gradlew.bat clean build`.

The remapped runtime JAR is:

- `build/libs/latitude-1.5.3+1.21.11.jar`

Do not upload development or sources JARs as the playable release file.

## License

Latitude is available under the [GNU General Public License v3.0 or later](LICENSE).

## Support

- [Beta testing guide](docs/testing-beta.md)
- [Report a bug](../../issues/new?template=bug_report.yml)
- [Support Latitude on Ko-fi](https://ko-fi.com/peetsa)
