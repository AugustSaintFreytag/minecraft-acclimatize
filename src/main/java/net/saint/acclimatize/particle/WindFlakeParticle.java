package net.saint.acclimatize.particle;

import me.shedaniel.math.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.random.Random;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;

@Environment(EnvType.CLIENT)
public class WindFlakeParticle extends SpriteBillboardParticle {

	// References

	private final SpriteProvider spriteProvider;

	// Init

	protected WindFlakeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
			SpriteProvider spriteProvider) {
		super(world, x, y, z, 0.0, 0.0, 0.0);

		this.spriteProvider = spriteProvider;
		this.setSpriteForAge(spriteProvider);

		var color = getRandomParticleColorForTemperature(random, ModClient.getAmbientTemperature());

		this.setMaxAge(80);
		this.scale(0.8f + this.random.nextFloat() * 0.1f);
		this.setColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);

		this.gravityStrength = 0.01f;
		this.velocityMultiplier = 1.0F;
		this.collidesWithWorld = Mod.CONFIG.enableWindParticleCollision;

		this.velocityX += velocityX;
		this.velocityY += velocityY;
		this.velocityZ += velocityZ;

		this.setSpriteForAge(spriteProvider);
	}

	// Factory

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<DefaultParticleType> {
		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f,
				double g, double h, double i) {
			return new WindFlakeParticle(clientWorld, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
		}
	}

	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
	}

	// Ticking

	@Override
	public void tick() {
		super.tick();
		this.setSpriteForAge(spriteProvider);
	}

	// Utility

	private static Color getRandomParticleColorForTemperature(Random random, double biomeTemperature) {
		var temperatureMax = 100.0;
		var temperaturePivot = 50.0;
		var temperatureFactor = ((temperaturePivot - biomeTemperature) / temperatureMax) * (temperatureMax / temperaturePivot);
		var temperatureSaturationFactor = (float) Math.abs(temperatureFactor) * getSaturationFactorForTemperature(biomeTemperature);

		var hue = getColorHueForTemperature(biomeTemperature);
		var saturation = 0.0F + 0.5F * temperatureSaturationFactor * (0.5F + 0.5F * random.nextFloat());
		var brightness = 0.4F + 0.45F * random.nextFloat();

		var colorValue = Color.HSBtoRGB(hue, saturation, brightness);

		return Color.ofOpaque(colorValue);
	}

	private static float getColorHueForTemperature(double biomeTemperature) {
		if (biomeTemperature > 50) {
			return 0.115F;
		} else {
			return 0.525F;
		}
	}

	private static float getSaturationFactorForTemperature(double biomeTemperature) {

		if (biomeTemperature > 50) {
			return 1.0F;
		} else {
			return 0.5F;
		}
	}

}
