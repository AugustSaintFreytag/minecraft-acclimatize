package net.saint.acclimatize;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Mod.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/ice.png")
public class ModConfig implements ConfigData {

	// HUD

	@ConfigEntry.Category("hud")
	@Comment("Horizontal offset of the temperature HUD element. (Default: 0)")
	public int temperatureXOffset = 0;

	@ConfigEntry.Category("hud")
	@Comment("Vertical offset of the temperature HUD element. (Default: 0)")
	public int temperatureYOffset = 0;

	@ConfigEntry.Category("hud")
	@Comment("Horizontal offset of the thermometer HUD element. (Default: 0)")
	public int thermometerXOffset = 0;

	@ConfigEntry.Category("hud")
	@Comment("Vertical offset of the thermometer HUD element. (Default: 0)")
	public int thermometerYOffset = 0;

	@ConfigEntry.Category("hud")
	@Comment("Different styles for the temperature display. (options: gauge, glass_thermometer)")
	public String temperatureDisplayType = "glass_thermometer";

	@ConfigEntry.Category("hud")
	@Comment("Automatically hides the temperature while in a vehicle (e.g. horses, boats, aircraft). (Default: true)")
	public boolean hideTemperatureWhileInVehicle = true;

	@ConfigEntry.Category("hud")
	@Comment("Whether or not temperature damage decreases your saturation. Beware disabling this makes it really easy to bypass temperature damage just by eating. (Default: true)")
	public boolean temperatureDamageDecreasesSaturation = true;

	@ConfigEntry.Category("hud")
	@Comment("When enabled, being cold enough causes a blue outline effect. And being hot enough causes an orange one. (Default: true)")
	public boolean enableTemperatureVignette = true;

	@ConfigEntry.Category("hud")
	@Comment("The opacity value used when rendering the temperature vignette. Can be used to fade or intensify the effect. (Default: 0.7)")
	public double temperatureVignetteAlpha = 0.7;

	@ConfigEntry.Category("hud")
	@Comment("The duration of the transition to show and hide the vignette overlay (in milliseconds). (Default: 1500)")
	public int temperatureVignetteTransitionDuration = 1500;

	@ConfigEntry.Category("hud")
	@Comment("When enabled, holding a thermometer will show the exact measured ambient temperature. (Default: false)")
	public boolean enableThermometerTemperatureDisplay = false;

	// Player

	@ConfigEntry.Category("player")
	@Comment("The rate by which body temperature adapts to ambient temperatures. (Default: 0.0075)")
	public double acclimatizationRate = 0.0075;

	@ConfigEntry.Category("player")
	@Comment("The lowest possible value for player acclimatization after all factors have been applied. (Default: 0.001)")
	public double acclimatizationRateMinimum = 0.001;

	@ConfigEntry.Category("player")
	@Comment("The factor by which player acclimatization rate is boosted when wet. (Default: 2.5)")
	public double wetAcclimatizationRateBoostFactor = 2.5;

	@ConfigEntry.Category("player")
	@Comment("First threshold for hypothermia, being below this you will start to freeze (Default: 30.0)")
	public double hypothermiaThresholdMinor = 30.0;

	@ConfigEntry.Category("player")
	@Comment("Second threshold for hypothermia, being below this you will freeze faster. (Default: 15.0)")
	public double hypothermiaThresholdMajor = 15.0;

	@ConfigEntry.Category("player")
	@Comment("First threshold for hyperthermia, being above this you will start to burn (Default: 70.0)")
	public double hyperthermiaThresholdMinor = 70.0;

	@ConfigEntry.Category("player")
	@Comment("Second threshold for hyperthermia, being above this you will burn faster (Default: 85.0)")
	public double hyperthermiaThresholdMajor = 85.0;

	// World

	@ConfigEntry.Category("world")
	@Comment("The number of ticks between temperature updates. (Default: 20)")
	public int temperatureTickInterval = 20;

	@ConfigEntry.Category("world")
	@Comment("Anchor value for zeroing the temperature value calculation from a biome's native temperature attribute. Increase causes global warming, decrease causes cooling. (Default: 1.0)")
	public double biomeTemperatureZeroingAnchor = 1.0;

	@ConfigEntry.Category("world")
	@Comment("The presumed sea level for the world, used to calculate altitude temperature deltas. Should be set to the median y coordinate players spend time at. (Default: 62)")
	public int altitudeZeroingAnchor = 62;

	@ConfigEntry.Category("world")
	@Comment("The final applied coefficient for altitude-based temperature deltas. At 1.0, y64 -> y0 would be 5 units warmer, y64 -> y128 would be 5 units colder. (Default: 1.0)")
	public double altitudeTemperatureFactor = 1.0;

