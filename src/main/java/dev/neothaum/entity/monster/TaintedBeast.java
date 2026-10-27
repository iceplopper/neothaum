package dev.neothaum.entity.monster;

import dev.neothaum.api.TaintedMob;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * What EntityTaintChicken, Cow, Pig, Sheep and Villager share: a farm animal twisted by the taint that hunts players, villagers and (most of
 * them) other animals, never despawns, bursts into being in a spray of purple slime, and squeals an octave lower than it used to.
 */
public abstract class TaintedBeast extends Monster implements TaintedMob {
	protected TaintedBeast(EntityType<? extends TaintedBeast> type, Level level) {
		super(type, level);
	}

	/** Whether it goes after other animals too (all but the sheep and the villager). */
	protected boolean huntsAnimals() {
		return true;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, false));
		if (this.huntsAnimals()) {
			this.targetSelector.addGoal(8, new NearestAttackableTargetGoal<>(this, Animal.class, false));
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide() && this.tickCount < 5) {
			for (int a = 0; a < 10; a++) {
				FX.sploosh(this);
			}
		}
	}

	@Override
	public float getVoicePitch() {
		return 0.7F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	/** One chance in {@code oneIn} of a drop, then even odds of tainted goo or a taint tendril. */
	protected void dropTaint(ServerLevel level, int oneIn) {
		if (this.random.nextInt(oneIn) == 0) {
			this.spawnAtLocation(level, new ItemStack(this.random.nextBoolean() ? TVItems.TAINTED_GOO : TVItems.TAINT_TENDRIL), this.getBbHeight() / 2.0F);
		}
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.dropTaint(level, 1);
	}
}
