package dev.neothaum.client.addon.minimap;

import dev.neothaum.NeoThaum;
import dev.neothaum.addon.totems.TotemMarks;
import dev.neothaum.addon.totems.TravellersTotems;
import dev.neothaum.config.NeoThaumConfig;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Addon: the totems you have attuned to, and your Home Totem, on maps: map items (MapRendererMixin), Xaero's World Map (here, named on
 * hover) and Xaero's Minimap (TotemMinimapElements). The server sends the list with the totems' names (TotemMarks). One setting, mapTotems,
 * shows or hides them everywhere; the World Map has a button for it beside the nodes' one.
 */
public final class TotemMapMarks {
	/** The map icons, 8 pixels square as vanilla's map decorations: violet for a Traveller's Totem, amber for the Home Totem. */
	public static final Identifier TOTEM_SPRITE = NeoThaum.id("totem");
	public static final Identifier HOME_SPRITE = NeoThaum.id("home_totem");
	private static final Identifier TOTEM_ICON = NeoThaum.id("textures/map/decorations/totem.png");
	private static final Identifier HOME_ICON = NeoThaum.id("textures/map/decorations/home_totem.png");
	private static final Identifier BUTTON = NeoThaum.id("textures/gui/worldmap_totems.png");
	private static List<TravellersTotems.Destination> marks = List.of();

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(TotemMarks.Marks.TYPE, (p, ctx) -> ctx.client().execute(() -> marks = List.copyOf(p.totems())));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> marks = List.of()));
	}

	/** Whether totems are to be shown at all. */
	public static boolean shown() {
		return NeoThaumConfig.current.mapTotems && !marks.isEmpty();
	}

	/** The totems in this dimension, if they are shown. */
	public static List<TravellersTotems.Destination> in(@Nullable ResourceKey<Level> dim) {
		if (!shown() || dim == null) {
			return List.of();
		}
		return marks.stream().filter(m -> m.pos().dimension().equals(dim)).toList();
	}

	// ------------------------------------------------------------------ Xaero's World Map

	/** The World Map's button for totems: the slot above the nodes' button in Xaero's right-hand column. */
	public static void addToggle(Screen screen, java.util.function.Consumer<Button> add) {
		WorldMapNodes.addToggle(screen, add, screen.height - 220, BUTTON, Component.translatable("neothaum.worldmap.totems.tooltip"),
			() -> NeoThaumConfig.current.mapTotems, () -> {
				NeoThaumConfig.current.mapTotems = !NeoThaumConfig.current.mapTotems;
				NeoThaumConfig.save();
			});
	}

	/** Draws the totems of the dimension the map is showing, as WorldMapNodes does nodes, each named when the cursor is over it. */
	public static void render(GuiGraphicsExtractor g, Screen screen, int mouseX, int mouseY, double cameraX, double cameraZ, double scale,
		double screenScale, @Nullable ResourceKey<Level> viewed) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}
		double ppb = scale / screenScale;
		float cx = screen.width / 2.0F, cy = screen.height / 2.0F;
		TravellersTotems.Destination hovered = null;
		for (TravellersTotems.Destination m : in(viewed != null ? viewed : mc.level.dimension())) {
			var p = m.pos().pos();
			int px = (int) Math.round(cx + (p.getX() + 0.5 - cameraX) * ppb);
			int py = (int) Math.round(cy + (p.getZ() + 0.5 - cameraZ) * ppb);
			if (px < -8 || py < -8 || px > screen.width + 8 || py > screen.height + 8) {
				continue;
			}
			g.blit(RenderPipelines.GUI_TEXTURED, m.home() ? HOME_ICON : TOTEM_ICON, px - 6, py - 6, 0.0F, 0.0F, 12, 12, 8, 8, 8, 8);
			if (Math.abs(mouseX - px) <= 6 && Math.abs(mouseY - py) <= 6) {
				hovered = m;
			}
		}
		if (hovered != null) {
			g.setTooltipForNextFrame(mc.font, label(hovered), mouseX, mouseY);
		}
	}

	/** A totem's name, as on hover; a Home Totem says so. */
	public static Component label(TravellersTotems.Destination m) {
		return m.home() ? Component.translatable("neothaum.worldmap.totems.home", m.name()) : Component.literal(m.name());
	}

	private TotemMapMarks() {
	}
}
