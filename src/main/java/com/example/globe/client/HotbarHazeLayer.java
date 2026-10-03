package com.example.globe.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The east/west sandstorm haze, drawn directly beneath the hotbar. Fabric reaches this from
 * {@code InGameHudMixin}; NeoForge, which replaces the vanilla hotbar method with GUI layers,
 * registers it as a layer just below its hotbar layer.
 */
public final class HotbarHazeLayer {
    private HotbarHazeLayer() {
    }

    public static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client != null
                && client.screen != null
                && !(client.screen instanceof LatitudeHudStudioScreen)) {
            return;
        }
        EwSandstormOverlayHud.render(context, tickCounter);
    }
}
