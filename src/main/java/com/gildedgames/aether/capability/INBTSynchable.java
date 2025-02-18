package com.gildedgames.aether.capability;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public interface INBTSynchable<T extends Tag> extends INBTSerializable<T> {
    T serializeSynchableNBT();
    void deserializeSynchableNBT(T nbt);
}
