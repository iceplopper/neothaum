package dev.neothaum.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * LocationIsValidSpawn, as each of TC4's structures has it: open air no more than two blocks above (i, j, k), and the ground there one of
 * the given blocks, or snow or grass growing on one of them. Water never passes, so nothing is built on lakes or the sea.
 */
final class TCSpawnCheck {
	private TCSpawnCheck() {
	}

	static boolean valid(WorldGenLevel world, int i, int j, int k, Block... ground) {
		int distanceToAir = 0;
		while (!world.getBlockState(new BlockPos(i, j + distanceToAir, k)).isAir()) {
			if (++distanceToAir > 2) {
				return false;
			}
		}
		j += distanceToAir - 1;
		BlockState here = world.getBlockState(new BlockPos(i, j, k));
		BlockState below = world.getBlockState(new BlockPos(i, j - 1, k));
		if (!world.getBlockState(new BlockPos(i, j + 1, k)).isAir()) {
			return false;
		}
		for (Block b : ground) {
			if (here.is(b) || (here.is(Blocks.SNOW) || here.is(Blocks.SHORT_GRASS)) && below.is(b)) {
				return true;
			}
		}
		return false;
	}
}
