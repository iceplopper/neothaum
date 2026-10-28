package dev.neothaum.world;

import dev.neothaum.blocks.eldritch.EldritchObeliskBlock;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.BannerBlockEntity;
import dev.neothaum.tiles.eldritch.EldritchAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of WorldGenEldritchRing: a seven-wide disc of obsidian tile sunk into the ground. At its centre an eldritch altar stands under a
 * floating obelisk; around its rim eight obsidian pillars each carry an eldritch cap. Four in ten altars summon cultists and are ringed by
 * their banners, two in ten summon eldritch guardians. An eerie node hangs between altar and obelisk.
 */
public final class EldritchRingFeature {
	private static final Block[] VALID = {Blocks.STONE, Blocks.SAND, Blocks.PACKED_ICE, Blocks.GRASS_BLOCK, Blocks.GRAVEL, Blocks.DIRT};

	private EldritchRingFeature() {
	}

	private static boolean validSpawn(WorldGenLevel world, int i, int j, int k) {
		return TCSpawnCheck.valid(world, i, j, k, VALID);
	}

	public static boolean generate(WorldGenLevel world, RandomSource rand, BlockPos origin) {
		int i = origin.getX(), j = origin.getY(), k = origin.getZ();
		if (!(validSpawn(world, i - 3, j, k - 3) && validSpawn(world, i, j, k) && validSpawn(world, i + 3, j, k) && validSpawn(world, i + 3, j, k + 3)
			&& validSpawn(world, i, j, k + 3))) {
			return false;
		}
		BlockState tile = TVBlocks.OBSIDIAN_TILE.defaultBlockState();
		for (int x = i - 3; x <= i + 3; x++) {
			for (int z = k - 3; z <= k + 3; z++) {
				if ((x == i - 3 || x == i + 3) && (z == k - 3 || z == k + 3)) {
					continue;
				}
				for (int q = -4; q < 5; q++) {
					BlockPos p = new BlockPos(x, j + q, z);
					BlockState bb = world.getBlockState(p);
					if (q <= 0 || bb.canBeReplaced() || !bb.isSolid() || bb.is(BlockTags.LEAVES)) {
						world.setBlock(p, rand.nextInt(4) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : tile, 3);
					}
					if (q > 0) {
						world.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
					}
				}
				if (x == i && z == k) {
					BlockPos altarPos = new BlockPos(x, j + 1, z);
					world.setBlock(altarPos, TVBlocks.ELDRITCH_ALTAR.defaultBlockState(), 3);
					world.setBlock(new BlockPos(x, j, z), tile, 3);
					int r = rand.nextInt(10);
					if (world.getBlockEntity(altarPos) instanceof EldritchAltarBlockEntity altar) {
						switch (r) {
							case 1, 2, 3, 4 -> {
								altar.setSpawner(true);
								altar.setSpawnType((byte) 0);
								// a cultist banner three blocks out on each side, turned to face away from the altar
								int[][] banners = {{0, -3, 8}, {0, 3, 0}, {3, 0, 12}, {-3, 0, 4}};
								for (int[] b : banners) {
									BlockPos bp = new BlockPos(x + b[0], j + 1, z + b[1]);
									world.setBlock(bp, TVBlocks.BANNER.defaultBlockState(), 3);
									if (world.getBlockEntity(bp) instanceof BannerBlockEntity banner) {
										banner.setRotation(b[2]);
									}
								}
							}
							case 6, 7 -> {
								altar.setSpawner(true);
								altar.setSpawnType((byte) 1);
							}
							default -> {
							}
						}
					}
					world.setBlock(new BlockPos(x, j + 3, z), TVBlocks.ELDRITCH_OBELISK.defaultBlockState(), 3);
					BlockState shaft = TVBlocks.ELDRITCH_OBELISK.defaultBlockState().setValue(EldritchObeliskBlock.BASE, false);
					for (int y = 4; y <= 7; y++) {
						world.setBlock(new BlockPos(x, j + y, z), shaft, 3);
					}
				} else if (((x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1 || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1)
					&& Math.abs(x - i) != Math.abs(z - k)) {
					world.setBlock(new BlockPos(x, j, z), tile, 3);
					world.setBlock(new BlockPos(x, j + 1, z), TVBlocks.ELDRITCH_CAP.defaultBlockState(), 3);
				}
			}
		}
		// ThaumcraftWorldGenerator: an eerie node hangs between the altar and the obelisk
		NodeGenerator.createRandomNodeAt(world, new BlockPos(i, j + 2, k), rand, false, true, false, true);
		return true;
	}
}
