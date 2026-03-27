package com.axiom.common.transport;

import net.minecraft.item.ItemStack;

/**
 * Handoff target for items that reach the end of a belt segment.
 * Implementations connect to adjacent belts, machine inventories, or vertical logistics blocks.
 *
 * <p>Belts are horizontal-only by contract. Vertical transitions must be handled by
 * Gravity Chutes, Bucket Elevators, or Pneumatic Tubes — never by belt logic.</p>
 */
public interface IBeltOutput {

    /**
     * A no-op output used when no valid neighbor exists; always blocks items.
     */
    IBeltOutput BLOCKED = new IBeltOutput() {
        @Override
        public boolean canAccept(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            return stack;
        }
    };

    /**
     * Returns {@code true} if this output could accept at least one unit of the given stack.
     * Cheap simulation; does not modify state.
     */
    boolean canAccept(ItemStack stack);

    /**
     * Attempts to insert {@code stack} into this output.
     *
     * @param stack    the stack to transfer (must not be empty)
     * @param simulate if true, no state changes are made
     * @return the remainder that could not be inserted; {@link ItemStack#EMPTY} on full success
     */
    ItemStack insert(ItemStack stack, boolean simulate);
}
