package dev.neothaum.entity.projectile;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.world.NodeGenerator;
import dev.neothaum.world.TVBiomes;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * EntityPrimalOrb: the primal focus's wandering ball of raw vis. It drifts after a second (or hunts the nearest creature, with the seeker
 * upgrade) and explodes; an unseeking orb sometimes leaves a node behind, or a splash of taint.
 */
public class PrimalOrb extends TCThrowable {
	private static final EntityDataAccessor<Boolean> SEEKER = SynchedEntityData.defineId(PrimalOrb.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(PrimalOrb.class, EntityDataSerializers.INT);
	private int count;

	public PrimalOrb(EntityType<? extends PrimalOrb> type, Level level) {
		super(type, level);
	}

	public PrimalOrb(Level level, LivingEntity thrower, boolean seeker) {
		super(TVEntities.PRIMAL_ORB, level, thrower, 0.5F, 1.0F);
		this.entityData.set(SEEKER, seeker);
		this.entityData.set(OWNER_ID, thrower.getId());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(SEEKER, false);
		builder.define(OWNER_ID, 0);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.001;
	}

	@Override
	public void tick() {
		this.count++;
		Level level = this.level();
		if (level.isClientSide()) {
			var r = this.random;
			for (int a = 0; a < 6; a++) {
				double x = this.getX() + (r.nextFloat() - r.nextFloat()) * 0.2F;
				double y = this.getY() + (r.nextFloat() - r.nextFloat()) * 0.2F;
				double z = this.getZ() + (r.nextFloat() - r.nextFloat()) * 0.2F;
				FX.wispTo(level, x, y, z, this.getX(), this.getY(), this.getZ(), 0.1F, a, true, 0.0F);
			}
			FX.wispTo(level, this.getX() + (r.nextFloat() - r.nextFloat()) * 0.2F, this.getY() + (r.nextFloat() - r.nextFloat()) * 0.2F,
				this.getZ() + (r.nextFloat() - r.nextFloat()) * 0.2F, this.getX(), this.getY(), this.getZ(), 0.1F, r.nextInt(6), true, 0.0F);
		}
		Random rr = new Random(this.getId() + this.count);
		if (this.tickCount > 20) {
			var m = this.getDeltaMovement();
			if (!this.entityData.get(SEEKER)) {
				this.setDeltaMovement(m.x + (rr.nextFloat() - rr.nextFloat()) * 0.01F, m.y + (rr.nextFloat() - rr.nextFloat()) * 0.01F,
					m.z + (rr.nextFloat() - rr.nextFloat()) * 0.01F);
			} else {
				double d = Double.MAX_VALUE;
				Entity t = null;
				for (Entity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0))) {
					if (e.getId() != this.entityData.get(OWNER_ID) && e.isAlive() && e.distanceTo(this) <= 16.0) {
						double dd = this.distanceToSqr(e);
						if (dd < d) {
							d = dd;
							t = e;
						}
					}
				}
				if (t != null) {
					double dx = (t.getX() - this.getX()) / d;
					double dy = (t.getBoundingBox().minY + t.getBbHeight() * 0.9 - this.getY()) / d;
					double dz = (t.getZ() - this.getZ()) / d;
					this.setDeltaMovement(Mth.clamp(m.x + dx * 0.2, -0.2, 0.2), Mth.clamp(m.y + dy * 0.2, -0.2, 0.2), Mth.clamp(m.z + dz * 0.2, -0.2, 0.2));
				}
			}
		}
		super.tick();
		if (this.tickCount > 5000) {
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		Level level = this.level();
		if (level.isClientSide()) {
			return;
		}
		ServerLevel sl = (ServerLevel) level;
		sl.broadcastEntityEvent(this, (byte) 3);
		float expl = 2.0F;
		float specialchance = 1.0F;
		if (hit.getType() == HitResult.Type.BLOCK && level.getBlockState(this.blockPosition()).is(net.minecraft.world.level.block.Blocks.NETHER_PORTAL)) {
			expl = 4.0F;
			specialchance = 10.0F;
		}
		sl.explode(null, this.getX(), this.getY(), this.getZ(), expl, true, Level.ExplosionInteraction.MOB);
		if (!this.entityData.get(SEEKER) && this.random.nextInt(100) <= specialchance) {
			if (this.random.nextBoolean()) {
				this.taintSplosion(sl);
			} else {
				BlockPos at = hit instanceof BlockHitResult bh ? bh.getBlockPos() : this.blockPosition();
				NodeGenerator.createRandomNodeAt(sl, at, this.random, false, false, true, false);
			}
		}
		this.discard();
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id != 3) {
			super.handleEntityEvent(id);
			return;
		}
		for (int a = 0; a < 6; a++) {
			for (int b = 0; b < 6; b++) {
				float fx = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
				float fy = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
				float fz = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
				FX.wispTo(this.level(), this.getX() + fx, this.getY() + fy, this.getZ() + fz, this.getX() + fx * 10.0F, this.getY() + fy * 10.0F,
					this.getZ() + fz * 10.0F, 0.4F, b, true, 0.05F);
			}
		}
	}

	private void taintSplosion(ServerLevel level) {
		int x = (int) this.getX();
		int z = (int) this.getZ();
		for (int a = 0; a < 10; a++) {
			int xx = x + (int) (this.random.nextFloat() - this.random.nextFloat() * 6.0F);
			int zz = z + (int) (this.random.nextFloat() - this.random.nextFloat() * 6.0F);
			BlockPos col = new BlockPos(xx, 0, zz);
			if (this.random.nextBoolean() && !TVBiomes.is(level, col.atY(level.getSeaLevel()), TVBiomes.TAINTED_LAND)) {
				TVBiomes.setBiomeAt(level, xx, zz, TVBiomes.TAINTED_LAND);
				int yy = level.getHeight(Heightmap.Types.MOTION_BLOCKING, xx, zz);
				BlockPos p = new BlockPos(xx, yy, zz);
				if (!level.isEmptyBlock(p.below())) {
					level.setBlock(p, TVBlocks.TAINT_FIBRES.defaultBlockState(), 3);
				}
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("seeker", this.entityData.get(SEEKER));
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(SEEKER, input.getBooleanOr("seeker", false));
	}
}
