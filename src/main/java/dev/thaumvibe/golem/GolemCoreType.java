package dev.thaumvibe.golem;

import net.minecraft.util.StringRepresentable;

/**
 * Port of ItemGolemCore types:
 * Cores dictating golem behavior and functionality.
 */
public enum GolemCoreType implements StringRepresentable {
	FILL(0, "fill", "golem_core_fill", true, true),
	EMPTY(1, "empty", "golem_core_empty", true, true),
	GATHER(2, "gather", "golem_core_gather", true, true),
	HARVEST(3, "harvest", "golem_core_harvest", false, false),
	GUARD(4, "guard", "golem_core_guard", false, true),
	DECANT(5, "liquid", "golem_core_liquid", true, true),
	ALCHEMY(6, "essentia", "golem_core_essentia", true, false),
	CHOP(7, "lumber", "golem_core_lumber", false, false),
	USE(8, "use", "golem_core_use", true, true),
	BUTCHER(9, "butcher", "golem_core_butcher", false, false),
	SORT(10, "sorting", "golem_core_sorting", true, true),
	FISH(11, "fish", "golem_core_fish", false, false),
	BLANK(100, "blank", "golem_core_blank", false, false);

	public final int id;
	private final String name;
	public final String texture;
	public final boolean hasInventory;
	public final boolean hasGUI;

	GolemCoreType(int id, String name, String texture, boolean hasInventory, boolean hasGUI) {
		this.id = id;
		this.name = name;
		this.texture = texture;
		this.hasInventory = hasInventory;
		this.hasGUI = hasGUI;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static GolemCoreType byId(int id) {
		for (GolemCoreType t : values()) {
			if (t.id == id) {
				return t;
			}
		}
		return BLANK;
	}
}
