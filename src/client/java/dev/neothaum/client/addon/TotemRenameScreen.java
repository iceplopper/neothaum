package dev.neothaum.client.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.addon.totems.TravellersTotems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Naming a totem (sneak and use one you know): a carved slab with the name cut into a tablet. Rename it, or give it back the Latin name
 * its surroundings give it.
 */
public class TotemRenameScreen extends Screen {
	private static final Identifier SLAB = NeoThaum.id("textures/gui/gui_totem.png");
	private final GlobalPos pos;
	private final String current;
	private EditBox box;

	public TotemRenameScreen(GlobalPos pos, String current) {
		super(Component.translatable("neothaum.totem.rename.title"));
		this.pos = pos;
		this.current = current;
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(TravellersTotems.OpenRename.TYPE, (p, ctx) -> ctx.client().execute(
			() -> ctx.client().gui.setScreen(new TotemRenameScreen(p.pos(), p.name()))));
	}

	private int slabX() {
		return this.width / 2 - 100;
	}

	private int slabY() {
		return this.height / 2 - 50;
	}

	@Override
	protected void init() {
		this.box = new EditBox(this.font, this.slabX() + 16, this.slabY() + 28, 168, 16, this.title);
		this.box.setMaxLength(TravellersTotems.NAME_LENGTH);
		this.box.setValue(this.current);
		this.box.setBordered(false);
		this.box.setTextColor(0xFFEDE4F7);
		this.addRenderableWidget(this.box);
		this.setInitialFocus(this.box);
		int y = this.slabY() + 78;
		this.addRenderableWidget(Button.builder(Component.translatable("neothaum.totem.rename.done"), b -> this.done(false))
			.bounds(this.width / 2 - 100, y, 48, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("neothaum.totem.rename.restore"), b -> this.done(true))
			.bounds(this.width / 2 - 50, y, 74, 20).build());
		// another name from the same surroundings; the screen comes back with it, to keep or draw again
		this.addRenderableWidget(Button.builder(Component.translatable("neothaum.totem.rename.reroll"), b -> this.reroll())
			.bounds(this.width / 2 + 26, y, 74, 20).build());
	}

	/** Sends the name (or asks for the totem's own), and closes. */
	public void done(boolean restore) {
		String name = this.box.getValue().strip();
		if (restore || !name.isEmpty() && !name.equals(this.current)) {
			ClientPlayNetworking.send(new TravellersTotems.Rename(this.pos, name, restore, false));
		}
		this.onClose();
	}

	/** Asks for another name from the totem's surroundings; the server renames it and opens this screen again on the new name. */
	public void reroll() {
		ClientPlayNetworking.send(new TravellersTotems.Rename(this.pos, "", false, true));
	}

	/** The name being typed, for tests. */
	public void type(String name) {
		this.box.setValue(name);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		// Enter names it
		if (event.isConfirmation()) {
			this.done(false);
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		g.blit(RenderPipelines.GUI_TEXTURED, SLAB, this.slabX(), this.slabY(), 0, 184, 200, 72, 256, 256);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractRenderState(g, mouseX, mouseY, a);
		g.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, this.slabY() + 10, 0xFFA996C9, false);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
