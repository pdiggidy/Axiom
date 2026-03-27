package com.axiom.common.tile.base;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

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

    // --- Vanilla tile entity sync (server → all watching clients) ---

    /**
     * Called by Minecraft when a block update packet needs to be sent to clients.
     * Subclasses that change client-visible state should call
     * {@code level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3)}
     * to trigger this path.
     */
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    /**
     * Produces the NBT payload included in the block update packet.
     * Defaults to the full save data; subclasses may narrow this to only client-relevant fields.
     */
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        this.saveInternal(tag);
        return tag;
    }

    /**
     * Applied on the client when the block update packet arrives.
     */
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.loadInternal(packet.getTag());
    }

    protected void saveInternal(CompoundNBT tag) {
    }

    protected void loadInternal(CompoundNBT tag) {
    }
}
