package net.saint.acclimatize;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.saint.acclimatize.data.player.PlayerEffectsUtil;
import net.saint.acclimatize.data.player.PlayerTemperatureUtil;
import net.saint.acclimatize.data.wind.WindTemperatureUtil;
import net.saint.acclimatize.data.wind.WindUtil;
import net.saint.acclimatize.networking.StateNetworkingPackets;
import net.saint.acclimatize.player.PlayerState;
import net.saint.acclimatize.server.ServerState;
import net.saint.acclimatize.server.ServerStateUtil;

public final class ModServerEvents {

	// Logic

	public static void registerServerEvents() {
		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Registering server events.");
		}

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var serverState = ServerStateUtil.getServerState(server);

			if (!Mod.MOD_VERSION.equals(serverState.worldVersion)) {
				serverState.worldVersion = Mod.MOD_VERSION;
				serverState.markDirty();
			}

			var player = handler.player;
			var playerState = ServerStateUtil.getPlayerState(player);

			if (playerState.bodyTemperature == 0.0) {
				playerState.bodyTemperature = 50.0;
				playerState.markDirty();
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			var player = handler.player;

			Mod.PLAYER_SPACE_MANAGER.clearManagerForPlayer(player);

			WindTemperatureUtil.cleanUpPlayerData(player);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			var serverState = ServerStateUtil.getServerState(server);
			var serverWorld = server.getOverworld();

			if (Mod.CONFIG.enableLogging) {
				Mod.LOGGER.info("Randomizing new wind direction and intensity at server start.");
			}

			WindUtil.tickWindInSchedule(serverWorld, serverState);
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			var server = world.getServer();

			if (world.getRegistryKey() != World.OVERWORLD) {
				// All end tick callbacks are called thrice by default.
				// Filter out ticks for non-overworld dimensions.
				return;
			}

			tickServerInSchedule(server);
			tickAllPlayersInSchedule(server);
		});
	}

	private static void tickServerInSchedule(MinecraftServer server) {
		var serverState = ServerStateUtil.getServerState(server);
		var serverWorld = server.getOverworld();

		WindUtil.tickWindInSchedule(serverWorld, serverState);
	}

	private static void tickAllPlayersInSchedule(MinecraftServer server) {
		var serverState = ServerStateUtil.getServerState(server);

		for (var player : server.getPlayerManager().getPlayerList()) {
			var playerState = ServerStateUtil.getPlayerState(player);

			tickPlayerInSchedule(serverState, playerState, player);

			if (serverState.isDirty() || playerState.isDirty()) {
				StateNetworkingPackets.sendStateToClient(server, player);
			}
		}
	}

	private static void tickPlayerInSchedule(ServerState serverState, PlayerState playerState, ServerPlayerEntity player) {
		// Temperature
		PlayerTemperatureUtil.tickPlayerTemperatureInSchedule(player, serverState, playerState);

		// Damage
		PlayerEffectsUtil.tickPlayerEffectsInSchedule(player, playerState);
	}

}
