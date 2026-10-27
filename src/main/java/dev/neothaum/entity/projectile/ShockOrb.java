package dev.neothaum.entity.projectile;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** EntityShockOrb: the earth shock upgrade's ball of lightning; it shocks everything in sight nearby and leaves static fields on the ground. */
public class ShockOrb extends TCThrowable {
	public int area = 4;
	public int damage = 5;

	public ShockOrb(EntityType<? extends ShockOrb> type, Level level) {
		super(type, level);
	}

	public ShockOrb(Level level, LivingEntity thrower) {
		super(TVEntities.SHOCK_ORB, level, thrower, 1.5F, 1.0F);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.05;
	}

	private boolean canSee(Vec3 to) {
		return this.level().clip(new ClipContext(this.position(), to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType()
			== HitResult.Type.MISS;
	}

	@Override
	protected void onHit(HitResult hit) {
		if (this.level() instanceof ServerLevel level) {
			for (Entity e : level.getEntities(this, this.getBoundingBox().inflate(this.area))) {
				if (e.distanceTo(this) <= this.area && this.canSee(e.getEyePosition())) {
					e.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()), this.damage);
				}
			}
			for (int a = 0; a < 20; a++) {
				int xx = Mth.floor(this.getX()) + this.random.nextInt(this.area) - this.random.nextInt(this.area);
				int yy = Mth.floor(this.getY()) + this.area;
				int zz = Mth.floor(this.getZ()) + this.random.nextInt(this.area) - this.random.nextInt(this.area);
				while (level.isEmptyBlock(new BlockPos(xx, yy, zz)) && yy > Mth.floor(this.getY()) - this.area) {
					yy--;
				}
				BlockPos above = new BlockPos(xx, yy + 1, zz);
				if (level.isEmptyBlock(above) && !level.isEmptyBlock(new BlockPos(xx, yy, zz)) && this.canSee(new Vec3(xx + 0.5, yy + 1.5, zz + 0.5))) {
					level.setBlock(above, TVBlocks.STATIC_FIELD.defaultBlockState(), 3);
				}
			}
			TVNetwork.sendEntityFx(this, 1, 0, 300);
			level.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.SHOCK, SoundSource.NEUTRAL, 1.0F,
				1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
		}
		this.discard();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.tickCount > 500) {
			this.discard();
		}
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.getEntity() != null) {
			this.setDeltaMovement(source.getEntity().getLookAngle().scale(0.9));
			this.needsSync = true;
			level.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.ZAP, SoundSource.NEUTRAL, 1.0F,
				1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
			return true;
		}
		return false;
	}
}
