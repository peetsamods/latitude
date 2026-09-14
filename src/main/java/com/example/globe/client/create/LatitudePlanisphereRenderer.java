package com.example.globe.client.create;

import com.example.globe.util.LatitudeBands;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Renders a live square latitude map for the bespoke create-world screen.
 * Responds to zone and size selection.
 */
public final class LatitudePlanisphereRenderer {

    private LatitudePlanisphereRenderer() {}

    // ── Band native colors (ARGB, indexed by Band.ordinal()) ──
    private static final int[] BAND_COLORS = {
            0xFF1A6B3C, // tropical
            0xFF8B7332, // subtropical
            0xFF3D6B4A, // temperate
            0xFF4A6A7D, // subpolar
            0xFF6A8599  // polar
    };

    private static final int OCEAN_COLOR = 0xFF162A3F;
    private static final int GOLD = 0xFFD4A74A;
    // Hoisted out of the per-frame square-map pass: values() clones the enum array on every call.
    private static final LatitudeBands.Band[] BANDS = LatitudeBands.Band.values();
    private static final double[] COMPACT_GUIDE_DEGREES = {0.0, 23.5, 35.0, 50.0, 66.5};
    private static final int COMPACT_GRID_COLOR = 0x36D9E2E8;
    /** Exactly ten percent opacity: the Regular-world underlay must never read as the selection. */
    static final int REGULAR_WORLD_UNDERLAY_ALPHA = 0x1A;

    /**
     * Render a compact latitude disc within a {@code size × size} pixel area.
     * Five-band filled disc with quiet latitude guides and selected-band edge emphasis.
     * No labels or outer perimeter. Returns immediately if {@code size < 20}.
     *
     * @param context      draw context
     * @param x            top-left X of the bounding square
     * @param y            top-left Y of the bounding square
     * @param size         both width and height of the bounding square (pixels)
     * @param selectedBand the currently selected latitude band, or {@code null} for Random
     */
    public static void renderCompact(GuiGraphics context, int x, int y, int size, LatitudeBands.Band selectedBand) {
        renderSquareMap(context, x, y, size, selectedBand, 0xFF, true);
    }

    /**
     * Draws a Regular-world reference at exactly ten percent opacity for smaller world selections.
     * It deliberately receives no selected band, so this layer can never imply a zone choice.
     */
    public static void renderRegularWorldUnderlay(GuiGraphics context, int x, int y, int size) {
        renderSquareMap(context, x, y, size, null, REGULAR_WORLD_UNDERLAY_ALPHA, false);
    }

    /**
     * Draws a deliberately darkened Regular-world copy over a larger selected Atlas.
     * This marks the familiar 20,000-block footprint without preserving the former full-Ginormous ghost.
     */
    public static void renderDarkenedRegularReference(GuiGraphics context, int x, int y, int size) {
        if (size < 8) return;
        context.fill(x, y, x + size, y + size, 0xA0000000);
        renderSquareMap(context, x, y, size, null, 0x78, false);
    }

    private static void renderSquareMap(GuiGraphics context, int x, int y, int size,
                                        LatitudeBands.Band selectedBand, int alpha, boolean showSelection) {
        if (size < 8) return;

        context.fill(x, y, x + size, y + size, multiplyAlpha(OCEAN_COLOR, alpha));
        LatitudeBands.Band[] bands = BANDS;
        for (int i = 0; i < bands.length; i++) {
            LatitudeBands.Band band = bands[i];
            boolean selected = showSelection && band == selectedBand;
            int color = selected ? lifted(BAND_COLORS[i]) : BAND_COLORS[i];
            drawSquareBand(context, x, y, size, band, multiplyAlpha(color, alpha));
            if (selected) {
                drawSquareBand(context, x, y, size, band, multiplyAlpha(0xFFFFCC66, 0x26));
            }
        }

        int guideColor = multiplyAlpha(COMPACT_GRID_COLOR, alpha);
        for (double deg : COMPACT_GUIDE_DEGREES) {
            int northY = latitudeY(y, size, deg);
            context.fill(x, northY, x + size, northY + 1, guideColor);
            if (deg > 0.0) {
                int southY = latitudeY(y, size, -deg);
                context.fill(x, southY, x + size, southY + 1, guideColor);
            }
        }
        if (showSelection && selectedBand != null) {
            int northEdge = latitudeY(y, size, selectedBand.highDeg());
            int northInner = latitudeY(y, size, selectedBand.lowDeg());
            int southInner = latitudeY(y, size, -selectedBand.lowDeg());
            int southEdge = latitudeY(y, size, -selectedBand.highDeg());
            drawHorizontalEdge(context, x, size, northEdge, GOLD);
            drawHorizontalEdge(context, x, size, northInner, GOLD);
            drawHorizontalEdge(context, x, size, southInner, GOLD);
            drawHorizontalEdge(context, x, size, southEdge, GOLD);
        }
        int border = multiplyAlpha(0xFFFFFFFF, alpha);
        context.fill(x, y, x + size, y + 1, border);
        context.fill(x, y + size - 1, x + size, y + size, border);
        context.fill(x, y, x + 1, y + size, border);
        context.fill(x + size - 1, y, x + size, y + size, border);
    }

    private static void drawSquareBand(GuiGraphics context, int x, int y, int size,
                                       LatitudeBands.Band band, int color) {
        int northTop = latitudeY(y, size, band.highDeg());
        int northBottom = latitudeY(y, size, band.lowDeg());
        int southTop = latitudeY(y, size, -band.lowDeg());
        int southBottom = latitudeY(y, size, -band.highDeg());
        context.fill(x, northTop, x + size, northBottom, color);
        context.fill(x, southTop, x + size, southBottom, color);
    }

    private static int latitudeY(int top, int size, double latitude) {
        return top + (int) Math.round((90.0 - latitude) * size / 180.0);
    }

    private static void drawHorizontalEdge(GuiGraphics context, int x, int size, int y, int color) {
        context.fill(x, y, x + size, y + 1, color);
    }

    private static int multiplyAlpha(int color, int opacity) {
        int sourceAlpha = (color >>> 24) & 0xFF;
        int scaledAlpha = Math.round(sourceAlpha * (opacity & 0xFF) / 255.0f);
        return (color & 0x00FFFFFF) | (scaledAlpha << 24);
    }

    private static int lifted(int color) {
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * 1.25f));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * 1.25f));
        int b = Math.min(255, (int) ((color & 0xFF) * 1.25f));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

}
