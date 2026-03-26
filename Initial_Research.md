# Building a Factorio-inspired Minecraft mod on 1.16.x

**Forge is the only viable framework for this project**, and the path to a working Factorio-style mod is well-trodden by mods like Create, Mekanism, and Immersive Engineering — all open-source, all targeting 1.16.5, and all containing battle-tested patterns for belts, machines, and logistics. The biggest technical risk isn't the automation systems (those have proven solutions) but the optional top-down camera, which requires deep rendering pipeline modifications and at least 3–4 weeks of dedicated work. Below is everything an intermediate Java developer needs to go from zero to a working prototype.

---

## Forge wins decisively for automation mods on 1.16.x

The framework decision is straightforward. **Forge's capability system** — `IItemHandler`, `IFluidHandler`, and `IEnergyStorage` — is the universal interop standard that every major tech mod uses. These interfaces let your belts push items into any Mekanism machine, your machines accept Forge Energy from any generator, and your pipes move fluids to/from any compatible tank. Fabric on 1.16.x lacked equivalent infrastructure: its Transfer API was experimental, energy had no standard (mods relied on third-party libraries like Tech Reborn Energy), and the ecosystem had roughly **one-fifth** the tech mods Forge offered.

The capability system works through `LazyOptional<T>` wrappers accessed via `TileEntity.getCapability(Capability<T> cap, @Nullable Direction side)`. The `Direction` parameter is critical for automation — it lets a machine expose input slots on its top face, output slots on the bottom, and energy on the sides, which is exactly the side-aware I/O that Factorio-style gameplay demands. The built-in implementations (`ItemStackHandler`, `FluidTank`, `EnergyStorage`) handle serialization, slot limits, and validation out of the box.

**Performance is your responsibility regardless of framework.** Neither Forge nor Fabric provides special optimization for thousands of ticking block entities. In 1.16.x, tile entities implement `ITickableTileEntity` and get called sequentially from an `ArrayList` on the server thread — no spatial partitioning, no parallelism. Fabric has Lithium (which provides ~45% server tick improvement), but that's a general optimization mod, not a substitute for smart architecture in your own belt system. The performance techniques that matter — controller-delegate patterns, network graph caching, lazy ticking — are mod-level design decisions covered in the belt implementation section below.

For tooling, Forge uses the MDK (Mod Development Kit) with ForgeGradle 3. Setup involves downloading the MDK zip from `files.minecraftforge.net`, extracting it, and running `gradlew genIntellijRuns`. Use **Official (Mojang) mappings with Parchment** for the best development experience — Official provides class/method/field names, and Parchment (via the Librarian Gradle plugin from `parchmentmc.org`) adds the parameter names and javadocs that Official mappings lack. The `build.gradle` configuration is approximately 100–150 lines. **Java 8** is the required JDK version for 1.16.x — do not use Java 16 or 17.

---

## How to build belts that scale to thousands of segments

The single most important architectural decision for belt performance is whether items exist as world entities or as data inside a controller. **Create mod's controller-delegate pattern is the gold standard** and should be your starting template. Immersive Engineering's entity-based approach is simpler to implement but collapses at Factorio scale.

### Create's belt architecture

In Create (branch `mc1.16/dev` on GitHub at `Creators-of-Create/Create`), a belt spanning 20 blocks places a `BeltBlock` at every position, but **only the first segment (the "controller") holds a `BeltInventory`**. All other segments store a relative offset to the controller and delegate via `getControllerTE()`. Items on the belt are `TransportedItemStack` objects — plain data with a `beltPosition` float, `sideOffset`, `prevBeltPosition` for interpolation, and an `ItemStack`. The `BeltInventory.tick()` method advances each item's position based on RPM. This reduces tick cost from O(n) per belt block to O(1) per belt strip.

Key classes in `com.simibubi.create.content.contraptions.relays.belt`:

- **`BeltTileEntity`** extends `KineticTileEntity` — each segment's tile entity; delegates inventory operations to controller
- **`BeltInventory`** — the actual item transport logic; stores `List<TransportedItemStack>`, advances positions per tick
- **`BeltItemHandler`** — implements `IItemHandler` to expose belt contents to the Forge capability system, enabling funnels, hoppers, and cross-mod interaction
- **`BeltRenderer`** — a `TileEntityRenderer` that draws items at interpolated positions using `Minecraft.getInstance().getItemRenderer().renderStatic()`; belt texture scrolls via UV animation driven by rotation speed

