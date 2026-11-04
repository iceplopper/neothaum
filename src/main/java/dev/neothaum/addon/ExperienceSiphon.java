package dev.neothaum.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * NeoThaum Addition: the Experience Siphon, an arcane stone plate that draws experience from a player who kneels (sneaks) on it into a store
 * of thirty levels, and pours it into empty warded jars, ten levels to a jar. Those jars hold experience instead of essentia, and tubes leave
 * them alone. A full jar and ten of each primal make Condensed Experience at the arcane worktable (the jar is kept), which gives the ten levels
 * back when eaten.
 *
 * <p>A "level" here always means the points from level 0 to that level, whatever level the player is at: ten levels are 160 points.
 */
public final class ExperienceSiphon {
	/** Points in a full jar: the 160 from level 0 to level 10. */
	public static final int JAR = pointsForLevels(10);
	/** Points the siphon holds: the 1395 from level 0 to level 30. */
	public static final int STORE = pointsForLevels(30);
	/** Points drawn each time: level 0 to 1. */
	public static final int DRAW = pointsForLevels(1);

	public static final Block SIPHON = TVBlocks.register("experience_siphon", ExperienceSiphonBlock::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 10.0F).sound(SoundType.STONE),
		false);
	public static final BlockEntityType<ExperienceSiphonBlockEntity> SIPHON_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
		NeoThaum.id("experience_siphon"), new BlockEntityType<>(ExperienceSiphonBlockEntity::new, Set.of(SIPHON)));
	public static Item siphonItem;
	public static Item condensed;

	public static boolean enabled() {
		return Additions.enabled("experience_siphon");
	}

	/** Minecraft's points from level 0 to this level. */
	public static int pointsForLevels(int level) {
		if (level <= 16) {
			return level * level + 6 * level;
		}
		if (level <= 31) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return (int) (4.5 * level * level - 162.5 * level + 2220);
	}

	/** The experience points a player has: their level's worth from zero and their progress into the next (totalExperience isn't kept
	 * up to date by every way of changing levels). */
	public static int pointsOf(net.minecraft.world.entity.player.Player p) {
		return pointsForLevels(p.experienceLevel) + Math.round(p.experienceProgress * p.getXpNeededForNextLevel());
	}

	/** The levels these points come to, counted from level 0, to one decimal place: for labels. */
	public static String levels(int points) {
		int level = 0;
		while (pointsForLevels(level + 1) <= points) {
			level++;
		}
		int span = pointsForLevels(level + 1) - pointsForLevels(level);
		int tenths = (points - pointsForLevels(level)) * 10 / Math.max(1, span);
		return tenths == 0 ? String.valueOf(level) : level + "." + tenths;
	}

	/** Whether the stack is a warded jar the siphon can fill: no essentia, no label, no experience yet. */
	public static boolean isEmptyJar(ItemStack stack) {
		return stack.is(TVBlocks.WARDED_JAR.asItem()) && stack.get(TVComponents.ASPECTS) == null && stack.get(TVComponents.ASPECT_FILTER) == null
			&& stack.getOrDefault(TVComponents.EXPERIENCE, 0) <= 0;
	}

	/** A warded jar holding experience. */
	public static boolean isExperienceJar(ItemStack stack) {
		return stack.is(TVBlocks.WARDED_JAR.asItem()) && stack.getOrDefault(TVComponents.EXPERIENCE, 0) > 0;
	}

	/** Called from NeoThaum.onInitialize after TVItems: the items. */
	public static void init() {
		var key = ResourceKey.create(Registries.ITEM, NeoThaum.id("experience_siphon"));
		siphonItem = Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(SIPHON, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
		var ckey = ResourceKey.create(Registries.ITEM, NeoThaum.id("condensed_experience"));
		condensed = Registry.register(BuiltInRegistries.ITEM, ckey, new CondensedExperienceItem(new Item.Properties().setId(ckey).stacksTo(16)
			.rarity(Rarity.UNCOMMON).component(net.minecraft.core.component.DataComponents.CONSUMABLE, Consumables.defaultFood().build())));
		int at = TVItems.TAB_ORDER.indexOf(TVBlocks.WARDED_JAR.asItem()) + 1;
		TVItems.TAB_ORDER.add(at > 0 ? at : TVItems.TAB_ORDER.size(), siphonItem);
		TVItems.TAB_ORDER.add(TVItems.TAB_ORDER.indexOf(siphonItem) + 1, condensed);
	}

	/** Whether the creative tab and recipe viewers show this item: the siphon's items only while it is on. */
	public static boolean shown(Item item) {
		return item != siphonItem && item != condensed || enabled();
	}

	/** Loads the class (registering the block) from NeoThaum.onInitialize, right after TVBlocks. */
	public static void registerBlocks() {
		SIPHON.getClass();
	}

	private ExperienceSiphon() {
	}
}
