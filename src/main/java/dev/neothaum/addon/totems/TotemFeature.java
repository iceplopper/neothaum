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
 * Each stands on a full block of arcane stone, ringed by arcane stone brick slabs, with a flush block of arcane stone at each cardinal
 * point; it names itself once its surroundings have finished generating (TotemBlockEntity.tick).
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
			dev.neothaum.NeoThaum.LOGGER.info("Traveller's Totem for the village at chunk {}, {}: {}", cx >> 4, cz >> 4,
				placed ? "raised" : "no room yet, to be tried once the village is built");
			if (!placed && world.getLevel().dimension() == net.minecraft.world.level.Level.OVERWORLD) {
				VillageTotems.defer(cx >> 4, cz >> 4);
			}
			return placed;
		}
		return random.nextInt(RARITY) == 0 && raise(world, random, cx + 8, cz + 8);
	}

	/** A village whose start is in this chunk, or null. */
	static net.minecraft.world.level.levelgen.structure.StructureStart villageStartingHere(net.minecraft.world.level.LevelReader world, int cx, int cz) {
		return villageIn(world.registryAccess(), world.getChunk(cx >> 4, cz >> 4));
	}

	/** The village starting in this chunk, or null. */
	static net.minecraft.world.level.levelgen.structure.StructureStart villageIn(net.minecraft.core.RegistryAccess access,
		net.minecraft.world.level.chunk.ChunkAccess chunk) {
		var registry = access.lookupOrThrow(Registries.STRUCTURE);
		for (var e : chunk.getAllStarts().entrySet()) {
			// looked up by key: a holder made straight from the structure carries no tags
			var key = registry.getResourceKey(e.getKey());
			if (e.getValue().isValid() && key.isPresent() && registry.getOrThrow(key.get()).is(StructureTags.VILLAGE)) {
				return e.getValue();
			}
		}
		return null;
	}

	/** A piece of a village: where it is, and whether it is a street (which a dais may sit on, as on any path). */
	record Piece(BoundingBox box, boolean street) {
		static java.util.List<Piece> of(net.minecraft.world.level.levelgen.structure.StructureStart village) {
			return village.getPieces().stream().map(p -> new Piece(p.getBoundingBox(),
				p instanceof net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece pool
					&& pool.getElement().toString().contains("street"))).toList();
		}
	}

	/**
	 * A totem for a village as it generates: on level natural ground as near its start as can be, within the chunks this feature may build
	 * in, and clear of every building the village has (and of its streets in the neighbouring chunks, which aren't built yet).
	 */
	public static boolean inVillage(WorldGenLevel world, RandomSource random, int cx, int cz,
		net.minecraft.world.level.levelgen.structure.StructureStart village) {
		var pieces = Piece.of(village);
		BlockPos best = nearest(world, pieces, cx + 8, cz + 8, cx - 14, cz - 14, cx + 29, cz + 29, Heightmap.Types.OCEAN_FLOOR_WG,
			(x, z) -> x >= cx && x < cx + 16 && z >= cz && z < cz + 16, false);
		if (best == null) {
			dev.neothaum.NeoThaum.LOGGER.info("No room for a village totem at chunk {}, {} yet: {}", cx >> 4, cz >> 4,
				reasons(world, pieces, cx - 14, cz - 14, cx + 29, cz + 29, Heightmap.Types.OCEAN_FLOOR_WG, (x, z) -> x >= cx && x < cx + 16 && z >= cz && z < cz + 16,
					false));
			return false;
		}
		build(world, random, best);
		return true;
	}

	/** How far from a village's start a totem may stand once the village is built. */
	public static final int BUILT_REACH = 64;

	/**
	 * A totem for a village already built (an older world's, or one with no room while it generated): the same choice of ground, over a wider
	 * area, judged by the blocks now there. Every chunk within {@link #BUILT_REACH} blocks of the start must be loaded.
	 */
	public static BlockPos inBuiltVillage(net.minecraft.server.level.ServerLevel level, RandomSource random, int cx, int cz,
		net.minecraft.world.level.levelgen.structure.StructureStart village) {
		var pieces = Piece.of(village);
		int vx = cx + 8;
		int vz = cz + 8;
		BlockPos best = nearest(level, pieces, vx, vz, vx - BUILT_REACH, vz - BUILT_REACH, vx + BUILT_REACH, vz + BUILT_REACH,
			Heightmap.Types.OCEAN_FLOOR, (x, z) -> true, true);
		if (best == null) {
			dev.neothaum.NeoThaum.LOGGER.info("No room for a totem in the village at chunk {}, {}: {}", cx >> 4, cz >> 4,
				reasons(level, pieces, vx - BUILT_REACH, vz - BUILT_REACH, vx + BUILT_REACH, vz + BUILT_REACH, Heightmap.Types.OCEAN_FLOOR, (x, z) -> true,
					true));
			return null;
		}
		build(level, random, best);
		return best.above(2);
	}

	/** Where a block is built already, so the blocks there decide; elsewhere the village's plan does. */
	interface Built {
		boolean at(int x, int z);
	}

	/**
	 * The nearest spot to (vx, vz) in the area, on its strictest pass that finds one, where a dais fits; null if none. {@code planned}: the
	 * whole village's plan is known to be built, so its buildings are kept clear by their outlines everywhere, its paved streets will do as
	 * ground, and (with more room to look) a fourth, looser pass is allowed.
	 */
	private static BlockPos nearest(WorldGenLevel world, java.util.List<Piece> pieces, int vx, int vz, int x0, int z0, int x1, int z1,
		Heightmap.Types heights, Built built, boolean planned) {
		BlockPos best = null;
		int bestScore = Integer.MAX_VALUE;
		for (int pass = 0; pass < (planned ? 4 : 3) && best == null; pass++) {
			for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) {
					int score = (x - vx) * (x - vx) + (z - vz) * (z - vz);
					if (score >= bestScore || clashes(pieces, x, z, built, planned)) {
						continue;
					}
					int g = groundIfFree(world, x, z, pass + 1, heights, planned);
					if (g != Integer.MIN_VALUE) {
						bestScore = score;
						best = new BlockPos(x, g, z);
					}
				}
			}
		}
		return best;
	}

	/**
	 * Whether a dais here (five by five, with a block's margin) would reach into the village. Where the village isn't built yet: into anything
	 * it will build there. Where it is: while generating, the blocks alone decide (groundIfFree: buildings aren't natural ground); once the
	 * whole village is built ({@code planned}), into any building's outline, though its streets will do, as open soil and paths will.
	 */
	private static boolean clashes(java.util.List<Piece> pieces, int x, int z, Built built, boolean planned) {
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				int px = x + dx;
				int pz = z + dz;
				boolean here = built.at(px, pz);
				if (here && !planned) {
					continue;
				}
				for (Piece p : pieces) {
					BoundingBox b = p.box();
					if ((!here || !p.street()) && px >= b.minX() && px <= b.maxX() && pz >= b.minZ() && pz <= b.maxZ()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** The ground's height under a five-by-five spot centred here, if it is natural, near level and open to the sky; else MIN_VALUE. */
	private static int groundIfFree(WorldGenLevel world, int x, int z, int unevenness, Heightmap.Types heights, boolean paved) {
		return why(world, x, z, unevenness, heights, paved) == null ? world.getHeight(heights, x, z) - 1 : Integer.MIN_VALUE;
	}

	/** Why a spot won't do for a dais, or null if it will. {@code paved}: a desert village's smooth sandstone streets will do. */
	private static String why(WorldGenLevel world, int x, int z, int unevenness, Heightmap.Types heights, boolean paved) {
		int g = world.getHeight(heights, x, z) - 1;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				int h = world.getHeight(heights, x + dx, z + dz) - 1;
				if (Math.abs(h - g) > unevenness) {
					return "uneven";
				}
				BlockPos p = new BlockPos(x + dx, h, z + dz);
				if (!world.getFluidState(p.above()).isEmpty() || !world.getFluidState(p).isEmpty()) {
					return "water";
				}
				if (!natural(world.getBlockState(p)) && !(paved && world.getBlockState(p).is(Blocks.SMOOTH_SANDSTONE))) {
					return "ground " + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(world.getBlockState(p).getBlock()).getPath();
				}
				for (int up = 1; up <= 3; up++) {
					BlockState above = world.getBlockState(p.above(up));
					if (!above.isAir() && !above.canBeReplaced()) {
						return "under " + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(above.getBlock()).getPath();
					}
				}
			}
		}
		return null;
	}

	/** Why each spot in the area (every other one) won't do, counted, for the log. */
	private static java.util.Map<String, Integer> reasons(WorldGenLevel world, java.util.List<Piece> pieces, int x0, int z0, int x1, int z1,
		Heightmap.Types heights, Built built, boolean planned) {
		java.util.Map<String, Integer> why = new java.util.TreeMap<>();
		for (int x = x0; x <= x1; x += 2) {
			for (int z = z0; z <= z1; z += 2) {
				String w = clashes(pieces, x, z, built, planned) ? "village" : why(world, x, z, planned ? 4 : 3, heights, planned);
				why.merge(w == null ? "fine" : w, 1, Integer::sum);
			}
		}
		return why;
	}

	/** The ground a village leaves alone: soil, sand, snow and the village's own dirt paths. */
	private static boolean natural(BlockState s) {
		Block b = s.getBlock();
		// grass isn't in 26.3's dirt tag, so the soils are named outright
		return s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || b == Blocks.GRASS_BLOCK || b == Blocks.PODZOL || b == Blocks.MYCELIUM
			|| b == Blocks.COARSE_DIRT || b == Blocks.ROOTED_DIRT || b == Blocks.MOSS_BLOCK || b == Blocks.DIRT || b == Blocks.DIRT_PATH
			|| b == Blocks.SNOW_BLOCK || b == Blocks.GRAVEL || b == Blocks.SANDSTONE || b == Blocks.TERRACOTTA
			|| b == Blocks.MUD || b == Blocks.PACKED_ICE;
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
	 * The dais, with the ground block at {@code ground}: a full arcane stone block raised in the middle, ringed by arcane stone brick slabs
	 * (all on arcane stone set flush in the ground), a flush arcane stone block at each cardinal point, and the totem on the middle block; a
	 * footing beneath and nothing growing through.
	 */
	public static void build(WorldGenLevel world, RandomSource random, BlockPos ground) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				boolean core = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
				// beyond the raised middle, one flush block at each cardinal point
				boolean point = dx == 0 && Math.abs(dz) == 2 || dz == 0 && Math.abs(dx) == 2;
				if (!core && !point) {
					continue;
				}
				BlockPos p = ground.offset(dx, 0, dz);
				world.setBlock(p, TVBlocks.ARCANE_STONE_BLOCK.defaultBlockState(), 2);
				for (int down = 1; down <= 3 && !world.getBlockState(p.below(down)).isSolid(); down++) {
					world.setBlock(p.below(down), Blocks.COBBLESTONE.defaultBlockState(), 2);
				}
				for (int up = 1; up <= 4; up++) {
					world.setBlock(p.above(up), Blocks.AIR.defaultBlockState(), 2);
				}
				if (dx == 0 && dz == 0) {
					// the totem stands on a full block of arcane stone, ringed by brick slabs
					world.setBlock(p.above(), TVBlocks.ARCANE_STONE_BLOCK.defaultBlockState(), 2);
				} else if (core) {
					world.setBlock(p.above(), TVBlocks.ARCANE_STONE_SLAB.defaultBlockState(), 2);
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
