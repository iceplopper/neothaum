package dev.neothaum.gametest;

import dev.neothaum.NeoThaum;
import dev.neothaum.client.gui.ResearchPagesScreen;
import dev.neothaum.client.gui.ThaumonomiconScreen;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.research.ResearchPage;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/** Builds small scenes and saves screenshots of NeoThaum's custom rendering, for comparing against Thaumcraft 4. */
public class VisualTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext ctx) {
		String scenesEnv = System.getenv("NEOTHAUM_SCENES");
		if ("biomes".equalsIgnoreCase(scenesEnv)) {
			// the biome map needs a real (default preset) overworld, not the flat test world
			try (TestSingleplayerContext sp = ctx.worldBuilder().setUseConsistentSettings(false).adjustSettings(st -> {
				st.setWorldType(st.getNormalPresetList().get(0));
				st.setSeed("neothaum");
			}).create()) {
				biomeScan(sp.getServer());
			}
			return;
		}
		try (TestSingleplayerContext sp = ctx.worldBuilder().create()) {
			var server = sp.getServer();
			sp.getConnection().waitForChunksRender();
			// NEOTHAUM_SCENES=essentia,suction,infusion runs just those scenes, for a quicker check of one area
			String only = System.getenv("NEOTHAUM_SCENES");
			if (only != null && !only.isBlank()) {
				java.util.Set<String> scenes = java.util.Set.of(only.toLowerCase().split("\\s*,\\s*"));
				server.runCommand("time set noon");
				server.runCommand("weather clear");
				if (scenes.contains("essentia")) {
					essentiaScene(ctx, server, sp);
				}
				if (scenes.contains("suction")) {
					suctionScene(ctx, server, sp);
				}
				if (scenes.contains("infusion")) {
					infusionScene(ctx, server, sp);
				}
				if (scenes.contains("tabs")) {
					tabsScene(ctx, server);
				}
				if (scenes.contains("bellows")) {
					bellowsScene(ctx, server, sp);
				}
				if (scenes.contains("mobs")) {
					mobsScene(ctx, server, sp);
				}
				if (scenes.contains("peaceful")) {
					peacefulScene(ctx, server, sp);
				}
				if (scenes.contains("wands")) {
					wandsScene(ctx, server, sp);
				}
				if (scenes.contains("visnet")) {
					visnetScene(ctx, server, sp);
				}
				if (scenes.contains("nodejar")) {
					nodeJarScene(ctx, server, sp);
				}
				return;
			}
			dumpAspectGaps(server);
			dumpAspectTable(ctx);
			ctx.waitTicks(40);
			auditResearch(ctx);
			checkSaplings(server);
			for (String cmd : new String[]{"time set noon", "weather clear", "gamemode creative @a", "tp @a 0 120 0 0 0",
				"fill -6 117 -2 6 125 12 air", "fill -6 116 -2 6 116 12 stone",
				// one cluster of each kind on the floor, plus wall and ceiling mounts
				"setblock -3 117 4 neothaum:air_crystal_cluster[facing=up]", "setblock -2 117 4 neothaum:fire_crystal_cluster[facing=up]",
				"setblock -1 117 4 neothaum:water_crystal_cluster[facing=up]", "setblock 0 117 4 neothaum:earth_crystal_cluster[facing=up]",
				"setblock 1 117 4 neothaum:order_crystal_cluster[facing=up]", "setblock 2 117 4 neothaum:entropy_crystal_cluster[facing=up]",
				"setblock 3 117 4 neothaum:mixed_crystal_cluster[facing=up]", "fill -2 117 7 2 121 7 stone",
				"setblock -1 118 6 neothaum:fire_crystal_cluster[facing=north]", "setblock 1 118 6 neothaum:water_crystal_cluster[facing=north]",
				"fill -2 121 3 2 121 5 stone", "setblock 0 120 5 neothaum:order_crystal_cluster[facing=down]"}) {
				server.runCommand(cmd);
			}
			sp.getConnection().waitForChunksRender();
			ctx.getInput().lookAt(new net.minecraft.core.BlockPos(0, 117, 5));
			ctx.waitTicks(20);
			shot(ctx, "crystals");

			server.runCommand("item replace entity @a weapon.mainhand with neothaum:thaumometer");
			server.runCommand("item replace entity @a hotbar.1 with neothaum:air_crystal_cluster");
			server.runCommand("item replace entity @a hotbar.2 with neothaum:mixed_crystal_cluster");
			server.runCommand("setblock 0 121 9 neothaum:earth_crystal_cluster[facing=north]");
			server.runCommand("fill -2 117 10 2 125 10 stone");
			server.runCommand("clear @a");
			ctx.waitTicks(5);
			server.runCommand("item replace entity @a weapon.mainhand with neothaum:thaumometer");
			server.runCommand("item replace entity @a hotbar.1 with neothaum:air_crystal_cluster");
			server.runCommand("item replace entity @a hotbar.2 with neothaum:mixed_crystal_cluster");
			ctx.getInput().lookAt(new net.minecraft.core.BlockPos(0, 121, 9));
			ctx.waitTicks(60);
			shot(ctx, "thaumometer_first_person");
			ctx.getInput().lookAt(new net.minecraft.core.BlockPos(0, 117, 4));
			ctx.waitTicks(10);
			shot(ctx, "thaumometer_first_person_down");
			ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			ctx.waitTicks(10);
			shot(ctx, "thaumometer_third_person");
			ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
			server.runCommand("gamemode survival @a");
			ctx.waitTicks(5);
			ctx.setScreen(() -> new InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
			ctx.waitTicks(5);
			shot(ctx, "inventory");
			ctx.setScreen(() -> null);
			server.runCommand("give @a neothaum:mundane_amulet");
			server.runCommand("give @a neothaum:mundane_ring");
			server.runCommand("give @a neothaum:mundane_belt");
			server.runCommand("item replace entity @a armor.head with neothaum:goggles_of_revealing");
			ctx.waitTicks(5);
			ctx.runOnClient(mc -> dev.neothaum.network.TVNetwork.clientSender.accept(new dev.neothaum.network.TVNetwork.OpenBaubles()));
			ctx.waitForScreen(dev.neothaum.client.gui.BaublesScreen.class);
			ctx.waitTicks(5);
			shot(ctx, "baubles");
			ctx.runOnClient(mc -> mc.player.closeContainer());

			ResearchItem page = ctx.computeOnClient(mc -> {
				for (ResearchItem r : ResearchManager.entries()) {
					if (!r.pages.isEmpty() && r.pages.get(0).type() == ResearchPage.Type.TEXT && r.pages.size() > 2) {
						return r;
					}
				}
				return null;
			});
			ctx.setScreen(ThaumonomiconScreen::new);
			ctx.waitTicks(5);
			shot(ctx, "thaumonomicon_map");
			if (page != null) {
				ctx.setScreen(() -> new ResearchPagesScreen(page, 0, 0, 0));
				ctx.waitTicks(5);
				shot(ctx, "thaumonomicon_page_" + page.key.toLowerCase());
			}
			ctx.setScreen(() -> null);
			// ores: a stone wall over a deepslate wall, each with Thaumcraft's ores set into it
			server.runCommand("fill -60 110 -40 -46 113 -40 stone");
			server.runCommand("fill -60 106 -40 -46 109 -40 deepslate");
			server.runCommand("fill -60 106 -39 -46 113 -30 air");
			String[] ores = {"air_infused_stone", "fire_infused_stone", "water_infused_stone", "earth_infused_stone", "order_infused_stone",
				"entropy_infused_stone", "amber_bearing_stone", "cinnabar_ore"};
			for (int i = 0; i < ores.length; i++) {
				server.runCommand("setblock " + (-59 + i * 2) + " 112 -40 neothaum:" + ores[i]);
				server.runCommand("setblock " + (-59 + i * 2) + " 108 -40 neothaum:deepslate_" + ores[i]);
			}
			server.runCommand("setblock -46 110 -39 neothaum:raw_cinnabar_block");
			server.runCommand("gamemode spectator @a");
			server.runCommand("tp @a -52.5 110 -33 180 0");
			sp.getConnection().waitForChunksRender();
			ctx.waitTicks(20);
			shot(ctx, "ores");
			server.runCommand("gamemode creative @a");
			server.runCommand("clear @a");
			for (String it : new String[]{"raw_cinnabar", "quicksilver_bead", "quicksilver_drop", "quicksilver", "raw_cinnabar_block",
				"deepslate_cinnabar_ore", "deepslate_fire_infused_stone", "native_copper_cluster"}) {
				server.runCommand("give @a neothaum:" + it);
			}
			ctx.waitTicks(5);
			ctx.setScreen(() -> new InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
			ctx.waitTicks(5);
			shot(ctx, "ore_items");
			ctx.setScreen(() -> null);
			// dev tools: one node of each type via the placer code, then the node editor
			server.runCommand("fill -100 99 -100 -84 106 -94 air");
			server.runCommand("fill -101 98 -101 -83 107 -101 black_concrete");
			server.runOnServer(srv -> {
				var level = srv.overworld();
				var types = dev.neothaum.api.nodes.NodeType.values();
				for (int i = 0; i < types.length; i++) {
					dev.neothaum.world.NodeGenerator.createRandomNodeAt(level, new net.minecraft.core.BlockPos(-98 + i * 3, 102, -98), level.getRandom(),
						false, false, false, true, types[i]);
				}
			});
			server.runCommand("gamemode creative @a");
			server.runCommand("item replace entity @a armor.head with neothaum:goggles_of_revealing");
			server.runCommand("tp @a -90.5 101.5 -92 180 0");
			sp.getConnection().waitForChunksRender();
			ctx.waitTicks(30);
			shot(ctx, "dev_nodes");
			var editPos = new net.minecraft.core.BlockPos(-98 + 2 * 3, 102, -98);
			ctx.setScreen(() -> new dev.neothaum.client.devtools.NodeEditorScreen(editPos));
			ctx.waitTicks(5);
			shot(ctx, "dev_node_editor");
			ctx.runOnClient(mc -> net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new dev.neothaum.devtools.DevTools.NodeEdit(
				editPos, dev.neothaum.api.nodes.NodeType.PURE.ordinal(), 0,
				new dev.neothaum.api.aspects.AspectList().add(dev.neothaum.api.aspects.Aspect.ORDER, 200), false)));
			ctx.waitTicks(10);
			server.runOnServer(srv -> {
				if (srv.overworld().getBlockEntity(editPos) instanceof dev.neothaum.tiles.NodeBlockEntity n) {
					NeoThaum.LOGGER.info("[visual-test] edited node -> {} {} {}", n.getNodeType(), n.getNodeModifier(), n.getAspects().getAmount(
						dev.neothaum.api.aspects.Aspect.ORDER));
				}
			});
			ctx.setScreen(() -> null);
			// batch items: candles, stairs and slabs, armour on the player
			server.runCommand("fill -80 99 -80 -60 106 -70 air");
			server.runCommand("fill -80 98 -80 -60 98 -70 stone");
			String[] colours = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue",
				"brown", "green", "red", "black"};
			for (int i = 0; i < colours.length; i++) {
				server.runCommand("setblock " + (-78 + i) + " 99 -76 neothaum:" + colours[i] + "_tallow_candle");
			}
			server.runCommand("setblock -76 99 -73 neothaum:arcane_stone_stairs[facing=south]");
			server.runCommand("setblock -75 99 -73 neothaum:arcane_stone_slab");
			server.runCommand("setblock -73 99 -73 neothaum:greatwood_stairs[facing=south]");
			server.runCommand("setblock -72 99 -73 neothaum:greatwood_slab[type=top]");
			server.runCommand("setblock -70 99 -73 neothaum:silverwood_stairs[facing=south]");
			server.runCommand("setblock -69 99 -73 neothaum:silverwood_slab[type=double]");
			server.runCommand("gamemode spectator @a");
			server.runCommand("tp @a -70.5 101.5 -70 160 25");
			sp.getConnection().waitForChunksRender();
			ctx.waitTicks(30);
			shot(ctx, "candles_stairs");
			server.runCommand("gamemode creative @a");
			server.runCommand("clear @a");
			for (String piece : new String[]{"head thaumium_helmet", "chest thaumium_chestplate", "legs thaumium_leggings", "feet thaumium_boots"}) {
				String[] sl = piece.split(" ");
				server.runCommand("item replace entity @a armor." + sl[0] + " with neothaum:" + sl[1]);
			}
			for (String it : new String[]{"thaumium_sword", "thaumium_pickaxe", "thaumium_axe", "thaumium_shovel", "thaumium_hoe", "triple_meat_treat",
				"beef_nugget", "white_tallow_candle", "thaumaturges_robe"}) {
				server.runCommand("give @a neothaum:" + it);
			}
			ctx.waitTicks(5);
			server.runOnServer(srv -> {
				var pl = srv.getPlayerList().getPlayers().get(0);
				NeoThaum.LOGGER.info("[visual-test] server player {} head={} chest={} main={}", pl.gameMode(), pl.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD),
					pl.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST), pl.getInventory().getItem(0));
			});
			ctx.runOnClient(mc -> NeoThaum.LOGGER.info("[visual-test] client player head={} main={}",
				mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD), mc.player.getInventory().getItem(0)));
			ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			ctx.waitTicks(10);
			shot(ctx, "thaumium_armor");
			server.runCommand("item replace entity @a armor.chest with neothaum:thaumaturges_robe");
			server.runCommand("item replace entity @a armor.legs with neothaum:thaumaturges_leggings");
			server.runCommand("item replace entity @a armor.feet with neothaum:thaumaturges_boots");
			server.runCommand("item replace entity @a armor.head with neothaum:goggles_of_revealing");
			ctx.waitTicks(10);
			shot(ctx, "robes");
			ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
			// nodes: one of every primal, wearing goggles so they render
			server.runCommand("fill -20 99 -60 -6 106 -54 air");
			server.runCommand("fill -21 98 -61 -5 107 -61 black_concrete");
			server.runCommand("fill -21 98 -61 -5 98 -50 black_concrete");
			server.runOnServer(srv -> {
				var level = srv.overworld();
				var primals = dev.neothaum.api.aspects.Aspect.getPrimalAspects();
				for (int i = 0; i < primals.size(); i++) {
					var pos = new net.minecraft.core.BlockPos(-18 + i * 2, 102, -57);
					level.setBlock(pos, dev.neothaum.registry.TVBlocks.AURA_NODE.defaultBlockState(), 3);
					if (level.getBlockEntity(pos) instanceof dev.neothaum.tiles.NodeBlockEntity node) {
						node.setNodeType(dev.neothaum.api.nodes.NodeType.NORMAL);
						node.setAspects(new dev.neothaum.api.aspects.AspectList().add(primals.get(i), 40));
						node.markForUpdate();
					}
				}
			});
			server.runCommand("item replace entity @a armor.head with neothaum:goggles_of_revealing");
			server.runCommand("gamemode creative @a");
			server.runCommand("effect give @a minecraft:levitation 5 255 true");
			server.runCommand("tp @a -12.5 101 -52 180 0");
			sp.getConnection().waitForChunksRender();
			ctx.waitTicks(30);
			shot(ctx, "nodes");
			essentiaScene(ctx, server, sp);
			suctionScene(ctx, server, sp);
			infusionScene(ctx, server, sp);
			// research table with scribing tools and a note on it
			server.runCommand("fill -30 99 -30 -24 104 -24 air");
			server.runCommand("fill -30 98 -30 -24 98 -24 stone");
			server.runOnServer(srv -> {
				var level = srv.overworld();
				var main = new net.minecraft.core.BlockPos(-28, 99, -27);
				dev.neothaum.blocks.ResearchTableBlock.place(level, main, net.minecraft.core.Direction.EAST);
				if (level.getBlockEntity(main) instanceof dev.neothaum.tiles.ResearchTableBlockEntity rt) {
					rt.setItem(0, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.SCRIBING_TOOLS));
					rt.setItem(1, ResearchManager.createNote(new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.RESEARCH_NOTES),
						"ALUMENTUM", level.getRandom()));
				}
			});
			server.runCommand("gamemode spectator @a");
			server.runCommand("tp @a -27 101.2 -29.3 0 50");
			sp.getConnection().waitForChunksRender();
			ctx.waitTicks(20);
			shot(ctx, "research_table");
			server.runCommand("tp @a 40 108 14 0 10");
			sp.getConnection().waitForChunksRender();
			ctx.waitTicks(20);
			shot(ctx, "trees");
			server.runCommand("gamemode creative @a");
			// Aspects of Magic: text page followed by the generated aspect pages
			ctx.runOnClient(mc -> dev.neothaum.api.aspects.Aspect.getPrimalAspects().forEach(a -> ResearchManager.completeAspect(mc.player, a, 0)));
			ResearchItem aspects = ctx.computeOnClient(mc -> ResearchManager.getResearch("ASPECTS"));
			if (aspects != null) {
				for (int pg = 0; pg < 6; pg += 2) {
					int page0 = pg;
					ctx.setScreen(() -> new ResearchPagesScreen(aspects, page0, 0, 0));
					ctx.waitTicks(5);
					shot(ctx, "thaumonomicon_aspects_" + pg);
				}
			}
			ctx.setScreen(() -> null);
		}
	}

	/** Plants each Thaumcraft sapling on open grass and grows it the way bone meal does. */
	private static void checkSaplings(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		// (a fill is capped at 32768 blocks)
		server.runCommand("fill 22 100 32 58 124 48 air");
		server.runCommand("fill 22 125 32 58 140 48 air");
		server.runCommand("fill 22 98 32 58 99 48 grass_block");
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var great = (dev.neothaum.blocks.TCSaplingBlock) dev.neothaum.registry.TVBlocks.GREATWOOD_SAPLING;
			var silver = (dev.neothaum.blocks.TCSaplingBlock) dev.neothaum.registry.TVBlocks.SILVERWOOD_SAPLING;
			var src = net.minecraft.world.level.block.BonemealSource.INTERACTION;
			// a lone greatwood sapling must not accept bone meal
			var lone = new net.minecraft.core.BlockPos(25, 100, 34);
			level.setBlock(lone, great.defaultBlockState(), 3);
			NeoThaum.LOGGER.info("[visual-test] sapling lone greatwood bonemealable={}", great.isValidBonemealTarget(level, lone, great.defaultBlockState(), src));
			// a 2x2 square grows from bone meal on any one sapling
			var c = new net.minecraft.core.BlockPos(30, 100, 40);
			for (var p : new net.minecraft.core.BlockPos[]{c, c.east(), c.south(), c.south().east()}) {
				level.setBlock(p, great.defaultBlockState(), 3);
			}
			var hit = c.south().east();
			NeoThaum.LOGGER.info("[visual-test] sapling square bonemealable={}", great.isValidBonemealTarget(level, hit, great.defaultBlockState(), src));
			great.grow(level, hit, level.getRandom());
			NeoThaum.LOGGER.info("[visual-test] sapling greatwood square -> {} {} {} {}", level.getBlockState(c), level.getBlockState(c.east()),
				level.getBlockState(c.south()), level.getBlockState(hit));
			var s = new net.minecraft.core.BlockPos(50, 100, 40);
			level.setBlock(s, silver.defaultBlockState(), 3);
			silver.grow(level, s, level.getRandom());
			NeoThaum.LOGGER.info("[visual-test] sapling silverwood -> {}", level.getBlockState(s));
		});
	}

	/** Checks every Thaumonomicon page that a player could open, and writes the problems to research_audit.txt. */
	private static void auditResearch(ClientGameTestContext ctx) {
		ctx.runOnClient(mc -> {
			var lang = net.minecraft.locale.Language.getInstance();
			var rm = mc.getResourceManager();
			StringBuilder sb = new StringBuilder();
			int pages = 0;
			for (ResearchItem r : ResearchManager.entries()) {
				if (r.isVirtual()) {
					continue;
				}
				if (r.iconItem == null && r.iconResource == null) {
					sb.append(r.key).append(": no icon").append(System.lineSeparator());
				}
				if (r.iconResource != null && rm.getResource(r.iconResource).isEmpty()) {
					sb.append(r.key).append(": icon texture missing ").append(r.iconResource).append(System.lineSeparator());
				}
				if (!lang.has("tc.research_name." + r.key)) {
					sb.append(r.key).append(": no name text").append(System.lineSeparator());
				}
				if (r.pages.isEmpty()) {
					sb.append(r.key).append(": no pages").append(System.lineSeparator());
				}
				for (int i = 0; i < r.pages.size(); i++) {
					ResearchPage p = r.pages.get(i);
					pages++;
					String where = r.key + " page " + (i + 1) + " (" + p.type() + ")";
					switch (p.type()) {
						case TEXT, TEXT_CONCEALED -> {
							if (!lang.has(p.text())) {
								sb.append(where).append(": missing text ").append(p.text()).append(System.lineSeparator());
							} else {
								var m = java.util.regex.Pattern.compile("<IMG>([^<]*)</IMG>").matcher(lang.getOrDefault(p.text()));
								while (m.find()) {
									String[] img = m.group(1).split(":");
									var tex = net.minecraft.resources.Identifier.fromNamespaceAndPath(img[0].equals("thaumcraft") ? "neothaum" : img[0], img[1]);
									if (rm.getResource(tex).isEmpty()) {
										sb.append(where).append(": image missing ").append(tex).append(System.lineSeparator());
									}
								}
							}
						}
						case RECIPE -> {
							if (p.recipes().isEmpty()) {
								sb.append(where).append(": no recipe ids").append(System.lineSeparator());
							}
							for (var id : p.recipes()) {
								if (!dev.neothaum.client.TVClientNetwork.RECIPES.containsKey(id)) {
									sb.append(where).append(": recipe not loaded ").append(id).append(System.lineSeparator());
								}
							}
						}
						case COMPOUND_CRAFTING, ASPECTS, SMELTING -> {
						}
						default -> sb.append(where).append(": page type not drawn").append(System.lineSeparator());
					}
				}
			}
			sb.append(pages).append(" pages checked").append(System.lineSeparator());
			try {
				java.nio.file.Files.writeString(java.nio.file.Path.of(System.getProperty("user.dir"), "research_audit.txt"), sb.toString());
			} catch (java.io.IOException e) {
				throw new java.io.UncheckedIOException(e);
			}
		});
	}

	/** Every item's computed aspects and display name, plus mob names, for the aspect review spreadsheet. */
	private static void dumpAspectTable(ClientGameTestContext ctx) {
		ctx.runOnClient(mc -> {
			com.google.gson.JsonObject items = new com.google.gson.JsonObject();
			var table = dev.neothaum.aspects.ObjectAspects.table();
			for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
				com.google.gson.JsonObject o = new com.google.gson.JsonObject();
				o.addProperty("name", item.getName(new net.minecraft.world.item.ItemStack(item)).getString());
				com.google.gson.JsonObject a = new com.google.gson.JsonObject();
				var al = table.get(item);
				if (al != null) {
					for (var asp : al.getAspects()) {
						a.addProperty(asp.getTag(), al.getAmount(asp));
					}
				}
				o.add("aspects", a);
				items.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString(), o);
			}
			com.google.gson.JsonObject mobs = new com.google.gson.JsonObject();
			for (var type : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
				mobs.addProperty(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type).toString(), type.getDescription().getString());
			}
			com.google.gson.JsonObject root = new com.google.gson.JsonObject();
			root.add("items", items);
			root.add("mobs", mobs);
			try {
				java.nio.file.Files.writeString(java.nio.file.Path.of(System.getProperty("user.dir"), "aspect_table.json"), root.toString());
			} catch (java.io.IOException e) {
				throw new java.io.UncheckedIOException(e);
			}
		});
	}

	/** Every Thaumcraft mob standing in a row (with no AI, so they hold still), logging any that fail to spawn. */
	private static void mobsScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		server.runCommand("difficulty normal");
		server.runCommand("fill -90 99 -80 -30 106 -70 air");
		server.runCommand("fill -90 98 -80 -30 98 -70 grass_block");
		server.runCommand("fill -90 107 -80 -30 107 -70 stone");
		server.runOnServer(srv -> {
			var level = srv.overworld();
			int i = 0;
			for (var egg : dev.neothaum.entity.TVEntities.EGGS) {
				var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(dev.neothaum.NeoThaum.id(egg.name()));
				var e = type.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				if (e == null) {
					NeoThaum.LOGGER.info("[visual-test] mobs: {} failed to spawn", egg.name());
					continue;
				}
				e.snapTo(-88 + i * 2.5, 99, -74, 180.0F, 0.0F);
				if (e instanceof net.minecraft.world.entity.Mob m) {
					m.setNoAi(true);
					m.setYBodyRot(180.0F);
					m.setYHeadRot(180.0F);
					m.setPersistenceRequired();
					if (m instanceof dev.neothaum.entity.monster.Pech p) {
						p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW));
					}
					if (m instanceof dev.neothaum.entity.monster.FireBat bat) {
						bat.setIsBatHanging(false);
					}
				}
				level.addFreshEntity(e);
				i++;
			}
			NeoThaum.LOGGER.info("[visual-test] mobs: spawned {}", i);
		});
		server.runCommand("gamemode spectator @a");
		server.runCommand("time set noon");
		for (int x = -82, n = 0; x <= -40; x += 12, n++) {
			server.runCommand("tp @a " + x + " 101 -81 0 12");
			sp.getConnection().waitForChunksRender();
			ctx.waitTicks(n == 0 ? 40 : 15);
			shot(ctx, "mobs_" + n);
		}
		server.runCommand("tp @a -78 100.2 -76.5 0 10");
		ctx.waitTicks(20);
		shot(ctx, "mobs_pech");
	}

	/** Close-ups of arcane bellows facing up, sideways and towards the camera. */
	private static void bellowsScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		server.runCommand("fill -70 99 -70 -62 103 -62 air");
		server.runCommand("fill -70 98 -70 -62 98 -62 stone");
		server.runOnServer(srv -> {
			var level = srv.overworld();
			net.minecraft.core.Direction[] faces = {net.minecraft.core.Direction.UP, net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.SOUTH};
			for (int i = 0; i < faces.length; i++) {
				var p = new net.minecraft.core.BlockPos(-68 + i * 2, 99, -68);
				level.setBlock(p, dev.neothaum.registry.TVBlocks.ARCANE_BELLOWS.defaultBlockState()
					.setValue(dev.neothaum.blocks.BellowsBlock.FACING, faces[i]), 3);
			}
		});
		server.runCommand("gamemode spectator @a");
		server.runCommand("tp @a -65.5 100.6 -65.2 160 25");
		sp.getConnection().waitForChunksRender();
		ctx.waitTicks(30);
		shot(ctx, "bellows");
	}

	/** The creative inventory on each Thaumcraft tab, logging the order the tabs were laid out in. */
	private static void tabsScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		server.runCommand("gamemode creative @a");
		ctx.waitTicks(10);
		ctx.setScreen(() -> {
			var mc = net.minecraft.client.Minecraft.getInstance();
			return new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), false);
		});
		ctx.waitTicks(5);
		java.util.List<net.minecraft.world.item.CreativeModeTab> tabs = java.util.List.of(dev.neothaum.registry.TVTab.TOOLS_TAB,
			dev.neothaum.registry.TVTab.BUILDING_TAB, dev.neothaum.registry.TVTab.NATURAL_TAB, dev.neothaum.registry.TVTab.ESSENTIA_TAB,
			dev.neothaum.registry.TVTab.OTHER_TAB, dev.neothaum.devtools.DevTools.TAB);
		for (int i = 0; i < tabs.size(); i++) {
			var tab = tabs.get(i);
			ctx.runOnClient(mc -> {
				var screen = (net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen) mc.gui.screen();
				screen.setSelectedTab(tab);
				NeoThaum.LOGGER.info("[visual-test] tab {} row={} column={} page={}", tab.getDisplayName().getString(), tab.row(), tab.column(),
					screen.getPage(tab));
			});
			ctx.waitTicks(3);
			shot(ctx, "tab_" + i);
		}
		ctx.setScreen(() -> null);
	}

	/**
	 * Suction priority: three alembics (air, fire, water) feed one tube line over a row of seven jars, the nearest already holding some air. Air
	 * should top up that jar, and fire and water should each claim the next empty jar rather than fight over one.
	 */
	private static void suctionScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		server.runCommand("fill -62 99 -62 -48 102 -57 air");
		server.runCommand("fill -62 98 -62 -48 98 -57 stone");
		server.runOnServer(srv -> {
			var level = srv.overworld();
			dev.neothaum.api.aspects.Aspect[] in = {dev.neothaum.api.aspects.Aspect.AIR, dev.neothaum.api.aspects.Aspect.FIRE,
				dev.neothaum.api.aspects.Aspect.WATER};
			for (int i = 0; i < 3; i++) {
				var p = new net.minecraft.core.BlockPos(-60 + i, 100, -60);
				level.setBlock(p, dev.neothaum.registry.TVBlocks.ARCANE_ALEMBIC.defaultBlockState(), 3);
				if (level.getBlockEntity(p) instanceof dev.neothaum.tiles.AlembicBlockEntity al) {
					al.facing = net.minecraft.core.Direction.NORTH;
					al.addToContainer(in[i], 20);
				}
			}
			for (int x = -60; x <= -50; x++) {
				level.setBlock(new net.minecraft.core.BlockPos(x, 100, -59), dev.neothaum.registry.TVBlocks.ESSENTIA_TUBE.defaultBlockState(), 3);
			}
			for (int x = -56; x <= -50; x++) {
				level.setBlock(new net.minecraft.core.BlockPos(x, 99, -59), dev.neothaum.registry.TVBlocks.WARDED_JAR.defaultBlockState(), 3);
			}
			if (level.getBlockEntity(new net.minecraft.core.BlockPos(-56, 99, -59)) instanceof dev.neothaum.tiles.JarBlockEntity j) {
				j.addToContainer(dev.neothaum.api.aspects.Aspect.AIR, 5);
			}
			for (int x = -61; x <= -49; x++) {
				for (int z = -61; z <= -58; z++) {
					dev.neothaum.blocks.TubeBlock.refresh(level, new net.minecraft.core.BlockPos(x, 100, z));
				}
			}
		});
		ctx.waitTicks(900);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			StringBuilder sb = new StringBuilder();
			for (int x = -56; x <= -50; x++) {
				if (level.getBlockEntity(new net.minecraft.core.BlockPos(x, 99, -59)) instanceof dev.neothaum.tiles.JarBlockEntity j) {
					sb.append(' ').append(j.aspect == null ? "-" : j.aspect.getTag()).append('=').append(j.amount);
				}
			}
			for (int i = 0; i < 3; i++) {
				if (level.getBlockEntity(new net.minecraft.core.BlockPos(-60 + i, 100, -60)) instanceof dev.neothaum.tiles.AlembicBlockEntity al) {
					sb.append(" alembic").append(i).append('=').append(al.amount);
				}
			}
			NeoThaum.LOGGER.info("[visual-test] suction: jars{}", sb);
		});
		// goggles readout over the air jar's face
		server.runCommand("gamemode creative @a");
		server.runCommand("item replace entity @a armor.head with neothaum:goggles_of_revealing");
		server.runCommand("tp @a -55.5 99 -56.5 180 30");
		sp.getConnection().waitForChunksRender();
		ctx.waitTicks(40);
		shot(ctx, "goggles_jar");
	}

	/**
	 * Furnace and alembic piped into a jar (logs whether essentia moved), a labelled jar, a void jar, and a row of every tube kind between two
	 * jars.
	 */
	private static void essentiaScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		server.runCommand("gamerule sendCommandFeedback false");
		server.runCommand("fill -44 99 -48 -28 106 -36 air");
		server.runCommand("fill -44 98 -48 -28 98 -36 stone");
		server.runCommand("fill -44 99 -49 -28 106 -49 black_concrete");
		server.runOnServer(srv -> {
			var level = srv.overworld();
			java.util.function.BiConsumer<net.minecraft.core.BlockPos, net.minecraft.world.level.block.Block> set = (p, b) -> level.setBlock(p,
				b.defaultBlockState(), 3);
			var furnace = new net.minecraft.core.BlockPos(-38, 99, -44);
			set.accept(furnace, dev.neothaum.registry.TVBlocks.ALCHEMICAL_FURNACE);
			set.accept(furnace.above(), dev.neothaum.registry.TVBlocks.ARCANE_ALEMBIC);
			set.accept(furnace.above().east(), dev.neothaum.registry.TVBlocks.ESSENTIA_TUBE);
			set.accept(furnace.above().east(2), dev.neothaum.registry.TVBlocks.ESSENTIA_TUBE);
			set.accept(furnace.east(2), dev.neothaum.registry.TVBlocks.WARDED_JAR);
			if (level.getBlockEntity(furnace) instanceof dev.neothaum.tiles.AlchemyFurnaceBlockEntity f) {
				f.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_LOG, 16));
				f.setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, 8));
			}
			if (level.getBlockEntity(furnace.above()) instanceof dev.neothaum.tiles.AlembicBlockEntity al) {
				al.facing = net.minecraft.core.Direction.SOUTH;
				al.addToContainer(dev.neothaum.api.aspects.Aspect.FIRE, 20);
			}
			// a labelled, part-full jar and a void jar
			var labelled = new net.minecraft.core.BlockPos(-41, 99, -44);
			set.accept(labelled, dev.neothaum.registry.TVBlocks.WARDED_JAR);
			if (level.getBlockEntity(labelled) instanceof dev.neothaum.tiles.JarBlockEntity j) {
				j.facing = net.minecraft.core.Direction.SOUTH;
				j.aspectFilter = dev.neothaum.api.aspects.Aspect.WATER;
				j.addToContainer(dev.neothaum.api.aspects.Aspect.WATER, 40);
			}
			set.accept(labelled.west(), dev.neothaum.registry.TVBlocks.VOID_JAR);
			set.accept(labelled.east(), dev.neothaum.registry.TVBlocks.BRAIN_IN_A_JAR);
			if (level.getBlockEntity(labelled.west()) instanceof dev.neothaum.tiles.JarBlockEntity j) {
				j.addToContainer(dev.neothaum.api.aspects.Aspect.ENTROPY, 64);
			}
			// every tube kind in a row between two jars' tops
			net.minecraft.world.level.block.Block[] row = {dev.neothaum.registry.TVBlocks.ESSENTIA_TUBE, dev.neothaum.registry.TVBlocks.ESSENTIA_VALVE,
				dev.neothaum.registry.TVBlocks.FILTERED_ESSENTIA_TUBE, dev.neothaum.registry.TVBlocks.RESTRICTED_ESSENTIA_TUBE,
				dev.neothaum.registry.TVBlocks.DIRECTIONAL_ESSENTIA_TUBE};
			for (int i = 0; i < row.length; i++) {
				set.accept(new net.minecraft.core.BlockPos(-41 + i, 100, -41), row[i]);
			}
			set.accept(new net.minecraft.core.BlockPos(-41, 99, -41), dev.neothaum.registry.TVBlocks.WARDED_JAR);
			set.accept(new net.minecraft.core.BlockPos(-37, 99, -41), dev.neothaum.registry.TVBlocks.WARDED_JAR);
			if (level.getBlockEntity(new net.minecraft.core.BlockPos(-39, 100, -41)) instanceof dev.neothaum.tiles.TubeBlockEntity t) {
				t.aspectFilter = dev.neothaum.api.aspects.Aspect.AIR;
			}
			if (level.getBlockEntity(new net.minecraft.core.BlockPos(-40, 100, -41)) instanceof dev.neothaum.tiles.TubeBlockEntity t) {
				t.facing = net.minecraft.core.Direction.SOUTH;
			}
			if (level.getBlockEntity(new net.minecraft.core.BlockPos(-37, 100, -41)) instanceof dev.neothaum.tiles.TubeBlockEntity t) {
				t.facing = net.minecraft.core.Direction.EAST;
			}
			// a bellows blowing into the furnace, and a buffer with its west side choked
			level.setBlock(furnace.west(), dev.neothaum.registry.TVBlocks.ARCANE_BELLOWS.defaultBlockState()
				.setValue(dev.neothaum.blocks.BellowsBlock.FACING, net.minecraft.core.Direction.EAST), 3);
			set.accept(new net.minecraft.core.BlockPos(-36, 100, -41), dev.neothaum.registry.TVBlocks.ESSENTIA_BUFFER);
			if (level.getBlockEntity(new net.minecraft.core.BlockPos(-36, 100, -41)) instanceof dev.neothaum.tiles.BufferBlockEntity b) {
				b.chokedSides[net.minecraft.core.Direction.WEST.ordinal()] = 1;
				b.chokedSides[net.minecraft.core.Direction.DOWN.ordinal()] = 2;
			}
			set.accept(new net.minecraft.core.BlockPos(-36, 99, -41), dev.neothaum.registry.TVBlocks.WARDED_JAR);
			for (int x = -42; x <= -34; x++) {
				for (int y = 99; y <= 100; y++) {
					for (int z = -45; z <= -40; z++) {
						dev.neothaum.blocks.TubeBlock.refresh(level, new net.minecraft.core.BlockPos(x, y, z));
					}
				}
			}
		});
		server.runCommand("gamemode spectator @a");
		server.runCommand("tp @a -38.5 101.4 -39.6 180 32");
		sp.getConnection().waitForChunksRender();
		ctx.waitTicks(120);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var jar = level.getBlockEntity(new net.minecraft.core.BlockPos(-36, 99, -44));
			var al = level.getBlockEntity(new net.minecraft.core.BlockPos(-38, 100, -44));
			var f = level.getBlockEntity(new net.minecraft.core.BlockPos(-38, 99, -44));
			NeoThaum.LOGGER.info("[visual-test] essentia: bellows on furnace={}", dev.neothaum.tiles.BellowsBlockEntity.getBellows(level,
				new net.minecraft.core.BlockPos(-38, 99, -44), net.minecraft.core.Direction.values()));
			NeoThaum.LOGGER.info("[visual-test] essentia: jar={} {} alembic={} {} furnace burning={} vis={} cook={}",
				jar instanceof dev.neothaum.tiles.JarBlockEntity j ? j.aspect : null, jar instanceof dev.neothaum.tiles.JarBlockEntity j ? j.amount : -1,
				al instanceof dev.neothaum.tiles.AlembicBlockEntity a ? a.aspect : null, al instanceof dev.neothaum.tiles.AlembicBlockEntity a ? a.amount : -1,
				f instanceof dev.neothaum.tiles.AlchemyFurnaceBlockEntity af && af.isBurning(), f instanceof dev.neothaum.tiles.AlchemyFurnaceBlockEntity af ? af.vis : -1,
				f instanceof dev.neothaum.tiles.AlchemyFurnaceBlockEntity af ? af.furnaceCookTime : -1);
		});
		shot(ctx, "essentia");
		server.runCommand("tp @a -38.5 101.2 -38.3 180 40");
		ctx.waitTicks(10);
		shot(ctx, "essentia_tubes");
		server.runCommand("gamemode survival @a");
		server.runCommand("clear @a");
		for (String it : new String[]{"warded_jar", "void_jar", "essentia_tube", "essentia_valve", "filtered_essentia_tube", "restricted_essentia_tube",
			"directional_essentia_tube", "essentia_buffer", "alchemical_furnace", "arcane_alembic", "arcane_bellows"}) {
			server.runCommand("give @a neothaum:" + it);
		}
		server.runCommand("give @a neothaum:jar_label[neothaum:aspect_filter=\"ignis\"]");
		server.runCommand("give @a neothaum:phial_of_essentia[neothaum:aspects={\"aqua\":8}]");
		server.runCommand("give @a neothaum:glass_phial");
		server.runCommand("give @a neothaum:vis_filter");
		ctx.waitTicks(5);
		ctx.setScreen(() -> new net.minecraft.client.gui.screens.inventory.InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
		ctx.waitTicks(5);
		shot(ctx, "essentia_items");
		ctx.setScreen(() -> null);
		server.runCommand("clear @a");
		server.runCommand("gamemode creative @a");
		server.runCommand("gamerule sendCommandFeedback true");
	}

	/** An infusion altar wanded into being, then infusing an inert thaumium cap; logs each stage and the result. */
	private static void infusionScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		server.runCommand("fill -84 99 -114 -62 108 -92 air");
		server.runCommand("fill -84 98 -114 -62 98 -92 stone");
		var c = new net.minecraft.core.BlockPos(-73, 99, -103);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var player = srv.getPlayerList().getPlayers().get(0);
			for (String key : new String[]{"INFUSION", "CAP_thaumium", "INFUSIONENCHANTMENT"}) {
				ResearchManager.completeResearch(player, key);
			}
			level.setBlockAndUpdate(c, dev.neothaum.registry.TVBlocks.ARCANE_PEDESTAL.defaultBlockState());
			for (int[] k : new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
				level.setBlockAndUpdate(c.offset(k[0], 0, k[1]), dev.neothaum.registry.TVBlocks.ARCANE_STONE_BRICKS.defaultBlockState());
				level.setBlockAndUpdate(c.offset(k[0], 1, k[1]), dev.neothaum.registry.TVBlocks.ARCANE_STONE_BLOCK.defaultBlockState());
			}
			level.setBlockAndUpdate(c.above(2), dev.neothaum.registry.TVBlocks.RUNIC_MATRIX.defaultBlockState());
			// three pedestals of salis mundus around it, and jars of what the cap needs
			for (int[] k : new int[][]{{3, 0}, {-3, 0}, {0, 3}}) {
				var p = c.offset(k[0], 0, k[1]);
				level.setBlockAndUpdate(p, dev.neothaum.registry.TVBlocks.ARCANE_PEDESTAL.defaultBlockState());
				if (level.getBlockEntity(p) instanceof dev.neothaum.tiles.PedestalBlockEntity ped) {
					ped.setItem(0, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.SALIS_MUNDUS));
				}
			}
			if (level.getBlockEntity(c) instanceof dev.neothaum.tiles.PedestalBlockEntity ped) {
				ped.setItem(0, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.THAUMIUM_CAP_INERT));
			}
			String[][] jars = {{"potentia", "-77", "-100"}, {"auram", "-69", "-100"}};
			for (String[] j : jars) {
				var p = new net.minecraft.core.BlockPos(Integer.parseInt(j[1]), 99, Integer.parseInt(j[2]));
				level.setBlockAndUpdate(p, dev.neothaum.registry.TVBlocks.WARDED_JAR.defaultBlockState());
				if (level.getBlockEntity(p) instanceof dev.neothaum.tiles.JarBlockEntity jar) {
					jar.facing = net.minecraft.core.Direction.SOUTH;
					jar.addToContainer(dev.neothaum.api.aspects.Aspect.getAspect(j[0]), 32);
				}
			}
			// a pair of candles as stabilisers
			level.setBlockAndUpdate(c.offset(-2, 0, 2), net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(NeoThaum.id("white_tallow_candle")).defaultBlockState());
			level.setBlockAndUpdate(c.offset(2, 0, 2), net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(NeoThaum.id("white_tallow_candle")).defaultBlockState());
			var wand = dev.neothaum.items.WandItem.createFull(dev.neothaum.registry.TVItems.WAND, "thaumium", "silverwood");
			var mpos = c.above(2);
			var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(mpos), net.minecraft.core.Direction.UP, mpos, false);
			if (level.getBlockEntity(mpos) instanceof dev.neothaum.tiles.InfusionMatrixBlockEntity m) {
				boolean formed = m.onWandRightClick(level, wand, player, mpos, net.minecraft.core.Direction.UP, hit);
				NeoThaum.LOGGER.info("[visual-test] infusion: altar formed={} active={} valid={} pillar={}", formed, m.active, m.validLocation(),
					level.getBlockState(c.offset(1, 0, 1)));
			}
		});
		server.runCommand("gamemode spectator @a");
		server.runCommand("tp @a -73.5 102.5 -96.5 180 25");
		sp.getConnection().waitForChunksRender();
		ctx.waitTicks(30);
		shot(ctx, "infusion_altar");
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var player = srv.getPlayerList().getPlayers().get(0);
			var mpos = c.above(2);
			var wand = dev.neothaum.items.WandItem.createFull(dev.neothaum.registry.TVItems.WAND, "thaumium", "silverwood");
			var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(mpos), net.minecraft.core.Direction.UP, mpos, false);
			if (level.getBlockEntity(mpos) instanceof dev.neothaum.tiles.InfusionMatrixBlockEntity m) {
				m.onWandRightClick(level, wand, player, mpos, net.minecraft.core.Direction.UP, hit);
				NeoThaum.LOGGER.info("[visual-test] infusion: crafting={} instability={} symmetry={} needs={}", m.crafting, m.instability, m.symmetry,
					m.getAspects());
			}
		});
		ctx.waitTicks(80);
		shot(ctx, "infusion_crafting");
		for (int i = 0; i < 8; i++) {
			ctx.waitTicks(100);
			server.runOnServer(srv -> {
				var level = srv.overworld();
				if (level.getBlockEntity(c.above(2)) instanceof dev.neothaum.tiles.InfusionMatrixBlockEntity mm) {
					NeoThaum.LOGGER.info("[visual-test] infusion: t={} crafting={} instability={} needs={}", level.getGameTime(), mm.crafting,
						mm.instability, mm.getAspects());
				}
			});
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var m = level.getBlockEntity(c.above(2)) instanceof dev.neothaum.tiles.InfusionMatrixBlockEntity mm ? mm : null;
			var ped = level.getBlockEntity(c) instanceof dev.neothaum.tiles.PedestalBlockEntity p ? p : null;
			NeoThaum.LOGGER.info("[visual-test] infusion: done crafting={} needs={} central={}", m == null ? null : m.crafting,
				m == null ? null : m.getAspects(), ped == null ? null : ped.getItem());
		});
		shot(ctx, "infusion_done");
		// the book's infusion and infusion enchantment pages
		for (String[] pg : new String[][]{{"CAP_thaumium", "2"}, {"INFUSIONENCHANTMENT", "6"}}) {
			ResearchItem ri = ctx.computeOnClient(mc -> ResearchManager.getResearch(pg[0]));
			if (ri != null) {
				int page = Integer.parseInt(pg[1]);
				ctx.setScreen(() -> new ResearchPagesScreen(ri, page, 0, 0));
				ctx.waitTicks(5);
				shot(ctx, "page_" + pg[0].toLowerCase());
			}
		}
		ctx.setScreen(() -> null);
		server.runCommand("gamemode creative @a");
	}

	/** Monsters hunt and hurt on easy and vanish in peaceful; tainted animals stay, and an animal dying of flux taint still turns. */
	private static void peacefulScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		server.runOnServer(srv -> NeoThaum.LOGGER.info("[visual-test] silver: CAP_silver research={} CAP_gold={}",
			dev.neothaum.research.ResearchManager.getResearch("CAP_silver"), dev.neothaum.research.ResearchManager.getResearch("CAP_gold") != null));
		for (String cmd : new String[]{"difficulty easy", "gamemode survival @a", "tp @a 0 120 0 0 0", "fill -8 117 -8 8 125 12 air",
			"fill -8 116 -8 8 116 12 stone", "effect give @a resistance 60 4 true",
			"summon neothaum:brainy_zombie 2 117 6", "summon neothaum:brainy_zombie -2 117 6 {IsBaby:1b}",
			"summon neothaum:taint_swarm 0 118 3", "summon neothaum:tainted_cow 4 117 8 {NoAI:1b}", "summon minecraft:cow -4 117 8 {NoAI:1b}"}) {
			server.runCommand(cmd);
		}
		sp.getConnection().waitForChunksRender();
		ctx.getInput().lookAt(new net.minecraft.core.BlockPos(0, 118, 6));
		ctx.waitTicks(10);
		shot(ctx, "zombies_adult_baby");
		ctx.waitTicks(80);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var player = srv.getPlayerList().getPlayers().get(0);
			for (var z : level.getEntitiesOfClass(dev.neothaum.entity.monster.BrainyZombie.class, player.getBoundingBox().inflate(32))) {
				NeoThaum.LOGGER.info("[visual-test] peaceful: easy zombie baby={} target={}", z.isBaby(), z.getTarget() == player);
			}
			NeoThaum.LOGGER.info("[visual-test] peaceful: easy player health={} hurtBy={}", player.getHealth(),
				player.getLastHurtByMob() == null ? null : player.getLastHurtByMob().getType());
		});
		server.runCommand("difficulty peaceful");
		ctx.waitTicks(5);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var player = srv.getPlayerList().getPlayers().get(0);
			var box = player.getBoundingBox().inflate(32);
			NeoThaum.LOGGER.info("[visual-test] peaceful: zombies={} swarms={} taintedCows={}",
				level.getEntitiesOfClass(dev.neothaum.entity.monster.BrainyZombie.class, box).size(),
				level.getEntitiesOfClass(dev.neothaum.entity.monster.TaintSwarm.class, box).size(),
				level.getEntitiesOfClass(dev.neothaum.entity.monster.TaintCow.class, box).size());
			for (var cow : level.getEntitiesOfClass(net.minecraft.world.entity.animal.cow.Cow.class, box)) {
				cow.addEffect(new net.minecraft.world.effect.MobEffectInstance(dev.neothaum.registry.TVMobEffects.FLUX_TAINT, 200, 0));
				cow.hurtServer(level, level.damageSources().magic(), 100.0F);
			}
		});
		ctx.waitTicks(5);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var box = srv.getPlayerList().getPlayers().get(0).getBoundingBox().inflate(32);
			NeoThaum.LOGGER.info("[visual-test] peaceful: after flux death taintedCows={} cows={}",
				level.getEntitiesOfClass(dev.neothaum.entity.monster.TaintCow.class, box).size(),
				level.getEntitiesOfClass(net.minecraft.world.entity.animal.cow.Cow.class, box).size());
		});
		server.runCommand("difficulty easy");
		server.runCommand("gamemode creative @a");
	}

	private static net.minecraft.world.item.ItemStack wand(String cap, String rod, net.minecraft.world.item.Item focus, boolean sceptre) {
		var w = dev.neothaum.items.WandItem.createFull(dev.neothaum.registry.TVItems.WAND, cap, rod);
		if (sceptre) {
			w.set(dev.neothaum.registry.TVComponents.SCEPTRE, true);
		}
		if (focus != null) {
			dev.neothaum.items.WandItem.setFocus(w, new net.minecraft.world.item.ItemStack(focus));
		}
		return w;
	}

	/** The wand model in every view, the foci on it, the radial menu, a few casts and the focus pouch. */
	private static void wandsScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		var I = dev.neothaum.registry.TVItems.class;
		for (String cmd : new String[]{"gamemode creative @a", "tp @a 0 120 0 180 0", "fill -8 117 -12 8 125 4 air", "fill -8 116 -12 8 116 4 stone",
			"fill -3 117 -10 3 121 -10 stone", "clear @a"}) {
			server.runCommand(cmd);
		}
		server.runOnServer(srv -> {
			var p = srv.getPlayerList().getPlayers().get(0);
			var inv = p.getInventory();
			inv.setItem(0, wand("iron", "wood", null, false));
			inv.setItem(1, wand("gold", "greatwood", dev.neothaum.registry.TVItems.FOCUS_FIRE, false));
			inv.setItem(2, wand("thaumium", "silverwood", dev.neothaum.registry.TVItems.FOCUS_WARDING, false));
			inv.setItem(3, wand("void", "silverwood", dev.neothaum.registry.TVItems.FOCUS_PRIMAL, true));
			inv.setItem(4, wand("thaumium", "greatwood_staff", dev.neothaum.registry.TVItems.FOCUS_TRADE, false));
			inv.setItem(5, wand("void", "primal_staff", dev.neothaum.registry.TVItems.FOCUS_HELLBAT, false));
			inv.setItem(6, wand("silver", "blaze", dev.neothaum.registry.TVItems.FOCUS_SHOCK, false));
			inv.setItem(7, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_FROST));
			inv.setItem(8, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_POUCH));
			inv.setItem(9, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_EXCAVATION));
			inv.setItem(10, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_PECH));
			inv.setItem(11, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_PORTABLE_HOLE));
			inv.setSelectedSlot(1);
		});
		sp.getConnection().waitForChunksRender();
		ctx.waitTicks(10);
		shot(ctx, "wand_firstperson");
		// the inventory icons
		ctx.setScreen(() -> new net.minecraft.client.gui.screens.inventory.InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
		ctx.waitTicks(5);
		shot(ctx, "wand_inventory");
		ctx.setScreen(() -> null);
		// held in third person, each wand in turn
		ctx.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
		for (int slot : new int[]{0, 3, 5}) {
			final int sl = slot;
			server.runOnServer(srv -> srv.getPlayerList().getPlayers().get(0).getInventory().setSelectedSlot(sl));
			ctx.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(sl));
			ctx.waitTicks(10);
			shot(ctx, "wand_thirdperson_" + slot);
		}
		ctx.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
		// staff in first person
		ctx.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(5));
		server.runOnServer(srv -> srv.getPlayerList().getPlayers().get(0).getInventory().setSelectedSlot(5));
		ctx.waitTicks(10);
		shot(ctx, "staff_firstperson");
		// the radial menu: hold F with a wand
		ctx.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(1));
		server.runOnServer(srv -> srv.getPlayerList().getPlayers().get(0).getInventory().setSelectedSlot(1));
		ctx.waitTicks(5);
		ctx.getInput().holdKey(dev.neothaum.client.hud.FocusRadial.keyF);
		ctx.waitTicks(30);
		shot(ctx, "focus_radial");
		ctx.getInput().releaseKey(dev.neothaum.client.hud.FocusRadial.keyF);
		ctx.waitTicks(30);
		// change focus through the server handler directly: fire -> the next one along
		server.runOnServer(srv -> {
			var p = srv.getPlayerList().getPlayers().get(0);
			var held = p.getMainHandItem();
			dev.neothaum.wands.WandFoci.changeFocus(held, p, "BF");
			NeoThaum.LOGGER.info("[visual-test] wands: after change focus={} slot7={}", dev.neothaum.items.WandItem.getFocusStack(held).getItem(),
				p.getInventory().getItem(7).getItem());
		});
		// casts: fire spray (held), frost shard, shock bolt
		server.runOnServer(srv -> {
			var p = srv.getPlayerList().getPlayers().get(0);
			p.getInventory().setItem(1, wand("thaumium", "silverwood", dev.neothaum.registry.TVItems.FOCUS_FIRE, false));
		});
		ctx.waitTicks(5);
		ctx.getInput().holdKey(o -> o.keyUse);
		ctx.waitTicks(20);
		shot(ctx, "cast_fire");
		ctx.getInput().releaseKey(o -> o.keyUse);
		server.runOnServer(srv -> srv.getPlayerList().getPlayers().get(0).getInventory().setItem(1,
			wand("thaumium", "silverwood", dev.neothaum.registry.TVItems.FOCUS_SHOCK, false)));
		ctx.waitTicks(5);
		ctx.getInput().holdKey(o -> o.keyUse);
		ctx.waitTicks(10);
		shot(ctx, "cast_shock");
		ctx.getInput().releaseKey(o -> o.keyUse);
		server.runOnServer(srv -> srv.getPlayerList().getPlayers().get(0).getInventory().setItem(1,
			wand("thaumium", "silverwood", dev.neothaum.registry.TVItems.FOCUS_EXCAVATION, false)));
		ctx.getInput().lookAt(new net.minecraft.core.BlockPos(0, 118, -10));
		ctx.waitTicks(5);
		ctx.getInput().holdKey(o -> o.keyUse);
		ctx.waitTicks(30);
		shot(ctx, "cast_excavation");
		ctx.getInput().releaseKey(o -> o.keyUse);
		for (var f : new net.minecraft.world.item.Item[]{dev.neothaum.registry.TVItems.FOCUS_FROST, dev.neothaum.registry.TVItems.FOCUS_PRIMAL,
			dev.neothaum.registry.TVItems.FOCUS_PECH}) {
			server.runOnServer(srv -> srv.getPlayerList().getPlayers().get(0).getInventory().setItem(1, wand("thaumium", "silverwood", f, false)));
			ctx.waitTicks(25);
			ctx.getInput().pressKey(o -> o.keyUse);
			ctx.waitTicks(4);
			shot(ctx, "cast_" + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(f).getPath());
		}
		// portable hole through the wall, and warding on it (from within reach)
		server.runCommand("tp @a 0 117 -7 180 0");
		ctx.waitTicks(10);
		server.runOnServer(srv -> srv.getPlayerList().getPlayers().get(0).getInventory().setItem(1,
			wand("thaumium", "silverwood", dev.neothaum.registry.TVItems.FOCUS_PORTABLE_HOLE, false)));
		ctx.getInput().lookAt(new net.minecraft.core.BlockPos(2, 118, -10));
		ctx.waitTicks(25);
		ctx.getInput().pressKey(o -> o.keyUse);
		ctx.waitTicks(10);
		server.runOnServer(srv -> {
			StringBuilder sb = new StringBuilder();
			for (var bp : net.minecraft.core.BlockPos.betweenClosed(-3, 117, -11, 3, 121, -9)) {
				if (srv.overworld().getBlockState(bp).is(dev.neothaum.registry.TVBlocks.EMPTY_SPACE)) {
					sb.append(bp.toShortString()).append(' ');
				}
			}
			var hit = srv.getPlayerList().getPlayers().get(0).pick(5.0, 1.0F, false);
			NeoThaum.LOGGER.info("[visual-test] wands: holes={} looking at={}", sb, hit instanceof net.minecraft.world.phys.BlockHitResult bh ? bh.getBlockPos() : hit);
		});
		shot(ctx, "cast_hole");
		ctx.waitTicks(140);
		server.runOnServer(srv -> {
			var p = srv.getPlayerList().getPlayers().get(0);
			p.getInventory().setItem(1, wand("thaumium", "silverwood", dev.neothaum.registry.TVItems.FOCUS_WARDING, false));
			NeoThaum.LOGGER.info("[visual-test] wands: wall back={}", srv.overworld().getBlockState(new net.minecraft.core.BlockPos(2, 118, -10)));
		});
		ctx.getInput().lookAt(new net.minecraft.core.BlockPos(-2, 119, -10));
		ctx.waitTicks(25);
		ctx.getInput().pressKey(o -> o.keyUse);
		ctx.waitTicks(10);
		server.runOnServer(srv -> NeoThaum.LOGGER.info("[visual-test] wands: warded={}", srv.overworld().getBlockState(
			new net.minecraft.core.BlockPos(-2, 119, -10))));
		shot(ctx, "cast_warding");
		// the pouch screen
		server.runOnServer(srv -> {
			var p = srv.getPlayerList().getPlayers().get(0);
			var pouch = new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_POUCH);
			dev.neothaum.items.FocusPouchItem.setInventory(pouch, java.util.List.of(new net.minecraft.world.item.ItemStack(
				dev.neothaum.registry.TVItems.FOCUS_FROST), new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_SHOCK)));
			p.getInventory().setItem(8, pouch);
			p.getInventory().setSelectedSlot(8);
		});
		ctx.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(8));
		ctx.waitTicks(5);
		ctx.getInput().pressKey(o -> o.keyUse);
		ctx.waitTicks(10);
		shot(ctx, "focus_pouch");
		ctx.setScreen(() -> null);
	}

	/** Node, stabilizer and transducer make an energized node; a relay feeds the focal manipulator; a wand pedestal drinks from a node. */
	private static void visnetScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		var c = new net.minecraft.core.BlockPos(0, 117, -6);
		for (String cmd : new String[]{"gamemode creative @a", "tp @a 0 120 2 180 20", "fill -8 117 -14 8 125 4 air", "fill -8 116 -14 8 116 4 stone",
			"clear @a"}) {
			server.runCommand(cmd);
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			level.setBlock(c, dev.neothaum.registry.TVBlocks.NODE_STABILIZER.defaultBlockState(), 3);
			dev.neothaum.world.NodeGenerator.createRandomNodeAt(level, c.above(), level.getRandom(), false, false, false, true,
				dev.neothaum.api.nodes.NodeType.NORMAL);
			level.setBlock(c.above(2), dev.neothaum.registry.TVBlocks.NODE_TRANSDUCER.defaultBlockState(), 3);
			level.setBlock(c.above(3), net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
			level.setBlock(c.offset(4, 0, 0), dev.neothaum.registry.TVBlocks.VIS_RELAY.defaultBlockState(), 3);
			level.setBlock(c.offset(-4, 0, 0), dev.neothaum.registry.TVBlocks.FOCAL_MANIPULATOR.defaultBlockState(), 3);
			level.setBlock(c.offset(0, 0, 4), dev.neothaum.registry.TVBlocks.WAND_RECHARGE_PEDESTAL.defaultBlockState(), 3);
			if (level.getBlockEntity(c.offset(0, 0, 4)) instanceof dev.neothaum.tiles.WandPedestalBlockEntity ped) {
				ped.setItem(0, dev.neothaum.items.WandItem.create(dev.neothaum.registry.TVItems.WAND, "gold", "greatwood"));
			}
			if (level.getBlockEntity(c.offset(-4, 0, 0)) instanceof dev.neothaum.tiles.FocalManipulatorBlockEntity fm) {
				fm.setItem(0, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_FIRE));
			}
		});
		// stabilizer node first, before the transducer has run its course
		sp.getConnection().waitForChunksRender();
		ctx.getInput().lookAt(c.above());
		ctx.waitTicks(60);
		shot(ctx, "visnet_energizing");
		server.runCommand("tick sprint 2000");
		for (int i = 0; i < 40; i++) {
			ctx.waitTicks(40);
			boolean done = server.computeOnServer(srv -> srv.overworld().getBlockState(c.above()).is(dev.neothaum.registry.TVBlocks.ENERGIZED_AURA_NODE));
			if (done) {
				break;
			}
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			NeoThaum.LOGGER.info("[visual-test] visnet: node now={} transducer={}", level.getBlockState(c.above()).getBlock(),
				level.getBlockEntity(c.above(2)) instanceof dev.neothaum.tiles.NodeTransducerBlockEntity t ? t.status + "/" + t.count : null);
			var p = srv.getPlayerList().getPlayers().get(0);
			if (level.getBlockEntity(c.offset(-4, 0, 0)) instanceof dev.neothaum.tiles.FocalManipulatorBlockEntity fm) {
				boolean started = fm.startCraft(dev.neothaum.api.wands.FocusUpgradeType.potency.id, p);
				NeoThaum.LOGGER.info("[visual-test] visnet: manipulator started={} price={}", started, fm.aspects);
			}
		});
		ctx.waitTicks(400);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			if (level.getBlockEntity(c.offset(-4, 0, 0)) instanceof dev.neothaum.tiles.FocalManipulatorBlockEntity fm) {
				NeoThaum.LOGGER.info("[visual-test] visnet: manipulator left={} focus upgrades={}", fm.aspects,
					java.util.Arrays.toString(dev.neothaum.api.wands.FocusItem.getAppliedUpgrades(fm.getItem(0))));
			}
			if (level.getBlockEntity(c.offset(0, 0, 4)) instanceof dev.neothaum.tiles.WandPedestalBlockEntity ped) {
				NeoThaum.LOGGER.info("[visual-test] visnet: pedestal wand vis={}", dev.neothaum.items.WandItem.getAllVis(ped.getItem()));
			}
			if (level.getBlockEntity(c.offset(4, 0, 0)) instanceof dev.neothaum.tiles.VisRelayBlockEntity r) {
				NeoThaum.LOGGER.info("[visual-test] visnet: relay parent={}", r.getParent() == null ? null : r.getParent().get());
			}
		});
		ctx.getInput().lookAt(c.above());
		ctx.waitTicks(20);
		shot(ctx, "visnet_done");
		server.runCommand("tick sprint stop");
		// the manipulator's screen
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var p = srv.getPlayerList().getPlayers().get(0);
			if (level.getBlockEntity(c.offset(-4, 0, 0)) instanceof dev.neothaum.tiles.FocalManipulatorBlockEntity fm) {
				p.openMenu(fm);
			}
		});
		ctx.waitTicks(10);
		shot(ctx, "focal_manipulator_gui");
		ctx.setScreen(() -> null);
	}

	/** Catching a node in a jar with a wand, the jar in the world and in hand, freeing it, and pure, sinister and tainted nodes changing the land. */
	private static void nodeJarScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		var c = new net.minecraft.core.BlockPos(0, 118, -6);
		for (String cmd : new String[]{"gamemode creative @a", "tp @a 0 120 0 180 20", "fill -40 117 -46 40 130 34 air",
			"fill -40 116 -46 40 116 34 grass_block", "clear @a", "fill -1 117 -7 1 119 -5 glass", "fill -1 120 -7 1 120 -5 oak_slab"}) {
			server.runCommand(cmd);
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var p = srv.getPlayerList().getPlayers().get(0);
			ResearchManager.completeResearch(p, "NODEJAR");
			level.setBlock(c, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
			dev.neothaum.world.NodeGenerator.createRandomNodeAt(level, c, level.getRandom(), false, false, false, true,
				dev.neothaum.api.nodes.NodeType.NORMAL);
			if (level.getBlockEntity(c) instanceof dev.neothaum.tiles.NodeBlockEntity n) {
				NeoThaum.LOGGER.info("[visual-test] nodejar: before type={} mod={} aspects={}", n.getNodeType(), n.getNodeModifier(), n.getAspects());
			}
			var w = wand("thaumium", "silverwood", null, false);
			p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, w);
			boolean caught = dev.neothaum.wands.WandManager.performTrigger(level, w, p, c.above(), net.minecraft.core.Direction.UP,
				net.minecraft.world.InteractionHand.MAIN_HAND);
			NeoThaum.LOGGER.info("[visual-test] nodejar: caught={} block={} glass left={}", caught, level.getBlockState(c).getBlock(),
				level.getBlockState(c.above()).getBlock());
			if (level.getBlockEntity(c) instanceof dev.neothaum.tiles.JarNodeBlockEntity j) {
				NeoThaum.LOGGER.info("[visual-test] nodejar: jar type={} mod={} aspects={}", j.getNodeType(), j.getNodeModifier(), j.getAspects());
			}
		});
		server.runCommand("tp @a 0 118 -3.5 180 20");
		ctx.waitTicks(20);
		ctx.getInput().lookAt(c);
		ctx.waitTicks(30);
		shot(ctx, "nodejar_world");
		ctx.runOnClient(mc -> NeoThaum.LOGGER.info("[visual-test] nodejar: client be={} renderer={}",
			mc.level.getBlockEntity(c) instanceof dev.neothaum.tiles.JarNodeBlockEntity j ? j.getAspects() + "/" + j.getNodeType() : mc.level.getBlockEntity(c),
			mc.level.getBlockEntity(c) == null ? null : mc.getBlockEntityRenderDispatcher().getRenderer(mc.level.getBlockEntity(c))));
		// break it: the item keeps the node; then place it back and set it free
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var p = srv.getPlayerList().getPlayers().get(0);
			var drops = net.minecraft.world.level.block.Block.getDrops(level.getBlockState(c), level, c, level.getBlockEntity(c));
			NeoThaum.LOGGER.info("[visual-test] nodejar: drop aspects={}", drops.isEmpty() ? null : drops.get(0).get(dev.neothaum.registry.TVComponents.ASPECTS));
			NeoThaum.LOGGER.info("[visual-test] nodejar: drops={} components={}", drops, drops.isEmpty() ? null : drops.get(0).getComponentsPatch());
			if (!drops.isEmpty()) {
				p.getInventory().setItem(0, drops.get(0));
				p.getInventory().setItem(1, drops.get(0).copy());
			}
			level.setBlock(c, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
		});
		ctx.waitTicks(20);
		shot(ctx, "nodejar_held");
		server.runCommand("gamemode survival @a");
		ctx.waitTicks(5);
		ctx.setScreen(() -> new net.minecraft.client.gui.screens.inventory.InventoryScreen(ctx.computeOnClient(mc -> mc.player)));
		ctx.waitTicks(10);
		shot(ctx, "nodejar_inventory");
		ctx.setScreen(() -> null);
		server.runCommand("gamemode creative @a");
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var p = srv.getPlayerList().getPlayers().get(0);
			var stack = p.getInventory().getItem(0);
			p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
			var use = new net.minecraft.world.item.context.UseOnContext(p, net.minecraft.world.InteractionHand.MAIN_HAND,
				new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(c.below(2)), net.minecraft.core.Direction.UP,
					c.below(2), false));
			var res = stack.useOn(use);
			var at = c.below();
			NeoThaum.LOGGER.info("[visual-test] nodejar: placed={} block={} data={}", res, level.getBlockState(at).getBlock(),
				level.getBlockEntity(at) instanceof dev.neothaum.tiles.JarNodeBlockEntity pj ? pj.getAspects() + "/" + pj.getNodeType() : null);
			var w = wand("thaumium", "silverwood", null, false);
			dev.neothaum.wands.WandManager.performTrigger(level, w, p, at, net.minecraft.core.Direction.UP, net.minecraft.world.InteractionHand.MAIN_HAND);
			if (level.getBlockEntity(at) instanceof dev.neothaum.tiles.NodeBlockEntity n) {
				NeoThaum.LOGGER.info("[visual-test] nodejar: freed type={} mod={} aspects={}", n.getNodeType(), n.getNodeModifier(), n.getAspects());
			} else {
				NeoThaum.LOGGER.info("[visual-test] nodejar: freed FAILED block={}", level.getBlockState(at).getBlock());
			}
			// land effects: sinister east, tainted west, pure ones south (one in tainted land, one in a silverwood trunk)
			var dark = new net.minecraft.core.BlockPos(24, 118, -6);
			var taint = new net.minecraft.core.BlockPos(-24, 118, -6);
			var pureTaint = new net.minecraft.core.BlockPos(-12, 118, 20);
			var pureTree = new net.minecraft.core.BlockPos(12, 118, 20);
			for (int x = -20; x <= -4; x++) {
				for (int z = 12; z <= 28; z++) {
					dev.neothaum.world.TVBiomes.setBiomeAt(level, x, z, dev.neothaum.world.TVBiomes.TAINTED_LAND);
				}
			}
			for (var d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
				level.setBlock(pureTree.relative(d), dev.neothaum.registry.TVBlocks.SILVERWOOD_LOG.defaultBlockState(), 3);
			}
			var al = new dev.neothaum.api.aspects.AspectList().add(dev.neothaum.api.aspects.Aspect.AIR, 30);
			dev.neothaum.world.NodeGenerator.createNodeAt(level, dark, dev.neothaum.api.nodes.NodeType.DARK, null, al.copy(), true);
			dev.neothaum.world.NodeGenerator.createNodeAt(level, taint, dev.neothaum.api.nodes.NodeType.TAINTED, null, al.copy(), true);
			dev.neothaum.world.NodeGenerator.createNodeAt(level, pureTaint, dev.neothaum.api.nodes.NodeType.PURE, null, al.copy(), true);
			dev.neothaum.world.NodeGenerator.createNodeAt(level, pureTree, dev.neothaum.api.nodes.NodeType.PURE, null, al.copy(), true);
			// a plain node in the tainted land, which may turn tainted
			dev.neothaum.world.NodeGenerator.createNodeAt(level, new net.minecraft.core.BlockPos(-8, 118, 14),
				dev.neothaum.api.nodes.NodeType.NORMAL, null, al.copy(), true);
		});
		server.runCommand("difficulty normal");
		long t0 = server.computeOnServer(srv -> srv.overworld().getGameTime());
		server.runCommand("tick sprint 6000");
		ctx.waitTicks(400);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			NeoThaum.LOGGER.info("[visual-test] nodeland: ticks run={}", level.getGameTime() - t0);
			int[][] centres = {{24, -6}, {-24, -6}, {-12, 20}, {12, 20}};
			String[] names = {"dark", "taint", "pure-in-taint", "pure-in-tree"};
			for (int i = 0; i < 4; i++) {
				int eerie = 0, tainted = 0, magic = 0, cells = 0;
				for (int x = centres[i][0] - 12; x <= centres[i][0] + 12; x += 4) {
					for (int z = centres[i][1] - 12; z <= centres[i][1] + 12; z += 4) {
						var bp = new net.minecraft.core.BlockPos(x, 118, z);
						cells++;
						eerie += dev.neothaum.world.TVBiomes.is(level, bp, dev.neothaum.world.TVBiomes.EERIE) ? 1 : 0;
						tainted += dev.neothaum.world.TVBiomes.is(level, bp, dev.neothaum.world.TVBiomes.TAINTED_LAND) ? 1 : 0;
						magic += dev.neothaum.world.TVBiomes.is(level, bp, dev.neothaum.world.TVBiomes.MAGICAL_FOREST) ? 1 : 0;
					}
				}
				NeoThaum.LOGGER.info("[visual-test] nodeland {}: of {} cells eerie={} tainted={} magical={}", names[i], cells, eerie, tainted, magic);
			}
			int fibres = 0;
			for (var bp : net.minecraft.core.BlockPos.betweenClosed(-30, 114, -12, -18, 124, 0)) {
				if (level.getBlockState(bp).is(dev.neothaum.registry.TVBlocks.TAINT_FIBRES)) {
					fibres++;
				}
			}
			int zombies = level.getEntitiesOfClass(dev.neothaum.entity.monster.GiantBrainyZombie.class,
				new net.minecraft.world.phys.AABB(24, 118, -6, 25, 119, -5).inflate(16)).size();
			var plain = level.getBlockEntity(new net.minecraft.core.BlockPos(-8, 118, 14));
			NeoThaum.LOGGER.info("[visual-test] nodeland: fibres={} giant zombies={} plain node now={}", fibres, zombies,
				plain instanceof dev.neothaum.tiles.NodeBlockEntity n ? n.getNodeType() : null);
		});
		server.runCommand("tick sprint stop");
		server.runCommand("tp @a 16 121 -6 -90 25");
		ctx.waitTicks(60);
		shot(ctx, "nodeland_eerie");
		server.runCommand("tp @a -16 121 -6 90 25");
		ctx.waitTicks(60);
		shot(ctx, "nodeland_taint");
	}

	/** Samples the climate biome map over 8192 blocks square: how much is magical forest and tainted land, and how often they touch. */
	private static void biomeScan(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		server.runOnServer(srv -> {
			var level = srv.overworld();
			int step = 64;
			int n = 8192 / step;
			int[][] grid = new int[n][n];
			int magic = 0;
			int taint = 0;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					var b = level.getUncachedNoiseBiome(((i - n / 2) * step) >> 2, 64 >> 2, ((j - n / 2) * step) >> 2);
					grid[i][j] = b.is(dev.neothaum.world.TVBiomes.MAGICAL_FOREST) ? 1 : b.is(dev.neothaum.world.TVBiomes.TAINTED_LAND) ? 2 : 0;
					magic += grid[i][j] == 1 ? 1 : 0;
					taint += grid[i][j] == 2 ? 1 : 0;
				}
			}
			int touching = 0;
			int nearby = 0;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (grid[i][j] != 2) {
						continue;
					}
					boolean adj = false;
					boolean near = false;
					for (int di = -3; di <= 3; di++) {
						for (int dj = -3; dj <= 3; dj++) {
							int a2 = i + di;
							int b2 = j + dj;
							if (a2 >= 0 && b2 >= 0 && a2 < n && b2 < n && grid[a2][b2] == 1) {
								near = true;
								adj |= Math.abs(di) <= 1 && Math.abs(dj) <= 1;
							}
						}
					}
					touching += adj ? 1 : 0;
					nearby += near ? 1 : 0;
				}
			}
			NeoThaum.LOGGER.info("[visual-test] biomes: samples={} magical={} tainted={} taint cells touching magical={} within 192 blocks={}", n * n,
				magic, taint, touching, nearby);
		});
	}

	/** Lists every item and living entity type that has no aspects, for filling in aspects_modern.json. */
	private static void dumpAspectGaps(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		server.runOnServer(srv -> {
			StringBuilder sb = new StringBuilder();
			var table = dev.neothaum.aspects.ObjectAspects.table();
			for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
				var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
				if (item != net.minecraft.world.item.Items.AIR && !table.containsKey(item)) {
					sb.append("item ").append(id).append(System.lineSeparator());
				}
			}
			for (var type : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
				var id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type);
				if (type.getCategory() != net.minecraft.world.entity.MobCategory.MISC && !dev.neothaum.aspects.ObjectAspects.hasEntityAspects(id)) {
					sb.append("entity ").append(id).append(System.lineSeparator());
				}
			}
			try {
				java.nio.file.Files.writeString(java.nio.file.Path.of(System.getProperty("user.dir"), "aspect_gaps.txt"), sb.toString());
			} catch (java.io.IOException e) {
				throw new java.io.UncheckedIOException(e);
			}
		});
	}

	private static void shot(ClientGameTestContext ctx, String name) {
		Path p = ctx.takeScreenshot("neothaum_" + name);
		NeoThaum.LOGGER.info("[visual-test] {} -> {}", name, p.toAbsolutePath());
	}
}
