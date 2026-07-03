# Phase 0 (Portability Foundation) — no-op scaffolding proof

`date: 2026-07-03` · `branch: port/canonical-26.2-pivot` · `scope: Phase 0 only, per docs/LATITUDE_2_0_OVERHAUL.md`

Ran the Phase 0 kickoff slice prompt from `docs/LATITUDE_2_0_OVERHAUL.md`. Objective: split pure world
logic from Minecraft adapter/mixin logic per `docs/porting/PORTABILITY_ARCHITECTURE.md`'s 5-layer target
structure, add no-op `GeoSummary`/`ClimateSummary` contracts and thin adapter interfaces, all behind
disabled flags, with a flag-off byte-identical proof. No GeoAuthority/ClimateAuthority algorithm work —
that is Phase 2/3.

## Preflight

Confirmed before any edit: branch `port/canonical-26.2-pivot`, HEAD `79c0729e` (descendant of
`93c21a6a`), clean tree, tag `save/canonical-26.2-baseline` present at `93c21a6a` and pushed.

## What was added

### Core Logic layer (pure Java, zero Minecraft imports)

- `com.example.globe.core.LatitudeV2Flags` — the two disabled-by-default flags:
  - `latitude.geoV2.enabled` (default `false`)
  - `latitude.climateV2.enabled` (default `false`)
- `com.example.globe.core.geo.GeoSummary` — record with all 15 fields from the architecture doc's
  `GeoSummary` contract (`land01` through `coastOutletId`). `GeoSummary.NEUTRAL` is a constant neutral
  instance (all `*01` fields `0.0`, all `*Id` fields `-1`, `isOceanIntent=false`) — the only instance
  produced while `geoV2` stays off.
- `com.example.globe.core.climate.LatitudeBand` — enum reusing the same five band names
  (`TROPICAL`/`SUBTROPICAL`/`TEMPERATE`/`SUBPOLAR`/`POLAR`) that `LatitudeBiomes`' existing `BAND_*`
  constants and `LatitudeBands.Band` already use, so this doesn't invent a second band taxonomy.
- `com.example.globe.core.climate.ClimateSummary` — record with all 14 fields from the architecture
  doc's `ClimateSummary` contract. `seasonalityClass`/`climateClass` are left as empty strings rather
  than enums, deliberately — Phase 3 is the phase that designs that taxonomy, not Phase 0.
  `ClimateSummary.neutral(latitudeDeg, band)` zeros every climate signal but keeps the real
  latitude/band (those are already cheaply known at the call site, not a new algorithm).

### Platform Adapters layer (thin, Minecraft-facing)

Four adapter interfaces, each with one trivial pass-through implementation (`Default*Adapter`), per
the architecture doc's four listed adapter concerns:

- `com.example.globe.adapter.biome.BiomeRegistryAdapter` / `DefaultBiomeRegistryAdapter` — wraps
  `Registry<Biome>#get(Identifier)`.
- `com.example.globe.adapter.biome.BiomeTagMembershipAdapter` / `DefaultBiomeTagMembershipAdapter` —
  wraps `Holder<Biome>#is(TagKey)`.
- `com.example.globe.adapter.biome.BiomeHolderConversionAdapter` /
  `DefaultBiomeHolderConversionAdapter` — wraps `Holder<Biome>#unwrapKey()`.
- `com.example.globe.adapter.climate.ClimateSamplerAdapter` / `DefaultClimateSamplerAdapter` — wraps
  `Climate.Sampler#sample(int, int, int)`.

Plus the two summary-provider seams that the disabled call sites actually invoke:

- `com.example.globe.adapter.geo.GeoSummaryProvider` / `NoOpGeoSummaryProvider` (always returns
  `GeoSummary.NEUTRAL`) — the seam Phase 2 replaces.
- `com.example.globe.adapter.climate.ClimateSummaryProvider` / `NoOpClimateSummaryProvider` (always
  returns `ClimateSummary.neutral(...)`) — the seam Phase 3 replaces.

