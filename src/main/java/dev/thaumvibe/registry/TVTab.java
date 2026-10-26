package dev.thaumvibe.registry;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.blocks.CrystalClusterBlock;
import dev.thaumvibe.blocks.TallowCandleBlock;
import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.items.EssentiaPhialItem;
import dev.thaumvibe.items.EtherealEssenceItem;
import dev.thaumvibe.items.WandItem;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * CreativeTabThaumcraft, split five ways: essentia; tools and utilities; natural blocks; building and functional blocks; and everything else.
 * Items keep their registration (ConfigItems) order within each tab. Fabric lays mod tabs out in id order, top row first, so the ids' letter
 * prefixes set the order: tools, building, natural, essentia, other (and the dev tools after them, on the bottom row).
 */
public final class TVTab {
	public enum Category { ESSENTIA, TOOLS, NATURAL, BUILDING, OTHER }

	private static final Set<Item> ESSENTIA = Set.of(TVItems.GLASS_PHIAL, TVItems.PHIAL_OF_ESSENTIA, TVItems.ETHEREAL_ESSENCE, TVItems.MANA_BEAN, TVItems.AIR_SHARD,
		TVItems.FIRE_SHARD, TVItems.WATER_SHARD, TVItems.EARTH_SHARD, TVItems.ORDER_SHARD, TVItems.ENTROPY_SHARD, TVItems.BALANCED_SHARD);
	private static final Set<Item> TOOLS = Set.of(TVItems.THAUMONOMICON, TVItems.WAND, TVItems.IRON_CAP, TVItems.GOLD_CAP, TVItems.COPPER_CAP,
		TVItems.THAUMIUM_CAP_INERT, TVItems.THAUMIUM_CAP, TVItems.GREATWOOD_ROD, TVItems.SILVERWOOD_ROD,
		TVItems.SILVER_CAP, TVItems.SILVER_CAP_INERT, TVItems.VOID_CAP, TVItems.VOID_CAP_INERT, TVItems.OBSIDIAN_ROD, TVItems.ICY_ROD, TVItems.QUARTZ_ROD, TVItems.REED_ROD,
		TVItems.BLAZING_ROD, TVItems.BONE_ROD, TVItems.GREATWOOD_STAFF_CORE, TVItems.OBSIDIAN_STAFF_CORE, TVItems.SILVERWOOD_STAFF_CORE, TVItems.ICY_STAFF_CORE, TVItems.QUARTZ_STAFF_CORE, TVItems.REED_STAFF_CORE, TVItems.BLAZING_STAFF_CORE, TVItems.BONE_STAFF_CORE, TVItems.PRIMAL_STAFF_CORE,
		TVItems.FOCUS_POUCH, TVItems.FOCUS_FIRE, TVItems.FOCUS_SHOCK, TVItems.FOCUS_FROST, TVItems.FOCUS_TRADE, TVItems.FOCUS_EXCAVATION,
		TVItems.FOCUS_PORTABLE_HOLE, TVItems.FOCUS_HELLBAT, TVItems.FOCUS_PECH, TVItems.FOCUS_WARDING, TVItems.FOCUS_PRIMAL, TVItems.THAUMOMETER,
		TVItems.GOGGLES_OF_REVEALING, TVItems.SCRIBING_TOOLS, TVItems.RESEARCH_NOTES, TVItems.MUNDANE_AMULET, TVItems.MUNDANE_RING,
		TVItems.MUNDANE_BELT, TVItems.RING_OF_PROTECTION, TVItems.RING_OF_RUNIC_SHIELDING, TVItems.CHARGED_RING_OF_SHIELDING,
		TVItems.REVITALIZING_RING_OF_SHIELDING, TVItems.AMULET_OF_RUNIC_SHIELDING, TVItems.AMULET_OF_EMERGENCY_SHIELDING,
		TVItems.GIRDLE_OF_RUNIC_SHIELDING, TVItems.KINETIC_GIRDLE_OF_SHIELDING, TVItems.VIS_STONE, TVItems.AMULET_OF_VIS_STORAGE,
		TVItems.THAUMOSTATIC_GIRDLE, TVItems.RESONATOR, TVItems.SANITY_CHECKER, TVItems.BOTTLED_TAINT,
		TVItems.IRON_ARCANE_KEY, TVItems.GOLD_ARCANE_KEY);
	private static final Set<Block> NATURAL = Set.of(TVBlocks.CINNABAR_ORE, TVBlocks.DEEPSLATE_CINNABAR_ORE, TVBlocks.AMBER_BEARING_STONE,
		TVBlocks.DEEPSLATE_AMBER_BEARING_STONE, TVBlocks.AIR_INFUSED_STONE, TVBlocks.FIRE_INFUSED_STONE, TVBlocks.WATER_INFUSED_STONE,
		TVBlocks.EARTH_INFUSED_STONE, TVBlocks.ORDER_INFUSED_STONE, TVBlocks.ENTROPY_INFUSED_STONE, TVBlocks.DEEPSLATE_AIR_INFUSED_STONE,
		TVBlocks.DEEPSLATE_FIRE_INFUSED_STONE, TVBlocks.DEEPSLATE_WATER_INFUSED_STONE, TVBlocks.DEEPSLATE_EARTH_INFUSED_STONE,
		TVBlocks.DEEPSLATE_ORDER_INFUSED_STONE, TVBlocks.DEEPSLATE_ENTROPY_INFUSED_STONE, TVBlocks.GREATWOOD_LOG, TVBlocks.SILVERWOOD_LOG,
		TVBlocks.GREATWOOD_LEAVES, TVBlocks.SILVERWOOD_LEAVES, TVBlocks.GREATWOOD_SAPLING, TVBlocks.SILVERWOOD_SAPLING, TVBlocks.SHIMMERLEAF,
		TVBlocks.CINDERPEARL, TVBlocks.VISHROOM, TVBlocks.CRUSTED_TAINT, TVBlocks.TAINTED_SOIL, TVBlocks.FLESH_BLOCK, TVBlocks.TAINT_FIBRES);
	private static final Set<Block> BUILDING = Set.of(TVBlocks.GREATWOOD_PLANKS, TVBlocks.SILVERWOOD_PLANKS, TVBlocks.GREATWOOD_STAIRS,
		TVBlocks.SILVERWOOD_STAIRS, TVBlocks.GREATWOOD_SLAB, TVBlocks.SILVERWOOD_SLAB, TVBlocks.ARCANE_STONE_BLOCK, TVBlocks.ARCANE_STONE_BRICKS,
		TVBlocks.ARCANE_STONE_STAIRS, TVBlocks.ARCANE_STONE_SLAB, TVBlocks.AMBER_BLOCK, TVBlocks.AMBER_BRICKS, TVBlocks.THAUMIUM_BLOCK,
		TVBlocks.TALLOW_BLOCK, TVBlocks.TABLE, TVBlocks.ARCANE_WORKTABLE, TVBlocks.RESEARCH_TABLE, TVBlocks.CRUCIBLE, TVBlocks.WARDED_JAR,
		TVBlocks.VOID_JAR, TVBlocks.BRAIN_IN_A_JAR, TVBlocks.NODE_IN_A_JAR, TVBlocks.ESSENTIA_TUBE, TVBlocks.ESSENTIA_VALVE, TVBlocks.FILTERED_ESSENTIA_TUBE,
		TVBlocks.RESTRICTED_ESSENTIA_TUBE, TVBlocks.DIRECTIONAL_ESSENTIA_TUBE, TVBlocks.ESSENTIA_BUFFER, TVBlocks.ARCANE_PEDESTAL,
		TVBlocks.RUNIC_MATRIX, TVBlocks.ARCANE_BELLOWS, TVBlocks.ALCHEMICAL_FURNACE, TVBlocks.ARCANE_ALEMBIC,
		TVBlocks.WAND_RECHARGE_PEDESTAL, TVBlocks.COMPOUND_RECHARGE_FOCUS, TVBlocks.NODE_STABILIZER, TVBlocks.ADVANCED_NODE_STABILIZER,
		TVBlocks.NODE_TRANSDUCER, TVBlocks.FOCAL_MANIPULATOR, TVBlocks.VIS_RELAY, TVBlocks.VIS_CHARGE_RELAY, TVBlocks.ARCANE_LAMP, TVBlocks.LAMP_OF_GROWTH, TVBlocks.LAMP_OF_FERTILITY,
		TVBlocks.ARCANE_DOOR, TVBlocks.WARDED_GLASS, TVBlocks.PAVING_STONE_OF_WARDING, TVBlocks.ARCANE_PRESSURE_PLATE, TVBlocks.ARCANE_EAR, TVBlocks.ARCANE_LEVITATOR);

