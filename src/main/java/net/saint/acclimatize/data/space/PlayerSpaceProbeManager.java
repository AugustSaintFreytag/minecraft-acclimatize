package net.saint.acclimatize.data.space;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;

public final class PlayerSpaceProbeManager {

	// State

	private final Map<UUID, SpaceProbeManager> spaceProbeByPlayer = new HashMap<>();

	// Access

	public SpaceProbeManager getManagerForPlayer(UUID playerUuid) {
		return spaceProbeByPlayer.computeIfAbsent(playerUuid, uuid -> new SpaceProbeManager());
	}

	public SpaceProbeManager getManagerForPlayer(PlayerEntity player) {
		return getManagerForPlayer(player.getUuid());
	}

	public void clearManagerForPlayer(UUID playerUuid) {
		spaceProbeByPlayer.remove(playerUuid);
	}

	public void clearManagerForPlayer(PlayerEntity player) {
		clearManagerForPlayer(player.getUuid());
	}

}
