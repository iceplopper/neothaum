package dev.neothaum.entity;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.items.WandItem;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * AspectOrbEntity: floating collectible orb of aspect energy (EntityAspectOrb in TC4).
 * Attracted to nearby players; restores vis to wands or adds research aspect points.
 */
public class AspectOrbEntity extends Entity {
	private static final EntityDataAccessor<String> ASPECT_TAG = SynchedEntityData.defineId(AspectOrbEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> VALUE = SynchedEntityData.defineId(AspectOrbEntity.class, EntityDataSerializers.INT);

	private int age = 0;
	private Player targetPlayer = null;

	public AspectOrbEntity(EntityType<? extends AspectOrbEntity> type, Level level) {
		super(type, level);
		this.noPhysics = false;
	}

	public AspectOrbEntity(Level level, double x, double y, double z, Aspect aspect, int value) {
		this(TVEntities.ASPECT_ORB, level);
		this.setPos(x, y, z);
		this.setDeltaMovement((this.random.nextDouble() - 0.5) * 0.2, 0.2, (this.random.nextDouble() - 0.5) * 0.2);
		this.setAspect(aspect);
		this.setValue(value);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ASPECT_TAG, "aer");
		builder.define(VALUE, 1);
	}

	public Aspect getAspect() {
		Aspect a = Aspect.getAspect(this.entityData.get(ASPECT_TAG));
		return a != null ? a : Aspect.AIR;
	}

	public void setAspect(Aspect aspect) {
		this.entityData.set(ASPECT_TAG, aspect != null ? aspect.getTag() : "aer");
	}

	public int getValue() {
		return this.entityData.get(VALUE);
	}

	public void setValue(int val) {
		this.entityData.set(VALUE, Math.max(1, val));
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.03;
	}

	@Override
	public void tick() {
		super.tick();

		this.age++;
		if (this.age > 1200) {
			this.discard();
			return;
		}

		// Gravity & movement
		Vec3 delta = this.getDeltaMovement();
		if (!this.isNoGravity()) {
			delta = delta.add(0, -this.getDefaultGravity(), 0);
		}

		// Player attraction
		if (this.tickCount % 5 == 0) {
			List<Player> nearby = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8.0));
			double closest = Double.MAX_VALUE;
			this.targetPlayer = null;
			for (Player p : nearby) {
				double d = p.distanceToSqr(this);
				if (d < closest && !p.isSpectator()) {
					closest = d;
					this.targetPlayer = p;
				}
			}
		}

		if (this.targetPlayer != null && !this.targetPlayer.isSpectator()) {
			Vec3 dir = this.targetPlayer.getEyePosition().subtract(this.position());
			double dist = dir.length();
			if (dist > 0.1) {
				double speed = 0.15 * (1.0 - Math.min(1.0, dist / 8.0));
				delta = delta.add(dir.normalize().scale(speed));
			}
		}

		this.setDeltaMovement(delta.scale(0.98));
		this.move(MoverType.SELF, this.getDeltaMovement());

		if (this.level().isClientSide() && this.random.nextInt(4) == 0) {
			this.level().addParticle(ParticleTypes.GLOW, this.getX(), this.getY() + 0.1, this.getZ(), 0, 0.01, 0);
		}
	}

	@Override
	public void playerTouch(Player player) {
		if (this.level().isClientSide() || this.isRemoved()) {
			return;
		}

		Aspect aspect = this.getAspect();
		int val = this.getValue();
		boolean absorbed = false;

		// 1. Try filling held or hotbar wand with vis (if primal aspect)
		if (aspect.isPrimal()) {
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack stack = player.getInventory().getItem(i);
				if (stack.getItem() instanceof WandItem) {
					int added = WandItem.addVis(stack, aspect, val * 100, true);
					if (added > 0) {
						absorbed = true;
						break;
					}
				}
			}
		}

		// 2. Otherwise add to player's research aspect pool
		if (!absorbed) {
			PlayerKnowledge k = ResearchManager.knowledge(player).copy();
			k.addAspectPool(aspect, val);
			ResearchManager.setKnowledge(player, k);
			absorbed = true;
		}

		if (absorbed) {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F,
				0.8F + this.random.nextFloat() * 0.4F);
			if (this.level() instanceof ServerLevel sl) {
				sl.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 0.1, this.getZ(), 5, 0.1, 0.1, 0.1, 0.05);
			}
			this.discard();
		}
	}

	@Override
	public final boolean hurtClient(DamageSource source) {
		return false;
	}

	@Override
	public final boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putString("Aspect", this.getAspect().getTag());
		output.putInt("Value", this.getValue());
		output.putInt("Age", this.age);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		this.setAspect(Aspect.getAspect(input.getStringOr("Aspect", "aer")));
		this.setValue(input.getIntOr("Value", 1));
		this.age = input.getIntOr("Age", 0);
	}

	@Override
	public boolean isAttackable() {
		return false;
	}
}
