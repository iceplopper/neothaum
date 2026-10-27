package dev.neothaum.world;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * 1:1 Port of WorldGenMound.
 * Generates an ancient earth-covered burial vault with stone/cobble chambers,
 * mob spawners, treasure chests with dungeon loot, and a hovering Sinister aura node.
 */
public final class BarrowMoundFeature {

	private BarrowMoundFeature() {}

	public static boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin) {
		int i = origin.getX();
		int j = origin.getY();
		int k = origin.getZ();

		// Check terrain suitability
		BlockPos centerCheck = new BlockPos(i + 9, j + 9, k + 9);
		if (!world.getBlockState(centerCheck).isSolid() && !world.getBlockState(centerCheck.below()).isSolid()) {
			return false;
		}

		// Outer shell: mound dome of earth/grass
		int radius = 9;
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				double dist = Math.sqrt(x * x + z * z);
				if (dist <= radius) {
					int height = (int) (Math.cos((dist / radius) * (Math.PI / 2.0)) * 6.0);
					for (int y = 0; y <= height; y++) {
						BlockPos p = new BlockPos(i + 9 + x, j + 7 + y, k + 9 + z);
						if (y == height) {
							world.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
						} else if (y >= height - 2) {
							world.setBlock(p, Blocks.DIRT.defaultBlockState(), 3);
						} else {
							// Stone & cobblestone vault ceiling
							BlockState st = (random.nextInt(3) == 0) ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
							world.setBlock(p, st, 3);
						}
					}
				}
			}
		}

		// Hollow out internal tomb chamber (radius 6, height 4)
		for (int x = -5; x <= 5; x++) {
			for (int z = -5; z <= 5; z++) {
				if (x * x + z * z <= 25) {
					// Floor
					BlockPos floorPos = new BlockPos(i + 9 + x, j + 7, k + 9 + z);
					BlockState floorTile = (random.nextInt(4) == 0) ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
					world.setBlock(floorPos, floorTile, 3);

					// Air inside chamber
					for (int y = 1; y <= 4; y++) {
						BlockPos airPos = floorPos.above(y);
						world.setBlock(airPos, Blocks.AIR.defaultBlockState(), 3);
					}
				}
			}
		}

		// Mob spawners inside
		BlockPos spawner1 = new BlockPos(i + 6, j + 8, k + 6);
		world.setBlock(spawner1, Blocks.SPAWNER.defaultBlockState(), 3);
		if (world.getBlockEntity(spawner1) instanceof SpawnerBlockEntity sbe1) {
			sbe1.setEntityId(EntityTypes.SKELETON, random);
		}

		BlockPos spawner2 = new BlockPos(i + 12, j + 8, k + 12);
		world.setBlock(spawner2, Blocks.SPAWNER.defaultBlockState(), 3);
		if (world.getBlockEntity(spawner2) instanceof SpawnerBlockEntity sbe2) {
			sbe2.setEntityId(EntityTypes.ZOMBIE, random);
		}

		// Central treasure chest
		BlockPos chestPos = new BlockPos(i + 9, j + 8, k + 9);
		world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
		if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
			chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, random.nextLong());
		}

		// Sinister aura node hovering in the upper vault
		BlockPos nodePos = new BlockPos(i + 9, j + 10, k + 9);
		NodeGenerator.createRandomNodeAt(world, nodePos, random, false, true, false, true);

		return true;
	}
}
