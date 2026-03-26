package com.axiom.common.tile.base;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;

public abstract class AbstractAxiomTileEntity extends TileEntity {

    protected AbstractAxiomTileEntity(TileEntityType<?> type) {
        super(type);
    }

    protected void invalidate(LazyOptional<?>... optionals) {
        for (LazyOptional<?> optional : optionals) {
            optional.invalidate();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.onInvalidated();
    }

    protected void onInvalidated() {
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        this.saveInternal(tag);
        return tag;
    }

    @Override
    public void load(net.minecraft.block.BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        this.loadInternal(tag);
    }

    protected void saveInternal(CompoundNBT tag) {
    }

    protected void loadInternal(CompoundNBT tag) {
    }
}
