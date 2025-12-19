package net.saint.acclimatize.data.wind;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.ModParticles;
import net.saint.acclimatize.util.MathUtil;

public class WindParticleUtil {

	private static final double PARTICLE_BASE_SPAWN_RATE = 4.0;

	// Spawning

	public static void spawnWindParticles(MinecraftClient client) {
		var player = client.player;
		var world = client.world;

		if (player == null || world == null || player.isSubmergedInWater() || ModClient.getIsPlayerInInterior()) {
			return;
		}

		var spawnCount = calculateNumberOfParticlesForTick(world);

		if (spawnCount <= 0) {
			return;
		}

		for (var i = 0; i < spawnCount; i++) {
			spawnWindParticle(world, player);
		}
	}

	private static void spawnWindParticle(World world, PlayerEntity entity) {
		var random = world.getRandom();

		var windDirection = ModClient.getLocalWindDirection();
		var spawnRadius = Math.max(1, Mod.CONFIG.windParticleSpawnRadius);
		var spawnAngle = random.nextDouble() * Math.PI * 2.0;

		var x = entity.getX() + Math.cos(spawnAngle) * spawnRadius;
		var z = entity.getZ() + Math.sin(spawnAngle) * spawnRadius;
		var y = entity.getBodyY(0.5) + random.nextTriangular(0, 1.5);

		var horizontalSpeed = random.nextDouble() * 0.005 + 0.001;
		var vx = -Math.sin(windDirection) * horizontalSpeed;
		var vz = Math.cos(windDirection) * horizontalSpeed;
		var vy = random.nextDouble() * 0.02 - 0.01;

		world.addParticle(ModParticles.WIND_FLAKE, x, y, z, vx, vy, vz);
	}

	// Spawn Rates & Counts

	private static int calculateNumberOfParticlesForTick(World world) {
		var random = world.getRandom();

		var spawnRate = calculateEffectiveParticleSpawnRate(world);
		var spawnCount = (int) spawnRate;
		var fractional = spawnRate - spawnCount;

		if (random.nextDouble() < fractional) {
			spawnCount++;
		}

		return spawnCount;
	}

	private static double calculateEffectiveParticleSpawnRate(World world) {
		var windIntensity = ModClient.getLocalWindIntensity();
		var spawnRate = Math.max(0.0, Mod.CONFIG.windParticleSpawnRate);
		var minIntensity = Mod.CONFIG.windIntensityMin;
		var maxIntensity = Mod.CONFIG.windIntensityMax;
		var normalizedIntensity = MathUtil.clamp((windIntensity - minIntensity) / (maxIntensity - minIntensity), 0.0, 1.0);

		var effectiveRate = PARTICLE_BASE_SPAWN_RATE * spawnRate * normalizedIntensity;

		if (world.isRaining()) {
			effectiveRate *= 1.5;
		}

		return effectiveRate;
	}

}
