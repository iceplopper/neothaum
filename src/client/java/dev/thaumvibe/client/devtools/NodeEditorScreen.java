package dev.thaumvibe.client.devtools;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.nodes.NodeModifier;
import dev.thaumvibe.api.nodes.NodeType;
import dev.thaumvibe.client.gui.GuiUtil;
import dev.thaumvibe.devtools.DevTools;
import dev.thaumvibe.tiles.NodeBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Dev tool screen: edit a node's type, modifier and aspects, or delete it. Click an aspect to add 5 (shift: 25, ctrl: 1), right-click to
 * take the same away, or scroll over it.
 */
public class NodeEditorScreen extends Screen {
	private static final int COLS = 12;
	private static final int CELL = 20;
	private final BlockPos pos;
	private final List<Aspect> aspects = new ArrayList<>(Aspect.aspects.values());
	private final AspectList values = new AspectList();
	private int type;
	private int modifier = -1;
	private Button typeButton;
	private Button modifierButton;
	private int left;
	private int top;

	public NodeEditorScreen(BlockPos pos) {
		super(Component.literal("Node Editor"));
		this.pos = pos;
		if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockEntity(pos) instanceof NodeBlockEntity node) {
			this.type = node.getNodeType().ordinal();
			this.modifier = node.getNodeModifier() == null ? -1 : node.getNodeModifier().ordinal();
			AspectList al = node.getAspectsBase();
			for (Aspect a : al.getAspects()) {
				this.values.add(a, al.getAmount(a));
			}
		}
	}

	@Override
	protected void init() {
		this.left = (this.width - COLS * CELL) / 2;
		this.top = (this.height - 200) / 2;
		this.typeButton = this.addRenderableWidget(Button.builder(this.typeLabel(), b -> {
			this.type = (this.type + 1) % NodeType.values().length;
			b.setMessage(this.typeLabel());
		}).bounds(this.left, this.top + 16, 116, 20).build());
		this.modifierButton = this.addRenderableWidget(Button.builder(this.modifierLabel(), b -> {
			this.modifier = this.modifier + 1 >= NodeModifier.values().length ? -1 : this.modifier + 1;
			b.setMessage(this.modifierLabel());
		}).bounds(this.left + 124, this.top + 16, 116, 20).build());
		int by = this.top + 46 + 4 * (CELL + 8) + 6;
		this.addRenderableWidget(Button.builder(Component.literal("Apply"), b -> this.send(false)).bounds(this.left, by, 44, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Clear aspects"), b -> this.values.aspects.clear())
			.bounds(this.left + 47, by, 80, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Delete node"), b -> {
			this.send(true);
			this.onClose();
		}).bounds(this.left + 130, by, 70, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
			this.send(false);
			this.onClose();
		}).bounds(this.left + 203, by, 37, 20).build());
	}

	private Component typeLabel() {
		return Component.literal("Type: ").append(Component.translatable("nodetype." + NodeType.values()[this.type].name() + ".name"));
	}

	private Component modifierLabel() {
		return Component.literal("Modifier: ").append(this.modifier < 0 ? Component.literal("none")
			: Component.translatable("nodemod." + NodeModifier.values()[this.modifier].name() + ".name"));
	}

	private void send(boolean delete) {
		ClientPlayNetworking.send(new DevTools.NodeEdit(this.pos, this.type, this.modifier, this.values.copy(), delete));
	}

	private int cellAt(double mx, double my) {
		int gx = (int) Math.floor((mx - this.left) / CELL);
		int gy = (int) Math.floor((my - this.top - 46) / (CELL + 8));
		if (mx < this.left || my < this.top + 46 || gx >= COLS || gy < 0) {
			return -1;
		}
		int i = gy * COLS + gx;
		return i < this.aspects.size() ? i : -1;
	}

	private void change(int index, int delta) {
		Aspect a = this.aspects.get(index);
		int v = Math.max(0, Math.min(1000, this.values.getAmount(a) + delta));
		this.values.aspects.remove(a);
		if (v > 0) {
			this.values.add(a, v);
		}
	}

	private static int step() {
		Minecraft mc = Minecraft.getInstance();
		return mc.hasShiftDown() ? 25 : mc.hasControlDown() ? 1 : 5;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int i = this.cellAt(event.x(), event.y());
		if (i >= 0) {
			int b = event.button();
			if (b == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
				this.change(i, step());
				return true;
			}
			if (b == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT) {
				this.change(i, -step());
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double sx, double sy) {
		int i = this.cellAt(mx, my);
		if (i >= 0 && sy != 0.0) {
			this.change(i, sy > 0 ? 1 : -1);
			return true;
		}
		return super.mouseScrolled(mx, my, sx, sy);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
		g.fill(this.left - 6, this.top - 6, this.left + COLS * CELL + 6, this.top + 46 + 4 * (CELL + 8) + 32, 0xE0101018);
		g.text(this.font, Component.literal("Node Editor  " + this.pos.toShortString() + "   (aura " + this.total() + ")"), this.left, this.top, 0xFFE0C0FF,
			true);
		for (int i = 0; i < this.aspects.size(); i++) {
			Aspect a = this.aspects.get(i);
			int x = this.left + (i % COLS) * CELL + 2;
			int y = this.top + 46 + (i / COLS) * (CELL + 8);
			int amount = this.values.getAmount(a);
			GuiUtil.drawTag(g, x, y, a, 0.0F, 0, 771, amount > 0 ? 1.0F : 0.3F, false);
			if (amount > 0) {
				String s = Integer.toString(amount);
				g.text(this.font, s, x + 8 - this.font.width(s) / 2, y + 17, 0xFFFFFFFF, true);
			}
			if (mx >= x - 2 && mx < x + CELL - 2 && my >= y && my < y + CELL + 6) {
				g.setComponentTooltipForNextFrame(this.font, List.of(Component.literal(a.getName()), Component.literal(a.getTag()),
					Component.literal("Left +, right -, scroll ±1; shift x25, ctrl x1")), mx, my);
			}
		}
		super.extractRenderState(g, mx, my, pt);
	}

	private int total() {
		int t = 0;
		for (Aspect a : this.values.getAspects()) {
			t += this.values.getAmount(a);
		}
		return t;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
