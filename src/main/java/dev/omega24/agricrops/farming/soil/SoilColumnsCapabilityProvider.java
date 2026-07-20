/*
 */
package dev.omega24.agricrops.farming.soil;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a fresh {@link ChunkSoilColumns} instance so it can be attached to a chunk via
 * {@link net.minecraftforge.event.AttachCapabilitiesEvent} and serialized with chunk NBT.
 */
public class SoilColumnsCapabilityProvider implements ICapabilitySerializable<NBTBase> {

    private final Capability<IChunkSoilColumns> capability;
    private final IChunkSoilColumns instance;

    public SoilColumnsCapabilityProvider(@Nonnull Capability<IChunkSoilColumns> capability) {
        this.capability = capability;
        this.instance = capability.getDefaultInstance();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == this.capability;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == this.capability ? (T) this.instance : null;
    }

    @Nonnull
    @Override
    public NBTBase serializeNBT() {
        return this.capability.getStorage().writeNBT(this.capability, this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        this.capability.getStorage().readNBT(this.capability, this.instance, null, nbt);
    }

}
