package dev.neothaum.client.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.addon.totems.TravellersTotems;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

/**
 * Travelling by totem, on a carved slab of arcane stone. The left tablet names the totem you stand at (or the compass) and lists where you
 * can go, nearest first and home last, each name kept to its tablet; the right tablet shows the one you pick, its name in full and how far
 * it is, with the button to go there. A glowing violet channel runs between them.
 */
public class TotemTravelScreen extends Screen {
	private static final Identifier SLAB = NeoThaum.id("textures/gui/gui_totem.png");
	private static final int PER_PAGE = 7;
	private static final int ROW = 13;
	/** Pale lettering on the dark stone, and a fainter violet for what goes with it. */
	private static final int INK = 0xFFEDE4F7;
	private static final int FADED = 0xFFA996C9;
	private static final int HOME = 0xFFF2C878;
	private static final int TEXT_W = 104;
	private final Optional<GlobalPos> from;
	private final String fromName;
	private final List<TravellersTotems.Destination> destinations;
	private int page;
	private int selected = -1;
	private Button travel;

	public TotemTravelScreen(Optional<GlobalPos> from, String fromName, List<TravellersTotems.Destination> destinations) {
		super(Component.translatable(from.isPresent() ? "neothaum.totem.screen.title" : "neothaum.compass.screen.title"));
		this.from = from;
		this.fromName = fromName;
		this.destinations = destinations;
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(TravellersTotems.OpenTravel.TYPE, (p, ctx) -> ctx.client().execute(
			() -> ctx.client().gui.setScreen(new TotemTravelScreen(p.from(), p.fromName(), p.destinations()))));
	}

	private int slabX() {
		return this.width / 2 - 128;
	}

	private int slabY() {
		return this.height / 2 - 90;
	}

	/** The left tablet's text column. */
	private int leftX() {
		return this.slabX() + 14;
	}

	/** The right tablet's text column. */
	private int rightX() {
		return this.slabX() + 138;
	}

	private int top() {
		return this.slabY() + 15;
	}

