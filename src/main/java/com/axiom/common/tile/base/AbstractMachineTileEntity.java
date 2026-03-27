package com.axiom.common.tile.base;

import com.axiom.common.menu.base.AbstractMachineContainer;
import com.axiom.common.util.AxiomNbtKeys;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractMachineTileEntity extends AbstractAxiomTileEntity
        implements ITickableTileEntity, INamedContainerProvider, ISideConfig, IMachineStatus {

    protected final ItemStackHandler inventory;
    protected final LazyOptional<ItemStackHandler> inventoryCapability;
    protected int progress;
    protected int maxProgress;
    protected boolean active;

    // Per-face IO configuration: indexed by Direction.get3DDataValue() (0=D,1=U,2=N,3=S,4=W,5=E).
    private final SideMode[] sideConfig = new SideMode[6];

    protected AbstractMachineTileEntity(TileEntityType<?> type, int slots) {
        super(type);
        this.inventory = this.createInventory(slots);
        this.inventoryCapability = LazyOptional.of(() -> this.inventory);
        this.maxProgress = 100;
        for (int i = 0; i < this.sideConfig.length; i++) {
            this.sideConfig[i] = SideMode.ANY;
        }
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

    protected boolean canProcess() {
        return false;
    }

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

    // --- ISideConfig ---

    @Override
    public SideMode getSideMode(Direction side) {
        return this.sideConfig[side.get3DDataValue()];
    }

    @Override
    public void setSideMode(Direction side, SideMode mode) {
        this.sideConfig[side.get3DDataValue()] = mode;
        this.setChanged();
    }

    // --- IMachineStatus ---

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public String getStatusKey() {
        return this.active ? null : "status.no_input";
    }

    // --- Progress sync contract for containers ---

    public IIntArray getDataAccess() {
        return new IIntArray() {
            @Override
            public int get(int index) {
                return index == 0 ? progress : index == 1 ? maxProgress : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) progress = value;
                else if (index == 1) maxProgress = value;
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

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    // --- Capability ---

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null) {
                // Null side means internal access (e.g. from the machine itself or automation that ignores config).
                return this.inventoryCapability.cast();
            }
            SideMode mode = this.getSideMode(side);
            if (mode.isDisabled()) {
                return LazyOptional.empty();
            }
            if (mode == SideMode.ANY) {
                return this.inventoryCapability.cast();
            }
            // Provide a filtered view based on whether this face allows input or output.
            IItemHandler filtered = this.createSidedHandler(mode);
            return LazyOptional.of(() -> filtered).cast();
        }
        return super.getCapability(capability, side);
    }

    /**
     * Creates an item handler view filtered to the allowed operations for a given side mode.
     * Subclasses may override to assign specific slots to input/output faces.
     */
    protected IItemHandler createSidedHandler(SideMode mode) {
        // Default: expose all slots but respect insertion/extraction rules.
        int slots = this.inventory.getSlots();
        if (mode.allowsInput() && !mode.allowsOutput()) {
            // INPUT-only: wrap all slots, disable extraction.
            return new RangedWrapper(this.inventory, 0, slots) {
                @Nonnull
                @Override
                public net.minecraft.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return net.minecraft.item.ItemStack.EMPTY;
                }
            };
        }
        if (mode.allowsOutput() && !mode.allowsInput()) {
            // OUTPUT-only: wrap all slots, disable insertion.
            return new RangedWrapper(this.inventory, 0, slots) {
                @Nonnull
                @Override
                public net.minecraft.item.ItemStack insertItem(int slot, @Nonnull net.minecraft.item.ItemStack stack, boolean simulate) {
                    return stack;
                }
            };
        }
        return this.inventory;
    }

    @Override
    protected void onInvalidated() {
        this.invalidate(this.inventoryCapability);
    }

    // --- NBT ---

    @Override
    protected void saveInternal(CompoundNBT tag) {
        tag.put(AxiomNbtKeys.INVENTORY, this.inventory.serializeNBT());
        tag.putInt(AxiomNbtKeys.PROGRESS, this.progress);
        tag.putInt(AxiomNbtKeys.MAX_PROGRESS, this.maxProgress);
        tag.putBoolean(AxiomNbtKeys.ACTIVE, this.active);
        byte[] sides = new byte[this.sideConfig.length];
        for (int i = 0; i < this.sideConfig.length; i++) {
            sides[i] = (byte) this.sideConfig[i].ordinal();
        }
        tag.putByteArray(AxiomNbtKeys.SIDE_CONFIG, sides);
    }

    @Override
    protected void loadInternal(CompoundNBT tag) {
        this.inventory.deserializeNBT(tag.getCompound(AxiomNbtKeys.INVENTORY));
        this.progress = tag.getInt(AxiomNbtKeys.PROGRESS);
        this.maxProgress = tag.getInt(AxiomNbtKeys.MAX_PROGRESS);
        this.active = tag.getBoolean(AxiomNbtKeys.ACTIVE);
        if (tag.contains(AxiomNbtKeys.SIDE_CONFIG)) {
            byte[] sides = tag.getByteArray(AxiomNbtKeys.SIDE_CONFIG);
            for (int i = 0; i < this.sideConfig.length && i < sides.length; i++) {
                this.sideConfig[i] = SideMode.fromOrdinal(sides[i]);
            }
        }
    }
}
