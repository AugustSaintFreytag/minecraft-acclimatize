package net.saint.acclimatize.data.world;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Precipitation;

public final class WorldUtil {

	public static boolean isRaining(World world, PlayerEntity player) {
		var position = player.getBlockPos();
		var biome = world.getBiome(position);
		var precipitation = biome.value().getPrecipitation(position);

		if (precipitation == Precipitation.SNOW) {
			return false;
		}

		return world.isRaining();
	}

	public static boolean isInCave(World world, PlayerEntity player) {
		var position = player.getBlockPos();
		var canSeeSky = world.isSkyVisible(position.up());

		if (canSeeSky) {
			return false;
		}

		var surfaceY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, position.getX(), position.getZ());
		var isBelowSurface = position.getY() < surfaceY;
		var isUnderThickCeiling = getCeilingThickness(world, position, 6) >= 3;

		return isBelowSurface && isUnderThickCeiling;
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

}