	@Override
	protected void init() {
		this.travel = this.addRenderableWidget(Button.builder(Component.translatable("neothaum.totem.screen.travel"), b -> this.travel())
			.bounds(this.rightX() + 12, this.top() + 128, 80, 20).build());
		this.travel.active = this.selected >= 0;
		int pages = this.pages();
		Button back = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.turn(-1))
			.bounds(this.leftX(), this.top() + 134, 16, 16).build());
		Button forward = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.turn(1))
			.bounds(this.leftX() + TEXT_W - 16, this.top() + 134, 16, 16).build());
		back.visible = forward.visible = pages > 1;
		back.active = this.page > 0;
		forward.active = this.page < pages - 1;
	}

	private int pages() {
		return Math.max(1, (this.destinations.size() + PER_PAGE - 1) / PER_PAGE);
	}

	private void turn(int by) {
		this.page = Math.max(0, Math.min(this.pages() - 1, this.page + by));
		this.rebuildWidgets();
	}

	/** The index of the list row under the mouse, or -1. */
	private int rowAt(double mx, double my) {
		int y0 = this.top() + 38;
		if (mx < this.leftX() - 2 || mx > this.leftX() + TEXT_W + 2 || my < y0) {
			return -1;
		}
		int row = (int) ((my - y0) / ROW);
		int i = this.page * PER_PAGE + row;
		return row < PER_PAGE && i < this.destinations.size() ? i : -1;
	}

	/** Picks a totem from the list (for tests, and for the mouse). */
	public void select(int index) {
		this.selected = index;
		this.travel.active = index >= 0;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int i = this.rowAt(event.x(), event.y());
		if (i >= 0) {
			this.select(i);
			// a second click on the same totem goes there
			if (doubleClick) {
				this.travel();
			}
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	private void travel() {
		if (this.selected < 0 || this.selected >= this.destinations.size()) {
			return;
		}
		ClientPlayNetworking.send(new TravellersTotems.TravelTo(this.destinations.get(this.selected).pos(), this.from.isEmpty()));
		this.onClose();
	}

	private String where(TravellersTotems.Destination d) {
		Minecraft mc = Minecraft.getInstance();
		if (d.home()) {
			return Component.translatable("neothaum.totem.screen.home").getString();
		}
		if (mc.player != null && d.pos().dimension() == mc.player.level().dimension()) {
			return (int) Math.sqrt(d.pos().pos().distToCenterSqr(mc.player.position())) + "m";
		}
		return d.pos().dimension().identifier().getPath().replace('_', ' ');
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		g.blit(RenderPipelines.GUI_TEXTURED, SLAB, this.slabX(), this.slabY(), 0, 0, 256, 180, 256, 256);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractRenderState(g, mouseX, mouseY, a);
		int x = this.leftX();
		int y = this.top();
		// the left page: where you stand, and where you can go
		this.centred(g, this.title, x, y, INK);
		Component here = this.from.isPresent() ? Component.translatable("neothaum.totem.screen.here", this.fromName)
			: Component.translatable("neothaum.compass.screen.here");
		int hy = y + 12;
		for (FormattedCharSequence line : this.font.split(here, TEXT_W)) {
			g.text(this.font, line, x + (TEXT_W - this.font.width(line)) / 2, hy, FADED, false);
			hy += 10;
		}
		if (this.destinations.isEmpty()) {
			this.centred(g, Component.translatable("neothaum.totem.screen.none"), x, y + 60, FADED);
		}
		int hover = this.rowAt(mouseX, mouseY);
		int start = this.page * PER_PAGE;
		for (int i = start; i < Math.min(this.destinations.size(), start + PER_PAGE); i++) {
			TravellersTotems.Destination d = this.destinations.get(i);
			int ry = y + 38 + (i - start) * ROW;
			if (i == this.selected || i == hover) {
				g.fill(RenderPipelines.GUI, x - 2, ry - 2, x + TEXT_W + 2, ry + ROW - 3, i == this.selected ? 0x609A6AE0 : 0x309A6AE0);
			}
			// the name has the line to itself; how far it is shows on the right page
			g.text(this.font, Component.literal(this.fit(d.name(), TEXT_W)), x, ry, d.home() ? HOME : INK, false);
		}
		if (this.pages() > 1) {
			this.centred(g, Component.literal((this.page + 1) + " / " + this.pages()), x, y + 138, FADED);
		}
		// the right page: the totem picked
		int rx = this.rightX();
		if (this.selected >= 0 && this.selected < this.destinations.size()) {
			TravellersTotems.Destination d = this.destinations.get(this.selected);
			int ly = y + 20;
			for (FormattedCharSequence line : this.font.split(Component.literal(d.name()), TEXT_W)) {
				g.text(this.font, line, rx + (TEXT_W - this.font.width(line)) / 2, ly, INK, false);
				ly += 11;
			}
			ly += 6;
			Component info = d.home() ? Component.translatable("neothaum.totem.screen.your_home")
				: Component.translatable("neothaum.totem.screen.away", this.where(d));
			for (FormattedCharSequence line : this.font.split(info, TEXT_W)) {
				g.text(this.font, line, rx + (TEXT_W - this.font.width(line)) / 2, ly, FADED, false);
				ly += 11;
			}
		} else if (!this.destinations.isEmpty()) {
			int ly = y + 60;
			for (FormattedCharSequence line : this.font.split(Component.translatable("neothaum.totem.screen.pick"), TEXT_W)) {
				g.text(this.font, line, rx + (TEXT_W - this.font.width(line)) / 2, ly, FADED, false);
				ly += 11;
			}
		}
	}

	private void centred(GuiGraphicsExtractor g, Component c, int x, int y, int colour) {
		g.text(this.font, c, x + (TEXT_W - this.font.width(c)) / 2, y, colour, false);
	}

	/** The text, shortened with an ellipsis to fit the width. */
	private String fit(String s, int width) {
		if (this.font.width(s) <= width) {
			return s;
		}
		return this.font.plainSubstrByWidth(s, width - this.font.width("...")) + "...";
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
