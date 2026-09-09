package com.example.globe.world;

import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Lets a Latitude-owned chunk generator answer structure siting with the biome the world will
 * actually paint.
 *
 * <p>A chunk generator is built before the level exists, so the biome source it wraps at
 * construction knows neither the biome registry nor the level's random state or height bounds.
 * That construction-time wrapper picks from the raw source's biome pool with no terrain evidence.
 * Chunk painting runs a different resolver: the registry-backed, terrain-aware pick. In a
 * vanilla-only world the two mostly agree; with biome packs installed they do not, and vanilla's
 * "is this biome allowed for this structure" test reads the generator's biome source, so a
 * woodland mansion could pass as "dark forest" on a column the world then painted as flower
 * forest or a pack biome.
 *
 * <p>The first caller that holds the registry, random state and height bounds hands them over
 * here; the generator then swaps its exposed biome source for the registry-backed resolver, once,
 * and keeps it for the life of the generator. Structure starts are generated before a chunk's
 * biomes are painted, so the structure guard is the usual first caller.
 */
public interface PaintedBiomeSiting {
    void globe$adoptPaintedBiomeSource(Registry<Biome> biomeRegistry, RandomState randomState,
                                       LevelHeightAccessor heightView);
}
