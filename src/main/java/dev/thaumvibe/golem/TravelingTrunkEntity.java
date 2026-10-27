package dev.thaumvibe.golem;

import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class TravelingTrunkEntity extends PathfinderMob implements Container {
	private static final EntityDataAccessor<Boolean> STAY = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> UPGRADE = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.BOOLEAN);

	private NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
	private UUID ownerId;
	private String ownerName = "";
	public float lidAngle = 0.0F;
	public float prevLidAngle = 0.0F;

	public TravelingTrunkEntity(EntityType<? extends TravelingTrunkEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 75.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ARMOR, 8.0)
			.add(Attributes.ATTACK_DAMAGE, 4.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STAY, false);
		builder.define(UPGRADE, -1);
		builder.define(ANGER, 0);
		builder.define(OPEN, false);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new TrunkFollowOwnerGoal(this));
	}

	public boolean isStay() {
		return this.entityData.get(STAY);
	}

	public void setStay(boolean stay) {
		this.entityData.set(STAY, stay);
	}

	public int getUpgrade() {
		return this.entityData.get(UPGRADE);
	}

	public void setUpgrade(int upgrade) {
		this.entityData.set(UPGRADE, upgrade);
	}

	public int getAnger() {
		return this.entityData.get(ANGER);
	}

	public void setAnger(int anger) {
		this.entityData.set(ANGER, anger);
	}

	public boolean isOpen() {
		return this.entityData.get(OPEN);
	}

	public void setOpen(boolean open) {
		this.entityData.set(OPEN, open);
	}

	public UUID getOwnerUUID() {
		return this.ownerId;
	}

	public void setOwner(Player player) {
		if (player != null) {
			this.ownerId = player.getUUID();
			this.ownerName = player.getName().getString();
		}
	}

	public Player getOwner() {
		if (this.ownerId != null) {
			return this.level().getPlayerByUUID(this.ownerId);
		}
		return null;
	}

	public int getSlotCount() {
		return this.getUpgrade() == GolemUpgradeType.EARTH.id ? 36 : 27;
	}

	public NonNullList<ItemStack> getItemsList() {
		return this.items;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.prevLidAngle = this.lidAngle;

		// Lid opening animation
		if (this.isOpen()) {
			this.lidAngle = Math.min(0.5F, this.lidAngle + 0.05F);
		} else if (!this.onGround()) {
			this.lidAngle = Math.min(0.2F, this.lidAngle + 0.02F);
		} else {
			this.lidAngle = Math.max(0.0F, this.lidAngle - 0.05F);
		}

		if (!this.level().isClientSide()) {
			// Regeneration
			if (this.getHealth() < this.getMaxHealth()) {
				int regenRate = this.getUpgrade() == GolemUpgradeType.WATER.id ? 20 : 50;
				if (this.tickCount % regenRate == 0) {
					this.heal(1.0F);
				}
			}

			// Entropy upgrade: vacuum nearby items
			if (this.getUpgrade() == GolemUpgradeType.ENTROPY.id && this.tickCount % 10 == 0) {
				AABB box = this.getBoundingBox().inflate(3.5);
				List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, box, e -> e.isAlive() && !e.hasPickUpDelay());
				for (ItemEntity item : items) {
					ItemStack stack = item.getItem();
					ItemStack remaining = this.insertItem(stack);
					if (remaining.isEmpty()) {
						item.discard();
					} else {
						item.setItem(remaining);
					}
				}
			}

			if (this.getAnger() > 0) {
				this.setAnger(this.getAnger() - 1);
			}
		}
	}

	public ItemStack insertItem(ItemStack stack) {
		ItemStack remaining = stack.copy();
		int max = this.getSlotCount();
		for (int i = 0; i < max; i++) {
			ItemStack in = this.items.get(i);
			if (in.isEmpty()) {
				this.items.set(i, remaining);
				return ItemStack.EMPTY;
			} else if (ItemStack.isSameItemSameComponents(in, remaining)) {
				int space = in.getMaxStackSize() - in.getCount();
				int add = Math.min(space, remaining.getCount());
				if (add > 0) {
					in.grow(add);
					remaining.shrink(add);
					if (remaining.isEmpty()) return ItemStack.EMPTY;
				}
			}
		}
		return remaining;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);

		// If holding Golem Bell:
		if (held.getItem() instanceof GolemBellItem) {
			if (player.isShiftKeyDown()) {
				// Pick up trunk
				if (!this.level().isClientSide()) {
					ItemStack spawner = new ItemStack(TVItems.TRUNK_SPAWNER);
					boolean keepItems = this.getUpgrade() == GolemUpgradeType.ORDER.id;
					if (keepItems) {
						spawner.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items));
					} else {
						// Drop all items
						for (ItemStack s : this.items) {
							if (!s.isEmpty()) {
								this.spawnAtLocation((ServerLevel) this.level(), s);
							}
						}
					}
					if (this.getUpgrade() >= 0) {
						spawner.set(TVComponents.GOLEM_CORE, this.getUpgrade());
					}
					this.discard();
					this.level().playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.ZAP, SoundSource.PLAYERS, 0.5F, 1.0F);
					if (!player.getInventory().add(spawner)) {
						player.spawnAtLocation((ServerLevel) this.level(), spawner);
					}
				}
				return InteractionResult.SUCCESS;
			} else {
				// Toggle stay / follow
				if (!this.level().isClientSide()) {
					boolean newStay = !this.isStay();
					this.setStay(newStay);
					player.sendSystemMessage(Component.translatable(newStay ? "entity.trunk.stay" : "entity.trunk.move"));
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.CAMERACLACK, SoundSource.PLAYERS, 0.5F, 1.2F);
				}
				return InteractionResult.SUCCESS;
			}
		}

		// Install Upgrade
		if (held.getItem() instanceof GolemUpgradeItem upItem && this.getUpgrade() == -1) {
			if (!this.level().isClientSide()) {
				this.setUpgrade(upItem.getUpgradeType().id);
				if (!player.getAbilities().instabuild) {
					held.shrink(1);
				}
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.UPGRADE, SoundSource.NEUTRAL, 0.5F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}

		// Feed food to heal
		if (held.has(net.minecraft.core.component.DataComponents.FOOD) && this.getHealth() < this.getMaxHealth()) {
			if (!this.level().isClientSide()) {
				var food = held.get(net.minecraft.core.component.DataComponents.FOOD);
				this.heal(food.nutrition() * 2.0F);
				if (!player.getAbilities().instabuild) {
					held.shrink(1);
				}
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.5F, 1.0F);
				if (this.level() instanceof ServerLevel sl) {
					sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 4, 0.2, 0.2, 0.2, 0.0);
				}
			}
			return InteractionResult.SUCCESS;
		}

		// Water upgrade check: only owner can open
		if (this.getUpgrade() == GolemUpgradeType.WATER.id && this.ownerId != null && !player.getUUID().equals(this.ownerId)) {
			return InteractionResult.PASS;
		}

		// Open chest container
		if (!this.level().isClientSide() && player instanceof ServerPlayer sp) {
			int rows = this.getSlotCount() / 9;
			MenuType<ChestMenu> menuType = rows == 4 ? MenuType.GENERIC_9x4 : MenuType.GENERIC_9x3;
			sp.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new ChestMenu(menuType, id, inv, this, rows),
				Component.literal(this.ownerName.isEmpty() ? "Traveling Trunk" : this.ownerName + "'s Trunk")
			));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		// Water upgrade makes trunk heavily resistant
		if (this.getUpgrade() == GolemUpgradeType.WATER.id) {
			amount = Math.min(amount, 2.0F);
		}
		return super.hurtServer(level, source, amount);
	}

	// Container interface
	@Override
	public int getContainerSize() {
		return this.getSlotCount();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack s : this.items) {
			if (!s.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return (slot >= 0 && slot < this.items.size()) ? this.items.get(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return ContainerHelper.removeItem(this.items, slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot >= 0 && slot < this.items.size()) {
			this.items.set(slot, stack);
		}
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return this.isAlive() && player.distanceToSqr(this) < 64.0;
	}

	@Override
	public void startOpen(ContainerUser player) {
		this.setOpen(true);
	}

	@Override
	public void stopOpen(ContainerUser player) {
		this.setOpen(false);
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Stay", this.isStay());
		output.putInt("Upgrade", this.getUpgrade());
		if (this.ownerId != null) output.putString("OwnerUUID", this.ownerId.toString());
		output.putString("OwnerName", this.ownerName);
		ContainerHelper.saveAllItems(output.child("Inventory"), this.items);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setStay(input.getBooleanOr("Stay", false));
		this.setUpgrade(input.getIntOr("Upgrade", -1));
		String uuidStr = input.getStringOr("OwnerUUID", "");
		if (!uuidStr.isEmpty()) {
			try { this.ownerId = UUID.fromString(uuidStr); } catch (Exception ignored) {}
		}
		this.ownerName = input.getStringOr("OwnerName", "");
		input.child("Inventory").ifPresent(c -> ContainerHelper.loadAllItems(c, this.items));
	}

	// Follow Owner Goal
	private static class TrunkFollowOwnerGoal extends Goal {
		private final TravelingTrunkEntity trunk;

		TrunkFollowOwnerGoal(TravelingTrunkEntity trunk) {
			this.trunk = trunk;
			this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (this.trunk.isStay()) return false;
			Player owner = this.trunk.getOwner();
			return owner != null && this.trunk.distanceToSqr(owner) > 16.0;
		}

		@Override
		public void tick() {
			Player owner = this.trunk.getOwner();
			if (owner == null) return;

			double distSq = this.trunk.distanceToSqr(owner);
			if (distSq > 400.0) {
				// Teleport near owner if > 20 blocks
				BlockPos pos = owner.blockPosition().offset(trunk.getRandom().nextInt(3) - 1, 0, trunk.getRandom().nextInt(3) - 1);
				this.trunk.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.trunk.getYRot(), this.trunk.getXRot());
			} else {
				double speed = this.trunk.getUpgrade() == GolemUpgradeType.AIR.id ? 1.4 : 1.0;
				this.trunk.getNavigation().moveTo(owner, speed);
			}
		}
	}
}
