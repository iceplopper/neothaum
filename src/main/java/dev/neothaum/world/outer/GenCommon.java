package dev.neothaum.world.outer;

import dev.neothaum.blocks.CrystalClusterBlock;
import dev.neothaum.blocks.eldritch.CrustedOpeningBlock;
import dev.neothaum.registry.TVBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * GenCommon: what every Outer Lands cell is built with. placeBlock takes TC4's block codes (bedrock shell, ancient stone, void, stairs,
 * doors, traps, crust); while building it notes stone that might become a decoration, a crab vent or an urn on a pedestal, and
 * processDecorations settles those once the cell is done. generateConnections opens the corridors to the neighbouring cells.
 */
public class GenCommon {
	static final int BEDROCK = 1;
	static final int STONE = 2;
	static final int VOID = 8;
	static final int AIR_REPL = 9;
	static final int STAIR_DIRECTIONAL = 10;
	static final int STAIR_DIRECTIONAL_INV = 11;
	static final int DOOR_BLOCK = 15;
	static final int DOOR_LOCK = 16;
	static final int VOID_DOOR = 17;
	static final int ROCK = 18;
	static final int STONE_NOSPAWN = 19;
	static final int STONE_TRAPPED = 20;
	static final int CRUST = 21;
	static final int BEDROCK_REPL = 99;
	/** A corridor's cross-section: bedrock shell, void, ancient stone walls with stair corners, air inside. */
	static final int[][] PAT_CONNECT = {
		{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
		{1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
		{1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
		{1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1},
		{1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
		{1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
		{1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
		{1, 8, 2, 3, 9, 9, 9, 4, 2, 8, 1},
		{1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
		{1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
		{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}};

	final WorldGenLevel world;
	final RandomSource rand;
	private final List<BlockPos> decoCommon = new ArrayList<>();
	private final List<BlockPos> crabSpawner = new ArrayList<>();
	private final List<BlockPos> decoUrn = new ArrayList<>();

	GenCommon(WorldGenLevel world, RandomSource rand) {
		this.world = world;
		this.rand = rand;
	}

	BlockState get(int x, int y, int z) {
		return this.world.getBlockState(new BlockPos(x, y, z));
	}

	boolean isAir(int x, int y, int z) {
		return this.get(x, y, z).isAir();
	}

	void set(int x, int y, int z, BlockState state) {
		this.world.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
	}

	/** A TC4 block and metadata set straight into the world, as its NeoThaum state. */
	void setTc(int x, int y, int z, String block, int meta) {
		BlockState state = switch (block) {
			case "blockEldritch" -> switch (meta) {
				case 1 -> OuterBlocks.obeliskBase();
				case 2 -> OuterBlocks.obeliskShaft();
				case 3 -> TVBlocks.ELDRITCH_CAP.defaultBlockState();
				case 4 -> TVBlocks.GLOWING_CRUSTED_STONE.defaultBlockState();
				case 5 -> TVBlocks.GLYPHED_STONE.defaultBlockState();
				case 7 -> TVBlocks.ANCIENT_DOORWAY.defaultBlockState();
				case 10 -> TVBlocks.RUNED_STONE.defaultBlockState();
				default -> TVBlocks.ELDRITCH_ALTAR.defaultBlockState();
			};
			case "blockCosmeticSolid" -> switch (meta) {
				case 12 -> TVBlocks.ANCIENT_ROCK.defaultBlockState();
				case 13 -> TVBlocks.ANCIENT_STONE_SEALED.defaultBlockState();
				case 14 -> TVBlocks.CRUSTED_STONE.defaultBlockState();
				case 15 -> TVBlocks.ANCIENT_PEDESTAL.defaultBlockState();
				default -> TVBlocks.ANCIENT_STONE.defaultBlockState();
			};
			case "blockSlabStone" -> OuterBlocks.slab(meta >= 8);
			case "blockTaintFibres" -> TVBlocks.TAINT_FIBRES.defaultBlockState().setValue(dev.neothaum.blocks.TaintFibreBlock.TYPE, meta);
			case "web" -> Blocks.COBWEB.defaultBlockState();
			case "portal" -> TVBlocks.ELDRITCH_PORTAL.defaultBlockState();
			default -> throw new IllegalArgumentException(block);
		};
		this.set(x, y, z, state);
	}

	/** Utils.setBiomeAt(world, x, z, biomeTaint), on the chunk being built. */
	void setTaintBiome(int x, int z) {
		var holder = this.world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME)
			.getOrThrow(dev.neothaum.world.TVBiomes.TAINTED_LAND);
		dev.neothaum.world.TVBiomes.setBiomeAt(this.world.getChunk(x >> 4, z >> 4), x, z, holder);
	}

	/** Any of TC4's BlockCosmeticSolid stones the maze is built of. */
	boolean isAncientStone(int x, int y, int z) {
		BlockState s = this.get(x, y, z);
		return s.is(TVBlocks.ANCIENT_STONE) || s.is(TVBlocks.ANCIENT_ROCK) || s.is(TVBlocks.ANCIENT_STONE_SEALED) || s.is(TVBlocks.CRUSTED_STONE)
			|| s.is(TVBlocks.ANCIENT_PEDESTAL);
	}

	boolean isAdjacentToSolid(int x, int y, int z) {
		BlockPos p = new BlockPos(x, y, z);
		for (Direction d : Direction.values()) {
			BlockPos n = p.relative(d);
			if (this.world.getBlockState(n).isFaceSturdy(this.world, n, d.getOpposite())) {
				return true;
			}
		}
		return false;
	}

	void spawner(int x, int y, int z, net.minecraft.world.entity.EntityType<?> type) {
		BlockPos p = new BlockPos(x, y, z);
		this.world.setBlock(p, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
		if (this.world.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity sp) {
			sp.setEntityId(type, this.rand);
		}
	}

	private boolean isNothing(int x, int y, int z) {
		return this.get(x, y, z).is(TVBlocks.ELDRITCH_NOTHING);
	}

	/** Any block of TC4's BlockEldritch: altar, obelisk, capstone, and the glowing, glyphed, door, lock, vent and trap stones. */
	public static boolean isEldritch(BlockState s) {
		return s.is(TVBlocks.ELDRITCH_ALTAR) || s.is(TVBlocks.ELDRITCH_OBELISK) || s.is(TVBlocks.ELDRITCH_CAP) || s.is(TVBlocks.GLOWING_CRUSTED_STONE)
			|| s.is(TVBlocks.GLYPHED_STONE) || s.is(TVBlocks.ANCIENT_DOORWAY) || s.is(TVBlocks.ANCIENT_LOCK) || s.is(TVBlocks.CRUSTED_OPENING)
			|| s.is(TVBlocks.RUNED_STONE);
	}

	void placeBlock(int x, int y, int z, int b, Cell cell) {
		this.placeBlock(x, y, z, b, null, cell);
	}

	/** ForgeDirection ordinals 2-5 (north, south, west, east) pick the stairs' metadata. */
	private static int axis(@Nullable Direction dir) {
		if (dir == Direction.NORTH || dir == Direction.SOUTH) {
			return 0;
		}
		return dir == Direction.WEST || dir == Direction.EAST ? 1 : -1;
	}

	void placeBlock(int x, int y, int z, int b, @Nullable Direction dir, Cell cell) {
		BlockPos pos = new BlockPos(x, y, z);
		BlockState block = null;
		boolean crust = false;
		switch (b) {
			case BEDROCK -> {
				if (this.isAir(x, y, z)) {
					block = Blocks.BEDROCK.defaultBlockState();
				}
			}
			case STONE -> {
				if (cell.feature == 7 && this.rand.nextInt(3) == 0) {
					crust = true;
				} else if (!this.isNothing(x, y, z)) {
					if (this.rand.nextInt(25) == 0) {
						boolean crab = cell.feature == 7 || this.rand.nextInt(50) == 0;
						if (crab && (cell.feature == 0 || cell.feature == 7)) {
							this.crabSpawner.add(pos);
						} else {
							this.decoCommon.add(pos);
						}
					}
					block = TVBlocks.ANCIENT_STONE.defaultBlockState();
				}
			}
			case 3, 4 -> {
				if (this.rand.nextFloat() < 0.005F) {
					this.decoUrn.add(pos);
				}
				int ax = axis(dir);
				block = OuterBlocks.stairs(b == 3 ? (ax == 0 ? 1 : ax == 1 ? 3 : 0) : (ax == 0 ? 0 : ax == 1 ? 2 : 0));
			}
			case 5 -> {
				int ax = axis(dir);
				block = OuterBlocks.stairs(ax == 0 ? 5 : ax == 1 ? 7 : 0);
			}
			case 6 -> {
				int ax = axis(dir);
				block = OuterBlocks.stairs(ax == 0 ? 4 : ax == 1 ? 6 : 0);
			}
			case 7 -> block = TVBlocks.GLOWING_CRUSTED_STONE.defaultBlockState();
			case VOID -> block = TVBlocks.ELDRITCH_NOTHING.defaultBlockState();
			case AIR_REPL -> {
				block = Blocks.AIR.defaultBlockState();
				this.decoCommon.remove(pos);
				this.crabSpawner.remove(pos);
				this.decoUrn.remove(pos);
			}
			case STAIR_DIRECTIONAL -> block = OuterBlocks.stairs(dir == Direction.NORTH ? 3 : dir == Direction.SOUTH ? 2 : dir == Direction.EAST ? 0 : 1);
			case STAIR_DIRECTIONAL_INV -> block = OuterBlocks.stairs(dir == Direction.NORTH ? 7 : dir == Direction.SOUTH ? 6 : dir == Direction.EAST ? 4 : 5);
			case DOOR_BLOCK, DOOR_LOCK -> {
				block = b == DOOR_BLOCK ? TVBlocks.ANCIENT_DOORWAY.defaultBlockState() : TVBlocks.ANCIENT_LOCK.defaultBlockState();
				this.decoCommon.remove(pos);
				this.crabSpawner.remove(pos);
				this.decoUrn.remove(pos);
			}
			case VOID_DOOR -> block = TVBlocks.IMPASSABLE.defaultBlockState();
			case ROCK -> {
				if (!this.isNothing(x, y, z)) {
					block = TVBlocks.ANCIENT_ROCK.defaultBlockState();
				}
			}
			case STONE_NOSPAWN -> {
				if (!this.isNothing(x, y, z)) {
					block = TVBlocks.ANCIENT_STONE_SEALED.defaultBlockState();
				}
			}
			case STONE_TRAPPED -> {
				if (!this.isNothing(x, y, z)) {
					block = TVBlocks.RUNED_STONE.defaultBlockState();
				}
			}
			case CRUST -> crust = true;
			case BEDROCK_REPL -> block = Blocks.BEDROCK.defaultBlockState();
			default -> {
			}
		}
		if (crust && !this.isNothing(x, y, z)) {
			block = TVBlocks.CRUSTED_STONE.defaultBlockState();
			if (this.rand.nextInt(25) == 0) {
				block = TVBlocks.GLOWING_CRUSTED_STONE.defaultBlockState();
			} else if (this.rand.nextInt(25) == 0) {
				boolean crab = cell.feature == 7 || (cell.feature == 12 && this.rand.nextBoolean() || this.rand.nextInt(25) == 0);
				if (crab && (cell.feature == 0 || cell.feature == 7 || cell.feature == 12)) {
					this.crabSpawner.add(pos);
				}
			}
		}
		if (block != null) {
			this.world.setBlock(pos, block, Block.UPDATE_CLIENTS);
		}
	}

	void genObelisk(int x, int y, int z) {
		this.set(x, y, z, OuterBlocks.obeliskBase());
		for (int i = 1; i <= 4; i++) {
			this.set(x, y + i, z, OuterBlocks.obeliskShaft());
		}
	}

	private int countExposedSides(BlockPos p) {
		int count = 0;
		for (Direction d : Direction.values()) {
			if (this.world.getBlockState(p.relative(d)).isAir()) {
				count++;
			}
		}
		return count;
	}

	private boolean nearEldritch(BlockPos p) {
		for (BlockPos q : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
			if (!q.equals(p) && isEldritch(this.world.getBlockState(q))) {
				return true;
			}
		}
		return false;
	}

	private boolean isBedrockShowing(BlockPos p) {
		for (Direction d : Direction.values()) {
			BlockState opposite = this.world.getBlockState(p.relative(d.getOpposite()));
			if (!this.world.getBlockState(p.relative(d)).isSolidRender() && (opposite.is(Blocks.BEDROCK) || opposite.is(TVBlocks.ELDRITCH_NOTHING))) {
				return true;
			}
		}
		return false;
	}

	/** Urns on pedestals; glowing and glyphed stone (now and then a runed trap, or strange crystals); crab vents facing the open air. */
	void processDecorations() {
		for (BlockPos p : this.decoUrn) {
			if (this.world.getBlockState(p.above()).isAir()) {
				this.world.setBlock(p, TVBlocks.ANCIENT_PEDESTAL.defaultBlockState(), Block.UPDATE_CLIENTS);
				float rr = this.rand.nextFloat();
				this.world.setBlock(p.above(), OuterBlocks.urn(rr < 0.025F ? 2 : rr < 0.1F ? 1 : 0), Block.UPDATE_CLIENTS);
			}
		}
		for (BlockPos p : this.decoCommon) {
			int exp = this.countExposedSides(p);
			if (exp > 0 && (exp == 1 || !this.isBedrockShowing(p)) && !this.nearEldritch(p)) {
				int meta = this.rand.nextInt(3) != 0 ? 4 : (this.rand.nextInt(8) != 0 ? 5 : 10);
				Block b = meta == 4 ? TVBlocks.GLOWING_CRUSTED_STONE : meta == 5 ? TVBlocks.GLYPHED_STONE : TVBlocks.RUNED_STONE;
				this.world.setBlock(p, b.defaultBlockState(), Block.UPDATE_CLIENTS);
				if (meta == 4 && this.rand.nextInt(12) == 0) {
					for (Direction d : Direction.values()) {
						if (this.world.getBlockState(p.relative(d)).isAir()) {
							this.world.setBlock(p.relative(d), TVBlocks.STRANGE_CRYSTALS.defaultBlockState().setValue(CrystalClusterBlock.FACING, d),
								Block.UPDATE_CLIENTS);
							break;
						}
					}
				}
			}
		}
		for (BlockPos p : this.crabSpawner) {
			if (this.countExposedSides(p) == 1 && !this.nearEldritch(p)) {
				Direction facing = Direction.UP;
				for (Direction d : Direction.values()) {
					if (this.world.getBlockState(p.relative(d)).isAir()) {
						facing = d;
						break;
					}
				}
				this.world.setBlock(p, TVBlocks.CRUSTED_OPENING.defaultBlockState().setValue(CrustedOpeningBlock.FACING, facing), Block.UPDATE_CLIENTS);
			}
		}
		this.decoCommon.clear();
		this.crabSpawner.clear();
		this.decoUrn.clear();
	}

	/** A corridor out of each open side, depth blocks long; justthetip trims the end to the doorway's outline. */
	void generateConnections(int cx, int cz, int y, Cell cell, int depth, boolean justthetip) {
		int x = cx * 16;
		int z = cz * 16;
		for (int side = 0; side < 4; side++) {
			boolean open = switch (side) {
				case 0 -> cell.north;
				case 1 -> cell.south;
				case 2 -> cell.east;
				default -> cell.west;
			};
			if (!open) {
				continue;
			}
			Direction dir = side == 0 ? Direction.NORTH : side == 1 ? Direction.SOUTH : side == 2 ? Direction.EAST : Direction.WEST;
			for (int d = 0; d <= depth; d++) {
				int lo = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0);
				int hi = d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11);
				for (int w = lo; w < hi; w++) {
					for (int h = lo; h < hi; h++) {
						if (d == depth && justthetip && PAT_CONNECT[h][w] == 8) {
							continue;
						}
						int px = switch (side) {
							case 0, 1 -> x + 3 + w;
							case 2 -> x + 16 - d;
							default -> x + d;
						};
						int pz = switch (side) {
							case 0 -> z + d;
							case 1 -> z + 16 - d;
							default -> z + 3 + w;
						};
						this.placeBlock(px, y + 10 - h, pz, PAT_CONNECT[h][w], dir, cell);
					}
				}
			}
		}
	}
}
