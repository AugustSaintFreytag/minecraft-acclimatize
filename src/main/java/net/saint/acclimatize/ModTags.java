package net.saint.acclimatize;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModTags {

	public static final TagKey<Block> CAVE_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(Mod.MOD_ID, "cave_blocks"));
	public static final TagKey<Block> OUTDOOR_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(Mod.MOD_ID, "outdoor_blocks"));

}
