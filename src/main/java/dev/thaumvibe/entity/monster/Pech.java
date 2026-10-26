package dev.thaumvibe.entity.monster;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.aspects.ObjectAspects;
import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.entity.projectile.PechBlast;
import dev.thaumvibe.items.ManaBeanItem;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.menu.PechMenu;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.world.TVBiomes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * EntityPech: a hunched little trader of the magical forests with a huge pack on its back. Pechs hoard whatever they find lying about, flee
 * players, and can be won over with something they value (gold, emeralds, mana beans, anything greedy); a friendly pech will trade a valued
 * item for a handful from its pack or its kind's trade list. Hurt one and every pech nearby turns on you. Foragers fight hand to hand,
 * stalkers carry bows, and mages throw blasts of curdled magic from a wand.
 */
public class Pech extends Monster implements RangedAttackMob, ExtendedMenuProvider<Integer> {
	private static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.defineId(Pech.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(Pech.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(Pech.class, EntityDataSerializers.BOOLEAN);

	/** EntityPech.valuedItems */
	private static final Map<net.minecraft.world.item.Item, Integer> VALUED = new HashMap<>();

	public final NonNullList<ItemStack> loot = NonNullList.withSize(9, ItemStack.EMPTY);
	public boolean trading;
	public boolean updateAINextTick;
	public float mumble;
	private int chargecount;

	private final RangedAttackGoal aiArrowAttack = new RangedAttackGoal(this, 0.6, 20, 50, 15.0F);
	private final RangedAttackGoal aiBlastAttack = new RangedAttackGoal(this, 0.6, 20, 30, 15.0F);
	private final MeleeAttackGoal aiMeleeAttack = new MeleeAttackGoal(this, 0.6, false);
	private final AvoidEntityGoal<Player> aiAvoidPlayer = new AvoidEntityGoal<>(this, Player.class, 8.0F, 0.5, 0.6);

	public Pech(EntityType<? extends Pech> type, Level level) {
		super(type, level);
		this.setDropChance(EquipmentSlot.MAINHAND, 0.2F);
		if (!level.isClientSide()) {
			this.setCombatTask();
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 6.0)
			.add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 16.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new TradePlayerGoal());
		this.goalSelector.addGoal(3, new ItemEntityGotoGoal());
		this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
		this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.5));
		this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.6));
		this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	// ------------------------------------------------------------------ synced data

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(TYPE, (byte) 0);
		entityData.define(ANGER, 0);
		entityData.define(TAMED, false);
	}

	public int getPechType() {
		return this.entityData.get(TYPE);
	}

	public void setPechType(int type) {
		this.entityData.set(TYPE, (byte) type);
	}

	public int getAnger() {
		return this.entityData.get(ANGER);
	}

	public void setAnger(int anger) {
		this.entityData.set(ANGER, anger);
	}

	public boolean isTamed() {
		return this.entityData.get(TAMED);
	}

	public void setTamed(boolean tamed) {
		this.entityData.set(TAMED, tamed);
	}

	@Override
	protected Component getTypeName() {
		return Component.translatable(switch (this.getPechType()) {
			case 1 -> "entity.thaumvibe.pech.mage";
			case 2 -> "entity.thaumvibe.pech.stalker";
			default -> "entity.thaumvibe.pech";
		});
	}

	// ------------------------------------------------------------------ spawning and equipment

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data) {
		data = super.finalizeSpawn(level, difficulty, reason, data);
		RandomSource r = this.random;
		ItemStack held = switch (r.nextInt(20)) {
			case 0, 12 -> {
				// a plain wand with a pech focus and a little vis
				ItemStack wand = WandItem.createFull(TVItems.WAND, "iron", "wood");
				WandItem.setFocus(wand, new ItemStack(TVItems.FOCUS_PECH));
				WandItem.storeVis(wand, Aspect.EARTH, (2 + r.nextInt(6)) * 100);
				WandItem.storeVis(wand, Aspect.ENTROPY, (2 + r.nextInt(6)) * 100);
				WandItem.storeVis(wand, Aspect.WATER, (2 + r.nextInt(6)) * 100);
				WandItem.storeVis(wand, Aspect.AIR, r.nextInt(4) * 100);
				WandItem.storeVis(wand, Aspect.FIRE, r.nextInt(4) * 100);
				WandItem.storeVis(wand, Aspect.ORDER, r.nextInt(4) * 100);
				yield wand;
			}
			case 1 -> new ItemStack(Items.STONE_SWORD);
			case 2, 4, 10, 11, 13 -> new ItemStack(Items.BOW);
			case 3 -> new ItemStack(Items.STONE_AXE);
			case 5 -> new ItemStack(Items.IRON_SWORD);
			case 6 -> new ItemStack(Items.IRON_AXE);
			case 7 -> new ItemStack(Items.FISHING_ROD);
			case 8 -> new ItemStack(Items.STONE_PICKAXE);
			case 9 -> new ItemStack(Items.IRON_PICKAXE);
			default -> ItemStack.EMPTY;
		};
		this.setItemSlot(EquipmentSlot.MAINHAND, held);
		if (held.getItem() instanceof WandItem) {
			this.setPechType(1);
			this.setDropChance(EquipmentSlot.MAINHAND, 0.1F);
		} else if (!held.isEmpty()) {
			if (held.is(Items.BOW)) {
				this.setPechType(2);
			}
			this.populateDefaultEquipmentEnchantments(level, r, difficulty);
		}
		this.setCanPickUpLoot(r.nextFloat() < 0.75F * difficulty.getSpecialMultiplier());
		this.updateAINextTick = true;
		return data;
	}

	/** EntityPech.getCanSpawnHere: in magical places (not the taint), and no more than four together. */
	public static boolean checkPechSpawnRules(EntityType<Pech> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
		RandomSource random) {
		var biome = level.getBiome(pos);
		boolean magic = biome.is(ConventionalBiomeTags.IS_MAGICAL) && !biome.is(TVBiomes.TAINTED_LAND);
		if (level.getLevel().dimension() != Level.OVERWORLD && !biome.is(TVBiomes.MAGICAL_FOREST) && !biome.is(TVBiomes.EERIE)) {
			magic = false;
		}
		return magic && level.getEntitiesOfClass(Pech.class, new AABB(pos).inflate(16.0)).size() < 4
			&& Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		int q = 0;
		for (ItemStack is : this.loot) {
			if (!is.isEmpty()) {
				q++;
			}
		}
		return q < 5;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public int getArmorValue() {
		return Math.min(20, super.getArmorValue() + 2);
	}

	// ------------------------------------------------------------------ combat

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
		super.setItemSlot(slot, stack);
		if (!this.level().isClientSide() && slot == EquipmentSlot.MAINHAND) {
			this.updateAINextTick = true;
		}
	}

	public void setCombatTask() {
		this.goalSelector.removeGoal(this.aiMeleeAttack);
		this.goalSelector.removeGoal(this.aiArrowAttack);
		this.goalSelector.removeGoal(this.aiBlastAttack);
		ItemStack held = this.getMainHandItem();
		if (held.is(Items.BOW)) {
			this.goalSelector.addGoal(2, this.aiArrowAttack);
		} else if (held.getItem() instanceof WandItem) {
			this.goalSelector.addGoal(2, this.aiBlastAttack);
		} else {
			this.goalSelector.addGoal(2, this.aiMeleeAttack);
		}
		this.goalSelector.removeGoal(this.aiAvoidPlayer);
		if (!this.isTamed()) {
			this.goalSelector.addGoal(4, this.aiAvoidPlayer);
		}
	}

	@Override
	public void performRangedAttack(LivingEntity target, float power) {
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		if (this.getPechType() == 2) {
			ItemStack bow = this.getMainHandItem();
			AbstractArrow arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), power, bow);
			double dx = target.getX() - this.getX();
			double dy = target.getY(0.3333333333333333) - arrow.getY();
			double dz = target.getZ() - this.getZ();
			double d = Math.sqrt(dx * dx + dz * dz);
			arrow.shoot(dx, dy + d * 0.2F, dz, 1.6F, 14 - level.getDifficulty().getId() * 4);
			this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
			level.addFreshEntity(arrow);
		} else if (this.getPechType() == 1) {
			PechBlast blast = new PechBlast(level, this, 1, 0, this.random.nextFloat() < 0.1F);
			double d0 = target.getX() + target.getDeltaMovement().x - this.getX();
			double d1 = target.getY() + target.getEyeHeight() - 1.500000023841858 - this.getY();
			double d2 = target.getZ() + target.getDeltaMovement().z - this.getZ();
			float f1 = (float) Math.sqrt(d0 * d0 + d2 * d2);
			blast.shoot(d0, d1 + f1 * 0.1F, d2, 1.5F, 4.0F);
			this.playSound(TVSounds.ICE, 0.4F, 1.0F + this.random.nextFloat() * 0.1F);
			level.addFreshEntity(blast);
		}
		this.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
	}

	private void becomeAngryAt(Entity entity) {
		if (this.getAnger() <= 0) {
			this.level().broadcastEntityEvent(this, (byte) 19);
			this.playSound(TVSounds.PECH_CHARGE, this.getSoundVolume(), this.getVoicePitch());
		}
		if (entity instanceof LivingEntity le) {
			this.setTarget(le);
		}
		this.setAnger(400 + this.random.nextInt(400));
		this.setTamed(false);
		this.updateAINextTick = true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.getEntity() instanceof Player player) {
			for (Pech other : level.getEntitiesOfClass(Pech.class, this.getBoundingBox().inflate(32.0, 16.0, 32.0), p -> p != this)) {
				other.becomeAngryAt(player);
			}
			this.becomeAngryAt(player);
		}
		return super.hurtServer(level, source, damage);
	}

	// ------------------------------------------------------------------ ticking

	@Override
	public void tick() {
		if (this.mumble > 0.0F) {
			this.mumble *= 0.75F;
		}
		if (!this.level().isClientSide()) {
			if (this.getAnger() > 0) {
				this.setAnger(this.getAnger() - 1);
			}
			if (this.getAnger() > 0 && this.getTarget() == null) {
				Player p = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0,
					e -> e instanceof Player pl && !pl.isCreative() && !pl.isSpectator() && pl.isAlive());
				this.setTarget(p);
				if (p != null) {
					if (this.chargecount > 0) {
						this.chargecount--;
					}
					if (this.chargecount == 0) {
						this.chargecount = 100;
						this.playSound(TVSounds.PECH_CHARGE, this.getSoundVolume(), this.getVoicePitch());
					}
					this.level().broadcastEntityEvent(this, (byte) 17);
				}
			}
		} else {
			if (this.random.nextInt(15) == 0 && this.getAnger() > 0) {
				this.villagerParticle(ParticleTypes.ANGRY_VILLAGER);
			}
			if (this.random.nextInt(25) == 0 && this.isTamed()) {
				this.villagerParticle(ParticleTypes.HAPPY_VILLAGER);
			}
		}
		super.tick();
	}

	private void villagerParticle(net.minecraft.core.particles.ParticleOptions type) {
		this.level().addParticle(type, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
			this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(), this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
			this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (this.updateAINextTick) {
			this.updateAINextTick = false;
			this.setCombatTask();
		}
		super.customServerAiStep(level);
		if (this.tickCount % 40 == 0) {
			this.heal(1.0F);
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 16) {
			this.mumble = (float) Math.PI;
		} else if (id == 17) {
			this.mumble = (float) (Math.PI * 2);
		} else if (id == 18) {
			for (int i = 0; i < 5; i++) {
				this.villagerParticle(ParticleTypes.HAPPY_VILLAGER);
			}
		} else if (id == 19) {
			for (int i = 0; i < 5; i++) {
				this.villagerParticle(ParticleTypes.ANGRY_VILLAGER);
			}
			this.mumble = (float) (Math.PI * 2);
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	public void playAmbientSound() {
		if (!this.level().isClientSide()) {
			if (this.random.nextInt(3) == 0 && !this.level().getEntitiesOfClass(Pech.class, this.getBoundingBox().inflate(4.0, 2.0, 4.0), p -> p != this)
				.isEmpty()) {
				this.level().broadcastEntityEvent(this, (byte) 17);
				this.playSound(TVSounds.PECH_TRADE, this.getSoundVolume(), this.getVoicePitch());
				return;
			}
			this.level().broadcastEntityEvent(this, (byte) 16);
		}
		super.playAmbientSound();
	}

	@Override
	public int getAmbientSoundInterval() {
		return 120;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.PECH_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TVSounds.PECH_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.PECH_DEATH;
	}

	// ------------------------------------------------------------------ the pack, and what a pech values

	public boolean isValued(ItemStack item) {
		if (item.isEmpty()) {
			return false;
		}
		if (VALUED.containsKey(item.getItem())) {
			return true;
		}
		AspectList al = ObjectAspects.getAspects(item);
		return al != null && al.getAmount(Aspect.GREED) > 0;
	}

	public int getValue(ItemStack item) {
		if (item.isEmpty()) {
			return 0;
		}
		int value = VALUED.getOrDefault(item.getItem(), 0);
		if (value == 0) {
			AspectList al = ObjectAspects.getAspects(item);
			value = al == null ? 0 : Math.min(32, al.getAmount(Aspect.GREED));
		}
		return value;
	}

	public boolean canPickup(ItemStack item) {
		if (item.isEmpty()) {
			return false;
		}
		if (!this.isTamed() && VALUED.containsKey(item.getItem())) {
			return true;
		}
		for (ItemStack s : this.loot) {
			if (s.isEmpty() || ItemStack.isSameItemSameComponents(item, s) && item.getCount() + s.getCount() <= s.getMaxStackSize()) {
				return true;
			}
		}
		return false;
	}

	/** Takes what it can of the stack into its pack (or, untamed, eats a valued item and may warm to you); returns what is left. */
	public ItemStack pickupItem(ItemStack item) {
		if (item.isEmpty()) {
			return item;
		}
		if (!this.isTamed() && this.isValued(item)) {
			if (this.random.nextInt(10) < this.getValue(item)) {
				this.setTamed(true);
				this.updateAINextTick = true;
				this.level().broadcastEntityEvent(this, (byte) 18);
			}
			item.shrink(1);
			return item;
		}
		for (int a = 0; a < this.loot.size() && !item.isEmpty(); a++) {
			ItemStack s = this.loot.get(a);
			if (!s.isEmpty() && s.getCount() < s.getMaxStackSize() && ItemStack.isSameItemSameComponents(item, s)) {
				int sz = Math.min(item.getCount(), s.getMaxStackSize() - s.getCount());
				s.grow(sz);
				item.shrink(sz);
			}
		}
		for (int a = 0; a < this.loot.size() && !item.isEmpty(); a++) {
			if (this.loot.get(a).isEmpty()) {
				this.loot.set(a, item.copy());
				item.setCount(0);
			}
		}
		return item;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (player.isShiftKeyDown() || player.getItemInHand(hand).getItem() instanceof NameTagItem) {
			return InteractionResult.PASS;
		}
		if (this.isTamed()) {
			if (player instanceof ServerPlayer sp) {
				sp.openMenu(this);
			}
			return InteractionResult.SUCCESS;
		}
		return super.mobInteract(player, hand);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new PechMenu(id, inv, this);
	}

	@Override
	public Integer getScreenOpeningData(ServerPlayer player) {
		return this.getId();
	}

	// ------------------------------------------------------------------ drops and saving

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int looting = TVEntities.looting(level, source);
		for (ItemStack s : this.loot) {
			if (!s.isEmpty() && this.random.nextFloat() < 0.88F) {
				this.spawnAtLocation(level, s.copy(), 1.5F);
			}
		}
		List<Aspect> primals = Aspect.getPrimalAspects();
		for (int a = 0; a < 1 + looting; a++) {
			if (this.random.nextBoolean()) {
				this.spawnAtLocation(level, ManaBeanItem.of(primals.get(this.random.nextInt(primals.size()))), 1.5F);
			}
		}
		if (this.random.nextInt(10) < 1 + looting) {
			this.spawnAtLocation(level, new ItemStack(TVItems.GOLD_COIN), 1.5F);
		}
		// EntityLiving.dropRareDrop
		if (killedByPlayer && this.random.nextInt(200) - looting < 5) {
			this.spawnAtLocation(level, new ItemStack(TVItems.KNOWLEDGE_FRAGMENT), 1.5F);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putByte("PechType", (byte) this.getPechType());
		output.putShort("Anger", (short) this.getAnger());
		output.putBoolean("Tamed", this.isTamed());
		ContainerHelper.saveAllItems(output.child("Loot"), this.loot);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setPechType(input.getByteOr("PechType", (byte) 0));
		this.setAnger(input.getShortOr("Anger", (short) 0));
		this.setTamed(input.getBooleanOr("Tamed", false));
		input.child("Loot").ifPresent(c -> ContainerHelper.loadAllItems(c, this.loot));
		this.updateAINextTick = true;
	}

	static {
		VALUED.put(TVItems.MANA_BEAN, 1);
		VALUED.put(Items.GOLD_INGOT, 2);
		VALUED.put(Items.GOLDEN_APPLE, 2);
		VALUED.put(Items.ENDER_PEARL, 3);
		VALUED.put(Items.DIAMOND, 4);
		VALUED.put(Items.EMERALD, 5);
	}

	// ------------------------------------------------------------------ AI

	/** AIPechTradePlayer: stand still while a player trades. */
	private class TradePlayerGoal extends Goal {
		TradePlayerGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
		}

		@Override
		public boolean canUse() {
			Pech p = Pech.this;
			return p.isAlive() && !p.isInWater() && p.isTamed() && p.onGround() && !p.needsSync && p.trading;
		}

		@Override
		public void start() {
			Pech.this.getNavigation().stop();
		}

		@Override
		public void stop() {
			Pech.this.trading = false;
		}
	}

	/** AIPechItemEntityGoto: wander over to anything worth having that lies within sixteen blocks, and pick it up. */
	private class ItemEntityGotoGoal extends Goal {
		private ItemEntity targetEntity;
		private int count;
		private int failedPathFindingPenalty;

		ItemEntityGotoGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			Pech p = Pech.this;
			if (p.tickCount % 5 > 0 || --this.count > 0) {
				return false;
			}
			double range = Double.MAX_VALUE;
			this.targetEntity = null;
			for (ItemEntity e : p.level().getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(16.0))) {
				if (p.canPickup(e.getItem()) && !e.entityTags().contains(PechMenu.PECH_DROP)) {
					double d = e.distanceToSqr(p);
					if (d < range && d <= 256.0) {
						range = d;
						this.targetEntity = e;
					}
				}
			}
			return this.targetEntity != null;
		}

		@Override
		public boolean canContinueToUse() {
			return this.targetEntity != null && this.targetEntity.isAlive() && !Pech.this.getNavigation().isDone()
				&& this.targetEntity.distanceToSqr(Pech.this) < 256.0;
		}

		@Override
		public void stop() {
			this.targetEntity = null;
		}

		@Override
		public void start() {
			Pech p = Pech.this;
			p.getNavigation().moveTo(this.targetEntity, p.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5);
			this.count = 0;
		}

		@Override
		public void tick() {
			Pech p = Pech.this;
			p.getLookControl().setLookAt(this.targetEntity, 30.0F, 30.0F);
			if (p.getSensing().hasLineOfSight(this.targetEntity) && --this.count <= 0) {
				this.count = this.failedPathFindingPenalty + 4 + p.getRandom().nextInt(4);
				p.getNavigation().moveTo(this.targetEntity, p.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5);
				var path = p.getNavigation().getPath();
				if (path != null && path.getEndNode() != null
					&& this.targetEntity.distanceToSqr(path.getEndNode().x, path.getEndNode().y, path.getEndNode().z) < 1.0) {
					this.failedPathFindingPenalty = 0;
				} else {
					this.failedPathFindingPenalty += 10;
				}
			}
			if (p.distanceToSqr(this.targetEntity.getX(), this.targetEntity.getBoundingBox().minY, this.targetEntity.getZ()) <= 1.5) {
				this.count = 0;
				ItemStack item = this.targetEntity.getItem().copy();
				int am = item.getCount();
				ItemStack left = p.pickupItem(item);
				if (!left.isEmpty()) {
					this.targetEntity.setItem(left);
				} else {
					this.targetEntity.discard();
				}
				if (left.isEmpty() || left.getCount() != am) {
					p.level().playSound(null, this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), SoundEvents.ITEM_PICKUP,
						net.minecraft.sounds.SoundSource.NEUTRAL, 0.2F, ((p.getRandom().nextFloat() - p.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
				}
			}
		}
	}
}
