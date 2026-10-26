package dev.thaumvibe.api.visnet;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.tiles.TVBlockEntity;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of thaumcraft.api.visnet.TileVisNode: a point in the vis network. Sources (energized nodes) are roots; every other node hangs off the
 * nearest node in range that it can see, and draws vis back up the chain.
 */
public abstract class VisNodeBlockEntity extends TVBlockEntity {
	WeakReference<VisNodeBlockEntity> parent;
	final List<WeakReference<VisNodeBlockEntity>> children = new ArrayList<>();
	protected int nodeCounter;
	private boolean nodeRegged;
	public boolean nodeRefresh;

	protected VisNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/** How far (in blocks) this node links. */
	public abstract int getRange();

	public abstract boolean isSource();

	/** -1, or the primal (0-5) a relay is attuned to; attuned nodes only link to matching or unattuned ones. */
	public byte getAttunement() {
		return -1;
	}

	/** Centivis drawn up the chain toward the source. */
	public int consumeVis(Aspect aspect, int vis) {
		if (VisNetHandler.isNodeValid(this.getParent())) {
			int out = this.getParent().get().consumeVis(aspect, vis);
			if (out > 0) {
				this.triggerConsumeEffect(aspect);
			}
			return out;
		}
		return 0;
	}

	public void triggerConsumeEffect(Aspect aspect) {
	}

	public void parentChanged() {
	}

	public WeakReference<VisNodeBlockEntity> getParent() {
		return this.parent;
	}

	public void setParent(WeakReference<VisNodeBlockEntity> parent) {
		this.parent = parent;
	}

	public List<WeakReference<VisNodeBlockEntity>> getChildren() {
		return this.children;
	}

	public WeakReference<VisNodeBlockEntity> getRootSource() {
		return VisNetHandler.isNodeValid(this.getParent()) ? this.getParent().get().getRootSource() : this.isSource() ? new WeakReference<>(this) : null;
	}

	public void removeThisNode() {
		for (WeakReference<VisNodeBlockEntity> n : new ArrayList<>(this.children)) {
			if (n != null && n.get() != null) {
				n.get().removeThisNode();
			}
		}
		this.children.clear();
		if (VisNetHandler.isNodeValid(this.getParent())) {
			this.getParent().get().nodeRefresh = true;
		}
		this.setParent(null);
		this.parentChanged();
		if (this.isSource() && this.level != null) {
			VisNetHandler.removeSource(this.level, this.worldPosition);
		}
		this.nodeRegged = false;
		if (this.level != null) {
			this.markForUpdate();
		}
	}

	@Override
	public void setRemoved() {
		if (this.level != null && !this.level.isClientSide()) {
			this.removeThisNode();
		}
		super.setRemoved();
	}

	/** TileVisNode.updateEntity, run by the subclass's ticker. */
	protected void visTick() {
		if (this.level == null || this.level.isClientSide() || this.nodeCounter++ % 40 != 0 && !this.nodeRefresh) {
			return;
		}
		if (!this.nodeRefresh && !this.children.isEmpty()) {
			for (WeakReference<VisNodeBlockEntity> n : this.children) {
				if (n == null || n.get() == null || !VisNetHandler.canNodeBeSeen(this, n.get())) {
					this.nodeRefresh = true;
					break;
				}
			}
		}
		if (this.nodeRefresh) {
			for (WeakReference<VisNodeBlockEntity> nx : this.children) {
				if (nx.get() != null) {
					nx.get().nodeRefresh = true;
				}
			}
			this.children.clear();
			this.parent = null;
		}
		if (this.isSource() && !this.nodeRegged) {
			VisNetHandler.addSource(this.level, this);
			this.nodeRegged = true;
		} else if (!this.isSource() && !VisNetHandler.isNodeValid(this.getParent())) {
			this.setParent(VisNetHandler.addNode(this.level, this));
			this.nodeRefresh = true;
		}
		if (this.nodeRefresh) {
			this.markForUpdate();
			this.parentChanged();
		}
		this.nodeRefresh = false;
	}
}
