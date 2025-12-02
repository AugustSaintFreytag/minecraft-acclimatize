package net.saint.acclimatize.data.world;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Precipitation;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModTags;

public final class WorldUtil {

	// Configuration

	private static final int CAVE_BLOCK_SCAN_RANGE = 5;

	// Rain Detection

	public static boolean isRaining(World world, PlayerEntity player) {
		var position = player.getBlockPos();
		var biome = world.getBiome(position);
		var precipitation = biome.value().getPrecipitation(position);

		if (precipitation == Precipitation.SNOW) {
			return false;
		}

		return world.isRaining();
	}

	// Cave Detection

	public static boolean isInCave(World world, PlayerEntity player) {
		var position = player.getBlockPos();
		var canSeeSky = world.isSkyVisible(position.up());

		if (canSeeSky) {
			return false;
		}

		var caveDepth = getApproximateCaveDepth(world, position);
		var isBelowSurface = caveDepth > 1;
		var isUnderThickCeiling = getCeilingThickness(world, position, 6) >= 3;

		if (isBelowSurface && isUnderThickCeiling) {
			return true;
		}

		var blockEnvironment = evaluateBlockEnvironment(world, player);
		var isCaveLikeEnvironment = blockEnvironment.caveBlockPercentage > 0.3 && blockEnvironment.skyExposedBlockPercentage < 0.05
				&& blockEnvironment.averageBlockLightLevel < 5.0;

		return isCaveLikeEnvironment;
	}

	public static int getApproximateCaveDepth(World world, BlockPos position) {
		var surfaceY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, position.getX(), position.getZ());
		return Math.max(0, surfaceY - position.getY());
	}

	private static int getCeilingThickness(World world, BlockPos startPos, int maxScan) {
		var thickness = 0;

		for (var y = startPos.getY() + 1; y <= startPos.getY() + maxScan; y++) {
			var pos = new BlockPos(startPos.getX(), y, startPos.getZ());

			if (world.getBlockState(pos).isOpaqueFullCube(world, pos)) {
				thickness++;
				continue;
			}

			if (thickness > 0) {
				break;
			}
		}

		return thickness;
	}

	private static BlockEnvironmentProperties evaluateBlockEnvironment(World world, PlayerEntity player) {
		var caveSearchRange = CAVE_BLOCK_SCAN_RANGE;
		var position = player.getBlockPos();

		var numberOfBlocks = 0;
		var numberOfAirBlocks = 0;
		var numberOfSolidBlocks = 0;
		var numberOfCaveBlocks = 0;
		var numberOfSkyExposedBlocks = 0;

		var totalLightValue = 0;

		for (var x = -caveSearchRange; x < caveSearchRange; x++) {
			for (var y = -caveSearchRange; y < caveSearchRange; y++) {
				for (var z = -caveSearchRange; z < caveSearchRange; z++) {
					numberOfBlocks++;

					var blockPosition = new BlockPos(position).add(x, y, z);
					var blockState = world.getBlockState(blockPosition);

					if (blockState.isAir()) {
						numberOfAirBlocks++;
						continue;
					}

					if (blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.WATER)) {
						continue;
					}

					var blockLightValue = world.getLightLevel(LightType.BLOCK, blockPosition);
					var skyLightValue = world.getLightLevel(LightType.SKY, blockPosition);

					totalLightValue += blockLightValue + skyLightValue;

					numberOfSolidBlocks++;

					if (world.isSkyVisible(blockPosition)) {
						numberOfSkyExposedBlocks++;
					}

					if (blockState.isIn(ModTags.CAVE_BLOCKS)) {
						numberOfCaveBlocks++;
					}
				}
			}
		}

		var caveBlockPercentage = (double) numberOfCaveBlocks / (double) numberOfSolidBlocks;
		var skyExposedBlocksPercentage = (double) numberOfSkyExposedBlocks / (double) numberOfSolidBlocks;
		var averageLightLevel = (double) totalLightValue / (double) numberOfAirBlocks;

		var properties = new BlockEnvironmentProperties(caveBlockPercentage, skyExposedBlocksPercentage, averageLightLevel);

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info(
					"Evaluated block environment for player '{}' at {}: Blocks = {}, Cave blocks = {}%, Sky-exposed blocks = {}%, Average light level = {}",
					player.getName().getString(), position, numberOfBlocks, properties.caveBlockPercentage,
					properties.skyExposedBlockPercentage, properties.averageBlockLightLevel);
		}

		return properties;
	}

	private record BlockEnvironmentProperties(double caveBlockPercentage, double skyExposedBlockPercentage, double averageBlockLightLevel) {
	};

}
