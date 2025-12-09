package net.saint.acclimatize;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.effect.HeatDissipationStatusEffect;
import net.saint.acclimatize.effect.HyperthermiaStatusEffect;
import net.saint.acclimatize.effect.HypothermiaStatusEffect;

public class ModStatusEffects {

	// Major Effects

	public static final StatusEffect HYPOTHERMIA = new HypothermiaStatusEffect();
	public static final StatusEffect HYPERTHERMIA = new HyperthermiaStatusEffect();

	// Item Effects

	public static final StatusEffect HEAT_DISSIPATION = new HeatDissipationStatusEffect();

	// Registration

	public static void registerStatusEffects() {
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Mod.MOD_ID, "heat_dissipation"), ModStatusEffects.HEAT_DISSIPATION);
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Mod.MOD_ID, "hypothermia"), ModStatusEffects.HYPOTHERMIA);
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Mod.MOD_ID, "hyperthermia"), ModStatusEffects.HYPERTHERMIA);
	}

}
