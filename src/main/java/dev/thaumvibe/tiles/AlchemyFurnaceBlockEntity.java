package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.aspects.ObjectAspects;
import dev.thaumvibe.blocks.AlchemyFurnaceBlock;
import dev.thaumvibe.menu.AlchemyFurnaceMenu;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVItems;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileAlchemyFurnace: burns fuel to boil items down into their aspects (up to 50 essentia), which rise into a column of up to five
 * alembics above it, one point every two seconds (one a second when fuelled with alumentum).
 */
public class AlchemyFurnaceBlockEntity extends TVBlockEntity implements WorldlyContainer, ExtendedMenuProvider<BlockPos> {
	private static final int[] SLOTS_BOTTOM = {1};
	private static final int[] SLOTS_TOP = {};
	private static final int[] SLOTS_SIDES = {0};
	public static final int MAX_VIS = 50;
	public AspectList aspects = new AspectList();
	public int vis;
	public int smeltTime = 100;
	int bellows = -1;
	boolean speedBoost;
	private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
	public int furnaceBurnTime;
	public int currentItemBurnTime;
	public int furnaceCookTime;
	int count;

	/** The container's progress bars: cook time, burn time, item burn time, vis, smelt time. */
	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int i) {
			AlchemyFurnaceBlockEntity f = AlchemyFurnaceBlockEntity.this;
			return switch (i) {
				case 0 -> f.furnaceCookTime;
				case 1 -> f.furnaceBurnTime;
				case 2 -> f.currentItemBurnTime;
				case 3 -> f.vis;
				default -> f.smeltTime;
			};
		}

		@Override
		public void set(int i, int v) {
		}

		@Override
		public int getCount() {
			return 5;
		}
	};

	public AlchemyFurnaceBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ALCHEMY_FURNACE, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.furnaceBurnTime = input.getShortOr("BurnTime", (short) 0);
		this.items.clear();
		ContainerHelper.loadAllItems(input, this.items);
		this.speedBoost = input.getBooleanOr("speedBoost", false);
		this.furnaceCookTime = input.getShortOr("CookTime", (short) 0);
		this.currentItemBurnTime = input.getIntOr("ItemBurnTime", 0);
		this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
		this.vis = this.aspects.visSize();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putShort("BurnTime", (short) this.furnaceBurnTime);
		output.putShort("Vis", (short) this.vis);
		ContainerHelper.saveAllItems(output, this.items);
		output.putBoolean("speedBoost", this.speedBoost);
		output.putShort("CookTime", (short) this.furnaceCookTime);
		output.putInt("ItemBurnTime", this.currentItemBurnTime);
		output.store("Aspects", AspectList.CODEC, this.aspects);
	}

	public boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	/** TileEntityFurnace.getItemBurnTime, now the COOKING_FUEL component resolved as vanilla's furnace does. */
	private int burnTime(ItemStack stack) {
		if (stack.isEmpty() || !stack.has(DataComponents.COOKING_FUEL)) {
			return 0;
		}
		if (!(this.level instanceof ServerLevel server)) {
			return 1;
		}
		LootContext ctx = new LootContext.Builder(new LootParams.Builder(server).withParameter(LootContextParams.BLOCK_STATE, this.getBlockState())
			.withParameter(LootContextParams.BLOCK_ENTITY, this).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
			.withParameter(LootContextParams.CONTAINER, this).create(LootContextParamSets.CONTAINER_PROCESS)).create(Optional.empty());
		return ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, ctx, 0);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AlchemyFurnaceBlockEntity f) {
		boolean wasBurning = f.furnaceBurnTime > 0;
		boolean changed = false;
		f.count++;
		if (f.furnaceBurnTime > 0) {
			f.furnaceBurnTime--;
		}
		if (level.isClientSide()) {
			return;
		}
		if (f.bellows < 0 || f.count % 20 == 0) {
			f.bellows = BellowsBlockEntity.getBellows(level, pos, Direction.values());
		}
		if (f.count % (f.speedBoost ? 20 : 40) == 0 && f.aspects.size() > 0) {
			f.feedAlembics();
		}
		if (f.furnaceBurnTime == 0 && f.canSmelt()) {
			f.currentItemBurnTime = f.furnaceBurnTime = f.burnTime(f.items.get(1));
			if (f.furnaceBurnTime > 0) {
				changed = true;
				ItemStack fuel = f.items.get(1);
				f.speedBoost = fuel.is(TVItems.ALUMENTUM);
				var remainder = fuel.getItem().getCraftingRemainder();
				fuel.shrink(1);
				if (fuel.isEmpty() && remainder != null) {
					f.items.set(1, remainder.create());
				}
			}
		}
		if (f.isBurning() && f.canSmelt()) {
			f.furnaceCookTime++;
			if (f.furnaceCookTime >= f.smeltTime) {
				f.furnaceCookTime = 0;
				f.smeltItem();
				changed = true;
			}
		} else {
			f.furnaceCookTime = 0;
		}
		if (wasBurning != f.furnaceBurnTime > 0) {
			changed = true;
		}
		AlchemyFurnaceBlock.updateState(level, pos, f.isBurning(), f.vis > 0);
		if (changed) {
			f.markForUpdate();
		}
	}

	private void feedAlembics() {
		AspectList exclude = new AspectList();
		for (int deep = 1; deep <= 5; deep++) {
			if (!(this.level.getBlockEntity(this.worldPosition.above(deep)) instanceof AlembicBlockEntity alembic)) {
				break;
			}
			if (alembic.aspect != null && alembic.amount < alembic.maxAmount && this.aspects.getAmount(alembic.aspect) > 0) {
				this.takeFromContainer(alembic.aspect, 1);
				alembic.addToContainer(alembic.aspect, 1);
				exclude.merge(alembic.aspect, 1);
				this.markForUpdate();
			}
		}
		for (int deep = 1; deep <= 5; deep++) {
			if (!(this.level.getBlockEntity(this.worldPosition.above(deep)) instanceof AlembicBlockEntity alembic)) {
				break;
			}
			if (alembic.aspect == null || alembic.amount == 0) {
				Aspect as = null;
				if (alembic.aspectFilter == null) {
					as = this.takeRandomAspect(exclude);
				} else if (this.takeFromContainer(alembic.aspectFilter, 1)) {
					as = alembic.aspectFilter;
				}
				if (as != null) {
					alembic.addToContainer(as, 1);
					this.markForUpdate();
					break;
				}
			}
		}
	}

	private boolean canSmelt() {
		ItemStack in = this.items.get(0);
		if (in.isEmpty()) {
			return false;
		}
		AspectList al = ObjectAspects.getAspects(in);
		if (al == null || al.size() == 0) {
			return false;
		}
		int vs = al.visSize();
		if (vs > MAX_VIS - this.vis) {
			return false;
		}
		this.smeltTime = (int) (vs * 10 * (1.0F - 0.125F * this.bellows));
		return true;
	}

	private void smeltItem() {
		if (this.canSmelt()) {
			AspectList al = ObjectAspects.getAspects(this.items.get(0));
			for (Aspect a : al.getAspects()) {
				this.aspects.add(a, al.getAmount(a));
			}
			this.vis = this.aspects.visSize();
			this.items.get(0).shrink(1);
		}
	}

	public Aspect takeRandomAspect(AspectList exclude) {
		if (this.aspects.size() > 0) {
			AspectList temp = this.aspects.copy();
			for (Aspect a : exclude.getAspects()) {
				temp.remove(a);
			}
			if (temp.size() > 0) {
				Aspect tag = temp.getAspects()[this.level.getRandom().nextInt(temp.getAspects().length)];
				this.aspects.remove(tag, 1);
				this.vis--;
				return tag;
			}
		}
		return null;
	}

	public boolean takeFromContainer(Aspect tag, int amount) {
		if (this.aspects.getAmount(tag) >= amount) {
			this.aspects.remove(tag, amount);
			this.vis -= amount;
			return true;
		}
		return false;
	}

	// ------------------------------------------------------------------ inventory

	@Override
	public int getContainerSize() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return this.items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack s = ContainerHelper.removeItem(this.items, slot, amount);
		this.setChanged();
		return s;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		stack.limitSize(this.getMaxStackSize(stack));
		this.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return net.minecraft.world.Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot == 0) {
			AspectList al = ObjectAspects.getAspects(stack);
			return al != null && al.size() > 0;
		}
		return slot == 1 && this.burnTime(stack) > 0;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.DOWN ? SLOTS_BOTTOM : side == Direction.UP ? SLOTS_TOP : SLOTS_SIDES;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		return side != Direction.UP && this.canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return side != Direction.DOWN || slot != 1 || stack.is(Items.BUCKET);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.alchemyfurnace");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new AlchemyFurnaceMenu(id, inv, this, this.data);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return this.worldPosition;
	}

	/** Drops the inventory when the block goes. */
	public void dropContents() {
		net.minecraft.world.Containers.dropContents(this.level, this.worldPosition, this);
	}
}
