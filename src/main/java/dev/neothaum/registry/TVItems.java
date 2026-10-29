package dev.neothaum.registry;

import dev.neothaum.items.equipment.BoneBowItem;
import dev.neothaum.items.equipment.BottledTaintItem;
import dev.neothaum.items.equipment.PrimalCrusherItem;
import dev.neothaum.items.equipment.ResonatorItem;
import dev.neothaum.items.equipment.TravellerBootsItem;
import dev.neothaum.items.equipment.VoidGear;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import dev.neothaum.NeoThaum;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import java.util.Map;
import java.util.EnumMap;
import dev.neothaum.golem.GolemBellItem;
import dev.neothaum.golem.GolemCoreItem;
import dev.neothaum.golem.GolemCoreType;
import dev.neothaum.golem.GolemDecorationItem;
import dev.neothaum.golem.GolemDecorationType;
import dev.neothaum.golem.GolemPlacerItem;
import dev.neothaum.golem.GolemType;
import dev.neothaum.golem.GolemUpgradeItem;
import dev.neothaum.golem.GolemUpgradeType;
import dev.neothaum.items.AlumentumItem;
import dev.neothaum.items.ArcaneKeyItem;
import dev.neothaum.items.BathSaltsItem;
import dev.neothaum.items.FluidBucketItem;
import dev.neothaum.items.KnowledgeFragmentItem;
import dev.neothaum.items.LootBagItem;
import dev.neothaum.items.RobeArmorItem;
import dev.neothaum.items.SanitizingSoapItem;
import dev.neothaum.items.SinisterLodestoneItem;
import dev.neothaum.items.equipment.FortressArmorItem;
import dev.neothaum.baubles.BaubleType;
import dev.neothaum.items.BaubleBlankItem;
import dev.neothaum.items.EssentiaPhialItem;
import dev.neothaum.items.ResourceItem;
import dev.neothaum.items.ThaumiumArmorItem;
import dev.neothaum.items.baubles.HoverGirdleItem;
import dev.neothaum.items.baubles.RunicBaubleItem;
import dev.neothaum.items.baubles.VisAmuletItem;
import dev.neothaum.items.equipment.ElementalAxeItem;
import dev.neothaum.items.equipment.ElementalHoeItem;
import dev.neothaum.items.equipment.ElementalPickaxeItem;
import dev.neothaum.items.equipment.ElementalShovelItem;
import dev.neothaum.items.equipment.ElementalSwordItem;
import dev.neothaum.items.GogglesItem;
import dev.neothaum.items.NitorItem;
import dev.neothaum.items.ResearchNotesItem;
import dev.neothaum.items.ScribingToolsItem;
import dev.neothaum.items.ThaumometerItem;
import dev.neothaum.items.ThaumonomiconItem;
import dev.neothaum.items.WandItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

public final class TVItems {
	/** Creative tab order (ConfigItems registration order). */
	public static final List<Item> TAB_ORDER = new ArrayList<>();

