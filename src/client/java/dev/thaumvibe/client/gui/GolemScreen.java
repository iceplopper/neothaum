package dev.thaumvibe.client.gui;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.golem.GolemCoreType;
import dev.thaumvibe.golem.GolemEntity;
import dev.thaumvibe.golem.GolemUpgradeType;
import dev.thaumvibe.golem.menu.GolemMenu;
import dev.thaumvibe.network.TVNetwork;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GolemScreen extends AbstractContainerScreen<GolemMenu> {
	private static final Identifier TEX = Thaumvibe.id("textures/gui/guigolem.png");

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
		int orderUpgrades = this.golem.getUpgradeCount(GolemUpgradeType.ORDER);
		int entropyUpgrades = this.golem.getUpgradeCount(GolemUpgradeType.ENTROPY);
		int typeLoc = this.golem.getGolemType().ordinal() * 24;

		// Render filter slots if core has inventory
		if (core >= 0 && GolemCoreType.byId(core).hasInventory) {
			for (int a = 0; a < 6; a++) {
				int sx = x + 96 + (a / 2) * 28;
				int sy = y + 12 + (a % 2) * 31;
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, sx, sy, 184, typeLoc, 24, 24, 256, 256);

				// Order upgrade marker tab
				if (orderUpgrades > 0) {
					g.blit(RenderPipelines.GUI_TEXTURED, TEX, sx, sy - 8, 72, 168, 24, 12, 256, 256);
					int slotIndex = a + this.menu.currentScroll * 6;
					int color = this.golem.getFilterColor(slotIndex);
					if (color >= 0 && color < MARKER_COLORS.length) {
						int argb = 0xFF000000 | MARKER_COLORS[color];
						g.blit(RenderPipelines.GUI_TEXTURED, TEX, sx + 9, sy - 5, 0, 176, 6, 6, 256, 256, argb);
					}
				}
			}

			// Scroll buttons
			if (this.menu.maxScroll > 0) {
				int uLeft = this.menu.currentScroll > 0 ? 0 : 0;
				int vLeft = this.menu.currentScroll > 0 ? 200 : 208;
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 111, y + 68, uLeft, vLeft, 24, 8, 256, 256);

				int uRight = this.menu.currentScroll < this.menu.maxScroll ? 24 : 24;
				int vRight = this.menu.currentScroll < this.menu.maxScroll ? 200 : 208;
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 135, y + 68, uRight, vRight, 24, 8, 256, 256);
			}
		}

		// Guard Core checkboxes (Core 4)
		if (core == 4) {
			for (int i = 0; i < 4; i++) {
				int cy = y + 8 + i * 16;
				boolean checked = this.golem.getTargetFlag(i);
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 104, cy, 8, checked ? 176 : 168, 8, 8, 256, 256);
			}
		}

		// Fill Core toggle (Core 0)
		if (core == 0) {
			boolean anyAmount = this.golem.getToggle(0);
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 55, y + 54, 8, anyAmount ? 176 : 168, 8, 8, 256, 256);
		}

		// Use Core toggles (Core 8)
		if (core == 8) {
			for (int i = 0; i < 3; i++) {
				boolean val = this.golem.getToggle(i);
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 10, y + 42 + i * 12, 8, val ? 176 : 168, 8, 8, 256, 256);
			}
		}

		// Sorting options (Core 10 or Sortable with Entropy upgrade)
		if (core == 10 || (entropyUpgrades > 0 && GolemCoreType.byId(core).hasInventory)) {
			int bx = (core == 10) ? x + 10 : x + 70;
			for (int i = 0; i < 3; i++) {
				boolean checked = this.golem.getSortFlag(i);
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, bx, y + 42 + i * 10, 8, checked ? 176 : 168, 8, 8, 256, 256);
			}
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		if (this.golem == null) return;

		// Copper Golem status indicator in title header
		if (this.golem.getGolemType() == dev.thaumvibe.golem.GolemType.COPPER) {
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

		// Blurb or Threat message on left side
		if (this.threat >= 0) {
			g.textWithWordWrap(this.font, Component.translatable("golemthreat." + this.threat + ".text"), 10, 14, 80, 0xDDDDDD);
		} else if (core >= 0) {
			g.textWithWordWrap(this.font, Component.translatable("golemblurb." + core + ".text"), 10, 14, 80, 0xDDDDDD);
		}

		// Page number text
		if (this.menu.maxScroll > 0) {
			String pageStr = (this.menu.currentScroll + 1) + "/" + (this.menu.maxScroll + 1);
			g.text(this.font, Component.literal(pageStr), 125, 74, 0xDDDDDD, false);
		}

		// Guard labels
		if (core == 4) {
			g.text(this.font, Component.literal("Monsters"), 116, 8, 0xFF5555, false);
			g.text(this.font, Component.literal("Animals"), 116, 24, 0x55FF55, false);
			g.text(this.font, Component.literal("Players"), 116, 40, 0x55FFFF, false);
			g.text(this.font, Component.literal("Creepers"), 116, 56, 0xFFFF55, false);
		}

		// Fill label
		if (core == 0) {
			String fillText = this.golem.getToggle(0) ? "Any amount" : "Precise amount";
			g.text(this.font, Component.literal(fillText), 30, 66, 0xFECE5D, false);
		}

		// Use labels
		if (core == 8) {
			g.text(this.font, Component.literal(this.golem.getToggle(0) ? "Empty space" : "Block"), 22, 42, 0xFECE5D, false);
			g.text(this.font, Component.literal(this.golem.getToggle(1) ? "Left click" : "Right click"), 22, 54, 0xFECE5D, false);
			g.text(this.font, Component.literal(this.golem.getToggle(2) ? "Sneaking" : "Not sneaking"), 22, 66, 0xFECE5D, false);
		}

		// Sorting labels
		int entropyUpgrades = this.golem.getUpgradeCount(GolemUpgradeType.ENTROPY);
		if (core == 10 || (entropyUpgrades > 0 && GolemCoreType.byId(core).hasInventory)) {
			int bx = (core == 10) ? 22 : 82;
			g.text(this.font, Component.literal("Tags/Ore"), bx, 42, 0xDDDDDD, false);
			g.text(this.font, Component.literal("Ignore damage"), bx, 52, 0xDDDDDD, false);
			g.text(this.font, Component.literal("Ignore NBT"), bx, 62, 0xDDDDDD, false);
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
		int orderUpgrades = this.golem.getUpgradeCount(GolemUpgradeType.ORDER);

		// Click Order upgrade marker tab above slot
		if (core >= 0 && GolemCoreType.byId(core).hasInventory && orderUpgrades > 0) {
			for (int a = 0; a < 6; a++) {
				int sx = x + 96 + (a / 2) * 28;
				int sy = y + 12 + (a % 2) * 31;
				if (mx >= sx && mx < sx + 24 && my >= sy - 8 && my < sy + 4) {
					int slotIndex = a + this.menu.currentScroll * 6;
					this.golem.handleButton(0, slotIndex);
					TVNetwork.clientSender.accept(new TVNetwork.GolemButton(this.golem.getId(), 0, slotIndex));
					return true;
				}
			}
		}

		// Click scroll buttons
		if (this.menu.maxScroll > 0) {
			if (mx >= x + 111 && mx < x + 135 && my >= y + 68 && my < y + 76) {
				if (this.menu.currentScroll > 0) {
					this.menu.setScroll(this.menu.currentScroll - 1);
					return true;
				}
			}
			if (mx >= x + 135 && mx < x + 159 && my >= y + 68 && my < y + 76) {
				if (this.menu.currentScroll < this.menu.maxScroll) {
					this.menu.setScroll(this.menu.currentScroll + 1);
					return true;
				}
			}
		}

		// Click Guard Core checkboxes
		if (core == 4) {
			for (int i = 0; i < 4; i++) {
				int cy = y + 8 + i * 16;
				if (mx >= x + 104 && mx < x + 120 && my >= cy && my < cy + 12) {
					this.golem.handleButton(2, i);
					TVNetwork.clientSender.accept(new TVNetwork.GolemButton(this.golem.getId(), 2, i));
					return true;
				}
			}
		}

		// Click Fill Core toggle
		if (core == 0) {
			if (mx >= x + 55 && mx < x + 70 && my >= y + 54 && my < y + 66) {
				this.golem.handleButton(1, 0);
				TVNetwork.clientSender.accept(new TVNetwork.GolemButton(this.golem.getId(), 1, 0));
				return true;
			}
		}

		// Click Use Core toggles
		if (core == 8) {
			for (int i = 0; i < 3; i++) {
				int cy = y + 42 + i * 12;
				if (mx >= x + 10 && mx < x + 25 && my >= cy && my < cy + 10) {
					this.golem.handleButton(1, i);
					TVNetwork.clientSender.accept(new TVNetwork.GolemButton(this.golem.getId(), 1, i));
					return true;
				}
			}
		}

		// Click Sorting options
		int entropyUpgrades = this.golem.getUpgradeCount(GolemUpgradeType.ENTROPY);
		if (core == 10 || (entropyUpgrades > 0 && GolemCoreType.byId(core).hasInventory)) {
			int bx = (core == 10) ? x + 10 : x + 70;
			for (int i = 0; i < 3; i++) {
				int cy = y + 42 + i * 10;
				if (mx >= bx && mx < bx + 15 && my >= cy && my < cy + 10) {
					this.golem.handleButton(3, i);
					TVNetwork.clientSender.accept(new TVNetwork.GolemButton(this.golem.getId(), 3, i));
					return true;
				}
			}
		}

		return super.mouseClicked(event, doubleClick);
	}
}
