package com.example.globe.mixin.client;

import com.example.globe.client.LatitudeClientState;
import com.example.globe.client.create.RecreatedWorldMetadata;
import com.example.globe.util.LatitudeBands;
import java.nio.file.Path;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Activates the Latitude loading overlay before vanilla shows its own first screen for a resumed
 * world, not after.
 *
 * <p>The first action of the flow that opens a saved world — before any file is even opened — is
 * to put a plain message screen on screen. Only much
 * later, after the save's registries and world stem are fully resolved, does
 * {@code Minecraft.doWorldLoad} run — the point {@link MinecraftClientStartIntegratedMixin} already
 * hooks to activate the overlay, because that is the earliest point a live {@code WorldStem} exists
 * to confirm "is this actually a Latitude world." Between those two points, every resumed world
 * shows several seconds of unbranded vanilla screens before the swap: exactly the "vanilla first,
 * then bespoke" Maintainer reported live on a reload, which the earlier hook could not have prevented no
 * matter how early inside {@code doWorldLoad} it ran, because {@code doWorldLoad} itself starts
 * well after the flash-prone screens are already up.
 *
 * <p>{@link RecreatedWorldMetadata} already solves the "identify a Latitude world before it loads"
 * problem for a different purpose — reading {@code latitude_world_state.dat} straight off disk to
 * recover a save's identity and last-known band before starting a server. This applies the same
 * read at the true first opportunity: the head of the flow's own entry point, using only the level
 * id and the save-directory resolve (a plain path resolve, no I/O) to find the save without needing
 * an opened {@code LevelStorageAccess} at all.
 *
 * <p>Fails soft like every other overlay hook in this lifecycle: a missing target, an unreadable or
 * absent save file, or any other exception here just means the overlay activates at its old, later
 * point instead — never a crash, never a wrongly-shown overlay for a real vanilla world (the
 * doWorldLoad hook's own {@code globe$isLatitudeWorld} check still runs afterward and clears the
 * flag if this guessed wrong).
 */
@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsEarlyLatitudeActivationMixin {
    @Unique private static final Logger GLOBE_LOGGER = LoggerFactory.getLogger("LatitudeLoadingOverlay");

    @Shadow
    private LevelStorageSource levelSource;

    /**
     * The level id this class has already acted on for a load that is still in flight. Only one of
     * the two injectors below can match on a given Minecraft version, but the comparison makes a
     * double-apply a no-op — and it re-arms of its own accord, because the loading flag is cleared
     * when a load ends, so re-entering the same world later still activates the overlay.
     */
    @Unique
    private static String globe$activatedLevelId;

    // The entry point renamed inside the supported range: the two oldest versions take the screen
    // to return to alongside the level id, the two newest take the level id and a failure callback.
    // Both shapes carry an explicit descriptor and require = 0, so whichever one this version
    // declares is the one that applies and the other is simply skipped.
    //
    // The 1.20.3/1.20.4 entry point is also spelled in intermediary form: this jar is remapped
    // with 1.20.1's mappings, which do not know checkForBackupAndLoad, so the named selector
    // shipped unremapped and never matched at runtime. The named form stays for the target
    // verifier; the intermediary form is what production matches on the two newest versions.
    @Inject(
            method = "loadLevel(Lnet/minecraft/client/gui/screens/Screen;Ljava/lang/String;)V",
            at = @At("HEAD"),
            require = 0,
            expect = 0)
    private void globe$activateEarlyBeforeLevelLoad(Screen lastScreen, String levelId, CallbackInfo ci) {
        globe$activateEarlyForResumedLatitudeWorld(levelId);
    }

    @Inject(
            method = {
                    "checkForBackupAndLoad(Ljava/lang/String;Ljava/lang/Runnable;)V",
                    "method_54618(Ljava/lang/String;Ljava/lang/Runnable;)V"
            },
            at = @At("HEAD"),
            require = 0,
            expect = 0)
    private void globe$activateEarlyBeforeBackupCheck(String levelId, Runnable onFail, CallbackInfo ci) {
        globe$activateEarlyForResumedLatitudeWorld(levelId);
    }

    @Unique
    private void globe$activateEarlyForResumedLatitudeWorld(String levelId) {
        if (levelId != null && levelId.equals(globe$activatedLevelId)
                && LatitudeClientState.isLatitudeWorldLoading()) {
            return;
        }
        try {
            // Vanilla's own one-line "save directory for this id" resolve is not public on every
            // version in the supported range, so the same resolve is spelled out here: the same
            // base directory, the same single path resolve, and still no file touched.
            Path worldRoot = levelSource.getBaseDir().resolve(levelId);
            String presetId = RecreatedWorldMetadata.latitudePresetId(worldRoot);
            if (presetId == null) {
                return;
            }
            globe$activatedLevelId = levelId;
            LatitudeClientState.beginExpedition(System.currentTimeMillis());
            LatitudeClientState.activateLatitudeLoading();
            LatitudeBands.Band band =
                    LatitudeBands.fromCanonicalId(RecreatedWorldMetadata.lastKnownBandId(worldRoot));
            if (band != null) {
                LatitudeClientState.setLoadingZoneLabel(band.displayName());
            }
        } catch (Exception e) {
            GLOBE_LOGGER.warn(
                    "[Latitude] could not pre-check saved world for early overlay "
                            + "activation; overlay will activate at its later, doWorldLoad fallback point",
                    e);
        }
    }
}
