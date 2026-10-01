package com.example.globe.dev;

import java.nio.file.Files;
import java.nio.file.Path;

public final class HeadlessWorldContextPolicyTest {
    private HeadlessWorldContextPolicyTest() {
    }

    public static void run() throws Exception {
        require(HeadlessWorldContextPolicy.canUseRecipe(false, 0, 0L, 7500, 77L, 20),
                "a neutral preview context can search multiple seeds");
        require(HeadlessWorldContextPolicy.canUseRecipe(true, 7500, 0L, 7500, 0L, 1),
                "matching seed zero is a valid live world context");
        require(!HeadlessWorldContextPolicy.canUseRecipe(true, 7500, 0L, 10000, 0L, 1),
                "a different recipe radius must not reuse live coverage plans");
        require(!HeadlessWorldContextPolicy.canUseRecipe(true, 7500, 77L, 7500, 0L, 1),
                "a different recipe seed must not reuse live coverage plans");
        require(!HeadlessWorldContextPolicy.canUseRecipe(true, 7500, 0L, 7500, 77L, 1),
                "zero must remain an ordinary seed in identity comparisons");
        require(!HeadlessWorldContextPolicy.canUseRecipe(true, 7500, 77L, 7500, 77L, 2),
                "a seed range must not reuse one world's captured coverage plans");
        require(!HeadlessWorldContextPolicy.canUseRecipe(false, 0, 0L, 0, 0L, 1),
                "an invalid recipe radius fails closed");
        require(!HeadlessWorldContextPolicy.canUseRecipe(false, 0, 0L, 7500, 0L, 0),
                "an empty seed range fails closed");
        String source = Files.readString(Path.of("src/main/java/com/example/globe/dev/BiomePreviewHeadlessRunner.java"));
        for (String method : new String[] {"runExportAndStop", "runSearchAndStop", "runAuditAndStop", "runLocateBoundaryAndStop"}) {
            String body = methodSection(source, "private static void " + method + "(");
            int guard = body.indexOf("refuseIfLiveContextDiffers(");
            int mutation = body.indexOf("LatitudeBiomes.setWorldSeed(");
            if (mutation < 0) mutation = body.indexOf(".searchSeeds(");
            require(guard >= 0 && mutation > guard,
                    method + " must reject incompatible context before any sampler mutation/search");
        }
        String search = methodSection(source, "private static void runSearchAndStop(").replaceAll("\\s+", " ");
        require(search.contains("config.seedStart, Math.max(1, config.seedCount)"),
                "the search guard must account for the entire requested seed range");
        System.out.println("HEADLESS_WORLD_CONTEXT_POLICY_TEST_PASS");
    }

    private static String methodSection(String source, String signature) {
        int start = source.indexOf(signature);
        require(start >= 0, "missing method: " + signature);
        int open = source.indexOf('{', start), depth = 0;
        for (int i = open; i < source.length(); i++) {
            if (source.charAt(i) == '{') depth++;
            if (source.charAt(i) == '}' && --depth == 0) return source.substring(open, i + 1);
        }
        throw new AssertionError("unclosed method: " + signature);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
