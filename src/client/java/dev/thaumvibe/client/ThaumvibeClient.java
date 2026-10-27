package dev.thaumvibe.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.thaumvibe.ClientBridge;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.blocks.CrystalClusterBlock;
import dev.thaumvibe.client.fx.FXImpl;
import dev.thaumvibe.client.gui.ArcaneWorkbenchScreen;
import dev.thaumvibe.client.gui.BaublesScreen;
import dev.thaumvibe.client.gui.ResearchTableScreen;
import dev.thaumvibe.client.gui.ThaumonomiconScreen;
import dev.thaumvibe.client.hud.AspectOverlays;
import dev.thaumvibe.client.hud.Notifications;
import dev.thaumvibe.client.hud.ResearchPopup;
import dev.thaumvibe.client.hud.WandHud;
import dev.thaumvibe.client.render.ArcaneWorktableRenderer;
import dev.thaumvibe.client.render.CrucibleRenderer;
import dev.thaumvibe.client.render.NodeRenderer;
import dev.thaumvibe.client.render.TVRenderTypes;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVMenus;
import dev.thaumvibe.research.ResearchNoteData;
import dev.thaumvibe.tiles.ResearchTableBlockEntity;
import dev.thaumvibe.wands.WandManager;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;

public class ThaumvibeClient implements ClientModInitializer {
	public static KeyMapping baublesKey;
	public static KeyMapping harnessKey;

