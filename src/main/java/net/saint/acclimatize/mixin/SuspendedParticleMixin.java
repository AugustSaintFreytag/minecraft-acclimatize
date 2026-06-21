package net.saint.acclimatize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SuspendParticle;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
@Mixin(SuspendParticle.class)
public abstract class SuspendedParticleMixin extends SpriteBillboardParticle {

	// Init

	private SuspendedParticleMixin(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
		super(clientWorld, d, e, f, g, h, i);
	}

	// Logic

	@Inject(method = "move", at = @At("TAIL"))
	public void acclimatize$move(double dx, double dy, double dz, CallbackInfo callbackInfo) {
		super.move(this.velocityX, this.velocityY, this.velocityZ);
	}

}
