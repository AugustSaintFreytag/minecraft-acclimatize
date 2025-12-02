package net.saint.acclimatize.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class AmbienceSoundInstance extends MovingSoundInstance {

	// Configuration

	private static final float VOLUME_STEP = 0.02f;

	// References

	private final MinecraftClient client;

	// Properties

	private float targetVolume;

	// Init

	protected AmbienceSoundInstance(MinecraftClient client, SoundEvent soundEvent) {
		super(soundEvent, SoundCategory.AMBIENT, SoundInstance.createRandom());

		this.client = client;
		this.repeat = true;
		this.repeatDelay = 0;
		this.volume = 0.01f;
		this.targetVolume = this.volume;
		this.pitch = 1.0f;
		this.relative = true;
	}

	// Tick

	@Override
	public void tick() {
		var player = client.player;

		if (player == null || player.isRemoved()) {
			setDone();
			return;
		}

		x = player.getX();
		y = player.getY();
		z = player.getZ();

		updateVolume();
	}

	public void setTargetVolume(float targetVolume) {
		this.targetVolume = Math.max(0.0f, targetVolume);
	}

	private void updateVolume() {
		if (volume < targetVolume) {
			volume = Math.min(targetVolume, volume + VOLUME_STEP);
		} else if (volume > targetVolume) {
			volume = Math.max(targetVolume, volume - VOLUME_STEP);
		}

		if (targetVolume == 0.0f && volume == 0.0f) {
			setDone();
		}
	}

}
