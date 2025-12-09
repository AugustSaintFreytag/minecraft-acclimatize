package net.saint.acclimatize.data.wind;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerWindManager {

	// State

	private final Map<UUID, WindManager> windManagerByPlayer = new HashMap<>();

	// Access

	public WindManager getManagerForPlayer(UUID playerUuid) {
		return windManagerByPlayer.computeIfAbsent(playerUuid, uuid -> new WindManager());
	}

	public WindManager getManagerForPlayer(ServerPlayerEntity player) {
		return getManagerForPlayer(player.getUuid());
	}

	public void clearManagerForPlayer(UUID playerUuid) {
		windManagerByPlayer.remove(playerUuid);
	}

	public void clearManagerForPlayer(ServerPlayerEntity player) {
		clearManagerForPlayer(player.getUuid());
	}

	public void clearAllManagers() {
		windManagerByPlayer.clear();
	}

}
