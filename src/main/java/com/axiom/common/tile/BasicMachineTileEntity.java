package com.axiom.common.tile;

import com.axiom.common.menu.BasicMachineContainer;
import com.axiom.common.tile.base.AbstractMachineTileEntity;
import com.axiom.common.registry.ModTileEntities;
import net.minecraft.entity.player.PlayerInventory;

public class BasicMachineTileEntity extends AbstractMachineTileEntity {

    public BasicMachineTileEntity() {
        super(ModTileEntities.BASIC_MACHINE.get(), 2);
        this.maxProgress = 80;
    }

    @Override
    protected boolean canProcess() {
        // The starter machine only checks for any input item; recipe logic comes later.
        return !this.inventory.getStackInSlot(0).isEmpty();
    }

    @Override
    public BasicMachineContainer createMenu(int windowId, PlayerInventory inventory) {
        return new BasicMachineContainer(windowId, inventory, this);
    }
}
