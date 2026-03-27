# Subagent Launch Pack

These briefs are derived from [axiom_implementation_plan.yaml](/home/philip/Documents/Axiom/docs/plans/axiom_implementation_plan.yaml) and are meant to be handed directly to separate agents for `T00` through `T03`.

## Recommended Launch Order

1. `t00_architecture.md`
2. `t01_core_framework.md`
3. `t02_data_framework.md`
4. `t03_transport_core.md`

## Concurrency Notes

- `T00` should land first if possible, but it has been intentionally narrowed to low-conflict architecture work so `T01` to `T03` can start in parallel.
- `T01` owns the generic block entity, capability, and packet framework.
- `T02` owns recipes, research definitions, and JSON-backed content definitions.
- `T03` owns transport internals, belt-focused blocks and block entities, and any belt debugging hooks.

## Shared Constraints

- Target Minecraft `1.16.5` on Forge with Java 8 compatibility.
- Do not revert other work in the repository.
- Avoid client-only references in common code.
- Prefer additive scaffolding over broad refactors unless the task explicitly requires a refactor.
- If you need a seam from another task, create a small extension point and document it instead of taking over that task's package.

## Handoff Expectation

Each agent should report:

- What changed
- What assumptions were made
- Any unresolved integration points
- Exact files changed