	@ConfigEntry.Category("world")
	@Comment("The distance in blocks from the player to sample biomes to determine climate zones. Use higher distances for larger biomes. (Default: 192)")
	public int biomeSamplingRadius = 192;

	@ConfigEntry.Category("world")
	@Comment("Enables a raycast to check if the player is exposed to the sun or in shade. (Default: true)")
	public boolean enableSunShadeCheck = true;

	@ConfigEntry.Category("world")
	@Comment("The temperature delta applied when the player is in shade from the sun. (Default: -6.0)")
	public double sunShadeTemperatureDelta = -6.0;

	@ConfigEntry.Category("world")
	@Comment("The factor of biome base temperature applied to the sun shade temperature delta. 0.0 means all biomes are equal. 0.0-1.0+ means hotter biomes provide stronger shade. (Default: 0.75)")
	public double sunShadeBiomeTemperatureFactor = 0.75;

	@ConfigEntry.Category("world")
	@Comment("The temperature delta applied to biome ambient temperature when it is raining. (Default: -4.0)")
	public double rainTemperatureDelta = -4.0;

	@ConfigEntry.Category("world")
	@Comment("The temperature delta applied to biome ambient temperature when it is snowing. (Default: -6.0)")
	public double snowTemperatureDelta = -6.0;

	@ConfigEntry.Category("world")
	@Comment("The temperature delta gradually applied between sunset and sunrise. (Default: -10.0)")
	public double nightTemperatureDelta = -10.0;

	@ConfigEntry.Category("world")
	@Comment("The default temperature value of all nether biomes. (Between 0 and 100) (Default: 75)")
	public double netherBiomeTemperature = 75;

	@ConfigEntry.Category("world")
	@Comment("The default temperature value of all end biomes. (Between 0 and 100) (Default: 10)")
	public double endBiomeTemperature = 10;

	@ConfigEntry.Category("world")
	@Comment("The number of rays cast in a cone shape around the player to check for an interior space. Higher values are more accurate but take longer to detect an interior. (Default: 10)")
	public int spaceNumberOfRaysTotal = 10;

	@ConfigEntry.Category("world")
	@Comment("The number of rays cast per tick to check for an interior space. Higher values mean faster results but higher cost. Scales per player, done server-side. (Default: 2)")
	public int spaceNumberOfRaysCastPerTick = 2;

	@ConfigEntry.Category("world")
	@Comment("The length of the rays cast around the player to check for an interior space. Higher means more coverage but higher cost. (Default: 32)")
	public int spaceRayLength = 32;

	// Blocks

	@ConfigEntry.Category("blocks")
	@Comment("The radius around the player in which blocks are checked for temperature effects. (Default: 8)")
	public int blockTemperatureRadius = 8;

	@ConfigEntry.Category("blocks")
	@Comment("The lowest possible value to be added to ambient temperatures based on blocks in the environment. (Default: -20.0")
	public double blockTemperatureAbsoluteMinimum = -20.0;

	@ConfigEntry.Category("blocks")
	@Comment("The highest possible value to be added to ambient temperatures based on blocks in the environment. (Default: 40.0)")
	public double blockTemperatureAbsoluteMaximum = 40.0;

	@ConfigEntry.Category("blocks")
	@Comment("The decay factor used to compute applied temperature by distance between player and heat source. Higher values mean quicker falloff. (Default: 0.1)")
	public double blockTemperatureDistanceFalloffFactor = 0.1;

	@ConfigEntry.Category("blocks")
	@Comment("The factor applied to base acclimatization rate when close to a block heat source. (Default: 4.0)")
	public double blockAcclimatizationBoostFactor = 4.0;

	@ConfigEntry.Category("blocks")
	@Comment("The minimum temperature at which the acclimatization boost is applied. (Default: 10.0)")
	public double blockTemperatureAcclimatizationBoostThreshold = 10.0;

	@ConfigEntry.Category("blocks")
	@Comment("Temperature value applied when the player is submerged in water. (Default: -15.0)")
	public double waterBlockTemperature = -15.0;

