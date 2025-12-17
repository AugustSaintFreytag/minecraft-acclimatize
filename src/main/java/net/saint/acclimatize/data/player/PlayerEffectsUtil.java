package net.saint.acclimatize.data.player;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModStatusEffects;
import net.saint.acclimatize.player.PlayerState;

public final class PlayerEffectsUtil {

	private static final int EFFECT_DURATION = 420; // 20+1 seconds
	private static final int EFFECT_TICK_INTERVAL = 10; // ticks per temperature tick

	private static int effectTick = -1;

	// Library

	public static enum TemperatureDamageKind {
		HEAT, COLD
	}

	public static enum TemperatureIntensityKind {
		MINOR, MAJOR
	}

	public static class TemperatureDamageTuple {
		public TemperatureDamageKind kind;
		public TemperatureIntensityKind intensity;

		public TemperatureDamageTuple(TemperatureDamageKind kind, TemperatureIntensityKind intensity) {
			this.kind = kind;
			this.intensity = intensity;
		}
	}

	// Handling

	public static void tickPlayerEffectsInSchedule(ServerPlayerEntity player, PlayerState playerState) {
		var world = player.getWorld();

		if (player.isSpectator() || player.isCreative()) {
			return;
		}

		if (world.getTimeOfDay() % Mod.CONFIG.temperatureTickInterval != 0) {
			return;
		}

		tickPlayerEffects(player, playerState);
	}

	public static void tickPlayerEffects(ServerPlayerEntity player, PlayerState playerState) {
		effectTick++;

		if (effectTick != 0 && effectTick < EFFECT_TICK_INTERVAL) {
			return;
		}

		var temperatureDamageTuple = temperatureDamageTupleForPlayerState(playerState);

		if (temperatureDamageTuple == null) {
			effectTick = -1;
			return;
		}

		effectTick = 0;

		if (player.getHealth() <= 0.0) {
			playerState.bodyTemperature = 50;
			return;
		}

		if (temperatureDamageTuple.kind == TemperatureDamageKind.COLD) {
			applyHypothermiaStatusEffects(player, temperatureDamageTuple.intensity);
		} else if (temperatureDamageTuple.kind == TemperatureDamageKind.HEAT) {
			applyHyperthermiaStatusEffects(player, temperatureDamageTuple.intensity);
		}
	}

	private static void applyHypothermiaStatusEffects(ServerPlayerEntity player, TemperatureIntensityKind intensity) {
		var playerHasHypothermia = player.hasStatusEffect(ModStatusEffects.HYPOTHERMIA);

		if (intensity == TemperatureIntensityKind.MINOR) {
			applyHypothermiaStatusEffect(player, 0);

			if (playerHasHypothermia) {
				applyHungerStatusEffect(player, 2);
			}
		} else {
			applyHypothermiaStatusEffect(player, 1);

			if (playerHasHypothermia) {
				applyHungerStatusEffect(player, 4);
			}
		}
	}

	private static void applyHyperthermiaStatusEffects(ServerPlayerEntity player, TemperatureIntensityKind intensity) {
		var playerHasHyperthermia = player.hasStatusEffect(ModStatusEffects.HYPERTHERMIA);

		if (intensity == TemperatureIntensityKind.MINOR) {
			applyHyperthermiaStatusEffect(player, 0);

			if (playerHasHyperthermia) {
				applyThirstStatusEffect(player, 0);
			}
		} else {
			applyHyperthermiaStatusEffect(player, 1);

			if (playerHasHyperthermia) {
				applyThirstStatusEffect(player, 1);
			}
		}
	}

	private static void applyHypothermiaStatusEffect(ServerPlayerEntity player, int amplifier) {
		var hypothermiaStatusEffect = new StatusEffectInstance(ModStatusEffects.HYPOTHERMIA, EFFECT_DURATION, amplifier);
		player.addStatusEffect(hypothermiaStatusEffect);
	}

	private static void applyHyperthermiaStatusEffect(ServerPlayerEntity player, int amplifier) {
		var hyperthermiaStatusEffect = new StatusEffectInstance(ModStatusEffects.HYPERTHERMIA, EFFECT_DURATION, amplifier);
		player.addStatusEffect(hyperthermiaStatusEffect);
	}

	private static void applyHungerStatusEffect(ServerPlayerEntity player, int amplifier) {
		var hungerStatusEffect = new StatusEffectInstance(StatusEffects.HUNGER, EFFECT_DURATION, amplifier);
		player.addStatusEffect(hungerStatusEffect);
	}

	private static void applyThirstStatusEffect(ServerPlayerEntity player, int amplifier) {
		if (!FabricLoader.getInstance().isModLoaded("dehydration")) {
			return;
		}

		var thirstStatusEffectId = new Identifier("dehydration", "thirst");
		var thirstStatusEffectType = Registries.STATUS_EFFECT.get(thirstStatusEffectId);
		var thirstStatusEffect = new StatusEffectInstance(thirstStatusEffectType, EFFECT_DURATION, amplifier);
		player.addStatusEffect(thirstStatusEffect);
	}

	// Damage

	private static TemperatureDamageTuple temperatureDamageTupleForPlayerState(PlayerState playerState) {
		var bodyTemperature = playerState.bodyTemperature;

		if (bodyTemperature <= Mod.CONFIG.hypothermiaThresholdMinor && bodyTemperature > Mod.CONFIG.hypothermiaThresholdMajor) {
			return new TemperatureDamageTuple(TemperatureDamageKind.COLD, TemperatureIntensityKind.MINOR);
		}

		if (bodyTemperature <= Mod.CONFIG.hypothermiaThresholdMajor) {
			return new TemperatureDamageTuple(TemperatureDamageKind.COLD, TemperatureIntensityKind.MAJOR);
		}

		if (bodyTemperature >= Mod.CONFIG.hyperthermiaThresholdMinor && bodyTemperature < Mod.CONFIG.hyperthermiaThresholdMajor) {
			return new TemperatureDamageTuple(TemperatureDamageKind.HEAT, TemperatureIntensityKind.MINOR);
		}

		if (bodyTemperature >= Mod.CONFIG.hyperthermiaThresholdMajor) {
			return new TemperatureDamageTuple(TemperatureDamageKind.HEAT, TemperatureIntensityKind.MAJOR);
		}

		return null;
	}

}
