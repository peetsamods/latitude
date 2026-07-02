# Model / reasoning-effort strategy for the Latitude 2.0 overhaul (2026-07-02)

`status: advisory — informs execution, does not authorize release/behavior change` · `scope: process, cost`

Peetsa asked for a critical, honest read on which model + reasoning effort to use for which part of the
Latitude 2.0 overhaul (`docs/LATITUDE_2_0_OVERHAUL.md`), after a single large unrelated task ("Slabbed") burned
$45 and exhausted a multi-day Fable-tier usage allocation in one day. That data point is the anchor for this
note: **whatever Fable's ceiling capability is, its cost/quota cadence does not fit a project built around many
small, checkable, iterative phases** — which is exactly how the overhaul plan is already structured (Phase -2
through 6, measurement-first, explicit Hard Stops). A future thread should read this before starting a phase and
either follow the recommendation or say plainly why it's deviating.

## Can a future thread self-adjust model/effort automatically? (asked directly — answered precisely)

**Partially, and the two halves matter:**

1. **Cannot.** A thread cannot change the *ambient* model it is itself running as. That's set by `/model`, a
   user-run local slash command — there is no tool that lets a session reassign its own underlying model
   mid-conversation.
2. **Can.** A thread *can* delegate a specific, bounded piece of work to a chosen model **and** reasoning effort
   via the `Agent` tool (`model` param: `sonnet` / `opus` / `haiku` / `fable`) or the `Workflow` tool
   (`opts.model` / `opts.effort` per `agent()` call inside a script). This is a real, already-used mechanism —
   this session spawned multiple Explore/general-purpose subagents for parallel investigation. For the genuinely
   hard design phases (2 and 3 below), a thread can spin up a single Opus-model agent call scoped to *just* that
   design decision, receive its output, and keep the ambient conversation on whatever cheaper model Peetsa has
   set — without Peetsa touching `/model` at all.

**Required protocol for a future thread:** at the start of each phase/slice, state plainly which path is being
used — *"recommend switching `/model` to Opus for this phase"* (when the work benefits from live, conversational,
high-stakes back-and-forth) vs. *"delegating this specific design question to an Opus sub-agent, staying on the
current ambient model"* (when the work is a boundable, one-shot decision that doesn't need a live back-and-forth).
Don't silently do neither and just proceed on whatever model happens to be set.

## Phase-by-phase recommendation

Mapped to `docs/LATITUDE_2_0_OVERHAUL.md`'s roadmap. "Effort" means the reasoning-effort tier for whichever model
is driving (low/medium/high/xhigh/max), not a separate axis from the model choice.

| Phase | Nature of the work | Model | Effort | Why |
|---|---|---|---|---|
| -2: Version Truth And 26.2 Availability | doc/toolchain lookup and confirmation | Sonnet | low | pure verification, no judgment call |
| -1: Canonical 26.2 Pivot | build metadata, narrow API/mixin drift repair, proof reruns | Sonnet | medium | bounded and compiler/proof-gated; bump to high only if drift repair turns out broad (a Hard Stop condition in its own right) |
| 0: Portability Foundation | pure no-op types, adapter interfaces, flag-off proof | Sonnet | medium | mechanical scaffolding, but correctness-sensitive — must prove flag-off output is byte-identical |
| 1: Measurement Harness | analyzer fixtures, old-red + fresh-baseline runs | Sonnet | medium | well-specified, test-driven; this is grunt work with craft, the same shape as most of this session's work |
| **2: GeoAuthority Prototype** | inventing the macro land/ocean/continent/ocean-basin algorithm from scratch | **Opus** | **high/xhigh** | genuinely novel design with the highest blast radius — every later phase depends on getting this right; this is where premium reasoning earns its cost |
| **3: ClimateAuthority Prototype** | inventing the wind/ocean/rain-shadow/monsoon climate model | **Opus** | **high/xhigh** | same reasoning as Phase 2 — a from-scratch model with long-lived downstream consequences |
| 4: Terrain Integration Spike | one narrow density-function hook, with a documented prior failure mode (the ocean-sink seam) | Opus | high, but keep the scope narrow per the plan's own "Potential problems" | history of a real regression here means the judgment matters, but the plan itself insists on narrow scope — don't let model choice justify scope creep |
| 5: Boundary Experience | visual/UX tuning of the projection edge, after Atlas is green | Sonnet | medium | mostly tuning and wording once geography/climate are already proven |
| 6: Release Candidate Hardening | matrix seeds/sizes, mod-present proofs, all-thread profiles, live flyovers, docs closure | Sonnet | medium, **ultracode for the final adversarial pass** | mirrors this session's two bug-catcher rounds — a real, proven-useful pattern for exactly this kind of pre-release sweep |

**Any Hard Stop trigger** (see the plan's own Hard Stops list) is itself a signal to consider a model/effort bump
for *that specific diagnosis* — regardless of which phase it happens in.

## On `ultracode` / multi-agent workflows specifically

Reserve for adversarial verification (the bug-catcher pattern used twice this session) and large mechanical
migrations once a design is already locked — not as a default execution mode for ordinary phase work. Cost
scales with agent count regardless of which underlying model runs each agent, so it's a coverage multiplier, not
a substitute for choosing the right model.

## On Fable specifically

Not in the default rotation for this project. The concrete data point behind this call: one large *unrelated*
task ("Slabbed") consumed $45 and exhausted a multi-day usage allocation on its first day of use — that's
evidence about Fable's cost/cadence generally, not something specific to Latitude. A roadmap built around many
small, frequently-checked phases needs a model that can be called constantly and cheaply; ration Fable to a rare
exception if a specific wall shows up that Opus at max effort provably can't clear, not as a starting option.

## Linked from

- `docs/LATITUDE_2_0_OVERHAUL.md` — the front door; add a pointer here and generalize the Fable-specific
  handoff section so it doesn't silently assume Fable is the executor.
