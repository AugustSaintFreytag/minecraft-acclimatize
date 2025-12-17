package net.saint.acclimatize.hud;

import java.awt.Point;
import java.util.HashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.data.player.PlayerTemperatureUtil;

public final class TemperatureHudUtil {

	// Library

	private enum THERMOMETER_STYLE {
		GLASS, GAUGE
	}

	private enum TEMPERATURE_LEVEL {
		EXTREMELY_COLD, VERY_COLD, COLD, SLIGHTLY_COLD, NEUTRAL, SLIGHTLY_HOT, HOT, VERY_HOT, EXTREMELY_HOT
	}

	private enum TEMPERATURE_CHANGE_INDICATOR {
		EXTREME_COOLING, REGULAR_COOLING, NEUTRAL, REGULAR_HEATING, EXTREME_HEATING
	}

	// State

	private static int glassShakeTick = 0;
	private static int glassShakeTickMax = 0;
	private static int glassShakePM = -1;
	private static boolean glassShakeAxis = false;

	// Textures

	private static final Identifier THERMOMETER_FRAME_TEXTURE = textureIdentifierForGaugeStyle("thermometer_frame.png");
	private static final Identifier THERMOMETER_GAUGE_TEXTURE = textureIdentifierForGaugeStyle("thermometer_gauge.png");
	private static final Identifier THERMOMETER_HAND_TEXTURE = textureIdentifierForGaugeStyle("thermometer_hand.png");

	private static final Identifier THERMOMETER_SNOWFLAKE_TEXTURE = textureIdentifierForThermometer("snowflake_icon_8x8.png");
	private static final Identifier THERMOMETER_FLAME_TEXTURE = textureIdentifierForThermometer("flame_icon_8x8.png");
	private static final Identifier THERMOMETER_STILL_TEXTURE = textureIdentifierForThermometer("temperate_icon.png");

	private static final Identifier THERMOMETER_OVERLAY_SHIELDED_TEMPERATURE = textureIdentifierForGlassStyle(
			"overlay_shielded_temperature.png");
	private static final Identifier THERMOMETER_OVERLAY_SHIELDED_WIND = textureIdentifierForGlassStyle("overlay_shielded_wind.png");

	private static final HashMap<TEMPERATURE_LEVEL, Identifier> THERMOMETER_FILL_TEXTURES = new HashMap<>() {
		{
			put(TEMPERATURE_LEVEL.EXTREMELY_COLD, textureIdentifierForGlassStyle("fill_extremely_cold.png"));
			put(TEMPERATURE_LEVEL.VERY_COLD, textureIdentifierForGlassStyle("fill_very_cold.png"));
			put(TEMPERATURE_LEVEL.COLD, textureIdentifierForGlassStyle("fill_cold.png"));
			put(TEMPERATURE_LEVEL.SLIGHTLY_COLD, textureIdentifierForGlassStyle("fill_slightly_cold.png"));
			put(TEMPERATURE_LEVEL.NEUTRAL, textureIdentifierForGlassStyle("fill_neutral.png"));
			put(TEMPERATURE_LEVEL.SLIGHTLY_HOT, textureIdentifierForGlassStyle("fill_slightly_hot.png"));
			put(TEMPERATURE_LEVEL.HOT, textureIdentifierForGlassStyle("fill_hot.png"));
			put(TEMPERATURE_LEVEL.VERY_HOT, textureIdentifierForGlassStyle("fill_very_hot.png"));
			put(TEMPERATURE_LEVEL.EXTREMELY_HOT, textureIdentifierForGlassStyle("fill_extremely_hot.png"));
		}
	};

	private static final HashMap<TEMPERATURE_CHANGE_INDICATOR, Identifier> THERMOMETER_OUTLINE_TEXTURES = new HashMap<>() {
		{
			put(TEMPERATURE_CHANGE_INDICATOR.EXTREME_COOLING, textureIdentifierForGlassStyle("outline_extreme_cooling.png"));
			put(TEMPERATURE_CHANGE_INDICATOR.REGULAR_COOLING, textureIdentifierForGlassStyle("outline_cooling.png"));
			put(TEMPERATURE_CHANGE_INDICATOR.NEUTRAL, textureIdentifierForGlassStyle("outline_neutral.png"));
			put(TEMPERATURE_CHANGE_INDICATOR.REGULAR_HEATING, textureIdentifierForGlassStyle("outline_heating.png"));
			put(TEMPERATURE_CHANGE_INDICATOR.EXTREME_HEATING, textureIdentifierForGlassStyle("outline_extreme_heating.png"));
		}
	};

