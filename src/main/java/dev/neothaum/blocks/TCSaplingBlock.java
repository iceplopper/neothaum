package dev.neothaum.blocks;

import dev.neothaum.world.GreatwoodTreeFeature;
import dev.neothaum.world.SilverwoodTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockCustomPlant metadata 0/1, with two NeoThaum changes: bone meal works (vanilla sapling odds, halved for the slower silverwood),
 * and a greatwood only grows from four saplings planted in a 2x2 square, like a dark oak. Any of the four can be bone mealed, and they
 * share one growth roll: each sapling rolls a quarter as often, so the square grows at TC4's single-sapling rate.
 */
public class TCSaplingBlock extends VegetationBlock implements BonemealableBlock {
	private static final VoxelShape SHAPE = box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);
	private final boolean silverwood;

	public TCSaplingBlock(boolean silverwood, BlockBehaviour.Properties props) {
		super(props);
		this.silverwood = silverwood;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getMaxLocalRawBrightness(pos.above()) < 9) {
			return;
		}
		if (this.silverwood) {
			if (random.nextInt(50) == 0) {
				this.grow(level, pos, random);
			}
		} else if (random.nextInt(25 * 4) == 0) {
			this.grow(level, pos, random);
		}
	}

	/** The north-west corner of a complete 2x2 square of this sapling that includes {@code pos}, or null. */
	private BlockPos square(BlockGetter level, BlockPos pos) {
		for (int dx = 0; dx >= -1; dx--) {
			for (int dz = 0; dz >= -1; dz--) {
				BlockPos c = pos.offset(dx, 0, dz);
				if (level.getBlockState(c).is(this) && level.getBlockState(c.east()).is(this) && level.getBlockState(c.south()).is(this)
					&& level.getBlockState(c.south().east()).is(this)) {
					return c;
				}
			}
		}
		return null;
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
		return this.canGrow(level, pos);
	}

	/** Whether {@link #grow} can do anything here: a silverwood always can, a greatwood only as part of a full 2x2 square. */
	public boolean canGrow(BlockGetter level, BlockPos pos) {
		return this.silverwood || this.square(level, pos) != null;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		return random.nextFloat() < (this.silverwood ? 0.225F : 0.45F);
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		this.grow(level, pos, random);
	}

	public void grow(ServerLevel level, BlockPos pos, RandomSource random) {
		if (this.silverwood) {
			BlockState self = level.getBlockState(pos);
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
			if (!SilverwoodTreeFeature.generate(level, random, pos, true, 7, 5)) {
				level.setBlock(pos, self, 4);
			}
			return;
		}
		BlockPos corner = this.square(level, pos);
		if (corner == null) {
			return;
		}
		BlockPos[] spots = {corner, corner.east(), corner.south(), corner.south().east()};
		BlockState self = level.getBlockState(pos);
		for (BlockPos p : spots) {
			level.setBlock(p, Blocks.AIR.defaultBlockState(), 4);
		}
		if (!GreatwoodTreeFeature.generateAt(level, random, corner)) {
			for (BlockPos p : spots) {
				level.setBlock(p, self, 4);
			}
		}
	}
}
