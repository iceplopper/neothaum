package dev.neothaum.tiles;

import dev.neothaum.mixin.FurnaceAccessor;
import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileBellows: puffs away unless powered by redstone. Each bellows pointing into an alchemical furnace, crucible or essentia buffer counts
 * towards its bellows bonus; one on a vanilla furnace pushes its cooking along.
 */
public class BellowsBlockEntity extends TVBlockEntity {
	public float inflation = 1.0F;
	boolean direction;
	boolean firstrun = true;
	/** The side the nozzle points out of. */
	public Direction orientation = Direction.DOWN;
	public boolean onVanillaFurnace;
	public int delay;

	public BellowsBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.BELLOWS, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.orientation = Direction.from3DDataValue(input.getByteOr("orientation", (byte) 0));
		this.onVanillaFurnace = input.getBooleanOr("onVanillaFurnace", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putByte("orientation", (byte) this.orientation.get3DDataValue());
		output.putBoolean("onVanillaFurnace", this.onVanillaFurnace);
	}

	public boolean gettingPower() {
		return this.level != null && this.level.hasNeighborSignal(this.worldPosition);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity b) {
		if (level.isClientSide()) {
			if (!b.gettingPower()) {
				if (b.firstrun) {
					b.inflation = 0.35F + level.getRandom().nextFloat() * 0.55F;
				}
				b.firstrun = false;
				if (b.inflation > 0.35F && !b.direction) {
					b.inflation -= 0.075F;
				}
				if (b.inflation <= 0.35F && !b.direction) {
					b.direction = true;
				}
				if (b.inflation < 1.0F && b.direction) {
					b.inflation += 0.025F;
				}
				if (b.inflation >= 1.0F && b.direction) {
					b.direction = false;
					level.playLocalSound(pos, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 0.01F,
						0.5F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F, false);
				}
			}
		} else if (b.onVanillaFurnace && !b.gettingPower() && ++b.delay >= 2) {
			b.delay = 0;
			if (level.getBlockEntity(pos.relative(b.orientation)) instanceof AbstractFurnaceBlockEntity furnace) {
				FurnaceAccessor f = (FurnaceAccessor) furnace;
				int t = f.neothaum$getCookingTimer();
				if (t > 0 && t < f.neothaum$getCookingTotalTime() - 1) {
					f.neothaum$setCookingTimer(t + 1);
				}
			}
		}
	}

	/** TileBellows.getBellows: unpowered bellows around pos whose nozzles point at it. */
	public static int getBellows(Level level, BlockPos pos, Direction[] directions) {
		int bellows = 0;
		for (Direction dir : directions) {
			BlockPos p = pos.relative(dir);
			if (level.getBlockEntity(p) instanceof BellowsBlockEntity b && b.orientation == dir.getOpposite() && !level.hasNeighborSignal(p)) {
				bellows++;
			}
		}
		return bellows;
	}
}