	// Rendering (Glass Thermometer)

	public static void renderGlassThermometerHud(DrawContext context) {
		var client = MinecraftClient.getInstance();
		var window = client.getWindow();

		var xOffset = Mod.CONFIG.temperatureXOffset;
		var yOffset = Mod.CONFIG.temperatureYOffset;

		var x = window.getScaledWidth() / 2 + xOffset;
		var y = window.getScaledHeight() - 48 + yOffset;

		var bodyTemperature = ModClient.getBodyTemperature();
		var ambientTemperature = ModClient.getAmbientTemperature();
		var acclimatizationRate = ModClient.getAcclimatizationRate();
		var localWindIntensity = ModClient.getLocalWindIntensity();
		var effectiveWindIntensity = ModClient.getEffectiveWindIntensity();
		var isShieldedInInterior = ModClient.getIsPlayerInInterior();
		var isShieldedFromWind = effectiveWindIntensity < localWindIntensity * 0.15;

		if (bodyTemperature == 0.0) {
			return;
		}

		if (!client.player.isSpectator() && !client.player.isCreative()) {
			var offset = applyGlassShakeForRender(bodyTemperature);
			var glassTexture = selectGlassThermometerFillTexture(bodyTemperature);
			var outlineTexture = selectGlassThermometerOutlineTexture(bodyTemperature, ambientTemperature, acclimatizationRate);

			var positionX = x + offset.x;
			var positionY = y + offset.y;

			context.drawTexture(glassTexture, positionX - 8, positionY - 10, 0, 0, 16, 21, 16, 21);
			context.drawTexture(outlineTexture, positionX - 8, positionY - 10, 0, 0, 16, 21, 16, 21);

			if (isShieldedInInterior) {
				var overlayTexture = THERMOMETER_OVERLAY_SHIELDED_TEMPERATURE;
				context.drawTexture(overlayTexture, positionX - 8, positionY - 10, 0, 0, 16, 21, 16, 21);
			} else if (isShieldedFromWind) {
				var overlayTexture = THERMOMETER_OVERLAY_SHIELDED_WIND;
				context.drawTexture(overlayTexture, positionX - 8, positionY - 10, 0, 0, 16, 21, 16, 21);
			}
		}
	}

	private static Point applyGlassShakeForRender(double temperature) {
		var burnThresholdMinor = Mod.CONFIG.hyperthermiaThresholdMinor;
		var burnThresholdMajor = Mod.CONFIG.hyperthermiaThresholdMajor;
		var freezeThresholdMinor = Mod.CONFIG.hypothermiaThresholdMinor;
		var freezeThresholdMajor = Mod.CONFIG.hypothermiaThresholdMajor;

		var newGlassShakeTickMax = 0;
		var newGlassShakeAxis = false;

		// Determine shake properties based on temperature

		if (temperature < freezeThresholdMinor + 1 && temperature > freezeThresholdMajor) {
			newGlassShakeTickMax = 4;
			newGlassShakeAxis = true;
		} else if (temperature < freezeThresholdMajor + 1) {
			newGlassShakeTickMax = 3;
			newGlassShakeAxis = true;
		} else if (temperature > burnThresholdMinor - 1 && temperature < burnThresholdMajor) {
			newGlassShakeTickMax = 4;
			newGlassShakeAxis = false;
		} else if (temperature > burnThresholdMajor - 1) {
			newGlassShakeTickMax = 3;
			newGlassShakeAxis = false;
		}

		glassShakeTickMax = newGlassShakeTickMax;
		glassShakeAxis = newGlassShakeAxis;

		if (glassShakeTickMax != 0) {
			var newGlassShakeTick = glassShakeTick + 1;
			var newGlassShakePM = glassShakePM;

			if (newGlassShakeTick >= glassShakeTickMax) {
				newGlassShakeTick = 0;
				newGlassShakePM = -glassShakePM;
			}

			glassShakeTick = newGlassShakeTick;
			glassShakePM = newGlassShakePM;

			if (glassShakeAxis) {
				return new Point(glassShakePM, 0);
			} else {
				return new Point(0, glassShakePM);
			}
		}
		return new Point(0, 0);
	}

