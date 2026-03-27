package com.axiom.common.recipe;

import com.axiom.Axiom;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

public final class ModRecipes {

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Axiom.MOD_ID);

    public static final RegistryObject<MachineRecipeSerializer> MACHINE_PROCESSING =
            RECIPE_SERIALIZERS.register("machine_processing", MachineRecipeSerializer::new);

    public static void register() {
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private ModRecipes() {}
}
