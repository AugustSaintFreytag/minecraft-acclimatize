package net.saint.acclimatize;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.particle.WindFlakeParticle;

public final class ModParticles {

	// Particles

	public static final DefaultParticleType WIND_FLAKE = registerParticle("wind_flake");

	// Init

	public static void registerParticles() {
		var registry = ParticleFactoryRegistry.getInstance();
		registry.register(ModParticles.WIND_FLAKE, WindFlakeParticle.Factory::new);
	}

	// Utility

	private static DefaultParticleType registerParticle(String name) {
		return Registry.register(Registries.PARTICLE_TYPE, new Identifier(Mod.MOD_ID, name), FabricParticleTypes.simple());
	}

}
