# Axiom Architecture Note

This note defines the low-conflict package and data boundaries that the rest of the implementation should follow.

## Package Layout

The current codebase already has a workable split, so the goal is to extend it consistently rather than reshuffle it early.

- `com.axiom.common.block` for concrete block implementations.
- `com.axiom.common.block.base` for shared block behavior and block-state conventions.
- `com.axiom.common.blockentity` for future block entity implementations.
- `com.axiom.common.blockentity.base` for shared block entity lifecycle, capability, and sync behavior.
- `com.axiom.common.menu` for concrete container/menu implementations.
- `com.axiom.common.menu.base` for shared container layout and player-inventory helpers.
- `com.axiom.common.transport` for belt-item movement primitives and future logistics graph objects.
- `com.axiom.common.network` for packets, sync messages, and server-client plumbing.
- `com.axiom.common.power` for the electrical grid, power sources, consumers, and monitors.
- `com.axiom.common.research` for research definitions, unlock storage, and gating logic.
- `com.axiom.common.rail` for minecart logistics, stations, signals, and route state.
- `com.axiom.common.constructor` for schematics and constructor gantry logic.
- `com.axiom.common.survey` for deposits, surveying, and mining metadata.
- `com.axiom.common.world` for saved data, world metadata, and server-side persistence helpers.
- `com.axiom.common.util` for low-risk shared helpers such as identifiers, NBT keys, directions, and block-pos codecs.
- `com.axiom.client.*` for screens, overlays, renderers, input bindings, and other client-only code.

The existing `tile` package remains a compatibility layer for now. New code should target `blockentity` and `blockentity.base`, and any later migration can happen as a dedicated refactor once the foundation is stable.

## Naming Conventions

- Blocks use `NounBlock` naming, for example `BasicMachineBlock`.
- Block entities use `NounBlockEntity` naming in new code. Existing `TileEntity` names stay untouched until a dedicated migration task.
- Menus use `NounContainer` naming.
- Screens use `NounScreen` naming.
- Packets use `NounPacket` naming.
- Data files use `snake_case` names and mirror their logical domain, such as `research/`, `recipes/`, or `worldgen/`.
- Registry holders stay in `ModBlocks`, `ModItems`, `ModTileEntities`, and `ModContainers` until a dedicated registry cleanup task exists.

## Saved Data Home

- World-scoped persistence belongs under `com.axiom.common.world`.
- Use saved data for deposit catalogs, research progress if it is world-scoped, rail reservations, and anchor coverage.
- Keep world metadata in common code so server logic owns the source of truth.
- If a system is player-scoped instead of world-scoped, keep that distinction explicit in the data model and naming.

## Packet Home

- Packet registration and handlers belong under `com.axiom.common.network`.
- Common code should expose sync-friendly state objects and events, but not render or open screens directly.
- Client-facing packet responses should stay in `com.axiom.client` or client-safe hooks invoked from there.

## Client/Common Boundary

- Common code must never reference `net.minecraft.client` classes.
- Client code may observe common state, but common code must not depend on client renderers, screens, or keybinds.
- If a feature needs both sides, common code should own the data model and synchronization while client code owns presentation.
- When in doubt, keep the state in common and keep the rendering in client.

## Extension Points

- Add small helper types when they reduce duplication across future systems, especially for identifiers, NBT keys, directions, block positions, and sync payloads.
- Keep shared helpers additive and narrowly scoped so later tasks can adopt them without changing established call sites.
- Avoid moving existing implementation-heavy classes during foundation work.
- Prefer stable, data-oriented contracts over early feature logic.
