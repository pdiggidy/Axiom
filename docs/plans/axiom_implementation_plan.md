# Axiom Implementation Plan

This plan translates the five `.docx` design notes into a delegatable roadmap. The canonical structured version lives in [docs/plans/axiom_implementation_plan.yaml](/home/philip/Documents/Axiom/docs/plans/axiom_implementation_plan.yaml).

## Current Baseline

The repository currently contains a small Forge 1.16.5 foundation:

- Deferred registers for blocks, items, tile entities, and containers
- Abstract machine and belt tile entities
- One basic machine and one basic belt
- Basic transported-item state for belts

That means nearly every system in the design docs is still ahead of the codebase, so the plan starts by stabilizing shared infrastructure before branching into mining, power, rail, and constructor work.

## Delegation Order

Use the tasks in this order:

1. `T00` architecture contracts
2. `T01` core block entity and sync framework
3. `T02` data-driven recipes and research definitions
4. `T03` horizontal transport core
5. `T04` power system
6. `T05` research progression
7. `T06` metrics-first UI
8. `T07` surveying and mining
9. `T08` vertical logistics and anchors
10. `T09` rail logistics
11. `T10` constructor and schematics
12. `T11` overlays and top-down diagnostics
13. `T12` integration, balance, and dedicated-server verification

## Parallelizable Batches

- Foundation batch: `T00`, `T01`, `T02`, `T03`
- Midgame systems batch: `T04`, `T05`
- World/factory expansion batch: `T07`, `T08`, `T09`, `T10`
- Integration/polish batch: `T11`, `T12`

## Notes For Agent Delegation

- Each task in the YAML file includes `write_scope`, `depends_on`, and `acceptance_criteria`.
- Prefer assigning one task per agent unless two tasks have fully disjoint write scopes and a tight shared deadline.
- Treat `T01`, `T02`, and `T03` as API-defining tasks. Their public interfaces should be reviewed before downstream work starts.
- Treat `T09` and `T10` as the highest-risk implementation tracks. They should get the strongest test coverage and the clearest API seams.
