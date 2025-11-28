package net.saint.acclimatize.data.biome;

import java.util.HashMap;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.util.MathUtil;

public final class BiomeTemperatureUtil {

	private static final Vec3i NORTH_VECTOR = new Vec3i(0, 0, -1);
	private static final Vec3i SOUTH_VECTOR = new Vec3i(0, 0, 1);
	private static final Vec3i EAST_VECTOR = new Vec3i(1, 0, 0);
	private static final Vec3i WEST_VECTOR = new Vec3i(-1, 0, 0);

	// Configuration

	private static final HashMap<String, Double> biomeRawTemperatureOverrides = new HashMap<String, Double>() {
		{
			// Cold Biomes
			put("minecraft:deep_frozen_ocean", -0.4);
			put("minecraft:deep_cold_ocean", -0.2);
			put("minecraft:cold_ocean", -0.1);
			put("minecraft:deep_ocean", 0.0);
			put("minecraft:ocean", 0.1);
			put("minecraft:deep_lukewarm_ocean", 0.15);
			put("minecraft:lukewarm_ocean", 0.2);
			put("minecraft:warm_ocean", 0.3);

			// Humid Biomes
			put("minecraft:swamp", 0.85);
			put("minecraft:mangrove_swamp", 0.8);

			// Hot Biomes
			put("minecraft:beach", 1.0);
			put("minecraft:savanna", 1.2);
			put("minecraft:savanna_plateau", 1.18);
			put("minecraft:windswept_savanna", 1.15);
			put("minecraft:desert", 1.35);
			put("minecraft:badlands", 1.45);
			put("minecraft:wooded_badlands", 1.42);
			put("minecraft:eroded_badlands", 1.55);
		}
	};

	// Positional Temperature

	public static double positionalTemperatureForPlayer(ServerPlayerEntity player, double biomeTemperature, boolean isInInterior) {
		var world = player.getWorld();
		var position = player.getBlockPos();
		var dimension = world.getDimension();

		// Nether & End

		if (!dimension.natural()) {
			return biomeTemperature;
		}

		// Altitude

		var height = position.getY();
		var heightTemperatureDelta = temperatureDeltaForAltitude(height);

		biomeTemperature += heightTemperatureDelta;

		// Daylight/Nighttime

		var skyFraction = world.getSkyAngle(0.0f);
		var dayNightTemperatureDelta = temperatureDeltaForDayNightTime(skyFraction);

		biomeTemperature += dayNightTemperatureDelta;

		// Precipitation

		var precipitationTemperatureDelta = temperatureDeltaForPrecipitation(world, position);
		biomeTemperature += precipitationTemperatureDelta;

		// Return

		return biomeTemperature;
	}

	// Biome Temperature

	public static double biomeTemperatureForPlayer(ServerPlayerEntity player, boolean isInInterior) {
		var world = player.getWorld();
		var position = player.getBlockPos();

		return biomeTemperatureForPosition(world, position);
	}

