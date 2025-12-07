package net.saint.acclimatize.mixinlogic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.AshParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.client.particle.RainSplashParticle;
import net.minecraft.client.particle.SnowflakeParticle;
import net.minecraft.client.particle.WhiteAshParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModClient;
import net.saint.acclimatize.compat.FallingLeafParticleCompat;
import net.saint.acclimatize.compat.ParticleRainParticleCompat;
import net.saint.acclimatize.data.world.WorldUtil;
import net.saint.acclimatize.util.MathUtil;

public interface ParticleMixinLogic {

	// Properties

	public Vec3d getPosition();

	public void setPosition(Vec3d position);

	Box getBoundingBox();

	MinecraftClient getClient();

	ClientWorld getWorld();

	double getHeatValue();

	void setHeatValue(double value);

	// Delta Calculation

	default double calculateDeltaX(double dx) {
		var position = getPosition();
		var motionVector = motionVectorForParticle();

		updateMotionFromHeatValueAtPosition(position);

		return dx + motionVector.x;
	}

	default double calculateDeltaZ(double dz) {
		var position = getPosition();
		var motionVector = motionVectorForParticle();

		updateMotionFromHeatValueAtPosition(position);

		return dz + motionVector.z;
	}

	// Motion Vector

	private Vec3d motionVectorForParticle() {
		var position = getPosition();
		updateMotionFromHeatValueAtPosition(position);

		var windVector = calculateWindVector();
		var motionVector = new Vec3d(-windVector.z, 0, windVector.x);
		var motionFactor = motionFactorForParticle(position);

		return motionVector.multiply(motionFactor);
	}

	private double motionFactorForParticle(Vec3d particlePosition) {
		var direction = ModClient.getWindDirection();
		var particleDirection = new Vec3d(MathUtil.cos(direction), 0, MathUtil.sin(direction));

		var baseFactor = windInfluenceFactorForUnblockedOppositeDirection(particlePosition, particleDirection);
		var spaceFactor = windInfluenceForCurrentSpace();
		var typeFactor = windInfluenceFactorForParticleType();

		return baseFactor * spaceFactor * typeFactor * Mod.CONFIG.particleWindEffectFactor;
	}

	// Calculation Details

	private double windInfluenceForCurrentSpace() {
		var player = ModClient.getPlayer();
		var world = getWorld();

		if (WorldUtil.isInCave(world, player)) {
			// In cave, no wind influence.
			return 0.0;
		}

		if (ModClient.getIsPlayerInInterior()) {
			// Player is in interior, minor wind influence.
			return 0.25;
		}

		return 1.0;
	}

	private double windInfluenceFactorForUnblockedOppositeDirection(Vec3d particlePosition, Vec3d windDirection) {
		if (!Mod.CONFIG.enableParticleBlockChecks) {
			// Block checks disabled, always return full influence.
			return 1.0;
		}

		var world = getWorld();

		// Define how far back in wind origin direction we should check.
		var range = 4;

		// Invert wind direction for checking.
		var invertedWindDirection = windDirection.multiply(-1);

		for (int i = 1; i <= range; i++) {
			var checkPosition = particlePosition.add(invertedWindDirection.multiply(i));
			var blockPosition = BlockPos.ofFloored(checkPosition);
			var state = world.getBlockState(blockPosition);

			if (state.isAir()) {
				// If block in path is air, check for fluid or return max influence.

				if (Mod.CONFIG.enableParticleFluidBlockChecks) {
					// Check if particle is within 0.5 blocks away from a fluid block.
					var fluidDistance = findDistanceToNearestFluidBlock(blockPosition, particlePosition);

					if (fluidDistance < 0.5) {
						// No influence if within 0.5 blocks away from fluid.
						return 0.0;
					}
				}

				// Continue checking until block is found.
				continue;
			} else {
				// Solid block found, return influence based on distance.
				// Not sure what checking for non-solid blocks like lava or water was about.
				return 0.0;
			}
		}

		// No blocks found in path, return full influence.
		return 1.0;
	}

