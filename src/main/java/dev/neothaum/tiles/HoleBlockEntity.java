package dev.neothaum.tiles;

import dev.neothaum.fx.FX;
import dev.neothaum.items.foci.PortableHoleFocusItem;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileHole (a TileMemory): a hole that, on its first tick, opens a ring of eight around itself and passes the bore one block deeper, then
 * waits out its countdown and puts back the block it replaced.
 */
public class HoleBlockEntity extends TVBlockEntity {
	public BlockState oldState = Blocks.AIR.defaultBlockState();
	public short countdown;
	public short countdownmax = 120;
	public byte count;
	/** The face the bore runs from (a 3D data value), or -1 for a ring hole that does not spread. */
	public byte direction = -1;

	public HoleBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.HOLE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, HoleBlockEntity be) {
		if (level.isClientSide()) {
			be.surroundWithSparkles(level, pos);
		}
		if (be.countdown == 0 && be.count > 1 && be.direction != -1 && !level.isClientSide()) {
			Direction dir = Direction.from3DDataValue(be.direction);
			for (int a = 0; a < 9; a++) {
				if (a / 3 == 1 && a % 3 == 1) {
					continue;
				}
				int u = -1 + a / 3;
				int v = -1 + a % 3;
				BlockPos ring = switch (dir.getAxis()) {
					case Y -> pos.offset(u, 0, v);
					case Z -> pos.offset(u, v, 0);
					case X -> pos.offset(0, u, v);
				};
				PortableHoleFocusItem.createHole(level, ring, -1, (byte) 1, be.countdownmax);
			}
			BlockPos next = pos.relative(dir.getOpposite());
			if (!PortableHoleFocusItem.createHole(level, next, be.direction, (byte) (be.count - 1), be.countdownmax)) {
				be.count = 0;
			}
		}
		be.countdown++;
		if (be.countdown >= be.countdownmax) {
			if (level.isClientSide()) {
				FX.blockSparkle(level, pos, 4194368);
			} else {
				level.setBlock(pos, be.oldState, 2);
				level.scheduleTick(pos, be.oldState.getBlock(), 2);
			}
		}
	}

	private void surroundWithSparkles(Level level, BlockPos pos) {
		var r = level.getRandom();
		boolean yp = level.getBlockState(pos.above()).isSolidRender();
		boolean yn = level.getBlockState(pos.below()).isSolidRender();
		boolean xp = level.getBlockState(pos.east()).isSolidRender();
		boolean xn = level.getBlockState(pos.west()).isSolidRender();
		boolean zp = level.getBlockState(pos.south()).isSolidRender();
		boolean zn = level.getBlockState(pos.north()).isSolidRender();
		boolean b1 = !level.getBlockState(pos.above()).is(TVBlocks.EMPTY_SPACE);
		boolean b2 = !level.getBlockState(pos.below()).is(TVBlocks.EMPTY_SPACE);
		boolean b3 = !level.getBlockState(pos.north()).is(TVBlocks.EMPTY_SPACE);
		boolean b4 = !level.getBlockState(pos.south()).is(TVBlocks.EMPTY_SPACE);
		boolean b5 = !level.getBlockState(pos.west()).is(TVBlocks.EMPTY_SPACE);
		boolean b6 = !level.getBlockState(pos.east()).is(TVBlocks.EMPTY_SPACE);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (!xp && yp && b6) {
			spark(level, x + 1, y + 1, z + r.nextFloat());
		}
		if (!xn && yp && b5) {
			spark(level, x, y + 1, z + r.nextFloat());
		}
		if (!zp && yp && b4) {
			spark(level, x + r.nextFloat(), y + 1, z + 1);
		}
		if (!zn && yp && b3) {
			spark(level, x + r.nextFloat(), y + 1, z);
		}
		if (!xp && yn && b6) {
			spark(level, x + 1, y, z + r.nextFloat());
		}
		if (!xn && yn && b5) {
			spark(level, x, y, z + r.nextFloat());
		}
		if (!zp && yn && b4) {
			spark(level, x + r.nextFloat(), y, z + 1);
		}
		if (!zn && yn && b3) {
			spark(level, x + r.nextFloat(), y, z);
		}
		if (!yp && xp && b1) {
			spark(level, x + 1, y + 1, z + r.nextFloat());
		}
		if (!yp && xn && b1) {
			spark(level, x, y + 1, z + r.nextFloat());
		}
		if (!yp && zp && b1) {
			spark(level, x + r.nextFloat(), y + 1, z + 1);
		}
		if (!yp && zn && b1) {
			spark(level, x + r.nextFloat(), y + 1, z);
		}
		if (!yn && xp && b2) {
			spark(level, x + 1, y, z + r.nextFloat());
		}
		if (!yn && xn && b2) {
			spark(level, x, y, z + r.nextFloat());
		}
		if (!yn && zp && b2) {
			spark(level, x + r.nextFloat(), y, z + 1);
		}
		if (!yn && zn && b2) {
			spark(level, x + r.nextFloat(), y, z);
		}
	}

	private static void spark(Level level, double x, double y, double z) {
		FX.sparkle(level, x, y, z, 1.0F, 2, 0.0F);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.oldState = input.read("oldState", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
		this.countdown = (short) input.getShortOr("countdown", (short) 0);
		this.countdownmax = (short) input.getShortOr("countdownmax", (short) 120);
		this.count = input.getByteOr("count", (byte) 0);
		this.direction = input.getByteOr("direction", (byte) -1);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("oldState", BlockState.CODEC, this.oldState);
		output.putShort("countdown", this.countdown);
		output.putShort("countdownmax", this.countdownmax);
		output.putByte("count", this.count);
		output.putByte("direction", this.direction);
	}
}
