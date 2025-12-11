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

	private AmbienceStateProperties activeProperties = AmbienceStateProperties.none();

	private AmbienceSoundInstance activeSound;
	private final List<AmbienceSoundInstance> fadingSounds = new ArrayList<>();

	// Access

	public AmbienceStateProperties getActiveSoundProperties() {
		return activeProperties;
	}

	// Tick

	public void tick(MinecraftClient client, boolean isPaused) {
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

	private void startOrUpdateSound(MinecraftClient client, AmbienceStateProperties properties, float targetVolume) {
		if (Mod.CONFIG.enableLogging && client.world.getTime() % 20 == 0) {
			Mod.LOGGER.info("Wind Ambience - Properties ({}), Target Volume: {}", properties.description(), targetVolume);
		}

		var soundManager = client.getSoundManager();

		if (activeSound == null || !activeProperties.equals(properties) || activeSound.isDone() || !soundManager.isPlaying(activeSound)) {
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

			soundManager.play(activeSound);

			return;
		}

		activeSound.setTargetVolume(targetVolume * Mod.CONFIG.ambientSoundVolume);
	}

	private void fadeOutActiveSound() {
		if (activeSound != null) {
			fadeOutSound(activeSound);
			activeSound = null;
		}

		activeProperties = AmbienceStateProperties.none();
	}

	private void fadeOutSound(AmbienceSoundInstance sound) {
		sound.setTargetVolume(0.0f);

		if (!fadingSounds.contains(sound)) {
			fadingSounds.add(sound);
		}
	}

	private void cleanUpFadingSounds() {
		fadingSounds.removeIf(AmbienceSoundInstance::isDone);
	}

	// Utilities

	private AmbienceStateProperties evaluateAmbienceProperties(MinecraftClient client) {
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

	private WindLevel determineWindLevel(double windIntensity) {
		if (windIntensity < LOW_WIND_THRESHOLD) {
			return WindLevel.NONE;
		}

		if (windIntensity < HIGH_WIND_THRESHOLD) {
			return WindLevel.LOW;
		}

		return WindLevel.HIGH;
	}

	private float volumeForProperties(World world, BlockPos position, AmbienceStateProperties properties) {
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

	private float baseVolumeForWindLevel(WindLevel windLevel, double windIntensity, double scalingFactor) {
		if (windLevel == WindLevel.NONE) {
			return 0.0f;
		}

		return calculateScaledVolume(windIntensity, LOW_WIND_THRESHOLD, MAX_WIND_REFERENCE, 0.2f, 1.0f, scalingFactor);
	}

	private float calculateScaledVolume(double windIntensity, double lowerBound, double upperBound, float minVolume, float maxVolume,
			double scalingFactor) {
		var clampedIntensity = MathUtil.clamp(windIntensity, lowerBound, upperBound);
		var normalizedIntensity = (clampedIntensity - lowerBound) / (upperBound - lowerBound);
		var clampedScalingFactor = MathUtil.clamp(scalingFactor, 0.0, 1.0);

		var volumeRange = maxVolume - minVolume;
		var pivotNormalized = 0.75;
		var adjustedNormalized = pivotNormalized + (normalizedIntensity - pivotNormalized) * clampedScalingFactor;
		var clampedNormalized = MathUtil.clamp(adjustedNormalized, 0.0, 1.0);
		var volume = minVolume + (float) (volumeRange * clampedNormalized);

		return volume;
	}

	private float volumeFactorForProperties(World world, BlockPos position, AmbienceStateProperties properties) {
		if (properties.isCave()) {
			var caveDepth = WorldUtil.getApproximateRelativeCaveDepth(world, position);
			var caveDepthFactor = 1 - MathUtil.clamp((float) caveDepth / 36.0f, 0.0f, 1.0f);
			return Math.max(0.15f, CAVE_VOLUME_MULTIPLIER * caveDepthFactor);
		}

		if (properties.isInterior()) {
			if (properties.isRaining()) {
				return INTERIOR_RAIN_VOLUME_MULTIPLIER;
			}

			return INTERIOR_VOLUME_MULTIPLIER;
		}

		if (Mod.CONFIG.enableRainSounds && properties.isRaining()) {
			return EXTERIOR_RAIN_VOLUME_MULTIPLIER * Mod.CONFIG.rainSoundVolume;
		}

		return switch (properties.biomeKind()) {
		case PLAINS -> PLAINS_VOLUME_MULTIPLIER;
		case FOREST -> FOREST_VOLUME_MULTIPLIER;
		case SNOW -> SNOW_VOLUME_MULTIPLIER;
		default -> 0.0f;
		};
	}

	private SoundCategory soundCategoryForEvent(SoundEvent soundEvent) {
		if (soundEvent == AmbienceSoundEvents.RAIN_EXTERIOR_LIGHT || soundEvent == AmbienceSoundEvents.RAIN_EXTERIOR_STRONG) {
			return SoundCategory.WEATHER;
		}

		return SoundCategory.AMBIENT;
	}

	private SoundEvent soundEventForProperties(AmbienceStateProperties properties) {
		// Interior / Cave

		if (properties.isCave()) {
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
