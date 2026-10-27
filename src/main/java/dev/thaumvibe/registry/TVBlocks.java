package dev.thaumvibe.registry;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.blocks.AlchemyFurnaceBlock;
import dev.thaumvibe.blocks.AlembicBlock;
import dev.thaumvibe.blocks.ArcaneDoorBlock;
import dev.thaumvibe.blocks.ArcaneEarBlock;
import dev.thaumvibe.blocks.ArcaneLampBlock;
import dev.thaumvibe.blocks.ArcaneLevitatorBlock;
import dev.thaumvibe.blocks.ArcanePressurePlateBlock;
import dev.thaumvibe.blocks.CentrifugeBlock;
import dev.thaumvibe.blocks.CrystallizerBlock;
import dev.thaumvibe.blocks.ReservoirBlock;
import dev.thaumvibe.blocks.ItemGrateBlock;
import dev.thaumvibe.blocks.LiquidDeathBlock;
import dev.thaumvibe.blocks.PavingStoneOfTravelBlock;
import dev.thaumvibe.blocks.PavingStoneOfWardingBlock;
import dev.thaumvibe.blocks.PurifyingFluidBlock;
import dev.thaumvibe.blocks.WardedGlassBlock;
import dev.thaumvibe.blocks.WardingAuraBlock;
import dev.thaumvibe.blocks.LampOfGrowthBlock;
import dev.thaumvibe.blocks.LampOfFertilityBlock;
import dev.thaumvibe.blocks.ArcaneWorktableBlock;
import dev.thaumvibe.blocks.BellowsBlock;
import dev.thaumvibe.blocks.BrainJarBlock;
import dev.thaumvibe.blocks.FluxFluidBlock;
import dev.thaumvibe.blocks.InfusionMatrixBlock;
import dev.thaumvibe.blocks.InfusionPillarBlock;
import dev.thaumvibe.blocks.PedestalBlock;
import dev.thaumvibe.blocks.AuraNodeBlock;
import dev.thaumvibe.blocks.CrucibleBlock;
import dev.thaumvibe.blocks.CrystalClusterBlock;
import dev.thaumvibe.blocks.MagicalLeavesBlock;
import dev.thaumvibe.blocks.JarBlock;
import dev.thaumvibe.blocks.NitorLightBlock;
import dev.thaumvibe.blocks.TubeBlock;
import dev.thaumvibe.tiles.TubeBlockEntity;
import dev.thaumvibe.blocks.ResearchTableBlock;
import dev.thaumvibe.blocks.TCPlantBlock;
import dev.thaumvibe.blocks.TableBlock;
import dev.thaumvibe.blocks.TCSaplingBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import dev.thaumvibe.blocks.TallowCandleBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class TVBlocks {
	/** Blocks that get a BlockItem, in creative-tab order. */
	public static final List<Block> WITH_ITEMS = new ArrayList<>();

	// ---- ores (BlockCustomOre) ----
	public static final Block CINNABAR_ORE = register("cinnabar_ore", Block::new, stone(1.5F, 5.0F), true);
	public static final Block AIR_INFUSED_STONE = infused("air_infused_stone");
	public static final Block FIRE_INFUSED_STONE = infused("fire_infused_stone");
	public static final Block WATER_INFUSED_STONE = infused("water_infused_stone");
	public static final Block EARTH_INFUSED_STONE = infused("earth_infused_stone");
	public static final Block ORDER_INFUSED_STONE = infused("order_infused_stone");
	public static final Block ENTROPY_INFUSED_STONE = infused("entropy_infused_stone");
	public static final Block AMBER_BEARING_STONE = register("amber_bearing_stone", p -> new DropExperienceBlock(UniformInt.of(1, 4), p),
		stone(1.5F, 5.0F), true);
	// deepslate variants (Thaumvibe): worldgen places these where it replaces deepslate, as vanilla does for its ores
	public static final Block DEEPSLATE_CINNABAR_ORE = register("deepslate_cinnabar_ore", Block::new, deepslate(3.0F, 5.0F), true);
	public static final Block DEEPSLATE_AIR_INFUSED_STONE = deepslateInfused("deepslate_air_infused_stone");
	public static final Block DEEPSLATE_FIRE_INFUSED_STONE = deepslateInfused("deepslate_fire_infused_stone");
	public static final Block DEEPSLATE_WATER_INFUSED_STONE = deepslateInfused("deepslate_water_infused_stone");
	public static final Block DEEPSLATE_EARTH_INFUSED_STONE = deepslateInfused("deepslate_earth_infused_stone");
	public static final Block DEEPSLATE_ORDER_INFUSED_STONE = deepslateInfused("deepslate_order_infused_stone");
	public static final Block DEEPSLATE_ENTROPY_INFUSED_STONE = deepslateInfused("deepslate_entropy_infused_stone");
	public static final Block DEEPSLATE_AMBER_BEARING_STONE = register("deepslate_amber_bearing_stone",
		p -> new DropExperienceBlock(UniformInt.of(1, 4), p), deepslate(3.0F, 5.0F), true);
	public static final Block RAW_CINNABAR_BLOCK = register("raw_cinnabar_block", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
			.strength(5.0F, 6.0F), true);

	// ---- crystals (BlockCrystal) ----
	public static final Block AIR_CRYSTAL_CLUSTER = crystal("air_crystal_cluster", 0);
	public static final Block FIRE_CRYSTAL_CLUSTER = crystal("fire_crystal_cluster", 1);
	public static final Block WATER_CRYSTAL_CLUSTER = crystal("water_crystal_cluster", 2);
	public static final Block EARTH_CRYSTAL_CLUSTER = crystal("earth_crystal_cluster", 3);
	public static final Block ORDER_CRYSTAL_CLUSTER = crystal("order_crystal_cluster", 4);
	public static final Block ENTROPY_CRYSTAL_CLUSTER = crystal("entropy_crystal_cluster", 5);
	public static final Block MIXED_CRYSTAL_CLUSTER = crystal("mixed_crystal_cluster", 6);

	// ---- trees (BlockMagicalLog / Leaves / CustomPlant / WoodenDevice planks) ----
	public static final Block GREATWOOD_LOG = register("greatwood_log", RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 5.0F).sound(SoundType.WOOD).ignitedByLava(), true);
	public static final Block SILVERWOOD_LOG = register("silverwood_log", RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(2.0F, 5.0F).sound(SoundType.WOOD).ignitedByLava()
			.lightLevel(s -> 5), true);
	public static final Block GREATWOOD_LEAVES = register("greatwood_leaves", p -> new MagicalLeavesBlock(false, p), leaves(), true);
	public static final Block SILVERWOOD_LEAVES = register("silverwood_leaves", p -> new MagicalLeavesBlock(true, p), leaves().lightLevel(s -> 7), true);
	public static final Block GREATWOOD_SAPLING = register("greatwood_sapling", p -> new TCSaplingBlock(false, p), sapling(), true);
	public static final Block SILVERWOOD_SAPLING = register("silverwood_sapling", p -> new TCSaplingBlock(true, p),
		sapling().lightLevel(s -> 8), true);
	public static final Block GREATWOOD_PLANKS = register("greatwood_planks", Block::new, planks(), true);
	public static final Block SILVERWOOD_PLANKS = register("silverwood_planks", Block::new, planks(), true);

	// ---- plants ----
	public static final Block SHIMMERLEAF = register("shimmerleaf", p -> new TCPlantBlock(TCPlantBlock.Kind.SHIMMERLEAF, p), plant().lightLevel(s -> 8), true);
	public static final Block CINDERPEARL = register("cinderpearl", p -> new TCPlantBlock(TCPlantBlock.Kind.CINDERPEARL, p), plant().lightLevel(s -> 8), true);
	public static final Block VISHROOM = register("vishroom", p -> new TCPlantBlock(TCPlantBlock.Kind.VISHROOM, p), plant().lightLevel(s -> 8), true);
	public static final Block ETHEREAL_BLOOM = register("ethereal_bloom", dev.thaumvibe.blocks.EtherealBloomBlock::new, plant().lightLevel(s -> 12), true);

	// ---- cosmetic ----
	public static final Block AMBER_BLOCK = register("amber_block", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5F, 5.0F).sound(SoundType.GLASS).noOcclusion(), true);
	public static final Block AMBER_BRICKS = register("amber_bricks", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5F, 5.0F).sound(SoundType.GLASS).noOcclusion(), true);
	public static final Block THAUMIUM_BLOCK = register("thaumium_block", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(4.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), true);
	public static final Block TALLOW_BLOCK = register("tallow_block", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(2.0F, 10.0F).sound(SoundType.STONE), true);
	public static final Block ARCANE_STONE_BLOCK = register("arcane_stone_block", Block::new, stone(2.0F, 10.0F), true);
	public static final Block ARCANE_STONE_BRICKS = register("arcane_stone_bricks", Block::new, stone(2.0F, 10.0F), true);
	public static final Block ARCANE_STONE_STAIRS = register("arcane_stone_stairs", p -> stairs(ARCANE_STONE_BRICKS, p), stone(2.0F, 10.0F), true);
	public static final Block ARCANE_STONE_SLAB = register("arcane_stone_slab", SlabBlock::new, stone(2.0F, 10.0F), true);
	public static final Block GREATWOOD_STAIRS = register("greatwood_stairs", p -> stairs(GREATWOOD_PLANKS, p), planks(), true);
	public static final Block SILVERWOOD_STAIRS = register("silverwood_stairs", p -> stairs(SILVERWOOD_PLANKS, p), planks(), true);
	public static final Block GREATWOOD_SLAB = register("greatwood_slab", SlabBlock::new, planks(), true);
	public static final Block SILVERWOOD_SLAB = register("silverwood_slab", SlabBlock::new, planks(), true);
	/** BlockCandle: one block per dye colour, light level 0.95 (14). */
	public static final Block[] TALLOW_CANDLES = candles();

	private static Block[] candles() {
		String[] names = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue",
			"brown", "green", "red", "black"};
		Block[] out = new Block[names.length];
		for (int i = 0; i < names.length; i++) {
			int c = i;
			out[i] = register(names[i] + "_tallow_candle", p -> new TallowCandleBlock(c, p),
				BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.1F).lightLevel(st -> 14).sound(SoundType.CANDLE).noOcclusion()
					.pushReaction(PushReaction.POPPED), true);
		}
		return out;
	}

	/** StairBlock's constructor is protected. */
	private static Block stairs(Block base, BlockBehaviour.Properties p) {
		return new StairBlock(base.defaultBlockState(), p) {
		};
	}

	// ---- devices ----
	public static final Block TABLE = register("table", TableBlock::new, wood(2.5F), true);
	public static final Block ARCANE_WORKTABLE = register("arcane_worktable", ArcaneWorktableBlock::new, wood(2.5F), true);
	public static final Block RESEARCH_TABLE = register("research_table", ResearchTableBlock::new, wood(2.5F), false);
	public static final Block CRUCIBLE = register("crucible", CrucibleBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.0F).sound(SoundType.METAL).noOcclusion(), true);

	// ---- airy (BlockAiry) ----
	public static final Block AURA_NODE = register("aura_node", AuraNodeBlock::new,
		BlockBehaviour.Properties.of().replaceable().noCollision().noLootTable().strength(-1.0F, 3600000.0F).lightLevel(s -> 8).noOcclusion()
			.pushReaction(PushReaction.IMMOVEABLE), false);
	public static final Block NITOR_LIGHT = register("nitor_light", NitorLightBlock::new,
		BlockBehaviour.Properties.of().noCollision().instabreak().lightLevel(s -> 15).noOcclusion().sound(SoundType.WOOL).pushReaction(PushReaction.POPPED),
		false);

	/** BlockAiry meta 10: the earth shock orb's static field. */
	public static final Block STATIC_FIELD = register("static_field", dev.thaumvibe.blocks.StaticFieldBlock::new,
		BlockBehaviour.Properties.of().replaceable().noCollision().noLootTable().strength(100.0F, 50.0F).noOcclusion().randomTicks()
			.pushReaction(PushReaction.POPPED), false);
	/** BlockHole: the portable hole's empty space. */
	public static final Block EMPTY_SPACE = register("empty_space", dev.thaumvibe.blocks.HoleBlock::new,
		BlockBehaviour.Properties.of().noCollision().noLootTable().strength(-1.0F, 6000000.0F).lightLevel(s -> 10).noOcclusion()
			.sound(SoundType.GLASS).pushReaction(PushReaction.IMMOVEABLE), false);
	/** BlockWarded: a block sealed by the warding focus. */
	public static final Block WARDED_BLOCK = register("warded_block", dev.thaumvibe.blocks.WardedBlock::new,
		BlockBehaviour.Properties.of().noLootTable().strength(-1.0F, 999.0F).lightLevel(s -> s.getValue(dev.thaumvibe.blocks.WardedBlock.LIGHT))
			.pushReaction(PushReaction.IMMOVEABLE).isValidSpawn((s, l, p, t) -> false), false);

	// ---- essentia (BlockJar, BlockTube) ----
	public static final Block WARDED_JAR = register("warded_jar", p -> new JarBlock(false, p), jar(), true);
	public static final Block VOID_JAR = register("void_jar", p -> new JarBlock(true, p), jar(), true);
	public static final Block BRAIN_IN_A_JAR = register("brain_in_a_jar", BrainJarBlock::new, jar(), true);
	/** BlockJar meta 2 (getLightValue 11); its item is TVItems.NODE_IN_A_JAR. */
	public static final Block NODE_IN_A_JAR = register("node_in_a_jar", dev.thaumvibe.blocks.JarNodeBlock::new, jar().lightLevel(s -> 11), false);
	public static final Block ESSENTIA_TUBE = tube("essentia_tube", TubeBlockEntity.Kind.NORMAL);
	public static final Block ESSENTIA_VALVE = tube("essentia_valve", TubeBlockEntity.Kind.VALVE);
	public static final Block FILTERED_ESSENTIA_TUBE = tube("filtered_essentia_tube", TubeBlockEntity.Kind.FILTER);
	public static final Block RESTRICTED_ESSENTIA_TUBE = tube("restricted_essentia_tube", TubeBlockEntity.Kind.RESTRICT);
	public static final Block DIRECTIONAL_ESSENTIA_TUBE = tube("directional_essentia_tube", TubeBlockEntity.Kind.ONEWAY);
	public static final Block ESSENTIA_BUFFER = tube("essentia_buffer", TubeBlockEntity.Kind.BUFFER);
	// ---- infusion (BlockStoneDevice 1 to 4) ----
	public static final Block ARCANE_PEDESTAL = register("arcane_pedestal", PedestalBlock::new, stone(2.0F, 10.0F).noOcclusion(), true);
	public static final Block RUNIC_MATRIX = register("runic_matrix", InfusionMatrixBlock::new, stone(2.0F, 10.0F).noOcclusion().lightLevel(s -> 10),
		true);
	public static final Block INFUSION_PILLAR = register("infusion_pillar", InfusionPillarBlock::new, stone(2.0F, 10.0F).noOcclusion(), false);
	// ---- vis network and wand devices (BlockStoneDevice 5, 8-11, 13; BlockMetalDevice 2, 14; BlockAiry 5) ----
	public static final Block WAND_RECHARGE_PEDESTAL = register("wand_recharge_pedestal", p -> new dev.thaumvibe.blocks.WandPedestalBlock(false, p),
		stone(2.0F, 10.0F).noOcclusion(), true);
	public static final Block COMPOUND_RECHARGE_FOCUS = register("compound_recharge_focus", p -> new dev.thaumvibe.blocks.WandPedestalBlock(true, p),
		stone(2.0F, 10.0F).noOcclusion(), true);
	public static final Block NODE_STABILIZER = register("node_stabilizer",
		p -> new dev.thaumvibe.blocks.NodeDeviceBlock(dev.thaumvibe.blocks.NodeDeviceBlock.Kind.STABILIZER, p), stone(2.0F, 10.0F).noOcclusion(), true);
	public static final Block ADVANCED_NODE_STABILIZER = register("advanced_node_stabilizer",
		p -> new dev.thaumvibe.blocks.NodeDeviceBlock(dev.thaumvibe.blocks.NodeDeviceBlock.Kind.ADVANCED_STABILIZER, p), stone(2.0F, 10.0F).noOcclusion(),
		true);
	public static final Block NODE_TRANSDUCER = register("node_transducer",
		p -> new dev.thaumvibe.blocks.NodeDeviceBlock(dev.thaumvibe.blocks.NodeDeviceBlock.Kind.TRANSDUCER, p), stone(2.0F, 10.0F).noOcclusion(), true);
	public static final Block ENERGIZED_AURA_NODE = register("energized_aura_node",
		p -> new dev.thaumvibe.blocks.NodeDeviceBlock(dev.thaumvibe.blocks.NodeDeviceBlock.Kind.ENERGIZED_NODE, p),
		BlockBehaviour.Properties.of().replaceable().noCollision().noLootTable().strength(-1.0F, 3600000.0F).lightLevel(s -> 8).noOcclusion()
			.pushReaction(PushReaction.IMMOVEABLE), false);
	public static final Block FOCAL_MANIPULATOR = register("focal_manipulator", dev.thaumvibe.blocks.FocalManipulatorBlock::new,
		stone(2.0F, 10.0F).noOcclusion(), true);
	public static final Block VIS_RELAY = register("vis_relay", p -> new dev.thaumvibe.blocks.VisRelayBlock(false, p),
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.0F).sound(SoundType.METAL).noOcclusion()
			.lightLevel(s -> 10), true);
	public static final Block VIS_CHARGE_RELAY = register("vis_charge_relay", p -> new dev.thaumvibe.blocks.VisRelayBlock(true, p),
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.0F).sound(SoundType.METAL).noOcclusion()
			.lightLevel(s -> 10), true);
	// ---- flux (BlockFluxGoo, BlockFluxGas) ----
	public static final Block FLUX_GOO = register("flux_goo", p -> new FluxFluidBlock(false, p), flux(), true);
	public static final Block FLUX_GAS = register("flux_gas", p -> new FluxFluidBlock(true, p), flux(), true);
	// ---- taint (BlockTaint 0 to 2, BlockTaintFibres) ----
	public static final Block CRUSTED_TAINT = register("crusted_taint", p -> new dev.thaumvibe.blocks.TaintBlock(dev.thaumvibe.blocks.TaintBlock.Kind.CRUST,
		p), taint(1.75F, 10.0F), true);
	public static final Block TAINTED_SOIL = register("tainted_soil", p -> new dev.thaumvibe.blocks.TaintBlock(dev.thaumvibe.blocks.TaintBlock.Kind.SOIL,
		p), taint(1.5F, 10.0F), true);
	public static final Block FLESH_BLOCK = register("flesh_block", p -> new dev.thaumvibe.blocks.TaintBlock(dev.thaumvibe.blocks.TaintBlock.Kind.FLESH,
		p), taint(0.2F, 10.0F), true);
	public static final Block TAINT_FIBRES = register("taint_fibres", dev.thaumvibe.blocks.TaintFibreBlock::new, taint(1.0F, 5.0F).noCollision()
		.noOcclusion().replaceable().lightLevel(dev.thaumvibe.blocks.TaintFibreBlock::lightLevel).pushReaction(PushReaction.POPPED), true);
	public static final Block ARCANE_BELLOWS = register("arcane_bellows", BellowsBlock::new, wood(1.0F), true);
	public static final Block HUNGRY_CHEST = register("hungry_chest", dev.thaumvibe.blocks.HungryChestBlock::new, wood(2.5F).noOcclusion(), true);

	public static final Block ARCANE_FURNACE = register("arcane_furnace", dev.thaumvibe.blocks.ArcaneFurnaceBlock::new, stone(10.0F, 500.0F).sound(SoundType.STONE).noOcclusion().lightLevel(s -> s.getValue(dev.thaumvibe.blocks.ArcaneFurnaceBlock.PART) == dev.thaumvibe.blocks.ArcaneFurnaceBlock.FurnacePart.CORE || s.getValue(dev.thaumvibe.blocks.ArcaneFurnaceBlock.PART) == dev.thaumvibe.blocks.ArcaneFurnaceBlock.FurnacePart.MOUTH ? 13 : 0), false);
	public static final Block ALCHEMICAL_FURNACE = register("alchemical_furnace", AlchemyFurnaceBlock::new,
		stone(2.0F, 10.0F).lightLevel(s -> s.getValue(AlchemyFurnaceBlock.LIT) ? 12 : 0), true);
	public static final Block ARCANE_ALEMBIC = register("arcane_alembic", AlembicBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.0F).sound(SoundType.METAL).noOcclusion(), true);
	public static final Block ARCANE_LAMP = register("arcane_lamp", ArcaneLampBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.0F).sound(SoundType.METAL).noOcclusion().lightLevel(s -> 15), true);
	public static final Block LAMP_OF_GROWTH = register("lamp_of_growth", LampOfGrowthBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3.0F, 17.0F).sound(SoundType.METAL).noOcclusion()
			.lightLevel(s -> s.getValue(LampOfGrowthBlock.LIT) ? 15 : 8), true);
	public static final Block LAMP_OF_FERTILITY = register("lamp_of_fertility", LampOfFertilityBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3.0F, 17.0F).sound(SoundType.METAL).noOcclusion()
			.lightLevel(s -> s.getValue(LampOfFertilityBlock.LIT) ? 15 : 8), true);
	public static final Block ARCANE_DOOR = register("arcane_door", ArcaneDoorBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(15.0F, 100.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.IMMOVEABLE), true);
	public static final Block WARDED_GLASS = register("warded_glass", WardedGlassBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(15.0F, 100.0F).sound(SoundType.GLASS).noOcclusion()
			.isValidSpawn((s, l, p, e) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p, a) -> false), true);
	public static final Block PAVING_STONE_OF_TRAVEL = register("paving_stone_of_travel", PavingStoneOfTravelBlock::new,
		stone(4.0F, 10.0F), true);
	public static final Block PAVING_STONE_OF_WARDING = register("paving_stone_of_warding", PavingStoneOfWardingBlock::new,
		stone(4.0F, 100.0F).noOcclusion(), true);
	public static final Block WARDING_AURA = register("warding_aura", WardingAuraBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.NONE).noLootTable().strength(-1.0F, 3600000.0F).noOcclusion().pushReaction(PushReaction.IMMOVEABLE), false);
	public static final Block ARCANE_PRESSURE_PLATE = register("arcane_pressure_plate", ArcanePressurePlateBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(15.0F, 100.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.POPPED), true);
	public static final Block ARCANE_EAR = register("arcane_ear", ArcaneEarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F, 5.0F).sound(SoundType.WOOD).noOcclusion(), true);
	public static final Block ARCANE_LEVITATOR = register("arcane_levitator", ArcaneLevitatorBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).noOcclusion(), true);
	public static final Block ITEM_GRATE = register("item_grate", ItemGrateBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 10.0F).sound(SoundType.METAL).noOcclusion(), true);
	public static final Block ALCHEMICAL_CENTRIFUGE = register("alchemical_centrifuge", CentrifugeBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 10.0F).sound(SoundType.METAL).noOcclusion(), true);
	public static final Block ESSENTIA_CRYSTALLIZER = register("essentia_crystallizer", CrystallizerBlock::new,
		stone(2.0F, 10.0F).noOcclusion(), true);
	public static final Block ESSENTIA_RESERVOIR = register("essentia_reservoir", ReservoirBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 17.0F).sound(SoundType.METAL).noOcclusion(), true);
	public static final Block LIQUID_DEATH = register("liquid_death", LiquidDeathBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).replaceable().noCollision().strength(100.0F).noLootTable().lightLevel(s -> 4).noOcclusion(), false);
	public static final Block PURIFYING_FLUID = register("purifying_fluid", PurifyingFluidBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).replaceable().noCollision().strength(100.0F).noLootTable().lightLevel(s -> 8).noOcclusion(), false);
	public static final Block MAGIC_MIRROR = register("magic_mirror", dev.thaumvibe.blocks.MagicMirrorBlock::new, mirror(), false);
	public static final Block ESSENTIA_MIRROR = register("essentia_mirror", dev.thaumvibe.blocks.EssentiaMirrorBlock::new, mirror(), false);
	public static final Block ALCHEMICAL_CONSTRUCT = register("alchemical_construct", dev.thaumvibe.blocks.AlchemicalConstructBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.5F).sound(SoundType.METAL), true);
	public static final Block THAUMATORIUM = register("thaumatorium", dev.thaumvibe.blocks.ThaumatoriumBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.5F).sound(SoundType.METAL).noOcclusion(), false);
	public static final Block THAUMATORIUM_TOP = register("thaumatorium_top", dev.thaumvibe.blocks.ThaumatoriumTopBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.5F).sound(SoundType.METAL).noOcclusion(), false);
	public static final Block MNEMONIC_MATRIX = register("mnemonic_matrix", dev.thaumvibe.blocks.MnemonicMatrixBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 10.0F).sound(SoundType.METAL).noOcclusion(), true);
	public static final Block ARCANE_SPA = register("arcane_spa", dev.thaumvibe.blocks.ArcaneSpaBlock::new,
		stone(2.0F, 10.0F).noOcclusion(), true);
	public static final Block ADVANCED_ALCHEMICAL_CONSTRUCT = register("advanced_alchemical_construct", dev.thaumvibe.blocks.AdvancedAlchemicalConstructBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.5F).sound(SoundType.METAL), true);
	public static final Block ADVANCED_ALCHEMICAL_FURNACE = register("advanced_alchemical_furnace", dev.thaumvibe.blocks.AdvancedAlchemyFurnaceBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 17.5F).sound(SoundType.METAL).noOcclusion(), false);
	public static final Block DECONSTRUCTION_TABLE = register("deconstruction_table", dev.thaumvibe.blocks.DeconstructionTableBlock::new,
		wood(2.5F).noOcclusion(), true);
	public static final Block ARCANE_BORE_BASE = register("arcane_bore_base", dev.thaumvibe.blocks.ArcaneBoreBaseBlock::new,
		wood(2.5F).noOcclusion(), true);
	public static final Block ARCANE_BORE = register("arcane_bore", dev.thaumvibe.blocks.ArcaneBoreBlock::new,
		wood(2.5F).noOcclusion(), true);
	public static final Block MANA_POD = register("mana_pod", dev.thaumvibe.blocks.ManaPodBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.5F).sound(SoundType.GRASS).noOcclusion().randomTicks().lightLevel(s -> s.getValue(dev.thaumvibe.blocks.ManaPodBlock.AGE)), false);
	public static final Block FLUX_SCRUBBER = register("flux_scrubber", dev.thaumvibe.blocks.FluxScrubberBlock::new,
		stone(3.0F, 17.0F).sound(SoundType.STONE).noOcclusion(), true);
	public static final Block BANNER = register("banner", dev.thaumvibe.blocks.BannerBlock::new,
		wood(1.0F).sound(SoundType.WOOD).noOcclusion(), false);

	// ---- Eldritch Blocks ----
	public static final Block OBSIDIAN_TILE = register("obsidian_tile", dev.thaumvibe.blocks.eldritch.ObsidianTileBlock::new,
		stone(50.0F, 2000.0F), true);
	public static final Block ELDRITCH_STONE = register("eldritch_stone", p -> new dev.thaumvibe.blocks.eldritch.EldritchStoneBlock(dev.thaumvibe.blocks.eldritch.EldritchStoneBlock.Variant.SMOOTH, p),
		stone(20.0F, 100.0F), true);
	public static final Block ELDRITCH_STONE_CRACKED = register("eldritch_stone_cracked", p -> new dev.thaumvibe.blocks.eldritch.EldritchStoneBlock(dev.thaumvibe.blocks.eldritch.EldritchStoneBlock.Variant.CRACKED, p),
		stone(20.0F, 100.0F), true);
	public static final Block ELDRITCH_STONE_GLYPH = register("eldritch_stone_glyph", p -> new dev.thaumvibe.blocks.eldritch.EldritchStoneBlock(dev.thaumvibe.blocks.eldritch.EldritchStoneBlock.Variant.GLYPH, p),
		stone(20.0F, 100.0F), true);
	public static final Block ELDRITCH_ALTAR = register("eldritch_altar", dev.thaumvibe.blocks.eldritch.EldritchAltarBlock::new,
		stone(20.0F, 100.0F).noOcclusion(), true);
	public static final Block ELDRITCH_CAP = register("eldritch_cap", dev.thaumvibe.blocks.eldritch.EldritchCapBlock::new,
		stone(20.0F, 100.0F).noOcclusion(), true);
	public static final Block ELDRITCH_OBELISK = register("eldritch_obelisk", dev.thaumvibe.blocks.eldritch.EldritchObeliskBlock::new,
		stone(20.0F, 100.0F).noOcclusion(), true);
	public static final Block ELDRITCH_PORTAL = register("eldritch_portal", dev.thaumvibe.blocks.eldritch.EldritchPortalBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noCollision().lightLevel(s -> 15), false);

	private static BlockBehaviour.Properties mirror() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.0F, 10.0F).sound(SoundType.GLASS).noOcclusion()
			.lightLevel(s -> s.getValue(dev.thaumvibe.blocks.MirrorBlock.OPEN) ? 8 : 0);
	}

	/** Config.fluxGoomaterial: a liquid you wade through, lit like the fluid's luminosity 7. */
	private static BlockBehaviour.Properties flux() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).replaceable().noCollision().strength(100.0F).noLootTable().randomTicks()
			.lightLevel(s -> 7).noOcclusion().pushReaction(PushReaction.POPPED);
	}

	/** Config.taintMaterial, with BlockTaint's gory step sound. */
	private static BlockBehaviour.Properties taint(float hardness, float resistance) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(hardness, resistance).sound(dev.thaumvibe.blocks.TaintBlock.GORE)
			.randomTicks();
	}

	private static BlockBehaviour.Properties jar() {
		return BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.GLASS).lightLevel(s -> 10).noOcclusion();
	}

	private static Block tube(String name, TubeBlockEntity.Kind kind) {
		return register(name, p -> new TubeBlock(kind, p),
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5F).sound(SoundType.METAL).noOcclusion(), true);
	}

	private static Block deepslateInfused(String name) {
		return register(name, p -> new DropExperienceBlock(UniformInt.of(0, 3), p), deepslate(3.0F, 5.0F).lightLevel(s -> 3), true);
	}

	/** Vanilla's deepslate ores: stone's properties, half again as hard, with deepslate's colour and sound. */
	private static BlockBehaviour.Properties deepslate(float hardness, float resistance) {
		return stone(hardness, resistance).mapColor(MapColor.DEEPSLATE).sound(SoundType.DEEPSLATE);
	}

	private static Block infused(String name) {
		return register(name, p -> new DropExperienceBlock(UniformInt.of(0, 3), p), stone(1.5F, 5.0F).lightLevel(s -> 3), true);
	}

	private static Block crystal(String name, int type) {
		return register(name, p -> new CrystalClusterBlock(type, p),
			BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.7F, 1.0F).lightLevel(s -> 8).sound(SoundType.AMETHYST_CLUSTER).noOcclusion()
				.pushReaction(PushReaction.POPPED), true);
	}

	private static BlockBehaviour.Properties stone(float hardness, float resistance) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
			.strength(hardness, resistance).sound(SoundType.STONE);
	}

	private static BlockBehaviour.Properties wood(float hardness) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(hardness).sound(SoundType.WOOD).noOcclusion()
			.ignitedByLava();
	}

	private static BlockBehaviour.Properties planks() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 5.0F).sound(SoundType.WOOD)
			.ignitedByLava();
	}

	private static BlockBehaviour.Properties leaves() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
			.isValidSpawn((s, l, p, e) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p, a) -> false).ignitedByLava()
			.pushReaction(PushReaction.POPPED).isRedstoneConductor((s, l, p) -> false);
	}

	private static BlockBehaviour.Properties sapling() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS)
			.pushReaction(PushReaction.POPPED);
	}

	private static BlockBehaviour.Properties plant() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED);
	}

	public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props, boolean item) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Thaumvibe.id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key)));
		if (item) {
			WITH_ITEMS.add(block);
		}
		return block;
	}

	public static void init() {
		// stone-like vanilla block reference keeps Blocks bootstrapped before ours
		Blocks.STONE.getClass();
	}
}
