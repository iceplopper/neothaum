package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.visnet.VisNetHandler;
import dev.neothaum.blocks.ArcaneFurnaceBlock;
import dev.neothaum.crafting.SmeltingBonus;
import dev.neothaum.registry.TVBlockEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ArcaneFurnaceBlockEntity extends TVBlockEntity implements Container {
	public static final int INVENTORY_SIZE = 32;
	private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	public int cookTime;
	public int maxCookTime;
	public int speedyTime;

	private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.SMELTING);

	public ArcaneFurnaceBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_FURNACE, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items.clear();
		ContainerHelper.loadAllItems(input, this.items);
		this.cookTime = input.getShortOr("CookTime", (short) 0);
		this.maxCookTime = input.getShortOr("MaxCookTime", (short) 0);
		this.speedyTime = input.getShortOr("SpeedyTime", (short) 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putShort("CookTime", (short) this.cookTime);
		output.putShort("MaxCookTime", (short) this.maxCookTime);
		output.putShort("SpeedyTime", (short) this.speedyTime);
	}

	public int getBellows() {
		if (this.level == null) {
			return 0;
		}
		int bellows = 0;
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos bPos = this.worldPosition.relative(dir, 2);
			if (this.level.getBlockEntity(bPos) instanceof BellowsBlockEntity b
				&& b.orientation == dir.getOpposite()
				&& !b.gettingPower()) {
				bellows++;
			}
		}
		return Math.min(3, bellows);
	}

	public int calcCookTime() {
		int base = (this.speedyTime > 0 ? 80 : 140) - 20 * this.getBellows();
		float speedMult = Math.max(0.1F, dev.neothaum.config.NeoThaumConfig.current.infernalFurnaceSpeedMultiplier);
		return Math.max(1, Math.round(base / speedMult));
	}

	public boolean canSmelt(ServerLevel serverLevel, ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		return this.quickCheck.getRecipeFor(new SingleRecipeInput(stack), serverLevel).isPresent();
	}

	public Optional<RecipeHolder<SmeltingRecipe>> getSmeltingRecipe(ServerLevel serverLevel, ItemStack stack) {
		if (stack.isEmpty()) {
			return Optional.empty();
		}
		return this.quickCheck.getRecipeFor(new SingleRecipeInput(stack), serverLevel);
	}

	public boolean addItemsToInventory(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (this.level instanceof ServerLevel serverLevel) {
			if (!this.canSmelt(serverLevel, stack)) {
				this.level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 2.0F);
				double px = this.worldPosition.getX() + 0.5;
				double py = this.worldPosition.getY() + 0.5;
				double pz = this.worldPosition.getZ() + 0.5;
				serverLevel.sendParticles(ParticleTypes.LAVA, px, py + 0.5, pz, 5, 0.2, 0.2, 0.2, 0.0);
				return true;
			}
			for (int i = 0; i < this.items.size(); i++) {
				ItemStack inSlot = this.items.get(i);
				if (!inSlot.isEmpty() && ItemStack.isSameItemSameComponents(inSlot, stack) && inSlot.getCount() < inSlot.getMaxStackSize()) {
					int space = inSlot.getMaxStackSize() - inSlot.getCount();
					int toAdd = Math.min(space, stack.getCount());
					inSlot.grow(toAdd);
					stack.shrink(toAdd);
					this.setChanged();
					if (stack.isEmpty()) {
						return true;
					}
				}
			}
			for (int i = 0; i < this.items.size(); i++) {
				if (this.items.get(i).isEmpty()) {
					this.items.set(i, stack.copy());
					stack.setCount(0);
					this.setChanged();
					return true;
				}
			}
		}
		return false;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneFurnaceBlockEntity furnace) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		boolean wasCooking = furnace.cookTime > 0;
		if (wasCooking) {
			furnace.cookTime--;
		}

		if (wasCooking && furnace.speedyTime > 0) {
			furnace.speedyTime--;
		}

		// TC4: each centivis of Ignis buys one tick of fast smelting (nozzle essentia buys 600)
		if (furnace.speedyTime <= 0) {
			furnace.speedyTime = VisNetHandler.drainVis(level, pos, Aspect.FIRE, 5);
		}

		if (furnace.maxCookTime == 0) {
			furnace.maxCookTime = furnace.calcCookTime();
		}

		if (furnace.cookTime == 0 && wasCooking) {
			for (int i = 0; i < furnace.items.size(); i++) {
				ItemStack stack = furnace.items.get(i);
				if (!stack.isEmpty()) {
					Optional<RecipeHolder<SmeltingRecipe>> opt = furnace.getSmeltingRecipe(serverLevel, stack);
					if (opt.isPresent()) {
						RecipeHolder<SmeltingRecipe> recipe = opt.get();
						ItemStack result = recipe.value().assemble(new SingleRecipeInput(stack));
						if (!result.isEmpty()) {
							furnace.ejectItem(serverLevel, state.getValue(ArcaneFurnaceBlock.FACING), result.copy(), stack.copy(), recipe.value().experience());
							stack.shrink(1);
							if (stack.isEmpty()) {
								furnace.items.set(i, ItemStack.EMPTY);
							}
							furnace.setChanged();
							break;
						}
					}
				}
			}
		}

		if (furnace.cookTime == 0) {
			for (int i = 0; i < furnace.items.size(); i++) {
				ItemStack stack = furnace.items.get(i);
				if (!stack.isEmpty() && furnace.canSmelt(serverLevel, stack)) {
					furnace.maxCookTime = furnace.calcCookTime();
					furnace.cookTime = furnace.maxCookTime;
					furnace.setChanged();
					break;
				}
			}
		}
	}

	public void ejectItem(ServerLevel serverLevel, Direction facing, ItemStack result, ItemStack input, float xp) {
		RandomSource random = serverLevel.getRandom();
		double lx = this.worldPosition.getX() + 0.5 + facing.getStepX() * 1.25;
		double ly = this.worldPosition.getY() + 0.4;
		double lz = this.worldPosition.getZ() + 0.5 + facing.getStepZ() * 1.25;

		double mx = facing.getStepX() == 0 ? (random.nextFloat() - random.nextFloat()) * 0.03 : facing.getStepX() * 0.13;
		double mz = facing.getStepZ() == 0 ? (random.nextFloat() - random.nextFloat()) * 0.03 : facing.getStepZ() * 0.13;

		ItemEntity itemEntity = new ItemEntity(serverLevel, lx, ly, lz, result);
		itemEntity.setDeltaMovement(mx, 0.0, mz);
		serverLevel.addFreshEntity(itemEntity);

		ItemStack bonus = SmeltingBonus.getBonus(input);
		if (bonus != null && !bonus.isEmpty()) {
			int bellows = this.getBellows();
			int bonusCount = 0;
			float bonusMult = Math.max(0.0F, dev.neothaum.config.NeoThaumConfig.current.infernalFurnaceBonusMultiplier);
			if (bellows == 0) {
				if (random.nextFloat() < (0.25F * bonusMult)) {
					bonusCount++;
				}
			} else {
				for (int a = 0; a < bellows; a++) {
					if (random.nextFloat() < (0.44F * bonusMult)) {
						bonusCount++;
					}
				}
			}
			if (bonusCount > 0) {
				ItemStack bonusStack = bonus.copyWithCount(bonusCount);
				ItemEntity bonusEntity = new ItemEntity(serverLevel, lx, ly, lz, bonusStack);
				bonusEntity.setDeltaMovement(mx, 0.0, mz);
				serverLevel.addFreshEntity(bonusEntity);
			}
		}

		if (xp > 0.0F) {
			int xpTotal = (int) xp;
			if (random.nextFloat() < (xp - xpTotal)) {
				xpTotal++;
			}
			while (xpTotal > 0) {
				int split = ExperienceOrb.getExperienceValue(xpTotal);
				xpTotal -= split;
				ExperienceOrb orb = new ExperienceOrb(serverLevel, lx, ly, lz, split);
				orb.setDeltaMovement(mx, 0.0, mz);
				serverLevel.addFreshEntity(orb);
			}
		}

		serverLevel.playSound(null, this.worldPosition, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.5F, 1.0F);
		serverLevel.sendParticles(ParticleTypes.FLAME, lx, ly, lz, 5, 0.1, 0.1, 0.1, 0.02);
	}

	// ---------------------------------------------------------------- Container
	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack s : this.items) {
			if (!s.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot >= 0 && slot < this.items.size() ? this.items.get(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int count) {
		ItemStack res = ContainerHelper.removeItem(this.items, slot, count);
		if (!res.isEmpty()) {
			this.setChanged();
		}
		return res;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack res = ContainerHelper.takeItem(this.items, slot);
		if (!res.isEmpty()) {
			this.setChanged();
		}
		return res;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot >= 0 && slot < this.items.size()) {
			this.items.set(slot, stack);
			this.setChanged();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.setChanged();
	}
}