	private static Identifier selectGlassThermometerFillTexture(double bodyTemperature) {
		var temperatureLevel = temperatureLevelForPlayer(bodyTemperature);
		return THERMOMETER_FILL_TEXTURES.get(temperatureLevel);
	}

	private static TEMPERATURE_LEVEL temperatureLevelForPlayer(double bodyTemperature) {
		// Assume temperature ranges from 0 to 90

		if (bodyTemperature <= 10) {
			return TEMPERATURE_LEVEL.EXTREMELY_COLD;
		} else if (bodyTemperature <= 20) {
			return TEMPERATURE_LEVEL.VERY_COLD;
		} else if (bodyTemperature <= 30) {
			return TEMPERATURE_LEVEL.COLD;
		} else if (bodyTemperature <= 40) {
			return TEMPERATURE_LEVEL.SLIGHTLY_COLD;
		} else if (bodyTemperature <= 50) {
			return TEMPERATURE_LEVEL.NEUTRAL;
		} else if (bodyTemperature <= 60) {
			return TEMPERATURE_LEVEL.SLIGHTLY_HOT;
		} else if (bodyTemperature <= 70) {
			return TEMPERATURE_LEVEL.HOT;
		} else if (bodyTemperature <= 80) {
			return TEMPERATURE_LEVEL.VERY_HOT;
		} else {
			return TEMPERATURE_LEVEL.EXTREMELY_HOT;
		}
	}

	private static Identifier selectGlassThermometerOutlineTexture(double bodyTemperature, double ambientTemperature,
			double acclimatizationRate) {
		// Temperature difference is positive if warming up.
		var temperatureDifference = ambientTemperature - bodyTemperature;
		var temperatureThresholdMargin = 2;
		var acclimatizationRateThreshold = PlayerTemperatureUtil.applicableAcclimatizationRate(Mod.CONFIG.acclimatizationRate) * 1.5;

		// If body temperature is decreasing at a regular rate, render minor cooling.
		if (acclimatizationRate <= acclimatizationRateThreshold && temperatureDifference < -temperatureThresholdMargin) {
			return THERMOMETER_OUTLINE_TEXTURES.get(TEMPERATURE_CHANGE_INDICATOR.REGULAR_COOLING);
		}

		// If body temperature is decreasing at an elevated rate, render major cooling.
		if (acclimatizationRate > acclimatizationRateThreshold && temperatureDifference < -temperatureThresholdMargin) {
			return THERMOMETER_OUTLINE_TEXTURES.get(TEMPERATURE_CHANGE_INDICATOR.EXTREME_COOLING);
		}

		// If body temperature is increasing at a regular rate, render minor heating.
		if (acclimatizationRate <= acclimatizationRateThreshold && temperatureDifference > temperatureThresholdMargin) {
			return THERMOMETER_OUTLINE_TEXTURES.get(TEMPERATURE_CHANGE_INDICATOR.REGULAR_HEATING);
		}

		// If body temperature is increasing at an elevated rate, render major heating.
		if (acclimatizationRate > acclimatizationRateThreshold && temperatureDifference > temperatureThresholdMargin) {
			return THERMOMETER_OUTLINE_TEXTURES.get(TEMPERATURE_CHANGE_INDICATOR.EXTREME_HEATING);
		}

		// If body temperature is safe and ambient temperature is within margins, render
		// neutral.
		return THERMOMETER_OUTLINE_TEXTURES.get(TEMPERATURE_CHANGE_INDICATOR.NEUTRAL);
	}

	// Rendering (Gauge Thermometer)

