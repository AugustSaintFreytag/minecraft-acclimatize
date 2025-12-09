package net.saint.acclimatize.data.space;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModTags;
import net.saint.acclimatize.util.MathUtil;

public final class SpaceProbeManager {

	// Configuration

	private static final double CONE_ANGLE = Math.toRadians(45);
	private static final double BASE_COS_ANGLE = MathUtil.cos(CONE_ANGLE);
	private static final double BASE_SIN_ANGLE = MathUtil.sin(CONE_ANGLE);

	// State

	private boolean[] spaceBuffer = new boolean[Mod.CONFIG.spaceNumberOfRaysTotal];

	private int spaceIndex = 0;

	// Checks

	public boolean checkPlayerIsInInterior(PlayerEntity player) {
		var profile = Mod.PROFILER.begin("space_check");
		var world = player.getWorld();

		// Pre-check by raycasting once straight up from player position.
		var preCheckRaycastIsInInterior = performStandaloneRaycastForPositionInInterior(world, player);

		if (!preCheckRaycastIsInInterior) {
			// Pre-check raycast hit did not hit blocks, assume exterior.
			// Having a single block above your head does not make an interior
			// but having no block above your head definitively makes an exterior.
			clearBuffer();
			profile.end();

			if (Mod.CONFIG.enableLogging) {
				Mod.LOGGER.info("Space check raycast (hit sky, pre-check), duration: " + profile.getDescription());
			}

			return false;
		}

		if (!performAccumulativeRaycastsForPositionInInterior(world, player)) {
			// Rays in ring buffer did not hit blocks, assume exterior.
			profile.end();

			if (Mod.CONFIG.enableLogging) {
				Mod.LOGGER.info("Space check raycast (cast " + Mod.CONFIG.spaceNumberOfRaysCastPerTick
						+ " rays, hit sky, extended check), duration: " + profile.getDescription());
			}

			return false;
		}

		profile.end();

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Space check raycast (cast " + Mod.CONFIG.spaceNumberOfRaysCastPerTick
					+ " rays, hit block, extended check), duration: " + profile.getDescription());
		}

		return true;
	}

	private boolean checkPlayerStandingOnNonExteriorBlock(PlayerEntity player) {
		var world = player.getWorld();
		var playerPos = BlockPos.ofFloored(player.getPos());
		var blockBelowPos = playerPos.down();
		var blockBelowState = world.getBlockState(blockBelowPos);

		return !blockBelowState.isIn(ModTags.OUTDOOR_BLOCKS);
	}

	private boolean performStandaloneRaycastForPositionInInterior(World world, PlayerEntity player) {
		var rayLength = Mod.CONFIG.spaceRayLength;
		var direction = new Vec3d(0, 1, 0);

		// Define origin as slightly above player position to avoid self or vehicle collision.
		var origin = player.getPos().add(direction);
		var target = origin.add(direction.multiply(rayLength));

		var hitResult = world
				.raycast(new RaycastContext(origin, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

		return !raycastResultHitVoid(world, hitResult);
	}

	private boolean performAccumulativeRaycastsForPositionInInterior(World world, PlayerEntity player) {
		var raysPerTick = Mod.CONFIG.spaceNumberOfRaysCastPerTick;
		var lastResult = false;

		for (int i = 0; i < raysPerTick; i++) {
			lastResult = performNextAccumulativeRaycastForPositionInInterior(world, player);
		}

		return lastResult;
	}

	private boolean performNextAccumulativeRaycastForPositionInInterior(World world, PlayerEntity player) {
		// Calculate ray offset for this check
		var rayOffset = spaceIndex % Mod.CONFIG.spaceNumberOfRaysTotal;

		// Perform single raycast and store result (true = ray hit sky)
		spaceBuffer[spaceIndex] = performSingleSpaceRaycast(world, player, rayOffset);
		// Update index for next call
		spaceIndex = (spaceIndex + 1) % Mod.CONFIG.spaceNumberOfRaysTotal;

		// Count rays that hit sky - any hit means we're outside
		for (var didHitVoid : spaceBuffer) {
			if (didHitVoid) {
				// Found a ray that hit sky, assume exterior space
				return false;
			}
		}

		// All rays hit blocks, player is indoors
		return true;
	}

	private boolean performSingleSpaceRaycast(World world, PlayerEntity player, int offset) {
		var origin = player.getPos();
		var theta = 2 * Math.PI * offset / Mod.CONFIG.spaceNumberOfRaysTotal;
		var direction = new Vec3d(BASE_SIN_ANGLE * MathUtil.cos(theta), BASE_COS_ANGLE, BASE_SIN_ANGLE * MathUtil.sin(theta));
		var target = origin.add(direction.multiply(Mod.CONFIG.spaceRayLength));

		var hitResult = world
				.raycast(new RaycastContext(origin, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

		return raycastResultHitVoid(world, hitResult);
	}

	private boolean raycastResultHitVoid(World world, HitResult hitResult) {
		if (hitResult.getType() == HitResult.Type.MISS) {
			return true;
		}

		var hitPosition = BlockPos.ofFloored(hitResult.getPos());
		var hitBlockState = world.getBlockState(hitPosition);

		// Check if hit block is leaves or other outdoors block.
		if (hitBlockState.isIn(ModTags.OUTDOOR_BLOCKS)) {
			// Hit a block that is outdoors, return true (presume hit sky)
			return true;
		}

		return false;
	}

	private void clearBuffer() {
		spaceBuffer = new boolean[Mod.CONFIG.spaceNumberOfRaysTotal];
	}

}