	@ConfigEntry.Category("blocks")
	@Comment("Blocks that will heat you up when near.")
	public String heatingBlocks = String.join(", ", "minecraft:fire = 5.0", "minecraft:lava = 2.0", "minecraft:magma = 1.0",
			"minecraft:campfire = 18.0", "minecraft:torch = 4.0", "minecraft:wall_torch = 4.0", "minecraft:redstone_torch = 1.0",
			"minecraft:redstone_wall_torch = 1.0", "minecraft:soul_torch = 4.0", "minecraft:soul_wall_torch = 4.0",
			"minecraft:soul_campfire = 18.0", "minecraft:lava_cauldron = 5.0", "minecraft:furnace = 10.0", "minecraft:blast_furnace = 10.0",
			"minecraft:smoker = 10.0", "minecraft:redstone_lamp = 2.0", "minecraft:ochre_froglight = 3.0",
			"minecraft:pearlescent_froglight = 3.0", "minecraft:verdant_froglight = 3.0", "minecraft:end_rod = 2.0",
			"hardcore_torches:lit_torch = 4.0", "hardcore_torches:lit_wall_torch = 4.0", "hardcore_torches:lit_lantern = 2.0",
			"farmersdelight:stove = 18.0", "refurbished_furniture:light_ceiling_light = 10.0",
			"refurbished_furniture:dark_ceiling_light = 10.0", "refurbished_furniture:black_lamp = 10.0",
			"refurbished_furniture:blue_lamp = 10.0", "refurbished_furniture:brown_lamp = 10.0", "refurbished_furniture:cyan_lamp = 10.0",
			"refurbished_furniture:gray_lamp = 10.0", "refurbished_furniture:green_lamp = 10.0",
			"refurbished_furniture:light_blue_lamp = 10.0", "refurbished_furniture:light_gray_lamp = 10.0",
			"refurbished_furniture:lime_lamp = 10.0", "refurbished_furniture:magenta_lamp = 10.0",
			"refurbished_furniture:orange_lamp = 10.0", "refurbished_furniture:pink_lamp = 10.0", "refurbished_furniture:red_lamp = 10.0",
			"refurbished_furniture:white_lamp = 10.0", "refurbished_furniture:yellow_lamp = 10.0");

	@ConfigEntry.Category("blocks")
	@Comment("Blocks that will cool you down when near.")
	public String coolingBlocks = String.join(", ", "minecraft:ice = -0.1", "minecraft:packed_ice = -0.5", "minecraft:blue_ice = -0.75",
			"minecraft:snow_block = -0.01");

	@ConfigEntry.Category("blocks")
	@Comment("Blocks that wind can pass through.")
	public String windPermeableBlocks = String.join(", ", "minecraft:oak_leaves", "minecraft:spruce_leaves", "minecraft:birch_leaves",
			"minecraft:jungle_leaves", "minecraft:acacia_leaves", "minecraft:dark_oak_leaves", "minecraft:mangrove_leaves",
			"minecraft:cherry_leaves", "minecraft:azalea_leaves", "minecraft:flowering_azalea_leaves", "minecraft:vine",
			"minecraft:waterlily", "minecraft:tall_grass", "minecraft:dead_bush", "minecraft:fern", "minecraft:dandelion",
			"minecraft:poppy", "minecraft:blue_orchid", "minecraft:allium", "minecraft:azure_bluet", "minecraft:red_tulip",
			"minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip", "minecraft:oxeye_daisy", "minecraft:cornflower",
			"minecraft:lily_of_the_valley", "minecraft:wither_rose", "minecraft:sunflower", "minecraft:lilac", "minecraft:rose_bush",
			"minecraft:peony");

	// Items

	@ConfigEntry.Category("items")
	@Comment("Item temperature values that add to body temperature calculation when actively held.")
	public String heldTemperatureItems = String.join(", ", "minecraft:torch = 2.0", "minecraft:lava_bucket = 1.0",
			"hardcore_torches:lit_torch = 2.0", "hardcore_torches:lit_lantern = 1.0");

	@ConfigEntry.Category("items")
	@Comment("Item temperature values that add to body temperature calculation when worn.")
	public String wornTemperatureItems = String.join(", ", "");

	@ConfigEntry.Category("items")
	@Comment("Material temperature values used to auto-assign values to wearable items.")
	public String materialAutoTemperature = String.join(", ", "leather = 1.25", "iron = 1.75", "gold = 0.5", "diamond = 2.0",
			"netherite = -1.0", "chainmail = 0.5", "aeternium = 1.85", "brass = 1.0", "bronze = 1.0", "cincinnasite = -1.0", "copper = 1.0",
			"crystalite = 3.5", "flaming_ruby = 2.5", "nether_ruby = -1.25", "resonarium = -1.75", "rose_quartz = 2.0", "steel = 2.0",
			"sterling = 1.5", "sturdy = 2.25", "terminite = 3.0", "thallasium = 2.1", "warden = 2.5", "turtle = 0.85", "death_knight = 1.5",
			"netherite_fire = -3.0", "netherite_ranger = -1.65", "ranger = 1.65", "netherite_rogue = -1.65", "rogue = 1.65",
			"netherite_assassin = -1.65", "assassin = 1.65", "archer = 1.65", "netherite_archer = -1.65", "netherite_frost = -2.75",
			"frost = 2.75", "fire = -2.75", "wizard = 0.85", "arcane = 0.85");

