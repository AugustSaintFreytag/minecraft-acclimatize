package net.saint.acclimatize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.data.block.BlockTemperatureUtil;
import net.saint.acclimatize.data.item.ItemTemperatureUtil;
import net.saint.acclimatize.data.wind.WindTemperatureUtil;
import net.saint.acclimatize.item.GoldSweetBerriesItem;
import net.saint.acclimatize.item.IceWaterBottleItem;
import net.saint.acclimatize.item.ThermometerItem;
import net.saint.acclimatize.item.WoolClothItem;
import net.saint.acclimatize.profiler.Profiler;
import net.saint.acclimatize.recipe.LeatherArmorWoolRecipe;
import net.saint.acclimatize.sound.WindSoundEvents;

public class Mod implements ModInitializer {
	// Metadata

	public static final String MOD_ID = "acclimatize";

	public static String MOD_VERSION;

	// Config

	public static ModConfig CONFIG;

	// Items

	public static final GoldSweetBerriesItem GOLDEN_SWEET_BERRIES_ITEM = new GoldSweetBerriesItem(new FabricItemSettings().maxCount(64));
	public static final IceWaterBottleItem ICE_WATER_BOTTLE_ITEM = new IceWaterBottleItem(new FabricItemSettings().maxCount(4));
	public static final ThermometerItem THERMOMETER_ITEM = new ThermometerItem(new FabricItemSettings().maxCount(1));
	public static final WoolClothItem WOOL_CLOTH_ITEM = new WoolClothItem(new FabricItemSettings().maxCount(64));

	// Block Items

	public static final BlockItem ICE_BOX_EMPTY_ITEM = new BlockItem(ModBlocks.ICE_BOX_EMPTY_BLOCK, new FabricItemSettings());
	public static final BlockItem ICE_BOX_FREEZING_ITEM = new BlockItem(ModBlocks.ICE_BOX_FREEZING_BLOCK, new FabricItemSettings());
	public static final BlockItem ICE_BOX_FROZEN_ITEM = new BlockItem(ModBlocks.ICE_BOX_FROZEN_BLOCK, new FabricItemSettings());

	// Special Recipes

	public static final RecipeSerializer<LeatherArmorWoolRecipe> LEATHER_ARMOR_WOOL_RECIPE_SERIALIZER = RecipeSerializer.register(
			"crafting_special_leather_armor_wool", new SpecialRecipeSerializer<LeatherArmorWoolRecipe>(LeatherArmorWoolRecipe::new));

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

		// Reload

		ItemTemperatureUtil.reloadItems();
		BlockTemperatureUtil.reloadBlocks();
		WindTemperatureUtil.reloadBlocks();

		// Registration

		registerSoundEvents();
		registerStatusEffects();
		registerItems();
		registerBlocks();
		registerServerEvents();
		registerCommands();

	}

	private static void registerStatusEffects() {
		Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "heat_dissipation"), ModStatusEffects.HEAT_DISSIPATION);
		Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "hypothermia"), ModStatusEffects.HYPOTHERMIA);
		Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "hyperthermia"), ModStatusEffects.HYPERTHERMIA);
	}

	private static void registerSoundEvents() {
		WindSoundEvents.registerAll();
	}

	private static void registerItems() {
		// Items

		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "thermometer"), THERMOMETER_ITEM);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "golden_sweet_berries"), GOLDEN_SWEET_BERRIES_ITEM);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "ice_water_bottle"), ICE_WATER_BOTTLE_ITEM);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "wool_cloth"), WOOL_CLOTH_ITEM);

		// Item Groups

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
			content.add(GOLDEN_SWEET_BERRIES_ITEM);
			content.add(ICE_WATER_BOTTLE_ITEM);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			content.add(THERMOMETER_ITEM);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
			content.add(ICE_BOX_EMPTY_ITEM);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
			content.add(WOOL_CLOTH_ITEM);
		});
	}

	private static void registerBlocks() {
		// Blocks

		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "ice_box_empty"), ModBlocks.ICE_BOX_EMPTY_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "ice_box_freezing"), ModBlocks.ICE_BOX_FREEZING_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "ice_box_frozen"), ModBlocks.ICE_BOX_FROZEN_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "smoke"), ModBlocks.SMOKE_BLOCK);

		// Block Item Registry

		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "ice_box_empty_item"), ICE_BOX_EMPTY_ITEM);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "ice_box_freezing_item"), ICE_BOX_FREEZING_ITEM);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "ice_box_frozen_item"), ICE_BOX_FROZEN_ITEM);
	}

	private static void registerServerEvents() {
		ModServerEvents.registerServerEvents();
	}

	private static void registerCommands() {
		ModCommands.registerCommands();
	}
}
