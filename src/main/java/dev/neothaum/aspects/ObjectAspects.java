package dev.neothaum.aspects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.crafting.ArcaneRecipe;
import dev.neothaum.crafting.CrucibleRecipe;
import dev.neothaum.items.WandItem;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVComponents;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;

/**
 * Port of ThaumcraftApi.objectTags + ThaumcraftCraftingManager.getObjectTags/getBonusTags/generateTags.
 * The server derives aspects for every item from the registered tags and recipes, then syncs the table to clients.
 */
public final class ObjectAspects {
	private record ObjEntry(String item, String tag, AspectList aspects, boolean complex) {
	}

	private record EntityEntry(Identifier entity, AspectList aspects, Map<String, Object> nbt) {
	}

	private static final List<ObjEntry> RAW_OBJECTS = new ArrayList<>();
	private static final List<EntityEntry> ENTITIES = new ArrayList<>();
	/** Explicit object tags (ThaumcraftApi.objectTags). */
	private static final Map<Item, AspectList> registered = new HashMap<>();
	/** registerComplexObjectTag additions applied on top of recipe-derived tags. */
	private static final Map<Item, AspectList> complex = new HashMap<>();
	/** Final table (server computed, client synced). */
	private static volatile Map<Item, AspectList> table = Map.of();

