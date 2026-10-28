package dev.neothaum.entity.monster;

import dev.neothaum.registry.TVItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * CrimsonKnightEntity: melee armored cultist with heavy cultist plate and sword.
 */
public class CrimsonKnightEntity extends CultistEntity {
	public CrimsonKnightEntity(EntityType<? extends CrimsonKnightEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return CultistEntity.createAttributes()
			.add(Attributes.MAX_HEALTH, 36.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
		this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new CultistHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TVItems.CULTIST_PLATE_HELM));
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TVItems.CULTIST_PLATE_CHEST));
		this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TVItems.CULTIST_PLATE_LEGS));
		this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TVItems.CULTIST_BOOTS));

		float hardChance = difficulty.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F;
		if (random.nextFloat() < hardChance) {
			int roll = random.nextInt(5);
			if (roll == 0) {
				this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TVItems.VOID_SWORD));
				this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TVItems.CULTIST_ROBE_HELM));
			} else {
				this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TVItems.THAUMIUM_SWORD));
				if (random.nextBoolean()) {
					this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
				}
			}
		} else {
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason spawnReason,
		SpawnGroupData spawnGroupData) {
		this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
		return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
	}
}
