package com.example.globe.client.create;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Panel cycling for Ctrl+Tab / Ctrl+Shift+Tab. The wrap in both directions is the whole point:
 * a plain {@code %} sends a reverse step off panel zero to -1, which is not a panel.
 *
 * <p>This suite also pins the create-world screen's frame geometry and the ownership of its tab
 * hitboxes, so a later layout retune cannot quietly loosen the margins or hand tab clicks back to
 * a hand-rolled dispatcher that competes with Minecraft's own widget input path.</p>
 */
public final class CreateWorldScreenUiPolicyTest {
    private static int assertions;

    private CreateWorldScreenUiPolicyTest() {
    }

    public static void run() throws IOException {
        cyclesForward();
        cyclesBackward();
        wrapsAtBothEnds();
        singlePanelStaysPut();
        rejectsNonPositivePanelCount();
        highScaleFrameKeepsTightMargins();
        bespokeBackgroundUsesFixedEightyPercentOpacity();
        panelOpacityStaysAFixedDesignValue();
        mouseAndKeyboardFocusFollowInputMode();
        selectedClimateDescriptionUsesReadableContrast();
        accessibilityFooterAvoidsTheCreateButtons();
        tabClicksUseRealWidgetOwnership();
        theStillTabStaysGone();
        screenWheelRoutingStaysOnTheFabricScreenEvents();
        System.out.println("PASS CreateWorldScreenUiPolicyTest assertions=" + assertions);
    }

    private static void cyclesForward() {
        expect(1, CreateWorldScreenUiPolicy.cyclePanel(0, 3, false), "0 -> 1");
        expect(2, CreateWorldScreenUiPolicy.cyclePanel(1, 3, false), "1 -> 2");
    }

    private static void cyclesBackward() {
        expect(1, CreateWorldScreenUiPolicy.cyclePanel(2, 3, true), "2 -> 1");
        expect(0, CreateWorldScreenUiPolicy.cyclePanel(1, 3, true), "1 -> 0");
    }

    private static void wrapsAtBothEnds() {
        expect(0, CreateWorldScreenUiPolicy.cyclePanel(2, 3, false), "last wraps forward to first");
        expect(2, CreateWorldScreenUiPolicy.cyclePanel(0, 3, true), "first wraps backward to last");
    }

    private static void singlePanelStaysPut() {
        expect(0, CreateWorldScreenUiPolicy.cyclePanel(0, 1, false), "single panel forward");
        expect(0, CreateWorldScreenUiPolicy.cyclePanel(0, 1, true), "single panel backward");
    }

