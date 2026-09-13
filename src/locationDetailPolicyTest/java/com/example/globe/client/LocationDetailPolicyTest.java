package com.example.globe.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LocationDetailPolicyTest {
    public static void main(String[] args) throws Exception {
        modeCycleIsExact();
        persistedFlagsCoverAllModesAndLegacyValues();
        compositionCoversAllModesInBiomeThenZoneOrder();
        biomeIdsBecomePlayerFacingTitleCase();
        customBiomeSourcesAreOptionalAndPlayerFacing();
        defaultDetachedBoundsStayReachableAtAcceptedGuiSize();
        locationTextScalePolicyIsBoundedAndIndependent();
        staticIntegrationProofsHold();
        System.out.println("LOCATION_DETAIL_POLICY_TEST_PASS");
    }

    private static void modeCycleIsExact() {
        var modes = LocationDetailPolicy.Mode.values();
        assertEquals(4, modes.length, "mode cycle has exactly four entries");
        assertEquals(LocationDetailPolicy.Mode.OFF, modes[0], "Off is first and default");
        assertEquals("Off", modes[0].label(), "Off label");
        assertEquals(LocationDetailPolicy.Mode.BIOME, modes[1], "Biome is second");
        assertEquals("Biome", modes[1].label(), "Biome label");
        assertEquals(LocationDetailPolicy.Mode.ZONE, modes[2], "Zone is third");
        assertEquals("Zone", modes[2].label(), "Zone label");
        assertEquals(LocationDetailPolicy.Mode.BIOME_AND_ZONE, modes[3], "combined is fourth");
        assertEquals("Biome + Zone", modes[3].label(), "combined label");
        assertEquals(LocationDetailPolicy.Mode.OFF, LocationDetailPolicy.DEFAULT_MODE, "default is Off");
    }

    private static void persistedFlagsCoverAllModesAndLegacyValues() {
        assertEquals(
                LocationDetailPolicy.Mode.OFF,
                LocationDetailPolicy.fromPersistedFlags(false, false),
                "both flags false maps to Off");
        assertEquals(
                LocationDetailPolicy.Mode.BIOME,
                LocationDetailPolicy.fromPersistedFlags(true, false),
                "biome-only flags map to Biome");
        assertEquals(
                LocationDetailPolicy.Mode.ZONE,
                LocationDetailPolicy.fromPersistedFlags(false, true),
                "zone-only flags map to Zone");
        assertEquals(
                LocationDetailPolicy.Mode.BIOME_AND_ZONE,
                LocationDetailPolicy.fromPersistedFlags(true, true),
                "both flags true map to Biome + Zone");

        assertEquals(
                LocationDetailPolicy.Mode.OFF,
                LocationDetailPolicy.fromPersistedFlags(false, false),
                "legacy displayZoneInHud=false maps to Off when new biome flag is absent/default false");
        assertEquals(
                LocationDetailPolicy.Mode.ZONE,
                LocationDetailPolicy.fromPersistedFlags(false, true),
                "legacy displayZoneInHud=true maps to Zone when new biome flag is absent/default false");
    }

    private static void compositionCoversAllModesInBiomeThenZoneOrder() {
        assertEquals(
                null,
                LocationDetailPolicy.compose(LocationDetailPolicy.Mode.OFF, "Plains", "Tropical"),
                "Off composes no location detail");
        assertEquals(
                "Plains",
                LocationDetailPolicy.compose(LocationDetailPolicy.Mode.BIOME, "Plains", "Tropical"),
                "Biome composes only biome");
        assertEquals(
                "Tropical",
                LocationDetailPolicy.compose(LocationDetailPolicy.Mode.ZONE, "Plains", "Tropical"),
                "Zone composes only zone");
        assertEquals(
                "Plains \u00b7 Tropical",
                LocationDetailPolicy.compose(
                        LocationDetailPolicy.Mode.BIOME_AND_ZONE,
                        "Plains",
                        "Tropical"),
                "combined detail composes biome before zone as one string");
    }

    private static void biomeIdsBecomePlayerFacingTitleCase() {
        assertEquals(
                "Plains",
                LocationDetailPolicy.titleCaseBiomeId("minecraft:plains"),
                "vanilla namespace is removed");
        assertEquals(
                "Pasture",
                LocationDetailPolicy.titleCaseBiomeId("biomesoplenty:pasture"),
                "mod namespace is removed");
        assertEquals(
                "Old Growth Birch Forest",
                LocationDetailPolicy.titleCaseBiomeId("minecraft:old_growth_birch_forest"),
                "multi-word biome path is title-cased");
    }

    private static void customBiomeSourcesAreOptionalAndPlayerFacing() {
        assertEquals(
                "Snowy Shield",
                LocationDetailPolicy.biomeLabel("biomesoplenty:snowy_shield", false),
                "source toggle off preserves the compact biome-only label");
        assertEquals(
                "Snowy Shield \u00b7 BIOMES O' PLENTY",
                LocationDetailPolicy.biomeLabel("biomesoplenty:snowy_shield", true),
                "known custom provider is named when the toggle is on");
        assertEquals(
                "Plains",
                LocationDetailPolicy.biomeLabel("minecraft:plains", true),
                "vanilla biomes stay unlabelled");
        assertEquals(
                "Pale Garden \u00b7 REGIONS UNEXPLORED",
                LocationDetailPolicy.biomeLabel("regions_unexplored:pale_garden", true),
                "generic custom-provider formatting is deterministic");
        assertEquals(
                "Plains \u00b7 VANILLA",
                LocationDetailPolicy.studioPreviewBiomeLabel("minecraft:plains", true),
                "Studio demonstrates the source toggle even when its coherent sample biome is vanilla");
        assertEquals(
                "Plains",
                LocationDetailPolicy.studioPreviewBiomeLabel("minecraft:plains", false),
                "Studio source example disappears when the toggle is off");
    }

    private static void defaultDetachedBoundsStayReachableAtAcceptedGuiSize() {
        int screenW = 427;
        int screenH = 240;
        var detail = centeredTop(screenW, 29, 9);
        var digital = centeredTop(screenW, 52, 15);
        var analog = centeredTop(screenW, 68, 48);

        var separatedDigital = moveDetachedDetail(detail, digital, screenH, true);
        var separatedAnalog = moveDetachedDetail(detail, analog, screenH, true);
        assertTrue(!intersects(digital, separatedDigital),
                "default Digital Detach detail does not overlap at accepted 427x240 GUI size");
        assertTrue(!intersects(analog, separatedAnalog),
                "default Analog Detach detail does not overlap at accepted 427x240 GUI size");
        assertEquals(digital.y + digital.h + 4, separatedDigital.y,
                "default Digital Detach detail uses the fixed four-pixel gap");
        assertEquals(analog.y + analog.h + 4, separatedAnalog.y,
                "default Analog Detach detail uses the fixed four-pixel gap");
        assertEquals(detail, moveDetachedDetail(detail, digital, screenH, false),
                "Follow or an explicit detached placement remains byte-for-byte unchanged");

        var alreadyClear = new Rect(detail.x, 80, detail.w, detail.h);
        assertEquals(alreadyClear, moveDetachedDetail(alreadyClear, digital, screenH, true),
                "a non-intersecting pristine default remains unchanged");

        var nearBottomCompass = new Rect(180, 220, 68, 15);
        var nearBottomDetail = new Rect(199, 226, 29, 9);
        var clampedAbove = moveDetachedDetail(
                nearBottomDetail,
                nearBottomCompass,
                screenH,
                true);
        assertTrue(!intersects(nearBottomCompass, clampedAbove),
                "surface clamp falls back above instead of reintroducing an overlap");
        assertEquals(207, clampedAbove.y,
                "surface-clamped fallback keeps the fixed four-pixel gap");
    }

    /**
     * Static integration proofs, rewritten for the merged 2.0 line (maintainer ruling, 2026-09-12).
     *
     * <p>WHAT CHANGED AND WHY. This method used to assert the SHAPE of the 1.5 HUD: one combined
     * "location detail" unit (biome and zone glued together, one pin, one {@code locationTextScale}), a
     * three-tab Studio, and a single {@code Location Detail} cycle button. The 2.0 HUD Studio is a
     * different product and a strict superset of that: zone, biome, coordinates and the clock readout are
     * each INDEPENDENT elements with their own pin, anchor, grow direction and text size, laid out by
     * {@code core.ui.HudLayoutMath}'s Pin-and-Grow model. Re-imposing the combined unit would remove the
     * per-element placement the 2.0 Studio exists to provide, so those assertions are genuinely superseded
     * and are replaced here by their 2.0 equivalents.
     *
     * <p>What is KEPT, because it is the part of the 1.5 feature that 2.0 did not already have: the
     * persisted-flag mapping (so an old config's zone boolean still means Zone), the derived four-state
     * mode, and the optional custom-biome SOURCE tag. Every pure assertion in this class is untouched.
     */
    private static void staticIntegrationProofsHold() throws IOException {
        String config = normalize(read("src/main/java/com/example/globe/client/CompassHudConfig.java"));
        assertTrue(
                config.contains("public boolean displayBiomeInHud = false;")
                        && config.contains("public boolean displayZoneInHud = false;")
                        && config.contains("public boolean showCustomBiomeSource = false;"),
                "persisted location-detail flags keep their legacy names and defaults");
        assertTrue(
                config.contains("LocationDetailPolicy.fromPersistedFlags(displayBiomeInHud, displayZoneInHud)"),
                "the four-state mode is derived from the persisted booleans, never stored separately");
        assertTrue(
                config.contains("displayBiomeInHud = selected.includesBiome();")
                        && config.contains("displayZoneInHud = selected.includesZone();"),
                "setting the mode writes back through the same two persisted booleans");
        // 2.0 equivalent of the old single locationTextScale: per-element text sizes. The combined-unit
        // scale is superseded by these, not dropped -- every element the 1.5 unit covered has its own.
        assertTrue(
                config.contains("public float zoneTextScale = 1.0f;")
                        && config.contains("public float biomeTextScale = 1.0f;")
                        && config.contains("public float coordsTextScale = 1.0f;"),
                "each location element carries its own independent text size");

        String hud = normalize(read("src/main/java/com/example/globe/client/CompassHud.java"));
        assertTrue(
                hud.contains("LocationDetailPolicy.customProviderLabel(biomeId)")
                        && hud.contains("cfg.showCustomBiomeSource"),
                "the biome label appends its provider only when the player asked for it");
        assertTrue(
                hud.contains("LocationDetailPolicy.COMBINED_SEPARATOR"),
                "the provider tag uses the shared separator rather than a local one");
        assertTrue(
                hud.contains("case \"EQUATOR\", \"TROPICAL\" -> \"Tropical\";")
                        || hud.contains("LatitudeBands.displayNameForZoneKey"),
                "zone display names come from the one canonical vocabulary");
        assertTrue(
                !hud.contains("\"Tropics\"") && !hud.contains("\"Subtropics\""),
                "the retired zone vocabulary cannot come back through the HUD");

        String studio = normalize(read("src/main/java/com/example/globe/client/LatitudeHudStudioScreen.java"));
        assertTrue(
                studio.contains("Component.literal(\"Show Biome Source\")")
                        && studio.contains("cfg.showCustomBiomeSource = value")
                        && studio.contains("locationDetailMode().includesBiome()"),
                "the Studio exposes the source tag and hides it when no biome is shown");
        assertTrue(
                studio.contains("Component.literal(\"Biome Text Size\")")
                        && studio.contains("Component.literal(\"Biome Placement\")"),
                "the Studio keeps per-element biome size and placement controls");
        assertTrue(
                !Files.exists(Path.of("src/main/java/com/example/globe/client/LatitudeSettingsScreen.java")),
                "the retired standalone settings screen cannot retain a parallel Boolean or reset path");

        String build = normalize(read("build.gradle"));
        assertTrue(
                build.contains("tasks.register('latitudeLocationDetailPolicyTest', JavaExec)")
                        && build.contains("dependsOn tasks.named('latitudeLocationDetailPolicyTest')"),
                "location-detail policy proof is automatically wired into Gradle check/build");
    }

    private static void locationTextScalePolicyIsBoundedAndIndependent() {
        assertEquals(1.0f, HudTextLayoutPolicy.sanitizeLocationTextScale(Float.NaN), "NaN restores default");
        assertEquals(1.0f, HudTextLayoutPolicy.sanitizeLocationTextScale(Float.POSITIVE_INFINITY), "infinity restores default");
        assertEquals(1.0f, HudTextLayoutPolicy.sanitizeLocationTextScale(0.0f), "missing primitive JSON value restores default");
        assertEquals(0.50f, HudTextLayoutPolicy.sanitizeLocationTextScale(0.49f), "low values clamp to 50%");
        assertEquals(1.25f, HudTextLayoutPolicy.sanitizeLocationTextScale(1.26f), "high values clamp to 125%");
        assertEquals(1.05f, HudTextLayoutPolicy.sanitizeLocationTextScale(1.03f), "saved values quantize to five-percent steps");

        int directionWidth = HudTextLayoutPolicy.scaledPixels(20, 1.25f);
        int smallLocationWidth = HudTextLayoutPolicy.scaledPixels(30, 0.50f);
        int largeLocationWidth = HudTextLayoutPolicy.scaledPixels(30, 1.25f);
        assertEquals(25, directionWidth, "digital direction uses compass scale");
        assertEquals(15, smallLocationWidth, "small location text uses its own scale");
        assertEquals(38, largeLocationWidth, "large location text uses its own scale");
        assertEquals(directionWidth, HudTextLayoutPolicy.scaledPixels(20, 1.25f),
                "changing location text size cannot change direction or compass size");

        int directionOnlySmall = HudTextLayoutPolicy.combinedTextHeight(9, 1.0f, 0.50f, false);
        int directionOnlyLarge = HudTextLayoutPolicy.combinedTextHeight(9, 1.0f, 1.25f, false);
        assertEquals(directionOnlySmall, directionOnlyLarge,
                "direction-only digital bounds ignore location text size");
        assertEquals(12, HudTextLayoutPolicy.combinedTextHeight(9, 1.0f, 1.25f, true),
                "location text contributes height only when it is present");

        int longProviderWidth = HudTextLayoutPolicy.digitalBoxWidth(
                3, 1.0f, 8, 20, 150, 1.25f);
        int sampleSubstitutionWidth = HudTextLayoutPolicy.digitalBoxWidth(
                3, 1.0f, 8, 20, 20, 1.25f);
        assertEquals(227, longProviderWidth,
                "125% custom-biome/provider content produces exact rendered compass width");
        assertTrue(longProviderWidth > sampleSubstitutionWidth,
                "live long provider content cannot be substituted by the short Studio sample");

        var longCompass = centeredTop(427, longProviderWidth, 24);
        var longDetail = centeredTop(427, 300, 18);
        var movedLongDetail = moveDetachedDetail(longDetail, longCompass, 240, true);
        assertEquals(32, movedLongDetail.y,
                "pristine detached detail moves below the exact long rendered compass");
        assertTrue(!intersects(longCompass, movedLongDetail),
                "long provider detail cannot overlap its exact runtime compass bounds");

        assertEquals(16.0, HudTextLayoutPolicy.titleDragCoordinate(13.0, true, 8),
                "SNAP title coordinate rounds to the grid");
        assertEquals(13.25, HudTextLayoutPolicy.titleDragCoordinate(13.25, false, 8),
                "FREE title coordinate remains unquantized");
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ");
    }

    private static String slice(String value, String start, String end) {
        int from = value.indexOf(start);
        int to = from < 0 ? -1 : value.indexOf(end, from + start.length());
        if (from < 0 || to < 0) {
            throw new AssertionError("missing source slice: " + start + " ... " + end);
        }
        return value.substring(from, to);
    }

    private static int occurrences(String value, String target) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(target, index)) >= 0) {
            count++;
            index += target.length();
        }
        return count;
    }

    private static Rect centeredTop(int screenW, int w, int h) {
        return new Rect((screenW - w) / 2, 4, w, h);
    }

    private static boolean intersects(Rect a, Rect b) {
        return a.x < b.x + b.w
                && a.x + a.w > b.x
                && a.y < b.y + b.h
                && a.y + a.h > b.y;
    }

    private static Rect moveDetachedDetail(
            Rect detail,
            Rect compass,
            int screenH,
            boolean pristineDefaultPlacement) {
        if (!pristineDefaultPlacement || !intersects(detail, compass)) {
            return detail;
        }
        int movedY = HudTextLayoutPolicy.movePristineDetachedY(
                detail.x,
                detail.y,
                detail.w,
                detail.h,
                compass.x,
                compass.y,
                compass.w,
                compass.h,
                screenH,
                4);
        return movedY == detail.y
                ? detail
                : new Rect(detail.x, movedY, detail.w, detail.h);
    }

    private record Rect(int x, int y, int w, int h) {
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
