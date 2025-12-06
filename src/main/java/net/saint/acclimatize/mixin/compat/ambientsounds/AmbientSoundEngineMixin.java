package net.saint.acclimatize.mixin.compat.ambientsounds;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.data.world.WorldUtil;
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

	private boolean hasSuppressedSounds = ModClient.getIsPlayerInInterior();

	// Tick

	@Inject(method = "tick", at = @At("TAIL"), remap = false)
	private void acclimatize$tick(CallbackInfo callbackInfo) {
		if (!Mod.CONFIG.enableAmbientSoundsInterop) {
			return;
		}

		var client = MinecraftClient.getInstance();
		var world = client.world;
		var player = client.player;

		var isInInterior = ModClient.getIsPlayerInInterior();
		var isInCave = WorldUtil.isInCave(world, player);

		var shouldSuppressSounds = isInInterior && !isInCave;

		if (shouldSuppressSounds != hasSuppressedSounds) {
			hasSuppressedSounds = shouldSuppressSounds;
			ticksSinceLastStateChange = 0;
		} else {
			ticksSinceLastStateChange++;
		}

		var fadeTickProgress = MathUtil.clamp((float) ticksSinceLastStateChange / Mod.CONFIG.soundSuppressionTransitionTicks, 0.0f, 1.0f);
		var soundVolumeFactor = hasSuppressedSounds && !isInCave ? Mod.CONFIG.interiorSoundSuppressionFactor : 1.0f;
		var numberOfAdjustedSounds = new AtomicInteger(0);

		synchronized (sounds) {
			try {
				for (SoundStream sound : sounds) {
					var soundVolume = getEffectiveSoundVolume(sound);
					var modifiedSoundVolume = MathUtil.lerp(soundVolume, soundVolume * soundVolumeFactor, fadeTickProgress);

					sound.generatedVoume = modifiedSoundVolume;
					numberOfAdjustedSounds.incrementAndGet();

					if (Mod.CONFIG.enableLogging && fadeTickProgress != 1.0f) {
						Mod.LOGGER.info("Transitioning volumes for ambient sound (interior: {}, cave: {}, progress: {}).",
								hasSuppressedSounds, isInCave, fadeTickProgress);
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
