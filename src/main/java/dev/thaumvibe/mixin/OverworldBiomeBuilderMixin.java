package dev.thaumvibe.mixin;

import com.mojang.datafixers.util.Pair;
import dev.thaumvibe.world.TVBiomes;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ThaumcraftWorldGenerator.initialize: BiomeManager.addBiome(WARM/COOL, magical forest, 5) and (taint, 2). TC4 placed each at random; here
 * they take slices of the forest's climate range, magical forest on the weird side and tainted land on the ordinary side, at opposite ends of
 * the humidity range so that they generate apart.
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
	@Inject(method = "addSurfaceBiome", at = @At("HEAD"), cancellable = true)
	private void thaumvibe$shareForest(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, Climate.Parameter temperature,
		Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter weirdness, float offset,
		ResourceKey<Biome> biome, CallbackInfo ci) {
		if (biome != Biomes.FOREST || !TVBiomes.GENERATE) {
			return;
		}
		long lo = humidity.min();
		long hi = humidity.max();
		long span = hi - lo;
		if (weirdness.max() >= 0L) {
			// the weird side: the driest 35% of the forest is magical forest
			long m1 = lo + span * 35 / 100;
			thaumvibe$add(biomes, temperature, new Climate.Parameter(lo, m1), continentalness, erosion, weirdness, offset, TVBiomes.MAGICAL_FOREST);
			thaumvibe$add(biomes, temperature, new Climate.Parameter(m1, hi), continentalness, erosion, weirdness, offset, Biomes.FOREST);
		} else {
			// the ordinary side: the wettest 14% is tainted land (TC4's 2 to the magical forest's 5). Opposite weirdness and opposite ends of
			// the humidity range, so the two never border each other
			long m2 = hi - span * 14 / 100;
			thaumvibe$add(biomes, temperature, new Climate.Parameter(lo, m2), continentalness, erosion, weirdness, offset, Biomes.FOREST);
			thaumvibe$add(biomes, temperature, new Climate.Parameter(m2, hi), continentalness, erosion, weirdness, offset, TVBiomes.TAINTED_LAND);
		}
		ci.cancel();
	}

	private static void thaumvibe$add(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, Climate.Parameter temperature,
		Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter weirdness, float offset,
		ResourceKey<Biome> biome) {
		biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(0.0F), weirdness, offset), biome));
		biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(1.0F), weirdness, offset), biome));
	}
}