	@Override
	public void onInitializeClient() {
		TVRenderTypes.init();
		FX.install(new FXImpl());
		TVClientNetwork.init();
		WandManager.setClientHooks(() -> Minecraft.getInstance().hasShiftDown(), () -> Minecraft.getInstance().player);
		ClientBridge.openScreen = name -> {
			if (name.equals("thaumonomicon")) {
				Minecraft.getInstance().gui.setScreen(new ThaumonomiconScreen());
			}
		};

		// dev tools: what each one does, under its name
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
			if (dev.thaumvibe.devtools.DevTools.ITEMS.contains(stack.getItem())) {
				String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
				String key = id.startsWith("dev_node_placer") ? "devtools.desc.node_placer" : "devtools.desc." + id;
				lines.add(net.minecraft.network.chat.Component.translatable(key).withStyle(net.minecraft.ChatFormatting.GRAY));
				lines.add(net.minecraft.network.chat.Component.translatable("devtools.desc.creative").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
			}
		});
		// EventHandlerRunic.tooltipEvent: runic shield strength
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
			int charge = dev.thaumvibe.api.RunicArmor.getFinalCharge(stack);
			if (charge > 0) {
				lines.add(Component.translatable("item.runic.charge").append(" +" + charge).withStyle(net.minecraft.ChatFormatting.GOLD));
			}
		});
		// jars and labels: what they hold and what they are marked for
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
			if (stack.is(TVItems.PHIAL_OF_ESSENTIA)) {
				return;
			}
			var al = stack.get(dev.thaumvibe.registry.TVComponents.ASPECTS);
			if (al != null && (stack.is(TVBlocks.WARDED_JAR.asItem()) || stack.is(TVBlocks.VOID_JAR.asItem()))) {
				for (var a : al.getAspects()) {
					lines.add(Component.literal(a.getName() + " x " + al.getAmount(a)).withStyle(net.minecraft.ChatFormatting.GRAY));
				}
			}
			var filter = stack.get(dev.thaumvibe.registry.TVComponents.ASPECT_FILTER);
			if (filter != null) {
				lines.add(Component.literal("§5" + filter.getName()));
			}
		});
		ClientBridge.burst = (e, size) -> Minecraft.getInstance().particleEngine.add(new dev.thaumvibe.client.fx.TCParticles.Burst(
			(net.minecraft.client.multiplayer.ClientLevel) e.level(), e.getX(), e.getY() + 0.45F, e.getZ(), size));
		ClientBridge.oreScan = dev.thaumvibe.client.render.OreScanOverlay::startScan;
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.thaumvibe.client.render.OreScanOverlay::render);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.thaumvibe.client.render.ShieldFX::render);
		ClientTickEvents.END_CLIENT_TICK.register(dev.thaumvibe.client.render.ShieldFX::tick);
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.player != null) {
				dev.thaumvibe.items.equipment.TravellerBootsItem.clientTick(mc.player);
			}
		});
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.thaumvibe.client.fx.BoltFX::render);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.thaumvibe.client.render.ContainerTags::render);
		ClientTickEvents.END_CLIENT_TICK.register(dev.thaumvibe.client.fx.BoltFX::tick);
		dev.thaumvibe.client.entity.TVEntityRenderers.init();
		ClientBridge.openNodeEditor = pos -> Minecraft.getInstance().gui.setScreen(new dev.thaumvibe.client.devtools.NodeEditorScreen(pos));

		MenuScreens.register(TVMenus.ARCANE_WORKBENCH, ArcaneWorkbenchScreen::new);
		MenuScreens.register(TVMenus.RESEARCH_TABLE, ResearchTableScreen::new);
		MenuScreens.register(TVMenus.BAUBLES, BaublesScreen::new);
		MenuScreens.register(TVMenus.ALCHEMY_FURNACE, dev.thaumvibe.client.gui.AlchemyFurnaceScreen::new);
		MenuScreens.register(TVMenus.PECH, dev.thaumvibe.client.gui.PechScreen::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.JAR, dev.thaumvibe.client.render.JarRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ALEMBIC, dev.thaumvibe.client.render.AlembicRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.TUBE, dev.thaumvibe.client.render.TubeRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.BUFFER, dev.thaumvibe.client.render.BufferRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.BELLOWS, dev.thaumvibe.client.render.BellowsRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.BRAIN_JAR, dev.thaumvibe.client.render.BrainJarRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.PEDESTAL, dev.thaumvibe.client.render.PedestalRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.INFUSION_MATRIX, dev.thaumvibe.client.render.RunicMatrixRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.INFUSION_PILLAR, dev.thaumvibe.client.render.InfusionPillarRenderer::new);

		BlockEntityRendererRegistry.register(TVBlockEntities.CENTRIFUGE, dev.thaumvibe.client.render.CentrifugeRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.CRYSTALLIZER, dev.thaumvibe.client.render.CrystallizerRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.RESERVOIR, dev.thaumvibe.client.render.ReservoirRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.THAUMATORIUM, dev.thaumvibe.client.render.ThaumatoriumRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ADVANCED_ALCHEMY_FURNACE, dev.thaumvibe.client.render.AdvancedAlchemyFurnaceRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ARCANE_BORE_BASE, dev.thaumvibe.client.render.ArcaneBoreBaseRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ARCANE_BORE, dev.thaumvibe.client.render.ArcaneBoreRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.MANA_POD, dev.thaumvibe.client.render.ManaPodRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.NODE, NodeRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.JAR_NODE, dev.thaumvibe.client.render.JarNodeRenderer::new);
		dev.thaumvibe.items.JarNodeItem.DISCOVERED = a -> {
			var p = net.minecraft.client.Minecraft.getInstance().player;
			return p == null || dev.thaumvibe.research.ResearchManager.knowledge(p).hasDiscoveredAspect(a);
		};
		BlockEntityRendererRegistry.register(TVBlockEntities.WARDED, dev.thaumvibe.client.render.WardedBlockRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.VIS_RELAY, dev.thaumvibe.client.render.VisDeviceRenderers.Relay::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.WORKBENCH_CHARGER, dev.thaumvibe.client.render.VisDeviceRenderers.Relay::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.NODE_STABILIZER, dev.thaumvibe.client.render.VisDeviceRenderers.Stabilizer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.NODE_TRANSDUCER, dev.thaumvibe.client.render.VisDeviceRenderers.Transducer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ENERGIZED_NODE, dev.thaumvibe.client.render.VisDeviceRenderers.EnergizedNode::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.WAND_PEDESTAL, dev.thaumvibe.client.render.VisDeviceRenderers.WandPedestal::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.FOCAL_MANIPULATOR, dev.thaumvibe.client.render.VisDeviceRenderers.FocalManipulator::new);
		MenuScreens.register(TVMenus.FOCAL_MANIPULATOR, dev.thaumvibe.client.gui.FocalManipulatorScreen::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ARCANE_FURNACE, dev.thaumvibe.client.render.ArcaneFurnaceRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.CRUCIBLE, CrucibleRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ARCANE_WORKTABLE, ArcaneWorktableRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.RESEARCH_TABLE, dev.thaumvibe.client.render.ResearchTableRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.CRYSTAL, dev.thaumvibe.client.render.CrystalRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.HUNGRY_CHEST, dev.thaumvibe.client.render.HungryChestRenderer::new);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("crystal"), dev.thaumvibe.client.render.CrystalRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("thaumometer"), dev.thaumvibe.client.render.ThaumometerRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("wand"), dev.thaumvibe.client.render.WandRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("jar_node"), dev.thaumvibe.client.render.JarNodeRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("vis_device"), dev.thaumvibe.client.render.VisDeviceRenderers.DeviceUnbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("alembic"), dev.thaumvibe.client.render.AlembicRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("bellows"), dev.thaumvibe.client.render.BellowsRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("runic_matrix"), dev.thaumvibe.client.render.RunicMatrixRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("hungry_chest"), dev.thaumvibe.client.render.HungryChestRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("centrifuge"), dev.thaumvibe.client.render.CentrifugeRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("crystallizer"), dev.thaumvibe.client.render.CrystallizerRenderer.Unbaked.CODEC);
		dev.thaumvibe.client.mixin.SpecialModelRenderersAccessor.thaumvibe$idMapper().put(Thaumvibe.id("reservoir"), dev.thaumvibe.client.render.ReservoirRenderer.Unbaked.CODEC);

		registerTints();
		net.minecraft.client.color.item.ItemTintSources.ID_MAPPER.put(Thaumvibe.id("aspect"), dev.thaumvibe.client.render.AspectTint.MAP_CODEC);

		HudElementRegistry.addLast(Thaumvibe.id("wand_dial"), WandHud::render);
		HudElementRegistry.addLast(Thaumvibe.id("focus_radial"), dev.thaumvibe.client.hud.FocusRadial::render);
		dev.thaumvibe.client.hud.FocusRadial.init();
		ClientTickEvents.START_CLIENT_TICK.register(dev.thaumvibe.client.hud.FocusRadial::startTick);
		ClientTickEvents.END_CLIENT_TICK.register(dev.thaumvibe.client.hud.FocusRadial::tick);
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.player != null && !mc.isPaused()) {
				dev.thaumvibe.items.equipment.TCEnchantments.updateSpeed(mc.player);
			}
		});
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.thaumvibe.client.fx.BeamFX::render);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.thaumvibe.client.render.ArchitectOverlay::render);
		ClientTickEvents.END_CLIENT_TICK.register(dev.thaumvibe.client.fx.BeamFX::tick);
		MenuScreens.register(TVMenus.FOCUS_POUCH, dev.thaumvibe.client.gui.FocusPouchScreen::new);
		MenuScreens.register(TVMenus.HOVER_HARNESS, dev.thaumvibe.client.gui.HoverHarnessScreen::new);
		MenuScreens.register(TVMenus.HAND_MIRROR, dev.thaumvibe.client.gui.HandMirrorScreen::new);
		MenuScreens.register(TVMenus.THAUMATORIUM, dev.thaumvibe.client.gui.ThaumatoriumScreen::new);
		MenuScreens.register(TVMenus.SPA, dev.thaumvibe.client.gui.SpaScreen::new);
		MenuScreens.register(TVMenus.DECONSTRUCTION_TABLE, dev.thaumvibe.client.gui.DeconstructionTableScreen::new);
		MenuScreens.register(TVMenus.ARCANE_BORE, dev.thaumvibe.client.gui.ArcaneBoreScreen::new);
		HudElementRegistry.addLast(Thaumvibe.id("notifications"), Notifications::render);
		HudElementRegistry.addLast(Thaumvibe.id("research_popup"), ResearchPopup::render);
		HudElementRegistry.addLast(Thaumvibe.id("runic_shield"), dev.thaumvibe.client.hud.RunicShieldHud::render);
		HudElementRegistry.addLast(Thaumvibe.id("sanity"), dev.thaumvibe.client.hud.SanityHud::render);
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.player != null && !mc.isPaused()) {
				dev.thaumvibe.items.equipment.TravellerBootsItem.clientTick(mc.player);
				dev.thaumvibe.items.equipment.ThaumostaticHarnessItem.clientTick(mc.player);
			}
		});

		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (screen instanceof AbstractContainerScreen<?> acs) {
				ScreenEvents.afterExtract(screen).register((s, g, mx, my, a) -> AspectOverlays.renderInGui(acs, g, mx, my));
			}
			// Baubles' inventory button, beside the top of the player portrait
			Component baubles = Component.translatable("key.thaumvibe.baubles");
			if (screen instanceof InventoryScreen inv) {
				Screens.getWidgets(screen).add(new dev.thaumvibe.client.gui.BaublesButton(inv, 66, 9, baubles, () -> true,
					() -> TVNetwork.clientSender.accept(new TVNetwork.OpenBaubles())));
			} else if (screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen creative) {
				Screens.getWidgets(screen).add(new dev.thaumvibe.client.gui.BaublesButton(creative, 96, 6, baubles, creative::isInventoryOpen,
					() -> TVNetwork.clientSender.accept(new TVNetwork.OpenBaubles())));
			} else if (screen instanceof BaublesScreen bs) {
				Screens.getWidgets(screen).add(new dev.thaumvibe.client.gui.BaublesButton(bs, 66, 9, Component.translatable("container.inventory"),
					() -> true, () -> {
						client.player.closeContainer();
						client.gui.setScreen(new InventoryScreen(client.player));
					}));
			}
		});

		baublesKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.thaumvibe.baubles", InputConstants.KEY_B,
			KeyMapping.Category.INVENTORY));
		harnessKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.thaumvibe.harness", InputConstants.KEY_H,
			KeyMapping.Category.MISC));
		ClientTickEvents.END_CLIENT_TICK.register(ThaumometerInventoryScan::tick);
		ClientTickEvents.END_CLIENT_TICK.register(dev.thaumvibe.client.fx.SourceFX::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (baublesKey.consumeClick()) {
				if (client.player != null && client.gui.screen() == null) {
					TVNetwork.clientSender.accept(new TVNetwork.OpenBaubles());
				}
			}
			while (harnessKey.consumeClick()) {
				if (client.player != null && client.gui.screen() == null) {
					var chest = client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
					if (chest.getItem() instanceof dev.thaumvibe.items.equipment.ThaumostaticHarnessItem) {
						TVNetwork.clientSender.accept(new TVNetwork.ToggleHarness());
					}
				}
			}
		});
	}

	private static void registerTints() {
		int[] ore = {0xFFFF7E, 0xFF3C01, 0x0090FF, 0x00A000, 0xEECCFF, 0x555577};
		var infused = new net.minecraft.world.level.block.Block[]{TVBlocks.AIR_INFUSED_STONE, TVBlocks.FIRE_INFUSED_STONE, TVBlocks.WATER_INFUSED_STONE,
			TVBlocks.EARTH_INFUSED_STONE, TVBlocks.ORDER_INFUSED_STONE, TVBlocks.ENTROPY_INFUSED_STONE};
		for (int i = 0; i < infused.length; i++) {
			int c = 0xFF000000 | ore[i];
			BlockColorRegistry.register(List.of((BlockTintSource) s -> c), infused[i]);
		}
		for (var candle : TVBlocks.TALLOW_CANDLES) {
			int c = 0xFF000000 | ((dev.thaumvibe.blocks.TallowCandleBlock) candle).color();
			BlockColorRegistry.register(List.of((BlockTintSource) s -> c), candle);
		}
		var deepInfused = new net.minecraft.world.level.block.Block[]{TVBlocks.DEEPSLATE_AIR_INFUSED_STONE, TVBlocks.DEEPSLATE_FIRE_INFUSED_STONE,
			TVBlocks.DEEPSLATE_WATER_INFUSED_STONE, TVBlocks.DEEPSLATE_EARTH_INFUSED_STONE, TVBlocks.DEEPSLATE_ORDER_INFUSED_STONE,
			TVBlocks.DEEPSLATE_ENTROPY_INFUSED_STONE};
		for (int i = 0; i < deepInfused.length; i++) {
			int c = 0xFF000000 | ore[i];
			BlockColorRegistry.register(List.of((BlockTintSource) s -> c), deepInfused[i]);
		}
		for (var b : new net.minecraft.world.level.block.Block[]{TVBlocks.AIR_CRYSTAL_CLUSTER, TVBlocks.FIRE_CRYSTAL_CLUSTER, TVBlocks.WATER_CRYSTAL_CLUSTER,
			TVBlocks.EARTH_CRYSTAL_CLUSTER, TVBlocks.ORDER_CRYSTAL_CLUSTER, TVBlocks.ENTROPY_CRYSTAL_CLUSTER, TVBlocks.MIXED_CRYSTAL_CLUSTER}) {
			int c = 0xFF000000 | CrystalClusterBlock.COLORS[((CrystalClusterBlock) b).type];
			BlockColorRegistry.register(List.of((BlockTintSource) s -> c), b);
		}
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return 0xFF000000 | FoliageColor.FOLIAGE_DEFAULT;
			}

			@Override
			public int colorInWorld(BlockState state, net.minecraft.client.renderer.block.BlockAndTintGetter level, net.minecraft.core.BlockPos pos) {
				return 0xFF000000 | net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(level, pos);
			}
		}), TVBlocks.GREATWOOD_LEAVES);
		BlockColorRegistry.register(List.of((BlockTintSource) s -> 0xFF000000 | 8952234), TVBlocks.SILVERWOOD_LEAVES);
		// BlockTubeRenderer meta 3: the filter's core takes its aspect's colour
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return 0xFFFFFFFF;
			}

			@Override
			public int colorInWorld(BlockState state, net.minecraft.client.renderer.block.BlockAndTintGetter level, net.minecraft.core.BlockPos pos) {
				if (level.getBlockEntity(pos) instanceof dev.thaumvibe.tiles.TubeBlockEntity t && t.aspectFilter != null) {
					return 0xFF000000 | t.aspectFilter.getColor();
				}
				return 0xFFFFFFFF;
			}
		}), TVBlocks.FILTERED_ESSENTIA_TUBE);
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return 0xFF999999;
			}

			@Override
			public int colorInWorld(BlockState state, net.minecraft.client.renderer.block.BlockAndTintGetter level, net.minecraft.core.BlockPos pos) {
				if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity rt) {
					ResearchNoteData d = rt.getData();
					if (d != null) {
						return 0xFF000000 | d.color;
					}
				}
				return 0xFF999999;
			}
		}), TVBlocks.RESEARCH_TABLE);
	}

	static {
		InventoryScreen.class.getClass();
	}
}
