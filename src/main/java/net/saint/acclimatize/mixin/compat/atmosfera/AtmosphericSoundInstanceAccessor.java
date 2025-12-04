package net.saint.acclimatize.mixin.compat.atmosfera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundInstance;

@Mixin(AtmosphericSoundInstance.class)
public interface AtmosphericSoundInstanceAccessor {

	@Accessor("volume")
	void acclimatize$setVolume(float volume);

}
