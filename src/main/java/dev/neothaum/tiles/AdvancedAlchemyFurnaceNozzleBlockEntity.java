package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock;
import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of TC4 TileAlchemyFurnaceAdvancedNozzle:
 * Four outward-facing nozzles that connect to essentia tubes to distribute boiled essentia.
 */
public class AdvancedAlchemyFurnaceNozzleBlockEntity extends TVBlockEntity implements EssentiaTransport {
	public AdvancedAlchemyFurnaceNozzleBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ADVANCED_ALCHEMY_FURNACE_NOZZLE, pos, state);
	}

	public Direction getFacing() {
		BlockState state = this.getBlockState();
		if (state.hasProperty(AdvancedAlchemyFurnaceBlock.FACING)) {
			return state.getValue(AdvancedAlchemyFurnaceBlock.FACING);
		}
		return Direction.NORTH;
	}

	public AdvancedAlchemyFurnaceBlockEntity getFurnace() {
		if (this.level == null) {
			return null;
		}
		BlockPos corePos = AdvancedAlchemyFurnaceBlock.findCore(this.level, this.worldPosition);
		if (corePos != null && this.level.getBlockEntity(corePos) instanceof AdvancedAlchemyFurnaceBlockEntity core) {
			return core;
		}
		return null;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedAlchemyFurnaceNozzleBlockEntity nozzle) {
		// Nozzle acts as passive essentia provider when suction is applied by connected tubes/jars.
	}

	@Override
	public boolean isConnectable(Direction face) {
		return face == this.getFacing();
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return false;
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return face == this.getFacing();
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		return null;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		return 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		if (face == this.getFacing()) {
			AdvancedAlchemyFurnaceBlockEntity furnace = this.getFurnace();
			if (furnace != null && furnace.aspects.getAmount(aspect) >= amount) {
				furnace.aspects.remove(aspect, amount);
				furnace.vis = furnace.aspects.visSize();
				furnace.markForUpdate();
				return amount;
			}
		}
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}

	@Override
	public Aspect getEssentiaType(Direction face) {
		AdvancedAlchemyFurnaceBlockEntity furnace = this.getFurnace();
		if (furnace != null && furnace.aspects.size() > 0) {
			return furnace.aspects.getAspects()[0];
		}
		return null;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		AdvancedAlchemyFurnaceBlockEntity furnace = this.getFurnace();
		if (furnace != null && furnace.aspects.size() > 0) {
			return furnace.aspects.getAmount(furnace.aspects.getAspects()[0]);
		}
		return 0;
	}

	@Override
	public int getMinimumSuction() {
		return 0;
	}

	@Override
	public boolean renderExtendedTube() {
		return false;
	}
}
