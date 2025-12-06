package net.saint.acclimatize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.saint.acclimatize.mixinlogic.ParticleAccessor;
import net.saint.acclimatize.mixinlogic.ParticleMixinLogic;

// Code originally ported from *Immersive Winds* by Vibzz.

@Mixin(Particle.class)
public abstract class ParticleMixin implements ParticleMixinLogic, ParticleAccessor {

	// Shadowed Properties

	@Shadow
	protected double x;

	@Shadow
	protected double y;

	@Shadow
	protected double z;

	@Shadow
	protected double velocityX;

	@Shadow
	protected double velocityY;

	@Shadow
	protected double velocityZ;

	@Shadow
	protected ClientWorld world;

	// Injected Properties

	@Unique
	private double heatValue = 0.0;

	// Accessors

	@Unique
	public double getVelocityX() {
		return this.velocityX;
	}

	@Unique
	public double getVelocityY() {
		return this.velocityY;
	}

	@Unique
	public double getVelocityZ() {
		return this.velocityZ;
	}

	// Logic

	@Unique
	public Vec3d getPosition() {
		return new Vec3d(x, y, z);
	}

	@Unique
	public void setPosition(Vec3d position) {
		this.x = position.getX();
		this.y = position.getY();
		this.z = position.getZ();
	}

	@Override
	public MinecraftClient getClient() {
		return MinecraftClient.getInstance();
	}

	@Override
	public ClientWorld getWorld() {
		return this.world;
	}

	@Override
	public double getHeatValue() {
		return this.heatValue;
	}

	@Override
	public void setHeatValue(double value) {
		this.heatValue = value;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private double mixinMoveDx(double dx) {
		return this.calculateDeltaX(dx);
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private double mixinMoveDy(double dy) {
		return dy;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private double mixinMoveDz(double dz) {
		return this.calculateDeltaZ(dz);
	}

}