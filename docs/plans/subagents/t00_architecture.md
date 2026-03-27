# T00 Subagent Brief

## Mission

Implement `T00` from [axiom_implementation_plan.yaml](/home/philip/Documents/Axiom/docs/plans/axiom_implementation_plan.yaml).

Focus on architecture contracts and low-conflict scaffolding only. Do not do sweeping package moves that will create merge pain for the other active tasks.

## Why This Task Exists

The repository is currently a thin skeleton:

- one basic machine
- one basic belt
- abstract machine/belt tile entity bases
- core registries

The rest of the implementation plan needs stable package boundaries, naming rules, and extension points before larger systems arrive.

## Ownership

Primary write scope:

- `docs/plans/**`
- `docs/**` architecture notes
- new low-risk shared utility classes under `src/main/java/com/axiom/common/**`
- small bootstrap edits in existing files only if they reduce ambiguity for later tasks

Avoid owning:

- transport internals (`T03`)
- generic sync/capability framework (`T01`)
- recipe/research loader internals (`T02`)

## Source Material

- `Axiom_Design_Document.docx`
- `Axiom_Research_Gating_Notes.docx`
- `docs/plans/axiom_implementation_plan.yaml`

## Step-By-Step Start

1. Read the current package layout under `src/main/java/com/axiom/common`.
2. Define the target package map for:
   - block
   - block entity or tile
   - item
   - menu
   - network
   - power
   - research
   - recipe
   - transport
   - logistics
   - rail
   - survey
   - world
   - constructor
   - client
3. Write a short architecture note in `docs/` that explains:
   - package responsibilities
   - naming conventions
   - common/client boundaries
   - saved-data home
   - packet/channel home
   - where future multiblock logic should live
4. Add small shared helpers only if they are broadly useful now:
   - NBT key constants
   - registry naming helpers
   - direction or block-pos utility helpers
5. Add TODO-level extension points where downstream tasks need a stable home, but stop short of implementing their feature logic.

## Deliverables

- architecture note
- shared package/naming conventions
- any low-risk utility scaffolding that helps later tasks

## Definition Of Done

- another agent can tell where new systems belong without inventing new top-level structure
- packet, saved-data, and client-only code locations are explicitly documented
- no broad refactors create unnecessary merge conflicts

## Report Back With

- short summary of changes
- assumptions
- files changed
