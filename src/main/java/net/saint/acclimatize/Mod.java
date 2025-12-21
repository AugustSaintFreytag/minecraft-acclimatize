package net.saint.acclimatize;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.saint.acclimatize.data.block.BlockTemperatureUtil;
import net.saint.acclimatize.data.item.ItemTemperatureUtil;
import net.saint.acclimatize.data.space.PlayerSpaceManager;
import net.saint.acclimatize.data.wind.PlayerWindManager;
import net.saint.acclimatize.data.wind.WindManager;
import net.saint.acclimatize.profiler.Profiler;
import net.saint.acclimatize.sound.AmbienceSoundEvents;
import net.saint.acclimatize.util.Logger;

public class Mod implements ModInitializer {
	// Metadata

	public static final String MOD_ID = "acclimatize";
	public static final String MOD_NAME = "Acclimatize";

	public static String MOD_VERSION;

	// Config

	public static ModConfig CONFIG;

	// Modules

	public static final Logger LOGGER = Logger.create(MOD_NAME);
	public static final Profiler PROFILER = Profiler.getProfiler(MOD_ID);

	public static final PlayerSpaceManager PLAYER_SPACE_MANAGER = new PlayerSpaceManager();
	public static final PlayerWindManager PLAYER_WIND_MANAGER = new PlayerWindManager();

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
			WindManager.reloadBlocks();

			return null;
		});

		// Registration

		ModBlocks.registerBlocks();
		ModItems.registerItems();
		ModStatusEffects.registerStatusEffects();
		AmbienceSoundEvents.registerSoundEvents();
		ModServerEvents.registerServerEvents();
		ModCommands.registerCommands();

		// Reload

		ItemTemperatureUtil.reloadItems();
		BlockTemperatureUtil.reloadBlocks();
		WindManager.reloadBlocks();
	}
}
