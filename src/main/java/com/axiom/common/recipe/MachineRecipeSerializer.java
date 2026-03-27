package com.axiom.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * Reads and writes {@link MachineRecipe} instances from JSON and packet buffers.
 * Invalid entries (missing fields, bad item IDs) throw loudly so packs are not silently broken.
 */
public class MachineRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<MachineRecipe> {

    @Override
    public MachineRecipe fromJson(ResourceLocation id, JsonObject json) {
        JsonElement inputElement = JSONUtils.isArrayNode(json, "input")
                ? JSONUtils.getAsJsonArray(json, "input")
                : JSONUtils.getAsJsonObject(json, "input");
        Ingredient input = Ingredient.fromJson(inputElement);

        JsonObject outputObj = JSONUtils.getAsJsonObject(json, "output");
        ItemStack output = ShapedRecipe.itemFromJson(outputObj);

        int processingTime = JSONUtils.getAsInt(json, "processing_time", 100);
        if (processingTime <= 0) {
            throw new IllegalArgumentException("processing_time must be positive, got " + processingTime + " in " + id);
        }

        return new MachineRecipe(id, input, output, processingTime);
    }

    @Nullable
    @Override
    public MachineRecipe fromNetwork(ResourceLocation id, PacketBuffer buf) {
        Ingredient input = Ingredient.fromNetwork(buf);
        ItemStack output = buf.readItem();
        int processingTime = buf.readVarInt();
        return new MachineRecipe(id, input, output, processingTime);
    }

    @Override
    public void toNetwork(PacketBuffer buf, MachineRecipe recipe) {
        recipe.getInput().toNetwork(buf);
        buf.writeItem(recipe.getResultItem());
        buf.writeVarInt(recipe.getProcessingTime());
    }
}
