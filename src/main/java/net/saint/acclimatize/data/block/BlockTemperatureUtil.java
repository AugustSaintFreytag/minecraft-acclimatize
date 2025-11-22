package net.saint.acclimatize.data.block;

import java.util.HashMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.config.MapConfigCodingUtil;

public final class BlockTemperatureUtil {

	private static HashMap<String, Double> blockTemperatureById;

	public static void reloadBlocks() {
		blockTemperatureById = allBlockTemperatureById();
	}

	public static double temperatureDeltaForBlocksInVicinity(ServerPlayerEntity player) {
		var radius = Mod.CONFIG.blockTemperatureRadius;
		var distanceFalloffFactor = Mod.CONFIG.blockTemperatureDistanceFalloffFactor;

		var world = player.getWorld();
		var playerPosition = player.getBlockPos();

		var aggregateTemperatureDelta = 0.0;

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius / 2; y <= radius / 2; y++) {
				for (int z = -radius; z <= radius; z++) {
					var blockPosition = playerPosition.add(x, y, z);
					var blockState = world.getBlockState(blockPosition);
					var blockId = Registries.BLOCK.getId(blockState.getBlock()).toString();
					var blockTemperature = temperatureDeltaForBlock(world, blockPosition, blockState, blockId);

					if (blockTemperature == 0.0) {
						continue;
					}

					var blockDistance = Math.sqrt(blockPosition.getSquaredDistance(player.getPos()));
					var fallOffTemperature = blockTemperature * Math.exp(-distanceFalloffFactor * blockDistance);

					aggregateTemperatureDelta += Math.min(blockTemperature, fallOffTemperature);
				}
			}
		}

		aggregateTemperatureDelta = Math.max(Mod.CONFIG.blockTemperatureAbsoluteMinimum, aggregateTemperatureDelta);
		aggregateTemperatureDelta = Math.min(Mod.CONFIG.blockTemperatureAbsoluteMaximum, aggregateTemperatureDelta);

		aggregateTemperatureDelta += waterDeltaForPlayer(player);

		return aggregateTemperatureDelta;
	}

	private static double waterDeltaForPlayer(ServerPlayerEntity player) {
		if (player.isSubmergedInWater()) {
			return Mod.CONFIG.waterBlockTemperature;
		}

		if (player.isTouchingWaterOrRain()) {
			return Mod.CONFIG.waterBlockTemperature * 0.75;
		}

		return 0.0;
	}

	private static double temperatureDeltaForBlock(World world, BlockPos position, BlockState blockState, String blockId) {
		var blockTemperature = rawBlockTemperatureForId(blockId);

		if (blockTemperature == 0) {
			return 0.0;
		}

		// Block-specific exceptions

		if ((blockState.isOf(Blocks.CAMPFIRE) || blockState.isOf(Blocks.SOUL_CAMPFIRE)) && !blockState.get(CampfireBlock.LIT)) {
			return 0.0;
		}

		if ((blockState.isOf(Blocks.FURNACE) || blockState.isOf(Blocks.BLAST_FURNACE) || blockState.isOf(Blocks.SMOKER))
				&& !blockState.get(FurnaceBlock.LIT)) {
			return 0.0;
		}

		if (blockId.contains(":stove") && blockState.getLuminance() == 0) {
			return 0.0;
		}

		if ((blockId.contains("_lamp") || blockId.contains("_light"))
				&& (!blockState.contains(Properties.POWERED) || blockState.getLuminance() == 0)) {
			return 0.0;
		}

		return blockTemperature;
	}

	private static double rawBlockTemperatureForId(String blockId) {
		if (blockTemperatureById.containsKey(blockId)) {
			return blockTemperatureById.get(blockId);
		}

		return 0;
	}

	private static HashMap<String, Double> allBlockTemperatureById() {
		var allBlocks = new HashMap<String, Double>();

		allBlocks.putAll(MapConfigCodingUtil.decodeDoubleValueMapFromRaw(Mod.CONFIG.heatingBlocks));
		allBlocks.putAll(MapConfigCodingUtil.decodeDoubleValueMapFromRaw(Mod.CONFIG.coolingBlocks));

		return allBlocks;
	}

}
