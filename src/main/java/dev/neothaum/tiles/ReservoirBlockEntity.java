package dev.neothaum.tiles;

import dev.neothaum.api.Wandable;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectContainer;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.blocks.ReservoirBlock;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Port of TileEssentiaReservoir:
 * Holds up to 256 essentia, 24 suction when not full, connects on facing.
 */
public class ReservoirBlockEntity extends TVBlockEntity implements AspectContainer, EssentiaTransport, Wandable {
	public static final int MAX_AMOUNT = 256;
	public AspectList aspects = new AspectList();
	public Direction facing = Direction.DOWN;
	public int count;

	public Aspect displayAspect;
	public float currentR = 1.0F;
	public float currentG = 1.0F;
	public float currentB = 1.0F;
	public float targetR = 1.0F;
	public float targetG = 1.0F;
	public float targetB = 1.0F;

	public ReservoirBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.RESERVOIR, pos, state);
		if (state.hasProperty(ReservoirBlock.FACING)) {
			this.facing = state.getValue(ReservoirBlock.FACING);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
		int f = input.getIntOr("Facing", Direction.DOWN.ordinal());
		this.facing = Direction.from3DDataValue(f);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("Aspects", AspectList.CODEC, this.aspects);
		output.putInt("Facing", this.facing.ordinal());
	}

	public Direction getFacing() {
		return this.facing;
	}

	@Override
	public AspectList getAspects() {
		return this.aspects;
	}

	@Override
	public void setAspects(AspectList aspects) {
		this.aspects = aspects.copy();
		this.markForUpdate();
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return true;
	}

	@Override
	public int addToContainer(Aspect tag, int amount) {
		if (amount <= 0) {
			return 0;
		}
		int space = MAX_AMOUNT - this.aspects.visSize();
		int added = Math.min(space, amount);
		if (added > 0) {
			this.aspects.add(tag, added);
			this.markForUpdate();
		}
		return amount - added;
	}

	@Override
	public boolean takeFromContainer(Aspect tag, int amount) {
		if (this.aspects.getAmount(tag) >= amount) {
			this.aspects.remove(tag, amount);
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
	public boolean doesContainerContainAmount(Aspect tag, int amount) {
		return this.aspects.getAmount(tag) >= amount;
	}

	@Override
	public boolean doesContainerContain(AspectList ot) {
		for (Aspect a : ot.getAspects()) {
			if (this.aspects.getAmount(a) < ot.getAmount(a)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int containerContains(Aspect tag) {
		return this.aspects.getAmount(tag);
	}

	@Override
	public boolean isConnectable(Direction face) {
		return face == this.facing;
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face == this.facing;
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return face == this.facing;
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
		return 24;
	}

	@Override
	public Aspect getSuctionType(Direction loc) {
		return null;
	}

	@Override
	public int getSuctionAmount(Direction loc) {
		return this.aspects.visSize() < MAX_AMOUNT ? 24 : 0;
	}

	@Override
	public Aspect getEssentiaType(Direction loc) {
		return this.aspects.visSize() > 0 && loc == null ? this.aspects.getAspects()[0] : null;
	}

	@Override
	public int getEssentiaAmount(Direction loc) {
		return this.aspects.visSize();
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ReservoirBlockEntity be) {
		be.count++;
		if (be.count % 5 == 0 && be.aspects.visSize() < MAX_AMOUNT) {
			be.fillReservoir(level, pos);
		}
	}

	private void fillReservoir(Level level, BlockPos pos) {
		BlockEntity te = level.getBlockEntity(pos.relative(this.facing));
		if (te instanceof EssentiaTransport ic) {
			Direction opp = this.facing.getOpposite();
			if (!ic.canOutputTo(opp)) {
				return;
			}
			Aspect ta = null;
			if (ic.getEssentiaAmount(opp) > 0
				&& ic.getSuctionAmount(opp) < this.getSuctionAmount(this.facing)
				&& this.getSuctionAmount(this.facing) >= ic.getMinimumSuction()) {
				ta = ic.getEssentiaType(opp);
			}
			if (ta != null && ic.getSuctionAmount(opp) < this.getSuctionAmount(this.facing)) {
				int taken = ic.takeEssentia(ta, 1, opp);
				if (taken > 0) {
					this.addToContainer(ta, taken);
				}
			}
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, ReservoirBlockEntity be) {
		be.count++;
		int vs = be.aspects.visSize();
		if (vs > 0) {
			if (level.getRandom().nextInt(Math.max(1, 500 - vs)) == 0) {
				level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TVSounds.CREAK, SoundSource.BLOCKS, 1.0F, 1.4F + level.getRandom().nextFloat() * 0.2F, false);
			}
			if (be.count % 20 == 0 && be.aspects.size() > 0) {
				be.displayAspect = be.aspects.getAspects()[(be.count / 20) % be.aspects.size()];
				int color = be.displayAspect.getColor();
				be.targetR = ((color >> 16) & 0xFF) / 255.0F;
				be.targetG = ((color >> 8) & 0xFF) / 255.0F;
				be.targetB = (color & 0xFF) / 255.0F;
			}
			if (be.displayAspect == null) {
				be.currentR = be.currentG = be.currentB = 1.0F;
			} else {
				be.currentR += (be.targetR - be.currentR) * 0.05F;
				be.currentG += (be.targetG - be.currentG) * 0.05F;
				be.currentB += (be.targetB - be.currentB) * 0.05F;
			}
		}
	}

	@Override
	public boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, BlockHitResult hit) {
		Direction newFacing = player.isShiftKeyDown() ? side : side.getOpposite();
		this.facing = newFacing;
		BlockState state = level.getBlockState(pos);
		if (state.hasProperty(ReservoirBlock.FACING)) {
			level.setBlock(pos, state.setValue(ReservoirBlock.FACING, newFacing), 3);
		}
		this.markForUpdate();
		return true;
	}
}
