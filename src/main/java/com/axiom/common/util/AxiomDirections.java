package com.axiom.common.util;

import net.minecraft.util.Direction;

public final class AxiomDirections {

    private AxiomDirections() {
    }

    public static boolean isHorizontal(Direction direction) {
        return direction != null && direction.getAxis().isHorizontal();
    }

    public static boolean isVertical(Direction direction) {
        return direction != null && direction.getAxis().isVertical();
    }
}
