package net.saint.acclimatize;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.block.IceBoxEmptyBlock;
import net.saint.acclimatize.block.IceBoxFreezingBlock;
import net.saint.acclimatize.block.IceBoxFrozenBlock;
import net.saint.acclimatize.block.SmokeBlock;

public class ModBlocks {

	public static final IceBoxEmptyBlock ICE_BOX_EMPTY_BLOCK = new IceBoxEmptyBlock(FabricBlockSettings.create().strength(1.0f));
	public static final IceBoxFreezingBlock ICE_BOX_FREEZING_BLOCK = new IceBoxFreezingBlock(
			FabricBlockSettings.create().strength(1.0f).ticksRandomly());
	public static final IceBoxFrozenBlock ICE_BOX_FROZEN_BLOCK = new IceBoxFrozenBlock(
			FabricBlockSettings.create().strength(2.0f).ticksRandomly());
	public static final SmokeBlock SMOKE_BLOCK = new SmokeBlock(
			FabricBlockSettings.create().replaceable().noCollision().dropsNothing().air());

	public static void registerBlocks() {
		// Blocks

		Registry.register(Registries.BLOCK, new Identifier(Mod.MOD_ID, "ice_box_empty"), ModBlocks.ICE_BOX_EMPTY_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(Mod.MOD_ID, "ice_box_freezing"), ModBlocks.ICE_BOX_FREEZING_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(Mod.MOD_ID, "ice_box_frozen"), ModBlocks.ICE_BOX_FROZEN_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(Mod.MOD_ID, "smoke"), ModBlocks.SMOKE_BLOCK);

		// Block Item Registry

		Registry.register(Registries.ITEM, new Identifier(Mod.MOD_ID, "ice_box_empty_item"), ModItems.ICE_BOX_EMPTY_ITEM);
		Registry.register(Registries.ITEM, new Identifier(Mod.MOD_ID, "ice_box_freezing_item"), ModItems.ICE_BOX_FREEZING_ITEM);
		Registry.register(Registries.ITEM, new Identifier(Mod.MOD_ID, "ice_box_frozen_item"), ModItems.ICE_BOX_FROZEN_ITEM);
	}

}
