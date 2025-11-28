package net.saint.acclimatize;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.text.Text;
import net.saint.acclimatize.data.wind.WindUtil;
import net.saint.acclimatize.server.ServerState;
import net.saint.acclimatize.server.ServerStateUtil;

public final class ModCommands {

	public static void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("acclimatize")
				// Reset temperature
				.then(literal("resetTemperature").requires(source -> source.hasPermissionLevel(4))
						.then(argument("player", EntityArgumentType.player()).executes(context -> {
							var player = EntityArgumentType.getPlayer(context, "player");
							var playerState = ServerStateUtil.getPlayerState(player);

							playerState.bodyTemperature = 50.0;
							playerState.markDirty();

							return 1;
						})))

				// Randomize wind
				.then(literal("randomizeWind").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var server = context.getSource().getServer();
					var serverWorld = server.getOverworld();

					var serverState = ServerStateUtil.getServerState(server);
					WindUtil.tickWindDirectionAndIntensity(serverWorld, serverState);

					context.getSource().sendMessage(Text.literal("Wind randomized."));
					context.getSource().sendMessage(
							Text.literal("Wind Direction: " + formattedValue(Math.toDegrees(serverState.windDirection)) + "°"));
					context.getSource().sendMessage(Text.literal("Wind Intensity: " + formattedValue(serverState.windIntensity)));

					return 1;
				}))

				// Force north wind for debugging
				.then(literal("debugForceNorthWind").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var server = context.getSource().getServer();
					var serverState = ServerStateUtil.getServerState(server);

					WindUtil.overrideWind(serverState, Math.toRadians(0.5), 5.0);

					context.getSource().sendMessage(Text.literal("Wind set to straight north."));
					context.getSource().sendMessage(
							Text.literal("Wind Direction: " + formattedValue(Math.toDegrees(serverState.windDirection)) + "°"));
					context.getSource().sendMessage(Text.literal("Wind Intensity: " + formattedValue(serverState.windIntensity)));

					return 1;
				}))

				// Print wind info
				.then(literal("printWind").requires(source -> source.hasPermissionLevel(4)).executes(context -> {

					ServerState serverState = ServerStateUtil.getServerState(context.getSource().getServer());

					context.getSource().sendMessage(
							Text.literal("§eWind Direction: §6" + formattedValue(Math.toDegrees(serverState.windDirection)) + "°"));
					context.getSource().sendMessage(Text.literal("§eWind Intensity: §6" + formattedValue(serverState.windIntensity)));

					return 1;
				}))));
	}

	// Formatting

	private static String formattedValue(double value) {
		return String.format("%.3f", value);
	}

}
