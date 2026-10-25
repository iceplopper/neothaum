package dev.thaumvibe.tiles;

import dev.thaumvibe.api.Wandable;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectContainer;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.blocks.TubeBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * TileTubeBuffer: holds up to 8 essentia of any mix, drawing with a weak suction of 1 (32 per bellows blowing into it). Wand a side to open
 * or seal it; sneak-wand a side to cycle it between normal, choked (no bellows boost, blue ring) and shut to suction (red ring).
 */
public class BufferBlockEntity extends TVBlockEntity implements AspectContainer, EssentiaTransport, Wandable {
	public static final int MAXAMOUNT = 8;
	public AspectList aspects = new AspectList();
	public final boolean[] openSides = {true, true, true, true, true, true};
	public byte[] chokedSides = {0, 0, 0, 0, 0, 0};
	int count;
	int bellows = -1;

	public BufferBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.BUFFER, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
		for (int a = 0; a < 6; a++) {
			this.openSides[a] = input.getBooleanOr("open" + a, true);
			this.chokedSides[a] = input.getByteOr("choke" + a, (byte) 0);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("Aspects", AspectList.CODEC, this.aspects);
		for (int a = 0; a < 6; a++) {
			output.putBoolean("open" + a, this.openSides[a]);
			output.putByte("choke" + a, this.chokedSides[a]);
		}
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BufferBlockEntity b) {
		b.count++;
		if (b.bellows < 0 || b.count % 20 == 0) {
			b.bellows = BellowsBlockEntity.getBellows(level, pos, Direction.values());
		}
		if (!level.isClientSide() && b.count % 5 == 0 && b.aspects.visSize() < MAXAMOUNT) {
			b.fillBuffer();
		}
	}

	private void fillBuffer() {
		for (Direction dir : Direction.values()) {
			EssentiaTransport ic = EssentiaTransport.connectable(this.level, this.worldPosition, dir);
			Aspect ta = ic == null ? null : ic.getEssentiaType(dir.getOpposite());
			if (ta != null && ic.getEssentiaAmount(dir.getOpposite()) > 0 && ic.getSuctionFor(ta, dir.getOpposite()) < this.getSuctionAmount(dir)
				&& this.getSuctionAmount(dir) >= ic.getMinimumSuction()) {
				this.addToContainer(ta, ic.takeEssentia(ta, 1, dir.getOpposite()));
				return;
			}
		}
	}

	// ------------------------------------------------------------------ IAspectContainer

	@Override
	public AspectList getAspects() {
		return this.aspects;
	}

	@Override
	public void setAspects(AspectList aspects) {
	}

	@Override
	public int addToContainer(Aspect tt, int am) {
		if (am != 1) {
			return am;
		}
		if (this.aspects.visSize() < MAXAMOUNT) {
			this.aspects.add(tt, am);
			this.markForUpdate();
			return 0;
		}
		return am;
	}

	@Override
	public boolean takeFromContainer(Aspect tt, int am) {
		if (this.aspects.getAmount(tt) >= am) {
			this.aspects.remove(tt, am);
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
	public boolean doesContainerContainAmount(Aspect tag, int amt) {
		return this.aspects.getAmount(tag) >= amt;
	}

	@Override
	public boolean doesContainerContain(AspectList ot) {
		return false;
	}

	@Override
	public int containerContains(Aspect tag) {
		return this.aspects.getAmount(tag);
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return true;
	}

	// ------------------------------------------------------------------ IEssentiaTransport

	@Override
	public boolean isConnectable(Direction face) {
		return face != null && this.openSides[face.ordinal()];
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face != null && this.openSides[face.ordinal()];
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return face != null && this.openSides[face.ordinal()];
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
		return null;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		if (face == null) {
			return 1;
		}
		byte choke = this.chokedSides[face.ordinal()];
		return choke == 2 ? 0 : this.bellows > 0 && choke != 1 ? this.bellows * 32 : 1;
	}

	@Override
	public Aspect getEssentiaType(Direction face) {
		Aspect[] as = this.aspects.getAspects();
		return as.length > 0 && this.level != null ? as[this.level.getRandom().nextInt(as.length)] : null;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		return this.aspects.visSize();
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		if (!this.canOutputTo(face)) {
			return 0;
		}
		EssentiaTransport asker = EssentiaTransport.connectable(this.level, this.worldPosition, face);
		int suction = asker == null ? 0 : asker.getSuctionFor(aspect, face.getOpposite());
		// hold on to it if something else around pulls harder for this aspect
		for (Direction dir : Direction.values()) {
			if (this.canOutputTo(dir) && dir != face) {
				EssentiaTransport ic = EssentiaTransport.connectable(this.level, this.worldPosition, dir);
				if (ic != null) {
					int sa = ic.getSuctionFor(aspect, dir.getOpposite());
					if (suction < sa && this.getSuctionAmount(dir) < sa) {
						return 0;
					}
				}
			}
		}
		amount = Math.min(amount, this.aspects.getAmount(aspect));
		return this.takeFromContainer(aspect, amount) ? amount : 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
	}

	// ------------------------------------------------------------------ wand

	@Override
	public boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, BlockHitResult hit) {
		Vec3 rel = hit.getLocation().subtract(Vec3.atCenterOf(pos));
		double ax = Math.abs(rel.x), ay = Math.abs(rel.y), az = Math.abs(rel.z);
		if (Math.max(ax, Math.max(ay, az)) < 0.25) {
			return true;
		}
		Direction d = ax >= ay && ax >= az ? (rel.x > 0 ? Direction.EAST : Direction.WEST)
			: ay >= az ? (rel.y > 0 ? Direction.UP : Direction.DOWN) : (rel.z > 0 ? Direction.SOUTH : Direction.NORTH);
		int i = d.ordinal();
		if (player.isShiftKeyDown()) {
			level.playSound(player, pos, TVSounds.SQUEEK, SoundSource.BLOCKS, 0.6F, 1.1F + level.getRandom().nextFloat() * 0.2F);
			if (!level.isClientSide()) {
				this.chokedSides[i] = (byte) ((this.chokedSides[i] + 1) % 3);
				this.markForUpdate();
			}
			return true;
		}
		level.playSound(player, pos, TVSounds.TOOL, SoundSource.BLOCKS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
		this.openSides[i] = !this.openSides[i];
		var other = level.getBlockEntity(pos.relative(d));
		if (other instanceof TubeBlockEntity t) {
			t.openSides[d.getOpposite().ordinal()] = this.openSides[i];
			t.markForUpdate();
		} else if (other instanceof BufferBlockEntity b) {
			b.openSides[d.getOpposite().ordinal()] = this.openSides[i];
			b.markForUpdate();
		}
		TubeBlock.refresh(level, pos.relative(d));
		this.markForUpdate();
		TubeBlock.refresh(level, pos);
		return true;
	}
}
