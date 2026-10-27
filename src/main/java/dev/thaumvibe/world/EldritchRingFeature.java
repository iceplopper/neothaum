package dev.thaumvibe.world;

import dev.thaumvibe.blocks.eldritch.EldritchStoneBlock;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.tiles.BannerBlockEntity;
import dev.thaumvibe.tiles.eldritch.EldritchAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1:1 Port of WorldGenEldritchRing.
 * Generates an Eldritch Ring structure with obsidian tiles, perimeter monolith pillars,
 * central Eldritch Altar, hovering Sinister node, Eldritch Cap, and obelisk spire.
 */
public final class EldritchRingFeature {

	private EldritchRingFeature() {}

	public static boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin) {
		int cx = origin.getX();
		int cy = origin.getY();
		int cz = origin.getZ();

		// Validate terrain flatness around center
		for (int x = -3; x <= 3; x += 3) {
			for (int z = -3; z <= 3; z += 3) {
				BlockPos check = new BlockPos(cx + x, cy, cz + z);
				if (!world.getBlockState(check).isSolid() && !world.getBlockState(check.below()).isSolid()) {
					return false;
				}
			}
		}

		// Excavate and lay foundation
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				if (Math.abs(x) == 3 && Math.abs(z) == 3) {
					continue; // skip corners
				}

				BlockPos surfacePos = new BlockPos(cx + x, cy, cz + z);

				// Foundation beneath
				for (int q = -3; q <= 0; q++) {
					BlockPos bp = surfacePos.above(q);
					BlockState tile = (random.nextInt(4) == 0) ? Blocks.OBSIDIAN.defaultBlockState() : TVBlocks.OBSIDIAN_TILE.defaultBlockState();
					world.setBlock(bp, tile, 3);
				}

				// Clear air above
				for (int q = 1; q <= 8; q++) {
					world.setBlock(surfacePos.above(q), Blocks.AIR.defaultBlockState(), 3);
				}

				// Center column
				if (x == 0 && z == 0) {
					// 1. Altar
					BlockPos altarPos = surfacePos.above(1);
					world.setBlock(altarPos, TVBlocks.ELDRITCH_ALTAR.defaultBlockState(), 3);
					if (world.getBlockEntity(altarPos) instanceof EldritchAltarBlockEntity altar) {
						int r = random.nextInt(10);
						if (r < 4) {
							// Cultist spawner with banners
							altar.setSpawner(true);
							altar.setSpawnType((byte) 0);
							for (Direction dir : Direction.Plane.HORIZONTAL) {
								BlockPos bannerPos = altarPos.relative(dir, 3);
								world.setBlock(bannerPos, TVBlocks.BANNER.defaultBlockState(), 3);
								if (world.getBlockEntity(bannerPos) instanceof BannerBlockEntity banner) {
									banner.setFacing(dir.getOpposite());
								}
							}
						} else if (r < 6) {
							// Eldritch Guardian spawner
							altar.setSpawner(true);
							altar.setSpawnType((byte) 1);
						}
					}

					// 2. Sinister Node hovering at altarPos.above(1)
					BlockPos nodePos = altarPos.above(1);
					NodeGenerator.createRandomNodeAt(world, nodePos, random, false, true, false, true);

					// 3. Cap
					BlockPos capPos = altarPos.above(2);
					world.setBlock(capPos, TVBlocks.ELDRITCH_CAP.defaultBlockState(), 3);

					// 4. Obelisk shaft (4 blocks tall)
					for (int dy = 3; dy <= 6; dy++) {
						world.setBlock(altarPos.above(dy), TVBlocks.ELDRITCH_OBELISK.defaultBlockState(), 3);
					}
				} else if (((Math.abs(x) == 3 && Math.abs(z) % 2 == 1) || (Math.abs(z) == 3 && Math.abs(x) % 2 == 1)) && Math.abs(x) != Math.abs(z)) {
					// Perimeter pillars
					BlockState pillarBlock;
					int r = random.nextInt(10);
					if (r < 6) {
						pillarBlock = TVBlocks.ELDRITCH_STONE.defaultBlockState();
					} else if (r < 8) {
						pillarBlock = TVBlocks.ELDRITCH_STONE_CRACKED.defaultBlockState();
					} else {
						pillarBlock = TVBlocks.ELDRITCH_STONE_GLYPH.defaultBlockState();
					}
					world.setBlock(surfacePos.above(1), pillarBlock, 3);
				}
			}
		}

		return true;
	}
}
