package dev.neothaum.entity.projectile;

import dev.neothaum.api.TaintedMob;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVMobEffects;
import dev.neothaum.world.TVBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

/** EntityBottleTaint: the bottle shatters, poisoning everything nearby with taint and tainting the ground where it lands. */
public class BottleTaint extends ThrowableItemProjectile {
	public BottleTaint(EntityType<? extends BottleTaint> type, Level level) {
		super(type, level);
	}

	public BottleTaint(Level level, LivingEntity thrower, ItemStack stack) {
		super(TVEntities.BOTTLE_TAINT, thrower, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return TVItems.BOTTLED_TAINT;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.05;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int i = 0; i < 8; i++) {
				this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(new ItemStack(TVItems.BOTTLED_TAINT))),
					this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
			}
			for (int i = 0; i < 100; i++) {
				FX.taintsplosion(this);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		for (LivingEntity el : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(5.0))) {
			if (!(el instanceof TaintedMob) && !el.is(EntityTypeTags.UNDEAD)) {
				el.addEffect(new MobEffectInstance(TVMobEffects.FLUX_TAINT, 100, 0, false, true));
			}
		}
		int x = (int) this.getX(), y = (int) this.getY(), z = (int) this.getZ();
		for (int a = 0; a < 10; a++) {
			int xx = x + (int) ((this.random.nextFloat() - this.random.nextFloat()) * 5.0F);
			int zz = z + (int) ((this.random.nextFloat() - this.random.nextFloat()) * 5.0F);
			BlockPos p = new BlockPos(xx, y, zz);
			if (level.getRandom().nextBoolean() && !TVBiomes.is(level, p, TVBiomes.TAINTED_LAND)) {
				TVBiomes.setBiomeAt(level, xx, zz, TVBiomes.TAINTED_LAND);
				if (level.getBlockState(p.below()).isRedstoneConductor(level, p.below()) && level.getBlockState(p).canBeReplaced()) {
					level.setBlock(p, TVBlocks.TAINT_FIBRES.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
		level.broadcastEntityEvent(this, (byte) 3);
		this.discard();
	}
}
