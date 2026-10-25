package dev.thaumvibe.entity.monster;

import dev.thaumvibe.api.TaintedMob;
import dev.thaumvibe.blocks.TaintFibreBlock;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVMobEffects;
import dev.thaumvibe.world.TVBiomes;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityTaintCreeper: a creeper full of taint. Its blast breaks nothing, but poisons everything around it with flux taint and splashes the
 * ground with taint fibres and tainted land.
 */
public class TaintCreeper extends TaintedBeast {
	private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(TaintCreeper.class, EntityDataSerializers.INT);
	private int lastActiveTime;
	private int timeSinceIgnited;
	private int fuseTime = 30;

	public TaintCreeper(EntityType<? extends TaintCreeper> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 2.0)
			.add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new SwellGoal());
		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(STATE, -1);
	}

	public int getCreeperState() {
		return this.entityData.get(STATE);
	}

	public void setCreeperState(int state) {
		this.entityData.set(STATE, state);
	}

	@Override
	public int getMaxFallDistance() {
		return this.getTarget() == null ? 3 : 3 + (int) (this.getHealth() - 1.0F);
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		boolean b = super.causeFallDamage(distance, multiplier, source);
		this.timeSinceIgnited = Math.min(this.fuseTime - 5, (int) (this.timeSinceIgnited + distance * 1.5F));
		return b;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return true;
	}

	public float getSwelling(float a) {
		return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * a) / 28.0F;
	}

	@Override
	public void tick() {
		if (this.isAlive()) {
			this.lastActiveTime = this.timeSinceIgnited;
			int state = this.getCreeperState();
			if (state > 0 && this.timeSinceIgnited == 0) {
				this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
			}
			this.timeSinceIgnited = Math.max(0, this.timeSinceIgnited + state);
			if (this.timeSinceIgnited >= 30) {
				this.timeSinceIgnited = 30;
				if (this.level() instanceof ServerLevel level) {
					this.explode(level);
					return;
				}
				for (int a = 0; a < 100; a++) {
					FX.taintsplosion(this);
				}
			}
		}
		super.tick();
	}

	private void explode(ServerLevel level) {
		level.explode(this, this.getX(), this.getY() + this.getBbHeight() / 2.0F, this.getZ(), 1.5F, Level.ExplosionInteraction.NONE);
		for (LivingEntity el : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0))) {
			if (!(el instanceof TaintedMob) && !el.isInvertedHealAndHarm()) {
				el.addEffect(new MobEffectInstance(TVMobEffects.FLUX_TAINT, 100, 0));
			}
		}
		int x = (int) this.getX(), y = (int) this.getY(), z = (int) this.getZ();
		TaintFibreBlock fibres = (TaintFibreBlock) TVBlocks.TAINT_FIBRES;
		for (int a = 0; a < 10; a++) {
			int xx = x + (int) ((this.random.nextFloat() - this.random.nextFloat()) * 5.0F);
			int zz = z + (int) ((this.random.nextFloat() - this.random.nextFloat()) * 5.0F);
			BlockPos p = new BlockPos(xx, y, zz);
			if (level.getRandom().nextBoolean() && !TVBiomes.is(level, p, TVBiomes.TAINTED_LAND)) {
				TVBiomes.setBiomeAt(level, xx, zz, TVBiomes.TAINTED_LAND);
				if (level.getBlockState(p.below()).isRedstoneConductor(level, p.below()) && level.getBlockState(p).canBeReplaced()) {
					level.setBlock(p, fibres.ofType(level, p, 0), Block.UPDATE_ALL);
				}
			}
		}
		level.broadcastEntityEvent(this, (byte) 61);
		this.discard();
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 61) {
			for (int a = 0; a < 100; a++) {
				FX.taintsplosion(this);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.CREEPER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.CREEPER_DEATH;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putShort("Fuse", (short) this.fuseTime);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.fuseTime = input.getShortOr("Fuse", (short) 30);
	}

	/** AICreeperSwell: swell up when the target is within three blocks and in sight, and calm down past seven. */
	private class SwellGoal extends Goal {
		private LivingEntity target;

		SwellGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			LivingEntity t = TaintCreeper.this.getTarget();
			return TaintCreeper.this.getCreeperState() > 0 || t != null && TaintCreeper.this.distanceToSqr(t) < 9.0;
		}

		@Override
		public void start() {
			TaintCreeper.this.getNavigation().stop();
			this.target = TaintCreeper.this.getTarget();
		}

		@Override
		public void stop() {
			this.target = null;
		}

		@Override
		public void tick() {
			TaintCreeper c = TaintCreeper.this;
			if (this.target == null || c.distanceToSqr(this.target) > 49.0 || !c.getSensing().hasLineOfSight(this.target)) {
				c.setCreeperState(-1);
			} else {
				c.setCreeperState(1);
			}
		}
	}
}
