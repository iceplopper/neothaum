package dev.neothaum.golem;

import net.minecraft.util.StringRepresentable;

/**
 * Port of EnumGolemType:
 * Materials for Thaumcraft golems with respective base stats.
 */
public enum GolemType implements StringRepresentable {
	STRAW("straw", 10, 0, 0.38F, false, 1, 1, 75, 0),
	WOOD("wood", 20, 6, 0.35F, false, 1, 4, 75, 1),
	TALLOW("tallow", 20, 9, 0.33F, false, 2, 8, 75, 2),
	CLAY("clay", 25, 9, 0.33F, true, 1, 8, 100, 2),
	FLESH("flesh", 15, 6, 0.35F, false, 2, 4, 40, 1),
	STONE("stone", 30, 12, 0.32F, true, 1, 16, 100, 3),
	IRON("iron", 35, 15, 0.31F, true, 1, 32, 125, 4),
	THAUMIUM("thaumium", 40, 15, 0.32F, true, 2, 32, 100, 4),
	COPPER("copper", 25, 9, 0.31F, true, 1, 8, 80, 2);

	private final String name;
	public final int health;
	public final int armor;
	public final float speed;
	public final boolean fireResist;
	public final int upgrades;
	public final int carry;
	public final int regenDelay;
	public final int strength;

	GolemType(String name, int health, int armor, float speed, boolean fireResist, int upgrades, int carry, int regenDelay, int strength) {
		this.name = name;
		this.health = health;
		this.armor = armor;
		this.speed = speed;
		this.fireResist = fireResist;
		this.upgrades = upgrades;
		this.carry = carry;
		this.regenDelay = regenDelay;
		this.strength = strength;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static GolemType byId(int id) {
		GolemType[] values = values();
		if (id < 0 || id >= values.length) {
			return WOOD;
		}
		return values[id];
	}
}
