package net.saint.acclimatize.data.wind;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.biome.Biome;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.server.ServerState;

public final class WindTemperatureUtil {

	// Configuration

	private static final int WIND_LOW_ELEVATION_CUTOFF = 20;

	// Wind Effects

	public static double windTemperatureForEnvironment(ServerState serverState, ServerPlayerEntity player, boolean isInInterior) {
		var world = player.getWorld();
		var dimension = world.getDimension();

		if (!Mod.CONFIG.enableWind || isInInterior || player.isSubmergedInWater() || !dimension.natural()) {
			Mod.PLAYER_WIND_MANAGER.clearManagerForPlayer(player);
			return 0.0;
		}

		if (player.getBlockPos().getY() <= WIND_LOW_ELEVATION_CUTOFF) {
			Mod.PLAYER_WIND_MANAGER.clearManagerForPlayer(player);
			return 0.0;
		}

		// Base Wind Temperature

		var windTemperature = serverState.windIntensity * Mod.CONFIG.windChillFactor;

		// Elevation Wind Factor

		var elevationWindFactor = elevationWindFactorForPlayer(player);
		windTemperature *= elevationWindFactor;

		// Precipitation Wind Chill

		var precipitationWindFactor = precipitationWindFactorForPlayer(serverState, player);
		windTemperature *= precipitationWindFactor;

		// Wind Raycast Hit Factor

		var windManager = Mod.PLAYER_WIND_MANAGER.getManagerForPlayer(player);
		var numberOfUnblockedRays = windManager.getUnblockedWindRaysForPlayer(serverState, player);
		var numberOfRaysFired = windManager.getNumberOfRaysFired();
		var windHitTemperatureFactor = ((double) numberOfUnblockedRays / (double) Math.max(numberOfRaysFired, 1));

		windTemperature *= windHitTemperatureFactor;

		return windTemperature;
	}

	private static double elevationWindFactorForPlayer(ServerPlayerEntity player) {
		var positionY = player.getBlockPos().getY();

		if (positionY <= 20) {
			return 0.0;
		}

		if (positionY <= 60) {
			return (positionY - 20) / 40.0;
		}

		if (positionY <= 100) {
			return 1.0 + (positionY - 60) / 80.0;
		}

		return 1.5;
	}

	private static double precipitationWindFactorForPlayer(ServerState serverState, ServerPlayerEntity player) {
		var world = player.getWorld();
		var position = player.getBlockPos();
		var biome = world.getBiome(position).value();
		var precipitation = biome.getPrecipitation(position);

		if (precipitation == Biome.Precipitation.RAIN) {
			if (world.isThundering()) {
				return 1.3;
			} else {
				return 1.1;
			}
		} else if (precipitation == Biome.Precipitation.SNOW) {
			if (world.isRaining()) {
				return 1.2;
			}
		}

		return 1.0;
	}

}
