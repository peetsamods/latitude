package com.example.globe.mixin.client;

import com.example.globe.client.CompassHud;
import com.example.globe.client.HotbarHazeLayer;
import com.example.globe.client.GlobeWarningOverlay;
import com.example.globe.client.LatitudeHudStudioScreen;
import com.example.globe.client.ZoneEnterTitleOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {
    // require = 0: NeoForge removes extractHotbarAndDecorations in favour of GUI layers and draws the
    // same haze through a layer below its hotbar. On Fabric the target is still checked at build time
    // by latitudeMixinTargetVerify, which ignores require.
    @Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"), require = 0)
    private void globe$renderEwHazeBeforeHotbar(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        HotbarHazeLayer.render(context, tickCounter);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void globe$renderOverlay(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client != null
                && client.screen != null
                && !(client.screen instanceof LatitudeHudStudioScreen)) {
            return;
        }
        GlobeWarningOverlay.render(context, tickCounter);
        CompassHud.render(context, tickCounter);
        if (client != null && client.getWindow() != null) {
            ZoneEnterTitleOverlay.render(context, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
        }
    }
}
