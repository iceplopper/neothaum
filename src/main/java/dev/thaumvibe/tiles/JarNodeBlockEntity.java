package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.nodes.NodeModifier;
import dev.thaumvibe.api.nodes.NodeType;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TileJarNode: a node trapped in a jar. It is frozen as it was when caught, so it neither recharges nor gives up vis; a wand frees it
 * again (WandManager.releaseNodeJar).
 */
public class JarNodeBlockEntity extends TVBlockEntity {
	private AspectList aspects = new AspectList();
	private NodeType nodeType = NodeType.NORMAL;
	private NodeModifier nodeModifier = null;
	/** Client only: when the "shrinking jar" capture animation ends (System.currentTimeMillis). */
	public long animate = 0L;

	public JarNodeBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.JAR_NODE, pos, state);
	}

	public AspectList getAspects() {
		return this.aspects;
	}

	public void setAspects(AspectList aspects) {
		this.aspects = aspects;
	}

	public NodeType getNodeType() {
		return this.nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public NodeModifier getNodeModifier() {
		return this.nodeModifier;
	}

	public void setNodeModifier(NodeModifier nodeModifier) {
		this.nodeModifier = nodeModifier;
	}

	/** TileJarNode.receiveClientEvent(9): the capture sparkles over the old 3x4x3 jar, and the jar shrinks into place. */
	@Override
	public boolean triggerEvent(int id, int param) {
		if (id != 9) {
			return super.triggerEvent(id, param);
		}
		if (this.level != null && this.level.isClientSide()) {
			for (int yy = -1; yy < 3; yy++) {
				for (int xx = -1; xx < 2; xx++) {
					for (int zz = -1; zz < 2; zz++) {
						dev.thaumvibe.fx.FX.blockSparkle(this.level, this.worldPosition.offset(xx, yy, zz), -9999);
					}
				}
			}
			this.animate = System.currentTimeMillis() + 1000L;
		}
		return true;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
		this.nodeType = NodeType.values()[input.getByteOr("type", (byte) 0)];
		byte mod = input.getByteOr("modifier", (byte) -1);
		this.nodeModifier = mod >= 0 ? NodeModifier.values()[mod] : null;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("Aspects", AspectList.CODEC, this.aspects);
		output.putByte("type", (byte) this.nodeType.ordinal());
		output.putByte("modifier", this.nodeModifier == null ? -1 : (byte) this.nodeModifier.ordinal());
	}

	// ItemJarNode keeps the node (BlockJar.getDrops meta 2, ItemJarNode.onItemUse)
	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		AspectList al = components.get(TVComponents.ASPECTS);
		if (al != null && al.size() > 0) {
			this.aspects = al.copy();
			this.nodeType = NodeType.values()[components.getOrDefault(TVComponents.NODE_TYPE, 0)];
			int mod = components.getOrDefault(TVComponents.NODE_MODIFIER, -1);
			this.nodeModifier = mod >= 0 ? NodeModifier.values()[mod] : null;
		}
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		if (this.aspects.size() > 0) {
			builder.set(TVComponents.ASPECTS, this.aspects.copy());
			builder.set(TVComponents.NODE_TYPE, this.nodeType.ordinal());
			if (this.nodeModifier != null) {
				builder.set(TVComponents.NODE_MODIFIER, this.nodeModifier.ordinal());
			}
		}
	}

	/** TileJarNode.onWandRightClick: the jar breaks and the node is set free where it stood, with what it held as its new full charge. */
	public void release() {
		var level = this.level;
		BlockPos pos = this.worldPosition;
		AspectList al = this.aspects.copy();
		NodeType nt = this.nodeType;
		NodeModifier nm = this.nodeModifier;
		level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(this.getBlockState()));
		level.setBlock(pos, TVBlocks.AURA_NODE.defaultBlockState(), 3);
		if (level.getBlockEntity(pos) instanceof NodeBlockEntity tn) {
			tn.setAspects(al);
			tn.setNodeModifier(nm);
			tn.setNodeType(nt);
			tn.markForUpdate();
		}
	}
}
