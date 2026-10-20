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
    @Test public void intermediatePostHarvestProgressRoundTripsWithoutResettingGenes() {
        dev.omega24.agricrops.core.content.CropDefinition definition = new dev.omega24.agricrops.core.content.CropDefinition(
                "agricrops:wheat", "minecraft:wheat_seeds", "minecraft:wheat", "minecraft:farmland", "agricrops.crop.wheat",
                dev.omega24.agricrops.content.CropDefinitions.timeline(20, 40, 60, 80),
                StandardGenome.neutral(), 1, 1, 9, 15, 5);
        CropState paid = specimen().at(200).planned(dev.omega24.agricrops.core.field.ProductionPlan.resolve(definition, specimen(), java.util.Collections.singletonMap(definition.id(), definition)));
        paid = paid.advance(200, 0, paid.plan().water, paid.plan().nutrients);
        CropState harvested = dev.omega24.agricrops.core.field.CropActions.harvest(definition, paid).crop;
        CropState decoded = CropNbt.read(CropNbt.write(harvested));
        assertEquals(143, decoded.progress()); assertEquals(5, definition.stageAt(decoded.progress()));
        assertEquals(specimen().genome(), decoded.genome());
        assertEquals(SeedOrigin.BRED, decoded.origin());
    }
    @Test(expected = IllegalArgumentException.class)
    public void unknownSchemaCannotSilentlyBecomeAFreshSeed() {
        NBTTagCompound data = CropNbt.write(specimen()); data.setInteger("schema", 99); CropNbt.read(data);
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
    @Test public void fullHybridRoundTripPreservesPaidBudgetAllocationTraitsAndRooting() {
        java.util.Map<String, Integer> weights = new java.util.TreeMap<>(); weights.put("agricrops:wheat", 2); weights.put("agricrops:beetroot", 1);
        java.util.Map<String, Integer> credit = new java.util.TreeMap<>(); credit.put("agricrops:wheat", -1); credit.put("agricrops:beetroot", 1);
        dev.omega24.agricrops.core.genetics.TraitGenome traits = new dev.omega24.agricrops.core.genetics.TraitGenome(java.util.Collections.singletonMap("agricrops:climate", new dev.omega24.agricrops.core.genetics.TraitGenome.Pair("agricrops:frost_hardiness", "agricrops:frost_hardiness", 3, true)));
        dev.omega24.agricrops.core.genetics.Specimen specimen = new dev.omega24.agricrops.core.genetics.Specimen("agricrops:wheat", StandardGenome.neutral(), SeedOrigin.BRED,
                new dev.omega24.agricrops.core.genetics.ProductMix(weights), traits, 831, 12);
        dev.omega24.agricrops.core.field.ProductionPlan plan = new dev.omega24.agricrops.core.field.ProductionPlan(java.util.Collections.singletonMap("agricrops:wheat", 1), credit, 9000, 7000);
        CropState source = new CropState(specimen, 800, 100, 0, plan, 317, 222, credit);
        CropState restored = CropNbt.read(CropNbt.write(source));
        assertEquals(CropNbt.write(source), CropNbt.write(restored)); assertTrue(restored.specimen().traits.loci().get("agricrops:climate").fixed);
        CropState replanted = CropNbt.read(CropNbt.seed(source.specimen())); assertNull(replanted.plan()); assertEquals(0, replanted.progress()); assertEquals(0, replanted.paidWater()); assertEquals(831, replanted.specimen().lineage);
        CropState clipping = CropState.plant(specimen.clipping(), 400); assertEquals(400, CropNbt.read(CropNbt.write(clipping)).rooting());
    }
    @Test public void schemaOneMigratesWithoutInventingPaidProduction() {
        NBTTagCompound old = new NBTTagCompound(); old.setInteger("schema", 1); old.setString("crop", "agricrops:wheat"); old.setString("origin", "BRED"); old.setLong("progress", 4800);
        old.setTag("genome", CropNbt.write(specimen()).getCompoundTag("genome")); CropState migrated = CropNbt.read(old);
        assertEquals(4800, migrated.progress()); assertFalse(migrated.funded()); assertEquals(SeedOrigin.BRED, migrated.origin());
    }
    @Test(expected = IllegalArgumentException.class) public void overpaidBudgetIsRejected() {
        NBTTagCompound data = CropNbt.write(specimen()); data.setInteger("paidWater", 1); CropNbt.read(data);
    }
    @Test public void organicReserveSurvivesAndUnseenSoilDoesNotMintFertilizer() {
        FieldSoils source = new FieldSoils(); BlockPos pos = new BlockPos(7, 8, 9);
        assertEquals(0, source.observe(pos, "minecraft:farmland").nutrients()); source.put(pos, new SoilCell("minecraft:farmland", 123, 234, 345));
        FieldSoils target = new FieldSoils(); target.readFromNBT(source.writeToNBT(new NBTTagCompound())); assertEquals(345, target.observe(pos, "minecraft:farmland").organic());
    }
    @Test public void rootProcessingIsOncePerCellPerIntervalNotOncePerNeighbor() {
        FieldSoils data = new FieldSoils(); assertTrue(data.beginInterval(BlockPos.ORIGIN, 10)); assertFalse(data.beginInterval(BlockPos.ORIGIN, 10)); assertTrue(data.beginInterval(BlockPos.ORIGIN, 11));
    }
    @Test public void futureReservoirDataIsQuarantinedAndRetainedNotResetOrExposedToPipes() {
        dev.omega24.agricrops.world.ReservoirTile tile = new dev.omega24.agricrops.world.ReservoirTile();
        NBTTagCompound future = new NBTTagCompound(); future.setInteger("agriSchema", 99); future.setString("unknown", "retain me");
        tile.readFromNBT(future); assertTrue(tile.quarantined()); assertEquals(0, tile.addNutrients(100, false));
        assertNull(tile.getCapability(net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
        // Unregistered unit-test tiles cannot call super.writeToNBT, but retained payload is qualified in-world.
    }
}
