package com.axiom.common.tile;

import com.axiom.common.menu.BasicMachineContainer;
import com.axiom.common.recipe.AxiomRecipeTypes;
import com.axiom.common.recipe.MachineRecipe;
import com.axiom.common.registry.ModTileEntities;
import com.axiom.common.tile.base.AbstractMachineTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Optional;

public class BasicMachineTileEntity extends AbstractMachineTileEntity {

    public BasicMachineTileEntity() {
        super(ModTileEntities.BASIC_MACHINE.get(), 2);
        this.maxProgress = 80;
    }

    @Override
    protected boolean canProcess() {
        return this.findActiveRecipe().isPresent();
    }

    @Override
    protected void finishProcess() {
        if (this.level == null) return;
        this.findActiveRecipe().ifPresent(recipe -> {
            ItemStack output = recipe.getResultItem().copy();
            ItemStack currentOutput = this.inventory.getStackInSlot(1);
            if (ItemHandlerHelper.canItemStacksStack(currentOutput, output)) {
                this.inventory.setStackInSlot(1, ItemHandlerHelper.copyStackWithSize(
                        currentOutput, currentOutput.getCount() + output.getCount()));
            } else if (currentOutput.isEmpty()) {
                this.inventory.setStackInSlot(1, output);
            } else {
                return; // output slot blocked — don't consume input
            }
            // Consume one item from the input slot.
            ItemStack input = this.inventory.getStackInSlot(0);
            input.shrink(1);
            this.inventory.setStackInSlot(0, input);
        });
    }

    @Override
    public String getStatusKey() {
        if (this.active) return null;
        if (this.inventory.getStackInSlot(0).isEmpty()) return "status.no_input";
        ItemStack currentOutput = this.inventory.getStackInSlot(1);
        if (!currentOutput.isEmpty() && currentOutput.getCount() >= currentOutput.getMaxStackSize()) {
            return "status.output_full";
        }
        return "status.no_recipe";
    }

    private Optional<MachineRecipe> findActiveRecipe() {
        if (this.level == null) return Optional.empty();
        return this.level.getRecipeManager()
                .getRecipesFor(AxiomRecipeTypes.MACHINE_PROCESSING, new net.minecraft.inventory.Inventory(this.inventory.getStackInSlot(0)), this.level)
                .stream()
                .findFirst();
    }

    @Override
    public BasicMachineContainer createMenu(int windowId, PlayerInventory inventory) {
        return new BasicMachineContainer(windowId, inventory, this);
    }
}
