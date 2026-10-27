package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.blocks.ArcaneBoreBaseBlock;
import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of TC4 TileArcaneBoreBase:
 * Ejects items into adjacent inventories or into the world, and draws Perditio essentia through tubes.
 */
public class ArcaneBoreBaseBlockEntity extends TVBlockEntity implements EssentiaTransport {
	public ArcaneBoreBaseBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_BORE_BASE, pos, state);
	}

	public Direction getFacing() {
		BlockState state = this.getBlockState();
		if (state.hasProperty(ArcaneBoreBaseBlock.FACING)) {
			return state.getValue(ArcaneBoreBaseBlock.FACING);
		}
		return Direction.NORTH;
	}

	public boolean drawEssentia() {
		if (this.level == null) {
			return false;
		}
		for (Direction dir : Direction.values()) {
			EssentiaTransport ic = EssentiaTransport.connectable(this.level, this.worldPosition, dir);
			if (ic != null && ic.canOutputTo(dir.getOpposite())) {
				if (ic.getSuctionAmount(dir.getOpposite()) < this.getSuctionAmount(dir)
					&& ic.takeEssentia(Aspect.ENTROPY, 1, dir.getOpposite()) == 1) {
					return true;
				}
			}
		}
		return false;
	}

	public void ejectItem(ItemStack stack) {
		if (stack.isEmpty() || this.level == null || this.level.isClientSide()) {
			return;
		}
		Direction facing = this.getFacing();
		BlockPos targetPos = this.worldPosition.relative(facing);
		BlockEntity be = this.level.getBlockEntity(targetPos);

		ItemStack remainder = stack.copy();
		if (be instanceof Container container) {
			remainder = insertIntoContainer(container, remainder, facing.getOpposite());
		}

		if (!remainder.isEmpty()) {
			double ex = this.worldPosition.getX() + 0.5 + facing.getStepX() * 0.7;
			double ey = this.worldPosition.getY() + 0.4;
			double ez = this.worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.7;
			ItemEntity item = new ItemEntity(this.level, ex, ey, ez, remainder);
			item.setDeltaMovement(facing.getStepX() * 0.15, 0.05, facing.getStepZ() * 0.15);
			this.level.addFreshEntity(item);
		}
	}

	private static ItemStack insertIntoContainer(Container container, ItemStack stack, Direction side) {
		for (int i = 0; i < container.getContainerSize(); i++) {
			if (!container.canPlaceItem(i, stack)) {
				continue;
			}
			ItemStack inSlot = container.getItem(i);
			if (inSlot.isEmpty()) {
				container.setItem(i, stack.copy());
				return ItemStack.EMPTY;
			} else if (ItemStack.isSameItemSameComponents(inSlot, stack)) {
				int space = inSlot.getMaxStackSize() - inSlot.getCount();
				if (space > 0) {
					int toAdd = Math.min(space, stack.getCount());
					inSlot.grow(toAdd);
					stack.shrink(toAdd);
					if (stack.isEmpty()) {
						return ItemStack.EMPTY;
					}
				}
			}
		}
		return stack;
	}

	@Override
	public boolean isConnectable(Direction face) {
		return true;
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return true;
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return false;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		return Aspect.ENTROPY;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		return face != this.getFacing() ? 128 : 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
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
	public int getMinimumSuction() {
		return 0;
	}

	@Override
	public boolean renderExtendedTube() {
		return true;
	}
}
