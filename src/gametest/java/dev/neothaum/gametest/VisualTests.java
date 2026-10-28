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
			waitChunks(sp, ctx);
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
				if (scenes.contains("icons")) {
					iconsScene(ctx);
				}
				if (scenes.contains("devices")) {
					devicesScene(ctx, server, sp);
				}
				if (scenes.contains("eldritch")) {
					eldritchScene(ctx, server, sp);
				}
				if (scenes.contains("sounds")) {
					soundsScene(ctx, server, sp);
				}
				if (scenes.contains("pech")) {
					pechScene(ctx, server, sp);
				}
				if (scenes.contains("outer")) {
					outerScene(ctx, server, sp);
				}
				if (scenes.contains("audit")) {
					auditScene(ctx, server, sp);
				}
				if (scenes.contains("bore")) {
					boreScene(ctx, server);
				}
				if (scenes.contains("harness")) {
					harnessScene(ctx, server);
				}
				if (scenes.contains("golemai")) {
					golemAiScene(ctx, server);
				}
				if (scenes.contains("worldmap")) {
					worldMapScene(ctx, server, sp);
				}
				if (scenes.contains("golemancy")) {
					golemancyScene(ctx, server);
				}
				if (scenes.contains("tooltips")) {
					tooltipDump(ctx);
				}
				if (scenes.contains("templates")) {
					buildVillageTemplates(server);
				}
				if (scenes.contains("config")) {
					for (int t = 0; t < 4; t++) {
						int tab = t;
						ctx.setScreen(() -> {
							var screen = new dev.neothaum.client.gui.NeoThaumConfigScreen(null);
							try {
								var f = dev.neothaum.client.gui.NeoThaumConfigScreen.class.getDeclaredField("activeTab");
								f.setAccessible(true);
								f.set(screen, tab);
							} catch (ReflectiveOperationException e) {
								throw new RuntimeException(e);
							}
							return screen;
						});
						ctx.waitTicks(3);
						shot(ctx, "config_" + t);
					}
					ctx.setScreen(() -> null);
				}
				if (scenes.contains("staves")) {
					server.runOnServer(srv -> {
						for (String rod : new String[]{"greatwood", "greatwood_staff", "obsidian_staff", "silverwood_staff", "primal_staff"}) {
							var w = dev.neothaum.items.WandItem.create(dev.neothaum.registry.TVItems.WAND, "gold", rod);
							NeoThaum.LOGGER.info("[visual-test] staves: {} staff={} maxVis={} attributes={}", rod, dev.neothaum.items.WandItem.isStaff(w),
								dev.neothaum.items.WandItem.getMaxVis(w), w.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS));
						}
					});
				}
				if (scenes.contains("golems")) {
					golemsScene(ctx, server, sp);
				}
				if (scenes.contains("villages")) {
					villagesScene(ctx, server, sp);
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
			waitChunks(sp, ctx);
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
			waitChunks(sp, ctx);
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
			waitChunks(sp, ctx);
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
			waitChunks(sp, ctx);
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
			waitChunks(sp, ctx);
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
			waitChunks(sp, ctx);
			ctx.waitTicks(20);
			shot(ctx, "research_table");
			server.runCommand("tp @a 40 108 14 0 10");
			waitChunks(sp, ctx);
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
			waitChunks(sp, ctx);
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
		waitChunks(sp, ctx);
		ctx.waitTicks(30);
		shot(ctx, "bellows");
	}

	/** The creative inventory on each Thaumcraft tab, logging the order the tabs were laid out in. */
	/**
	 * Devices drawn by their tile renderers: a row of machines and banners, an ethereal bloom growing, and a generated eldritch ring with its
	 * altar, capped pillars and floating obelisk.
	 */
	private static void devicesScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode creative @a", "fill -24 117 -30 24 135 2 air", "fill -24 116 -30 24 116 2 grass_block",
			"fill -24 113 -30 24 115 2 dirt", "setblock -8 117 -8 neothaum:arcane_spa", "setblock -6 117 -8 neothaum:advanced_alchemical_construct",
			"setblock -4 117 -8 neothaum:deconstruction_table", "setblock -2 117 -8 neothaum:arcane_bore_base", "setblock 0 117 -8 neothaum:flux_scrubber",
			"setblock 2 117 -8 neothaum:banner", "setblock 4 117 -8 neothaum:banner"}) {
			server.runCommand(cmd);
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			if (level.getBlockEntity(new net.minecraft.core.BlockPos(4, 117, -8)) instanceof dev.neothaum.tiles.BannerBlockEntity b) {
				b.setColor(14);
				b.setChanged();
				level.sendBlockUpdated(b.getBlockPos(), b.getBlockState(), b.getBlockState(), 3);
			}
			boolean ring = dev.neothaum.world.EldritchRingFeature.generate(level, level.getRandom(), new net.minecraft.core.BlockPos(10, 116, -18));
			NeoThaum.LOGGER.info("[visual-test] devices: ring generated={} altar={} obelisk={} shaft={}", ring,
				level.getBlockState(new net.minecraft.core.BlockPos(10, 117, -18)).getBlock(), level.getBlockState(new net.minecraft.core.BlockPos(10, 119, -18)),
				level.getBlockState(new net.minecraft.core.BlockPos(10, 121, -18)));
		});
		server.runCommand("setblock -10 117 -8 neothaum:ethereal_bloom");
		server.runCommand("tp @a -1 119 -3 180 20");
		waitChunks(sp, ctx);
		ctx.waitTicks(30);
		shot(ctx, "devices_row_early");
		ctx.waitTicks(90);
		shot(ctx, "devices_row");
		server.runCommand("tp @a -8 118.5 -6 180 30");
		ctx.waitTicks(10);
		shot(ctx, "devices_bloom");
		server.runCommand("tp @a 10 121 -8 180 10");
		ctx.waitTicks(30);
		shot(ctx, "devices_ring");
		server.runCommand("tp @a 13 119 -15 135 -10");
		ctx.waitTicks(20);
		shot(ctx, "devices_obelisk");
	}

	/**
	 * The cultists and eldritch mobs in a row facing the camera, each spawned as the world would spawn it (so in its armour), and armour stands
	 * in the fortress and void robe sets, seen from the front and from behind.
	 */
	private static void eldritchScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode spectator @a", "difficulty normal", "time set noon", "fill -30 117 -40 30 130 -20 air",
			"fill -30 116 -40 30 116 -20 stone"}) {
			server.runCommand(cmd);
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			String[] names = {"cultist_knight", "cultist_cleric", "cultist_leader", "inhabited_zombie", "eldritch_guardian", "eldritch_crab",
				"eldritch_crab", "mind_spider", "eldritch_warden", "eldritch_golem"};
			double x = -18;
			for (int i = 0; i < names.length; i++) {
				var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(NeoThaum.id(names[i]));
				var e = type.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				if (!(e instanceof net.minecraft.world.entity.Mob m)) {
					continue;
				}
				double w = names[i].contains("golem") || names[i].contains("warden") ? 4.0 : 2.5;
				x += w / 2;
				m.snapTo(x, 117, -30, 0.0F, 0.0F);
				m.finalizeSpawn(level, level.getCurrentDifficultyAt(m.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.COMMAND, null);
				if (m instanceof dev.neothaum.entity.monster.EldritchCrabEntity crab) {
					crab.setHelm(i == 5);
				}
				if (m instanceof dev.neothaum.entity.monster.CrimsonClericEntity cleric) {
					cleric.setRitualist(true);
				}
				if (m instanceof dev.neothaum.entity.monster.EldritchWardenEntity wd) {
					wd.spawnTimer = 0;
				}
				if (m instanceof dev.neothaum.entity.monster.EldritchGolemEntity g) {
					g.spawnTimer = 0;
				}
				m.setNoAi(true);
				m.setYBodyRot(0.0F);
				m.setYHeadRot(0.0F);
				m.setPersistenceRequired();
				level.addFreshEntity(m);
				x += w / 2;
			}
			// armour stands in the fortress set (with a mask) and in dyed void robes
			for (int s = 0; s < 2; s++) {
				var stand = new net.minecraft.world.entity.decoration.ArmorStand(level, 14 + s * 2.5, 117, -27);
				stand.setYRot(0.0F);
				stand.setYBodyRot(0.0F);
				if (s == 0) {
					var helm = new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.THAUMIUM_FORTRESS_HELM);
					helm.set(dev.neothaum.registry.TVComponents.MASK, 1);
					stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, helm);
					stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
						new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.THAUMIUM_FORTRESS_CUIRASS));
					stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS,
						new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.THAUMIUM_FORTRESS_THIGH_GUARDS));
				} else {
					stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.VOID_ROBE_HELM));
					stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.VOID_ROBE_CHEST));
					stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.VOID_ROBE_LEGS));
				}
				level.addFreshEntity(stand);
			}
			NeoThaum.LOGGER.info("[visual-test] eldritch: spawned up to x={}", x);
		});
		for (int[] view : new int[][]{{-12, 0}, {0, 0}, {11, 0}, {15, 0}}) {
			server.runCommand("tp @a " + view[0] + " 118.6 -23 180 10");
			waitChunks(sp, ctx);
			ctx.waitTicks(20);
			shot(ctx, "eldritch_front_" + view[0]);
		}
		server.runCommand("tp @a -6 119 -37 0 15");
		ctx.waitTicks(15);
		shot(ctx, "eldritch_back");
	}

	/** One golem of each material, the copper one at each stage of oxidation, and a shambling husk, facing the camera. */
	private static void golemsScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode spectator @a", "time set noon", "fill -20 117 -40 20 125 -20 air", "fill -20 116 -40 20 116 -20 stone"}) {
			server.runCommand(cmd);
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			double x = -14;
			for (var type : dev.neothaum.golem.GolemType.values()) {
				int stages = type == dev.neothaum.golem.GolemType.COPPER ? 4 : 1;
				for (int st = 0; st < stages; st++) {
					var g = dev.neothaum.entity.TVEntities.GOLEM.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
					g.setGolemType(type);
					g.setOxidationLevel(st);
					g.snapTo(x, 117, -30, 0.0F, 0.0F);
					g.setNoAi(true);
					g.setYBodyRot(0.0F);
					g.setYHeadRot(0.0F);
					level.addFreshEntity(g);
					x += 1.6;
				}
			}
			var husk = dev.neothaum.entity.TVEntities.INHABITED_ZOMBIE.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			husk.snapTo(x + 1, 117, -30, 0.0F, 0.0F);
			husk.finalizeSpawn(level, level.getCurrentDifficultyAt(husk.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.COMMAND, null);
			husk.setNoAi(true);
			husk.setYBodyRot(0.0F);
			husk.setYHeadRot(0.0F);
			level.addFreshEntity(husk);
		});
		for (int vx : new int[]{-8, 4}) {
			server.runCommand("tp @a " + vx + " 118.5 -25 180 10");
			waitChunks(sp, ctx);
			ctx.waitTicks(15);
			shot(ctx, "golems_" + vx);
		}
	}

	/**
	 * TC4's village buildings: the tower and a banker's house placed straight from their templates for a look, then a run of generated
	 * villages searched for them (by the tower's loot table and the bankers' iron-bar windows in planks walls).
	 */
	private static void villagesScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode spectator @a", "time set noon", "fill -12 117 -12 12 135 12 air", "fill -12 116 -12 12 116 12 grass_block",
			"place template neothaum:village/wizard_tower -6 117 0", "place template neothaum:village/banker_home_tall_2 2 117 0",
			"tp @a 0 122 16 180 15"}) {
			server.runCommand(cmd);
		}
		waitChunks(sp, ctx);
		ctx.waitTicks(20);
		shot(ctx, "village_pieces");
		// up close at the tower's lower window, to see the pane joined to the walls
		server.runCommand("tp @a -3 119 7 180 0");
		ctx.waitTicks(10);
		shot(ctx, "village_tower_window");
		// the biome versions, in a row: desert, savanna, taiga, snowy
		server.runCommand("fill -40 117 20 40 140 40 air");
		server.runCommand("fill -40 116 20 40 116 40 grass_block");
		String[] kinds = {"desert", "savanna", "taiga", "snowy"};
		for (int k = 0; k < kinds.length; k++) {
			int bx = -36 + k * 18;
			server.runCommand("place template neothaum:village/" + kinds[k] + "/wizard_tower " + bx + " 117 24");
			server.runCommand("place template neothaum:village/" + kinds[k] + "/banker_home_1 " + (bx + 8) + " 117 24");
		}
		server.runCommand("tp @a 0 128 58 180 15");
		waitChunks(sp, ctx);
		ctx.waitTicks(20);
		shot(ctx, "village_variants");
		server.runOnServer(srv -> {
			for (var v : srv.overworld().getEntitiesOfClass(net.minecraft.world.entity.npc.villager.Villager.class,
				new net.minecraft.world.phys.AABB(-40, 110, 20, 40, 140, 40))) {
				NeoThaum.LOGGER.info("[visual-test] villages: variant villager {} {} at {}", v.getVillagerData().profession().unwrapKey().orElseThrow().identifier(),
					v.getVillagerData().type().unwrapKey().orElseThrow().identifier(), v.blockPosition());
			}
		});
		server.runOnServer(srv -> {
			var level = srv.overworld();
			for (var v : level.getEntitiesOfClass(net.minecraft.world.entity.npc.villager.Villager.class, new net.minecraft.world.phys.AABB(-12, 110, -12, 12, 140, 12))) {
				var offers = v.getOffers();
				StringBuilder sb = new StringBuilder();
				for (var o : offers) {
					sb.append(o.getCostA().getCount()).append(' ').append(o.getCostA().getItem()).append(" -> ").append(o.getResult().getCount()).append(' ')
						.append(o.getResult().getItem()).append("; ");
				}
				NeoThaum.LOGGER.info("[visual-test] villages: villager {} at {} trades={} [{}]", v.getVillagerData().profession().unwrapKey(),
					v.blockPosition(), offers.size(), sb);
			}
		});
		int[] found = new int[2];
		for (int v = 0; v < 12; v++) {
			int vx = 300 + v * 200;
			server.runCommand("tp @a " + vx + " 150 300");
			waitChunks(sp, ctx);
			ctx.waitTicks(10);
			server.runCommand("place structure minecraft:village_plains " + vx + " 116 300");
			ctx.waitTicks(5);
			int x0 = vx;
			server.runOnServer(srv -> {
				var level = srv.overworld();
				for (var pos : net.minecraft.core.BlockPos.betweenClosed(x0 - 64, -64, 236, x0 + 64, -30, 364)) {
					if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest && chest.getLootTable() != null
						&& chest.getLootTable().identifier().getPath().equals("chests/wizard_tower")) {
						found[0]++;
						NeoThaum.LOGGER.info("[visual-test] villages: wizard tower chest at {}", pos.immutable());
					}
					if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BELL)) {
						NeoThaum.LOGGER.info("[visual-test] villages: bell at {}", pos.immutable());
					}
					if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.STONE_PRESSURE_PLATE)
						&& level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.COBBLESTONE_WALL)) {
						found[1]++;
					}
				}
			});
		}
		NeoThaum.LOGGER.info("[visual-test] villages: 12 plains villages, wizard towers={}, banker tables={}", found[0], found[1]);
	}

	/**
	 * Builds TC4's village buildings (TCVillagePieces) and saves them as structure templates into the mod's resources, with the entrance jigsaw
	 * that lets a village street place them, and the tower's chest set to its loot table. Run after changing TCVillagePieces.
	 */
	private static void buildVillageTemplates(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var out = java.nio.file.Path.of("").toAbsolutePath().resolve("../../../src/main/resources/data/neothaum/structure/village").normalize();
			record Piece(String name, net.minecraft.core.Vec3i size, net.minecraft.core.BlockPos entrance,
				java.util.function.Consumer<java.util.function.BiConsumer<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState>> build) {
			}
			java.util.List<Piece> pieces = new java.util.ArrayList<>();
			pieces.add(new Piece("wizard_tower", new net.minecraft.core.Vec3i(6, 12, 6), dev.neothaum.world.TCVillagePieces.TOWER_ENTRANCE,
				dev.neothaum.world.TCVillagePieces::wizardTower));
			for (int t = 0; t < 2; t++) {
				{
					boolean tall = t == 1;
					int tp = tall ? 2 : 1;
					int table = tp;
					pieces.add(new Piece("banker_home_" + (tall ? "tall_" : "") + table, new net.minecraft.core.Vec3i(4, 6, 6),
						dev.neothaum.world.TCVillagePieces.BANKER_ENTRANCE, set -> dev.neothaum.world.TCVillagePieces.bankerHome(set, tall, tp)));
				}
			}
			int px = 40;
			for (Piece piece : pieces) {
				var origin = new net.minecraft.core.BlockPos(px, 140, 40);
				px += 10;
				for (var p : net.minecraft.core.BlockPos.betweenClosed(origin, origin.offset(piece.size.getX() - 1, piece.size.getY() - 1, piece.size.getZ() - 1))) {
					level.setBlock(p, net.minecraft.world.level.block.Blocks.STRUCTURE_VOID.defaultBlockState(), 2);
				}
				piece.build.accept((p, st) -> level.setBlock(origin.offset(p), st, 2));
				var entrance = origin.offset(piece.entrance);
				level.setBlock(entrance, net.minecraft.world.level.block.Blocks.JIGSAW.defaultBlockState(), 2);
				if (level.getBlockEntity(entrance) instanceof net.minecraft.world.level.block.entity.JigsawBlockEntity jig) {
					jig.setName(net.minecraft.resources.Identifier.withDefaultNamespace("building_entrance"));
					jig.setTarget(net.minecraft.resources.Identifier.withDefaultNamespace("building_entrance"));
					jig.setPool(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.TEMPLATE_POOL,
						net.minecraft.resources.Identifier.withDefaultNamespace("empty")));
					jig.setFinalState("minecraft:cobblestone_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]");
					jig.setJoint(net.minecraft.world.level.block.entity.JigsawBlockEntity.JointType.ALIGNED);
				}
				if (piece.name.equals("wizard_tower")) {
					net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, level.getRandom(),
						origin.offset(dev.neothaum.world.TCVillagePieces.TOWER_CHEST), net.minecraft.resources.ResourceKey.create(
							net.minecraft.core.registries.Registries.LOOT_TABLE, NeoThaum.id("chests/wizard_tower")));
				}
				var template = new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate();
				template.fillFromWorld(level, origin, piece.size, false, java.util.List.of(net.minecraft.world.level.block.Blocks.STRUCTURE_VOID));
				try {
					java.nio.file.Files.createDirectories(out);
					net.minecraft.nbt.NbtIo.writeCompressed(template.save(new net.minecraft.nbt.CompoundTag()), out.resolve(piece.name + ".nbt"));
					NeoThaum.LOGGER.info("[visual-test] templates: wrote {}", out.resolve(piece.name + ".nbt"));
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	/** Every NeoThaum item's name and tooltip (as a player sees it, advanced tooltips off), written to build/tooltips.txt. */
	private static void tooltipDump(ClientGameTestContext ctx) {
		ctx.runOnClient(mc -> {
			StringBuilder sb = new StringBuilder();
			var tabItems = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
			for (var tab : dev.neothaum.registry.TVTab.TABS) {
				tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(mc.player.connection.enabledFeatures(), false,
					mc.level.registryAccess()));
				tabItems.addAll(tab.getDisplayItems());
			}
			for (var stack : tabItems) {
				var lines = stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level), mc.player,
					net.minecraft.world.item.TooltipFlag.NORMAL);
				sb.append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())).append(System.lineSeparator());
				for (var l : lines) {
					sb.append("    ").append(l.getString()).append(System.lineSeparator());
				}
			}
			try {
				java.nio.file.Files.writeString(java.nio.file.Path.of("tooltips.txt"), sb.toString());
				NeoThaum.LOGGER.info("[visual-test] tooltips: {} stacks -> {}", tabItems.size(), java.nio.file.Path.of("tooltips.txt").toAbsolutePath());
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/** The Golemancy tab of the Thaumonomicon for a new player, after the first two entries, and with everything researched. */
	private static void golemancyScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		String[][] stages = {{}, {"HUNGRYCHEST", "GOLEMSTRAW"}, null};
		for (int i = 0; i < stages.length; i++) {
			String[] keys = stages[i];
			server.runOnServer(srv -> {
				var p = srv.getPlayerList().getPlayers().get(0);
				if (keys == null) {
					for (ResearchItem r : ResearchManager.entries()) {
						ResearchManager.completeResearch(p, r.key);
					}
				} else {
					for (String k : keys) {
						ResearchManager.completeResearch(p, k);
					}
				}
			});
			ctx.waitTicks(10);
			ctx.setScreen(() -> {
				try {
					var f = ThaumonomiconScreen.class.getDeclaredField("selectedCategory");
					f.setAccessible(true);
					f.set(null, "GOLEMANCY");
					// open on the middle of the golem tree
					var lx = ThaumonomiconScreen.class.getDeclaredField("lastX");
					lx.setAccessible(true);
					lx.set(null, 0);
					var ly = ThaumonomiconScreen.class.getDeclaredField("lastY");
					ly.setAccessible(true);
					ly.set(null, 4);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
				return new ThaumonomiconScreen();
			});
			ctx.waitTicks(10);
			ctx.runOnClient(mc -> {
				var cat = ResearchManager.categories().get("GOLEMANCY");
				var k = ResearchManager.knowledge(mc.player);
				NeoThaum.LOGGER.info("[visual-test] golemancy: category entries={} known={} of {} (e.g. GOLEMSTRAW known={})", cat.research.size(),
					cat.research.keySet().stream().filter(k.research::contains).count(), k.research.size(), k.research.contains("GOLEMSTRAW"));
				try {
					var f = ThaumonomiconScreen.class.getDeclaredField("research");
					f.setAccessible(true);
					NeoThaum.LOGGER.info("[visual-test] golemancy: screen list={}", ((java.util.List<?>) f.get(mc.gui.screen())).size());
				} catch (ReflectiveOperationException e) {
					NeoThaum.LOGGER.info("[visual-test] golemancy: no list", e);
				}
			});
			shot(ctx, "golemancy_" + i);
			ctx.setScreen(() -> null);
		}
	}

	/**
	 * Aura nodes on Xaero's World Map (NEOTHAUM_TEST_XAERO=1): three nodes are seen through goggles, two of them scanned (only those may
	 * show), then the map is opened, then the nodes are hidden with the map's button.
	 */
	private static void worldMapScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode creative @a", "tp @a 0 120 0 0 30", "fill -30 117 -30 30 130 30 air",
			"item replace entity @a armor.head with neothaum:goggles_of_revealing"}) {
			server.runCommand(cmd);
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			int[][] at = {{8, 120, 8}, {-12, 121, 4}, {3, 119, -15}};
			dev.neothaum.api.nodes.NodeType[] types = {dev.neothaum.api.nodes.NodeType.NORMAL, dev.neothaum.api.nodes.NodeType.DARK,
				dev.neothaum.api.nodes.NodeType.PURE};
			for (int i = 0; i < at.length; i++) {
				var pos = new net.minecraft.core.BlockPos(at[i][0], at[i][1], at[i][2]);
				dev.neothaum.world.NodeGenerator.createRandomNodeAt(level, pos, level.getRandom(), false, false, false, true, types[i]);
				// the pure node is never scanned, so it must stay off the map
				if (i < 2) {
					NeoThaum.LOGGER.info("[visual-test] worldmap: scanned {} = {}", types[i], dev.neothaum.research.ScanManager.completeScan(
						srv.getPlayerList().getPlayers().get(0), dev.neothaum.research.ScanManager.ScanResult.node(pos), "@"));
				}
			}
		});
		ctx.waitTicks(60);
		// what the M key does: a GuiMap on the current session's map processor
		ctx.runOnClient(mc -> {
			try {
				Object session = Class.forName("xaero.map.WorldMapSession").getMethod("getCurrentSession").invoke(null);
				Object processor = session.getClass().getMethod("getMapProcessor").invoke(session);
				Class<?> gui = Class.forName("xaero.map.gui.GuiMap");
				Object screen = gui.getConstructor(net.minecraft.client.gui.screens.Screen.class, net.minecraft.client.gui.screens.Screen.class,
					processor.getClass(), net.minecraft.world.entity.Entity.class).newInstance(null, null, processor, mc.player);
				mc.gui.setScreen((net.minecraft.client.gui.screens.Screen) screen);
			} catch (ReflectiveOperationException e) {
				NeoThaum.LOGGER.info("[visual-test] worldmap: could not open the map", e);
			}
		});
		ctx.waitTicks(80);
		ctx.runOnClient(mc -> NeoThaum.LOGGER.info("[visual-test] worldmap: screen={}", mc.gui.screen() == null ? null : mc.gui.screen().getClass().getName()));
		shot(ctx, "worldmap_nodes");
		ctx.runOnClient(mc -> {
			var screen = mc.gui.screen();
			if (screen != null) {
				// a real click in the middle of the node button's slot, through the map screen's own mouse handling
				int x = screen.width - 10, y = screen.height - 190;
				for (var w : screen.children()) {
					if (w.isMouseOver(x, y)) {
						NeoThaum.LOGGER.info("[visual-test] worldmap: listener under the node slot: {} {}", w.getClass().getName(),
							w instanceof net.minecraft.client.gui.components.AbstractWidget b ? b.getX() + "," + b.getY() + " active=" + b.active
								+ " visible=" + b.visible + " isActive=" + b.isActive() : "");
					}
				}
				// Xaero's waypoints button in the bottom-right corner, which the old toggle used to cover
				NeoThaum.LOGGER.info("[visual-test] worldmap: widgets over the waypoints button: {}",
					screen.children().stream().filter(w -> w.isMouseOver(screen.width - 10, screen.height - 10)).count());
				boolean before = dev.neothaum.config.NeoThaumConfig.current.worldMapNodes;
				var click = new net.minecraft.client.input.MouseButtonEvent(x, y, new net.minecraft.client.input.MouseButtonInfo(1, 0)); // 1 is the left button in 26.3
				boolean handled = screen.mouseClicked(click, false);
				boolean afterPress = dev.neothaum.config.NeoThaumConfig.current.worldMapNodes;
				screen.mouseReleased(click);
				NeoThaum.LOGGER.info("[visual-test] worldmap: node toggle {} -> {} on press (handled {}) -> {} on release", before, afterPress, handled,
					dev.neothaum.config.NeoThaumConfig.current.worldMapNodes);
			}
		});
		ctx.waitTicks(5);
		shot(ctx, "worldmap_hidden");
		ctx.runOnClient(mc -> {
			dev.neothaum.config.NeoThaumConfig.current.worldMapNodes = true;
			mc.gui.setScreen(null);
		});
	}

	/** Befriending a pech by dropping gold for it, then right-clicking it to trade. */
	private static void pechScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode survival @a", "difficulty normal", "fill -10 117 -10 10 125 10 air", "fill -10 116 -10 10 116 10 grass_block",
			"tp @a 0 117 5 180 10", "clear @a"}) {
			server.runCommand(cmd);
		}
		int[] pechId = new int[1];
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var pech = dev.neothaum.entity.TVEntities.PECH.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			pech.snapTo(0, 117, 0, 0.0F, 0.0F);
			pech.setPersistenceRequired();
			level.addFreshEntity(pech);
			pechId[0] = pech.getId();
			// thrown by the player, as in play (Q with gold in hand)
			var player = srv.getPlayerList().getPlayers().get(0);
			player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, pech.position());
			for (int i = 0; i < 40; i++) {
				player.drop(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT), true, net.minecraft.util.Prediction.PREDICTED);
			}
		});
		ctx.waitTicks(200);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var p = srv.getPlayerList().getPlayers().get(0);
			if (level.getEntity(pechId[0]) instanceof dev.neothaum.entity.monster.Pech pech) {
				NeoThaum.LOGGER.info("[visual-test] pech: tamed={} anger={} gold left on ground={}", pech.isTamed(), pech.getAnger(),
					level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, pech.getBoundingBox().inflate(8)).size());
				p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, pech.getEyePosition());
				var result = p.interactOn(pech, net.minecraft.world.InteractionHand.MAIN_HAND, pech.position());
				NeoThaum.LOGGER.info("[visual-test] pech: interact result={} menu={}", result, p.containerMenu.getClass().getSimpleName());
			} else {
				NeoThaum.LOGGER.info("[visual-test] pech: gone");
			}
		});
		for (int t : new int[]{1, 4, 15, 100}) {
			ctx.waitTicks(t);
			ctx.runOnClient(mc -> NeoThaum.LOGGER.info("[visual-test] pech: client screen={} menu={}", mc.gui.screen() == null ? null
				: mc.gui.screen().getClass().getSimpleName(), mc.player.containerMenu.getClass().getSimpleName()));
			server.runOnServer(srv -> NeoThaum.LOGGER.info("[visual-test] pech: server menu={}",
				srv.getPlayerList().getPlayers().get(0).containerMenu.getClass().getSimpleName()));
		}
		shot(ctx, "pech_trade");
		// put a valued item on offer and roll the dice
		server.runOnServer(srv -> {
			var p = srv.getPlayerList().getPlayers().get(0);
			if (p.containerMenu instanceof dev.neothaum.menu.PechMenu menu) {
				menu.getSlot(0).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT, 4));
				boolean rolled = menu.clickMenuButton(p, 0);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < 5; i++) {
					sb.append(menu.getSlot(i).getItem()).append(' ');
				}
				NeoThaum.LOGGER.info("[visual-test] pech: rolled={} slots={}", rolled, sb);
			}
		});
		ctx.waitTicks(40);
		server.runOnServer(srv -> {
			var p = srv.getPlayerList().getPlayers().get(0);
			if (p.containerMenu instanceof dev.neothaum.menu.PechMenu menu) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < 5; i++) {
					sb.append(menu.getSlot(i).getItem()).append(' ');
				}
				NeoThaum.LOGGER.info("[visual-test] pech: after 2s slots={}", sb);
			}
		});
		shot(ctx, "pech_paid");
		ctx.runOnClient(mc -> mc.player.closeContainer());
	}

	/** Thaumic slimes of three sizes left to hop about and then hurt, with every sound the client starts logged. */
	private static void soundsScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode creative @a", "difficulty normal", "fill -10 117 -10 10 125 10 air", "fill -10 116 -10 10 116 10 stone",
			"tp @a 0 117 6 180 20"}) {
			server.runCommand(cmd);
		}
		java.util.Map<String, Integer> heard = new java.util.TreeMap<>();
		net.minecraft.client.sounds.SoundEventListener listener = (sound, events, range) -> {
			synchronized (heard) {
				heard.merge(sound.getIdentifier().toString(), 1, Integer::sum);
			}
		};
		ctx.runOnClient(mc -> mc.getSoundManager().addListener(listener));
		server.runOnServer(srv -> {
			var level = srv.overworld();
			int[] sizes = {1, 4, 9};
			for (int i = 0; i < sizes.length; i++) {
				var slime = dev.neothaum.entity.TVEntities.THAUMIC_SLIME.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				slime.setSlimeSize(sizes[i]);
				slime.snapTo(-4 + i * 4, 117, -2, 0.0F, 0.0F);
				level.addFreshEntity(slime);
			}
		});
		ctx.waitTicks(100);
		synchronized (heard) {
			NeoThaum.LOGGER.info("[visual-test] sounds: idle/jumping {}", heard);
			heard.clear();
		}
		server.runCommand("damage @e[type=neothaum:thaumic_slime,limit=3] 1 minecraft:player_attack");
		ctx.waitTicks(10);
		server.runCommand("kill @e[type=neothaum:thaumic_slime]");
		ctx.waitTicks(10);
		synchronized (heard) {
			NeoThaum.LOGGER.info("[visual-test] sounds: hurt/death {}", heard);
		}
		ctx.runOnClient(mc -> mc.getSoundManager().removeListener(listener));
	}

	/** Every NeoThaum item drawn as an inventory icon, a page at a time; items with no item definition are logged by name. */
	private static void iconsScene(ClientGameTestContext ctx) {
		java.util.List<net.minecraft.world.item.ItemStack> stacks = new java.util.ArrayList<>();
		java.util.List<String> ids = new java.util.ArrayList<>();
		for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
			var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
			if (id.getNamespace().equals(NeoThaum.MOD_ID)) {
				ids.add(id.getPath());
			}
		}
		java.util.Collections.sort(ids);
		ctx.runOnClient(mc -> {
			var rm = mc.getResourceManager();
			for (String id : ids) {
				if (rm.getResource(NeoThaum.id("items/" + id + ".json")).isEmpty()) {
					NeoThaum.LOGGER.info("[visual-test] icons: no item definition for {}", id);
				}
			}
		});
		for (String id : ids) {
			stacks.add(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(NeoThaum.id(id))));
		}
		int per = IconSheetScreen.COLS * IconSheetScreen.ROWS;
		for (int page = 0; page * per < stacks.size(); page++) {
			StringBuilder sb = new StringBuilder();
			for (int i = page * per; i < Math.min(stacks.size(), (page + 1) * per); i++) {
				sb.append(i - page * per).append('=').append(ids.get(i)).append(' ');
			}
			NeoThaum.LOGGER.info("[visual-test] icons page {}: {}", page, sb);
			int p = page;
			ctx.setScreen(() -> new IconSheetScreen(stacks, p));
			ctx.waitTicks(4);
			shot(ctx, "icons_" + page);
		}
		ctx.setScreen(() -> null);
	}

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
		waitChunks(sp, ctx);
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
		waitChunks(sp, ctx);
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
		waitChunks(sp, ctx);
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
		waitChunks(sp, ctx);
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
		waitChunks(sp, ctx);
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
		waitChunks(sp, ctx);
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

	
	private static void waitChunks(TestSingleplayerContext sp, ClientGameTestContext ctx) {
		try {
			sp.getConnection().waitForChunksRender();
		} catch (Throwable t) {
			ctx.waitTicks(20);
		}
	}

	/** The Thaumostatic Harness on the player's back, idle and then hovering, seen from behind. */
	private static void harnessScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		for (String cmd : new String[]{"gamemode survival @a", "time set noon", "weather clear", "fill -6 117 -6 6 125 6 air",
			"fill -6 116 -6 6 116 6 stone", "tp @a 0 117 0 0 0", "item replace entity @a armor.chest with neothaum:thaumostatic_harness"}) {
			server.runCommand(cmd);
		}
		ctx.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK));
		ctx.waitTicks(20);
		shot(ctx, "harness_idle");
		server.runOnServer(srv -> {
			var p = srv.getPlayerList().getPlayers().get(0);
			p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).set(dev.neothaum.registry.TVComponents.HARNESS_ACTIVE, true);
		});
		ctx.waitTicks(10);
		shot(ctx, "harness_hover");
		ctx.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
	}

	/** An arcane bore on its base boring north into stone, with a chest for its spoils and an arcane lamp to light the way. */
	private static void boreScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		for (String cmd : new String[]{"gamemode creative @a", "time set noon", "fill -8 112 -16 8 125 8 air", "fill -8 111 -16 8 111 8 stone",
			"fill -6 112 -14 6 124 -3 stone", "fill -6 112 -14 6 124 -3 iron_ore replace stone", "fill -1 116 -14 1 120 -3 stone",
			"setblock 0 117 0 neothaum:arcane_bore_base[facing=east]", "setblock 0 118 0 neothaum:arcane_bore[facing=north,base_dir=down]",
			"setblock 1 117 0 chest", "setblock -1 117 0 redstone_block", "setblock 0 117 1 neothaum:arcane_lamp", "tp @a 4 120 6 150 20"}) {
			server.runCommand(cmd);
		}
		server.runOnServer(srv -> {
			var level = srv.overworld();
			if (level.getBlockEntity(new net.minecraft.core.BlockPos(0, 118, 0)) instanceof dev.neothaum.tiles.ArcaneBoreBlockEntity bore) {
				bore.setOrientation(net.minecraft.core.Direction.NORTH, true);
				bore.setItem(0, new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.FOCUS_EXCAVATION));
				bore.setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
			}
		});
		for (int r = 1; r <= 2; r++) {
			ctx.waitTicks(400);
			int round = r;
			server.runOnServer(srv -> {
				var level = srv.overworld();
				int dug = 0;
				int lights = 0;
				for (var p : net.minecraft.core.BlockPos.betweenClosed(-6, 112, -14, 6, 124, -3)) {
					if (level.getBlockState(p).isAir()) {
						dug++;
					}
					if (level.getBlockState(p).is(dev.neothaum.registry.TVBlocks.LAMP_LIGHT)) {
						lights++;
					}
				}
				StringBuilder sb = new StringBuilder();
				if (level.getBlockEntity(new net.minecraft.core.BlockPos(1, 117, 0)) instanceof net.minecraft.world.Container c) {
					for (int i = 0; i < c.getContainerSize(); i++) {
						if (!c.getItem(i).isEmpty()) {
							sb.append(c.getItem(i).getCount()).append(' ').append(c.getItem(i).getItem()).append(", ");
						}
					}
				}
				var bore = (dev.neothaum.tiles.ArcaneBoreBlockEntity) level.getBlockEntity(new net.minecraft.core.BlockPos(0, 118, 0));
				NeoThaum.LOGGER.info("[visual-test] bore {}: blocks dug={} lamp lights={} chest=[{}] pick damage={}", round, dug, lights, sb,
					bore.getItem(1).getDamageValue());
				NeoThaum.LOGGER.info("[visual-test] bore {}: rot {} {} tar {} {} toDig={} dig={} focus={} pick={} power={} valid={} orient={}", round, bore.rotX,
					bore.rotZ, bore.tarX, bore.tarZ, bore.toDig, bore.digPos, bore.hasFocus, bore.hasPickaxe, bore.isGettingPower(), bore.areItemsValid(),
					bore.orientation);
			});
		}
		shot(ctx, "bore");
	}

	/** One golem per core, each at a little workplace of its own, and what each has done after a while. */
	private static void golemAiScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
		for (String cmd : new String[]{"gamemode creative @a", "difficulty peaceful", "gamerule doDaylightCycle false", "gamerule doMobSpawning false",
			"weather clear", "time set 6000", "fill -72 117 -54 72 130 54 air", "fill -72 116 -54 72 116 54 stone", "tp @a 0 140 0"}) {
			server.runCommand(cmd);
		}
		ctx.waitTicks(20);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var dim = level.dimension();
			java.util.function.BiFunction<int[], Integer, dev.neothaum.golem.GolemEntity> golem = (c, core) -> {
				var g = new dev.neothaum.golem.GolemEntity(dev.neothaum.entity.TVEntities.GOLEM, level);
				g.setGolemType(dev.neothaum.golem.GolemType.IRON);
				var home = new net.minecraft.core.BlockPos(c[0], 117, c[1]);
				g.snapTo(c[0] + 0.5, 117, c[1] + 0.5, 0, 0);
				g.setHomeArea(home, net.minecraft.core.Direction.SOUTH);
				g.setCore(core);
				level.addFreshEntity(g);
				return g;
			};
			java.util.function.Function<net.minecraft.core.BlockPos, net.minecraft.world.level.block.entity.ChestBlockEntity> chest = p -> {
				level.setBlock(p, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState(), 3);
				return (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(p);
			};
			java.util.function.BiFunction<net.minecraft.core.BlockPos, net.minecraft.core.Direction, dev.neothaum.golem.GolemMarker> mark =
				(p, d) -> new dev.neothaum.golem.GolemMarker(p, dim, (byte) d.get3DDataValue(), (byte) -1);
			var up = net.minecraft.core.Direction.UP;
			// fill: keep four cobblestone in the home chest
			int[] c = {-54, -36};
			chest.apply(new net.minecraft.core.BlockPos(c[0], 117, c[1] - 1));
			chest.apply(new net.minecraft.core.BlockPos(c[0] + 5, 117, c[1])).setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 64));
			var g = golem.apply(c, 0);
			g.setFilterItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 4));
			g.setMarkers(java.util.List.of(mark.apply(new net.minecraft.core.BlockPos(c[0] + 5, 117, c[1]), up)));
			// empty: carry the home chest's dirt to the marked chest
			c = new int[]{-18, -36};
			chest.apply(new net.minecraft.core.BlockPos(c[0], 117, c[1] - 1)).setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIRT, 40));
			chest.apply(new net.minecraft.core.BlockPos(c[0] + 5, 117, c[1]));
			golem.apply(c, 1).setMarkers(java.util.List.of(mark.apply(new net.minecraft.core.BlockPos(c[0] + 5, 117, c[1]), up)));
			// gather: pick up the apples lying about
			c = new int[]{18, -36};
			chest.apply(new net.minecraft.core.BlockPos(c[0], 117, c[1] - 1));
			golem.apply(c, 2);
			for (int i = 0; i < 3; i++) {
				level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, c[0] + 4.5 + i, 117.2, c[1] + 3.5,
					new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE, 2)));
			}
			// harvest: a little field of ripe wheat
			c = new int[]{54, -36};
			golem.apply(c, 3);
			for (int i = 0; i < 3; i++) {
				var soil = new net.minecraft.core.BlockPos(c[0] + 2 + i, 116, c[1] + 2);
				level.setBlock(soil, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 3);
				level.setBlock(soil.above(), net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState()
					.setValue(net.minecraft.world.level.block.CropBlock.AGE, 7), 3);
			}
			// guard: a zombie that stands still
			c = new int[]{-54, 0};
			golem.apply(c, 4);
			var z = new net.minecraft.world.entity.monster.zombie.Zombie(level);
			z.snapTo(c[0] + 4.5, 117, c[1] + 4.5, 0, 0);
			z.setNoAi(true);
			z.setPersistenceRequired();
			level.addFreshEntity(z);
			// liquid: fill the home cauldron from a marked pool of water
			c = new int[]{-18, 0};
			level.setBlock(new net.minecraft.core.BlockPos(c[0], 117, c[1] - 1), net.minecraft.world.level.block.Blocks.CAULDRON.defaultBlockState(), 3);
			var pool = new net.minecraft.core.BlockPos(c[0] + 4, 116, c[1]);
			level.setBlock(pool, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(), 3);
			golem.apply(c, 5).setMarkers(java.util.List.of(mark.apply(pool, up)));
			// essentia: move ignis from the home jar to a marked jar
			c = new int[]{18, 0};
			var homeJarPos = new net.minecraft.core.BlockPos(c[0], 117, c[1] - 1);
			level.setBlock(homeJarPos, dev.neothaum.registry.TVBlocks.WARDED_JAR.defaultBlockState(), 3);
			((dev.neothaum.tiles.JarBlockEntity) level.getBlockEntity(homeJarPos)).addToContainer(dev.neothaum.api.aspects.Aspect.FIRE, 20);
			var jarPos = new net.minecraft.core.BlockPos(c[0] + 5, 117, c[1]);
			level.setBlock(jarPos, dev.neothaum.registry.TVBlocks.WARDED_JAR.defaultBlockState(), 3);
			golem.apply(c, 6).setMarkers(java.util.List.of(mark.apply(jarPos, up)));
			// lumber: a bare oak trunk
			c = new int[]{54, 0};
			golem.apply(c, 7);
			for (int y = 117; y < 121; y++) {
				level.setBlock(new net.minecraft.core.BlockPos(c[0] + 3, y, c[1] + 3), net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState(), 3);
			}
			// use: plant the home chest's seeds on the marked farmland
			c = new int[]{-54, 36};
			chest.apply(new net.minecraft.core.BlockPos(c[0], 117, c[1] - 1)).setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT_SEEDS, 5));
			var farm = new net.minecraft.core.BlockPos(c[0] + 3, 116, c[1] + 2);
			level.setBlock(farm, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 3);
			golem.apply(c, 8).setMarkers(java.util.List.of(mark.apply(farm, up)));
			// butcher: four cows, one too many
			c = new int[]{-18, 36};
			golem.apply(c, 9);
			for (int i = 0; i < 4; i++) {
				var cow = new net.minecraft.world.entity.animal.cow.Cow(net.minecraft.world.entity.EntityTypes.COW, level);
				cow.snapTo(c[0] + 3.5 + i, 117, c[1] + 4.5, 0, 0);
				cow.setNoAi(true);
				cow.setPersistenceRequired();
				level.addFreshEntity(cow);
			}
			// sorting: dirt to the chest with dirt, stone to the chest with stone
			c = new int[]{18, 36};
			var hc = chest.apply(new net.minecraft.core.BlockPos(c[0], 117, c[1] - 1));
			hc.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIRT, 5));
			hc.setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE, 5));
			var dirtChest = new net.minecraft.core.BlockPos(c[0] + 5, 117, c[1]);
			var stoneChest = new net.minecraft.core.BlockPos(c[0] - 5, 117, c[1]);
			chest.apply(dirtChest).setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIRT));
			chest.apply(stoneChest).setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE));
			golem.apply(c, 10).setMarkers(java.util.List.of(mark.apply(dirtChest, up), mark.apply(stoneChest, up)));
			// fish: a pond
			c = new int[]{54, 36};
			golem.apply(c, 11);
			for (var p : net.minecraft.core.BlockPos.betweenClosed(c[0] + 2, 114, c[1] + 2, c[0] + 6, 116, c[1] + 6)) {
				level.setBlock(p, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(), 3);
			}
		});
		for (int round = 1; round <= 3; round++) {
			ctx.waitTicks(400);
			int r = round;
			server.runOnServer(srv -> {
				var level = srv.overworld();
				java.util.function.BiFunction<Integer, Integer, String> inv = (x, zz) -> {
					var be = level.getBlockEntity(new net.minecraft.core.BlockPos(x, 117, zz));
					if (!(be instanceof net.minecraft.world.Container ct)) {
						return "none";
					}
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < ct.getContainerSize(); i++) {
						if (!ct.getItem(i).isEmpty()) {
							sb.append(ct.getItem(i).getCount()).append(' ').append(ct.getItem(i).getItem()).append(", ");
						}
					}
					return sb.toString();
				};
				var box = new net.minecraft.world.phys.AABB(-72, 110, -54, 72, 130, 54);
				int zombies = level.getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.Zombie.class, box).size();
				int cows = level.getEntitiesOfClass(net.minecraft.world.entity.animal.cow.Cow.class, box).size();
				int wheat = 0;
				for (int i = 0; i < 3; i++) {
					if (level.getBlockState(new net.minecraft.core.BlockPos(56 + i, 117, -34)).is(net.minecraft.world.level.block.Blocks.WHEAT)) {
						wheat++;
					}
				}
				int logs = 0;
				for (int y = 117; y < 121; y++) {
					if (level.getBlockState(new net.minecraft.core.BlockPos(57, y, 3)).is(net.minecraft.world.level.block.Blocks.OAK_LOG)) {
						logs++;
					}
				}
				var jar = (dev.neothaum.tiles.JarBlockEntity) level.getBlockEntity(new net.minecraft.core.BlockPos(23, 117, 0));
				var homeJar = (dev.neothaum.tiles.JarBlockEntity) level.getBlockEntity(new net.minecraft.core.BlockPos(18, 117, -1));
				int bobbers = level.getEntitiesOfClass(dev.neothaum.golem.GolemBobberEntity.class, box).size();
				var cauldron = level.getBlockState(new net.minecraft.core.BlockPos(-18, 117, -1));
				NeoThaum.LOGGER.info("[visual-test] golemai {}: fill home=[{}] source=[{}]", r, inv.apply(-54, -37), inv.apply(-49, -36));
				NeoThaum.LOGGER.info("[visual-test] golemai {}: empty home=[{}] target=[{}]", r, inv.apply(-18, -37), inv.apply(-13, -36));
				NeoThaum.LOGGER.info("[visual-test] golemai {}: gather home=[{}]", r, inv.apply(18, -37));
				NeoThaum.LOGGER.info("[visual-test] golemai {}: harvest ripe wheat left={} of 3", r, wheat);
				NeoThaum.LOGGER.info("[visual-test] golemai {}: guard zombies left={}", r, zombies);
				NeoThaum.LOGGER.info("[visual-test] golemai {}: liquid cauldron={} pool={}", r, cauldron,
					level.getBlockState(new net.minecraft.core.BlockPos(-14, 116, 0)));
				NeoThaum.LOGGER.info("[visual-test] golemai {}: essentia home jar={} {} marked jar={} {}", r, homeJar.aspect, homeJar.amount, jar.aspect,
					jar.amount);
				NeoThaum.LOGGER.info("[visual-test] golemai {}: lumber logs left={} of 4", r, logs);
				NeoThaum.LOGGER.info("[visual-test] golemai {}: use home=[{}] above farmland={}", r, inv.apply(-54, 35),
					level.getBlockState(new net.minecraft.core.BlockPos(-51, 117, 38)));
				NeoThaum.LOGGER.info("[visual-test] golemai {}: butcher cows left={} of 4", r, cows);
				NeoThaum.LOGGER.info("[visual-test] golemai {}: sorting home=[{}] dirt chest=[{}] stone chest=[{}]", r, inv.apply(18, 35),
					inv.apply(23, 36), inv.apply(13, 36));
				NeoThaum.LOGGER.info("[visual-test] golemai {}: fish bobbers={}", r, bobbers);
			});
		}
		server.runCommand("tp @a 18 125 30");
		ctx.waitTicks(10);
		shot(ctx, "golemai");
	}

	/** The TC4-accuracy fixes: lamp lights, finite and flowing fluids, dissolve drops, champions and the golemancer's bell. */
	private static void auditScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode creative @a", "difficulty normal", "gamerule doDaylightCycle false", "gamerule doMobSpawning false",
			"weather clear", "time set 6000", "fill -24 117 -24 24 135 24 air", "fill -24 116 -24 24 116 24 stone", "tp @a 0 117 0"}) {
			server.runCommand(cmd);
		}
		// --- arcane lamp: nothing in daylight, lights at night, all gone when the lamp is broken
		server.runCommand("setblock 0 117 0 neothaum:arcane_lamp");
		ctx.waitTicks(200);
		java.util.function.IntSupplier lights = () -> {
			int[] n = new int[1];
			server.runOnServer(srv -> {
				var level = srv.overworld();
				for (var p : net.minecraft.core.BlockPos.betweenClosed(-16, 117, -16, 16, 135, 16)) {
					if (level.getBlockState(p).is(dev.neothaum.registry.TVBlocks.LAMP_LIGHT)
						|| level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.LIGHT)) {
						n[0]++;
					}
				}
			});
			return n[0];
		};
		NeoThaum.LOGGER.info("[visual-test] audit: lamp lights above ground at noon = {}", lights.getAsInt());
		server.runCommand("time set 18000");
		ctx.waitTicks(200);
		NeoThaum.LOGGER.info("[visual-test] audit: lamp lights above ground at midnight = {}", lights.getAsInt());
		server.runCommand("setblock 0 117 0 air destroy");
		ctx.waitTicks(5);
		NeoThaum.LOGGER.info("[visual-test] audit: lamp lights after breaking the lamp = {}", lights.getAsInt());
		server.runCommand("time set 6000");

		// --- liquid death spreads out as a finite fluid; purifying fluid flows from a source
		server.runCommand("setblock 8 117 8 neothaum:liquid_death");
		server.runCommand("setblock -8 117 -8 neothaum:purifying_fluid");
		ctx.waitTicks(120);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			int blocks = 0, quanta = 0, pure = 0, pureSources = 0;
			for (var p : net.minecraft.core.BlockPos.betweenClosed(-20, 116, -20, 20, 118, 20)) {
				var st = level.getBlockState(p);
				if (st.is(dev.neothaum.registry.TVBlocks.LIQUID_DEATH)) {
					blocks++;
					quanta += st.getValue(dev.neothaum.blocks.LiquidDeathBlock.LEVEL) + 1;
				}
				if (st.is(dev.neothaum.registry.TVBlocks.PURIFYING_FLUID)) {
					pure++;
					if (level.getFluidState(p).isSource()) {
						pureSources++;
					}
				}
			}
			NeoThaum.LOGGER.info("[visual-test] audit: liquid death blocks = {}, total quanta = {} (4 expected)", blocks, quanta);
			NeoThaum.LOGGER.info("[visual-test] audit: purifying fluid blocks = {}, sources = {}, is water = {}", pure, pureSources,
				level.getFluidState(new net.minecraft.core.BlockPos(-8, 117, -8)).is(net.minecraft.tags.FluidTags.WATER));
		});
		server.runCommand("tp @a 0 121 0 135 35");
		ctx.waitTicks(20);
		shot(ctx, "audit_fluids");

		// --- dissolve: a pig in a pit of liquid death dies and drops crystallized essence
		server.runCommand("fill 15 116 15 15 115 15 air");
		server.runCommand("setblock 15 115 15 stone");
		server.runCommand("setblock 15 116 15 neothaum:liquid_death");
		server.runCommand("summon minecraft:pig 15.5 116 15.5");
		ctx.waitTicks(100);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var box = new net.minecraft.world.phys.AABB(10, 110, 10, 20, 125, 20);
			int pigs = level.getEntitiesOfClass(net.minecraft.world.entity.animal.pig.Pig.class, box).size();
			int crystals = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box).stream()
				.filter(e -> e.getItem().is(dev.neothaum.registry.TVItems.CRYSTAL_ESSENCE)).mapToInt(e -> e.getItem().getCount()).sum();
			NeoThaum.LOGGER.info("[visual-test] audit: pigs left in liquid death = {}, crystal essence dropped = {}", pigs, crystals);
		});

		// --- champions: a zombie made champion, and a giant taintacle, which always is one
		server.runCommand("summon minecraft:zombie 0 117 -10 {NoAI:1b}");
		server.runCommand("summon neothaum:taintacle_giant 6 117 -10");
		ctx.waitTicks(5);
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var box = new net.minecraft.world.phys.AABB(-5, 110, -15, 10, 125, -5);
			for (var z : level.getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.Zombie.class, box)) {
				if (!dev.neothaum.entity.champion.ChampionEvents.isChampion(z)) {
					dev.neothaum.entity.champion.ChampionEvents.makeChampion(z, false);
				}
				NeoThaum.LOGGER.info("[visual-test] audit: champion zombie {} name={} maxHealth={} health={} damage={} nameVisible={}",
					dev.neothaum.entity.champion.ChampionEvents.getModifier(z), z.getCustomName() == null ? null : z.getCustomName().getString(),
					z.getMaxHealth(), z.getHealth(), z.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),
					z.isCustomNameVisible());
			}
			for (var t : level.getEntitiesOfClass(dev.neothaum.entity.monster.TaintacleGiant.class, box)) {
				NeoThaum.LOGGER.info("[visual-test] audit: giant taintacle champion={} modifier={} name={}",
					dev.neothaum.entity.champion.ChampionEvents.isChampion(t), dev.neothaum.entity.champion.ChampionEvents.getModifier(t),
					t.getCustomName() == null ? null : t.getCustomName().getString());
			}
		});

		// --- golemancer's bell: select a golem, mark a chest and two faces, unmark one, see them drawn
		server.runCommand("kill @e[type=!player]");
		server.runCommand("setblock -3 117 3 minecraft:chest");
		server.runCommand("setblock 3 117 3 minecraft:chest");
		server.runCommand("item replace entity @a weapon.mainhand with neothaum:golem_bell");
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var player = srv.getPlayerList().getPlayers().get(0);
			var golem = new dev.neothaum.golem.GolemEntity(dev.neothaum.entity.TVEntities.GOLEM, level);
			golem.setGolemType(dev.neothaum.golem.GolemType.WOOD);
			golem.snapTo(-3.5, 118, 3.5, 0, 0);
			golem.setHomeArea(new net.minecraft.core.BlockPos(-3, 117, 3), net.minecraft.core.Direction.UP);
			golem.setNoAi(true);
			level.addFreshEntity(golem);
			var bell = player.getMainHandItem();
			bell.getItem().interactLivingEntity(bell, player, golem, net.minecraft.world.InteractionHand.MAIN_HAND);
			var up = net.minecraft.core.Direction.UP;
			dev.neothaum.golem.GolemBellItem.changeMarkers(bell, player, level, new net.minecraft.core.BlockPos(3, 117, 3), up);
			dev.neothaum.golem.GolemBellItem.changeMarkers(bell, player, level, new net.minecraft.core.BlockPos(0, 116, 4), up);
			dev.neothaum.golem.GolemBellItem.changeMarkers(bell, player, level, new net.minecraft.core.BlockPos(1, 116, 4), up);
			// clicking a marked face again (no Order upgrade) unmarks it
			dev.neothaum.golem.GolemBellItem.changeMarkers(bell, player, level, new net.minecraft.core.BlockPos(1, 116, 4), up);
			NeoThaum.LOGGER.info("[visual-test] audit: bell linked={} bell markers={} golem markers={} marked containers={}",
				dev.neothaum.golem.GolemBellItem.getGolemId(bell) == golem.getId(), dev.neothaum.golem.GolemBellItem.getMarkers(bell).size(),
				golem.getMarkers().size(), golem.getMarkedContainers());
			NeoThaum.LOGGER.info("[visual-test] audit: placer keeps markers = {}",
				golem.toPlacerItem().getOrDefault(dev.neothaum.registry.TVComponents.GOLEM_MARKERS, java.util.List.of()).size());
		});
		server.runCommand("tp @a 0 120 -3 0 30");
		ctx.waitTicks(20);
		shot(ctx, "audit_bell");
		server.runCommand("kill @e[type=!player]");
	}

	/** The Outer Lands: open an altar's portal, step through into the maze, find its rooms, wake a boss, and come back. */
	private static void outerScene(ClientGameTestContext ctx, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
		TestSingleplayerContext sp) {
		for (String cmd : new String[]{"gamemode creative @a", "difficulty normal", "gamerule doDaylightCycle false", "gamerule doMobSpawning false",
			"time set 6000", "fill -8 117 -8 8 130 8 air", "fill -8 116 -8 8 116 8 stone", "tp @a 3 117 3"}) {
			server.runCommand(cmd);
		}
		net.minecraft.core.BlockPos altarPos = new net.minecraft.core.BlockPos(0, 117, 0);
		// an eldritch altar under a sinister node, an obelisk above, and all four eyes
		server.runOnServer(srv -> {
			var level = srv.overworld();
			var player = srv.getPlayerList().getPlayers().get(0);
			level.setBlockAndUpdate(altarPos, dev.neothaum.registry.TVBlocks.ELDRITCH_ALTAR.defaultBlockState());
			dev.neothaum.world.NodeGenerator.createNodeAt(level, altarPos.above(), dev.neothaum.api.nodes.NodeType.DARK, null,
				new dev.neothaum.api.aspects.AspectList().add(dev.neothaum.api.aspects.Aspect.AIR, 10), true);
			level.setBlockAndUpdate(altarPos.above(2), dev.neothaum.registry.TVBlocks.ELDRITCH_OBELISK.defaultBlockState());
			for (int i = 3; i <= 6; i++) {
				level.setBlockAndUpdate(altarPos.above(i), dev.neothaum.registry.TVBlocks.ELDRITCH_OBELISK.defaultBlockState()
					.setValue(dev.neothaum.blocks.eldritch.EldritchObeliskBlock.BASE, false));
			}
			if (level.getBlockEntity(altarPos) instanceof dev.neothaum.tiles.eldritch.EldritchAltarBlockEntity altar) {
				var eyes = new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.ELDRITCH_EYE, 4);
				for (int i = 0; i < 4; i++) {
					altar.insertEye(player, eyes);
				}
				NeoThaum.LOGGER.info("[visual-test] outer: eyes in = {}", altar.getEyes());
			}
		});
		ctx.waitTicks(60);
		server.runOnServer(srv -> {
			var cell = dev.neothaum.world.outer.MazeHandler.getCell(0, 0);
			NeoThaum.LOGGER.info("[visual-test] outer: maze ready = {} (portal cell feature {})", cell != null && cell.feature == 1,
				cell == null ? null : cell.feature);
			var level = srv.overworld();
			var player = srv.getPlayerList().getPlayers().get(0);
			var wand = dev.neothaum.items.WandItem.createFull(dev.neothaum.registry.TVItems.WAND, "gold", "primal_staff");
			if (level.getBlockEntity(altarPos) instanceof dev.neothaum.tiles.eldritch.EldritchAltarBlockEntity altar) {
				var r = altar.activateWand(player, wand);
				NeoThaum.LOGGER.info("[visual-test] outer: wand strike = {}, portal opened = {}", r,
					level.getBlockState(altarPos.above()).is(dev.neothaum.registry.TVBlocks.ELDRITCH_PORTAL));
			}
		});
		server.runCommand("tp @a 4 117 4 135 15");
		ctx.waitTicks(10);
		shot(ctx, "outer_portal_overworld");
		// step into the portal
		server.runCommand("tp @a 0.5 118 0.5");
		ctx.waitTicks(60);
		server.runOnServer(srv -> {
			var player = srv.getPlayerList().getPlayers().get(0);
			NeoThaum.LOGGER.info("[visual-test] outer: after the portal: dimension={} pos={} ENTEROUTER={}", player.level().dimension().identifier(),
				player.blockPosition(), dev.neothaum.research.ResearchManager.knowledge(player).research.contains("ENTEROUTER"));
		});
		ctx.waitTicks(40);
		shot(ctx, "outer_arrival");
		server.runCommand("execute in neothaum:outer_lands run tp @a 8.5 53 5.5 0 10");
		ctx.waitTicks(40);
		shot(ctx, "outer_portal_hall");
		// what the maze holds: its rooms, the key room's tablet, and the boss room's locks
		int[] boss = new int[2];
		int[] key = new int[2];
		server.runOnServer(srv -> {
			var outer = srv.getLevel(dev.neothaum.world.outer.OuterLands.KEY);
			java.util.Map<Integer, Integer> features = new java.util.TreeMap<>();
			for (int x = -20; x <= 20; x++) {
				for (int z = -20; z <= 20; z++) {
					var c = dev.neothaum.world.outer.MazeHandler.getCell(x, z);
					if (c != null && c.feature != 0) {
						features.merge((int) c.feature, 1, Integer::sum);
						if (c.feature == 2) {
							boss[0] = x;
							boss[1] = z;
						}
						if (c.feature == 6) {
							key[0] = x;
							key[1] = z;
						}
					}
				}
			}
			NeoThaum.LOGGER.info("[visual-test] outer: maze features {}", features);
			// build the key room and look for its tablet
			outer.getChunk(key[0], key[1]);
			var box = new net.minecraft.world.phys.AABB(key[0] * 16, 40, key[1] * 16, key[0] * 16 + 16, 70, key[1] * 16 + 16);
			NeoThaum.LOGGER.info("[visual-test] outer: key room at chunk {},{}: tablets={} guardians={}", key[0], key[1],
				outer.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box,
					e -> e.getItem().is(dev.neothaum.registry.TVItems.RUNED_TABLET)).size(),
				outer.getEntitiesOfClass(dev.neothaum.entity.monster.EldritchGuardianEntity.class, box.inflate(8)).size());
			int stone = 0, voids = 0, decor = 0;
			for (var p : net.minecraft.core.BlockPos.betweenClosed(0, 40, 0, 15, 70, 15)) {
				var s = outer.getBlockState(p);
				if (s.is(dev.neothaum.registry.TVBlocks.ANCIENT_STONE) || s.is(dev.neothaum.registry.TVBlocks.ANCIENT_STONE_SEALED)) {
					stone++;
				} else if (s.is(dev.neothaum.registry.TVBlocks.ELDRITCH_NOTHING)) {
					voids++;
				} else if (s.is(dev.neothaum.registry.TVBlocks.GLOWING_CRUSTED_STONE) || s.is(dev.neothaum.registry.TVBlocks.GLYPHED_STONE)
					|| s.is(dev.neothaum.registry.TVBlocks.RUNED_STONE)) {
					decor++;
				}
			}
			NeoThaum.LOGGER.info("[visual-test] outer: portal cell: ancient stone={} void={} decorations={}", stone, voids, decor);
		});
		// visit the key room: its tablet and guardians load once someone is there
		server.runCommand("execute in neothaum:outer_lands run tp @a " + (key[0] * 16 + 8.5) + " 53 " + (key[1] * 16 + 8.5));
		ctx.waitTicks(60);
		server.runOnServer(srv -> {
			var outer = srv.getLevel(dev.neothaum.world.outer.OuterLands.KEY);
			var box = new net.minecraft.world.phys.AABB(key[0] * 16 - 8, 40, key[1] * 16 - 8, key[0] * 16 + 24, 70, key[1] * 16 + 24);
			NeoThaum.LOGGER.info("[visual-test] outer: in the key room: tablets={} guardians={}",
				outer.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box,
					e -> e.getItem().is(dev.neothaum.registry.TVItems.RUNED_TABLET)).size(),
				outer.getEntitiesOfClass(dev.neothaum.entity.monster.EldritchGuardianEntity.class, box).size());
			srv.getPlayerList().getPlayers().get(0).addEffect(new net.minecraft.world.effect.MobEffectInstance(
				net.minecraft.world.effect.MobEffects.NIGHT_VISION, 2000, 0, false, false));
		});
		server.runCommand("execute in neothaum:outer_lands run tp @a " + (key[0] * 16 + 4.5) + " 53 " + (key[1] * 16 + 4.5) + " facing "
			+ (key[0] * 16 + 8.5) + " 53 " + (key[1] * 16 + 8.5));
		ctx.waitTicks(20);
		shot(ctx, "outer_key_room");
		// go to the boss room's lock
		net.minecraft.core.BlockPos[] lock = new net.minecraft.core.BlockPos[1];
		server.runOnServer(srv -> {
			var outer = srv.getLevel(dev.neothaum.world.outer.OuterLands.KEY);
			for (int a = 0; a < 2; a++) {
				for (int b = 0; b < 2; b++) {
					outer.getChunk(boss[0] + a, boss[1] + b);
				}
			}
			for (var p : net.minecraft.core.BlockPos.betweenClosed(boss[0] * 16 - 1, 45, boss[1] * 16 - 1, boss[0] * 16 + 33, 65, boss[1] * 16 + 33)) {
				if (outer.getBlockState(p).is(dev.neothaum.registry.TVBlocks.ANCIENT_LOCK)) {
					lock[0] = p.immutable();
				}
			}
			NeoThaum.LOGGER.info("[visual-test] outer: boss room at chunk {},{}: lock at {}", boss[0], boss[1], lock[0]);
		});
		if (lock[0] != null) {
			var face = net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
			net.minecraft.core.BlockPos[] stand = new net.minecraft.core.BlockPos[1];
			server.runOnServer(srv -> {
				var outer = srv.getLevel(dev.neothaum.world.outer.OuterLands.KEY);
				var dir = outer.getBlockState(lock[0]).getValue(face);
				stand[0] = lock[0].relative(dir, 6).below(2);
			});
			server.runCommand("execute in neothaum:outer_lands run tp @a " + (stand[0].getX() + 0.5) + " " + stand[0].getY() + " " + (stand[0].getZ() + 0.5)
				+ " facing " + (lock[0].getX() + 0.5) + " " + (lock[0].getY() + 0.5) + " " + (lock[0].getZ() + 0.5));
			ctx.waitTicks(40);
			shot(ctx, "outer_lock");
			// fit the Runed Tablet
			server.runOnServer(srv -> {
				var outer = srv.getLevel(dev.neothaum.world.outer.OuterLands.KEY);
				var player = srv.getPlayerList().getPlayers().get(0);
				var tablet = new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.RUNED_TABLET);
				var state = outer.getBlockState(lock[0]);
				var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(lock[0]), state.getValue(face), lock[0], false);
				var r = state.useItemOn(tablet, outer, player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
				NeoThaum.LOGGER.info("[visual-test] outer: tablet fitted = {}", r);
			});
			ctx.waitTicks(130);
			server.runOnServer(srv -> {
				var outer = srv.getLevel(dev.neothaum.world.outer.OuterLands.KEY);
				var box = new net.minecraft.world.phys.AABB(boss[0] * 16, 40, boss[1] * 16, boss[0] * 16 + 32, 70, boss[1] * 16 + 32);
				var bosses = outer.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, box).stream()
					.map(m -> m.getType().getDescriptionId() + (m.hasCustomName() ? " '" + m.getCustomName().getString() + "'" : "")).toList();
				NeoThaum.LOGGER.info("[visual-test] outer: lock gone = {}, woke {}", !outer.getBlockState(lock[0]).is(dev.neothaum.registry.TVBlocks.ANCIENT_LOCK),
					bosses);
			});
			server.runCommand("execute in neothaum:outer_lands run tp @a " + (boss[0] * 16 + 16.5) + " 56 " + (boss[1] * 16 + 16.5) + " 45 40");
			ctx.waitTicks(30);
			shot(ctx, "outer_boss_room");
		}
		// a tainted corridor's biome, and a crab vent steaming
		net.minecraft.core.BlockPos[] vent = new net.minecraft.core.BlockPos[1];
		server.runOnServer(srv -> {
			var outer = srv.getLevel(dev.neothaum.world.outer.OuterLands.KEY);
			for (int x = -20; x <= 20; x++) {
				for (int z = -20; z <= 20; z++) {
					var c = dev.neothaum.world.outer.MazeHandler.getCell(x, z);
					if (c != null && c.feature == 13) {
						outer.getChunk(x, z);
						var biome = outer.getBiome(new net.minecraft.core.BlockPos(x * 16 + 8, 54, z * 16 + 8));
						NeoThaum.LOGGER.info("[visual-test] outer: tainted corridor at chunk {},{} biome = {}", x, z, biome.unwrapKey().map(k -> k.identifier()).orElse(null));
					}
				}
			}
			for (int x = -2; x <= 2 && vent[0] == null; x++) {
				for (int z = -2; z <= 2 && vent[0] == null; z++) {
					outer.getChunk(x, z);
					for (var p : net.minecraft.core.BlockPos.betweenClosed(x * 16, 45, z * 16, x * 16 + 15, 65, z * 16 + 15)) {
						if (outer.getBlockState(p).is(dev.neothaum.registry.TVBlocks.CRUSTED_OPENING)) {
							vent[0] = p.immutable();
							break;
						}
					}
				}
			}
			NeoThaum.LOGGER.info("[visual-test] outer: a crab vent at {}", vent[0]);
		});
		if (vent[0] != null) {
			server.runOnServer(srv -> {
				var outer = srv.getLevel(dev.neothaum.world.outer.OuterLands.KEY);
				var dir = outer.getBlockState(vent[0]).getValue(dev.neothaum.blocks.eldritch.CrustedOpeningBlock.FACING);
				var at = vent[0].relative(dir, 2);
				srv.getCommands().performPrefixedCommand(srv.createCommandSourceStack(), "execute in neothaum:outer_lands run tp @a " + (at.getX() + 0.5)
					+ " " + (at.getY() - 1) + " " + (at.getZ() + 0.5) + " facing " + (vent[0].getX() + 0.5) + " " + (vent[0].getY() + 0.5) + " "
					+ (vent[0].getZ() + 0.5));
			});
			ctx.waitTicks(60);
			shot(ctx, "outer_crab_vent");
		}
		// and back through the maze's portal (after the portal cooldown wears off)
		server.runCommand("execute in neothaum:outer_lands run tp @a 12.5 52 8.5");
		ctx.waitTicks(120);
		server.runCommand("execute in neothaum:outer_lands run tp @a 8.5 53 8.5");
		ctx.waitTicks(40);
		server.runOnServer(srv -> {
			var player = srv.getPlayerList().getPlayers().get(0);
			NeoThaum.LOGGER.info("[visual-test] outer: home again: dimension={} pos={}", player.level().dimension().identifier(), player.blockPosition());
			player.removeAllEffects();
		});
		server.runCommand("kill @e[type=!player]");
		// an eldritch guardian's gaze clouds the overworld air
		server.runCommand("summon neothaum:eldritch_guardian 4 117 -4 {NoAI:1b}");
		ctx.waitTicks(120);
		ctx.runOnClient(mc -> NeoThaum.LOGGER.info("[visual-test] outer: guardian gaze: fog fiddled={} density={}",
			dev.neothaum.client.hud.FogFiddle.fiddled, dev.neothaum.client.hud.FogFiddle.target));
		server.runCommand("tp @a 0 117 6 180 0");
		ctx.waitTicks(5);
		shot(ctx, "outer_guardian_gaze");
		server.runCommand("kill @e[type=!player]");
	}

	private static void shot(ClientGameTestContext ctx, String name) {
		Path p = ctx.takeScreenshot("neothaum_" + name);
		NeoThaum.LOGGER.info("[visual-test] {} -> {}", name, p.toAbsolutePath());
	}
}
