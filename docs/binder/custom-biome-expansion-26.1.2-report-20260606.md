# Custom-biome compatibility expansion — 26.1.2 (Stage 3)

**Branch:** `feat/custom-biome-expansion-26.1.2` (off the released `feat/1.3.1-cohesive-horizons-26.1.2` @ `691e54b4` — the released branch itself was **NOT** touched).
**Version:** `mod_version = 1.4.1-beta.1+26.1.2` (1.4.0 already shipped; this is the next content increment, beta until jar-validated).
**Workflow:** `latitude-regression-fix-workflow`. Date 2026-06-06, autonomous (Opus).

## What this adds

Stage 3 broadens the proven Stage-2a custom-biome **TAG admission** pattern (`{"id":"<mod>:<biome>","required":false}` into the per-band `lat_*` worldgen biome tags) to four more popular Fabric biome mods, on top of the already-admitted BoP / Terralith / Promenade / Biomes We've Gone set that ships on the released branch.

**Mods added (101 new `required:false` entries):**

| Mod | namespace | entries | ID source |
|---|---|---|---|
| Oh The Biomes You'll Go (BYG) | `byg` | 51 | `Potion-Studios/BYG` `BYGBiomes.java` (overworld + beach; nether/end excluded) |
| Terrestria | `terrestria` | 20 | `TerraformersMC/Terrestria` `TerrestriaBiomes.java` |
| Wilder Wild | `wilderwild` | 23 | `WilderWild-4.2.9-mc26.1.jar` worldgen/biome/*.json (temp/downfall verified from jar) |
| Traverse | `traverse` | 7 | `TerraformersMC/Traverse` `TraverseBiomes.java` |

All entries `required:false` (absent-mod-safe). Climate placement is by temperature/humidity/name analog into the existing band semantics (subpolar=boreal, temperate=mid-lat forest, `_mountain`=alpine, `_accent`=meadow/floral, arid=hot desert, trans_arid_tropics=savanna, tropics=jungle/rainforest). Applied idempotently via `scripts/apply_custom_biome_tags.py`.

## Proof (headless atlas, NO chunk pregen)

Seed `2533348776566713405`, `--size=small --radius=7500 --step=16 --emitbiomeindex=true`. JDK temurin-25.

### (1) Absent-mod-safe + generation-neutral — VANILLA atlas (no provider mods)
With all 139 custom `required:false` entries present and NO provider mods, the datapack loads without error and worldgen is **generation-neutral**:
- 46 distinct vanilla biomes present, **0 non-`minecraft:` biomes** appear (the custom entries are inert when their mods are absent).
- Equator jungle-dominant (0–5° wet 58.7%, savanna 33.0% present) — **not a monoculture**.
- No vanilla biome vanished — **Art X clean**.

### (2) Mod-present placement — REAL provider mods loaded (BoP + Terralith + Promenade)
The 26.1.2 line carries real version-matched jars (BoP 26.1.2.0.2 + TerraBlender + GlitchCore, Terralith 2.6.2, Promenade 5.5.0). Atlas with these loaded as actual mods + datapacks:
- **77 distinct biomes** present: 45 vanilla + **20 BoP + 9 Terralith + 3 Promenade**, each in its **correctly-tagged band** (e.g. `biomesoplenty:tundra`→subpolar, `biomesoplenty:tropics`→tropical, `terralith:siberian_taiga`→subpolar, `promenade:glacarian_taiga`→subpolar, `promenade:blush_sakura_grove`→temperate).
- Controlled share — custom biomes coexist with vanilla; vanilla `jungle` (32773 hits, tropical), `savanna`, etc. all remain. **Art X clean, no monoculture.**

This proves the full admission chain (`lat_*` tags → `LatitudeBiomes` band selection → existing source-wrap) end-to-end for the mods whose jars resolve. The four NEW mods use the identical chain and entry shape, so they are **proven-safe-to-load** (the vanilla run confirms generation-neutrality) and their in-band placement is `NEEDS-JAR-VALIDATION` (see below).

## Per-mod status

- **BoP / Terralith / Promenade — PROVEN** (atlas-validated, mod-present, this run). Consistent with the released-branch set.
- **Wilder Wild — NEEDS-JAR-VALIDATION.** A version-matched jar (`WilderWild-4.2.9-mc26.1.jar`) IS present, but it `depends` on **FrozenLib** (`>=2.4.4`), which is not on this machine — Fabric refuses to load WW without it, so its in-band placement could not be exercised in-atlas (anti-spiral: did not chase the external dep chain overnight). Its 23 entries are climate-placed from the jar's own `temperature`/`downfall` values (the most precise of the four), so placement is high-confidence; only the runtime in-band proof is outstanding. Action for Julia: drop FrozenLib + Fabric API into `run/mods` and re-run with `-Platdev.syncPreviewMods=true`.
- **BYG / Terrestria / Traverse — NEEDS-JAR-VALIDATION.** No resolvable 26.1.2 jars on this machine (BYG/Terrestria/Traverse have no published 26.1.2 Fabric build found in the Stage-3 pass). IDs are from each mod's source (`*Biomes.java`), so they are real registry IDs; placement is best-effort by analog. They do not affect vanilla worldgen (proven generation-neutral).

## NEEDS-JULIA-PLACEMENT-REVIEW (ambiguous bands — best guess made, flagged in `scripts/apply_custom_biome_tags.py` with `# REVIEW`)

- `byg:jacaranda_forest` — jacaranda is subtropical IRL; placed `temperate_secondary` (flowering analog).
- `byg:crag_gardens` — rocky garden; placed `temperate_mountain` (alpine guess).
- `byg:temperate_rainforest` — wet; placed `tropics_secondary` (could be temperate-humid).
- `byg:lush_stacks` — lush wet stacks; placed `tropics_secondary` (guess).
- `byg:coniferous_forest` — name implies cold, but BYG registers it warm/humid; left in `tropics_secondary` — verify climate.
- `terrestria:oasis` — desert water-feature; placed `tropics_secondary` (humid) — could be `arid_accent`.
- `terrestria:volcanic_island` — placed `tropics_secondary` (tropical-island guess).
- `traverse:lush_swamp` — warm wetland; placed `tropics_secondary` (could be temperate).

UNADMITTED on purpose (no confident land band; need a coast gate): `byg:rainbow_beach`, `byg:basalt_barrera`, `byg:dacite_shore`.

## Guardrails

Branches only. The released `feat/1.3.1-cohesive-horizons-26.1.2` (`691e54b4`) and `main` (`4ccb9e9e`) are untouched. No tags pushed (local `save/*` only). No GitHub releases / Modrinth. `run-headless/server.properties` (`max-tick-time=-1`) is a local-only atlas tweak, NOT committed. The downloaded provider jars under `run/mods` are gitignored.
