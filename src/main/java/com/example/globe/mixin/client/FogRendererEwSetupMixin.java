package com.example.globe.mixin.client;

import com.example.globe.client.EwPresentationPolicy;
import com.example.globe.client.GlobeClientState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * EAST/WEST DEPTH FOG at the world edge (maintainer ruling, 2026-09-12).
 *
 * <p>The two lines that merged here had moved in opposite directions on the same feature: one grew a flat
 * screen-space tan veil, the other replaced that veil with genuine distance fog. The ruling took the depth
 * fog, because a flat full-screen fill erases scene depth -- the wall two blocks in front of you tints exactly
 * as hard as the horizon -- while tightening the engine's own fog distances is depth-correct: near geometry
 * stays crisp and only the distance dissolves. The veil's owner ({@code EwSandstormOverlayHud}) is now a
 * documented no-op and its two call sites in {@code InGameHudMixin} are gone.
 *
 * <p>This mixin is the EAST/WEST half only. Polar fog is owned by {@link FogRendererPolarSetupMixin} and the
 * {@code core.PolarFogLaw} cap table, and the passage approach by {@link FogRendererPassageSetupMixin}; porting
 * the 1.5 polar half alongside them would double-tighten the poles against a law that already covers them.
 * The envelope and the colour rule are the pure, client-free {@link EwPresentationPolicy} (a 400 -> 50 block
 * smoothstep, warm sand haze below 50 degrees absolute latitude only), and every write here is MIN-guarded
 * against the value the engine already computed, so Latitude can only ever tighten vanilla's fog and rejoins
 * the live baseline continuously at 400 blocks.
 *
 * <p>Ordering: {@code priority = 900} puts this BELOW Sodium's default-priority snapshot of {@code FogData},
 * so the values Sodium reads are the ones Latitude wrote. Against the sibling Latitude fog mixins the order is
 * immaterial -- distances are min-guarded (commutative) and the colour rules are disjoint by latitude (sand
 * haze is exclusive below 50 degrees, polar fog begins at 80).
 */
@Mixin(value = FogRenderer.class, priority = 900)
public class FogRendererEwSetupMixin {

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void latitude$applyEwSetupFog(
            Camera camera,
            int viewDistance,
            DeltaTracker tickCounter,
            float tickDelta,
            ClientLevel level,
            CallbackInfoReturnable<FogData> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null || client.player == null) {
            return;
        }
        // Atmospheric only: underwater / lava / powder snow are vanilla's fog to own.
        if (camera.getFluidInCamera() != FogType.NONE) {
            return;
        }
        if (!GlobeClientState.isGlobeWorld()) {
            return;
        }

        GlobeClientState.Eval eval = GlobeClientState.evaluate(client);
        if (!eval.active()) {
            return;
        }

        FogData fog = cir.getReturnValue();
        if (fog == null) {
            return;
        }
        double absoluteLatitude = GlobeClientState.absoluteLatitudeDegrees(
                level.getWorldBorder(),
                client.player.getZ());
        latitude$applyEwFog(fog, client.player.getX(), absoluteLatitude);
    }

    @Unique
    private static void latitude$applyEwFog(FogData fog, double x, double absoluteLatitude) {
        fog.environmentalStart = latitude$tightenStart(fog.environmentalStart, x);
        fog.renderDistanceStart = latitude$tightenStart(fog.renderDistanceStart, x);
        fog.environmentalEnd = latitude$tightenEnd(fog.environmentalEnd, x);
        fog.renderDistanceEnd = latitude$tightenEnd(fog.renderDistanceEnd, x);
        fog.skyEnd = latitude$tightenEnd(fog.skyEnd, x);
        fog.cloudEnd = latitude$tightenEnd(fog.cloudEnd, x);

        float colorIntensity = EwPresentationPolicy.sandHazeColorIntensity(
                GlobeClientState.distanceToEwBorderBlocks(x),
                GlobeClientState.ewPresentationVisibility(),
                absoluteLatitude);
        if (colorIntensity > 0.0f) {
            latitude$blendSandHazeColor(fog.color, colorIntensity);
        }
    }

    @Unique
    private static void latitude$blendSandHazeColor(Vector4f color, float intensity) {
        color.set(
                EwPresentationPolicy.blendFogColorChannel(
                        color.x(), EwPresentationPolicy.SAND_HAZE_TARGET_RED, intensity),
                EwPresentationPolicy.blendFogColorChannel(
                        color.y(), EwPresentationPolicy.SAND_HAZE_TARGET_GREEN, intensity),
                EwPresentationPolicy.blendFogColorChannel(
                        color.z(), EwPresentationPolicy.SAND_HAZE_TARGET_BLUE, intensity),
                color.w());
    }

    @Unique
    private static float latitude$tightenEnd(float currentEnd, double x) {
        double desiredEnd = GlobeClientState.computeEwFogEnd(x, currentEnd);
        if (desiredEnd < 0.0) {
            return currentEnd;
        }
        return (float) Math.min(currentEnd, desiredEnd);
    }

    @Unique
    private static float latitude$tightenStart(float currentStart, double x) {
        return Math.min(currentStart, GlobeClientState.computeEwFogStart(x, currentStart));
    }
}
