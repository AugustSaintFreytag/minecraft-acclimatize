package net.saint.acclimatize.data.wind;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.config.SetConfigCodingUtil;
import net.saint.acclimatize.library.common.RingBuffer;
import net.saint.acclimatize.server.ServerState;
import net.saint.acclimatize.util.MathUtil;

public final class WindManager {

	// Configuration

	private static final double WIND_RAY_TURBULENCE = Math.toRadians(35);

	private static Set<String> windPermeableBlocks = new HashSet<String>();

	// State

	private RingBuffer<Boolean> windSamples;

	private int numberOfRaysFired;

	// Reload

	public static void reloadBlocks() {
		windPermeableBlocks = SetConfigCodingUtil.decodeStringValueSetFromRaw(Mod.CONFIG.windPermeableBlocks);
	}

	// Access

	public int getNumberOfRaysFired() {
		return numberOfRaysFired;
	}

	// Wind

	public double getWindIntensityFactorForPlayer(ServerState serverState, ServerPlayerEntity player) {
		var unblockedRays = getUnblockedWindRaysForPlayer(serverState, player);

		if (numberOfRaysFired == 0) {
			return 0.0;
		}

		return (double) unblockedRays / (double) numberOfRaysFired;
	}

	private int getUnblockedWindRaysForPlayer(ServerState serverState, ServerPlayerEntity player) {
		// Profile Start Time
		var profile = Mod.PROFILER.begin("wind");

		// Perform only a single raycast and store the result
		var windRaycastIsUnblocked = performSingleWindRaycast(serverState, player);
		windSamples.enqueue(windRaycastIsUnblocked);

		// Update number of rays fired
		numberOfRaysFired = Math.min(numberOfRaysFired + 1, Mod.CONFIG.windRayCount);

		// Count unblocked rays in the buffer
		var numberOfUnblockedRays = 0;

		for (var isUnblocked : windSamples) {
			if (Boolean.TRUE.equals(isUnblocked)) {
				numberOfUnblockedRays++;
			}
		}

		profile.end();

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Wind raycast, " + numberOfUnblockedRays + " unblocked ray(s), duration: " + profile.getDescription());
		}

		return numberOfUnblockedRays;
	}

	private static RingBuffer<Boolean> makeEmptyWindSampleBuffer() {
		var buffer = new RingBuffer<Boolean>(Mod.CONFIG.windRayCount);

		// Fill with false to indicate all rays are blocked initially
		buffer.fill(false);

		return buffer;
	}

	private boolean performSingleWindRaycast(ServerState serverState, ServerPlayerEntity player) {
		var world = player.getWorld();
		var random = world.getRandom();

		var windDirection = serverState.windDirection;
		var turbulentAngle = windDirection + Math.PI + random.nextTriangular(0, WIND_RAY_TURBULENCE);
		var directionVector = new Vec3d(MathUtil.sin(turbulentAngle), 0, MathUtil.cos(turbulentAngle));

		var startVector = new Vec3d(player.getPos().x, player.getPos().y + 1, player.getPos().z);
		var endVector = startVector.add(directionVector.multiply(Mod.CONFIG.windRayLength));

		var hitResult = world.raycast(
				new RaycastContext(startVector, endVector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

		// Return true if ray is unblocked (missed all blocks)
		if (hitResult.getType() == HitResult.Type.MISS) {
			return true;
		}

		var blockPosition = BlockPos.ofFloored(hitResult.getPos());
		var block = world.getBlockState(blockPosition).getBlock();

		return blockIsWindPermeable(block);
	}

	private static boolean blockIsWindPermeable(Block block) {
		var blockId = Registries.BLOCK.getId(block).toString();
		return windPermeableBlocks.contains(blockId);
	}

	// Buffer

	private void resetSamples() {
		windSamples = makeEmptyWindSampleBuffer();
		numberOfRaysFired = 0;
	}

}
