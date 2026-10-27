package dev.thaumvibe.tiles;

import dev.thaumvibe.blocks.MirrorBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class MagicMirrorBlockEntity extends TVBlockEntity implements Container {
	private Identifier linkDim;
	private BlockPos linkPos;
	private boolean linked;
	private final List<ItemStack> outputQueue = new ArrayList<>();
	private int tickCount = 0;

	public MagicMirrorBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.MAGIC_MIRROR, pos, state);
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

		if (targetLevel.getBlockEntity(this.linkPos) instanceof MagicMirrorBlockEntity tm) {
			if (tm.linked && this.level.dimension().identifier().equals(tm.linkDim) && this.worldPosition.equals(tm.linkPos)) {
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

		if (targetLevel.getBlockEntity(this.linkPos) instanceof MagicMirrorBlockEntity tm) {
			this.linked = true;
			tm.linked = true;
			tm.linkDim = this.level.dimension().identifier();
			tm.linkPos = this.worldPosition;
			tm.updateBlockState();
			tm.setChanged();
			this.updateBlockState();
			this.setChanged();
		}
	}

	public void invalidateLink() {
		if (this.linked && this.linkDim != null && this.linkPos != null && this.level instanceof ServerLevel serverLevel) {
			ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, this.linkDim);
			ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
			if (targetLevel != null && targetLevel.isLoaded(this.linkPos)) {
				if (targetLevel.getBlockEntity(this.linkPos) instanceof MagicMirrorBlockEntity tm) {
					tm.linked = false;
					tm.updateBlockState();
					tm.setChanged();
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

	public boolean transport(ItemEntity item) {
		if (!isLinkValid() || !(this.level instanceof ServerLevel serverLevel)) {
			return false;
		}

		ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, this.linkDim);
		ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
		if (targetLevel != null && targetLevel.getBlockEntity(this.linkPos) instanceof MagicMirrorBlockEntity target) {
			target.addStack(item.getItem().copy());
			item.discard();
			this.level.playSound(null, this.worldPosition, TVSounds.JAR, SoundSource.BLOCKS, 0.5F, 2.0F);
			serverLevel.sendParticles(ParticleTypes.PORTAL, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.5);
			return true;
		}
		return false;
	}

	public void addStack(ItemStack stack) {
		if (!stack.isEmpty()) {
			this.outputQueue.add(stack);
			this.setChanged();
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

		if (!this.outputQueue.isEmpty() && this.tickCount % 20 == 0) {
			ItemStack out = this.outputQueue.remove(0);
			Direction face = state.getValue(MirrorBlock.FACING);
			double spawnX = pos.getX() + 0.5 + face.getStepX() * 0.5;
			double spawnY = pos.getY() + 0.5 + face.getStepY() * 0.5;
			double spawnZ = pos.getZ() + 0.5 + face.getStepZ() * 0.5;

			ItemEntity entity = new ItemEntity(level, spawnX, spawnY, spawnZ, out);
			entity.setDeltaMovement(new Vec3(face.getStepX() * 0.15, face.getStepY() * 0.15 + 0.05, face.getStepZ() * 0.15));
			level.addFreshEntity(entity);

			level.playSound(null, pos, TVSounds.JAR, SoundSource.BLOCKS, 0.5F, 2.0F);
			if (level instanceof ServerLevel sl) {
				sl.sendParticles(ParticleTypes.PORTAL, spawnX, spawnY, spawnZ, 8, 0.1, 0.1, 0.1, 0.2);
			}
			this.setChanged();
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

	// ------------------------------------------------------------------ Container interface
	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.outputQueue.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (!stack.isEmpty()) {
			if (isLinkValid() && this.level instanceof ServerLevel serverLevel) {
				ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, this.linkDim);
				ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
				if (targetLevel != null && targetLevel.getBlockEntity(this.linkPos) instanceof MagicMirrorBlockEntity target) {
					target.addStack(stack.copy());
					this.level.playSound(null, this.worldPosition, TVSounds.JAR, SoundSource.BLOCKS, 0.5F, 2.0F);
					serverLevel.sendParticles(ParticleTypes.PORTAL, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 8, 0.1, 0.1, 0.1, 0.2);
					return;
				}
			}
			addStack(stack);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		this.outputQueue.clear();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return isLinkValid();
	}
}
