package com.example.globe.dev;

/** Recipe compatibility with the coverage plans of a loaded Latitude world. */
public final class HeadlessWorldContextPolicy {
    private HeadlessWorldContextPolicy() {
    }

    public static boolean canUseRecipe(boolean latitudeWorld, int liveRadius, long liveSeed,
                                       int recipeRadius, long recipeSeed, int seedCount) {
        if (recipeRadius <= 0 || seedCount <= 0) return false;
        return !latitudeWorld || (liveRadius == recipeRadius && liveSeed == recipeSeed && seedCount == 1);
    }
}
