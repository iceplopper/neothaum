package dev.thaumvibe.tiles;

import dev.thaumvibe.menu.SpaMenu;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SpaBlockEntity extends TVBlockEntity implements WorldlyContainer, MenuProvider {
	private ItemStack bathSalts = ItemStack.EMPTY;
	private boolean mix = true;
	private String fluidType = "water";
	private int fluidAmount = 0;
	private int counter = 0;

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> fluidAmount;
				case 1 -> mix ? 1 : 0;
				case 2 -> "purifying_fluid".equals(fluidType) ? 1 : 0;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> fluidAmount = value;
				case 1 -> mix = value != 0;
				case 2 -> fluidType = value == 1 ? "purifying_fluid" : "water";
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	};

	public SpaBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.SPA, pos, state);
	}

	public void toggleMix() {
		this.mix = !this.mix;
		this.markForUpdate();
	}

	public boolean getMix() {
		return this.mix;
	}

	public int getFluidAmount() {
		return this.fluidAmount;
	}

	public String getFluidType() {
		return this.fluidType;
	}

	public boolean fillFluid(String type, int amount) {
		if (this.fluidAmount == 0) {
			this.fluidType = type;
			this.fluidAmount = Math.min(5000, amount);
			this.markForUpdate();
			return true;
		} else if (this.fluidType.equals(type) && this.fluidAmount < 5000) {
			this.fluidAmount = Math.min(5000, this.fluidAmount + amount);
			this.markForUpdate();
			return true;
		}
		return false;
	}

	public int drainFluid(int amount) {
		int drained = Math.min(amount, this.fluidAmount);
		this.fluidAmount -= drained;
		if (this.fluidAmount <= 0) {
			this.fluidAmount = 0;
			this.fluidType = "water";
		}
		this.markForUpdate();
		return drained;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, SpaBlockEntity spa) {
		if (level.isClientSide()) return;
		if (spa.counter++ % 40 == 0 && !level.hasNeighborSignal(pos) && spa.hasIngredients()) {
			Block tb = spa.mix ? TVBlocks.PURIFYING_FLUID : ("purifying_fluid".equals(spa.fluidType) ? TVBlocks.PURIFYING_FLUID : Blocks.WATER);
			BlockState aboveState = level.getBlockState(pos.above());

			if (aboveState.is(tb)) {
				for (int xx = -2; xx <= 2; xx++) {
					for (int zz = -2; zz <= 2; zz++) {
						BlockPos targetPos = pos.above().offset(xx, 0, zz);
						if (spa.isValidLocation(level, targetPos, true, tb)) {
							spa.consumeIngredients();
							level.setBlock(targetPos, tb.defaultBlockState(), 3);
							if (level instanceof ServerLevel sl) {
								sl.sendParticles(ParticleTypes.BUBBLE, targetPos.getX() + 0.5, targetPos.getY() + 0.2, targetPos.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.05);
							}
							level.playSound(null, targetPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.0F);
							return;
						}
					}
				}
			} else if (spa.isValidLocation(level, pos.above(), false, tb)) {
				spa.consumeIngredients();
				level.setBlock(pos.above(), tb.defaultBlockState(), 3);
				if (level instanceof ServerLevel sl) {
					sl.sendParticles(ParticleTypes.BUBBLE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.05);
				}
				level.playSound(null, pos.above(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.0F);
			}
		}
	}

	private boolean hasIngredients() {
		if (this.mix) {
			if (!"water".equals(this.fluidType) || this.fluidAmount < 1000) {
				return false;
			}
			if (this.bathSalts.isEmpty() || !this.bathSalts.is(TVItems.PURIFYING_BATH_SALTS)) {
				return false;
			}
		} else {
			if (this.fluidAmount < 1000) {
				return false;
			}
		}
		return true;
	}

	private void consumeIngredients() {
		if (this.mix) {
			this.removeItem(0, 1);
		}
		this.drainFluid(1000);
	}

	private boolean isValidLocation(Level level, BlockPos p, boolean mustBeAdjacent, Block target) {
		if (target == Blocks.WATER && level.dimension() == Level.NETHER) {
			return false;
		}
		BlockState below = level.getBlockState(p.below());
		BlockState at = level.getBlockState(p);
		if ((below.isSolid() || below.isFaceSturdy(level, p.below(), Direction.UP))
			&& at.canBeReplaced() && !at.is(target)) {
			if (!mustBeAdjacent) {
				return true;
			}
			for (Direction dir : Direction.values()) {
				if (level.getBlockState(p.relative(dir)).is(target)) {
					return true;
				}
			}
		}
		return false;
	}

	// ---------------- Container implementation ----------------
	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.bathSalts.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot == 0 ? this.bathSalts : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot == 0 && !this.bathSalts.isEmpty()) {
			ItemStack split = this.bathSalts.split(amount);
			if (this.bathSalts.isEmpty()) {
				this.bathSalts = ItemStack.EMPTY;
			}
			this.markForUpdate();
			return split;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot == 0) {
			ItemStack stack = this.bathSalts;
			this.bathSalts = ItemStack.EMPTY;
			return stack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0) {
			this.bathSalts = stack;
			if (stack.getCount() > this.getMaxStackSize()) {
				stack.setCount(this.getMaxStackSize());
			}
			this.markForUpdate();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.bathSalts = ItemStack.EMPTY;
		this.markForUpdate();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side != Direction.UP ? new int[]{0} : new int[0];
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, Direction direction) {
		return direction != Direction.UP && itemStack.is(TVItems.PURIFYING_BATH_SALTS);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
		return direction != Direction.UP;
	}

	// ---------------- NBT ----------------
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean("mix", this.mix);
		output.putInt("fluidAmount", this.fluidAmount);
		output.putString("fluidType", this.fluidType);
		if (!this.bathSalts.isEmpty()) {
			output.store("item", ItemStack.OPTIONAL_CODEC, this.bathSalts);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.mix = input.getBooleanOr("mix", true);
		this.fluidAmount = input.getIntOr("fluidAmount", 0);
		this.fluidType = input.getStringOr("fluidType", "water");
		this.bathSalts = input.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
	}

	// ---------------- Screen / Menu ----------------
	@Override
	public Component getDisplayName() {
		return Component.translatable("tile.blockStoneDevice.12.name");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new SpaMenu(containerId, playerInventory, this.worldPosition);
	}
}
