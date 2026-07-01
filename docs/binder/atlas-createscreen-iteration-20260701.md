# Atlas + create-screen iteration (TEST 7→10, 2026-07-01)

`status: active — atlas landed, 2 items open` · `scope: rendering, create-world UI` · `branch: feat/custom-biome-expansion-26.1.2`

Live-feedback loop with Peetsa on the bespoke create-world atlas + world-shape controls. Files:
`client/create/LatitudePlanisphereRenderer.java`, `client/create/LatitudeCreateWorldScreen.java`.

## Landed
- **Continents**: ovals → **fractal value-noise landmasses** (3-octave fbm, edge-suppressed so seas surround
  them, run-length filled). Peetsa: "the continents are beautiful, looks great." (`b5462e88`, tuned since.)
- **Map frame = the REAL vanilla texture** (`minecraft:textures/map/map_background.png`). See mapping notes
  below — this was the hard part. (`b5462e88` → path fix `0a92373d`.)
- **Frame centering**: the frame now FILLS the atlas box and the climate map draws INSET inside it
  (`previewFrameBorder(radius)` ≈ 16% of radius); labels map to the inner radius so they stay aligned. Was
  drawn outward before → overflowed the preview box → panel scissor clipped it asymmetrically → looked
  off-center. (`0a92373d`)
- **Band contrast**: distinct climate palette (deep green / arid ochre / temperate green / cool blue / pale
  ice) + tuned alpha (wash `0x86`, selected band `0xE6`). Peetsa flagged low contrast twice; still verifying it
  now "sticks out" enough.
- **Labels de-crammed**: column slides up by overflow to stay within the atlas height.
- **Grey-out**: World **Shape** + World **Size** steppers (buttons inactive + `DISABLED_COLOR` labels) for BOTH
  Vanilla + Vanilla Superflat (only apply to Latitude worlds). (`ad8a19a1`)

## Mapping notes — how to blit a vanilla texture in THIS project (durable reference)
This project's mappings are a hybrid: `net.minecraft.client.gui.GuiGraphicsExtractor` for the GUI context,
`net.minecraft.resources.Identifier` for resource locations. Blitting a texture:
- The method is **`blit`**, NOT `drawTexture` (yarn's name). 10-arg overload:
  `ctx.blit(RenderPipeline, Identifier, int x, int y, float u, float v, int w, int h, int texW, int texH)`.
- The pipeline constant is **`net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED`** — NOT yarn's
  `net.minecraft.client.gl.RenderPipelines`.
- The Identifier path **must include `.png`** (`textures/map/map_background.png`) or you get magenta
  missing-texture.
- Found by `javap`-ing `~/.gradle/caches/fabric-loom/26.1.2/minecraft-merged.jar`.

## Open
- **World Shape → left "World" pane, under World Size** (Peetsa TEST 9). This is the A4 layout reorg scoped to
  one stepper: move `worldShape*Btn` from the settings rail to the left pane (init positioning, updateLeftLayout
  add a row + shift the preview, updateSettingsLayout drop the row 9→8, render the label/value in left geometry
  — `drawSettingsStepperValue` is rail-specific so needs a left-pane draw, applyTabbedVisibility World tab,
  left-widget visibility). ~6 interlocking methods, live-only-verifiable; **not yet done** — flagged as the
  invasive change best done as a focused pass (ideally with preview/verify tooling), especially stacked on the
  just-changed preview layout.
- **Lag** (still): base-biome sample now cached per column (`a54297cb`) on top of the per-column pick cache.
  Peetsa: "still a long delay to tpedge east, maybe a little better." Spark report shared
  (https://spark.lucko.me/cXV9L86RDv) but it's protobuf-backed — not readable via WebFetch and no browser
  connected this session, so the top frames are needed in TEXT form (spark Flat view) to target precisely.
  Leading hypothesis remains Terralith TERRAIN density noise (outside Latitude's biome layer) + the per-cell
  `current` source sample; a Mercator "Regular" world is also 4× the area so a far tpedge inherently generates a
  lot of virgin chunks.
