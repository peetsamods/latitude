package com.example.globe.client.create;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class FooterLabelFitPolicyTest {
    static void run() throws IOException {
        for (int envelope : new int[]{40, 80, 120, 160, 240, 280, 308}) {
            for (int desired : new int[]{1, 56, 1000}) {
                for (int gap : new int[]{8, 10}) {
                    int equal = Math.max(1, (envelope - 8 - gap) / 3);
                    int door = VanillaFooterLayoutPolicy.doorWidthForThree(envelope, 8, gap, desired);
                    int main = VanillaFooterLayoutPolicy.mainWidthForThree(envelope, 8, gap, door);
                    int[] xs = VanillaFooterLayoutPolicy.threeButtonXsWeighted(4, main, door, 8, gap);
                    check(door >= 1 && door <= equal, "the door cannot steal width from the main actions");
                    check(main >= equal, "main actions retain at least their old width");
                    check(xs[1] - xs[0] - main == 8 && xs[2] - xs[1] - main == gap,
                            "native gaps are preserved");
                    check(xs[2] + door <= 4 + envelope, "the row stays inside its envelope");
                }
            }
        }
        int door = VanillaFooterLayoutPolicy.doorWidthForThree(308, 8, 10, 56);
        check(door == 56 && VanillaFooterLayoutPolicy.mainWidthForThree(308, 8, 10, door) == 117,
                "a short label returns useful width to Play and Create");
        String source = Files.readString(Path.of(
                "src/main/java/com/example/globe/mixin/client/SelectWorldScreenVanillaDoorMixin.java"));
        check(source.contains("client.font.width(") && source.contains("mainWidthForThree(")
                        && source.contains("setWidth(doorWidth)"),
                "live initial and resized buttons must use the measured label and weighted widths");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