	private double findDistanceToNearestFluidBlock(BlockPos blockPosition, Vec3d particlePosition) {
		var fluidPosition = findPositionOfNearestFluidBlock(blockPosition);

		if (fluidPosition == null) {
			// No fluid block found, return max distance.
			return 1000;
		}

		var fluidPositionCenter = fluidPosition.toCenterPos();
		return particlePosition.distanceTo(fluidPositionCenter);
	}

	private BlockPos findPositionOfNearestFluidBlock(BlockPos position) {
		var world = getWorld();

		for (var x = -1; x <= 1; x++) {
			for (var y = -1; y <= 1; y++) {
				for (var z = -1; z <= 1; z++) {
					var nearbyPos = position.add(x, y, z);
					var nearbyState = world.getBlockState(nearbyPos);

					if (isNonSolidBlock(nearbyState)) {
						return nearbyPos;
					}
				}
			}
		}
		return null;
	}

	private boolean isNonSolidBlock(BlockState state) {
		return state.getFluidState().getFluid() == Fluids.WATER || state.getFluidState().getFluid() == Fluids.LAVA;
	}

	private Vec3d calculateWindVector() {
		var world = getWorld();

		if (!world.getRegistryKey().equals(World.OVERWORLD)) {
			// Return 0 wind strength if not in overworld.
			return new Vec3d(0, 0, 0);
		}

		var windDirection = ModClient.getWindDirection();
		var windIntensity = ModClient.getWindIntensity();

		var windX = Math.cos(windDirection) * windIntensity * 0.01;
		var windZ = Math.sin(windDirection) * windIntensity * 0.01;
		var initialWindVector = new Vec3d(windX, 0, windZ);
		var position = getPosition();

		if (!Mod.CONFIG.enableParticleComplexInteractions) {
			// No complex interactions, return initial wind vector.
			return initialWindVector;
		}

		return windVectorFromRealisticFlow(initialWindVector, position);
	}

	private Vec3d windVectorFromRealisticFlow(Vec3d windVector, Vec3d particlePosition) {
		if (checkWorldForWallProximityNearParticle(particlePosition)) {
			windVector = adjustWindFlow(windVector, particlePosition, windVector.x, windVector.z);
		}

		windVector = windVectorForStructureAvoidance(windVector, particlePosition);

		if (!Mod.CONFIG.enableParticleFunneling) {
			return windVector;
		}

		return windVectorForFunnelAttraction(windVector, particlePosition);
	}

	private Vec3d adjustWindFlow(Vec3d windVector, Vec3d particlePosition, double windX, double windZ) {
		var windDirection = windDirectionForComponents(windX, windZ);
		var wallDirection = wallFacingDirectionForPosition(particlePosition, windDirection);
		var incidenceAngle = calculateIncidenceAngle(windDirection, wallDirection);

		if (incidenceAngleIsLaminarFlow(incidenceAngle)) {
			return wallGuidedDirectionForWind(windVector, wallDirection);
		} else {
			return deflectedDirectionForWind(windVector, particlePosition);
		}
	}

	private Vec3d deflectedDirectionForWind(Vec3d windEffect, Vec3d particlePosition) {
		var windDirection = windDirectionForComponents(windEffect.x, windEffect.z);
		var wallDirection = wallFacingDirectionForPosition(particlePosition, windDirection);
		var incidenceAngle = calculateIncidenceAngle(windDirection, wallDirection);
		var deflectionFactor = deflectionFactorForWindAndAngle(incidenceAngle, windEffect.x, windEffect.z);

		var deflectedWindX = windEffect.x * deflectionFactor;
		var deflectedWindZ = windEffect.z * deflectionFactor;

		deflectedWindX += randomizedDeflection(incidenceAngle);
		deflectedWindZ += randomizedDeflection(incidenceAngle);

		return new Vec3d(deflectedWindX, 0, deflectedWindZ);
	}

