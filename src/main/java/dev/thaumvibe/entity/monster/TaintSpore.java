package dev.thaumvibe.entity.monster;

import dev.thaumvibe.api.TaintedMob;
import dev.thaumvibe.blocks.TaintFibreBlock;
import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.world.TVBiomes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * EntityTaintSpore: a swelling pod on top of a taint spore stalk. It grows a size every minute up to ten, and bursts into taint spiders the
 * moment its stalk is gone, a player touches it or it is hurt.
 */
public class TaintSpore extends Monster implements TaintedMob {
	protected static final EntityDataAccessor<Byte> SIZE = SynchedEntityData.defineId(TaintSpore.class, EntityDataSerializers.BYTE);

	public final List<Object> swarm = new ArrayList<>();
	protected int growth;
	public float displaySize;

	public TaintSpore(EntityType<? extends TaintSpore> type, Level level) {
		super(type, level);
		this.setSporeSize(2);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	/** A new spore on top of the stalk at pos. */
	public static void spawnOnStalk(ServerLevel level, BlockPos pos) {
		if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
			return;
		}
		TaintSpore spore = TVEntities.TAINT_SPORE.create(level, EntitySpawnReason.NATURAL);
		if (spore != null) {
			spore.snapTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);
			level.addFreshEntity(spore);
		}
	}

	public static boolean hasSporeOn(Level level, BlockPos pos) {
		return !level.getEntitiesOfClass(TaintSpore.class, new AABB(pos.above())).isEmpty();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(SIZE, (byte) 1);
	}

	public void setSporeSize(int size) {
		this.entityData.set(SIZE, (byte) size);
		this.refreshDimensions();
		this.xpReward = size;
	}

	public int getSporeSize() {
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
		float size = Math.max(0.15F * this.getSporeSize(), 0.5F);
		return EntityDimensions.scalable(size, size);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Size", this.getSporeSize() - 1);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setSporeSize(input.getIntOr("Size", 1) + 1);
		this.displaySize = this.getSporeSize();
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public void move(MoverType type, Vec3 delta) {
		double dy = Math.min(0.0, delta.y);
		if (!this.onSporeStalk()) {
			super.move(type, new Vec3(0.0, dy, 0.0));
		}
	}

	protected boolean onSporeStalk() {
		BlockState below = this.level().getBlockState(BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1, this.getZ()));
		return below.is(TVBlocks.TAINT_FIBRES) && below.getValue(TaintFibreBlock.TYPE) == 4;
	}

	@Override
	protected void registerGoals() {
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return distance < 4096.0;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide() && this.tickCount % 20 == 0 && !TVBiomes.is(this.level(), this.blockPosition(), TVBiomes.TAINTED_LAND)) {
			this.hurtServer((ServerLevel) this.level(), this.damageSources().starve(), 1.0F);
		}
		this.sporeOnUpdate();
	}

	protected void sporeOnUpdate() {
		if (this.getSporeSize() < 10 && this.growth++ == 1200) {
			this.setSporeSize(this.getSporeSize() + 1);
			this.growth = 0;
		}
		if (this.level().isClientSide()) {
			if (this.displaySize < this.getSporeSize()) {
				this.displaySize += 0.02F;
			}
			this.swarm.removeIf(p -> !FX.isAlive(p));
			if (this.swarm.size() < this.getSporeSize() / 3) {
				Object fx = FX.swarm(this, 0.1F, 10.0F, 0.0F);
				if (fx != null) {
					this.swarm.add(fx);
				}
			}
		}
		if (!this.onSporeStalk() || this.deathTime > 0) {
			this.spiderBurst();
		}
	}

	@Override
	public void playerTouch(Player player) {
		this.spiderBurst();
	}

	protected void spiderBurst() {
		Level level = this.level();
		if (level instanceof ServerLevel sl) {
			level.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.GORE, SoundSource.HOSTILE, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.1F);
			int q = this.getSporeSize() / 3 + level.getRandom().nextInt(this.getSporeSize() / 2 + 1);
			for (int a = 0; a < q; a++) {
				TaintSpider spiderling = TVEntities.TAINT_SPIDER.create(sl, EntitySpawnReason.MOB_SUMMONED);
				if (spiderling != null) {
					spiderling.snapTo(this.getX() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), this.getY() + level.getRandom().nextFloat(),
						this.getZ() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), level.getRandom().nextFloat() * 360.0F, 0.0F);
					sl.addFreshEntity(spiderling);
				}
			}
			BlockPos below = BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1, this.getZ());
			BlockState bs = level.getBlockState(below);
			if (bs.is(TVBlocks.TAINT_FIBRES) && bs.getValue(TaintFibreBlock.TYPE) == 4) {
				level.setBlock(below, bs.setValue(TaintFibreBlock.TYPE, 3), Block.UPDATE_ALL);
			}
			level.broadcastEntityEvent(this, (byte) 60);
			this.discard();
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 60) {
			this.sploosh(50);
		} else {
			super.handleEntityEvent(id);
		}
	}

	protected void sploosh(int amt) {
		for (int a = 0; a < amt; a++) {
			FX.sploosh(this);
		}
	}

	@Override
	protected float getSoundVolume() {
		return 0.1F;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 200;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.SWARM;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TVSounds.GORE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.GORE;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.spawnAtLocation(level, new ItemStack(this.random.nextBoolean() ? TVItems.TAINTED_GOO : TVItems.TAINT_TENDRIL), this.getBbHeight() / 2.0F);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}
}