Create also uses the **Flywheel** library for instanced rendering, batching hundreds of belt segment draw calls into a single GPU operation. This is essential for visual performance at scale.

### What to avoid: entity-based transport

Immersive Engineering (`BluSunrize/ImmersiveEngineering` on GitHub, branch `1.16.5`) spawns actual `ItemEntity` objects and modifies their `setDeltaMovement()` vectors each tick in `onEntityCollision()`. This gives you free rendering (entities render themselves) and physical interaction, but each entity carries collision detection, network synchronization, and tick overhead. IE's conveyor classes in `blusunrize.immersiveengineering.common.blocks.metal.conveyors` — particularly `ConveyorHandler` and the `IConveyorBelt` interface — are worth studying for their clean type registry and upgrade system, but don't copy the transport mechanism for a Factorio-scale mod.

### Performance techniques for large networks

- **Lazy ticking**: Skip `tick()` for belts carrying zero items. Create does this implicitly since empty `BeltInventory` lists iterate instantly, but an explicit sleep state saves the method call overhead.
- **Network graph caching**: Mekanism's `LogisticalTransporterBase` runs pathfinding per-pipe per-tick, which their own community identified as a scalability bottleneck (GitHub issue #7983). **Pre-compute paths when topology changes** (block placed/broken) and cache them. Mekanism's `TransporterPathfinder` uses Dijkstra-like traversal — study it as a cautionary example.
- **Capability caching**: When a belt pushes items to an adjacent machine, cache the neighbor's `LazyOptional<IItemHandler>` and register an invalidation listener rather than querying every tick. Pattern: `target.addListener(self -> cache.put(dir, null))`.
- **Avoid per-tick NBT serialization**: Use dirty flags (`setChanged()`) and let Minecraft's chunk save system handle persistence at its own cadence.
- **Item batching**: A stack of 64 identical items on a belt should have the same overhead as a single item. Track stacks, not individual items.

### Rendering items on belts

Register a `TileEntityRenderer` via `ClientRegistry.bindTileEntityRenderer(BeltTileEntity.class, BeltRenderer::new)` in `FMLClientSetupEvent`. In the `render()` method, iterate through belt items and draw each `ItemStack` at its interpolated world position using `partialTicks` between `prevBeltPosition` and `beltPosition`. Apply `poseStack.translate()` and `poseStack.scale()` transforms. For hundreds of belts, consider Create's Flywheel-style instanced rendering to avoid per-segment draw calls.

---

## Machines, recipes, and energy follow established Forge patterns

### Custom crafting machines

The standard architecture for a processing machine involves four classes:

1. **`MachineBlock extends Block`** — handles placement, blockstate properties (facing, active/inactive), and opens the GUI via `NetworkHooks.openGui()` in the `use()` override
2. **`MachineTileEntity extends TileEntity implements ITickableTileEntity`** — holds `ItemStackHandler` inventories (input/output), an `EnergyStorage`, processing progress counter, and recipe lookup logic
3. **`MachineContainer extends Container`** — links server-side inventories to client GUI slots using `SlotItemHandler`; syncs integer data (progress, energy) via `addDataSlot()`
4. **`MachineScreen extends ContainerScreen<MachineContainer>`** — renders the GUI texture, progress arrows, energy bars, and tooltips

The tile entity's `tick()` method follows this pattern: check `level.isClientSide` (skip if true), find a matching recipe via `level.getRecipeManager().getRecipeFor()`, verify inputs and output space, increment progress, consume inputs and produce outputs when complete. Call `setChanged()` after any state modification.

For **multi-input/output machines with side configuration**, expose different `ItemStackHandler` instances on different faces via the capability system. Use `CombinedInvWrapper` to merge handlers for the null-direction case. Restrict output slots to extraction-only by wrapping in a custom handler that returns `false` from `insertItem()`.

### Custom recipe system

Minecraft 1.16.x uses three components for custom recipes, all documented at `forge.gemwire.uk/wiki/Custom_Recipes/1.16`:

