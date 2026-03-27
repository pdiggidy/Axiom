package com.axiom.common.tile.base;

import com.axiom.common.block.base.AbstractBeltBlock;
import com.axiom.common.transport.BeltInventory;
import com.axiom.common.transport.BeltThroughputTracker;
import com.axiom.common.transport.IBeltOutput;
import com.axiom.common.transport.TransportedItemStack;
import com.axiom.common.util.AxiomNbt;
import com.axiom.common.util.AxiomNbtKeys;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public abstract class AbstractBeltTileEntity extends AbstractAxiomTileEntity implements ITickableTileEntity {

    /** Items moved per tick window (20 ticks = 1 second). */
    private static final int THROUGHPUT_WINDOW = 20;

    private BeltInventory inventory = new BeltInventory();
    private final BeltThroughputTracker throughput = new BeltThroughputTracker(THROUGHPUT_WINDOW);

    /** Null when this tile entity IS the controller; set to controller pos for delegates. */
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
        IBeltOutput output = this.resolveNeighborOutput();
        int handed = this.inventory.tick(output, this.level.getGameTime());
        if (handed > 0) {
            this.throughput.recordHandoff(handed, this.level.getGameTime());
        }
        this.setChanged();
    }

    // --- Controller/delegate graph ---

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

    // --- Item intake ---

    /** Inserts an item at the start of this belt segment. Returns true if accepted. */
    public boolean addItem(TransportedItemStack stack) {
        if (this.isController() && this.inventory.canAccept()) {
            return this.inventory.add(stack);
        }
        return false;
    }

    /** True if this belt could accept a new item at the input end right now. */
    public boolean canReceiveItem() {
        return this.isController() && this.inventory.canAccept();
    }

    // --- Throughput ---

    /** Items handed off in the last completed throughput window. */
    public int getThroughputLastWindow() {
        return this.throughput.getLastWindowCount();
    }

    /** Window size in ticks used by the throughput tracker. */
    public int getThroughputWindowTicks() {
        return this.throughput.getWindowTicks();
    }

    // --- Accessors ---

    public BeltInventory getInventory() {
        return this.inventory;
    }

    // --- Neighbor resolution ---

    /**
     * Discovers the logical handoff target in this belt's facing direction.
     * Tries adjacent belt first, then machine item handler, then returns BLOCKED.
     * Called every controller tick — no caching because the world topology can change.
     */
    private IBeltOutput resolveNeighborOutput() {
        if (this.level == null) return IBeltOutput.BLOCKED;

        Direction facing = this.getBlockState().getValue(AbstractBeltBlock.FACING);
        BlockPos neighborPos = this.worldPosition.relative(facing);
        TileEntity neighbor = this.level.getBlockEntity(neighborPos);

        if (neighbor instanceof AbstractBeltTileEntity) {
            AbstractBeltTileEntity beltNeighbor = (AbstractBeltTileEntity) neighbor;
            return new IBeltOutput() {
                @Override
                public boolean canAccept(ItemStack stack) {
                    return beltNeighbor.canReceiveItem();
                }

                @Override
                public ItemStack insert(ItemStack stack, boolean simulate) {
                    if (!simulate) {
                        beltNeighbor.addItem(new TransportedItemStack(stack));
                        return ItemStack.EMPTY;
                    }
                    return beltNeighbor.canReceiveItem() ? ItemStack.EMPTY : stack;
                }
            };
        }

        if (neighbor != null) {
            // Try the adjacent block's item handler capability on the face we're pointing at.
            return neighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
                    .map(handler -> (IBeltOutput) new IBeltOutput() {
                        @Override
                        public boolean canAccept(ItemStack stack) {
                            return !ItemHandlerHelper.insertItem(handler, stack, true).equals(stack);
                        }

                        @Override
                        public ItemStack insert(ItemStack stack, boolean simulate) {
                            return ItemHandlerHelper.insertItem(handler, stack, simulate);
                        }
                    })
                    .orElse(IBeltOutput.BLOCKED);
        }

        return IBeltOutput.BLOCKED;
    }

    // --- NBT ---

    @Override
    protected void saveInternal(CompoundNBT tag) {
        AxiomNbt.putNullableBlockPos(tag, AxiomNbtKeys.CONTROLLER_POS, this.controllerPos);

        ListNBT items = new ListNBT();
        for (TransportedItemStack stack : this.inventory.getItems()) {
            items.add(stack.serializeNBT());
        }
        tag.put(AxiomNbtKeys.ITEMS, items);
        tag.putInt(AxiomNbtKeys.THROUGHPUT, this.throughput.getLastWindowCount());
    }

    @Override
    protected void loadInternal(CompoundNBT tag) {
        this.controllerPos = AxiomNbt.getNullableBlockPos(tag, AxiomNbtKeys.CONTROLLER_POS);
        this.inventory = new BeltInventory();

        ListNBT items = tag.getList(AxiomNbtKeys.ITEMS, 10);
        for (int i = 0; i < items.size(); i++) {
            this.inventory.add(TransportedItemStack.fromNBT(items.getCompound(i)));
        }
    }
}
