package net.saint.acclimatize.mixin.compat.dynamicsurroundings;

import org.orecruncher.dsurround.processing.scanner.CeilingScanner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;

@Mixin(CeilingScanner.class)
public abstract class CeilingScannerMixin {

	@Inject(method = "<init>", at = @At("TAIL"))
	private void acclimatize$init(CallbackInfo callbackInfo) {
		if (!Mod.CONFIG.enableDynamicSurroundingsInterop) {
			return;
		}

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Loaded Dynamic Surroundings compatibility layer.");
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void acclimatize$tick(CallbackInfo callbackInfo) {
		if (!Mod.CONFIG.enableDynamicSurroundingsInterop) {
			return;
		}

		callbackInfo.cancel();
	}

	@Inject(method = "isReallyInside", at = @At("HEAD"), cancellable = true)
	private void isReallyInside(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (!Mod.CONFIG.enableDynamicSurroundingsInterop) {
			return;
		}

		var isInInterior = ModClient.getIsPlayerInInterior();

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Passing interior detection result {} to Dynamic Surroundings.", isInInterior);
		}

		callbackInfo.setReturnValue(isInInterior);
	}

}
