package dev.neothaum.entity.monster;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.registry.TVItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityGiantBrainyZombie: a hulking brainy zombie that grows with its anger. Every blow it takes swells it a little (up to twice its calm
 * size), it hits harder while enraged and the rage slowly ebbs away.
 */
public class GiantBrainyZombie extends BrainyZombie {
	private static final EntityDataAccessor<Float> ANGER = SynchedEntityData.defineId(GiantBrainyZombie.class, EntityDataSerializers.FLOAT);

	public GiantBrainyZombie(EntityType<? extends GiantBrainyZombie> type, Level level) {
		super(type, level);
		this.xpReward = 15;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return BrainyZombie.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, 7.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(ANGER, 1.0F);
	}

	public float getAnger() {
		return this.entityData.get(ANGER);
	}

	public void setAnger(float anger) {
		this.entityData.set(ANGER, anger);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		super.onSyncedDataUpdated(accessor);
		if (ANGER.equals(accessor)) {
			this.refreshDimensions();
		}
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return super.getDefaultDimensions(pose).scale(1.2F + this.getAnger());
	}

	@Override
	public boolean isBaby() {
		return false;
	}

	@Override
	public void setBaby(boolean baby) {
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.getAnger() > 1.0F) {
			this.setAnger(this.getAnger() - 0.002F);
		}
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.0F + (this.getAnger() - 1.0F) * 5.0F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		this.setAnger(Math.min(2.0F, this.getAnger() + 0.1F));
		return super.hurtServer(level, source, damage);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putFloat("Anger", this.getAnger());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setAnger(input.getFloatOr("Anger", 1.0F));
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		int looting = TVEntities.looting(level, source);
		for (int a = 0; a < 12; a++) {
			if (this.random.nextBoolean()) {
				this.spawnAtLocation(level, new ItemStack(Items.ROTTEN_FLESH, 2));
			}
		}
		if (this.random.nextInt(10) - looting <= 4) {
			this.spawnAtLocation(level, new ItemStack(TVItems.ZOMBIE_BRAIN), 2.0F);
		}
		// EntityLiving.onDeath: a rare drop for recent player kills
		if (killedByPlayer && this.random.nextInt(200) - looting < 5) {
			switch (this.random.nextInt(4)) {
				case 0 -> this.spawnAtLocation(level, new ItemStack(TVItems.THAUMIUM_INGOT), 2.0F);
				case 1 -> this.spawnAtLocation(level, Items.CARROT);
				case 2 -> this.spawnAtLocation(level, Items.POTATO);
				default -> this.spawnAtLocation(level, new ItemStack(TVItems.AMBER), 2.0F);
			}
		}
	}
}
