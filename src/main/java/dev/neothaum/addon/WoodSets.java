package dev.neothaum.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * NeoThaum Addition: the rest of a wood set for greatwood and silverwood, which TC4 left at logs, planks, stairs and slabs. Stripped logs,
 * bark-covered wood, fences and gates, doors and trapdoors, pressure plates and buttons, shelves, and boats with and without a chest. They are
 * always registered (so a world that has them still loads with the addition off), but are only shown and craftable while it is on.
 */
public final class WoodSets {
	/** One wood's set. Greatwood plays as oak; silverwood, a paler and lighter wood, as cherry. */
	public static final class Set1 {
		public final String name;
		public final Block strippedLog, wood, strippedWood, fence, fenceGate, door, trapdoor, pressurePlate, button, shelf;
		public final EntityType<Boat> boat;
		public final EntityType<ChestBoat> chestBoat;
		public Item boatItem, chestBoatItem;

		Set1(String name, MapColor color, BlockSetType set, WoodType type, int light) {
			this.name = name;
			this.strippedLog = block("stripped_" + name + "_log", RotatedPillarBlock::new, log(color, light));
			this.wood = block(name + "_wood", RotatedPillarBlock::new, log(color, light));
			this.strippedWood = block("stripped_" + name + "_wood", RotatedPillarBlock::new, log(color, light));
			this.fence = block(name + "_fence", FenceBlock::new, BlockBehaviour.Properties.of().mapColor(color).forceSolidOn()
				.instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());
			this.fenceGate = block(name + "_fence_gate", p -> new FenceGateBlock(type, p), BlockBehaviour.Properties.of().mapColor(color)
				.forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
			this.door = block(name + "_door", p -> new DoorBlock(set, p), BlockBehaviour.Properties.of().mapColor(color)
				.instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED));
			this.trapdoor = block(name + "_trapdoor", p -> new TrapDoorBlock(set, p), BlockBehaviour.Properties.of().mapColor(color)
				.instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn((s, l, p, e) -> false).ignitedByLava());
			this.pressurePlate = block(name + "_pressure_plate", p -> new PressurePlateBlock(set, p), BlockBehaviour.Properties.of()
				.mapColor(color).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).ignitedByLava()
				.pushReaction(PushReaction.POPPED));
			this.button = block(name + "_button", p -> new ButtonBlock(set, 30, p), BlockBehaviour.Properties.of().noCollision().strength(0.5F)
				.pushReaction(PushReaction.POPPED));
			this.shelf = block(name + "_shelf", ShelfBlock::new, BlockBehaviour.Properties.of().mapColor(color)
				.instrument(NoteBlockInstrument.BASS).sound(SoundType.SHELF).ignitedByLava().strength(2.0F, 3.0F));
			this.boat = entity(name + "_boat", (t, l) -> new Boat(t, l, () -> this.boatItem));
			this.chestBoat = entity(name + "_chest_boat", (t, l) -> new ChestBoat(t, l, () -> this.chestBoatItem));
		}

