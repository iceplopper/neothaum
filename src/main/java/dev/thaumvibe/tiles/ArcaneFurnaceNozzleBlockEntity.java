package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.blocks.ArcaneFurnaceBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ArcaneFurnaceNozzleBlockEntity extends TVBlockEntity implements EssentiaTransport {
	private int drawDelay;

	public ArcaneFurnaceNozzleBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_FURNACE_NOZZLE, pos, state);
	}

	public Direction getOutwardFacing() {
		if (this.level == null) {
			return Direction.NORTH;
		}
		BlockPos core = ArcaneFurnaceBlock.findCore(this.level, this.worldPosition);
		if (core != null) {
			int dx = this.worldPosition.getX() - core.getX();
			int dz = this.worldPosition.getZ() - core.getZ();
			for (Direction d : Direction.Plane.HORIZONTAL) {
				if (d.getStepX() == dx && d.getStepZ() == dz) {
					return d;
				}
			}
		}
		return Direction.NORTH;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneFurnaceNozzleBlockEntity nozzle) {
		BlockPos corePos = ArcaneFurnaceBlock.findCore(level, pos);
		if (corePos != null && level.getBlockEntity(corePos) instanceof ArcaneFurnaceBlockEntity furnace) {
			if (furnace.speedyTime < 60 && nozzle.drawEssentia(level, pos, nozzle.getOutwardFacing())) {
				furnace.speedyTime += 600;
				furnace.setChanged();
			}
		}
	}

	private boolean drawEssentia(Level level, BlockPos pos, Direction facing) {
		if (++this.drawDelay % 5 != 0) {
			return false;
		}
		EssentiaTransport transport = EssentiaTransport.connectable(level, pos, facing);
		if (transport != null) {
			Direction opp = facing.getOpposite();
			if (!transport.canOutputTo(opp)) {
				return false;
			}
			if (transport.getSuctionFor(Aspect.FIRE, opp) < this.getSuctionAmount(facing)
				&& transport.takeEssentia(Aspect.FIRE, 1, opp) == 1) {
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- EssentiaTransport
	@Override
	public boolean isConnectable(Direction face) {
		return face == this.getOutwardFacing();
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face == this.getOutwardFacing();
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return false;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
	}

	@Override
	public boolean renderExtendedTube() {
		return false;
	}

	@Override
	public int getMinimumSuction() {
		return 0;
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		return Aspect.FIRE;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		return face == this.getOutwardFacing() ? 128 : 0;
	}

	@Override
	public Aspect getEssentiaType(Direction face) {
		return null;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		return 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}
}
