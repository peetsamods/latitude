package com.example.globe.tools;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

/**
 * Receives the server command dispatcher when the loader opens command registration. Kept inside
 * the tools package so the loader seam can hand the dispatcher over without naming Brigadier.
 */
@FunctionalInterface
public interface LatitudeCommandRegistrar {
    void register(CommandDispatcher<CommandSourceStack> dispatcher);
}
