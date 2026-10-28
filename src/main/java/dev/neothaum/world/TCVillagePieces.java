package dev.neothaum.world;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * The blocks of TC4's two village buildings, ComponentWizardTower and ComponentBankerHome, in their own coordinates with the doorway on the
 * low-z side. They are built once into structure templates (by the "templates" gametest scene) that villages place from their house pools;
 * the levelling, the foundation and the clearing above that the 1.7 components did by hand is left to the village's terrain adaptation.
 */
public final class TCVillagePieces {
	/** Where the entrance jigsaw goes: one block in front of the door, at floor level. */
	public static final BlockPos TOWER_ENTRANCE = new BlockPos(3, 0, 0);
	public static final BlockPos BANKER_ENTRANCE = new BlockPos(1, 0, 0);
	/** The tower's chest, filled from neothaum:chests/wizard_tower (towerChestContents). */
	public static final BlockPos TOWER_CHEST = new BlockPos(2, 6, 2);

	private TCVillagePieces() {
	}

	private static void fill(BiConsumer<BlockPos, BlockState> set, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
		for (BlockPos p : BlockPos.betweenClosed(x0, y0, z0, x1, y1, z1)) {
			set.accept(p.immutable(), s);
		}
	}

	private static void door(BiConsumer<BlockPos, BlockState> set, int x, int y, int z) {
		BlockState d = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
		set.accept(new BlockPos(x, y, z), d.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
		set.accept(new BlockPos(x, y + 1, z), d.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
	}

	/** The stairs up to the door, which the entrance jigsaw turns into once placed. */
	public static BlockState entranceStairs() {
		return Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, Half.BOTTOM);
	}

	/** ComponentWizardTower.addComponentParts: a five-wide cobblestone tower of three floors, the ladder in a corner, a chest upstairs. */
	public static void wizardTower(BiConsumer<BlockPos, BlockState> set) {
		BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
		BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
		fill(set, 2, 1, 2, 4, 11, 4, Blocks.AIR.defaultBlockState());
		fill(set, 2, 0, 2, 4, 0, 4, planks);
		fill(set, 2, 5, 2, 4, 5, 4, planks);
		fill(set, 2, 10, 2, 4, 10, 4, planks);
		fill(set, 1, 0, 2, 1, 11, 4, cobble);
		fill(set, 2, 0, 1, 4, 11, 1, cobble);
		fill(set, 5, 0, 2, 5, 11, 4, cobble);
		fill(set, 2, 0, 5, 4, 11, 5, cobble);
		for (int y : new int[]{0, 5, 10}) {
			for (int x : new int[]{1, 5}) {
				for (int z : new int[]{1, 5}) {
					set.accept(new BlockPos(x, y, z), cobble);
				}
			}
		}
		BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
		for (int[] p : new int[][]{{3, 7, 1}, {3, 8, 1}, {3, 7, 5}, {3, 8, 5}, {3, 2, 5}, {3, 3, 5}}) {
			set.accept(new BlockPos(p[0], p[1], p[2]), pane);
		}
		for (int y = 1; y <= 9; y++) {
			set.accept(new BlockPos(4, y, 3), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
		}
		set.accept(new BlockPos(4, 10, 3), Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.WEST)
			.setValue(TrapDoorBlock.OPEN, true));
		set.accept(new BlockPos(3, 5, 3), Blocks.GLOWSTONE.defaultBlockState());
		set.accept(TOWER_CHEST, Blocks.CHEST.defaultBlockState());
		door(set, 3, 1, 1);
	}

	/**
	 * ComponentBankerHome.addComponentParts, moved one block along z so its front step is inside the template: a small timber house with iron
	 * bar windows, its log roof one block lower when it is the "tall" kind, and in two of three a fence-post table by the back wall.
	 */
	public static void bankerHome(BiConsumer<BlockPos, BlockState> set, boolean tall, int tablePosition) {
		BiConsumer<BlockPos, BlockState> at = (p, s) -> set.accept(p.offset(0, 0, 1), s);
		BlockState log = Blocks.OAK_LOG.defaultBlockState();
		BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
		fill(at, 1, 1, 1, 3, 5, 4, Blocks.AIR.defaultBlockState());
		fill(at, 0, 0, 0, 3, 0, 4, Blocks.COBBLESTONE.defaultBlockState());
		fill(at, 1, 0, 1, 2, 0, 3, Blocks.DIRT.defaultBlockState());
		int roof = tall ? 4 : 5;
		fill(at, 1, roof, 1, 2, roof, 3, log);
		for (int[] p : new int[][]{{1, 0}, {2, 0}, {1, 4}, {2, 4}, {0, 1}, {0, 2}, {0, 3}, {3, 1}, {3, 2}, {3, 3}}) {
			at.accept(new BlockPos(p[0], 4, p[1]), log);
		}
		fill(at, 0, 1, 0, 0, 3, 0, log);
		fill(at, 3, 1, 0, 3, 3, 0, log);
		fill(at, 0, 1, 4, 0, 3, 4, log);
		fill(at, 3, 1, 4, 3, 3, 4, log);
		fill(at, 0, 1, 1, 0, 3, 3, planks);
		fill(at, 3, 1, 1, 3, 3, 3, planks);
		fill(at, 1, 1, 0, 2, 3, 0, planks);
		fill(at, 1, 1, 4, 2, 3, 4, planks);
		at.accept(new BlockPos(0, 2, 2), Blocks.IRON_BARS.defaultBlockState());
		at.accept(new BlockPos(3, 2, 2), Blocks.IRON_BARS.defaultBlockState());
		if (tablePosition > 0) {
			at.accept(new BlockPos(tablePosition, 1, 3), Blocks.COBBLESTONE_WALL.defaultBlockState());
			at.accept(new BlockPos(tablePosition, 2, 3), Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
		}
		door(at, 1, 1, 0);
	}
}
