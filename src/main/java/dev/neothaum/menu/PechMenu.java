package dev.neothaum.menu;

import dev.neothaum.entity.monster.Pech;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVMenus;
import dev.neothaum.registry.TVSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * ContainerPech and InventoryPech: put something the pech values in the left slot and roll the dice; the pech pays out up to four stacks from
 * its pack, its kind's trade list, or (for a rich offer) dungeon loot. Sometimes it decides it has been generous enough and stops trading.
 */
public class PechMenu extends AbstractContainerMenu {
	/** EntityItem thrower "PechDrop": items handed back when the window closes, which pechs won't go and pick up again. */
	public static final String PECH_DROP = "neothaum_pech_drop";

	public final Pech pech;
	private final Container inventory = new SimpleContainer(5);
	private final Player player;

	public PechMenu(int id, Inventory inv, Integer entityId) {
		this(id, inv, inv.player.level().getEntity(entityId) instanceof Pech p ? p : null);
	}

	public PechMenu(int id, Inventory inv, Pech pech) {
		super(TVMenus.PECH, id);
		this.pech = pech;
		this.player = inv.player;
		if (pech != null) {
			pech.trading = true;
		}
		this.addSlot(new Slot(this.inventory, 0, 36, 29));
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				this.addSlot(new Slot(this.inventory, 1 + j + i * 2, 106 + 18 * j, 20 + 18 * i) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return false;
					}
				});
			}
		}
		for (int row = 0; row < 3; row++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + row * 9 + 9, 8 + j * 18, 84 + row * 18));
			}
		}
		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(inv, j, 8 + j * 18, 142));
		}
	}

	/** Whether the dice can be rolled: a valued offer and nothing waiting to be collected. */
	public boolean canTrade() {
		ItemStack offer = this.slots.get(0).getItem();
		if (this.pech == null || offer.isEmpty() || !this.pech.isValued(offer)) {
			return false;
		}
		for (int i = 1; i < 5; i++) {
			if (!this.slots.get(i).getItem().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == 0) {
			this.generateContents();
			return true;
		}
		return super.clickMenuButton(player, id);
	}

	private boolean hasStuffInPack() {
		return this.pech.loot.stream().anyMatch(s -> !s.isEmpty());
	}

	private void give(ItemStack stack) {
		this.moveItemStackTo(stack, 1, 5, false);
	}

	private void generateContents() {
		if (!(this.player.level() instanceof ServerLevel level) || !this.canTrade()) {
			return;
		}
		RandomSource rand = level.getRandom();
		ItemStack offer = this.inventory.getItem(0);
		int value = this.pech.getValue(offer);
		if (rand.nextInt(100) <= value / 2) {
			this.pech.setTamed(false);
			this.pech.updateAINextTick = true;
			this.pech.playSound(TVSounds.PECH_TRADE, 0.4F, 1.0F);
		}
		if (rand.nextInt(5) == 0) {
			value += rand.nextInt(3);
		} else if (rand.nextBoolean()) {
			value -= rand.nextInt(3);
		}
		List<Trade> pos = trades(this.pech.getPechType(), level.registryAccess());
		while (value > 0) {
			int am = Math.min(5, Math.max((value + 1) / 2, rand.nextInt(value) + 1));
			value -= am;
			if (am == 1 && rand.nextBoolean() && this.hasStuffInPack()) {
				List<Integer> full = new ArrayList<>();
				for (int a = 0; a < this.pech.loot.size(); a++) {
					if (!this.pech.loot.get(a).isEmpty()) {
						full.add(a);
					}
				}
				ItemStack s = this.pech.loot.get(full.get(rand.nextInt(full.size())));
				this.give(s.copyWithCount(1));
				s.shrink(1);
			} else if (am >= 4 && rand.nextBoolean()) {
				// ChestGenHooks "dungeonChest": one of the dungeon's own treasures
				var table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
				var items = table.getRandomItems(new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, this.pech.position())
					.create(LootContextParamSets.CHEST));
				if (items.isEmpty()) {
					value += am;
				} else {
					this.give(items.get(rand.nextInt(items.size())));
				}
			} else {
				List<Trade> worth = pos.stream().filter(t -> t.value == am).toList();
				if (!worth.isEmpty()) {
					this.give(worth.get(rand.nextInt(worth.size())).stack.copy());
				}
			}
		}
		this.inventory.removeItem(0, 1);
		this.broadcastChanges();
	}

	private record Trade(int value, ItemStack stack) {
	}

	private static ItemStack potion(Holder<Potion> p) {
		return PotionContents.createItemStack(Items.POTION, p);
	}

	private static ItemStack book(HolderLookup.Provider reg, ResourceKey<Enchantment> e) {
		return net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new EnchantmentInstance(reg.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(e), 1));
	}

	/** EntityPech.tradeInventory, by the pech's kind (forager, mage, stalker). */
	private static List<Trade> trades(int type, HolderLookup.Provider reg) {
		List<Trade> t = new ArrayList<>();
		t.add(new Trade(1, new ItemStack(TVItems.MANA_BEAN)));
		switch (type) {
			case 1 -> {
				for (var shard : new net.minecraft.world.item.Item[]{TVItems.AIR_SHARD, TVItems.FIRE_SHARD, TVItems.WATER_SHARD, TVItems.EARTH_SHARD,
					TVItems.ORDER_SHARD, TVItems.ENTROPY_SHARD}) {
					t.add(new Trade(1, new ItemStack(shard)));
				}
				t.add(new Trade(1, new ItemStack(TVItems.KNOWLEDGE_FRAGMENT)));
				t.add(new Trade(2, new ItemStack(TVItems.KNOWLEDGE_FRAGMENT)));
				t.add(new Trade(2, potion(Potions.REGENERATION)));
				t.add(new Trade(2, potion(Potions.HEALING)));
				t.add(new Trade(3, book(reg, dev.neothaum.items.equipment.TCEnchantments.HASTE)));
				t.add(new Trade(3, new ItemStack(Items.GOLDEN_APPLE)));
				t.add(new Trade(3, potion(Potions.STRONG_REGENERATION)));
				t.add(new Trade(3, potion(Potions.STRONG_HEALING)));
				for (Block b : new Block[]{TVBlocks.AIR_CRYSTAL_CLUSTER, TVBlocks.FIRE_CRYSTAL_CLUSTER, TVBlocks.WATER_CRYSTAL_CLUSTER,
					TVBlocks.EARTH_CRYSTAL_CLUSTER, TVBlocks.ORDER_CRYSTAL_CLUSTER, TVBlocks.ENTROPY_CRYSTAL_CLUSTER, TVBlocks.MIXED_CRYSTAL_CLUSTER}) {
					t.add(new Trade(4, new ItemStack(b)));
				}
				t.add(new Trade(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
				t.add(new Trade(5, book(reg, dev.neothaum.items.equipment.TCEnchantments.REPAIR)));
				t.add(new Trade(5, new ItemStack(TVItems.FOCUS_POUCH)));
				t.add(new Trade(5, new ItemStack(TVItems.FOCUS_PECH)));
				t.add(new Trade(5, new ItemStack(TVItems.VIS_STONE)));
			}
			case 2 -> {
				for (int a = 0; a < 15; a++) {
					t.add(new Trade(1, new ItemStack(TVBlocks.TALLOW_CANDLES[a])));
				}
				t.add(new Trade(2, new ItemStack(Items.GHAST_TEAR)));
				t.add(new Trade(2, potion(Potions.SWIFTNESS)));
				t.add(new Trade(2, potion(Potions.STRENGTH)));
				t.add(new Trade(2, book(reg, Enchantments.POWER)));
				t.add(new Trade(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
				t.add(new Trade(3, new ItemStack(TVItems.KNOWLEDGE_FRAGMENT)));
				t.add(new Trade(3, potion(Potions.LONG_INVISIBILITY)));
				t.add(new Trade(3, potion(Potions.STRONG_REGENERATION)));
				t.add(new Trade(3, new ItemStack(Items.GOLDEN_APPLE)));
				t.add(new Trade(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
				t.add(new Trade(4, new ItemStack(TVItems.THAUMIUM_BOOTS)));
				t.add(new Trade(5, new ItemStack(TVItems.RING_OF_PROTECTION)));
				t.add(new Trade(5, book(reg, Enchantments.FLAME)));
				t.add(new Trade(5, book(reg, Enchantments.INFINITY)));
			}
			default -> {
				t.add(new Trade(1, new ItemStack(TVItems.NATIVE_IRON_CLUSTER)));
				t.add(new Trade(1, new ItemStack(TVItems.NATIVE_GOLD_CLUSTER)));
				t.add(new Trade(1, new ItemStack(TVItems.NATIVE_CINNABAR_CLUSTER)));
				t.add(new Trade(1, new ItemStack(TVItems.NATIVE_COPPER_CLUSTER)));
				t.add(new Trade(2, new ItemStack(Items.BLAZE_ROD)));
				t.add(new Trade(2, new ItemStack(TVBlocks.GREATWOOD_SAPLING)));
				t.add(new Trade(2, potion(Potions.STRENGTH)));
				t.add(new Trade(2, potion(Potions.SWIFTNESS)));
				t.add(new Trade(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
				t.add(new Trade(3, new ItemStack(TVItems.KNOWLEDGE_FRAGMENT)));
				t.add(new Trade(3, new ItemStack(Items.GOLDEN_APPLE)));
				t.add(new Trade(3, potion(Potions.LONG_STRENGTH)));
				t.add(new Trade(3, potion(Potions.LONG_NIGHT_VISION)));
				t.add(new Trade(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
				t.add(new Trade(4, new ItemStack(TVItems.THAUMIUM_PICKAXE)));
				t.add(new Trade(5, new ItemStack(TVBlocks.SILVERWOOD_SAPLING)));
				t.add(new Trade(5, new ItemStack(TVBlocks.SILVERWOOD_SAPLING)));
			}
		}
		return t;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.pech != null && this.pech.isAlive() && this.pech.isTamed() && player.distanceToSqr(this.pech) < 64.0;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			if (index < 5) {
				if (!this.moveItemStackTo(stack, 5, 41, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(stack, 0, 1, true)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (stack.getCount() == result.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stack);
		}
		return result;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (this.pech != null) {
			this.pech.trading = false;
		}
		if (!player.level().isClientSide()) {
			for (int a = 0; a < 5; a++) {
				ItemStack s = this.inventory.removeItemNoUpdate(a);
				if (!s.isEmpty()) {
					ItemEntity ei = player.drop(s, false, net.minecraft.util.Prediction.SERVER_ONLY);
					if (ei != null) {
						ei.addTag(PECH_DROP);
					}
				}
			}
		}
	}
}
