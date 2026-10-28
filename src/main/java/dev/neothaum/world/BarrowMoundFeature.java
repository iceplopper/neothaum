package dev.neothaum.world;

import dev.neothaum.NeoThaum;
import dev.neothaum.registry.TVBlocks;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Port of WorldGenMound: a grassy barrow, 19 blocks across, over a cobblestone crypt with stairs down, a chest between two urns or crates,
 * and a skeleton and a zombie spawner in its side chambers. Its 2,443 fixed placements are in data/neothaum/tc_structure/barrow_mound.txt
 * (written from the 1.7 source by agentic/tools/port_structure.py); the rolls are made here.
 */
public final class BarrowMoundFeature {
	private static final Block[] VALID = {Blocks.STONE, Blocks.GRASS_BLOCK, Blocks.DIRT};
	private static List<Placement> placements;

	private record Placement(int x, int y, int z, BlockState state) {
	}

	private BarrowMoundFeature() {
	}

	private static List<Placement> placements() {
		if (placements == null) {
			List<Placement> list = new ArrayList<>();
			try (var in = BarrowMoundFeature.class.getResourceAsStream("/data/neothaum/tc_structure/barrow_mound.txt");
				var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
				String line;
				while ((line = r.readLine()) != null) {
					String[] p = line.trim().split(" ", 4);
					if (p.length == 4) {
						BlockState s = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, p[3], false).blockState();
						list.add(new Placement(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), s));
					}
				}
			} catch (Exception e) {
				NeoThaum.LOGGER.error("Could not read the barrow mound", e);
			}
			placements = list;
		}
		return placements;
	}

	/** func_76484_a: (i, j, k) is the mound's corner, nine below the ground it sits on. */
	public static boolean generate(WorldGenLevel world, RandomSource rand, BlockPos origin) {
		int i = origin.getX(), j = origin.getY(), k = origin.getZ();
		if (!(TCSpawnCheck.valid(world, i + 9, j + 9, k + 9, VALID) && TCSpawnCheck.valid(world, i, j + 9, k, VALID)
			&& TCSpawnCheck.valid(world, i + 18, j + 9, k, VALID) && TCSpawnCheck.valid(world, i + 18, j + 9, k + 18, VALID)
			&& TCSpawnCheck.valid(world, i, j + 9, k + 18, VALID))) {
			return false;
		}
		for (Placement p : placements()) {
			world.setBlock(origin.offset(p.x, p.y, p.z), p.state, Block.UPDATE_CLIENTS);
		}
		// an urn or (three times in ten) a crate either side of the chest, a tenth of them rare and a quarter more uncommon
		for (int z : new int[]{7, 11}) {
			float rr = rand.nextFloat();
			int md = rr < 0.1F ? 2 : rr < 0.33F ? 1 : 0;
			boolean crate = rand.nextFloat() < 0.3F;
			Block loot = crate ? new Block[]{TVBlocks.LOOT_CRATE_COMMON, TVBlocks.LOOT_CRATE_UNCOMMON, TVBlocks.LOOT_CRATE_RARE}[md]
				: new Block[]{TVBlocks.LOOT_URN_COMMON, TVBlocks.LOOT_URN_UNCOMMON, TVBlocks.LOOT_URN_RARE}[md];
			world.setBlock(origin.offset(9, 1, z), loot.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		// a third of the chests are trapped, over TNT
		BlockPos chest = origin.offset(10, 1, 9);
		if (rand.nextInt(3) == 0) {
			world.setBlock(chest, Blocks.TRAPPED_CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
			world.setBlock(origin.offset(10, -1, 9), Blocks.TNT.defaultBlockState(), Block.UPDATE_CLIENTS);
		} else {
			world.setBlock(chest, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		RandomizableContainer.setBlockEntityLootTable(world, rand, chest, BuiltInLootTables.SIMPLE_DUNGEON);
		spawner(world, rand, origin.offset(4, 5, 4), EntityTypes.SKELETON);
		spawner(world, rand, origin.offset(4, 5, 14), EntityTypes.ZOMBIE);
		return true;
	}

	private static void spawner(WorldGenLevel world, RandomSource rand, BlockPos pos, EntityType<?> type) {
		if (world.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
			spawner.setEntityId(type, rand);
		}
	}
}
