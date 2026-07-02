# Code Red Biome/Decoration Port-Cleanup Lessons - 2026-06-20

`status: lesson captured from side-review`  
`scope: Latitude docs/history root diff review from e039b427 to fe660b50`  
`mutation: documentation only`

## Trigger

Julia reported a code-red Latitude 1.4 / 26.1.2 live symptom cluster: very slow chunk loading, missing decoration population, invisible/pickable palm leaves, odd canyon/desert terrain, and biome output that looked wrong for the expected latitude/cohesion rules.

A side conversation performed a read-only manual review of the `/Users/joolmac/CascadeProjects/Latitude (Globe)` diff from `e039b427` to `fe660b50` after CodeRabbit could not review the whole diff because it exceeded the free file limit.

## What The Side Review Found

The likely failure shape is not one single bad line. It is a compound regression where several safety systems can be removed or weakened at the same time:

- Latitude biome tag pools can be accidentally narrowed to mostly vanilla IDs. In the reviewed diff, several optional BOP/Terralith/Promenade/BWG biome IDs disappeared from tags, and one arid accent pool became empty. That can collapse diversity and increase fallback behavior even when generation technically succeeds.
- Climate safety rails are coupled. Equatorial wet-bias, desert/badlands demotion, warm-province classification, arid fallback behavior, and final sanitize passes work together. Removing one rail can make another rail suddenly too aggressive.
- Decoration parity is separate from biome selection. A selected custom biome does not guarantee its placed features survive feature indexing and decoration. The reviewed diff removed the old custom-biome `featuresPerStep` / `retainAll` retention mixin without an obvious replacement.
- Live visual proof and atlas proof need to meet at exact biome IDs plus decoration evidence. Atlas can prove biome choice; live proof can prove render/decor behavior. This symptom cluster crosses both.
- Port cleanup is not behavior-neutral. Deleted hooks, emptied tags, and removed demotion passes are as risky as new code, especially across Minecraft mapping/API changes.

## Required Future Behavior

Before declaring a Latitude port, backport, or release-readiness pass green, perform a "removed safety systems" audit in addition to compile and live proof:

- Diff biome tag files for pool shrinkage, optional biome-pack ID loss, and empty latitude pools.
- Diff worldgen mixins for deleted hooks around biome source wrapping, chunk biome population, feature indexing, decoration retention, vegetation guards, and surface/snow/climate guards.
- Diff climate/province code for removed wet-bias, dry-biome demotion, arid fallback, badlands/desert caps, and final sanitize passes.
- Prove exact biome IDs and decoration together for representative biome packs. Do not treat natural-color atlas screenshots or "biome selected" logs as proof that decorations survived.
- Treat "cleanup" commits that delete compatibility systems as red until the removed behavior is either replaced, explicitly retired by design, or proven unnecessary by targeted evidence.

## Evidence Pointers

- Side-review scope: `/Users/joolmac/CascadeProjects/Latitude (Globe)`, `main`, `fe660b50`, diff base `e039b427`.
- Strong reviewed areas:
  - `src/main/resources/data/globe/tags/worldgen/biome/*.json`
  - `src/main/resources/globe.mixins.json`
  - `src/main/java/com/example/globe/world/ProvinceAuthority.java`
  - `src/main/java/com/example/globe/world/LatitudeBiomes.java`
  - Deleted historical hook: `src/main/java/com/example/globe/mixin/ChunkGeneratorGenerateFeaturesBiomeSetMixin.java`
- Limitation: this binder note records lessons from the docs/history root side-review. Before source repair, re-check the canonical 26.1.2 root at `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2` and the active Modrinth profile jar.

## Durable Lesson

Do not treat port cleanup as behavior-neutral. For Latitude worldgen, "removed code" must be reviewed as aggressively as new code because tag pools, biome climate rails, feature-retention hooks, and decoration guards form one user-visible contract: diverse, coherent biomes with their expected visible features.
