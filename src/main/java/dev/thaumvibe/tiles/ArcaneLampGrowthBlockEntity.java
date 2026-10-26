package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.blocks.AbstractLampBlock;
import dev.thaumvibe.blocks.LampOfGrowthBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileArcaneLampGrowth: feeds on Herba essentia (Aspect.PLANT; 1 Herba = 100 charges).
 * Scans a 6-block radius and accelerates crop/plant growth.
 */
public class ArcaneLampGrowthBlockEntity extends TVBlockEntity implements EssentiaTransport {
	private static final int DISTANCE = 6;
	public int charges = -1;
	private boolean reserve = false;
	private int drawDelay = 0;
	private final List<long[]> checklist = new ArrayList<>();

	public ArcaneLampGrowthBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.LAMP_OF_GROWTH, pos, state);
	}

	public Direction getFacing() {
		BlockState state = this.getBlockState();
		return state.hasProperty(AbstractLampBlock.FACING) ? state.getValue(AbstractLampBlock.FACING) : Direction.DOWN;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ArcaneLampGrowthBlockEntity lamp) {
		if (level.isClientSide()) {
			return;
		}
		lamp.serverTick(level, pos, state);
	}

	private void serverTick(Level level, BlockPos pos, BlockState state) {
		Direction facing = this.getFacing();

		if (this.charges <= 0) {
			if (this.reserve) {
				this.charges = 100;
				this.reserve = false;
				this.setChanged();
			} else if (this.drawEssentia(level, pos, facing)) {
				this.charges = 100;
				this.setChanged();
			}
		}

		if (!this.reserve && this.drawEssentia(level, pos, facing)) {
			this.reserve = true;
			this.setChanged();
		}

		if (this.charges == 0) {
			this.charges = -1;
			this.setChanged();
		}

		boolean isLit = this.charges > 0;
		if (state.hasProperty(LampOfGrowthBlock.LIT) && state.getValue(LampOfGrowthBlock.LIT) != isLit) {
			level.setBlock(pos, state.setValue(LampOfGrowthBlock.LIT, isLit), 3);
		}

		if (this.charges > 0) {
			this.updatePlant(level, pos);
		}
	}

	private boolean drawEssentia(Level level, BlockPos pos, Direction facing) {
		if (++this.drawDelay % 5 != 0) {
			return false;
		}
		EssentiaTransport transport = EssentiaTransport.connectable(level, pos, facing);
		if (transport != null) {
			Direction opp = facing.getOpposite();
			if (!transport.canOutputTo(opp)) {
				return false;
			}
			if (transport.getSuctionFor(Aspect.PLANT, opp) < this.getSuctionAmount(facing)
				&& transport.takeEssentia(Aspect.PLANT, 1, opp) == 1) {
				return true;
			}
		}
		return false;
	}

	private void updatePlant(Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		if (this.checklist.isEmpty()) {
			for (int a = -DISTANCE; a <= DISTANCE; a++) {
				for (int b = -DISTANCE; b <= DISTANCE; b++) {
					if (a * a + b * b <= DISTANCE * DISTANCE) {
						this.checklist.add(new long[]{a, b});
					}
				}
			}
			Collections.shuffle(this.checklist);
		}

		long[] col = this.checklist.remove(0);
		int dx = (int) col[0];
		int dz = (int) col[1];

		for (int dy = DISTANCE; dy >= -DISTANCE; dy--) {
			BlockPos target = pos.offset(dx, dy, dz);
			if (pos.distSqr(target) > DISTANCE * DISTANCE) {
				continue;
			}
			BlockState targetState = level.getBlockState(target);
			Block block = targetState.getBlock();

			if (block instanceof BonemealableBlock bmb && !(block instanceof GrassBlock) && !(block instanceof NyliumBlock)
				&& !targetState.is(Blocks.MOSS_BLOCK)) {
				if (bmb.isValidBonemealTarget(level, target, targetState, BonemealSource.INTERACTION)) {
					bmb.performBonemeal(serverLevel, level.getRandom(), target, targetState, BonemealSource.INTERACTION);
					serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 4, 0.3,
						0.3, 0.3, 0.0);
					this.charges--;
					this.setChanged();
					return;
				}
			} else if (block instanceof NetherWartBlock && targetState.getValue(NetherWartBlock.AGE) < 3) {
				level.setBlock(target, targetState.setValue(NetherWartBlock.AGE, targetState.getValue(NetherWartBlock.AGE) + 1), 2);
				serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 4, 0.3, 0.3,
					0.3, 0.0);
				this.charges--;
				this.setChanged();
				return;
			} else if (block instanceof SugarCaneBlock || block instanceof CactusBlock) {
				if (level.isEmptyBlock(target.above())) {
					int h = 1;
					while (level.getBlockState(target.below(h)).is(block)) {
						h++;
					}
					if (h < 3) {
						level.setBlock(target.above(), block.defaultBlockState(), 3);
						serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5, target.getY() + 1.5, target.getZ() + 0.5, 4,
							0.3, 0.3, 0.3, 0.0);
						this.charges--;
						this.setChanged();
						return;
					}
				}
			}
		}
	}

	@Override
	public boolean isConnectable(Direction face) {
		return face == this.getFacing();
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face == this.getFacing();
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return false;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		return Aspect.PLANT;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		return (face != this.getFacing() || (this.reserve && this.charges > 0)) ? 0 : 128;
	}

	@Override
	public Aspect getEssentiaType(Direction face) {
		return null;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		return 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}

	@Override
	public int getMinimumSuction() {
		return 0;
	}

	@Override
	public boolean renderExtendedTube() {
		return false;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.charges = input.getIntOr("charges", -1);
		this.reserve = input.getBooleanOr("reserve", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("charges", this.charges);
		output.putBoolean("reserve", this.reserve);
	}
}
