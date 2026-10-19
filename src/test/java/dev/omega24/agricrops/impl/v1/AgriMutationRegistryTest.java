package dev.omega24.agricrops.impl.v1;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import java.lang.reflect.Proxy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AgriMutationRegistryTest {

    @Test
    public void resolvesChildByChildIdInsteadOfMutationId() {
        IAgriPlant child = plant("registry_test_child");
        IAgriPlant parentA = plant("registry_test_parent_a");
        IAgriPlant parentB = plant("registry_test_parent_b");
        AgriApi.getPlantRegistry().add(child);
        AgriApi.getPlantRegistry().add(parentA);
        AgriApi.getPlantRegistry().add(parentB);

        AgriMutationRegistry registry = new AgriMutationRegistry();
        assertTrue(registry.add("registry_test_mutation", 0.5, child.getId(),
                parentA.getId(), parentB.getId()));
        assertEquals(child, registry.get("registry_test_mutation").get().getChild());
    }

    private static IAgriPlant plant(String id) {
        return (IAgriPlant) Proxy.newProxyInstance(
                IAgriPlant.class.getClassLoader(), new Class<?>[]{IAgriPlant.class},
                (proxy, method, args) -> {
                    if ("getId".equals(method.getName())) {
                        return id;
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if ("toString".equals(method.getName())) {
                        return id;
                    }
                    Class<?> type = method.getReturnType();
                    if (type == boolean.class) return false;
                    if (type == int.class) return 0;
                    if (type == double.class) return 0.0;
                    return null;
                });
    }
}