	public static void renderGaugeThermometerHud(DrawContext context) {
		var client = MinecraftClient.getInstance();
		var window = client.getWindow();

		var temperature = ModClient.getBodyTemperature();
		var temperatureDifference = ModClient.getAmbientTemperature() - temperature;

		if (temperature == 0.0) {
			return;
		}

		var xOffset = Mod.CONFIG.temperatureXOffset;
		var yOffset = Mod.CONFIG.temperatureYOffset;

		var centerX = window.getScaledWidth() / 2 + xOffset;
		var centerY = window.getScaledHeight() + yOffset;

		var spacingFactor = 1.5f;
		var temperatureFraction = calculateTemperatureFraction(temperature, spacingFactor);

		context.drawTexture(THERMOMETER_GAUGE_TEXTURE, centerX - ((44 + 149) - Math.round(2 * spacingFactor)),
				centerY - (Math.round(8 * spacingFactor) + Math.round(3 * spacingFactor) + 1), 0, 0, Math.round(40 * spacingFactor),
				Math.round(9 * spacingFactor), Math.round(40 * spacingFactor), Math.round(9 * spacingFactor));

		context.drawTexture(THERMOMETER_HAND_TEXTURE, centerX - (int) (((44 + 149) - Math.round(2 * spacingFactor)) - temperatureFraction),
				centerY - (Math.round(8 * spacingFactor) + Math.round(3 * spacingFactor) + 1), 0, 0, Math.round(1),
				Math.round(9 * spacingFactor), Math.round(1), Math.round(9 * spacingFactor));

		var frameY = centerY - (Math.round(13 * spacingFactor) + 1);
		context.drawTexture(THERMOMETER_FRAME_TEXTURE, centerX - (44 + 149), frameY, 0, 0, Math.round(44 * spacingFactor),
				Math.round(13 * spacingFactor), Math.round(44 * spacingFactor), Math.round(13 * spacingFactor));

		var indicatorTexture = selectIndicatorTexture(temperatureDifference);
		if (indicatorTexture != null) {
			context.drawTexture(indicatorTexture, centerX - (17 + 149), centerY - (Math.round(22 * spacingFactor)), 0, 0,
					Math.round(8 * spacingFactor), Math.round(8 * spacingFactor), Math.round(8 * spacingFactor),
					Math.round(8 * spacingFactor));
		}
	}

	private static double calculateTemperatureFraction(double temperature, double spacingFactor) {
		var scaledValue = (temperature / 100f) * Math.round(40 * spacingFactor);

		if (scaledValue > 59.0f) {
			return (97f / 100f) * Math.round(40 * spacingFactor);
		}

		if ((temperature / 100f) < 0f) {
			return 0f;
		}

		return scaledValue;
	}

	private static Identifier selectIndicatorTexture(double temperatureDifference) {
		if (temperatureDifference > 0) {
			return THERMOMETER_FLAME_TEXTURE;
		}

		if (temperatureDifference < 0) {
			return THERMOMETER_SNOWFLAKE_TEXTURE;
		}

		return THERMOMETER_STILL_TEXTURE;
	}

	// Resource Utility

	private static Identifier textureIdentifierForGlassStyle(String textureName) {
		return textureIdentifier(THERMOMETER_STYLE.GLASS, textureName);
	}

	private static Identifier textureIdentifierForGaugeStyle(String textureName) {
		return textureIdentifier(THERMOMETER_STYLE.GAUGE, textureName);
	}

	private static Identifier textureIdentifierForThermometer(String textureName) {
		return new Identifier(Mod.MOD_ID, "textures/thermometer" + textureName);
	}

	private static Identifier textureIdentifier(THERMOMETER_STYLE style, String textureName) {
		return new Identifier(Mod.MOD_ID, "textures/" + textureGroupForThermometerStyle(style) + "/" + textureName);
	}

	private static String textureGroupForThermometerStyle(THERMOMETER_STYLE style) {
		switch (style) {
		case GLASS:
			return "glass_thermometer";
		case GAUGE:
			return "thermometer";
		default:
			throw new IllegalArgumentException("Invalid thermometer style: " + style);
		}
	}
}
