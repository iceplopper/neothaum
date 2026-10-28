package dev.neothaum.golem;

import net.minecraft.world.item.Item;

/**
 * Port of ItemGolemDecoration:
 * Aesthetic and functional accessories (hats, spectacles, bowties, armor, etc.) worn by golems.
 */
public class GolemDecorationItem extends Item {
	private final GolemDecorationType decorationType;

	public GolemDecorationItem(GolemDecorationType type, Properties props) {
		super(props);
		this.decorationType = type;
	}

	public GolemDecorationType getDecorationType() {
		return this.decorationType;
	}
}
