package com.axiom.common.recipe;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * A machine-processing recipe: one ingredient in, one result out, with a configurable
 * processing time in ticks. Queries use slot 0 of the supplied inventory as the input.
 *
 * <p>JSON format (type {@code "axiom:machine_processing"}):
 * <pre>{@code
 * {
 *   "type": "axiom:machine_processing",
 *   "input": { "item": "minecraft:iron_ore" },
 *   "output": { "item": "minecraft:iron_ingot", "count": 1 },
 *   "processing_time": 80
 * }
 * }</pre>
 */
public class MachineRecipe implements IRecipe<IInventory> {

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int processingTime;

    public MachineRecipe(ResourceLocation id, Ingredient input, ItemStack output, int processingTime) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(IInventory inventory, World world) {
        return this.input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(IInventory inventory) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return this.output;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.MACHINE_PROCESSING.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return AxiomRecipeTypes.MACHINE_PROCESSING;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }
}
