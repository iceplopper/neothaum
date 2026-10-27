package dev.thaumvibe.golem;

import dev.thaumvibe.golem.ai.GolemAttackGoal;
import dev.thaumvibe.golem.ai.GolemFillGoal;
import dev.thaumvibe.golem.ai.GolemHarvestCropsGoal;
import dev.thaumvibe.golem.ai.GolemHarvestLogsGoal;
import dev.thaumvibe.golem.ai.GolemItemPickupGoal;
import dev.thaumvibe.golem.ai.GolemReturnHomeGoal;
import dev.thaumvibe.golem.menu.GolemMenu;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of EntityGolemBase:
 * Autonomous animated automaton crafted from various materials.
 * Carries items, follows orders based on installed Golem Core,
 * and accepts upgrades and accessories.
 */
public class GolemEntity extends AbstractGolem implements ExtendedMenuProvider<Integer> {
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> ADVANCED = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> CORE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<String> DECORATION = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BLOCK_POS);
	private static final EntityDataAccessor<Integer> HOME_FACING = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<ItemStack> CARRIED_ITEM = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.ITEM_STACK);

	private List<Integer> upgrades = new ArrayList<>();
	private UUID ownerId;
	private String ownerName = "";
	private int regenTimer = 0;
	private float bootup = 0.0F;
	private boolean paused = false;

	private final NonNullList<ItemStack> filterItems = NonNullList.withSize(36, ItemStack.EMPTY);
	private final byte[] filterColors = new byte[36];
	private final boolean[] toggles = new boolean[4];
	private byte targetFlags = 1;
	private byte sortFlags = 0;

	public GolemEntity(EntityType<? extends GolemEntity> type, Level level) {
		super(type, level);
		Arrays.fill(this.filterColors, (byte) -1);
		this.setGolemType(GolemType.WOOD);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return AbstractGolem.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 20.0)
			.add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ARMOR, 6.0)
			.add(Attributes.ATTACK_DAMAGE, 2.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TYPE, GolemType.WOOD.ordinal());
		builder.define(ADVANCED, false);
		builder.define(CORE, -1);
		builder.define(DECORATION, "");
		builder.define(HOME_POS, BlockPos.ZERO);
		builder.define(HOME_FACING, Direction.UP.get3DDataValue());
		builder.define(CARRIED_ITEM, ItemStack.EMPTY);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
		this.goalSelector.addGoal(1, new GolemAttackGoal(this));
		this.goalSelector.addGoal(2, new GolemItemPickupGoal(this));
		this.goalSelector.addGoal(2, new GolemHarvestCropsGoal(this));
		this.goalSelector.addGoal(2, new GolemHarvestLogsGoal(this));
		this.goalSelector.addGoal(2, new GolemFillGoal(this));
		this.goalSelector.addGoal(5, new GolemReturnHomeGoal(this));
		this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
	}

	public GolemType getGolemType() {
		return GolemType.byId(this.entityData.get(TYPE));
	}

	public void setGolemType(GolemType type) {
		this.entityData.set(TYPE, type.ordinal());
		this.updateStatsForType(type);
	}

	private void updateStatsForType(GolemType type) {
		var hpAttr = this.getAttribute(Attributes.MAX_HEALTH);
		if (hpAttr != null) {
			hpAttr.setBaseValue(type.health);
		}
		var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speedAttr != null) {
			speedAttr.setBaseValue(type.speed);
		}
		var armorAttr = this.getAttribute(Attributes.ARMOR);
		if (armorAttr != null) {
			armorAttr.setBaseValue(type.armor);
		}
		var atkAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
		if (atkAttr != null) {
			atkAttr.setBaseValue(type.strength);
		}
	}

	public boolean isAdvanced() {
		return this.entityData.get(ADVANCED);
	}

	public void setAdvanced(boolean adv) {
		this.entityData.set(ADVANCED, adv);
	}

	public int getCore() {
		return this.entityData.get(CORE);
	}

	public void setCore(int core) {
		this.entityData.set(CORE, core);
	}

	public String getDecoration() {
		return this.entityData.get(DECORATION);
	}

	public void setDecoration(String deco) {
		this.entityData.set(DECORATION, deco != null ? deco : "");
	}

	public boolean hasDecoration(char code) {
		return this.getDecoration().indexOf(code) >= 0;
	}

	public boolean addDecoration(GolemDecorationType deco) {
		String cur = this.getDecoration();
		if (cur.indexOf(deco.code) >= 0) {
			return false;
		}
		// Incompatibilities:
		if (deco.code == 'F' && cur.indexOf('H') >= 0) return false;
		if (deco.code == 'H' && cur.indexOf('F') >= 0) return false;
		if (deco.code == 'G' && cur.indexOf('V') >= 0) return false;
		if (deco.code == 'V' && cur.indexOf('G') >= 0) return false;
		if (deco.code == 'B' && cur.indexOf('P') >= 0) return false;
		if (deco.code == 'P' && cur.indexOf('B') >= 0) return false;

		this.setDecoration(cur + deco.code);
		return true;
	}

	public BlockPos getHomePos() {
		return this.entityData.get(HOME_POS);
	}

	public Direction getHomeFacing() {
		return Direction.from3DDataValue(this.entityData.get(HOME_FACING));
	}

	public void setHomeArea(BlockPos pos, Direction facing) {
		this.entityData.set(HOME_POS, pos.immutable());
		this.entityData.set(HOME_FACING, facing.get3DDataValue());
	}

	public ItemStack getCarriedItem() {
		return this.entityData.get(CARRIED_ITEM);
	}

	public ItemStack getCarried() {
		return this.getCarriedItem();
	}

	public void setCarriedItem(ItemStack stack) {
		this.entityData.set(CARRIED_ITEM, stack.copy());
	}

	public void setCarried(ItemStack stack) {
		this.setCarriedItem(stack);
	}

	public float getBootup() {
		return this.bootup;
	}

	public boolean isInactive() {
		return this.paused;
	}

	public void setInactive(boolean inactive) {
		this.paused = inactive;
	}

	public int getFilterSlotCount() {
		int core = this.getCore();
		if (core == 5) {
			return Math.min(36, 1 + this.getUpgradeCount(GolemUpgradeType.ORDER));
		}
		if (core == 0 || core == 1 || core == 2 || core == 8 || core == 10) {
			return Math.min(36, 6 + this.getUpgradeCount(GolemUpgradeType.ORDER) * 6);
		}
		return 6;
	}

	public ItemStack getFilterItem(int slot) {
		return (slot >= 0 && slot < this.filterItems.size()) ? this.filterItems.get(slot) : ItemStack.EMPTY;
	}

	public void setFilterItem(int slot, ItemStack stack) {
		if (slot >= 0 && slot < this.filterItems.size()) {
			this.filterItems.set(slot, stack.copy());
		}
	}

	public byte getFilterColor(int slot) {
		return (slot >= 0 && slot < this.filterColors.length) ? this.filterColors[slot] : -1;
	}

	public void cycleFilterColor(int slot) {
		if (slot >= 0 && slot < this.filterColors.length) {
			byte c = this.filterColors[slot];
			c++;
			if (c > 15) c = -1;
			this.filterColors[slot] = c;
		}
	}

	public boolean getToggle(int index) {
		return (index >= 0 && index < this.toggles.length) ? this.toggles[index] : false;
	}

	public void setToggle(int index, boolean val) {
		if (index >= 0 && index < this.toggles.length) {
			this.toggles[index] = val;
		}
	}

	public boolean getTargetFlag(int bit) {
		return (this.targetFlags & (1 << bit)) != 0;
	}

	public void toggleTargetFlag(int bit) {
		this.targetFlags ^= (byte) (1 << bit);
	}

	public boolean getSortFlag(int bit) {
		return (this.sortFlags & (1 << bit)) != 0;
	}

	public void toggleSortFlag(int bit) {
		this.sortFlags ^= (byte) (1 << bit);
	}

	public void handleButton(int buttonId, int value) {
		switch (buttonId) {
			case 0 -> this.cycleFilterColor(value);
			case 1 -> this.setToggle(value, !this.getToggle(value));
			case 2 -> this.toggleTargetFlag(value);
			case 3 -> this.toggleSortFlag(value);
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new GolemMenu(id, inv, this);
	}

	@Override
	public Integer getScreenOpeningData(ServerPlayer player) {
		return this.getId();
	}

	public List<Integer> getUpgrades() {
		return this.upgrades;
	}

	public void setUpgrades(List<Integer> ups) {
		this.upgrades = new ArrayList<>(ups != null ? ups : List.of());
	}

	public int getMaxUpgrades() {
		return this.getGolemType().upgrades + (this.isAdvanced() ? 1 : 0);
	}

	public int getUpgradeCount(GolemUpgradeType type) {
		int count = 0;
		for (int u : this.upgrades) {
			if (u == type.id) count++;
		}
		return count;
	}

	public boolean addUpgrade(GolemUpgradeType type) {
		if (this.upgrades.size() >= this.getMaxUpgrades()) {
			return false;
		}
		if (this.getUpgradeCount(type) >= 2) {
			return false;
		}
		this.upgrades.add(type.id);
		return true;
	}

	public int getCarryCapacity() {
		int base = this.getGolemType().carry;
		int earth = this.getUpgradeCount(GolemUpgradeType.EARTH);
		return base * (1 + earth);
	}

	public void setOwner(Player player) {
		this.ownerId = player.getUUID();
		this.ownerName = player.getName().getString();
	}

	public boolean isOwner(Player player) {
		return player != null && (player.getUUID().equals(this.ownerId) || player.getName().getString().equals(this.ownerName));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.bootup < 1.0F) {
			this.bootup += 0.05F;
			if (this.level().isClientSide() && this.tickCount % 4 == 0) {
				this.playSound(TVSounds.CAMERATICKS, this.bootup * 0.2F, this.bootup);
			}
		}

		if (!this.level().isClientSide()) {
			// Regeneration
			GolemType type = this.getGolemType();
			if (this.getHealth() < this.getMaxHealth()) {
				this.regenTimer++;
				if (this.regenTimer >= type.regenDelay) {
					this.heal(1.0F);
					this.regenTimer = 0;
				}
			}

			// Fire resistance for fire-resistant golems
			if (type.fireResist && this.isOnFire()) {
				this.clearFire();
			}
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);

		// If holding Golem Bell:
		if (held.getItem() instanceof GolemBellItem) {
			return InteractionResult.PASS;
		}

		// Cookie healing & speed buff (TC4 feature):
		if (held.is(Items.COOKIE)) {
			if (!player.getAbilities().instabuild) {
				held.shrink(1);
			}
			this.heal(5.0F);
			this.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 0));
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.CAMERACLACK, SoundSource.NEUTRAL, 0.5F, 1.2F);
			if (this.level() instanceof ServerLevel sl) {
				sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 5, 0.2, 0.2, 0.2, 0.0);
			}
			return InteractionResult.SUCCESS;
		}

		// Shears: remove all decorations
		if (held.is(Items.SHEARS) && !this.getDecoration().isEmpty()) {
			if (!this.level().isClientSide()) {
				String dec = this.getDecoration();
				this.setDecoration("");
				for (int i = 0; i < dec.length(); i++) {
					GolemDecorationType dt = GolemDecorationType.byCode(dec.charAt(i));
					if (dt != null) {
						ItemStack drop = GolemPlacerItem.createDecorationStack(dt);
						this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), drop));
					}
				}
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.CAMERACLACK, SoundSource.NEUTRAL, 1.0F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}

		// Core installation:
		if (held.getItem() instanceof GolemCoreItem coreItem) {
			if (coreItem.getCoreType() != GolemCoreType.BLANK) {
				if (!this.level().isClientSide()) {
					int oldCore = this.getCore();
					this.setCore(coreItem.getCoreType().id);
					if (oldCore >= 0) {
						ItemStack oldStack = GolemPlacerItem.createCoreStack(GolemCoreType.byId(oldCore));
						this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), oldStack));
					}
					if (!player.getAbilities().instabuild) {
						held.shrink(1);
					}
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.UPGRADE, SoundSource.NEUTRAL, 0.5F, 1.0F);
				}
				return InteractionResult.SUCCESS;
			}
		}

		// Upgrade installation:
		if (held.getItem() instanceof GolemUpgradeItem upItem) {
			if (this.addUpgrade(upItem.getUpgradeType())) {
				if (!this.level().isClientSide()) {
					if (!player.getAbilities().instabuild) {
						held.shrink(1);
					}
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.UPGRADE, SoundSource.NEUTRAL, 0.5F, 1.0F);
				}
				return InteractionResult.SUCCESS;
			}
		}

		// Decoration installation:
		if (held.getItem() instanceof GolemDecorationItem decoItem) {
			if (this.addDecoration(decoItem.getDecorationType())) {
				if (!this.level().isClientSide()) {
					if (!player.getAbilities().instabuild) {
						held.shrink(1);
					}
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.CAMERACLACK, SoundSource.NEUTRAL, 1.0F, 1.0F);
				}
				return InteractionResult.SUCCESS;
			}
		}

		if (this.getCore() >= 0 && GolemCoreType.byId(this.getCore()).hasGUI) {
			if (!this.level().isClientSide() && player instanceof ServerPlayer sp) {
				sp.openMenu(this);
			}
			return InteractionResult.SUCCESS;
		}

		return super.mobInteract(player, hand);
	}

	public ItemStack toPlacerItem() {
		ItemStack stack = switch (this.getGolemType()) {
			case STRAW -> new ItemStack(TVItems.GOLEM_STRAW);
			case WOOD -> new ItemStack(TVItems.GOLEM_WOOD);
			case TALLOW -> new ItemStack(TVItems.GOLEM_TALLOW);
			case CLAY -> new ItemStack(TVItems.GOLEM_CLAY);
			case FLESH -> new ItemStack(TVItems.GOLEM_FLESH);
			case STONE -> new ItemStack(TVItems.GOLEM_STONE);
			case IRON -> new ItemStack(TVItems.GOLEM_IRON);
			case THAUMIUM -> new ItemStack(TVItems.GOLEM_THAUMIUM);
		};

		if (this.isAdvanced()) {
			stack.set(TVComponents.GOLEM_ADVANCED, true);
		}
		if (this.getCore() >= 0) {
			stack.set(TVComponents.GOLEM_CORE, this.getCore());
		}
		if (!this.upgrades.isEmpty()) {
			stack.set(TVComponents.GOLEM_UPGRADES, new ArrayList<>(this.upgrades));
		}
		if (!this.getDecoration().isEmpty()) {
			stack.set(TVComponents.GOLEM_DECORATION, this.getDecoration());
		}
		if (this.hasCustomName()) {
			stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, this.getCustomName());
		}

		return stack;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.CAMERACLACK;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TVSounds.CAMERACLACK;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.CAMERACLACK;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("GolemType", this.getGolemType().ordinal());
		output.putBoolean("Advanced", this.isAdvanced());
		output.putInt("Core", this.getCore());
		output.putString("Decoration", this.getDecoration());
		output.putInt("HomeX", this.getHomePos().getX());
		output.putInt("HomeY", this.getHomePos().getY());
		output.putInt("HomeZ", this.getHomePos().getZ());
		output.putInt("HomeFacing", this.entityData.get(HOME_FACING));
		output.store("CarriedItem", ItemStack.OPTIONAL_CODEC, this.getCarriedItem());
		output.store("Upgrades", com.mojang.serialization.Codec.INT.listOf(), this.upgrades);
		if (this.ownerId != null) {
			output.putString("OwnerUUID", this.ownerId.toString());
		}
		output.putString("OwnerName", this.ownerName);
		ContainerHelper.saveAllItems(output.child("FilterItems"), this.filterItems);
		int[] colorsInt = new int[this.filterColors.length];
		for (int i = 0; i < this.filterColors.length; i++) colorsInt[i] = this.filterColors[i];
		output.putIntArray("FilterColors", colorsInt);
		output.putByte("TargetFlags", this.targetFlags);
		output.putByte("SortFlags", this.sortFlags);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setGolemType(GolemType.byId(input.getIntOr("GolemType", GolemType.WOOD.ordinal())));
		this.setAdvanced(input.getBooleanOr("Advanced", false));
		this.setCore(input.getIntOr("Core", -1));
		this.setDecoration(input.getStringOr("Decoration", ""));
		int hx = input.getIntOr("HomeX", 0);
		int hy = input.getIntOr("HomeY", 0);
		int hz = input.getIntOr("HomeZ", 0);
		int hf = input.getIntOr("HomeFacing", Direction.UP.get3DDataValue());
		this.setHomeArea(new BlockPos(hx, hy, hz), Direction.from3DDataValue(hf));
		this.setCarriedItem(input.read("CarriedItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
		this.setUpgrades(input.read("Upgrades", com.mojang.serialization.Codec.INT.listOf()).orElse(List.of()));
		input.child("FilterItems").ifPresent(c -> ContainerHelper.loadAllItems(c, this.filterItems));
		input.getIntArray("FilterColors").ifPresent(arr -> {
			for (int i = 0; i < Math.min(arr.length, this.filterColors.length); i++) {
				this.filterColors[i] = (byte) arr[i];
			}
		});
		this.targetFlags = input.getByteOr("TargetFlags", (byte) 1);
		this.sortFlags = input.getByteOr("SortFlags", (byte) 0);
		String uuidStr = input.getStringOr("OwnerUUID", "");
		if (!uuidStr.isEmpty()) {
			try {
				this.ownerId = UUID.fromString(uuidStr);
			} catch (Exception ignored) {
			}
		}
		this.ownerName = input.getStringOr("OwnerName", "");
	}
}
