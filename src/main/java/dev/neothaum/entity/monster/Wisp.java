package dev.neothaum.entity.monster;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.fx.FX;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.world.TVBiomes;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * EntityWisp: a drifting ball of aspect that wanders about on random waypoints, and now and then takes a dislike to a nearby player and zaps
 * them with lightning (more likely to hit someone standing still). In eerie places they are made of darker things.
 */
public class Wisp extends Mob implements Enemy {
	private static final EntityDataAccessor<String> TYPE = SynchedEntityData.defineId(Wisp.class, EntityDataSerializers.STRING);

	private int courseChangeCooldown;
	private double waypointX, waypointY, waypointZ;
	private LivingEntity targetedEntity;
	private int aggroCooldown;
	public int prevAttackCounter;
	public int attackCounter;

	public Wisp(EntityType<? extends Wisp> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 22.0).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.FOLLOW_RANGE, 16.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(TYPE, "");
	}

	public String getWispType() {
		return this.entityData.get(TYPE);
	}

	public void setWispType(String t) {
		this.entityData.set(TYPE, t);
	}

	public Aspect getAspect() {
		return Aspect.getAspect(this.getWispType());
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	protected int decreaseAirSupply(int air) {
		return air;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean onClimbable() {
		return false;
	}

	@Override
	public void travel(Vec3 input) {
		this.travelFlying(input, 0.02F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.getDirectEntity() instanceof LivingEntity le) {
			this.targetedEntity = le;
			this.aggroCooldown = 200;
		}
		if (source.getEntity() instanceof LivingEntity le) {
			this.targetedEntity = le;
			this.aggroCooldown = 200;
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (!this.level().isClientSide()) {
			TVNetwork.sendEntityFx(this, 1, 0, 100);
		}
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.level();
		if (level.isClientSide()) {
			if (this.tickCount <= 1) {
				// Thaumcraft.proxy.burst on the first tick, done client side here as TC did
				dev.neothaum.ClientBridge.burst.accept(this, 1.0F);
			}
			Aspect a = this.getAspect();
			if (level.getRandom().nextBoolean() && a != null) {
				int c = a.getColor();
				RandomSource r = level.getRandom();
				FX.wisp(level, this.getX() + (r.nextFloat() - r.nextFloat()) * 0.7F, this.getY() + 0.45F + (r.nextFloat() - r.nextFloat()) * 0.7F,
					this.getZ() + (r.nextFloat() - r.nextFloat()) * 0.7F, 0.1F, (c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F,
					false, false, 0.02F);
			}
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (this.getAspect() == null) {
			if (level.getBiome(BlockPos.containing(Mth.ceil(this.getX()), this.getY(), Mth.ceil(this.getZ()))).is(TVBiomes.EERIE)) {
				Aspect[] eerie = {Aspect.DARKNESS, Aspect.UNDEAD, Aspect.ENTROPY, Aspect.ELDRITCH, Aspect.POISON, Aspect.DEATH};
				this.setWispType(eerie[this.random.nextInt(6)].getTag());
			} else if (level.getRandom().nextInt(10) != 0) {
				List<Aspect> as = Aspect.getPrimalAspects();
				this.setWispType(as.get(level.getRandom().nextInt(as.size())).getTag());
			} else {
				List<Aspect> as = Aspect.getCompoundAspects();
				this.setWispType(as.get(level.getRandom().nextInt(as.size())).getTag());
			}
		}
		if (level.getDifficulty() == Difficulty.PEACEFUL) {
			this.discard();
			return;
		}
		this.prevAttackCounter = this.attackCounter;
		double attackrange = 16.0;
		double d = this.waypointX - this.getX();
		double d1 = this.waypointY - this.getY();
		double d2 = this.waypointZ - this.getZ();
		double d3 = d * d + d1 * d1 + d2 * d2;
		if (d3 < 1.0 || d3 > 3600.0) {
			this.waypointX = this.getX() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0;
			this.waypointY = this.getY() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0;
			this.waypointZ = this.getZ() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0;
		}
		if (this.courseChangeCooldown-- <= 0) {
			this.courseChangeCooldown += this.random.nextInt(5) + 2;
			d3 = Math.sqrt(d3);
			if (this.isCourseTraversable(d3)) {
				this.setDeltaMovement(this.getDeltaMovement().add(d / d3 * 0.1, d1 / d3 * 0.1, d2 / d3 * 0.1));
			} else {
				this.waypointX = this.getX();
				this.waypointY = this.getY();
				this.waypointZ = this.getZ();
			}
		}
		if (this.targetedEntity != null && !this.targetedEntity.isAlive()) {
			this.targetedEntity = null;
		}
		this.aggroCooldown--;
		if (level.getRandom().nextInt(1000) == 0 && (this.targetedEntity == null || this.aggroCooldown-- <= 0)) {
			this.targetedEntity = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0,
				e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
			if (this.targetedEntity != null) {
				this.aggroCooldown = 50;
			}
		}
		if (this.targetedEntity != null && this.targetedEntity.distanceToSqr(this) < attackrange * attackrange) {
			double d5 = this.targetedEntity.getX() - this.getX();
			double d7 = this.targetedEntity.getZ() - this.getZ();
			this.setYRot(-((float) Math.atan2(d5, d7)) * 180.0F / (float) Math.PI);
			this.yBodyRot = this.getYRot();
			if (this.hasLineOfSight(this.targetedEntity)) {
				this.attackCounter++;
				if (this.attackCounter == 20) {
					level.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.ZAP, SoundSource.HOSTILE, 1.0F, 1.1F);
					TVNetwork.sendEntityFx(this, 0, this.targetedEntity.getId(), 0);
					float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
					Vec3 m = this.targetedEntity.getDeltaMovement();
					if (!(Math.abs(m.x) > 0.1F) && !(Math.abs(m.y) > 0.1F) && !(Math.abs(m.z) > 0.1F)) {
						if (level.getRandom().nextFloat() < 0.66F) {
							this.targetedEntity.hurtServer(level, this.damageSources().mobAttack(this), damage + 1.0F);
						}
					} else if (level.getRandom().nextFloat() < 0.4F) {
						this.targetedEntity.hurtServer(level, this.damageSources().mobAttack(this), damage);
					}
					this.attackCounter = -20 + level.getRandom().nextInt(20);
				}
			} else if (this.attackCounter > 0) {
				this.attackCounter--;
			}
		} else {
			Vec3 m = this.getDeltaMovement();
			this.setYRot(-((float) Math.atan2(m.x, m.z)) * 180.0F / (float) Math.PI);
			this.yBodyRot = this.getYRot();
			if (this.attackCounter > 0) {
				this.attackCounter--;
			}
		}
	}

	private boolean isCourseTraversable(double d3) {
		double d4 = (this.waypointX - this.getX()) / d3;
		double d5 = (this.waypointY - this.getY()) / d3;
		double d6 = (this.waypointZ - this.getZ()) / d3;
		AABB bb = this.getBoundingBox();
		for (int i = 1; i < d3; i++) {
			bb = bb.move(d4, d5, d6);
			if (!this.level().noCollision(this, bb)) {
				return false;
			}
		}
		BlockPos wp = BlockPos.containing((int) this.waypointX, (int) this.waypointY, (int) this.waypointZ);
		if (this.level().getBlockState(wp).liquid()) {
			return false;
		}
		for (int a = 0; a < 11; a++) {
			if (!this.level().isEmptyBlock(wp.below(a))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.WISPLIVE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.FIRE_EXTINGUISH;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.WISPDEAD;
	}

	@Override
	protected float getSoundVolume() {
		return 0.25F;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		Aspect a = this.getAspect();
		if (a != null) {
			ItemStack ess = new ItemStack(TVItems.ETHEREAL_ESSENCE);
			ess.set(TVComponents.ASPECTS, new AspectList().add(a, 2));
			this.spawnAtLocation(level, ess, 0.0F);
		}
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return true;
	}

	/** EntityWisp.getCanSpawnHere: no more than eight together, in the dark. */
	public static boolean checkWispSpawnRules(EntityType<Wisp> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
		RandomSource random) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) {
			return false;
		}
		if (EntitySpawnReason.isSpawner(reason)) {
			return true;
		}
		if (level.getEntitiesOfClass(Wisp.class, new AABB(pos).inflate(16.0)).size() >= 8) {
			return false;
		}
		if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) {
			return false;
		}
		int l = level.getMaxLocalRawBrightness(pos);
		if (level.getLevel().isThundering()) {
			l = level.getMaxLocalRawBrightness(pos, 10);
		}
		return l <= random.nextInt(8);
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 2;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putString("Type", this.getWispType());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setWispType(input.getStringOr("Type", ""));
	}
}
