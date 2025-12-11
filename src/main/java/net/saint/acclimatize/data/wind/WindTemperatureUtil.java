package net.saint.acclimatize.data.wind;

import net.minecraft.server.network.ServerPlayerEntity;
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

		// Wind Temperature

		var windManager = Mod.PLAYER_WIND_MANAGER.getManagerForPlayer(player);
		var windIntensity = windManager.getWindIntensity();
		var windTemperature = windIntensity * Mod.CONFIG.windChillFactor;

		// Finalize

		return windTemperature;
	}

}
