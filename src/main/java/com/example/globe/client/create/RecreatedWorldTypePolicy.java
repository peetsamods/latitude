package com.example.globe.client.create;

import java.util.Map;
import java.util.List;

/**
 * Recovers Latitude's world-preset identity from the saved overworld noise settings used by
 * Minecraft's Re-Create flow.
 */
public final class RecreatedWorldTypePolicy {
    private static final List<String> LATITUDE_NOISE_SETTINGS_IDS = List.of(
            "globe:overworld",
            "globe:overworld_xsmall",
            "globe:overworld_small",
            "globe:overworld_regular",
            "globe:overworld_large",
            "globe:overworld_massive");
    private static final Map<String, String> LATITUDE_PRESET_BY_NOISE_SETTINGS = Map.of(
            "globe:overworld", "globe:globe",
            "globe:overworld_xsmall", "globe:globe_xsmall",
            "globe:overworld_small", "globe:globe_small",
            "globe:overworld_regular", "globe:globe_regular",
            "globe:overworld_large", "globe:globe_large",
            "globe:overworld_massive", "globe:globe_massive");
    private static final Map<Integer, String> LATITUDE_PRESET_BY_RADIUS = Map.of(
            3750, "globe:globe_xsmall",
            5000, "globe:globe_small",
            7500, "globe:globe_regular",
            10000, "globe:globe_large",
            15000, "globe:globe",
            20000, "globe:globe_massive");

    private RecreatedWorldTypePolicy() {
    }

    static List<String> latitudeNoiseSettingsIds() {
        return LATITUDE_NOISE_SETTINGS_IDS;
    }

    public static String effectivePresetId(
            boolean recreated,
            String selectedPresetId,
            String overworldNoiseSettingsId) {
        return effectivePresetId(recreated, selectedPresetId, null, overworldNoiseSettingsId);
    }

    public static String effectivePresetId(
            boolean recreated,
            String selectedPresetId,
            String persistedPresetId,
            String overworldNoiseSettingsId) {
        if (!recreated) {
            return selectedPresetId;
        }
        // On 1.20.1-1.20.4 vanilla's preset recognition for an existing world knows only the Flat
        // and Debug generators; every noise world, Latitude's included, arrives here with NO
        // selected preset (1.21.1 reports Normal for the same world). A missing selection must
        // therefore not short-circuit the recovery below, or Re-Create on a Latitude world falls
        // through to vanilla's screen. With no persisted radius and no Latitude noise settings the
        // missing selection is returned as-is, which keeps a genuinely vanilla world vanilla.
        if (persistedPresetId != null && LATITUDE_PRESET_BY_RADIUS.containsValue(persistedPresetId)) {
            return persistedPresetId;
        }
        return LATITUDE_PRESET_BY_NOISE_SETTINGS.getOrDefault(
                overworldNoiseSettingsId,
                selectedPresetId);
    }

    public static String presetIdForRadius(int radiusBlocks) {
        return LATITUDE_PRESET_BY_RADIUS.get(radiusBlocks);
    }
}
