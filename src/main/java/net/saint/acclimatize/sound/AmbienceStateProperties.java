package net.saint.acclimatize.sound;

public record AmbienceStateProperties(WindLevel level, AmbienceBiomeKind biomeKind, boolean isRaining, boolean isInterior, boolean isCave) {

	public static AmbienceStateProperties none() {
		return new AmbienceStateProperties(WindLevel.NONE, AmbienceBiomeKind.NONE, false, false, false);
	}

	public String description() {
		return String.format("Level: %s, Biome: %s, Raining: %s, Interior: %s, Cave: %s", level, biomeKind, isRaining, isInterior, isCave);
	}

}
