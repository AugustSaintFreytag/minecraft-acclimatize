package net.saint.acclimatize;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.saint.acclimatize.data.wind.WindParticleUtil;

public final class ModClientEvents {

	// Events

	public static void registerClientEvents() {
		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Registering client events.");
		}

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ModClient.resetClientTickElapsed();
			ModClient.resetClientSpaceManager();
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			var world = client.world;

			if (world == null || !world.isClient() || client.player == null) {
				ModClient.AMBIENCE_SOUND_MANAGER.tick(client, true);
				return;
			}

			ModClient.incrementClientTickElapsed();

			var isPaused = client.isInSingleplayer() && client.isPaused();

			if (!isPaused) {
				tickActiveClient(client, world);
			}
		});
	}

	// Ticking

	private static void tickActiveClient(MinecraftClient client, World world) {
		ModClient.SPACE_MANAGER.tickIfScheduled(client.player);
		ModClient.AMBIENCE_SOUND_MANAGER.tick(client, false);

		if (Mod.CONFIG.enableWindParticles) {
			WindParticleUtil.spawnWindParticles(client);
		}

		if (Mod.CONFIG.enableSkyAngleLogging && world.getTime() % 20 == 0) {
			var skyAngle = world.getSkyAngle(1.0f);
			client.player.sendMessage(Text.of("Current sky angle: " + skyAngle));
		}
	}

}
