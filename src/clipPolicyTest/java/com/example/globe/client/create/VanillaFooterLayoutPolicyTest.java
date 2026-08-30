package com.example.globe.client.create;

/**
 * The footer arithmetic itself, exercised directly. Until 2026-08-30 this line pinned only that the
 * mixins CALL the policy (source scans in {@code VanillaHandoffCostPolicyTest}); the arithmetic ran
 * untested. The weighted split added that day is separate code from the sibling line's tested copy
 * -- same idea, different constants -- and an untested port of tested math is exactly the parity
 * trap this file closes.
 */
public final class VanillaFooterLayoutPolicyTest {
    private static int assertions;

    private VanillaFooterLayoutPolicyTest() {
    }

    public static void run() {
        equalSplitReproducesVanillaAndNeverOutgrowsTheRow();
        theDoorTakesOnlyWhatItsLabelNeedsAndTheMainsRecoverTheRest();
        System.out.println("PASS VanillaFooterLayoutPolicyTest assertions=" + assertions);
    }

    /** The policy is only trustworthy for three if it agrees with vanilla for two. */
    private static void equalSplitReproducesVanillaAndNeverOutgrowsTheRow() {
        expectTrue(VanillaFooterLayoutPolicy.buttonWidth(2, VanillaFooterLayoutPolicy.VANILLA_ROW_WIDTH) == 150,
                "two buttons in vanilla's own 308 envelope must give back vanilla's own 150");
        int three = VanillaFooterLayoutPolicy.buttonWidth(3, VanillaFooterLayoutPolicy.VANILLA_ROW_WIDTH);
        expectTrue(3 * three + 2 * VanillaFooterLayoutPolicy.SPACING <= VanillaFooterLayoutPolicy.VANILLA_ROW_WIDTH,
                "three equal buttons must stay inside the row's own footprint");
    }

    /**
     * The weighted split that lets "Play Selected World" fit again (maintainer report, 2026-08-30).
     * Two-sided: the door takes only what its label needs, and it can never take MORE than the
     * equal share -- so Play and Create sit at or above the old equal width at every envelope, and
     * a squeezed row degrades to exactly the old behaviour rather than past it.
     */
    private static void theDoorTakesOnlyWhatItsLabelNeedsAndTheMainsRecoverTheRest() {
        for (int envelope : new int[] {308, 300, 280, 240, 200, 160, 120, 80, 40}) {
            int share = VanillaFooterLayoutPolicy.buttonWidth(3, envelope);
            int door = VanillaFooterLayoutPolicy.doorWidthForThree(envelope, 56);
            int main = VanillaFooterLayoutPolicy.mainWidthForThree(envelope, door);
            expectTrue(door >= 1 && main >= 1, "widths must stay positive at envelope " + envelope);
            expectTrue(door <= share,
                    "the door can never take more than an equal split at envelope " + envelope);
            expectTrue(main >= share,
                    "the mains must never fall below the equal share at envelope " + envelope
                            + "; main " + main + " vs share " + share);
            int[] xs = VanillaFooterLayoutPolicy.threeButtonXsWeighted(100, main, door);
            expectTrue(xs[0] < xs[1] && xs[1] < xs[2], "buttons must stay in order at " + envelope);
            expectTrue(xs[1] - (xs[0] + main) == VanillaFooterLayoutPolicy.SPACING
                            && xs[2] - (xs[1] + main) == VanillaFooterLayoutPolicy.SPACING,
                    "vanilla's own spacing must separate all three at " + envelope);
            expectTrue(xs[2] + door <= 100 + envelope,
                    "the weighted row must stay inside the row's own footprint at envelope " + envelope
                            + "; right edge " + (xs[2] + door) + " vs " + (100 + envelope));
        }

        // A greedy door is capped at the equal share, so weighting can never make the mains WORSE.
        int share = VanillaFooterLayoutPolicy.buttonWidth(3, 308);
        expectTrue(VanillaFooterLayoutPolicy.doorWidthForThree(308, 10_000) == share,
                "a label wider than the equal share is capped at it");
        expectTrue(VanillaFooterLayoutPolicy.mainWidthForThree(308, share) >= share,
                "and at the cap the mains still never fall below the old equal width");

        expectThrows(() -> VanillaFooterLayoutPolicy.mainWidthForThree(308, 0), "a zero door width");
        expectThrows(() -> VanillaFooterLayoutPolicy.threeButtonXsWeighted(0, 0, 56), "a zero main width");
        expectThrows(() -> VanillaFooterLayoutPolicy.threeButtonXsWeighted(0, 100, 0), "a zero door width placed");
    }

    private static void expectTrue(boolean condition, String label) {
        assertions++;
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void expectThrows(Runnable action, String label) {
        assertions++;
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(label + " must be rejected");
    }
}
