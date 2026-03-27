package com.axiom.common.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public final class AxiomNbt {

    private AxiomNbt() {
    }

    public static CompoundNBT putNullableBlockPos(CompoundNBT tag, String key, @Nullable BlockPos value) {
        if (value != null) {
            tag.putLong(key, value.asLong());
        }
        return tag;
    }

    @Nullable
    public static BlockPos getNullableBlockPos(CompoundNBT tag, String key) {
        return tag.contains(key, 4) ? BlockPos.of(tag.getLong(key)) : null;
    }
}
