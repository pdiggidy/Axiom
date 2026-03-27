package com.axiom.common.research;

import net.minecraft.util.ResourceLocation;

/**
 * A single thing that a research node unlocks when completed.
 *
 * <p>Design rule: only MACHINE, ITEM, CAPABILITY, and TIER types are valid.
 * STAT_MODIFIER is explicitly rejected by the loader — all efficiency gains
 * must come from physical upgrades installed in the world, not invisible number changes.</p>
 */
public final class UnlockEntry {

    public enum UnlockType {
        /** Allows placing/using a new machine block type. */
        MACHINE,
        /** Makes a new item accessible (crafting recipe revealed, item obtainable). */
        ITEM,
        /** Activates a game capability such as "use seismic surveyor" or "build anchor". */
        CAPABILITY,
        /** Unlocks a new physical tier of an existing item (e.g. Heavy Cable, Wide-Bore Shaft). */
        TIER,
        /**
         * Forbidden. Encoding a hidden percentage modifier here violates the design contract.
         * The loader throws if this type appears in any data file.
         */
        STAT_MODIFIER
    }

    private final UnlockType type;
    private final ResourceLocation target;

    public UnlockEntry(UnlockType type, ResourceLocation target) {
        this.type = type;
        this.target = target;
    }

    public UnlockType getType() {
        return this.type;
    }

    public ResourceLocation getTarget() {
        return this.target;
    }

    @Override
    public String toString() {
        return this.type + ":" + this.target;
    }
}
