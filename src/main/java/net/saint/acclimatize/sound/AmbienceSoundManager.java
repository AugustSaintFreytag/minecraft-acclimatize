package net.saint.acclimatize.sound;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.data.world.WorldUtil;
import net.saint.acclimatize.util.MathUtil;

public final class AmbienceSoundManager {

	// Configuration

	private static final double LOW_WIND_THRESHOLD = 0.01;
	private static final double MAX_WIND_REFERENCE = 6.0;
	private static final double HIGH_WIND_THRESHOLD = 2.65;

	private static final float INTERIOR_RAIN_VOLUME_MULTIPLIER = 1.0f;
	private static final float EXTERIOR_RAIN_VOLUME_MULTIPLIER = 1.0f;

	private static final float INTERIOR_VOLUME_MULTIPLIER = 0.7f;
	private static final float CAVE_VOLUME_MULTIPLIER = 0.85f;
	private static final float PLAINS_VOLUME_MULTIPLIER = 0.85f;
	private static final float FOREST_VOLUME_MULTIPLIER = 0.95f;
	private static final float SNOW_VOLUME_MULTIPLIER = 1.0f;

	// References

	private static AmbienceStateProperties activeProperties = AmbienceStateProperties.none();

	private static AmbienceSoundInstance activeSound;
	private static final List<AmbienceSoundInstance> fadingSounds = new ArrayList<>();

	// Access

	public static AmbienceStateProperties getActiveSoundProperties() {
		return activeProperties;
	}

	// Tick

	public static void tick(MinecraftClient client, boolean isPaused) {
		cleanUpFadingSounds();

		if (isPaused || !Mod.CONFIG.enableWind || !Mod.CONFIG.enableAmbientSounds) {
			fadeOutActiveSound();
			return;
		}

		var player = client.player;
		var world = client.world;
		var position = player.getBlockPos();

		var properties = evaluateAmbienceProperties(client);

		if (player == null || world == null || properties.level() == WindLevel.NONE) {
			fadeOutActiveSound();
			return;
		}

		var volume = volumeForProperties(world, position, properties);

		if (volume <= 0.0f) {
			fadeOutActiveSound();
			return;
		}

		startOrUpdateSound(client, properties, volume);
	}

	private static void startOrUpdateSound(MinecraftClient client, AmbienceStateProperties properties, float targetVolume) {
		if (Mod.CONFIG.enableLogging && client.world.getTime() % 20 == 0) {
			Mod.LOGGER.info("Wind Ambience - Properties ({}), Target Volume: {}", properties.description(), targetVolume);
		}

		if (activeSound == null || !activeProperties.equals(properties) || activeSound.isDone()) {
			if (activeSound != null) {
				fadeOutSound(activeSound);
			}

			var soundEvent = soundEventForProperties(properties);
			var soundCategory = soundCategoryForEvent(soundEvent);

			if (soundEvent == null) {
				return;
			}

			activeProperties = properties;
			activeSound = new AmbienceSoundInstance(client, soundCategory, soundEvent);
			activeSound.setTargetVolume(targetVolume * Mod.CONFIG.ambientSoundVolume);

			client.getSoundManager().play(activeSound);

			return;
		}

		activeSound.setTargetVolume(targetVolume * Mod.CONFIG.ambientSoundVolume);
	}

	private static void fadeOutActiveSound() {
		if (activeSound != null) {
			fadeOutSound(activeSound);
			activeSound = null;
		}

		activeProperties = AmbienceStateProperties.none();
	}

	private static void fadeOutSound(AmbienceSoundInstance sound) {
		sound.setTargetVolume(0.0f);

		if (!fadingSounds.contains(sound)) {
			fadingSounds.add(sound);
		}
	}

	private static void cleanUpFadingSounds() {
		fadingSounds.removeIf(AmbienceSoundInstance::isDone);
	}

	// Utilities

	private static AmbienceStateProperties evaluateAmbienceProperties(MinecraftClient client) {
		var world = client.world;
		var player = client.player;

		if (player.isSubmergedInWater()) {
			return AmbienceStateProperties.none();
		}

		var windIntensity = Math.max(0.0, ModClient.getWindIntensity());
		var windLevel = determineWindLevel(windIntensity);
		var windBiomeKind = WindBiomeKindUtil.kindForPlayer(client);

		var isRaining = WorldUtil.isRaining(world, player);
		var isCave = WorldUtil.isInCave(world, player);
		var isInterior = ModClient.getIsPlayerInInterior();

		var windProperties = new AmbienceStateProperties(windLevel, windBiomeKind, isRaining, isInterior, isCave);

		return windProperties;
	}

	private static WindLevel determineWindLevel(double windIntensity) {
		if (windIntensity < LOW_WIND_THRESHOLD) {
			return WindLevel.NONE;
		}

		if (windIntensity < HIGH_WIND_THRESHOLD) {
			return WindLevel.LOW;
		}

		return WindLevel.HIGH;
	}

