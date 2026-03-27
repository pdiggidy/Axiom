# T03 Subagent Brief

## Mission

Implement `T03` from [axiom_implementation_plan.yaml](/home/philip/Documents/Axiom/docs/plans/axiom_implementation_plan.yaml).

Expand the current belt primitive into a stronger horizontal transport layer while keeping the first implementation practical.

## Ownership

Primary write scope:

- `src/main/java/com/axiom/common/transport/**`
- belt-focused block classes
- belt-focused tile entity classes
- belt-related client debug/render hooks if needed
- belt-related assets if added

Avoid owning:

- generic sync/capability framework beyond narrow compatibility updates (`T01`)
- recipes and research (`T02`)
- broad architecture refactors (`T00`)

## Source Material

- `Axiom_Design_Document.docx`
- `docs/plans/axiom_implementation_plan.yaml`

## Goals

- controller/delegate belt graph rules
- handoff between belts and machines
- stable serialization for moving items
- explicit horizontal-only guarantees
- throughput hooks for later metrics UI work

## Step-By-Step Start

1. Review the existing transport classes and belt block entity behavior.
2. Formalize how a controller belt owns moving item state and how delegates resolve that controller.
3. Add graph rebuilding or discovery rules so contiguous belt runs can recover after placement, removal, or world load.
4. Implement insertion and extraction touchpoints between belts and adjacent machine inventories.
5. Strengthen moving-item serialization and restore behavior across save/load.
6. Add lightweight throughput accounting so later UI tasks can read items-per-time metrics.
7. If rendering is too large for this pass, expose debug-friendly state instead of overreaching.

## Deliverables

- stronger belt graph behavior
- machine handoff plumbing
- stable serialization and metrics hooks
- optional debug state exposure

## Definition Of Done

- belts clearly remain horizontal-only
- transported items survive save/load and block updates predictably
- adjacent machines can exchange items with belts through a clean API seam
- later UI work has a place to read transport metrics from

## Report Back With

- short summary of changes
- unresolved integration points
- files changed
