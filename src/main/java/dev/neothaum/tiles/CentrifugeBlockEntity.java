package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectContainer;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TileCentrifuge: splits compound essentia into component aspects.
 * Inputs from bottom with suction 128 (empty) or 64 (processing). Outputs to top.
 */
public class CentrifugeBlockEntity extends TVBlockEntity implements AspectContainer, EssentiaTransport {
	public Aspect aspectIn = null;
	public Aspect aspectOut = null;
	public int process = 0;
	private int count = 0;

	// Client-side animation
	public float rotationSpeed = 0.0F;
	public float rotation = 0.0F;
	public float prevRotation = 0.0F;

	public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.CENTRIFUGE, pos, state);
	}

	public boolean isPowered() {
		return this.level != null && this.level.hasNeighborSignal(this.worldPosition);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity be) {
		be.prevRotation = be.rotation;
		boolean active = be.aspectIn != null && !be.isPowered();
		if (active && be.rotationSpeed < 20.0F) {
			be.rotationSpeed += 2.0F;
		}
		if (!active && be.rotationSpeed > 0.0F) {
			be.rotationSpeed = Math.max(0.0F, be.rotationSpeed - 0.5F);
		}
		be.rotation = (be.rotation + be.rotationSpeed) % 360.0F;
		if (be.rotation % 180.0F <= 20.0F && be.prevRotation % 180.0F >= 160.0F && be.rotationSpeed > 0.0F) {
			level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				TVSounds.PUMP, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, false);
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity be) {
		if (be.isPowered()) {
			return;
		}
		if (be.aspectOut == null && be.aspectIn == null && ++be.count % 5 == 0) {
			be.drawEssentia();
		}
		if (be.process > 0) {
			be.process--;
		}
		if (be.aspectOut == null && be.aspectIn != null && be.process == 0) {
			be.processEssentia();
		}
	}

	private void drawEssentia() {
		EssentiaTransport ic = EssentiaTransport.connectable(this.level, this.worldPosition, Direction.DOWN);
		if (ic != null && ic.canOutputTo(Direction.UP)) {
			Aspect ta = null;
			if (ic.getEssentiaAmount(Direction.UP) > 0
				&& ic.getSuctionAmount(Direction.UP) < this.getSuctionAmount(Direction.DOWN)
				&& this.getSuctionAmount(Direction.DOWN) >= ic.getMinimumSuction()) {
				ta = ic.getEssentiaType(Direction.UP);
			}
			if (ta != null
				&& !ta.isPrimal()
				&& ic.getSuctionAmount(Direction.UP) < this.getSuctionAmount(Direction.DOWN)
				&& ic.takeEssentia(ta, 1, Direction.UP) == 1) {
				this.aspectIn = ta;
				this.process = 39;
				this.markForUpdate();
			}
		}
	}

	private void processEssentia() {
		Aspect[] comps = this.aspectIn.getComponents();
		if (comps != null && comps.length == 2) {
			this.aspectOut = comps[this.level.getRandom().nextInt(2)];
		}
		this.aspectIn = null;
		this.markForUpdate();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String in = input.getStringOr("aspectIn", "");
		this.aspectIn = in.isEmpty() ? null : Aspect.getAspect(in);
		String out = input.getStringOr("aspectOut", "");
		this.aspectOut = out.isEmpty() ? null : Aspect.getAspect(out);
		this.process = input.getIntOr("process", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.aspectIn != null) {
			output.putString("aspectIn", this.aspectIn.getTag());
		}
		if (this.aspectOut != null) {
			output.putString("aspectOut", this.aspectOut.getTag());
		}
		output.putInt("process", this.process);
	}

	// ------------------------------------------------------------------ AspectContainer
	@Override
	public AspectList getAspects() {
		AspectList al = new AspectList();
		if (this.aspectOut != null) {
			al.add(this.aspectOut, 1);
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
		if (am > 0 && this.aspectOut == null) {
			this.aspectOut = tt;
			this.markForUpdate();
			am--;
		}
		return am;
	}

	@Override
	public boolean takeFromContainer(Aspect tt, int am) {
		if (this.aspectOut != null && tt == this.aspectOut) {
			this.aspectOut = null;
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
		return amount == 1 && tag == this.aspectOut;
	}

	@Override
	public boolean doesContainerContain(AspectList list) {
		for (Aspect a : list.getAspects()) {
			if (a == this.aspectOut) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int containerContains(Aspect tag) {
		return tag == this.aspectOut ? 1 : 0;
	}

	// ------------------------------------------------------------------ EssentiaTransport
	@Override
	public boolean isConnectable(Direction face) {
		return face == Direction.UP || face == Direction.DOWN;
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return face == Direction.UP;
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
		return face == Direction.DOWN ? (this.isPowered() ? 0 : (this.aspectIn == null ? 128 : 64)) : 0;
	}

	@Override
	public Aspect getEssentiaType(Direction face) {
		return this.aspectOut;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		return this.aspectOut != null ? 1 : 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		if (this.aspectIn == null && !aspect.isPrimal()) {
			this.aspectIn = aspect;
			this.process = 39;
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
