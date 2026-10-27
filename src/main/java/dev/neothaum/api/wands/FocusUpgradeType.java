package dev.neothaum.api.wands;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Port of thaumcraft.api.wands.FocusUpgradeType: a numbered focus upgrade with its icon, name, description and research aspects. */
public class FocusUpgradeType {
	public static FocusUpgradeType[] types = new FocusUpgradeType[21];

	public final short id;
	public final Identifier icon;
	public final String name;
	public final String text;
	public final AspectList aspects;

	public static final FocusUpgradeType potency = new FocusUpgradeType(0, "potency", new AspectList().add(Aspect.WEAPON, 1));
	public static final FocusUpgradeType frugal = new FocusUpgradeType(1, "frugal", new AspectList().add(Aspect.HUNGER, 1));
	public static final FocusUpgradeType treasure = new FocusUpgradeType(2, "treasure", new AspectList().add(Aspect.GREED, 1));
	public static final FocusUpgradeType enlarge = new FocusUpgradeType(3, "enlarge", new AspectList().add(Aspect.TRAVEL, 1));
	public static final FocusUpgradeType alchemistsfire = new FocusUpgradeType(4, "alchemistsfire",
		new AspectList().add(Aspect.ENERGY, 1).add(Aspect.SLIME, 1));
	public static final FocusUpgradeType alchemistsfrost = new FocusUpgradeType(5, "alchemistsfrost",
		new AspectList().add(Aspect.COLD, 1).add(Aspect.TRAP, 1));
	public static final FocusUpgradeType architect = new FocusUpgradeType(6, "architect", new AspectList().add(Aspect.CRAFT, 1));
	public static final FocusUpgradeType extend = new FocusUpgradeType(7, "extend", new AspectList().add(Aspect.EXCHANGE, 1));
	public static final FocusUpgradeType silktouch = new FocusUpgradeType(8, "silktouch", new AspectList().add(Aspect.GREED, 1));

	public FocusUpgradeType(int id, String key, AspectList aspects) {
		this.id = (short) id;
		this.icon = Identifier.fromNamespaceAndPath("neothaum", "textures/foci/" + key + ".png");
		this.name = "focus.upgrade." + key + ".name";
		this.text = "focus.upgrade." + key + ".text";
		this.aspects = aspects;
		if (id >= types.length) {
			FocusUpgradeType[] temp = new FocusUpgradeType[id + 1];
			System.arraycopy(types, 0, temp, 0, types.length);
			types = temp;
		}
		types[id] = this;
	}

	public Component getLocalizedName() {
		return Component.translatable(this.name);
	}

	public Component getLocalizedText() {
		return Component.translatable(this.text);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof FocusUpgradeType t && t.id == this.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}
}
