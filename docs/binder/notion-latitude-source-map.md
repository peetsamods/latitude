# Notion Latitude Source Map

## Purpose
Track high-signal Notion Latitude records and map them to binder topics, including explicit unresolved binder gaps.

## Notion sources reviewed in this sync
- `https://www.notion.so/80d8439c65ac4438a37673609410b4c9` (`LATITUDE Hub`)
- `https://www.notion.so/bac96324aef54b1a83d03799ea91af2a` (`Latitude Running Log`)
- `https://www.notion.so/960eb89f5349402e88996894693100d7` (`Latitude Commit Index`)
- `https://www.notion.so/614013a05e7143fdbebf9b3292013a1f` (`Latitude Bug & Regression Summary`)
- `https://www.notion.so/93f1d390b9b54a97b1180fcb6d97e04b` (`Latitude Release Hard Gate`) (from search index)
- `https://www.notion.so/ae284050d222413e98d032fe4b32c256` (`Latitude Worldgen Architecture Reference`) (from search index)
- `https://www.notion.so/fb7aea67b4a24c81b78bae79550e2e7f` (`Latitude Constitution`) (from search index)

## Binder topic mapping

| Notion source | Binder topic | Canonical local source | Sync status | Gap |
| --- | --- | --- | --- | --- |
| LATITUDE Hub | release and handoff readiness | [/Users/joolmac/CascadeProjects/Latitude (Globe)/CHANGELOG.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/CHANGELOG.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/release/checklist.md) | partial | Hub links many operating docs not yet mirrored in local binder index. |
| Latitude Running Log | live proof and runtime evidence | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/binder/evidence-registry.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/binder/evidence-registry.md) | partial | Registry has only bootstrap rows; missing historical ingestion policy and normalized backfill process. |
| Latitude Commit Index | release and handoff readiness | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/fix-history.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/fix-history.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/CHANGELOG.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/CHANGELOG.md) | partial | Commit lineage is richer in Notion than local release history docs. |
| Latitude Bug & Regression Summary | bug blaster investigation and closure | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/fix-history.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/fix-history.md) | partial | Bug Blaster and blocked classifications are not fully reflected in local fix-history chronology. |
| Latitude Release Hard Gate | release and handoff readiness | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/release/checklist.md) | partial | Hard-gate criteria parity between Notion and local checklist needs explicit periodic diff review. |
| Latitude Worldgen Architecture Reference | worldgen policy and biome behavior | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md) | partial | Architecture-level invariants are broader in Notion and need selective codification in local docs. |
| Latitude Constitution | worldgen policy and biome behavior | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md) | partial | Constitution-level doctrine is not yet represented as an explicit local doctrine note. |

## Current gap ledger
1. Local binder did not include a machine-readable schema file; added `docs/binder/evidence-schema.json` in this sync.
2. No explicit Notion-to-binder coverage map existed; this file now serves as that source.
3. Historical evidence normalization from Notion Running Log/Commit Index into local evidence rows is not yet backfilled.
4. Release Hard Gate parity check workflow exists conceptually but is not yet codified as a scheduled routine in local docs.

## Next binder slice (narrow)
- Add one scoped historical backfill policy note defining:
  - how many past Notion entries to ingest per slice
  - which fields are mandatory when translating Notion records into `evidence-registry.md`
  - stop rule for avoiding broad retroactive churn
