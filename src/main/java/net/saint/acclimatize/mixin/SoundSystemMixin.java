package net.saint.acclimatize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.saint.acclimatize.Mod;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {

	@Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
	private void acclimatize$play(SoundInstance soundInstance, CallbackInfo callbackInfo) {
		if (!Mod.CONFIG.enableRainSounds) {
			return;
		}

		if (soundInstance.getCategory() != SoundCategory.WEATHER) {
			return;
		}

		var soundId = soundInstance.getId();

		if (soundId.equals(SoundEvents.WEATHER_RAIN.getId()) || soundId.equals(SoundEvents.WEATHER_RAIN_ABOVE.getId())) {
			callbackInfo.cancel();
		}
	}

}
