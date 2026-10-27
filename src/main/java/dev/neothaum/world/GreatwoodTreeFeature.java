package dev.neothaum.world;

import dev.neothaum.registry.TVBlocks;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/** Port of WorldGenGreatwoodTrees (a two-tier "big oak" with a 2x2 trunk). */
public final class GreatwoodTreeFeature {
	static final byte[] otherCoordPairs = {2, 0, 0, 1, 2, 1};
	final Random rand = new Random();
	LevelAccessor worldObj;
	final int[] basePos = {0, 0, 0};
	int heightLimit = 0;
	int height;
	double heightAttenuation = 0.618;
	double branchSlope = 0.38;
	double scaleWidth = 1.2;
	double leafDensity = 0.9;
	int trunkSize = 2;
	int heightLimitLimit = 11;
	int leafDistanceLimit = 4;
	int[][] leafNodes;
	final int flags;

	private GreatwoodTreeFeature(boolean notify) {
		this.flags = notify ? 3 : 2;
	}

	public static boolean generate(LevelAccessor level, RandomSource random, BlockPos pos, boolean notify, boolean spiders) {
		return new GreatwoodTreeFeature(notify).generate(level, random, pos.getX(), pos.getY(), pos.getZ(), spiders);
	}

	/** Grows the 2x2 trunk exactly on {@code corner} and its +x/+z neighbours (where four saplings stood) instead of searching nearby. */
	public static boolean generateAt(LevelAccessor level, RandomSource random, BlockPos corner) {
		GreatwoodTreeFeature f = new GreatwoodTreeFeature(true);
		f.fixedBase = true;
		return f.generate(level, random, corner.getX(), corner.getY(), corner.getZ(), false);
	}

	private boolean fixedBase;

	private BlockState get(int x, int y, int z) {
		return this.worldObj.getBlockState(new BlockPos(x, y, z));
	}

	private void set(int x, int y, int z, BlockState s) {
		this.worldObj.setBlock(new BlockPos(x, y, z), s, this.flags);
	}

	private static BlockState log(int meta) {
		Direction.Axis axis = meta == 4 ? Direction.Axis.X : meta == 8 ? Direction.Axis.Z : Direction.Axis.Y;
		return TVBlocks.GREATWOOD_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
	}

	void generateLeafNodeList() {
		this.height = (int) (this.heightLimit * this.heightAttenuation);
		if (this.height >= this.heightLimit) {
			this.height = this.heightLimit - 1;
		}
		int var1 = (int) (1.382 + Math.pow(this.leafDensity * this.heightLimit / 13.0, 2.0));
		if (var1 < 1) {
			var1 = 1;
		}
		int[][] var2 = new int[var1 * this.heightLimit][4];
		int var3 = this.basePos[1] + this.heightLimit - this.leafDistanceLimit;
		int var4 = 1;
		int var5 = this.basePos[1] + this.height;
		int var6 = var3 - this.basePos[1];
		var2[0][0] = this.basePos[0];
		var2[0][1] = var3;
		var2[0][2] = this.basePos[2];
		var2[0][3] = var5;
		var3--;
		while (var6 >= 0) {
			int var7 = 0;
			float var8 = this.layerSize(var6);
			if (var8 < 0.0F) {
				var3--;
				var6--;
			} else {
				for (double var9 = 0.5; var7 < var1; var7++) {
					double var11 = this.scaleWidth * var8 * (this.rand.nextFloat() + 0.328);
					double var13 = this.rand.nextFloat() * 2.0 * Math.PI;
					int var15 = Mth.floor(var11 * Math.sin(var13) + this.basePos[0] + var9);
					int var16 = Mth.floor(var11 * Math.cos(var13) + this.basePos[2] + var9);
					int[] var17 = {var15, var3, var16};
					int[] var18 = {var15, var3 + this.leafDistanceLimit, var16};
					if (this.checkBlockLine(var17, var18) == -1) {
						int[] var19 = {this.basePos[0], this.basePos[1], this.basePos[2]};
						double var20 = Math.sqrt(Math.pow(Math.abs(this.basePos[0] - var17[0]), 2.0) + Math.pow(Math.abs(this.basePos[2] - var17[2]), 2.0));
						double var22 = var20 * this.branchSlope;
						if (var17[1] - var22 > var5) {
							var19[1] = var5;
						} else {
							var19[1] = (int) (var17[1] - var22);
						}
						if (this.checkBlockLine(var19, var17) == -1) {
							var2[var4][0] = var15;
							var2[var4][1] = var3;
							var2[var4][2] = var16;
							var2[var4][3] = var19[1];
							var4++;
						}
					}
				}
				var3--;
				var6--;
			}
		}
		this.leafNodes = new int[var4][4];
		System.arraycopy(var2, 0, this.leafNodes, 0, var4);
	}