	private double calculateIncidenceAngle(Direction windDirection, Direction wallDirection) {
		var windAngle = angleFromDirection(windDirection);
		var wallAngle = angleFromDirection(wallDirection);
		var angleDifference = Math.abs(windAngle - wallAngle);

		if (angleDifference > 180) {
			angleDifference = 360 - angleDifference;
		}

		return angleDifference;
	}

	private int angleFromDirection(Direction direction) {
		return switch (direction) {
		case NORTH -> 180;
		case WEST -> 270;
		case EAST -> 90;
		default -> 0;
		};
	}

	private double deflectionFactorForWindAndAngle(double incidenceAngle, double windX, double windZ) {
		var baseDeflection = 0.01;
		var velocityFactor = MathUtil.sqrt(windX * windX + windZ * windZ) * 0.01;
		var angleFactor = MathUtil.sqrt(Math.toRadians(incidenceAngle));

		return baseDeflection * angleFactor * velocityFactor;
	}

	private double randomizedDeflection(double incidenceAngle) {
		var world = getWorld();
		var random = world.getRandom();

		return random.nextDouble() * MathUtil.cos(Math.toRadians(incidenceAngle)) * 0.05;
	}

	private Vec3d wallGuidedDirectionForWind(Vec3d windVector, Direction wallDirection) {
		return switch (wallDirection) {
		case NORTH, SOUTH -> new Vec3d(windVector.x, windVector.y, 0);
		case EAST, WEST -> new Vec3d(0, windVector.y, windVector.z);
		default -> windVector;
		};
	}

	private boolean incidenceAngleIsLaminarFlow(double incidenceAngle) {
		return incidenceAngle < 45;
	}

	private Direction windDirectionForComponents(double windX, double windZ) {
		var angle = Math.toDegrees(Math.atan2(windZ, windX));

		if (angle < 0)
			angle += 360;
		if (angle <= 45 || angle > 315)
			return Direction.EAST;
		if (angle > 45 && angle <= 135)
			return Direction.SOUTH;
		if (angle > 135 && angle <= 225)
			return Direction.WEST;
		if (angle > 225)
			return Direction.NORTH;

		return Direction.EAST;
	}

	private Direction wallFacingDirectionForPosition(Vec3d particlePosition, Direction windDirection) {
		var world = getWorld();
		var blockPosition = BlockPos.ofFloored(particlePosition);

		for (var direction : Direction.values()) {
			var state = world.getBlockState(blockPosition.offset(direction));

			if (state.isSolidBlock(world, blockPosition.offset(direction)) && direction.getAxis().isHorizontal()) {
				return direction;
			}
		}

		return windDirection;
	}

	private Vec3d windVectorForStructureAvoidance(Vec3d windVector, Vec3d particlePosition) {
		var windDirection = windDirectionForComponents(windVector.x, windVector.z);
		var wallDirection = wallFacingDirectionForPosition(particlePosition, windDirection);
		var incidenceAngle = calculateIncidenceAngle(windDirection, wallDirection);

		if (incidenceAngle >= 45 && incidenceAngle <= 135) {
			var funnelFactor = 1.0 + (1.0 - MathUtil.cos(Math.toRadians(incidenceAngle))) * 0.5;
			return windVector.multiply(funnelFactor);
		}

		return windVector;
	}

	private Vec3d windVectorForFunnelAttraction(Vec3d windVector, Vec3d particlePosition) {
		if (checkWorldForFunnelProximityNearParticle(particlePosition)) {
			var attractionFactor = 1.5;
			return windVector.multiply(attractionFactor);
		}

		return windVector;
	}

