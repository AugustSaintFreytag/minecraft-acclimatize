package net.saint.acclimatize.mixin.compat.atmosfera;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableList;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSound;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundHandler;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundInstance;
import net.minecraft.client.MinecraftClient;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.mixin.AbstractSoundInstanceAccessor;
import net.saint.acclimatize.util.MathUtil;

@Mixin(AtmosphericSoundHandler.class)
public abstract class AtmosphericSoundHandlerMixin {

	// Properties (Shadows)

	@Shadow
	@Final
	private ImmutableList<AtmosphericSound> sounds;

	@Shadow
	@Final
	private ImmutableList<AtmosphericSound> musics;

	@Shadow
	@Final
	private Map<AtmosphericSound, AtmosphericSoundInstance> playingSounds;

	// State

	private long ticksSinceLastStateChange = 0;

	private boolean assumesInterior = false;

	// Tick

	@Inject(method = "tick", at = @At("TAIL"), remap = false)
	private void acclimatize$tick(CallbackInfo callbackInfo) {
		if (!Mod.CONFIG.enableAtmosferaInterop) {
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

		if (world == null) {
			return;
		}

		var fadeTickProgress = MathUtil.clamp((float) ticksSinceLastStateChange / Mod.CONFIG.soundSuppressionTransitionTicks, 0.0f, 1.0f);
		var soundVolumeFactor = assumesInterior ? Mod.CONFIG.interiorSoundSuppressionFactor : 1.0f;
		var numberOfAdjustedSounds = new AtomicInteger(0);

		this.playingSounds.values().removeIf(AtmosphericSoundInstance::isDone);
		this.playingSounds.forEach((sound, soundInstance) -> {
			var soundVolume = sound.getVolume(world);
			var modifiedSoundVolume = MathUtil.lerp(soundVolume, soundVolume * soundVolumeFactor, fadeTickProgress);

			((AbstractSoundInstanceAccessor) soundInstance).acclimatize$setVolume(modifiedSoundVolume);
			numberOfAdjustedSounds.incrementAndGet();
		});

		if (Mod.CONFIG.enableLogging && fadeTickProgress != 1.0f && ticksSinceLastStateChange % 10 == 0) {
			Mod.LOGGER.info("Transitioning volumes for {} atmosfera sound(s) (interior: {}, progress: {}).", numberOfAdjustedSounds.get(),
					assumesInterior, fadeTickProgress);
		}
	}

}
