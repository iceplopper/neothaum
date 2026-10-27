package dev.thaumvibe.golem;

import dev.thaumvibe.api.aspects.Aspect;
import net.minecraft.util.StringRepresentable;

/**
 * Port of ItemGolemUpgrade types:
 * Primal upgrades modifying golem capabilities.
 */
public enum GolemUpgradeType implements StringRepresentable {
	AIR(0, "air", "golem_upgrade_air", Aspect.AIR),
	EARTH(1, "earth", "golem_upgrade_earth", Aspect.EARTH),
	FIRE(2, "fire", "golem_upgrade_fire", Aspect.FIRE),
	WATER(3, "water", "golem_upgrade_water", Aspect.WATER),
	ORDER(4, "order", "golem_upgrade_order", Aspect.ORDER),
	ENTROPY(5, "entropy", "golem_upgrade_entropy", Aspect.ENTROPY);

	public final int id;
	private final String name;
	public final String texture;
	public final Aspect aspect;

	GolemUpgradeType(int id, String name, String texture, Aspect aspect) {
		this.id = id;
		this.name = name;
		this.texture = texture;
		this.aspect = aspect;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static GolemUpgradeType byId(int id) {
		GolemUpgradeType[] values = values();
		if (id < 0 || id >= values.length) {
			return AIR;
		}
		return values[id];
	}
}
