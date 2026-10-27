package dev.thaumvibe.registry;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.tiles.AlchemyFurnaceBlockEntity;
import dev.thaumvibe.tiles.AlembicBlockEntity;
import dev.thaumvibe.tiles.ArcaneWorktableBlockEntity;
import dev.thaumvibe.tiles.BellowsBlockEntity;
import dev.thaumvibe.tiles.BrainJarBlockEntity;
import dev.thaumvibe.tiles.BufferBlockEntity;
import dev.thaumvibe.tiles.CrucibleBlockEntity;
import dev.thaumvibe.tiles.CrystalBlockEntity;
import dev.thaumvibe.tiles.InfusionMatrixBlockEntity;
import dev.thaumvibe.tiles.InfusionPillarBlockEntity;
import dev.thaumvibe.tiles.JarBlockEntity;
import dev.thaumvibe.tiles.PedestalBlockEntity;
import dev.thaumvibe.tiles.TubeBlockEntity;
import dev.thaumvibe.tiles.NodeBlockEntity;
import dev.thaumvibe.tiles.ResearchTableBlockEntity;
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
		TVBlocks.ENTROPY_CRYSTAL_CLUSTER, TVBlocks.MIXED_CRYSTAL_CLUSTER);
	public static final BlockEntityType<JarBlockEntity> JAR = register("jar", JarBlockEntity::new, TVBlocks.WARDED_JAR, TVBlocks.VOID_JAR);
	public static final BlockEntityType<dev.thaumvibe.tiles.ArcaneFurnaceBlockEntity> ARCANE_FURNACE = register("arcane_furnace", dev.thaumvibe.tiles.ArcaneFurnaceBlockEntity::new, TVBlocks.ARCANE_FURNACE);
	public static final BlockEntityType<dev.thaumvibe.tiles.ArcaneFurnaceNozzleBlockEntity> ARCANE_FURNACE_NOZZLE = register("arcane_furnace_nozzle", dev.thaumvibe.tiles.ArcaneFurnaceNozzleBlockEntity::new, TVBlocks.ARCANE_FURNACE);
	public static final BlockEntityType<AlchemyFurnaceBlockEntity> ALCHEMY_FURNACE = register("alchemy_furnace", AlchemyFurnaceBlockEntity::new,
		TVBlocks.ALCHEMICAL_FURNACE);
	public static final BlockEntityType<AlembicBlockEntity> ALEMBIC = register("alembic", AlembicBlockEntity::new, TVBlocks.ARCANE_ALEMBIC);
	public static final BlockEntityType<BufferBlockEntity> BUFFER = register("buffer", BufferBlockEntity::new, TVBlocks.ESSENTIA_BUFFER);
	public static final BlockEntityType<BellowsBlockEntity> BELLOWS = register("bellows", BellowsBlockEntity::new, TVBlocks.ARCANE_BELLOWS);
	public static final BlockEntityType<dev.thaumvibe.tiles.HungryChestBlockEntity> HUNGRY_CHEST = register("hungry_chest",
		dev.thaumvibe.tiles.HungryChestBlockEntity::new, TVBlocks.HUNGRY_CHEST);
	public static final BlockEntityType<dev.thaumvibe.tiles.ItemGrateBlockEntity> ITEM_GRATE = register("item_grate",
		dev.thaumvibe.tiles.ItemGrateBlockEntity::new, TVBlocks.ITEM_GRATE);
	public static final BlockEntityType<dev.thaumvibe.tiles.CentrifugeBlockEntity> CENTRIFUGE = register("centrifuge",
		dev.thaumvibe.tiles.CentrifugeBlockEntity::new, TVBlocks.ALCHEMICAL_CENTRIFUGE);
	public static final BlockEntityType<dev.thaumvibe.tiles.CrystallizerBlockEntity> CRYSTALLIZER = register("essentia_crystallizer",
		dev.thaumvibe.tiles.CrystallizerBlockEntity::new, TVBlocks.ESSENTIA_CRYSTALLIZER);
	public static final BlockEntityType<dev.thaumvibe.tiles.ReservoirBlockEntity> RESERVOIR = register("essentia_reservoir",
		dev.thaumvibe.tiles.ReservoirBlockEntity::new, TVBlocks.ESSENTIA_RESERVOIR);
	public static final BlockEntityType<BrainJarBlockEntity> BRAIN_JAR = register("brain_jar", BrainJarBlockEntity::new, TVBlocks.BRAIN_IN_A_JAR);
	public static final BlockEntityType<dev.thaumvibe.tiles.JarNodeBlockEntity> JAR_NODE = register("jar_node", dev.thaumvibe.tiles.JarNodeBlockEntity::new,
		TVBlocks.NODE_IN_A_JAR);
	public static final BlockEntityType<PedestalBlockEntity> PEDESTAL = register("pedestal", PedestalBlockEntity::new, TVBlocks.ARCANE_PEDESTAL);
	public static final BlockEntityType<InfusionMatrixBlockEntity> INFUSION_MATRIX = register("infusion_matrix", InfusionMatrixBlockEntity::new,
		TVBlocks.RUNIC_MATRIX);
	public static final BlockEntityType<InfusionPillarBlockEntity> INFUSION_PILLAR = register("infusion_pillar", InfusionPillarBlockEntity::new,
		TVBlocks.INFUSION_PILLAR);
	public static final BlockEntityType<TubeBlockEntity> TUBE = register("tube", TubeBlockEntity::new, TVBlocks.ESSENTIA_TUBE, TVBlocks.ESSENTIA_VALVE,
		TVBlocks.FILTERED_ESSENTIA_TUBE, TVBlocks.RESTRICTED_ESSENTIA_TUBE, TVBlocks.DIRECTIONAL_ESSENTIA_TUBE);

	public static final BlockEntityType<dev.thaumvibe.tiles.HoleBlockEntity> HOLE = register("hole", dev.thaumvibe.tiles.HoleBlockEntity::new,
		TVBlocks.EMPTY_SPACE);
	public static final BlockEntityType<dev.thaumvibe.tiles.WardedBlockEntity> WARDED = register("warded", dev.thaumvibe.tiles.WardedBlockEntity::new,
		TVBlocks.WARDED_BLOCK);

	public static final BlockEntityType<dev.thaumvibe.tiles.VisRelayBlockEntity> VIS_RELAY = register("vis_relay",
		dev.thaumvibe.tiles.VisRelayBlockEntity::new, TVBlocks.VIS_RELAY);
	public static final BlockEntityType<dev.thaumvibe.tiles.WorkbenchChargerBlockEntity> WORKBENCH_CHARGER = register("vis_charge_relay",
		dev.thaumvibe.tiles.WorkbenchChargerBlockEntity::new, TVBlocks.VIS_CHARGE_RELAY);
	public static final BlockEntityType<dev.thaumvibe.tiles.EnergizedNodeBlockEntity> ENERGIZED_NODE = register("energized_node",
		dev.thaumvibe.tiles.EnergizedNodeBlockEntity::new, TVBlocks.ENERGIZED_AURA_NODE);
	public static final BlockEntityType<dev.thaumvibe.tiles.NodeStabilizerBlockEntity> NODE_STABILIZER = register("node_stabilizer",
		dev.thaumvibe.tiles.NodeStabilizerBlockEntity::new, TVBlocks.NODE_STABILIZER, TVBlocks.ADVANCED_NODE_STABILIZER);
	public static final BlockEntityType<dev.thaumvibe.tiles.NodeTransducerBlockEntity> NODE_TRANSDUCER = register("node_transducer",
		dev.thaumvibe.tiles.NodeTransducerBlockEntity::new, TVBlocks.NODE_TRANSDUCER);
	public static final BlockEntityType<dev.thaumvibe.tiles.WandPedestalBlockEntity> WAND_PEDESTAL = register("wand_pedestal",
		dev.thaumvibe.tiles.WandPedestalBlockEntity::new, TVBlocks.WAND_RECHARGE_PEDESTAL);
	public static final BlockEntityType<dev.thaumvibe.tiles.FocalManipulatorBlockEntity> FOCAL_MANIPULATOR = register("focal_manipulator",
		dev.thaumvibe.tiles.FocalManipulatorBlockEntity::new, TVBlocks.FOCAL_MANIPULATOR);
	public static final BlockEntityType<dev.thaumvibe.tiles.ArcaneLampBlockEntity> ARCANE_LAMP = register("arcane_lamp",
		dev.thaumvibe.tiles.ArcaneLampBlockEntity::new, TVBlocks.ARCANE_LAMP);
	public static final BlockEntityType<dev.thaumvibe.tiles.ArcaneLampGrowthBlockEntity> LAMP_OF_GROWTH = register("lamp_of_growth",
		dev.thaumvibe.tiles.ArcaneLampGrowthBlockEntity::new, TVBlocks.LAMP_OF_GROWTH);
	public static final BlockEntityType<dev.thaumvibe.tiles.ArcaneLampFertilityBlockEntity> LAMP_OF_FERTILITY = register("lamp_of_fertility",
		dev.thaumvibe.tiles.ArcaneLampFertilityBlockEntity::new, TVBlocks.LAMP_OF_FERTILITY);
	public static final BlockEntityType<dev.thaumvibe.tiles.OwnedBlockEntity> OWNED = register("owned",
		dev.thaumvibe.tiles.OwnedBlockEntity::new, TVBlocks.ARCANE_DOOR, TVBlocks.WARDED_GLASS);
	public static final BlockEntityType<dev.thaumvibe.tiles.ArcanePressurePlateBlockEntity> ARCANE_PRESSURE_PLATE = register("arcane_pressure_plate",
		dev.thaumvibe.tiles.ArcanePressurePlateBlockEntity::new, TVBlocks.ARCANE_PRESSURE_PLATE);
	public static final BlockEntityType<dev.thaumvibe.tiles.ArcaneEarBlockEntity> ARCANE_EAR = register("arcane_ear",
		dev.thaumvibe.tiles.ArcaneEarBlockEntity::new, TVBlocks.ARCANE_EAR);
	public static final BlockEntityType<dev.thaumvibe.tiles.ArcaneLevitatorBlockEntity> ARCANE_LEVITATOR = register("arcane_levitator",
		dev.thaumvibe.tiles.ArcaneLevitatorBlockEntity::new, TVBlocks.ARCANE_LEVITATOR);
	public static final BlockEntityType<dev.thaumvibe.tiles.WardingStoneBlockEntity> WARDING_STONE = register("warding_stone",
		dev.thaumvibe.tiles.WardingStoneBlockEntity::new, TVBlocks.PAVING_STONE_OF_WARDING);
	public static final BlockEntityType<dev.thaumvibe.tiles.MagicMirrorBlockEntity> MAGIC_MIRROR = register("magic_mirror",
		dev.thaumvibe.tiles.MagicMirrorBlockEntity::new, TVBlocks.MAGIC_MIRROR);
	public static final BlockEntityType<dev.thaumvibe.tiles.EssentiaMirrorBlockEntity> ESSENTIA_MIRROR = register("essentia_mirror",
		dev.thaumvibe.tiles.EssentiaMirrorBlockEntity::new, TVBlocks.ESSENTIA_MIRROR);
	public static final BlockEntityType<dev.thaumvibe.tiles.ThaumatoriumBlockEntity> THAUMATORIUM = register("thaumatorium",
		dev.thaumvibe.tiles.ThaumatoriumBlockEntity::new, TVBlocks.THAUMATORIUM);
	public static final BlockEntityType<dev.thaumvibe.tiles.ThaumatoriumTopBlockEntity> THAUMATORIUM_TOP = register("thaumatorium_top",
		dev.thaumvibe.tiles.ThaumatoriumTopBlockEntity::new, TVBlocks.THAUMATORIUM_TOP);
	public static final BlockEntityType<dev.thaumvibe.tiles.SpaBlockEntity> SPA = register("spa",
		dev.thaumvibe.tiles.SpaBlockEntity::new, TVBlocks.ARCANE_SPA);
	public static final BlockEntityType<dev.thaumvibe.tiles.DeconstructionTableBlockEntity> DECONSTRUCTION_TABLE = register("deconstruction_table",
		dev.thaumvibe.tiles.DeconstructionTableBlockEntity::new, TVBlocks.DECONSTRUCTION_TABLE);

	private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumvibe.id(name), new BlockEntityType<>(factory, Set.of(blocks)));
	}

	public static void init() {
	}
}
