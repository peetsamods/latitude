# Latitude

A globe-style world + latitude-based biome bands with a customizable compass HUD and warnings.

## Latitude 2.0 overhaul front door

The current planning front door is `docs/LATITUDE_2_0_OVERHAUL.md`.

That document supersedes older "Mercator", E/W wrap, and ocean-seam design records for the Latitude 2.0 overhaul direction. The 2.0 plan keeps the 2:1 projected-planet foundation and starts with portability plus Atlas geography measurement before any visible continent/climate behavior changes. Its canonical Minecraft target moved from `26.2` to `26.3` (maintainer ruling, 2026-09-12).

## Status

This checkout tracks the Latitude 2.0 line on Minecraft 26.3 (`2.0-beta.2+26.3-rc-2`, branch
`port/2.0-26.3-fabric`). It absorbs every fix shipped on the 1.5 line through `1.5.1-beta.5` and the
1.5 port to Minecraft 26.3. See `CHANGELOG.md` for what changed.

## Features

- **Latitude biome bands** via biome tags under `globe:lat_*`.
- **Warning overlays** (e.g. pole / edge warnings).
- **Compass HUD**
  - Toggle keybind: `K`
  - Open settings: `F9`
  - Preview mode toggle: `P`
  - Alt+Left-Click a compass icon in inventory to toggle (shows a red X when disabled)
  - Fully configurable: anchors, offsets, scale, background alpha, colors, show modes, direction modes

## Design Spec / Release Gate
See:

- `docs/LATITUDE_2_0_OVERHAUL.md` for the Latitude 2.0 overhaul plan.
- `docs/design-spec.md` for the existing design spec.
- `docs/release/history/1.4/checklist.md` for release gates.

## Dependencies

- Fabric Loader (Minecraft 26.3)
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

For Latitude 1.4 readiness, build from the canonical `26.1.2` worktree named above, not from this historical `1.21.11` checkout.

## Support

ko-fi.com/peetsa

## License

Latitude — a Minecraft mod.
Copyright (C) 2026 Peetsa

This program is free software: you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this
program. If not, see <https://www.gnu.org/licenses/>. The full text is in
[`LICENSE`](LICENSE).

*(Previously MIT through early 2026; relicensed to GPL-3.0-or-later going forward.
Versions already published under MIT remain available under MIT.)*
