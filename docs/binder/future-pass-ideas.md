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
