package dev.neothaum.blocks.eldritch;

import net.minecraft.world.level.block.Block;

/** Ancient eldritch stone with obsidian-like durability. */
public class EldritchStoneBlock extends Block {
	public enum Variant {
		SMOOTH,
		CRACKED,
		GLYPH
	}

	private final Variant variant;

	public EldritchStoneBlock(Variant variant, Properties props) {
		super(props);
		this.variant = variant;
	}

	public Variant getVariant() {
		return this.variant;
	}
}
