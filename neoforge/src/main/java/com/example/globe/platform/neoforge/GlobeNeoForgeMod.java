package com.example.globe.platform.neoforge;

import com.example.globe.GlobeMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** NeoForge common entrypoint; all startup work lives in {@link GlobeMod#onInitialize()}. */
@Mod(GlobeMod.MOD_ID)
public final class GlobeNeoForgeMod {
    public GlobeNeoForgeMod(IEventBus modBus) {
        NeoForgeLatitudePlatform.bind(modBus);
        new GlobeMod().onInitialize();
    }
}
