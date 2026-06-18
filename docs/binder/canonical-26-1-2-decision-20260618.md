# Latitude canonical line decision (2026-06-18)

`scope: project structure` · `status: decision recorded` · `canonical line: 26.1.2`

## Decision

Julia recorded the current project truth:

- **Minecraft 26.1.2 is the current canonical Latitude line.**
- **Minecraft 1.21.11 is a backport target**, not the current primary development line.
- The project is **not ready to resume the 1.21.11 backport yet**; older-version work should wait until the 26.1.2 source of truth is stable enough to backport deliberately.
- Julia noted that Minecraft **26.2** is now released. Treat 26.2 as a target to consider later, but it does not change the immediate canon. For now, the active canon remains **26.1.2**.

## Why this matters

The 2026-06-14 binder entry found that 1.21.11 was missing canonical v1.4 `ProvinceAuthority`, which explained the confetti, temperate desert leakage, and related worldgen symptoms seen in the 1.21.11 beta. That investigation left an open question: whether 1.21.11 should be primary again, or whether it was a backport from 26.1.2.

This entry resolves that question: treat 26.1.2 as the source of truth.

## Practical boundary

- Do not continue layering ad-hoc worldgen fixes onto the 1.21.11 beta line as if it were primary.
- Do not start 26.2 planning from this note alone; it is a future target consideration.
- Next implementation work should be scoped from the 26.1.2 line with a fresh root/branch/status preflight and an explicit proof gate.
- Any future 1.21.11 work should be framed as a backport from accepted 26.1.2 behavior, likely including a scoped `ProvinceAuthority` backport plan.

## Current pointer

The live resume pointer is `docs/HANDOFF.md`. This binder entry is the dated evidence for the canonical-line decision.
