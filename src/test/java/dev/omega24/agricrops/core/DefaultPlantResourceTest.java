package dev.omega24.agricrops.core;

import java.net.URL;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DefaultPlantResourceTest {

    @Test
    public void everyDefaultPlantAndSeedTextureExists() {
        for (String id : new String[]{"cactus", "carrot", "potato", "sugarcane", "melon", "pumpkin"}) {
            for (int stage = 1; stage <= 4; stage++) {
                assertResource("assets/agricrops/textures/blocks/crop_" + id + stage + ".png");
            }
        }
        for (String id : new String[]{"cactus", "carrot", "potato", "sugarcane"}) {
            assertResource("assets/agricrops/textures/items/seed_" + id + ".png");
        }
        for (int stage = 0; stage < 8; stage++) {
            assertResource("assets/minecraft/textures/blocks/wheat_stage_" + stage + ".png");
        }
        for (int stage = 0; stage < 4; stage++) {
            assertResource("assets/minecraft/textures/blocks/beetroots_stage_" + stage + ".png");
        }
        for (int stage = 0; stage < 3; stage++) {
            assertResource("assets/minecraft/textures/blocks/nether_wart_stage_" + stage + ".png");
        }
        for (String name : new String[]{"seeds_wheat", "beetroot_seeds", "nether_wart", "seeds_melon", "seeds_pumpkin"}) {
            assertResource("assets/minecraft/textures/items/" + name + ".png");
        }
    }

    private static void assertResource(String path) {
        URL resource = DefaultPlantResourceTest.class.getClassLoader().getResource(path);
        assertNotNull("Missing default plant resource: " + path, resource);
    }
}
