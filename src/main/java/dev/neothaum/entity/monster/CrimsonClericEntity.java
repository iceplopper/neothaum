package dev.neothaum.entity.monster;

import dev.neothaum.entity.projectile.GolemOrbEntity;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * CrimsonClericEntity: crimson robe caster cultist with fireballs, homing energy orbs, and chanting rituals.
 */
public class CrimsonClericEntity extends CultistEntity implements RangedAttackMob {
	private static final EntityDataAccessor<Boolean> RITUALIST = SynchedEntityData.defineId(CrimsonClericEntity.class, EntityDataSerializers.BOOLEAN);

	public CrimsonClericEntity(EntityType<? extends CrimsonClericEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return CultistEntity.createAttributes()
			.add(Attributes.MAX_HEALTH, 30.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(RITUALIST, false);
	}

	public boolean isRitualist() {
		return this.entityData.get(RITUALIST);
	}

	public void setRitualist(boolean ritualist) {
		this.entityData.set(RITUALIST, ritualist);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 20, 40, 24.0F));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
		this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(CultistEntity.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TVItems.CULTIST_ROBE_HELM));
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TVItems.CULTIST_ROBE_CHEST));
		this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TVItems.CULTIST_ROBE_LEGS));

		float bootChance = difficulty.getDifficulty() == Difficulty.HARD ? 0.3F : 0.1F;
		if (random.nextFloat() < bootChance) {
			this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TVItems.CULTIST_BOOTS));
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason spawnReason,
		SpawnGroupData spawnGroupData) {
		this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
		return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		double dx = target.getX() - this.getX();
		double dy = target.getY(0.5) - this.getY(0.5);
		double dz = target.getZ() - this.getZ();

		if (this.random.nextFloat() > 0.66F) {
			// Shoot homing golem orb
			GolemOrbEntity orb = new GolemOrbEntity(this.level(), this, target, true);
			orb.setPos(this.getX(), this.getY(0.5) + 0.5, this.getZ());
			orb.heading(dx, dy + 2.0, dz, 0.66F, 3.0F);
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.EGATTACK, SoundSource.HOSTILE, 1.0F,
				1.0F + this.random.nextFloat() * 0.1F);
			this.level().addFreshEntity(orb);
		} else {
			// Shoot 3 small fireballs
			this.level().levelEvent(null, 1018, this.blockPosition(), 0);
			for (int i = 0; i < 3; i++) {
				double spread = (this.random.nextDouble() - 0.5) * 0.4;
				Vec3 dir = new Vec3(dx + spread, dy, dz + spread).normalize();
				SmallFireball fb = new SmallFireball(this.level(), this, dir);
				fb.setPos(this.getX(), this.getY(0.5) + 0.5, this.getZ());
				this.level().addFreshEntity(fb);
			}
		}
	}

	@Override
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount) {
		this.setRitualist(false);
		return super.hurtServer(level, source, amount);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.CHANT;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 500;
	}
}
