# T01 Subagent Brief

## Mission

Implement `T01` from [axiom_implementation_plan.yaml](/home/philip/Documents/Axiom/docs/plans/axiom_implementation_plan.yaml).

Build the reusable block entity, capability, and sync framework that later machines and logistics systems can build on.

## Ownership

Primary write scope:

- `src/main/java/com/axiom/common/block/base/**`
- `src/main/java/com/axiom/common/tile/base/**`
- `src/main/java/com/axiom/common/menu/base/**`
- `src/main/java/com/axiom/common/network/**`
- minimal supporting edits in `src/main/java/com/axiom/common/registry/**`

Avoid owning:

- transport internals outside compatibility touchpoints (`T03`)
- recipe/research systems (`T02`)
- architecture docs unless you need to reference them

## Source Material

- `Axiom_Design_Document.docx`
- `docs/plans/axiom_implementation_plan.yaml`

## Goals

- reusable server/client sync scaffolding
- safer capability lifecycle handling
- side-aware machine IO foundation
- stable menu data sync hooks
- helper interfaces for metrics and status reporting

## Step-By-Step Start

1. Review the current abstract base classes:
   - `AbstractAxiomTileEntity`
   - `AbstractMachineTileEntity`
   - `AbstractBeltTileEntity`
   - menu base classes
2. Add a packet registration scaffold under `com.axiom.common.network`.
3. Introduce reusable sync/update hooks for block entities.
4. Improve capability exposure so machines can evolve toward side-aware IO instead of exposing everything identically on every face.
5. Add helper contracts for:
   - status reporting
   - metric emission
   - tick throttling or work pacing
6. Backfill the existing basic machine and belt only as much as needed to prove the framework works.
7. Keep broad transport logic out of scope unless required for compatibility.

## Deliverables

- packet channel scaffold
- improved base block entity abstractions
- menu/container sync contract
- compatibility pass for existing machine and belt

## Definition Of Done

- existing basic content still compiles and behaves sensibly
- future machines can expose differentiated IO rules without rewriting the base classes
- server-safe invalidation and sync paths are clearer than before

## Report Back With

- short summary of changes
- unresolved seams other tasks should know about
- files changed
