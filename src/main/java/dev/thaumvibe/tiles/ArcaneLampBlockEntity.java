package dev.thaumvibe.tiles;

import dev.thaumvibe.registry.TVBlockEntities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileArcaneLamp: emits light 15 and places invisible light blocks within a 16-block radius
 * where light is below 9. Cleans up placed lights when removed.
 */
public class ArcaneLampBlockEntity extends TVBlockEntity {
	private final Set<BlockPos> lights = new HashSet<>();
	private int tickCount = 0;

	public ArcaneLampBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_LAMP, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ArcaneLampBlockEntity lamp) {
		if (level.isClientSide()) {
			return;
		}
		lamp.serverTick(level, pos);
	}

	private void serverTick(Level level, BlockPos pos) {
		this.tickCount++;
		if (this.tickCount % 100 == 0) {
			this.lights.removeIf(p -> level.isLoaded(p) && !level.getBlockState(p).is(Blocks.LIGHT));
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
		if (level.isLoaded(target)) {
			BlockState targetState = level.getBlockState(target);
			if (targetState.isAir() && level.getBrightness(LightLayer.BLOCK, target) < 9) {
				level.setBlock(target, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
				this.lights.add(target);
				this.setChanged();
			}
		}
	}

	public void removeLights(Level level) {
		for (BlockPos p : this.lights) {
			if (level.isLoaded(p) && level.getBlockState(p).is(Blocks.LIGHT)) {
				level.removeBlock(p, false);
			}
		}
		this.lights.clear();

		BlockPos origin = this.worldPosition;
		for (int dx = -15; dx <= 15; dx++) {
			for (int dy = -15; dy <= 15; dy++) {
				for (int dz = -15; dz <= 15; dz++) {
					BlockPos p = origin.offset(dx, dy, dz);
					if (level.isLoaded(p) && level.getBlockState(p).is(Blocks.LIGHT)) {
						level.removeBlock(p, false);
					}
				}
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.lights.clear();
		input.read("lights", BlockPos.CODEC.listOf()).ifPresent(this.lights::addAll);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!this.lights.isEmpty()) {
			output.store("lights", BlockPos.CODEC.listOf(), new ArrayList<>(this.lights));
		}
	}
}
