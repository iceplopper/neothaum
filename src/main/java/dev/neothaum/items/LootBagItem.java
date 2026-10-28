package dev.neothaum.items;

import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Port of ItemLootBag:
 * Common, Uncommon, and Rare treasure bags dropped by champions, eldritch guardians, and cultists.
 * Opening a bag drops 8-12 items from TC4's weighted drop table and plays the coins sound.
 */
public class LootBagItem extends Item {
	private final int tier; // 0 = Common, 1 = Uncommon, 2 = Rare

	public LootBagItem(int tier, Properties props) {
		super(props);
		this.tier = tier;
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level instanceof ServerLevel sl) {
			int q = 8 + level.getRandom().nextInt(5);
			for (int a = 0; a < q; a++) {
				ItemStack loot = generateLoot(this.tier, level.getRandom(), sl);
				if (!loot.isEmpty()) {
					ItemEntity ent = new ItemEntity(sl, player.getX(), player.getY() + 0.5, player.getZ(), loot);
					ent.setDefaultPickUpDelay();
					sl.addFreshEntity(ent);
				}
			}
			level.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.COINS, SoundSource.PLAYERS, 0.75F, 1.0F);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("tc.lootbag").withStyle(ChatFormatting.GRAY));
	}

	private record WeightedLoot(Supplier<ItemStack> supplier, int weight) {
	}

	public static ItemStack generateLoot(int rarity, RandomSource rand, ServerLevel sl) {
		if (rarity > 0 && rand.nextFloat() < 0.025F * rarity) {
			ItemStack gear = genGear(rarity, rand, sl);
			if (gear != null && !gear.isEmpty()) {
				return gear;
			}
		}

		List<WeightedLoot> pool = getPool(rarity);
		int totalWeight = 0;
		for (WeightedLoot wl : pool) {
			totalWeight += wl.weight();
		}
		if (totalWeight <= 0) {
			return new ItemStack(TVItems.GOLD_COIN);
		}

		int roll = rand.nextInt(totalWeight);
		ItemStack result = ItemStack.EMPTY;
		for (WeightedLoot wl : pool) {
			roll -= wl.weight();
			if (roll < 0) {
				result = wl.supplier().get();
				break;
			}
		}
		if (result.isEmpty()) {
			result = new ItemStack(TVItems.GOLD_COIN);
		}

		if (result.is(Items.BOOK)) {
			ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
			int enchantLvl = (int) (5.0F + rarity * 0.75F * rand.nextInt(18));
			return EnchantmentHelper.enchantItem(rand, book, enchantLvl, sl.registryAccess(), Optional.empty());
		}

		return result.copy();
	}

	private static List<WeightedLoot> getPool(int rarity) {
		List<WeightedLoot> p = new ArrayList<>();
		int coinCount = rarity == 0 ? 1 : (rarity == 1 ? 2 : 3);
		int coinWeight = rarity == 0 ? 2500 : (rarity == 1 ? 2250 : 2000);
		p.add(new WeightedLoot(() -> new ItemStack(TVItems.GOLD_COIN, coinCount), coinWeight));

		if (rarity == 2) {
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.PRIMORDIAL_PEARL), 1)); // itemEldritchObject:3
			p.add(new WeightedLoot(() -> new ItemStack(Items.NETHER_STAR), 1));
		}

		p.add(new WeightedLoot(() -> new ItemStack(Items.DIAMOND), rarity == 0 ? 10 : 50));
		p.add(new WeightedLoot(() -> new ItemStack(Items.EMERALD), rarity == 0 ? 15 : 75));
		p.add(new WeightedLoot(() -> new ItemStack(Items.GOLD_INGOT), 100));
		p.add(new WeightedLoot(() -> new ItemStack(Items.ENDER_PEARL), 100));
		p.add(new WeightedLoot(() -> new ItemStack(TVItems.KNOWLEDGE_FRAGMENT), 25));

		// Baubles
		if (rarity == 0) {
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.MUNDANE_RING), 10));
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.MUNDANE_BELT), 10));
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.MUNDANE_AMULET), 10));
		} else {
			int bWeight = rarity == 1 ? 5 : 7;
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.MUNDANE_RING), bWeight));
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.MUNDANE_BELT), bWeight));
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.MUNDANE_AMULET), bWeight));
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.AMULET_OF_VIS_STORAGE), 6));
			p.add(new WeightedLoot(() -> new ItemStack(TVItems.RING_OF_RUNIC_SHIELDING), 5));
		}

		p.add(new WeightedLoot(() -> new ItemStack(Items.EXPERIENCE_BOTTLE), rarity == 0 ? 5 : (rarity == 1 ? 10 : 20)));
		p.add(new WeightedLoot(() -> new ItemStack(Items.GOLDEN_APPLE), rarity == 0 ? 3 : (rarity == 1 ? 6 : 9)));
		p.add(new WeightedLoot(() -> new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), rarity == 0 ? 1 : (rarity == 1 ? 2 : 3)));
		p.add(new WeightedLoot(() -> new ItemStack(Items.BOOK), 10));

		// Potions
		Holder<Potion>[] potions = new Holder[]{
			Potions.SWIFTNESS, Potions.SLOWNESS, Potions.STRENGTH, Potions.HEALING,
			Potions.HARMING, Potions.LEAPING, Potions.REGENERATION, Potions.FIRE_RESISTANCE,
			Potions.WATER_BREATHING, Potions.INVISIBILITY, Potions.NIGHT_VISION, Potions.WEAKNESS, Potions.POISON
		};
		int potWeight = rarity == 0 ? 2 : 3;
		for (Holder<Potion> pot : potions) {
			p.add(new WeightedLoot(() -> PotionContents.createItemStack(Items.POTION, pot), potWeight));
		}

		return p;
	}

	private static ItemStack genGear(int rarity, RandomSource rand, ServerLevel sl) {
		int quality = rand.nextInt(2);
		if (rand.nextFloat() < 0.2F) quality++;
		if (rand.nextFloat() < 0.15F) quality++;
		if (rand.nextFloat() < 0.1F) quality++;
		if (rand.nextFloat() < 0.095F) quality++;
		if (rand.nextFloat() < 0.095F) quality++;
		if (quality > 6) quality = 6;

		int slot = rand.nextInt(5);
		Item item = getGearItemForSlot(slot, quality);
		if (item == null) {
			return ItemStack.EMPTY;
		}

		ItemStack is = new ItemStack(item);
		int maxDmg = is.getMaxDamage();
		if (maxDmg > 6) {
			is.setDamageValue(rand.nextInt(1 + maxDmg / 6));
		}
		if (rand.nextInt(4) < rarity) {
			int enchantLvl = (int) (5.0F + rarity * 0.75F * rand.nextInt(18));
			is = EnchantmentHelper.enchantItem(rand, is, enchantLvl, sl.registryAccess(), Optional.empty());
		}
		return is;
	}

	private static Item getGearItemForSlot(int slot, int quality) {
		return switch (slot) {
			case 4 -> switch (quality) { // Helmet
				case 0 -> Items.LEATHER_HELMET;
				case 1 -> Items.GOLDEN_HELMET;
				case 2 -> Items.CHAINMAIL_HELMET;
				case 3 -> Items.IRON_HELMET;
				case 4 -> TVItems.THAUMIUM_HELMET;
				case 5 -> Items.DIAMOND_HELMET;
				default -> TVItems.VOID_HELMET;
			};
			case 3 -> switch (quality) { // Chestplate
				case 0 -> Items.LEATHER_CHESTPLATE;
				case 1 -> Items.GOLDEN_CHESTPLATE;
				case 2 -> Items.CHAINMAIL_CHESTPLATE;
				case 3 -> Items.IRON_CHESTPLATE;
				case 4 -> TVItems.THAUMIUM_CHESTPLATE;
				case 5 -> Items.DIAMOND_CHESTPLATE;
				default -> TVItems.VOID_CHESTPLATE;
			};
			case 2 -> switch (quality) { // Leggings
				case 0 -> Items.LEATHER_LEGGINGS;
				case 1 -> Items.GOLDEN_LEGGINGS;
				case 2 -> Items.CHAINMAIL_LEGGINGS;
				case 3 -> Items.IRON_LEGGINGS;
				case 4 -> TVItems.THAUMIUM_LEGGINGS;
				case 5 -> Items.DIAMOND_LEGGINGS;
				default -> TVItems.VOID_LEGGINGS;
			};
			case 1 -> switch (quality) { // Boots
				case 0 -> Items.LEATHER_BOOTS;
				case 1 -> Items.GOLDEN_BOOTS;
				case 2 -> Items.CHAINMAIL_BOOTS;
				case 3 -> Items.IRON_BOOTS;
				case 4 -> TVItems.THAUMIUM_BOOTS;
				case 5 -> Items.DIAMOND_BOOTS;
				default -> TVItems.VOID_BOOTS;
			};
			default -> switch (quality) { // Weapon
				case 0 -> Items.IRON_AXE;
				case 1 -> Items.IRON_SWORD;
				case 2 -> Items.GOLDEN_AXE;
				case 3 -> Items.GOLDEN_SWORD;
				case 4 -> TVItems.THAUMIUM_SWORD;
				case 5 -> Items.DIAMOND_SWORD;
				default -> TVItems.VOID_SWORD;
			};
		};
	}
}