	private static float volumeForProperties(World world, BlockPos position, AmbienceStateProperties properties) {
		var windLevel = properties.level();
		var windIntensity = ModClient.getWindIntensity();
		var volumeScalingFactor = Mod.CONFIG.ambientSoundWindIntensityFactor;

		if (Mod.CONFIG.enableRainSounds && properties.isRaining()) {
			// Allow rain to be louder and less varied by wind intensity.
			volumeScalingFactor *= 0.5;
		}

		var baseVolume = baseVolumeForWindLevel(windLevel, windIntensity, volumeScalingFactor);
		var volumeFactor = volumeFactorForProperties(world, position, properties);

		return baseVolume * volumeFactor;
	}

	private static float baseVolumeForWindLevel(WindLevel windLevel, double windIntensity, double scalingFactor) {
		return switch (windLevel) {
		case NONE -> 0.0f;
		case LOW -> calculateScaledVolume(windIntensity, LOW_WIND_THRESHOLD, HIGH_WIND_THRESHOLD, 0.18f, 0.5f, scalingFactor);
		case HIGH -> calculateScaledVolume(windIntensity, HIGH_WIND_THRESHOLD, MAX_WIND_REFERENCE, 0.55f, 0.95f, scalingFactor);
		};
	}

	private static float calculateScaledVolume(double windIntensity, double lowerBound, double upperBound, float minVolume, float maxVolume,
			double scalingFactor) {
		var clampedValue = MathUtil.clamp(windIntensity, lowerBound, upperBound);
		var progress = (clampedValue - lowerBound) / (upperBound - lowerBound);
		progress *= MathUtil.clamp(scalingFactor, 0.0, 1.0);

		return minVolume + (float) progress * (maxVolume - minVolume);
	}

	private static float volumeFactorForProperties(World world, BlockPos position, AmbienceStateProperties properties) {
		if (properties.isCave()) {
			var caveDepth = WorldUtil.getApproximateCaveDepth(world, position);
			var caveDepthFactor = 1 - MathUtil.clamp((float) caveDepth / 36.0f, 0.0f, 1.0f);
			return CAVE_VOLUME_MULTIPLIER * caveDepthFactor;
		}

		if (properties.isInterior()) {
			if (properties.isRaining()) {
				return INTERIOR_RAIN_VOLUME_MULTIPLIER;
			}

			return INTERIOR_VOLUME_MULTIPLIER;
		}

		if (Mod.CONFIG.enableRainSounds && properties.isRaining()) {
			return EXTERIOR_RAIN_VOLUME_MULTIPLIER;
		}

		return switch (properties.biomeKind()) {
		case PLAINS -> PLAINS_VOLUME_MULTIPLIER;
		case FOREST -> FOREST_VOLUME_MULTIPLIER;
		case SNOW -> SNOW_VOLUME_MULTIPLIER;
		default -> 0.0f;
		};
	}

	private static SoundCategory soundCategoryForEvent(SoundEvent soundEvent) {
		if (soundEvent == AmbienceSoundEvents.RAIN_EXTERIOR_LIGHT || soundEvent == AmbienceSoundEvents.RAIN_EXTERIOR_STRONG) {
			return SoundCategory.WEATHER;
		}

		return SoundCategory.AMBIENT;
	}

	private static SoundEvent soundEventForProperties(AmbienceStateProperties properties) {
		// Interior / Cave

		if (properties.isInterior() && properties.isCave()) {
			return switch (properties.level()) {
			case LOW -> AmbienceSoundEvents.WIND_CAVE_LIGHT;
			case HIGH -> AmbienceSoundEvents.WIND_CAVE_STRONG;
			default -> null;
			};
		}

		// Interior / Rain

		if (properties.isInterior() && !properties.isRaining()) {
			return switch (properties.level()) {
			case LOW -> AmbienceSoundEvents.WIND_INTERIOR_LIGHT;
			case HIGH -> AmbienceSoundEvents.WIND_INTERIOR_STRONG;
			default -> null;
			};
		}

		if (properties.isInterior() && properties.isRaining()) {
			return switch (properties.level()) {
			case LOW -> AmbienceSoundEvents.RAIN_INTERIOR_LIGHT;
			case HIGH -> AmbienceSoundEvents.RAIN_INTERIOR_STRONG;
			default -> null;
			};
		}

		// Exterior / Rain

		if (Mod.CONFIG.enableRainSounds && properties.isRaining()) {
			return switch (properties.level()) {
			case LOW -> AmbienceSoundEvents.RAIN_EXTERIOR_LIGHT;
			case HIGH -> AmbienceSoundEvents.RAIN_EXTERIOR_STRONG;
			default -> null;
			};
		}

		// Exterior / Biomes

		return switch (properties.biomeKind()) {
		case PLAINS -> switch (properties.level()) {
		case LOW -> AmbienceSoundEvents.WIND_PLAINS_LIGHT;
		case HIGH -> AmbienceSoundEvents.WIND_PLAINS_STRONG;
		default -> null;
		};
		case FOREST -> switch (properties.level()) {
		case LOW -> AmbienceSoundEvents.WIND_FOREST_LIGHT;
		case HIGH -> AmbienceSoundEvents.WIND_FOREST_STRONG;
		default -> null;
		};
		case SNOW -> switch (properties.level()) {
		case LOW -> AmbienceSoundEvents.WIND_SNOW_LIGHT;
		case HIGH -> AmbienceSoundEvents.WIND_SNOW_STRONG;
		default -> null;
		};
		default -> null;
		};
	}

}
