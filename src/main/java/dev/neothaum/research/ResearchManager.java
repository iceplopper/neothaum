package dev.neothaum.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVAttachments;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Port of ResearchCategories + ResearchManager. Research data is a static mod resource, identical on client and server. */
public final class ResearchManager {
	private static final Map<String, ResearchCategory> CATEGORIES = new LinkedHashMap<>();
	private static final Map<String, ResearchItem> ENTRIES = new LinkedHashMap<>();
	private static List<ResearchItem> allHiddenResearch;
	private static List<ResearchItem> allValidResearch;

	private static boolean loaded;

	public static void init() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(server -> ensureLoaded());
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.player));
	}

	// ------------------------------------------------------------------ loading

	/** Loaded on first use: item stacks can't be built until item components are bound, after mod init. */
	private static synchronized void ensureLoaded() {
		if (!loaded) {
			loaded = true;
			load();
		}
	}

	private static void load() {
		try (InputStream in = ResearchManager.class.getResourceAsStream("/data/neothaum/neothaum/research.json")) {
			JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (JsonElement ce : root.getAsJsonArray("categories")) {
				JsonObject c = ce.getAsJsonObject();
				CATEGORIES.put(c.get("key").getAsString(), new ResearchCategory(c.get("key").getAsString(), Identifier.parse(c.get("icon").getAsString()),
					Identifier.parse(c.get("background").getAsString())));
			}
			int skipped = 0;
			Map<String, ResearchItem> parsed = new LinkedHashMap<>();
			for (JsonElement re : root.getAsJsonArray("research")) {
				ResearchItem r = parseResearch(re.getAsJsonObject());
				if (r == null) {
					skipped++;
					continue;
				}
				// NeoThaum Additions' research appears only while its addition is switched on
				if (re.getAsJsonObject().has("addition") && !dev.neothaum.addon.Additions.enabled(re.getAsJsonObject().get("addition").getAsString())) {
					skipped++;
					continue;
				}
				if (re.getAsJsonObject().has("requires_tag")) {
					r.requiresTag = re.getAsJsonObject().get("requires_tag").getAsString();
				}
				parsed.put(r.key, r);
			}
			// an entry whose parents belong to a later phase waits with them; otherwise the book would offer research the server refuses
			boolean dropped = true;
			while (dropped) {
				dropped = parsed.values().removeIf(r -> !parsed.keySet().containsAll(r.parents) || !parsed.keySet().containsAll(r.parentsHidden));
				if (dropped) {
					skipped = root.getAsJsonArray("research").size() - parsed.size();
				}
			}
			for (ResearchItem r : parsed.values()) {
				ENTRIES.put(r.key, r);
				ResearchCategory cat = CATEGORIES.get(r.category);
				if (cat != null && !r.isVirtual()) {
					cat.research.put(r.key, r);
					cat.minDisplayColumn = Math.min(cat.minDisplayColumn, r.displayColumn);
					cat.minDisplayRow = Math.min(cat.minDisplayRow, r.displayRow);
					cat.maxDisplayColumn = Math.max(cat.maxDisplayColumn, r.displayColumn);
					cat.maxDisplayRow = Math.max(cat.maxDisplayRow, r.displayRow);
				}
			}
			// drop categories with nothing ported yet
			CATEGORIES.values().removeIf(c -> c.research.isEmpty());
			NeoThaum.LOGGER.info("Loaded {} research entries ({} waiting on unported content)", ENTRIES.size(), skipped);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load research.json", e);
		}
	}

	private static ItemStack stackFrom(JsonObject o) {
		if (o.has("components") && o.get("item").getAsString().equals("neothaum:wand")) {
			JsonObject c = o.getAsJsonObject("components");
			ItemStack w = dev.neothaum.items.WandItem.createFull(TVItems.WAND, c.get("neothaum:wand_cap").getAsString(),
				c.get("neothaum:wand_rod").getAsString());
			if (c.has("neothaum:sceptre")) {
				w.set(dev.neothaum.registry.TVComponents.SCEPTRE, c.get("neothaum:sceptre").getAsBoolean());
			}
			return w;
		}
		if (o.has("item")) {
			var item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(o.get("item").getAsString()));
			return item.map(ItemStack::new).orElse(null);
		}
		if (o.has("tag")) {
			// use the first ported block/item in the tag family as display, e.g. "#minecraft:logs"
			return null;
		}
		return null;
	}

	private static AspectList aspects(JsonObject o) {
		AspectList l = new AspectList();
		if (o != null) {
			for (var e : o.entrySet()) {
				Aspect a = Aspect.getAspect(e.getKey());
				if (a != null) {
					l.add(a, e.getValue().getAsInt());
				}
			}
		}
		return l;
	}

	private static List<String> strings(JsonObject o, String key) {
		List<String> out = new ArrayList<>();
		if (o.has(key)) {
			for (JsonElement e : o.getAsJsonArray(key)) {
				out.add(e.getAsString());
			}
		}
		return out;
	}

	private static ResearchItem parseResearch(JsonObject o) {
		String key = o.get("key").getAsString();
		ItemStack iconItem = null;
		Identifier iconRes = null;
		if (o.has("icon") && o.get("icon").isJsonObject()) {
			JsonObject ic = o.getAsJsonObject("icon");
			if (ic.has("texture")) {
				iconRes = Identifier.parse(ic.get("texture").getAsString());
			} else {
				iconItem = stackFrom(ic);
				if (iconItem == null) {
					return null; // icon item not ported yet -> entry belongs to a later phase
				}
			}
		}
		List<ItemStack> itemTriggers = new ArrayList<>();
		if (o.has("item_triggers")) {
			for (JsonElement e : o.getAsJsonArray("item_triggers")) {
				ItemStack s = stackFrom(e.getAsJsonObject());
				if (s != null) {
					itemTriggers.add(s);
				}
			}
		}
		List<Aspect> aspectTriggers = new ArrayList<>();
		for (String s : strings(o, "aspect_triggers")) {
			aspectTriggers.add(Aspect.getAspect(s));
		}
		List<ResearchPage> pages = new ArrayList<>();
		if (o.has("pages")) {
			for (JsonElement pe : o.getAsJsonArray("pages")) {
				ResearchPage p = parsePage(pe.getAsJsonObject());
				if (p != null) {
					pages.add(p);
				}
			}
		}
		return new ResearchItem(key, o.get("category").getAsString(), aspects(o.getAsJsonObject("aspects")), o.get("col").getAsInt(), o.get("row").getAsInt(),
			o.get("complexity").getAsInt(), iconItem, iconRes, strings(o, "parents"), strings(o, "parents_hidden"), strings(o, "siblings"),
			strings(o, "entity_triggers"), itemTriggers, aspectTriggers, pages, strings(o, "flags"), o.has("warp") ? o.get("warp").getAsInt() : 0);
	}

	private static ResearchPage parsePage(JsonObject p) {
		String type = p.get("type").getAsString();
		switch (type) {
			case "text": {
				String research = p.has("research") ? p.get("research").getAsString() : null;
				return new ResearchPage(research != null ? ResearchPage.Type.TEXT_CONCEALED : ResearchPage.Type.TEXT, p.get("text").getAsString(), research,
					List.of(), null, null, null, null);
			}
			case "image":
				return new ResearchPage(ResearchPage.Type.IMAGE, p.get("caption").getAsString(), null, List.of(), Identifier.parse(p.get("image").getAsString()),
					null, null, null);
			case "aspects":
				return new ResearchPage(ResearchPage.Type.ASPECTS, null, null, List.of(), null, aspects(p.getAsJsonObject("aspects")), null, null);
			case "recipe": {
				List<Identifier> ids = new ArrayList<>();
				for (JsonElement r : p.getAsJsonArray("recipes")) {
					if (r.isJsonPrimitive()) {
						ids.add(Identifier.parse(r.getAsString()));
					}
				}
				return ids.isEmpty() ? null : new ResearchPage(ResearchPage.Type.RECIPE, null, null, ids, null, null, null, null);
			}
			case "compound":
				return new ResearchPage(ResearchPage.Type.COMPOUND_CRAFTING, null, null, List.of(), null, null, null, p.get("data"));
			case "smelting": {
				ItemStack s = stackFrom(p.getAsJsonObject("input"));
				return s == null ? null : new ResearchPage(ResearchPage.Type.SMELTING, null, null, List.of(), null, null, s, null);
			}
			default:
				return null;
		}
	}

	// ------------------------------------------------------------------ registry access

	public static Collection<ResearchItem> entries() {
		ensureLoaded();
		return ENTRIES.values().stream().filter(ResearchManager::isAvailable).toList();
	}

	/** False for research whose required material no loaded mod provides (an empty tag): it is left out of the book entirely. */
	public static boolean isAvailable(ResearchItem r) {
		if (r.requiresTag == null) {
			return true;
		}
		var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(r.requiresTag));
		return net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator().hasNext();
	}

	public static Map<String, ResearchCategory> categories() {
		ensureLoaded();
		return CATEGORIES;
	}

	/**
	 * Whether research is bought with research points rather than solved at the research table, by Config.researchDifficulty: everything on easy
	 * (-1), the secondary research on normal (0), nothing on hard (1).
	 */
	public static boolean boughtWithPoints(ResearchItem ri) {
		int difficulty = dev.neothaum.config.NeoThaumConfig.researchDifficulty();
		return difficulty == -1 || difficulty == 0 && ri.isSecondary();
	}

	public static ResearchItem getResearch(String key) {
		ensureLoaded();
		ResearchItem r = ENTRIES.get(key);
		return r == null || isAvailable(r) ? r : null;
	}

	// ------------------------------------------------------------------ player knowledge

	public static PlayerKnowledge knowledge(Player player) {
		PlayerKnowledge k = player.getAttached(TVAttachments.KNOWLEDGE);
		return k != null ? k : PlayerKnowledge.empty();
	}

	public static void setKnowledge(Player player, PlayerKnowledge k) {
		player.setAttached(TVAttachments.KNOWLEDGE, k);
	}

	private static void onPlayerJoin(ServerPlayer player) {
		grantStartingKnowledge(player);
	}

	/** What every player starts with: the primal aspects and the auto-unlocked research. */
	public static void grantStartingKnowledge(ServerPlayer player) {
		PlayerKnowledge k = knowledge(player).copy();
		k.addDiscoveredPrimalAspects();
		for (ResearchItem ri : entries()) {
			if (ri.isAutoUnlock()) {
				k.research.add(ri.key);
			}
		}
		setKnowledge(player, k);
	}

	public static boolean isResearchComplete(Player player, String key) {
		if (!key.startsWith("@") && getResearch(key) == null) {
			return false;
		}
		return knowledge(player).research.contains(key);
	}

	public static boolean doesPlayerHaveRequisites(Player player, String key) {
		ResearchItem ri = getResearch(key);
		if (ri == null) {
			return false;
		}
		PlayerKnowledge k = knowledge(player);
		for (String p : ri.parents) {
			if (!k.research.contains(p)) {
				return false;
			}
		}
		for (String p : ri.parentsHidden) {
			if (!k.research.contains(p)) {
				return false;
			}
		}
		return true;
	}

	/** ResearchManager.completeResearch (server side; syncs the attachment and notifies the client). */
	public static boolean completeResearch(Player player, String key) {
		PlayerKnowledge k = knowledge(player);
		if (k.research.contains(key)) {
			return false;
		}
		k = k.copy();
		k.research.add(key);
		ResearchItem ri = getResearch(key);
		// no warp from research in wuss mode (the Warp setting off), as TC4's Config.wuss
		int warp = ri != null && dev.neothaum.config.NeoThaumConfig.current.allowWarp ? ri.warp : 0;
		if (warp > 0) {
			if (warp > 1) {
				int w2 = warp / 2;
				k.warp += warp - w2;
				k.warpSticky += w2;
			} else {
				k.warp += warp;
			}
		}
		setKnowledge(player, k);
		return true;
	}

	public static void completeAspect(Player player, Aspect aspect, int amount) {
		PlayerKnowledge k = knowledge(player).copy();
		k.addDiscoveredAspect(aspect);
		k.setAspectPool(aspect, amount);
		setKnowledge(player, k);
	}

	public static Aspect getCombinationResult(Aspect aspect1, Aspect aspect2) {
		for (Aspect aspect : Aspect.aspects.values()) {
			Aspect[] c = aspect.getComponents();
			if (c != null && c.length == 2 && (c[0] == aspect1 && c[1] == aspect2 || c[0] == aspect2 && c[1] == aspect1)) {
				return aspect;
			}
		}
		return null;
	}

	public static AspectList reduceToPrimals(AspectList al) {
		return reduceToPrimals(al, false);
	}

	public static AspectList reduceToPrimals(AspectList al, boolean merge) {
		AspectList out = new AspectList();
		for (Aspect aspect : al.getAspects()) {
			if (aspect.isPrimal()) {
				if (merge) {
					out.merge(aspect, al.getAmount(aspect));
				} else {
					out.add(aspect, al.getAmount(aspect));
				}
			} else {
				AspectList send = new AspectList();
				send.add(aspect.getComponents()[0], al.getAmount(aspect));
				send.add(aspect.getComponents()[1], al.getAmount(aspect));
				send = reduceToPrimals(send, merge);
				for (Aspect a : send.getAspects()) {
					if (merge) {
						out.merge(a, send.getAmount(a));
					} else {
						out.add(a, send.getAmount(a));
					}
				}
			}
		}
		return out;
	}

	public static boolean createClue(Level level, Player player, Object clue, AspectList aspects) {
		List<String> keys = new ArrayList<>();
		PlayerKnowledge k = knowledge(player);
		outer:
		for (ResearchItem ri : entries()) {
			boolean valid = ri.tags.size() > 0 && (ri.isLost() || ri.isHidden()) && !k.research.contains(ri.key) && !k.research.contains("@" + ri.key);
			if (!valid) {
				continue;
			}
			if (clue instanceof ItemStack stack && !ri.itemTriggers.isEmpty()) {
				for (ItemStack t : ri.itemTriggers) {
					if (ItemStack.isSameItem(t, stack)) {
						keys.add(ri.key);
						continue outer;
					}
				}
			} else if (clue instanceof String s && ri.entityTriggers.contains(s)) {
				keys.add(ri.key);
				continue;
			}
			if (aspects != null && aspects.size() > 0) {
				for (Aspect aspect : ri.aspectTriggers) {
					if (aspects.getAmount(aspect) > 0) {
						keys.add(ri.key);
						break;
					}
				}
			}
		}
		if (!keys.isEmpty()) {
			String key = keys.get(level.getRandom().nextInt(keys.size()));
			if (completeResearch(player, "@" + key) && player instanceof ServerPlayer sp) {
				TVNetwork.sendResearchComplete(sp, "@" + key);
			}
			return true;
		}
		return false;
	}

	public static String findHiddenResearch(Player player) {
		if (allHiddenResearch == null) {
			allHiddenResearch = new ArrayList<>();
			for (ResearchItem ri : entries()) {
				if (ri.isHidden() && ri.tags.size() > 0) {
					allHiddenResearch.add(ri);
				}
			}
		}
		List<String> keys = new ArrayList<>();
		for (ResearchItem r : allHiddenResearch) {
			if (!isResearchComplete(player, r.key) && doesPlayerHaveRequisites(player, r.key)
				&& (!r.itemTriggers.isEmpty() || !r.entityTriggers.isEmpty() || !r.aspectTriggers.isEmpty())) {
				keys.add(r.key);
			}
		}
		Random rand = new Random(player.level().getGameTime() / 10L / 5L);
		return keys.isEmpty() ? "FAIL" : keys.get(rand.nextInt(keys.size()));
	}

	public static String findMatchingResearch(Player player, Aspect aspect) {
		if (allValidResearch == null) {
			allValidResearch = new ArrayList<>();
			for (ResearchItem ri : entries()) {
				boolean secondary = boughtWithPoints(ri);
				if (!secondary && !ri.isHidden() && !ri.isLost() && !ri.isAutoUnlock() && !ri.isVirtual() && !ri.isStub()) {
					allValidResearch.add(ri);
				}
			}
		}
		List<String> keys = new ArrayList<>();
		for (ResearchItem r : allValidResearch) {
			if (!isResearchComplete(player, r.key) && doesPlayerHaveRequisites(player, r.key) && r.tags.getAmount(aspect) > 0) {
				keys.add(r.key);
			}
		}
		return keys.isEmpty() ? null : keys.get(player.getRandom().nextInt(keys.size()));
	}

	// ------------------------------------------------------------------ research notes

	public static int getResearchSlot(Player player, String key) {
		var inv = player.getInventory();
		for (int a = 0; a < inv.getContainerSize(); a++) {
			ItemStack s = inv.getItem(a);
			ResearchNoteData d = s.is(TVItems.RESEARCH_NOTES) ? s.get(TVComponents.RESEARCH_NOTE) : null;
			if (d != null && d.key.equals(key)) {
				return a;
			}
		}
		return -1;
	}

	public static boolean consumeInkFromPlayer(Player player, boolean doit) {
		var inv = player.getInventory();
		for (int a = 0; a < inv.getContainerSize(); a++) {
			ItemStack s = inv.getItem(a);
			if (s.is(TVItems.SCRIBING_TOOLS) && s.getDamageValue() < s.getMaxDamage()) {
				if (doit) {
					s.setDamageValue(s.getDamageValue() + 1);
				}
				return true;
			}
		}
		return false;
	}

	public static boolean consumeInkFromTable(ItemStack stack, boolean doit) {
		if (stack.is(TVItems.SCRIBING_TOOLS) && stack.getDamageValue() < stack.getMaxDamage()) {
			if (doit) {
				stack.setDamageValue(stack.getDamageValue() + 1);
			}
			return true;
		}
		return false;
	}

	public static ItemStack createResearchNoteForPlayer(Level level, Player player, String key) {
		int slot = getResearchSlot(player, key);
		if (slot >= 0) {
			return player.getInventory().getItem(slot);
		}
		var inv = player.getInventory();
		int paper = -1;
		for (int a = 0; a < inv.getContainerSize(); a++) {
			if (inv.getItem(a).is(Items.PAPER)) {
				paper = a;
				break;
			}
		}
		if (consumeInkFromPlayer(player, false) && paper >= 0) {
			inv.getItem(paper).shrink(1);
			consumeInkFromPlayer(player, true);
			ItemStack note = createNote(new ItemStack(TVItems.RESEARCH_NOTES), key, level.getRandom());
			if (note != null) {
				player.getInventory().placeItemBackInInventory(note, net.minecraft.util.Prediction.SERVER_ONLY);
			}
			player.inventoryMenu.broadcastChanges();
			return note;
		}
		return null;
	}

	public static ItemStack createNote(ItemStack stack, String key, RandomSource rand) {
		ResearchItem rr = getResearch(key);
		Aspect primary = rr == null ? null : rr.getResearchPrimaryTag();
		if (primary == null) {
			return null;
		}
		ResearchNoteData data = new ResearchNoteData();
		data.key = key;
		data.color = primary.getColor();
		data.complete = false;
		data.copies = 0;
		int radius = 1 + Math.min(3, rr.getComplexity());
		Map<String, HexUtils.Hex> hexLocs = HexUtils.generateHexes(radius);
		List<HexUtils.Hex> outerRing = HexUtils.distributeRingRandomly(radius, rr.tags.size(), rand);
		for (HexUtils.Hex hex : hexLocs.values()) {
			data.hexes.put(hex.toString(), hex);
			data.hexEntries.put(hex.toString(), new ResearchNoteData.HexEntry(null, 0));
		}
		int count = 0;
		for (HexUtils.Hex hex : outerRing) {
			data.hexes.put(hex.toString(), hex);
			data.hexEntries.put(hex.toString(), new ResearchNoteData.HexEntry(rr.tags.getAspects()[count], 1));
			count++;
		}
		if (rr.getComplexity() > 1) {
			int blanks = rr.getComplexity() * 2;
			HexUtils.Hex[] temp = data.hexes.values().toArray(new HexUtils.Hex[0]);
			int guard = 0;
			while (blanks > 0 && guard++ < 10000) {
				int indx = rand.nextInt(temp.length);
				ResearchNoteData.HexEntry he = data.hexEntries.get(temp[indx].toString());
				if (he != null && he.type() == 0) {
					boolean gtg = true;
					for (int n = 0; n < 6; n++) {
						HexUtils.Hex neighbour = temp[indx].getNeighbour(n);
						if (data.hexes.containsKey(neighbour.toString()) && data.hexEntries.get(neighbour.toString()).type() == 1) {
							int cc = 0;
							for (int q = 0; q < 6; q++) {
								if (data.hexes.containsKey(data.hexes.get(neighbour.toString()).getNeighbour(q).toString())) {
									cc++;
								}
								if (cc >= 2) {
									break;
								}
							}
							if (cc < 2) {
								gtg = false;
								break;
							}
						}
					}
					if (gtg) {
						data.hexes.remove(temp[indx].toString());
						data.hexEntries.remove(temp[indx].toString());
						temp = data.hexes.values().toArray(new HexUtils.Hex[0]);
						blanks--;
					}
				}
			}
		}
		stack.set(TVComponents.RESEARCH_NOTE, data);
		return stack;
	}

	public static boolean checkResearchCompletion(ItemStack contents, ResearchNoteData note, Player player) {
		List<String> checked = new ArrayList<>();
		List<String> main = new ArrayList<>();
		List<String> remains = new ArrayList<>();
		for (HexUtils.Hex hex : note.hexes.values()) {
			if (note.hexEntries.get(hex.toString()).type() == 1) {
				main.add(hex.toString());
			}
		}
		for (HexUtils.Hex hex : note.hexes.values()) {
			if (note.hexEntries.get(hex.toString()).type() == 1) {
				main.remove(hex.toString());
				checkConnections(note, hex, checked, main, remains, knowledge(player));
				break;
			}
		}
		if (!main.isEmpty()) {
			return false;
		}
		List<String> remove = new ArrayList<>();
		for (HexUtils.Hex hex : note.hexes.values()) {
			if (note.hexEntries.get(hex.toString()).type() != 1 && !remains.contains(hex.toString())) {
				remove.add(hex.toString());
			}
		}
		for (String s : remove) {
			note.hexEntries.remove(s);
			note.hexes.remove(s);
		}
		note.complete = true;
		contents.set(TVComponents.RESEARCH_NOTE, note.copy());
		return true;
	}

	private static void checkConnections(ResearchNoteData note, HexUtils.Hex hex, List<String> checked, List<String> main, List<String> remains,
		PlayerKnowledge k) {
		checked.add(hex.toString());
		for (int a = 0; a < 6; a++) {
			HexUtils.Hex target = hex.getNeighbour(a);
			ResearchNoteData.HexEntry te = note.hexEntries.get(target.toString());
			if (!checked.contains(target.toString()) && te != null && te.type() >= 1) {
				Aspect aspect1 = note.hexEntries.get(hex.toString()).aspect();
				Aspect aspect2 = te.aspect();
				if (aspect1 != null && aspect2 != null && k.hasDiscoveredAspect(aspect1) && k.hasDiscoveredAspect(aspect2)
					&& (!aspect1.isPrimal() && (aspect1.getComponents()[0] == aspect2 || aspect1.getComponents()[1] == aspect2)
					|| !aspect2.isPrimal() && (aspect2.getComponents()[0] == aspect1 || aspect2.getComponents()[1] == aspect1))) {
					remains.add(target.toString());
					if (te.type() == 1) {
						main.remove(target.toString());
					}
					checkConnections(note, target, checked, main, remains, k);
				}
			}
		}
	}

	private ResearchManager() {
	}
}
