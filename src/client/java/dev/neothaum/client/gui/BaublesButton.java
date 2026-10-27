package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.client.mixin.ContainerScreenAccessor;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Baubles' GuiBaublesButton: small beveled button at the top-right of the player portrait that swaps between
 * the normal inventory and the bauble slots. It follows the screen's left edge, which moves when the recipe book opens.
 */
public class BaublesButton extends AbstractButton {
	public static final Identifier BAUBLES_ICON = NeoThaum.id("textures/items/bauble_amulet.png");
	public static final Identifier INVENTORY_ICON = NeoThaum.id("textures/gui/icon_inventory.png");

	private final AbstractContainerScreen<?> screen;
	private final int offsetX;
	private final int offsetY;
	private final Identifier icon;
	private final BooleanSupplier shown;
	private final Runnable action;

	public BaublesButton(AbstractContainerScreen<?> screen, int offsetX, int offsetY, Component tooltip, BooleanSupplier shown, Runnable action) {
		this(screen, offsetX, offsetY, BAUBLES_ICON, tooltip, shown, action);
	}

	public BaublesButton(AbstractContainerScreen<?> screen, int offsetX, int offsetY, Identifier icon, Component tooltip, BooleanSupplier shown, Runnable action) {
		super(0, 0, 10, 10, tooltip);
		this.screen = screen;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.icon = icon != null ? icon : BAUBLES_ICON;
		this.shown = shown;
		this.action = action;
		this.setTooltip(Tooltip.create(tooltip));
		this.follow();
	}

	private void follow() {
		ContainerScreenAccessor acc = (ContainerScreenAccessor) this.screen;
		this.setX(acc.neothaum$leftPos() + this.offsetX);
		this.setY(acc.neothaum$topPos() + this.offsetY);
		this.visible = this.shown.getAsBoolean();
	}

	@Override
	public void onPress(InputWithModifiers input) {
		this.action.run();
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		this.follow();
		if (!this.visible) {
			return;
		}
		int x = this.getX();
		int y = this.getY();
		boolean hovered = this.isHoveredOrFocused();

		// Clean vanilla beveled button styling matching Minecraft's inventory interface
		int topLight = hovered ? 0xFFFFFFFF : 0xFFE0E0E0;
		int botDark = hovered ? 0xFF555555 : 0xFF373737;
		int bg = hovered ? 0xFFA0A0C8 : 0xFF8B8B8B;

		g.fill(x, y, x + 10, y + 10, bg);
		g.fill(x, y, x + 10, y + 1, topLight);
		g.fill(x, y, x + 1, y + 10, topLight);
		g.fill(x, y + 9, x + 10, y + 10, botDark);
		g.fill(x + 9, y, x + 10, y + 10, botDark);

		// Render the full 16x16 icon cleanly scaled into the 8x8 interior without clipping
		GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, this.icon, x + 1, y + 1, 8, 8, hovered ? 0xFFFFFFFF : 0xFFE0E0E0);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput out) {
		this.defaultButtonNarrationText(out);
	}
}
