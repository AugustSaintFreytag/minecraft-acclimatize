package net.saint.acclimatize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.sound.AbstractSoundInstance;

@Mixin(AbstractSoundInstance.class)
public interface AbstractSoundInstanceAccessor {

	@Accessor("volume")
	void acclimatize$setVolume(float volume);

}
