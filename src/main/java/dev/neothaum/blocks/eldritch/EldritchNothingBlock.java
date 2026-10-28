package dev.neothaum.blocks.eldritch;

import dev.neothaum.tiles.eldritch.EldritchNothingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockEldritchNothing: the void that walls in the Outer Lands maze. Unbreakable, drawn as a window onto a starfield wherever it faces open
 * space, and it tears at anything that pushes into it (8 damage a tick, after an entity's first second; creative players are spared).
 */
public class EldritchNothingBlock extends BaseEntityBlock {
	private static final VoxelShape INSET = Shapes.box(0.125, 0.125, 0.125, 0.875, 0.875, 0.875);

	public EldritchNothingBlock(Properties props) {
		super(props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EldritchNothingBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	/** A slightly smaller box, so entities brushing it are caught by it. */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return INSET;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean isPrecise) {
		if (level instanceof ServerLevel sl && entity.tickCount > 20 && !(entity instanceof Player p && p.isCreative())) {
			entity.hurtServer(sl, sl.damageSources().fellOutOfWorld(), 8.0F);
		}
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return ItemStack.EMPTY;
	}
}
