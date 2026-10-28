package dev.neothaum.entity.monster;

import dev.neothaum.entity.monster.boss.LongRangeAttackGoal;
import dev.neothaum.entity.monster.boss.ThaumcraftBoss;
import dev.neothaum.entity.projectile.EldritchOrbEntity;
import dev.neothaum.fx.FX;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.warp.Warp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityEldritchWarden: the Outer Lands' warden boss. It rises out of the ground over seven seconds and wears a ward of absorption two thirds
 * its health, which grows back a point every 25 ticks it goes unhurt. It trails a sapping field wherever it walks. When the ward first breaks it
 * vanishes home and, untouchable for seven seconds, throws out rings of sapping field. At range it mostly hurls eldritch orbs, but one time in
 * five it screeches instead: knocking its target back, withering and weakening it, and lending a player a little temporary warp.
 */
public class EldritchWardenEntity extends ThaumcraftBoss implements RangedAttackMob, dev.neothaum.api.EldritchMob,
	dev.neothaum.entity.champion.ChampionBoss {
	public static final String[] TITLES = new String[]{"Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha", "Oorn", "Shaikorth",
		"Rhan-Tegoth", "Rhogog", "Shudde M'ell", "Vulthoom", "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon"};

	private static final EntityDataAccessor<Integer> TITLE = SynchedEntityData.defineId(EldritchWardenEntity.class, EntityDataSerializers.INT);

	private boolean fieldFrenzy;
	private int fieldFrenzyCounter;
	private boolean lastBlast;
	/** Client-side arm raise after each blast (entity events 15, 16, 17). */
	public float armLiftL;
	public float armLiftR;

	public EldritchWardenEntity(EntityType<? extends EldritchWardenEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return ThaumcraftBoss.createBossAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.33)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 4.0).add(Attributes.MAX_ABSORPTION, 200.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TITLE, 0);
	}

	public int getTitleIndex() {
		return this.entityData.get(TITLE);
	}

	public void setTitleIndex(int idx) {
		this.entityData.set(TITLE, Mth.clamp(idx, 0, TITLES.length - 1));
	}

	/** entity.Thaumcraft.EldritchWarden.name: "<title> the <modifier>". */
	@Override
	public void nameAsChampion(Component modifier) {
		this.setCustomName(Component.literal(TITLES[this.getTitleIndex()] + " the ").append(modifier));
	}

	@Override
	protected void registerGoals() {
		if (this.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.GroundPathNavigation nav) {
			nav.setCanOpenDoors(true);
		}
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new LongRangeAttackGoal(this, 3.0, 1.0, 20, 40, 24.0F));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
		this.spawnTimer = 150;
		this.setTitleIndex(level.getRandom().nextInt(TITLES.length));
		this.setAbsorptionAmount((float) (this.getAbsorptionAmount() + this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 0.66));
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	public Component getName() {
		return this.hasCustomName() ? super.getName() : Component.literal(TITLES[this.getTitleIndex()] + " the Eldritch Warden");
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (this.fieldFrenzyCounter > 0) {
			for (var f : net.minecraft.world.entity.ai.goal.Goal.Flag.values()) {
				this.goalSelector.setControlFlag(f, false);
			}
		}
		// the ward regrows while it goes unhurt
		if (this.hurtTime <= 0 && this.tickCount % 25 == 0) {
			int bh = (int) (this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 0.66);
			if (this.getAbsorptionAmount() < bh) {
				this.setAbsorptionAmount(this.getAbsorptionAmount() + 1.0F);
			}
		}
	}

	@Override
	public void tick() {
		if (this.spawnTimer == 150 && !this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 18);
		}
		super.tick();
		if (this.level().isClientSide()) {
			if (this.armLiftL > 0.0F) {
				this.armLiftL -= 0.05F;
			}
			if (this.armLiftR > 0.0F) {
				this.armLiftR -= 0.05F;
			}
			float x = (float) (this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
			float z = (float) (this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
			FX.wisp(this.level(), x, this.getY() + 0.25 * this.getBbHeight(), z, 1.0F, 0.25F, 0.0F, 0.33F, false, true, -0.01F);
			if (this.spawnTimer > 0) {
				float he = Math.max(1.0F, this.getBbHeight() * ((150 - this.spawnTimer) / 150.0F));
				for (int a = 0; a < 33; a++) {
					FX.smokeSpiral(this.level(), this.getX(), this.getBoundingBox().minY + he / 2.0F, this.getZ(), he, this.random.nextInt(360),
						Mth.floor(this.getBoundingBox().minY) - 1, 2232623);
				}
			}
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		Level level = this.level();
		for (int l = 0; l < 4; l++) {
			BlockPos p = BlockPos.containing(this.getX() + (l % 2 * 2 - 1) * 0.25F, this.getY(), this.getZ() + (l / 2 % 2 * 2 - 1) * 0.25F);
			if (level.getBlockState(p).isAir()) {
				level.setBlock(p, TVBlocks.SAPPING_FIELD.defaultBlockState(), 3);
			}
		}
		if (!level.isClientSide() && this.fieldFrenzyCounter > 0) {
			if (this.fieldFrenzyCounter == 150) {
				this.teleportHome();
			}
			this.performFieldFrenzy();
		}
	}

	/** Rings of sapping field spreading out from it, a little sparser each time. */
	private void performFieldFrenzy() {
		if (this.fieldFrenzyCounter < 121 && this.fieldFrenzyCounter % 10 == 0) {
			ServerLevel level = (ServerLevel) this.level();
			level.broadcastEntityEvent(this, (byte) 17);
			double radius = (150 - this.fieldFrenzyCounter) / 8.0;
			int d = 1 + this.fieldFrenzyCounter / 8;
			BlockPos c = this.blockPosition();
			for (int q = 0; q < 180 / d; q++) {
				double radians = Math.toRadians(q * 2 * d);
				BlockPos p = c.offset((int) (radius * Math.cos(radians)), 0, (int) (radius * Math.sin(radians)));
				if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) {
					level.setBlock(p, TVBlocks.SAPPING_FIELD.defaultBlockState(), 3);
					level.scheduleTick(p, TVBlocks.SAPPING_FIELD, 250 + this.random.nextInt(150));
					if (this.random.nextFloat() < 0.3F) {
						TVNetwork.sendBlockArc(this, p);
					} else {
						TVNetwork.sendBlockSparkle(level, p, 8388736);
					}
				}
			}
			this.playSound(TVSounds.ZAP, 1.0F, 0.9F + this.random.nextFloat() * 0.1F);
		}
		this.fieldFrenzyCounter--;
	}

	/** Back to where it first appeared (or nearby, if that is blocked), with an enderman's flourish. */
	private void teleportHome() {
		if (!this.hasHome()) {
			return;
		}
		BlockPos home = this.getHomePosition();
		double ox = this.getX(), oy = this.getY(), oz = this.getZ();
		int i = home.getX(), j = home.getY(), k = home.getZ();
		for (int tries = 20; tries > 0; tries--) {
			BlockPos p = new BlockPos(i, j, k);
			if (this.level().getBlockState(p.below()).isSolid() && !this.level().getBlockState(p).isSolid()) {
				this.setPos(i + 0.5, j + 0.1, k + 0.5);
				if (this.level().noCollision(this)) {
					this.level().playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
					this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
					((ServerLevel) this.level()).sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY(0.5), this.getZ(), 128, this.getBbWidth(),
						this.getBbHeight() / 2.0, this.getBbWidth(), 0.1);
					return;
				}
				this.setPos(ox, oy, oz);
			}
			i = home.getX() + this.random.nextInt(8) - this.random.nextInt(8);
			k = home.getZ() + this.random.nextInt(8) - this.random.nextInt(8);
		}
	}

	@Override
	public boolean isInvulnerable() {
		return this.fieldFrenzyCounter > 0 || super.isInvulnerable();
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (this.isInvulnerable() || source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER)) {
			return false;
		}
		boolean hurt = super.hurtServer(level, source, damage);
		if (hurt && !this.fieldFrenzy && this.getAbsorptionAmount() <= 0.0F) {
			this.fieldFrenzy = true;
			this.fieldFrenzyCounter = 150;
		}
		return hurt;
	}

	/** Four blasts in five are eldritch orbs from alternating hands; otherwise, if it can see its target, a screech. */
	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		if (this.random.nextFloat() > 0.2F) {
			EldritchOrbEntity blast = new EldritchOrbEntity(this.level(), this);
			this.lastBlast = !this.lastBlast;
			this.level().broadcastEntityEvent(this, (byte) (this.lastBlast ? 16 : 15));
			int rr = this.lastBlast ? 90 : 180;
			double xx = Mth.cos((this.getYRot() + rr) % 360.0F / 180.0F * (float) Math.PI) * 0.5F;
			double zz = Mth.sin((this.getYRot() + rr) % 360.0F / 180.0F * (float) Math.PI) * 0.5F;
			blast.setPos(blast.getX() - xx, blast.getY() - 0.13, blast.getZ() - zz);
			var tm = target.getDeltaMovement();
			blast.heading(target.getX() + tm.x - this.getX(), target.getY() - this.getY() - target.getBbHeight() / 2.0F, target.getZ() + tm.z - this.getZ(),
				1.0F, 2.0F);
			this.playSound(TVSounds.EGATTACK, 2.0F, 1.0F + this.random.nextFloat() * 0.1F);
			this.level().addFreshEntity(blast);
		} else if (this.hasLineOfSight(target)) {
			TVNetwork.sendEntityFx(this, 5, 0, 0);
			target.push(-Mth.sin(this.getYRot() * (float) Math.PI / 180.0F) * 1.5F, 0.1, Mth.cos(this.getYRot() * (float) Math.PI / 180.0F) * 1.5F);
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
			if (target instanceof ServerPlayer sp) {
				Warp.addWarpToPlayer(sp, 3 + this.random.nextInt(3), true);
			}
			this.playSound(TVSounds.EGSCREECH, 4.0F, 1.0F + this.random.nextFloat() * 0.1F);
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		switch (id) {
			case 15 -> this.armLiftL = 0.5F;
			case 16 -> this.armLiftR = 0.5F;
			case 17 -> {
				this.armLiftL = 0.9F;
				this.armLiftR = 0.9F;
			}
			case 18 -> this.spawnTimer = 150;
			default -> super.handleEntityEvent(id);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.EGIDLE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.EGDEATH;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 500;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putByte("title", (byte) this.getTitleIndex());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setTitleIndex(input.getByteOr("title", (byte) 0));
	}
}
