# 1.21.11 worldgen quality gap + the "which line is primary?" question (2026-06-14)

`scope: project structure + worldgen` · `status: PAUSED pending Julia's structural decision` · `severity: not-broken, but needs a deliberate call`

## Resolution update (2026-06-18)
Julia resolved the structural question: **26.1.2 is the current canonical line.** The 1.21.11 line should be treated as a future backport, not as the primary development target, and not resumed until the 26.1.2 source of truth is ready for that work. Julia noted that Minecraft 26.2 is now released; treat it as a future target to consider, not as a change to the immediate canonical decision. See `canonical-26-1-2-decision-20260618.md`.

## Why this entry exists
Julia tested the 1.21.11 beta (beta.27, world TEST X) and saw confetti, deserts-in-temperate, and "cherries everywhere." Investigation traced these to the 1.21.11 line **lacking `ProvinceAuthority`** (the v1.4 Cohesive Horizons coherence+climate engine). That, in turn, surfaced a structural question Julia raised — *"I thought this was developed for 1.21.11 in the first place?"* — which must be settled before more worldgen work. **Nothing is corrupted; all work is committed or safely staged.** Full detail: `Latitude-port-1.4.0-1.21.11/docs/porting/HANDOFF-1.21.11-state-and-province-gap-20260614.md`.

## The finding (data, verified)
- Headless biome render seeded to TEST X (seed 7696320487994181022): **temperate band = 10.7% desert + 6.2% savanna + 4.9% jungle** (warm/arid leak), and **44% of temperate biome patches are single ~24-block specks** (confetti). Julia's live Terralith probe: desert 19.4% in temperate.
- **Root cause:** PORT `port/1.4.0-beta-1.21.11` has NO `ProvinceAuthority`; canonical `feat/1.3.1-cohesive-horizons-26.1.2` HAS it. ProvinceAuthority (per v1.4 fix-recipe) cut confetti 24%→7% and arid 40%→17% — exactly these symptoms. The 1.4-beta backport (2026-06-07) **documented** this gap: "warm-band rebalance N/A — 1.3.0 lacks ProvinceAuthority." So it's a known partial backport, not a new break.

## The structural question (Julia decides)
Repo/governance say: **26.1.2 (`feat/1.3.1-cohesive-horizons-26.1.2`) is the canonical v1.4 worldgen line; 1.21.11/1.21.1/1.20.1 are backports** (Art II: worldgen on canonical first, then port). Historically 1.21.11 WAS primary for **1.3.0** (`v1.3.0+1.21.11`), so "developed for 1.21.11 first" is true for 1.3.0 — v1.4's worldgen moved to 26.1.2.
- **If that's the intended structure:** durable fix = port `ProvinceAuthority` canonical→1.21.11 (+ other lines). Large.
- **If 1.21.11 is meant to be primary** (and 26.1.2 is secondary/experimental): the structure itself is the thing to correct, and "reconcile" may point the other way. Only Julia knows the intent.

## Known port↔canonical divergences (each direction)
- Port MISSING (canonical has): `ProvinceAuthority`; cherry keep-gate (port gateless, band-aided this session vanilla-only).
- Port HAS (canonical lacks): equatorial dry-biome overhaul (`shouldDemoteEquatorial*`); tree-line/alpine + pale_garden backport.
→ Reconciliation is non-trivial in both directions; needs a plan, not an ad-hoc merge.

## Session state (for continuity)
- beta.26 COMMITTED (HEAD 7828e764): tree-line/cherry retune, pale_garden containment (atlas-proven, 2 seeds), Codex loading fix. ~11 commits ahead of origin, NOT pushed.
- beta.27 UNCOMMITTED in working tree (safe), staged in Modrinth instance: cherry-province gate (vanilla only), E/W warning-timing fix, /latdev restore (beta-only + op-gated). `/latdev` confirmed working in-game; E/W + cherry pending in-game confirm.
- The pink "cherry" biomes Julia sees = `terralith:sakura_grove` + `terralith:sakura_valley` (Terralith), NOT vanilla cherry_grove → outside the beta.27 gate.

## Recommended next step
PAUSE worldgen. Julia settles the structural question above. Then: scope the ProvinceAuthority port (effort estimate) if 1.21.11 is a backport; cheap stopgaps available regardless (sakura gate, temperate warm-clamp). Amplitude / no-high-altitude-terrain (treeline never triggers) is a separate large track. Related memory: `port-1211-missing-provinceauthority`, `cherry-grove-gate-regression`, `repo-canonical-layout`, `latitude-governance`.