	public static double biomeTemperatureForPosition(World world, BlockPos position) {
		var dimensionKey = world.getRegistryKey();

		if (dimensionKey == World.NETHER) {
			return Mod.CONFIG.netherBiomeTemperature;
		}

		if (dimensionKey == World.END) {
			return Mod.CONFIG.endBiomeTemperature;
		}

		var naiveBiomeTemperature = baseTemperatureForBiomeAtPosition(world, position);
		var averagedBiomeTemperature = baseTemperatureForBiomeInEnvirons(world, position);

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Determined averaged biome temperature " + averagedBiomeTemperature + " vs. naive temperature "
					+ naiveBiomeTemperature + ".");
		}

		return averagedBiomeTemperature;
	}

	private static double baseTemperatureForBiomeInEnvirons(World world, BlockPos position) {
		var samplingRadius = Mod.CONFIG.biomeSamplingRadius;
		var temperatureSum = 0.0;

		// Sample one biome in each cardinal direction at the given radius from the player.
		var northPosition = position.add(NORTH_VECTOR.multiply(samplingRadius));
		var southPosition = position.add(SOUTH_VECTOR.multiply(samplingRadius));
		var eastPosition = position.add(EAST_VECTOR.multiply(samplingRadius));
		var westPosition = position.add(WEST_VECTOR.multiply(samplingRadius));

		temperatureSum += baseTemperatureForBiomeAtPosition(world, position);
		temperatureSum += baseTemperatureForBiomeAtPosition(world, northPosition);
		temperatureSum += baseTemperatureForBiomeAtPosition(world, southPosition);
		temperatureSum += baseTemperatureForBiomeAtPosition(world, eastPosition);
		temperatureSum += baseTemperatureForBiomeAtPosition(world, westPosition);

		return temperatureSum / 5;
	}

	private static double baseTemperatureForBiomeAtPosition(World world, BlockPos position) {
		var biomeEntry = world.getBiome(position);
		return baseTemperatureForBiomeEntry(biomeEntry);
	}

	private static double baseTemperatureForBiomeEntry(RegistryEntry<Biome> biomeEntry) {
		var rawBiomeTemperature = rawTemperatureForBiome(biomeEntry);
		var baseTemperature = ((rawBiomeTemperature + Mod.CONFIG.biomeTemperatureZeroingAnchor) / 3) * 100;

		return baseTemperature;
	}

	private static double rawTemperatureForBiome(RegistryEntry<Biome> biomeEntry) {
		var biome = biomeEntry.value();
		var rawBiomeTemperature = (double) biome.getTemperature();

		rawBiomeTemperature = rawTemperatureOverrideForBiome(biomeEntry, rawBiomeTemperature);

		return rawBiomeTemperature;
	}

	private static double rawTemperatureOverrideForBiome(RegistryEntry<Biome> biomeEntry, double baseTemperature) {
		var biomeId = biomeEntry.getKey().get().getValue().toString();

		if (!biomeRawTemperatureOverrides.containsKey(biomeId)) {
			return baseTemperature;
		}

		return biomeRawTemperatureOverrides.get(biomeId);
	}

	// Precipitation

	private static double temperatureDeltaForPrecipitation(World world, BlockPos position) {
		var biomeEntry = world.getBiome(position);
		var precipitation = biomeEntry.value().getPrecipitation(position);
		var temperatureDelta = 0.0;

		if (precipitation == Biome.Precipitation.RAIN) {
			temperatureDelta = Mod.CONFIG.rainTemperatureDelta;
		} else if (precipitation == Biome.Precipitation.SNOW) {
			temperatureDelta = Mod.CONFIG.snowTemperatureDelta;
		}

		return temperatureDelta;
	}

	// Altitude

	private static double temperatureDeltaForAltitude(double altitude) {
		var scalingFactor = -0.01; // Scaling factor: α
		var growthFactor = 1.5; // Growth factor: γ
		var softeningFactor = 15.0; // Softening factor: σ

		var lowerBound = -15.0; // Lower bound: L
		var upperBound = 15.0; // Upper bound: U

		var altitudeAnchor = (double) Mod.CONFIG.altitudeZeroingAnchor; // Altitude anchor: h_n

		// Formula: ΔT_alt = α * (sgn(h - h_n) * |h - h_n|^γ - α * σ^γ) - ae
		var delta = scalingFactor * Math.signum(altitude - altitudeAnchor) * Math.pow(Math.abs(altitude - altitudeAnchor), growthFactor)
				- scalingFactor * Math.pow(softeningFactor, growthFactor) - 0.5;

		return MathUtil.clamp(delta, lowerBound, upperBound) * Mod.CONFIG.altitudeTemperatureFactor;
	}

	// Day/Night

	private static double temperatureDeltaForDayNightTime(float skyAngle) {
		var phaseValue = phaseValueFromSkyAngle(skyAngle); // Phase value: φ
		var plateau = 1.8; // Plateau: p
		var offset = 1.6 * Math.PI; // Offset: ε

		// Formula: ΔT_{dn} = ((1 + cos(φ - ε)) / 2) ^ p
		var dropFactor = Math.pow(((1 + MathUtil.cos(phaseValue - offset)) / 2), plateau);
		var temperatureDelta = Mod.CONFIG.nightTemperatureDelta * dropFactor;

		return temperatureDelta;
	}

	// Phase

	public static double phaseValueFromSkyAngle(float skyFraction) {
		// Minecraft sky angle mapping to phase values (in radians):
		// 0.000 (noon) -> π/2, 0.216 (sunset) -> π, 0.500 (midnight)
		// 0.500 (midnight) -> 3π/2, 0.784 (sunrise) -> 2π/0

		// Define key time points
		final var noon = 0.000;
		final var sunset = 0.216;
		final var midnight = 0.500;
		final var sunrise = 0.784;

		// Define corresponding phase values
		final var noonPhase = Math.PI * 0.5; // π/2
		final var sunsetPhase = Math.PI; // π
		final var midnightPhase = Math.PI * 1.5; // 3π/2
		final var sunrisePhase = Math.PI * 2.0; // 2π

		// Map sky fraction to phase value using linear interpolation between key points
		if (skyFraction <= sunset) {
			// Noon to sunset: π/2 to π
			var progress = skyFraction / sunset;
			return noonPhase + progress * (sunsetPhase - noonPhase);
		} else if (skyFraction <= midnight) {
			// Sunset to midnight: π to 3π/2
			var progress = (skyFraction - sunset) / (midnight - sunset);
			return sunsetPhase + progress * (midnightPhase - sunsetPhase);
		} else if (skyFraction <= sunrise) {
			// Midnight to sunrise: 3π/2 to 2π
			var progress = (skyFraction - midnight) / (sunrise - midnight);
			return midnightPhase + progress * (sunrisePhase - midnightPhase);
		} else {
			// Sunrise to noon (next day): 2π/0 to π/2
			var progress = (skyFraction - sunrise) / (1.0 - sunrise);
			return progress * noonPhase; // Wraps from 2π back to 0, then to π/2
		}
	}

}
