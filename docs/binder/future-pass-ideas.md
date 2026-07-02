# Future Pass Ideas

Parking lot for ideas Julia wants future agents to notice without making them active scope yet.

## Ideas

### 2026-07-02 - Arid belt dry mosaic pass
- Idea: Revisit the subtropical arid belt so it still reads as an Earth-like dry belt, but with more visible biome variety than the current savanna/desert-heavy distribution. Target a "dry mosaic" feel: desert and badlands core, dry grassland/savanna shoulders, scrub, dryland, wasteland/steppe, oasis, canyon/mesa, and rocky arid accents where installed biome-provider mods make those available.
- Full assessment: `arid-belt-dry-mosaic-assessment-20260702.md`
- Context: Current Atlas assessment found the subtropical belt is legal and coherent, but visually repetitive: subtropical land is dominated by savanna/grass-dry and desert/badlands families. Hard-arid legality is green, but provider representation is weak; vanilla desert carries most hard-arid cells while BoP/Terralith/CliffTree arid candidates are mostly cameo-level or low-weight accent entries.
- Candidate provider direction: BoP dryland, wasteland, wasteland_steppe, rocky_shrubland, scrubland, shrubland, lush_desert, prairie/lush_savanna as transition; Terralith hot_shrubland, shrubland, steppe, arid_highlands, savanna_badlands, desert/canyon/oasis/mesa families; CliffTree shrubland, oasis, coniferous_badlands, maybe desert_cliff only after profile/tag-script discrepancy is resolved. Promenade did not show a clean arid candidate.
- Implementation note: Start conservatively with atlas metrics and tag/ladder tuning before touching broader climate authority. Do not widen the belt or adjust ProvinceAuthority first; the problem is representation inside a legal belt, not band-law failure. If custom arid IDs remain starved, then consider a narrow WARM_DRY approved-custom survival path while keeping tropical and poleward demotion guards authoritative.
- Proof idea: Add a focused arid-belt metrics report that measures hard-arid share, dry-transition share, exact-ID dominance, provider share, north/south symmetry, and starved intended IDs across the main seed plus cross-seeds. Keep band correctness strict: tropics and temperate+ must remain arid-clean.
- Tags: worldgen, arid-belt, subtropical, biome-diversity, custom-biomes, atlas-metrics
- Source: Julia chat + read-only Codex/subagent assessment of `LatitudeAtlasExport_20260702-064708_20260702-065928.zip`
- Repo state when captured: feat/custom-biome-expansion-26.1.2 / f86944cb
- Status: untriaged; saved for a future pass

### 2026-07-02 - HUD Studio detachable band and biome text
- Idea: HUD Studio should let the latitude band text and biome text be toggled together or displayed with biome next to the band. These HUD elements should be detachable so they can be moved around freely on the HUD. Add much richer text color customization; the current limited set of about six colors is too constrained, and Julia wants full rainbow/color-picker style control.
- Context: Reference screenshot in the chat shows the current compass plus text readout style: latitude/longitude followed by band label such as Subpolar.
- Tags: hud, ui, latitude-band, biome-text, color-customization
- Source: Julia chat + HUD screenshot
- Repo state when captured: feat/custom-biome-expansion-26.1.2 / f86944cb
- Status: untriaged; saved for a future pass

### 2026-07-02 - Latitude 2.0 world create and loading screen identity
- Idea: Improve the world creation screen and loading screen so they subtly emphasize that this is Latitude 2.0. Keep it Minecraft-canon and restrained, not garish or over the top. Explore removing the existing panels, or making them clearly glass/translucent, and replacing the vanilla rotating background with a parallax scrolling atlas world map. In general, refine the world creation screen presentation.
- Context: This is a parked design idea for a later UI pass, related to the 2.0/Longitude identity and atlas-map visual direction.
- Tags: ui, world-create, loading-screen, latitude-2.0, atlas-map
- Source: Julia chat
- Repo state when captured: feat/custom-biome-expansion-26.1.2 / f86944cb
- Status: untriaged; saved for a future pass

