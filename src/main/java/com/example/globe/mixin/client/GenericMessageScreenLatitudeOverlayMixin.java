package com.example.globe.mixin.client;

import com.example.globe.client.LatitudeClientState;
import com.example.globe.client.LatitudeLoadingPane;
import com.example.globe.client.create.CreateWorldIntroClock;
import com.example.globe.client.create.CreateWorldIntroTitle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the Latitude loading pane over the plain message screens a world load passes through
 * before the real loading screen exists.
 *
 * <p>Two flows show one. Creating a world shows {@code CreateWorldScreen}'s
 * "Preparing for world creation" screen while datapacks load, before
 * {@code LatitudeCreateWorldScreen} is even constructed. Resuming a save shows the same screen
 * class from {@code WorldOpenFlows} on the newer versions of the supported range — at its own head,
 * and again while the level data and the world stem are read — long before {@code doWorldLoad}
 * reaches {@code LevelLoadingScreen}. The overlay only ever painted that final screen, so a reload
 * showed several seconds of vanilla panorama and then snapped to Latitude's pane: the "vanilla
 * loading screen, last minute switches to bespoke" reported live on 2026-08-09.
 *
 * <p>This screen declares {@code render}, so that is the hook. Painting at its TAIL puts the pane
 * over vanilla's own centred message line, which this screen draws from inside that same method
 * rather than through a widget the overlay could hide — which is why nothing is hidden and nothing
 * has to be restored here. A message screen shown for any other reason draws exactly as vanilla
 * intends, because the pane is only painted while Latitude owns the load.
 *
 * <p>The create-world case is recognised by translation key rather than a state flag, since nothing
 * runs before vanilla's own {@code show()} to set one. That title (see {@link CreateWorldIntroTitle})
 * previously started its own fade-in clock fresh once the bespoke screen finally appeared, so this
 * vanilla text always got to flash first no matter how long the load took (maintainer report,
 * 2026-08-10).
 *
 * <p>Fail-soft like every overlay hook in this lifecycle ({@code require = 0}): a missed target just
 * restores the previous behaviour of branding only the final screen, never a crash.
 */
@Mixin(GenericDirtMessageScreen.class)
public abstract class GenericMessageScreenLatitudeOverlayMixin {

    @Unique
    private static final String CREATE_WORLD_PREPARING_KEY = "createWorld.preparing";

    @Inject(method = "render", at = @At("TAIL"), require = 0, expect = 1)
    private void globe$paintLatitudeLoadingPane(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GenericDirtMessageScreen self = (GenericDirtMessageScreen) (Object) this;
        if (globe$isCreateWorldPreparing(self)) {
            // Always paints here, without predicting whether the eventual LatitudeCreateWorldScreen
            // will land in tabbedMode -- a prediction miss left this screen blank while the shared
            // clock kept advancing, so the title had already finished most of its fade-in by the time
            // the real screen took over and could paint it, and the fade read as an instant "burst"
            // instead (maintainer report, live 2026-08-10). The rare three-column-layout case just
            // gets a brief title flash that vanishes once the real screen's plain header replaces it.
            long now = Util.getMillis();
            CreateWorldIntroClock.beginForOwner(self, now);
            CreateWorldIntroClock.advance(now);
            CreateWorldIntroTitle.render(context, Minecraft.getInstance().font, self.width, self.height);
            return;
        }

        boolean loading = LatitudeClientState.isLatitudeWorldLoading();
        if (!loading) {
            return;
        }
        long now = Util.getMillis();
        LatitudeLoadingPane.start(now);
        // No chunk progress exists this early — the track draws empty so the pane's geometry does
        // not shift when LevelLoadingScreen takes over and starts filling it.
        LatitudeLoadingPane.render(context, net.minecraft.client.Minecraft.getInstance().font,
                delta, LatitudeLoadingPane.NO_PROGRESS, now);
    }

    @Unique
    private static boolean globe$isCreateWorldPreparing(GenericDirtMessageScreen screen) {
        return screen.getTitle().getContents() instanceof TranslatableContents translatable
                && CREATE_WORLD_PREPARING_KEY.equals(translatable.getKey());
    }
}
