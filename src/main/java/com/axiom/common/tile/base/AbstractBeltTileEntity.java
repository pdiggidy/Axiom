package com.axiom.common.tile.base;

import com.axiom.common.transport.BeltInventory;
import com.axiom.common.transport.TransportedItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public abstract class AbstractBeltTileEntity extends AbstractAxiomTileEntity implements ITickableTileEntity {

    // The controller holds the moving item data; delegate segments just point at it.
    private BeltInventory inventory = new BeltInventory();
    @Nullable
    private BlockPos controllerPos;

    protected AbstractBeltTileEntity(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide || !this.isController()) {
            return;
        }
        // Only the controller advances transport state, which keeps long belts cheap to tick.
        this.inventory.tick();
        this.setChanged();
    }

    public boolean isController() {
        return this.controllerPos == null || this.controllerPos.equals(this.worldPosition);
    }

    public void setControllerPos(@Nullable BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        this.setChanged();
    }

    @Nullable
    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public BeltInventory getInventory() {
        return this.inventory;
    }

    public void addItem(TransportedItemStack stack) {
        if (this.isController()) {
            this.inventory.add(stack);
            this.setChanged();
        }
    }

    @Override
    protected void saveInternal(CompoundNBT tag) {
        if (this.controllerPos != null) {
            tag.putLong("controller_pos", this.controllerPos.asLong());
        }

        ListNBT items = new ListNBT();
        for (TransportedItemStack stack : this.inventory.getItems()) {
            items.add(stack.serializeNBT());
        }
        tag.put("items", items);
    }

    @Override
    protected void loadInternal(CompoundNBT tag) {
        this.controllerPos = tag.contains("controller_pos") ? BlockPos.of(tag.getLong("controller_pos")) : null;
        this.inventory = new BeltInventory();

        ListNBT items = tag.getList("items", 10);
        for (int index = 0; index < items.size(); index++) {
            this.inventory.add(TransportedItemStack.fromNBT(items.getCompound(index)));
        }
    }
}
