package dev.neothaum.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * NeoThaum Addition: aspects for other mods' items. An item from another mod with no aspects of its own and no recipe to work them out
 * from takes them from its common tags (the first that fits, most specific first). Crafted items then get theirs from these through TC4's
 * recipe rule as usual. Vanilla and NeoThaum items are left exactly as TC4 has them. A datapack entry in aspects.json still wins.
 */
public final class ModdedAspects {
	private record Rule(TagKey<Item> tag, AspectList aspects) {
	}

	private static final Rule[] RULES = {
		rule("c:ores", "terra", 1, "metallum", 2),
		rule("c:raw_materials", "metallum", 2, "terra", 1),
		rule("c:ingots", "metallum", 3),
		rule("c:nuggets", "metallum", 1),
		rule("c:gems", "vitreus", 3, "lucrum", 2),
		rule("c:dusts", "perditio", 1, "terra", 1),
		rule("c:tools/melee_weapon", "telum", 2, "instrumentum", 1),
		rule("c:tools", "instrumentum", 2),
		rule("c:armors", "tutamen", 3),
		rule("c:foods", "fames", 2),
		rule("c:crops", "messis", 1, "herba", 1),
		rule("c:seeds", "herba", 1),
		rule("minecraft:saplings", "herba", 2, "arbor", 1),
		rule("minecraft:logs", "arbor", 4),
		rule("minecraft:planks", "arbor", 1),
		rule("minecraft:leaves", "herba", 1),
		rule("minecraft:flowers", "herba", 1, "sensus", 1),
		rule("c:slime_balls", "limus", 2),
		rule("c:ender_pearls", "alienis", 2, "iter", 2),
		rule("c:glass_blocks", "vitreus", 1),
		rule("c:cobblestones", "terra", 1, "perditio", 1),
		rule("c:stones", "terra", 2),
		rule("c:dyes", "sensus", 1),
	};

	/** The aspects an item gets from its tags, or null when it is not another mod's, the addition is off, or no tag fits. */
	public static AspectList forItem(Item item) {
		if (!Additions.enabled("modded_aspects")) {
			return null;
		}
		String ns = BuiltInRegistries.ITEM.getKey(item).getNamespace();
		if (ns.equals("minecraft") || ns.equals(NeoThaum.MOD_ID)) {
			return null;
		}
		return fromTags(item);
	}

	/** The first tag rule that fits the item, or null. */
	public static AspectList fromTags(Item item) {
		var holder = BuiltInRegistries.ITEM.wrapAsHolder(item);
		for (Rule r : RULES) {
			if (holder.is(r.tag())) {
				return r.aspects().copy();
			}
		}
		return null;
	}

	private static Rule rule(String tag, Object... pairs) {
		AspectList l = new AspectList();
		for (int i = 0; i < pairs.length; i += 2) {
			l.add(Aspect.getAspect((String) pairs[i]), (Integer) pairs[i + 1]);
		}
		return new Rule(TagKey.create(Registries.ITEM, Identifier.parse(tag)), l);
	}

	private ModdedAspects() {
	}
}
