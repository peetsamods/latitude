package com.example.globe.client.create;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Wiring checks for both directions of the vanilla world-creation detour. */
public final class VanillaCreateOptionsCarryTest {
    private VanillaCreateOptionsCarryTest() {
    }

    public static void main(String[] args) throws IOException {
        run();
    }

    public static void run() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/example/globe/client/create/LatitudeCreateWorldScreen.java"));
        String hatch = methodSection(source, "private void openOtherWorldTypes()");
        int constructed = hatch.indexOf("CreateWorldScreen vanillaScreen =");
        int shown = hatch.indexOf("setScreen(vanillaScreen)");
        if (shown < 0) shown = hatch.indexOf("setScreenAndShow(vanillaScreen)");
        require(constructed >= 0 && shown > constructed,
                "the detour must construct the exact vanilla screen before showing it");
        String outbound = normalize(hatch.substring(constructed, shown));
        require(outbound.contains("outboundState.setBonusChest(this.bonusChest);"),
                "the detour must apply the selected bonus-chest option before showing vanilla");
        String normalizedHatch = normalize(hatch);
        String redirect = normalize(Files.readString(Path.of(
                "src/main/java/com/example/globe/mixin/client/CreateWorldScreenInitRedirectMixin.java")));
        boolean structuresThroughPayload = normalizedHatch.contains("this.generateStructures, exitCreateFlow")
                && redirect.contains("setGenerateStructures(payload.generateStructures())");
        require(outbound.contains("outboundState.setGenerateStructures(this.generateStructures);")
                        || structuresThroughPayload,
                "Generate Structures must reach the exact vanilla screen");
        boolean rulesThroughSettings = normalizedHatch.contains(
                "this.allowCommands, this.gameRules, this.holder.dataConfiguration()");
        require(outbound.contains("outboundState.setGameRules(this.gameRules);") || rulesThroughSettings,
                "Game Rules must reach the exact vanilla screen");
        require(outbound.contains("VanillaCreateWorldUiStateCarrier")
                        && outbound.contains("vanillaScreen"),
                "the outbound options must target the exact screen through its plain carrier interface");
        String returned;
        String receiver;
        if (hatch.contains("absorbVanillaCreateState(")) {
            returned = methodSection(source, "private void absorbVanillaCreateState(");
            receiver = "state";
        } else {
            int callback = hatch.indexOf("Runnable returnToLatitude =");
            int exit = hatch.indexOf("Runnable exitCreateFlow =");
            require(callback >= 0 && exit > callback, "the return callback must remain distinct from exit");
            returned = hatch.substring(callback, exit);
            receiver = "vanillaState";
        }
        returned = normalize(returned);
        require(returned.contains("this.generateStructures = " + receiver + ".isGenerateStructures();"),
                "Generate Structures must be read back from vanilla");
        require(returned.contains("this.bonusChest = " + receiver + ".isBonusChest();"),
                "Bonus Chest must be read back from vanilla");
        require(returned.contains("this.gameRules = " + receiver + ".getGameRules();"),
                "Game Rules must be read back from vanilla");
        System.out.println("VANILLA_CREATE_OPTIONS_CARRY_TEST_PASS");
    }

    private static String methodSection(String source, String signature) {
        int start = source.indexOf(signature);
        require(start >= 0, "missing method: " + signature);
        int open = source.indexOf('{', start);
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            if (source.charAt(i) == '{') depth++;
            if (source.charAt(i) == '}' && --depth == 0) return source.substring(open, i + 1);
        }
        throw new AssertionError("unclosed method: " + signature);
    }

    private static String normalize(String text) {
        return text.replaceAll("\\s+", " ");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
