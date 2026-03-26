package com.axiom.common.registry;

import com.axiom.Axiom;
import com.axiom.common.block.BasicBeltBlock;
import com.axiom.common.block.BasicMachineBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModBlocks {

    // DeferredRegister keeps Forge registration timing correct and avoids static-init ordering bugs.
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Axiom.MOD_ID);

    public static final RegistryObject<Block> BASIC_BELT = BLOCKS.register(
            "basic_belt",
            () -> new BasicBeltBlock(AbstractBlock.Properties
                    .of(Material.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    public static final RegistryObject<Block> BASIC_MACHINE = BLOCKS.register(
            "basic_machine",
            () -> new BasicMachineBlock(AbstractBlock.Properties
                    .of(Material.METAL)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.METAL))
    );

    private ModBlocks() {
    }
}
