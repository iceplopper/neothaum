package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.golem.GolemCoreType;
import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemUpgradeType;
import dev.neothaum.golem.menu.GolemMenu;
import dev.neothaum.network.TVNetwork;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GolemScreen extends AbstractContainerScreen<GolemMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/guigolem.png");

	public static final int[] MARKER_COLORS = {
		0xFFFFFF, 0xD87F33, 0xB24CD8, 0x6689D3, 0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
		0xABABAB, 0x287697, 0x7B2FBE, 0x253192, 0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B
	};

	public static final String[] MARKER_COLOR_NAMES = {
		"White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray",
		"Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"
	};

	private final GolemEntity golem;
	private final int threat;

	public GolemScreen(GolemMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 166);
		this.golem = menu.golem;
		if (this.golem != null && this.golem.isAdvanced() && this.golem.getRandom().nextInt(4) == 0) {
			this.threat = this.golem.getRandom().nextInt(9);
		} else {
			this.threat = -1;
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(g, mouseX, mouseY, partialTick);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;

		// Main GUI texture
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

		if (this.golem == null) return;

		int core = this.golem.getCore();
		boolean order = this.golem.getUpgradeCount(GolemUpgradeType.ORDER) > 0;
		int slots = this.golem.getInventorySize();
		int typeLoc = this.golem.getGolemType().ordinal() * 24;

		if (core >= 0 && GolemCoreType.byId(core).hasInventory) {
			for (int a = 0; a < Math.min(6, slots); a++) {
				int sx = x + 96 + (a / 2) * 28;
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, sx, y + 12 + (a % 2) * 31, 184, typeLoc, 24, 24, 256, 256);
				if (order) {
					g.blit(RenderPipelines.GUI_TEXTURED, TEX, sx, y + 4 + (a % 2) * 31, 72, 168, 24, 12, 256, 256);
					int color = this.golem.getFilterColor(a + this.menu.currentScroll * 6);
					if (color >= 0 && color < MARKER_COLORS.length) {
						g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 105 + (a / 2) * 28, y + 7 + (a % 2) * 31, 0, 176, 6, 6, 256, 256,
							0xFF000000 | MARKER_COLORS[color]);
					}
				}
			}
			if (slots > 6) {
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 111, y + 68, 0, this.menu.currentScroll > 0 ? 200 : 208, 24, 8, 256, 256);
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 135, y + 68, 24, this.menu.currentScroll < this.menu.maxScroll ? 200 : 208, 24, 8, 256, 256);
			}
		}
		// guard: what it may attack (an order upgrade lets it choose)
		if (core == 4 && order) {
			for (int i = 0; i < 4; i++) {
				this.box(g, x + 104, y + 5 + i * 16, this.golem.getTargetFlag(i));
			}
		}
		// fill: precise amount or any amount
		if (core == 0) {
			this.box(g, x + 62, y + 54, !this.golem.getToggle(0));
		}
		// use: block or empty space, right or left click, sneaking
		if (core == 8) {
			for (int i = 0; i < 3; i++) {
				this.box(g, x + 42, y + 40 + i * 10, !this.golem.getToggle(i));
			}
		}
		// entropy: how strictly items must match
		if (this.canSortGui()) {
			int sx = core == 10 ? 66 : 180;
			int sy = core == 10 ? 12 : 0;
			for (int i = 0; i < 3; i++) {
				this.box(g, x + sx, y + 24 + i * 10 + sy, this.golem.getSortFlag(i));
			}
		}
	}

	private void box(GuiGraphicsExtractor g, int bx, int by, boolean checked) {
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, bx, by, 8, 168, 8, 8, 256, 256);
		if (checked) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, bx, by, 8, 176, 8, 8, 256, 256);
		}
	}

	/** ItemGolemCore.canSort with an entropy upgrade: fill, empty, gather, use and sorting cores. */
	private boolean canSortGui() {
		int core = this.golem.getCore();
		return this.golem.getUpgradeCount(GolemUpgradeType.ENTROPY) > 0 && (core == 0 || core == 1 || core == 2 || core == 8 || core == 10);
	}

	private void halfText(GuiGraphicsExtractor g, String text, float tx, float ty, int color, boolean centred) {
		g.pose().pushMatrix();
		g.pose().translate(tx, ty);
		g.pose().scale(0.5F, 0.5F);
		g.text(this.font, Component.literal(text), centred ? -this.font.width(text) / 2 : 0, 0, 0xFF000000 | color, false);
		g.pose().popMatrix();
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		if (this.golem == null) return;

		// Copper Golem status indicator in title header
		if (this.golem.getGolemType() == dev.neothaum.golem.GolemType.COPPER) {
			String oxStr = switch (this.golem.getOxidationLevel()) {
				case 0 -> "Unaffected";
				case 1 -> "Exposed";
				case 2 -> "Weathered";
				default -> "Oxidized";
			};
			int oxColor = switch (this.golem.getOxidationLevel()) {
				case 0 -> 0xDE7744;
				case 1 -> 0xA87755;
				case 2 -> 0x669988;
				default -> 0x44AA88;
			};
			String waxStr = this.golem.isPermanentlyWaxed() ? "Tallow Waxed" : (this.golem.isWaxed() ? "Waxed" : "Unwaxed");
			String fullStatus = oxStr + " • " + waxStr + (this.golem.isCharged() ? " ⚡" : "");
			g.text(this.font, Component.literal(fullStatus), 55, 6, oxColor, false);
		}

		int core = this.golem.getCore();
		// TC4 writes the blurb (or an advanced golem's occasional threat) at half size
		g.pose().pushMatrix();
		g.pose().scale(0.5F, 0.5F);
		Component blurb = this.threat >= 0 ? Component.translatable("golemthreat." + this.threat + ".text")
			: Component.translatable("golemblurb." + core + ".text");
		g.textWithWordWrap(this.font, blurb, 80, 22, 110, 0xFFDDDDDD);
		if (this.menu.maxScroll > 0) {
			g.text(this.font, Component.literal((this.menu.currentScroll + 1) + "/" + (this.menu.maxScroll + 1)), 323, 140, 0xFFDDDDDD, false);
		}
		g.pose().popMatrix();
		if (core == 4 && this.golem.getUpgradeCount(GolemUpgradeType.ORDER) > 0) {
			g.text(this.font, Component.literal("Monsters"), 122, 6, 0xFFFFCCCC, false);
			g.text(this.font, Component.literal("Animals"), 122, 22, 0xFFFFFFCC, false);
			g.text(this.font, Component.literal("Players"), 122, 38, 0xFFCCCCFF, false);
			g.text(this.font, Component.literal("Creepers"), 122, 54, 0xFFCCFFCC, false);
		}
		if (core == 0) {
			this.halfText(g, this.golem.getToggle(0) ? "Any amount" : "Precise amount", 66, 48, 16645629, true);
		}
		if (core == 8) {
			this.halfText(g, this.golem.getToggle(0) ? "Empty space" : "Block", 53, 42, 16645629, false);
			this.halfText(g, this.golem.getToggle(1) ? "Left click" : "Right click", 53, 52, 16645629, false);
			this.halfText(g, this.golem.getToggle(2) ? "Sneaking" : "Not sneaking", 53, 62, 16645629, false);
		}
		if (this.canSortGui()) {
			int sx = core == 10 ? 66 : 180;
			int sy = core == 10 ? 12 : 0;
			this.halfText(g, "Use Ore dictionary", sx + 10, 26 + sy, this.golem.checkOreDict() ? 16645629 : 6710886, false);
			this.halfText(g, "Ignore item damage", sx + 10, 36 + sy, this.golem.ignoreDamage() ? 16645629 : 6710886, false);
			this.halfText(g, "Ignore NBT values", sx + 10, 46 + sy, this.golem.ignoreNBT() ? 16645629 : 6710886, false);
		}
		// the colour under the mouse, over the slots
		if (core >= 0 && GolemCoreType.byId(core).hasInventory && this.golem.getUpgradeCount(GolemUpgradeType.ORDER) > 0) {
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			for (int a = 0; a < Math.min(6, this.golem.getInventorySize()); a++) {
				int mx = mouseX - (x + 96 + a / 2 * 28);
				int my = mouseY - (y + 4 + a % 2 * 31);
				if (mx >= 0 && my >= 0 && mx < 24 && my < 12) {
					int c = this.golem.getFilterColor(a + this.menu.currentScroll * 6);
					String text = c >= 0 ? MARKER_COLOR_NAMES[c] : "Any color";
					g.text(this.font, Component.literal(text), 133 - this.font.width(text) / 2, -6, 0xFFFDFDFD, false);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (this.golem == null) return super.mouseClicked(event, doubleClick);

		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		double mx = event.x();
		double my = event.y();

		int core = this.golem.getCore();
		int slots = this.golem.getInventorySize();
		if (core >= 0 && GolemCoreType.byId(core).hasInventory) {
			if (this.golem.getUpgradeCount(GolemUpgradeType.ORDER) > 0) {
				for (int a = 0; a < Math.min(6, slots); a++) {
					int slot = a + this.menu.currentScroll * 6;
					double ty = my - (y + 4 + a % 2 * 31);
					double lx = mx - (x + 96 + a / 2 * 28);
					if (ty >= 0 && ty < 12 && lx >= 0 && lx < 8) {
						return this.press(4, slot);
					}
					if (ty >= 0 && ty < 12 && lx >= 16 && lx < 24) {
						return this.press(0, slot);
					}
				}
			}
			if (slots > 6 && my >= y + 68 && my < y + 76) {
				if (mx >= x + 111 && mx < x + 135 && this.menu.currentScroll > 0) {
					this.menu.setScroll(this.menu.currentScroll - 1);
					return true;
				}
				if (mx >= x + 135 && mx < x + 159 && this.menu.currentScroll < this.menu.maxScroll) {
					this.menu.setScroll(this.menu.currentScroll + 1);
					return true;
				}
			}
		}
		if (core == 4) {
			for (int i = 0; i < 4; i++) {
				if (this.in(mx, my, x + 104, y + 5 + 16 * i, 8, 8)) {
					return this.press(1, i + 1);
				}
			}
		}
		if (core == 0 && this.in(mx, my, x + 62, y + 54, 8, 8)) {
			return this.press(1, 0);
		}
		if (core == 8) {
			for (int i = 0; i < 3; i++) {
				if (this.in(mx, my, x + 42, y + 40 + i * 10, 8, 8)) {
					return this.press(1, i);
				}
			}
		}
		if (this.canSortGui()) {
			int sx = core == 10 ? 66 : 180;
			int sy = core == 10 ? 12 : 0;
			for (int i = 0; i < 3; i++) {
				if (this.in(mx, my, x + sx, y + 24 + i * 10 + sy, 64, 8)) {
					return this.press(1, 5 + i);
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private boolean in(double mx, double my, int bx, int by, int w, int h) {
		return mx >= bx && my >= by && mx < bx + w && my < by + h;
	}

	/** ContainerGolem.enchantItem: send a button to the golem, with a click. */
	private boolean press(int button, int value) {
		this.golem.handleButton(button, value);
		TVNetwork.clientSender.accept(new TVNetwork.GolemButton(this.golem.getId(), button, value));
		this.golem.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.2F, 0.8F);
		return true;
	}
}
