package net.saint.acclimatize.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.data.world.WorldUtil;
import net.saint.acclimatize.util.MathUtil;

public final class WeatherSoundManager {

	// Configuration

	private static final float MIN_RAIN_GRADIENT = 0.01f;

	// References

	private static WeatherSoundInstance activeSound;

	// Tick

	public static void tick(MinecraftClient client, boolean isPaused) {
		cleanUpInactiveSound(client);

		if (isPaused || !Mod.CONFIG.enableRainSounds) {
			fadeOutActiveSound();
			return;
		}

		var world = client.world;
		var player = client.player;

		if (world == null || player == null || !WorldUtil.isRaining(world, player)) {
			fadeOutActiveSound();
			return;
		}

		var rainGradient = world.getRainGradient(1.0f);

		if (rainGradient <= MIN_RAIN_GRADIENT) {
			fadeOutActiveSound();
			return;
		}

		var ambienceProperties = AmbienceSoundManager.getActiveSoundProperties();
		var targetVolume = calculateTargetVolume(rainGradient, ambienceProperties);

		if (targetVolume <= 0.0f) {
			fadeOutActiveSound();
			return;
		}

		startOrUpdateSound(client, targetVolume);
	}

	private static float calculateTargetVolume(float rainGradient, AmbienceStateProperties ambienceProperties) {
		var volume = Mod.CONFIG.rainSoundVolume * rainGradient;

		if (ambienceProperties.isCave()) {
			return 0.0f;
		}

		if (ambienceProperties.isInterior()) {
			volume *= Mod.CONFIG.interiorSoundSuppressionFactor;
		}

		volume *= Mod.CONFIG.ambientSoundVolume;

		return MathUtil.clamp(volume, 0.0f, 4.0f);
	}

	private static void startOrUpdateSound(MinecraftClient client, float targetVolume) {
		if (activeSound == null) {
			if (Mod.CONFIG.enableLogging) {
				Mod.LOGGER.info("Starting new rain sound.");
			}

			activeSound = new WeatherSoundInstance(client, SoundEvents.WEATHER_RAIN);
			client.getSoundManager().play(activeSound);
		}

		activeSound.setTargetVolume(targetVolume);

		if (Mod.CONFIG.enableLogging && client.world.getTime() % 20 == 0) {
			Mod.LOGGER.info("Rain Sound - Target Volume: {}, Current Volume: {}", targetVolume, activeSound.getCurrentVolume());
		}
	}

	private static void fadeOutActiveSound() {
		if (activeSound != null) {
			activeSound.setTargetVolume(0.0f);
		}
	}

	private static void cleanUpInactiveSound(MinecraftClient client) {
		if (activeSound == null) {
			return;
		}

		var soundManager = client.getSoundManager();
		var isPlaying = soundManager.isPlaying(activeSound);

		if (activeSound.isDone() || !isPlaying) {
			if (Mod.CONFIG.enableLogging && !isPlaying && !activeSound.isDone()) {
				Mod.LOGGER.info("Rain sound instance was not playing, clearing to restart.");
			}

			activeSound = null;
		}
	}

}
