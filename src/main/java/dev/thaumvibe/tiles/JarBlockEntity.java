package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.aspects.AspectSource;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.blocks.JarBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileJarFillable and TileJarFillableVoid: a jar holding up to 64 of one aspect. It pulls essentia down from a tube above it, and a label
 * (the aspect filter) locks it to one aspect. A void jar never refuses essentia: whatever does not fit is destroyed.
 */
public class JarBlockEntity extends TVBlockEntity implements AspectSource, EssentiaTransport {
	public static final int MAX = 64;
	public Aspect aspect;
	public Aspect aspectFilter;
	public int amount;
	/** The side the label faces (TileJarFillable.facing). */
	public Direction facing = Direction.NORTH;
	private int count;

	public JarBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.JAR, pos, state);
	}

	public boolean isVoid() {
		return this.getBlockState().getBlock() instanceof JarBlock j && j.isVoid;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.aspect = input.read("Aspect", Aspect.CODEC).orElse(null);
		this.aspectFilter = input.read("AspectFilter", Aspect.CODEC).orElse(null);
		this.amount = input.getShortOr("Amount", (short) 0);
		this.facing = Direction.from3DDataValue(input.getByteOr("facing", (byte) 2));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.aspect != null) {
			output.store("Aspect", Aspect.CODEC, this.aspect);
		}
		if (this.aspectFilter != null) {
			output.store("AspectFilter", Aspect.CODEC, this.aspectFilter);
		}
		output.putShort("Amount", (short) this.amount);
		output.putByte("facing", (byte) this.facing.get3DDataValue());
	}

	// the jar item keeps its contents and label (ItemJarFilled)
	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		AspectList al = components.get(TVComponents.ASPECTS);
		if (al != null && al.size() == 1) {
			this.aspect = al.getAspects()[0];
			this.amount = al.getAmount(this.aspect);
		}
		this.aspectFilter = components.get(TVComponents.ASPECT_FILTER);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		if (this.aspect != null && this.amount > 0) {
			builder.set(TVComponents.ASPECTS, new AspectList().add(this.aspect, this.amount));
		}
		if (this.aspectFilter != null) {
			builder.set(TVComponents.ASPECT_FILTER, this.aspectFilter);
		}
	}

	public static void tick(Level level, BlockPos pos, BlockState state, JarBlockEntity jar) {
		if (!level.isClientSide() && ++jar.count % 5 == 0 && (jar.isVoid() || jar.amount < MAX)) {
			jar.fillJar();
		}
	}

	/** Draws one essentia from a connected tube above, if this jar's suction beats the tube's. */
	private void fillJar() {
		EssentiaTransport ic = EssentiaTransport.connectable(this.level, this.worldPosition, Direction.UP);
		if (ic == null || !ic.canOutputTo(Direction.DOWN)) {
			return;
		}
		Aspect ta = null;
		if (this.aspectFilter != null) {
			ta = this.aspectFilter;
		} else if (this.aspect != null && this.amount > 0) {
			ta = this.aspect;
		} else if (ic.getEssentiaAmount(Direction.DOWN) > 0 && this.getSuctionAmount(Direction.UP) >= ic.getMinimumSuction()) {
			ta = ic.getEssentiaType(Direction.DOWN);
		}
		if (ta != null && ic.getSuctionFor(ta, Direction.DOWN) < this.getSuctionFor(ta, Direction.UP)) {
			this.addToContainer(ta, ic.takeEssentia(ta, 1, Direction.DOWN));
		}
	}

	// ------------------------------------------------------------------ IAspectContainer

	@Override
	public AspectList getAspects() {
		AspectList al = new AspectList();
		if (this.aspect != null && this.amount > 0) {
			al.add(this.aspect, this.amount);
		}
		return al;
	}

	@Override
	public void setAspects(AspectList aspects) {
	}

	@Override
	public int addToContainer(Aspect tt, int am) {
		if (am == 0) {
			return am;
		}
		boolean wasNotFull = this.amount < MAX;
		if (this.isVoid()) {
			if (tt == this.aspect || this.amount == 0) {
				this.aspect = tt;
				this.amount = Math.min(MAX, this.amount + am);
				am = 0;
			}
			if (wasNotFull) {
				this.markForUpdate();
			}
			return am;
		}
		if (this.amount < MAX && tt == this.aspect || this.amount == 0) {
			this.aspect = tt;
			int added = Math.min(am, MAX - this.amount);
			this.amount += added;
			am -= added;
		}
		this.markForUpdate();
		return am;
	}

	@Override
	public boolean takeFromContainer(Aspect tt, int am) {
		if (this.amount >= am && tt == this.aspect) {
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
	public boolean doesContainerContainAmount(Aspect tag, int amt) {
		return this.amount >= amt && tag == this.aspect;
	}

	@Override
	public boolean doesContainerContain(AspectList ot) {
		for (Aspect tt : ot.getAspects()) {
			if (this.amount > 0 && tt == this.aspect) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int containerContains(Aspect tag) {
		return 0;
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return this.aspectFilter == null || tag.equals(this.aspectFilter);
	}

	// ------------------------------------------------------------------ IEssentiaTransport

	@Override
	public boolean isConnectable(Direction face) {
		return face == Direction.UP;
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face == Direction.UP;
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return face == Direction.UP;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
	}

	@Override
	public boolean renderExtendedTube() {
		return true;
	}

	@Override
	public int getMinimumSuction() {
		if (this.isVoid()) {
			return this.aspectFilter != null ? 48 : 32;
		}
		return this.aspectFilter != null ? 64 : 32;
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		return this.aspectFilter != null ? this.aspectFilter : this.aspect;
	}

	/**
	 * Suction, strongest first: a labelled jar (64), a jar already holding some essentia, which pulls only for that aspect (48), a labelled
	 * void jar (40), an empty jar, which takes whatever no other jar is waiting for (32), then void jars, partly filled (28) or empty (24).
	 * Each tube between costs one point, so of two equal jars the nearer one fills first.
	 */
	@Override
	public int getSuctionAmount(Direction face) {
		if (this.isVoid()) {
			return this.aspectFilter != null ? 40 : this.aspect != null && this.amount > 0 ? 28 : 24;
		}
		if (this.amount < MAX) {
			return this.aspectFilter != null ? 64 : this.aspect != null && this.amount > 0 ? 48 : 32;
		}
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
		return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
	}
}
