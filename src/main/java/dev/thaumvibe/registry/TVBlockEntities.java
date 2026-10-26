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
	public static final BlockEntityType<AlchemyFurnaceBlockEntity> ALCHEMY_FURNACE = register("alchemy_furnace", AlchemyFurnaceBlockEntity::new,
		TVBlocks.ALCHEMICAL_FURNACE);
	public static final BlockEntityType<AlembicBlockEntity> ALEMBIC = register("alembic", AlembicBlockEntity::new, TVBlocks.ARCANE_ALEMBIC);
	public static final BlockEntityType<BufferBlockEntity> BUFFER = register("buffer", BufferBlockEntity::new, TVBlocks.ESSENTIA_BUFFER);
	public static final BlockEntityType<BellowsBlockEntity> BELLOWS = register("bellows", BellowsBlockEntity::new, TVBlocks.ARCANE_BELLOWS);
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

	private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumvibe.id(name), new BlockEntityType<>(factory, Set.of(blocks)));
	}

	public static void init() {
	}
}
