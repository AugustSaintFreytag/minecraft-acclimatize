package net.saint.acclimatize.data.wind;

import java.util.HashMap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.saint.acclimatize.Mod;

public class BiomeWindUtil {

	// Configuration

	private static final HashMap<String, Double> windFactorByBiomeId = new HashMap<String, Double>() {
		{
			put("minecraft:frozen_peaks", 1.4);
			put("minecraft:jagged_peaks", 1.4);
			put("minecraft:stony_peaks", 1.4);
			put("minecraft:cherry_grove", 1.3);
			put("minecraft:grove", 1.3);
			put("minecraft:windswept_hills", 1.3);
			put("minecraft:windswept_gravelly_hills", 1.3);
			put("minecraft:windswept_forest", 1.2);
			put("minecraft:jungle", 0.8);
			put("minecraft:sparse_jungle", 0.85);
			put("minecraft:bamboo_jungle", 0.75);
			put("minecraft:swamp", 0.75);
			put("minecraft:mangrove_swamp", 0.75);
		}
	};

	// Wind

	public static double positionalWindFactorForPlayer(PlayerEntity player) {
		var world = player.getWorld();
		var position = player.getBlockPos();

		return positionalWindFactorForBiome(world, position);
	}

	public static double positionalWindFactorForBiome(World world, BlockPos position) {
		if (!world.getDimension().natural()) {
			return 0.0;
		}

		var biomeEntry = world.getBiome(position);
		var biomeWindFactor = baseWindFactorForBiome(biomeEntry);
		var altitudeWindFactor = windFactorForAltitude(position.getY());

		var aggregateWindFactor = biomeWindFactor * altitudeWindFactor;

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Determined wind factor {} for player at position {}.", aggregateWindFactor, position);
		}

		return aggregateWindFactor;
	}

	private static double baseWindFactorForBiome(RegistryEntry<Biome> biomeEntry) {
		var biomeId = biomeEntry.getKey().get().getValue().toString();
		var windFactor = windFactorByBiomeId.getOrDefault(biomeId, 1.0);

		return windFactor;
	}

	private static double windFactorForAltitude(double altitude) {
		var anchorAltitude = (double) Mod.CONFIG.altitudeZeroingAnchor;
		var maximumAltitude = (double) Mod.CONFIG.windAltitudeMax;
		var minimumAltitude = 30.0;

		// Lower Depths
		if (altitude <= minimumAltitude) {
			return 0.0;
		}

		// Higher Highs
		if (altitude >= maximumAltitude) {
			return Mod.CONFIG.windAltitudeFactor;
		}

		// Below Ground
		if (altitude <= anchorAltitude) {
			var belowAnchorFactor = 1 - (altitude - minimumAltitude) / (anchorAltitude - minimumAltitude);
			// Start from baseline 100%, decrease to 0% below ground, no altitude factor applied.
			return belowAnchorFactor;
		}

		// Above Ground
		var aboveAnchorFactor = (altitude - anchorAltitude) / (maximumAltitude - anchorAltitude);
		// Start from baseline 100%, increase to altitude factor at max altitude.
		return aboveAnchorFactor * Mod.CONFIG.windAltitudeFactor;
	}

}
