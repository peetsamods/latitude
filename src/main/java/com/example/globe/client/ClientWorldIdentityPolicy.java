package com.example.globe.client;

/** Shared world identity for Latitude client presentation. */
public final class ClientWorldIdentityPolicy {
    private ClientWorldIdentityPolicy() {
    }

    public static boolean shouldPresent(boolean latitudeSave, boolean overworld) {
        return latitudeSave && overworld;
    }
}
