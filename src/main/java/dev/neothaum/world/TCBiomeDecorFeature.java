package dev.neothaum.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.neothaum.blocks.TaintFibreBlock;
import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluids;

/**
 * BiomeGenMagicalForest.decorate, BiomeGenTaint.decorate and BiomeGenEerie's decorator settings, run once per chunk from its min corner: trees,
 * grass, flowers, lily pads, mushrooms, mossy boulders and vishrooms for the magical forest; trees, grass, crusted taint and fibres for the
 * tainted land; a few trees, flowers and grass for eerie places.
 */
public record TCBiomeDecorFeature(String biome) implements Feature {
	public static final MapCodec<TCBiomeDecorFeature> CODEC = Codec.STRING.fieldOf("biome").xmap(TCBiomeDecorFeature::new, TCBiomeDecorFeature::biome);

	private static final Block[] RED_FLOWERS = {Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
		Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY};

	@Override
	public MapCodec<TCBiomeDecorFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator gen, RandomSource random, BlockPos origin) {
		int x = origin.getX() & ~15;
		int z = origin.getZ() & ~15;
		switch (this.biome) {
			case "magical_forest" -> magicalForest(world, gen, random, x, z);
			case "tainted_land" -> taint(world, gen, random, x, z);
			default -> eerie(world, gen, random, x, z);
		}
		return true;
	}

	private static int height(WorldGenLevel world, int x, int z) {
		return world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
	}

	/** Ensures trees, rocks, and structures are never planted on water, in water, or on non-soil floating blocks. */
	private static boolean canPlaceTreeAt(WorldGenLevel world, BlockPos pos) {
		if (pos.getY() <= world.getMinY() + 4 || pos.getY() >= world.getMaxY() - 10) {
			return false;
		}
		// Tree base position must not be inside water or any fluid
		if (!world.getFluidState(pos).isEmpty()) {
			return false;
		}
		BlockPos below = pos.below();
		// Block below must not be water, waterlogged, or any fluid
		if (!world.getFluidState(below).isEmpty()) {
			return false;
		}
		// Ground below must be valid solid soil (grass block, dirt, podzol, rooted dirt...)
		BlockState soil = world.getBlockState(below);
		if (!soil.is(BlockTags.DIRT) && !soil.is(BlockTags.SUPPORTS_VEGETATION)) {
			return false;
		}
		// The position itself must be air or replaceable by trees
		BlockState at = world.getBlockState(pos);
		return at.isAir() || at.is(BlockTags.REPLACEABLE_BY_TREES);
	}

	private static void placeVanilla(WorldGenLevel world, ChunkGenerator gen, RandomSource random, String name, BlockPos pos) {
		world.registryAccess().lookupOrThrow(Registries.FEATURE).get(ResourceKey.create(Registries.FEATURE, Identifier.withDefaultNamespace(name)))
			.ifPresent(f -> f.value().place(world, gen, random, pos));
	}

	// ------------------------------------------------------------------ BiomeGenMagicalForest

	private static void magicalForest(WorldGenLevel world, ChunkGenerator gen, RandomSource random, int x, int z) {
		int k = random.nextInt(3);
		for (int l = 0; l < k; l++) {
			int i1 = x + random.nextInt(16) + 8;
			int j1 = z + random.nextInt(16) + 8;
			BlockPos p = new BlockPos(i1, height(world, i1, j1), j1);
			if (canPlaceTreeAt(world, p)) {
				placeVanilla(world, gen, random, "forest_rock", p);
			}
		}
		for (int a = 0; a < 4; a++) {
			for (int b = 0; b < 4; b++) {
				int i1 = x + a * 4 + 1 + 8 + random.nextInt(3);
				int j1 = z + b * 4 + 1 + 8 + random.nextInt(3);
				if (random.nextInt(40) == 0) {
					BlockPos p = new BlockPos(i1, height(world, i1, j1), j1);
					if (canPlaceTreeAt(world, p)) {
						placeVanilla(world, gen, random, random.nextBoolean() ? "huge_brown_mushroom" : "huge_red_mushroom", p);
					}
				}
			}
		}
		// BiomeDecorator: two trees a chunk, and a tenth of the time one more
		int trees = 2 + (random.nextInt(10) == 0 ? 1 : 0);
		for (int t = 0; t < trees; t++) {
			for (int tries = 0; tries < 3; tries++) {
				int tx = x + random.nextInt(16) + 8;
				int tz = z + random.nextInt(16) + 8;
				BlockPos p = new BlockPos(tx, height(world, tx, tz), tz);
				if (canPlaceTreeAt(world, p)) {
					if (random.nextInt(14) == 0) {
						SilverwoodTreeFeature.generate(world, random, p, false, 8, 5);
					} else if (random.nextInt(10) == 0) {
						GreatwoodTreeFeature.generate(world, random, p, false, false);
					} else {
						placeVanilla(world, gen, random, "fancy_oak", p);
					}
					break;
				}
			}
		}
		for (int f = 0; f < 10; f++) {
			scatter(world, random, x + random.nextInt(16) + 8, z + random.nextInt(16) + 8, 64, RED_FLOWERS[random.nextInt(RED_FLOWERS.length)]
				.defaultBlockState());
		}
		for (int g = 0; g < 12; g++) {
			scatter(world, random, x + random.nextInt(16) + 8, z + random.nextInt(16) + 8, 128,
				(random.nextInt(4) == 0 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState());
		}
		for (int w = 0; w < 6; w++) {
			lilyPads(world, random, x + random.nextInt(16) + 8, z + random.nextInt(16) + 8);
		}
		for (int m = 0; m < 6; m++) {
			if (random.nextInt(4) == 0) {
				scatter(world, random, x + random.nextInt(16) + 8, z + random.nextInt(16) + 8, 64, Blocks.BROWN_MUSHROOM.defaultBlockState());
			}
			if (random.nextInt(8) == 0) {
				scatter(world, random, x + random.nextInt(16) + 8, z + random.nextInt(16) + 8, 64, Blocks.RED_MUSHROOM.defaultBlockState());
			}
		}
		for (int a = 0; a < 8; a++) {
			int xx = x + random.nextInt(16);
			int zz = z + random.nextInt(16);
			int yy = height(world, xx, zz);
			while (yy > 50 && !world.getBlockState(new BlockPos(xx, yy, zz)).is(Blocks.GRASS_BLOCK)) {
				yy--;
			}
			BlockPos ground = new BlockPos(xx, yy, zz);
			if (world.getBlockState(ground).is(Blocks.GRASS_BLOCK) && world.getBlockState(ground.above()).canBeReplaced()
				&& adjacentToWood(world, ground.above())) {
				world.setBlock(ground.above(), TVBlocks.VISHROOM.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}

		// TC4 WorldGenManaPods: 10 attempts per chunk hanging from logs
		for (int p = 0; p < 10; p++) {
			int px = x + random.nextInt(16) + 8;
			int pz = z + random.nextInt(16) + 8;
			generateManaPods(world, random, px, 64, pz);
		}
	}

	private static void generateManaPods(WorldGenLevel world, RandomSource random, int startX, int startY, int startZ) {
		int x = startX;
		int z = startZ;
		int maxY = Math.min(128, height(world, x, z));
		int y = startY;

		while (y < maxY) {
			BlockPos p = new BlockPos(x, y, z);
			if (world.isEmptyBlock(p) && world.isEmptyBlock(p.below())) {
				BlockPos above = p.above();
				if (world.getBlockState(above).is(BlockTags.LOGS)) {
					int age = 2 + random.nextInt(5);
					world.setBlock(p, TVBlocks.MANA_POD.defaultBlockState().setValue(dev.neothaum.blocks.ManaPodBlock.AGE, age), Block.UPDATE_CLIENTS);
					if (world.getBlockEntity(p) instanceof dev.neothaum.tiles.ManaPodBlockEntity be) {
						be.checkGrowth(age);
					}
					break;
				}
			} else {
				x = startX + random.nextInt(4) - random.nextInt(4);
				z = startZ + random.nextInt(4) - random.nextInt(4);
			}
			y++;
		}
	}

	private static boolean adjacentToWood(WorldGenLevel world, BlockPos pos) {
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (!p.equals(pos) && world.getBlockState(p).is(BlockTags.LOGS)) {
				return true;
			}
		}
		return false;
	}

	/** WorldGenTallGrass / WorldGenFlowers: tries scattered round a surface point, kept wherever the plant can stand. */
	private static void scatter(WorldGenLevel world, RandomSource random, int x, int z, int tries, BlockState state) {
		int y = height(world, x, z);
		for (int i = 0; i < tries; i++) {
			BlockPos p = new BlockPos(x + random.nextInt(8) - random.nextInt(8), y + random.nextInt(4) - random.nextInt(4),
				z + random.nextInt(8) - random.nextInt(8));
			if (world.isEmptyBlock(p) && state.canSurvive(world, p)) {
				world.setBlock(p, state, Block.UPDATE_CLIENTS);
			}
		}
	}

	private static void lilyPads(WorldGenLevel world, RandomSource random, int x, int z) {
		int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		for (int i = 0; i < 10; i++) {
			BlockPos p = new BlockPos(x + random.nextInt(8) - random.nextInt(8), y + random.nextInt(4) - random.nextInt(4),
				z + random.nextInt(8) - random.nextInt(8));
			if (world.isEmptyBlock(p) && world.getFluidState(p.below()).is(Fluids.WATER) && Blocks.LILY_PAD.defaultBlockState().canSurvive(world, p)) {
				world.setBlock(p, Blocks.LILY_PAD.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
	}

	// ------------------------------------------------------------------ BiomeGenTaint

	private static void taint(WorldGenLevel world, ChunkGenerator gen, RandomSource random, int x, int z) {
		int trees = 2 + (random.nextInt(10) == 0 ? 1 : 0);
		for (int t = 0; t < trees; t++) {
			for (int tries = 0; tries < 3; tries++) {
				int tx = x + random.nextInt(16) + 8;
				int tz = z + random.nextInt(16) + 8;
				BlockPos p = new BlockPos(tx, height(world, tx, tz), tz);
				if (canPlaceTreeAt(world, p)) {
					placeVanilla(world, gen, random, random.nextInt(8) == 0 ? "fancy_oak" : random.nextInt(10) == 0 ? "fancy_oak" : "oak", p);
					break;
				}
			}
		}
		for (int g = 0; g < 2; g++) {
			scatter(world, random, x + random.nextInt(16) + 8, z + random.nextInt(16) + 8, 128, Blocks.SHORT_GRASS.defaultBlockState());
		}
		int k = random.nextInt(3);
		for (int l = 0; l < k; l++) {
			int i1 = x + random.nextInt(16) + 8;
			int j1 = z + random.nextInt(16) + 8;
			BlockPos p = new BlockPos(i1, height(world, i1, j1), j1);
			if (canPlaceTreeAt(world, p)) {
				blob(world, random, p, TVBlocks.CRUSTED_TAINT.defaultBlockState());
			}
		}
		TaintFibreBlock fibres = (TaintFibreBlock) TVBlocks.TAINT_FIBRES;
		for (int a = 0; a < 10; a++) {
			int xx = x + random.nextInt(16);
			int zz = z + random.nextInt(16);
			BlockPos top = new BlockPos(xx, height(world, xx, zz) - 1, zz);
			if (!world.getFluidState(top).isEmpty() || !world.getFluidState(top.above()).isEmpty()) {
				continue;
			}
			BlockState l1 = world.getBlockState(top);
			if (!l1.isAir()) {
				if (l1.is(Blocks.GRASS_BLOCK)) {
					world.setBlock(top.above(), fibres.ofType(world, top.above(), 0), Block.UPDATE_CLIENTS);
				} else if (l1.canBeReplaced() && world.getBlockState(top.below()).is(Blocks.GRASS_BLOCK)) {
					world.setBlock(top, fibres.ofType(world, top, 0), Block.UPDATE_CLIENTS);
				}
			}
		}
		for (int a = 0; a < 8; a++) {
			int xx = x + random.nextInt(16);
			int zz = z + random.nextInt(16);
			BlockPos p = new BlockPos(xx, height(world, xx, zz), zz);
			if (!world.getFluidState(p).isEmpty() || !world.getFluidState(p.below()).isEmpty()) {
				continue;
			}
			if (world.isEmptyBlock(p) && TaintFibreBlock.isAdjacentToSolidBlock(world, p)) {
				world.setBlock(p, fibres.ofType(world, p, 0), Block.UPDATE_CLIENTS);
			}
		}
	}

	/** WorldGenBlockBlob(block, 0): a lump of the block sitting on the ground. */
	private static void blob(WorldGenLevel world, RandomSource random, BlockPos pos, BlockState state) {
		while (pos.getY() > world.getMinY() + 3) {
			BlockState below = world.getBlockState(pos.below());
			if (!below.isAir() && (below.is(BlockTags.DIRT) || below.is(BlockTags.BASE_STONE_OVERWORLD))) {
				break;
			}
			pos = pos.below();
		}
		if (pos.getY() <= world.getMinY() + 3) {
			return;
		}
		for (int c = 0; c < 3; c++) {
			int xr = random.nextInt(2);
			int yr = random.nextInt(2);
			int zr = random.nextInt(2);
			float r = (xr + yr + zr) * 0.333F + 0.5F;
			for (BlockPos p : BlockPos.betweenClosed(pos.offset(-xr, -yr, -zr), pos.offset(xr, yr, zr))) {
				if (p.distSqr(pos) <= r * r) {
					world.setBlock(p, state, Block.UPDATE_CLIENTS);
				}
			}
			pos = pos.offset(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
		}
	}

	// ------------------------------------------------------------------ BiomeGenEerie

	private static void eerie(WorldGenLevel world, ChunkGenerator gen, RandomSource random, int x, int z) {
		int trees = 2 + (random.nextInt(10) == 0 ? 1 : 0);
		for (int t = 0; t < trees; t++) {
			for (int tries = 0; tries < 3; tries++) {
				int tx = x + random.nextInt(16) + 8;
				int tz = z + random.nextInt(16) + 8;
				BlockPos p = new BlockPos(tx, height(world, tx, tz), tz);
				if (canPlaceTreeAt(world, p)) {
					placeVanilla(world, gen, random, random.nextInt(10) == 0 ? "fancy_oak" : "oak", p);
					break;
				}
			}
		}
		scatter(world, random, x + random.nextInt(16) + 8, z + random.nextInt(16) + 8, 64,
			(random.nextInt(3) == 0 ? Blocks.POPPY : Blocks.DANDELION).defaultBlockState());
		for (int g = 0; g < 2; g++) {
			scatter(world, random, x + random.nextInt(16) + 8, z + random.nextInt(16) + 8, 128, Blocks.SHORT_GRASS.defaultBlockState());
		}
	}
}
