package dev.neothaum.tiles.eldritch;

import dev.neothaum.blocks.eldritch.CrustedOpeningBlock;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.entity.monster.EldritchCrabEntity;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * TileEldritchCrabSpawner: with a player within 16 blocks and no more than five crabs within 32, a crab crawls out every 7.5-12.5 seconds,
 * the vent fizzing and steaming just before. It steams idly the rest of the time.
 */
public class CrustedOpeningBlockEntity extends BlockEntity {
	private int count = 150;
	private int venting;

	public CrustedOpeningBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.CRUSTED_OPENING, pos, state);
	}

	private Direction facing() {
		return this.getBlockState().getValue(CrustedOpeningBlock.FACING);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CrustedOpeningBlockEntity vent) {
		if (level instanceof ServerLevel sl) {
			vent.count--;
			if (vent.count < 0) {
				vent.count = 50 + level.getRandom().nextInt(50);
			} else {
				if (vent.count == 15 && vent.isActivated() && !vent.maxEntitiesReached()) {
					level.blockEvent(pos, state.getBlock(), 1, 0);
					level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
				}
				if (vent.count <= 0 && vent.isActivated() && !vent.maxEntitiesReached()) {
					vent.count = 150 + level.getRandom().nextInt(100);
					vent.spawnCrab(sl);
					level.playSound(null, pos, TVSounds.GORE, SoundSource.BLOCKS, 0.5F, 1.0F);
				}
			}
		} else if (vent.venting > 0) {
			vent.venting--;
			for (int a = 0; a < 3; a++) {
				vent.drawVent();
			}
		} else if (level.getRandom().nextInt(20) == 0) {
			vent.drawVent();
		}
	}

	private void drawVent() {
		var r = this.level.getRandom();
		Direction dir = this.facing();
		double x = this.worldPosition.getX() + 0.5 + (0.15 - r.nextFloat() * 0.3) + dir.getStepX() / 2.1;
		double y = this.worldPosition.getY() + 0.5 + (0.15 - r.nextFloat() * 0.3) + dir.getStepY() / 2.1;
		double z = this.worldPosition.getZ() + 0.5 + (0.15 - r.nextFloat() * 0.3) + dir.getStepZ() / 2.1;
		this.level.addParticle(ParticleTypes.SMOKE, x, y, z, dir.getStepX() / 3.0 + 0.1 - r.nextFloat() * 0.2, dir.getStepY() / 3.0 + 0.1 - r.nextFloat() * 0.2,
			dir.getStepZ() / 3.0 + 0.1 - r.nextFloat() * 0.2);
	}

	public boolean triggerEvent(int id, int param) {
		if (id == 1) {
			this.venting = 20;
			return true;
		}
		return false;
	}

	private boolean maxEntitiesReached() {
		return this.level.getEntitiesOfClass(EldritchCrabEntity.class, new AABB(this.worldPosition).inflate(32.0)).size() > 5;
	}

	private boolean isActivated() {
		BlockPos p = this.worldPosition;
		return this.level.getNearestPlayer(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 16.0, false) != null;
	}

	private void spawnCrab(ServerLevel sl) {
		Direction dir = this.facing();
		EldritchCrabEntity crab = TVEntities.ELDRITCH_CRAB.create(sl, EntitySpawnReason.SPAWNER);
		if (crab == null) {
			return;
		}
		BlockPos at = this.worldPosition.relative(dir);
		crab.snapTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
		crab.finalizeSpawn(sl, sl.getCurrentDifficultyAt(at), EntitySpawnReason.SPAWNER, null);
		crab.setHelm(false);
		crab.setDeltaMovement(dir.getStepX() * 0.2, dir.getStepY() * 0.2, dir.getStepZ() * 0.2);
		sl.addFreshEntity(crab);
	}
}
