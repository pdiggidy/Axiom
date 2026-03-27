package com.axiom.common.recipe;

import net.minecraft.item.crafting.IRecipeType;

/**
 * Axiom's custom recipe type constants.
 * Each type is registered with Minecraft's recipe manager so recipes can be
 * queried via {@code world.getRecipeManager().getRecipesFor(TYPE, inventory, world)}.
 */
public final class AxiomRecipeTypes {

    /** Processing recipe used by all Axiom machine tile entities. */
    public static final IRecipeType<MachineRecipe> MACHINE_PROCESSING =
            IRecipeType.register("axiom:machine_processing");

    private AxiomRecipeTypes() {}
}