	void genTreeLayer(int par1, int par2, int par3, float par4, byte par5) {
		int var7 = (int) (par4 + 0.618);
		byte var8 = otherCoordPairs[par5];
		byte var9 = otherCoordPairs[par5 + 3];
		int[] var10 = {par1, par2, par3};
		int[] var11 = {0, 0, 0};
		int var12 = -var7;
		var11[par5] = var10[par5];
		for (; var12 <= var7; var12++) {
			var11[var8] = var10[var8] + var12;
			int var13 = -var7;
			while (var13 <= var7) {
				double var15 = Math.pow(Math.abs(var12) + 0.5, 2.0) + Math.pow(Math.abs(var13) + 0.5, 2.0);
				if (var15 <= par4 * par4) {
					var11[var9] = var10[var9] + var13;
					BlockState block = get(var11[0], var11[1], var11[2]);
					if (block.isAir() || block.is(TVBlocks.GREATWOOD_LEAVES)) {
						set(var11[0], var11[1], var11[2], TVBlocks.GREATWOOD_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1));
					}
				}
				var13++;
			}
		}
	}

	float layerSize(int par1) {
		if (par1 < this.heightLimit * 0.3) {
			return -1.618F;
		}
		float var2 = this.heightLimit / 2.0F;
		float var3 = this.heightLimit / 2.0F - par1;
		float var4;
		if (var3 == 0.0F) {
			var4 = var2;
		} else if (Math.abs(var3) >= var2) {
			var4 = 0.0F;
		} else {
			var4 = (float) Math.sqrt(Math.pow(Math.abs(var2), 2.0) - Math.pow(Math.abs(var3), 2.0));
		}
		return var4 * 0.5F;
	}

	float leafSize(int par1) {
		return par1 >= 0 && par1 < this.leafDistanceLimit ? (par1 != 0 && par1 != this.leafDistanceLimit - 1 ? 3.0F : 2.0F) : -1.0F;
	}

	void generateLeafNode(int par1, int par2, int par3) {
		for (int var4 = par2; var4 < par2 + this.leafDistanceLimit; var4++) {
			this.genTreeLayer(par1, var4, par3, this.leafSize(var4 - par2), (byte) 1);
		}
	}

	void placeBlockLine(int[] a, int[] b) {
		int[] var4 = {0, 0, 0};
		byte var6 = 0;
		for (byte var5 = 0; var5 < 3; var5++) {
			var4[var5] = b[var5] - a[var5];
			if (Math.abs(var4[var5]) > Math.abs(var4[var6])) {
				var6 = var5;
			}
		}
		if (var4[var6] != 0) {
			byte var7 = otherCoordPairs[var6];
			byte var8 = otherCoordPairs[var6 + 3];
			byte var9 = (byte) (var4[var6] > 0 ? 1 : -1);
			double var10 = (double) var4[var7] / var4[var6];
			double var12 = (double) var4[var8] / var4[var6];
			int[] var14 = {0, 0, 0};
			for (int var15 = 0, var16 = var4[var6] + var9; var15 != var16; var15 += var9) {
				var14[var6] = Mth.floor(a[var6] + var15 + 0.5);
				var14[var7] = Mth.floor(a[var7] + var15 * var10 + 0.5);
				var14[var8] = Mth.floor(a[var8] + var15 * var12 + 0.5);
				int meta = 0;
				int var18 = Math.abs(var14[0] - a[0]);
				int var19 = Math.abs(var14[2] - a[2]);
				int var20 = Math.max(var18, var19);
				if (var20 > 0) {
					if (var18 == var20) {
						meta = 4;
					} else if (var19 == var20) {
						meta = 8;
					}
				}
				set(var14[0], var14[1], var14[2], log(meta));
			}
		}
	}

	void generateLeaves() {
		for (int[] n : this.leafNodes) {
			this.generateLeafNode(n[0], n[1], n[2]);
		}
	}

	boolean leafNodeNeedsBase(int par1) {
		return par1 >= this.heightLimit * 0.2;
	}

	void generateTrunk() {
		int var1 = this.basePos[0];
		int var2 = this.basePos[1];
		int var3 = this.basePos[1] + this.height;
		int var4 = this.basePos[2];
		int[] var5 = {var1, var2, var4};
		int[] var6 = {var1, var3, var4};
		this.placeBlockLine(var5, var6);
		if (this.trunkSize == 2) {
			var5[0]++;
			var6[0]++;
			this.placeBlockLine(var5, var6);
			var5[2]++;
			var6[2]++;
			this.placeBlockLine(var5, var6);
			var5[0]--;
			var6[0]--;
			this.placeBlockLine(var5, var6);
		}
	}

	void generateLeafNodeBases() {
		int[] var3 = {this.basePos[0], this.basePos[1], this.basePos[2]};
		for (int[] var4 : this.leafNodes) {
			int[] var5 = {var4[0], var4[1], var4[2]};
			var3[1] = var4[3];
			if (this.leafNodeNeedsBase(var3[1] - this.basePos[1])) {
				this.placeBlockLine(var3, var5);
			}
		}
	}

	int checkBlockLine(int[] a, int[] b) {
		int[] var3 = {0, 0, 0};
		byte var5 = 0;
		for (byte var4 = 0; var4 < 3; var4++) {
			var3[var4] = b[var4] - a[var4];
			if (Math.abs(var3[var4]) > Math.abs(var3[var5])) {
				var5 = var4;
			}
		}
		if (var3[var5] == 0) {
			return -1;
		}
		byte var6 = otherCoordPairs[var5];
		byte var7 = otherCoordPairs[var5 + 3];
		byte var8 = (byte) (var3[var5] > 0 ? 1 : -1);
		double var9 = (double) var3[var6] / var3[var5];
		double var11 = (double) var3[var7] / var3[var5];
		int[] var13 = {0, 0, 0};
		int var14 = 0;
		int var15 = var3[var5] + var8;
		for (; var14 != var15; var14 += var8) {
			var13[var5] = a[var5] + var14;
			var13[var6] = Mth.floor(a[var6] + var14 * var9);
			var13[var7] = Mth.floor(a[var7] + var14 * var11);
			BlockState s = get(var13[0], var13[1], var13[2]);
			if (!s.isAir() && !s.is(TVBlocks.GREATWOOD_LEAVES)) {
				break;
			}
		}
		return var14 == var15 ? -1 : Math.abs(var14);
	}

	boolean validTreeLocation(int x, int z) {
		int[] var1x = {this.basePos[0] + x, this.basePos[1], this.basePos[2] + z};
		int[] var2x = {this.basePos[0] + x, this.basePos[1] + this.heightLimit - 1, this.basePos[2] + z};
		BlockState soil = get(this.basePos[0] + x, this.basePos[1] - 1, this.basePos[2] + z);
		if (!soil.is(BlockTags.SUPPORTS_VEGETATION)) { // canSustainPlant for a sapling: grass, dirt, podzol, farmland...
			return false;
		}
		int var4 = this.checkBlockLine(var1x, var2x);
		if (var4 == -1) {
			return true;
		}
		if (var4 < 6) {
			return false;
		}
		this.heightLimit = var4;
		return true;
	}

	boolean generate(LevelAccessor world, RandomSource random, int par3, int par4, int par5, boolean spiders) {
		this.worldObj = world;
		this.rand.setSeed(random.nextLong());
		this.basePos[0] = par3;
		this.basePos[1] = par4;
		this.basePos[2] = par5;
		if (this.heightLimit == 0) {
			this.heightLimit = this.heightLimitLimit + this.rand.nextInt(this.heightLimitLimit);
		}
		boolean valid = false;
		int range = this.fixedBase ? 0 : 1;
		outer:
		for (int a = -range; a <= range; a++) {
			inner:
			for (int b = -range; b <= range; b++) {
				for (int i = 0; i < this.trunkSize; i++) {
					for (int j = 0; j < this.trunkSize; j++) {
						if (!this.validTreeLocation(i + a, j + b)) {
							continue inner;
						}
					}
				}
				valid = true;
				this.basePos[0] += a;
				this.basePos[2] += b;
				break outer;
			}
		}
		if (!valid) {
			return false;
		}
		this.generateLeafNodeList();
		this.generateLeaves();
		this.generateLeafNodeBases();
		this.generateTrunk();
		this.scaleWidth = 1.66;
		this.basePos[0] = par3;
		this.basePos[1] = par4 + this.height;
		this.basePos[2] = par5;
		this.generateLeafNodeList();
		this.generateLeaves();
		this.generateLeafNodeBases();
		this.generateTrunk();
		if (spiders) {
			BlockPos sp = new BlockPos(par3, par4 - 1, par5);
			world.setBlock(sp, Blocks.SPAWNER.defaultBlockState(), 3);
			if (world.getBlockEntity(sp) instanceof SpawnerBlockEntity spawner) {
				spawner.setEntityId(net.minecraft.world.entity.EntityTypes.CAVE_SPIDER, random);
				for (int a = 0; a < 50; a++) {
					int xx = par3 - 7 + random.nextInt(14);
					int yy = par4 + random.nextInt(10);
					int zz = par5 - 7 + random.nextInt(14);
					BlockPos p = new BlockPos(xx, yy, zz);
					if (world.isEmptyBlock(p) && touching(world, p)) {
						world.setBlock(p, Blocks.COBWEB.defaultBlockState(), 3);
					}
				}
				BlockPos cp = new BlockPos(par3, par4 - 2, par5);
				world.setBlock(cp, Blocks.CHEST.defaultBlockState(), 3);
				if (world.getBlockEntity(cp) instanceof ChestBlockEntity chest) {
					chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, random.nextLong());
				}
			}
		}
		return true;
	}

	private static boolean touching(LevelAccessor world, BlockPos p) {
		for (Direction d : Direction.values()) {
			BlockState s = world.getBlockState(p.relative(d));
			if (s.is(TVBlocks.GREATWOOD_LEAVES) || s.is(TVBlocks.GREATWOOD_LOG)) {
				return true;
			}
		}
		return false;
	}
}
