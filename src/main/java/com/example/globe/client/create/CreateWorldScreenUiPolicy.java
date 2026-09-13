package com.example.globe.client.create;

/**
 * Minecraft-free navigation rule for the create-world screen's tab strip.
 *
 * <p>Every constant and rule here is executable without Minecraft on purpose: the screen's margins,
 * its fixed background opacity, its Ctrl+Tab cycling and its accessibility-footer collision test are
 * the parts a headless test can actually pin, so they live here rather than as literals in the
 * screen. The clipped-click geometry that goes with them lives next door in {@link ViewportClipPolicy},
 * which rejects a degenerate rectangle rather than accepting it.</p>
 */
final class CreateWorldScreenUiPolicy {
    static final int EDGE_MARGIN = 4;
    static final int HEADER_GAP = 2;
    static final int PANEL_BOTTOM_MARGIN = 24;
    static final int BUTTON_ROW_TOP_FROM_BOTTOM = 20;
    static final int PANE_GAP = 2;
    static final int TAB_GAP = 1;
    static final int BESPOKE_BACKGROUND_OPACITY_PERCENT = 80;

    private CreateWorldScreenUiPolicy() {
    }

    static int cyclePanel(int activePanel, int panelCount, boolean reverse) {
        if (panelCount <= 0) {
            throw new IllegalArgumentException("panelCount must be positive");
        }
        return Math.floorMod(activePanel + (reverse ? -1 : 1), panelCount);
    }

    static int bespokeBackground(int rgb) {
        int alpha = Math.round(255 * BESPOKE_BACKGROUND_OPACITY_PERCENT / 100.0f);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    static boolean accessibilityControlsNeedOwnRow(int buttonRowStartX, int controlsWidth) {
        return EDGE_MARGIN + controlsWidth + PANE_GAP > buttonRowStartX;
    }

    static boolean shouldRetainButtonFocus(boolean lastInputWasMouse, boolean hovered) {
        return !lastInputWasMouse || hovered;
    }
}