### 2026-07-02 - Fog-masked world wrapping boundary spike
- Idea: Reopen world wrapping as a future Latitude 2.0 topology/boundary spike: do implement wrapping, but make the world-border edge an oceanic heavy-fog / whiteout / low-visibility zone so the teleport crossing is visually masked. The entire world border should be ocean. When the player approaches the boundary above water, ramp into heavy fog/whiteout with one warning level, for example "Heavy fog ahead." If the player is swimming or underwater, use opaque water coloring / underwater fog so nothing useful can be seen through the seam, with one swimming-specific alert such as "Heavy turbulence ahead, proceed with caution." The ramp-up should be gradual, not sudden.
- Scope pass 1: This is a parked future-pass idea, not current implementation authorization. It should be treated as a refinement/reopening of the boundary decision after the earlier "do not make wrap the 2.0 centerpiece" plan. Future workers must not quietly revive the old ocean-sink seam; they should start a named topology/boundary spike and prove each old failure mode is handled.
- Objective pass 2: The goal is not to make opposite-edge terrain visually match across a visible seam. The goal is to make crossing feel seamless because visibility collapses before the teleport: open ocean at every border, whiteout/fog above water, opaque turbulence underwater, and warning copy that feels like dangerous weather rather than an arbitrary wall.
- Phase pass 3: Proposed sequence is (A) write a concise UX/topology spec that decides whether wrap is E/W only, N/S polar handling, or full-border behavior; (B) add Atlas/boundary metrics proving every border segment is ocean or ice-ocean by latitude band; (C) prototype the client-side fog, water opacity, particles, and alert cooldown without teleport; (D) add server-side wrapping with player, boat, mount, velocity, facing, and safe-arrival rules; (E) add preloading/performance mitigation for the far side; (F) run live proof by boat, swimming, flying, and walking approaches from multiple climate bands.
- Problems pass 4: Old wrap failed because the teleport was still visible: cloud position shifted, chunks could hitch/reload, ocean features/layout did not look continuous, and the seam felt fake. This new design must also handle boats, mounts, projectiles/items if in scope, multiplayer edge cases, maps/compass/HUD longitude display, vanilla world-border warning/damage interactions, render-distance differences, accessibility of near-white fog, alert spam/cooldowns, underwater visibility, Sodium/render-mod compatibility, and mapping-sensitive fog hooks during future Minecraft ports.
- Plan-integration pass 5: Keep the canonical 26.2 pivot, portability spine, and Phase 1 measurement work ahead of this. If promoted, this spike belongs around the boundary-experience phase, after GeoAuthority can prove the border is ocean and before any claim of "seamless wrap." Acceptance should require atlas edge-ocean metrics, headless/server wrap checks, client visual proof, underwater proof, vehicle proof, and all-thread performance profiling around the crossing.
- Open clarification for implementation: Julia said "heavy fog--whiteout condition north-south" and "the entire world border should be ocean." Future implementers should confirm whether N/S means polar boundary treatment only while wrapping remains E/W, or whether Julia wants full-border wrap semantics. Do not assume N/S teleport wrap until this is clarified.
- Tags: latitude-2.0, worldgen, world-wrapping, boundary-ux, fog, ocean-edge, topology-spike
- Source: Julia chat
- Repo state when captured: feat/custom-biome-expansion-26.1.2 / e2c8ee69, with pre-existing unrelated dirty files in `run-headless/server.properties` and `src/main/java/com/example/globe/world/LatitudeBiomes.java`
- Status: untriaged; saved for a future pass

### 2026-07-02 - No rivers in deserts
- Idea: Desert biomes should not have ordinary wet rivers running through them. If a future hydrology pass needs drainage across desert terrain, prefer dry washes, wadis, canyons, intermittent beds, or rare oasis/coast transitions rather than full river biome corridors.
- Context: Parked Latitude 2.0 worldgen/hydrology rule for future GeoAuthority, ClimateAuthority, river, and biome-family work.
- Tags: worldgen, hydrology, deserts, rivers, climate
- Source: Julia chat
- Repo state when captured: feat/custom-biome-expansion-26.1.2 / d7771838, with pre-existing unrelated dirty files in `run-headless/server.properties` and `src/main/java/com/example/globe/world/LatitudeBiomes.java`
- Status: untriaged; saved for a future pass
