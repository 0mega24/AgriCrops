package dev.omega24.agricrops.init;

import dev.omega24.agricrops.blocks.BlockCrop;
import dev.omega24.agricrops.blocks.BlockCropScanner;
import dev.omega24.agricrops.blocks.BlockSeedAnalyzer;
import dev.omega24.agricrops.blocks.BlockPeripheral;
import com.infinityraider.infinitylib.block.BlockBase;
import javax.annotation.Nonnull;

public class AgriBlocks {

    private static final AgriBlocks INSTANCE = new AgriBlocks();

    public static AgriBlocks getInstance() {
        return INSTANCE;
    }

    private AgriBlocks() {
        CROP = new BlockCrop();
        SEED_ANALYZER = new BlockSeedAnalyzer();
        PERIPHERAL = new BlockPeripheral();
        CROP_SCANNER = new BlockCropScanner();
    }

    // Crops
    @Nonnull
    public final BlockBase CROP;

    // Analyzers
    @Nonnull
    public final BlockBase SEED_ANALYZER;

    // Peripherals
    @Nonnull
    public final BlockBase PERIPHERAL;

    // Crop Scanner hub (crop-mod-design.md §10)
    @Nonnull
    public final BlockBase CROP_SCANNER;

}
