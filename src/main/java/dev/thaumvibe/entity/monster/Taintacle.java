package dev.thaumvibe.entity.monster;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.TaintedMob;
import dev.thaumvibe.blocks.TaintBlock;
import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.world.TVBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * EntityTaintacle: a great purple tentacle rooted in the tainted land. It cannot move; it lashes anything that isn't tainted within reach, and
 * anything standing on taint further off has small taintacles burst up beneath it. Away from the taint it slowly starves.
 */
public class Taintacle extends Monster implements TaintedMob {
	public static final ResourceKey<DamageType> TENTACLE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Thaumvibe.id("tentacle"));

	public float flailIntensity = 1.0F;
	/** Client: ticks since it last struck (set by the attack entity event), for the flailing. */
	public int attackTime;
	private LivingEntity entityToAttack;

	public Taintacle(EntityType<? extends Taintacle> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.ATTACK_DAMAGE, 7.0);
	}

	/** EntityTaintacle.getCanSpawnHere: on bare fibres or tainted soil in the tainted land, dark enough, and none other within 24 blocks. */
	public static boolean checkTaintacleSpawnRules(EntityType<? extends Taintacle> type, ServerLevelAccessor level, EntitySpawnReason reason,
		BlockPos pos, RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) {
			return true;
		}
		BlockState bs = level.getBlockState(pos);
		BlockState below = level.getBlockState(pos.below());
		boolean onTaint = (bs.is(TVBlocks.TAINT_FIBRES) && bs.getValue(dev.thaumvibe.blocks.TaintFibreBlock.TYPE) == 0 || below.is(TVBlocks.TAINTED_SOIL))
			&& level.getBiome(pos).is(TVBiomes.TAINTED_LAND);
		return onTaint && level.getEntitiesOfClass(Taintacle.class, new AABB(pos).inflate(24.0, 8.0, 24.0)).isEmpty()
			&& level.getBrightness(LightLayer.BLOCK, pos) <= random.nextInt(7) && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}

	@Override
	protected void registerGoals() {
	}

	@Override
	public void move(MoverType type, Vec3 delta) {
		super.move(type, new Vec3(0.0, Math.min(0.0, delta.y), 0.0));
	}

	protected LivingEntity findPlayerToAttack() {
		LivingEntity found = null;
		double distance = Double.MAX_VALUE;
		float h = this.getBbHeight();
		for (LivingEntity el : this.level().getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(h * 6.0F, h * 3.0F,
			h * 6.0F))) {
			if (el == this || el instanceof TaintedMob || el instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())) {
				continue;
			}
			double d = el.distanceToSqr(this);
			if (d < distance) {
				distance = d;
				found = el;
			}
		}
		return found;
	}

	public boolean getAgitationState(LivingEntity target) {
		float h = this.getBbHeight() * 7.0F;
		return target != null && target.distanceToSqr(this) < h * h;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (this.entityToAttack != null) {
			double dx = this.entityToAttack.getX() - this.getX();
			double dz = this.entityToAttack.getZ() - this.getZ();
			float target = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
			this.setYRot(this.getYRot() + Mth.clamp(Mth.wrapDegrees(target - this.getYRot()), -5.0F, 5.0F));
			this.yBodyRot = this.getYRot();
			this.yHeadRot = this.getYRot();
		}
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.level();
		float h = this.getBbHeight();
		if (level instanceof ServerLevel sl && this.tickCount % 20 == 0 && !TVBiomes.is(level, this.blockPosition(), TVBiomes.TAINTED_LAND)) {
			this.hurtServer(sl, this.damageSources().starve(), 1.0F);
		}
		if (this.attackTime > 0) {
			this.attackTime--;
		}
		if (level.isClientSide()) {
			LivingEntity target = this.getTarget();
			if (this.tickCount > h * 10.0F && (this.hurtTime > 0 || this.attackTime > 0 || target != null && target.distanceTo(this) < h)) {
				if (this.flailIntensity < 3.0F) {
					this.flailIntensity += 0.2F;
				}
			} else if (this.flailIntensity > 1.0F) {
				this.flailIntensity -= 0.2F;
			}
			if (this.tickCount < h * 10.0F && this.onGround()) {
				FX.tentacleArise(this);
			}
			return;
		}
		if (this.entityToAttack == null) {
			this.entityToAttack = this.findPlayerToAttack();
			this.setTarget(this.entityToAttack);
		} else if (this.entityToAttack.isAlive() && this.getAgitationState(this.entityToAttack)) {
			if (this.hasLineOfSight(this.entityToAttack)) {
				this.attackEntity((ServerLevel) level, this.entityToAttack, this.entityToAttack.distanceTo(this));
			}
		} else {
			this.entityToAttack = null;
			this.setTarget(null);
		}
	}

	protected void attackEntity(ServerLevel level, LivingEntity entity, float dist) {
		if (this.attackTime > 0) {
			return;
		}
		float h = this.getBbHeight();
		if (dist <= h && entity.getBoundingBox().maxY > this.getBoundingBox().minY && entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
			this.attackTime = 20;
			level.broadcastEntityEvent(this, (byte) 4);
			float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
			entity.hurtServer(level, level.damageSources().source(TENTACLE_DAMAGE, this), damage);
			this.playSound(TVSounds.TENTACLE, this.getSoundVolume(), this.getVoicePitch());
		} else if (dist > h && entity.onGround() && !(this instanceof TaintacleSmall)) {
			this.spawnTentacles(level, entity);
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 4) {
			this.attackTime = 20;
		} else {
			super.handleEntityEvent(id);
		}
	}

	/** A small taintacle bursts up beneath whoever stands on taint out of reach. */
	protected void spawnTentacles(ServerLevel level, Entity entity) {
		BlockPos p = BlockPos.containing(entity.getX(), entity.getBoundingBox().minY, entity.getZ());
		if (!TVBiomes.is(level, p, TVBiomes.TAINTED_LAND) || !level.getBlockState(p).is(TaintBlock.TAINT) && !level.getBlockState(p.below()).is(TaintBlock.TAINT)) {
			return;
		}
		this.attackTime = 40 + level.getRandom().nextInt(20);
		TaintacleSmall taintlet = TVEntities.TAINTACLE_SMALL.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (taintlet != null) {
			taintlet.snapTo(entity.getX() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), entity.getY(),
				entity.getZ() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), 0.0F, 0.0F);
			level.addFreshEntity(taintlet);
		}
		this.playSound(TVSounds.TENTACLE, this.getSoundVolume(), this.getVoicePitch());
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (!(this instanceof TaintacleSmall) && source.getEntity() != null && this.distanceTo(source.getEntity()) > 16.0F) {
			this.spawnTentacles(level, source.getEntity());
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 200;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.ROOTS;
	}

	@Override
	public float getVoicePitch() {
		return 1.3F - this.getBbHeight() / 10.0F;
	}

	@Override
	protected float getSoundVolume() {
		return this.getBbHeight() / 8.0F;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TVSounds.TENTACLE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.TENTACLE;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.spawnAtLocation(level, new ItemStack(this.random.nextBoolean() ? TVItems.TAINTED_GOO : TVItems.TAINT_TENDRIL), this.getBbHeight() / 2.0F);
	}
}
