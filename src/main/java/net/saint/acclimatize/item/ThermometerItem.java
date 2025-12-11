package net.saint.acclimatize.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.saint.acclimatize.data.biome.BiomeTemperatureUtil;
import net.saint.acclimatize.data.world.TemperatureEstimationUtil;
import net.saint.acclimatize.server.ServerStateUtil;

public class ThermometerItem extends Item {

	public ThermometerItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		var itemStack = player.getStackInHand(hand);

		if (world.isClient()) {
			return TypedActionResult.success(itemStack);
		}

		var server = world.getServer();
		var playerState = ServerStateUtil.getPlayerState(player);
		var serverState = ServerStateUtil.getServerState(server);

		var skyAngle = world.getSkyAngle(0.0f);
		var dayNightProgression = BiomeTemperatureUtil.phaseValueFromSkyAngle(skyAngle) / (2 * Math.PI);

		player.sendMessage(Text.of(
				"♜ Body: " + formattedValue(playerState.bodyTemperature) + " (↕ Acclim " + formattedValue(playerState.acclimatizationRate)
						+ ", " + TemperatureEstimationUtil.estimateTicksToExtremeTemperatureForPlayer(playerState).description()
						+ ") \n(♯ Ambient " + formattedValue(playerState.ambientTemperature) + " at " + formattedValue(dayNightProgression)
						+ " day/night" + ", ♣ Biome " + formattedValue(playerState.biomeTemperature) + ", ☼ Sun/Shade "
						+ formattedValue(playerState.sunShadeTemperature) + ", ☰ Wind " + formattedValue(playerState.windTemperature)
						+ " from " + Math.floor(Math.toDegrees(serverState.windDirection)) + "° at "
						+ formattedValue(playerState.effectiveWindIntensity) + " (baseline " + formattedValue(serverState.windIntensity)
						+ "), ♢ Blocks " + formattedValue(playerState.blockTemperature) + ", ☍ Items "
						+ formattedValue(playerState.itemTemperature) + ", ☈ Interior " + formattedValue(playerState.isInInterior) + ")"));

		return TypedActionResult.success(itemStack);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	// Formatting

	private static String formattedValue(boolean value) {
		if (value) {
			return "Yes";
		} else {
			return "No";
		}
	}

	private static String formattedValue(double value) {
		return String.format("%.3f", value);
	}

}
