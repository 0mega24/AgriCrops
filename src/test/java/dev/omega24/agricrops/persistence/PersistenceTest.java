package dev.omega24.agricrops.persistence;

import dev.omega24.agricrops.core.field.CropState;
import dev.omega24.agricrops.core.field.SoilCell;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.genetics.StandardStat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;
import static org.junit.Assert.*;

public class PersistenceTest {
    private CropState specimen() {
        return new CropState("agricrops:wheat", StandardGenome.neutral().with(StandardStat.LIGHT_TOLERANCE, -63), SeedOrigin.BRED, 42);
    }
    @Test public void specimenRoundTripPreservesIdentityOriginAdaptationAndProgress() {
        CropState state = specimen(); CropState decoded = CropNbt.read(CropNbt.write(state));
        assertEquals(state.crop(), decoded.crop()); assertEquals(state.genome(), decoded.genome());
        assertEquals(state.origin(), decoded.origin()); assertEquals(42, decoded.progress());
    }
    @Test(expected = IllegalArgumentException.class)
    public void unknownSchemaCannotSilentlyBecomeAFreshSeed() {
        NBTTagCompound data = CropNbt.write(specimen()); data.setInteger("schema", 2); CropNbt.read(data);
    }
    @Test(expected = IllegalArgumentException.class)
    public void missingGenesCannotBeSilentlyFilledWithZeros() {
        NBTTagCompound data = CropNbt.write(specimen()); data.getCompoundTag("genome").removeTag("growth"); CropNbt.read(data);
    }
    @Test public void soilSaveRetainsDepletionAndIsIndependentFromACropItem() {
        FieldSoils store = new FieldSoils(); BlockPos pos = new BlockPos(3, 64, 9);
        store.put(pos, new SoilCell("minecraft:farmland", 18, 27));
        assertTrue(store.isDirty());
        FieldSoils restored = new FieldSoils(); restored.readFromNBT(store.writeToNBT(new NBTTagCompound()));
        assertEquals(27, restored.observe(pos, "minecraft:farmland").nutrients());
        assertEquals(18, restored.observe(pos, "minecraft:farmland").water());
        restored.reconcileTracked(pos, "minecraft:dirt");
        assertEquals(0, restored.observe(pos, "minecraft:farmland").nutrients());
    }
    @Test public void rejectedSoilSaveCannotBeUsedAsFreshFertileGround() {
        FieldSoils store = new FieldSoils();
        NBTTagCompound future = new NBTTagCompound(); future.setInteger("schema", 2);
        future.setString("futureData", "retain me");
        try { store.readFromNBT(future); fail("Unknown schema accepted"); }
        catch (IllegalArgumentException expected) { /* MapStorage also catches this. */ }
        assertEquals(future, store.writeToNBT(new NBTTagCompound()));
        try { store.observe(BlockPos.ORIGIN, "minecraft:farmland"); fail("Rejected save became fresh soil"); }
        catch (IllegalStateException expected) { /* Quarantined data stays unavailable. */ }
    }
    @Test(expected = IllegalArgumentException.class)
    public void malformedSoilListCannotSilentlyBecomeEmpty() {
        NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("schema", 1);
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
        list.appendTag(new net.minecraft.nbt.NBTTagString("not a cell")); tag.setTag("cells", list);
        new FieldSoils().readFromNBT(tag);
    }
    @Test public void malformedTilePayloadIsPreservedRatherThanFreshened() {
        dev.omega24.agricrops.world.CropTile tile = new dev.omega24.agricrops.world.CropTile();
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("agricrops", "invalid but retained");
        tile.readFromNBT(tag);
        assertTrue(tile.quarantined()); assertNull(tile.crop());
        assertEquals(tag.getTag("agricrops"), tile.data());
    }
}
