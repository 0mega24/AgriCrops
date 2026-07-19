/*
 */
package dev.omega24.agricrops.utility;

import com.agricrops.agricore.config.AgriConfigCategory;
import com.agricrops.agricore.config.AgriConfigurable;
import com.agricrops.agricore.core.AgriCore;
import com.agricrops.agricore.util.TypeHelper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Removes the annoying model errors. Credits go to TTerrag.
 */
@SideOnly(Side.CLIENT)
public class ModelErrorSuppressor {

    private static final List<String> IGNORES = TypeHelper.asList(
            "agricrops",
            "agricropsitem"
    );

    @AgriConfigurable(key = "Suppress Model Errors", category = AgriConfigCategory.DEBUG, comment = "Set to true to prevent any annoying AgriCraft model loading errors from spamming the log.")
    private static boolean supressErrors = true;

    @SubscribeEvent
    public void onModelBakePost(ModelBakeEvent event) {
        if (supressErrors) {
            Map<ResourceLocation, Exception> modelErrors = (Map<ResourceLocation, Exception>) ReflectionHelper.getPrivateValue(ModelLoader.class, event.getModelLoader(), "loadingExceptions");
            Set<ModelResourceLocation> missingVariants = (Set<ModelResourceLocation>) ReflectionHelper.getPrivateValue(ModelLoader.class, event.getModelLoader(), "missingVariants");

            List<ResourceLocation> errored = modelErrors.keySet().stream().filter((r) -> IGNORES.contains(r.getResourceDomain())).collect(Collectors.toList());
            List<ResourceLocation> missing = missingVariants.stream().filter((r) -> IGNORES.contains(r.getResourceDomain())).collect(Collectors.toList());

            errored.forEach(modelErrors::remove);
            missing.forEach(missingVariants::remove);

            AgriCore.getLogger("agricrops").info("Suppressed {0} Model Loading Errors!", errored.size());
            AgriCore.getLogger("agricrops").info("Suppressed {0} Missing Model Variants!", missing.size());
        }
    }

    static {
        AgriCore.getConfig().addConfigurable(ModelErrorSuppressor.class);
    }

}
