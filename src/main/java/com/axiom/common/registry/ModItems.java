package com.axiom.common.registry;

import com.axiom.Axiom;
import com.axiom.common.item.AxiomItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Axiom.MOD_ID);

    // Every placeable block gets a matching BlockItem so it can exist in inventories and creative tabs.
    public static final RegistryObject<Item> BASIC_BELT_ITEM = ITEMS.register(
            "basic_belt",
            () -> new BlockItem(ModBlocks.BASIC_BELT.get(), new Item.Properties().tab(AxiomItemGroup.MAIN))
    );

    public static final RegistryObject<Item> BASIC_MACHINE_ITEM = ITEMS.register(
            "basic_machine",
            () -> new BlockItem(ModBlocks.BASIC_MACHINE.get(), new Item.Properties().tab(AxiomItemGroup.MAIN))
    );

    private ModItems() {
    }
}
