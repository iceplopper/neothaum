package dev.thaumvibe.tiles;

import dev.thaumvibe.api.Wandable;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.blocks.TubeBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVSounds;
import java.util.HashMap;
import java.util.Map;
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
 * TileTube and its variants: carries one essentia at a time towards the neighbour with the strongest suction. The valve closes on a
 * redstone signal, the filtered tube only passes its labelled aspect, the restricted tube halves suction, and the one-way tube only
 * draws from behind its facing. Wand-click an arm to seal or open that side; click the middle to turn the tube.
 */
public class TubeBlockEntity extends TVBlockEntity implements EssentiaTransport, Wandable {
	public enum Kind { NORMAL, VALVE, FILTER, RESTRICT, ONEWAY, BUFFER }

	public Direction facing = Direction.NORTH;
	public final boolean[] openSides = {true, true, true, true, true, true};
	Aspect essentiaType;
	int essentiaAmount;
	/** The strongest suction on this tube, whatever its aspect (what other blocks see from getSuctionType / getSuctionAmount). */
	Aspect suctionType;
	int suction;
	/** Suction for particular aspects (jars that already hold or are labelled for one), and for any aspect at all (empty jars). */
	final Map<Aspect, Integer> typedSuction = new HashMap<>();
	int anySuction;
	int count;
	public Aspect aspectFilter;
	public boolean allowFlow = true;
	boolean wasPoweredLastTick;
	/** Client: the valve handle's turn, 0 (open) to 360 (shut). */
	public float rotation;

