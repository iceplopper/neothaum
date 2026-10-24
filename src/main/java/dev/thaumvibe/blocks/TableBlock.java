package dev.thaumvibe.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** BlockTable metas 0/1: a plain wooden table. Wand it into an arcane worktable; use scribing tools on two for a research table. */
public class TableBlock extends Block {
	public TableBlock(BlockBehaviour.Properties props) {
		super(props);
	}
}
