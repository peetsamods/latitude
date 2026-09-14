package com.example.globe;

public final class GlobePending {
    private GlobePending() {
    }

    private static volatile String pendingSpawnZone;

    public static volatile boolean startWithCompass = true;

    /**
     * Border radius selected by the bespoke create-world screen. Consumed once
     * by the server on first overworld load and persisted into LatitudeWorldState.
     */
    public static volatile int pendingGlobeRadius = 0;

    /**
     * Reads the create screen's compass choice and restores the default in the same step, so a
     * choice made for one world can never be applied to a world opened later in the session.
     */
    public static boolean consumeStartWithCompass() {
        boolean value = startWithCompass;
        startWithCompass = true;
        return value;
    }

    public static void set(String zoneId) {
        pendingSpawnZone = zoneId;
    }

    public static String peek() {
        return pendingSpawnZone;
    }

    public static String consume() {
        String v = pendingSpawnZone;
        pendingSpawnZone = null;
        return v;
    }
}
