package dev.neothaum.entity.monster;

import dev.neothaum.entity.monster.boss.LongRangeAttackGoal;
import dev.neothaum.entity.monster.boss.ThaumcraftBoss;
import dev.neothaum.entity.projectile.GolemOrbEntity;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityCultistLeader, the Crimson Praetor: a boss in crimson plate with a crimson blade (a void sword on easy), sometimes enchanted. Cultists
 * within eight blocks of it regenerate. It fights hand to hand up close and lobs red golem orbs at anything more than sixteen blocks off, and
 * when hurt it rallies the cultists about it. It leaves a rare loot bag.
 */
public class CrimsonLeaderEntity extends ThaumcraftBoss implements RangedAttackMob, dev.neothaum.entity.champion.ChampionBoss {
	public static final String[] TITLES = new String[]{"Alberic", "Anselm", "Bastian", "Beturian", "Chabier", "Chorache", "Chuse", "Dodorol", "Ebardo",
		"Ferrando", "Fertus", "Guillen", "Larpe", "Obano", "Zelipe"};

	private static final EntityDataAccessor<Integer> TITLE = SynchedEntityData.defineId(CrimsonLeaderEntity.class, EntityDataSerializers.INT);

	public CrimsonLeaderEntity(EntityType<? extends CrimsonLeaderEntity> type, Level level) {
		super(type, level);
		this.xpReward = 40;
		this.bossEvent.setColor(net.minecraft.world.BossEvent.BossBarColor.RED);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return ThaumcraftBoss.createBossAttributes().add(Attributes.MAX_HEALTH, 125.0).add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 5.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TITLE, 0);
	}

	public int getTitleIndex() {
		return this.entityData.get(TITLE);
	}

	public void setTitleIndex(int idx) {
		this.entityData.set(TITLE, Mth.clamp(idx, 0, TITLES.length - 1));
	}

	/** entity.Thaumcraft.CultistLeader.name: "Praetor <title> the <modifier>". */
	@Override
	public void nameAsChampion(Component modifier) {
		this.setCustomName(Component.literal("Praetor " + TITLES[this.getTitleIndex()] + " the ").append(modifier));
	}

	@Override
	public Component getName() {
		return this.hasCustomName() ? super.getName() : Component.literal(TITLES[this.getTitleIndex()] + " the Crimson Praetor");
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new LongRangeAttackGoal(this, 16.0, 1.0, 30, 40, 24.0F));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new CultistHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TVItems.CULTIST_LEADER_HELM));
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TVItems.CULTIST_LEADER_CHEST));
		this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TVItems.CULTIST_LEADER_LEGS));
		this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TVItems.CULTIST_BOOTS));
		this.setItemSlot(EquipmentSlot.MAINHAND,
			new ItemStack(this.level().getDifficulty() == Difficulty.EASY ? TVItems.VOID_SWORD : TVItems.CRIMSON_BLADE));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
		this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
		// addRandomEnchantment: half the time at full regional difficulty
		float f = difficulty.getSpecialMultiplier();
		ItemStack weapon = this.getMainHandItem();
		if (!weapon.isEmpty() && this.random.nextFloat() < 0.5F * f) {
			this.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(this.random, weapon, (int) (7.0F + f * this.random.nextInt(22)),
				level.registryAccess(), java.util.Optional.empty()));
		}
		this.setTitleIndex(level.getRandom().nextInt(TITLES.length));
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		return !(target instanceof CultistEntity || target instanceof CrimsonLeaderEntity) && super.canAttack(target);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		for (CultistEntity c : level.getEntitiesOfClass(CultistEntity.class, this.getBoundingBox().inflate(8.0))) {
			if (!c.hasEffect(MobEffects.REGENERATION)) {
				c.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
			}
		}
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		if (!this.hasLineOfSight(target)) {
			return;
		}
		this.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
		this.getLookControl().setLookAt(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0F, target.getZ(), 30.0F, 30.0F);
		GolemOrbEntity blast = new GolemOrbEntity(this.level(), this, target, true);
		var m = blast.getDeltaMovement();
		blast.setPos(blast.getX() + m.x / 2.0, blast.getY(), blast.getZ() + m.z / 2.0);
		double d0 = target.getX() - this.getX();
		double d1 = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
		double d2 = target.getZ() - this.getZ();
		blast.heading(d0, d1 + 2.0, d2, 0.66F, 3.0F);
		this.playSound(TVSounds.EGATTACK, 1.0F, 1.0F + this.random.nextFloat() * 0.1F);
		this.level().addFreshEntity(blast);
	}

	/** Only the rare loot bag (no pearl, and none of its gear). */
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.spawnAtLocation(level, new ItemStack(TVItems.LOOT_BAG_RARE), 1.5F);
	}

	@Override
	protected void dropEquipment(ServerLevel level) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putByte("title", (byte) this.getTitleIndex());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setTitleIndex(input.getByteOr("title", (byte) 0));
	}
}
