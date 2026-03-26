package com.axiom.common.item;

import com.axiom.common.registry.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class AxiomItemGroup {

    // Keep all early automation parts under one creative tab while the content set is still small.
    public static final ItemGroup MAIN = new ItemGroup("axiom") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.BASIC_MACHINE_ITEM.get());
        }
    };

    private AxiomItemGroup() {
    }
}
