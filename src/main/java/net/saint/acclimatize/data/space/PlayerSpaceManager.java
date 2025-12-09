package net.saint.acclimatize.data.space;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;

public final class PlayerSpaceManager {

	// State

	private final Map<UUID, SpaceManager> spaceProbeByPlayer = new HashMap<>();

	// Access

	public SpaceManager getManagerForPlayer(UUID playerUuid) {
		return spaceProbeByPlayer.computeIfAbsent(playerUuid, uuid -> new SpaceManager());
	}

	public SpaceManager getManagerForPlayer(PlayerEntity player) {
		return getManagerForPlayer(player.getUuid());
	}

	public void clearManagerForPlayer(UUID playerUuid) {
		spaceProbeByPlayer.remove(playerUuid);
	}

	public void clearManagerForPlayer(PlayerEntity player) {
		clearManagerForPlayer(player.getUuid());
	}

}
