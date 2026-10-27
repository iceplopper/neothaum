package dev.neothaum.blocks;

import dev.neothaum.tiles.ArcanePressurePlateBlockEntity;
import dev.neothaum.tiles.OwnedBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/** Port of BlockWoodenDevice meta 2: Arcane Pressure Plate with 3 entity trigger modes. */
public class ArcanePressurePlateBlock extends BasePressurePlateBlock implements EntityBlock {
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final IntegerProperty SETTING = IntegerProperty.create("setting", 0, 2);

	public ArcanePressurePlateBlock(BlockBehaviour.Properties props) {
		super(props, BlockSetType.IRON);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(SETTING, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED, SETTING);
	}

	@Override
	protected int getSignalForState(BlockState state) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected BlockState setSignalForState(BlockState state, int signal) {
		return state.setValue(POWERED, signal > 0);
	}

	@Override
	protected int getSignalStrength(Level level, BlockPos pos) {
		AABB aabb = TOUCH_AABB.move(pos);
		List<Entity> entities = level.getEntities((Entity) null, aabb);
		if (entities.isEmpty()) {
			return 0;
		}

		ArcanePressurePlateBlockEntity be = level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity e ? e : null;
		int setting = be != null ? be.getSetting() : 0;

		boolean active = false;
		for (Entity e : entities) {
			if (!e.isIgnoringBlockTriggers()) {
				boolean isPlayer = e instanceof Player;
				boolean permitted = isPlayer && be != null && be.hasAccess((Player) e);

				if (setting == 0) {
					active = true;
					break;
				} else if (setting == 1) {
					if (!permitted) {
						active = true;
						break;
					}
				} else if (setting == 2) {
					if (permitted) {
						active = true;
						break;
					}
				}
			}
		}

		if (active && !level.isClientSide()) {
			// Trigger adjacent Arcane Doors if permitted
			notifyAdjacentArcaneDoors(level, pos, be, true);
		} else if (!active && !level.isClientSide()) {
			notifyAdjacentArcaneDoors(level, pos, be, false);
		}

		return active ? 15 : 0;
	}

	private static void notifyAdjacentArcaneDoors(Level level, BlockPos pos, ArcanePressurePlateBlockEntity plateBe, boolean open) {
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos check = pos.relative(dir);
			BlockState s = level.getBlockState(check);
			if (s.getBlock() instanceof ArcaneDoorBlock door) {
				if (level.getBlockEntity(check) instanceof OwnedBlockEntity doorBe) {
					// Check if plate owner has access to door
					boolean allowed = plateBe == null || doorBe.isOwner(null) // unowned
						|| (plateBe.getOwnerUuid() != null && plateBe.getOwnerUuid().equals(doorBe.getOwnerUuid()))
						|| (!plateBe.getOwnerName().isEmpty() && plateBe.getOwnerName().equalsIgnoreCase(doorBe.getOwnerName()));
					if (allowed) {
						door.setOpen(null, level, s, check, open);
					}
				}
			}
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity be) {
			if (be.isOwner(player) || be.hasAdminAccess(player)) {
				if (!level.isClientSide()) {
					be.cycleSetting(player);
					level.setBlock(pos, state.setValue(SETTING, be.getSetting()), 3);
					level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.9F);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (placer instanceof Player player && level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity be) {
			be.setOwner(player);
		}
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof OwnedBlockEntity be) {
			if (!be.isOwner(player)) {
				return 0.0F; // Unbreakable to non-owners
			}
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ArcanePressurePlateBlockEntity(pos, state);
	}
}