	public TubeBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.TUBE, pos, state);
	}

	public Kind kind() {
		return this.getBlockState().getBlock() instanceof TubeBlock t ? t.kind : Kind.NORMAL;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.essentiaType = input.read("type", Aspect.CODEC).orElse(null);
		this.essentiaAmount = input.getIntOr("amount", 0);
		this.facing = Direction.from3DDataValue(input.getIntOr("side", 2));
		for (int a = 0; a < 6; a++) {
			this.openSides[a] = input.getBooleanOr("open" + a, true);
		}
		this.suctionType = input.read("stype", Aspect.CODEC).orElse(null);
		this.suction = input.getIntOr("samount", 0);
		this.aspectFilter = input.read("AspectFilter", Aspect.CODEC).orElse(null);
		this.allowFlow = input.getBooleanOr("flow", true);
		this.wasPoweredLastTick = input.getBooleanOr("hadpower", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.essentiaType != null) {
			output.store("type", Aspect.CODEC, this.essentiaType);
		}
		output.putInt("amount", this.essentiaAmount);
		output.putInt("side", this.facing.get3DDataValue());
		for (int a = 0; a < 6; a++) {
			output.putBoolean("open" + a, this.openSides[a]);
		}
		if (this.suctionType != null) {
			output.store("stype", Aspect.CODEC, this.suctionType);
		}
		output.putInt("samount", this.suction);
		if (this.aspectFilter != null) {
			output.store("AspectFilter", Aspect.CODEC, this.aspectFilter);
		}
		output.putBoolean("flow", this.allowFlow);
		output.putBoolean("hadpower", this.wasPoweredLastTick);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TubeBlockEntity t) {
		if (t.kind() == Kind.VALVE) {
			t.tickValve(level);
		}
		if (t.count == 0) {
			t.count = level.getRandom().nextInt(10);
		}
		// TC4 vented tubes caught between two aspects' suction; with suction kept per aspect that no longer happens
		if (!level.isClientSide()) {
			if (++t.count % 2 == 0) {
				t.calculateSuction(t.kind() == Kind.FILTER ? t.aspectFilter : null, t.kind() == Kind.RESTRICT, t.kind() == Kind.ONEWAY);
				if (t.essentiaType != null && t.essentiaAmount == 0) {
					t.essentiaType = null;
				}
			}
			if (t.count % 5 == 0 && t.suction > 0) {
				t.equalizeWithNeighbours(t.kind() == Kind.ONEWAY);
			}
		}
	}

	private void tickValve(Level level) {
		if (!level.isClientSide() && this.count % 5 == 0) {
			boolean power = level.hasNeighborSignal(this.worldPosition);
			if (this.wasPoweredLastTick && !power && !this.allowFlow || !this.wasPoweredLastTick && power && this.allowFlow) {
				this.allowFlow = !this.allowFlow;
				level.playSound(null, this.worldPosition, TVSounds.SQUEEK, SoundSource.BLOCKS, 0.7F, 0.9F + level.getRandom().nextFloat() * 0.2F);
				this.markForUpdate();
			}
			this.wasPoweredLastTick = power;
		}
		if (level.isClientSide()) {
			if (!this.allowFlow && this.rotation < 360.0F) {
				this.rotation += 20.0F;
			} else if (this.allowFlow && this.rotation > 0.0F) {
				this.rotation -= 20.0F;
			}
		}
	}

	/**
	 * Gathers suction from every connected neighbour, one point weaker per tube. Unlike TC4's single suction per tube, each aspect keeps its own
	 * suction alongside an "any aspect" one, so a jar that already holds one aspect only claims that aspect and empty jars further along still
	 * draw everything else.
	 */
	void calculateSuction(Aspect filter, boolean restrict, boolean directional) {
		this.typedSuction.clear();
		this.anySuction = 0;
		this.suction = 0;
		this.suctionType = null;
		if (this.kind() == Kind.VALVE && !this.allowFlow) {
			return;
		}
		for (Direction loc : Direction.values()) {
			if ((!directional || this.facing == loc.getOpposite()) && this.isConnectable(loc)) {
				EssentiaTransport ic = EssentiaTransport.connectable(this.level, this.worldPosition, loc);
				if (ic == null) {
					continue;
				}
				if (ic instanceof TubeBlockEntity t) {
					for (Map.Entry<Aspect, Integer> e : t.typedSuction.entrySet()) {
						this.offer(e.getKey(), e.getValue(), filter, restrict);
					}
					this.offer(null, t.anySuction, filter, restrict);
				} else {
					this.offer(ic.getSuctionType(loc.getOpposite()), ic.getSuctionAmount(loc.getOpposite()), filter, restrict);
				}
			}
		}
		this.typedSuction.values().removeIf(v -> v <= this.anySuction);
		this.suction = this.anySuction;
		for (Map.Entry<Aspect, Integer> e : this.typedSuction.entrySet()) {
			if (e.getValue() > this.suction) {
				this.suction = e.getValue();
				this.suctionType = e.getKey();
			}
		}
	}

	private void offer(Aspect type, int suck, Aspect filter, boolean restrict) {
		if (suck <= 0) {
			return;
		}
		int v = restrict ? suck / 2 : suck - 1;
		if (v <= 0) {
			return;
		}
		if (filter != null) {
			// a filtered tube passes only its aspect, so untyped suction becomes suction for that aspect
			if (type != null && type != filter) {
				return;
			}
			type = filter;
		}
		if (type == null) {
			this.anySuction = Math.max(this.anySuction, v);
		} else {
			this.typedSuction.merge(type, v, Math::max);
		}
	}

	@Override
	public int getSuctionFor(Aspect aspect, Direction face) {
		return Math.max(this.typedSuction.getOrDefault(aspect, 0), this.anySuction);
	}

	void equalizeWithNeighbours(boolean directional) {
		if (this.essentiaAmount > 0) {
			return;
		}
		for (Direction loc : Direction.values()) {
			if ((directional && this.facing == loc.getOpposite()) || !this.isConnectable(loc)) {
				continue;
			}
			EssentiaTransport ic = EssentiaTransport.connectable(this.level, this.worldPosition, loc);
			if (ic == null || !ic.canOutputTo(loc.getOpposite())) {
				continue;
			}
			Aspect a = ic.getEssentiaType(loc.getOpposite());
			if (a == null) {
				a = ic.getEssentiaType(null);
			}
			if (a == null) {
				continue;
			}
			int mine = this.getSuctionFor(a, loc);
			if (mine > ic.getSuctionFor(a, loc.getOpposite()) && mine >= ic.getMinimumSuction()) {
				int am = this.addEssentia(a, ic.takeEssentia(a, 1, loc.getOpposite()), loc);
				if (am > 0) {
					if (this.level.getRandom().nextInt(100) == 0) {
						this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 0, 0);
					}
					return;
				}
			}
		}
	}

	/** Block event 0: the tube creaks. */
	@Override
	public boolean triggerEvent(int id, int param) {
		if (id == 0) {
			if (this.level.isClientSide()) {
				this.level.playLocalSound(this.worldPosition, TVSounds.CREAK, SoundSource.BLOCKS, 1.0F, 1.3F + this.level.getRandom().nextFloat() * 0.2F,
					false);
			}
			return true;
		}
		return super.triggerEvent(id, param);
	}

	// ------------------------------------------------------------------ wand

	@Override
	public boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, BlockHitResult hit) {
		Vec3 rel = hit.getLocation().subtract(Vec3.atCenterOf(pos));
		double ax = Math.abs(rel.x), ay = Math.abs(rel.y), az = Math.abs(rel.z);
		level.playSound(player, pos, TVSounds.TOOL, SoundSource.BLOCKS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
		if (Math.max(ax, Math.max(ay, az)) < 0.19) {
			// the middle: turn the tube (valves and one-way tubes point somewhere)
			int a = this.facing.get3DDataValue();
			while (++a < 20) {
				Direction d = Direction.from3DDataValue(a % 6);
				if (this.kind() == Kind.VALVE ? !this.connectsTo(d) : this.connectsTo(d.getOpposite()) && this.isConnectable(d.getOpposite())) {
					this.facing = d;
					break;
				}
			}
		} else {
			Direction d = ax >= ay && ax >= az ? (rel.x > 0 ? Direction.EAST : Direction.WEST)
				: ay >= az ? (rel.y > 0 ? Direction.UP : Direction.DOWN) : (rel.z > 0 ? Direction.SOUTH : Direction.NORTH);
			this.openSides[d.ordinal()] = !this.openSides[d.ordinal()];
			if (level.getBlockEntity(pos.relative(d)) instanceof TubeBlockEntity other) {
				other.openSides[d.getOpposite().ordinal()] = this.openSides[d.ordinal()];
				other.markForUpdate();
				TubeBlock.refresh(level, pos.relative(d));
			}
		}
		this.markForUpdate();
		TubeBlock.refresh(level, pos);
		return true;
	}

	/** TileTube.canConnectSide: whether anything that carries essentia is next to this side. */
	private boolean connectsTo(Direction d) {
		return this.level.getBlockEntity(this.worldPosition.relative(d)) instanceof EssentiaTransport;
	}

	// ------------------------------------------------------------------ IEssentiaTransport

	@Override
	public boolean isConnectable(Direction face) {
		if (face == null) {
			return false;
		}
		if (this.kind() == Kind.VALVE && face == this.facing) {
			return false;
		}
		return this.openSides[face.ordinal()];
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
		if (this.kind() == Kind.VALVE && !this.allowFlow) {
			return;
		}
		this.suctionType = aspect;
		this.suction = amount;
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		return this.suctionType;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		return this.suction;
	}

	@Override
	public Aspect getEssentiaType(Direction face) {
		return this.essentiaType;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		return this.essentiaAmount;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		if (this.canOutputTo(face) && this.essentiaType == aspect && this.essentiaAmount > 0 && amount > 0) {
			this.essentiaAmount--;
			if (this.essentiaAmount <= 0) {
				this.essentiaType = null;
			}
			// synced so goggles can show what the tube is carrying
			this.markForUpdate();
			return 1;
		}
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		if (this.canInputFrom(face) && this.essentiaAmount == 0 && amount > 0) {
			this.essentiaType = aspect;
			this.essentiaAmount++;
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
