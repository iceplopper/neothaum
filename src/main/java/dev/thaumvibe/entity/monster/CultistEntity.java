package dev.thaumvibe.entity.monster;

import dev.thaumvibe.registry.TVItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * CultistEntity: base class for Crimson Cultists (Knight, Cleric).
 * Open doors, coordinate attacks, drop fragments, void seeds, gold coins, and rare Crimson Rites.
 */
public abstract class CultistEntity extends Monster {
	private BlockPos homePos = null;
	private int homeRadius = -1;

	protected CultistEntity(EntityType<? extends CultistEntity> type, Level level) {
		super(type, level);
		this.xpReward = 10;
		if (this.getNavigation() instanceof GroundPathNavigation gpn) {
			gpn.setCanOpenDoors(true);
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 4.0);
	}

	public void setHome(BlockPos pos, int radius) {
		this.homePos = pos;
		this.homeRadius = radius;
		this.setHomeTo(pos, radius);
	}

	public BlockPos getHomePos() {
		return this.homePos;
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (target instanceof CultistEntity || target instanceof CrimsonLeaderEntity) {
			return false;
		}
		return super.canAttack(target);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		int r = this.random.nextInt(10);
		if (r == 0) {
			this.spawnAtLocation(level, new ItemStack(TVItems.KNOWLEDGE_FRAGMENT));
		} else if (r <= 1) {
			this.spawnAtLocation(level, new ItemStack(TVItems.VOID_SEED));
		} else if (r <= 3) {
			this.spawnAtLocation(level, new ItemStack(TVItems.GOLD_COIN));
		}

		// Rare drop: Crimson Rites
		if (this.random.nextFloat() < (recentlyHit ? 0.05F : 0.02F)) {
			this.spawnAtLocation(level, new ItemStack(TVItems.CRIMSON_RITES));
		}
	}
}
