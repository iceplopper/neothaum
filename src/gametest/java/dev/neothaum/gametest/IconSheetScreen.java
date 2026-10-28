package dev.neothaum.gametest;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Draws one page of item icons in a grid on a plain background, so every inventory icon can be checked from a screenshot. */
public class IconSheetScreen extends Screen {
	public static final int COLS = 20;
	public static final int ROWS = 10;
	private final List<ItemStack> stacks;
	private final int page;

	public IconSheetScreen(List<ItemStack> stacks, int page) {
		super(Component.literal("icons"));
		this.stacks = stacks;
		this.page = page;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
		g.fill(0, 0, this.width, this.height, 0xFF8B8B8B);
		int x0 = (this.width - COLS * 20) / 2;
		int y0 = 4;
		for (int i = 0; i < COLS * ROWS; i++) {
			int idx = this.page * COLS * ROWS + i;
			if (idx >= this.stacks.size()) {
				break;
			}
			int x = x0 + (i % COLS) * 20;
			int y = y0 + (i / COLS) * 20;
			g.fill(x, y, x + 18, y + 18, 0xFF6B6B6B);
			g.item(this.stacks.get(idx), x + 1, y + 1);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
