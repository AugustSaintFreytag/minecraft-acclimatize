package net.saint.acclimatize;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.saint.acclimatize.data.item.ItemTemperatureUtil;
import net.saint.acclimatize.data.space.SpaceManager;
import net.saint.acclimatize.networking.StateNetworkingPackets;
import net.saint.acclimatize.networking.StateNetworkingPackets.TemperaturePacketTuple;
import net.saint.acclimatize.sound.AmbienceSoundManager;
import net.saint.acclimatize.util.MathUtil;

public class ModClient implements ClientModInitializer {

	// References

	public static SpaceManager SPACE_MANAGER;
	public static AmbienceSoundManager AMBIENCE_SOUND_MANAGER;

	private static KeyBinding HUD_KEYBINDING;

	// State

	private static TemperaturePacketTuple cachedTemperatureValues = new TemperaturePacketTuple();

	private static long clientTickElapsed = 0;

	private static long lastWindUpdateTick = 0;

	private static double lastEffectiveWindIntensity = 0;
	private static double lastLocalWindIntensity = 0;
	private static double lastLocalWindDirection = 0;

	// Properties

	public static boolean enableHUD = true;

	// Init

	@Override
	public void onInitializeClient() {
		setUpParticles();
		setUpKeybindings();
		setUpNetworkingPacketRegistration();
		setUpItemTooltipCallback();
		setUpClientEvents();
		setUpAmbienceSoundManager();
		setUpClientSpaceManager();
	}

	// Globals

	public static MinecraftClient getClient() {
		return MinecraftClient.getInstance();
	}

	private static ClientWorld getWorld() {
		return getClient().world;
	}

	public static ClientPlayerEntity getPlayer() {
		return getClient().player;
	}

	// Access

	public static long getClientTickElapsed() {
		return clientTickElapsed;
	}

	public static void resetClientTickElapsed() {
		clientTickElapsed = 0;
	}

	public static void incrementClientTickElapsed() {
		clientTickElapsed++;
	}

	public static void updateTemperatureValuesFromPacket(TemperaturePacketTuple values) {
		var world = MinecraftClient.getInstance().world;
		var serverTick = world.getTimeOfDay();
		var previousValues = cachedTemperatureValues;

		cachedTemperatureValues = values;

		if (previousValues.localWindDirection != values.localWindDirection) {
			lastLocalWindDirection = previousValues.localWindDirection;
			lastWindUpdateTick = serverTick;
		}

		if (previousValues.playerWindIntensity != values.playerWindIntensity) {
			lastEffectiveWindIntensity = previousValues.playerWindIntensity;
			lastWindUpdateTick = serverTick;
		}
	}

	public static double getAcclimatizationRate() {
		return cachedTemperatureValues.playerAcclimatizationRate;
	}

	public static double getBodyTemperature() {
		return cachedTemperatureValues.playerBodyTemperature;
	}

	public static double getAmbientTemperature() {
		return cachedTemperatureValues.playerAmbientTemperature;
	}

	public static double getWindTemperature() {
		return cachedTemperatureValues.playerWindTemperature;
	}

	public static double getEffectiveWindIntensity() {
		return MathUtil.lerp(lastEffectiveWindIntensity, cachedTemperatureValues.playerWindIntensity, windInterpolationValue());
	}

	public static double getLocalWindIntensity() {
		return MathUtil.lerp(lastLocalWindIntensity, cachedTemperatureValues.localWindIntensity, windInterpolationValue());
	}

	public static double getLocalWindDirection() {
		return MathUtil.lerp(lastLocalWindDirection, cachedTemperatureValues.localWindDirection, windInterpolationValue());
	}

	public static boolean getIsPlayerInInterior() {
		return SPACE_MANAGER.isPlayerInInterior();
	}

	// Transforms

	private static double windInterpolationValue() {
		var serverTick = getWorld().getTime();
		var deltaTime = serverTick - lastWindUpdateTick;
		var transitionFactor = (double) deltaTime / (double) Mod.CONFIG.windTransitionInterval;

		return MathUtil.clamp(transitionFactor, 0.0, 1.0);
	}

	// Set-Up

	private static void setUpParticles() {
		ModParticles.registerParticles();
	}

	private static void setUpKeybindings() {
		HUD_KEYBINDING = KeyBindingHelper
				.registerKeyBinding(new KeyBinding("Toggle Temperature GUI", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "Acclimatize"));

		if (HUD_KEYBINDING.wasPressed()) {
			enableHUD = !enableHUD;
		}
	}

	private static void setUpNetworkingPacketRegistration() {
		StateNetworkingPackets.registerS2CPackets();
	}

	private static void setUpClientEvents() {
		ModClientEvents.registerClientEvents();
	}

	private static void setUpItemTooltipCallback() {
		ItemTooltipCallback.EVENT.register((stack, context, tooltip) -> {
			if (!stack.hasNbt()) {
				return;
			}

			var rawTemperature = ItemTemperatureUtil.temperatureValueForItem(stack);

			if (rawTemperature == 0) {
				return;
			}

			var temperature = Math.round(rawTemperature * 10.0) / 10.0;
			var prefix = temperature > 0 ? "§9+" : "§c";

			tooltip.add(Text.literal(prefix + temperature + " Temperature"));
		});
	}

	// Space

	public static void resetClientSpaceManager() {
		SPACE_MANAGER.clearBuffer();
	}

	private static void setUpAmbienceSoundManager() {
		AMBIENCE_SOUND_MANAGER = new AmbienceSoundManager();
	}

	private static void setUpClientSpaceManager() {
		var spaceTickInterval = Mod.CONFIG.temperatureTickInterval / Math.max(1, Mod.CONFIG.clientTickFactor);
		var spaceNumberOfRaysTotal = Mod.CONFIG.spaceNumberOfRaysTotal * Mod.CONFIG.clientRayCastFactor;
		var spaceNumberOfRaysCastPerTick = Mod.CONFIG.spaceNumberOfRaysCastPerTick * Mod.CONFIG.clientTickFactor;
		var spaceRayLength = Mod.CONFIG.spaceRayLength;

		SPACE_MANAGER = new SpaceManager(spaceTickInterval, spaceNumberOfRaysTotal, spaceNumberOfRaysCastPerTick, spaceRayLength, true);
	}

}
