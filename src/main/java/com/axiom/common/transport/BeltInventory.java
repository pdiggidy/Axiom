package com.axiom.common.transport;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the ordered list of items moving along one belt controller's segment.
 *
 * <p>Items are sorted by position (highest = closest to output end).
 * When the leading item reaches the end it attempts to hand off via the supplied
 * {@link IBeltOutput}. If handoff fails the item is clamped at the end and blocks
 * items behind it (no item advances past the item immediately in front of it).</p>
 *
 * <p>Belts are strictly horizontal; this class never makes vertical decisions.</p>
 */
public class BeltInventory {

    public static final float BELT_LENGTH = 1.0F;
    /** Minimum gap maintained between adjacent items to prevent overlap. */
    public static final float MIN_ITEM_SPACING = 0.25F;

    private static final float DEFAULT_SPEED = 0.125F;

    private final List<TransportedItemStack> items = new ArrayList<>();
    private float speed = DEFAULT_SPEED;

    /**
     * Ticks all items using {@link IBeltOutput#BLOCKED} (no handoff).
     * Used when the owning tile entity has not yet loaded its neighbor state.
     */
    public void tick() {
        tick(IBeltOutput.BLOCKED, 0L);
    }

    /**
     * Advances all items and attempts handoff for the leading item.
     *
     * @param output   the target for items that exit this segment
     * @param gameTick current world game time, used for throughput tracking in the tile entity
     * @return number of items successfully handed off this tick
     */
    public int tick(IBeltOutput output, long gameTick) {
        if (this.items.isEmpty()) {
            return 0;
        }

        // Sort descending by position so index 0 is the item closest to the output.
        this.items.sort(Comparator.comparingDouble(TransportedItemStack::getBeltPosition).reversed());

        int handedOff = 0;

        for (int i = 0; i < this.items.size(); i++) {
            TransportedItemStack current = this.items.get(i);

            // Calculate how far this item can advance this tick.
            float maxAdvance = this.speed;

            // Never advance past the item ahead of it.
            if (i > 0) {
                TransportedItemStack ahead = this.items.get(i - 1);
                float gap = ahead.getBeltPosition() - current.getBeltPosition();
                float allowedAdvance = gap - MIN_ITEM_SPACING;
                if (allowedAdvance <= 0) {
                    // Blocked by item ahead — don't advance at all.
                    current.advance(0);
                    continue;
                }
                maxAdvance = Math.min(maxAdvance, allowedAdvance);
            }

            float targetPos = current.getBeltPosition() + maxAdvance;

            if (i == 0 && targetPos >= BELT_LENGTH) {
                // Leading item reached the end — attempt handoff.
                ItemStack stack = current.getStack();
                ItemStack remainder = output.insert(stack, false);
                if (remainder.isEmpty()) {
                    // Handoff succeeded: remove this item.
                    this.items.remove(i);
                    handedOff++;
                    // Adjust index since we removed the current element.
                    i--;
                } else {
                    // Blocked: clamp position at belt end, do not advance.
                    current.advance(0);
                }
            } else {
                current.advance(maxAdvance);
            }
        }

        return handedOff;
    }

    /**
     * Adds a new item to the belt at position 0 (the input end), if there is room.
     * Returns {@code true} if the item was accepted.
     */
    public boolean add(TransportedItemStack stack) {
        // Check that no existing item is blocking the input end.
        for (TransportedItemStack existing : this.items) {
            if (existing.getBeltPosition() < MIN_ITEM_SPACING) {
                return false;
            }
        }
        this.items.add(stack);
        return true;
    }

    /** Returns true if a new item could be inserted at the start of this belt. */
    public boolean canAccept() {
        for (TransportedItemStack existing : this.items) {
            if (existing.getBeltPosition() < MIN_ITEM_SPACING) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public List<TransportedItemStack> getItems() {
        return this.items;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
