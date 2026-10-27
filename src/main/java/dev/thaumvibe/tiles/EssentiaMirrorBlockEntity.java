package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.aspects.AspectSource;
import dev.thaumvibe.api.aspects.EssentiaHandler;
import dev.thaumvibe.blocks.MirrorBlock;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EssentiaMirrorBlockEntity extends TVBlockEntity implements AspectSource {
	private Identifier linkDim;
	private BlockPos linkPos;
	private boolean linked;
	private int tickCount = 0;

	public EssentiaMirrorBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ESSENTIA_MIRROR, pos, state);
	}

	public Identifier getLinkDim() {
		return this.linkDim;
	}

	public BlockPos getLinkPos() {
		return this.linkPos;
	}

	public boolean isLinked() {
		return this.linked;
	}

	public void setLink(Identifier dim, BlockPos pos) {
		this.linkDim = dim;
		this.linkPos = pos;
		this.setChanged();
	}

	public boolean isLinkValid() {
		if (!this.linked || this.linkDim == null || this.linkPos == null || this.level == null) {
			return false;
		}
		if (!(this.level instanceof ServerLevel serverLevel)) {
			return this.linked;
		}

		ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, this.linkDim);
		ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
		if (targetLevel == null || !targetLevel.isLoaded(this.linkPos)) {
			return false;
		}

		if (targetLevel.getBlockEntity(this.linkPos) instanceof EssentiaMirrorBlockEntity em) {
			if (em.linked && this.level.dimension().identifier().equals(em.linkDim) && this.worldPosition.equals(em.linkPos)) {
				return true;
			}
		}

		this.linked = false;
		updateBlockState();
		this.setChanged();
		return false;
	}

	public void restoreLink() {
		if (this.linkDim == null || this.linkPos == null || !(this.level instanceof ServerLevel serverLevel)) {
			return;
		}

		ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, this.linkDim);
		ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
		if (targetLevel == null || !targetLevel.isLoaded(this.linkPos)) {
			return;
		}

		if (targetLevel.getBlockEntity(this.linkPos) instanceof EssentiaMirrorBlockEntity em) {
			this.linked = true;
			em.linked = true;
			em.linkDim = this.level.dimension().identifier();
			em.linkPos = this.worldPosition;
			em.updateBlockState();
			em.setChanged();
			this.updateBlockState();
			this.setChanged();
		}
	}

	public void invalidateLink() {
		if (this.linked && this.linkDim != null && this.linkPos != null && this.level instanceof ServerLevel serverLevel) {
			ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, this.linkDim);
			ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
			if (targetLevel != null && targetLevel.isLoaded(this.linkPos)) {
				if (targetLevel.getBlockEntity(this.linkPos) instanceof EssentiaMirrorBlockEntity em) {
					em.linked = false;
					em.updateBlockState();
					em.setChanged();
				}
			}
		}
		this.linked = false;
		this.updateBlockState();
		this.setChanged();
	}

	private void updateBlockState() {
		if (this.level != null && this.getBlockState().getBlock() instanceof MirrorBlock) {
			boolean current = this.getBlockState().getValue(MirrorBlock.OPEN);
			if (current != this.linked) {
				this.level.setBlock(this.worldPosition, this.getBlockState().setValue(MirrorBlock.OPEN, this.linked), 3);
			}
		}
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		this.tickCount++;
		if (level.isClientSide()) {
			return;
		}

		if (this.tickCount % 40 == 0) {
			if (!this.linked && this.linkDim != null && this.linkPos != null) {
				restoreLink();
			} else if (this.linked) {
				isLinkValid();
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.linkDim = input.read("linkDim", Identifier.CODEC).orElse(null);
		this.linkPos = input.read("linkPos", BlockPos.CODEC).orElse(null);
		this.linked = input.getBooleanOr("linked", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.linkDim != null) {
			output.store("linkDim", Identifier.CODEC, this.linkDim);
		}
		if (this.linkPos != null) {
			output.store("linkPos", BlockPos.CODEC, this.linkPos);
		}
		output.putBoolean("linked", this.linked);
	}

	// ------------------------------------------------------------------ AspectSource
	@Override
	public AspectList getAspects() {
		return null;
	}

	@Override
	public void setAspects(AspectList aspects) {
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return false;
	}

	@Override
	public int addToContainer(Aspect tag, int amount) {
		return 0;
	}

	@Override
	public boolean takeFromContainer(Aspect tag, int amount) {
		if (isLinkValid() && this.level instanceof ServerLevel serverLevel) {
			ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, this.linkDim);
			ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
			if (targetLevel != null && targetLevel.getBlockEntity(this.linkPos) instanceof EssentiaMirrorBlockEntity target) {
				boolean drained = EssentiaHandler.drainEssentia(target, tag, null, 8);
				if (drained) {
					TVNetwork.sendEssentiaSource(serverLevel, this.worldPosition, this.worldPosition, tag.getColor());
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList ot) {
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect tag, int amount) {
		if (isLinkValid() && this.level instanceof ServerLevel serverLevel) {
			ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, this.linkDim);
			ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
			if (targetLevel != null && targetLevel.getBlockEntity(this.linkPos) instanceof EssentiaMirrorBlockEntity target) {
				return EssentiaHandler.findEssentia(target, tag, null, 8);
			}
		}
		return false;
	}

	@Override
	public boolean doesContainerContain(AspectList ot) {
		return false;
	}

	@Override
	public int containerContains(Aspect tag) {
		return 0;
	}
}
