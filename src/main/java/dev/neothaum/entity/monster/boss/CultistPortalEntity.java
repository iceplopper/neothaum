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
		ServerLevel sl = (ServerLevel) this.level();
		if (this.stageCounter <= 0) {
			if (sl.getNearestPlayer(this, 48.0) != null) {
				this.setPulse(10);
				sl.broadcastEntityEvent(this, (byte) 16);
				if (this.stage <= 4) {
					this.stageCounter = 15 + this.random.nextInt(10 - this.stage) - this.stage;
					this.spawnMinion();
				} else if (this.stage == 12) {
					this.stageCounter = 50 + this.getTiming() * 2 + this.random.nextInt(50);
					this.spawnBoss();
				} else {
					int t = this.getTiming();
					this.stageCounter = t + this.random.nextInt(5 + t / 3);
					this.spawnMinion();
				}
				this.stage++;
			} else {
				this.stageCounter = 30 + this.random.nextInt(30);
			}
		} else {
			this.stageCounter--;
			if (this.stageCounter == 160 && this.stage == 0) {
				this.spawnBanners();
			}
			if (this.stageCounter > 20 && this.stageCounter < 150 && this.stage == 0 && this.stageCounter % 13 == 0) {
				this.spawnLootCrate(sl);
			}
		}
		// it mends itself until the Praetor comes
		if (this.stage < 12) {
			this.heal(1.0F);
		}
	}

	/** onCollideWithPlayer: touching the portal zaps for 8. */
	@Override
	public void playerTouch(Player player) {
		if (this.level() instanceof ServerLevel sl && this.distanceToSqr(player) < 3.0
			&& player.hurtServer(sl, this.damageSources().mobAttack(this), 8.0F)) {
			this.playSound(TVSounds.ZAP, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F + 1.0F);
		}
	}

	/** Twenty points a cultist within 32 blocks: the more there are, the slower the waves. */
	private int getTiming() {
		return this.level().getEntitiesOfClass(dev.neothaum.entity.monster.CultistEntity.class, this.getBoundingBox().inflate(32.0)).size() * 20;
	}

	/** Four crimson banners six blocks out, facing in. */
	private void spawnBanners() {
		if (!(this.level() instanceof ServerLevel sl)) {
			return;
		}
		sl.broadcastEntityEvent(this, (byte) 16);
		for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
			BlockPos bp = this.blockPosition().relative(dir, 6);
			sl.setBlockAndUpdate(bp, TVBlocks.BANNER.defaultBlockState());
			if (sl.getBlockEntity(bp) instanceof BannerBlockEntity banner) {
				banner.setFacing(dir.getOpposite());
				this.playSound(TVSounds.WANDFAIL, 1.0F, 1.0F);
			}
		}
	}

	/** Early on, loot crates pop up near the portal: rare 5%, uncommon 15%, else common. */
	private void spawnLootCrate(ServerLevel sl) {
		int x = (int) this.getX() + this.random.nextInt(5) - this.random.nextInt(5);
		int z = (int) this.getZ() + this.random.nextInt(5) - this.random.nextInt(5);
		BlockPos at = new BlockPos(x, (int) this.getY(), z);
		if (x != (int) this.getX() && z != (int) this.getZ() && sl.isEmptyBlock(at)) {
			sl.broadcastEntityEvent(this, (byte) 16);
			float rr = this.random.nextFloat();
			sl.setBlockAndUpdate(at, (rr < 0.05F ? TVBlocks.LOOT_CRATE_RARE : rr < 0.2F ? TVBlocks.LOOT_CRATE_UNCOMMON : TVBlocks.LOOT_CRATE_COMMON)
				.defaultBlockState());
			this.playSound(TVSounds.WANDFAIL, 1.0F, 1.0F);
		}
	}

	/** A knight (two in three) or a cleric, leashed within 32. Once the Praetor is out, every one costs the portal 5 to 9 health. */
	private void spawnMinion() {
		if (!(this.level() instanceof ServerLevel sl)) {
			return;
		}
		Mob cultist = this.random.nextFloat() > 0.33F ? TVEntities.CULTIST_KNIGHT.create(sl, EntitySpawnReason.EVENT)
			: TVEntities.CULTIST_CLERIC.create(sl, EntitySpawnReason.EVENT);
		if (cultist != null) {
			this.placeSummoned(sl, cultist);
		}
		if (this.stage > 12) {
			this.hurtServer(sl, this.damageSources().fellOutOfWorld(), 5 + this.random.nextInt(5));
		}
	}

	private void spawnBoss() {
		if (this.level() instanceof ServerLevel sl) {
			CrimsonLeaderEntity leader = TVEntities.CULTIST_LEADER.create(sl, EntitySpawnReason.EVENT);
			if (leader != null) {
				this.placeSummoned(sl, leader);
			}
		}
	}

	private void placeSummoned(ServerLevel sl, Mob mob) {
		mob.snapTo(this.getX() + this.random.nextFloat() - this.random.nextFloat(), this.getY() + 0.25,
			this.getZ() + this.random.nextFloat() - this.random.nextFloat(), this.random.nextFloat() * 360.0F, 0.0F);
		mob.finalizeSpawn(sl, sl.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.EVENT, null);
		mob.setHomeTo(this.blockPosition(), 32);
		mob.spawnAnim();
		sl.addFreshEntity(mob);
		mob.playSound(TVSounds.WANDFAIL, 1.0F, 1.0F);
	}

	/** It bursts (no harm to blocks) and leaves a Primordial Pearl. */
	@Override
	public void die(DamageSource damageSource) {
		if (this.level() instanceof ServerLevel sl) {
			sl.explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
		}
		super.die(damageSource);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		var pearl = new net.minecraft.world.entity.item.ItemEntity(level, this.getX(), this.getY() + this.getBbHeight() / 2.0F, this.getZ(),
			new ItemStack(TVItems.PRIMORDIAL_PEARL));
		pearl.setDeltaMovement(Vec3.ZERO);
		level.addFreshEntity(pearl);
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
