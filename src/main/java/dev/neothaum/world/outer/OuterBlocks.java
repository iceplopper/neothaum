package dev.neothaum.world.outer;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/** TC4's metadata for the Outer Lands' stairs, slabs and loot, as block states. */
public final class OuterBlocks {
	private OuterBlocks() {
	}

	/** BlockStairs metadata: 0 east, 1 west, 2 south, 3 north; +4 upside down. */
	public static BlockState stairs(int meta) {
		Direction facing = switch (meta & 3) {
			case 0 -> Direction.EAST;
			case 1 -> Direction.WEST;
			case 2 -> Direction.SOUTH;
			default -> Direction.NORTH;
		};
		return TVBlocks.ANCIENT_STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing)
			.setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
	}

	/** blockSlabStone 1 (bottom) or 9 (top): the ancient stone slab. */
	public static BlockState slab(boolean top) {
		return TVBlocks.ANCIENT_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
	}

	/** blockLootUrn 0 common, 1 uncommon, 2 rare. */
	public static BlockState urn(int md) {
		return (md == 2 ? TVBlocks.LOOT_URN_RARE : md == 1 ? TVBlocks.LOOT_URN_UNCOMMON : TVBlocks.LOOT_URN_COMMON).defaultBlockState();
	}

	/** blockLootCrate 0 common, 1 uncommon, 2 rare. */
	public static BlockState crate(int md) {
		return (md == 2 ? TVBlocks.LOOT_CRATE_RARE : md == 1 ? TVBlocks.LOOT_CRATE_UNCOMMON : TVBlocks.LOOT_CRATE_COMMON).defaultBlockState();
	}

	public static BlockState obeliskBase() {
		return TVBlocks.ELDRITCH_OBELISK.defaultBlockState();
	}

	public static BlockState obeliskShaft() {
		return TVBlocks.ELDRITCH_OBELISK.defaultBlockState().setValue(dev.neothaum.blocks.eldritch.EldritchObeliskBlock.BASE, false);
	}
}
