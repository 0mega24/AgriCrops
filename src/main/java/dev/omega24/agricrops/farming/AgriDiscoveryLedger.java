package dev.omega24.agricrops.farming;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * A queryable, global (not per-player-item) discovery ledger (crop-mod-design.md §10):
 * "Keep [the Agricultural Journal] concept but back it with a queryable store... so a script
 * can call getKnownSpecies()... instead of only a player reading a GUI page." Species-only for
 * now - no mutation-discovery tracking exists anywhere in the codebase yet to feed a
 * getKnownMutations() from (§11 step 8's own scope note), so that's left for whenever mutation
 * discovery tracking is actually added, rather than faked here.
 * <p>
 * Attached to the overworld's {@link MapStorage} regardless of which dimension records the
 * discovery, since "known to any player, anywhere" is the intended semantics - not a per-
 * dimension ledger.
 */
public class AgriDiscoveryLedger extends WorldSavedData {

    private static final String NAME = "agricrops_discovery_ledger";
    private static final String TAG_SPECIES = "species";

    private final Set<String> species = ConcurrentHashMap.newKeySet();

    public AgriDiscoveryLedger() {
        super(NAME);
    }

    public AgriDiscoveryLedger(String name) {
        super(name);
    }

    /**
     * Fetches the ledger attached to the overworld, or {@code null} if no server is currently
     * running (e.g. called from purely client-side code).
     */
    public static AgriDiscoveryLedger get() {
        final net.minecraft.server.MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return null;
        }
        final World overworld = server.getWorld(0);
        final MapStorage storage = overworld.getMapStorage();
        AgriDiscoveryLedger instance = (AgriDiscoveryLedger) storage.getOrLoadData(AgriDiscoveryLedger.class, NAME);
        if (instance == null) {
            instance = new AgriDiscoveryLedger();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    /**
     * Records a species as globally discovered. Safe to call redundantly.
     */
    public void recordSpecies(@Nonnull String speciesId) {
        if (this.species.add(speciesId)) {
            this.markDirty();
        }
    }

    @Nonnull
    public Collection<String> getKnownSpecies() {
        return Collections.unmodifiableSet(this.species);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.species.clear();
        final NBTTagList list = nbt.getTagList(TAG_SPECIES, 8 /* NBT.TAG_STRING */);
        for (int i = 0; i < list.tagCount(); i++) {
            this.species.add(list.getStringTagAt(i));
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        final NBTTagList list = new NBTTagList();
        for (String id : this.species) {
            list.appendTag(new NBTTagString(id));
        }
        compound.setTag(TAG_SPECIES, list);
        return compound;
    }

}
