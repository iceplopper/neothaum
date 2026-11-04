package dev.neothaum.addon.totems;

import com.mojang.serialization.MapCodec;
import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * A Traveller's Totem left in the world: rarely (one chunk in {@link #RARITY}), in plains, meadows, forests, taiga and savanna, never where
 * a structure stands. It stands on a weathered three-by-three dais of arcane stone; it names itself once its surroundings have finished
 * generating (TotemBlockEntity.tick).
 */
public record TotemFeature() implements Feature {
	public static final MapCodec<TotemFeature> CODEC = MapCodec.unit(new TotemFeature());
	/** One chunk in this many: about one totem every eight or nine hundred blocks in the lands they favour. */
	public static final int RARITY = 3000;

	@Override
	public MapCodec<TotemFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator gen, RandomSource random, BlockPos origin) {
		if (!TravellersTotems.enabled() || random.nextInt(RARITY) != 0) {
			return false;
		}
		int x = (origin.getX() & ~15) + 8;
		int z = (origin.getZ() & ~15) + 8;
		return raise(world, random, x, z);
	}

	/** Raises a totem and its dais at this column, if the land suits; for the feature and for tests. */
	public static boolean raise(WorldGenLevel world, RandomSource random, int x, int z) {
		int y = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
		BlockPos ground = new BlockPos(x, y - 1, z);
		if (!suits(world.getBiome(ground)) || !world.getFluidState(ground.above()).isEmpty() || !world.getBlockState(ground).isSolid()
			|| !world.getChunk(ground).getAllReferences().isEmpty()) {
			return false;
		}
		// level ground only: the dais's corners within a block of each other
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (Math.abs(world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x + dx, z + dz) - y) > 1) {
					return false;
				}
			}
		}
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				BlockPos p = ground.offset(dx, 0, dz);
				BlockState stone = random.nextInt(3) == 0 ? TVBlocks.ARCANE_STONE_BRICKS.defaultBlockState() : TVBlocks.ARCANE_STONE_BLOCK.defaultBlockState();
				world.setBlock(p, stone, 2);
				// nothing growing through it, and a footing beneath
				world.setBlock(p.above(), Blocks.AIR.defaultBlockState(), 2);
				world.setBlock(p.above(2), Blocks.AIR.defaultBlockState(), 2);
				if (!world.getBlockState(p.below()).isSolid()) {
					world.setBlock(p.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
				}
				if ((dx != 0 || dz != 0) && random.nextInt(4) == 0) {
					world.setBlock(p.above(), Blocks.MOSS_CARPET.defaultBlockState(), 2);
				}
			}
		}
		BlockPos at = ground.above();
		world.setBlock(at, TravellersTotems.TOTEM.defaultBlockState(), 2);
		if (world.getBlockEntity(at) instanceof TotemBlockEntity totem) {
			totem.natural = true;
		}
		return true;
	}

	private static boolean suits(Holder<Biome> b) {
		return b.is(BiomeTags.IS_FOREST) || b.is(BiomeTags.IS_TAIGA) || b.is(BiomeTags.IS_SAVANNA) || b.is(Biomes.PLAINS)
			|| b.is(Biomes.SUNFLOWER_PLAINS) || b.is(Biomes.MEADOW);
	}
}
