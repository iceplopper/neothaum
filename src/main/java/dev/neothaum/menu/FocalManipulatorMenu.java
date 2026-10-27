package dev.neothaum.menu;

import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.registry.TVMenus;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.FocalManipulatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** ContainerFocalManipulator: the focus slot over the player's inventory; button N starts upgrade N. */
public class FocalManipulatorMenu extends AbstractContainerMenu {
	public final Container tile;
	public final ContainerData data;
	private final FocalManipulatorBlockEntity table;
	public BlockPos pos = BlockPos.ZERO;

	public FocalManipulatorMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, new SimpleContainer(1), new SimpleContainerData(8));
		this.pos = pos;
	}

	public FocalManipulatorMenu(int id, Inventory inv, Container tile, ContainerData data) {
		super(TVMenus.FOCAL_MANIPULATOR, id);
		this.tile = tile;
		this.data = data;
		this.table = tile instanceof FocalManipulatorBlockEntity t ? t : null;
		if (this.table != null) {
			this.pos = this.table.getBlockPos();
		}
		this.addSlot(new Slot(tile, 0, 88, 60) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof FocusItem;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 16 + j * 18, 151 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 16 + i * 18, 209));
		}
		this.addDataSlots(data);
	}

	@Override
	public boolean clickMenuButton(Player p, int button) {
		if (this.table != null && button >= 0 && !this.table.startCraft(button, p)) {
			p.level().playSound(null, this.table.getBlockPos(), TVSounds.CRAFTFAIL, SoundSource.BLOCKS, 0.33F, 1.0F);
		}
		return false;
	}

	@Override
	public ItemStack quickMoveStack(Player p, int index) {
		Slot slot = this.slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack in = slot.getItem();
		ItemStack copy = in.copy();
		if (index != 0) {
			if (in.getItem() instanceof FocusItem) {
				if (!this.moveItemStackTo(in, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < 28) {
				if (!this.moveItemStackTo(in, 28, 37, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(in, 1, 28, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.moveItemStackTo(in, 1, 37, false)) {
			return ItemStack.EMPTY;
		}
		if (in.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return copy;
	}

	@Override
	public boolean stillValid(Player p) {
		return this.tile.stillValid(p);
	}
}
