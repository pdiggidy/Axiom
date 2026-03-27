package com.axiom.common.util;

import com.axiom.Axiom;
import net.minecraft.util.ResourceLocation;

public final class AxiomIds {

    private AxiomIds() {
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Axiom.MOD_ID, path);
    }
}
