package com.example.globe.util;

import com.example.globe.world.LatitudeBiomeSource;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * The shipped sampler surface: a template describing the live world's biome source and noise
 * settings, used by the entry point for the reserved-biome census. The dev-only inventory, seed
 * search and band-audit report tooling lives in dev/BiomeSamplerDevTools and never ships.
 */
public final class BiomeSamplerTools {
    private BiomeSamplerTools() {
    }

    public static SamplerTemplate createTemplate(ServerLevel world) {
        ChunkGenerator generator = world.getChunkSource().getGenerator();
        if (!(generator instanceof NoiseBasedChunkGenerator noiseGenerator)) {
            throw new IllegalStateException("Sampler search requires a NoiseChunkGenerator");
        }

        BiomeSource biomeSource = generator.getBiomeSource();
        BiomeSource baseSource = biomeSource instanceof LatitudeBiomeSource latitudeSource
                ? latitudeSource.original()
                : biomeSource;
        Registry<Biome> biomeRegistry = world.registryAccess().registryOrThrow(Registries.BIOME);
        HolderGetter<NormalNoise.NoiseParameters> noiseParameters =
                world.registryAccess().lookupOrThrow(Registries.NOISE);

        return new SamplerTemplate(
                biomeRegistry,
                baseSource,
                noiseGenerator.generatorSettings(),
                noiseParameters,
                world.getSeed());
    }

    public record SamplerTemplate(Registry<Biome> biomeRegistry,
                                  BiomeSource baseSource,
                                  Holder<NoiseGeneratorSettings> settings,
                                  HolderGetter<NormalNoise.NoiseParameters> noiseParameters,
                                  long templateSeed) {
    }
}
