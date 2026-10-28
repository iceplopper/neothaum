package dev.neothaum.entity.monster.boss;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.EldritchMob;
import dev.neothaum.registry.TVItems;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityThaumcraftBoss: the common ground of TC4's bosses. Each keeps a tally of the damage done to it by everyone, and turns on whoever has
 * hurt it most (by a clear margin); with more than one player in that tally it grows 50 health and half a point of damage for each extra
 * player (up to five). A blow of more than 35 is capped at 35 and enrages it for ten seconds (regeneration, strength and speed). It heals a
 * point every 30 ticks, can't drown or be pushed while invulnerable, stays near where it appeared, and drops a primordial pearl and a rare
 * loot bag. A spawn timer can hold it still and unhurt while it rises.
 */
public abstract class ThaumcraftBoss extends Monster {
	private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(ThaumcraftBoss.class, EntityDataSerializers.INT);
	private static final Identifier[] HPBUFF = {NeoThaum.id("health_buff_1"), NeoThaum.id("health_buff_2"), NeoThaum.id("health_buff_3"),
		NeoThaum.id("health_buff_4"), NeoThaum.id("health_buff_5")};
	private static final Identifier[] DMGBUFF = {NeoThaum.id("damage_buff_1"), NeoThaum.id("damage_buff_2"), NeoThaum.id("damage_buff_3"),
		NeoThaum.id("damage_buff_4"), NeoThaum.id("damage_buff_5")};
	private final Map<Integer, Integer> aggro = new HashMap<>();
	public int spawnTimer;
	protected final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), this.getDisplayName(), BossEvent.BossBarColor.PURPLE,
		BossEvent.BossBarOverlay.PROGRESS);

	protected ThaumcraftBoss(EntityType<? extends ThaumcraftBoss> type, Level level) {
		super(type, level);
		this.xpReward = 50;
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createBossAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 0.95).add(Attributes.FOLLOW_RANGE, 40.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ANGER, 0);
	}

	public int getAnger() {
		return this.entityData.get(ANGER);
	}

	public void setAnger(int anger) {
		this.entityData.set(ANGER, anger);
	}

	public int getSpawnTimer() {
		return this.spawnTimer;
	}

	/** onSpawnWithEgg: stay within 24 blocks of where it appeared. */
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
		this.setHomeTo(this.blockPosition(), 24);
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		this.holdAi();
		if (this.getTarget() != null && !this.getTarget().isAlive()) {
			this.setTarget(null);
		}
	}

	/** While the spawn timer runs it does nothing (updateAITasks waits for it). */
	private void holdAi() {
		boolean hold = this.spawnTimer > 0;
		for (var f : net.minecraft.world.entity.ai.goal.Goal.Flag.values()) {
			this.goalSelector.setControlFlag(f, !hold);
			this.targetSelector.setControlFlag(f, !hold);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.spawnTimer > 0) {
			this.spawnTimer--;
		}
		if (this.getAnger() > 0) {
			this.setAnger(this.getAnger() - 1);
		}
		if (this.level().isClientSide()) {
			if (this.random.nextInt(15) == 0 && this.getAnger() > 0) {
				this.level().addParticle(ParticleTypes.ANGRY_VILLAGER, this.getX() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
					this.getBoundingBox().minY + this.getBbHeight() + this.random.nextFloat() * 0.5,
					this.getZ() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0, this.random.nextGaussian() * 0.02,
					this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
			}
			return;
		}
		this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
		this.bossEvent.setName(this.getDisplayName());
		if (this.tickCount % 30 == 0) {
			this.heal(1.0F);
		}
		if (this.getTarget() != null && this.tickCount % 20 == 0) {
			this.updateAggro();
		}
	}

	/** Turn on whoever has hurt it most, and grow with the number of players in the fight. */
	private void updateAggro() {
		int cur = this.getTarget().getId();
		int best = this.aggro.getOrDefault(cur, 0);
		Entity newTarget = null;
		int players = 0;
		var it = this.aggro.entrySet().iterator();
		while (it.hasNext()) {
			var e = it.next();
			Entity ent = this.level().getEntity(e.getKey());
			if (ent == null || !ent.isAlive() || this.distanceToSqr(ent) > 16384.0) {
				it.remove();
				continue;
			}
			if (ent instanceof Player) {
				players++;
			}
			int ca = e.getValue();
			if (ca > best + 25 && ca > best * 1.1) {
				best = ca;
				newTarget = ent;
			}
		}
		if (newTarget instanceof LivingEntity le && le.getId() != cur) {
			this.setTarget(le);
		}
		float om = this.getMaxHealth();
		AttributeInstance hp = this.getAttribute(Attributes.MAX_HEALTH);
		AttributeInstance dmg = this.getAttribute(Attributes.ATTACK_DAMAGE);
		for (int a = 0; a < 5; a++) {
			hp.removeModifier(HPBUFF[a]);
			if (dmg != null) {
				dmg.removeModifier(DMGBUFF[a]);
			}
		}
		for (int a = 0; a < Math.min(5, players - 1); a++) {
			hp.addTransientModifier(new AttributeModifier(HPBUFF[a], 50.0, AttributeModifier.Operation.ADD_VALUE));
			if (dmg != null) {
				dmg.addTransientModifier(new AttributeModifier(DMGBUFF[a], 0.5, AttributeModifier.Operation.ADD_VALUE));
			}
		}
		this.setHealth(this.getHealth() * (this.getMaxHealth() / om));
	}

	@Override
	public boolean isInvulnerable() {
		return super.isInvulnerable() || this.spawnTimer > 0;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (this.spawnTimer > 0) {
			return false;
		}
		if (source.getEntity() instanceof LivingEntity attacker) {
			this.aggro.merge(attacker.getId(), (int) damage, Integer::sum);
		}
		if (damage > 35.0F) {
			if (this.getAnger() == 0) {
				this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, (int) (damage / 15.0F)));
				this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, (int) (damage / 40.0F)));
				this.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, (int) (damage / 40.0F)));
				this.setAnger(200);
				if (source.getEntity() instanceof Player p) {
					p.sendSystemMessage(Component.empty().append(this.getDisplayName()).append(" ").append(Component.translatable("tc.boss.enrage")));
				}
			}
			damage = 35.0F;
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	public boolean isPushable() {
		return super.isPushable() && !this.isInvulnerable();
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.spawnAtLocation(level, new ItemStack(TVItems.PRIMORDIAL_PEARL), this.getBbHeight() / 2.0F);
		this.spawnAtLocation(level, new ItemStack(TVItems.LOOT_BAG_RARE), 1.5F);
	}

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
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (this.hasHome()) {
			output.putInt("HomeD", this.getHomeRadius());
			output.putInt("HomeX", this.getHomePosition().getX());
			output.putInt("HomeY", this.getHomePosition().getY());
			output.putInt("HomeZ", this.getHomePosition().getZ());
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		int d = input.getIntOr("HomeD", 0);
		if (d > 0) {
			this.setHomeTo(new net.minecraft.core.BlockPos(input.getIntOr("HomeX", 0), input.getIntOr("HomeY", 0), input.getIntOr("HomeZ", 0)), d);
		}
	}
}
