package dev.neothaum.client.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.addon.totems.TravellersTotems;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.ImageButton;
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
	private static final int ROW = 13;
	/** Where the list must end, above the page number and the page buttons. */
	private static final int LIST_BOTTOM = 132;
	/** Space between the heading (however many lines it wraps to) and the list. */
	private static final int GAP = 10;
	/** Pale lettering on the dark stone, and a fainter violet for what goes with it. */
	private static final int INK = 0xFFEDE4F7;
	private static final int FADED = 0xFFA996C9;
	private static final int HOME = 0xFFF2C878;
	private static final int TEXT_W = 104;
	/** The home button, carved in the slab's stone with a house glowing in it; unlit and grey when there is no Home Totem to go to. */
	private static final WidgetSprites HOME_SPRITES = new WidgetSprites(NeoThaum.id("totem/home"), NeoThaum.id("totem/home_disabled"),
		NeoThaum.id("totem/home_highlighted"));
	/** Marks beside a totem in another world: a nether portal, an eye of ender. */
	private static final Identifier NETHER_MARK = NeoThaum.id("totem/nether");
	private static final Identifier END_MARK = NeoThaum.id("totem/end");
	private static final int MARK = 9;
	private final Optional<GlobalPos> from;
	private final String fromName;
	/** The totems in the list (the Home Totem has its own button). */
	private final List<TravellersTotems.Destination> destinations;
	/** The Home Totem, if there is one to go to from here. */
	private final TravellersTotems.Destination home;
	private int page;
	private int selected = -1;
	private boolean homeSelected;
	private Button travel;
	private Button homeButton;

	public TotemTravelScreen(Optional<GlobalPos> from, String fromName, List<TravellersTotems.Destination> destinations) {
		super(Component.translatable(from.isPresent() ? "neothaum.totem.screen.title" : "neothaum.compass.screen.title"));
		this.from = from;
		this.fromName = fromName;
		this.destinations = destinations.stream().filter(d -> !d.home()).toList();
		this.home = destinations.stream().filter(TravellersTotems.Destination::home).findFirst().orElse(null);
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
			.bounds(this.rightX() + 26, this.top() + 128, 76, 20).build());
		this.travel.active = this.picked() != null;
		// the Home Totem, a button of its own beside Travel
		this.homeButton = this.addRenderableWidget(new ImageButton(this.rightX() + 2, this.top() + 128, 20, 20, HOME_SPRITES, b -> this.pickHome(),
			Component.translatable("neothaum.totem.screen.home")));
		this.homeButton.active = this.home != null;
		this.homeButton.setTooltip(Tooltip.create(this.home != null ? Component.translatable("neothaum.totem.screen.home_named", this.home.name())
			: this.hasHome() ? Component.translatable("neothaum.totem.screen.home_here") : Component.translatable("neothaum.totem.screen.no_home")));
		int pages = this.pages();
		Button back = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.turn(-1))
			.bounds(this.leftX(), this.top() + 134, 16, 16).build());
		Button forward = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.turn(1))
			.bounds(this.leftX() + TEXT_W - 16, this.top() + 134, 16, 16).build());
		back.visible = forward.visible = pages > 1;
		back.active = this.page > 0;
		forward.active = this.page < pages - 1;
	}

	/** The heading over the list: the totem you stand at (or the compass), wrapped to the tablet. */
	private List<FormattedCharSequence> heading() {
		Component here = this.from.isPresent() ? Component.translatable("neothaum.totem.screen.here", this.fromName)
			: Component.translatable("neothaum.compass.screen.here");
		return this.font.split(here, TEXT_W);
	}

	/** Where the list starts, below the heading however long the name makes it (from the tablet's top). */
	private int listTop() {
		return Math.max(40, 12 + this.heading().size() * 10 + GAP);
	}

	/** How many totems fit on a page below the heading: seven, or fewer under a long name. */
	private int perPage() {
		return Math.max(3, Math.min(7, (LIST_BOTTOM - this.listTop()) / ROW));
	}

	private int pages() {
		return Math.max(1, (this.destinations.size() + this.perPage() - 1) / this.perPage());
	}

	private void turn(int by) {
		this.page = Math.max(0, Math.min(this.pages() - 1, this.page + by));
		this.rebuildWidgets();
	}

	/** The index of the list row under the mouse, or -1. */
	private int rowAt(double mx, double my) {
		int y0 = this.top() + this.listTop();
		if (mx < this.leftX() - 2 || mx > this.leftX() + TEXT_W + 2 || my < y0) {
			return -1;
		}
		int row = (int) ((my - y0) / ROW);
		int i = this.page * this.perPage() + row;
		return row < this.perPage() && i < this.destinations.size() ? i : -1;
	}

	/** Picks a totem from the list (for tests, and for the mouse). */
	public void select(int index) {
		this.selected = index;
		this.homeSelected = false;
		this.travel.active = this.picked() != null;
	}

	/** Picks the Home Totem. */
	public void pickHome() {
		if (this.home != null) {
			this.selected = -1;
			this.homeSelected = true;
			this.travel.active = true;
		}
	}

	/** The totem picked, from the list or the home button, or null. */
	private TravellersTotems.Destination picked() {
		if (this.homeSelected) {
			return this.home;
		}
		return this.selected >= 0 && this.selected < this.destinations.size() ? this.destinations.get(this.selected) : null;
	}

	/** Whether this player has a Home Totem at all (it may be the one they stand at). */
	private boolean hasHome() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null && mc.player.getAttachedOrCreate(TravellersTotems.KNOWN).home().isPresent();
	}

	/** The mark for a totem in the Nether or the End, or null. */
	private static Identifier mark(TravellersTotems.Destination d) {
		var dim = d.pos().dimension();
		return dim == net.minecraft.world.level.Level.NETHER ? NETHER_MARK : dim == net.minecraft.world.level.Level.END ? END_MARK : null;
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
		TravellersTotems.Destination d = this.picked();
		if (d == null) {
			return;
		}
		ClientPlayNetworking.send(new TravellersTotems.TravelTo(d.pos(), this.from.isEmpty()));
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
		int hy = y + 12;
		for (FormattedCharSequence line : this.heading()) {
			g.text(this.font, line, x + (TEXT_W - this.font.width(line)) / 2, hy, FADED, false);
			hy += 10;
		}
		if (this.destinations.isEmpty()) {
			this.centred(g, Component.translatable("neothaum.totem.screen.none"), x, y + 60, FADED);
		}
		int hover = this.rowAt(mouseX, mouseY);
		int per = this.perPage();
		int top = this.listTop();
		int start = this.page * per;
		for (int i = start; i < Math.min(this.destinations.size(), start + per); i++) {
			TravellersTotems.Destination d = this.destinations.get(i);
			int ry = y + top + (i - start) * ROW;
			if (i == this.selected || i == hover) {
				g.fill(RenderPipelines.GUI, x - 2, ry - 2, x + TEXT_W + 2, ry + ROW - 3, i == this.selected ? 0x609A6AE0 : 0x309A6AE0);
			}
			// the name has the line to itself (after a mark if it is in the Nether or the End); how far it is shows on the right page
			Identifier mark = mark(d);
			int tx = x;
			if (mark != null) {
				g.blitSprite(RenderPipelines.GUI_TEXTURED, mark, x, ry - 1, MARK, MARK);
				tx += MARK + 2;
			}
			g.text(this.font, Component.literal(this.fit(d.name(), TEXT_W - (tx - x))), tx, ry, INK, false);
		}
		if (this.pages() > 1) {
			this.centred(g, Component.literal((this.page + 1) + " / " + this.pages()), x, y + 138, FADED);
		}
		// the right page: the totem picked
		int rx = this.rightX();
		if (this.picked() != null) {
			TravellersTotems.Destination d = this.picked();
			int ly = y + 20;
			for (FormattedCharSequence line : this.font.split(Component.literal(d.name()), TEXT_W)) {
				g.text(this.font, line, rx + (TEXT_W - this.font.width(line)) / 2, ly, d.home() ? HOME : INK, false);
				ly += 11;
			}
			Identifier mark = mark(d);
			if (mark != null) {
				g.blitSprite(RenderPipelines.GUI_TEXTURED, mark, rx + (TEXT_W - MARK) / 2, ly, MARK, MARK);
				ly += MARK + 2;
			}
			ly += 6;
			Component info = d.home() ? Component.translatable("neothaum.totem.screen.your_home")
				: Component.translatable("neothaum.totem.screen.away", this.where(d));
			for (FormattedCharSequence line : this.font.split(info, TEXT_W)) {
				g.text(this.font, line, rx + (TEXT_W - this.font.width(line)) / 2, ly, FADED, false);
				ly += 11;
			}
		} else if (!this.destinations.isEmpty() || this.home != null) {
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
