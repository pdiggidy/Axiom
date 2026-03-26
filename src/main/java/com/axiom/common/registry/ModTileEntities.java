package com.axiom.common.registry;

import com.axiom.Axiom;
import com.axiom.common.tile.BasicBeltTileEntity;
import com.axiom.common.tile.BasicMachineTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModTileEntities {

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Axiom.MOD_ID);

    // These types bind the reusable tile entity logic to their corresponding placed blocks.
    public static final RegistryObject<TileEntityType<BasicBeltTileEntity>> BASIC_BELT = TILE_ENTITIES.register(
            "basic_belt",
            () -> TileEntityType.Builder.of(BasicBeltTileEntity::new, ModBlocks.BASIC_BELT.get()).build(null)
    );

    public static final RegistryObject<TileEntityType<BasicMachineTileEntity>> BASIC_MACHINE = TILE_ENTITIES.register(
            "basic_machine",
            () -> TileEntityType.Builder.of(BasicMachineTileEntity::new, ModBlocks.BASIC_MACHINE.get()).build(null)
    );

    private ModTileEntities() {
    }
}
