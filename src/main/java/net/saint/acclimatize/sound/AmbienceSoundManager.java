package net.saint.acclimatize.sound;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.data.world.WorldUtil;
import net.saint.acclimatize.util.MathUtil;

public final class AmbienceSoundManager {

	// Configuration

	private static final double LOW_WIND_THRESHOLD = 0.01;
	private static final double MAX_WIND_REFERENCE = 6.0;
	private static final double HIGH_WIND_THRESHOLD = 1.75;

	private static final float INTERIOR_VOLUME_MULTIPLIER = 0.85f;
	private static final float CAVE_VOLUME_MULTIPLIER = 0.85f;
	private static final float FOREST_VOLUME_MULTIPLIER = 1.0f;
	private static final float SNOW_VOLUME_MULTIPLIER = 1.0f;

	// References

	private static AmbienceStateProperties activeProperties = AmbienceStateProperties.none();

	private static AmbienceSoundInstance activeSound;
	private static final List<AmbienceSoundInstance> fadingSounds = new ArrayList<>();

	// Tick

	public static void tick(MinecraftClient client, boolean isPaused) {
		cleanUpFadingSounds();

		if (isPaused || !Mod.CONFIG.enableWind || !Mod.CONFIG.enableAmbientSounds) {
			fadeOutActiveSound();
			return;
		}

		var player = client.player;
		var world = client.world;

		var properties = getCurrentWindProperties(client);

		if (player == null || world == null || properties.level() == WindLevel.NONE) {
			fadeOutActiveSound();
			return;
		}

		var windIntensity = Math.max(0.0, ModClient.getWindIntensity());
		var targetVolume = volumeForIntensity(properties.level(), windIntensity);

		targetVolume = applyVolumeForWindProperties(properties, targetVolume);

		if (targetVolume <= 0.0f) {
			fadeOutActiveSound();
			return;
		}

		startOrUpdateSound(client, properties, targetVolume);
	}

	private static void startOrUpdateSound(MinecraftClient client, AmbienceStateProperties properties, float targetVolume) {
		if (Mod.CONFIG.enableLogging && client.world.getTime() % 20 == 0) {
			Mod.LOGGER.info("Wind Ambience - Properties ({}), Target Volume: {}", properties.description(), targetVolume);
		}

		if (activeSound == null || !activeProperties.equals(properties) || activeSound.isDone()) {
			if (activeSound != null) {
				fadeOutSound(activeSound);
			}

			var soundEvent = soundEventForWindProperties(properties);

			if (soundEvent == null) {
				return;
			}

			activeProperties = properties;
			activeSound = new AmbienceSoundInstance(client, soundEvent);
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

	private static AmbienceStateProperties getCurrentWindProperties(MinecraftClient client) {
		var world = client.world;
		var player = client.player;

		var windIntensity = Math.max(0.0, ModClient.getWindIntensity());
		var windLevel = determineWindLevel(windIntensity);
		var windBiomeKind = WindBiomeKindUtil.kindForPlayer(client);

		var isCave = WorldUtil.isInCave(world, player);
		var isInterior = ModClient.getIsPlayerInInterior();
		var isRaining = WorldUtil.isRaining(world, player);

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

	private static float volumeForIntensity(WindLevel windLevel, double windIntensity) {
		return switch (windLevel) {
		case NONE -> 0.0f;
		case LOW -> calculateScaledVolume(windIntensity, LOW_WIND_THRESHOLD, HIGH_WIND_THRESHOLD, 0.18f, 0.5f);
		case HIGH -> calculateScaledVolume(windIntensity, HIGH_WIND_THRESHOLD, MAX_WIND_REFERENCE, 0.55f, 0.95f);
		};
	}

	private static float calculateScaledVolume(double value, double lowerBound, double upperBound, float minVolume, float maxVolume) {
		var clampedValue = MathUtil.clamp(value, lowerBound, upperBound);
		var progress = (clampedValue - lowerBound) / (upperBound - lowerBound);
		progress *= MathUtil.clamp(Mod.CONFIG.ambientSoundWindIntensityFactor, 0.0, 1.0);

		return minVolume + (float) progress * (maxVolume - minVolume);
	}

	private static float applyVolumeForWindProperties(AmbienceStateProperties properties, float baseVolume) {
		if (properties.isInterior()) {
			return baseVolume * INTERIOR_VOLUME_MULTIPLIER;
		}

		if (properties.isCave()) {
			return baseVolume * CAVE_VOLUME_MULTIPLIER;
		}

		return switch (properties.biomeKind()) {
		case PLAINS -> baseVolume;
		case FOREST -> baseVolume * FOREST_VOLUME_MULTIPLIER;
		case SNOW -> baseVolume * SNOW_VOLUME_MULTIPLIER;
		default -> 0.0f;
		};
	}

	private static SoundEvent soundEventForWindProperties(AmbienceStateProperties properties) {
		if (properties.isInterior() && !properties.isRaining()) {
			return switch (properties.level()) {
			case LOW -> AmbienceSoundEvents.WIND_INTERIOR_LIGHT;
			case HIGH -> AmbienceSoundEvents.WIND_INTERIOR_STRONG;
			default -> null;
			};
		}

		if (properties.isInterior() && properties.isRaining()) {
			return switch (properties.level()) {
			case LOW -> AmbienceSoundEvents.WIND_INTERIOR_RAIN_LIGHT;
			case HIGH -> AmbienceSoundEvents.WIND_INTERIOR_RAIN_STRONG;
			default -> null;
			};
		}

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
