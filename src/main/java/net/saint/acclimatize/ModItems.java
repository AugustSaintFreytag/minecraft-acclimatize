package net.saint.acclimatize;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.item.GoldSweetBerriesItem;
import net.saint.acclimatize.item.IceWaterBottleItem;
import net.saint.acclimatize.item.ThermometerItem;
import net.saint.acclimatize.item.WoolClothItem;

public final class ModItems {

	// Items

	public static final GoldSweetBerriesItem GOLDEN_SWEET_BERRIES_ITEM = new GoldSweetBerriesItem(new FabricItemSettings().maxCount(64));
	public static final IceWaterBottleItem ICE_WATER_BOTTLE_ITEM = new IceWaterBottleItem(new FabricItemSettings().maxCount(4));
	public static final ThermometerItem THERMOMETER_ITEM = new ThermometerItem(new FabricItemSettings().maxCount(1));
	public static final WoolClothItem WOOL_CLOTH_ITEM = new WoolClothItem(new FabricItemSettings().maxCount(64));

	// Block Items

	public static final BlockItem ICE_BOX_EMPTY_ITEM = new BlockItem(ModBlocks.ICE_BOX_EMPTY_BLOCK, new FabricItemSettings());
	public static final BlockItem ICE_BOX_FREEZING_ITEM = new BlockItem(ModBlocks.ICE_BOX_FREEZING_BLOCK, new FabricItemSettings());
	public static final BlockItem ICE_BOX_FROZEN_ITEM = new BlockItem(ModBlocks.ICE_BOX_FROZEN_BLOCK, new FabricItemSettings());

	// Initialization

	public static void registerItems() {
		// Items

		Registry.register(Registries.ITEM, new Identifier(Mod.MOD_ID, "thermometer"), THERMOMETER_ITEM);
		Registry.register(Registries.ITEM, new Identifier(Mod.MOD_ID, "golden_sweet_berries"), GOLDEN_SWEET_BERRIES_ITEM);
		Registry.register(Registries.ITEM, new Identifier(Mod.MOD_ID, "ice_water_bottle"), ICE_WATER_BOTTLE_ITEM);
		Registry.register(Registries.ITEM, new Identifier(Mod.MOD_ID, "wool_cloth"), WOOL_CLOTH_ITEM);

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

}
