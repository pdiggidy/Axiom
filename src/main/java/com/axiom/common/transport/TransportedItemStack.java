package com.axiom.common.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class TransportedItemStack {

    // This is the lightweight transport record belts can tick and render without spawning entities.
    private final ItemStack stack;
    private float beltPosition;
    private float previousPosition;
    private float sideOffset;

    public TransportedItemStack(ItemStack stack) {
        this(stack, 0.0F, 0.0F, 0.0F);
    }

    public TransportedItemStack(ItemStack stack, float beltPosition, float previousPosition, float sideOffset) {
        this.stack = stack.copy();
        this.beltPosition = beltPosition;
        this.previousPosition = previousPosition;
        this.sideOffset = sideOffset;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public float getBeltPosition() {
        return this.beltPosition;
    }

    public float getPreviousPosition() {
        return this.previousPosition;
    }

    public float getSideOffset() {
        return this.sideOffset;
    }

    public void advance(float delta) {
        this.previousPosition = this.beltPosition;
        this.beltPosition += delta;
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("stack", this.stack.serializeNBT());
        tag.putFloat("belt_position", this.beltPosition);
        tag.putFloat("previous_position", this.previousPosition);
        tag.putFloat("side_offset", this.sideOffset);
        return tag;
    }

    public static TransportedItemStack fromNBT(CompoundNBT tag) {
        return new TransportedItemStack(
                ItemStack.of(tag.getCompound("stack")),
                tag.getFloat("belt_position"),
                tag.getFloat("previous_position"),
                tag.getFloat("side_offset")
        );
    }
}
