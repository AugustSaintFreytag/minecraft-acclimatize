package net.saint.acclimatize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.hud.TemperatureHudOverlayUtil;
import net.saint.acclimatize.hud.TemperatureHudUtil;

@Environment(EnvType.CLIENT) @Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 1))
	private void mixinRenderStatusBars(DrawContext context, CallbackInfo callbackInfo) {
		if (!ModClient.enableHUD) {
			return;
		}

		var client = MinecraftClient.getInstance();
		var player = client.player;

		if (Mod.CONFIG.hideTemperatureWhileInVehicle && player.hasVehicle()) {
			return;
		}

		if (Mod.CONFIG.temperatureDisplayType.equals("gauge")) {
			TemperatureHudUtil.renderGaugeThermometerHud(context);
			return;
		}

		if (Mod.CONFIG.temperatureDisplayType.equals("glass_thermometer")) {
			TemperatureHudUtil.renderGlassThermometerHud(context);
			return;
		}
	}

	@Inject(method = "renderVignetteOverlay", at = @At(value = "HEAD"))
	private void mixinRenderVignetteOverlay(DrawContext context, Entity entity, CallbackInfo callbackInfo) {
		TemperatureHudOverlayUtil.renderVignetteHudOverlayIfNeeded(context, entity);
	}
}