package net.saint.acclimatize.data.wind;

import java.util.Set;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;

public class WindParticleUtil {

	private static final Set<DefaultParticleType> PARTICLE_TYPES = Set.of(ParticleTypes.ASH, ParticleTypes.WHITE_ASH);
	private static final DefaultParticleType[] PARTICLE_POOL = PARTICLE_TYPES.toArray(new DefaultParticleType[0]);

	public static void renderWindParticles(MinecraftClient client) {
		var player = client.player;
		var world = client.world;

		if (player == null || world == null || ModClient.getIsPlayerInInterior()) {
			return;
		}

		var random = world.getRandom();
		var windIntensity = ModClient.getWindIntensity();
		var spawnRate = Math.max(0.0, Mod.CONFIG.windParticleSpawnRate);
		var minIntensity = Mod.CONFIG.windIntensityMin;
		var maxIntensity = Mod.CONFIG.windIntensityMax;
		var normalizedIntensity = maxIntensity > minIntensity
				? Math.min(1.0, Math.max(0.0, (windIntensity - minIntensity) / (maxIntensity - minIntensity)))
				: 1.0;

		var effectiveRate = spawnRate * normalizedIntensity;

		var spawnCount = (int) effectiveRate;
		var fractional = effectiveRate - spawnCount;

		if (random.nextDouble() < fractional) {
			spawnCount++;
		}

		if (spawnCount <= 0) {
			return;
		}

		var windDirection = ModClient.getWindDirection();
		var spawnRadius = Math.max(1, Mod.CONFIG.windParticleSpawnRadius);

		for (var i = 0; i < spawnCount; i++) {
			var spawnAngle = random.nextDouble() * Math.PI * 2.0;
			var x = player.getX() + Math.cos(spawnAngle) * spawnRadius;
			var z = player.getZ() + Math.sin(spawnAngle) * spawnRadius;
			var y = player.getBodyY(0.5) + random.nextTriangular(0, 1.5);

			var horizontalSpeed = random.nextDouble() * 0.005 + 0.001;
			var vx = -Math.sin(windDirection) * horizontalSpeed;
			var vz = Math.cos(windDirection) * horizontalSpeed;
			var vy = random.nextDouble() * 0.02 - 0.01;

			var particleType = PARTICLE_POOL[random.nextInt(PARTICLE_POOL.length)];
			world.addParticle(particleType, x, y, z, vx, vy, vz);
		}
	}

}
