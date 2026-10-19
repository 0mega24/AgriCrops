package dev.omega24.agricrops.world;

import dev.omega24.agricrops.Agricrops;
import dev.omega24.agricrops.persistence.FieldSoils;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Agricrops.MOD_ID)
public final class SoilChanges {
    private static final Map<WorldServer, Set<BlockPos>> PENDING = new WeakHashMap<>();
    private SoilChanges() {}
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void broken(BlockEvent.BreakEvent event) {
        reconcileAfterAction(event);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void placed(BlockEvent.PlaceEvent event) {
        reconcileAfterAction(event);
    }
    private static void reconcileAfterAction(BlockEvent event) {
        if (!(event.getWorld() instanceof WorldServer)) return;
        WorldServer world = (WorldServer) event.getWorld();
        PENDING.computeIfAbsent(world, ignored -> new HashSet<>()).add(event.getPos().toImmutable());
    }
    @SubscribeEvent public static void afterWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) event.world;
        Set<BlockPos> positions = PENDING.remove(world);
        if (positions == null) return;
        // addScheduledTask runs immediately on the server thread, so it cannot defer an event.
        // End-of-world-tick observation sees the final block after cancellation/placement.
        for (BlockPos pos : positions) {
            if (world.isBlockLoaded(pos)) FieldSoils.get(world).reconcileTracked(pos,
                    world.getBlockState(pos).getBlock().getRegistryName().toString());
        }
    }
}
