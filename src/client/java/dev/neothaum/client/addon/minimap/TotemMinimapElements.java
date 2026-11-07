package dev.neothaum.client.addon.minimap;

import dev.neothaum.NeoThaum;
import dev.neothaum.addon.totems.TravellersTotems;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.data.AtlasIds;
import xaero.common.HudMod;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.lib.client.graphics.XaeroBufferProvider;

/**
 * Addon: the totems you have attuned to (and your Home Totem) on Xaero's Minimap, through its element renderers (as its own tracked players
 * are drawn): the same icons as on map items. Only loaded when Xaero's Minimap is installed (MinimapIntegration).
 */
public final class TotemMinimapElements {
	private static boolean registered;

	/** From the client tick: once Xaero's Minimap is ready, adds the totems to what it draws over the minimap. */
	public static void tick() {
		if (registered || HudMod.INSTANCE == null || HudMod.INSTANCE.getMinimap() == null
			|| HudMod.INSTANCE.getMinimap().getOverMapRendererHandler() == null) {
			return;
		}
		registered = true;
		HudMod.INSTANCE.getMinimap().getOverMapRendererHandler().add(new Renderer());
		NeoThaum.LOGGER.info("Xaero's Minimap detected - the totems you have attuned to will be shown on it.");
	}

	/** Nothing to keep between elements. */
	private static final class Context {
	}

	private static final class Reader extends MinimapElementReader<TravellersTotems.Destination, Context> {
		@Override
		public boolean isHidden(TravellersTotems.Destination m, Context c) {
			return false;
		}

		@Override
		public double getRenderX(TravellersTotems.Destination m, Context c, float pt) {
			return m.pos().pos().getX() + 0.5;
		}

		@Override
		public double getRenderY(TravellersTotems.Destination m, Context c, float pt) {
			return m.pos().pos().getY() + 0.5;
		}

		@Override
		public double getRenderZ(TravellersTotems.Destination m, Context c, float pt) {
			return m.pos().pos().getZ() + 0.5;
		}

		@Override
		public int getInteractionBoxLeft(TravellersTotems.Destination m, Context c, float pt) {
			return -5;
		}

		@Override
		public int getInteractionBoxRight(TravellersTotems.Destination m, Context c, float pt) {
			return 5;
		}

		@Override
		public int getInteractionBoxTop(TravellersTotems.Destination m, Context c, float pt) {
			return -5;
		}

		@Override
		public int getInteractionBoxBottom(TravellersTotems.Destination m, Context c, float pt) {
			return 5;
		}

		@Override
		public int getRenderBoxLeft(TravellersTotems.Destination m, Context c, float pt) {
			return -8;
		}

		@Override
		public int getRenderBoxRight(TravellersTotems.Destination m, Context c, float pt) {
			return 8;
		}

		@Override
		public int getRenderBoxTop(TravellersTotems.Destination m, Context c, float pt) {
			return -8;
		}

		@Override
		public int getRenderBoxBottom(TravellersTotems.Destination m, Context c, float pt) {
			return 8;
		}

		@Override
		public int getLeftSideLength(TravellersTotems.Destination m, Minecraft mc) {
			return 9;
		}

		@Override
		public String getMenuName(TravellersTotems.Destination m) {
			return TotemMapMarks.label(m).getString();
		}

		@Override
		public String getFilterName(TravellersTotems.Destination m) {
			return m.name();
		}

		@Override
		public int getMenuTextFillLeftPadding(TravellersTotems.Destination m) {
			return 0;
		}

		@Override
		public int getRightClickTitleBackgroundColor(TravellersTotems.Destination m) {
			return m.home() ? 0xFFB07A28 : 0xFF6E44B8;
		}

		@Override
		public boolean shouldScaleBoxWithOptionalScale() {
			return true;
		}
	}

	private static final class Provider extends MinimapElementRenderProvider<TravellersTotems.Destination, Context> {
		private Iterator<TravellersTotems.Destination> it = List.<TravellersTotems.Destination>of().iterator();

		@Override
		public void begin(MinimapElementRenderLocation location, Context c) {
			Minecraft mc = Minecraft.getInstance();
			this.it = TotemMapMarks.in(mc.level == null ? null : mc.level.dimension()).iterator();
		}

		@Override
		public boolean hasNext(MinimapElementRenderLocation location, Context c) {
			return this.it.hasNext();
		}

		@Override
		public TravellersTotems.Destination getNext(MinimapElementRenderLocation location, Context c) {
			return this.it.next();
		}

		@Override
		public void end(MinimapElementRenderLocation location, Context c) {
			this.it = List.<TravellersTotems.Destination>of().iterator();
		}
	}

	private static final class Renderer extends MinimapElementRenderer<TravellersTotems.Destination, Context> {
		Renderer() {
			super(new Reader(), new Provider(), new Context());
		}

		@Override
		public boolean renderElement(TravellersTotems.Destination m, boolean highlighted, boolean outOfBounds, double depth, float scale,
			double partialX, double partialY, MinimapElementRenderInfo info, MinimapElementGraphics g, XaeroBufferProvider buffers) {
			var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.MAP_DECORATIONS)
				.getSprite(m.home() ? TotemMapMarks.HOME_SPRITE : TotemMapMarks.TOTEM_SPRITE);
			var pose = g.pose();
			pose.pushPose();
			pose.scale(scale, scale, 1.0F);
			g.blit(sprite, -4, -4, 8, 8, RenderPipelines.GUI_TEXTURED);
			pose.popPose();
			return true;
		}

		@Override
		public void preRender(MinimapElementRenderInfo info, XaeroBufferProvider buffers, MultiTextureRenderTypeRendererProvider multi) {
		}

		@Override
		public void postRender(MinimapElementRenderInfo info, XaeroBufferProvider buffers, MultiTextureRenderTypeRendererProvider multi) {
			buffers.endBatch();
		}

		@Override
		public boolean shouldRender(MinimapElementRenderLocation location) {
			return location == MinimapElementRenderLocation.OVER_MINIMAP || location == MinimapElementRenderLocation.IN_MINIMAP;
		}
	}

	private TotemMinimapElements() {
	}
}
