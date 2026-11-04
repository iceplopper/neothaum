package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.menu.DeconstructionTableMenu;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TileDeconstructionTable:
 * Slot 0 takes items and deconstructs them one by one into primal research aspects.
 */
public class DeconstructionTableBlockEntity extends TVBlockEntity implements WorldlyContainer,
	net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<net.minecraft.core.BlockPos> {
	public ItemStack inputStack = ItemStack.EMPTY;
	/**
	 * The primal points broken out and waiting to be claimed, by PRIMALS index. TC4 held one point and stopped until it was taken; these stack,
	 * up to MAX of each, and the table keeps working meanwhile.
	 */
	public final int[] points = new int[6];
	public static final int MAX = 64;
	public int breaktime = 0;

	private static final Aspect[] PRIMALS = {
		Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY
	};

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return index == 0 ? breaktime : index <= 6 ? points[index - 1] : 0;
		}

		@Override
		public void set(int index, int value) {
			if (index == 0) {
				breaktime = value;
			} else if (index <= 6) {
				points[index - 1] = value;
			}
		}

		@Override
		public int getCount() {
			return 7;
		}
	};

	private static int aspectIndex(Aspect a) {
		if (a == null) return -1;
		for (int i = 0; i < PRIMALS.length; i++) {
			if (PRIMALS[i] == a) return i;
		}
		return -1;
	}

	private static Aspect aspectFromIndex(int idx) {
		if (idx >= 0 && idx < PRIMALS.length) {
			return PRIMALS[idx];
		}
		return null;
	}

	public DeconstructionTableBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.DECONSTRUCTION_TABLE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, DeconstructionTableBlockEntity table) {
		if (level.isClientSide()) return;
		boolean dirty = false;

		// it pauses only while one of the primals is full
		boolean full = false;
		for (int n : table.points) {
			full |= n >= MAX;
		}
		if (!full && !table.inputStack.isEmpty()) {
			AspectList al = ObjectAspects.getAspects(table.inputStack);
			if (al != null && al.size() > 0) {
				if (table.breaktime == 0) {
					table.breaktime = 40;
					dirty = true;
				} else {
					table.breaktime--;
					if (table.breaktime == 0) {
						AspectList primals = ResearchManager.reduceToPrimals(al);
						// TC4 rolled against 80; NeoThaum doubles the chance
						if (primals != null && primals.size() > 0 && level.getRandom().nextInt(40) < primals.visSize()) {
							Aspect[] parray = primals.getAspects();
							int idx = aspectIndex(parray[level.getRandom().nextInt(parray.length)]);
							if (idx >= 0) {
								table.points[idx]++;
							}
						}
						table.inputStack.shrink(1);
						dirty = true;
					}
				}
			} else {
				if (table.breaktime > 0) {
					table.breaktime = 0;
					dirty = true;
				}
			}
		} else {
			if (table.breaktime > 0) {
				table.breaktime = 0;
				dirty = true;
			}
		}

		if (dirty) {
			table.markForUpdate();
		}
	}

	/** Gives the player the waiting points of one primal (by PRIMALS index), or of all six when index is -1. */
	public void claimAspect(Player player, int index) {
		if (!(player instanceof ServerPlayer sp)) {
			return;
		}
		PlayerKnowledge k = ResearchManager.knowledge(sp).copy();
		boolean changed = false;
		for (int i = 0; i < PRIMALS.length; i++) {
			if ((index == -1 || index == i) && this.points[i] > 0 && k.addAspectPool(PRIMALS[i], this.points[i])) {
				TVNetwork.sendAspectPool(sp, PRIMALS[i], this.points[i], k.getAspectPool(PRIMALS[i]));
				this.points[i] = 0;
				changed = true;
			}
		}
		if (changed) {
			ResearchManager.setKnowledge(sp, k);
			this.markForUpdate();
		}
	}

	// ---------------- Container implementation ----------------
	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.inputStack.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot == 0 ? this.inputStack : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot == 0 && !this.inputStack.isEmpty()) {
			ItemStack split = this.inputStack.split(amount);
			this.markForUpdate();
			return split;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot == 0) {
			ItemStack stack = this.inputStack;
			this.inputStack = ItemStack.EMPTY;
			return stack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0) {
			this.inputStack = stack;
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
		this.inputStack = ItemStack.EMPTY;
		this.markForUpdate();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		if (dir == Direction.UP) return false;
		AspectList al = ObjectAspects.getAspects(stack);
		return al != null && al.size() > 0;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	// ---------------- NBT ----------------
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("breaktime", this.breaktime);
		output.putIntArray("points", this.points);
		if (!this.inputStack.isEmpty()) {
			output.store("inputStack", ItemStack.OPTIONAL_CODEC, this.inputStack);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.breaktime = input.getIntOr("breaktime", 0);
		int[] saved = input.getIntArray("points").orElse(new int[0]);
		java.util.Arrays.fill(this.points, 0);
		System.arraycopy(saved, 0, this.points, 0, Math.min(saved.length, 6));
		// a table saved before the points stacked held a single aspect
		Aspect old = Aspect.getAspect(input.getStringOr("aspect", ""));
		if (old != null && aspectIndex(old) >= 0 && saved.length == 0) {
			this.points[aspectIndex(old)] = 1;
		}
		this.inputStack = input.read("inputStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
	}

	// ---------------- MenuProvider ----------------
	@Override
	public Component getDisplayName() {
		return Component.translatable("tile.blockTable.14.name");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new DeconstructionTableMenu(containerId, playerInventory, this, this.worldPosition, this.data);
	}

	/** The menu type sends the table's position, so the client's menu can find it; without it the screen never opened. */
	@Override
	public net.minecraft.core.BlockPos getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
		return this.worldPosition;
	}
}
