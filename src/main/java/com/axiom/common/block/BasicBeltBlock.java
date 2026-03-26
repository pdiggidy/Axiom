package com.axiom.common.block;

import com.axiom.common.block.base.AbstractBeltBlock;
import com.axiom.common.tile.BasicBeltTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BasicBeltBlock extends AbstractBeltBlock {

    public BasicBeltBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader level) {
        return new BasicBeltTileEntity();
    }
}
