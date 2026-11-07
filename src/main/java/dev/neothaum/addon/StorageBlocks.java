package dev.neothaum.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * NeoThaum Addition: storage blocks TC4 never had, beside its Block of Thaumium. A Block of Quicksilver (nine quicksilver, held in an iron
 * case; four interiors picked at random per block) and a Block of Void Metal (nine void metal ingots). Each crafts back into its nine.
 */
public final class StorageBlocks {
	public static final Block QUICKSILVER_BLOCK = TVBlocks.register("quicksilver_block", Block::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.COLOR_LIGHT_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops().strength(3.0F, 6.0F)
		.sound(SoundType.METAL), false);
	public static final Block VOID_METAL_BLOCK = TVBlocks.register("void_metal_block", Block::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops().strength(5.0F, 12.0F)
		.sound(SoundType.NETHERITE_BLOCK), false);

	public static boolean enabled() {
		return Additions.enabled("storage_blocks");
	}

	/** Loads the class (registering the blocks) from NeoThaum.onInitialize, right after TVBlocks. */
	public static void registerBlocks() {
		QUICKSILVER_BLOCK.getClass();
	}

	/** Called from NeoThaum.onInitialize after TVItems: the items, placed in the tab after the Block of Thaumium. */
	public static void init() {
		int at = TVItems.TAB_ORDER.indexOf(TVBlocks.THAUMIUM_BLOCK.asItem()) + 1;
		for (Block b : new Block[]{QUICKSILVER_BLOCK, VOID_METAL_BLOCK}) {
			var key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(b));
			Item item = Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(b, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
			Additions.hideWith(item, "storage_blocks");
			TVItems.TAB_ORDER.add(at > 0 ? at++ : TVItems.TAB_ORDER.size(), item);
		}
		NeoThaum.LOGGER.debug("NeoThaum storage blocks registered");
	}

	private StorageBlocks() {
	}
}
