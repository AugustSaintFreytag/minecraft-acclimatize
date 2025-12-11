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
import net.minecraft.world.biome.Biome;
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

	private RingBuffer<Boolean> windSamples = makeEmptyWindSampleBuffer();

	private int numberOfRaysFired;

	private double windIntensity = 0.0;

	// Reload

	public static void reloadBlocks() {
		windPermeableBlocks = SetConfigCodingUtil.decodeStringValueSetFromRaw(Mod.CONFIG.windPermeableBlocks);
	}

	// Access

	public double getWindIntensity() {
		return windIntensity;
	}

	public int getNumberOfRaysFired() {
		return numberOfRaysFired;
	}

	// Ticking

	public void tick(ServerState serverState, ServerPlayerEntity player) {
		windIntensity = getWindIntensityFactorForPlayer(serverState, player);
	}

	// Wind (Aggregate)

	public double getWindIntensityFactorForPlayer(ServerState serverState, ServerPlayerEntity player) {
		var baseWindIntensity = serverState.windIntensity;
		var positionalWindFactor = BiomeWindUtil.positionalWindFactorForPlayer(player);
		var precipitationWindFactor = getPrecipitationWindFactorForPlayer(player);
		var unblockedWindFactor = getUnblockedWindFactorForPlayer(player, serverState.windDirection);

		return baseWindIntensity * positionalWindFactor * precipitationWindFactor * unblockedWindFactor;
	}

	// Wind (Weather)

	private static double getPrecipitationWindFactorForPlayer(ServerPlayerEntity player) {
		var world = player.getWorld();
		var position = player.getBlockPos();
		var biome = world.getBiome(position).value();
		var precipitation = biome.getPrecipitation(position);

		if (precipitation == Biome.Precipitation.RAIN) {
			if (world.isThundering()) {
				return 1.4;
			}

			if (world.isRaining()) {
				return 1.2;
			}
		} else if (precipitation == Biome.Precipitation.SNOW) {
			if (world.isRaining()) {
				return 1.1;
			}
		}

		return 1.0;
	}

	// Wind (Blocking/Raycasting)

	private double getUnblockedWindFactorForPlayer(ServerPlayerEntity player, double windDirection) {
		var unblockedRays = processUnblockedWindRaysForPlayer(player, windDirection);

		if (numberOfRaysFired == 0) {
			return 0.0;
		}

		return (double) unblockedRays / (double) numberOfRaysFired;
	}

	private int processUnblockedWindRaysForPlayer(ServerPlayerEntity player, double windDirection) {
		// Profile Start Time
		var profile = Mod.PROFILER.begin("wind");

		// Perform only a single raycast and store the result
		var windRaycastIsUnblocked = performSingleWindRaycast(player, windDirection);
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

	private boolean performSingleWindRaycast(ServerPlayerEntity player, double windDirection) {
		var world = player.getWorld();
		var random = world.getRandom();

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

	private static RingBuffer<Boolean> makeEmptyWindSampleBuffer() {
		var buffer = new RingBuffer<Boolean>(Mod.CONFIG.windRayCount);

		// Fill with false to indicate all rays are blocked initially
		buffer.fill(false);

		return buffer;
	}

	private void resetSamples() {
		windSamples = makeEmptyWindSampleBuffer();
		numberOfRaysFired = 0;
	}

}
