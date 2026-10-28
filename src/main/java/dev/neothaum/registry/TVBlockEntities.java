package dev.neothaum.registry;

import dev.neothaum.NeoThaum;
import dev.neothaum.tiles.AlchemyFurnaceBlockEntity;
import dev.neothaum.tiles.AlembicBlockEntity;
import dev.neothaum.tiles.ArcaneWorktableBlockEntity;
import dev.neothaum.tiles.BellowsBlockEntity;
import dev.neothaum.tiles.BrainJarBlockEntity;
import dev.neothaum.tiles.BufferBlockEntity;
import dev.neothaum.tiles.CrucibleBlockEntity;
import dev.neothaum.tiles.CrystalBlockEntity;
import dev.neothaum.tiles.InfusionMatrixBlockEntity;
import dev.neothaum.tiles.InfusionPillarBlockEntity;
import dev.neothaum.tiles.JarBlockEntity;
import dev.neothaum.tiles.PedestalBlockEntity;
import dev.neothaum.tiles.TubeBlockEntity;
import dev.neothaum.tiles.NodeBlockEntity;
import dev.neothaum.tiles.ResearchTableBlockEntity;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class TVBlockEntities {
	public static final BlockEntityType<NodeBlockEntity> NODE = register("node", NodeBlockEntity::new, TVBlocks.AURA_NODE);
	public static final BlockEntityType<CrucibleBlockEntity> CRUCIBLE = register("crucible", CrucibleBlockEntity::new, TVBlocks.CRUCIBLE);
	public static final BlockEntityType<ArcaneWorktableBlockEntity> ARCANE_WORKTABLE = register("arcane_worktable", ArcaneWorktableBlockEntity::new,
		TVBlocks.ARCANE_WORKTABLE);
	public static final BlockEntityType<ResearchTableBlockEntity> RESEARCH_TABLE = register("research_table", ResearchTableBlockEntity::new,
		TVBlocks.RESEARCH_TABLE);
	public static final BlockEntityType<CrystalBlockEntity> CRYSTAL = register("crystal", CrystalBlockEntity::new, TVBlocks.AIR_CRYSTAL_CLUSTER,
		TVBlocks.FIRE_CRYSTAL_CLUSTER, TVBlocks.WATER_CRYSTAL_CLUSTER, TVBlocks.EARTH_CRYSTAL_CLUSTER, TVBlocks.ORDER_CRYSTAL_CLUSTER,
		TVBlocks.ENTROPY_CRYSTAL_CLUSTER, TVBlocks.MIXED_CRYSTAL_CLUSTER, TVBlocks.STRANGE_CRYSTALS);
	public static final BlockEntityType<JarBlockEntity> JAR = register("jar", JarBlockEntity::new, TVBlocks.WARDED_JAR, TVBlocks.VOID_JAR);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneFurnaceBlockEntity> ARCANE_FURNACE = register("arcane_furnace", dev.neothaum.tiles.ArcaneFurnaceBlockEntity::new, TVBlocks.ARCANE_FURNACE);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneFurnaceNozzleBlockEntity> ARCANE_FURNACE_NOZZLE = register("arcane_furnace_nozzle", dev.neothaum.tiles.ArcaneFurnaceNozzleBlockEntity::new, TVBlocks.ARCANE_FURNACE);
	public static final BlockEntityType<AlchemyFurnaceBlockEntity> ALCHEMY_FURNACE = register("alchemy_furnace", AlchemyFurnaceBlockEntity::new,
		TVBlocks.ALCHEMICAL_FURNACE);
	public static final BlockEntityType<AlembicBlockEntity> ALEMBIC = register("alembic", AlembicBlockEntity::new, TVBlocks.ARCANE_ALEMBIC);
	public static final BlockEntityType<BufferBlockEntity> BUFFER = register("buffer", BufferBlockEntity::new, TVBlocks.ESSENTIA_BUFFER);
	public static final BlockEntityType<BellowsBlockEntity> BELLOWS = register("bellows", BellowsBlockEntity::new, TVBlocks.ARCANE_BELLOWS);
	public static final BlockEntityType<dev.neothaum.tiles.HungryChestBlockEntity> HUNGRY_CHEST = register("hungry_chest",
		dev.neothaum.tiles.HungryChestBlockEntity::new, TVBlocks.HUNGRY_CHEST);
	public static final BlockEntityType<dev.neothaum.tiles.ItemGrateBlockEntity> ITEM_GRATE = register("item_grate",
		dev.neothaum.tiles.ItemGrateBlockEntity::new, TVBlocks.ITEM_GRATE);
	public static final BlockEntityType<dev.neothaum.tiles.CentrifugeBlockEntity> CENTRIFUGE = register("centrifuge",
		dev.neothaum.tiles.CentrifugeBlockEntity::new, TVBlocks.ALCHEMICAL_CENTRIFUGE);
	public static final BlockEntityType<dev.neothaum.tiles.CrystallizerBlockEntity> CRYSTALLIZER = register("essentia_crystallizer",
		dev.neothaum.tiles.CrystallizerBlockEntity::new, TVBlocks.ESSENTIA_CRYSTALLIZER);
	public static final BlockEntityType<dev.neothaum.tiles.ReservoirBlockEntity> RESERVOIR = register("essentia_reservoir",
		dev.neothaum.tiles.ReservoirBlockEntity::new, TVBlocks.ESSENTIA_RESERVOIR);
	public static final BlockEntityType<BrainJarBlockEntity> BRAIN_JAR = register("brain_jar", BrainJarBlockEntity::new, TVBlocks.BRAIN_IN_A_JAR);
	public static final BlockEntityType<dev.neothaum.tiles.JarNodeBlockEntity> JAR_NODE = register("jar_node", dev.neothaum.tiles.JarNodeBlockEntity::new,
		TVBlocks.NODE_IN_A_JAR);
	public static final BlockEntityType<PedestalBlockEntity> PEDESTAL = register("pedestal", PedestalBlockEntity::new, TVBlocks.ARCANE_PEDESTAL);
	public static final BlockEntityType<InfusionMatrixBlockEntity> INFUSION_MATRIX = register("infusion_matrix", InfusionMatrixBlockEntity::new,
		TVBlocks.RUNIC_MATRIX);
	public static final BlockEntityType<InfusionPillarBlockEntity> INFUSION_PILLAR = register("infusion_pillar", InfusionPillarBlockEntity::new,
		TVBlocks.INFUSION_PILLAR);
	public static final BlockEntityType<TubeBlockEntity> TUBE = register("tube", TubeBlockEntity::new, TVBlocks.ESSENTIA_TUBE, TVBlocks.ESSENTIA_VALVE,
		TVBlocks.FILTERED_ESSENTIA_TUBE, TVBlocks.RESTRICTED_ESSENTIA_TUBE, TVBlocks.DIRECTIONAL_ESSENTIA_TUBE);

	public static final BlockEntityType<dev.neothaum.tiles.HoleBlockEntity> HOLE = register("hole", dev.neothaum.tiles.HoleBlockEntity::new,
		TVBlocks.EMPTY_SPACE);
	public static final BlockEntityType<dev.neothaum.tiles.WardedBlockEntity> WARDED = register("warded", dev.neothaum.tiles.WardedBlockEntity::new,
		TVBlocks.WARDED_BLOCK);

	public static final BlockEntityType<dev.neothaum.tiles.VisRelayBlockEntity> VIS_RELAY = register("vis_relay",
		dev.neothaum.tiles.VisRelayBlockEntity::new, TVBlocks.VIS_RELAY);
	public static final BlockEntityType<dev.neothaum.tiles.WorkbenchChargerBlockEntity> WORKBENCH_CHARGER = register("vis_charge_relay",
		dev.neothaum.tiles.WorkbenchChargerBlockEntity::new, TVBlocks.VIS_CHARGE_RELAY);
	public static final BlockEntityType<dev.neothaum.tiles.EnergizedNodeBlockEntity> ENERGIZED_NODE = register("energized_node",
		dev.neothaum.tiles.EnergizedNodeBlockEntity::new, TVBlocks.ENERGIZED_AURA_NODE);
	public static final BlockEntityType<dev.neothaum.tiles.NodeStabilizerBlockEntity> NODE_STABILIZER = register("node_stabilizer",
		dev.neothaum.tiles.NodeStabilizerBlockEntity::new, TVBlocks.NODE_STABILIZER, TVBlocks.ADVANCED_NODE_STABILIZER);
	public static final BlockEntityType<dev.neothaum.tiles.NodeTransducerBlockEntity> NODE_TRANSDUCER = register("node_transducer",
		dev.neothaum.tiles.NodeTransducerBlockEntity::new, TVBlocks.NODE_TRANSDUCER);
	public static final BlockEntityType<dev.neothaum.tiles.WandPedestalBlockEntity> WAND_PEDESTAL = register("wand_pedestal",
		dev.neothaum.tiles.WandPedestalBlockEntity::new, TVBlocks.WAND_RECHARGE_PEDESTAL);
	public static final BlockEntityType<dev.neothaum.tiles.FocalManipulatorBlockEntity> FOCAL_MANIPULATOR = register("focal_manipulator",
		dev.neothaum.tiles.FocalManipulatorBlockEntity::new, TVBlocks.FOCAL_MANIPULATOR);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneLampBlockEntity> ARCANE_LAMP = register("arcane_lamp",
		dev.neothaum.tiles.ArcaneLampBlockEntity::new, TVBlocks.ARCANE_LAMP);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneLampGrowthBlockEntity> LAMP_OF_GROWTH = register("lamp_of_growth",
		dev.neothaum.tiles.ArcaneLampGrowthBlockEntity::new, TVBlocks.LAMP_OF_GROWTH);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneLampFertilityBlockEntity> LAMP_OF_FERTILITY = register("lamp_of_fertility",
		dev.neothaum.tiles.ArcaneLampFertilityBlockEntity::new, TVBlocks.LAMP_OF_FERTILITY);
	public static final BlockEntityType<dev.neothaum.tiles.OwnedBlockEntity> OWNED = register("owned",
		dev.neothaum.tiles.OwnedBlockEntity::new, TVBlocks.ARCANE_DOOR, TVBlocks.WARDED_GLASS);
	public static final BlockEntityType<dev.neothaum.tiles.ArcanePressurePlateBlockEntity> ARCANE_PRESSURE_PLATE = register("arcane_pressure_plate",
		dev.neothaum.tiles.ArcanePressurePlateBlockEntity::new, TVBlocks.ARCANE_PRESSURE_PLATE);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneEarBlockEntity> ARCANE_EAR = register("arcane_ear",
		dev.neothaum.tiles.ArcaneEarBlockEntity::new, TVBlocks.ARCANE_EAR);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneLevitatorBlockEntity> ARCANE_LEVITATOR = register("arcane_levitator",
		dev.neothaum.tiles.ArcaneLevitatorBlockEntity::new, TVBlocks.ARCANE_LEVITATOR);
	public static final BlockEntityType<dev.neothaum.tiles.WardingStoneBlockEntity> WARDING_STONE = register("warding_stone",
		dev.neothaum.tiles.WardingStoneBlockEntity::new, TVBlocks.PAVING_STONE_OF_WARDING);
	public static final BlockEntityType<dev.neothaum.tiles.MagicMirrorBlockEntity> MAGIC_MIRROR = register("magic_mirror",
		dev.neothaum.tiles.MagicMirrorBlockEntity::new, TVBlocks.MAGIC_MIRROR);
	public static final BlockEntityType<dev.neothaum.tiles.EssentiaMirrorBlockEntity> ESSENTIA_MIRROR = register("essentia_mirror",
		dev.neothaum.tiles.EssentiaMirrorBlockEntity::new, TVBlocks.ESSENTIA_MIRROR);
	public static final BlockEntityType<dev.neothaum.tiles.ThaumatoriumBlockEntity> THAUMATORIUM = register("thaumatorium",
		dev.neothaum.tiles.ThaumatoriumBlockEntity::new, TVBlocks.THAUMATORIUM);
	public static final BlockEntityType<dev.neothaum.tiles.ThaumatoriumTopBlockEntity> THAUMATORIUM_TOP = register("thaumatorium_top",
		dev.neothaum.tiles.ThaumatoriumTopBlockEntity::new, TVBlocks.THAUMATORIUM_TOP);
	public static final BlockEntityType<dev.neothaum.tiles.SpaBlockEntity> SPA = register("spa",
		dev.neothaum.tiles.SpaBlockEntity::new, TVBlocks.ARCANE_SPA);
	public static final BlockEntityType<dev.neothaum.tiles.DeconstructionTableBlockEntity> DECONSTRUCTION_TABLE = register("deconstruction_table",
		dev.neothaum.tiles.DeconstructionTableBlockEntity::new, TVBlocks.DECONSTRUCTION_TABLE);
	public static final BlockEntityType<dev.neothaum.tiles.AdvancedAlchemyFurnaceBlockEntity> ADVANCED_ALCHEMY_FURNACE = register("advanced_alchemy_furnace",
		dev.neothaum.tiles.AdvancedAlchemyFurnaceBlockEntity::new, TVBlocks.ADVANCED_ALCHEMICAL_FURNACE);
	public static final BlockEntityType<dev.neothaum.tiles.AdvancedAlchemyFurnaceNozzleBlockEntity> ADVANCED_ALCHEMY_FURNACE_NOZZLE = register("advanced_alchemy_furnace_nozzle",
		dev.neothaum.tiles.AdvancedAlchemyFurnaceNozzleBlockEntity::new, TVBlocks.ADVANCED_ALCHEMICAL_FURNACE);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneBoreBaseBlockEntity> ARCANE_BORE_BASE = register("arcane_bore_base",
		dev.neothaum.tiles.ArcaneBoreBaseBlockEntity::new, TVBlocks.ARCANE_BORE_BASE);
	public static final BlockEntityType<dev.neothaum.tiles.ArcaneBoreBlockEntity> ARCANE_BORE = register("arcane_bore",
		dev.neothaum.tiles.ArcaneBoreBlockEntity::new, TVBlocks.ARCANE_BORE);
	public static final BlockEntityType<dev.neothaum.tiles.ManaPodBlockEntity> MANA_POD = register("mana_pod",
		dev.neothaum.tiles.ManaPodBlockEntity::new, TVBlocks.MANA_POD);
	public static final BlockEntityType<dev.neothaum.tiles.FluxScrubberBlockEntity> FLUX_SCRUBBER = register("flux_scrubber",
		dev.neothaum.tiles.FluxScrubberBlockEntity::new, TVBlocks.FLUX_SCRUBBER);
	public static final BlockEntityType<dev.neothaum.tiles.BannerBlockEntity> BANNER = register("banner",
		dev.neothaum.tiles.BannerBlockEntity::new, TVBlocks.BANNER);

	// ---- Eldritch Block Entities ----
	public static final BlockEntityType<dev.neothaum.tiles.eldritch.EldritchAltarBlockEntity> ELDRITCH_ALTAR = register("eldritch_altar",
		dev.neothaum.tiles.eldritch.EldritchAltarBlockEntity::new, TVBlocks.ELDRITCH_ALTAR);
	public static final BlockEntityType<dev.neothaum.tiles.eldritch.EldritchCapBlockEntity> ELDRITCH_CAP = register("eldritch_cap",
		dev.neothaum.tiles.eldritch.EldritchCapBlockEntity::new, TVBlocks.ELDRITCH_CAP);
	public static final BlockEntityType<dev.neothaum.tiles.eldritch.EldritchObeliskBlockEntity> ELDRITCH_OBELISK = register("eldritch_obelisk",
		dev.neothaum.tiles.eldritch.EldritchObeliskBlockEntity::new, TVBlocks.ELDRITCH_OBELISK);
	public static final BlockEntityType<dev.neothaum.tiles.eldritch.EldritchPortalBlockEntity> ELDRITCH_PORTAL = register("eldritch_portal",
		dev.neothaum.tiles.eldritch.EldritchPortalBlockEntity::new, TVBlocks.ELDRITCH_PORTAL);
	public static final BlockEntityType<dev.neothaum.tiles.eldritch.EldritchNothingBlockEntity> ELDRITCH_NOTHING = register("eldritch_nothing",
		dev.neothaum.tiles.eldritch.EldritchNothingBlockEntity::new, TVBlocks.ELDRITCH_NOTHING);
	public static final BlockEntityType<dev.neothaum.tiles.eldritch.RunedStoneBlockEntity> RUNED_STONE = register("runed_stone",
		dev.neothaum.tiles.eldritch.RunedStoneBlockEntity::new, TVBlocks.RUNED_STONE);
	public static final BlockEntityType<dev.neothaum.tiles.eldritch.CrustedOpeningBlockEntity> CRUSTED_OPENING = register("crusted_opening",
		dev.neothaum.tiles.eldritch.CrustedOpeningBlockEntity::new, TVBlocks.CRUSTED_OPENING);
	public static final BlockEntityType<dev.neothaum.tiles.eldritch.AncientLockBlockEntity> ANCIENT_LOCK = register("ancient_lock",
		dev.neothaum.tiles.eldritch.AncientLockBlockEntity::new, TVBlocks.ANCIENT_LOCK);
	public static final BlockEntityType<dev.neothaum.tiles.EtherealBloomBlockEntity> ETHEREAL_BLOOM = register("ethereal_bloom",
		dev.neothaum.tiles.EtherealBloomBlockEntity::new, TVBlocks.ETHEREAL_BLOOM);

	private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, NeoThaum.id(name), new BlockEntityType<>(factory, Set.of(blocks)));
	}

	public static void init() {
	}
}
