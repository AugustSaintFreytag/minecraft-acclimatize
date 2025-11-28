package net.saint.acclimatize.data.space;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.data.biome.BiomeTemperatureUtil;

public final class SunShadeTemperatureUtil {

	public static double sunShadeTemperatureDelta(ServerPlayerEntity player, boolean isInInterior) {
		// If player is in interior or not exposed to sun, return 0.
		if (!isInInterior && playerIsExposedToSun(player)) {
			return 0.0;
		}

		var world = player.getWorld();
		var biomeBaseTemperature = BiomeTemperatureUtil.biomeTemperatureForPosition(world, player.getBlockPos());
		var biomeTemperatureFactor = (1 + biomeBaseTemperature / 100) * Mod.CONFIG.sunShadeBiomeTemperatureFactor;
		var sunShadeTemperatureDelta = Mod.CONFIG.sunShadeTemperatureDelta * biomeTemperatureFactor;

		return sunShadeTemperatureDelta;
	}

	public static boolean playerIsExposedToSun(ServerPlayerEntity player) {
		var world = player.getWorld();

		// Check if it's daytime (between 250 and number of ticks in daylight).
		if (!world.isDay()) {
			return false;
		}

		// Calculate sun position vector
		var sunPosition = sunPositionFromWorld(world);

		// Perform raycast from player position toward the sun
		var playerPosition = player.getPos();
		var startVector = new Vec3d(playerPosition.x, playerPosition.y + 1, playerPosition.z);
		var endVector = startVector.add(sunPosition.multiply(100));

		var hitResult = world.raycast(
				new RaycastContext(startVector, endVector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

		// Return true if ray reaches the sun without hitting any blocks
		return hitResult.getType() == HitResult.Type.MISS;
	}

	public static Vec3d sunPositionFromWorld(World world) {
		// 1) fraction [0..1) around full day
		var skyFraction = world.getSkyAngle(0.0f);

		// 2) map into [0..2π), shift so 0→–π/2 (sun below horizon)
		var angleRad = skyFraction * 2.0 * Math.PI - Math.PI * 0.5;

		// 3) compute in east–west (X) / vertical (Y) plane
		var x = Math.cos(angleRad);
		var y = Math.sin(angleRad);

		// 4) THIS VECTOR POINTS AT THE MOON, so invert for the sun:
		return new Vec3d(-x, -y, 0.0).normalize();
	}

	public static void renderSunVectorDebug(World world, Vec3d startVector, Vec3d endVector) {
		// Create a line of particles from start to end
		var steps = 40; // Number of particles along the line

		for (var i = 0; i <= steps; i++) {
			var t = (double) i / steps;
			var particlePos = startVector.lerp(endVector, t);

			// Use different particle types to distinguish the vector
			// Gold sparkle particles for the sun vector
			world.addParticle(net.minecraft.particle.ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
		}

		// Add a marker particle at the calculated sun position
		var sunMarkerPos = startVector.add(endVector.multiply(15));
		world.addParticle(net.minecraft.particle.ParticleTypes.FLAME, sunMarkerPos.x, sunMarkerPos.y, sunMarkerPos.z, 0.0, 0.05, 0.0);
	}

}
