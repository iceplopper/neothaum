package dev.thaumvibe.client.gui;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.client.mixin.ContainerScreenAccessor;
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
 * Baubles' GuiBaublesButton: the small button at the top right of the player portrait that swaps between the normal inventory and the
 * bauble slots. It follows the screen's left edge, which moves when the recipe book opens.
 */
public class BaublesButton extends AbstractButton {
	private static final Identifier ICON = Thaumvibe.id("textures/items/bauble_amulet.png");
	private final AbstractContainerScreen<?> screen;
	private final int offsetX;
	private final int offsetY;
	private final BooleanSupplier shown;
	private final Runnable action;

	public BaublesButton(AbstractContainerScreen<?> screen, int offsetX, int offsetY, Component tooltip, BooleanSupplier shown, Runnable action) {
		super(0, 0, 10, 10, tooltip);
		this.screen = screen;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.shown = shown;
		this.action = action;
		this.setTooltip(Tooltip.create(tooltip));
		this.follow();
	}

	private void follow() {
		ContainerScreenAccessor acc = (ContainerScreenAccessor) this.screen;
		this.setX(acc.thaumvibe$leftPos() + this.offsetX);
		this.setY(acc.thaumvibe$topPos() + this.offsetY);
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
		int edge = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF373737;
		g.fill(x, y, x + 10, y + 10, edge);
		g.fill(x + 1, y + 1, x + 9, y + 9, this.isHoveredOrFocused() ? 0xFFA8A8C8 : 0xFF8B8B8B);
		g.blit(RenderPipelines.GUI_TEXTURED, ICON, x + 1, y + 1, 0.0F, 0.0F, 8, 8, 16, 16, 16, 16);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput out) {
		this.defaultButtonNarrationText(out);
	}
}