    private static void rejectsNonPositivePanelCount() {
        boolean threw = false;
        try {
            CreateWorldScreenUiPolicy.cyclePanel(0, 0, false);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        assertions++;
        if (!threw) {
            throw new AssertionError("zero panel count must be rejected");
        }
    }

    /**
     * This Minecraft line draws the create-world screen over a flat dirt backdrop, with no moving
     * panorama behind it, so a control that swaps the panorama for a flat fill has nothing to do
     * here and must not be offered (maintainer ruling, 2026-09-08). Pinning its absence is what
     * stops a later port merge from quietly reintroducing the widget and its saved setting.
     */
    private static void theStillTabStaysGone() throws IOException {
        String screen = Files.readString(Path.of(
                "src/main/java/com/example/globe/client/create/LatitudeCreateWorldScreen.java"));
        expectTrue(!screen.contains("StillTabWidget") && !screen.contains("createWorldStillBackground"),
                "the create-world screen must carry no Still tab and no still-background setting");
    }

    private static void bespokeBackgroundUsesFixedEightyPercentOpacity() {
        int background = CreateWorldScreenUiPolicy.bespokeBackground(0x3A302A);
        expect(80, CreateWorldScreenUiPolicy.BESPOKE_BACKGROUND_OPACITY_PERCENT,
                "fixed background opacity percentage");
        expect(204, background >>> 24, "exact 80 percent alpha channel");
        expect(0x3A302A, background & 0x00FFFFFF,
                "bespoke background keeps its brown color");
    }

    private static void panelOpacityStaysAFixedDesignValue() throws IOException {
        String screen = Files.readString(Path.of(
                "src/main/java/com/example/globe/client/create/LatitudeCreateWorldScreen.java"));
        String config = Files.readString(Path.of(
                "src/main/java/com/example/globe/client/LatitudeConfig.java"));
        expectTrue(!screen.contains("BackgroundOpacitySlider"),
                "world creation must not carry a one-use opacity slider");
        expectTrue(!config.contains("createWorldPanelOpacity"),
                "panel opacity must remain a fixed design value, not saved configuration");
    }

    private static void mouseAndKeyboardFocusFollowInputMode() {
        expectTrue(!CreateWorldScreenUiPolicy.shouldRetainButtonFocus(true, false),
                "mouse focus must clear after the pointer leaves");
        expectTrue(CreateWorldScreenUiPolicy.shouldRetainButtonFocus(true, true),
                "real mouse hover must remain highlighted");
        expectTrue(CreateWorldScreenUiPolicy.shouldRetainButtonFocus(false, false),
                "keyboard focus must remain visible without mouse hover");
    }

    private static void selectedClimateDescriptionUsesReadableContrast() throws IOException {
        String screen = Files.readString(Path.of(
                "src/main/java/com/example/globe/client/create/LatitudeCreateWorldScreen.java"));
        expectTrue(screen.contains("selected ? WARM_WHITE : MUTED, false"),
                "the selected climate description must brighten while other rows stay muted");
    }

    private static void accessibilityFooterAvoidsTheCreateButtons() {
        expectTrue(!CreateWorldScreenUiPolicy.accessibilityControlsNeedOwnRow(300, 96),
                "wide screens keep one compact footer row");
        expectTrue(CreateWorldScreenUiPolicy.accessibilityControlsNeedOwnRow(90, 96),
                "narrow screens give accessibility controls their own row");
    }

    private static void highScaleFrameKeepsTightMargins() {
        expect(4, CreateWorldScreenUiPolicy.EDGE_MARGIN, "screen-edge margin");
        expect(2, CreateWorldScreenUiPolicy.HEADER_GAP, "top margin");
        expect(2, CreateWorldScreenUiPolicy.PANE_GAP, "world-panel gap");
        expect(1, CreateWorldScreenUiPolicy.TAB_GAP, "tab gap");
        expect(4,
                CreateWorldScreenUiPolicy.PANEL_BOTTOM_MARGIN
                        - CreateWorldScreenUiPolicy.BUTTON_ROW_TOP_FROM_BOTTOM,
                "panel-to-button gap");
    }

    private static void tabClicksUseRealWidgetOwnership() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/example/globe/client/create/LatitudeCreateWorldScreen.java"));
        expectTrue(source.contains("class TabHitboxWidget extends AbstractWidget"),
                "tab hitboxes must be real screen widgets");
        expectTrue(source.contains("this.addRenderableWidget(hitbox)"),
                "tab hitboxes must be registered for Minecraft input dispatch");
        expectTrue(!source.contains("handleTabClick("),
                "manual tab click dispatch must not compete with widget ownership");
    }

    /**
     * Minecraft 1.20.2 changed the scroll-wheel signature mid-range, and the shipped jar is remapped with
     * 1.20.1's mappings, so a plain override can only ever reach one half of the 1.20 line. Wheel handling
     * for the bespoke screens therefore goes through the Fabric screen events, which carry both amounts on
     * every 1.20 version. Losing that registration would silently drop wheel scrolling in the create screen.
     */
    private static void screenWheelRoutingStaysOnTheFabricScreenEvents() throws IOException {
        String client = Files.readString(Path.of("src/main/java/com/example/globe/GlobeModClient.java"));
        String screen = Files.readString(Path.of(
                "src/main/java/com/example/globe/client/create/LatitudeCreateWorldScreen.java"));
        String studio = Files.readString(Path.of("src/main/java/com/example/globe/client/LatitudeHudStudioScreen.java"));
        expectTrue(client.contains("registerScreenWheelRouting();"),
                "the client entrypoint must register the screen wheel routing");
        expectTrue(client.contains("ScreenMouseEvents.allowMouseScroll(screen)"),
                "wheel routing must use the Fabric screen mouse events, which span every 1.20 version");
        expectTrue(!screen.contains("mouseScrolled("),
                "the create screen must not also override mouseScrolled, or one version would scroll twice");
        expectTrue(!studio.contains("mouseScrolled("),
                "the HUD studio must not also override mouseScrolled, or one version would scroll twice");
    }

    private static void expect(int expected, int actual, String label) {
        assertions++;
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " but was " + actual);
        }
    }

    private static void expectTrue(boolean condition, String label) {
        assertions++;
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
