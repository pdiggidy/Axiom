package com.axiom.common.tile.base;

/**
 * Controls how a machine face exposes its item handler capability.
 * All efficiency gains come from placed physical items, not from modes that
 * change throughput rates — modes only gate which faces accept or eject items.
 */
public enum SideMode {
    /** Accept items pushed into this machine from this face. */
    INPUT,
    /** Push items out of this machine through this face. */
    OUTPUT,
    /** Expose no capability on this face. */
    DISABLED,
    /** Allow any item handler interaction on this face (default). */
    ANY;

    public boolean allowsInput() {
        return this == INPUT || this == ANY;
    }

    public boolean allowsOutput() {
        return this == OUTPUT || this == ANY;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public static SideMode fromOrdinal(int ordinal) {
        SideMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ANY;
    }
}
