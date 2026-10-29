package dev.neothaum.golem;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.golem.ai.AvoidCreeperSwellGoal;
import dev.neothaum.golem.ai.DartAttackGoal;
import dev.neothaum.golem.ai.EmptyDropGoal;
import dev.neothaum.golem.ai.EmptyGotoGoal;
import dev.neothaum.golem.ai.EmptyPlaceGoal;
import dev.neothaum.golem.ai.EssentiaEmptyGoal;
import dev.neothaum.golem.ai.EssentiaGatherGoal;
import dev.neothaum.golem.ai.EssentiaGotoGoal;
import dev.neothaum.golem.ai.FillGotoGoal;
import dev.neothaum.golem.ai.FillTakeGoal;
import dev.neothaum.golem.ai.FishGoal;
import dev.neothaum.golem.ai.GolemAttackOnCollideGoal;
import dev.neothaum.golem.ai.GolemHurtByTargetGoal;
import dev.neothaum.golem.ai.HarvestCropsGoal;
import dev.neothaum.golem.ai.HarvestLogsGoal;
import dev.neothaum.golem.ai.HomeDropGoal;
import dev.neothaum.golem.ai.HomePlaceGoal;
import dev.neothaum.golem.ai.HomeReplaceGoal;
import dev.neothaum.golem.ai.HomeTakeGoal;
import dev.neothaum.golem.ai.HomeTakeSortingGoal;
import dev.neothaum.golem.ai.ItemPickupGoal;
import dev.neothaum.golem.ai.LiquidEmptyGoal;
import dev.neothaum.golem.ai.LiquidGatherGoal;
import dev.neothaum.golem.ai.LiquidGotoGoal;
import dev.neothaum.golem.ai.NearestAttackableTargetGoal;
import dev.neothaum.golem.ai.NearestButcherTargetGoal;
import dev.neothaum.golem.ai.OpenGateGoal;
import dev.neothaum.golem.ai.ReturnHomeGoal;
import dev.neothaum.golem.ai.SortingGotoGoal;
import dev.neothaum.golem.ai.SortingPlaceGoal;
import dev.neothaum.golem.ai.UseItemGoal;
import dev.neothaum.golem.menu.GolemMenu;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityGolemBase: a golem of straw, wood, tallow, clay, flesh, stone, iron or thaumium (and the copper golem add-on). Its core decides what it
 * does: setupGolem gives it that core's tasks, and setupGolemInventory sizes its filter (six slots plus six per fire upgrade; one plus one per
 * fire upgrade for a liquid core; none for harvest, guard and essentia cores). It works within range of its home, the block it was placed on,
 * whose facing side points at its home container. The eight toggles are TC4's: 0 the core's own switch (fill: any amount; use: empty space),
 * 1-4 don't attack monsters, animals, players, creepers (use core: 1 left click, 2 sneak), 5 ore dictionary, 6 ignore damage, 7 ignore NBT.
 */
