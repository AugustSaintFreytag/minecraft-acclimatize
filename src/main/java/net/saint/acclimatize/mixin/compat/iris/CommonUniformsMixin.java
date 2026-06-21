package net.saint.acclimatize.mixin.compat.iris;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.saint.acclimatize.ModClient;

@Mixin(CommonUniforms.class)
public abstract class CommonUniformsMixin {

	@Inject(method = "addNonDynamicUniforms", at = @At("TAIL"), remap = false)
	private static void acclimatize$generalCommonUniforms(UniformHolder uniforms, IdMap idMap, PackDirectives directives,
			FrameUpdateNotifier updateNotifier, CallbackInfo callbackInfo) {
		// Add client-cached wind intensity and direction as uniforms for shaders.
		acclimatize$addWindUniforms(uniforms);
	}

	private static void acclimatize$addWindUniforms(UniformHolder uniforms) {
		uniforms.uniform1f(PER_TICK, "acclimatize_windDirection", () -> {
			return ModClient.getLocalWindDirection();
		});

		uniforms.uniform1f(PER_TICK, "acclimatize_windIntensity", () -> {
			return ModClient.getLocalWindIntensity();
		});
	}

}
