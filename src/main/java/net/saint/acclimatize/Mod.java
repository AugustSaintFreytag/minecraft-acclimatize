package net.saint.acclimatize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.saint.acclimatize.data.block.BlockTemperatureUtil;
import net.saint.acclimatize.data.item.ItemTemperatureUtil;
import net.saint.acclimatize.data.space.PlayerSpaceProbeManager;
import net.saint.acclimatize.data.wind.WindTemperatureUtil;
import net.saint.acclimatize.profiler.Profiler;
import net.saint.acclimatize.sound.AmbienceSoundEvents;

public class Mod implements ModInitializer {
	// Metadata

	public static final String MOD_ID = "acclimatize";

	public static String MOD_VERSION;

	// Config

	public static ModConfig CONFIG;

	// Modules

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Profiler PROFILER = Profiler.getProfiler(MOD_ID);

	// Init

	@Override
	public void onInitialize() {
		// Metadata

		var fabricLoader = FabricLoader.getInstance();

		fabricLoader.getModContainer(MOD_ID).ifPresent(modContainer -> {
			MOD_VERSION = modContainer.getMetadata().getVersion().getFriendlyString();
		});

		// Config

		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener((config, data) -> {
			ItemTemperatureUtil.reloadItems();
			BlockTemperatureUtil.reloadBlocks();
			WindTemperatureUtil.reloadBlocks();
			WindTemperatureUtil.cleanUpAllPlayerData();

			return null;
		});

		// Registration

		ModStatusEffects.registerStatusEffects();
		AmbienceSoundEvents.registerSoundEvents();
		ModBlocks.registerBlocks();
		ModItems.registerItems();
		ModServerEvents.registerServerEvents();
		ModCommands.registerCommands();

		// Reload

		ItemTemperatureUtil.reloadItems();
		BlockTemperatureUtil.reloadBlocks();
		WindTemperatureUtil.reloadBlocks();
	}
}
