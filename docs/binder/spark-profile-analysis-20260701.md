# Spark profile analysis — TEST 9/10 lag capture (2026-07-01)

`status: analyzed — real freeze confirmed; likely cause = system memory/swap pressure, not (only) code; re-profile needed` · `scope: worldgen, perf`

Peetsa shared `cXV9L86RDv.sparkprofile` (a spark profiler capture, 300s, taken while testing chunk-gen lag /
`tpedge` to the east border). No browser was available this session and the spark viewer is JS/protobuf-backed
(WebFetch only sees the empty page shell), so the `.sparkprofile` file was decoded directly: a schema-less
protobuf structural parser (`try_parse_message`, prefer-text-over-submessage for length-delimited fields to
resolve the class/method-name ambiguity) reconstructed the sampler tree without the `.proto` source. Scripts
are throwaway (session scratchpad), but the technique and findings are worth keeping.

## Finding 1 — a real, severe, ~3-minute freeze is objectively in the data
The file's tick-timing windows (root field 7, 60-second buckets: tick count + mspt percentile doubles +
start/end epoch ms) show:

| window | ticks completed | TPS |
|---|---|---|
| 0s–60s | 1200 | 20.0 (healthy) |
| 60s–120s | 45 | **0.75** |
| 120s–180s | **0** (proto3 omits the zero field) | **~0** — a full minute with no ticks recorded |
| 180s–240s | 440 | 7.33 (recovering) |
| 240s–300s | 1200 | 20.0 (healthy again) |

So there's an unambiguous, dramatic near-total server freeze from roughly **t=+60s to t=+240s** (180 seconds),
bookended by two fully healthy minutes. This is not "a little lag" — it's the server nearly hanging for three
minutes. This almost certainly corresponds to the `tpedge` east-border teleport and the chunk-gen storm it
triggered.

## Finding 2 — the capture can't show WHY, because it only recorded "Server thread"
The sampler tree has exactly **one** `ThreadNode`, named `"Server thread"` (7249 distinct call-site entries,
6942 total sample ticks). **Neither `com.example.globe` (Latitude) nor Terralith/density-function classes
appear anywhere in the file at all.** The top self-time entries during the capture are ordinary main-thread
tick cost — entity AI/movement/collision (`LivingEntity.aiStep`, `Entity.move`, `BlockCollisions`), block-state
lookups, and chunk-tracking bookkeeping (`ChunkMap`, `DistanceManager`, `ChunkTracker`) — plus direct evidence
the main thread was **blocking**, not computing: `BlockableEventLoop.managedBlock`,
`ServerChunkCache$MainThreadExecutor.pollTask/doRunTask`.

That's consistent with the freeze: modern chunk/biome/density generation runs on a **separate worker thread
pool**, not the main "Server thread". During a teleport, the main thread synchronously blocks waiting for that
pool to catch up (`managedBlock`) — so the *actual* expensive computation (Latitude's `pick()`, Terralith's
terrain density noise) happened on a thread this profile never sampled. The main thread's own samples during
the freeze are mostly it sitting there waiting, which is why nothing worldgen-related shows up.

## Conclusion
- The lag is real and severe (objectively ~3 minutes of near-zero TPS), not just "maybe a little better."
- This profile cannot confirm or rule out Latitude/Terralith as the cause, because it didn't sample the thread
  that does the actual generation work.
- The C3 fixes landed this session (per-column `pick()` memoize, cached column-invariant `base` sample) are
  still believed-correct optimizations, but **unconfirmed** by this data one way or the other.

## Next step (needs Peetsa)
Re-run spark capturing **all threads**, specifically around a `tpedge`:
```
/spark profiler --thread * start
```
...then teleport to the border, wait for the freeze to resolve, then:
```
/spark profiler stop
```
`--thread *` is the key flag — without it spark defaults to the main thread only, which is what happened here.
That capture will show the actual worker-thread hot path and let us target the real bottleneck instead of
guessing.

## Finding 3 (2026-07-01, from spark's own web viewer) — the machine is nearly out of physical RAM
Peetsa pulled up the actual spark web viewer (not decodable via WebFetch/no-browser earlier, but readable once
they screenshotted it) for both this profile and a second capture ("Profile @ 09:54"). System-level stats:

- **Physical memory: 23.9 GB / 24 GB — 99.71% used.**
- **Swap: 4.2 GB / 5 GB — 83.9% used.**
- Heap (the JVM's own pool, separate from the above): 5.3 GB used / 11 GB committed / **16 GB max** —
  `-Xmx16384M` confirmed correctly applied in the JVM Flags tab. (Peetsa's question — "only 4.4GB committed,
  not the full 16" — was reading the G1 **Old Gen** sub-pool specifically, not total heap; G1GC commits regions
  lazily on demand, this is normal, not a misconfiguration.)

**This is a stronger lead than anything in Finding 1/2.** A near-full physical memory + heavy swap use means any
GC pause that touches a swapped-out page can balloon from milliseconds to seconds/minutes — this plausibly
explains part or all of the ~3-minute freeze in Finding 1 far better than a Latitude/Terralith code hotspot
would. With `Xmx16384M` on a 24 GB machine, only ~8 GB is left for the OS + JVM off-heap (Metaspace, native
render buffers, thread stacks) + everything else running. Recommended before the next capture: close other
memory-heavy apps, and/or try LOWERING Xmx (e.g. 10-12 GB) — a smaller heap that stays fully resident with quick
GCs can beat a large one that swap-thrashes during a pause.

**Also:** the second capture ("Profile @ 09:54") shows "No Data — this profile doesn't contain any data!" in
its All View — it captured summary stats (TPS/memory/GC) but no actual call-tree samples, likely the
async-profiler engine failing to attach (not uncommon on Apple Silicon). Still waiting on a real `--thread *`
capture with tree data to see the worker-pool hot path.

## Aside — client performance mods (Sodium / FerriteCore / ImmediatelyFast)
Peetsa noted the game "runs better" with these. Worth being precise about why: they're all **client-side**
(rendering batching / memory footprint / render-thread CPU), not server/worldgen code — they don't change the
chunk-generation algorithm's cost at all. But in singleplayer the client and integrated server share one JVM
process and CPU, so less client-side CPU/GC pressure (FerriteCore's lower memory footprint means fewer/shorter
GC pauses; Sodium/ImmediatelyFast reduce render-thread cycles) can leave more headroom for the server and
worker threads — a real but *indirect* effect, not a fix for the underlying generation cost.
