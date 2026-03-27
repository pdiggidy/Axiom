# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew compileJava          # compile only — fastest way to check for errors
./gradlew build                # full build + jar
./gradlew jar                  # assemble mod jar without running tests
./gradlew runClient            # launch Minecraft client in dev environment
./gradlew runServer            # launch dedicated server in dev environment
./gradlew runData              # run data generators (recipes, loot tables, tags)
./gradlew genIntellijRuns      # generate IntelliJ IDEA run configurations
```

There are no automated tests in this project yet. `compileJava` is the primary feedback loop — run it after any change.

The working directory for run tasks is `run/` (created on first launch, not committed).

## Architecture reference

The full architecture document lives at `docs/architecture/axiom_mod_architecture.md`. Read it before making structural changes — it covers class hierarchies, package roles, design invariants, and step-by-step guides for adding new machines, recipes, and research nodes.

Implementation roadmap and task breakdown: `docs/plans/axiom_implementation_plan.yaml`

## Platform

- Minecraft **1.16.5**, Forge **36.2.42**, Java **8**
- Mappings: official Mojang + Parchment (`mapping_channel`/`mapping_version` in `gradle.properties`)
- Mod ID: `axiom` (matches `@Mod` annotation and all `ResourceLocation` prefixes)

## Project structure

**Entry point:** `com.axiom.Axiom` — `@Mod` class. All `DeferredRegister` objects are registered onto the **mod event bus** in the constructor. `commonSetup` handles post-registration wiring (network channel). Never register game objects in `commonSetup`.

**Registry holders:** `com.axiom.common.registry` — `ModBlocks`, `ModItems`, `ModTileEntities`, `ModContainers`. Serializers live in `com.axiom.common.recipe.ModRecipes`.

**`tile` vs `blockentity` packages:** The existing `com.axiom.common.tile` package is the live implementation. `com.axiom.common.blockentity` is reserved for a future naming migration — do not put new code there yet.

**Client boundary:** `com.axiom.common` must never import `net.minecraft.client.*`. Use `DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)` in packet handlers that need to reach the client world. Client screens, overlays, and renderers go in `com.axiom.client` (not yet created).

## Key wiring patterns

**New block + tile entity pair:** Block registered in `ModBlocks` → BlockItem in `ModItems` → TE type in `ModTileEntities` → container type in `ModContainers`. Block's `use()` calls `NetworkHooks.openGui`; container's packet constructor reads `BlockPos` from buffer and looks up the TE.

**Machine processing loop:** `AbstractMachineTileEntity.tick()` calls `canProcess()` → increments `progress` → calls `finishProcess()` at `maxProgress`. Override both hooks in subclasses. Progress syncs to the container automatically via `getDataAccess()` (`IIntArray` of `{progress, maxProgress}`).

**Capability exposure:** `AbstractMachineTileEntity.getCapability(cap, side)` filters by `SideMode` (stored as `byte[6]` in NBT key `side_config`). `null` side = internal/unrestricted access. Override `createSidedHandler(SideMode)` to assign slot ranges to faces.

**Tile entity sync to clients:** Override `getUpdateTag()` to narrow the NBT payload, then call `level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3)` when state clients need changes. `AbstractAxiomTileEntity` already provides `getUpdatePacket()` and `onDataPacket()`.

**Belt handoff:** `AbstractBeltTileEntity.tick()` calls `resolveNeighborOutput()` every tick (no caching) — returns an `IBeltOutput` backed by the adjacent belt or machine's item handler capability. `BeltInventory.tick(output, gameTick)` handles item advancement, blocking at end, and handoff attempts.

**JSON data loading:** Extend `com.axiom.common.util.SimpleJsonReloadListener` (not `net.minecraft.client.resources.JsonReloadListener` — that is client-only). Register the singleton instance via `AddReloadListenerEvent` in `Axiom.onAddReloadListeners()`.

## Design rules

- **Research unlocks capabilities, never stats.** `ResearchLoader` hard-rejects any `UnlockEntry` with type `STAT_MODIFIER` at load time. Efficiency gains must come from physical world upgrades (drill modules, cable tiers, belt replacements).
- **Belts are horizontal-only.** `AbstractBeltBlock.FACING` is constrained to `Direction.Plane.HORIZONTAL`. Vertical logistics go through dedicated blocks (T08).
- **All NBT keys are constants** in `com.axiom.common.util.AxiomNbtKeys`. No inline strings in `saveInternal`/`loadInternal`.
- **`LazyOptional` must be invalidated** in `onInvalidated()` (called from `setRemoved()`). Forgetting this causes capability leaks.
