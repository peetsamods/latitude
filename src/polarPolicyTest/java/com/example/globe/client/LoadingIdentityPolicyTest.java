package com.example.globe.client;

import java.nio.file.Files;
import java.nio.file.Path;

final class LoadingIdentityPolicyTest {
    static void runAll() throws Exception {
        LatitudeClientState.clearLatitudeLoadingState();
        check(!LatitudeClientState.consumePreActivatedWorld("world-a"), "no stale identity at start");
        LatitudeClientState.markPreActivatedWorld("world-a");
        check(LatitudeClientState.consumePreActivatedWorld("world-a"), "the same saved world is trusted");
        check(!LatitudeClientState.consumePreActivatedWorld("world-a"), "identity is consumed only once");
        LatitudeClientState.markPreActivatedWorld("world-a");
        check(!LatitudeClientState.consumePreActivatedWorld("world-b"), "another world cannot inherit the overlay");
        check(!LatitudeClientState.consumePreActivatedWorld("world-a"), "a mismatch consumes stale identity");
        LatitudeClientState.markPreActivatedWorld("world-a");
        LatitudeClientState.clearLatitudeLoadingState();
        check(!LatitudeClientState.consumePreActivatedWorld("world-a"), "failed or closed loading clears identity");
        LatitudeClientState.markPreActivatedWorld(null);
        check(!LatitudeClientState.consumePreActivatedWorld(null), "missing paths never prove identity");
        String load = Files.readString(Path.of(
                "src/main/java/com/example/globe/mixin/client/MinecraftClientStartIntegratedMixin.java"));
        String early = Files.readString(Path.of(
                "src/main/java/com/example/globe/mixin/client/WorldOpenFlowsEarlyLatitudeActivationMixin.java"));
        check(load.contains("!detectedLatitudeWorld && !preActivatedFromSave")
                        && load.contains("consumePreActivatedWorld(globe$worldRootKey(session))"),
                "reload admission must retain the matching saved-world witness");
        check(early.contains("markPreActivatedWorld(worldRoot.toAbsolutePath().normalize().toString())"),
                "the early saved-world read must pass the same normalized identity");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
