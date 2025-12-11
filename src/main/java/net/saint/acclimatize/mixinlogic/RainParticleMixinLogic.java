package net.saint.acclimatize.mixinlogic;

import net.minecraft.util.math.Vec3d;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.util.MathUtil;
import pigcart.particlerain.particle.RainParticle;

public interface RainParticleMixinLogic {

	public static class ValueTuple {
		public double velocityX;
		public double velocityZ;
		public double angle;

		public ValueTuple(double velocityX, double velocityY, double angle) {
			this.velocityX = velocityX;
			this.velocityZ = velocityY;
			this.angle = angle;
		}
	}

	public default ValueTuple windAffectedVelocityForParticle(RainParticle particle, Vec3d velocity) {
		// Fetch our mod's wind parameters
		double windDirection = ModClient.getLocalWindDirection(); // radians, 0 = north→south
		double windIntensity = ModClient.getEffectiveWindIntensity();
		double effectFactor = Mod.CONFIG.particleWeatherEffectFactor;

		// Build a horizontal vector so 0 rad → +Z
		double speed = effectFactor * windIntensity;
		double vx = MathUtil.sin(windDirection) * speed;
		double vz = MathUtil.cos(windDirection) * speed;

		// Clamp so the slant angle never exceeds maxAngle (e.g. 45°)
		double maxAngleRad = Math.toRadians(Mod.CONFIG.particleRainMaxAngle);
		double vertical = Math.abs(velocity.y);
		double maxH = vertical * MathUtil.tan(maxAngleRad);
		double hLen = Math.hypot(vx, vz);

		if (hLen > maxH) {
			double scale = maxH / hLen;
			vx *= scale;
			vz *= scale;
		}

		// Quad yaw: rotate particle‐quad to face *into* the wind
		double angle = windDirection;

		return new ValueTuple(vx, vz, angle);
	}

}
