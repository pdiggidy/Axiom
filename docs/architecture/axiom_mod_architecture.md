# Axiom Mod Architecture

**Platform:** Minecraft 1.16.5 · Forge 36.2.42 · Java 8
**Mod ID:** `axiom`
**Last updated:** 2026-03-27 (reflects T00–T03)

This document is the single place to look up how parts of the mod connect. It is meant to be updated as new systems land so neither the developer nor a future agent has to re-read all source files to understand structure.

---

## Table of Contents

1. [Entry Point & Boot Order](#1-entry-point--boot-order)
2. [Package Map](#2-package-map)
3. [Registration System](#3-registration-system)
4. [Block Hierarchy](#4-block-hierarchy)
5. [Tile Entity Hierarchy](#5-tile-entity-hierarchy)
6. [Side Configuration](#6-side-configuration)
7. [Container / Menu System](#7-container--menu-system)
8. [Network & Sync](#8-network--sync)
9. [Transport System (Belts)](#9-transport-system-belts)
10. [Recipe System](#10-recipe-system)
11. [Research & Progression](#11-research--progression)
12. [Utility Layer](#12-utility-layer)
13. [Data File Conventions](#13-data-file-conventions)
14. [NBT Key Registry](#14-nbt-key-registry)
15. [Design Invariants](#15-design-invariants)
16. [How to Add Things](#16-how-to-add-things)
17. [Planned Systems (T04–T12)](#17-planned-systems-t04t12)

---

## 1. Entry Point & Boot Order

**File:** `com.axiom.Axiom` (`src/main/java/com/axiom/Axiom.java`)

The `@Mod` class constructor runs at game startup and performs all registration. Order matters.

```
Axiom constructor
├── Adds mod-event-bus listeners (commonSetup, clientSetup)
├── Registers DeferredRegisters onto mod event bus:
│     ModBlocks.BLOCKS
│     ModItems.ITEMS
│     ModTileEntities.TILE_ENTITIES
│     ModContainers.CONTAINERS
│     ModRecipes.RECIPE_SERIALIZERS        ← via ModRecipes.register()
├── Registers Forge-event-bus listeners (this):
│     onServerStarting
│     onAddReloadListeners                 ← adds ResearchLoader.INSTANCE
└── MinecraftForge.EVENT_BUS.register(this)

commonSetup (FMLCommonSetupEvent)
└── AxiomNetwork.registerPackets()         ← registers SideConfigPacket on SimpleChannel

clientSetup (FMLClientSetupEvent)
└── (currently empty — future screen registration goes here)
```

**Rule:** All `DeferredRegister` objects must be registered onto the **mod event bus** in the constructor, not in `commonSetup`. The mod event bus fires registration events before `commonSetup` fires.

---

## 2. Package Map

```
com.axiom
├── Axiom.java                    ← @Mod entry point

com.axiom.common
├── block/                        ← Concrete block implementations
│   ├── base/                     ← Shared block behavior & block-state conventions
│   │     AbstractAxiomBlock      ← Root block base
│   │     AbstractMachineBlock    ← FACING + ACTIVE state, use() opens GUI
│   │     AbstractBeltBlock       ← FACING state, horizontal-only contract
│   ├── BasicMachineBlock
│   └── BasicBeltBlock

├── blockentity/                  ← RESERVED — future BlockEntity impls (post-migration)
│   └── base/                     ← RESERVED — future BE base classes

├── item/
│   └── AxiomItemGroup            ← Creative tab definition

├── menu/                         ← Concrete container/menu implementations
│   ├── base/
│   │     AbstractMachineContainer
│   └── BasicMachineContainer

├── network/                      ← Packets, sync messages, server–client plumbing
│   ├── AxiomNetwork              ← SimpleChannel bootstrap
│   └── SideConfigPacket          ← client→server face IO mode change

├── power/                        ← RESERVED — T04 electrical grid
├── rail/                         ← RESERVED — T09 minecart logistics
├── constructor/                  ← RESERVED — T10 schematic gantry
├── survey/                       ← RESERVED — T07 deposit world data

├── recipe/
│   ├── AxiomRecipeTypes          ← IRecipeType constants
│   ├── MachineRecipe             ← IRecipe<IInventory> for machine processing
│   ├── MachineRecipeSerializer   ← JSON + packet I/O for MachineRecipe
│   └── ModRecipes                ← DeferredRegister<IRecipeSerializer<?>>

├── registry/
│   ├── ModBlocks                 ← DeferredRegister<Block>
│   ├── ModItems                  ← DeferredRegister<Item>
│   ├── ModTileEntities           ← DeferredRegister<TileEntityType<?>>
│   └── ModContainers             ← DeferredRegister<ContainerType<?>>

├── research/
│   ├── ResearchLoader            ← Reload listener; loads axiom_research/ JSONs
│   ├── ResearchDefinition        ← Immutable research node POJO
│   ├── UnlockEntry               ← Single unlock within a definition
│   └── MachineTierDefinition     ← Physical tier records (belt, cable, shaft, …)

├── tile/                         ← Concrete tile entity implementations
│   ├── base/
│   │     AbstractAxiomTileEntity ← Save/load hooks, vanilla sync packet support
│   │     AbstractMachineTileEntity ← Ticking, processing loop, ISideConfig, IMachineStatus
│   │     AbstractBeltTileEntity  ← Controller/delegate, handoff, throughput
│   │     ISideConfig             ← Interface: per-face IO configuration
│   │     IMachineStatus          ← Interface: isActive() + statusKey for UI
│   │     SideMode                ← Enum: ANY | INPUT | OUTPUT | DISABLED
│   ├── BasicMachineTileEntity
│   └── BasicBeltTileEntity

├── transport/
│   ├── BeltInventory             ← Ordered item list with handoff & blocking
│   ├── TransportedItemStack      ← Per-item position record (belt coords)
│   ├── IBeltOutput               ← Handoff target interface
│   └── BeltThroughputTracker     ← Rolling-window items-handed-off counter

├── util/
│   ├── AxiomIds                  ← ResourceLocation helper (mod ID prefix)
│   ├── AxiomNbt                  ← Nullable BlockPos NBT helpers
│   ├── AxiomNbtKeys              ← All NBT key string constants
│   ├── AxiomDirections           ← isHorizontal / isVertical helpers
│   └── SimpleJsonReloadListener  ← Server-safe base for JSON reload listeners

└── world/                        ← RESERVED — T07/T09 saved data

com.axiom.client                  ← RESERVED — screens, overlays, renderers (T06+)
```

---

## 3. Registration System

All game objects use `DeferredRegister` so registration fires safely during Forge's registry event phase.

| Registry holder | Type | Where registered |
|---|---|---|
| `ModBlocks.BLOCKS` | `DeferredRegister<Block>` | `Axiom` constructor |
| `ModItems.ITEMS` | `DeferredRegister<Item>` | `Axiom` constructor |
| `ModTileEntities.TILE_ENTITIES` | `DeferredRegister<TileEntityType<?>>` | `Axiom` constructor |
| `ModContainers.CONTAINERS` | `DeferredRegister<ContainerType<?>>` | `Axiom` constructor |
| `ModRecipes.RECIPE_SERIALIZERS` | `DeferredRegister<IRecipeSerializer<?>>` | `ModRecipes.register()` in `Axiom` constructor |

**Registered objects:**

| Registry key | Class | Notes |
|---|---|---|
| `axiom:basic_belt` | `BasicBeltBlock` | Metal, no occlusion |
| `axiom:basic_machine` | `BasicMachineBlock` | Metal, strength 3.5/6.0 |
| `axiom:basic_belt` (item) | `BlockItem` | Wraps belt block |
| `axiom:basic_machine` (item) | `BlockItem` | Wraps machine block |
| `axiom:basic_belt` (TE) | `BasicBeltTileEntity` | Bound to belt block |
| `axiom:basic_machine` (TE) | `BasicMachineTileEntity` | Bound to machine block |
| `axiom:basic_machine` (container) | `BasicMachineContainer` | Opened by machine block's `use()` |
| `axiom:machine_processing` (serializer) | `MachineRecipeSerializer` | Reads machine recipe JSONs |

**Adding a new block/TE pair:** see [§16 — How to Add Things](#16-how-to-add-things).

---

## 4. Block Hierarchy

```
Block (Minecraft)
└── AbstractAxiomBlock              ← thin wrapper, no extra logic
    ├── AbstractMachineBlock        ← block states: FACING (horizontal) + ACTIVE (boolean)
    │   │  getStateForPlacement()   ← faces opposite to player
    │   │  use()                    ← opens GUI via NetworkHooks.openGui (server-side only)
    │   │  onRemove()               ← calls te.dropContents()
    │   │  createTileEntity()       ← abstract
    │   └── BasicMachineBlock
    │
    └── AbstractBeltBlock           ← block state: FACING (horizontal only)
        │  FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL)
        │  createTileEntity()       ← abstract
        └── BasicBeltBlock
```

**Block state conventions:**
- Machine blocks always have `FACING` (horizontal 4-way) and `ACTIVE` (boolean).
- Belt blocks always have `FACING` (horizontal 4-way). No `ACTIVE` state — belts are always running.
- `FACING` on a machine points toward the front (output face default). It is set to the direction **opposite** the player's horizontal look on placement.

---

## 5. Tile Entity Hierarchy

```
TileEntity (Minecraft)
└── AbstractAxiomTileEntity
    │  save() / load()              ← delegates to saveInternal / loadInternal
    │  setRemoved()                 ← calls onInvalidated() hook
    │  onInvalidated()              ← override to invalidate LazyOptionals
    │  getUpdatePacket()            ← wraps getUpdateTag() in SUpdateTileEntityPacket
    │  getUpdateTag()               ← calls saveInternal(); override to narrow scope
    │  onDataPacket()               ← calls loadInternal() on client
    │
    ├── AbstractMachineTileEntity   ← implements ITickableTileEntity, INamedContainerProvider,
    │   │                              ISideConfig, IMachineStatus
    │   │  tick()                   ← canProcess() → progress++ → finishProcess()
    │   │  canProcess()             ← abstract hook (override with recipe check)
    │   │  finishProcess()          ← abstract hook (override to consume/produce)
    │   │  getCapability(cap, side) ← filters by SideMode; returns sided wrapper
    │   │  createMenu()             ← abstract; returns concrete container
    │   │  getDataAccess()          ← IIntArray{progress, maxProgress} for container sync
    │   │  getSideMode(direction)   ← ISideConfig implementation
    │   │  setSideMode(dir, mode)   ← ISideConfig implementation, calls setChanged()
    │   │  isActive() / getStatusKey() ← IMachineStatus implementation
    │   │  sideConfig[6]            ← SideMode per Direction.get3DDataValue() index
    │   └── BasicMachineTileEntity
    │         canProcess()          ← looks up MachineRecipe via world.getRecipeManager()
    │         finishProcess()       ← consumes slot 0, produces slot 1
    │         getStatusKey()        ← "no_input" | "output_full" | "no_recipe" | null
    │
    └── AbstractBeltTileEntity      ← implements ITickableTileEntity
        │  tick()                   ← controller-only; resolves output, ticks BeltInventory
        │  resolveNeighborOutput()  ← discovers adjacent belt or machine cap each tick
        │  addItem(stack)           ← inserts at belt start if controller + space available
        │  canReceiveItem()         ← true if controller and BeltInventory.canAccept()
        │  getThroughputLastWindow() ← items/window for metrics display
        │  controllerPos            ← null = IS controller; set = delegate to controller
        └── BasicBeltTileEntity
```

### Ticking contract

Only **server-side** ticking ever modifies state (`level.isClientSide` guard in all tick methods). Client-side state comes exclusively from sync packets or chunk load data.

### Processing loop (machines)

```
Every tick (server only):
  wasActive = active
  active = canProcess()
  if active:
    progress = min(progress + 1, maxProgress)
    if progress == maxProgress:
      progress = 0
      finishProcess()
  else:
    progress = 0               ← resets on stall, no partial credit
  if wasActive != active:
    setBlockState(ACTIVE = active)
  setChanged()
```

---

## 6. Side Configuration

**Interface:** `ISideConfig` (`com.axiom.common.tile.base`)
**Enum:** `SideMode` (ANY · INPUT · OUTPUT · DISABLED)
**Implemented by:** `AbstractMachineTileEntity`

### How sides work

```
AbstractMachineTileEntity.sideConfig[6]
  index = Direction.get3DDataValue()
  0 = DOWN, 1 = UP, 2 = NORTH, 3 = SOUTH, 4 = WEST, 5 = EAST
  default = ANY (all faces expose full inventory)
```

`getCapability(ITEM_HANDLER_CAPABILITY, side)`:
- `null` side → full inventory (used by internal/automation code that ignores face rules)
- `DISABLED` → `LazyOptional.empty()`
- `ANY` → full `inventoryCapability`
- `INPUT` → `RangedWrapper` with `extractItem` disabled
- `OUTPUT` → `RangedWrapper` with `insertItem` disabled

**Changing sides at runtime:** client sends `SideConfigPacket` → server validates reach → calls `setSideMode()` → `setChanged()` → GUI/overlay updates from the next sync. Subclasses can override `createSidedHandler(SideMode)` to assign specific slots to specific modes.

**NBT key:** `SIDE_CONFIG` — stored as `byte[6]`, one byte per direction ordinal.

---

## 7. Container / Menu System

```
Container (Minecraft)
└── AbstractMachineContainer
    │  tileEntity field             ← AbstractMachineTileEntity reference
    │  addMachineSlots(te)          ← abstract; subclass adds SlotItemHandler rows
    │  addPlayerInventory()         ← standard 3×9 + hotbar at y=84/142
    │  stillValid()                 ← player within 8 blocks of TE position
    │  getMachineInventory()        ← gets IItemHandler capability from TE (null side)
    └── BasicMachineContainer
          slots: 2 slots at (44,35) with 68px horizontal gap
          packet constructor: reads BlockPos from buffer, looks up TE in client world
```

**Progress bar sync:** The container binds `te.getDataAccess()` (an `IIntArray` of {progress, maxProgress}) via `addDataSlots()`. Minecraft's container sync sends this to the client automatically each tick without a custom packet.

**Opening a GUI:** `AbstractMachineBlock.use()` calls `NetworkHooks.openGui(serverPlayer, te, buf -> buf.writeBlockPos(te.getBlockPos()))`. The packet constructor on the client reads the `BlockPos` and looks up the TE.

---

## 8. Network & Sync

### SimpleChannel

**Class:** `AxiomNetwork` — channel ID `axiom:main`, protocol version `"1"`
**Registration:** `AxiomNetwork.registerPackets()` called in `commonSetup`

| Packet | Direction | Purpose |
|---|---|---|
| `SideConfigPacket` | Client → Server | Change one face's `SideMode` |

### Tile entity sync (vanilla mechanism)

`AbstractAxiomTileEntity` provides:
- `getUpdatePacket()` → `SUpdateTileEntityPacket` wrapping `getUpdateTag()`
- `getUpdateTag()` → calls `saveInternal()` (subclasses may narrow this)
- `onDataPacket()` → calls `loadInternal()` on the client

To push an update to watching clients, call:
```java
level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
```
This triggers Minecraft to call `getUpdatePacket()` and deliver it to all clients with the chunk loaded.

**Container data sync** uses `IIntArray` + `addDataSlots()` — handled automatically by the container system, no extra packet needed.

### Client / common boundary rule

`com.axiom.common` must never import `net.minecraft.client.*`. Client responses to packets belong in `com.axiom.client` (to be created in T06). Use `DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)` in packet handlers that need to reach the client world.

---

## 9. Transport System (Belts)

All belt logic is horizontal-only by contract. Vertical movement is handled exclusively by future logistics blocks (Gravity Chute, Bucket Elevator, Pneumatic Tube — T08).

### Class relationships

```
AbstractBeltTileEntity
│  inventory: BeltInventory         ← owns the item list for this controller segment
│  throughput: BeltThroughputTracker
│  controllerPos: BlockPos?         ← null = IS controller
│
│  tick() calls:
│    resolveNeighborOutput()        ← world lookup: adjacent belt or machine handler
│    BeltInventory.tick(output, gameTick) → returns handoff count
│    BeltThroughputTracker.recordHandoff()

BeltInventory
│  items: List<TransportedItemStack>  ← sorted descending by beltPosition each tick
│  speed: float (default 0.125 blocks/tick)
│  BELT_LENGTH = 1.0
│  MIN_ITEM_SPACING = 0.25
│
│  tick(output, gameTick):
│    sort items by position (highest first)
│    for each item:
│      compute maxAdvance = min(speed, gap_to_item_ahead - MIN_ITEM_SPACING)
│      if leading item reaches end: attempt output.insert()
│        success → remove item, count handoff
│        fail    → clamp position at 1.0 (blocked)
│      else: advance(maxAdvance)

TransportedItemStack
│  stack: ItemStack (immutable copy)
│  beltPosition: float  [0.0 = input, 1.0 = output]
│  previousPosition: float  (for client interpolation)
│  sideOffset: float  (visual lane variation)
│  advance(delta): previousPosition = beltPosition; beltPosition += delta

IBeltOutput
│  BLOCKED constant (always refuses)
│  canAccept(stack): boolean
│  insert(stack, simulate): ItemStack remainder

BeltThroughputTracker
│  windowTicks: int (default 20 = 1 second)
│  recordHandoff(count, gameTick)
│  getLastWindowCount(): int  ← use for display (stable between resets)
```

### Neighbor resolution (called every controller tick)

```
resolveNeighborOutput():
  facing = blockState.getValue(FACING)
  neighbor = level.getBlockEntity(worldPosition.relative(facing))

  if neighbor instanceof AbstractBeltTileEntity:
    → IBeltOutput backed by neighbor.addItem() / neighbor.canReceiveItem()

  else if neighbor has ITEM_HANDLER_CAPABILITY on facing.getOpposite():
    → IBeltOutput backed by ItemHandlerHelper.insertItem()

  else:
    → IBeltOutput.BLOCKED
```

### NBT persistence for belts

Items are serialized as a `ListNBT` of `CompoundNBT` (via `TransportedItemStack.serializeNBT()`). `controllerPos` uses nullable BlockPos helpers from `AxiomNbt`. `THROUGHPUT` stores the last window count for debug display after reload.

---

## 10. Recipe System

**Recipe type:** `axiom:machine_processing`
**IRecipeType constant:** `AxiomRecipeTypes.MACHINE_PROCESSING`
**Serializer registry key:** `axiom:machine_processing`
**JSON folder:** `data/<namespace>/recipes/machine/` (standard Minecraft recipe folder)

### JSON format

```json
{
  "type": "axiom:machine_processing",
  "input": { "item": "minecraft:iron_ore" },
  "output": { "item": "minecraft:iron_ingot", "count": 1 },
  "processing_time": 80
}
```

`input` accepts any Forge `Ingredient` format (item, tag, array of alternatives).
`processing_time` must be > 0; defaults to 100 if omitted; serializer throws loudly on invalid values.

### Query pattern (in tile entity)

```java
world.getRecipeManager()
     .getRecipesFor(AxiomRecipeTypes.MACHINE_PROCESSING,
                    new Inventory(inventory.getStackInSlot(0)), world)
     .stream().findFirst()
```

`MachineRecipe.matches()` checks slot 0 of the provided inventory against the `Ingredient`. Future machines with multiple input slots should either wrap multiple slots in an `Inventory` or add a dedicated recipe type.

### Seeded recipes

| File | Input | Output | Time |
|---|---|---|---|
| `basic_smelt.json` | `minecraft:iron_ore` | `minecraft:iron_ingot` | 80t |
| `basic_smelt_gold.json` | `minecraft:gold_ore` | `minecraft:gold_ingot` | 80t |

---

## 11. Research & Progression

### Core design rule

> Research unlocks **capabilities** (new machine types, new item tiers, new game actions).
> Research never modifies **stats** invisibly.
> All efficiency gains come from **physical upgrades** placed in the world (drill modules, cable tiers, shaft widths).

The loader enforces this at load time: any `UnlockEntry` with type `STAT_MODIFIER` throws `IllegalArgumentException` and is rejected with a log error. The `STAT_MODIFIER` enum value exists only to be caught and rejected.

### Class summary

```
ResearchLoader (extends SimpleJsonReloadListener)
│  folder: "axiom_research" → data/<ns>/axiom_research/<name>.json
│  INSTANCE: singleton, registered on Forge event bus via AddReloadListenerEvent
│  definitions: Map<ResourceLocation, ResearchDefinition>  ← volatile, replaced on reload
│  apply(): parses JSON, validates each def, logs errors, never crashes

ResearchDefinition
│  id: ResourceLocation
│  displayName: String
│  prerequisites: List<ResourceLocation>   ← IDs of definitions that must be complete first
│  unlocks: List<UnlockEntry>
│  catalysts: List<ResourceLocation>       ← optional acceleration items (never mandatory)

UnlockEntry
│  type: UnlockType (MACHINE | ITEM | CAPABILITY | TIER | STAT_MODIFIER[rejected])
│  target: ResourceLocation

MachineTierDefinition (loaded by a future tier loader — schema defined, loader pending T05)
│  id, displayName, tier: int, category: String, unlockedByResearch: ResourceLocation
```

### Seeded research nodes

| File | Display name | Prerequisites | Unlocks |
|---|---|---|---|
| `basic_automation.json` | Basic Automation | — | machine:`axiom:basic_machine`, item:`axiom:basic_belt` |
| `power_grid.json` | Power Grid | `axiom:basic_automation` | capability:`axiom:electrical_grid` |

### Query pattern

```java
ResearchLoader.INSTANCE.getDefinition(new ResourceLocation("axiom", "basic_automation"))
    .ifPresent(def -> { /* check prerequisites, surface unlocks */ });
```

Player/world progression state (tracking which nodes are completed) is deferred to T05.

---

## 12. Utility Layer

All utilities are in `com.axiom.common.util`. They are stateless helpers or constant holders with private constructors.

| Class | Purpose |
|---|---|
| `AxiomIds` | `id(String path)` → `ResourceLocation("axiom", path)` |
| `AxiomNbtKeys` | String constants for every NBT key used in the mod |
| `AxiomNbt` | `putNullableBlockPos` / `getNullableBlockPos` for optional positions |
| `AxiomDirections` | `isHorizontal(Direction)` / `isVertical(Direction)` |
| `SimpleJsonReloadListener` | Abstract server-safe base for JSON data loaders (implements `IFutureReloadListener`) |

**`SimpleJsonReloadListener`** replaces `net.minecraft.client.resources.JsonReloadListener` (which is client-only in 1.16.5). Subclasses pass a `Gson` and folder name, then implement `apply(Map, IResourceManager, IProfiler)`. Files are read from `data/<any-namespace>/<folder>/<name>.json`.

---

## 13. Data File Conventions

| Content type | Folder | Extension | Loaded by |
|---|---|---|---|
| Machine recipes | `data/<ns>/recipes/machine/` | `.json` | Vanilla recipe manager |
| Research definitions | `data/<ns>/axiom_research/` | `.json` | `ResearchLoader` |
| Machine tier defs (future) | `data/<ns>/axiom_tiers/` | `.json` | Tier loader (T05) |
| Loot tables | `data/<ns>/loot_tables/` | `.json` | Vanilla |
| Tags | `data/<ns>/tags/` | `.json` | Vanilla |

**Naming:** `snake_case` filenames, always. Namespace is `axiom` for first-party content.

---

## 14. NBT Key Registry

All keys live in `AxiomNbtKeys`. Never use string literals directly in save/load code.

| Constant | Value | Used by |
|---|---|---|
| `VERSION` | `"version"` | future migration |
| `BLOCK_POS` | `"block_pos"` | generic position |
| `DIRECTION` | `"direction"` | generic direction |
| `INDEX` | `"index"` | generic index |
| `COUNT` | `"count"` | generic count |
| `OWNER` | `"owner"` | future player/team ownership |
| `TARGET` | `"target"` | future routing |
| `ENERGY` | `"energy"` | T04 power |
| `FILTER` | `"filter"` | future item filters |
| `CHANNEL` | `"channel"` | future network channels |
| `INVENTORY` | `"inventory"` | machine inventory NBT |
| `PROGRESS` | `"progress"` | machine processing progress |
| `MAX_PROGRESS` | `"max_progress"` | machine processing max |
| `ACTIVE` | `"active"` | machine active flag |
| `CONTROLLER_POS` | `"controller_pos"` | belt controller reference |
| `ITEMS` | `"items"` | belt item list |
| `BELT_POSITION` | `"belt_position"` | item position on belt |
| `PREVIOUS_POSITION` | `"previous_position"` | item previous position |
| `SIDE_OFFSET` | `"side_offset"` | item visual lane offset |
| `SIDE_CONFIG` | `"side_config"` | machine face IO modes |
| `THROUGHPUT` | `"throughput"` | belt throughput last window |

---

## 15. Design Invariants

These rules must not be broken by any task. They are derived from the design documents.

### Never break
1. **No `net.minecraft.client` imports in common packages.** Common code must work on a dedicated server. Use `DistExecutor` at call sites that branch on Dist.
2. **Belts are horizontal-only.** `AbstractBeltBlock.FACING` is constrained to `Direction.Plane.HORIZONTAL`. Vertical movement goes through Gravity Chute / Bucket Elevator / Pneumatic Tube (T08).
3. **Research never encodes stat modifiers.** `ResearchLoader.validateDefinition()` enforces this at load time. Physical tier upgrades in the world are the only allowed efficiency mechanism.
4. **All game objects go through `DeferredRegister`.** No `Registry.register()` calls at class-init time.
5. **LazyOptional must be invalidated in `setRemoved()`.** `AbstractAxiomTileEntity.onInvalidated()` is the hook. Forgetting this causes capability memory leaks.
6. **Only the server modifies state.** Every tick method guards `level.isClientSide`. Client state comes from sync packets or chunk data.

### Convention rules
7. Block names: `NounBlock`. Tile entity names: `NounTileEntity` (existing) / `NounBlockEntity` (new code targeting `blockentity` package after migration). Container names: `NounContainer`. Screen names: `NounScreen`. Packet names: `NounPacket`.
8. New machines inherit from `AbstractMachineTileEntity`. New belts inherit from `AbstractBeltTileEntity`. Do not bypass the base classes.
9. All NBT string keys are constants in `AxiomNbtKeys`. No inline strings.
10. `setChanged()` marks dirty for saving. `level.sendBlockUpdated(...)` pushes state to watching clients. Call both when state that clients care about changes.

---

## 16. How to Add Things

### New machine

1. Create `NewMachineBlock extends AbstractMachineBlock` — implement `createTileEntity()`.
2. Create `NewMachineTileEntity extends AbstractMachineTileEntity` — implement `canProcess()`, `finishProcess()`, `createMenu()`.
3. Create `NewMachineContainer extends AbstractMachineContainer` — implement `addMachineSlots()`.
4. Register block in `ModBlocks`, BlockItem in `ModItems`, TE type in `ModTileEntities`, container in `ModContainers`.
5. Add machine recipe JSON under `data/axiom/recipes/machine/`.
6. Add a research unlock entry if this machine should be gated.

**Per-face IO:** Override `createSidedHandler(SideMode mode)` to assign specific slot ranges (e.g. slots 0–1 for input, slot 2 for output) to specific modes. Default behavior exposes all slots in the allowed direction.

### New machine recipe

Drop a JSON file in `data/axiom/recipes/machine/`:
```json
{
  "type": "axiom:machine_processing",
  "input": { "tag": "forge:ores/copper" },
  "output": { "item": "minecraft:copper_ingot", "count": 1 },
  "processing_time": 80
}
```
No code changes required. `MachineRecipeSerializer` handles all Forge `Ingredient` formats.

### New research node

Drop a JSON file in `data/axiom/axiom_research/`:
```json
{
  "display_name": "Node Name",
  "prerequisites": ["axiom:basic_automation"],
  "unlocks": [
    { "type": "machine", "target": "axiom:new_machine" }
  ],
  "catalysts": []
}
```
No code changes required. `ResearchLoader` picks it up on next data reload. Unlock enforcement logic belongs in T05.

### New belt type

1. Create `NewBeltBlock extends AbstractBeltBlock` — implement `createTileEntity()`.
2. Create `NewBeltTileEntity extends AbstractBeltTileEntity` — constructor only unless custom behavior is needed.
3. Register in `ModBlocks`, `ModItems`, `ModTileEntities`.
4. Speed, item spacing, and handoff rules are all in `BeltInventory` / `AbstractBeltTileEntity` and are inherited automatically.

### New packet

1. Create `NewActionPacket` with static `encode`, `decode`, `handle` methods.
2. In `AxiomNetwork.registerPackets()`, add:
   ```java
   CHANNEL.registerMessage(nextPacketId++, NewActionPacket.class,
       NewActionPacket::encode, NewActionPacket::decode, NewActionPacket::handle);
   ```
3. Keep client-side handler logic in `com.axiom.client` behind `DistExecutor`.

### New JSON data type

1. Create a POJO with a `fromJson(ResourceLocation, JsonObject)` factory.
2. Create a loader extending `SimpleJsonReloadListener` with a singleton `INSTANCE`.
3. Register it in `Axiom.onAddReloadListeners()`.

---

## 17. Planned Systems (T04–T12)

| Task | System | Home package | Status | Key dependencies |
|---|---|---|---|---|
| T04 | Electrical grid, cable tiers, brownout model | `common.power` | Planned | T01, T02, T03 |
| T05 | Research terminal, progression tracking, unlock enforcement | `common.research` | Partial (data layer done) | T01, T02 |
| T06 | Metrics-first UI, screens, overlays | `client.gui`, `client.overlay` | Planned | T04, T05 |
| T07 | Deposit world data, seismic surveyor, Mine Head | `common.survey`, `common.world` | Planned | T01, T02, T04, T05 |
| T08 | Vertical logistics (chute, elevator, tube) + anchors | `common.logistics`, `common.anchor` | Planned | T01, T03, T07 |
| T09 | Minecart logistics, stations, signals, consists | `common.rail` | Planned | T01, T02, T03, T07 |
| T10 | Constructor gantry, schematic scanner | `common.constructor` | Planned | T01, T02, T03, T04, T06 |
| T11 | Factory-map overlays, top-down diagnostics | `client.overlay` | Planned | T06, T08, T10 |
| T12 | Integration, balance, server verification | whole project | Planned | T04–T10 |

### Key seams to know for upcoming tasks

**T04 (power):**
- Machines expose a power consumption hook via `AbstractMachineTileEntity` (slot TBD).
- Power state should scale machine work rate (brownout = slower progress), not hard-stop it.
- Cable blocks expose the `common.power` capability; the grid engine manages network topology.

**T05 (research progression):**
- `ResearchLoader.INSTANCE.getDefinitions()` is already available.
- Player progress storage (which nodes complete) needs to be added — probably `PlayerCapability` or `WorldSavedData` if team-scoped.
- `UnlockEntry` types (MACHINE, ITEM, CAPABILITY, TIER) are the enforcement vocabulary.

**T06 (UI):**
- `IMachineStatus.getStatusKey()` and `getDataAccess()` are already the data contract for screens.
- `BeltThroughputTracker.getLastWindowCount()` is the data contract for belt overlays.
- Screens belong exclusively in `com.axiom.client`. Never let screen code reach into common packages that reference it back.

**T08 (vertical logistics):**
- `IBeltOutput` is the handoff contract belts already use. Vertical block inputs can implement it so belts hand off to them naturally.
- Anchor coverage API should be accessible via a `WorldSavedData` object in `common.world`.

**T09 (rail):**
- Station load/unload needs `IBeltOutput`-compatible interop with belts.
- Route reservation state is world-scoped saved data.
