package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.api.visnet.VisNetHandler;
import dev.thaumvibe.blocks.FluxFluidBlock;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TC4 TileFluxScrubber:
 * Device that draws Aer centivis to seek and cleanse flux goo and flux gas within 16 blocks.
 * Cleansed flux accumulates charges which condense into Praecantatio essentia and output via tubes.
 */
public class FluxScrubberBlockEntity extends TVBlockEntity implements EssentiaTransport {
	public int essentia = 0;
	public int charges = 0;
	public int power = 0;
	public Direction facing = Direction.DOWN;
	public int count = 0;
	private final List<BlockPos> checklist = new ArrayList<>();

	public FluxScrubberBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.FLUX_SCRUBBER, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluxScrubberBlockEntity be) {
		be.tick(level, pos, state);
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (this.count == 0) {
			this.count = level.getRandom().nextInt(1000);
		}

		if (!level.isClientSide()) {
			// Convert 4 charges into Praecantatio essentia (25% chance)
			if (this.charges >= 4) {
				this.charges -= 4;
				if (level.getRandom().nextInt(4) == 0) {
					if (this.essentia < 4) {
						this.essentia++;
					}
					this.setChanged();
				}
			}

			// Draw Aer cv from vis network (needs 5 power to cleanse 1 flux block)
			if (this.power < 5) {
				this.power += VisNetHandler.drainVis(level, pos, Aspect.AIR, 10);
			}

			if (this.power >= 5) {
				this.checkFlux(level, pos);
			}
		}
	}

	private boolean isFlux(Level level, BlockPos p) {
		BlockState s = level.getBlockState(p);
		return s.is(TVBlocks.FLUX_GOO) || s.is(TVBlocks.FLUX_GAS) || s.getBlock() instanceof FluxFluidBlock;
	}

	private void checkFlux(Level level, BlockPos pos) {
		int distance = 16;
		if (this.checklist.isEmpty()) {
			for (int a = -distance; a <= distance; a++) {
				for (int b = -distance; b <= distance; b++) {
					for (int c = -distance; c <= distance; c++) {
						this.checklist.add(pos.offset(a, b, c));
					}
				}
			}
			Collections.shuffle(this.checklist);
		}

		int cc = 0;
		while (cc < 16 && !this.checklist.isEmpty()) {
			cc++;
			BlockPos target = this.checklist.remove(0);
			if (!level.isEmptyBlock(target) && this.isFlux(level, target) && pos.distSqr(target) <= distance * distance) {
				this.power -= 5;
				level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);

				// Sparkle effect at cleansed position (TC4: 14483711 / 0xDCF0FF)
				TVNetwork.sendBlockSparkle(level, target, 14483711);

				this.charges++;
				this.setChanged();
				this.markForUpdate();
				return;
			}
		}
	}

	public void setFacing(Direction dir) {
		this.facing = dir;
		this.markForUpdate();
	}

	// ------------------------------------------------------------------ EssentiaTransport
	@Override
	public boolean isConnectable(Direction face) {
		return face == this.facing;
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return face == this.facing;
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return false;
	}

	@Override
	public int getMinimumSuction() {
		return 0;
	}

	@Override
	public Aspect getSuctionType(Direction loc) {
		return null;
	}

	@Override
	public int getSuctionAmount(Direction loc) {
		return 0;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
	}

	@Override
	public Aspect getEssentiaType(Direction loc) {
		return Aspect.MAGIC; // Praecantatio
	}

	@Override
	public int getEssentiaAmount(Direction loc) {
		return this.essentia;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction loc) {
		if (loc == this.facing && (aspect == null || aspect == Aspect.MAGIC)) {
			int taken = Math.min(this.essentia, amount);
			this.essentia -= taken;
			this.setChanged();
			this.markForUpdate();
			return taken;
		}
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction loc) {
		return 0;
	}

	@Override
	public boolean renderExtendedTube() {
		return false;
	}

	// ------------------------------------------------------------------ Serialization
	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.facing = Direction.from3DDataValue(input.getIntOr("facing", Direction.DOWN.get3DDataValue()));
		this.charges = input.getIntOr("charges", 0);
		this.power = input.getIntOr("power", 0);
		this.essentia = input.getIntOr("essentia", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("facing", this.facing.get3DDataValue());
		output.putInt("charges", this.charges);
		output.putInt("power", this.power);
		output.putInt("essentia", this.essentia);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.putInt("facing", this.facing.get3DDataValue());
		tag.putInt("count", this.count);
		return tag;
	}
}