- **`IRecipe<C>`** — defines `matches()`, `assemble()`, `getSerializer()`, and `getType()`. For multi-input assembler recipes, store a `NonNullList<Ingredient>` and iterate through inputs in `matches()`.
- **`IRecipeType<T>`** — a category identifier registered via `IRecipeType.register("mymod:assembler")` during `FMLCommonSetupEvent` inside `enqueueWork()` (thread-safety requirement).
- **`IRecipeSerializer<T>`** — handles JSON parsing (`fromJson`) and network sync (`fromNetwork`/`toNetwork`). Register as a Forge registry object via `RegistryEvent.Register<IRecipeSerializer<?>>`.

Recipes live as JSON files in `data/mymod/recipes/` and support Forge's tag system (`"tag": "forge:gears/iron"`) for flexible ingredient matching. The recipe manager caches all loaded recipes, so lookups via `getRecipeFor()` are efficient.

### Energy and fluids

**Forge Energy (`IEnergyStorage`)** uses a push model: generators call `extractEnergy()` on themselves and `receiveEnergy()` on adjacent blocks each tick. The `EnergyStorage` implementation handles capacity, max transfer rates, and simulation mode. For energy networks (cables/wires), study Mekanism's `DynamicNetwork<>` base class in `mekanism.common.content.network` — it provides a generic graph pattern reused for `EnergyNetwork`, `FluidNetwork`, and `InventoryNetwork`.

**Fluid handling (`IFluidHandler`)** works analogously to items. `FluidTank` is the default implementation; `FluidUtil.tryFluidTransfer()` handles transfers between handlers; `FluidUtil.interactWithFluidHandler()` manages bucket interactions.

---

## A custom research system requires building beyond advancements

Minecraft's built-in advancement system is **insufficient for Factorio-style research**. Advancements are binary (complete/incomplete), have no resource costs, offer no partial progress, and their recipe-unlocking only affects the recipe book — it doesn't actually prevent crafting. A proper tech tree needs a custom system.

### Recommended architecture

Build a **JSON data-driven research tree** with a Java runtime engine. Define research entries in `data/mymod/research/`:

```json
{
  "id": "basic_automation",
  "parent": "manual_crafting",
  "costs": [
    { "item": "minecraft:iron_ingot", "count": 32 },
    { "item": "minecraft:redstone", "count": 16 }
  ],
  "research_time": 200,
  "unlocks": ["mymod:assembler", "mymod:inserter"],
  "prerequisites": ["manual_crafting"]
}
```

Load these via a `ResearchManager extends SimpleJsonResourceReloadListener` that parses JSON into `ResearchEntry` objects and builds the tree graph in memory. Store per-player progress in a **Forge Capability** attached via `AttachCapabilitiesEvent<Entity>`. The capability tracks a `Set<ResourceLocation>` of unlocked research IDs, serializes to NBT, and **must be manually synced to clients** via custom network packets — capabilities do not auto-sync.

Critical lifecycle events to handle: sync all data on `PlayerLoggedInEvent`, copy data in `PlayerEvent.Clone` (death/respawn), and re-sync on `PlayerChangedDimensionEvent`.

### Gating recipes behind research

The cleanest approach is a **custom crafting block** (like a "Factorio Assembler") whose `Container` checks the player's research capability before producing output. For gating vanilla crafting, subscribe to `PlayerEvent.ItemCraftedEvent` and void the output if the player lacks the required research. For block placement gating, cancel `BlockEvent.EntityPlaceEvent`. The **GameStages API** by Darkhax (available for 1.16.5) provides a proven framework for this pattern — it stores named boolean flags per player with automatic persistence and syncing, and addon mods like Recipe Stages hook into the crafting system to enforce stage requirements.

### Tech tree UI

Build a custom `Screen` subclass with scrollable, zoomable rendering. Use `RenderSystem.enableScissor()` to clip the viewport, `MatrixStack.translate()` and `scale()` for pan/zoom, and handle `mouseDragged()` for panning and `mouseScrolled()` for zooming. The vanilla `AdvancementScreen` class is a useful reference for the node-and-connection rendering pattern. Draw research nodes using `blit()` for textures and `drawString()` for labels, with color/opacity indicating locked/in-progress/completed states.

---

## Top-down camera is feasible but expensive to implement

