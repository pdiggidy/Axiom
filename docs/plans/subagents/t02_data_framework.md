# T02 Subagent Brief

## Mission

Implement `T02` from [axiom_implementation_plan.yaml](/home/philip/Documents/Axiom/docs/plans/axiom_implementation_plan.yaml).

Create the data-driven content layer for recipes, research definitions, and tier metadata.

## Ownership

Primary write scope:

- `src/main/java/com/axiom/common/recipe/**`
- `src/main/java/com/axiom/common/research/**`
- `src/main/resources/data/axiom/**`
- minimal serializer/type registration edits where necessary

Avoid owning:

- transport internals (`T03`)
- generic block entity framework (`T01`)
- large architecture refactors (`T00`)

## Source Material

- `Axiom_Design_Document.docx`
- `Axiom_Progression_Philosophy.docx`
- `Axiom_Research_Gating_Notes.docx`
- `docs/plans/axiom_implementation_plan.yaml`

## Goals

- JSON-backed machine recipe definitions
- research definition loader and schema
- unlock metadata that gates capabilities instead of invisibly modifying machine stats
- tier and upgrade records for future systems

## Step-By-Step Start

1. Inspect current registry patterns and keep the new content loaders consistent with them.
2. Create a minimal machine recipe type/serializer scaffold suitable for the current machine foundation.
3. Define research data records with:
   - id
   - display metadata
   - prerequisites
   - unlock outputs
   - optional catalysts
4. Encode the rule from the research notes directly in the model:
   - research unlocks items, blocks, capabilities, or categories
   - research does not store invisible machine stat buffs
5. Add tier or upgrade definition records for future belts, cables, drill modules, shafts, carts, scanners, and constructor limits.
6. Seed a small starter dataset in `data/axiom/` so downstream tasks have working examples.

## Deliverables

- recipe type and serializer scaffold
- research definition loader
- starter JSON assets
- clear data shapes for future expansion

## Definition Of Done

- new recipe and research content can be added mostly by data
- invalid or incomplete definitions fail loudly
- the data model reinforces multiplayer-safe physical upgrades instead of hidden stat modifiers

## Report Back With

- short summary of changes
- assumptions
- files changed
