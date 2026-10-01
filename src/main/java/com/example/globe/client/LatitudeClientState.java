package com.example.globe.client;

public final class LatitudeClientState {
    private LatitudeClientState() {
    }

    /** Timestamp (System.currentTimeMillis) when beginExpedition was called. */
    public static long expeditionStartMs = 0L;
    /** Last observed vanilla loading progress (0..1), used across loading-screen handoff. */
    public static volatile float latitudeLoadingProgress = 0f;
    /** Single source of truth for bespoke loading overlay lifecycle. */
    private static volatile boolean latitudeWorldLoading = false;
    /** Latches first client-ready observation to avoid log spam across ticks. */
    private static volatile boolean clientReadyObserved = false;
    /** Last elapsed value captured during a lifecycle clear, for post-clear logging. */
    private static volatile long lastLifecycleClearElapsedMs = -1L;
    /**
     * Display label for the loading screen's optional "Loading &lt;Zone&gt;" line, or null to show
     * nothing. Reset to null whenever a new loading sequence begins (see
     * {@link #activateLatitudeLoading()}) so a prior world's label can never leak into the next.
     */
    private static volatile String loadingZoneLabel;
    public static long elapsedSinceExpeditionMs() {
        return expeditionStartMs > 0L ? System.currentTimeMillis() - expeditionStartMs : -1L;
    }

    public static synchronized void beginExpedition(long startMs) {
        expeditionStartMs = startMs;
        latitudeLoadingProgress = 0f;
        latitudeWorldLoading = false;
        clientReadyObserved = false;
        lastLifecycleClearElapsedMs = -1L;
    }

    public static synchronized void activateLatitudeLoading() {
        if (expeditionStartMs <= 0L) {
            expeditionStartMs = System.currentTimeMillis();
        }
        if (!latitudeWorldLoading) {
            // Only clear on this sequence's FIRST activation. activateLatitudeLoading() is called
            // again later in the SAME sequence — e.g. when the GlobeStatePayload handshake packet
            // arrives after join — and must not wipe out a label a caller already set for this
            // load. (Bug: the label was showing for ~1s then vanishing mid-load, because this
            // second call was clearing it before the loading screen ever closed.)
            loadingZoneLabel = null;
        }
        latitudeWorldLoading = true;
        latitudeLoadingProgress = 0f;
        clientReadyObserved = false;
    }

    public static boolean isLatitudeWorldLoading() {
        return latitudeWorldLoading;
    }

    /**
     * Normalised save-directory path of the world the early world-open hook pre-activated the
     * overlay for after reading Latitude's own state file, or null. Consumed by the world-load
     * hook so a save that Latitude wrote is trusted even when the generator settings Minecraft
     * hands back on reload cannot be matched to a Latitude preset (seen on 26.1: the overlay was
     * cleared again and vanilla's screen showed until the last second).
     */
    private static volatile String preActivatedWorldRoot;

    public static void markPreActivatedWorld(String normalisedWorldRoot) {
        preActivatedWorldRoot = normalisedWorldRoot;
    }

    /** True once per pre-activation, and only for the same save it was made for. */
    public static synchronized boolean consumePreActivatedWorld(String normalisedWorldRoot) {
        String pending = preActivatedWorldRoot;
        preActivatedWorldRoot = null;
        return pending != null && pending.equals(normalisedWorldRoot);
    }

    /** Sets the loading screen's zone label. Pass null to show no zone line for this load. */
    public static void setLoadingZoneLabel(String label) {
        loadingZoneLabel = label;
    }

    public static String loadingZoneLabel() {
        return loadingZoneLabel;
    }

    public static synchronized boolean markClientReadyObserved() {
        if (!latitudeWorldLoading || clientReadyObserved) {
            return false;
        }
        clientReadyObserved = true;
        return true;
    }

    public static long lastLifecycleClearElapsedMs() {
        return lastLifecycleClearElapsedMs;
    }

    public static synchronized long clearLatitudeLoadingState() {
        long sinceExpedition = elapsedSinceExpeditionMs();
        latitudeWorldLoading = false;
        clientReadyObserved = false;
        lastLifecycleClearElapsedMs = sinceExpedition;
        expeditionStartMs = 0L;
        latitudeLoadingProgress = 0f;
        loadingZoneLabel = null;
        preActivatedWorldRoot = null;
        return sinceExpedition;
    }

}
