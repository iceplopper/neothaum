package dev.neothaum.client.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.addon.totems.TravellersTotems;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The list of totems to travel to, on a page of TC4's Thaumonomicon: each known totem by name, with how far it is (or the world it is in),
 * and the Home Totem last. Eight to a page.
 */
public class TotemTravelScreen extends Screen {
	private static final Identifier PAGE = NeoThaum.id("textures/gui/gui_researchbook.png");
	private static final int PER_PAGE = 8;
	private final Optional<GlobalPos> from;
	private final List<TravellersTotems.Destination> destinations;
	private int page;

	public TotemTravelScreen(Optional<GlobalPos> from, List<TravellersTotems.Destination> destinations) {
		super(Component.translatable(from.isPresent() ? "neothaum.totem.screen.title" : "neothaum.compass.screen.title"));
		this.from = from;
		this.destinations = destinations;
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(TravellersTotems.OpenTravel.TYPE,
			(p, ctx) -> ctx.client().execute(() -> ctx.client().gui.setScreen(new TotemTravelScreen(p.from(), p.destinations()))));
	}

	@Override
	protected void init() {
		int x = this.width / 2 - 100;
		int y = this.height / 2 - 70;
		int start = this.page * PER_PAGE;
		for (int i = start; i < Math.min(this.destinations.size(), start + PER_PAGE); i++) {
			TravellersTotems.Destination d = this.destinations.get(i);
			this.addRenderableWidget(Button.builder(this.label(d), b -> this.travel(d)).bounds(x, y + (i - start) * 22, 200, 20).build());
		}
		if (this.destinations.size() > PER_PAGE) {
			int pages = (this.destinations.size() + PER_PAGE - 1) / PER_PAGE;
			this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.turn(-1)).bounds(x, y + PER_PAGE * 22 + 4, 20, 20).build())
				.active = this.page > 0;
			this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.turn(1)).bounds(x + 180, y + PER_PAGE * 22 + 4, 20, 20).build())
				.active = this.page < pages - 1;
		}
	}

	private Component label(TravellersTotems.Destination d) {
		Minecraft mc = Minecraft.getInstance();
		String where;
		if (d.home()) {
			where = Component.translatable("neothaum.totem.screen.home").getString();
		} else if (mc.player != null && d.pos().dimension() == mc.player.level().dimension()) {
			where = (int) Math.sqrt(d.pos().pos().distToCenterSqr(mc.player.position())) + "m";
		} else {
			where = d.pos().dimension().identifier().getPath().replace('_', ' ');
		}
		return Component.literal(d.name()).append(Component.literal("  " + where).withStyle(ChatFormatting.GRAY));
	}

	private void turn(int by) {
		this.page += by;
		this.rebuildWidgets();
	}

	private void travel(TravellersTotems.Destination d) {
		ClientPlayNetworking.send(new TravellersTotems.TravelTo(d.pos(), this.from.isEmpty()));
		this.onClose();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		// the open Thaumonomicon behind the list (its 512-pixel sheet, drawn at half size)
		g.blit(RenderPipelines.GUI_TEXTURED, PAGE, this.width / 2 - 127, this.height / 2 - 95, 0, 0, 254, 181, 256, 256);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractRenderState(g, mouseX, mouseY, a);
		g.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, this.height / 2 - 86, 0xFF3A2A1A, false);
		if (this.destinations.isEmpty()) {
			Component none = Component.translatable("neothaum.totem.screen.none");
			g.text(this.font, none, this.width / 2 - this.font.width(none) / 2, this.height / 2 - 10, 0xFF5A4A3A, false);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
