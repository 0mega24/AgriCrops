/*
 */
package com.agricraft.agricore.util;

import com.agricraft.agricore.core.AgriCore;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author RlonRyan
 */
public class ResourceHelper {

    // Agricrops: this used to scan the jar for bundled default JSON resources via
    // org.reflections. The fork dropped the upstream AgriPlants defaults (and the
    // org.reflections runtime dependency along with them), so there is nothing to
    // discover here; the fork defines its species via the ZenScript-driven
    // registries (design §7/§11) instead. Returns empty.
    public static Set<String> findResources(Predicate<String> nameFilter) {
        return Collections.emptySet();
    }

    /**
     * Copies a file from inside the jar to the specified location outside the
     * jar, retaining the file name. The default copy action is to not overwrite
     * an existing file.
     *
     * @param from the location of the internal resource.
     * @param to the location to copy the resource to.
     * @param overwrite if the copy task should overwrite existing files.
     */
    public static void copyResource(String from, Path to, boolean overwrite) {
        try {
            if (overwrite || !Files.exists(to)) {
                Files.createDirectories(to.getParent());
                Files.copy(getResourceAsStream(from), to, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            AgriCore.getLogger("AgriCraft").error(
                    "Unable to copy Jar resource: \"{0}\" to: \"{1}\"!",
                    from,
                    to
            );
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the requested resource by using the current thread's class
     * loader or the AgriCore class loader.
     *
     * @param location the location of the desired resource stream.
     * @return the resource, as a stream.
     */
    public static InputStream getResourceAsStream(String location) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
        return in != null ? in : ResourceHelper.class.getResourceAsStream(location);
    }

}
