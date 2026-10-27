package dev.neothaum.blocks;

import dev.neothaum.fx.FX;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Greatwood / silverwood leaves (BlockMagicalLeaves). Silverwood leaves occasionally sparkle. */
public class MagicalLeavesBlock extends LeavesBlock {
	private final boolean silverwood;

	public MagicalLeavesBlock(boolean silverwood, BlockBehaviour.Properties props) {
		super(AmbientLeavesBlockSoundPlayer.noAmbientSound(), props);
		this.silverwood = silverwood;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);
		if (this.silverwood && random.nextInt(500) == 0) {
			FX.sparkle(level, pos.getX() + 0.5F + random.nextFloat() - random.nextFloat(), pos.getY() + 0.5F + random.nextFloat() - random.nextFloat(),
				pos.getZ() + 0.5F + random.nextFloat() - random.nextFloat(), 2.0F, 7, 0.0F);
		}
	}
}
