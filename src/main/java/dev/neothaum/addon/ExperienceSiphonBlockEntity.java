package dev.neothaum.addon;

import dev.neothaum.registry.TVComponents;
import dev.neothaum.tiles.TVBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/** The siphon's store of experience points and the one jar it is filling. */
public class ExperienceSiphonBlockEntity extends TVBlockEntity implements WorldlyContainer {
	public int store;
	public ItemStack jar = ItemStack.EMPTY;
	private int count;

	public ExperienceSiphonBlockEntity(BlockPos pos, BlockState state) {
		super(ExperienceSiphon.SIPHON_ENTITY, pos, state);
	}

	/** Every ten ticks: draws a level's worth (from zero) from each player kneeling on it, then fills its jar if it holds enough. */
	public static void tick(Level level, BlockPos pos, BlockState state, ExperienceSiphonBlockEntity siphon) {
		if (++siphon.count % 10 != 0 || !ExperienceSiphon.enabled() || level.hasNeighborSignal(pos) || !(level instanceof ServerLevel sl)) {
			return;
		}
		boolean changed = false;
		for (Player p : level.getEntitiesOfClass(Player.class, new AABB(pos.getX(), pos.getY() + 0.4, pos.getZ(), pos.getX() + 1, pos.getY() + 1.2,
			pos.getZ() + 1))) {
			int room = ExperienceSiphon.STORE - siphon.store;
			int take = Math.min(Math.min(ExperienceSiphon.DRAW, room), ExperienceSiphon.pointsOf(p));
			if (!p.isShiftKeyDown() || p.isSpectator() || p.getAbilities().instabuild || take <= 0) {
				continue;
			}
			p.giveExperiencePoints(-take);
			siphon.store += take;
			changed = true;
			sl.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.1F, 0.5F + sl.getRandom().nextFloat() * 0.2F);
			sl.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 0.3, p.getZ(), 3, 0.25, 0.3, 0.25, 0.0);
		}
		if (ExperienceSiphon.isEmptyJar(siphon.jar) && siphon.store >= ExperienceSiphon.JAR) {
			siphon.store -= ExperienceSiphon.JAR;
			siphon.jar.set(TVComponents.EXPERIENCE, ExperienceSiphon.JAR);
			sl.playSound(null, pos, dev.neothaum.registry.TVSounds.JAR, SoundSource.BLOCKS, 0.4F, 1.4F);
			changed = true;
		}
		if (changed) {
			siphon.markForUpdate();
			level.updateNeighbourForOutputSignal(pos, state.getBlock());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.store = input.getIntOr("Store", 0);
		this.jar = input.read("Jar", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("Store", this.store);
		if (!this.jar.isEmpty()) {
			output.store("Jar", ItemStack.OPTIONAL_CODEC, this.jar);
		}
	}

	// ------------------------------------------------------------------ the jar slot (hoppers: empty jars in at the sides, full jars out below)

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.jar.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot == 0 ? this.jar : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot != 0 || this.jar.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack out = this.jar.split(amount);
		this.markForUpdate();
		return out;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack out = slot == 0 ? this.jar : ItemStack.EMPTY;
		if (slot == 0) {
			this.jar = ItemStack.EMPTY;
		}
		return out;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0) {
			this.jar = stack;
			if (stack.getCount() > 1) {
				stack.setCount(1);
			}
			this.markForUpdate();
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return this.jar.isEmpty() && ExperienceSiphon.isEmptyJar(stack);
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.jar = ItemStack.EMPTY;
		this.markForUpdate();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return dir != Direction.DOWN && this.canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return dir == Direction.DOWN && ExperienceSiphon.isExperienceJar(stack);
	}
}
