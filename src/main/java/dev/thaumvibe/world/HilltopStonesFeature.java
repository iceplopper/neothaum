package dev.thaumvibe.world;

import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * 1:1 Port of WorldGenHilltopStones.
 * Generates an ancient megalith stone circle on hilltops (y >= 85) made of Arcane Stone
 * and Obsidian Tiles, a central Wisp spawner beneath a trapped chest, and a floating aura node.
 */
public final class HilltopStonesFeature {

	private HilltopStonesFeature() {}

	public static boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin) {
		int i = origin.getX();
		int j = origin.getY();
		int k = origin.getZ();

		if (j < 85) {
			return false;
		}

		// Lay 7x7 stone foundation and standing monoliths
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				if (Math.abs(x) == 3 && Math.abs(z) == 3) {
					continue; // skip corners
				}

				BlockPos ground = new BlockPos(i + x, j, k + z);
				BlockState tile = (random.nextBoolean()) ? TVBlocks.OBSIDIAN_TILE.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
				world.setBlock(ground, tile, 3);

				// Fill underneath if floating
				for (int dy = 1; dy <= 4; dy++) {
					BlockPos below = ground.below(dy);
					if (world.isEmptyBlock(below) || !world.getBlockState(below).isSolid()) {
						world.setBlock(below, Blocks.STONE.defaultBlockState(), 3);
					}
				}

				// Standing pillars on outer ring
				if (((Math.abs(x) == 3 && Math.abs(z) % 2 == 1) || (Math.abs(z) == 3 && Math.abs(x) % 2 == 1))) {
					int pillarHeight = 2 + random.nextInt(3);
					for (int y = 1; y <= pillarHeight; y++) {
						BlockPos pillarPos = ground.above(y);
						world.setBlock(pillarPos, TVBlocks.ARCANE_STONE_BLOCK.defaultBlockState(), 3);

						// Hanging vines
						for (Direction dir : Direction.Plane.HORIZONTAL) {
							if (random.nextInt(3) == 0) {
								BlockPos vinePos = pillarPos.relative(dir);
								if (world.isEmptyBlock(vinePos)) {
									world.setBlock(vinePos, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(dir.getOpposite()), true), 3);
								}
							}
						}
					}
				}
			}
		}

		// Center: Wisp spawner under trapped chest
		BlockPos spawnerPos = new BlockPos(i, j + 1, k);
		world.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 3);
		if (world.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity sbe) {
			sbe.setEntityId(TVEntities.WISP, random);
		}

		BlockPos chestPos = new BlockPos(i, j + 2, k);
		world.setBlock(chestPos, Blocks.TRAPPED_CHEST.defaultBlockState(), 3);
		if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
			chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, random.nextLong());
		}

		// Hovering aura node above chest
		BlockPos nodePos = new BlockPos(i, j + 4, k);
		NodeGenerator.createRandomNodeAt(world, nodePos, random, false, false, false, true);

		return true;
	}
}