		public Block[] blocks() {
			return new Block[]{this.strippedLog, this.wood, this.strippedWood, this.fence, this.fenceGate, this.door, this.trapdoor,
				this.pressurePlate, this.button, this.shelf};
		}
	}

	private static final List<Block> BLOCKS = new ArrayList<>();
	/** Every item of the addition, in creative-tab order. */
	public static final Set<Item> ITEMS = new LinkedHashSet<>();

	public static final Set1 GREATWOOD = new Set1("greatwood", MapColor.WOOD, BlockSetType.OAK, WoodType.OAK, 0);
	// TC4's silverwood log gives light 5; its stripped log and wood keep it
	public static final Set1 SILVERWOOD = new Set1("silverwood", MapColor.QUARTZ, BlockSetType.CHERRY, WoodType.CHERRY, 5);

	public static boolean enabled() {
		return Additions.enabled("wood_sets");
	}


	/** Called after the blocks: their items, the boats' items, stripping, burning and the shelves' block entity. */
	public static void init() {
		for (Block b : BLOCKS) {
			var key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(b));
			Item.Properties props = new Item.Properties().setId(key).useBlockDescriptionPrefix();
			// burning as vanilla's wood does; the logs and wood as TC4's magical logs (EventHandlerWorld.getBurnTime: 400)
			if (b instanceof RotatedPillarBlock) {
				props.component(DataComponents.COOKING_FUEL, TVItems.fuel(400));
			} else if (b instanceof DoorBlock) {
				props.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE);
			} else if (b instanceof ButtonBlock) {
				props.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_EXTRA_SMALL);
			} else {
				props.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
			}
			if (b instanceof ShelfBlock) {
				props.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
			}
			Item item = b instanceof DoorBlock ? new DoubleHighBlockItem(b, props) : new BlockItem(b, props);
			ITEMS.add(Registry.register(BuiltInRegistries.ITEM, key, item));
		}
		ITEMS.forEach(i -> Additions.hideWith(i, "wood_sets"));
		for (Set1 s : new Set1[]{GREATWOOD, SILVERWOOD}) {
			s.boatItem = item(s.name + "_boat", p -> new BoatItem(s.boat, p.stacksTo(1).cookingFuel(ContextIntProviders.COOKING_TIME_BOATS)));
			s.chestBoatItem = item(s.name + "_chest_boat",
				p -> new BoatItem(s.chestBoat, p.stacksTo(1).cookingFuel(ContextIntProviders.COOKING_TIME_BOATS)));
			BlockEntityTypes.SHELF.addValidBlock(s.shelf);
			FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
			for (Block b : new Block[]{s.strippedLog, s.wood, s.strippedWood}) {
				fire.add(b, 5, 5);
			}
			for (Block b : new Block[]{s.fence, s.fenceGate}) {
				fire.add(b, 5, 20);
			}
		}
		Additions.hideWith(GREATWOOD.boatItem, "wood_sets");
		Additions.hideWith(GREATWOOD.chestBoatItem, "wood_sets");
		Additions.hideWith(SILVERWOOD.boatItem, "wood_sets");
		Additions.hideWith(SILVERWOOD.chestBoatItem, "wood_sets");
		// in the creative tab after the woods' slabs
		int at = TVItems.TAB_ORDER.indexOf(TVBlocks.SILVERWOOD_SLAB.asItem()) + 1;
		TVItems.TAB_ORDER.addAll(at > 0 ? at : TVItems.TAB_ORDER.size(), ITEMS);
		// an axe strips a log or its wood, as for vanilla wood
		// An axe strips a log or its wood, as for vanilla wood. 26.3 keeps vanilla's stripping in its data-driven axe block transformer, which
		// a mod can only add to by replacing vanilla's, so ours are done here; with the addition off an axe leaves them alone, as in TC4.
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			var held = player.getItemInHand(hand);
			if (!held.is(ItemTags.AXES) || player.isSpectator()) {
				return InteractionResult.PASS;
			}
			var pos = hit.getBlockPos();
			var state = level.getBlockState(pos);
			Block to = stripped(state.getBlock());
			if (to == null) {
				return InteractionResult.PASS;
			}
			if (!enabled()) {
				return InteractionResult.FAIL;
			}
			level.playSound(player, pos, SoundEvents.AXE_STRIP.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
			if (!level.isClientSide()) {
				level.setBlock(pos, to.withPropertiesOf(state), 11);
				if (!player.isCreative()) {
					held.hurtAndBreak(1, player, hand.asEquipmentSlot());
				}
			}
			return InteractionResult.SUCCESS;
		});
	}

	/** What an axe turns a block into, or null: the magical logs and their wood into their stripped forms. */
	public static Block stripped(Block b) {
		if (b == TVBlocks.GREATWOOD_LOG) {
			return GREATWOOD.strippedLog;
		}
		if (b == TVBlocks.SILVERWOOD_LOG) {
			return SILVERWOOD.strippedLog;
		}
		if (b == GREATWOOD.wood) {
			return GREATWOOD.strippedWood;
		}
		if (b == SILVERWOOD.wood) {
			return SILVERWOOD.strippedWood;
		}
		return null;
	}

	private static Block block(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		Block b = TVBlocks.register(name, factory, props, false);
		BLOCKS.add(b);
		return b;
	}

	private static Item item(String name, Function<Item.Properties, Item> factory) {
		var key = ResourceKey.create(Registries.ITEM, NeoThaum.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
		ITEMS.add(item);
		return item;
	}

	private static <T extends AbstractBoat> EntityType<T> entity(String name, EntityType.EntityFactory<T> factory) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, NeoThaum.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder.of(factory, MobCategory.MISC).noLootTable()
			.sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(key));
	}

	private static BlockBehaviour.Properties log(MapColor color, int light) {
		return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0F, 5.0F).sound(SoundType.WOOD)
			.ignitedByLava().lightLevel(s -> light);
	}

	/** Loads the class (and so registers the blocks and boats) from NeoThaum.onInitialize, right after TVBlocks. */
	public static void registerBlocks() {
		NeoThaum.LOGGER.debug("NeoThaum wood sets: {} blocks", BLOCKS.size());
	}

	private WoodSets() {
	}
}
