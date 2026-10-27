package dev.neothaum.world;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Port of WorldGenSilverwoodTrees. Pure nodes sit in gaps of the trunk like Thaumcraft's node-bearing logs. */
public final class SilverwoodTreeFeature {
	private SilverwoodTreeFeature() {
	}

	private static BlockState log(int meta) {
		Direction.Axis axis = (meta & 12) == 4 ? Direction.Axis.X : (meta & 12) == 8 ? Direction.Axis.Z : Direction.Axis.Y;
		return TVBlocks.SILVERWOOD_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
	}

	private static boolean replaceable(BlockState s) {
		return s.isAir() || s.is(BlockTags.LEAVES) || s.canBeReplaced();
	}

	public static boolean generate(LevelAccessor world, RandomSource random, BlockPos pos, boolean notify, int minTreeHeight, int randomTreeHeight) {
		int flags = notify ? 3 : 2;
		boolean worldgen = !notify;
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		int height = random.nextInt(randomTreeHeight) + minTreeHeight;
		boolean flag = true;
		if (y < world.getMinY() + 1 || y + height + 1 > world.getMaxY()) {
			return false;
		}
		for (int i1 = y; i1 <= y + 1 + height; i1++) {
			int spread = 1;
			if (i1 == y) {
				spread = 0;
			}
			if (i1 >= y + 1 + height - 2) {
				spread = 3;
			}
			for (int j1 = x - spread; j1 <= x + spread && flag; j1++) {
				for (int k1 = z - spread; k1 <= z + spread && flag; k1++) {
					if (!replaceable(world.getBlockState(new BlockPos(j1, i1, k1))) && i1 > y) {
						flag = false;
					}
				}
			}
		}
		if (!flag) {
			return false;
		}
		BlockState soil = world.getBlockState(new BlockPos(x, y - 1, z));
		if (!soil.is(BlockTags.SUPPORTS_VEGETATION)) { // canSustainPlant for a sapling: grass, dirt, podzol, farmland...
			return false;
		}
		BlockState leaves = TVBlocks.SILVERWOOD_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
		int start = y + height - 5;
		int end = y + height + 3 + random.nextInt(3);
		for (int k2 = start; k2 <= end; k2++) {
			int cty = Mth.clamp(k2, y + height - 3, y + height);
			for (int xx = x - 5; xx <= x + 5; xx++) {
				for (int zz = z - 5; zz <= z + 5; zz++) {
					double d3 = xx - x;
					double d4 = k2 - cty;
					double d5 = zz - z;
					double dist = d3 * d3 + d4 * d4 + d5 * d5;
					BlockPos lp = new BlockPos(xx, k2, zz);
					if (dist < 10 + random.nextInt(8) && replaceable(world.getBlockState(lp))) {
						world.setBlock(lp, leaves, flags);
					}
				}
			}
		}
		int chance = (int) (height * 1.5);
		boolean lastblock = false;
		int t;
		for (t = 0; t < height; t++) {
			BlockPos tp = new BlockPos(x, y + t, z);
			if (replaceable(world.getBlockState(tp))) {
				if (t > 0 && !lastblock && random.nextInt(chance) == 0) {
					NodeGenerator.createRandomNodeAt(world, tp, random, true, false, false, true);
					chance += height;
					lastblock = true;
				} else {
					world.setBlock(tp, log(1), flags);
					lastblock = false;
				}
				world.setBlock(tp.west(), log(1), flags);
				world.setBlock(tp.east(), log(1), flags);
				world.setBlock(tp.north(), log(1), flags);
				world.setBlock(tp.south(), log(1), flags);
			}
		}
		world.setBlock(new BlockPos(x, y + t, z), log(1), flags);
		world.setBlock(new BlockPos(x - 1, y, z - 1), log(1), flags);
		world.setBlock(new BlockPos(x + 1, y, z + 1), log(1), flags);
		world.setBlock(new BlockPos(x - 1, y, z + 1), log(1), flags);
		world.setBlock(new BlockPos(x + 1, y, z - 1), log(1), flags);
		if (random.nextInt(3) != 0) {
			world.setBlock(new BlockPos(x - 1, y + 1, z - 1), log(1), flags);
		}
		if (random.nextInt(3) != 0) {
			world.setBlock(new BlockPos(x + 1, y + 1, z + 1), log(1), flags);
		}
		if (random.nextInt(3) != 0) {
			world.setBlock(new BlockPos(x - 1, y + 1, z + 1), log(1), flags);
		}
		if (random.nextInt(3) != 0) {
			world.setBlock(new BlockPos(x + 1, y + 1, z - 1), log(1), flags);
		}
		world.setBlock(new BlockPos(x - 2, y, z), log(5), flags);
		world.setBlock(new BlockPos(x + 2, y, z), log(5), flags);
		world.setBlock(new BlockPos(x, y, z - 2), log(9), flags);
		world.setBlock(new BlockPos(x, y, z + 2), log(9), flags);
		world.setBlock(new BlockPos(x - 2, y - 1, z), log(1), flags);
		world.setBlock(new BlockPos(x + 2, y - 1, z), log(1), flags);
		world.setBlock(new BlockPos(x, y - 1, z - 2), log(1), flags);
		world.setBlock(new BlockPos(x, y - 1, z + 2), log(1), flags);
		world.setBlock(new BlockPos(x - 1, y + (height - 4), z - 1), log(1), flags);
		world.setBlock(new BlockPos(x + 1, y + (height - 4), z + 1), log(1), flags);
		world.setBlock(new BlockPos(x - 1, y + (height - 4), z + 1), log(1), flags);
		world.setBlock(new BlockPos(x + 1, y + (height - 4), z - 1), log(1), flags);
		if (random.nextInt(3) == 0) {
			world.setBlock(new BlockPos(x - 1, y + (height - 5), z - 1), log(1), flags);
		}
		if (random.nextInt(3) == 0) {
			world.setBlock(new BlockPos(x + 1, y + (height - 5), z + 1), log(1), flags);
		}
		if (random.nextInt(3) == 0) {
			world.setBlock(new BlockPos(x - 1, y + (height - 5), z + 1), log(1), flags);
		}
		if (random.nextInt(3) == 0) {
			world.setBlock(new BlockPos(x + 1, y + (height - 5), z - 1), log(1), flags);
		}
		world.setBlock(new BlockPos(x - 2, y + (height - 4), z), log(5), flags);
		world.setBlock(new BlockPos(x + 2, y + (height - 4), z), log(5), flags);
		world.setBlock(new BlockPos(x, y + (height - 4), z - 2), log(9), flags);
		world.setBlock(new BlockPos(x, y + (height - 4), z + 2), log(9), flags);
		if (worldgen) {
			NodeGenerator.generateFlowers(world, random, pos, TVBlocks.SHIMMERLEAF.defaultBlockState());
		}
		return true;
	}
}
