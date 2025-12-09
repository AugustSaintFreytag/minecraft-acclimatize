package net.saint.acclimatize.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.ModDebugRenderer;
import net.saint.acclimatize.player.PlayerState;
import net.saint.acclimatize.server.ServerState;
import net.saint.acclimatize.server.ServerStateUtil;

public class StateNetworkingPackets {

	public static class TemperaturePacketTuple {
		public double acclimatizationRate;
		public double bodyTemperature;
		public double ambientTemperature;
		public double windTemperature;
		public double windDirection;
		public double windIntensity;
		public boolean isInInterior;

		public TemperaturePacketTuple() {
			this.acclimatizationRate = 0;
			this.bodyTemperature = 0;
			this.ambientTemperature = 0;
			this.windTemperature = 0;
			this.windDirection = 0;
			this.windIntensity = 0;
			this.isInInterior = false;
		}

		public void encodeValuesToBuffer(PacketByteBuf buffer) {
			buffer.writeDouble(acclimatizationRate);
			buffer.writeDouble(bodyTemperature);
			buffer.writeDouble(ambientTemperature);
			buffer.writeDouble(windTemperature);
			buffer.writeDouble(windDirection);
			buffer.writeDouble(windIntensity);
			buffer.writeBoolean(isInInterior);
		}

		public static TemperaturePacketTuple valuesFromBuffer(PacketByteBuf buffer) {
			var tuple = new TemperaturePacketTuple();

			tuple.acclimatizationRate = buffer.readDouble();
			tuple.bodyTemperature = buffer.readDouble();
			tuple.ambientTemperature = buffer.readDouble();
			tuple.windTemperature = buffer.readDouble();
			tuple.windDirection = buffer.readDouble();
			tuple.windIntensity = buffer.readDouble();
			tuple.isInInterior = buffer.readBoolean();

			return tuple;
		}
	}

	// Packets

	public static final Identifier PLAYER_S2C_PACKET_ID = new Identifier(Mod.MOD_ID, "player_s2c_packet");

	// Reception

	public static void registerS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(PLAYER_S2C_PACKET_ID, (client, handler, buffer, responseSender) -> {
			var receivedValues = TemperaturePacketTuple.valuesFromBuffer(buffer);

			client.execute(() -> {
				ModClient.updateTemperatureValuesFromPacket(receivedValues);

				// Debugging

				if (Mod.CONFIG.enableSunVectorDebug) {
					ModDebugRenderer.renderSunVectorDebug(client);
				}
			});
		});
	}

	// Transmission

	public static void sendStateToClient(MinecraftServer server, ServerPlayerEntity player) {
		ServerState serverState = ServerStateUtil.getServerState(server);
		PlayerState playerState = ServerStateUtil.getPlayerState(player);

		var tuple = new TemperaturePacketTuple();

		tuple.acclimatizationRate = playerState.acclimatizationRate;
		tuple.bodyTemperature = playerState.bodyTemperature;
		tuple.ambientTemperature = playerState.ambientTemperature;
		tuple.windTemperature = playerState.windTemperature;
		tuple.windDirection = serverState.windDirection;
		tuple.windIntensity = serverState.windIntensity;
		tuple.isInInterior = playerState.isInInterior;

		var outgoingBuffer = PacketByteBufs.create();
		tuple.encodeValuesToBuffer(outgoingBuffer);

		ServerPlayNetworking.send(player, PLAYER_S2C_PACKET_ID, outgoingBuffer);
	}
}
