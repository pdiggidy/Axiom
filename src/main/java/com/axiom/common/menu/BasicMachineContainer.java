package com.axiom.common.menu;

import com.axiom.common.menu.base.AbstractMachineContainer;
import com.axiom.common.registry.ModContainers;
import com.axiom.common.tile.BasicMachineTileEntity;
import com.axiom.common.tile.base.AbstractMachineTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class BasicMachineContainer extends AbstractMachineContainer {

    public BasicMachineContainer(int windowId, PlayerInventory playerInventory, BasicMachineTileEntity tileEntity) {
        super(ModContainers.BASIC_MACHINE.get(), windowId, playerInventory, tileEntity);
    }

    public BasicMachineContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    @Override
    protected void addMachineSlots(AbstractMachineTileEntity tileEntity) {
        // Reserve one input and one output lane for the first processing machine shape.
        IItemHandler handler = this.getMachineInventory();
        this.addSlotRange(handler, 0, 44, 35, 2, 68);
    }

    private static BasicMachineTileEntity getTileEntity(PlayerInventory playerInventory, PacketBuffer data) {
        World world = playerInventory.player.level;
        BlockPos pos = data.readBlockPos();
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (!(tileEntity instanceof BasicMachineTileEntity)) {
            throw new IllegalStateException("Expected basic machine tile entity at " + pos);
        }
        return (BasicMachineTileEntity) tileEntity;
    }
}
