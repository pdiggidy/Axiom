package com.axiom.common.tile.base;

import net.minecraft.util.Direction;

/**
 * Implemented by machines that support per-face item handler configuration.
 * Later tasks can query this to present per-face UI or enforce routing rules.
 */
public interface ISideConfig {

    SideMode getSideMode(Direction side);

    void setSideMode(Direction side, SideMode mode);

    default void resetSideModes() {
        for (Direction side : Direction.values()) {
            setSideMode(side, SideMode.ANY);
        }
    }
}
