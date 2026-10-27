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
public class DeconstructionTableBlockEntity extends TVBlockEntity implements WorldlyContainer, MenuProvider {
	public ItemStack inputStack = ItemStack.EMPTY;
	public Aspect aspect = null;
	public int breaktime = 0;

	private static final Aspect[] PRIMALS = {
		Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY
	};

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> breaktime;
				case 1 -> aspectIndex(aspect);
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> breaktime = value;
				case 1 -> aspect = aspectFromIndex(value);
			}
		}

		@Override
		public int getCount() {
			return 2;
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

		if (table.aspect == null && !table.inputStack.isEmpty()) {
			AspectList al = ObjectAspects.getAspects(table.inputStack);
			if (al != null && al.size() > 0) {
				if (table.breaktime == 0) {
					table.breaktime = 40;
					dirty = true;
				} else {
					table.breaktime--;
					if (table.breaktime == 0) {
						AspectList primals = ResearchManager.reduceToPrimals(al);
						if (primals != null && primals.size() > 0 && level.getRandom().nextInt(80) < primals.visSize()) {
							Aspect[] parray = primals.getAspects();
							table.aspect = parray[level.getRandom().nextInt(parray.length)];
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

	public void claimAspect(Player player) {
		if (this.aspect != null) {
			if (player instanceof ServerPlayer sp) {
				PlayerKnowledge k = ResearchManager.knowledge(sp).copy();
				if (k.addAspectPool(this.aspect, 1)) {
					ResearchManager.setKnowledge(sp, k);
					TVNetwork.sendAspectPool(sp, this.aspect, 1, k.getAspectPool(this.aspect));
				}
			}
			this.aspect = null;
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
		output.putString("aspect", this.aspect != null ? this.aspect.getTag() : "");
		if (!this.inputStack.isEmpty()) {
			output.store("inputStack", ItemStack.OPTIONAL_CODEC, this.inputStack);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.breaktime = input.getIntOr("breaktime", 0);
		String tag = input.getStringOr("aspect", "");
		this.aspect = tag.isEmpty() ? null : Aspect.getAspect(tag);
		this.inputStack = input.read("inputStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
	}

	// ---------------- MenuProvider ----------------
	@Override
	public Component getDisplayName() {
		return Component.translatable("tile.blockTable.14.name");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new DeconstructionTableMenu(containerId, playerInventory, this.worldPosition);
	}
}
