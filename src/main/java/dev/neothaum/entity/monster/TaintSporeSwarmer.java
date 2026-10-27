package dev.neothaum.entity.monster;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * EntityTaintSporeSwarmer: a great pod that crusted taint sometimes heaves up. It sits still and, whenever a player comes near, every 25 seconds
 * splits open to release a taint swarm.
 */
public class TaintSporeSwarmer extends TaintSpore {
	private int spawnCounter = 500;

	public TaintSporeSwarmer(EntityType<? extends TaintSporeSwarmer> type, Level level) {
		super(type, level);
		this.setSporeSize(10);
		this.displaySize = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TaintSpore.createAttributes().add(Attributes.MAX_HEALTH, 75.0);
	}

	/** BlockTaint: crusted taint with open air above may become a swarmer, if there is no other within 16 blocks. */
	public static void spawnFromCrust(ServerLevel level, BlockPos pos) {
		if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL
			|| !level.getEntitiesOfClass(TaintSporeSwarmer.class, new AABB(pos).inflate(16.0)).isEmpty()) {
			return;
		}
		TaintSporeSwarmer spore = TVEntities.TAINT_SPORE_SWARMER.create(level, EntitySpawnReason.NATURAL);
		if (spore != null) {
			level.removeBlock(pos, false);
			spore.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
			level.addFreshEntity(spore);
			level.playSound(null, spore.getX(), spore.getY(), spore.getZ(), TVSounds.ROOTS, SoundSource.HOSTILE, 0.1F,
				0.9F + level.getRandom().nextFloat() * 0.2F);
		}
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return EntityDimensions.scalable(1.0F, 1.0F);
	}

	@Override
	public void handleDamageEvent(DamageSource source) {
		super.handleDamageEvent(source);
		this.sploosh(10);
	}

	@Override
	protected void sporeOnUpdate() {
		if (this.spawnCounter > 0) {
			this.spawnCounter--;
		}
		if (!this.level().isClientSide() && this.spawnCounter <= 0
			&& this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0, e -> e instanceof Player p && !p.isCreative() && !p.isSpectator())
			!= null) {
			this.spawnCounter = 500;
			this.swarmBurst(1);
		}
		if (this.level().isClientSide()) {
			this.swarm.removeIf(p -> !FX.isAlive(p));
			if (this.swarm.size() < (500 - this.spawnCounter) / 25) {
				Object fx = FX.swarm(this, 0.1F, 10.0F, 0.0F);
				if (fx != null) {
					this.swarm.add(fx);
				}
			}
		}
		if (this.deathTime == 1) {
			this.swarmBurst(1);
		}
	}

	@Override
	public void playerTouch(Player player) {
	}

	protected void swarmBurst(int amt) {
		if (this.level() instanceof ServerLevel sl) {
			sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.GORE, SoundSource.HOSTILE, 1.0F, 0.9F + sl.getRandom().nextFloat() * 0.1F);
			for (int a = 0; a < amt; a++) {
				TaintSwarm swarm = TVEntities.TAINT_SWARM.create(sl, EntitySpawnReason.MOB_SUMMONED);
				if (swarm != null) {
					swarm.snapTo(this.getX(), this.getY() + 0.5, this.getZ(), sl.getRandom().nextFloat() * 360.0F, 0.0F);
					sl.addFreshEntity(swarm);
				}
			}
			sl.broadcastEntityEvent(this, (byte) 6);
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 6) {
			this.spawnCounter = 500;
			this.sploosh(25);
			for (Object p : this.swarm) {
				FX.kill(p);
			}
			this.swarm.clear();
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.ROOTS;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		for (int a = 0; a <= 1; a++) {
			this.spawnAtLocation(level, new ItemStack(this.random.nextBoolean() ? TVItems.TAINTED_GOO : TVItems.TAINT_TENDRIL), this.getBbHeight() / 2.0F);
		}
	}
}
