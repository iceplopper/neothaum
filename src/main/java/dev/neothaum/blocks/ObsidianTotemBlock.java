package dev.neothaum.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * BlockCosmeticSolid metadata 0, the obsidian totem (getBlockTexture): stacked, the top block shows a carved face on every side and those
 * beneath it are shaded; a lone totem is plain. Which of the four faces each side shows depends on the block's position.
 */
public class ObsidianTotemBlock extends Block {
	public enum Part implements StringRepresentable {
		SINGLE, TOP, BELOW;

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase();
		}
	}

	public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
	public static final IntegerProperty FACE = IntegerProperty.create("face", 0, 3);

	public ObsidianTotemBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(PART, Part.SINGLE).setValue(FACE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PART, FACE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.shape(ctx.getLevel(), ctx.getClickedPos());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
		BlockState neighbour, RandomSource random) {
		return dir.getAxis() == Direction.Axis.Y ? this.shape(level, pos) : state;
	}

	private BlockState shape(BlockGetter level, BlockPos pos) {
		boolean above = level.getBlockState(pos.above()).is(this);
		boolean below = level.getBlockState(pos.below()).is(this);
		Part part = above ? Part.BELOW : below ? Part.TOP : Part.SINGLE;
		int face = Math.floorMod(pos.getX() % 4 + pos.getZ() % 4 + pos.getY() % 4, 4);
		return this.defaultBlockState().setValue(PART, part).setValue(FACE, face);
	}
}
