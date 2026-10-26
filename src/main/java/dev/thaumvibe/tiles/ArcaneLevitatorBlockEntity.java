package dev.thaumvibe.tiles;

import dev.thaumvibe.blocks.ArcaneLevitatorBlock;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Port of TileLifter: lifts entities upward within its beam column. */
public class ArcaneLevitatorBlockEntity extends BlockEntity {
	private int rangeAbove = 0;
	private int counter = 0;
	public boolean requiresUpdate = true;

	public ArcaneLevitatorBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_LEVITATOR, pos, state);
	}

	public int getRangeAbove() {
		return this.rangeAbove;
	}

	public boolean isGettingPower() {
		if (this.level == null) {
			return false;
		}
		return this.level.hasNeighborSignal(this.worldPosition) || this.level.hasNeighborSignal(this.worldPosition.above());
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, ArcaneLevitatorBlockEntity tile) {
		if (!tile.isGettingPower() && tile.rangeAbove > 0) {
			RandomSource r = level.getRandom();
			FX.sparkle(level, pos.getX() + 0.2 + r.nextFloat() * 0.6, pos.getY() + 1.0, pos.getZ() + 0.2 + r.nextFloat() * 0.6, 1.0F, 3, -0.3F);
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneLevitatorBlockEntity tile) {
		tile.counter++;
		if (tile.requiresUpdate || tile.counter % 100 == 0) {
			tile.requiresUpdate = false;
			int max = 10;
			// Stacked levitators increase range by +10 each
			for (int count = 1; level.getBlockState(pos.below(count)).is(TVBlocks.ARCANE_LEVITATOR); count++) {
				if (!level.hasNeighborSignal(pos.below(count))) {
					max += 10;
				}
			}

			tile.rangeAbove = 0;
			while (tile.rangeAbove < max) {
				BlockPos check = pos.above(1 + tile.rangeAbove);
				BlockState s = level.getBlockState(check);
				if (!s.isAir() && s.isFaceSturdy(level, check, Direction.DOWN)) {
					break;
				}
				tile.rangeAbove++;
			}
		}

		if (tile.rangeAbove > 0 && !tile.isGettingPower()) {
			AABB beamBox = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(),
				pos.getX() + 1, pos.getY() + 1 + tile.rangeAbove, pos.getZ() + 1);
			List<Entity> targets = level.getEntities((Entity) null, beamBox);
			for (Entity e : targets) {
				if (e.isPassenger()) {
					continue;
				}
				boolean sneaking = e.isShiftKeyDown();
				Vec3 m = e.getDeltaMovement();
				if (sneaking) {
					// Softly lower sneaking players/entities
					if (m.y < 0.0) {
						e.setDeltaMovement(m.x, m.y * 0.9, m.z);
					}
				} else {
					// Lift upwards up to 0.35 max vertical velocity
					if (m.y < 0.35) {
						e.setDeltaMovement(m.x, Math.min(0.35, m.y + 0.1), m.z);
					}
				}
				e.resetFallDistance();
				e.syncVelocity = true;
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("range", this.rangeAbove);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.rangeAbove = input.getIntOr("range", 0);
	}
}