	@ConfigEntry.Category("items")
	@Comment("Temperature factor for helmets with auto-assigned material-based values. (Default: 1.0)")
	public double helmetAutoTemperatureFactor = 1.0;

	@ConfigEntry.Category("items")
	@Comment("Temperature factor for chestplates with auto-assigned material-based values. (Default: 2.0)")
	public double leggingsAutoTemperatureFactor = 2.0;

	@ConfigEntry.Category("items")
	@Comment("Temperature factor for leggings with auto-assigned material-based values. (Default: 3.0)")
	public double chestplateAutoTemperatureFactor = 3.0;

	@ConfigEntry.Category("items")
	@Comment("Temperature factor for boots with auto-assigned material-based values. (Default: 1.0)")
	public double bootsAutoTemperatureFactor = 1.0;

	@ConfigEntry.Category("items")
	@Comment("The factor for how much the raw temperature value of worn items adds to player temperature. (Default: 0.35)")
	public double itemTemperatureFactor = 0.35;

	@ConfigEntry.Category("items")
	@Comment("Factor for how much player acclimatization is affected by the temperature value of worn items. (Default: -0.0125)")
	public double itemAcclimatizationRateFactor = -0.0125;

	@ConfigEntry.Category("items")
	@Comment("Duration of the cooling effect of ice water drinks in ticks. (Default: 4800)")
	public int iceWaterEffectDuration = 4800;

	@ConfigEntry.Category("items")
	@Comment("The ambient temperature offset while the ice water heat dissipation effect is active. (Default: -10.0)")
	public double iceWaterCoolingFactor = -10.0;

	@ConfigEntry.Category("items")
	@Comment("Multiplier for how much each level of fire protection cools you (Default: -2.5)")
	public double fireProtectionCoolingFactor = -2.5;

	// Wind

	@ConfigEntry.Category("wind")
	@Comment("Toggles wind simulation, including effects on effective temperatures and particles. (Default: true)")
	public boolean enableWind = true;

	@ConfigEntry.Category("wind")
	@Comment("The base number of ticks until wind direction is updated, plus randomization. (Default: 6000)")
	public int windDirectionUpdateInterval = 6000;

	@ConfigEntry.Category("wind")
	@Comment("The base number of ticks until wind intensity is updated, plus randomization. (Default: 1200)")
	public int windIntensityUpdateInterval = 1200;

	@ConfigEntry.Category("wind")
	@Comment("The number of ticks to transition from one wind intensity or direction to a new value. (Default: 200)")
	public int windTransitionInterval = 200;

	@ConfigEntry.Category("wind")
	@Comment("The minimum intensity wind can be transitioned to in random tick assignment. (Default: 0.15)")
	public double windIntensityMin = 0.15;

	@ConfigEntry.Category("wind")
	@Comment("The maximum intensity wind can be transitioned to in random tick assignment. (Default: 5.0)")
	public double windIntensityMax = 5.0;

	@ConfigEntry.Category("wind")
	@Comment("The factor for how much wind affects ambient temperature when fully exposed. (Default: -1.25)")
	public double windChillFactor = -1.25;

	@ConfigEntry.Category("wind")
	@Comment("Number of rays used in wind calculation. Increase for more precise wind simulation. (Default: 8)")
	public int windRayCount = 8;

	@ConfigEntry.Category("wind")
	@Comment("How many blocks long wind rays are. Increase for larger spaces. (Default: 8)")
	public int windRayLength = 8;

	// Sounds

	@ConfigEntry.Category("sounds")
	@Comment("Toggles wind ambient sounds based on biome, wind intensity, and exterior/interior state. (Default: true)")
	public boolean enableAmbientSounds = true;

	@ConfigEntry.Category("sounds")
	@Comment("Replaces vanilla rain sound playback with custom sounds. Required to fade rain sound playback in interiors. (Default: true)")
	public boolean enableRainSounds = true;

	@ConfigEntry.Category("sounds")
	@Comment("The base volume for all added ambient sounds. (Default: 1.0)")
	public float ambientSoundVolume = 1.0f;

