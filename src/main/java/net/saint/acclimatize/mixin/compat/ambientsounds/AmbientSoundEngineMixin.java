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

	private boolean assumesInterior = ModClient.getIsPlayerInInterior();

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

		var client = MinecraftClient.getInstance();
		var world = client.world;
		var player = client.player;

		var fadeTickProgress = MathUtil.clamp((float) ticksSinceLastStateChange / Mod.CONFIG.soundSuppressionTransitionTicks, 0.0f, 1.0f);
		var soundVolumeFactor = assumesInterior ? Mod.CONFIG.interiorSoundSuppressionFactor : 1.0f;
		var numberOfAdjustedSounds = new AtomicInteger(0);

		synchronized (sounds) {
			try {
				for (SoundStream sound : sounds) {
					if (WorldUtil.isInCave(world, player)) {
						// Allow full ambient sounds in caves, assume reliable detection.
						continue;
					}

					var soundVolume = getEffectiveSoundVolume(sound);
					var modifiedSoundVolume = MathUtil.lerp(soundVolume, soundVolume * soundVolumeFactor, fadeTickProgress);

					sound.generatedVoume = modifiedSoundVolume;
					numberOfAdjustedSounds.incrementAndGet();

					if (Mod.CONFIG.enableLogging && fadeTickProgress != 1.0f) {
						Mod.LOGGER.info("Transitioning volumes for ambient sound '{}' (interior: {}, progress: {}).", description,
								assumesInterior, fadeTickProgress);
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
