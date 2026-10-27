package dev.neothaum.entity.monster;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * InhabitedZombieEntity: zombie inhabited by an eldritch crab, wearing cultist plate.
 * Erupts into a helmeted Eldritch Crab upon death.
 */
public class InhabitedZombieEntity extends Zombie {
	public InhabitedZombieEntity(EntityType<? extends InhabitedZombieEntity> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 30.0)
			.add(Attributes.ATTACK_DAMAGE, 5.0)
			.add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
	}

	@Override
	protected void addBehaviourGoals() {
		super.addBehaviourGoals();
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		float diff = difficulty.getDifficulty() == Difficulty.HARD ? 0.9F : 0.6F;
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TVItems.CULTIST_PLATE_HELM));
		if (random.nextFloat() <= diff) {
			this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TVItems.CULTIST_PLATE_CHEST));
		}
		if (random.nextFloat() <= diff) {
			this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TVItems.CULTIST_PLATE_LEGS));
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason spawnReason,
		SpawnGroupData spawnGroupData) {
		this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
		return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
	}

	@Override
	public void die(DamageSource source) {
		if (this.level() instanceof ServerLevel sl) {
			EldritchCrabEntity crab = new EldritchCrabEntity(TVEntities.ELDRITCH_CRAB, sl);
			crab.setPos(this.getX(), this.getY() + this.getEyeHeight() * 0.5, this.getZ());
			crab.setYRot(this.getYRot());
			crab.setXRot(this.getXRot());
			crab.setHelm(true);
			sl.addFreshEntity(crab);

			sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
			sl.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(0.5), this.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
		}
		super.die(source);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.CRABTALK;
	}
}
