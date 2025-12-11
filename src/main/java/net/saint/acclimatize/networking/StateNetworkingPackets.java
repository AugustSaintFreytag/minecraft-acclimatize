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
		public double playerAcclimatizationRate;
		public double playerBodyTemperature;
		public double playerAmbientTemperature;
		public double playerWindTemperature;
		public double playerWindIntensity;
		public double localWindIntensity;
		public double localWindDirection;

		public TemperaturePacketTuple() {
			this.playerAcclimatizationRate = 0;
			this.playerBodyTemperature = 0;
			this.playerAmbientTemperature = 0;
			this.playerWindTemperature = 0;
			this.playerWindIntensity = 0;
			this.localWindDirection = 0;
		}

		public void encodeValuesToBuffer(PacketByteBuf buffer) {
			buffer.writeDouble(playerAcclimatizationRate);
			buffer.writeDouble(playerBodyTemperature);
			buffer.writeDouble(playerAmbientTemperature);
			buffer.writeDouble(playerWindTemperature);
			buffer.writeDouble(playerWindIntensity);
			buffer.writeDouble(localWindIntensity);
			buffer.writeDouble(localWindDirection);
		}

		public static TemperaturePacketTuple valuesFromBuffer(PacketByteBuf buffer) {
			var tuple = new TemperaturePacketTuple();

			tuple.playerAcclimatizationRate = buffer.readDouble();
			tuple.playerBodyTemperature = buffer.readDouble();
			tuple.playerAmbientTemperature = buffer.readDouble();
			tuple.playerWindTemperature = buffer.readDouble();
			tuple.playerWindIntensity = buffer.readDouble();
			tuple.localWindIntensity = buffer.readDouble();
			tuple.localWindDirection = buffer.readDouble();

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

		tuple.playerAcclimatizationRate = playerState.acclimatizationRate;
		tuple.playerBodyTemperature = playerState.bodyTemperature;
		tuple.playerAmbientTemperature = playerState.ambientTemperature;
		tuple.playerWindTemperature = playerState.windTemperature;
		tuple.playerWindIntensity = playerState.effectiveWindIntensity;
		tuple.localWindIntensity = playerState.localWindIntensity;
		tuple.localWindDirection = serverState.windDirection;

		var outgoingBuffer = PacketByteBufs.create();
		tuple.encodeValuesToBuffer(outgoingBuffer);

		ServerPlayNetworking.send(player, PLAYER_S2C_PACKET_ID, outgoingBuffer);
	}
}
