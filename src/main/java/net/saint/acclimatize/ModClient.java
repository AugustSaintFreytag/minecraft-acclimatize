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
	private static double lastWindIntensity = 0;
	private static double lastWindDirection = 0;

	// Properties

	public static boolean enableHUD = true;

	// Init

	@Override
	public void onInitializeClient() {
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

		if (previousValues.windDirection != values.windDirection) {
			lastWindDirection = previousValues.windDirection;
			lastWindUpdateTick = serverTick;
		}

		if (previousValues.windIntensity != values.windIntensity) {
			lastWindIntensity = previousValues.windIntensity;
			lastWindUpdateTick = serverTick;
		}
	}

	public static double getAcclimatizationRate() {
		return cachedTemperatureValues.acclimatizationRate;
	}

	public static double getBodyTemperature() {
		return cachedTemperatureValues.bodyTemperature;
	}

	public static double getAmbientTemperature() {
		return cachedTemperatureValues.ambientTemperature;
	}

	public static double getWindTemperature() {
		return cachedTemperatureValues.windTemperature;
	}

	public static double getWindDirection() {
		return MathUtil.lerp(lastWindDirection, cachedTemperatureValues.windDirection, windInterpolationValue());
	}

	public static boolean getIsPlayerInInterior() {
		return SPACE_MANAGER.isPlayerInInterior();
	}

	public static double getWindIntensity() {
		return MathUtil.lerp(lastWindIntensity, cachedTemperatureValues.windIntensity, windInterpolationValue())
				* windPrecipitationFactor();
	}

	// Transforms

	private static double windInterpolationValue() {
		var serverTick = getWorld().getTime();
		var deltaTime = serverTick - lastWindUpdateTick;
		var transitionFactor = (double) deltaTime / (double) Mod.CONFIG.windTransitionInterval;

		return MathUtil.clamp(transitionFactor, 0.0, 1.0);
	}

	private static double windPrecipitationFactor() {
		var world = getWorld();

		if (world.isThundering()) {
			return 3.0;
		} else if (world.isRaining()) {
			return 2.0;
		}

		return 1.0;
	}

	// Set-Up

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
