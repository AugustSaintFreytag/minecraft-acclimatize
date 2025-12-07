package net.saint.acclimatize.data.wind;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

public final class WindTemperatureUtil {

	// Configuration

	private static final double WIND_RAY_TURBULENCE = Math.toRadians(35);
	private static final int WIND_LOW_ELEVATION_CUTOFF = 20;

	// State

	private static Set<String> windPermeableBlocks = new HashSet<String>();

	private static Map<UUID, RingBuffer<Boolean>> windSamplesByPlayer = new HashMap<>();
	private static Map<UUID, Integer> numberOfRaysFiredByPlayer = new HashMap<>();

	// Init

	public static void reloadBlocks() {
		windPermeableBlocks = SetConfigCodingUtil.decodeStringValueSetFromRaw(Mod.CONFIG.windPermeableBlocks);
	}

	// Wind Effects

	public static double windTemperatureForEnvironment(ServerState serverState, ServerPlayerEntity player, boolean isInInterior) {
		var world = player.getWorld();
		var dimension = world.getDimension();

		if (!Mod.CONFIG.enableWind || isInInterior || player.isSubmergedInWater() || !dimension.natural()) {
			cleanUpPlayerData(player);
			return 0.0;
		}

		if (player.getBlockPos().getY() <= WIND_LOW_ELEVATION_CUTOFF) {
			cleanUpPlayerData(player);
			return 0.0;
		}

		// Base Wind Temperature

		var windTemperature = serverState.windIntensity * Mod.CONFIG.windChillFactor;

		// Elevation Wind Factor

		var elevationWindFactor = elevationWindFactorForPlayer(player);
		windTemperature *= elevationWindFactor;

		// Precipitation Wind Chill

		var precipitationWindFactor = precipitationWindFactorForPlayer(serverState, player);
		windTemperature *= precipitationWindFactor;

		// Wind Raycast Hit Factor

		var numberOfUnblockedRays = getUnblockedWindRaysForPlayer(serverState, player);
		var numberOfRaysFired = numberOfRaysFiredByPlayer.getOrDefault(player.getUuid(), 1);
		var windHitTemperatureFactor = ((double) numberOfUnblockedRays / (double) numberOfRaysFired);

		windTemperature *= windHitTemperatureFactor;

		return windTemperature;
	}

	private static double elevationWindFactorForPlayer(ServerPlayerEntity player) {
		var positionY = player.getBlockPos().getY();

		if (positionY <= 20) {
			return 0.0;
		}

		if (positionY <= 60) {
			return (positionY - 20) / 40.0;
		}

		if (positionY <= 100) {
			return 1.0 + (positionY - 60) / 80.0;
		}

		return 1.5;
	}

	private static double precipitationWindFactorForPlayer(ServerState serverState, ServerPlayerEntity player) {
		var world = player.getWorld();
		var position = player.getBlockPos();
		var biome = world.getBiome(position).value();
		var precipitation = biome.getPrecipitation(position);

		if (precipitation == Biome.Precipitation.RAIN) {
			if (world.isThundering()) {
				return 1.3;
			} else {
				return 1.1;
			}
		} else if (precipitation == Biome.Precipitation.SNOW) {
			if (world.isRaining()) {
				return 1.2;
			}
		}

		return 1.0;
	}

	private static int getUnblockedWindRaysForPlayer(ServerState serverState, ServerPlayerEntity player) {
		var playerId = player.getUuid();

		// Initialize buffer for this player if needed
		var windSampleBuffer = windSamplesByPlayer.getOrDefault(playerId, makeEmptyWindSampleBuffer());

		// Profile Start Time
		var profile = Mod.PROFILER.begin("wind");

		// Perform only a single raycast and store the result
		var windRaycastIsUnblocked = performSingleWindRaycast(serverState, player);
		windSampleBuffer.enqueue(windRaycastIsUnblocked);

		// Update number of rays fired
		var numberOfRaysFired = Math.min(numberOfRaysFiredByPlayer.getOrDefault(playerId, 0) + 1, Mod.CONFIG.windRayCount);
		numberOfRaysFiredByPlayer.put(playerId, numberOfRaysFired);

		// Count unblocked rays in the buffer
		var numberOfUnblockedRays = 0;

		for (var isUnblocked : windSampleBuffer) {
			if (isUnblocked) {
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

	private static boolean performSingleWindRaycast(ServerState serverState, ServerPlayerEntity player) {
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

	public static void cleanUpPlayerData(ServerPlayerEntity player) {
		var playerId = player.getUuid();
		windSamplesByPlayer.remove(playerId);
	}

	public static void cleanUpAllPlayerData() {
		windSamplesByPlayer.clear();
	}

}
