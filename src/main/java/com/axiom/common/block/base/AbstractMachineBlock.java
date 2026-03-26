package com.axiom.common.block.base;

import com.axiom.common.tile.base.AbstractMachineTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class AbstractMachineBlock extends AbstractAxiomBlock {

    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected AbstractMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, Boolean.FALSE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (level.isClientSide) {
            return ActionResultType.SUCCESS;
        }

        // GUI opening is server-only; the client just reports a successful interaction.
        TileEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof AbstractMachineTileEntity && player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, (AbstractMachineTileEntity) tileEntity, pos);
            return ActionResultType.CONSUME;
        }

        return ActionResultType.PASS;
    }

    @Override
    public void onRemove(BlockState state, World level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileEntity = level.getBlockEntity(pos);
            if (tileEntity instanceof AbstractMachineTileEntity) {
                // Drop contents before the old tile entity is discarded.
                ((AbstractMachineTileEntity) tileEntity).dropContents();
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean triggerEvent(BlockState state, World level, BlockPos pos, int id, int type) {
        super.triggerEvent(state, level, pos, id, type);
        TileEntity tileEntity = level.getBlockEntity(pos);
        return tileEntity != null && tileEntity.triggerEvent(id, type);
    }

    @Override
    public abstract TileEntity createTileEntity(BlockState state, IBlockReader level);
}
