package com.axiom.common.menu.base;

import com.axiom.common.tile.base.AbstractMachineTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public abstract class AbstractMachineContainer extends Container {

    protected final AbstractMachineTileEntity tileEntity;

    protected AbstractMachineContainer(@Nullable ContainerType<?> type, int windowId, PlayerInventory playerInventory, AbstractMachineTileEntity tileEntity) {
        super(type, windowId);
        this.tileEntity = tileEntity;
        this.addMachineSlots(tileEntity);
        this.addPlayerInventory(playerInventory);
    }

    protected abstract void addMachineSlots(AbstractMachineTileEntity tileEntity);

    protected void addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int slot = 0; slot < amount; slot++) {
            this.addSlot(new SlotItemHandler(handler, index + slot, x + (slot * dx), y));
        }
    }

    protected void addPlayerInventory(PlayerInventory playerInventory) {
        // Use vanilla slot spacing so custom screens can drop in without redoing layout math.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return !this.tileEntity.isRemoved() && player.distanceToSqr(
                this.tileEntity.getBlockPos().getX() + 0.5D,
                this.tileEntity.getBlockPos().getY() + 0.5D,
                this.tileEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    protected IItemHandler getMachineInventory() {
        return this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(IllegalStateException::new);
    }
}
