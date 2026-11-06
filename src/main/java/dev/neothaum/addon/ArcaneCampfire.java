package dev.neothaum.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * NeoThaum Addition: the Arcane Campfire, a campfire of greatwood kept burning by nitor, with a violet flame. It does all a campfire does
 * (cooks, sends up smoke, can be put out and lit again), and it gives full light. Under an arcane levitator (or the bottom of a stack of them)
 * it lifts the beam to {@link #LEVITATOR_RANGE} blocks; under a crucible it heats it twice as fast; under the crucible of a thaumatorium it
 * works a little faster.
 */
public final class ArcaneCampfire {
	/** How high a levitator above a lit arcane campfire lifts. */
	public static final int LEVITATOR_RANGE = 256;

	public static final Block CAMPFIRE = TVBlocks.register("arcane_campfire", p -> new CampfireBlock(true, 1, p), BlockBehaviour.Properties.of()
		.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD)
		.lightLevel(s -> s.getValue(CampfireBlock.LIT) ? 15 : 0).noOcclusion().ignitedByLava(), false);
	public static Item item;

	public static boolean enabled() {
		return Additions.enabled("arcane_campfire");
	}

	/** Whether a lit arcane campfire is here, and the addition on. */
	public static boolean litAt(BlockGetter level, BlockPos pos) {
		BlockState s = level.getBlockState(pos);
		return s.is(CAMPFIRE) && s.getValue(CampfireBlock.LIT) && enabled();
	}

	/** Loads the class (registering the block) from NeoThaum.onInitialize, right after TVBlocks. */
	public static void registerBlocks() {
		CAMPFIRE.getClass();
	}

	/** Called from NeoThaum.onInitialize after TVItems: the item, and vanilla's campfire block entity (cooking) for the block. */
	public static void init() {
		var key = ResourceKey.create(Registries.ITEM, NeoThaum.id("arcane_campfire"));
		item = Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(CAMPFIRE, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
		Additions.hideWith(item, "arcane_campfire");
		int at = TVItems.TAB_ORDER.indexOf(TVBlocks.ARCANE_LEVITATOR.asItem()) + 1;
		TVItems.TAB_ORDER.add(at > 0 ? at : TVItems.TAB_ORDER.size(), item);
		BlockEntityTypes.CAMPFIRE.addValidBlock(CAMPFIRE);
	}

	private ArcaneCampfire() {
	}
}