	public static Category categoryOf(Item item) {
		if (ESSENTIA.contains(item)) {
			return Category.ESSENTIA;
		}
		if (TOOLS.contains(item) || TVEntities.EGGS.stream().anyMatch(e -> e.item() == item)
			|| item.components().has(DataComponents.TOOL) || item.components().has(DataComponents.EQUIPPABLE)
			|| item.components().has(DataComponents.WEAPON)) {
			return Category.TOOLS;
		}
		if (item instanceof BlockItem bi) {
			Block b = bi.getBlock();
			if (NATURAL.contains(b) || b instanceof CrystalClusterBlock) {
				return Category.NATURAL;
			}
			if (BUILDING.contains(b) || b instanceof TallowCandleBlock) {
				return Category.BUILDING;
			}
		}
		return Category.OTHER;
	}

	private static CreativeModeTab tab(String name, java.util.function.Supplier<ItemStack> icon, Category category) {
		Predicate<Item> in = item -> categoryOf(item) == category;
		return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Thaumvibe.id(name), FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.thaumvibe." + name))
			.icon(icon)
			.displayItems((params, out) -> {
				for (Item item : TVItems.TAB_ORDER) {
					if (!in.test(item)) {
						continue;
					}
					if (item == TVItems.WAND) {
						out.accept(WandItem.createFull(TVItems.WAND, "iron", "wood"));
						out.accept(WandItem.createFull(TVItems.WAND, "gold", "greatwood"));
						out.accept(WandItem.createFull(TVItems.WAND, "thaumium", "silverwood"));
						ItemStack sceptre = WandItem.create(TVItems.WAND, "thaumium", "silverwood");
						sceptre.set(TVComponents.SCEPTRE, true);
						for (Aspect a : Aspect.getPrimalAspects()) {
							WandItem.addVis(sceptre, a, WandItem.getMaxVis(sceptre), true);
						}
						out.accept(sceptre);
					} else if (item == TVItems.PHIAL_OF_ESSENTIA) {
						// ItemEssence.getSubItems: a full phial of every aspect
						for (Aspect a : Aspect.aspects.values()) {
							out.accept(EssentiaPhialItem.of(a));
						}
					} else if (item == TVItems.ETHEREAL_ESSENCE) {
						for (Aspect a : Aspect.aspects.values()) {
							out.accept(EtherealEssenceItem.of(a));
						}
					} else if (item == TVItems.NODE_IN_A_JAR) {
						// not in TC4's tabs (only a wand makes one); one jarred node of each type, so creative players can place them
						for (dev.thaumvibe.api.nodes.NodeType t : dev.thaumvibe.api.nodes.NodeType.values()) {
							ItemStack jar = new ItemStack(item);
							dev.thaumvibe.api.aspects.AspectList al = new dev.thaumvibe.api.aspects.AspectList();
							for (Aspect a : Aspect.getPrimalAspects()) {
								al.add(a, 20);
							}
							jar.set(TVComponents.ASPECTS, al);
							jar.set(TVComponents.NODE_TYPE, t.ordinal());
							out.accept(jar);
						}
					} else {
						out.accept(item);
					}
				}
			})
			.build());
	}

	public static final CreativeModeTab ESSENTIA_TAB = tab("d_essentia", () -> EssentiaPhialItem.of(Aspect.MAGIC), Category.ESSENTIA);
	public static final CreativeModeTab TOOLS_TAB = tab("a_tools_utilities", () -> new ItemStack(TVItems.THAUMONOMICON), Category.TOOLS);
	public static final CreativeModeTab NATURAL_TAB = tab("c_natural", () -> new ItemStack(TVBlocks.GREATWOOD_SAPLING), Category.NATURAL);
	public static final CreativeModeTab BUILDING_TAB = tab("b_building", () -> new ItemStack(TVBlocks.ARCANE_STONE_BRICKS), Category.BUILDING);
	public static final CreativeModeTab OTHER_TAB = tab("e_other", () -> new ItemStack(TVItems.NITOR), Category.OTHER);
	public static final List<CreativeModeTab> TABS = List.of(ESSENTIA_TAB, TOOLS_TAB, NATURAL_TAB, BUILDING_TAB, OTHER_TAB);

	public static void init() {
	}
}
