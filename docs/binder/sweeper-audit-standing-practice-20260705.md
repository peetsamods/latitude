# Sweeper adversarial audit — established as a standing practice

`date: 2026-07-05` · `branch: port/canonical-26.2-pivot` · `scope: process, not worldgen behavior`

Per Peetsa's instruction ("the sweeper should always be in place to check these sorts of things while
you continue your work"), the adversarial find-then-verify audit pattern used for the Biome Consumer bug
(sweeper audit #1, `docs/binder/biome-consumer-sweeper-fixes-20260705.md`) is now a documented, reusable
practice rather than a one-off script, and a second sweep (Phases 0-3 comprehensive) has been launched
against the rest of the overhaul so far.

## What the pattern is

1. **Find**: N independent agents, each given the same scope brief but a genuinely different adversarial
   *lens* (exhaustiveness-of-a-cascade, test-validity, a known recurring risk pattern, flag-gating
   correctness, docs-vs-code contradiction, freeform adversarial-scenario construction). Each returns
   structured findings: file, line/anchor, severity, concrete failure scenario, self-rated confidence.
2. **Verify**: one independent skeptical agent per raw finding, told to default to REFUTING and only
   return CONFIRMED/PLAUSIBLE if it can trace or reproduce the defect itself by reading the real current
   code — not just re-read the claim.
3. **Synthesize**: survivors (CONFIRMED or PLAUSIBLE) only, ranked by severity.

This exists because LESSONS L14 (main worktree) showed that logical/aggregate reasoning ("the flag is
off so it's inert," "the acceptance test passed") is not sufficient proof against a silent classification
fallthrough — the bug that triggered this whole practice passed every existing check and still shipped
into a live game.

**Model/effort:** both Find and Verify agents run on Opus at high effort, no exceptions — this is
exactly the kind of judgment-heavy, must-not-miss-or-hallucinate work `model-effort-strategy-20260702.md`
reserves top-tier model+effort for. A missed live-worldgen bug costs far more than the extra tokens.

## Where the reusable template lives

`.claude/workflows/latitude-sweeper-audit.js` (this repo, committed, indexed) — the canonical, in-sync
copy of the lens/schema/find-verify-synthesize shape, with a header comment documenting how to adapt it
for a new sweep.

**Known limitation, confirmed 2026-07-05:** `Workflow({ name: 'latitude-sweeper-audit', args })` does
NOT resolve this repo-local file in this environment — the tool only resolves built-ins ("deep-research",
"code-review"), and a separate args-array-field bug (`"undefined is not an object (evaluating
'lenses.length')"`) was also hit the same day. **Always invoke by copying the template's body into a
`Workflow({ script: ... })` call** with `repoRoot`/`scopeLabel`/`scopeBrief`/`lenses` hardcoded as
literals for the new scope, rather than fighting name/args resolution. Both sweeper audit #1
(`latitude-sweeper-audit-biome-consumer-wf_d0c82c96-3d7.js`) and sweeper audit #2
(`latitude-sweeper-audit-phases-0-3-wf_9c9dbfa8-fec.js`) used this pattern successfully.

## Sweeper audit #2: Phases 0-3 comprehensive sweep

Launched immediately after this note was drafted, scoped to everything sweeper audit #1 did NOT already
cover: GeoAuthority's own internal decision cascades (continent/ocean-basin/mountain-intent/orogen
assignment), the Phase 0 Platform Adapter shells' genuine zero-cost-when-off property, `tools/atlas/
geography_analyzer.py`'s math and test coverage, GeoAuthority's documented size-invariance and id
invariants under adversarial (seed, radius, aspect) inputs, and ClimateAuthority's probe-gathering side
(`sample()`'s fetch/orographic/current probing, `seasonality()`) plus cross-phase docs-vs-code
consistency for `GeoSummary`/`ClimateSummary`'s contracts. Explicitly excludes `classifyBase`/
`climateFamilyMismatch`/`applyClimateCompatReroll`, already fixed and tested in audit #1. Results and
triage will be recorded in a follow-up binder note once the sweep completes.
