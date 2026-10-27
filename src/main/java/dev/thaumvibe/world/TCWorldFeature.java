package dev.thaumvibe.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;

/**
 * Port of ThaumcraftWorldGenerator.generateSurface / generateNether for one chunk: magical trees, cinderpearl,
 * cinnabar, amber, infused stone and wild aura nodes. Placed once per chunk with the chunk's min corner as origin.
 */
public record TCWorldFeature(boolean nether) implements Feature {
	public static final MapCodec<TCWorldFeature> CODEC = Codec.BOOL.optionalFieldOf("nether", false).xmap(TCWorldFeature::new, TCWorldFeature::nether);

	@Override
	public MapCodec<TCWorldFeature> codec() {
		return CODEC;
	}

	private static final Block[] INFUSED = new Block[7];
	private static final Block[] DEEP_INFUSED = new Block[7];

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator gen, RandomSource random, BlockPos origin) {
		int cx = origin.getX() & ~15;
		int cz = origin.getZ() & ~15;
		if (INFUSED[1] == null) {
			INFUSED[1] = TVBlocks.AIR_INFUSED_STONE;
			INFUSED[2] = TVBlocks.FIRE_INFUSED_STONE;
			INFUSED[3] = TVBlocks.WATER_INFUSED_STONE;
			INFUSED[4] = TVBlocks.EARTH_INFUSED_STONE;
			INFUSED[5] = TVBlocks.ORDER_INFUSED_STONE;
			INFUSED[6] = TVBlocks.ENTROPY_INFUSED_STONE;
			DEEP_INFUSED[1] = TVBlocks.DEEPSLATE_AIR_INFUSED_STONE;
			DEEP_INFUSED[2] = TVBlocks.DEEPSLATE_FIRE_INFUSED_STONE;
			DEEP_INFUSED[3] = TVBlocks.DEEPSLATE_WATER_INFUSED_STONE;
			DEEP_INFUSED[4] = TVBlocks.DEEPSLATE_EARTH_INFUSED_STONE;
			DEEP_INFUSED[5] = TVBlocks.DEEPSLATE_ORDER_INFUSED_STONE;
			DEEP_INFUSED[6] = TVBlocks.DEEPSLATE_ENTROPY_INFUSED_STONE;
		}
		if (this.nether) {
			generateWildNodes(world, random, cx, cz);
			return true;
		}
		generateVegetation(world, random, cx, cz);
		generateStructures(world, random, cx, cz);
		generateOres(world, random, cx, cz);
		generateWildNodes(world, random, cx, cz);
		return true;
	}

	private void generateStructures(WorldGenLevel world, RandomSource random, int cx, int cz) {
		if (random.nextInt(66) == 0) {
			int x = cx + random.nextInt(16);
			int z = cz + random.nextInt(16);
			int y = heightAt(world, x, z);
			EldritchRingFeature.generate(world, random, new BlockPos(x, y - 1, z));
		}
		if (random.nextInt(150) == 0) {
			int x = cx + random.nextInt(16);
			int z = cz + random.nextInt(16);
			int y = heightAt(world, x, z);
			BarrowMoundFeature.generate(world, random, new BlockPos(x - 9, y - 8, z - 9));
		}
		int sx = cx + random.nextInt(16);
		int sz = cz + random.nextInt(16);
		int sy = heightAt(world, sx, sz);
		if (sy >= 85 && random.nextInt(40) == 0) {
			HilltopStonesFeature.generate(world, random, new BlockPos(sx, sy, sz));
		}
		if (random.nextInt(120) == 0) {
			int x = cx + random.nextInt(16);
			int z = cz + random.nextInt(16);
			int y = heightAt(world, x, z);
			WizardTowerFeature.generate(world, random, new BlockPos(x, y - 1, z));
		} else if (random.nextInt(140) == 0) {
			int x = cx + random.nextInt(16);
			int z = cz + random.nextInt(16);
			int y = heightAt(world, x, z);
			BankerHomeFeature.generate(world, random, new BlockPos(x, y - 1, z));
		}
	}

	private static int heightAt(WorldGenLevel world, int x, int z) {
		return world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
	}

	private void generateVegetation(WorldGenLevel world, RandomSource random, int cx, int cz) {
		if (random.nextInt(60) == 3) {
			int x = cx + random.nextInt(16);
			int z = cz + random.nextInt(16);
			int y = heightAt(world, x, z);
			Holder<Biome> bio = world.getBiome(new BlockPos(x, y, z));
			if (NodeGenerator.isMagical(bio) || bio.is(ConventionalBiomeTags.IS_FOREST) && bio.is(ConventionalBiomeTags.IS_HILL)
				|| bio.is(ConventionalBiomeTags.IS_BIRCH_FOREST) || bio.is(ConventionalBiomeTags.IS_FLOWER_FOREST)) {
				SilverwoodTreeFeature.generate(world, random, new BlockPos(x, y, z), false, 7, 4);
			}
		}
		if (random.nextInt(25) == 7) {
			int x = cx + random.nextInt(16);
			int z = cz + random.nextInt(16);
			int y = heightAt(world, x, z);
			Holder<Biome> bio = world.getBiome(new BlockPos(x, y, z));
			if (NodeGenerator.getBiomeSupportsGreatwood(bio) > random.nextFloat()) {
				GreatwoodTreeFeature.generate(world, random, new BlockPos(x, y, z), false, random.nextInt(8) == 0);
			}
		}
		int x = cx + random.nextInt(16);
		int z = cz + random.nextInt(16);
		int y = heightAt(world, x, z);
		BlockPos p = new BlockPos(x, y, z);
		Holder<Biome> bio = world.getBiome(p);
		if (world.getBlockState(p.below()).is(BlockTags.SAND) && bio.value().getBaseTemperature() > 1.0F && random.nextInt(30) == 0) {
			NodeGenerator.generateFlowers(world, random, p, TVBlocks.CINDERPEARL.defaultBlockState());
		}
	}

	/** The ore to put in place of {@code existing}: the deepslate variant in deepslate, the stone one in stone, else null. */
	private static BlockState oreFor(BlockState existing, Block stoneOre, Block deepslateOre) {
		if (existing.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
			return deepslateOre.defaultBlockState();
		}
		return existing.is(BlockTags.STONE_ORE_REPLACEABLES) ? stoneOre.defaultBlockState() : null;
	}

	private static void placeOre(WorldGenLevel world, BlockPos p, Block stoneOre, Block deepslateOre) {
		BlockState ore = oreFor(world.getBlockState(p), stoneOre, deepslateOre);
		if (ore != null) {
			world.setBlock(p, ore, 2);
		}
	}

	private void generateOres(WorldGenLevel world, RandomSource random, int cx, int cz) {
		int bottom = world.getMinY();
		// cinnabar: 18 single blocks in the bottom fifth of the world height (TC4: of 1.7's 256; here of -64..320, so mostly deepslate)
		for (int i = 0; i < 18; i++) {
			int x = cx + random.nextInt(16);
			int y = bottom + random.nextInt((world.getMaxY() - bottom) / 5);
			int z = cz + random.nextInt(16);
			placeOre(world, new BlockPos(x, y, z), TVBlocks.CINNABAR_ORE, TVBlocks.DEEPSLATE_CINNABAR_ORE);
		}
		// amber: 20 single blocks within 25 blocks of the surface
		for (int i = 0; i < 20; i++) {
			int x = cx + random.nextInt(16);
			int z = cz + random.nextInt(16);
			int y = heightAt(world, x, z) - random.nextInt(25);
			placeOre(world, new BlockPos(x, y, z), TVBlocks.AMBER_BEARING_STONE, TVBlocks.DEEPSLATE_AMBER_BEARING_STONE);
		}
		// infused stone: 8 veins of 6, biased towards the biome's primal
		for (int i = 0; i < 8; i++) {
			int x = cx + random.nextInt(16);
			int z = cz + random.nextInt(16);
			// TC4: anywhere from bedrock to 5 below the surface; the world now starts at y -64
			int y = bottom + random.nextInt(Math.max(5, heightAt(world, x, z) - 5 - bottom));
			int md = random.nextInt(6) + 1;
			if (random.nextInt(3) == 0) {
				Aspect tag = NodeGenerator.getRandomBiomeTag(world.getBiome(new BlockPos(x, y, z)), random);
				if (tag == null) {
					md = 1 + random.nextInt(6);
				} else if (tag == Aspect.AIR) {
					md = 1;
				} else if (tag == Aspect.FIRE) {
					md = 2;
				} else if (tag == Aspect.WATER) {
					md = 3;
				} else if (tag == Aspect.EARTH) {
					md = 4;
				} else if (tag == Aspect.ORDER) {
					md = 5;
				} else if (tag == Aspect.ENTROPY) {
					md = 6;
				}
			}
			minable(world, random, x, y, z, INFUSED[md], DEEP_INFUSED[md], 6);
		}
	}

	/** WorldGenMinable (1.7.10). */
	private static void minable(WorldGenLevel world, RandomSource rand, int x, int y, int z, Block stoneOre, Block deepslateOre, int numberOfBlocks) {
		float f = rand.nextFloat() * (float) Math.PI;
		double d0 = x + 8 + Mth.sin(f) * numberOfBlocks / 8.0F;
		double d1 = x + 8 - Mth.sin(f) * numberOfBlocks / 8.0F;
		double d2 = z + 8 + Mth.cos(f) * numberOfBlocks / 8.0F;
		double d3 = z + 8 - Mth.cos(f) * numberOfBlocks / 8.0F;
		double d4 = y + rand.nextInt(3) - 2;
		double d5 = y + rand.nextInt(3) - 2;
		for (int l = 0; l <= numberOfBlocks; l++) {
			double d6 = d0 + (d1 - d0) * l / numberOfBlocks;
			double d7 = d4 + (d5 - d4) * l / numberOfBlocks;
			double d8 = d2 + (d3 - d2) * l / numberOfBlocks;
			double d9 = rand.nextDouble() * numberOfBlocks / 16.0;
			double d10 = (Mth.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
			double d11 = (Mth.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
			int i1 = Mth.floor(d6 - d10 / 2.0);
			int j1 = Mth.floor(d7 - d11 / 2.0);
			int k1 = Mth.floor(d8 - d10 / 2.0);
			int l1 = Mth.floor(d6 + d10 / 2.0);
			int i2 = Mth.floor(d7 + d11 / 2.0);
			int j2 = Mth.floor(d8 + d10 / 2.0);
			for (int k2 = i1; k2 <= l1; k2++) {
				double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
				if (d12 * d12 < 1.0) {
					for (int l2 = j1; l2 <= i2; l2++) {
						double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
						if (d12 * d12 + d13 * d13 < 1.0) {
							for (int i3 = k1; i3 <= j2; i3++) {
								double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
								BlockPos p = new BlockPos(k2, l2, i3);
								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0) {
									placeOre(world, p, stoneOre, deepslateOre);
								}
							}
						}
					}
				}
			}
		}
	}

	/** generateWildNodes (Config.nodeRarity = 36). */
	private void generateWildNodes(WorldGenLevel world, RandomSource random, int cx, int cz) {
		if (random.nextInt(36) != 0) {
			return;
		}
		int x = cx + random.nextInt(16);
		int z = cz + random.nextInt(16);
		int q = heightAt(world, x, z);
		if (q <= world.getMinY() + 1) {
			q = 32 + random.nextInt(64);
		}
		if (world.isEmptyBlock(new BlockPos(x, q + 1, z))) {
			q++;
		}
		int p = random.nextInt(4);
		BlockState b = world.getBlockState(new BlockPos(x, q + p, z));
		if (b.isAir() || b.canBeReplaced()) {
			q += p;
		}
		if (q >= world.getMaxY()) {
			return;
		}
		NodeGenerator.createRandomNodeAt(world, new BlockPos(x, q, z), random, false, false, false, false);
	}
}
