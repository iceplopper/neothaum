package dev.neothaum.tiles;

import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.world.TVBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEtherealBloom: cleanses taint biomes and blocks in an 8-block radius.
 */
public class EtherealBloomBlockEntity extends BlockEntity {
	public int counter = 0;
	public int growthCounter = 0;

	public EtherealBloomBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ETHEREAL_BLOOM, pos, state);
	}

	public void clientTick(Level level, BlockPos pos, BlockState state) {
		if (this.growthCounter == 0) {
			level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TVSounds.ROOTS, SoundSource.BLOCKS, 1.0F, 0.6F, false);
		}
		this.growthCounter++;
	}

	public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		if (this.counter == 0) {
			this.counter = level.getRandom().nextInt(100);
		}
		this.counter++;

		if (this.counter % 20 == 0) {
			int dx = level.getRandom().nextInt(8) - level.getRandom().nextInt(8);
			int dz = level.getRandom().nextInt(8) - level.getRandom().nextInt(8);
			if (dx * dx + dz * dz <= 64) {
				int targetX = pos.getX() + dx;
				int targetZ = pos.getZ() + dz;
				BlockPos targetColPos = new BlockPos(targetX, pos.getY(), targetZ);

				// Cleanse biome
				if (TVBiomes.is(level, targetColPos, TVBiomes.TAINTED_LAND) || TVBiomes.is(level, targetColPos, TVBiomes.EERIE)) {
					TVBiomes.setBiomeAt(level, targetX, targetZ, Biomes.PLAINS);
				}

				// Cleanse vertical column of blocks within [-4, +4] range
				for (int dy = -4; dy <= 4; dy++) {
					BlockPos p = new BlockPos(targetX, pos.getY() + dy, targetZ);
					BlockState bs = level.getBlockState(p);

					if (bs.is(TVBlocks.TAINTED_SOIL)) {
						level.setBlock(p, Blocks.DIRT.defaultBlockState(), 3);
					} else if (bs.is(TVBlocks.CRUSTED_TAINT)) {
						level.destroyBlock(p, false);
					} else if (bs.is(TVBlocks.TAINT_FIBRES)) {
						level.removeBlock(p, false);
					} else if (bs.is(TVBlocks.FLUX_GOO) || bs.is(TVBlocks.FLUX_GAS)) {
						level.removeBlock(p, false);
					}
				}
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.counter = input.getIntOr("counter", 0);
		this.growthCounter = input.getIntOr("growthCounter", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("counter", this.counter);
		output.putInt("growthCounter", this.growthCounter);
	}
}
