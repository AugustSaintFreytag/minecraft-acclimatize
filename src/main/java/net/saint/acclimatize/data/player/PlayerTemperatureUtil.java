package net.saint.acclimatize.data.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.data.biome.BiomeTemperatureUtil;
import net.saint.acclimatize.data.block.BlockTemperatureUtil;
import net.saint.acclimatize.data.item.ItemTemperatureUtil;
import net.saint.acclimatize.data.space.SpaceUtil;
import net.saint.acclimatize.data.space.SunShadeTemperatureUtil;
import net.saint.acclimatize.data.wind.WindTemperatureUtil;
import net.saint.acclimatize.player.PlayerState;
import net.saint.acclimatize.server.ServerState;
import net.saint.acclimatize.util.MathUtil;

public class PlayerTemperatureUtil {

	public static void tickPlayerTemperatureInSchedule(ServerPlayerEntity player, ServerState serverState, PlayerState playerState) {
		var world = player.getWorld();

		if (world.getTimeOfDay() % Mod.CONFIG.temperatureTickInterval != 0) {
			return;
		}

		tickPlayerTemperature(player, serverState, playerState);
	}

	public static void tickPlayerTemperature(ServerPlayerEntity player, ServerState serverState, PlayerState playerState) {
		// Prerequisites

		var bodyTemperature = playerState.bodyTemperature;
		var isInInterior = SpaceUtil.checkPlayerIsInInterior(player);

		// Biome Temperature

		var biomeTemperature = BiomeTemperatureUtil.biomeTemperatureForPlayer(player, isInInterior);
		var effectiveTemperature = biomeTemperature;

		// Shade Temperature

		var sunShadeTemperature = SunShadeTemperatureUtil.sunShadeTemperatureDelta(player, isInInterior);
		effectiveTemperature += sunShadeTemperature;

		// Block Temperature (Heating & Cooling)

		var blockTemperatureDelta = BlockTemperatureUtil.temperatureDeltaForBlocksInVicinity(player);
		effectiveTemperature += blockTemperatureDelta;

		// Wind

		var windTemperatureDelta = WindTemperatureUtil.windTemperatureForEnvironment(serverState, player, isInInterior);
		effectiveTemperature += windTemperatureDelta;

		// Item Temperature (Wearables)

		var itemTemperatureDelta = ItemTemperatureUtil.temperatureDeltaForAllArmorItems(player);
		effectiveTemperature += itemTemperatureDelta;

		// Effects

		var effectsTemperatureDelta = PlayerStatusEffectsTemperatureUtil.temperatureDeltaForItemsAndStatusEffects(player,
				effectiveTemperature);
		effectiveTemperature += effectsTemperatureDelta;

		// Acclimatization Rate

		var acclimatizationRate = Mod.CONFIG.acclimatizationRate;

		if (blockTemperatureDelta > Mod.CONFIG.blockTemperatureAcclimatizationBoostThreshold
				|| blockTemperatureDelta < -Mod.CONFIG.blockTemperatureAcclimatizationBoostThreshold) {
			// Boost acclimatization when excessively heated or cooled by blocks.
			acclimatizationRate *= Mod.CONFIG.blockAcclimatizationBoostFactor;
		}

		if (player.isWet()) {
			// Increase acclimatization rate when wet.
			acclimatizationRate *= Mod.CONFIG.wetAcclimatizationRateBoostFactor;
		}

		// Worn Items

		acclimatizationRate += ItemTemperatureUtil.acclimatizationRateDeltaForItemTemperature(itemTemperatureDelta);

		// Player Temperature

		acclimatizationRate = applicableAcclimatizationRate(acclimatizationRate);
		bodyTemperature = (1 - acclimatizationRate) * bodyTemperature + acclimatizationRate * effectiveTemperature;

		// State

		playerState.isInInterior = isInInterior;
		playerState.acclimatizationRate = acclimatizationRate;
		playerState.bodyTemperature = bodyTemperature;
		playerState.ambientTemperature = effectiveTemperature;
		playerState.biomeTemperature = biomeTemperature;
		playerState.sunShadeTemperature = sunShadeTemperature;
		playerState.windTemperature = windTemperatureDelta;
		playerState.blockTemperature = blockTemperatureDelta;
		playerState.itemTemperature = itemTemperatureDelta;

		playerState.markDirty();
	}

	// Utility

	public static double applicableAcclimatizationRate(double acclimatizationRate) {
		return MathUtil.clamp(acclimatizationRate, Mod.CONFIG.acclimatizationRateMinimum, 1.0);
	}

}