A toggleable top-down or isometric camera **has been proven viable** by mods like Dungeons Perspective (Fabric, 125K+ downloads) which provides full gameplay in a 45° top-down view with zoom, rotation, and block culling. However, it requires modifying Minecraft's rendering pipeline at a deep level. Budget **3–4 weeks minimum** for a basic implementation, longer for polish.

### What needs to change

Three systems require modification via Mixins (Forge supports Mixins in 1.16.x):

**Camera positioning** — Inject into `ActiveRenderInfo.setup()` (the 1.16.x camera class) to position the camera above the player at a fixed pitch (-60° to -90°) and controlled yaw. This is the easiest part.

**Projection matrix** — Inject into `GameRenderer.getProjectionMatrix()` to replace the perspective `Matrix4f` with `Matrix4f.orthographic()` for true isometric, or simply increase FOV for a pseudo-top-down perspective view. The orthographic projection eliminates depth-based size scaling, giving the flat Factorio look.

**Frustum culling** — This is **the hardest problem**. Minecraft's `ClippingHelper` culls chunks outside the camera's perspective frustum. Looking straight down creates a narrow frustum that misses chunks visible in an orthographic projection. You must override frustum setup to match the actual visible area, or significantly expand the frustum bounds. Additionally, Minecraft's occlusion culling (`VisGraph.computeVisibility()`) can incorrectly hide chunks that are "behind" each other from a top-down view. The Mineshot mod documents this exact issue: "chunks that are rendered depend upon chunks visible in the standard camera view."

Secondary challenges include remapping WASD to screen-relative movement, raycasting from cursor position through the top-down projection for block interaction, disabling hand rendering, suppressing fog (designed for horizontal distance, not vertical), and handling block occlusion between camera and player (Dungeons Perspective solves this with flood-fill room detection).

### Practical recommendation

**Implement this as a secondary "factory view" mode**, not the primary camera. Trigger it via keybind or item, restrict it to factory management (placing machines, routing belts, inspecting throughput), and keep first-person for exploration and combat. This dramatically reduces scope — you don't need to support combat, inventory management, or all block interactions in top-down view. A simpler alternative is a **2D factory map overlay** rendered as a custom `Screen`, which avoids the rendering pipeline entirely.

---

## Six open-source mods to study, and where to look in each

The most educational source repositories for a Factorio-style mod, with specific classes to examine:

**Create** (`Creators-of-Create/Create`, branch `mc1.16/dev`) — Study `BeltTileEntity` and `BeltInventory` for the controller-delegate belt pattern. `KineticTileEntity` and `KineticNetwork` demonstrate a stress-based power system analogous to Factorio's electricity. `TileEntityBehaviour` shows a composition pattern for modular block entity features. `ProcessingRecipe` and `ProcessingRecipeBuilder` demonstrate a clean custom recipe framework. Custom license — study permitted, redistribution restricted.

**Mekanism** (`mekanism/Mekanism`, branch `1.16.x`, **MIT license** — most permissive) — Study `LogisticalTransporterBase` and `TransporterPathfinder` for pipe network routing. `TileEntityMekanism` is the root machine class; `TileComponentConfig` handles per-side I/O configuration. `DynamicNetwork<>` provides a reusable generic network graph for energy, fluids, items, and chemicals. Their recipe system in `mekanism.api.recipes` demonstrates multi-type I/O (items + fluids + gases).

**Immersive Engineering** (`BluSunrize/ImmersiveEngineering`, branch `1.16.5`) — Study `ConveyorHandler` and `IConveyorBelt` for a clean conveyor type registry with modular variants (splitting, extracting, vertical). The multiblock system (`IMultiblock`, `TemplateMultiblock`) shows how to implement large structures from individual blocks with master/slave delegation.

**Applied Energistics 2** (`AppliedEnergistics/Applied-Energistics-2`, **LGPL-3.0**) — Study `Grid` and `GridNode` for network discovery and management. The autocrafting system in `appeng.crafting` calculates multi-step recipe trees — valuable for understanding queued processing. The channel system (8/32 channels per cable) models bandwidth-limited logistics.

**Industrial Foregoing** (`InnovativeOnlineIndustries/Industrial-Foregoing`, branch `1.16`) — Conveyor belt system with upgrade slots (Extraction, Insertion, Splitting, Bouncing). Uses the Titanium library for reusable GUI and inventory components.

