package com.example.globe.client.create;

import com.example.globe.client.LatitudeConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws the LATITUDE / New World fade title that opens the bespoke create-world screen in tabbedMode.
 * {@link CreateWorldIntroClock} owns the timing; this class owns only the pixels, so any surface that
 * paints this title stays pixel-identical to every other.
 *
 * <p>The nameplate itself is {@link LatitudeWordmark}, the same helper the permanent create-screen header
 * and the bespoke loading overlay use, with the clock's alpha applied (maintainer ruling, 2026-09-12).
 * Painting it here rather than from the screen is the whole point of this class: it has three callers --
 * the create screen, {@link CreateWorldPreparingScreen}, and the generic-message overlay mixin -- and a
 * wordmark on only one of them would make the handoff between them visibly jump.</p>
 */
public final class CreateWorldIntroTitle {
    private CreateWorldIntroTitle() {
    }

    private static final int WARM_WHITE = 0xFFEDE0D0;
    private static final String SUBTITLE = "New World";
    private static final int SUBTITLE_GAP = 10;
    /** Matches the permanent create-screen header, so the intro hands over to it without a size jump. */
    private static final float WORDMARK_SCALE = 1.5f;
    private static final int WORDMARK_SPARKLES = 4;
    /** Room the wordmark is offered; it returns 0 and we fall back to plain text when it cannot fit. */
    private static final int WORDMARK_BAND_HEIGHT = 28;

    /** Draws the centered, alpha-faded title/subtitle pair into a width x height viewport, at
     *  whatever alpha the shared clock currently reads. No-op once that alpha reaches zero, so
     *  callers can invoke this unconditionally every frame. */
    public static void render(GuiGraphicsExtractor context, Font font, int width, int height) {
        float alpha = CreateWorldIntroClock.alpha();
        if (alpha <= 0f) {
            return;
        }
        int a = Math.round(alpha * 255f) << 24;
        int warmA = (WARM_WHITE & 0x00FFFFFF) | a;

        int bandHeight = Math.max(font.lineHeight, WORDMARK_BAND_HEIGHT);
        int blockHeight = bandHeight + SUBTITLE_GAP + font.lineHeight;
        int startY = (height - blockHeight) / 2;
        int cx = width / 2;

        int drawn = LatitudeWordmark.draw(
                context, font, 0, startY, width, bandHeight,
                WORDMARK_SCALE, WORDMARK_SPARKLES, LatitudeConfig.reduceMotion, alpha);
        int subtitleY = startY + (drawn > 0 ? drawn : bandHeight) + SUBTITLE_GAP;

        int subtitleWidth = font.width(SUBTITLE);
        context.text(font, SUBTITLE, cx - subtitleWidth / 2, subtitleY, warmA, true);
    }
}
