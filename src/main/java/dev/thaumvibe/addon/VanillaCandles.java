package dev.thaumvibe.addon;

import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.tiles.InfusionMatrixBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Addon (not in TC4): vanilla candles stand in for tallow candles around an infusion altar while lit. A block of two to four candles counts
 * as one candle, as a tallow candle does. Each tallow candle and the vanilla candle of its colour also trade for each other one to one in a
 * crafting grid (data/thaumvibe/recipe/addon/), and the Thaumonomicon's tallow entry explains it.
 */
public final class VanillaCandles {
	public static void init() {
		InfusionMatrixBlockEntity.EXTRA_STABILISERS.add((level, pos) -> isLitCandle(level.getBlockState(pos)));
		// the tallow entry shows the vanilla candle's own recipe
		TVNetwork.EXTRA_SYNCED_RECIPES.add(Identifier.withDefaultNamespace("candle"));
	}

	public static boolean isLitCandle(BlockState state) {
		return state.is(BlockTags.CANDLES) && state.hasProperty(CandleBlock.LIT) && state.getValue(CandleBlock.LIT);
	}

	private VanillaCandles() {
	}
}
