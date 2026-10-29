package dev.neothaum.client.hud;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.items.ThaumometerItem;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.research.ScanManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * renderAspectsInGui (shift over a scanned item shows its aspects) and the thaumometer's readout of the scanned target.
 */
public final class AspectOverlays {
	private static final Identifier BACK = NeoThaum.id("textures/aspects/_back.png");
	private static final Identifier UNKNOWN = NeoThaum.id("textures/aspects/_unknown.png");

	public static void drawAspectRow(GuiGraphicsExtractor g, AspectList tags, int baseX, int baseY, PlayerKnowledge k) {
		int index = 0;
		for (Aspect tag : tags.getAspectsSortedAmount()) {
			int x = baseX + index * 18;
			GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, BACK, x - 2, baseY - 2, 20, 20, 0xFFFFFFFF);
			if (k.hasDiscoveredAspect(tag)) {
				GuiUtil.drawTag(g, x, baseY, tag, tags.getAmount(tag), 0, 771, 1.0F, false);
			} else {
				GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, UNKNOWN, x, baseY, 16, 16, 0xFFFFFFFF);
			}
			index++;
		}
	}

	/** Called after a container screen renders. */
	public static void renderInGui(AbstractContainerScreen<?> screen, GuiGraphicsExtractor g, int mx, int my) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.hasShiftDown() == dev.neothaum.config.NeoThaumConfig.current.displayAspects || mc.player == null) {
			return;
		}
		Slot slot = ((dev.neothaum.client.mixin.ContainerScreenAccessor) screen).neothaum$hoveredSlot();
		if (slot == null || !slot.hasItem()) {
			return;
		}
		ItemStack stack = slot.getItem();
		ScanManager.ScanResult sr = ScanManager.ScanResult.item(stack.getItem());
		if (!ScanManager.hasBeenScanned(mc.player, sr)) {
			return;
		}
		AspectList tags = ObjectAspects.getAspects(stack);
		if (tags != null && tags.size() > 0) {
			drawAspectRow(g, tags, mx + 17 - 8, my + 7 - 33 - 8, ResearchManager.knowledge(mc.player));
		}
	}

	/** Thaumometer readout: name and (if scanned) aspects of whatever the thaumometer points at. */
	public static void renderThaumometer(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		Player p = mc.player;
		if (p == null || !(p.getMainHandItem().getItem() instanceof ThaumometerItem)) {
			return;
		}
		var target = ThaumometerItem.getPointedEntity(mc.level, p, 10.0);
		ScanManager.ScanResult sr = null;
		Component name = null;
		if (target != null) {
			sr = ScanManager.ScanResult.entity(target);
			name = target.getDisplayName();
		} else if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult bhr && mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
			var pos = bhr.getBlockPos();
			if (mc.level.getBlockEntity(pos) instanceof dev.neothaum.tiles.NodeBlockEntity nbe) {
				sr = ScanManager.ScanResult.node(pos);
				net.minecraft.network.chat.MutableComponent n = Component.translatable("nodetype." + nbe.getNodeType() + ".name");
				if (nbe.getNodeModifier() != null) {
					n.append(", ").append(Component.translatable("nodemod." + nbe.getNodeModifier() + ".name"));
				}
				name = n;
			} else if (mc.level.getBlockEntity(pos) instanceof dev.neothaum.tiles.JarNodeBlockEntity jbe) {
				sr = ScanManager.ScanResult.node(pos);
				net.minecraft.network.chat.MutableComponent n = Component.translatable("block.neothaum.node_in_a_jar");
				n.append(": ").append(Component.translatable("nodetype." + jbe.getNodeType() + ".name"));
				if (jbe.getNodeModifier() != null) {
					n.append(", ").append(Component.translatable("nodemod." + jbe.getNodeModifier() + ".name"));
				}
				name = n;
			} else {
				var item = mc.level.getBlockState(pos).getBlock().asItem();
				if (item != net.minecraft.world.item.Items.AIR) {
					sr = ScanManager.ScanResult.item(item);
					name = item.getName(new ItemStack(item));
				}
			}
		}
		if (sr == null) {
			return;
		}
		int cx = g.guiWidth() / 2;
		int cy = g.guiHeight() / 2;
		boolean scanned = ScanManager.hasBeenScanned(p, sr);
		g.text(mc.font, name, cx + 12, cy - 4, 0xFFFFFFFF, true);
		if (scanned) {
			AspectList tags = null;
			if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult bhr
				&& mc.level.getBlockEntity(bhr.getBlockPos()) instanceof dev.neothaum.tiles.NodeBlockEntity nbe) {
				tags = nbe.getAspects();
			} else if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult bhr
				&& mc.level.getBlockEntity(bhr.getBlockPos()) instanceof dev.neothaum.tiles.JarNodeBlockEntity jbe) {
				tags = jbe.getAspects();
			} else {
				tags = ScanManager.getScanAspects(sr, mc.level);
			}
			if (tags != null) {
				drawAspectRow(g, tags, cx + 12, cy + 8, ResearchManager.knowledge(p));
			}
		} else {
			g.text(mc.font, "?", cx + 12, cy + 8, 0xFFAA88FF, true);
		}
	}

	private AspectOverlays() {
	}
}