public class GolemEntity extends AbstractGolem implements ExtendedMenuProvider<Integer>, ContainerUser {
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> ADVANCED = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> CORE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<String> DECORATION = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BLOCK_POS);
	private static final EntityDataAccessor<Integer> HOME_FACING = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<ItemStack> CARRIED_ITEM = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Byte> TOGGLES = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<String> COLORS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> UPGRADES = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> SLOTS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> OXIDATION = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> WAXED_TICKS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> PERM_WAXED = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> CHARGED_TICKS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);

	/** The filter ("inventory"): ghost stacks the golem looks for, and a marker colour for each. */
	private final NonNullList<ItemStack> filterItems = NonNullList.withSize(36, ItemStack.EMPTY);
	private final byte[] colors = new byte[36];
	private List<Integer> upgrades = new ArrayList<>();
	private List<GolemMarker> markers = new ArrayList<>();
	public ItemStack itemCarried = ItemStack.EMPTY;
	public @Nullable Fluid fluidCarried;
	public int fluidAmount;
	public @Nullable Aspect essentia;
	public int essentiaAmount;
	/** What a fill or liquid golem is out fetching. */
	public ItemStack itemWatched = ItemStack.EMPTY;
	private @Nullable UUID ownerId;
	private String ownerName = "";
	private int regenTimer;
	public boolean paused;
	public boolean inactive;
	public int action;
	public int leftArm;
	public int rightArm;
	public int healing;
	public float bootup = -1.0F;
	private @Nullable BlockPos openContainer;
	private double lastSpeed = -1.0;

	public GolemEntity(EntityType<? extends GolemEntity> type, Level level) {
		super(type, level);
		Arrays.fill(this.colors, (byte) -1);
		this.setPersistenceRequired();
		if (this.getNavigation() instanceof GroundPathNavigation nav) {
			nav.setCanOpenDoors(true);
		}
		this.setGolemType(GolemType.WOOD);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return AbstractGolem.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ARMOR, 6.0)
			.add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.STEP_HEIGHT, 1.0);
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
		builder.define(TOGGLES, (byte) 0);
		builder.define(COLORS, "");
		builder.define(UPGRADES, "");
		builder.define(SLOTS, 0);
		builder.define(OXIDATION, 0);
		builder.define(WAXED_TICKS, 0);
		builder.define(PERM_WAXED, false);
		builder.define(CHARGED_TICKS, 0);
	}

	/** The tasks come from the core (setupGolem). */
	@Override
	protected void registerGoals() {
	}

	// ------------------------------------------------------------------ setup

	/** setupGolem: stats from type, decorations and upgrades, and the core's tasks. */
	public void setupGolem() {
		GolemType type = this.getGolemType();
		boolean heavy = type == GolemType.STONE || type == GolemType.IRON || type == GolemType.THAUMIUM;
		this.setPathfindingMalus(PathType.WATER, heavy ? -1.0F : 8.0F);
		this.setAttr(Attributes.MAX_HEALTH, type.health + (this.hasDecoration('H') ? 5 : 0));
		this.setAttr(Attributes.ATTACK_DAMAGE, 2 + this.getGolemStrength() + this.getUpgradeAmount(1) + (this.hasDecoration('M') ? 2 : 0));
		this.setAttr(Attributes.ARMOR, Math.min(20, type.armor + (this.hasDecoration('V') ? 1 : 0) + (this.hasDecoration('P') ? 4 : 0)));
		this.lastSpeed = -1.0;
		if (this.level().isClientSide()) {
			return;
		}
		this.goalSelector.removeAllGoals(g -> true);
		this.targetSelector.removeAllGoals(g -> true);
		int core = this.getCore();
		if (core > -1) {
			this.goalSelector.addGoal(0, new AvoidCreeperSwellGoal(this));
		}
		switch (core) {
			case 0 -> {
				this.goalSelector.addGoal(0, new HomeReplaceGoal(this));
				this.goalSelector.addGoal(1, new HomePlaceGoal(this));
				this.goalSelector.addGoal(2, new HomeDropGoal(this));
				this.goalSelector.addGoal(3, new FillTakeGoal(this));
				this.goalSelector.addGoal(4, new FillGotoGoal(this));
			}
			case 1 -> {
				this.goalSelector.addGoal(0, new HomeReplaceGoal(this));
				this.goalSelector.addGoal(1, new EmptyPlaceGoal(this));
				this.goalSelector.addGoal(2, new EmptyDropGoal(this));
				this.goalSelector.addGoal(3, new EmptyGotoGoal(this));
				this.goalSelector.addGoal(4, new HomeTakeGoal(this));
			}
			case 2 -> {
				this.goalSelector.addGoal(0, new HomeReplaceGoal(this));
				this.goalSelector.addGoal(1, new HomePlaceGoal(this));
				this.goalSelector.addGoal(2, new ItemPickupGoal(this));
			}
			case 3 -> this.goalSelector.addGoal(2, new HarvestCropsGoal(this));
			case 4 -> {
				if (this.hasDecoration('R')) {
					this.goalSelector.addGoal(2, new DartAttackGoal(this));
				}
				this.goalSelector.addGoal(3, new GolemAttackOnCollideGoal(this));
				this.targetSelector.addGoal(1, new GolemHurtByTargetGoal(this));
				this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this));
			}
			case 5 -> {
				this.goalSelector.addGoal(1, new LiquidEmptyGoal(this));
				this.goalSelector.addGoal(2, new LiquidGatherGoal(this));
				this.goalSelector.addGoal(3, new LiquidGotoGoal(this));
			}
			case 6 -> {
				this.goalSelector.addGoal(1, new EssentiaEmptyGoal(this));
				this.goalSelector.addGoal(2, new EssentiaGatherGoal(this));
				this.goalSelector.addGoal(3, new EssentiaGotoGoal(this));
			}
			case 7 -> this.goalSelector.addGoal(2, new HarvestLogsGoal(this));
			case 8 -> {
				this.goalSelector.addGoal(0, new HomeReplaceGoal(this));
				this.goalSelector.addGoal(0, new UseItemGoal(this));
				this.goalSelector.addGoal(4, new HomeTakeGoal(this));
			}
			case 9 -> {
				if (this.hasDecoration('R')) {
					this.goalSelector.addGoal(2, new DartAttackGoal(this));
				}
				this.goalSelector.addGoal(3, new GolemAttackOnCollideGoal(this));
				this.targetSelector.addGoal(1, new NearestButcherTargetGoal(this));
			}
			case 10 -> {
				this.goalSelector.addGoal(0, new HomeReplaceGoal(this));
				this.goalSelector.addGoal(1, new SortingPlaceGoal(this));
				this.goalSelector.addGoal(3, new SortingGotoGoal(this));
				this.goalSelector.addGoal(4, new HomeTakeSortingGoal(this));
			}
			case 11 -> this.goalSelector.addGoal(2, new FishGoal(this));
			default -> {
			}
		}
		if (core > -1) {
			this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
			this.goalSelector.addGoal(5, new OpenGateGoal(this));
			this.goalSelector.addGoal(6, new ReturnHomeGoal(this));
			this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
			this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		}
	}

	private void setAttr(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double value) {
		var a = this.getAttribute(attr);
		if (a != null) {
			a.setBaseValue(value);
		}
	}

	/** setupGolemInventory: how many filter slots the core and fire upgrades give. */
	public void setupGolemInventory() {
		int core = this.getCore();
		int size = 0;
		if (core > -1 && GolemCoreType.byId(core).hasInventory) {
			size = core == 5 ? 1 + this.getUpgradeAmount(2) : 6 + this.getUpgradeAmount(2) * 6;
		}
		this.entityData.set(SLOTS, size);
		for (int a = size; a < this.filterItems.size(); a++) {
			this.filterItems.set(a, ItemStack.EMPTY);
		}
		this.syncColors();
	}

	// ------------------------------------------------------------------ type, core, decoration

	public GolemType getGolemType() {
		return GolemType.byId(this.entityData.get(TYPE));
	}

	public void setGolemType(GolemType type) {
		this.entityData.set(TYPE, type.ordinal());
		this.setupGolem();
		this.setHealth(this.getMaxHealth());
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
		this.setupGolem();
		this.setupGolemInventory();
	}

	public String getDecoration() {
		return this.entityData.get(DECORATION);
	}

	public void setDecoration(String deco) {
		this.entityData.set(DECORATION, deco != null ? deco : "");
		this.setupGolem();
	}

	public boolean hasDecoration(char code) {
		return this.getDecoration().indexOf(code) >= 0;
	}

	/** addDecoration: one of each, and not a fez with a top hat, glasses with a visor, or a bowtie with armour. */
	public boolean addDecoration(GolemDecorationType deco) {
		String cur = this.getDecoration();
		char c = deco.code;
		if (cur.indexOf(c) >= 0 || (c == 'F' || c == 'H') && (cur.indexOf('F') >= 0 || cur.indexOf('H') >= 0)
			|| (c == 'G' || c == 'V') && (cur.indexOf('G') >= 0 || cur.indexOf('V') >= 0)
			|| (c == 'B' || c == 'P') && (cur.indexOf('B') >= 0 || cur.indexOf('P') >= 0)) {
			return false;
		}
		this.setDecoration(cur + c);
		return true;
	}

	// ------------------------------------------------------------------ home

	public BlockPos getHomePos() {
		return this.entityData.get(HOME_POS);
	}

	public Direction getHomeFacing() {
		return Direction.from3DDataValue(this.entityData.get(HOME_FACING));
	}

	public int getHomeFacingId() {
		return this.entityData.get(HOME_FACING);
	}

	public void setHomeArea(BlockPos pos, Direction facing) {
		this.entityData.set(HOME_POS, pos.immutable());
		this.entityData.set(HOME_FACING, facing.get3DDataValue());
		this.setHomeTo(pos, 32);
	}

	/** The home container: the block the golem was placed against (home minus its facing). */
	public BlockPos getHomeContainerPos() {
		return this.getHomePos().relative(this.getHomeFacing().getOpposite());
	}

	public double distToHomeSqr() {
		BlockPos h = this.getHomePos();
		return this.distanceToSqr(h.getX() + 0.5, h.getY() + 0.5, h.getZ() + 0.5);
	}

	/** getRange: 16, four more per water upgrade, a tenth more with glasses and a fifth more if advanced. */
	public float getRange() {
		float dmod = 16 + this.getUpgradeAmount(3) * 4;
		if (this.hasDecoration('G')) {
			dmod += Math.max(dmod * 0.1F, 1.0F);
		}
		if (this.isAdvanced()) {
			dmod += Math.max(dmod * 0.2F, 2.0F);
		}
		return dmod;
	}

	public boolean isWithinHomeRange(BlockPos pos) {
		float dmod = this.getRange();
		return this.getHomePos().distSqr(pos) < dmod * dmod;
	}

	// ------------------------------------------------------------------ carrying

	public int getCarryLimit() {
		int base = this.getGolemType().carry;
		return base + Math.min(16, Math.max(4, base)) * this.getUpgradeAmount(1);
	}

	/** In millibuckets. */
	public int getFluidCarryLimit() {
		return (int) Math.floor(Math.sqrt(this.getCarryLimit())) * 1000;
	}

	public ItemStack getCarried() {
		if (!this.itemCarried.isEmpty() && this.itemCarried.getCount() <= 0) {
			this.setCarried(ItemStack.EMPTY);
		}
		return this.itemCarried;
	}

	public void setCarried(ItemStack stack) {
		this.itemCarried = stack == null ? ItemStack.EMPTY : stack;
		this.updateCarried();
	}

	public int getCarrySpace() {
		return this.itemCarried.isEmpty() ? this.getCarryLimit()
			: Math.min(this.getCarryLimit() - this.itemCarried.getCount(), this.itemCarried.getMaxStackSize() - this.itemCarried.getCount());
	}

	/** What the golem holds up for everyone to see: the item, a bucket of its liquid, or a jar of its essentia. */
	public void updateCarried() {
		ItemStack show = ItemStack.EMPTY;
		if (!this.itemCarried.isEmpty()) {
			show = this.itemCarried.copy();
		} else if (this.getCore() == 5 && this.fluidCarried != null && this.fluidAmount > 0) {
			show = new ItemStack(this.fluidCarried.getBucket());
		} else if (this.getCore() == 6) {
			show = new ItemStack(dev.neothaum.registry.TVBlocks.WARDED_JAR);
			int amt = (int) (64.0F * ((float) this.essentiaAmount / this.getCarryLimit()));
			if (this.essentia != null && this.essentiaAmount > 0) {
				show.set(TVComponents.ASPECTS, new AspectList().add(this.essentia, Math.max(1, amt)));
			}
		}
		this.entityData.set(CARRIED_ITEM, show);
	}

	/** For the renderer. */
	public ItemStack getCarriedItem() {
		return this.entityData.get(CARRIED_ITEM);
	}

	public boolean hasSomething() {
		for (int a = 0; a < this.getInventorySize(); a++) {
			if (!this.filterItems.get(a).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------------ filter

	public int getInventorySize() {
		return this.entityData.get(SLOTS);
	}

	public int getFilterSlotCount() {
		return this.getInventorySize();
	}

	public ItemStack getFilterItem(int slot) {
		return slot >= 0 && slot < this.filterItems.size() ? this.filterItems.get(slot) : ItemStack.EMPTY;
	}

	public void setFilterItem(int slot, ItemStack stack) {
		if (slot >= 0 && slot < this.filterItems.size()) {
			this.filterItems.set(slot, stack.copy());
		}
	}

	public byte getFilterColor(int slot) {
		String s = this.entityData.get(COLORS);
		if (slot < 0 || slot >= s.length() || s.charAt(slot) == 'h') {
			return -1;
		}
		return (byte) Character.digit(s.charAt(slot), 16);
	}

	public void setFilterColor(int slot, int color) {
		if (slot >= 0 && slot < this.colors.length) {
			this.colors[slot] = (byte) color;
			this.syncColors();
		}
	}

	public void cycleFilterColor(int slot) {
		if (slot >= 0 && slot < this.colors.length) {
			int c = this.colors[slot] + 1;
			this.setFilterColor(slot, c > 15 ? -1 : c);
		}
	}

	private void syncColors() {
		StringBuilder s = new StringBuilder();
		for (int a = 0; a < this.getInventorySize(); a++) {
			s.append(this.colors[a] < 0 ? "h" : Integer.toHexString(this.colors[a]));
		}
		this.entityData.set(COLORS, s.toString());
	}

	/** getColorsMatching: the colours of the filter slots holding this item (all of them when the filter is empty). */
	public List<Byte> getColorsMatching(ItemStack match) {
		List<Byte> l = new ArrayList<>();
		int size = this.getInventorySize();
		if (size > 0) {
			boolean allNull = true;
			for (int a = 0; a < size; a++) {
				ItemStack f = this.filterItems.get(a);
				if (!f.isEmpty()) {
					allNull = false;
				}
				if (GolemInventory.areItemStacksEqual(f, match, this.checkOreDict(), this.ignoreDamage(), this.ignoreNBT())) {
					l.add(this.colors[a]);
				}
			}
			if (allNull) {
				for (int a = 0; a < size; a++) {
					l.add(this.colors[a]);
				}
			}
		}
		return l;
	}

	/** InventoryMob.getAmountNeededSmart: how many of this item the filter asks for. */
	public int getAmountNeededSmart(ItemStack stack, boolean fuzzy) {
		int amt = 0;
		for (int a = 0; a < this.getInventorySize(); a++) {
			ItemStack f = this.filterItems.get(a);
			if (f.isEmpty()) {
				continue;
			}
			if (fuzzy) {
				if (ItemStack.isSameItem(f, stack) || GolemInventory.shareOreTag(f, stack)) {
					amt += f.getCount();
				}
			} else if (ItemStack.isSameItemSameComponents(f, stack)) {
				amt += f.getCount();
			}
		}
		return amt;
	}

	/** InventoryMob.getItemsNeeded: the filter's stacks (with their ore dictionary relatives when fuzzy). */
	public List<ItemStack> getItemsNeeded(boolean fuzzy) {
		List<ItemStack> needed = new ArrayList<>();
		for (int a = 0; a < this.getInventorySize(); a++) {
			ItemStack f = this.filterItems.get(a);
			if (!f.isEmpty()) {
				if (fuzzy) {
					needed.addAll(GolemInventory.oreVariants(f));
				} else {
					needed.add(f.copy());
				}
			}
		}
		return needed;
	}

	public boolean allEmpty() {
		return !this.hasSomething();
	}

	// ------------------------------------------------------------------ toggles

	public boolean getToggle(int index) {
		return index >= 0 && index < 8 && (this.entityData.get(TOGGLES) & 1 << index) != 0;
	}

	public void setToggle(int index, boolean val) {
		byte t = this.entityData.get(TOGGLES);
		this.entityData.set(TOGGLES, (byte) (val ? t | 1 << index : t & ~(1 << index)));
	}

	public boolean canAttackHostiles() {
		return !this.getToggle(1);
	}

	public boolean canAttackAnimals() {
		return !this.getToggle(2);
	}

	public boolean canAttackPlayers() {
		return !this.getToggle(3);
	}

	public boolean canAttackCreepers() {
		return !this.getToggle(4);
	}

	public boolean checkOreDict() {
		return this.getToggle(5);
	}

	public boolean ignoreDamage() {
		return this.getToggle(6);
	}

	public boolean ignoreNBT() {
		return this.getToggle(7);
	}

	/** The guard core's four targets, in the GUI's order: monsters, animals, players, creepers. */
	public boolean getTargetFlag(int bit) {
		return !this.getToggle(bit + 1);
	}

	/** The entropy upgrade's three matching switches: ore dictionary, ignore damage, ignore NBT. */
	public boolean getSortFlag(int bit) {
		return this.getToggle(bit + 5);
	}

	/** GUI buttons: 0 next colour for a slot, 4 previous colour, 1 flip toggle n, 2 flip a guard target, 3 flip a matching switch. */
	public void handleButton(int buttonId, int value) {
		switch (buttonId) {
			case 0 -> this.cycleFilterColor(value);
			case 1 -> this.setToggle(value, !this.getToggle(value));
			case 2 -> this.setToggle(value + 1, !this.getToggle(value + 1));
			case 3 -> this.setToggle(value + 5, !this.getToggle(value + 5));
			case 4 -> {
				int c = this.getFilterColor(value) - 1;
				this.setFilterColor(value, c < -1 ? 15 : c);
			}
			default -> {
			}
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

	// ------------------------------------------------------------------ upgrades

	public List<Integer> getUpgrades() {
		return this.upgrades;
	}

	public void setUpgrades(List<Integer> ups) {
		this.upgrades = new ArrayList<>(ups != null ? ups : List.of());
		while (this.upgrades.size() > this.getMaxUpgrades()) {
			this.upgrades.remove(this.upgrades.size() - 1);
		}
		this.syncUpgrades();
		this.setupGolem();
		this.setupGolemInventory();
	}

	private void syncUpgrades() {
		StringBuilder s = new StringBuilder();
		for (int u : this.upgrades) {
			s.append(Integer.toHexString(u));
		}
		this.entityData.set(UPGRADES, s.toString());
	}

	public int getMaxUpgrades() {
		return this.getGolemType().upgrades + (this.isAdvanced() ? 1 : 0);
	}

	/** getUpgradeAmount: 0 air, 1 earth, 2 fire, 3 water, 4 order, 5 entropy. */
	public int getUpgradeAmount(int type) {
		String s = this.entityData.get(UPGRADES);
		int a = 0;
		for (int i = 0; i < s.length(); i++) {
			if (Character.digit(s.charAt(i), 16) == type) {
				a++;
			}
		}
		return a;
	}

	public int getUpgradeCount(GolemUpgradeType type) {
		return this.getUpgradeAmount(type.id);
	}

	public boolean addUpgrade(GolemUpgradeType type) {
		if (this.upgrades.size() >= this.getMaxUpgrades() || this.getUpgradeAmount(type.id) >= 2) {
			return false;
		}
		this.upgrades.add(type.id);
		this.syncUpgrades();
		this.setupGolem();
		this.setupGolemInventory();
		return true;
	}

	public int getGolemStrength() {
		return this.getGolemType().strength + this.getUpgradeAmount(1);
	}

	// ------------------------------------------------------------------ owner, markers

	public void setOwner(Player player) {
		this.ownerId = player.getUUID();
		this.ownerName = player.getName().getString();
	}

	public String getOwnerName() {
		return this.ownerName;
	}

	public boolean isOwner(Player player) {
		return player != null && (player.getUUID().equals(this.ownerId) || player.getName().getString().equals(this.ownerName));
	}

	public @Nullable Player getOwner() {
		return this.ownerId == null ? null : this.level().getPlayerByUUID(this.ownerId);
	}

	/** getMarkers: those in this dimension. */
	public List<GolemMarker> getMarkers() {
		this.markers.removeIf(m -> !m.dim().equals(this.level().dimension()));
		return this.markers;
	}

	public void setMarkers(List<GolemMarker> markers) {
		this.markers = new ArrayList<>(markers);
	}

	/** The marked blocks in this dimension holding an inventory, nearest first (for the bell overlay and tests). */
	public List<BlockPos> getMarkedContainers() {
		return this.getMarkers().stream().map(GolemMarker::pos).distinct().filter(p -> GolemInventory.getContainer(this.level(), p) != null)
			.sorted(java.util.Comparator.comparingDouble(p -> p.distToCenterSqr(this.position()))).toList();
	}

	// ------------------------------------------------------------------ copper golem add-on

	public int getOxidationLevel() {
		return this.entityData.get(OXIDATION);
	}

	public void setOxidationLevel(int level) {
		this.entityData.set(OXIDATION, Math.max(0, Math.min(3, level)));
	}

	public int getWaxedTicks() {
		return this.entityData.get(WAXED_TICKS);
	}

	public void setWaxedTicks(int ticks) {
		this.entityData.set(WAXED_TICKS, Math.max(0, ticks));
	}

	public boolean isPermanentlyWaxed() {
		return this.entityData.get(PERM_WAXED);
	}

	public void setPermanentlyWaxed(boolean perm) {
		this.entityData.set(PERM_WAXED, perm);
	}

	public boolean isWaxed() {
		return this.isPermanentlyWaxed() || this.getWaxedTicks() > 0;
	}

	public int getChargedTicks() {
		return this.entityData.get(CHARGED_TICKS);
	}

	public void setChargedTicks(int ticks) {
		this.entityData.set(CHARGED_TICKS, Math.max(0, ticks));
	}

	public boolean isCharged() {
		return this.getChargedTicks() > 0;
	}

	// ------------------------------------------------------------------ state

	public float getBootup() {
		return this.bootup;
	}

	public boolean isInactive() {
		return this.paused || this.inactive;
	}

	/** The golem stands still while a player has its GUI open. */
	public void setInactive(boolean inactive) {
		this.paused = inactive;
	}

	/** getAIMoveSpeed: the type's speed, faster with a bowtie, air upgrades or when advanced, slower in armour; heavy golems wade fast. */
	public float getGolemSpeed() {
		if (this.isInactive()) {
			return 0.0F;
		}
		float speed = this.getGolemType().speed;
		if (this.getGolemType() == GolemType.COPPER) {
			speed = switch (this.getOxidationLevel()) {
				case 0 -> 0.31F;
				case 1 -> 0.28F;
				case 2 -> 0.25F;
				default -> 0.21F;
			} * (this.isCharged() ? 1.3F : 1.0F);
		}
		speed *= this.hasDecoration('B') ? 1.1F : 1.0F;
		if (this.hasDecoration('P')) {
			speed *= 0.88F;
		}
		speed *= 1.0F + this.getUpgradeAmount(0) * 0.15F;
		if (this.isAdvanced()) {
			speed *= 1.1F;
		}
		GolemType t = this.getGolemType();
		if (this.isInWater() && (t == GolemType.STONE || t == GolemType.IRON || t == GolemType.THAUMIUM)) {
			speed *= 2.0F;
		}
		return speed;
	}

	public int getActionTimer() {
		return 3 - Math.abs(this.action - 3);
	}

	public void startActionTimer() {
		if (this.action == 0) {
			this.action = 6;
			this.level().broadcastEntityEvent(this, (byte) 4);
		}
	}

	public void startLeftArmTimer() {
		if (this.leftArm == 0) {
			this.leftArm = 5;
			this.level().broadcastEntityEvent(this, (byte) 6);
		}
	}

	public void startRightArmTimer() {
		if (this.rightArm == 0) {
			this.rightArm = 5;
			this.level().broadcastEntityEvent(this, (byte) 8);
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		switch (id) {
			case 4 -> this.action = 6;
			case 5 -> this.healing = 5;
			case 6 -> this.leftArm = 5;
			case 7 -> this.bootup = 33.0F;
			case 8 -> this.rightArm = 5;
			default -> super.handleEntityEvent(id);
		}
	}

	/** The chest the golem has open, so it shows open while the golem works it (Config.golemChestInteract). */
	public void openContainer(@Nullable BlockPos pos) {
		if (pos == null || !dev.neothaum.config.NeoThaumConfig.current.golemChestInteract) {
			this.closeContainer();
			return;
		}
		if (pos.equals(this.openContainer)) {
			return;
		}
		this.closeContainer();
		var inv = GolemInventory.getContainer(this.level(), pos);
		if (inv != null) {
			this.openContainer = pos.immutable();
			inv.startOpen(this);
		}
	}

	public void closeContainer() {
		if (this.openContainer != null) {
			var inv = GolemInventory.getContainer(this.level(), this.openContainer);
			this.openContainer = null;
			if (inv != null) {
				inv.stopOpen(this);
			}
		}
	}

	@Override
	public boolean hasContainerOpen(ContainerOpenersCounter container, BlockPos blockPos) {
		return this.openContainer != null && this.openContainer.distManhattan(blockPos) <= 1;
	}

	@Override
	public double getContainerInteractionRange() {
		return 4.0;
	}

	// ------------------------------------------------------------------ ticking

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.action > 0) {
			this.action--;
		}
		if (this.leftArm > 0) {
			this.leftArm--;
		}
		if (this.rightArm > 0) {
			this.rightArm--;
		}
		if (this.healing > 0) {
			this.healing--;
		}
		this.inactive = this.getBlockY() > this.level().getMinY() && dev.neothaum.blocks.GolemFetterBlock.holds(this.level(), this.blockPosition());
		if (!this.level().isClientSide()) {
			if (this.regenTimer > 0) {
				this.regenTimer--;
			} else {
				this.regenTimer = this.getGolemType().regenDelay;
				if (this.hasDecoration('F')) {
					this.regenTimer = (int) (this.regenTimer * 0.66F);
				}
				if (this.getHealth() < this.getMaxHealth()) {
					this.level().broadcastEntityEvent(this, (byte) 5);
					this.heal(1.0F);
				}
			}
			double speed = this.getGolemSpeed();
			if (speed != this.lastSpeed) {
				this.lastSpeed = speed;
				this.setAttr(Attributes.MOVEMENT_SPEED, speed);
			}
			if (this.distToHomeSqr() >= 2304.0 || this.isInWall()) {
				this.teleportNearHome();
			}
			if (this.getGolemType() == GolemType.COPPER) {
				this.copperTick();
			}
			if (this.openContainer != null && this.tickCount % 20 == 0 && this.openContainer.distToCenterSqr(this.position()) > 16.0) {
				this.closeContainer();
			}
		} else if (this.bootup > 0.0F && this.getCore() > -1) {
			this.bootup = this.bootup * (this.bootup / 33.1F);
			this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), TVSounds.CAMERATICKS, SoundSource.NEUTRAL, this.bootup * 0.2F, this.bootup,
				false);
		}
		if (this.level().isClientSide() && this.getGolemType() == GolemType.COPPER && this.isCharged() && this.random.nextInt(3) == 0) {
			this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX() + (this.random.nextDouble() - 0.5) * 0.4, this.getY() + 0.8,
				this.getZ() + (this.random.nextDouble() - 0.5) * 0.4, 0, 0.02, 0);
		}
	}

	/** Back to a free spot by home when lost or stuck in a wall. */
	private void teleportNearHome() {
		BlockPos h = this.getHomePos();
		for (int dy = 1; dy >= -1; dy--) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos p = h.offset(dx, dy, dz);
					if (this.level().getBlockState(p.below()).isFaceSturdy(this.level(), p.below(), Direction.UP)
						&& !this.level().getBlockState(p).isSuffocating(this.level(), p)) {
						this.snapTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, this.getYRot(), this.getXRot());
						this.getNavigation().stop();
						return;
					}
				}
			}
		}
	}

	/** While paused or fettered the golem does nothing but bob in water (EntityGolemBase.updateEntityActionState). */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		boolean idle = this.isInactive();
		this.goalSelector.setControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, !idle);
		this.goalSelector.setControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK, !idle);
		this.targetSelector.setControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.TARGET, !idle);
		if (idle) {
			this.getNavigation().stop();
		}
		if ((this.isInWater() || this.isInLava()) && this.random.nextFloat() < 0.8F) {
			this.getJumpControl().jump();
		}
	}

	private void copperTick() {
		if (this.getWaxedTicks() > 0) {
			this.setWaxedTicks(this.getWaxedTicks() - 1);
		} else if (!this.isPermanentlyWaxed() && this.random.nextInt(24000) == 0 && this.getOxidationLevel() < 3) {
			this.setOxidationLevel(this.getOxidationLevel() + 1);
		}
		if (this.getChargedTicks() > 0) {
			this.setChargedTicks(this.getChargedTicks() - 1);
		}
		if (this.level().isThundering() && this.level().canSeeSky(this.blockPosition()) && this.random.nextInt(1200) == 0
			&& this.level() instanceof ServerLevel sl) {
			this.setChargedTicks(600);
			this.setOxidationLevel(0);
			sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.SHOCK, SoundSource.NEUTRAL, 1.0F, 1.2F);
			sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.8, this.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
		}
		if ((this.isCharged() || this.level().isThundering()) && this.tickCount % 20 == 0 && this.level() instanceof ServerLevel sl) {
			for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6.0))) {
				for (InteractionHand h : InteractionHand.values()) {
					ItemStack item = p.getItemInHand(h);
					if (item.getItem() instanceof WandItem) {
						for (Aspect a : Aspect.getPrimalAspects()) {
							WandItem.addVis(item, a, 100, true);
						}
						sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1.0, p.getZ(), 3, 0.1, 0.1, 0.1, 0.02);
					}
				}
			}
		}
	}

	// ------------------------------------------------------------------ combat

	@Override
	public boolean fireImmune() {
		return this.getGolemType().fireResist || super.fireImmune();
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource ds, float amount) {
		this.paused = false;
		if (ds.is(DamageTypes.CACTUS)) {
			return false;
		}
		if (this.getGolemType() == GolemType.THAUMIUM && ds.is(DamageTypes.MAGIC)) {
			amount *= 0.5F;
		}
		if (ds.is(DamageTypeTags.IS_FIRE) && this.getGolemType().fireResist) {
			return false;
		}
		if (ds.is(DamageTypes.IN_WALL) || ds.is(DamageTypes.FELL_OUT_OF_WORLD)) {
			BlockPos h = this.getHomePos();
			this.snapTo(h.getX() + 0.5, h.getY() + 0.5, h.getZ() + 0.5, 0.0F, 0.0F);
		}
		int entropy = this.getUpgradeAmount(5);
		if (ds.getDirectEntity() != null && entropy > 0 && ds.getDirectEntity() != this) {
			Entity src = ds.getDirectEntity();
			src.hurtServer(level, this.damageSources().thorns(this), entropy * 2 + this.random.nextInt(2 * entropy));
			src.playSound(SoundEvents.THORNS_HIT, 0.5F, 1.0F);
		}
		return super.hurtServer(level, ds, amount);
	}

	/** attackEntityAsMob: the golem's blow; a visor makes the kill count as a player's, fire upgrades set the target alight. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		float f = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
		boolean hit = target.hurtServer(level, this.damageSources().mobAttack(this), f);
		if (hit) {
			if (this.hasDecoration('V') && target instanceof LivingEntity le && this.getOwner() != null) {
				le.setLastHurtByPlayer(this.getOwner(), 100);
			}
			int j = this.getUpgradeAmount(2);
			if (j > 0) {
				target.igniteForSeconds(j * 4);
			}
		}
		return hit;
	}

	/** isValidTarget: in range, not the owner; a butcher picks grown, untamed animals; a guard monsters (and with order more). */
	public boolean isValidTarget(Entity target) {
		if (!target.isAlive() || target instanceof Player p && p.getName().getString().equals(this.ownerName)) {
			return false;
		}
		if (!this.isWithinHomeRange(target.blockPosition())) {
			return false;
		}
		boolean animal = target instanceof Animal && !(target instanceof Enemy) && !(target instanceof TamableAnimal t && t.isTame())
			&& !(target instanceof AbstractGolem);
		if (this.getCore() == 9) {
			return animal && !((Animal) target).isBaby();
		}
		boolean order = this.getUpgradeAmount(4) > 0;
		if (this.canAttackCreepers() && order && target instanceof Creeper) {
			return true;
		}
		if (this.canAttackHostiles() && target instanceof Enemy && !(target instanceof Creeper)) {
			return true;
		}
		if (this.canAttackAnimals() && order && animal) {
			return true;
		}
		return this.canAttackPlayers() && order && target instanceof Player p && !p.isCreative() && !p.isSpectator();
	}

	public int getAttackSpeed() {
		return 20 - (this.isAdvanced() ? 2 : 0);
	}

	/** attackEntityWithRangedAttack: a dart, more accurate with water upgrades. */
	public void attackEntityWithRangedAttack(LivingEntity target) {
		if (!(this.level() instanceof ServerLevel sl)) {
			return;
		}
		DartEntity dart = new DartEntity(sl, this, target, 1.6F, 7.0F - this.getUpgradeAmount(3) * 1.75F);
		dart.setBaseDamage(this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4);
		this.playSound(TVSounds.GOLEMIRONSHOOT, 0.5F, 1.0F / (this.random.nextFloat() * 0.4F + 0.6F));
		sl.addFreshEntity(dart);
		this.startLeftArmTimer();
	}

	@Override
	public @Nullable LivingEntity getTarget() {
		LivingEntity e = super.getTarget();
		return e != null && !e.isAlive() ? null : e;
	}

	// ------------------------------------------------------------------ interaction

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		ItemStack held = player.getItemInHand(hand);
		if (held.getItem() instanceof GolemBellItem) {
			return InteractionResult.PASS;
		}
		if (this.getCore() == -1 && held.getItem() instanceof GolemCoreItem coreItem && coreItem.getCoreType() != GolemCoreType.BLANK) {
			if (!this.level().isClientSide()) {
				this.setCore(coreItem.getCoreType().id);
				held.shrink(1);
				this.playSound(TVSounds.UPGRADE, 0.5F, 1.0F);
				this.level().broadcastEntityEvent(this, (byte) 7);
			}
			return InteractionResult.SUCCESS;
		}
		if (held.getItem() instanceof GolemUpgradeItem upItem) {
			if (this.upgrades.size() < this.getMaxUpgrades() && this.getUpgradeAmount(upItem.getUpgradeType().id) < 2) {
				if (!this.level().isClientSide()) {
					this.addUpgrade(upItem.getUpgradeType());
					held.shrink(1);
					this.playSound(TVSounds.UPGRADE, 0.5F, 1.0F);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}
		if (this.copperInteract(player, held)) {
			return InteractionResult.SUCCESS;
		}
		return this.customInteraction(player, hand, held);
	}

	private InteractionResult customInteraction(Player player, InteractionHand hand, ItemStack held) {
		if (held.getItem() instanceof GolemDecorationItem decoItem) {
			if (!this.level().isClientSide() && this.addDecoration(decoItem.getDecorationType())) {
				held.shrink(1);
				this.playSound(TVSounds.CAMERACLACK, 1.0F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}
		if (held.is(Items.COOKIE)) {
			held.shrink(1);
			for (int a = 0; a < 3; a++) {
				this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
					this.getRandomZ(1.0), this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
				this.playSound(SoundEvents.GENERIC_EAT.value(), 0.3F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
			}
			if (!this.level().isClientSide()) {
				int duration = 600;
				MobEffectInstance cur = this.getEffect(MobEffects.SPEED);
				if (cur != null && cur.getDuration() < 2400) {
					duration += cur.getDuration();
				}
				this.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0));
				this.heal(5.0F);
			}
			return InteractionResult.SUCCESS;
		}
		if (this.getCore() > -1 && GolemCoreType.byId(this.getCore()).hasGUI && !(held.getItem() instanceof WandItem)) {
			if (!this.level().isClientSide() && player instanceof ServerPlayer sp) {
				sp.openMenu(this);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	/** The copper golem add-on: honeycomb or magic tallow wax it, an axe scrapes wax or verdigris off. */
	private boolean copperInteract(Player player, ItemStack held) {
		if (this.getGolemType() != GolemType.COPPER || this.level().isClientSide()) {
			return this.getGolemType() == GolemType.COPPER && (held.is(Items.HONEYCOMB) || held.is(TVItems.MAGIC_TALLOW)
				|| held.is(net.minecraft.tags.ItemTags.AXES) && (this.isWaxed() || this.getOxidationLevel() > 0));
		}
		ServerLevel sl = (ServerLevel) this.level();
		if ((held.is(Items.HONEYCOMB) || held.is(TVItems.MAGIC_TALLOW)) && !this.isPermanentlyWaxed()) {
			held.consume(1, player);
			if (held.is(TVItems.MAGIC_TALLOW)) {
				this.setPermanentlyWaxed(true);
				sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.UPGRADE, SoundSource.PLAYERS, 1.0F, 1.0F);
				sl.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 0.5, this.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
			} else {
				this.setWaxedTicks(48000);
				sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);
				sl.sendParticles(ParticleTypes.WAX_ON, this.getX(), this.getY() + 0.5, this.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
			}
			return true;
		}
		if (held.is(net.minecraft.tags.ItemTags.AXES) && (this.isWaxed() || this.getOxidationLevel() > 0)) {
			if (this.isWaxed()) {
				this.setPermanentlyWaxed(false);
				this.setWaxedTicks(0);
				sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AXE_WAX_OFF, SoundSource.PLAYERS, 1.0F, 1.0F);
				sl.sendParticles(ParticleTypes.WAX_OFF, this.getX(), this.getY() + 0.5, this.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
			} else {
				this.setOxidationLevel(this.getOxidationLevel() - 1);
				sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AXE_SCRAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
				sl.sendParticles(ParticleTypes.SCRAPE, this.getX(), this.getY() + 0.5, this.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
			}
			held.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
			return true;
		}
		return false;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		if (!this.itemCarried.isEmpty()) {
			this.spawnAtLocation(level, this.itemCarried, 0.5F);
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		this.closeContainer();
		super.remove(reason);
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	public net.minecraft.network.chat.Component getTypeName() {
		return net.minecraft.network.chat.Component.translatable("item.ItemGolemPlacer." + this.getGolemType().ordinal() + ".name");
	}

	/** ItemGolemBell pickup: a placer that remembers everything about this golem. */
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
			case COPPER -> new ItemStack(TVItems.GOLEM_COPPER);
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
		if (!this.markers.isEmpty()) {
			stack.set(TVComponents.GOLEM_MARKERS, new ArrayList<>(this.markers));
		}
		List<ItemStack> filter = new ArrayList<>();
		for (int a = 0; a < this.getInventorySize(); a++) {
			filter.add(this.filterItems.get(a).copy());
		}
		if (filter.stream().anyMatch(s -> !s.isEmpty())) {
			stack.set(TVComponents.GOLEM_INVENTORY, filter);
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
	protected float getSoundVolume() {
		return 0.1F;
	}

	// ------------------------------------------------------------------ saving

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
		output.putInt("HomeFacing", this.getHomeFacingId());
		output.putByte("toggles", this.entityData.get(TOGGLES));
		output.putInt("Oxidation", this.getOxidationLevel());
		output.putInt("WaxedTicks", this.getWaxedTicks());
		output.putBoolean("PermWaxed", this.isPermanentlyWaxed());
		output.putInt("ChargedTicks", this.getChargedTicks());
		output.store("CarriedItem", ItemStack.OPTIONAL_CODEC, this.itemCarried);
		if (this.getCore() == 5 && this.fluidCarried != null && this.fluidAmount > 0) {
			output.putString("Fluid", BuiltInRegistries.FLUID.getKey(this.fluidCarried).toString());
			output.putInt("FluidAmount", this.fluidAmount);
		}
		if (this.getCore() == 6 && this.essentia != null && this.essentiaAmount > 0) {
			output.putString("essentia", this.essentia.getTag());
			output.putInt("essentiaAmount", this.essentiaAmount);
		}
		output.store("Upgrades", com.mojang.serialization.Codec.INT.listOf(), this.upgrades);
		if (this.ownerId != null) {
			output.putString("OwnerUUID", this.ownerId.toString());
		}
		output.putString("OwnerName", this.ownerName);
		ContainerHelper.saveAllItems(output.child("FilterItems"), this.filterItems);
		int[] colorsInt = new int[this.colors.length];
		for (int i = 0; i < this.colors.length; i++) {
			colorsInt[i] = this.colors[i];
		}
		output.putIntArray("FilterColors", colorsInt);
		output.store("Markers", GolemMarker.LIST_CODEC, this.markers);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(TYPE, GolemType.byId(input.getIntOr("GolemType", GolemType.WOOD.ordinal())).ordinal());
		this.setAdvanced(input.getBooleanOr("Advanced", false));
		this.entityData.set(CORE, input.getIntOr("Core", -1));
		this.entityData.set(DECORATION, input.getStringOr("Decoration", ""));
		int hf = input.getIntOr("HomeFacing", Direction.UP.get3DDataValue());
		this.setHomeArea(new BlockPos(input.getIntOr("HomeX", 0), input.getIntOr("HomeY", 0), input.getIntOr("HomeZ", 0)), Direction.from3DDataValue(hf));
		this.entityData.set(TOGGLES, input.getByteOr("toggles", (byte) 0));
		this.setOxidationLevel(input.getIntOr("Oxidation", 0));
		this.setWaxedTicks(input.getIntOr("WaxedTicks", 0));
		this.setPermanentlyWaxed(input.getBooleanOr("PermWaxed", false));
		this.setChargedTicks(input.getIntOr("ChargedTicks", 0));
		this.itemCarried = input.read("CarriedItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		String fluid = input.getStringOr("Fluid", "");
		this.fluidCarried = fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getValue(Identifier.parse(fluid));
		if (this.fluidCarried == Fluids.EMPTY) {
			this.fluidCarried = null;
		}
		this.fluidAmount = input.getIntOr("FluidAmount", 0);
		this.essentia = Aspect.getAspect(input.getStringOr("essentia", ""));
		this.essentiaAmount = this.essentia == null ? 0 : input.getIntOr("essentiaAmount", 0);
		this.upgrades = new ArrayList<>(input.read("Upgrades", com.mojang.serialization.Codec.INT.listOf()).orElse(List.of()));
		this.syncUpgrades();
		input.child("FilterItems").ifPresent(c -> ContainerHelper.loadAllItems(c, this.filterItems));
		input.getIntArray("FilterColors").ifPresent(arr -> {
			for (int i = 0; i < Math.min(arr.length, this.colors.length); i++) {
				this.colors[i] = (byte) arr[i];
			}
		});
		String uuidStr = input.getStringOr("OwnerUUID", "");
		if (!uuidStr.isEmpty()) {
			try {
				this.ownerId = UUID.fromString(uuidStr);
			} catch (IllegalArgumentException ignored) {
			}
		}
		this.ownerName = input.getStringOr("OwnerName", "");
		this.setMarkers(input.read("Markers", GolemMarker.LIST_CODEC).orElse(List.of()));
		this.setupGolem();
		this.setupGolemInventory();
		this.updateCarried();
	}
}
