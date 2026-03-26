package com.axiom.common.tile.base;

import com.axiom.common.menu.base.AbstractMachineContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractMachineTileEntity extends AbstractAxiomTileEntity implements ITickableTileEntity, INamedContainerProvider {

    protected final ItemStackHandler inventory;
    protected final LazyOptional<ItemStackHandler> inventoryCapability;
    protected int progress;
    protected int maxProgress;
    protected boolean active;

    protected AbstractMachineTileEntity(TileEntityType<?> type, int slots) {
        super(type);
        this.inventory = this.createInventory(slots);
        this.inventoryCapability = LazyOptional.of(() -> this.inventory);
        this.maxProgress = 100;
    }

    protected ItemStackHandler createInventory(int slots) {
        return new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        // Machines own their full processing loop so subclasses only need recipe-specific rules.
        boolean wasActive = this.active;
        this.active = this.canProcess();
        if (this.active) {
            this.progress = Math.min(this.progress + 1, this.maxProgress);
            if (this.progress >= this.maxProgress) {
                this.progress = 0;
                this.finishProcess();
            }
        } else if (this.progress != 0) {
            this.progress = 0;
        }

        if (wasActive != this.active) {
            BlockState state = this.getBlockState();
            this.level.setBlock(this.worldPosition, state.setValue(com.axiom.common.block.base.AbstractMachineBlock.ACTIVE, this.active), 3);
        }

        this.setChanged();
    }

    // Subclasses decide whether inputs, power, and recipe state allow progress this tick.
    protected boolean canProcess() {
        return false;
    }

    // Hook for recipe completion, output creation, and energy consumption.
    protected void finishProcess() {
    }

    public void dropContents() {
        if (this.level != null) {
            for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
                InventoryHelper.dropItemStack(
                        this.level,
                        this.worldPosition.getX(),
                        this.worldPosition.getY(),
                        this.worldPosition.getZ(),
                        this.inventory.getStackInSlot(slot)
                );
                this.inventory.setStackInSlot(slot, net.minecraft.item.ItemStack.EMPTY);
            }
        }
    }

    protected ITextComponent getMachineName() {
        return new TranslationTextComponent(this.getBlockState().getBlock().getDescriptionId());
    }

    public abstract AbstractMachineContainer createMenu(int windowId, PlayerInventory inventory);

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return this.createMenu(windowId, inventory);
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.getMachineName();
    }

    public IIntArray getDataAccess() {
        // Keep progress sync compact so screens can bind to a stable two-int contract.
        return new IIntArray() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    return progress;
                }
                if (index == 1) {
                    return maxProgress;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    progress = value;
                } else if (index == 1) {
                    maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            // Side-aware IO can be layered later; for now every face exposes the machine inventory.
            return this.inventoryCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void onInvalidated() {
        this.invalidate(this.inventoryCapability);
    }

    @Override
    protected void saveInternal(CompoundNBT tag) {
        tag.put("inventory", this.inventory.serializeNBT());
        tag.putInt("progress", this.progress);
        tag.putInt("max_progress", this.maxProgress);
        tag.putBoolean("active", this.active);
    }

    @Override
    protected void loadInternal(CompoundNBT tag) {
        this.inventory.deserializeNBT(tag.getCompound("inventory"));
        this.progress = tag.getInt("progress");
        this.maxProgress = tag.getInt("max_progress");
        this.active = tag.getBoolean("active");
    }
}
