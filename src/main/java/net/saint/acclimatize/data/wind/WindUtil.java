package net.saint.acclimatize.data.wind;

import net.minecraft.server.world.ServerWorld;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.server.ServerState;

public final class WindUtil {

	// Configuration

	private static final double WIND_INTERVAL_JITTER_FACTOR = 0.15;

	// Wind Override

	public static void overrideWind(ServerState serverState, double windDirection, double windIntensity) {
		serverState.windDirection = windDirection;
		serverState.windIntensity = windIntensity;
	}

	// Wind Tick

	public static void tickWindInSchedule(ServerWorld world, ServerState serverState) {
		var random = world.getRandom();
		var serverTick = world.getTimeOfDay();

		if (serverTick > serverState.nextWindIntensityTick) {
			tickWindIntensity(world, serverState);

			var intervalJitter = (int) (WIND_INTERVAL_JITTER_FACTOR * Mod.CONFIG.windIntensityUpdateInterval);
			serverState.nextWindIntensityTick = serverTick + Mod.CONFIG.windIntensityUpdateInterval
					+ random.nextBetween(-intervalJitter, intervalJitter);

			if (Mod.CONFIG.enableLogging) {
				Mod.LOGGER.info("Randomizing new wind intensity via tick at " + serverTick + ", next scheduled for "
						+ serverState.nextWindIntensityTick + ".");
			}

		}

		if (serverTick > serverState.nextWindDirectionTick) {
			tickWindDirection(world, serverState);

			var intervalJitter = (int) (WIND_INTERVAL_JITTER_FACTOR * Mod.CONFIG.windDirectionUpdateInterval);
			serverState.nextWindDirectionTick = serverTick + Mod.CONFIG.windDirectionUpdateInterval
					+ random.nextBetween(-intervalJitter, intervalJitter);

			if (Mod.CONFIG.enableLogging) {
				Mod.LOGGER.info("Randomizing new wind direction via tick at " + serverTick + ", next scheduled for "
						+ serverState.nextWindDirectionTick + ".");
			}
		}
	}

	public static void tickWindDirectionAndIntensity(ServerWorld world, ServerState serverState) {
		tickWindDirection(world, serverState);
		tickWindIntensity(world, serverState);
	}

	private static void tickWindDirection(ServerWorld world, ServerState serverState) {
		var currentWindDirection = serverState.windDirection;

		var random = world.getRandom();
		var randomWindOffset = Math.toRadians(random.nextDouble() * 110.0 - 55.0);

		serverState.windDirection = wrapRadians(currentWindDirection + randomWindOffset);
		serverState.markDirty();
	}

	private static void tickWindIntensity(ServerWorld world, ServerState serverState) {
		var random = world.getRandom();
		var min = Mod.CONFIG.windIntensityMin;
		var max = Mod.CONFIG.windIntensityMax;
		var windIntensity = random.nextDouble() * (max - min) + min;

		if (world.isThundering()) {
			windIntensity *= 2.0;
		} else if (world.isRaining()) {
			windIntensity *= 1.65;
		}

		serverState.windIntensity = Math.max(0, windIntensity);
		serverState.markDirty();
	}

	private static double wrapRadians(double angle) {
		var wrapped = angle % (Math.PI * 2.0);

		if (wrapped < 0) {
			wrapped += Math.PI * 2.0;
		}

		return wrapped;
	}
}
