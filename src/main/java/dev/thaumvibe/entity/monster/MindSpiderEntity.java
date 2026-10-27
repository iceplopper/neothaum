package dev.thaumvibe.entity.monster;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** EntityMindSpider: a tiny hallucinated or real spider summoned by warp events. */
public class MindSpiderEntity extends Spider {
	private static final EntityDataAccessor<Boolean> HARMLESS = SynchedEntityData.defineId(MindSpiderEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<String> VIEWER = SynchedEntityData.defineId(MindSpiderEntity.class, EntityDataSerializers.STRING);

	private int lifeSpan = Integer.MAX_VALUE;

	public MindSpiderEntity(EntityType<? extends MindSpiderEntity> type, Level level) {
		super(type, level);
		this.xpReward = 1;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes()
			.add(Attributes.MAX_HEALTH, 1.0)
			.add(Attributes.ATTACK_DAMAGE, 1.0)
			.add(Attributes.FOLLOW_RANGE, 12.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HARMLESS, false);
		builder.define(VIEWER, "");
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public boolean isHarmless() {
		return this.entityData.get(HARMLESS);
	}

	public void setHarmless(boolean harmless) {
		this.entityData.set(HARMLESS, harmless);
		if (harmless) {
			this.lifeSpan = 1200;
			this.xpReward = 0;
		}
	}

	public String getViewer() {
		return this.entityData.get(VIEWER);
	}

	public void setViewer(String viewer) {
		this.entityData.set(VIEWER, viewer != null ? viewer : "");
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level().isClientSide() && this.tickCount > this.lifeSpan) {
			this.discard();
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (this.isHarmless()) {
			return false;
		}
		return super.doHurtTarget(level, target);
	}

	@Override
	public float getVoicePitch() {
		return 0.7F;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		// Harmless mind spiders drop nothing
		if (!this.isHarmless()) {
			super.dropCustomDeathLoot(level, source, killedByPlayer);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Harmless", this.isHarmless());
		output.putString("Viewer", this.getViewer());
		output.putInt("LifeSpan", this.lifeSpan);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setHarmless(input.getBooleanOr("Harmless", false));
		this.setViewer(input.getStringOr("Viewer", ""));
		this.lifeSpan = input.getIntOr("LifeSpan", Integer.MAX_VALUE);
	}
}
