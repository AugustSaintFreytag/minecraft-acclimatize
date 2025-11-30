package net.saint.acclimatize.mixin.compat.dynamicsurroundings;

import org.orecruncher.dsurround.sound.SoundVolumeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;

@Mixin(SoundVolumeEvaluator.class)
public abstract class SoundVolumeEvaluatorMixin {

	private static int FADE_IN_DELAY = 80; // 4 seconds
	private static int FADE_IN_LENGTH = 60; // 3 seconds

	@Inject(method = "getAdjustedVolume", at = @At("RETURN"), cancellable = true, remap = false)
	private static void acclimatize$getAdjustedVolume(SoundInstance sound, CallbackInfoReturnable<Float> callbackInfo) {
		if (!Mod.CONFIG.enableDynamicSurroundingsInterop || !shouldModifyVolume()) {
			return;
		}

		var volume = callbackInfo.getReturnValue();
		var tick = ModClient.getClientTickElapsed();
		var modifier = volumeModifierForTick(tick);
		var modifiedVolume = volume * modifier;

		callbackInfo.setReturnValue(modifiedVolume);
	}

	private static float volumeModifierForTick(long tick) {
		if (tick <= FADE_IN_DELAY) {
			return 0.0f;
		} else if (tick <= FADE_IN_DELAY + FADE_IN_LENGTH) {
			var progress = (float) (tick - FADE_IN_DELAY) / (float) FADE_IN_LENGTH;
			return progress;
		} else {
			return 1.0f;
		}
	}

	private static boolean shouldModifyVolume() {
		var client = MinecraftClient.getInstance();

		if (client != null && client.world != null && client.player != null) {
			return true;
		}

		return false;
	}

}
