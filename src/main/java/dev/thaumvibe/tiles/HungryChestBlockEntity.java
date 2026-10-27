package dev.thaumvibe.tiles;

import dev.thaumvibe.registry.TVBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Port of thaumcraft.common.tiles.TileChestHungry: a 27-slot sentient wooden chest with sharp teeth along its lid
 * that hungrily consumes any items dropped on top or pushed into it.
 */
public class HungryChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
	private static final Component DEFAULT_NAME = Component.translatable("container.thaumvibe.hungry_chest");
	private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
	private final ChestLidController chestLidController = new ChestLidController();
	private final ContainerOpenersCounter openersCounter;
	public float biteAngle = 0.0F;
	public float prevBiteAngle = 0.0F;

	public HungryChestBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.HUNGRY_CHEST, pos, state);
		this.openersCounter = new ContainerOpenersCounter() {
			@Override
			protected void onOpen(Level level, BlockPos pos, BlockState state) {
				level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
			}

			@Override
			protected void onClose(Level level, BlockPos pos, BlockState state) {
				level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
			}

			@Override
			protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
				level.blockEvent(pos, state.getBlock(), 1, newCount);
			}

			@Override
			public boolean isOwnContainer(Player player) {
				if (player.containerMenu instanceof ChestMenu menu) {
					return menu.getContainer() == HungryChestBlockEntity.this;
				}
				return false;
			}
		};
	}

	@Override
	public int getContainerSize() {
		return 27;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	protected Component getDefaultName() {
		return DEFAULT_NAME;
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return ChestMenu.threeRows(id, inventory, this);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(input)) {
			ContainerHelper.loadAllItems(input, this.items);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!this.trySaveLootTable(output)) {
			ContainerHelper.saveAllItems(output, this.items);
		}
	}

	@Override
	public boolean triggerEvent(int id, int data) {
		if (id == 1) {
			this.chestLidController.shouldBeOpen(data > 0);
			return true;
		} else if (id == 2) {
			this.biteAngle = 0.35F;
			return true;
		}
		return super.triggerEvent(id, data);
	}

	@Override
	public void startOpen(ContainerUser user) {
		if (user instanceof LivingEntity living && this.level != null && !this.remove) {
			this.openersCounter.incrementOpeners(living, this.level, this.worldPosition, this.getBlockState(), 4.0);
		}
	}

	@Override
	public void stopOpen(ContainerUser user) {
		if (user instanceof LivingEntity living && this.level != null) {
			this.openersCounter.decrementOpeners(living, this.level, this.worldPosition, this.getBlockState());
		}
	}

	public void recheckOpen() {
		if (this.level != null && !this.remove) {
			this.openersCounter.recheckOpeners(this.level, this.worldPosition, this.getBlockState());
		}
	}

	@Override
	public float getOpenNess(float pt) {
		float lid = this.chestLidController.getOpenness(pt);
		float bite = Mth.lerp(pt, this.prevBiteAngle, this.biteAngle);
		return Math.max(lid, bite);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity be) {
		be.chestLidController.tickLid();
		be.prevBiteAngle = be.biteAngle;
		if (be.biteAngle > 0.0F) {
			be.biteAngle = Math.max(0.0F, be.biteAngle - 0.09F);
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity be) {
		be.openersCounter.recheckOpeners(level, pos, state);
		suckInItems(level, pos, be);
	}

	public static void suckInItems(Level level, BlockPos pos, HungryChestBlockEntity be) {
		AABB box = new AABB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.25, pos.getZ() + 1.0);
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box, EntitySelector.ENTITY_STILL_ALIVE);
		boolean ateAny = false;
		for (ItemEntity item : items) {
			if (eatItem(be, item)) {
				ateAny = true;
			}
		}
		if (ateAny) {
			level.playSound(null, pos, SoundEvents.GENERIC_EAT.value(), SoundSource.BLOCKS, 0.35F,
				(level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
			level.blockEvent(pos, be.getBlockState().getBlock(), 2, 2);
			be.setChanged();
		}
	}

	public static boolean eatItem(HungryChestBlockEntity be, ItemEntity item) {
		if (!item.isAlive()) {
			return false;
		}
		return HopperBlockEntity.addItem(be, item);
	}
}