	@ConfigEntry.Category("sounds")
	@Comment("The base volume for rain ambient sounds. Only used when enabled. (Default: 1.0)")
	public float rainSoundVolume = 1.0f;

	@ConfigEntry.Category("sounds")
	@Comment("Scales how strongly wind intensity affects ambient sound volume. 1.0 produces a broad range, lower values soften the impact. (Default: 1.0)")
	public double ambientSoundWindIntensityFactor = 1.0;

	// Particles

	@ConfigEntry.Category("particles")
	@Comment("Toggles particles being affected by wind intensity and direction. (Default: true)")
	public boolean enableParticleWindEffects = true;

	@ConfigEntry.Category("particles")
	@Comment("When enabled, random particles will spawn based on wind intensity. Helps with naturally determining wind direction. (Default: true)")
	public boolean enableWindParticles = true;

	@ConfigEntry.Category("particles")
	@Comment("Spawn rate for random wind particles. Spawn rate is first determined by wind intensity, then scaled by this value. (Default: 0.05)")
	public double windParticleSpawnRate = 0.05;

	@ConfigEntry.Category("particles")
	@Comment("Radius around the player to spawn wind particles. (Default: 16)")
	public int windParticleSpawnRadius = 16;

	@ConfigEntry.Category("particles")
	@Comment("The intensity by which all particles are affected by wind if enabled. (Default: 1.0)")
	public double particleWindEffectFactor = 1.0;

	@ConfigEntry.Category("particles")
	@Comment("The intensity by which weather particles are titled towards wind direction. (Default: 0.1)")
	public double particleWeatherEffectFactor = 0.1;

	@ConfigEntry.Category("particles")
	@Comment("The maximum angle a rain particle can get skewed by wind intensity and angle, in degrees. (Default: 30.0)")
	public double particleRainMaxAngle = 30.0;

	@ConfigEntry.Category("particles")
	@Comment("Toggles particles being affected by heat source blocks (only applies to smoke). (Performance impact: low) (Default: true)")
	public boolean enableParticleHeatEffects = true;

	@ConfigEntry.Category("particles")
	@Comment("Toggles blocks being able to affect particle velocity if wind path is obstructed. (Performance impact: medium) (Default: true)")
	public boolean enableParticleBlockChecks = true;

	@ConfigEntry.Category("particles")
	@Comment("Toggles particles being affected by near-by fluid blocks. (Performance impact: medium) (Default: true)")
	public boolean enableParticleFluidBlockChecks = true;

	@ConfigEntry.Category("particles")
	@Comment("Toggles complex wind simulation, including being pushed alongside and around obstacles. (Performance impact: high) (Default: false)")
	public boolean enableParticleComplexInteractions = false;

	@ConfigEntry.Category("particles")
	@Comment("Toggles complex wind simulation to allow particles to be funelled through gaps in obstacles. (Performance impact: high) (Default: false)")
	public boolean enableParticleFunneling = false;

	// Compatibility

	@ConfigEntry.Category("compatibility")
	@Comment("Forces Dynamic Surroundings to use Acclimatize interior detection. Only used if mod is installed. (Default: true)")
	public boolean enableDynamicSurroundingsInterop = true;

	@ConfigEntry.Category("compatibility")
	@Comment("Fades Ambient Sounds sound volume when in interior spaces. Only used if mod is installed. (Default: true)")
	public boolean enableAmbientSoundsInterop = true;

	@ConfigEntry.Category("compatibility")
	@Comment("The volume factor applied to mod ambient sounds when in an interior space. (Default: 0.2)")
	public float interiorSoundSuppressionFactor = 0.2f;

	@ConfigEntry.Category("compatibility")
	@Comment("The duration of smoothly transitioning from one volume level to another, in ticks. (Default: 40)")
	public int soundSuppressionTransitionTicks = 40;

	// Developer

	@ConfigEntry.Category("developer")
	@Comment("Enables debug logging and performance profiling to the console. (Default: false)")
	public boolean enableLogging = false;

	@ConfigEntry.Category("developer")
	@Comment("Enables temperature ticking for players in creative or spectator mode. Sound systems may also rely on environmental data. (Default: true)")
	public boolean enableCreativeModeTemperature = true;

	@ConfigEntry.Category("developer")
	@Comment("Enables debug visualization of sun vectors showing the raycast direction from player to sun. (Default: false)")
	public boolean enableSunVectorDebug = false;

	@ConfigEntry.Category("developer")
	@Comment("Enables continuous logging of the current world sky angle. (Default: false)")
	public boolean enableSkyAngleLogging = false;

}
