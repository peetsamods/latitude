package com.example.globe.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * The shared "c" convention biome tags Latitude reads. Fabric and NeoForge both populate these ids,
 * so owning the keys here keeps the check identical on either loader.
 */
public final class LatitudeConventionBiomeTags {
    public static final TagKey<Biome> IS_CAVE = tag("is_cave");
    public static final TagKey<Biome> IS_UNDERGROUND = tag("is_underground");

    private LatitudeConventionBiomeTags() {
    }

    private static TagKey<Biome> tag(String path) {
        return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", path));
    }
}
