package net.saint.acclimatize.player;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class PlayerState extends PersistentState {

	// Properties

	public boolean isInInterior = false;

	public double bodyTemperature = 0;
	public double acclimatizationRate = 0;
	public double ambientTemperature = 0;

	public double biomeTemperature = 0;
	public double sunShadeTemperature = 0;
	public double windTemperature = 0;
	public double blockTemperature = 0;
	public double itemTemperature = 0;

	public double windIntensity = 0;

	// NBT

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putBoolean(PlayerStateNBTKeys.isInInterior, isInInterior);

		nbt.putDouble(PlayerStateNBTKeys.bodyTemperature, bodyTemperature);
		nbt.putDouble(PlayerStateNBTKeys.acclimatizationRate, acclimatizationRate);
		nbt.putDouble(PlayerStateNBTKeys.ambientTemperature, ambientTemperature);

		nbt.putDouble(PlayerStateNBTKeys.biomeTemperature, biomeTemperature);
		nbt.putDouble(PlayerStateNBTKeys.sunShadeTemperature, sunShadeTemperature);
		nbt.putDouble(PlayerStateNBTKeys.windTemperature, windTemperature);
		nbt.putDouble(PlayerStateNBTKeys.blockTemperature, blockTemperature);
		nbt.putDouble(PlayerStateNBTKeys.itemTemperature, itemTemperature);

		nbt.putDouble(PlayerStateNBTKeys.windIntensity, windIntensity);

		return nbt;
	}

	public static PlayerState fromNbt(NbtCompound nbt) {
		PlayerState playerState = new PlayerState();

		playerState.isInInterior = nbt.getBoolean(PlayerStateNBTKeys.isInInterior);

		playerState.bodyTemperature = nbt.getDouble(PlayerStateNBTKeys.bodyTemperature);
		playerState.acclimatizationRate = nbt.getDouble(PlayerStateNBTKeys.acclimatizationRate);
		playerState.ambientTemperature = nbt.getDouble(PlayerStateNBTKeys.ambientTemperature);

		playerState.biomeTemperature = nbt.getDouble(PlayerStateNBTKeys.biomeTemperature);
		playerState.sunShadeTemperature = nbt.getDouble(PlayerStateNBTKeys.sunShadeTemperature);
		playerState.windTemperature = nbt.getDouble(PlayerStateNBTKeys.windTemperature);
		playerState.blockTemperature = nbt.getDouble(PlayerStateNBTKeys.blockTemperature);
		playerState.itemTemperature = nbt.getDouble(PlayerStateNBTKeys.itemTemperature);

		playerState.windIntensity = nbt.getDouble(PlayerStateNBTKeys.windIntensity);

		return playerState;
	}

}