**No standalone Factorio mod exists for Minecraft.** The closest is **Manufactio**, a 1.12.2 modpack combining IE belts, Mekanism machines, and CraftTweaker scripts. This gap is exactly what your mod could fill.

---

## Getting started without getting stuck

### Setup checklist

Install **JDK 8** (Adoptium Temurin), **IntelliJ IDEA Community Edition**, and download the **Forge 1.16.5 MDK** (version 36.2.x recommended) from `files.minecraftforge.net`. Extract, open in IntelliJ as a Gradle project, set the Gradle JVM to JDK 8, run `gradlew genIntellijRuns`, and verify the example mod loads with the `runClient` configuration.

### The five pitfalls that catch every new modder

**Client/server separation** is the number-one source of crashes. Minecraft runs both a logical client and logical server in singleplayer — `level.isClientSide` returns `true` on the client, `false` on the server. All game logic (inventory changes, energy transfer, recipe processing) runs server-side only. All rendering runs client-side only. Never reference classes from `net.minecraft.client` in common code — this causes `ClassNotFoundException` on dedicated servers. Always test on a dedicated server early and often.

**Registration timing** trips up developers who try to access `RegistryObject.get()` before registration completes. Use `DeferredRegister` exclusively — it handles timing automatically. Register blocks before items, items before tile entity types, tile entity types before container types.

**Capability invalidation** is easy to forget. Always call `LazyOptional.invalidate()` in `TileEntity.remove()` (1.16.x method name). Always cache `LazyOptional` references from neighboring blocks with invalidation listeners. Never assume a capability exists — use `ifPresent()` or `orElse()`.

**Chunk lifecycle** means your tile entity will be unloaded and reloaded without warning. Serialize all state in `save(CompoundNBT)` and restore in `load(BlockState, CompoundNBT)`. Don't assume neighboring tile entities are loaded. Don't perform heavy computation in `tick()` — it runs 20 times per second for every loaded instance.

**Data generation** saves enormous time. Instead of hand-writing blockstate JSON, model JSON, recipe JSON, and loot table JSON, extend `BlockStateProvider`, `RecipeProvider`, and `BlockLootSubProvider` and run the `runData` Gradle task. McJty's tutorial episode 3 covers this thoroughly.

### Recommended learning path

Start with **McJty's 1.16 modding tutorials** (`wiki.mcjty.eu/modding/`) — they cover tile entities, GUIs, energy, networking, and data generation with full source code on GitHub. Supplement with **Kaupenjoe's step-by-step 1.16.5 guides** (`tutorialsbykaupenjoe.net`) for visual learners, and **TheGreyGhost's MinecraftByExample** (`github.com/TheGreyGhost/MinecraftByExample`) for isolated concept demonstrations. The **Forge Community Wiki** (`forge.gemwire.uk/wiki/`) has version-specific pages on capabilities, custom recipes, and the event system.

### Build order for the mod

Prototype in this sequence: a single machine block with inventory and GUI → energy generation and consumption → a basic belt using the controller-delegate pattern → item insertion/extraction between belts and machines → the custom recipe system → the research tree and gating → multi-block assemblers → polish and the optional camera mode. Each step builds on the previous one and produces a testable, playable increment.

---

## Conclusion

The technical foundation for a Factorio-in-Minecraft mod is solid and well-proven. **Forge 1.16.5 provides the right ecosystem**: its capability system, massive tech mod community, and extensive documentation make it the clear choice. The most important architectural decision is adopting Create's **controller-delegate pattern for belts** — storing items as data within a single controller tile entity per belt strip rather than using world entities. For machines and recipes, Forge's `IRecipe`/`IRecipeType`/`IRecipeSerializer` framework plus the capability system for side-aware I/O cover every need. The research tree requires a custom system (advancements are insufficient), but the GameStages pattern and JSON data-driven design make this manageable.

The top-down camera is the project's highest-risk feature. If it's central to your vision, study Dungeons Perspective's approach and budget significant time for frustum culling fixes. If it's nice-to-have, defer it until core gameplay works and consider a simpler 2D map overlay instead. The gap in the modding ecosystem — no standalone Factorio mod for Minecraft exists — means the market opportunity is real, but shipping playable core mechanics matters more than camera novelty.