/*
 */
package dev.omega24.agricrops.handler;

import dev.omega24.agricrops.farming.soil.ChunkSoilColumns;
import dev.omega24.agricrops.farming.soil.ChunkSoilColumnsStorage;
import dev.omega24.agricrops.farming.soil.IChunkSoilColumns;
import dev.omega24.agricrops.farming.soil.SoilColumnsCapabilityProvider;
import dev.omega24.agricrops.reference.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Registers and attaches {@link IChunkSoilColumns}, per crop-mod-design.md §3. Forge
 * capabilities can't attach to a bare {@code Block} instance (it's a singleton shared by
 * every occurrence of that block in the world), so the per-column soil state is attached
 * to the {@link Chunk} instead, keyed internally by world position.
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class SoilColumnCapabilityHandler {

    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Reference.MOD_ID, "soil_columns");

    @CapabilityInject(IChunkSoilColumns.class)
    public static Capability<IChunkSoilColumns> SOIL_COLUMNS_CAPABILITY;

    private SoilColumnCapabilityHandler() {
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IChunkSoilColumns.class, new ChunkSoilColumnsStorage(), ChunkSoilColumns::new);
    }

    @SubscribeEvent
    public static void onAttachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(CAPABILITY_ID, new SoilColumnsCapabilityProvider(SOIL_COLUMNS_CAPABILITY));
    }

}
