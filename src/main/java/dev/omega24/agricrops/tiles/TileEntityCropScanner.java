package dev.omega24.agricrops.tiles;

import com.agricraft.agricore.util.MathHelper;
import com.infinityraider.infinitylib.block.tile.TileEntityBase;
import com.infinityraider.infinitylib.utility.WorldHelper;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.farming.AgriDiscoveryLedger;
import dev.omega24.agricrops.reference.AgriNBT;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * The Crop Scanner hub (crop-mod-design.md §10): a single OC component covering a whole
 * field, rather than wiring an Adapter to every individual crop stick - "the single biggest
 * usability gap between 'technically has OC support' and 'actually nice to automate with.'"
 * Watches a configurable cuboid centered on this tile ({@link #setArea(int, int)}).
 */
@net.minecraftforge.fml.common.Optional.Interface(modid = "opencomputers", iface = "li.cil.oc.api.network.SimpleComponent")
@net.minecraftforge.fml.common.Optional.Interface(modid = "opencomputers", iface = "li.cil.oc.api.network.ManagedPeripheral")
public class TileEntityCropScanner extends TileEntityBase implements SimpleComponent, ManagedPeripheral {

    public static final int DEFAULT_RADIUS_XZ = 8;
    public static final int DEFAULT_RADIUS_Y = 4;
    public static final int MAX_RADIUS = 32;

    private int radiusXZ = DEFAULT_RADIUS_XZ;
    private int radiusY = DEFAULT_RADIUS_Y;

    public int getRadiusXZ() {
        return this.radiusXZ;
    }

    public int getRadiusY() {
        return this.radiusY;
    }

    public void setArea(int radiusXZ, int radiusY) {
        this.radiusXZ = MathHelper.inRange(radiusXZ, 1, MAX_RADIUS);
        this.radiusY = MathHelper.inRange(radiusY, 1, MAX_RADIUS);
        this.markForUpdate();
    }

    /**
     * Every planted crop stick in the scanned area, as parallel arrays (relative x/y/z offset
     * from this hub, species id, growth stage, stage count) - flat arrays rather than a nested
     * table per plant, so a script can zip them without depending on richer table-conversion
     * support.
     */
    public List<TileEntityCrop> scan() {
        final List<TileEntityCrop> found = new ArrayList<>();
        final BlockPos origin = this.getPos();
        for (BlockPos p : BlockPos.getAllInBoxMutable(
                origin.add(-radiusXZ, -radiusY, -radiusXZ),
                origin.add(radiusXZ, radiusY, radiusXZ))) {
            WorldHelper.getTile(this.getWorld(), p, TileEntityCrop.class)
                    .filter(TileEntityCrop::hasSeed)
                    .ifPresent(found::add);
        }
        return found;
    }

    @Override
    protected void writeTileNBT(NBTTagCompound tag) {
        tag.setInteger(AgriNBT.SCANNER_RADIUS_XZ, this.radiusXZ);
        tag.setInteger(AgriNBT.SCANNER_RADIUS_Y, this.radiusY);
    }

    @Override
    protected void readTileNBT(NBTTagCompound tag) {
        this.radiusXZ = tag.hasKey(AgriNBT.SCANNER_RADIUS_XZ) ? tag.getInteger(AgriNBT.SCANNER_RADIUS_XZ) : DEFAULT_RADIUS_XZ;
        this.radiusY = tag.hasKey(AgriNBT.SCANNER_RADIUS_Y) ? tag.getInteger(AgriNBT.SCANNER_RADIUS_Y) : DEFAULT_RADIUS_Y;
    }

    // =========================================================================
    // OpenComputers
    // =========================================================================
    @Override
    @Nonnull
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "crop_scanner";
    }

    @Override
    @Nonnull
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[]{"scanArea", "getPlant", "setArea", "getArea", "getKnownSpecies"};
    }

    @Override
    @Nullable
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public Object[] invoke(@Nullable String methodName, Context context, @Nullable Arguments args) throws Exception {
        if ("scanArea".equals(methodName)) {
            final List<TileEntityCrop> crops = this.scan();
            final BlockPos origin = this.getPos();
            final Object[] xs = new Object[crops.size()];
            final Object[] ys = new Object[crops.size()];
            final Object[] zs = new Object[crops.size()];
            final Object[] species = new Object[crops.size()];
            final Object[] stages = new Object[crops.size()];
            for (int i = 0; i < crops.size(); i++) {
                final TileEntityCrop crop = crops.get(i);
                final BlockPos rel = crop.getPos().subtract(origin);
                xs[i] = rel.getX();
                ys[i] = rel.getY();
                zs[i] = rel.getZ();
                species[i] = crop.getSeed().getPlant().getId();
                stages[i] = crop.getGrowthStage();
            }
            return new Object[]{xs, ys, zs, species, stages};
        } else if ("getPlant".equals(methodName)) {
            if (args == null || args.count() < 3) {
                throw new Exception("Expected (x, y, z) relative offsets!");
            }
            final BlockPos target = this.getPos().add(args.checkInteger(0), args.checkInteger(1), args.checkInteger(2));
            final Object[] result = WorldHelper.getTile(this.getWorld(), target, TileEntityCrop.class)
                    .filter(TileEntityCrop::hasSeed)
                    .<Object[]>map(crop -> {
                        final AgriSeed seed = crop.getSeed();
                        return new Object[]{
                            seed.getPlant().getId(),
                            crop.getGrowthStage(),
                            seed.getPlant().getGrowthStages(),
                            crop.isMature(),
                            crop.isCrossCrop()
                        };
                    })
                    .orElse(null);
            return result;
        } else if ("setArea".equals(methodName)) {
            if (args == null || args.count() < 2) {
                throw new Exception("Expected (radiusXZ, radiusY)!");
            }
            this.setArea(args.checkInteger(0), args.checkInteger(1));
            return new Object[]{this.radiusXZ, this.radiusY};
        } else if ("getArea".equals(methodName)) {
            return new Object[]{this.radiusXZ, this.radiusY};
        } else if ("getKnownSpecies".equals(methodName)) {
            final AgriDiscoveryLedger ledger = AgriDiscoveryLedger.get();
            return new Object[]{ledger == null ? new Object[0] : ledger.getKnownSpecies().toArray()};
        }
        return null;
    }

}
