package dev.neothaum.entity.monster.boss;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.entity.monster.CrimsonClericEntity;
import dev.neothaum.entity.monster.CrimsonKnightEntity;
import dev.neothaum.entity.monster.CrimsonLeaderEntity;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.BannerBlockEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 1:1 Port of EntityCultistPortal.
 * Crimson rift boss summoned by the Crimson Cult.
 * 500 HP, summons waves of Knights and Clerics, erecting banners, and culminating in the Crimson Praetor.
 */
public class CultistPortalEntity extends Monster {
	private static final EntityDataAccessor<Integer> PULSE = SynchedEntityData.defineId(CultistPortalEntity.class, EntityDataSerializers.INT);

	private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), Component.literal("Cultist Portal"),
		BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

	private int stage = 0;
	private int stageCounter = 200;

	public CultistPortalEntity(EntityType<? extends CultistPortalEntity> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 500.0)
			.add(Attributes.MOVEMENT_SPEED, 0.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
			.add(Attributes.ARMOR, 5.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PULSE, 0);
	}

	public int getPulse() {
		return this.entityData.get(PULSE);
	}

	public void setPulse(int pulse) {
		this.entityData.set(PULSE, pulse);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void push(Entity entity) {}

	@Override
	public void push(double x, double y, double z) {}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.bossEvent.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossEvent.removePlayer(player);
	}

	@Override
	public void tick() {
		super.tick();
		this.setDeltaMovement(Vec3.ZERO);

		if (this.level().isClientSide()) {
			int p = this.getPulse();
			if (p > 0) {
				this.setPulse(p - 1);
			}
			if (this.random.nextInt(3) == 0) {
				double rx = this.getX() + (this.random.nextDouble() - 0.5) * 2.0;
				double ry = this.getY() + 0.5 + this.random.nextDouble() * 2.5;
				double rz = this.getZ() + (this.random.nextDouble() - 0.5) * 2.0;
				this.level().addParticle(ParticleTypes.FLAME, rx, ry, rz, 0, 0.02, 0);
			}
			return;
		}

		this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

		// Proximity zap
		AABB zapArea = this.getBoundingBox().inflate(3.0);
		List<Player> nearby = this.level().getEntitiesOfClass(Player.class, zapArea);
		for (Player p : nearby) {
			if (!p.isCreative() && !p.isSpectator()) {
				p.hurtServer((ServerLevel) this.level(), this.damageSources().mobAttack(this), 8.0F);
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.ZAP, SoundSource.HOSTILE, 1.0F, 1.0F);
			}
		}

		if (this.stageCounter <= 0) {
			Player target = this.level().getNearestPlayer(this, 48.0);
			if (target != null) {
				this.setPulse(10);
				this.level().broadcastEntityEvent(this, (byte) 16);

				if (this.stage < 12) {
					int timing = this.getTiming();
					this.stageCounter = Math.max(20, timing + this.random.nextInt(10));
					this.spawnMinion();
				} else if (this.stage == 12) {
					this.stageCounter = 80;
					this.spawnBoss();
				} else {
					this.stageCounter = 40;
					this.hurtServer((ServerLevel) this.level(), this.damageSources().generic(), 5.0F + this.random.nextInt(5));
				}
				this.stage++;
			} else {
				this.stageCounter = 30 + this.random.nextInt(30);
			}
		} else {
			this.stageCounter--;
			// Stage 0 banner placement
			if (this.stage == 0 && this.stageCounter == 160) {
				this.spawnBanners();
			}
		}

		// Passive regeneration before boss phase
		if (this.stage < 12 && this.tickCount % 20 == 0) {
			this.heal(1.0F);
		}
	}

	private int getTiming() {
		AABB area = this.getBoundingBox().inflate(32.0);
		List<LivingEntity> cultists = this.level().getEntitiesOfClass(LivingEntity.class, area,
			e -> e instanceof CrimsonKnightEntity || e instanceof CrimsonClericEntity);
		return cultists.size() * 20;
	}

	private void spawnBanners() {
		if (!(this.level() instanceof ServerLevel sl)) return;
		Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
		BlockPos center = this.blockPosition();
		for (Direction dir : dirs) {
			BlockPos bp = center.relative(dir, 6);
			// Find ground level
			while (bp.getY() > sl.getMinY() && sl.isEmptyBlock(bp.below())) {
				bp = bp.below();
			}
			if (sl.isEmptyBlock(bp)) {
				sl.setBlockAndUpdate(bp, TVBlocks.BANNER.defaultBlockState());
				if (sl.getBlockEntity(bp) instanceof BannerBlockEntity banner) {
					banner.setFacing(dir.getOpposite());
				}
			}
		}
	}

	private void spawnMinion() {
		if (!(this.level() instanceof ServerLevel sl)) return;
		Mob cultist;
		if (this.random.nextFloat() > 0.33F) {
			cultist = TVEntities.CULTIST_KNIGHT.create(sl, EntitySpawnReason.EVENT);
		} else {
			cultist = TVEntities.CULTIST_CLERIC.create(sl, EntitySpawnReason.EVENT);
		}
		if (cultist != null) {
			double sx = this.getX() + (this.random.nextDouble() - this.random.nextDouble()) * 1.5;
			double sy = this.getY() + 0.25;
			double sz = this.getZ() + (this.random.nextDouble() - this.random.nextDouble()) * 1.5;
			cultist.snapTo(sx, sy, sz, this.random.nextFloat() * 360.0F, 0.0F);
			sl.addFreshEntity(cultist);
			sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.WANDFAIL, SoundSource.HOSTILE, 1.0F, 1.0F);
		}
	}

	private void spawnBoss() {
		if (!(this.level() instanceof ServerLevel sl)) return;
		CrimsonLeaderEntity leader = TVEntities.CULTIST_LEADER.create(sl, EntitySpawnReason.EVENT);
		if (leader != null) {
			double sx = this.getX() + (this.random.nextDouble() - this.random.nextDouble()) * 1.5;
			double sy = this.getY() + 0.25;
			double sz = this.getZ() + (this.random.nextDouble() - this.random.nextDouble()) * 1.5;
			leader.snapTo(sx, sy, sz, this.random.nextFloat() * 360.0F, 0.0F);
			sl.addFreshEntity(leader);
			sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.WANDFAIL, SoundSource.HOSTILE, 1.0F, 1.0F);
		}
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
		if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
			sl.explode(this, this.getX(), this.getY() + 1.0, this.getZ(), 2.5F, Level.ExplosionInteraction.MOB);
			// Guaranteed drop of Crimson Rites and Rare Loot Bag
			this.spawnAtLocation(sl, new ItemStack(TVItems.CRIMSON_RITES));
			this.spawnAtLocation(sl, new ItemStack(TVItems.LOOT_BAG_RARE));
			if (this.random.nextBoolean()) {
				this.spawnAtLocation(sl, new ItemStack(TVItems.PRIMORDIAL_PEARL));
			}
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 16) {
			this.setPulse(10);
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("stage", this.stage);
		output.putInt("stagecounter", this.stageCounter);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.stage = input.getIntOr("stage", 0);
		this.stageCounter = input.getIntOr("stagecounter", 200);
	}
}
