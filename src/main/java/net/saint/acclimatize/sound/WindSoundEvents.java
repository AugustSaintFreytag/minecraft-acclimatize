package net.saint.acclimatize.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.Mod;

public final class WindSoundEvents {

	// Properties

	public static final SoundEvent WIND_PLAINS_LIGHT = SoundEvent.of(new Identifier(Mod.MOD_ID, "wind_plains_light"));
	public static final SoundEvent WIND_PLAINS_STRONG = SoundEvent.of(new Identifier(Mod.MOD_ID, "wind_plains_strong"));
	public static final SoundEvent WIND_FOREST_LIGHT = SoundEvent.of(new Identifier(Mod.MOD_ID, "wind_forest_light"));
	public static final SoundEvent WIND_FOREST_STRONG = SoundEvent.of(new Identifier(Mod.MOD_ID, "wind_forest_strong"));
	public static final SoundEvent WIND_SNOW_LIGHT = SoundEvent.of(new Identifier(Mod.MOD_ID, "wind_snow_light"));
	public static final SoundEvent WIND_SNOW_STRONG = SoundEvent.of(new Identifier(Mod.MOD_ID, "wind_snow_strong"));
	public static final SoundEvent WIND_INTERIOR_LIGHT = SoundEvent.of(new Identifier(Mod.MOD_ID, "wind_interior_light"));
	public static final SoundEvent WIND_INTERIOR_STRONG = SoundEvent.of(new Identifier(Mod.MOD_ID, "wind_interior_strong"));

	// Registration

	public static void registerAll() {
		register(WIND_PLAINS_LIGHT);
		register(WIND_PLAINS_STRONG);
		register(WIND_FOREST_LIGHT);
		register(WIND_FOREST_STRONG);
		register(WIND_SNOW_LIGHT);
		register(WIND_SNOW_STRONG);
		register(WIND_INTERIOR_LIGHT);
		register(WIND_INTERIOR_STRONG);
	}

	private static void register(SoundEvent soundEvent) {
		Registry.register(Registries.SOUND_EVENT, soundEvent.getId(), soundEvent);
	}

}
