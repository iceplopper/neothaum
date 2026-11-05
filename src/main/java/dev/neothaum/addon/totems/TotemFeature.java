package dev.neothaum.addon.totems;

import com.mojang.serialization.MapCodec;
import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Traveller's Totems in the world.
 * <ul>
 * <li>Every village has one, in the chunk where the village starts, on the nearest level natural ground to the village's start (never on
 * anything the village built).</li>
 * <li>Elsewhere, rarely (one chunk in {@link #RARITY}), in plains, meadows, forests, taiga and savanna, away from any structure.</li>
 * </ul>
 * Each stands on a raised three-by-three dais of arcane stone bricks set in a flush rim of arcane stone, and names itself once its
 * surroundings have finished generating (TotemBlockEntity.tick).
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
		if (!TravellersTotems.enabled()) {
			return false;
		}
		int cx = origin.getX() & ~15;
		int cz = origin.getZ() & ~15;
		var village = villageStartingHere(world, cx, cz);
		if (village != null) {
			boolean placed = inVillage(world, random, cx, cz, village);
			dev.neothaum.NeoThaum.LOGGER.info("Traveller's Totem for the village at chunk {}, {}: {}", cx >> 4, cz >> 4, placed ? "raised" : "no room");
			return placed;
		}
		return random.nextInt(RARITY) == 0 && raise(world, random, cx + 8, cz + 8);
	}

	/** A village whose start is in this chunk, or null. */
	private static net.minecraft.world.level.levelgen.structure.StructureStart villageStartingHere(WorldGenLevel world, int cx, int cz) {
		var chunk = world.getChunk(cx >> 4, cz >> 4);
		var registry = world.registryAccess().lookupOrThrow(Registries.STRUCTURE);
		for (var e : chunk.getAllStarts().entrySet()) {
			// looked up by key: a holder made straight from the structure carries no tags
			var key = registry.getResourceKey(e.getKey());
			if (e.getValue().isValid() && key.isPresent() && registry.getOrThrow(key.get()).is(StructureTags.VILLAGE)) {
				return e.getValue();
			}
		}
		return null;
	}

	/**
	 * A totem for a village: on level natural ground as near its start as can be, within the chunks this feature may build in, and clear of
	 * every building and path the village has (those in the neighbouring chunks aren't built yet, but the village knows where they go).
	 */
	public static boolean inVillage(WorldGenLevel world, RandomSource random, int cx, int cz,
		net.minecraft.world.level.levelgen.structure.StructureStart village) {
		java.util.List<BoundingBox> pieces = village.getPieces().stream().map(p -> p.getBoundingBox()).toList();
		int vx = cx + 8;
		int vz = cz + 8;
		BlockPos best = null;
		int bestScore = Integer.MAX_VALUE;
		for (int pass = 0; pass < 3 && best == null; pass++) {
			for (int x = cx - 14; x <= cx + 29; x++) {
				for (int z = cz - 14; z <= cz + 29; z++) {
					int score = (x - vx) * (x - vx) + (z - vz) * (z - vz);
					if (score >= bestScore || clashes(pieces, x, z, cx, cz)) {
						continue;
					}
					int g = groundIfFree(world, x, z, pass + 1);
					if (g != Integer.MIN_VALUE) {
						bestScore = score;
						best = new BlockPos(x, g, z);
					}
				}
			}
		}
		if (best == null) {
			return false;
		}
		build(world, random, best);
		return true;
	}

	/**
	 * Whether a dais here (five by five, with a block's margin) would reach into a building or path the village has yet to build: those in
	 * the neighbouring chunks, which aren't built until those chunks are. In this chunk the village is already built, and the blocks
	 * themselves decide (groundIfFree): open soil and dirt paths will do, so a dais may sit on a village path.
	 */
	private static boolean clashes(java.util.List<BoundingBox> pieces, int x, int z, int cx, int cz) {
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				int px = x + dx;
				int pz = z + dz;
				if (px >= cx && px < cx + 16 && pz >= cz && pz < cz + 16) {
					continue;
				}
				for (BoundingBox b : pieces) {
					if (px >= b.minX() && px <= b.maxX() && pz >= b.minZ() && pz <= b.maxZ()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** The ground's height under a five-by-five spot centred here, if it is natural, near level and open to the sky; else MIN_VALUE. */
	private static int groundIfFree(WorldGenLevel world, int x, int z, int unevenness) {
		int g = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				int h = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x + dx, z + dz) - 1;
				if (Math.abs(h - g) > unevenness) {
					return Integer.MIN_VALUE;
				}
				BlockPos p = new BlockPos(x + dx, h, z + dz);
				if (!natural(world.getBlockState(p)) || !world.getFluidState(p.above()).isEmpty()) {
					return Integer.MIN_VALUE;
				}
				for (int up = 1; up <= 3; up++) {
					BlockState above = world.getBlockState(p.above(up));
					if (!above.isAir() && !above.canBeReplaced()) {
						return Integer.MIN_VALUE;
					}
				}
			}
		}
		return g;
	}

	/** The ground a village leaves alone: soil, sand, snow and the village's own dirt paths. */
	private static boolean natural(BlockState s) {
		Block b = s.getBlock();
		// grass isn't in 26.3's dirt tag, so the soils are named outright
		return s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || b == Blocks.GRASS_BLOCK || b == Blocks.PODZOL || b == Blocks.MYCELIUM
			|| b == Blocks.COARSE_DIRT || b == Blocks.ROOTED_DIRT || b == Blocks.MOSS_BLOCK || b == Blocks.DIRT || b == Blocks.DIRT_PATH
			|| b == Blocks.SNOW_BLOCK || b == Blocks.GRAVEL || b == Blocks.SANDSTONE || b == Blocks.TERRACOTTA || b == Blocks.MUD;
	}

	/** Raises a wild totem and its dais at this column, if the land suits; for the feature and for tests. */
	public static boolean raise(WorldGenLevel world, RandomSource random, int x, int z) {
		int y = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
		BlockPos ground = new BlockPos(x, y - 1, z);
		if (!suits(world.getBiome(ground)) || !world.getFluidState(ground.above()).isEmpty() || !world.getBlockState(ground).isSolid()
			|| !world.getChunk(ground).getAllReferences().isEmpty()) {
			return false;
		}
		// level ground only
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (Math.abs(world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x + dx, z + dz) - y) > 1) {
					return false;
				}
			}
		}
		build(world, random, ground);
		return true;
	}

	/**
	 * The dais, with the ground block at {@code ground}: a flush rim of arcane stone (five by five, without its corners), a raised three by
	 * three of arcane stone bricks, and the totem on top; a footing beneath and nothing growing through it.
	 */
	public static void build(WorldGenLevel world, RandomSource random, BlockPos ground) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (Math.abs(dx) == 2 && Math.abs(dz) == 2) {
					continue;
				}
				boolean core = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
				BlockPos p = ground.offset(dx, 0, dz);
				world.setBlock(p, TVBlocks.ARCANE_STONE_BLOCK.defaultBlockState(), 2);
				for (int down = 1; down <= 3 && !world.getBlockState(p.below(down)).isSolid(); down++) {
					world.setBlock(p.below(down), Blocks.COBBLESTONE.defaultBlockState(), 2);
				}
				for (int up = 1; up <= 3; up++) {
					world.setBlock(p.above(up), Blocks.AIR.defaultBlockState(), 2);
				}
				if (core) {
					world.setBlock(p.above(), TVBlocks.ARCANE_STONE_BRICKS.defaultBlockState(), 2);
				} else if (random.nextInt(5) == 0) {
					world.setBlock(p.above(), Blocks.MOSS_CARPET.defaultBlockState(), 2);
				}
			}
		}
		BlockPos at = ground.above(2);
		world.setBlock(at, TravellersTotems.TOTEM.defaultBlockState(), 2);
		if (world.getBlockEntity(at) instanceof TotemBlockEntity totem) {
			totem.natural = true;
		}
	}

	private static boolean suits(Holder<Biome> b) {
		return b.is(BiomeTags.IS_FOREST) || b.is(BiomeTags.IS_TAIGA) || b.is(BiomeTags.IS_SAVANNA) || b.is(Biomes.PLAINS)
			|| b.is(Biomes.SUNFLOWER_PLAINS) || b.is(Biomes.MEADOW);
	}
}
