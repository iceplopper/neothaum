package dev.neothaum.golem.ai;

import dev.neothaum.blocks.ManaPodBlock;
import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CropUtils.isGrownCrop: a crop that has finished growing (no more bonemeal to give it), ripe nether wart and cocoa, melons, pumpkins, a ripe
 * mana pod, and the upper part of sugar cane and cacti.
 */
public final class CropUtils {
	private CropUtils() {
	}

	public static boolean isGrownCrop(Level level, BlockPos pos) {
		BlockState s = level.getBlockState(pos);
		if (s.isAir()) {
			return false;
		}
		Block b = s.getBlock();
		if (b instanceof BonemealableBlock bm && !(b instanceof StemBlock) && (s.is(BlockTags.CROPS) || b instanceof SweetBerryBushBlock)
			&& !bm.isValidBonemealTarget(level, pos, s, net.minecraft.world.level.block.BonemealSource.INTERACTION)) {
			return true;
		}
		if (b instanceof CropBlock crop && crop.isMaxAge(s)) {
			return true;
		}
		if (b == Blocks.NETHER_WART && s.getValue(NetherWartBlock.AGE) >= 3 || b == Blocks.COCOA && s.getValue(CocoaBlock.AGE) >= 2) {
			return true;
		}
		if (b == Blocks.MELON || b == Blocks.PUMPKIN || b == TVBlocks.MANA_POD && s.getValue(ManaPodBlock.AGE) >= 7) {
			return true;
		}
		return (b == Blocks.SUGAR_CANE || b == Blocks.CACTUS) && level.getBlockState(pos.below()).is(b);
	}

	/** Crops picked by right-clicking rather than breaking (CropUtils.clickableCrops): sweet berries. */
	public static boolean isClickable(BlockState s) {
		return s.getBlock() instanceof SweetBerryBushBlock;
	}
}
