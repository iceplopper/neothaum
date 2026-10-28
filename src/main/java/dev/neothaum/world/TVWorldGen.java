package dev.neothaum.world;

import dev.neothaum.NeoThaum;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class TVWorldGen {
	public static final ResourceKey<PlacedFeature> TC_WORLD = ResourceKey.create(Registries.PLACED_FEATURE, NeoThaum.id("tc_world"));
	public static final ResourceKey<PlacedFeature> TC_NETHER = ResourceKey.create(Registries.PLACED_FEATURE, NeoThaum.id("tc_nether"));

	public static void init() {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, NeoThaum.id("tc_world"), TCWorldFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, NeoThaum.id("tc_biome_decor"), TCBiomeDecorFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, NeoThaum.id("outer_maze"), dev.neothaum.world.outer.OuterMazeFeature.CODEC);
		// MazeHandler.loadMaze / the labyrinth lives with the server
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(dev.neothaum.world.outer.MazeHandler::load);
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(s -> dev.neothaum.world.outer.MazeHandler.unload());
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.VEGETAL_DECORATION, TC_WORLD);
		BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Decoration.VEGETAL_DECORATION, TC_NETHER);
		// ConfigEntities.initEntitySpawns: angry zombies wherever monsters spawn, wisps in the nether
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld().and(BiomeSelectors.excludeByKey(TVBiomes.TAINTED_LAND)),
			net.minecraft.world.entity.MobCategory.MONSTER, dev.neothaum.entity.TVEntities.BRAINY_ZOMBIE, 10, 1, 1);
		int wisps = dev.neothaum.config.NeoThaumConfig.scale(dev.neothaum.config.NeoThaumConfig.WISP_SPAWN_WEIGHT, dev.neothaum.config.NeoThaumConfig.current.wispSpawnMultiplier);
		if (wisps > 0) {
			BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), net.minecraft.world.entity.MobCategory.MONSTER,
				dev.neothaum.entity.TVEntities.WISP, wisps, 1, 1);
		}
		BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), net.minecraft.world.entity.MobCategory.MONSTER,
			dev.neothaum.entity.TVEntities.FIREBAT, 10, 1, 2);
		// and on Halloween, firebats everywhere
		java.time.LocalDate today = java.time.LocalDate.now();
		if (today.getMonthValue() == 10 && today.getDayOfMonth() == 31) {
			BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), net.minecraft.world.entity.MobCategory.MONSTER,
				dev.neothaum.entity.TVEntities.FIREBAT, 5, 1, 2);
		}
	}

	private TVWorldGen() {
	}
}
