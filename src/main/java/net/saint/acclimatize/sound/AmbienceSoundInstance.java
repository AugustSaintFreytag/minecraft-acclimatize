package net.saint.acclimatize.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.saint.acclimatize.data.space.SpaceKind;

public class AmbienceSoundInstance extends MovingSoundInstance {

	// References

	private final MinecraftClient client;

	// Properties

	private SpaceKind spaceKind;

	private float targetVolume;
	private float volumeStep = 0.01f;

	// Init

	protected AmbienceSoundInstance(MinecraftClient client, SoundCategory soundCategory, SoundEvent soundEvent, SpaceKind spaceKind) {
		super(soundEvent, soundCategory, SoundInstance.createRandom());

		this.client = client;
		this.repeat = true;
		this.repeatDelay = 0;
		this.volume = 0.01f;
		this.targetVolume = this.volume;
		this.pitch = 1.0f;
		this.relative = true;
		this.spaceKind = spaceKind;
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

	// Access

	public SpaceKind getSpaceKind() {
		return spaceKind;
	}

	public float getTargetVolume() {
		return targetVolume;
	}

	public void setTargetVolume(float targetVolume) {
		this.targetVolume = Math.max(0.0f, targetVolume);
	}

	public float getVolumeStep() {
		return volumeStep;
	}

	public void setVolumeStep(float volumeStep) {
		this.volumeStep = volumeStep;
	}

	// Volume

	private void updateVolume() {
		if (volume < targetVolume) {
			volume = Math.min(targetVolume, volume + volumeStep);
		} else if (volume > targetVolume) {
			volume = Math.max(targetVolume, volume - volumeStep / 2.0f);
		}

		if (targetVolume == 0.0f && volume == 0.0f) {
			setDone();
		}
	}

}
