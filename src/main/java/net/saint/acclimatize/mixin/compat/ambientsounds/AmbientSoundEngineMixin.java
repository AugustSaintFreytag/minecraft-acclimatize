package net.saint.acclimatize.mixin.compat.ambientsounds;

import java.util.ConcurrentModificationException;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.util.MathUtil;
import team.creative.ambientsounds.sound.AmbientSound.SoundStream;
import team.creative.ambientsounds.sound.AmbientSoundEngine;

@Mixin(AmbientSoundEngine.class)
public abstract class AmbientSoundEngineMixin {

	// Properties (Shadowed)

	@Shadow
	private List<SoundStream> sounds;

	// State

	private long ticksSinceLastStateChange = 0;

	private boolean assumesInterior = false;

	// Tick

	@Inject(method = "tick", at = @At("TAIL"), remap = false)
	private void acclimatize$tick(CallbackInfo callbackInfo) {
		if (!Mod.CONFIG.enableAmbientSoundsInterop) {
			return;
		}

		if (ModClient.getIsPlayerInInterior() != assumesInterior) {
			assumesInterior = ModClient.getIsPlayerInInterior();
			ticksSinceLastStateChange = 0;
		} else {
			ticksSinceLastStateChange++;
		}

		var fadeTickProgress = MathUtil.clamp((float) ticksSinceLastStateChange / Mod.CONFIG.soundSuppressionTransitionTicks, 0.0f, 1.0f);

		synchronized (sounds) {
			try {
				for (SoundStream sound : sounds) {
					var soundVolume = getEffectiveSoundVolume(sound);
					var soundVolumeFactor = assumesInterior ? Mod.CONFIG.interiorSoundSuppressionFactor : 1.0f;
					var modifiedSoundVolume = MathUtil.lerp(soundVolume, soundVolume * soundVolumeFactor, fadeTickProgress);

					sound.generatedVoume = modifiedSoundVolume;

					if (Mod.CONFIG.enableLogging && soundVolume != modifiedSoundVolume && fadeTickProgress != 1.0f
							&& ticksSinceLastStateChange % 20 == 0) {
						Mod.LOGGER.info(
								"Adjusted Ambient Sounds mod sound volume: originalVolume={}, adjustedVolume={}, isInterior={}, fadeTickProgress={}",
								soundVolume, modifiedSoundVolume, assumesInterior, fadeTickProgress);
					}
				}
			} catch (ConcurrentModificationException e) {
				Mod.LOGGER.warn("Concurrent modification detected when adjusting ambient sound volumes for interior suppression.", e);
			}
		}
	}

	private float getEffectiveSoundVolume(SoundStream sound) {
		// Property name sic, as it is written in Ambient Sounds.
		return sound.generatedVoume;
	}

}
