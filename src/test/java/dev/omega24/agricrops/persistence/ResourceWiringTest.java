package dev.omega24.agricrops.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

/** Static resource wiring, not a substitute for client model baking/visual QA. */
public class ResourceWiringTest {
    private JsonObject json(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull("Missing resource " + path, stream);
            return new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    @Test public void allWheatStagesResolveToExistingNativeModelsAndTextures() throws Exception {
        JsonArray parts = json("/assets/agricrops/blockstates/crop_sticks.json").getAsJsonArray("multipart");
        assertEquals(9, parts.size());
        for (int age = 0; age < 8; age++) {
            JsonObject part = parts.get(age + 1).getAsJsonObject();
            assertEquals(Integer.toString(age), part.getAsJsonObject("when").get("age").getAsString());
            assertEquals("true", part.getAsJsonObject("when").get("planted").getAsString());
            assertEquals("minecraft:wheat_stage" + age, part.getAsJsonObject("apply").get("model").getAsString());
            JsonObject model = json("/assets/minecraft/models/block/wheat_stage" + age + ".json");
            String texture = model.getAsJsonObject("textures").get("crop").getAsString();
            try (InputStream stream = getClass().getResourceAsStream("/assets/minecraft/textures/" + texture + ".png")) {
                assertNotNull("Missing vanilla texture " + texture, stream);
            }
        }
    }
    @Test public void stickModelUsesExistingVanillaWoodAndItemArt() throws Exception {
        assertEquals("minecraft:blocks/planks_oak", json("/assets/agricrops/models/block/crop_sticks.json")
                .getAsJsonObject("textures").get("wood").getAsString());
        assertEquals("minecraft:items/stick", json("/assets/agricrops/models/item/crop_sticks.json")
                .getAsJsonObject("textures").get("layer0").getAsString());
        assertNotNull(getClass().getResource("/assets/minecraft/textures/blocks/planks_oak.png"));
        assertNotNull(getClass().getResource("/assets/minecraft/textures/items/stick.png"));
    }
}
