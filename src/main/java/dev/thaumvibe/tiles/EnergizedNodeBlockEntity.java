package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.nodes.NodeModifier;
import dev.thaumvibe.api.nodes.NodeType;
import dev.thaumvibe.api.visnet.VisNodeBlockEntity;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.research.ResearchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileNodeEnergized: an aura node remade by the node transducer into a vis source. Its aura is reduced to primals and square-rooted into a fixed
 * flow of centivis that refills every tick, for the relays and machines around it.
 */
public class EnergizedNodeBlockEntity extends VisNodeBlockEntity {
	private AspectList auraBase = new AspectList().add(Aspect.AIR, 20).add(Aspect.FIRE, 20).add(Aspect.EARTH, 20).add(Aspect.WATER, 20)
		.add(Aspect.ORDER, 20).add(Aspect.ENTROPY, 20);
	AspectList visBase = new AspectList();
	AspectList vis = new AspectList();
	private NodeType nodeType = NodeType.NORMAL;
	private NodeModifier nodeModifier;

	public EnergizedNodeBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ENERGIZED_NODE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, EnergizedNodeBlockEntity be) {
		be.visTick();
		if (level.isClientSide()) {
			return;
		}
		if (be.nodeType == NodeType.UNSTABLE && level.getRandom().nextInt(500) == 1) {
			be.visBase = new AspectList();
		}
		if (be.visBase.size() == 0 && be.auraBase.size() > 0) {
			be.setupNode();
		}
		be.vis = be.visBase.copy();
	}

	public void setupNode() {
		this.visBase = new AspectList();
		AspectList temp = ResearchManager.reduceToPrimals(this.auraBase, true);
		for (Aspect aspect : temp.getAspects()) {
			int amt = temp.getAmount(aspect);
			if (this.nodeModifier == NodeModifier.BRIGHT) {
				amt = (int) (amt * 1.2F);
			}
			if (this.nodeModifier == NodeModifier.PALE) {
				amt = (int) (amt * 0.8F);
			}
			if (this.nodeModifier == NodeModifier.FADING) {
				amt = (int) (amt * 0.5F);
			}
			amt = Mth.floor(Math.sqrt(amt));
			if (this.nodeType == NodeType.UNSTABLE && this.level != null) {
				amt += this.level.getRandom().nextInt(5) - 2;
			}
			if (amt >= 1) {
				this.visBase.merge(aspect, amt);
			}
		}
		if (this.level != null) {
			this.markForUpdate();
		}
	}

	@Override
	public int consumeVis(Aspect aspect, int amount) {
		int drain = Math.min(this.vis.getAmount(aspect), amount);
		if (drain > 0) {
			this.vis.reduce(aspect, drain);
		}
		return drain;
	}

	@Override
	public int getRange() {
		return 8;
	}

	@Override
	public boolean isSource() {
		return true;
	}

	public AspectList getAuraBase() {
		return this.auraBase;
	}

	public void setAuraBase(AspectList aspects) {
		this.auraBase = aspects;
	}

	/** The vis it gives each tick (what a thaumometer reads). */
	public AspectList getAspects() {
		return this.visBase;
	}

	public NodeType getNodeType() {
		return this.nodeType;
	}

	public void setNodeType(NodeType t) {
		this.nodeType = t;
	}

	public NodeModifier getNodeModifier() {
		return this.nodeModifier;
	}

	public void setNodeModifier(NodeModifier m) {
		this.nodeModifier = m;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.nodeType = NodeType.values()[Mth.clamp(input.getByteOr("type", (byte) 0), 0, NodeType.values().length - 1)];
		byte mod = input.getByteOr("modifier", (byte) -1);
		this.nodeModifier = mod >= 0 && mod < NodeModifier.values().length ? NodeModifier.values()[mod] : null;
		this.visBase = input.read("AEB", AspectList.CODEC).orElseGet(AspectList::new);
		this.auraBase = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putByte("type", (byte) this.nodeType.ordinal());
		output.putByte("modifier", this.nodeModifier == null ? -1 : (byte) this.nodeModifier.ordinal());
		output.store("AEB", AspectList.CODEC, this.visBase);
		output.store("Aspects", AspectList.CODEC, this.auraBase);
	}
}