	public static final Item THAUMONOMICON = register("thaumonomicon", ThaumonomiconItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item WAND = register("wand", WandItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item IRON_CAP = simple("iron_cap");
	public static final Item GOLD_CAP = simple("gold_cap");
	public static final Item COPPER_CAP = simple("copper_cap");
	public static final Item THAUMIUM_CAP_INERT = simple("thaumium_cap_inert");
	public static final Item THAUMIUM_CAP = register("thaumium_cap", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item GREATWOOD_ROD = simple("greatwood_rod");
	public static final Item SILVERWOOD_ROD = register("silverwood_rod", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
	// ItemWandCap metas 4, 5, 7, 8 and ItemWandRod metas 1-7, 50-57 and 100
	public static final Item SILVER_CAP = rod("silver_cap", Rarity.UNCOMMON);
	public static final Item SILVER_CAP_INERT = simple("silver_cap_inert");
	public static final Item VOID_CAP = rod("void_cap", Rarity.RARE);
	public static final Item VOID_CAP_INERT = simple("void_cap_inert");
	public static final Item OBSIDIAN_ROD = rod("obsidian_rod", Rarity.UNCOMMON);
	public static final Item ICY_ROD = rod("icy_rod", Rarity.UNCOMMON);
	public static final Item QUARTZ_ROD = rod("quartz_rod", Rarity.UNCOMMON);
	public static final Item REED_ROD = rod("reed_rod", Rarity.UNCOMMON);
	public static final Item BLAZING_ROD = rod("blazing_rod", Rarity.UNCOMMON);
	public static final Item BONE_ROD = rod("bone_rod", Rarity.UNCOMMON);
	public static final Item GREATWOOD_STAFF_CORE = rod("greatwood_staff_core", Rarity.UNCOMMON);
	public static final Item OBSIDIAN_STAFF_CORE = rod("obsidian_staff_core", Rarity.RARE);
	public static final Item SILVERWOOD_STAFF_CORE = rod("silverwood_staff_core", Rarity.RARE);
	public static final Item ICY_STAFF_CORE = rod("icy_staff_core", Rarity.RARE);
	public static final Item QUARTZ_STAFF_CORE = rod("quartz_staff_core", Rarity.RARE);
	public static final Item REED_STAFF_CORE = rod("reed_staff_core", Rarity.RARE);
	public static final Item BLAZING_STAFF_CORE = rod("blazing_staff_core", Rarity.RARE);
	public static final Item BONE_STAFF_CORE = rod("bone_staff_core", Rarity.RARE);
	public static final Item PRIMAL_STAFF_CORE = rod("primal_staff_core", Rarity.EPIC);
	// ItemFocus*: the ten wand foci and the focus pouch
	public static final Item NODE_IN_A_JAR = register("node_in_a_jar", p -> new dev.neothaum.items.JarNodeItem(TVBlocks.NODE_IN_A_JAR, p),
		new Item.Properties().stacksTo(1).useBlockDescriptionPrefix());
	public static final Item FOCUS_POUCH = register("focus_pouch", dev.neothaum.items.FocusPouchItem::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final Item FOCUS_FIRE = register("focus_fire", dev.neothaum.items.foci.FireFocusItem::new, new Item.Properties());
	public static final Item FOCUS_SHOCK = register("focus_shock", dev.neothaum.items.foci.ShockFocusItem::new, new Item.Properties());
	public static final Item FOCUS_FROST = register("focus_frost", dev.neothaum.items.foci.FrostFocusItem::new, new Item.Properties());
	public static final Item FOCUS_TRADE = register("focus_trade", dev.neothaum.items.foci.TradeFocusItem::new, new Item.Properties());
	public static final Item FOCUS_EXCAVATION = register("focus_excavation", dev.neothaum.items.foci.ExcavationFocusItem::new, new Item.Properties());
	public static final Item FOCUS_PORTABLE_HOLE = register("focus_portable_hole", dev.neothaum.items.foci.PortableHoleFocusItem::new,
		new Item.Properties());
	public static final Item FOCUS_HELLBAT = register("focus_hellbat", dev.neothaum.items.foci.HellbatFocusItem::new, new Item.Properties());
	public static final Item FOCUS_PECH = register("focus_pech", dev.neothaum.items.foci.PechFocusItem::new, new Item.Properties());
	public static final Item FOCUS_WARDING = register("focus_warding", dev.neothaum.items.foci.WardingFocusItem::new, new Item.Properties());
	public static final Item FOCUS_PRIMAL = register("focus_primal", dev.neothaum.items.foci.PrimalFocusItem::new, new Item.Properties());
	public static final Item THAUMOMETER = register("thaumometer", ThaumometerItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item GOGGLES_OF_REVEALING = register("goggles_of_revealing", GogglesItem::new, GogglesItem.properties());
	public static final Item SCRIBING_TOOLS = register("scribing_tools", ScribingToolsItem::new, new Item.Properties().durability(100));
	/** ItemEssence meta 0 and 1: the empty phial and the phial of essentia (its aspect in the ASPECTS component). */
	public static final Item GLASS_PHIAL = register("glass_phial", p -> new EssentiaPhialItem(false, p), new Item.Properties());
	public static final Item PHIAL_OF_ESSENTIA = register("phial_of_essentia", p -> new EssentiaPhialItem(true, p), new Item.Properties().stacksTo(64));
	/** ItemWispEssence: its aspect in the ASPECTS component. */
	public static final Item ETHEREAL_ESSENCE = register("ethereal_essence", dev.neothaum.items.EtherealEssenceItem::new, new Item.Properties());
	/** ItemCrystalEssence: a solid crystal of essentia with its aspect in the ASPECTS component. */
	public static final Item CRYSTAL_ESSENCE = register("crystal_essence", dev.neothaum.items.CrystalEssenceItem::new, new Item.Properties());
	/** ItemManaBean: its aspect in the ASPECTS component; ItemFood(1, 0.5), always edible, eaten in half a second. */
	public static final Item MANA_BEAN = register("mana_bean", dev.neothaum.items.ManaBeanItem::new, new Item.Properties().food(
		new FoodProperties.Builder().nutrition(1).saturationModifier(0.5F).alwaysEdible().build(), Consumables.defaultFood().consumeSeconds(0.5F).build()));
	public static final Item RESEARCH_NOTES = register("research_notes", ResearchNotesItem::new, new Item.Properties().stacksTo(1));

	// ItemResource
	/** EventHandlerWorld.getBurnTime: alumentum burns for 6400 ticks (and the alchemical furnace runs twice as fast on it). Thrown with velocity 0.75. */
	public static final Item ALUMENTUM = register("alumentum", AlumentumItem::new, new Item.Properties().component(DataComponents.COOKING_FUEL, fuel(6400)));
	public static final Item NITOR = register("nitor", NitorItem::new, new Item.Properties());
	public static final Item THAUMIUM_INGOT = simple("thaumium_ingot");
	public static final Item QUICKSILVER = simple("quicksilver");
	public static final Item MAGIC_TALLOW = simple("magic_tallow");
	public static final Item AMBER = simple("amber");
	public static final Item ENCHANTED_FABRIC = simple("enchanted_fabric");
	public static final Item KNOWLEDGE_FRAGMENT = register("knowledge_fragment", KnowledgeFragmentItem::new, new Item.Properties());
	public static final Item SALIS_MUNDUS = simple("salis_mundus");
	/** ItemZombieBrain: ItemFood(4, 0.2F, wolf food), Hunger for 30s four times in five. */
	public static final Item ZOMBIE_BRAIN = register("zombie_brain", dev.neothaum.items.ZombieBrainItem::new, new Item.Properties().food(
		new FoodProperties.Builder().nutrition(4).saturationModifier(0.2F).build(),
		Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.8F)).build()));
	public static final Item VIS_FILTER = simple("vis_filter");
	public static final Item MIRRORED_GLASS = simple("mirrored_glass");
	public static final Item TAINTED_GOO = register("tainted_goo", p -> new ResourceItem(ResourceItem.Kind.TAINT, p), new Item.Properties());
	public static final Item TAINT_TENDRIL = register("taint_tendril", p -> new ResourceItem(ResourceItem.Kind.TAINT, p), new Item.Properties());
	public static final Item PRIMAL_CHARM = register("primal_charm", p -> new ResourceItem(ResourceItem.Kind.PRIMAL_CHARM, p),
		new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item VOID_METAL_INGOT = simple("void_metal_ingot");
	public static final Item VOID_SEED = simple("void_seed");
	public static final Item PRIMORDIAL_PEARL = register("primordial_pearl", dev.neothaum.items.eldritch.PrimordialPearlItem::new, new Item.Properties());
	public static final Item CRIMSON_RITES = register("crimson_rites", dev.neothaum.items.eldritch.CrimsonRitesItem::new, new Item.Properties());
	public static final Item ELDRITCH_EYE = register("eldritch_eye", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item RUNED_TABLET = register("runed_tablet", Item::new, new Item.Properties().rarity(Rarity.RARE));
	public static final Item OBELISK_PLACER = register("obelisk_placer", dev.neothaum.items.eldritch.ObeliskPlacerItem::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item BANNER = register("banner", props -> new dev.neothaum.items.BannerItem(props, false), new Item.Properties().stacksTo(16));
	public static final Item CULTIST_BANNER = register("cultist_banner", props -> new dev.neothaum.items.BannerItem(props, true), new Item.Properties().stacksTo(16));
	public static final Item GOLD_COIN = simple("gold_coin");
	/** ItemResource 13; once a phial has marked it, it carries the aspect in ASPECT_FILTER. */
	public static final Item JAR_LABEL = simple("jar_label");

	// ItemShard
	public static final Item AIR_SHARD = simple("air_shard");
	public static final Item FIRE_SHARD = simple("fire_shard");
	public static final Item WATER_SHARD = simple("water_shard");
	public static final Item EARTH_SHARD = simple("earth_shard");
	public static final Item ORDER_SHARD = simple("order_shard");
	public static final Item ENTROPY_SHARD = simple("entropy_shard");
	public static final Item BALANCED_SHARD = simple("balanced_shard");

	// ItemNugget
	public static final Item QUICKSILVER_DROP = simple("quicksilver_drop");
	/** NeoThaum: smelted from vanilla cinnabar, nine to a quicksilver drop. */
	public static final Item QUICKSILVER_BEAD = simple("quicksilver_bead");
	/** NeoThaum: what cinnabar ore drops, as vanilla ores drop raw metal. */
	public static final Item RAW_CINNABAR = simple("raw_cinnabar");
	public static final Item THAUMIUM_NUGGET = simple("thaumium_nugget");
	public static final Item VOID_METAL_NUGGET = simple("void_metal_nugget");
	/** Merged into vanilla Items.COPPER_NUGGET (Minecraft 26.3 provides copper nuggets natively). */
	public static final Item COPPER_NUGGET = Items.COPPER_NUGGET;
	public static final Item NATIVE_IRON_CLUSTER = simple("native_iron_cluster");
	public static final Item NATIVE_COPPER_CLUSTER = simple("native_copper_cluster");
	/** TC4's tin, silver and lead clusters and nuggets (itemNugget 18-20 and 2-4): only of use, and only listed, when a mod adds the ingot. */
	public static final Item NATIVE_TIN_CLUSTER = simple("native_tin_cluster");
	public static final Item NATIVE_SILVER_CLUSTER = simple("native_silver_cluster");
	public static final Item NATIVE_LEAD_CLUSTER = simple("native_lead_cluster");
	public static final Item TIN_NUGGET = simple("tin_nugget");
	public static final Item SILVER_NUGGET = simple("silver_nugget");
	public static final Item LEAD_NUGGET = simple("lead_nugget");

	/** Config.foundTinIngot and the rest: whether a loaded mod puts anything in c:ingots/<metal>. */
	public static boolean hasIngot(String metal) {
		var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
			net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/" + metal));
		return BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator().hasNext();
	}

	/** The metal of one of the cross-mod items above, or null for any other item. */
	public static String crossModMetal(Item item) {
		if (item == NATIVE_TIN_CLUSTER || item == TIN_NUGGET) {
			return "tin";
		}
		if (item == NATIVE_SILVER_CLUSTER || item == SILVER_NUGGET) {
			return "silver";
		}
		if (item == NATIVE_LEAD_CLUSTER || item == LEAD_NUGGET) {
			return "lead";
		}
		return null;
	}
	public static final Item NATIVE_GOLD_CLUSTER = simple("native_gold_cluster");
	public static final Item NATIVE_CINNABAR_CLUSTER = simple("native_cinnabar_cluster");

	// ItemBaubleBlanks
	public static final Item MUNDANE_AMULET = register("mundane_amulet", p -> new BaubleBlankItem(BaubleType.AMULET, p), new Item.Properties().stacksTo(1));
	public static final Item MUNDANE_RING = register("mundane_ring", p -> new BaubleBlankItem(BaubleType.RING, p), new Item.Properties().stacksTo(1));
	/** ItemBaubleBlanks 3-8: the Apprentice's Rings, one per primal aspect. */
	public static final Item APPRENTICE_RING_AER = register("apprentice_ring_aer", p -> new dev.neothaum.items.ApprenticeRingItem(dev.neothaum.api.aspects.Aspect.AIR, p), new Item.Properties().stacksTo(1));
	public static final Item APPRENTICE_RING_TERRA = register("apprentice_ring_terra", p -> new dev.neothaum.items.ApprenticeRingItem(dev.neothaum.api.aspects.Aspect.EARTH, p), new Item.Properties().stacksTo(1));
	public static final Item APPRENTICE_RING_IGNIS = register("apprentice_ring_ignis", p -> new dev.neothaum.items.ApprenticeRingItem(dev.neothaum.api.aspects.Aspect.FIRE, p), new Item.Properties().stacksTo(1));
	public static final Item APPRENTICE_RING_AQUA = register("apprentice_ring_aqua", p -> new dev.neothaum.items.ApprenticeRingItem(dev.neothaum.api.aspects.Aspect.WATER, p), new Item.Properties().stacksTo(1));
	public static final Item APPRENTICE_RING_ORDO = register("apprentice_ring_ordo", p -> new dev.neothaum.items.ApprenticeRingItem(dev.neothaum.api.aspects.Aspect.ORDER, p), new Item.Properties().stacksTo(1));
	public static final Item APPRENTICE_RING_PERDITIO = register("apprentice_ring_perditio", p -> new dev.neothaum.items.ApprenticeRingItem(dev.neothaum.api.aspects.Aspect.ENTROPY, p), new Item.Properties().stacksTo(1));
	public static final Item MUNDANE_BELT = register("mundane_belt", p -> new BaubleBlankItem(BaubleType.BELT, p), new Item.Properties().stacksTo(1));

	// ItemRingRunic, ItemAmuletRunic, ItemGirdleRunic, ItemAmuletVis, ItemGirdleHover
	public static final Item RING_OF_PROTECTION = runic("ring_of_protection", BaubleType.RING, 1, RunicBaubleItem.Kind.NONE, Rarity.UNCOMMON);
	public static final Item RING_OF_RUNIC_SHIELDING = runic("ring_of_runic_shielding", BaubleType.RING, 5, RunicBaubleItem.Kind.NONE, Rarity.RARE);
	public static final Item CHARGED_RING_OF_SHIELDING = runic("charged_ring_of_shielding", BaubleType.RING, 4, RunicBaubleItem.Kind.CHARGED, Rarity.RARE);
	public static final Item REVITALIZING_RING_OF_SHIELDING = runic("revitalizing_ring_of_shielding", BaubleType.RING, 4, RunicBaubleItem.Kind.HEALING,
		Rarity.RARE);
	public static final Item AMULET_OF_RUNIC_SHIELDING = runic("amulet_of_runic_shielding", BaubleType.AMULET, 8, RunicBaubleItem.Kind.NONE, Rarity.RARE);
	public static final Item AMULET_OF_EMERGENCY_SHIELDING = runic("amulet_of_emergency_shielding", BaubleType.AMULET, 7, RunicBaubleItem.Kind.EMERGENCY,
		Rarity.RARE);
	public static final Item GIRDLE_OF_RUNIC_SHIELDING = runic("girdle_of_runic_shielding", BaubleType.BELT, 10, RunicBaubleItem.Kind.NONE, Rarity.RARE);
	public static final Item KINETIC_GIRDLE_OF_SHIELDING = runic("kinetic_girdle_of_shielding", BaubleType.BELT, 9, RunicBaubleItem.Kind.KINETIC,
		Rarity.RARE);
	public static final Item VIS_STONE = register("vis_stone", p -> new VisAmuletItem(false, p), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item AMULET_OF_VIS_STORAGE = register("amulet_of_vis_storage", p -> new VisAmuletItem(true, p),
		new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final Item THAUMOSTATIC_GIRDLE = register("thaumostatic_girdle", HoverGirdleItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

	// ThaumcraftApi.toolMatThaumium: harvest level 3 (diamond tier), 400 uses, speed 7, +2 damage (as iron), enchantability 22
	private static final TagKey<Item> THAUMIUM_INGOTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/thaumium"));
	public static final ToolMaterial THAUMIUM_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 400, 7.0F, 2.0F, 22, THAUMIUM_INGOTS);
	public static final Item THAUMIUM_SWORD = register("thaumium_sword", Item::new, new Item.Properties().sword(THAUMIUM_TOOLS, 3.0F, -2.4F));
	public static final Item THAUMIUM_SHOVEL = register("thaumium_shovel", Item::new, new Item.Properties().shovel(THAUMIUM_TOOLS, 1.5F, -3.0F));
	public static final Item THAUMIUM_PICKAXE = register("thaumium_pickaxe", Item::new, new Item.Properties().pickaxe(THAUMIUM_TOOLS, 1.0F, -2.8F));
	public static final Item THAUMIUM_AXE = register("thaumium_axe", Item::new, new Item.Properties().axe(THAUMIUM_TOOLS, 6.0F, -3.1F));
	public static final Item THAUMIUM_HOE = register("thaumium_hoe", Item::new, new Item.Properties().hoe(THAUMIUM_TOOLS, -2.0F, -1.0F));

	// ThaumcraftApi.toolMatElemental: harvest level 3, 1500 uses, speed 10, +3 damage, enchantability 18 (the hoe overrides it to 5)
	public static final ToolMaterial ELEMENTAL_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 10.0F, 3.0F, 18, THAUMIUM_INGOTS);
	public static final ToolMaterial ELEMENTAL_HOE = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 10.0F, 3.0F, 5, THAUMIUM_INGOTS);
	public static final Item SHOVEL_OF_THE_EARTHMOVER = register("shovel_of_the_earthmover", ElementalShovelItem::new,
		new Item.Properties().shovel(ELEMENTAL_TOOLS, 1.5F, -3.0F).rarity(Rarity.RARE));
	public static final Item PICKAXE_OF_THE_CORE = register("pickaxe_of_the_core", ElementalPickaxeItem::new,
		new Item.Properties().pickaxe(ELEMENTAL_TOOLS, 1.0F, -2.8F).rarity(Rarity.RARE));
	public static final Item SWORD_OF_THE_ZEPHYR = register("sword_of_the_zephyr", ElementalSwordItem::new,
		new Item.Properties().sword(ELEMENTAL_TOOLS, 3.0F, -2.4F).rarity(Rarity.RARE));
	public static final Item AXE_OF_THE_STREAM = register("axe_of_the_stream", ElementalAxeItem::new,
		new Item.Properties().axe(ELEMENTAL_TOOLS, 6.0F, -3.1F).rarity(Rarity.RARE));
	public static final Item HOE_OF_GROWTH = register("hoe_of_growth", ElementalHoeItem::new,
		new Item.Properties().hoe(ELEMENTAL_HOE, -2.0F, -1.0F).rarity(Rarity.RARE));

	// ThaumcraftApi.armorMatThaumium (25, {2,6,5,2}, 25), and armorMatSpecial (25, {1,3,2,1}, 25) for the robes
	public static final ArmorMaterial THAUMIUM_ARMOR = armor(25, 2, 6, 5, 2, SoundEvents.ARMOR_EQUIP_IRON, THAUMIUM_INGOTS, "thaumium");
	public static final ArmorMaterial ROBE_ARMOR = armor(25, 1, 3, 2, 1, SoundEvents.ARMOR_EQUIP_LEATHER,
		TagKey.create(Registries.ITEM, NeoThaum.id("enchanted_fabrics")), "robes");
	public static final Item THAUMIUM_HELMET = register("thaumium_helmet", ThaumiumArmorItem::new, new Item.Properties().humanoidArmor(THAUMIUM_ARMOR, ArmorType.HELMET));
	public static final Item THAUMIUM_CHESTPLATE = register("thaumium_chestplate", ThaumiumArmorItem::new,
		new Item.Properties().humanoidArmor(THAUMIUM_ARMOR, ArmorType.CHESTPLATE));
	public static final Item THAUMIUM_LEGGINGS = register("thaumium_leggings", ThaumiumArmorItem::new,
		new Item.Properties().humanoidArmor(THAUMIUM_ARMOR, ArmorType.LEGGINGS));
	public static final Item THAUMIUM_BOOTS = register("thaumium_boots", ThaumiumArmorItem::new, new Item.Properties().humanoidArmor(THAUMIUM_ARMOR, ArmorType.BOOTS));
	public static final Item THAUMATURGES_ROBE = robe("thaumaturges_robe", EquipmentSlot.CHEST, ArmorType.CHESTPLATE);
	public static final Item THAUMATURGES_LEGGINGS = robe("thaumaturges_leggings", EquipmentSlot.LEGS, ArmorType.LEGGINGS);
	public static final Item THAUMATURGES_BOOTS = robe("thaumaturges_boots", EquipmentSlot.FEET, ArmorType.BOOTS);

	public static final ArmorMaterial TRAVELLER_ARMOR = armor(25, 1, 3, 2, 1, SoundEvents.ARMOR_EQUIP_LEATHER,
		TagKey.create(Registries.ITEM, NeoThaum.id("enchanted_fabrics")), "bootstraveler");
	public static final ArmorMaterial HARNESS_ARMOR = armor(26, 2, 6, 5, 2, SoundEvents.ARMOR_EQUIP_IRON,
		net.minecraft.tags.ItemTags.REPAIRS_GOLD_ARMOR, "thaumostatic_harness");
	public static final Item THAUMOSTATIC_HARNESS = register("thaumostatic_harness", dev.neothaum.items.equipment.ThaumostaticHarnessItem::new,
		new Item.Properties().humanoidArmor(HARNESS_ARMOR, ArmorType.CHESTPLATE).durability(400).rarity(Rarity.EPIC));
	public static final Item BOOTS_OF_THE_TRAVELLER = register("boots_of_the_traveller", TravellerBootsItem::new,
		new Item.Properties().humanoidArmor(TRAVELLER_ARMOR, ArmorType.BOOTS).durability(350).rarity(Rarity.RARE)
			.attributes(ItemAttributeModifiers.builder()
				.add(Attributes.ARMOR, new AttributeModifier(NeoThaum.id("traveller_armor"), 1.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET)
				.add(Attributes.STEP_HEIGHT, new AttributeModifier(NeoThaum.id("traveller_step"), 0.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET)
				.add(Attributes.JUMP_STRENGTH, new AttributeModifier(NeoThaum.id("traveller_jump"), 0.275, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET)
				.build()));

	public static final Item BOW_OF_BONE = register("bow_of_bone", BoneBowItem::new, new Item.Properties().durability(512));
	public static final Item RESONATOR = register("essentia_resonator", ResonatorItem::new, new Item.Properties().stacksTo(1));
	public static final Item SANITY_CHECKER = register("sanity_checker", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item BOTTLED_TAINT = register("bottled_taint", BottledTaintItem::new, new Item.Properties().stacksTo(16));
	public static final Item SINISTER_LODESTONE = register("sinister_lodestone", SinisterLodestoneItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final Item LOOT_BAG_COMMON = register("loot_bag_common", p -> new LootBagItem(0, p), new Item.Properties().stacksTo(16).rarity(Rarity.COMMON));
	public static final Item LOOT_BAG_UNCOMMON = register("loot_bag_uncommon", p -> new LootBagItem(1, p), new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
	public static final Item LOOT_BAG_RARE = register("loot_bag_rare", p -> new LootBagItem(2, p), new Item.Properties().stacksTo(16).rarity(Rarity.RARE));
	// ItemKey stacks to 64 (its recipes make two); imprinting takes one blank from the stack
	public static final Item IRON_ARCANE_KEY = register("iron_arcane_key", p -> new ArcaneKeyItem(false, p), new Item.Properties());
	public static final Item GOLD_ARCANE_KEY = register("gold_arcane_key", p -> new ArcaneKeyItem(true, p), new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item MAGIC_MIRROR = register("magic_mirror", p -> new dev.neothaum.items.MirrorItem(TVBlocks.MAGIC_MIRROR, false, p), new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item ESSENTIA_MIRROR = register("essentia_mirror", p -> new dev.neothaum.items.MirrorItem(TVBlocks.ESSENTIA_MIRROR, true, p), new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item MAGIC_HAND_MIRROR = register("magic_hand_mirror", dev.neothaum.items.HandMirrorItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

	// Golemancy
	public static final Item GOLEM_BELL = register("golem_bell", GolemBellItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

	public static final Item GOLEM_STRAW = register("golem_straw", p -> new GolemPlacerItem(GolemType.STRAW, p), new Item.Properties().stacksTo(1));
	public static final Item GOLEM_WOOD = register("golem_wood", p -> new GolemPlacerItem(GolemType.WOOD, p), new Item.Properties().stacksTo(1));
	public static final Item GOLEM_TALLOW = register("golem_tallow", p -> new GolemPlacerItem(GolemType.TALLOW, p), new Item.Properties().stacksTo(1));
	public static final Item GOLEM_CLAY = register("golem_clay", p -> new GolemPlacerItem(GolemType.CLAY, p), new Item.Properties().stacksTo(1));
	public static final Item GOLEM_FLESH = register("golem_flesh", p -> new GolemPlacerItem(GolemType.FLESH, p), new Item.Properties().stacksTo(1));
	public static final Item GOLEM_STONE = register("golem_stone", p -> new GolemPlacerItem(GolemType.STONE, p), new Item.Properties().stacksTo(1));
	public static final Item GOLEM_IRON = register("golem_iron", p -> new GolemPlacerItem(GolemType.IRON, p), new Item.Properties().stacksTo(1));
	public static final Item GOLEM_THAUMIUM = register("golem_thaumium", p -> new GolemPlacerItem(GolemType.THAUMIUM, p), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item GOLEM_COPPER = register("golem_copper", p -> new GolemPlacerItem(GolemType.COPPER, p), new Item.Properties().stacksTo(1));

	public static final Item GOLEM_CORE_BLANK = register("golem_core_blank", p -> new GolemCoreItem(GolemCoreType.BLANK, p), new Item.Properties());
	public static final Item GOLEM_CORE_FILL = register("golem_core_fill", p -> new GolemCoreItem(GolemCoreType.FILL, p), new Item.Properties());
	public static final Item GOLEM_CORE_EMPTY = register("golem_core_empty", p -> new GolemCoreItem(GolemCoreType.EMPTY, p), new Item.Properties());
	public static final Item GOLEM_CORE_GATHER = register("golem_core_gather", p -> new GolemCoreItem(GolemCoreType.GATHER, p), new Item.Properties());
	public static final Item GOLEM_CORE_HARVEST = register("golem_core_harvest", p -> new GolemCoreItem(GolemCoreType.HARVEST, p), new Item.Properties());
	public static final Item GOLEM_CORE_GUARD = register("golem_core_guard", p -> new GolemCoreItem(GolemCoreType.GUARD, p), new Item.Properties());
	public static final Item GOLEM_CORE_DECANT = register("golem_core_liquid", p -> new GolemCoreItem(GolemCoreType.DECANT, p), new Item.Properties());
	public static final Item GOLEM_CORE_ALCHEMY = register("golem_core_essentia", p -> new GolemCoreItem(GolemCoreType.ALCHEMY, p), new Item.Properties());
	public static final Item GOLEM_CORE_CHOP = register("golem_core_lumber", p -> new GolemCoreItem(GolemCoreType.CHOP, p), new Item.Properties());
	public static final Item GOLEM_CORE_USE = register("golem_core_use", p -> new GolemCoreItem(GolemCoreType.USE, p), new Item.Properties());
	public static final Item GOLEM_CORE_BUTCHER = register("golem_core_butcher", p -> new GolemCoreItem(GolemCoreType.BUTCHER, p), new Item.Properties());
	public static final Item GOLEM_CORE_SORT = register("golem_core_sorting", p -> new GolemCoreItem(GolemCoreType.SORT, p), new Item.Properties());
	public static final Item GOLEM_CORE_FISH = register("golem_core_fish", p -> new GolemCoreItem(GolemCoreType.FISH, p), new Item.Properties());

	public static final Item GOLEM_UPGRADE_AIR = register("golem_upgrade_air", p -> new GolemUpgradeItem(GolemUpgradeType.AIR, p), new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item GOLEM_UPGRADE_EARTH = register("golem_upgrade_earth", p -> new GolemUpgradeItem(GolemUpgradeType.EARTH, p), new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item GOLEM_UPGRADE_FIRE = register("golem_upgrade_fire", p -> new GolemUpgradeItem(GolemUpgradeType.FIRE, p), new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item GOLEM_UPGRADE_WATER = register("golem_upgrade_water", p -> new GolemUpgradeItem(GolemUpgradeType.WATER, p), new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item GOLEM_UPGRADE_ORDER = register("golem_upgrade_order", p -> new GolemUpgradeItem(GolemUpgradeType.ORDER, p), new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item GOLEM_UPGRADE_ENTROPY = register("golem_upgrade_entropy", p -> new GolemUpgradeItem(GolemUpgradeType.ENTROPY, p), new Item.Properties().rarity(Rarity.UNCOMMON));

	public static final Item GOLEM_DECO_TOPHAT = register("golem_deco_tophat", p -> new GolemDecorationItem(GolemDecorationType.TOP_HAT, p), new Item.Properties());
	public static final Item GOLEM_DECO_GLASSES = register("golem_deco_glasses", p -> new GolemDecorationItem(GolemDecorationType.GLASSES, p), new Item.Properties());
	public static final Item GOLEM_DECO_BOWTIE = register("golem_deco_bowtie", p -> new GolemDecorationItem(GolemDecorationType.BOWTIE, p), new Item.Properties());
	public static final Item GOLEM_DECO_FEZ = register("golem_deco_fez", p -> new GolemDecorationItem(GolemDecorationType.FEZ, p), new Item.Properties());
	public static final Item GOLEM_DECO_DART = register("golem_deco_dart", p -> new GolemDecorationItem(GolemDecorationType.DARTGUN, p), new Item.Properties());
	public static final Item GOLEM_DECO_VISOR = register("golem_deco_visor", p -> new GolemDecorationItem(GolemDecorationType.VISOR, p), new Item.Properties());
	public static final Item GOLEM_DECO_ARMOR = register("golem_deco_armor", p -> new GolemDecorationItem(GolemDecorationType.ARMOR, p), new Item.Properties());
	public static final Item GOLEM_DECO_MACE = register("golem_deco_mace", p -> new GolemDecorationItem(GolemDecorationType.MACE, p), new Item.Properties());
	public static final Item TRAVELING_TRUNK = register("traveling_trunk", dev.neothaum.golem.TrunkSpawnerItem::new, new Item.Properties().stacksTo(1));
	public static final Item TRUNK_SPAWNER = TRAVELING_TRUNK;

	public static final TagKey<Item> VOID_INGOTS = TagKey.create(Registries.ITEM, NeoThaum.id("void_metal_ingots"));
	public static final ToolMaterial VOID_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 150, 8.0F, 3.0F, 10, VOID_INGOTS);
	public static final Item VOID_SWORD = register("void_sword", p -> new VoidGear.Tool(60, p), new Item.Properties().sword(VOID_TOOLS, 3.0F, -2.4F));
	public static final Item VOID_SHOVEL = register("void_shovel", p -> new VoidGear.Tool(80, p), new Item.Properties().shovel(VOID_TOOLS, 1.5F, -3.0F));
	public static final Item VOID_PICKAXE = register("void_pickaxe", p -> new VoidGear.Tool(80, p), new Item.Properties().pickaxe(VOID_TOOLS, 1.0F, -2.8F));
	public static final Item VOID_AXE = register("void_axe", p -> new VoidGear.Tool(80, p), new Item.Properties().axe(VOID_TOOLS, 6.0F, -3.1F));
	public static final Item VOID_HOE = register("void_hoe", p -> new VoidGear.Tool(80, p), new Item.Properties().hoe(VOID_TOOLS, -2.0F, -1.0F));

	public static final ToolMaterial PRIMAL_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 500, 8.0F, 4.0F, 20, VOID_INGOTS);
	public static final Item PRIMAL_CRUSHER = register("primal_crusher", PrimalCrusherItem::new,
		new Item.Properties().pickaxe(PRIMAL_TOOLS, 1.0F, -2.8F).rarity(Rarity.RARE));

	public static final ArmorMaterial VOID_ARMOR = armor(10, 3, 7, 6, 3, SoundEvents.ARMOR_EQUIP_IRON, VOID_INGOTS, "void");
	public static final Item VOID_HELMET = register("void_helm", VoidGear.Armor::new, new Item.Properties().humanoidArmor(VOID_ARMOR, ArmorType.HELMET));
	public static final Item VOID_CHESTPLATE = register("void_chestplate", VoidGear.Armor::new, new Item.Properties().humanoidArmor(VOID_ARMOR, ArmorType.CHESTPLATE));
	public static final Item VOID_LEGGINGS = register("void_leggings", VoidGear.Armor::new, new Item.Properties().humanoidArmor(VOID_ARMOR, ArmorType.LEGGINGS));
	public static final Item VOID_BOOTS = register("void_boots", VoidGear.Armor::new, new Item.Properties().humanoidArmor(VOID_ARMOR, ArmorType.BOOTS));

	public static final ArmorMaterial FORTRESS_ARMOR = armor(40, 3, 7, 6, 3, SoundEvents.ARMOR_EQUIP_IRON, THAUMIUM_INGOTS, "fortress");
	public static final Item THAUMIUM_FORTRESS_HELM = register("thaumium_fortress_helm", p -> new FortressArmorItem(EquipmentSlot.HEAD, p),
		new Item.Properties().humanoidArmor(FORTRESS_ARMOR, ArmorType.HELMET).rarity(Rarity.RARE));
	public static final Item THAUMIUM_FORTRESS_CUIRASS = register("thaumium_fortress_cuirass", p -> new FortressArmorItem(EquipmentSlot.CHEST, p),
		new Item.Properties().humanoidArmor(FORTRESS_ARMOR, ArmorType.CHESTPLATE).rarity(Rarity.RARE));
	public static final Item THAUMIUM_FORTRESS_THIGH_GUARDS = register("thaumium_fortress_thigh_guards", p -> new FortressArmorItem(EquipmentSlot.LEGS, p),
		new Item.Properties().humanoidArmor(FORTRESS_ARMOR, ArmorType.LEGGINGS).rarity(Rarity.RARE));

	public static final ArmorMaterial VOID_ROBE_ARMOR = armor(18, 3, 7, 6, 3, SoundEvents.ARMOR_EQUIP_IRON, VOID_INGOTS, "void_robe");
	public static final Item VOID_ROBE_HELM = register("void_robe_helm", p -> new dev.neothaum.items.equipment.VoidRobeArmorItem(EquipmentSlot.HEAD, p),
		new Item.Properties().humanoidArmor(VOID_ROBE_ARMOR, ArmorType.HELMET).rarity(Rarity.EPIC)
			.component(DataComponents.DYED_COLOR, new DyedItemColor(dev.neothaum.items.equipment.VoidRobeArmorItem.DEFAULT_COLOR)));
	public static final Item VOID_ROBE_CHEST = register("void_robe_chest", p -> new dev.neothaum.items.equipment.VoidRobeArmorItem(EquipmentSlot.CHEST, p),
		new Item.Properties().humanoidArmor(VOID_ROBE_ARMOR, ArmorType.CHESTPLATE).rarity(Rarity.EPIC)
			.component(DataComponents.DYED_COLOR, new DyedItemColor(dev.neothaum.items.equipment.VoidRobeArmorItem.DEFAULT_COLOR)));
	public static final Item VOID_ROBE_LEGS = register("void_robe_legs", p -> new dev.neothaum.items.equipment.VoidRobeArmorItem(EquipmentSlot.LEGS, p),
		new Item.Properties().humanoidArmor(VOID_ROBE_ARMOR, ArmorType.LEGGINGS).rarity(Rarity.EPIC)
			.component(DataComponents.DYED_COLOR, new DyedItemColor(dev.neothaum.items.equipment.VoidRobeArmorItem.DEFAULT_COLOR)));

	public static final ArmorMaterial CULTIST_ROBE_ARMOR = armor(15, 2, 6, 5, 2, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.ItemTags.REPAIRS_IRON_ARMOR, "cultist_robe");
	public static final Item CULTIST_ROBE_HELM = register("cultist_robe_helm", p -> new dev.neothaum.items.equipment.CultistRobeArmorItem(EquipmentSlot.HEAD, p),
		new Item.Properties().humanoidArmor(CULTIST_ROBE_ARMOR, ArmorType.HELMET).rarity(Rarity.UNCOMMON));
	public static final Item CULTIST_ROBE_CHEST = register("cultist_robe_chest", p -> new dev.neothaum.items.equipment.CultistRobeArmorItem(EquipmentSlot.CHEST, p),
		new Item.Properties().humanoidArmor(CULTIST_ROBE_ARMOR, ArmorType.CHESTPLATE).rarity(Rarity.UNCOMMON));
	public static final Item CULTIST_ROBE_LEGS = register("cultist_robe_legs", p -> new dev.neothaum.items.equipment.CultistRobeArmorItem(EquipmentSlot.LEGS, p),
		new Item.Properties().humanoidArmor(CULTIST_ROBE_ARMOR, ArmorType.LEGGINGS).rarity(Rarity.UNCOMMON));

	public static final ArmorMaterial CULTIST_BOOTS_ARMOR = armor(15, 2, 6, 5, 2, SoundEvents.ARMOR_EQUIP_IRON, net.minecraft.tags.ItemTags.REPAIRS_IRON_ARMOR, "cultist_boots");
	public static final Item CULTIST_BOOTS = register("cultist_boots", dev.neothaum.items.equipment.CultistBootsItem::new,
		new Item.Properties().humanoidArmor(CULTIST_BOOTS_ARMOR, ArmorType.BOOTS).rarity(Rarity.UNCOMMON));

	public static final ArmorMaterial CULTIST_PLATE_ARMOR = armor(15, 2, 6, 5, 2, SoundEvents.ARMOR_EQUIP_IRON, net.minecraft.tags.ItemTags.REPAIRS_IRON_ARMOR, "cultist_plate");
	public static final Item CULTIST_PLATE_HELM = register("cultist_plate_helm", p -> new dev.neothaum.items.equipment.CultistPlateArmorItem(EquipmentSlot.HEAD, p),
		new Item.Properties().humanoidArmor(CULTIST_PLATE_ARMOR, ArmorType.HELMET).rarity(Rarity.UNCOMMON));
	public static final Item CULTIST_PLATE_CHEST = register("cultist_plate_chest", p -> new dev.neothaum.items.equipment.CultistPlateArmorItem(EquipmentSlot.CHEST, p),
		new Item.Properties().humanoidArmor(CULTIST_PLATE_ARMOR, ArmorType.CHESTPLATE).rarity(Rarity.UNCOMMON));
	public static final Item CULTIST_PLATE_LEGS = register("cultist_plate_legs", p -> new dev.neothaum.items.equipment.CultistPlateArmorItem(EquipmentSlot.LEGS, p),
		new Item.Properties().humanoidArmor(CULTIST_PLATE_ARMOR, ArmorType.LEGGINGS).rarity(Rarity.UNCOMMON));

	public static final ArmorMaterial CULTIST_LEADER_ARMOR = armor(40, 3, 7, 6, 3, SoundEvents.ARMOR_EQUIP_IRON, net.minecraft.tags.ItemTags.REPAIRS_IRON_ARMOR, "cultist_leader");
	public static final Item CULTIST_LEADER_HELM = register("cultist_leader_helm", p -> new dev.neothaum.items.equipment.CultistLeaderArmorItem(EquipmentSlot.HEAD, p),
		new Item.Properties().humanoidArmor(CULTIST_LEADER_ARMOR, ArmorType.HELMET).rarity(Rarity.RARE));
	public static final Item CULTIST_LEADER_CHEST = register("cultist_leader_chest", p -> new dev.neothaum.items.equipment.CultistLeaderArmorItem(EquipmentSlot.CHEST, p),
		new Item.Properties().humanoidArmor(CULTIST_LEADER_ARMOR, ArmorType.CHESTPLATE).rarity(Rarity.RARE));
	public static final Item CULTIST_LEADER_LEGS = register("cultist_leader_legs", p -> new dev.neothaum.items.equipment.CultistLeaderArmorItem(EquipmentSlot.LEGS, p),
		new Item.Properties().humanoidArmor(CULTIST_LEADER_ARMOR, ArmorType.LEGGINGS).rarity(Rarity.RARE));

	public static final ToolMaterial CRIMSON_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 200, 8.0F, 3.5F, 20, VOID_INGOTS);
	public static final Item CRIMSON_BLADE = register("crimson_blade", dev.neothaum.items.equipment.CrimsonSwordItem::new,
		new Item.Properties().sword(CRIMSON_TOOL, 3.5F, -2.4F).rarity(Rarity.RARE));

	public static final Item PRIMAL_ARROW_AIR = register("primal_arrow_air", p -> new dev.neothaum.items.equipment.PrimalArrowItem(0, p), new Item.Properties());
	public static final Item PRIMAL_ARROW_FIRE = register("primal_arrow_fire", p -> new dev.neothaum.items.equipment.PrimalArrowItem(1, p), new Item.Properties());
	public static final Item PRIMAL_ARROW_WATER = register("primal_arrow_water", p -> new dev.neothaum.items.equipment.PrimalArrowItem(2, p), new Item.Properties());
	public static final Item PRIMAL_ARROW_EARTH = register("primal_arrow_earth", p -> new dev.neothaum.items.equipment.PrimalArrowItem(3, p), new Item.Properties());
	public static final Item PRIMAL_ARROW_ORDER = register("primal_arrow_order", p -> new dev.neothaum.items.equipment.PrimalArrowItem(4, p), new Item.Properties());
	public static final Item PRIMAL_ARROW_ENTROPY = register("primal_arrow_entropy", p -> new dev.neothaum.items.equipment.PrimalArrowItem(5, p), new Item.Properties());

	// ItemNuggetEdible (1, 0.3) and ItemTripleMeatTreat (6, 0.8, always edible, 66% chance of 5 s of regeneration)
	public static final Item CHICKEN_NUGGET = food("chicken_nugget");
	public static final Item BEEF_NUGGET = food("beef_nugget");
	public static final Item PORK_NUGGET = food("pork_nugget");
	public static final Item FISH_NUGGET = food("fish_nugget");
	public static final Item TRIPLE_MEAT_TREAT = register("triple_meat_treat", Item::new, new Item.Properties().food(
		new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).alwaysEdible().build(),
		Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.66F)).build()));

	public static final Item BUCKET_OF_LIQUID_DEATH = register("bucket_of_liquid_death", p -> new FluidBucketItem(() -> TVBlocks.LIQUID_DEATH, p), new Item.Properties());
	public static final Item BUCKET_OF_PURIFYING_FLUID = register("bucket_of_purifying_fluid", p -> new net.minecraft.world.item.BucketItem(dev.neothaum.fluids.TVFluids.PURIFYING, p),
		new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET));
	public static final Item PURIFYING_BATH_SALTS = register("purifying_bath_salts", BathSaltsItem::new, new Item.Properties());
	public static final Item SANITIZING_SOAP = register("sanitizing_soap", SanitizingSoapItem::new, new Item.Properties());

	private static ArmorMaterial armor(int durability, int head, int chest, int legs, int feet, Holder<SoundEvent> sound, TagKey<Item> repair,
		String asset) {
		Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
		defense.put(ArmorType.HELMET, head);
		defense.put(ArmorType.CHESTPLATE, chest);
		defense.put(ArmorType.LEGGINGS, legs);
		defense.put(ArmorType.BOOTS, feet);
		defense.put(ArmorType.BODY, chest);
		return new ArmorMaterial(durability, defense, 25, sound, 0.0F, 0.0F, repair, ResourceKey.create(EquipmentAssets.ROOT_ID, NeoThaum.id(asset)));
	}

	private static Item robe(String name, EquipmentSlot slot, ArmorType type) {
		return register(name, p -> new RobeArmorItem(slot, p), new Item.Properties().humanoidArmor(ROBE_ARMOR, type)
			.component(DataComponents.DYED_COLOR, new DyedItemColor(RobeArmorItem.DEFAULT_COLOR)));
	}

	private static Item runic(String name, BaubleType type, int charge, RunicBaubleItem.Kind kind, Rarity rarity) {
		return register(name, p -> new RunicBaubleItem(type, charge, kind, p), new Item.Properties().stacksTo(1).rarity(rarity));
	}

	private static net.minecraft.world.item.component.CookingFuel fuel(int ticks) {
		return new net.minecraft.world.item.component.CookingFuel(new net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant(ticks),
			net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat.fromKey(
				net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER));
	}

	private static Item food(String name) {
		return register(name, Item::new, new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build()));
	}

	private static Item rod(String name, Rarity rarity) {
		return register(name, Item::new, new Item.Properties().rarity(rarity));
	}

	private static Item simple(String name) {
		return register(name, Item::new, new Item.Properties());
	}

	public static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, NeoThaum.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
		TAB_ORDER.add(item);
		return item;
	}

	public static void init() {
		for (Block block : TVBlocks.WITH_ITEMS) {
			var id = BuiltInRegistries.BLOCK.getKey(block);
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
			Item.Properties props = new Item.Properties().setId(key).useBlockDescriptionPrefix();
			if (block == TVBlocks.GREATWOOD_LOG || block == TVBlocks.SILVERWOOD_LOG) {
				// EventHandlerWorld.getBurnTime: magical logs burn for 400
				props.component(DataComponents.COOKING_FUEL, fuel(400));
			}
			Item item = Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, props));
			TAB_ORDER.add(item);
		}
	}
}
