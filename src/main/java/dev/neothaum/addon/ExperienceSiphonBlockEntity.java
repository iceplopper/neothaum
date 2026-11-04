package dev.neothaum.addon;

import dev.neothaum.registry.TVComponents;
import dev.neothaum.tiles.TVBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * The siphon's store of experience points and its two slots: empty jars in (slot 0), full jars out (slot 1). While it holds a jar's worth,
 * has an empty jar waiting and room for a full one, it fills the jar over {@link #FILL} ticks and moves it to the output.
 */
public class ExperienceSiphonBlockEntity extends TVBlockEntity implements WorldlyContainer,
	net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPos> {
	/** Ticks to fill a jar. */
	public static final int FILL = 40;
	public static final int IN = 0;
	public static final int OUT = 1;
	public int store;
	public int progress;
	public final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
	private int count;
	/** The store and the fill progress, for the screen. */
	public final net.minecraft.world.inventory.ContainerData data = new net.minecraft.world.inventory.ContainerData() {
		@Override
		public int get(int i) {
			return i == 0 ? ExperienceSiphonBlockEntity.this.store : i == 1 ? ExperienceSiphonBlockEntity.this.progress : 0;
		}

		@Override
		public void set(int i, int value) {
			if (i == 0) {
				ExperienceSiphonBlockEntity.this.store = value;
			} else if (i == 1) {
				ExperienceSiphonBlockEntity.this.progress = value;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public ExperienceSiphonBlockEntity(BlockPos pos, BlockState state) {
		super(ExperienceSiphon.SIPHON_ENTITY, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ExperienceSiphonBlockEntity siphon) {
		if (!ExperienceSiphon.enabled() || level.hasNeighborSignal(pos) || !(level instanceof ServerLevel sl)) {
			return;
		}
		boolean changed = false;
		// every ten ticks: a level's worth (from zero) from each player kneeling on it
		if (++siphon.count % 10 == 0) {
			for (Player p : level.getEntitiesOfClass(Player.class, new AABB(pos.getX(), pos.getY() + 0.4, pos.getZ(), pos.getX() + 1,
				pos.getY() + 1.2, pos.getZ() + 1))) {
				int take = Math.min(Math.min(ExperienceSiphon.DRAW, ExperienceSiphon.STORE - siphon.store), ExperienceSiphon.pointsOf(p));
				if (!p.isShiftKeyDown() || p.isSpectator() || p.getAbilities().instabuild || take <= 0) {
					continue;
				}
				p.giveExperiencePoints(-take);
				siphon.store += take;
				changed = true;
				sl.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.1F, 0.5F + sl.getRandom().nextFloat() * 0.2F);
				sl.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 0.3, p.getZ(), 3, 0.25, 0.3, 0.25, 0.0);
			}
		}
		// filling: an empty jar in, a jar's worth stored, and room for it out
		if (siphon.canFill()) {
			if (++siphon.progress >= FILL) {
				siphon.progress = 0;
				siphon.store -= ExperienceSiphon.JAR;
				ItemStack full = siphon.items.get(IN).split(1);
				full.set(TVComponents.EXPERIENCE, ExperienceSiphon.JAR);
				ItemStack out = siphon.items.get(OUT);
				if (out.isEmpty()) {
					siphon.items.set(OUT, full);
				} else {
					out.grow(1);
				}
				sl.playSound(null, pos, dev.neothaum.registry.TVSounds.JAR, SoundSource.BLOCKS, 0.4F, 1.4F);
				changed = true;
			}
		} else if (siphon.progress > 0) {
			siphon.progress = 0;
		}
		if (changed) {
			siphon.markForUpdate();
			level.updateNeighbourForOutputSignal(pos, state.getBlock());
		}
		// the orb on its top shows how full the store is
		int fill = ExperienceSiphonBlock.fillFor(siphon.store);
		if (state.hasProperty(ExperienceSiphonBlock.FILL) && state.getValue(ExperienceSiphonBlock.FILL) != fill) {
			level.setBlock(pos, state.setValue(ExperienceSiphonBlock.FILL, fill), 3);
		}
	}

	private boolean canFill() {
		ItemStack in = this.items.get(IN);
		ItemStack out = this.items.get(OUT);
		if (!ExperienceSiphon.isEmptyJar(in) || this.store < ExperienceSiphon.JAR) {
			return false;
		}
		if (out.isEmpty()) {
			return true;
		}
		ItemStack full = in.copyWithCount(1);
		full.set(TVComponents.EXPERIENCE, ExperienceSiphon.JAR);
		return ItemStack.isSameItemSameComponents(out, full) && out.getCount() < out.getMaxStackSize();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.store = input.getIntOr("Store", 0);
		this.progress = input.getIntOr("Progress", 0);
		this.items.clear();
		ContainerHelper.loadAllItems(input, this.items);
		// a siphon from before it had two slots kept its one jar under "Jar"
		input.read("Jar", ItemStack.OPTIONAL_CODEC).ifPresent(j -> this.items.set(ExperienceSiphon.isExperienceJar(j) ? OUT : IN, j));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("Store", this.store);
		output.putInt("Progress", this.progress);
		ContainerHelper.saveAllItems(output, this.items);
	}

	@Override
	public net.minecraft.network.chat.Component getDisplayName() {
		return net.minecraft.network.chat.Component.translatable("block.neothaum.experience_siphon");
	}

	@Override
	public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, Player player) {
		return new ExperienceSiphonMenu(id, inv, this, this.data);
	}

	@Override
	public BlockPos getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
		return this.worldPosition;
	}

	// ------------------------------------------------------------------ the slots (hoppers: empty jars in above and at the sides, full jars out below)

	@Override
	public int getContainerSize() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return this.items.get(IN).isEmpty() && this.items.get(OUT).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot >= 0 && slot < 2 ? this.items.get(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack out = ContainerHelper.removeItem(this.items, slot, amount);
		if (!out.isEmpty()) {
			this.markForUpdate();
		}
		return out;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot >= 0 && slot < 2) {
			this.items.set(slot, stack);
			if (stack.getCount() > this.getMaxStackSize(stack)) {
				stack.setCount(this.getMaxStackSize(stack));
			}
			this.markForUpdate();
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot == IN && ExperienceSiphon.isEmptyJar(stack);
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.markForUpdate();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.DOWN ? new int[]{OUT} : new int[]{IN};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return dir != Direction.DOWN && this.canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return dir == Direction.DOWN && slot == OUT;
	}
}