None of the four registry/tag/holder/sampler adapters are wired into a call site yet — they exist as
shells only, per the working card ("interfaces plus trivial pass-through implementations only, no new
algorithm"). Only the two summary providers are wired, and only behind the disabled flags.

### Disabled-by-default call site

Both overloads of `LatitudeBiomes.pick(...)` (the `Registry<Biome>` entry point used by the live
worldgen mixin path, and the `Collection<Holder<Biome>>` entry point used by Atlas/headless callers)
got the identical addition, right after `bandIndex` is computed:

```java
if (LatitudeV2Flags.GEO_V2_ENABLED) {
    GeoSummaryProvider geoSummaryProvider = NoOpGeoSummaryProvider.INSTANCE;
    geoSummaryProvider.summarize(blockX, blockZ);
}
if (LatitudeV2Flags.CLIMATE_V2_ENABLED) {
    ClimateSummaryProvider climateSummaryProvider = NoOpClimateSummaryProvider.INSTANCE;
    climateSummaryProvider.summarize(
            latitudeDegreesFromRadius(blockZ, effectiveRadius), LatitudeBand.valueOf(band.name()));
}
```

Both flags default `false`, so both branches are dead code today; the result is discarded either way.
Wiring both overloads (rather than just one) matches this file's existing pattern of maintaining
parallel `Registry`/`Collection` implementations side by side, and avoids a future inconsistency where
flipping a flag behaves differently depending on which `pick()` overload a caller happens to use.

### Test infrastructure

No JUnit setup existed in this repo before this slice (Fabric Loom projects don't ship one by default).
Added to `build.gradle`:

```groovy
testImplementation platform('org.junit:junit-bom:5.11.3')
testImplementation 'org.junit.jupiter:junit-jupiter'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

plus a `test { useJUnitPlatform() }` block. This only adds a `test` source set; it does not touch Loom's
`minecraft`/`fabric-api` wiring, and the new tests do not reference any Minecraft type (that's the point —
they prove the Core Logic layer has zero Minecraft dependency by compiling and running with no Minecraft
on the test classpath at all).

12 tests added, covering `LatitudeV2Flags` defaults, `GeoSummary`/`ClimateSummary` neutral values, and the
two `NoOp*Provider` implementations. All 12 pass, 0 failures.

## Proof gate results

1. **`compileJava`: green.** One fix needed along the way: `Registry<Biome>#get(Identifier)` in 26.2
   returns `Optional<Holder.Reference<Biome>>`, not `Optional<Holder<Biome>>` — Java generics don't widen
   that automatically, so `DefaultBiomeRegistryAdapter` needed an explicit
   `.map(reference -> (Holder<Biome>) reference)`.
2. **New pure-JVM tests: green.** `./gradlew test` — 12/12 passed, 0 failures, 0 skipped (see
   `build/test-results/test/*.xml`).
3. **Headless Atlas + exact-ID proof: byte-identical against `save/canonical-26.2-baseline`.**
   - Command (both runs): `env -u JAVA_TOOL_OPTIONS ./gradlew --no-daemon --console plain runBiomePreview
     --args="--seed 42424242 --radius 2000 --step 64 --emitBiomeIndex true --out <dir>"` (requested
     radius 2000 was outside the canonical preview radii and clamped to the default 10000 — identically
     on both runs, so this doesn't affect the comparison).
   - Baseline: temporary `git worktree add <scratch> save/canonical-26.2-baseline` (HEAD `93c21a6a`),
     removed after the run (`git worktree remove --force`) — no baseline files were touched.
   - Candidate: current worktree at HEAD `79c0729e` (Phase 0 changes, both flags at their disabled
     defaults).
   - Result: all 16 exported artifacts byte-identical by SHA-256 (`biome_ids.png`, `biome_palette.json`,
     `biomes.png`, `chosen_bands.png`, `land_bands.png`, `legend.json`, `legend.txt`,
     `palette_authority.json`, `seam_band_legend.txt`, 3× `seam_crop_*.png`, `seam_row_markers.txt`,
     `seam_rows.txt`, `seam_temperate_composition.txt`, `world_biome_inventory.json`). The only textual
     diff anywhere was the `durationMs=` line inside `biomes.txt` (wall-clock timing metadata, not biome
     data — expected to vary run to run).

This clears the Phase 0 proof gate ("compileJava green; new pure-Java tests green; headless Atlas +
exact-ID proof byte-identical to the save/canonical-26.2-baseline tag with both flags left at their
disabled defaults") and the relevant Hard Stops ("flag-off output differs for Classic/current Longitude
at all" — it doesn't; "new authority code leaks seed/radius/shape across consecutive world loads" — the
no-op providers carry no state at all, nothing to leak).

## What Phase 0 deliberately did not do

- No GeoAuthority/ClimateAuthority algorithm — `GeoSummary`/`ClimateSummary` are pure data shapes with
  one neutral instance each.
- No wiring of the registry/tag/holder/sampler adapters into any call site — those stay unused shells
  until a Phase 2/3 (or later port) consumer actually needs them.
- Did not touch the confirmed-dead `ZoneEntryNotifier.java`/`ui.ZoneTitleOverlay.java` follow-up
  (`task_7003cfac`, still open) — flagged as its own lane, left alone per the working card.
- Did not tag or push. Local commits only.

## Files touched

- `build.gradle` (test infra)
- `src/main/java/com/example/globe/core/LatitudeV2Flags.java` (new)
- `src/main/java/com/example/globe/core/geo/GeoSummary.java` (new)
- `src/main/java/com/example/globe/core/climate/LatitudeBand.java` (new)
- `src/main/java/com/example/globe/core/climate/ClimateSummary.java` (new)
- `src/main/java/com/example/globe/adapter/biome/BiomeRegistryAdapter.java` (new)
- `src/main/java/com/example/globe/adapter/biome/DefaultBiomeRegistryAdapter.java` (new)
- `src/main/java/com/example/globe/adapter/biome/BiomeTagMembershipAdapter.java` (new)
- `src/main/java/com/example/globe/adapter/biome/DefaultBiomeTagMembershipAdapter.java` (new)
- `src/main/java/com/example/globe/adapter/biome/BiomeHolderConversionAdapter.java` (new)
- `src/main/java/com/example/globe/adapter/biome/DefaultBiomeHolderConversionAdapter.java` (new)
- `src/main/java/com/example/globe/adapter/climate/ClimateSamplerAdapter.java` (new)
- `src/main/java/com/example/globe/adapter/climate/DefaultClimateSamplerAdapter.java` (new)
- `src/main/java/com/example/globe/adapter/geo/GeoSummaryProvider.java` (new)
- `src/main/java/com/example/globe/adapter/geo/NoOpGeoSummaryProvider.java` (new)
- `src/main/java/com/example/globe/adapter/climate/ClimateSummaryProvider.java` (new)
- `src/main/java/com/example/globe/adapter/climate/NoOpClimateSummaryProvider.java` (new)
- `src/main/java/com/example/globe/world/LatitudeBiomes.java` (import + no-op call site, both `pick()`
  overloads)
- `src/test/java/com/example/globe/core/LatitudeV2FlagsTest.java` (new)
- `src/test/java/com/example/globe/core/geo/GeoSummaryTest.java` (new)
- `src/test/java/com/example/globe/core/climate/ClimateSummaryTest.java` (new)
- `src/test/java/com/example/globe/adapter/NoOpProvidersTest.java` (new)
- `docs/porting/PORTABILITY_ARCHITECTURE.md` (marked scaffolded vs. target-only contracts)

## Next

Phase 1 (Measurement Harness) is next, per `docs/LATITUDE_2_0_OVERHAUL.md`. Nothing here needs Peetsa's
input before that starts — this slice's proof gate is fully green and self-contained.
