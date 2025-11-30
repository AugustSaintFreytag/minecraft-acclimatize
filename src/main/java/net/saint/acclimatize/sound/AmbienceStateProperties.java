package net.saint.acclimatize.sound;

public record AmbienceStateProperties(WindLevel level, WindBiomeKind biomeKind, boolean isRaining, boolean isInterior, boolean isCave) {

	public static AmbienceStateProperties none() {
		return new AmbienceStateProperties(WindLevel.NONE, WindBiomeKind.NONE, false, false, false);
	}

	public String description() {
		return String.format("Level: %s, Biome: %s, Raining: %s, Interior: %s, Cave: %s", level, biomeKind, isRaining, isInterior, isCave);
	}

}
