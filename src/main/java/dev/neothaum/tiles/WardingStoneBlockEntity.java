package dev.neothaum.tiles;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Port of TileWardingStone: maintains a 2-block high mob-repelling warding aura above it. */
public class WardingStoneBlockEntity extends BlockEntity {
	private int count = 0;

	public WardingStoneBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.WARDING_STONE, pos, state);
	}

	public boolean isGettingPower() {
		return this.level != null && this.level.hasNeighborSignal(this.worldPosition);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, WardingStoneBlockEntity tile) {
		tile.count++;
		if (tile.count % 5 == 0 && !tile.isGettingPower()) {
			AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
				pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1).inflate(0.1);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box);
			for (LivingEntity e : targets) {
				if (!e.onGround() && !(e instanceof Player)) {
					float yaw = e.getYRot() + 180.0F;
					Vec3 push = new Vec3(-Mth.sin(yaw * (float) Math.PI / 180.0F) * 0.2, -0.1,
						Mth.cos(yaw * (float) Math.PI / 180.0F) * 0.2);
					e.setDeltaMovement(e.getDeltaMovement().add(push));
					e.syncVelocity = true;
				}
			}
		}

		if (tile.count % 100 == 0) {
			for (int dy = 1; dy <= 2; dy++) {
				BlockPos target = pos.above(dy);
				BlockState s = level.getBlockState(target);
				if (!s.is(TVBlocks.WARDING_AURA) && (s.isAir() || s.canBeReplaced())) {
					level.setBlock(target, TVBlocks.WARDING_AURA.defaultBlockState(), 3);
				}
			}
		}
	}

	@Override
	public void setRemoved() {
		// Clean up aura above when broken
		if (this.level != null && !this.level.isClientSide()) {
			for (int dy = 1; dy <= 2; dy++) {
				BlockPos target = this.worldPosition.above(dy);
				if (this.level.getBlockState(target).is(TVBlocks.WARDING_AURA)) {
					this.level.removeBlock(target, false);
				}
			}
		}
		super.setRemoved();
	}
}
