package dev.neothaum.world;

import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * 1:1 Port of ComponentWizardTower.
 * 5x5 three-story stone tower housing arcane research, bookshelves, and high-tier thaumic loot.
 */
public final class WizardTowerFeature {

	private WizardTowerFeature() {}

	public static boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin) {
		int cx = origin.getX();
		int cy = origin.getY();
		int cz = origin.getZ();

		// Tower dimensions: 5x5 base, 12 blocks high
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				// Solid foundation down to surface
				for (int dy = 0; dy <= 4; dy++) {
					BlockPos bp = new BlockPos(cx + x, cy - dy, cz + z);
					if (world.isEmptyBlock(bp) || !world.getBlockState(bp).isSolid()) {
						world.setBlock(bp, Blocks.COBBLESTONE.defaultBlockState(), 3);
					}
				}

				// Walls and interior
				boolean isWall = Math.abs(x) == 2 || Math.abs(z) == 2;
				for (int y = 1; y <= 12; y++) {
					BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
					if (isWall) {
						if (y == 12) {
							// Battlements
							if ((x + z) % 2 == 0) {
								world.setBlock(p, Blocks.STONE_BRICK_WALL.defaultBlockState(), 3);
							} else {
								world.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 3);
							}
						} else {
							world.setBlock(p, (random.nextInt(4) == 0) ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState(), 3);
						}
					} else {
						// Floor plates at y = 1, y = 5, y = 10
						if (y == 1 || y == 5 || y == 10) {
							world.setBlock(p, Blocks.OAK_PLANKS.defaultBlockState(), 3);
						} else {
							world.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
						}
					}
				}
			}
		}

		// Door on South face
		BlockPos doorBottom = new BlockPos(cx, cy + 1, cz + 2);
		BlockPos doorTop = doorBottom.above();
		world.setBlock(doorBottom, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.FACING, Direction.SOUTH), 3);
		world.setBlock(doorTop, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.FACING, Direction.SOUTH), 3);

		// Ladder in North-East corner going up all floors
		for (int y = 1; y <= 10; y++) {
			BlockPos ladderPos = new BlockPos(cx + 1, cy + y, cz + 1);
			world.setBlock(ladderPos, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 3);
		}

		// Middle floor furniture (y = 6)
		world.setBlock(new BlockPos(cx - 1, cy + 6, cz - 1), Blocks.BOOKSHELF.defaultBlockState(), 3);
		world.setBlock(new BlockPos(cx, cy + 6, cz - 1), Blocks.BOOKSHELF.defaultBlockState(), 3);
		world.setBlock(new BlockPos(cx - 1, cy + 6, cz), TVBlocks.ARCANE_WORKTABLE.defaultBlockState(), 3);

		// Top floor chest (y = 11) with authentic Thaumcraft tower loot
		BlockPos chestPos = new BlockPos(cx - 1, cy + 11, cz - 1);
		world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
		if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
			chest.setItem(0, new ItemStack(TVItems.THAUMONOMICON));
			chest.setItem(1, new ItemStack(TVItems.SCRIBING_TOOLS));
			chest.setItem(2, new ItemStack(Items.PAPER, 3 + random.nextInt(4)));
			chest.setItem(3, new ItemStack(TVItems.AMBER, 1 + random.nextInt(3)));
			chest.setItem(4, new ItemStack(TVItems.AIR_SHARD, 1 + random.nextInt(2)));
			chest.setItem(5, new ItemStack(TVItems.FIRE_SHARD, 1 + random.nextInt(2)));
		}

		return true;
	}
}
