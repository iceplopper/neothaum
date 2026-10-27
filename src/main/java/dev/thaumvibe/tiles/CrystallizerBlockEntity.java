package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectContainer;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.blocks.CrystallizerBlock;
import dev.thaumvibe.items.CrystalEssenceItem;
import dev.thaumvibe.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TileEssentiaCrystalizer: solidifies 1 point of essentia into a crystal essence item.
 * Connects to essentia transport on its attached face (facing).
 */
public class CrystallizerBlockEntity extends TVBlockEntity implements AspectContainer, EssentiaTransport {
	public Aspect aspect = null;
	public int progress = 0;
	private int count = 0;

	// Client animation state
	public float spin = 0.0F;
	public float spinInc = 0.0F;
	public float prevSpin = 0.0F;

	public CrystallizerBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.CRYSTALLIZER, pos, state);
	}

	public Direction getFacing() {
		return this.getBlockState().hasProperty(CrystallizerBlock.FACING)
			? this.getBlockState().getValue(CrystallizerBlock.FACING)
			: Direction.DOWN;
	}

	public boolean isPowered() {
		return this.level != null && this.level.hasNeighborSignal(this.worldPosition);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, CrystallizerBlockEntity be) {
		be.prevSpin = be.spin;
		be.spin = (be.spin + be.spinInc) % 360.0F;
		boolean active = be.aspect != null && !be.isPowered();
		if (active && be.spinInc < 20.0F) {
			be.spinInc += 0.2F;
			if (be.spinInc > 20.0F) {
				be.spinInc = 20.0F;
			}
		} else if (!active && be.spinInc > 0.0F) {
			be.spinInc = Math.max(0.0F, be.spinInc - 0.2F);
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CrystallizerBlockEntity be) {
		if (be.isPowered()) {
			return;
		}
		if (be.aspect == null && ++be.count % 5 == 0) {
			be.fillReservoir();
		} else if (be.aspect != null) {
			be.progress++;
			if (be.progress >= 200) {
				be.eject();
				be.aspect = null;
				be.progress = 0;
				be.markForUpdate();
			}
		}
	}

	private void fillReservoir() {
		Direction facing = this.getFacing();
		EssentiaTransport ic = EssentiaTransport.connectable(this.level, this.worldPosition, facing);
		if (ic != null && ic.canOutputTo(facing.getOpposite())) {
			Aspect ta = null;
			if (ic.getEssentiaAmount(facing.getOpposite()) > 0
				&& ic.getSuctionAmount(facing.getOpposite()) < this.getSuctionAmount(facing)
				&& this.getSuctionAmount(facing) >= ic.getMinimumSuction()) {
				ta = ic.getEssentiaType(facing.getOpposite());
			}
			if (ta != null
				&& ic.getSuctionAmount(facing.getOpposite()) < this.getSuctionAmount(facing)
				&& ic.takeEssentia(ta, 1, facing.getOpposite()) == 1) {
				this.aspect = ta;
				this.progress = 0;
				this.markForUpdate();
			}
		}
	}

	private void eject() {
		if (this.aspect == null || this.level == null) {
			return;
		}
		ItemStack stack = CrystalEssenceItem.of(this.aspect);
		Direction ejectDir = this.getFacing().getOpposite();
		BlockPos targetPos = this.worldPosition.relative(ejectDir);

		if (this.level.getBlockEntity(targetPos) instanceof Container targetContainer) {
			stack = HopperBlockEntity.addItem(null, targetContainer, stack, ejectDir.getOpposite());
		}

		if (!stack.isEmpty()) {
			ItemEntity item = new ItemEntity(this.level,
				this.worldPosition.getX() + 0.5 + ejectDir.getStepX() * 0.65,
				this.worldPosition.getY() + 0.5 + ejectDir.getStepY() * 0.65,
				this.worldPosition.getZ() + 0.5 + ejectDir.getStepZ() * 0.65,
				stack);
			item.setDeltaMovement(ejectDir.getStepX() * 0.05, ejectDir.getStepY() * 0.05, ejectDir.getStepZ() * 0.05);
			this.level.addFreshEntity(item);
		}

		this.level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH,
			SoundSource.BLOCKS, 0.25F, 2.6F + (this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.8F);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String a = input.getStringOr("Aspect", "");
		this.aspect = a.isEmpty() ? null : Aspect.getAspect(a);
		this.progress = input.getIntOr("Progress", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.aspect != null) {
			output.putString("Aspect", this.aspect.getTag());
		}
		output.putInt("Progress", this.progress);
	}

	// ------------------------------------------------------------------ AspectContainer
	@Override
	public AspectList getAspects() {
		AspectList al = new AspectList();
		if (this.aspect != null) {
			al.add(this.aspect, 1);
		}
		return al;
	}

	@Override
	public void setAspects(AspectList aspects) {
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return true;
	}

	@Override
	public int addToContainer(Aspect tt, int am) {
		if (am > 0 && this.aspect == null) {
			this.aspect = tt;
			this.progress = 0;
			this.markForUpdate();
			am--;
		}
		return am;
	}

	@Override
	public boolean takeFromContainer(Aspect tt, int am) {
		if (this.aspect != null && tt == this.aspect) {
			this.aspect = null;
			this.progress = 0;
			this.markForUpdate();
			return true;
		}
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList list) {
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect tag, int amount) {
		return amount == 1 && tag == this.aspect;
	}

	@Override
	public boolean doesContainerContain(AspectList list) {
		for (Aspect a : list.getAspects()) {
			if (a == this.aspect) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int containerContains(Aspect tag) {
		return tag == this.aspect ? 1 : 0;
	}

	// ------------------------------------------------------------------ EssentiaTransport
	@Override
	public boolean isConnectable(Direction face) {
		return face == this.getFacing();
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face == this.getFacing();
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
		return null;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		return face == this.getFacing() ? (this.isPowered() ? 0 : (this.aspect == null ? 128 : 64)) : 0;
	}

	@Override
	public Aspect getEssentiaType(Direction face) {
		return this.aspect;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		return this.aspect != null ? 1 : 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		if (face == this.getFacing() && this.aspect == null) {
			this.aspect = aspect;
			this.progress = 0;
			this.markForUpdate();
			return 1;
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
