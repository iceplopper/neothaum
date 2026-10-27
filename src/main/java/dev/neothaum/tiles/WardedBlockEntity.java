package dev.neothaum.tiles;

import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** TileWarded: the block a warded block holds, and its owner (TC4 keeps the hash of the owner's name). */
public class WardedBlockEntity extends TVBlockEntity {
	public BlockState held = Blocks.STONE.defaultBlockState();
	public int owner;

	public WardedBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.WARDED, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.held = input.read("held", BlockState.CODEC).orElse(Blocks.STONE.defaultBlockState());
		this.owner = input.getIntOr("owner", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("held", BlockState.CODEC, this.held);
		output.putInt("owner", this.owner);
	}
}
