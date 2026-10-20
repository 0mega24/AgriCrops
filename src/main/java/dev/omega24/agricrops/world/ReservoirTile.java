package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.PocContent;
import dev.omega24.agricrops.core.field.*;
import dev.omega24.agricrops.persistence.FieldSoils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

/** Small finite supply controller. Fluid input and soil delivery debit the very same tank. */
public final class ReservoirTile extends TileEntity implements ITickable {
    private final FluidTank tank = new FluidTank(8000) {
        @Override public boolean canFillFluidType(FluidStack fluid) { return fluid.getFluid() == FluidRegistry.WATER; }
        @Override protected void onContentsChanged() { markDirty(); }
    };
    private int nutrients, organic, cursor;
    private NBTTagCompound rejected;
    public boolean quarantined() { return rejected != null; }
    public int water() { return tank.getFluidAmount(); }
    public int nutrients() { return nutrients; }
    public int organic() { return organic; }
    public int addWater(int amount) { return quarantined() || amount <= 0 ? 0 : tank.fill(new FluidStack(FluidRegistry.WATER, amount), true); }
    public int addNutrients(int amount, boolean compost) {
        if (quarantined() || amount < 0) return 0;
        int accepted = Math.min(amount, 10000 - (compost ? organic : nutrients));
        if (compost) organic += accepted; else nutrients += accepted; if (accepted > 0) markDirty(); return accepted;
    }
    @Override public void update() {
        if (!world.isRemote && Math.floorMod(world.getTotalWorldTime(), 20) == Math.floorMod(pos.toLong(), 20)) advanceInterval();
    }
    public void advanceInterval() {
        if (world.isRemote || quarantined()) return;
        int released = Math.min(10, Math.min(organic, 10000 - nutrients)); organic -= released; nutrients += released;
        // At most 25 loaded candidates and four deliveries per interval, round-robin rather than nearest-first starvation.
        int delivered = 0;
        for (int checked = 0; checked < 25 && delivered < 4; checked++) {
            int index = cursor; cursor = (cursor + 1) % 25;
            BlockPos plant = pos.add(index % 5 - 2, 0, index / 5 - 2);
            if (!world.isBlockLoaded(plant) || !(world.getTileEntity(plant) instanceof CropTile)) continue;
            BlockPos soilPos = plant.down(); String block = world.getBlockState(soilPos).getBlock().getRegistryName().toString();
            SoilProfile profile = PocContent.soils().get(block); if (profile == null) continue;
            FieldSoils soils = FieldSoils.get(world); SoilCell cell = soils.observe(soilPos, block);
            int w = Math.min(120, Math.min(water(), Math.max(0, profile.waterCapacity - cell.water())));
            int n = Math.min(120, Math.min(nutrients, Math.max(0, profile.nutrientCapacity - cell.nutrients())));
            if (w == 0 && n == 0) continue;
            tank.drain(w, true); nutrients -= n; soils.put(soilPos, cell.add(w, n)); delivered++;
        }
        markDirty();
    }
    @Override public boolean hasCapability(Capability<?> capability, EnumFacing side) {
        return !quarantined() && (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side));
    }
    @Override public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (quarantined()) return null;
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank) : super.getCapability(capability, side);
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); if (rejected != null) { tag.merge(rejected); return tag; }
        tag.setInteger("agriSchema", 1); tag.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        tag.setInteger("nutrients", nutrients); tag.setInteger("organic", organic); tag.setInteger("cursor", cursor); return tag;
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        rejected = null; tank.setFluid(null); nutrients = 0; organic = 0; cursor = 0;
        try { decode(tag); } catch (RuntimeException exception) { rejected = tag.copy(); }
    }
    private void decode(NBTTagCompound tag) {
        if (!tag.hasKey("agriSchema", 3) || !tag.hasKey("nutrients", 3) || !tag.hasKey("organic", 3) || !tag.hasKey("cursor", 3)
                || tag.getInteger("agriSchema") != 1 || !tag.hasKey("tank", 10) || tag.getInteger("nutrients") < 0 || tag.getInteger("nutrients") > 10000
                || tag.getInteger("organic") < 0 || tag.getInteger("organic") > 10000 || tag.getInteger("cursor") < 0 || tag.getInteger("cursor") > 24) throw new IllegalArgumentException("Invalid Agricrops reservoir state");
        NBTTagCompound fluidTag = tag.getCompoundTag("tank");
        if (!fluidTag.hasKey("Empty", 8) && (!fluidTag.hasKey("FluidName", 8) || !fluidTag.hasKey("Amount", 3) || !fluidTag.getString("FluidName").equals("water"))) throw new IllegalArgumentException("Unknown/incomplete fluid identity");
        FluidTank candidate = new FluidTank(8000); candidate.readFromNBT(fluidTag);
        if (candidate.getFluidAmount() > 8000 || candidate.getFluidAmount() < 0 || (candidate.getFluid() != null && candidate.getFluid().getFluid() != FluidRegistry.WATER)) throw new IllegalArgumentException("Invalid reservoir fluid");
        tank.setFluid(candidate.getFluid()); nutrients = tag.getInteger("nutrients"); organic = tag.getInteger("organic"); cursor = tag.getInteger("cursor");
    }
}