	public static void init() {
		load();
		ServerLifecycleEvents.SERVER_STARTED.register(ObjectAspects::rebuild);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, rm, ok) -> {
			rebuild(server);
			server.getPlayerList().getPlayers().forEach(TVNetwork::sendObjectAspects);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> TVNetwork.sendObjectAspects(handler.player));
	}

	/**
	 * aspects.json is ConfigAspects converted 1:1; aspects_modern.json covers content added to Minecraft after 1.7.10, in the same style
	 * (from the reviewed spreadsheet); aspects_additions.json covers NeoThaum's own new items, such as the deepslate ores.
	 */
	private static void load() {
		load("aspects.json");
		load("aspects_modern.json");
		load("aspects_additions.json");
	}

	private static void load(String file) {
		try (InputStream in = ObjectAspects.class.getResourceAsStream("/data/neothaum/neothaum/" + file)) {
			JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (JsonElement e : root.getAsJsonArray("objects")) {
				JsonObject o = e.getAsJsonObject();
				RAW_OBJECTS.add(new ObjEntry(o.has("item") ? o.get("item").getAsString() : null, o.has("tag") ? o.get("tag").getAsString() : null,
					parse(o.getAsJsonObject("aspects")), o.has("complex") && o.get("complex").getAsBoolean()));
			}
			for (JsonElement e : root.getAsJsonArray("entities")) {
				JsonObject o = e.getAsJsonObject();
				Map<String, Object> nbt = new HashMap<>();
				if (o.has("nbt")) {
					o.getAsJsonObject("nbt").entrySet().forEach(n -> nbt.put(n.getKey(), n.getValue().getAsString()));
				}
				ENTITIES.add(new EntityEntry(Identifier.parse(o.get("entity").getAsString()), parse(o.getAsJsonObject("aspects")), nbt));
			}
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to load " + file, ex);
		}
	}

	private static AspectList parse(JsonObject o) {
		AspectList l = new AspectList();
		for (var e : o.entrySet()) {
			Aspect a = Aspect.getAspect(e.getKey());
			if (a != null) {
				l.add(a, e.getValue().getAsInt());
			}
		}
		return l;
	}

	// ----------------------------------------------------------------------------- server computation

	private static MinecraftServer server;
	private static final Map<Item, AspectList> generated = new HashMap<>();

	private static void rebuild(MinecraftServer srv) {
		server = srv;
		registered.clear();
		complex.clear();
		generated.clear();
		for (ObjEntry o : RAW_OBJECTS) {
			for (Item item : resolve(o)) {
				if (o.complex()) {
					complex.put(item, o.aspects());
				} else {
					registered.put(item, o.aspects());
				}
			}
		}
		Map<Item, AspectList> out = new LinkedHashMap<>();
		for (Item item : BuiltInRegistries.ITEM) {
			if (item == Items.AIR) {
				continue;
			}
			AspectList l = baseTags(item);
			if (l != null && l.size() > 0) {
				out.put(item, l);
			}
		}
		table = out;
		NeoThaum.LOGGER.info("Computed aspects for {} items", out.size());
	}

	private static List<Item> resolve(ObjEntry o) {
		List<Item> items = new ArrayList<>();
		if (o.item() != null) {
			BuiltInRegistries.ITEM.getOptional(Identifier.parse(o.item())).ifPresent(items::add);
		} else if (o.tag() != null) {
			TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(o.tag()));
			BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(h -> items.add(h.value()));
			if (items.isEmpty()) {
				// block tags (#minecraft:logs etc.) for blocks whose items aren't tagged
				var btag = TagKey.create(Registries.BLOCK, Identifier.parse(o.tag()));
				BuiltInRegistries.BLOCK.getTagOrEmpty(btag).forEach(h -> {
					Item it = h.value().asItem();
					if (it != Items.AIR) {
						items.add(it);
					}
				});
			}
		}
		return items;
	}

	private static AspectList baseTags(Item item) {
		AspectList tmp = registered.get(item);
		if (tmp == null) {
			tmp = generateTags(item, new HashSet<>());
		}
		return capAspects(tmp, 64);
	}

	/** ThaumcraftCraftingManager.generateTags with history-based cycle protection. */
	private static AspectList generateTags(Item item, Set<Item> history) {
		if (registered.containsKey(item)) {
			return registered.get(item);
		}
		if (generated.containsKey(item)) {
			return generated.get(item);
		}
		if (history.contains(item) || history.size() >= 100) {
			return null;
		}
		history.add(item);
		AspectList ret = generateTagsFromRecipes(item, history);
		if (ret == null || ret.size() == 0) {
			// NeoThaum Addition: another mod's item with no recipe takes aspects from its tags
			AspectList modded = dev.neothaum.addon.ModdedAspects.forItem(item);
			if (modded != null) {
				ret = modded;
			}
		}
		AspectList cx = complex.get(item);
		if (cx != null) {
			ret = ret == null ? cx.copy() : ret.copy().add(cx);
		}
		ret = capAspects(ret, 64);
		generated.put(item, ret);
		history.remove(item);
		return ret;
	}

	private static AspectList generateTagsFromRecipes(Item item, Set<Item> history) {
		if (server == null) {
			return null;
		}
		AspectList ret = generateTagsFromCrucibleRecipes(item, history);
		if (ret != null) {
			return ret;
		}
		ret = generateTagsFromArcaneRecipes(item, history);
		if (ret != null) {
			return ret;
		}
		ret = generateTagsFromInfusionRecipes(item, history);
		if (ret != null) {
			return ret;
		}
		return generateTagsFromCraftingRecipes(item, history);
	}

	private static AspectList generateTagsFromCrucibleRecipes(Item item, Set<Item> history) {
		for (RecipeHolder<?> h : server.getRecipeManager().getRecipes()) {
			if (h.value() instanceof CrucibleRecipe cr && cr.result().item().value() == item) {
				int ss = cr.result().count();
				List<Holder<Item>> cats = cr.catalyst().items().toList();
				if (cats.isEmpty()) {
					continue;
				}
				AspectList ot2 = generateTags(cats.get(0).value(), history);
				AspectList out = new AspectList();
				if (ot2 != null) {
					out.add(ot2);
				}
				for (Aspect tt : cr.aspects().getAspects()) {
					out.add(tt, (int) (Math.sqrt(cr.aspects().getAmount(tt)) / ss));
				}
				return out.cull();
			}
		}
		return null;
	}

	private static AspectList generateTagsFromArcaneRecipes(Item item, Set<Item> history) {
		AspectList ret = null;
		for (RecipeHolder<?> h : server.getRecipeManager().getRecipes()) {
			if (h.value() instanceof ArcaneRecipe ar && ar.result().item().value() == item) {
				List<Item> ingredients = new ArrayList<>();
				for (Ingredient ing : ar.ingredientList()) {
					Item it = firstWithTags(ing, history);
					if (it != null) {
						ingredients.add(it);
					}
				}
				AspectList ph = getAspectsFromIngredients(ingredients, ar.result().count(), history);
				for (Aspect a : ar.aspects().getAspects()) {
					ph.add(a, (int) (Math.sqrt(ar.aspects().getAmount(a)) / ar.result().count()));
				}
				ret = ph.cull();
			}
		}
		return ret;
	}

	/** The central item and components, plus the square root of each essentia in the recipe. */
	private static AspectList generateTagsFromInfusionRecipes(Item item, Set<Item> history) {
		for (RecipeHolder<?> h : server.getRecipeManager().getRecipes()) {
			if (h.value() instanceof dev.neothaum.crafting.InfusionRecipe ir && ir.result().item().value() == item) {
				List<Item> ingredients = new ArrayList<>();
				Item central = firstWithTags(ir.central(), history);
				if (central != null) {
					ingredients.add(central);
				}
				for (Ingredient ing : ir.components()) {
					Item it = firstWithTags(ing, history);
					if (it != null) {
						ingredients.add(it);
					}
				}
				int count = ir.result().count();
				AspectList out = getAspectsFromIngredients(ingredients, count, history);
				for (Aspect tt : ir.aspects().getAspects()) {
					out.add(tt, (int) (Math.sqrt(ir.aspects().getAmount(tt)) / count));
				}
				for (Aspect as : out.getAspects()) {
					if (out.getAmount(as) <= 0) {
						out.remove(as);
					}
				}
				return out;
			}
		}
		return null;
	}

	private static AspectList generateTagsFromCraftingRecipes(Item item, Set<Item> history) {
		AspectList ret = null;
		int value = Integer.MAX_VALUE;
		for (RecipeHolder<?> h : server.getRecipeManager().getRecipes()) {
			if (!(h.value() instanceof CraftingRecipe recipe)) {
				continue;
			}
			ItemStack out = resultOf(recipe);
			if (out.isEmpty() || out.getItem() != item) {
				continue;
			}
			List<Item> ingredients = new ArrayList<>();
			for (Ingredient ing : recipe.placementInfo().ingredients()) {
				Item it = firstWithTags(ing, history);
				if (it != null) {
					ingredients.add(it);
				}
			}
			AspectList ph = getAspectsFromIngredients(ingredients, out.getCount(), history).cull();
			if (ph.visSize() < value && ph.visSize() > 0) {
				ret = ph;
				value = ph.visSize();
			}
		}
		return ret;
	}

	public static ItemStack resultOf(Recipe<?> recipe) {
		for (RecipeDisplay d : recipe.display()) {
			SlotDisplay r = d.result();
			if (r instanceof SlotDisplay.ItemStackSlotDisplay s) {
				return s.stack().create();
			}
			if (r instanceof SlotDisplay.ItemSlotDisplay s) {
				return new ItemStack(s.item());
			}
		}
		return ItemStack.EMPTY;
	}

	private static Item firstWithTags(Ingredient ing, Set<Item> history) {
		List<Holder<Item>> opts = ing.items().toList();
		if (opts.size() == 1) {
			return opts.get(0).value();
		}
		for (Holder<Item> h : opts) {
			AspectList obj = generateTags(h.value(), history);
			if (obj != null && obj.size() > 0) {
				return h.value();
			}
		}
		return opts.isEmpty() ? null : opts.get(0).value();
	}

	private static AspectList getAspectsFromIngredients(List<Item> ingredients, int outCount, Set<Item> history) {
		AspectList out = new AspectList();
		AspectList mid = new AspectList();
		for (Item is : ingredients) {
			AspectList obj = generateTags(is, history);
			var remT = is.getCraftingRemainder();
			ItemStack rem = remT == null ? ItemStack.EMPTY : remT.create();
			if (!rem.isEmpty() && rem.getItem() != is) {
				AspectList objC = generateTags(rem.getItem(), history);
				if (objC != null) {
					for (Aspect a : objC.getAspects()) {
						out.reduce(a, objC.getAmount(a));
					}
				}
			}
			if (obj != null) {
				mid.add(obj);
			}
		}
		for (Aspect as : mid.getAspects()) {
			out.add(as, (int) (mid.getAmount(as) * 0.75F / Math.max(1, outCount)));
		}
		return out.cull();
	}

	private static AspectList capAspects(AspectList src, int amount) {
		if (src == null) {
			return null;
		}
		AspectList out = new AspectList();
		for (Aspect a : src.getAspects()) {
			out.merge(a, Math.min(amount, src.getAmount(a)));
		}
		return out;
	}

	// ----------------------------------------------------------------------------- lookups (both sides)

	public static Map<Item, AspectList> table() {
		return table;
	}

	public static void setClientTable(Map<Item, AspectList> t) {
		table = t;
	}

	/** getObjectTags + getBonusTags, culled to 6 aspects like ThaumcraftApiHelper.cullTags. */
	public static AspectList getAspects(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		AspectList base = table.get(stack.getItem());
		AspectList tmp = base == null ? new AspectList() : base.copy();
		if (stack.getItem() instanceof WandItem) {
			int cc = WandItem.getRod(stack).getCraftCost() + WandItem.getCap(stack).getCraftCost();
			tmp.merge(Aspect.MAGIC, cc / 2);
			tmp.merge(Aspect.TOOL, cc / 3);
		}
		PotionContents pc = stack.get(DataComponents.POTION_CONTENTS);
		if (pc != null && (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION))) {
			tmp.merge(Aspect.WATER, 1);
			if (!stack.is(Items.POTION)) {
				tmp.merge(Aspect.ENTROPY, 2);
			}
			for (MobEffectInstance e : pc.getAllEffects()) {
				int lv = e.getAmplifier() + 1;
				tmp.merge(Aspect.MAGIC, lv * 2);
				Holder<MobEffect> eff = e.getEffect();
				if (eff.is(MobEffects.BLINDNESS)) tmp.merge(Aspect.DARKNESS, lv * 3);
				else if (eff.is(MobEffects.NAUSEA)) tmp.merge(Aspect.ELDRITCH, lv * 3);
				else if (eff.is(MobEffects.STRENGTH)) tmp.merge(Aspect.WEAPON, lv * 3);
				else if (eff.is(MobEffects.MINING_FATIGUE)) tmp.merge(Aspect.TRAP, lv * 3);
				else if (eff.is(MobEffects.HASTE)) tmp.merge(Aspect.TOOL, lv * 3);
				else if (eff.is(MobEffects.FIRE_RESISTANCE)) {
					tmp.merge(Aspect.ARMOR, lv);
					tmp.merge(Aspect.FIRE, lv * 2);
				} else if (eff.is(MobEffects.INSTANT_DAMAGE)) tmp.merge(Aspect.DEATH, lv * 3);
				else if (eff.is(MobEffects.INSTANT_HEALTH)) tmp.merge(Aspect.HEAL, lv * 3);
				else if (eff.is(MobEffects.HUNGER)) tmp.merge(Aspect.DEATH, lv * 3);
				else if (eff.is(MobEffects.INVISIBILITY)) tmp.merge(Aspect.SENSES, lv * 3);
				else if (eff.is(MobEffects.JUMP_BOOST)) tmp.merge(Aspect.FLIGHT, lv * 3);
				else if (eff.is(MobEffects.SLOWNESS)) tmp.merge(Aspect.TRAP, lv * 3);
				else if (eff.is(MobEffects.SPEED)) tmp.merge(Aspect.MOTION, lv * 3);
				else if (eff.is(MobEffects.NIGHT_VISION)) tmp.merge(Aspect.SENSES, lv * 3);
				else if (eff.is(MobEffects.POISON)) tmp.merge(Aspect.POISON, lv * 3);
				else if (eff.is(MobEffects.REGENERATION)) tmp.merge(Aspect.HEAL, lv * 3);
				else if (eff.is(MobEffects.RESISTANCE)) tmp.merge(Aspect.ARMOR, lv * 3);
				else if (eff.is(MobEffects.WATER_BREATHING)) tmp.merge(Aspect.AIR, lv * 3);
				else if (eff.is(MobEffects.WEAKNESS)) tmp.merge(Aspect.DEATH, lv * 3);
			}
		}
		tmp = capAspects(tmp, 64);
		return bonusTags(stack, tmp);
	}

	private static AspectList bonusTags(ItemStack itemstack, AspectList sourcetags) {
		AspectList tmp = new AspectList();
		AspectList ess = itemstack.get(TVComponents.ASPECTS);
		if (ess != null) {
			tmp.add(ess.copy().cull());
		}
		if (sourcetags != null) {
			tmp.add(sourcetags);
		}
		ItemAttributeModifiers mods = itemstack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		double armor = 0;
		double damage = 0;
		for (ItemAttributeModifiers.Entry e : mods.modifiers()) {
			if (e.attribute().is(Attributes.ARMOR)) {
				armor += e.modifier().amount();
			} else if (e.attribute().is(Attributes.ATTACK_DAMAGE)) {
				damage += e.modifier().amount();
			}
		}
		var tool = itemstack.get(DataComponents.TOOL);
		if (armor > 0) {
			tmp.merge(Aspect.ARMOR, (int) armor);
		} else if (itemstack.is(net.minecraft.tags.ItemTags.SWORDS) && damage + 1.0 > 0.0) {
			tmp.merge(Aspect.WEAPON, (int) (damage + 1.0));
		} else if (itemstack.is(Items.BOW) || itemstack.is(Items.CROSSBOW)) {
			tmp.merge(Aspect.WEAPON, 3).merge(Aspect.FLIGHT, 1);
		} else if (itemstack.is(net.minecraft.tags.ItemTags.PICKAXES) && tool != null) {
			tmp.merge(Aspect.MINE, harvestLevel(itemstack) + 1);
		} else if ((itemstack.is(net.minecraft.tags.ItemTags.AXES) || itemstack.is(net.minecraft.tags.ItemTags.SHOVELS)) && tool != null) {
			tmp.merge(Aspect.TOOL, harvestLevel(itemstack) + 1);
		} else if (itemstack.is(Items.SHEARS) || itemstack.is(net.minecraft.tags.ItemTags.HOES)) {
			int dur = itemstack.getMaxDamage();
			tmp.merge(Aspect.HARVEST, dur <= 59 ? 1 : dur <= 131 ? 2 : dur <= 250 ? 3 : 4);
		}
		ItemEnchantments ench = itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		ItemEnchantments stored = itemstack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		int total = 0;
		for (ItemEnchantments e : List.of(ench, stored)) {
			for (var entry : e.entrySet()) {
				int lvl = entry.getIntValue();
				Aspect a = enchantAspect(entry.getKey());
				if (a != null) {
					tmp.merge(a, lvl);
				}
				total += lvl;
			}
		}
		if (total > 0) {
			tmp.merge(Aspect.MAGIC, total);
		}
		return cullTags(tmp);
	}

	private static int harvestLevel(ItemStack s) {
		int dur = s.getMaxDamage();
		if (dur >= 1561) {
			return 3;
		}
		if (dur >= 250) {
			return 2;
		}
		if (dur >= 131) {
			return 1;
		}
		return 0;
	}

	private static Aspect enchantAspect(Holder<Enchantment> e) {
		Optional<ResourceKey<Enchantment>> k = e.unwrapKey();
		if (k.isEmpty()) {
			return null;
		}
		ResourceKey<Enchantment> key = k.get();
		if (key == Enchantments.AQUA_AFFINITY) return Aspect.WATER;
		if (key == Enchantments.BANE_OF_ARTHROPODS) return Aspect.BEAST;
		if (key == Enchantments.BLAST_PROTECTION) return Aspect.ARMOR;
		if (key == Enchantments.EFFICIENCY) return Aspect.TOOL;
		if (key == Enchantments.FEATHER_FALLING) return Aspect.FLIGHT;
		if (key == Enchantments.FIRE_ASPECT) return Aspect.FIRE;
		if (key == Enchantments.FIRE_PROTECTION) return Aspect.ARMOR;
		if (key == Enchantments.FLAME) return Aspect.FIRE;
		if (key == Enchantments.FORTUNE) return Aspect.GREED;
		if (key == Enchantments.INFINITY) return Aspect.CRAFT;
		if (key == Enchantments.KNOCKBACK) return Aspect.AIR;
		if (key == Enchantments.LOOTING) return Aspect.GREED;
		if (key == Enchantments.POWER) return Aspect.WEAPON;
		if (key == Enchantments.PROJECTILE_PROTECTION) return Aspect.ARMOR;
		if (key == Enchantments.PROTECTION) return Aspect.ARMOR;
		if (key == Enchantments.PUNCH) return Aspect.AIR;
		if (key == Enchantments.RESPIRATION) return Aspect.AIR;
		if (key == Enchantments.SHARPNESS) return Aspect.WEAPON;
		if (key == Enchantments.SILK_TOUCH) return Aspect.EXCHANGE;
		if (key == Enchantments.THORNS) return Aspect.WEAPON;
		if (key == Enchantments.SMITE) return Aspect.ENTROPY;
		if (key == Enchantments.UNBREAKING) return Aspect.EARTH;
		if (key == Enchantments.LUCK_OF_THE_SEA) return Aspect.GREED;
		if (key == Enchantments.LURE) return Aspect.BEAST;
		if (key == dev.neothaum.items.equipment.TCEnchantments.HASTE) return Aspect.MOTION;
		if (key == dev.neothaum.items.equipment.TCEnchantments.REPAIR) return Aspect.TOOL;
		return null;
	}

	/** ThaumcraftApiHelper.cullTags: keep at most 6 aspects, dropping the "cheapest" first. */
	public static AspectList cullTags(AspectList temp) {
		AspectList temp2 = new AspectList();
		for (Aspect tag : temp.getAspects()) {
			temp2.add(tag, temp.getAmount(tag));
		}
		while (temp2.size() > 6) {
			Aspect lowest = null;
			float low = 32767.0F;
			for (Aspect t : temp2.getAspects()) {
				float ta = temp2.getAmount(t);
				if (t.isPrimal()) {
					ta *= 0.9F;
				} else {
					Aspect[] c = t.getComponents();
					if (!c[0].isPrimal()) {
						ta *= 1.1F;
						if (!c[0].getComponents()[0].isPrimal()) {
							ta *= 1.05F;
						}
						if (!c[0].getComponents()[1].isPrimal()) {
							ta *= 1.05F;
						}
					}
					if (!c[1].isPrimal()) {
						ta *= 1.1F;
						if (!c[1].getComponents()[0].isPrimal()) {
							ta *= 1.05F;
						}
						if (!c[1].getComponents()[1].isPrimal()) {
							ta *= 1.05F;
						}
					}
				}
				if (ta < low) {
					low = ta;
					lowest = t;
				}
			}
			temp2.aspects.remove(lowest);
		}
		return temp2;
	}

	private static Map<net.minecraft.world.level.block.Block, AspectList> blockTable;

	/** Aspects for blocks that have no item (water, lava, fire): read straight from the data, so the client needs no sync. */
	public static AspectList getBlockAspects(net.minecraft.world.level.block.Block block) {
		if (blockTable == null) {
			Map<net.minecraft.world.level.block.Block, AspectList> t = new HashMap<>();
			for (ObjEntry o : RAW_OBJECTS) {
				if (o.item() != null && BuiltInRegistries.ITEM.getOptional(Identifier.parse(o.item())).isEmpty()) {
					BuiltInRegistries.BLOCK.getOptional(Identifier.parse(o.item())).ifPresent(b -> t.put(b, o.aspects()));
				}
			}
			blockTable = t;
		}
		AspectList l = blockTable.get(block);
		return l == null ? null : l.copy();
	}

	public static boolean hasEntityAspects(Identifier id) {
		return ENTITIES.stream().anyMatch(e -> e.entity().equals(id));
	}

	/** ScanManager.generateEntityAspects. */
	public static AspectList getEntityAspects(Entity entity) {
		if (entity instanceof Player p) {
			AspectList tags = new AspectList();
			tags.add(Aspect.MAN, 4);
			String name = p.getName().getString();
			if (name.equalsIgnoreCase("azanor")) {
				tags.add(Aspect.ELDRITCH, 20);
			} else if (name.equalsIgnoreCase("direwolf20")) {
				tags.add(Aspect.BEAST, 20);
			} else if (name.equalsIgnoreCase("pahimar")) {
				tags.add(Aspect.EXCHANGE, 20);
			} else {
				java.util.Random rand = new java.util.Random(("player_" + name).hashCode());
				Aspect[] posa = Aspect.aspects.values().toArray(new Aspect[0]);
				tags.add(posa[rand.nextInt(posa.length)], 4);
				tags.add(posa[rand.nextInt(posa.length)], 4);
				tags.add(posa[rand.nextInt(posa.length)], 4);
			}
			return tags;
		}
		Identifier id = EntityType.getKey(entity.getType());
		AspectList tags = null;
		for (EntityEntry et : ENTITIES) {
			if (!et.entity().equals(id)) {
				continue;
			}
			if (et.nbt().isEmpty()) {
				tags = et.aspects();
			} else if (et.nbt().containsKey("powered") && entity instanceof Creeper c && c.isPowered()) {
				tags = et.aspects();
			}
		}
		return tags;
	}

	private ObjectAspects() {
	}
}
