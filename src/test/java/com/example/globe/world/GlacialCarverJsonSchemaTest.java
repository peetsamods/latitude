package com.example.globe.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 5 B-9 P1 JSON schema tripwire: parses the two carver JSONs off the classpath (the same
 * {@code data/globe/worldgen/carver/} files the datapack loader reads) and asserts every REQUIRED
 * codec field key is present per the swept design's field list, plus the two grammar forms that
 * actually break parses (height-provider bounds are VerticalAnchor OBJECTS; float-provider bounds are
 * plain NUMBERS -- both verbatim-mirrored from the 26.3-rc-2 vanilla {@code cave.json}/
 * {@code canyon.json} extracted from the loom jar). This is deliberately a CHEAP tripwire: a
 * missing/renamed key fails here in the unit suite; full codec validation happens at boot (world
 * creation IS the datapack parse gate, design proof plan step 2).
 *
 * <p>26.3 rewrite (verified against the 26.3-rc-2 jar and javap on
 * {@code CanyonWorldCarver}/{@code CaveWorldCarver}): {@code data/minecraft/worldgen/configured_carver/}
 * no longer exists -- the registry directory is {@code worldgen/carver/}, the {@code "config"} wrapper
 * is gone (fields sit flat at the top level), and neither carver record carries {@code lava_level} or
 * {@code replaceable} any more (both dropped -- there is no lava-window guard or replaceable-block tag
 * at the carver level on 26.3; DROPPED, not renamed). The canyon's vertical-scale field moved from a
 * carver-level {@code "yScale"} into {@code shape.y_scale} (a FloatProvider, so a plain number is a
 * valid constant). {@code CaveWorldCarver} carries no y-scale field at all; the record instead exposes
 * {@code count}/{@code thickness}/{@code weird_thickness_bias}/{@code room_vertical_radius_multiplier}
 * (four fields with no 26.2 equivalent in this file -- {@code count}/{@code thickness}/
 * {@code weird_thickness_bias} are mirrored verbatim from vanilla {@code cave.json} since Latitude
 * never customized them; {@code room_vertical_radius_multiplier} reuses the OLD carver-level
 * {@code "yScale"} uniform(0.1, 0.9) value, which is numerically identical to vanilla's own default for
 * that field).
 */
class GlacialCarverJsonSchemaTest {

    private static final String CARVER_DIR = "/data/globe/worldgen/carver/";

