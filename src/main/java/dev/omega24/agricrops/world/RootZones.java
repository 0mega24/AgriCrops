package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.PocContent;
import dev.omega24.agricrops.core.field.*;
import dev.omega24.agricrops.persistence.FieldSoils;
import java.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Exactly three vertical positions, never an aggregate resource inventory. */
public final class RootZones {
    private RootZones() {}
    public static RootZone read(World world, BlockPos plant) {
        List<SoilCell> cells = new ArrayList<>(); List<SoilProfile> profiles = new ArrayList<>();
        for (int depth = 1; depth <= 3; depth++) {
            BlockPos pos = plant.down(depth); if (!world.isBlockLoaded(pos)) break;
            String block = world.getBlockState(pos).getBlock().getRegistryName().toString();
            SoilProfile profile = PocContent.soils().get(block);
            if (profile == null) break;
            cells.add(FieldSoils.get(world).observe(pos, block)); profiles.add(profile);
        }
        if (cells.isEmpty()) {
            BlockPos pos = plant.down(); String block = world.getBlockState(pos).getBlock().getRegistryName().toString();
            return RootZone.single(FieldSoils.get(world).observe(pos, block));
        }
        return new RootZone(cells, profiles);
    }
    public static void write(World world, BlockPos plant, RootZone roots) {
        for (int i = 0; i < roots.cells.size(); i++) FieldSoils.get(world).put(plant.down(i + 1), roots.cells.get(i));
    }
    public static RootZone tick(World world, BlockPos plant, int surfaceIntake) {
        RootZone zone = read(world, plant); FieldSoils soils = FieldSoils.get(world);
        for (int i = 0; i < zone.cells.size(); i++) {
            BlockPos pos = plant.down(i + 1);
            if (!soils.beginInterval(pos, world.getTotalWorldTime() / 20)) continue;
            SoilProfile profile = zone.profiles.get(i);
            SoilCell cell = soils.observe(pos, zone.cells.get(i).block());
            int input = i == 0 ? surfaceIntake + (world.isRainingAt(plant.up()) ? 12 : 0) : 0;
            cell = profile.tick(cell, input, i == 0 && world.canSeeSky(plant.up()) ? 1 : 0.25);
            int drain = Math.min(profile.drainage, Math.max(0, cell.water() - (int) (profile.waterCapacity * profile.saturationStart)));
            if (i + 1 < zone.cells.size()) {
                BlockPos lowerPos = pos.down(); SoilProfile lowerProfile = zone.profiles.get(i + 1);
                SoilCell lower = soils.observe(lowerPos, zone.cells.get(i + 1).block());
                drain = Math.min(drain, Math.max(0, lowerProfile.waterCapacity - lower.water()));
                soils.put(lowerPos, lower.add(drain, 0));
            }
            soils.put(pos, cell.consume(drain, 0)); // Bottom outflow is a real, explicit loss.
        }
        return read(world, plant);
    }
}
