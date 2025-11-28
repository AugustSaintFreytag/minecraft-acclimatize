package net.saint.acclimatize.sound;

public record WindProperties(WindLevel level, WindBiomeKind biomeKind, boolean isInterior) {

	public static WindProperties none() {
		return new WindProperties(WindLevel.NONE, WindBiomeKind.NONE, false);
	}

}