    private static JsonObject load(String fileName) {
        InputStream stream = GlacialCarverJsonSchemaTest.class.getResourceAsStream(CARVER_DIR + fileName);
        assertNotNull(stream, "Carver JSON must be on the classpath (main resources): " + fileName);
        return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static JsonObject requireFlatCarver(JsonObject root, String fileName, String expectedType,
                                                 String... requiredKeys) {
        assertTrue(root.has("type"), fileName + ": must declare \"type\"");
        assertEquals(expectedType, root.get("type").getAsString(),
                fileName + ": carver type must be the vanilla type the design picked");
        assertFalse(root.has("config"), fileName + ": 26.3 carvers are flat -- no \"config\" wrapper");
        for (String key : requiredKeys) {
            assertTrue(root.has(key), fileName + ": required codec field missing: " + key);
        }
        assertFalse(root.has("lava_level"),
                fileName + ": lava_level no longer exists on the 26.3 carver record -- must be dropped");
        assertFalse(root.has("replaceable"),
                fileName + ": replaceable no longer exists on the 26.3 carver record -- must be dropped");
        return root;
    }

    /** Height-provider bounds are VerticalAnchor OBJECTS ({"absolute": N}) -- the grammar that breaks
     *  world creation if written as plain numbers. */
    private static void assertAbsoluteHeightProviderBound(JsonObject provider, String boundKey,
                                                          int expected, String context) {
        JsonElement bound = provider.get(boundKey);
        assertNotNull(bound, context + ": height provider must carry " + boundKey);
        assertTrue(bound.isJsonObject(),
                context + ": height-provider " + boundKey + " must be a VerticalAnchor OBJECT, not a number");
        assertEquals(expected, bound.getAsJsonObject().get("absolute").getAsInt(),
                context + ": " + boundKey + " absolute anchor");
    }

    @Test
    void crevasseCanyonCarrierHasEveryRequiredCodecField() {
        JsonObject config = requireFlatCarver(load("crevasse.json"), "crevasse.json", "minecraft:canyon",
                "probability", "y", "vertical_rotation", "shape");

        JsonObject shape = config.getAsJsonObject("shape");
        for (String key : new String[]{"distance_factor", "thickness", "width_smoothness",
                "horizontal_radius_factor", "vertical_radius_default_factor", "vertical_radius_center_factor",
                "y_scale"}) {
            assertTrue(shape.has(key), "crevasse.json: required shape codec field missing: " + key);
        }
        // y_scale moved OFF the carver root and INTO shape on 26.3 (CanyonWorldCarver$Shape.yScale).
        assertFalse(config.has("yScale"), "crevasse.json: yScale must not sit at the carver root any more");
        assertTrue(shape.get("y_scale").isJsonPrimitive(),
                "crevasse.json: shape.y_scale is a FloatProvider -- a plain number is a valid constant form");
        assertEquals(4.0, shape.get("y_scale").getAsDouble(), 1e-9, "crevasse.json: shape.y_scale value");

        // The design's Y band: absolute uniform 66..112, anchored to the real polar surface distribution.
        JsonObject y = config.getAsJsonObject("y");
        assertEquals("minecraft:uniform", y.get("type").getAsString(), "crevasse.json: y provider type");
        assertAbsoluteHeightProviderBound(y, "min_inclusive", 66, "crevasse.json y");
        assertAbsoluteHeightProviderBound(y, "max_inclusive", 112, "crevasse.json y");

        // Grammar tripwire: FLOAT-provider bounds are plain numbers (uniform float uses max_exclusive).
        JsonObject verticalRotation = config.getAsJsonObject("vertical_rotation");
        assertTrue(verticalRotation.get("min_inclusive").isJsonPrimitive(),
                "crevasse.json: float-provider min_inclusive must be a plain number, not an anchor object");
        assertTrue(verticalRotation.has("max_exclusive"),
                "crevasse.json: uniform float provider upper bound is max_exclusive");
    }

    @Test
    void glacialTunnelsCaveCarrierHasEveryRequiredCodecField() {
        JsonObject config = requireFlatCarver(load("glacial_tunnels.json"), "glacial_tunnels.json", "minecraft:cave",
                "probability", "y", "count", "thickness", "weird_thickness_bias",
                "room_vertical_radius_multiplier", "horizontal_radius_multiplier",
                "vertical_radius_multiplier", "floor_level");

        // The design's Y band, S25b DEEPENED: absolute uniform -40..90 (owner TEST 117: caves "should extend
        // down further into the sub y zero zone... it still seems like it ends pretty abruptly"). The floor
        // dropped 30 -> -40 into the deepslate labyrinth; the top stays 90; lava_level is gone entirely on
        // 26.3 (checked by requireFlatCarver above -- there is no lava-window guard to keep below the floor).
        JsonObject y = config.getAsJsonObject("y");
        assertEquals("minecraft:uniform", y.get("type").getAsString(), "glacial_tunnels.json: y provider type");
        assertAbsoluteHeightProviderBound(y, "min_inclusive", -40, "glacial_tunnels.json y");
        assertAbsoluteHeightProviderBound(y, "max_inclusive", 90, "glacial_tunnels.json y");

        // The tightened-radii design intent (horizontal ~0.7x, vertical ~0.5x of vanilla) -- pin the
        // exact dev-pick numbers recorded in the LatitudeV2Flags javadoc.
        JsonObject horizontal = config.getAsJsonObject("horizontal_radius_multiplier");
        assertEquals(0.49, horizontal.get("min_inclusive").getAsDouble(), 1e-9,
                "glacial_tunnels.json: horizontal radius min = 0.7 x vanilla 0.7");
        assertEquals(0.98, horizontal.get("max_exclusive").getAsDouble(), 1e-9,
                "glacial_tunnels.json: horizontal radius max = 0.7 x vanilla 1.4");
        JsonObject vertical = config.getAsJsonObject("vertical_radius_multiplier");
        assertEquals(0.4, vertical.get("min_inclusive").getAsDouble(), 1e-9,
                "glacial_tunnels.json: vertical radius min = 0.5 x vanilla 0.8");
        assertEquals(0.65, vertical.get("max_exclusive").getAsDouble(), 1e-9,
                "glacial_tunnels.json: vertical radius max = 0.5 x vanilla 1.3");

        // room_vertical_radius_multiplier carries the OLD carver-root "yScale" uniform(0.1, 0.9) value --
        // CaveWorldCarver has no top-level y-scale field on 26.3, but this is the field that value was
        // always describing (it is also, numerically, vanilla cave.json's own default for this field).
        JsonObject roomVertical = config.getAsJsonObject("room_vertical_radius_multiplier");
        assertEquals("minecraft:uniform", roomVertical.get("type").getAsString());
        assertEquals(0.1, roomVertical.get("min_inclusive").getAsDouble(), 1e-9,
                "glacial_tunnels.json: room_vertical_radius_multiplier min (was carver-root yScale)");
        assertEquals(0.9, roomVertical.get("max_exclusive").getAsDouble(), 1e-9,
                "glacial_tunnels.json: room_vertical_radius_multiplier max (was carver-root yScale)");
    }
}
