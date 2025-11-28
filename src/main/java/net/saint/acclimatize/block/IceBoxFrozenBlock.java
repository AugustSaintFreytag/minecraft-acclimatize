package net.saint.acclimatize.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModBlocks;
import net.saint.acclimatize.data.biome.BiomeTemperatureUtil;

public class IceBoxFrozenBlock extends Block {

	public IceBoxFrozenBlock(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

		if (!world.isClient) {
			world.setBlockState(pos, ModBlocks.ICE_BOX_EMPTY_BLOCK.getDefaultState());
			IceBoxFrozenBlock.dropStack(world, pos, new ItemStack(Items.ICE, 3));
		}

		return ActionResult.SUCCESS;
	}

	public void randomTick(BlockState state, ServerWorld world, BlockPos position, Random random) {
		if (world.isClient()) {
			return;
		}

		var biomeBaseTemperature = BiomeTemperatureUtil.biomeTemperatureForPosition(world, position);
		var biomeIsCold = biomeBaseTemperature <= Mod.CONFIG.hypothermiaThresholdMinor;
		var biomeIsVeryCold = biomeBaseTemperature <= Mod.CONFIG.hypothermiaThresholdMajor;

		if (biomeIsVeryCold && random.nextFloat() < 0.02) {
			world.setBlockState(position, ModBlocks.ICE_BOX_EMPTY_BLOCK.getDefaultState());
			return;
		}

		if (biomeIsCold && random.nextFloat() < 0.15) {
			world.setBlockState(position, ModBlocks.ICE_BOX_FREEZING_BLOCK.getDefaultState());
			return;
		}
	}

}