package net.saint.acclimatize.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModBlocks;
import net.saint.acclimatize.data.biome.BiomeTemperatureUtil;

public class IceBoxFreezingBlock extends Block {

	public IceBoxFreezingBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos position, Random random) {
		if (world.isClient()) {
			return;
		}

		var biomeBaseTemperature = BiomeTemperatureUtil.biomeTemperatureForPosition(world, position);
		var biomeIsCold = biomeBaseTemperature <= Mod.CONFIG.hypothermiaThresholdMinor;

		if (biomeIsCold && random.nextFloat() < 0.2) {
			world.setBlockState(position, ModBlocks.ICE_BOX_FROZEN_BLOCK.getDefaultState());
		}
	}

}