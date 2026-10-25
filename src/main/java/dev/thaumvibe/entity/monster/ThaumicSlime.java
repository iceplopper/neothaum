package dev.thaumvibe.entity.monster;

import dev.thaumvibe.api.TaintedMob;
import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * EntityThaumicSlime: purple slime curdled out of flux goo. Left alone, thaumic slimes creep together and merge into ever larger blobs (up to
 * size 100); near a player they hop at them, and big ones spit off little slimes like shots. Killed, a big one bursts into small ones.
 */
public class ThaumicSlime extends Monster implements TaintedMob {
	private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(ThaumicSlime.class, EntityDataSerializers.INT);

	public float squishAmount;
	public float squishFactor;
	public float prevSquishFactor;
	private int slimeJumpDelay;
	private int launched = 10;
	private int spitCounter = 100;
	private boolean wasOnGround;

	public ThaumicSlime(EntityType<? extends ThaumicSlime> type, Level level) {
		super(type, level);
		// TC4 steers the slime itself; a move control would zero its hops every tick
		this.moveControl = new MoveControl(this) {
			@Override
			public void tick() {
			}
		};
		this.slimeJumpDelay = this.random.nextInt(20) + 10;
		this.setSlimeSize(1 << this.random.nextInt(3));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.2);
	}

	/** The spitting constructor: a size 1 slime flung from {@code thrower} at {@code target}. */
	public static ThaumicSlime spit(ServerLevel level, LivingEntity thrower, LivingEntity target) {
		ThaumicSlime s = TVEntities.THAUMIC_SLIME.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (s == null) {
			return null;
		}
		s.setSlimeSize(1);
		double y = (thrower.getBoundingBox().minY + thrower.getBoundingBox().maxY) / 2.0;
		double dx = target.getX() - thrower.getX();
		double dy = target.getBoundingBox().minY + target.getBbHeight() / 3.0F - y;
		double dz = target.getZ() - thrower.getZ();
		double d = Math.sqrt(dx * dx + dz * dz);
		if (d >= 1.0E-7) {
			float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
			float pitch = (float) -(Math.atan2(dy, d) * 180.0 / Math.PI);
			s.snapTo(thrower.getX() + dx / d, y, thrower.getZ() + dz / d, yaw, pitch);
			float lift = (float) d * 0.2F;
			s.shoot(dx, dy + lift, dz, 1.5F, 1.0F);
		} else {
			s.snapTo(thrower.getX(), y, thrower.getZ(), 0.0F, 0.0F);
		}
		return s;
	}

	/** setThrowableHeading */
	private void shoot(double x, double y, double z, float speed, float spread) {
		double len = Math.sqrt(x * x + y * y + z * z);
		x = x / len + this.random.nextGaussian() * 0.0075F * spread;
		y = y / len + this.random.nextGaussian() * 0.0075F * spread;
		z = z / len + this.random.nextGaussian() * 0.0075F * spread;
		this.setDeltaMovement(x * speed, y * speed, z * speed);
		double h = Math.sqrt(x * x + z * z);
		this.setYRot((float) (Math.atan2(x, z) * 180.0 / Math.PI));
		this.setXRot((float) (Math.atan2(y, h) * 180.0 / Math.PI));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(SIZE, 1);
	}

	public void setSlimeSize(int size) {
		this.entityData.set(SIZE, size);
		this.refreshDimensions();
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(size);
		this.setHealth(this.getMaxHealth());
		this.xpReward = (int) Math.sqrt(size);
	}

	public int getSlimeSize() {
		return this.entityData.get(SIZE);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		super.onSyncedDataUpdated(accessor);
		if (SIZE.equals(accessor)) {
			this.refreshDimensions();
		}
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		float ss = (float) Math.sqrt(this.getSlimeSize());
		return EntityDimensions.scalable(0.25F * ss + 0.25F, 0.25F * ss + 0.25F);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Size", this.getSlimeSize() - 1);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setSlimeSize(input.getIntOr("Size", 0) + 1);
	}

	private SoundEvent slimeSound() {
		return this.getSlimeSize() > 3 ? SoundEvents.SLIME_SQUISH : SoundEvents.SLIME_SQUISH_SMALL;
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide() && this.level().getDifficulty() == Difficulty.PEACEFUL && this.getSlimeSize() > 0) {
			this.discard();
			return;
		}
		this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5F;
		this.prevSquishFactor = this.squishFactor;
		boolean flag = this.wasOnGround;
		super.tick();
		int i = (int) Math.sqrt(this.getSlimeSize());
		if (this.launched > 0) {
			this.launched--;
			if (this.level().isClientSide()) {
				for (int j = 0; j < i * (this.launched + 1); j++) {
					FX.slimeJump(this, i);
				}
			}
		}
		if (this.onGround() && !flag) {
			if (this.level().isClientSide()) {
				for (int j = 0; j < i * 8; j++) {
					FX.slimeJump(this, i);
				}
			}
			if (this.getSlimeSize() > 5) {
				this.playSound(this.slimeSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			}
			this.squishAmount = -0.5F;
		} else if (!this.onGround() && flag) {
			this.squishAmount = 1.0F;
		}
		this.wasOnGround = this.onGround();
		this.squishAmount *= 0.6F;
	}

	private ThaumicSlime getClosestMergableSlime() {
		ThaumicSlime closest = null;
		double distance = Double.MAX_VALUE;
		List<ThaumicSlime> ents = this.level().getEntitiesOfClass(ThaumicSlime.class, new AABB(this.position(), this.position()).inflate(16.0, 8.0, 16.0));
		for (ThaumicSlime slime : ents) {
			// as TC4 wrote it: the last slime looked at sets the distance to beat, so "closest" is only roughly so
			if (slime.getId() != this.getId() && slime.tickCount > 100 && slime.getSlimeSize() < 100 && this.distanceToSqr(slime) < distance) {
				closest = slime;
			}
			distance = this.distanceToSqr(slime);
		}
		return closest;
	}

	private void face(Entity e, float maxYaw) {
		double dx = e.getX() - this.getX();
		double dz = e.getZ() - this.getZ();
		float target = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
		this.setYRot(this.getYRot() + Mth.clamp(Mth.wrapDegrees(target - this.getYRot()), -maxYaw, maxYaw));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		Player player = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0,
			e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
		if (player != null) {
			if (this.spitCounter > 0) {
				this.spitCounter--;
			}
			this.face(player, 10.0F);
			if (this.distanceTo(player) > 4.0F && this.spitCounter <= 0 && this.getSlimeSize() > 3) {
				this.spitCounter = 101;
				ThaumicSlime fly = spit(level, this, player);
				if (fly != null) {
					level.addFreshEntity(fly);
				}
				level.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.GORE, SoundSource.HOSTILE, 1.0F,
					((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
				this.setSlimeSize(this.getSlimeSize() - 1);
			}
		} else {
			ThaumicSlime slime = this.getClosestMergableSlime();
			if (slime != null) {
				this.face(slime, 10.0F);
				if (this.distanceTo(slime) < this.getBbWidth() + slime.getBbWidth()) {
					slime.setSlimeSize(Math.min(100, slime.getSlimeSize() + this.getSlimeSize()));
					this.discard();
					return;
				}
			}
		}
		this.yBodyRot = this.getYRot();
		this.yHeadRot = this.getYRot();
		if (this.onGround() && this.slimeJumpDelay-- <= 0) {
			this.slimeJumpDelay = this.random.nextInt(16) + 8;
			if (player != null) {
				this.slimeJumpDelay /= 3;
			}
			this.getJumpControl().jump();
			if (this.getSlimeSize() > 3) {
				this.playSound(this.slimeSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
			}
			this.xxa = 1.0F - this.random.nextFloat() * 2.0F;
			this.zza = (float) Math.sqrt(this.getSlimeSize());
		} else if (this.onGround()) {
			this.xxa = this.zza = 0.0F;
		}
	}

	/** setDead: a big slime killed bursts into small ones. */
	@Override
	public void remove(Entity.RemovalReason reason) {
		int i = (int) Math.sqrt(this.getSlimeSize());
		if (this.level() instanceof ServerLevel level && reason == Entity.RemovalReason.KILLED && i > 1 && this.isDeadOrDying()) {
			for (int k = 0; k < i; k++) {
				float f = (k % 2 - 0.5F) * i / 4.0F;
				float f1 = (k / 2 - 0.5F) * i / 4.0F;
				ThaumicSlime child = TVEntities.THAUMIC_SLIME.create(level, EntitySpawnReason.TRIGGERED);
				if (child != null) {
					child.setSlimeSize(1);
					child.snapTo(this.getX() + f, this.getY() + 0.5, this.getZ() + f1, this.random.nextFloat() * 360.0F, 0.0F);
					level.addFreshEntity(child);
				}
			}
		}
		super.remove(reason);
	}

	@Override
	public void playerTouch(Player player) {
		if (!(this.level() instanceof ServerLevel level) || this.getSlimeSize() <= 0) {
			return;
		}
		int i = (int) Math.max(1.0, Math.sqrt(this.getSlimeSize()));
		if (this.launched > 0 && i == 2) {
			i = 3;
		}
		if (this.hasLineOfSight(player) && this.distanceToSqr(player) < 0.8 * i * 0.8 * i
			&& player.hurtServer(level, this.damageSources().mobAttack(this), this.getSlimeSize())) {
			this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return this.getSlimeSize() > 3 ? SoundEvents.SLIME_HURT : SoundEvents.SLIME_HURT_SMALL;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return this.getSlimeSize() > 3 ? SoundEvents.SLIME_DEATH : SoundEvents.SLIME_DEATH_SMALL;
	}

	@Override
	protected float getSoundVolume() {
		return 0.1F * (float) Math.sqrt(this.getSlimeSize());
	}

	@Override
	public int getMaxHeadXRot() {
		return 0;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		if (this.getSlimeSize() < 3 && this.random.nextInt(3) == 0) {
			this.spawnAtLocation(level, new ItemStack(TVItems.TAINTED_GOO), this.getBbHeight() / 2.0F);
		}
	}
}
