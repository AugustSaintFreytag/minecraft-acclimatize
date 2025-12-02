package net.saint.acclimatize.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public final class WindBiomeKindUtil {

	// Configuration

	private static final double SNOW_TEMPERATURE_THRESHOLD = 0.15;

	// Kind

	public static AmbienceBiomeKind kindForPlayer(MinecraftClient client) {
		var player = client.player;
		var world = client.world;

		if (player == null || world == null) {
			return AmbienceBiomeKind.PLAINS;
		}

		var position = player.getBlockPos();
		var biomeEntry = world.getBiome(position);

		return kindForBiome(biomeEntry, position);
	}

	private static AmbienceBiomeKind kindForBiome(RegistryEntry<Biome> biomeEntry, BlockPos position) {
		var biome = biomeEntry.value();

		if (isSnowBiome(biomeEntry, biome, position)) {
			return AmbienceBiomeKind.SNOW;
		}

		if (isForestBiome(biomeEntry)) {
			return AmbienceBiomeKind.FOREST;
		}

		return AmbienceBiomeKind.PLAINS;
	}

	private static boolean isSnowBiome(RegistryEntry<Biome> biomeEntry, Biome biome, BlockPos position) {
		if (biome.getPrecipitation(position) == Biome.Precipitation.SNOW) {
			return true;
		}

		if (biome.getTemperature() <= SNOW_TEMPERATURE_THRESHOLD) {
			return true;
		}

		return biomeIdContainsAny(biomeEntry, "snow", "ice", "frozen", "mountain", "peaks", "windswept");
	}

	private static boolean isForestBiome(RegistryEntry<Biome> biomeEntry) {
		return biomeIdContainsAny(biomeEntry, "forest", "taiga", "jungle", "swamp", "grove", "wooded");
	}

	private static boolean biomeIdContainsAny(RegistryEntry<Biome> biomeEntry, String... needles) {
		var biomeId = biomeEntry.getKey().map(key -> key.getValue().toString()).orElse("").toLowerCase();

		for (var needle : needles) {
			if (biomeId.contains(needle)) {
				return true;
			}
		}

		return false;
	}

}