	private boolean checkWorldForFunnelProximityNearParticle(Vec3d particlePosition) {
		var numberOfAirBlocks = 0;
		var numberOfSolidBlocks = 0;

		var world = getWorld();
		var blockPosition = BlockPos.ofFloored(particlePosition);

		for (var dx = -1; dx <= 1; dx++) {
			for (var dy = -1; dy <= 1; dy++) {
				for (var dz = -1; dz <= 1; dz++) {
					var checkPos = blockPosition.add(dx, dy, dz);
					var state = world.getBlockState(checkPos);

					if (state.isAir()) {
						numberOfAirBlocks++;
					} else if (state.isSolidBlock(world, checkPos)) {
						numberOfSolidBlocks++;
					}
				}
			}
		}

		return numberOfAirBlocks >= 15 && numberOfSolidBlocks >= 10;
	}

	private boolean checkWorldForWallProximityNearParticle(Vec3d particlePosition) {
		var world = getWorld();
		var blockPosition = BlockPos.ofFloored(particlePosition);

		for (var direction : Direction.values()) {
			var state = world.getBlockState(blockPosition.offset(direction));

			if (state.isSolidBlock(world, blockPosition.offset(direction))) {
				return true;
			}
		}

		return false;
	}

	// Heat

	private void updateMotionFromHeatValueAtPosition(Vec3d particlePosition) {
		if (!Mod.CONFIG.enableParticleHeatEffects || !shouldApplyHeatBasedWindEffects()) {
			// No heat-based wind effects for blacklisted particle type.
			return;
		}

		// Reset heat value for new tick.
		setHeatValue(0.0);

		var world = getWorld();
		var blockPosition = BlockPos.ofFloored(particlePosition);
		var maxHeatInfluenceDistance = 4.0;
		var heatValueIncrement = 0.05;

		var checkPosition = new BlockPos.Mutable();
		checkPosition.set(blockPosition);

		for (; checkPosition.getY() >= 0; checkPosition.move(Direction.DOWN)) {
			var state = world.getBlockState(checkPosition);

			if (blockIsHeatSource(state)) {
				var destinationPos = new Vec3d(checkPosition.getX() + 0.5, checkPosition.getY() + 0.5, checkPosition.getZ() + 0.5);
				var distance = particlePosition.distanceTo(destinationPos);

				if (distance <= maxHeatInfluenceDistance) {
					var influence = (maxHeatInfluenceDistance - distance) / maxHeatInfluenceDistance;
					setHeatValue(getHeatValue() + heatValueIncrement * influence);

					var maxHeatValue = 1.0;

					// Cap the heat value to `maxHeatValue`.
					setHeatValue(Math.min(getHeatValue(), maxHeatValue));

					// Bail after finding the first heat source.
					break;
				}
			}
		}

		if (getHeatValue() > 0) {
			var motionY = liftFromHeatValue();
			setVerticalMotion(motionY);
		} else {
			setVerticalMotion(0);
		}
	}

	private double liftFromHeatValue() {
		// Multiply heatValue by a factor to determine lift strength
		return getHeatValue() * 0.1;
	}

	private boolean blockIsHeatSource(BlockState state) {
		return state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE) || state.isOf(Blocks.TORCH) || state.isOf(Blocks.CAMPFIRE);
	}

	private void setVerticalMotion(double motionY) {
		var position = getPosition();
		var updatedPosition = new Vec3d(position.getX(), position.getY() + motionY, position.getZ());

		setPosition(updatedPosition);
	}

	// Particle Specifics

	private double windInfluenceFactorForParticleType() {
		if (this instanceof SnowflakeParticle) {
			return 1.75;
		}

		if (this instanceof RainSplashParticle) {
			return 0.25;
		}

		if (this instanceof AshParticle || this instanceof WhiteAshParticle) {
			return 2.0;
		}

		if (ParticleRainParticleCompat.isRainParticle(this)) {
			return 0.85;
		}

		if (FallingLeafParticleCompat.isLeafParticle(this)) {
			return 1.5;
		}

		return 1.0;
	}

	private boolean shouldApplyHeatBasedWindEffects() {
		if (this instanceof LargeFireSmokeParticle) {
			return true;
		}

		return false;
	}
}
