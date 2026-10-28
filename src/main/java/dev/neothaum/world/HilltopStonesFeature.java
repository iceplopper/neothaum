package dev.neothaum.world;

import dev.neothaum.registry.TVBlocks;
import dev.neothaum.entity.TVEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Port of WorldGenHilltopStones: on high ground (y 85 and up) a seven-wide floor of obsidian and obsidian tile, ringed by eight obsidian
 * totems one to four high, vine-hung outside snowy places. In its middle a dungeon chest stands on a tile over a wisp spawner. The ground
 * under the floor is filled in where it was air or plants.
 */
public final class HilltopStonesFeature {
	private static final Block[] VALID = {Blocks.STONE, Blocks.GRASS_BLOCK, Blocks.DIRT};

	private HilltopStonesFeature() {
	}

	private static boolean validSpawn(WorldGenLevel world, int i, int j, int k) {
		return j >= 85 && TCSpawnCheck.valid(world, i, j, k, VALID);
	}

	public static boolean generate(WorldGenLevel world, RandomSource rand, BlockPos origin) {
		int i = origin.getX(), j = origin.getY(), k = origin.getZ();
		if (!(validSpawn(world, i - 2, j, k - 2) && validSpawn(world, i, j, k) && validSpawn(world, i + 2, j, k) && validSpawn(world, i + 2, j, k + 2)
			&& validSpawn(world, i, j, k + 2))) {
			return false;
		}
		// the biome's top block fills holes under the floor; vines only where it does not snow
		var biome = world.getBiome(origin).value();
		BlockState replace = biome.getBaseTemperature() > 1.0F && !biome.hasPrecipitation() ? Blocks.SAND.defaultBlockState()
			: Blocks.GRASS_BLOCK.defaultBlockState();
		boolean genVines = !biome.coldEnoughToSnow(origin, world.getSeaLevel());
		BlockState tile = TVBlocks.OBSIDIAN_TILE.defaultBlockState();
		for (int x = i - 3; x <= i + 3; x++) {
			for (int z = k - 3; z <= k + 3; z++) {
				if ((x == i - 3 || x == i + 3) && (z == k - 3 || z == k + 3)) {
					continue;
				}
				set(world, x, j, z, rand.nextBoolean() ? tile : Blocks.OBSIDIAN.defaultBlockState());
				boolean stop = false;
				for (int y = 1; y < 5; y++) {
					if (j - y < world.getMinY()) {
						continue;
					}
					BlockState below = world.getBlockState(new BlockPos(x, j - y, z));
					if (below.is(Blocks.SNOW) || below.is(BlockTags.SMALL_FLOWERS) || below.is(Blocks.SHORT_GRASS) || below.isAir()) {
						set(world, x, j - y, z, replace);
					}
					if (x == i && z == k && y == 1) {
						set(world, x, j + 1, z, tile);
						BlockPos chest = new BlockPos(x, j + 2, z);
						set(world, x, j + 2, z, Blocks.CHEST.defaultBlockState());
						RandomizableContainer.setBlockEntityLootTable(world, rand, chest, BuiltInLootTables.SIMPLE_DUNGEON);
						BlockPos sp = new BlockPos(x, j, z);
						set(world, x, j, z, Blocks.SPAWNER.defaultBlockState());
						if (world.getBlockEntity(sp) instanceof SpawnerBlockEntity spawner) {
							spawner.setEntityId(TVEntities.WISP, rand);
						}
					}
					if (!stop && ((x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1 || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1)) {
						set(world, x, j + y, z, TVBlocks.OBSIDIAN_TOTEM.defaultBlockState());
						if (y >= 2 && rand.nextBoolean()) {
							stop = true;
							if (genVines) {
								vines(world, rand, x - 1, j + y, z, VineBlock.EAST);
								vines(world, rand, x + 1, j + y, z, VineBlock.WEST);
								vines(world, rand, x, j + y, z - 1, VineBlock.SOUTH);
								vines(world, rand, x, j + y, z + 1, VineBlock.NORTH);
							}
						}
					}
				}
			}
		}
		return true;
	}

	/** One time in three, if there is room, a vine on the totem's face hanging down up to four more blocks (growVines). */
	private static void vines(WorldGenLevel world, RandomSource rand, int x, int y, int z, BooleanProperty side) {
		if (rand.nextInt(3) != 0 || !world.isEmptyBlock(new BlockPos(x, y, z))) {
			return;
		}
		BlockState vine = Blocks.VINE.defaultBlockState().setValue(side, true);
		set(world, x, y, z, vine);
		for (int n = 4; n > 0 && world.isEmptyBlock(new BlockPos(x, --y, z)); n--) {
			set(world, x, y, z, vine);
		}
	}

	private static void set(WorldGenLevel world, int x, int y, int z, BlockState s) {
		world.setBlock(new BlockPos(x, y, z), s, Block.UPDATE_CLIENTS);
	}
}
