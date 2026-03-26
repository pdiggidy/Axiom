package com.axiom.common.block;

import com.axiom.common.block.base.AbstractMachineBlock;
import com.axiom.common.tile.BasicMachineTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BasicMachineBlock extends AbstractMachineBlock {

    public BasicMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader level) {
        return new BasicMachineTileEntity();
    }
}
