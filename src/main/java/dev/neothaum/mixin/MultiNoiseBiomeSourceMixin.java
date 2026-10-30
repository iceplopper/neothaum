package dev.neothaum.mixin;

import com.mojang.datafixers.util.Pair;
import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.world.TVBiomes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Magical Forest and Tainted Land in biome layouts that don't come from vanilla's builder (OverworldBiomeBuilderMixin covers that one):
 * Terralith and other mods give the overworld their own list of biomes. In such a list, each plain forest entry is split the same way as in
 * vanilla's layout: its driest 30% (scaled by the size setting) on the weird side becomes Magical Forest, its wettest 5% deep on the
 * negative weird side, inland, Tainted Land. A list with no forest that rare (Terralith's) uses its inland forest on the negative weird side
 * for Tainted Land instead. A list that already has NeoThaum's biomes is left alone.
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
	@Unique
	private static final long MID_INLAND_MIN = Climate.quantizeCoord(0.03F);
	@Unique
	private static final long RARE_WEIRDNESS_MAX = Climate.quantizeCoord(-0.4F);

	@Unique
	private Climate.ParameterList<Holder<Biome>> neothaum$source;
	@Unique
	private Climate.ParameterList<Holder<Biome>> neothaum$shared;

	@Inject(method = "parameters", at = @At("RETURN"), cancellable = true)
	private void neothaum$shareForests(CallbackInfoReturnable<Climate.ParameterList<Holder<Biome>>> cir) {
		Climate.ParameterList<Holder<Biome>> list = cir.getReturnValue();
		if (list == this.neothaum$source && this.neothaum$shared != null) {
			cir.setReturnValue(this.neothaum$shared);
			return;
		}
		Holder<Biome> magical = TVBiomes.holder(TVBiomes.MAGICAL_FOREST);
		Holder<Biome> tainted = TVBiomes.holder(TVBiomes.TAINTED_LAND);
		if (!TVBiomes.GENERATE || magical == null || tainted == null) {
			return;
		}
		boolean forest = false;
		boolean rareForest = false;
		for (Pair<Climate.ParameterPoint, Holder<Biome>> p : list.values()) {
			if (p.getSecond().is(TVBiomes.MAGICAL_FOREST) || p.getSecond().is(TVBiomes.TAINTED_LAND)) {
				return;
			}
			if (p.getSecond().is(Biomes.FOREST)) {
				forest = true;
				rareForest |= neothaum$taintable(p.getFirst(), true);
			}
		}
		if (!forest) {
			return;
		}
		var cfg = NeoThaumConfig.current;
		int magicPct = cfg.magicalForestGenerate ? Math.min(100, NeoThaumConfig.scale(NeoThaumConfig.MAGICAL_FOREST_PERCENT, cfg.magicalForestSizeMultiplier)) : 0;
		int taintPct = cfg.taintedLandGenerate ? Math.min(100, NeoThaumConfig.scale(NeoThaumConfig.TAINTED_LAND_PERCENT, cfg.taintedLandSizeMultiplier)) : 0;
		List<Pair<Climate.ParameterPoint, Holder<Biome>>> out = new ArrayList<>();
		for (Pair<Climate.ParameterPoint, Holder<Biome>> p : list.values()) {
			Climate.ParameterPoint pt = p.getFirst();
			if (!p.getSecond().is(Biomes.FOREST)) {
				out.add(p);
				continue;
			}
			long lo = pt.humidity().min();
			long hi = pt.humidity().max();
			long span = hi - lo;
			if (pt.weirdness().max() >= 0L && magicPct > 0) {
				long m1 = lo + span * magicPct / 100;
				out.add(Pair.of(neothaum$withHumidity(pt, lo, m1), magical));
				out.add(Pair.of(neothaum$withHumidity(pt, m1, hi), p.getSecond()));
			} else if (pt.weirdness().max() < 0L && taintPct > 0 && neothaum$taintable(pt, rareForest)) {
				long m2 = hi - span * taintPct / 100;
				out.add(Pair.of(neothaum$withHumidity(pt, lo, m2), p.getSecond()));
				out.add(Pair.of(neothaum$withHumidity(pt, m2, hi), tainted));
			} else {
				out.add(p);
			}
		}
		this.neothaum$source = list;
		this.neothaum$shared = new Climate.ParameterList<>(out);
		cir.setReturnValue(this.neothaum$shared);
	}

	/** Inland forest on the negative weird side; when the list has some, only the rare deep-weird part, as in vanilla's layout. */
	@Unique
	private static boolean neothaum$taintable(Climate.ParameterPoint pt, boolean rareOnly) {
		return pt.weirdness().max() < 0L && pt.continentalness().min() >= MID_INLAND_MIN && (!rareOnly || pt.weirdness().max() <= RARE_WEIRDNESS_MAX);
	}

	@Unique
	private static Climate.ParameterPoint neothaum$withHumidity(Climate.ParameterPoint pt, long lo, long hi) {
		return new Climate.ParameterPoint(pt.temperature(), new Climate.Parameter(lo, hi), pt.continentalness(), pt.erosion(), pt.depth(),
			pt.weirdness(), pt.offset());
	}
}
