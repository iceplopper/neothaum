package dev.neothaum.tiles;

import dev.neothaum.api.Wandable;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectContainer;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

/**
 * TileAlembic: catches the essentia an alchemical furnace boils off (up to 32 of one aspect). Tubes draw it from any side but the front
 * (where the label goes) and the bottom. A label locks it to one aspect, which the furnace then sends to it on purpose.
 */
public class AlembicBlockEntity extends TVBlockEntity implements AspectContainer, Wandable, EssentiaTransport {
	public Aspect aspect;
	public Aspect aspectFilter;
	public int amount;
	public final int maxAmount = 32;
	public Direction facing = Direction.NORTH;

	public AlembicBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ALEMBIC, pos, state);
	}

	/** getAppearance: the renderer drops the pot's legs onto a furnace and stacks onto another alembic. */
	public boolean aboveFurnace() {
		return this.level != null && this.level.getBlockState(this.worldPosition.below()).is(TVBlocks.ALCHEMICAL_FURNACE);
	}

	public boolean aboveAlembic() {
		return this.level != null && this.level.getBlockState(this.worldPosition.below()).is(TVBlocks.ARCANE_ALEMBIC);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.facing = Direction.from3DDataValue(input.getByteOr("facing", (byte) 2));
		this.aspectFilter = input.read("AspectFilter", Aspect.CODEC).orElse(null);
		this.aspect = input.read("aspect", Aspect.CODEC).orElse(null);
		this.amount = input.getShortOr("amount", (short) 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.aspect != null) {
			output.store("aspect", Aspect.CODEC, this.aspect);
		}
		if (this.aspectFilter != null) {
			output.store("AspectFilter", Aspect.CODEC, this.aspectFilter);
		}
		output.putShort("amount", (short) this.amount);
		output.putByte("facing", (byte) this.facing.get3DDataValue());
	}

	@Override
	public AspectList getAspects() {
		return this.aspect != null ? new AspectList().add(this.aspect, this.amount) : new AspectList();
	}

	@Override
	public void setAspects(AspectList aspects) {
	}

	@Override
	public int addToContainer(Aspect tt, int am) {
		if (this.amount < this.maxAmount && tt == this.aspect || this.amount == 0) {
			this.aspect = tt;
			int added = Math.min(am, this.maxAmount - this.amount);
			this.amount += added;
			am -= added;
		}
		this.markForUpdate();
		return am;
	}

	@Override
	public boolean takeFromContainer(Aspect tt, int am) {
		if (this.amount == 0 || this.aspect == null) {
			this.aspect = null;
			this.amount = 0;
		}
		if (this.aspect != null && this.amount >= am && tt == this.aspect) {
			this.amount -= am;
			if (this.amount <= 0) {
				this.aspect = null;
				this.amount = 0;
			}
			this.markForUpdate();
			return true;
		}
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList ot) {
		return false;
	}

	@Override
	public boolean doesContainerContain(AspectList ot) {
		return this.amount > 0 && this.aspect != null && ot.getAmount(this.aspect) > 0;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect tt, int am) {
		return this.amount >= am && tt == this.aspect;
	}

	@Override
	public int containerContains(Aspect tt) {
		return tt == this.aspect ? this.amount : 0;
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return true;
	}

	/** The wand turns the alembic's front (its label side) to the clicked side. */
	@Override
	public boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, BlockHitResult hit) {
		if (side.getAxis() == Direction.Axis.Y) {
			return false;
		}
		this.facing = side;
		this.markForUpdate();
		return true;
	}

	// ------------------------------------------------------------------ IEssentiaTransport

	@Override
	public boolean isConnectable(Direction face) {
		return face != this.facing && face != Direction.DOWN;
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return false;
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return face != this.facing && face != Direction.DOWN;
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
	public Aspect getEssentiaType(Direction face) {
		return this.aspect;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		return this.amount;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
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
