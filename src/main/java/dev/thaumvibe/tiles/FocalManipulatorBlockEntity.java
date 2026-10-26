package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.visnet.VisNetHandler;
import dev.thaumvibe.api.wands.FocusItem;
import dev.thaumvibe.api.wands.FocusUpgradeType;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.menu.FocalManipulatorMenu;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.ResearchManager;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
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
 * TileFocalManipulator: puts the next upgrade on a focus. Starting one costs the player 8 levels per rank and sets a vis price (200 centivis
 * of each of the upgrade's aspects, doubling per rank, reduced to primals), which it then draws from the vis network a little at a time.
 */
public class FocalManipulatorBlockEntity extends TVBlockEntity implements Container, ExtendedMenuProvider<BlockPos> {
	public static final int XP_MULT = 8;
	public static final int VIS_MULT = 200;
	private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
	public AspectList aspects = new AspectList();
	public int size;
	public int upgrade = -1;
	public int rank = -1;
	private int ticks;

	/** What the menu mirrors to the client: the six primals still owed, the full price, and the upgrade under way. */
	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int i) {
			if (i < 6) {
				return FocalManipulatorBlockEntity.this.aspects.getAmount(Aspect.getPrimalAspects().get(i));
			}
			return i == 6 ? FocalManipulatorBlockEntity.this.size : FocalManipulatorBlockEntity.this.upgrade;
		}

		@Override
		public void set(int i, int v) {
		}

		@Override
		public int getCount() {
			return 8;
		}
	};

	public FocalManipulatorBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.FOCAL_MANIPULATOR, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, FocalManipulatorBlockEntity be) {
		boolean complete = false;
		if (!level.isClientSide()) {
			if (be.rank < 0) {
				be.rank = 0;
			}
			be.ticks++;
			if (be.ticks % 5 == 0) {
				if (be.size > 0 && (be.aspects.visSize() <= 0 || be.getItem(0).isEmpty())) {
					complete = true;
					level.playSound(null, pos, TVSounds.CRAFTFAIL, SoundSource.BLOCKS, 0.33F, 1.0F);
				}
				if (be.size > 0) {
					for (Aspect aspect : be.aspects.getAspectsSortedAmount()) {
						int drain = VisNetHandler.drainVis(level, pos, aspect, Math.min(100, be.aspects.getAmount(aspect)));
						if (drain > 0) {
							be.aspects.reduce(aspect, drain);
							be.markForUpdate();
						}
					}
					if (be.aspects.visSize() <= 0 && be.getItem(0).getItem() instanceof FocusItem focus) {
						complete = true;
						focus.applyUpgrade(be.getItem(0), FocusUpgradeType.types[be.upgrade], be.rank);
						level.playSound(null, pos, TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);
					}
				}
			}
		} else if (be.size > 0) {
			var r = level.getRandom();
			FX.generic(level, pos.getX() + 0.5 + (r.nextFloat() - r.nextFloat()) * 0.3F, pos.getY() + 1.25 + (r.nextFloat() - r.nextFloat()) * 0.3F,
				pos.getZ() + 0.5 + (r.nextFloat() - r.nextFloat()) * 0.3F, 0.0, 0.0, 0.0, 0.5F + r.nextFloat() * 0.4F, 1.0F - r.nextFloat() * 0.4F,
				1.0F - r.nextFloat() * 0.4F, 0.8F, false, 112, 9, 1, 6 + r.nextInt(5), 0, 0.7F + r.nextFloat() * 0.4F);
		}
		if (complete) {
			be.size = 0;
			be.rank = -1;
			be.aspects = new AspectList();
			be.markForUpdate();
		}
	}

	/** startCraft: the next empty rank, if the player has the levels and the upgrade is offered there. */
	public boolean startCraft(int id, Player p) {
		ItemStack fs = this.getItem(0);
		if (this.size > 0 || !(fs.getItem() instanceof FocusItem focus)) {
			return false;
		}
		int[] s = FocusItem.getAppliedUpgrades(fs);
		this.rank = 1;
		while (this.rank <= 5 && s[this.rank - 1] != -1) {
			this.rank++;
		}
		int xp = this.rank * XP_MULT;
		if (p.experienceLevel < xp && !p.isCreative()) {
			return false;
		}
		FocusUpgradeType[] ut = focus.getPossibleUpgradesByRank(fs, this.rank);
		if (ut == null) {
			return false;
		}
		boolean offered = false;
		for (FocusUpgradeType t : ut) {
			if (t.id == id) {
				offered = true;
				break;
			}
		}
		if (!offered || id >= FocusUpgradeType.types.length || FocusUpgradeType.types[id] == null
			|| !focus.canApplyUpgrade(fs, p, FocusUpgradeType.types[id], this.rank)) {
			return false;
		}
		this.aspects = price(id, this.rank);
		this.size = this.aspects.visSize();
		this.upgrade = id;
		if (!p.isCreative()) {
			p.giveExperienceLevels(-xp);
		}
		this.markForUpdate();
		this.level.playSound(null, this.worldPosition, TVSounds.CRAFTSTART, SoundSource.BLOCKS, 0.25F, 1.0F);
		return true;
	}

	/** The vis an upgrade costs at a rank, in centivis of primals. */
	public static AspectList price(int id, int rank) {
		int amt = VIS_MULT;
		for (int a = 1; a < rank; a++) {
			amt *= 2;
		}
		AspectList tal = new AspectList();
		for (Aspect as : FocusUpgradeType.types[id].aspects.getAspects()) {
			tal.add(as, amt);
		}
		return ResearchManager.reduceToPrimals(tal);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items.clear();
		ContainerHelper.loadAllItems(input, this.items);
		this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
		this.size = input.getIntOr("size", 0);
		this.upgrade = input.getIntOr("upgrade", -1);
		this.rank = input.getIntOr("rank", -1);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.store("Aspects", AspectList.CODEC, this.aspects);
		output.putInt("size", this.size);
		output.putInt("upgrade", this.upgrade);
		output.putInt("rank", this.rank);
	}

	// ------------------------------------------------------------------ inventory

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.items.get(0).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack s = ContainerHelper.removeItem(this.items, slot, amount);
		this.slotChanged();
		return s;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		this.slotChanged();
	}

	/** setInventorySlotContents: a new focus wipes any price in progress. */
	private void slotChanged() {
		if (this.level != null && !this.level.isClientSide()) {
			this.aspects = new AspectList();
		}
		this.markForUpdate();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return stack.getItem() instanceof FocusItem;
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("block.thaumvibe.focal_manipulator");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new FocalManipulatorMenu(id, inv, this, this.data);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return this.worldPosition;
	}
}
