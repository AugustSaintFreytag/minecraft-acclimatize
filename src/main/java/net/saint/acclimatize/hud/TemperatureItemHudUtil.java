package net.saint.acclimatize.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.ModItems;

public class TemperatureItemHudUtil {

	private static final Identifier THERMOMETER_DISPLAY_TEXTURE = new Identifier(Mod.MOD_ID,
			"textures/thermometer/thermometer_display.png");

	public static void renderTemperatureItemHud(DrawContext context) {
		var client = MinecraftClient.getInstance();
		var player = client.player;
		var mainHand = player.getMainHandStack();
		var offHand = player.getOffHandStack();

		if (!Mod.CONFIG.enableThermometerTemperatureDisplay
				|| (!mainHand.isOf(ModItems.THERMOMETER_ITEM) && !offHand.isOf(ModItems.THERMOMETER_ITEM))) {
			return;
		}

		var temperature = ModClient.getBodyTemperature();

		if (temperature == 0.0) {
			return;
		}

		var xOffset = Mod.CONFIG.temperatureXOffset;
		var yOffset = Mod.CONFIG.temperatureYOffset;

		var tx = (client.getWindow().getScaledWidth() / 2) + xOffset;
		var ty = client.getWindow().getScaledHeight() + yOffset;

		var text = "§7" + (Math.round(temperature * 10.0) / 10.0);
		var textWidth = client.textRenderer.getWidth(text);
		var textHeight = client.textRenderer.fontHeight;

		float spacingFactor = 1.75f;
		int tFrameY = ty - (Math.round(textHeight * spacingFactor) + 1);

		context.drawTexture(THERMOMETER_DISPLAY_TEXTURE, (tx - (tx - textWidth)) + xOffset, tFrameY + yOffset, 0, 0,
				Math.round(textWidth * spacingFactor), Math.round(textHeight * spacingFactor), Math.round(textWidth * spacingFactor),
				Math.round(textHeight * spacingFactor));

		context.drawText(client.textRenderer, text, ((tx - (tx - textWidth)) + 6) + xOffset, (tFrameY + 7) + yOffset, 16777215, true);
	}

}
