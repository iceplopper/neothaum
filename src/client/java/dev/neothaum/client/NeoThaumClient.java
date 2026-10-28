package dev.neothaum.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.neothaum.ClientBridge;
import dev.neothaum.NeoThaum;
import dev.neothaum.blocks.CrystalClusterBlock;
import dev.neothaum.client.fx.FXImpl;
import dev.neothaum.client.gui.ArcaneWorkbenchScreen;
import dev.neothaum.client.gui.BaublesScreen;
import dev.neothaum.client.gui.ResearchTableScreen;
import dev.neothaum.client.gui.ThaumonomiconScreen;
import dev.neothaum.client.hud.AspectOverlays;
import dev.neothaum.client.hud.Notifications;
import dev.neothaum.client.hud.ResearchPopup;
import dev.neothaum.client.hud.WandHud;
import dev.neothaum.client.render.ArcaneWorktableRenderer;
import dev.neothaum.client.render.CrucibleRenderer;
import dev.neothaum.client.render.NodeRenderer;
import dev.neothaum.client.render.TVRenderTypes;
import dev.neothaum.fx.FX;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVMenus;
import dev.neothaum.research.ResearchNoteData;
import dev.neothaum.tiles.ResearchTableBlockEntity;
import dev.neothaum.wands.WandManager;
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

public class NeoThaumClient implements ClientModInitializer {
	public static KeyMapping baublesKey;
	public static KeyMapping harnessKey;

