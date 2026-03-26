package com.axiom.common.transport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeltInventory {

    private static final float DEFAULT_SPEED = 0.125F;
    private static final float BELT_LENGTH = 1.0F;

    private final List<TransportedItemStack> items = new ArrayList<>();
    private float speed = DEFAULT_SPEED;

    public void tick() {
        // Remove stacks once they leave this segment; later this can hand them to the next belt or machine.
        Iterator<TransportedItemStack> iterator = this.items.iterator();
        while (iterator.hasNext()) {
            TransportedItemStack stack = iterator.next();
            stack.advance(this.speed);
            if (stack.getBeltPosition() >= BELT_LENGTH) {
                iterator.remove();
            }
        }
    }

    public void add(TransportedItemStack stack) {
        this.items.add(stack);
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
