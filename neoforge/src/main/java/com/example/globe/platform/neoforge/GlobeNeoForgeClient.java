package com.example.globe.platform.neoforge;

import com.example.globe.GlobeMod;
import com.example.globe.GlobeModClient;
import com.example.globe.client.HotbarHazeLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** NeoForge client entrypoint; all startup work lives in {@link GlobeModClient#onInitializeClient()}. */
@Mod(value = GlobeMod.MOD_ID, dist = Dist.CLIENT)
public final class GlobeNeoForgeClient {
    private static final Identifier HOTBAR_HAZE_LAYER = Identifier.fromNamespaceAndPath(GlobeMod.MOD_ID, "ew_haze");

    public GlobeNeoForgeClient(IEventBus modBus) {
        NeoForgeLatitudeClientPlatform.bind(modBus);
        modBus.addListener(RegisterGuiLayersEvent.class, GlobeNeoForgeClient::registerGuiLayers);
        new GlobeModClient().onInitializeClient();
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        if (hasVanillaHotbarMethod()) {
            // InGameHudMixin's hotbar hook will apply on this NeoForge build; a layer would draw twice.
            GlobeMod.LOGGER.warn("[Latitude] Gui.extractHotbarAndDecorations exists on this NeoForge build; "
                    + "using the mixin hook instead of a GUI layer for the sandstorm haze");
            return;
        }
        // Vanilla draws the hotbar only while the HUD is shown; mod layers carry no such gate.
        event.registerBelow(VanillaGuiLayers.HOTBAR, HOTBAR_HAZE_LAYER, GlobeNeoForgeClient::renderHotbarHaze);
    }

    private static void renderHotbarHaze(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.hideGui) {
            return;
        }
        HotbarHazeLayer.render(graphics, deltaTracker);
    }

    private static boolean hasVanillaHotbarMethod() {
        try {
            Gui.class.getDeclaredMethod("extractHotbarAndDecorations", GuiGraphicsExtractor.class, DeltaTracker.class);
            return true;
        } catch (NoSuchMethodException absent) {
            return false;
        }
    }
}
