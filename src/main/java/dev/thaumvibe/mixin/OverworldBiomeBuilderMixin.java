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
 * ThaumcraftWorldGenerator.initialize: BiomeManager.addBiome(WARM/COOL, magical forest, 5) and (taint, 2).
 * Magical forest generates on the weird positive side. Tainted land is placed as a rare, localized inland blight
 * on deep negative weirdness, far from coasts, taking a small 5% slice of the wettest humidity.
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
	private static final long MID_INLAND_MIN = Climate.quantizeCoord(0.03F);
	private static final long RARE_WEIRDNESS_MAX = Climate.quantizeCoord(-0.4F);

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
			// the weird side: the driest 30% of the forest is magical forest
			long m1 = lo + span * 30 / 100;
			thaumvibe$add(biomes, temperature, new Climate.Parameter(lo, m1), continentalness, erosion, weirdness, offset, TVBiomes.MAGICAL_FOREST);
			thaumvibe$add(biomes, temperature, new Climate.Parameter(m1, hi), continentalness, erosion, weirdness, offset, Biomes.FOREST);
			ci.cancel();
		} else {
			// Tainted land must be rare and localized:
			// 1. Only deep inland (continentalness >= 0.03F), never bordering coast, beaches, or ocean/coastal water.
			// 2. Only in deep negative weirdness (<= -0.4F), avoiding the common low/mid slices near normal terrain.
			// 3. Narrow 5% humidity slice so tainted biomes are small, tight pockets rather than massive sprawling regions.
			if (continentalness.min() >= MID_INLAND_MIN && weirdness.max() <= RARE_WEIRDNESS_MAX) {
				long m2 = hi - span * 5 / 100;
				thaumvibe$add(biomes, temperature, new Climate.Parameter(lo, m2), continentalness, erosion, weirdness, offset, Biomes.FOREST);
				thaumvibe$add(biomes, temperature, new Climate.Parameter(m2, hi), continentalness, erosion, weirdness, offset, TVBiomes.TAINTED_LAND);
				ci.cancel();
			}
			// Otherwise, let vanilla Forest generate normally without interception.
		}
	}

	private static void thaumvibe$add(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, Climate.Parameter temperature,
		Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter weirdness, float offset,
		ResourceKey<Biome> biome) {
		biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(0.0F), weirdness, offset), biome));
		biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(1.0F), weirdness, offset), biome));
	}
}
