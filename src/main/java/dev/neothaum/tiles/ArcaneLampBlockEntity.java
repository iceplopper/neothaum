package dev.neothaum.tiles;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileArcaneLamp: every tick picks a random spot within 15 blocks (no higher than 4 above the ground) and, if it is empty and its light
 * (sky or block, whichever is brighter) is below 9, puts an invisible lamp light there. Breaking the lamp clears the lights around it.
 */
public class ArcaneLampBlockEntity extends TVBlockEntity {
	/** Vanilla light blocks placed by NeoThaum before 1.0.4, still cleared when the lamp goes. */
	private final Set<BlockPos> legacyLights = new HashSet<>();

	public ArcaneLampBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_LAMP, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ArcaneLampBlockEntity lamp) {
		if (level.isClientSide()) {
			return;
		}
		RandomSource rand = level.getRandom();
		int x = pos.getX() + rand.nextInt(16) - rand.nextInt(16);
		int y = pos.getY() + rand.nextInt(16) - rand.nextInt(16);
		int z = pos.getZ() + rand.nextInt(16) - rand.nextInt(16);
		int height = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		if (y > height + 4) {
			y = height + 4;
		}
		if (y < level.getMinY() + 5) {
			y = level.getMinY() + 5;
		}
		BlockPos target = new BlockPos(x, y, z);
		if (level.isLoaded(target) && level.isEmptyBlock(target) && level.getMaxLocalRawBrightness(target) < 9) {
			level.setBlock(target, TVBlocks.LAMP_LIGHT.defaultBlockState(), 3);
		}
	}

	/** TileArcaneLamp.removeLights: clears every lamp light within 15 blocks of where the lamp stood. */
	public static void clearLights(Level level, BlockPos origin) {
		for (BlockPos p : BlockPos.betweenClosed(origin.offset(-15, -15, -15), origin.offset(15, 15, 15))) {
			if (level.isLoaded(p) && level.getBlockState(p).is(TVBlocks.LAMP_LIGHT)) {
				level.removeBlock(p, false);
			}
		}
	}

	/** Also clears the vanilla light blocks older versions placed. */
	public void removeLights(Level level) {
		clearLights(level, this.worldPosition);
		for (BlockPos p : this.legacyLights) {
			if (level.isLoaded(p) && level.getBlockState(p).is(Blocks.LIGHT)) {
				level.removeBlock(p, false);
			}
		}
		this.legacyLights.clear();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.legacyLights.clear();
		input.read("lights", BlockPos.CODEC.listOf()).ifPresent(this.legacyLights::addAll);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!this.legacyLights.isEmpty()) {
			output.store("lights", BlockPos.CODEC.listOf(), new ArrayList<>(this.legacyLights));
		}
	}
}
