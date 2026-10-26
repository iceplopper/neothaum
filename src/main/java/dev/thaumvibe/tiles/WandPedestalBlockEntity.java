package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.items.baubles.VisAmuletItem;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileWandPedestal: recharges a wand (or vis amulet) set on it, a centivis at a time, from the aura nodes within eight blocks. A compound
 * recharge focus on top lets it break compound aspects down for their primals. A wooden rod or iron cap may drain a node dry; better wands
 * leave it one point.
 */
public class WandPedestalBlockEntity extends TVBlockEntity implements WorldlyContainer {
	private static final int[] SLOTS = {0};
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private int counter;
	private boolean somethingChanged;
	private List<BlockPos> nodes;
	public boolean draining;
	public BlockPos drain = BlockPos.ZERO;
	public int drainColor;

	public WandPedestalBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.WAND_PEDESTAL, pos, state);
	}

	public ItemStack getItem() {
		return this.inventory.get(0);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, WandPedestalBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		if (be.nodes == null) {
			be.findNodes();
		}
		be.counter++;
		boolean recalc = false;
		ItemStack stack = be.getItem();
		if (be.counter % 20 == 0 && be.somethingChanged && !be.nodes.isEmpty() && !stack.isEmpty()) {
			level.updateNeighbourForOutputSignal(pos, state.getBlock());
			be.somethingChanged = false;
		}
		if (be.counter % 5 == 0 && !be.nodes.isEmpty() && !stack.isEmpty()) {
			boolean focus = level.getBlockState(pos.above()).is(TVBlocks.COMPOUND_RECHARGE_FOCUS);
			boolean wand = stack.getItem() instanceof WandItem;
			boolean amulet = stack.getItem() instanceof VisAmuletItem;
			if (wand || amulet) {
				int min = 1;
				if (wand && (WandItem.getCap(stack).getTag().equals("iron") || WandItem.getRod(stack).getTag().equals("wood"))) {
					min = 0;
				}
				AspectList room = wand ? WandItem.getAspectsWithRoom(stack) : ((VisAmuletItem) stack.getItem()).getAspectsWithRoom(stack);
				boolean was = be.draining;
				BlockPos oldDrain = be.drain;
				be.draining = false;
				if (room.size() > 0) {
					outer:
					for (BlockPos co : be.nodes) {
						if (!(level.getBlockEntity(co) instanceof NodeBlockEntity node)) {
							continue;
						}
						for (Aspect aspect : room.getAspects()) {
							if (node.getAspects().getAmount(aspect) > min) {
								be.give(stack, aspect);
								node.takeFromContainer(aspect, 1);
								node.markForUpdate();
								be.drainFrom(co, aspect);
								break outer;
							}
						}
						if (focus) {
							for (Aspect ax : node.getAspects().getAspects()) {
								if (ax == null || ax.isPrimal()) {
									continue;
								}
								AspectList primals = ResearchManager.reduceToPrimals(new AspectList().add(ax, 1));
								for (Aspect a2 : room.getAspects()) {
									if (primals.getAmount(a2) > 0 && node.getAspects().getAmount(ax) > min) {
										be.give(stack, a2);
										node.takeFromContainer(ax, 1);
										node.markForUpdate();
										be.drainFrom(co, ax);
										break outer;
									}
								}
							}
						}
					}
					if (!be.draining) {
						recalc = true;
					}
				}
				if (was != be.draining || !oldDrain.equals(be.drain) || be.draining && be.counter % 20 == 0) {
					be.markForUpdate();
				}
			}
		}
		if (be.counter % 100 == 0 && (recalc || be.nodes.isEmpty())) {
			be.findNodes();
		}
	}

	private void give(ItemStack stack, Aspect aspect) {
		if (stack.getItem() instanceof WandItem) {
			WandItem.addVis(stack, aspect, 1, true);
		} else if (stack.getItem() instanceof VisAmuletItem amulet) {
			amulet.addRealVis(stack, aspect, 100, true);
		}
		this.somethingChanged = true;
		this.setChanged();
	}

	private void drainFrom(BlockPos co, Aspect aspect) {
		this.draining = true;
		this.drain = co;
		this.drainColor = aspect.getColor();
	}

	private void findNodes() {
		this.nodes = new ArrayList<>();
		for (BlockPos p : BlockPos.betweenClosed(this.worldPosition.offset(-8, -8, -8), this.worldPosition.offset(8, 8, 8))) {
			if (this.level.getBlockEntity(p) instanceof NodeBlockEntity) {
				this.nodes.add(p.immutable());
			}
		}
	}

	/** getAspects, for the comparator and goggles: the item's vis in whole points. */
	public AspectList getAspects() {
		ItemStack s = this.getItem();
		AspectList al;
		if (s.getItem() instanceof WandItem) {
			al = WandItem.getAllVis(s);
		} else if (s.getItem() instanceof VisAmuletItem a) {
			al = new AspectList();
			for (Aspect p : Aspect.getPrimalAspects()) {
				al.merge(p, a.getVis(s, p));
			}
		} else {
			return null;
		}
		AspectList out = new AspectList();
		for (Aspect a : al.getAspectsSorted()) {
			out.add(a, al.getAmount(a) / 100);
		}
		return out;
	}

	/** BlockStoneDevice.getComparatorInputOverride: how full the wand on it is (amulets give nothing). */
	public int comparator() {
		ItemStack s = this.getItem();
		if (!(s.getItem() instanceof WandItem)) {
			return 0;
		}
		float r = WandItem.getAllVis(s).visSize() / (WandItem.getMaxVis(s) * 6.0F);
		return (int) Math.floor(r * 14.0F) + 1;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.inventory.clear();
		ContainerHelper.loadAllItems(input, this.inventory);
		this.draining = input.getBooleanOr("draining", false);
		this.drain = input.read("drain", BlockPos.CODEC).orElse(BlockPos.ZERO);
		this.drainColor = input.getIntOr("drainColor", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.inventory);
		output.putBoolean("draining", this.draining);
		output.store("drain", BlockPos.CODEC, this.drain);
		output.putInt("drainColor", this.drainColor);
	}

	// ------------------------------------------------------------------ inventory

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.get(0).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.inventory.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack s = ContainerHelper.removeItem(this.inventory, slot, amount);
		this.markForUpdate();
		return s;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.inventory, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.inventory.set(slot, stack);
		this.draining = false;
		this.markForUpdate();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean stillValid(Player player) {
		return net.minecraft.world.Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.inventory.clear();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return stack.getItem() instanceof WandItem || stack.getItem() instanceof VisAmuletItem;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		return this.getItem().isEmpty() && this.canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return true;
	}
}