	@Override
	public void onInitializeClient() {
		TVRenderTypes.init();
		// BlockFluidPure's one texture, still and flowing
		var pure = new net.minecraft.client.resources.model.sprite.Material(dev.neothaum.NeoThaum.id("blocks/fluidpure"));
		net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry.register(dev.neothaum.fluids.TVFluids.PURIFYING,
			dev.neothaum.fluids.TVFluids.FLOWING_PURIFYING, new net.minecraft.client.renderer.block.FluidModel.Unbaked(pure, pure, null, null));
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
			if (dev.neothaum.devtools.DevTools.ITEMS.contains(stack.getItem())) {
				String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
				String key = id.startsWith("dev_node_placer") ? "devtools.desc.node_placer" : "devtools.desc." + id;
				lines.add(net.minecraft.network.chat.Component.translatable(key).withStyle(net.minecraft.ChatFormatting.GRAY));
				lines.add(net.minecraft.network.chat.Component.translatable("devtools.desc.creative").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
			}
		});
		// EventHandlerRunic.tooltipEvent: runic shield strength
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
			int charge = dev.neothaum.api.RunicArmor.getFinalCharge(stack);
			if (charge > 0) {
				lines.add(Component.translatable("item.runic.charge").append(" +" + charge).withStyle(net.minecraft.ChatFormatting.GOLD));
			}
		});
		// jars and labels: what they hold and what they are marked for
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
			if (stack.is(TVItems.PHIAL_OF_ESSENTIA)) {
				return;
			}
			var al = stack.get(dev.neothaum.registry.TVComponents.ASPECTS);
			if (al != null && (stack.is(TVBlocks.WARDED_JAR.asItem()) || stack.is(TVBlocks.VOID_JAR.asItem()))) {
				for (var a : al.getAspects()) {
					lines.add(Component.literal(a.getName() + " x " + al.getAmount(a)).withStyle(net.minecraft.ChatFormatting.GRAY));
				}
			}
			var filter = stack.get(dev.neothaum.registry.TVComponents.ASPECT_FILTER);
			if (filter != null) {
				lines.add(Component.literal("§5" + filter.getName()));
			}
		});
		ClientBridge.burst = (e, size) -> Minecraft.getInstance().particleEngine.add(new dev.neothaum.client.fx.TCParticles.Burst(
			(net.minecraft.client.multiplayer.ClientLevel) e.level(), e.getX(), e.getY() + 0.45F, e.getZ(), size));
		ClientBridge.oreScan = dev.neothaum.client.render.OreScanOverlay::startScan;
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.neothaum.client.render.OreScanOverlay::render);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.neothaum.client.render.ShieldFX::render);
		ClientTickEvents.END_CLIENT_TICK.register(dev.neothaum.client.render.ShieldFX::tick);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.neothaum.client.fx.ArcSonicFX::render);
		ClientTickEvents.END_CLIENT_TICK.register(dev.neothaum.client.fx.ArcSonicFX::tick);
		ClientTickEvents.END_CLIENT_TICK.register(mc -> dev.neothaum.client.hud.FogFiddle.tick());
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.player != null) {
				dev.neothaum.items.equipment.TravellerBootsItem.clientTick(mc.player);
			}
		});
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.neothaum.client.fx.BoltFX::render);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.neothaum.client.render.ContainerTags::render);
		ClientTickEvents.END_CLIENT_TICK.register(dev.neothaum.client.fx.BoltFX::tick);
		dev.neothaum.client.entity.TVEntityRenderers.init();
		dev.neothaum.client.render.armor.TCArmorRenderers.init();
		ClientBridge.openNodeEditor = pos -> Minecraft.getInstance().gui.setScreen(new dev.neothaum.client.devtools.NodeEditorScreen(pos));

		MenuScreens.register(TVMenus.ARCANE_WORKBENCH, ArcaneWorkbenchScreen::new);
		MenuScreens.register(TVMenus.RESEARCH_TABLE, ResearchTableScreen::new);
		MenuScreens.register(TVMenus.BAUBLES, BaublesScreen::new);
		MenuScreens.register(TVMenus.ALCHEMY_FURNACE, dev.neothaum.client.gui.AlchemyFurnaceScreen::new);
		MenuScreens.register(TVMenus.PECH, dev.neothaum.client.gui.PechScreen::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.JAR, dev.neothaum.client.render.JarRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ALEMBIC, dev.neothaum.client.render.AlembicRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ETHEREAL_BLOOM, dev.neothaum.client.render.EtherealBloomRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.TUBE, dev.neothaum.client.render.TubeRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.BUFFER, dev.neothaum.client.render.BufferRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.BELLOWS, dev.neothaum.client.render.BellowsRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.BRAIN_JAR, dev.neothaum.client.render.BrainJarRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.PEDESTAL, dev.neothaum.client.render.PedestalRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.INFUSION_MATRIX, dev.neothaum.client.render.RunicMatrixRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.INFUSION_PILLAR, dev.neothaum.client.render.InfusionPillarRenderer::new);

		BlockEntityRendererRegistry.register(TVBlockEntities.CENTRIFUGE, dev.neothaum.client.render.CentrifugeRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.CRYSTALLIZER, dev.neothaum.client.render.CrystallizerRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.RESERVOIR, dev.neothaum.client.render.ReservoirRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.THAUMATORIUM, dev.neothaum.client.render.ThaumatoriumRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ADVANCED_ALCHEMY_FURNACE, dev.neothaum.client.render.AdvancedAlchemyFurnaceRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ARCANE_BORE_BASE, dev.neothaum.client.render.ArcaneBoreBaseRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ARCANE_BORE, dev.neothaum.client.render.ArcaneBoreRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.MANA_POD, dev.neothaum.client.render.ManaPodRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.FLUX_SCRUBBER, dev.neothaum.client.render.FluxScrubberRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.BANNER, dev.neothaum.client.render.BannerRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.NODE, NodeRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.JAR_NODE, dev.neothaum.client.render.JarNodeRenderer::new);
		dev.neothaum.items.JarNodeItem.DISCOVERED = a -> {
			var p = net.minecraft.client.Minecraft.getInstance().player;
			return p == null || dev.neothaum.research.ResearchManager.knowledge(p).hasDiscoveredAspect(a);
		};
		BlockEntityRendererRegistry.register(TVBlockEntities.WARDED, dev.neothaum.client.render.WardedBlockRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.VIS_RELAY, dev.neothaum.client.render.VisDeviceRenderers.Relay::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.WORKBENCH_CHARGER, dev.neothaum.client.render.VisDeviceRenderers.Relay::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.NODE_STABILIZER, dev.neothaum.client.render.VisDeviceRenderers.Stabilizer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.NODE_TRANSDUCER, dev.neothaum.client.render.VisDeviceRenderers.Transducer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ENERGIZED_NODE, dev.neothaum.client.render.VisDeviceRenderers.EnergizedNode::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.WAND_PEDESTAL, dev.neothaum.client.render.VisDeviceRenderers.WandPedestal::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.FOCAL_MANIPULATOR, dev.neothaum.client.render.VisDeviceRenderers.FocalManipulator::new);
		MenuScreens.register(TVMenus.FOCAL_MANIPULATOR, dev.neothaum.client.gui.FocalManipulatorScreen::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ARCANE_FURNACE, dev.neothaum.client.render.ArcaneFurnaceRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.CRUCIBLE, CrucibleRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ARCANE_WORKTABLE, ArcaneWorktableRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.RESEARCH_TABLE, dev.neothaum.client.render.ResearchTableRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.CRYSTAL, dev.neothaum.client.render.CrystalRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ELDRITCH_NOTHING, dev.neothaum.client.render.EldritchNothingRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.CRUSTED_OPENING, dev.neothaum.client.render.CrustedOpeningRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ANCIENT_LOCK, dev.neothaum.client.render.AncientLockRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.HUNGRY_CHEST, dev.neothaum.client.render.HungryChestRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ELDRITCH_ALTAR, dev.neothaum.client.render.EldritchAltarRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ELDRITCH_CAP, dev.neothaum.client.render.EldritchObeliskRenderer.CapRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ELDRITCH_OBELISK, dev.neothaum.client.render.EldritchObeliskRenderer::new);
		BlockEntityRendererRegistry.register(TVBlockEntities.ELDRITCH_PORTAL, dev.neothaum.client.render.EldritchPortalRenderer::new);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("crystal"), dev.neothaum.client.render.CrystalRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("thaumometer"), dev.neothaum.client.render.ThaumometerRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("wand"), dev.neothaum.client.render.WandRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("jar_node"), dev.neothaum.client.render.JarNodeRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("vis_device"), dev.neothaum.client.render.VisDeviceRenderers.DeviceUnbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("alembic"), dev.neothaum.client.render.AlembicRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("device"), dev.neothaum.client.render.DeviceItemRenderers.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("bellows"), dev.neothaum.client.render.BellowsRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("runic_matrix"), dev.neothaum.client.render.RunicMatrixRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("hungry_chest"), dev.neothaum.client.render.HungryChestRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("centrifuge"), dev.neothaum.client.render.CentrifugeRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("crystallizer"), dev.neothaum.client.render.CrystallizerRenderer.Unbaked.CODEC);
		dev.neothaum.client.mixin.SpecialModelRenderersAccessor.neothaum$idMapper().put(NeoThaum.id("reservoir"), dev.neothaum.client.render.ReservoirRenderer.Unbaked.CODEC);

		registerTints();
		net.minecraft.client.color.item.ItemTintSources.ID_MAPPER.put(NeoThaum.id("aspect"), dev.neothaum.client.render.AspectTint.MAP_CODEC);

		HudElementRegistry.addFirst(NeoThaum.id("warp_vignette"), dev.neothaum.client.hud.WarpVisuals::render);
		ClientTickEvents.END_CLIENT_TICK.register(dev.neothaum.client.hud.WarpVisuals::tick);
		HudElementRegistry.addLast(NeoThaum.id("wand_dial"), WandHud::render);
		HudElementRegistry.addLast(NeoThaum.id("focus_radial"), dev.neothaum.client.hud.FocusRadial::render);
		dev.neothaum.client.hud.FocusRadial.init();
		ClientTickEvents.START_CLIENT_TICK.register(dev.neothaum.client.hud.FocusRadial::startTick);
		ClientTickEvents.END_CLIENT_TICK.register(dev.neothaum.client.hud.FocusRadial::tick);
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.player != null && !mc.isPaused()) {
				dev.neothaum.items.equipment.TCEnchantments.updateSpeed(mc.player);
			}
		});
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.neothaum.client.fx.BeamFX::render);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.neothaum.client.render.ArchitectOverlay::render);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(dev.neothaum.client.render.BellOverlay::render);
		ClientTickEvents.END_CLIENT_TICK.register(dev.neothaum.client.fx.BeamFX::tick);
		MenuScreens.register(TVMenus.FOCUS_POUCH, dev.neothaum.client.gui.FocusPouchScreen::new);
		MenuScreens.register(TVMenus.HOVER_HARNESS, dev.neothaum.client.gui.HoverHarnessScreen::new);
		MenuScreens.register(TVMenus.HAND_MIRROR, dev.neothaum.client.gui.HandMirrorScreen::new);
		MenuScreens.register(TVMenus.THAUMATORIUM, dev.neothaum.client.gui.ThaumatoriumScreen::new);
		MenuScreens.register(TVMenus.SPA, dev.neothaum.client.gui.SpaScreen::new);
		MenuScreens.register(TVMenus.DECONSTRUCTION_TABLE, dev.neothaum.client.gui.DeconstructionTableScreen::new);
		MenuScreens.register(TVMenus.ARCANE_BORE, dev.neothaum.client.gui.ArcaneBoreScreen::new);
		MenuScreens.register(TVMenus.GOLEM, dev.neothaum.client.gui.GolemScreen::new);
		HudElementRegistry.addLast(NeoThaum.id("notifications"), Notifications::render);
		HudElementRegistry.addLast(NeoThaum.id("research_popup"), ResearchPopup::render);
		HudElementRegistry.addLast(NeoThaum.id("runic_shield"), dev.neothaum.client.hud.RunicShieldHud::render);
		HudElementRegistry.addLast(NeoThaum.id("sanity"), dev.neothaum.client.hud.SanityHud::render);
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.player != null && !mc.isPaused()) {
				dev.neothaum.items.equipment.TravellerBootsItem.clientTick(mc.player);
				dev.neothaum.items.equipment.ThaumostaticHarnessItem.clientTick(mc.player);
			}
		});

		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (screen instanceof AbstractContainerScreen<?> acs) {
				ScreenEvents.afterExtract(screen).register((s, g, mx, my, a) -> AspectOverlays.renderInGui(acs, g, mx, my));
			}
			// Baubles' inventory button, beside the top of the player portrait
			Component baubles = Component.translatable("container.baubles");
			if (screen instanceof InventoryScreen inv) {
				Screens.getWidgets(screen).add(new dev.neothaum.client.gui.BaublesButton(inv, 64, 9, dev.neothaum.client.gui.BaublesButton.BAUBLES_ICON, baubles, () -> true,
					() -> TVNetwork.clientSender.accept(new TVNetwork.OpenBaubles())));
			} else if (screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen creative) {
				Screens.getWidgets(screen).add(new dev.neothaum.client.gui.BaublesButton(creative, 96, 6, dev.neothaum.client.gui.BaublesButton.BAUBLES_ICON, baubles, creative::isInventoryOpen,
					() -> TVNetwork.clientSender.accept(new TVNetwork.OpenBaubles())));
			} else if (screen instanceof BaublesScreen bs) {
				Screens.getWidgets(screen).add(new dev.neothaum.client.gui.BaublesButton(bs, 64, 9, dev.neothaum.client.gui.BaublesButton.INVENTORY_ICON, Component.translatable("container.inventory"),
					() -> true, () -> {
						client.player.closeContainer();
						client.gui.setScreen(new InventoryScreen(client.player));
					}));
			}
		});

		baublesKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.neothaum.baubles", InputConstants.KEY_B,
			KeyMapping.Category.INVENTORY));
		harnessKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.neothaum.harness", InputConstants.KEY_H,
			KeyMapping.Category.MISC));
		ClientTickEvents.END_CLIENT_TICK.register(ThaumometerInventoryScan::tick);
		ClientTickEvents.END_CLIENT_TICK.register(dev.neothaum.client.fx.SourceFX::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (baublesKey.consumeClick()) {
				if (client.player != null && client.gui.screen() == null) {
					TVNetwork.clientSender.accept(new TVNetwork.OpenBaubles());
				}
			}
			while (harnessKey.consumeClick()) {
				if (client.player != null && client.gui.screen() == null) {
					var chest = client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
					if (chest.getItem() instanceof dev.neothaum.items.equipment.ThaumostaticHarnessItem) {
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
			int c = 0xFF000000 | ((dev.neothaum.blocks.TallowCandleBlock) candle).color();
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
				if (level.getBlockEntity(pos) instanceof dev.neothaum.tiles.TubeBlockEntity t && t.aspectFilter != null) {
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

		dev.neothaum.client.addon.minimap.MinimapIntegration.init();
	}

	static {
		InventoryScreen.class.getClass();
	}
}
