package dev.neothaum.menu;

import dev.neothaum.items.HandMirrorItem;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVMenus;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.MagicMirrorBlockEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * ContainerHandMirror: 1 transport slot at (80, 24).
 * Items placed or shift-clicked in are instantly teleported to the linked Magic Mirror.
 */
public class HandMirrorMenu extends AbstractContainerMenu {
	private final Container input = new SimpleContainer(1);
	private final Player player;
	private final int lockedSlot;

	public HandMirrorMenu(int id, Inventory inv) {
		this(id, inv, inv.getSelectedSlot());
	}

	public HandMirrorMenu(int id, Inventory inv, int lockedSlot) {
		super(TVMenus.HAND_MIRROR, id);
		this.player = inv.player;
		this.lockedSlot = lockedSlot;

		// Transport slot 0 at (80, 24)
		this.addSlot(new Slot(this.input, 0, 80, 24) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return !(stack.getItem() instanceof HandMirrorItem);
			}

			@Override
			public void setByPlayer(ItemStack stack) {
				super.setByPlayer(stack);
				onItemInserted();
			}

			@Override
			public void set(ItemStack stack) {
				super.set(stack);
				onItemInserted();
			}
		});

		// Player inventory (3 rows of 9)
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		// Player hotbar (9 slots)
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
		}
	}

	public int lockedHotbarSlot() {
		return this.lockedSlot;
	}

	private ItemStack findMirror() {
		if (this.lockedSlot >= 0 && this.lockedSlot < 9) {
			ItemStack s = this.player.getInventory().getItem(this.lockedSlot);
			if (s.getItem() instanceof HandMirrorItem) return s;
		}
		ItemStack main = this.player.getMainHandItem();
		if (main.getItem() instanceof HandMirrorItem) return main;
		ItemStack off = this.player.getOffhandItem();
		if (off.getItem() instanceof HandMirrorItem) return off;
		return ItemStack.EMPTY;
	}

	private void onItemInserted() {
		if (this.player.level().isClientSide()) return;
		ItemStack in = this.input.getItem(0);
		if (in.isEmpty()) return;

		ItemStack mirror = this.findMirror();
		if (transport(mirror, in)) {
			this.input.setItem(0, ItemStack.EMPTY);
		}
	}

	public boolean transport(ItemStack mirror, ItemStack item) {
		if (mirror.isEmpty() || item.isEmpty() || !(this.player.level() instanceof ServerLevel level)) return false;
		TVComponents.MirrorLink link = mirror.get(TVComponents.MIRROR_LINK);
		if (link == null) return false;

		ServerLevel targetLevel = level.getServer().getLevel(link.levelKey());
		if (targetLevel == null || !targetLevel.isLoaded(link.pos())) return false;

		if (targetLevel.getBlockEntity(link.pos()) instanceof MagicMirrorBlockEntity targetMirror) {
			if (targetMirror.isLinkValid()) {
				targetMirror.addStack(item.copy());
				level.playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), TVSounds.JAR, SoundSource.PLAYERS, 0.8F, 2.0F);
				level.sendParticles(ParticleTypes.PORTAL, this.player.getX(), this.player.getY() + 1.0, this.player.getZ(), 16, 0.2, 0.2, 0.2, 0.5);
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack quickMoveStack(Player p, int slotIndex) {
		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		if (stack.getItem() instanceof HandMirrorItem) return ItemStack.EMPTY;

		ItemStack copy = stack.copy();
		if (slotIndex == 0) {
			if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else {
			// Transport directly from inventory via shift-click!
			ItemStack mirror = this.findMirror();
			if (transport(mirror, stack)) {
				slot.setByPlayer(ItemStack.EMPTY);
				return copy;
			}
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return copy;
	}

	@Override
	public void clicked(int slotId, int button, ContainerInput inputType, Player p) {
		if (this.lockedSlot >= 0 && slotId == this.lockedSlot + 28) {
			return;
		}
		super.clicked(slotId, button, inputType, p);
		onItemInserted();
	}

	@Override
	public boolean stillValid(Player p) {
		return !this.findMirror().isEmpty();
	}

	@Override
	public void removed(Player p) {
		super.removed(p);
		if (!p.level().isClientSide()) {
			ItemStack leftover = this.input.getItem(0);
			if (!leftover.isEmpty()) {
				if (!p.getInventory().add(leftover)) {
					p.drop(leftover, false, net.minecraft.util.Prediction.SERVER_ONLY);
				}
			}
		}
	}
}
