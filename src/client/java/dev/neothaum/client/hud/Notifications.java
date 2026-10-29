package dev.neothaum.client.hud;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.client.render.TVRenderTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Ports of PlayerNotifications + REHNotifyHandler: the text log bottom-right and aspect icons flying to the corner. */
public final class Notifications {
	private static final Identifier BOOK = NeoThaum.id("textures/items/thaumonomicon.png");

	record Note(String text, Identifier image, long expire, long created, int color) {
	}

	record AspectNote(Aspect aspect, float startX, float startY, long created, long expire) {
	}

	/** Config.notificationDelay: how long a notification takes to scroll down and fade, in milliseconds. */
	private static long delay() {
		return dev.neothaum.config.NeoThaumConfig.current.notificationDelay;
	}

	private static List<Note> notes = new ArrayList<>();
	private static List<AspectNote> aspects = new ArrayList<>();

	public static void add(String text) {
		add(text, null, 0xFFFFFF);
	}

	public static void add(String text, Aspect aspect) {
		add(text, aspect.getImage(), aspect.getColor());
	}

	public static void add(String text, Identifier image, int color) {
		long time = System.nanoTime() / 1000000L;
		long timeBonus = notes.isEmpty() ? delay() / 2 : 0L;
		notes.add(new Note(text, image, time + delay() + timeBonus, time + delay() / 4, color));
	}

	public static void addAspect(Aspect aspect) {
		var rand = Minecraft.getInstance().level.getRandom();
		long time = System.nanoTime() / 1000000L + rand.nextInt(1000);
		aspects.add(new AspectNote(aspect, 0.4F + rand.nextFloat() * 0.2F, 0.4F + rand.nextFloat() * 0.2F, time, time + 1500L));
	}

	private static List<Note> listAndUpdate(long time) {
		List<Note> temp = new ArrayList<>();
		boolean first = true;
		for (Note li : notes) {
			if (li.expire >= time) {
				temp.add(first ? li : new Note(li.text, li.image, time + delay(), li.created, li.color));
			}
			first = false;
		}
		notes = temp;
		return temp;
	}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		long time = System.nanoTime() / 1000000L;
		int k = g.guiWidth();
		int l = g.guiHeight();
		List<Note> list = listAndUpdate(time);
		float shift = -8.0F;
		for (int entry = 0; entry < list.size() && entry < dev.neothaum.config.NeoThaumConfig.current.notificationMax; entry++) {
			Note li = list.get(entry);
			int size = mc.font.width(li.text) / 2;
			int alpha = 255;
			if (entry == list.size() - 1 && li.created > time) {
				alpha = 255 - (int) ((float) (li.created - time) / (delay() / 4) * 240.0F);
			}
			if (li.expire < time + delay()) {
				alpha = (int) (255.0F - (float) (time + delay() - li.expire) / delay() * 240.0F);
				shift = -8.0F * (alpha / 255.0F);
			}
			int color = (Math.max(4, alpha / 2) << 24) | 0xFFFFFF;
			g.pose().pushMatrix();
			g.pose().translate(k - size - 10, l - entry * 8 + shift);
			g.pose().scale(0.5F, 0.5F);
			g.text(mc.font, li.text, -4, -8, color, true);
			g.pose().popMatrix();
			if (li.image != null) {
				GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, li.image, k - 9, l - entry * 8 + shift - 6.0F, 8, 8, GuiUtil.color(li.color, alpha / 511.0F));
			}
			if (entry == list.size() - 1 && li.created > time) {
				float scale = (float) (li.created - time) / (delay() / 4);
				int a2 = 255 - (int) (scale * 240.0F);
				g.pose().pushMatrix();
				g.pose().translate(k - 5 - 8.0F * scale - (1.0F - scale) * (1.0F - scale) * (1.0F - scale) * size * 3.0F, l - entry * 8 + shift - 2.0F - 8.0F * scale);
				g.pose().scale(scale, scale);
				int px = 16 * ((mc.player.tickCount + entry * 3) % 16);
				g.blit(RenderPipelines.GUI_TEXTURED, TVRenderTypes.PARTICLES, 0, 0, px, 80, 16, 16, 256, 256, GuiUtil.color(0xFFFFFF, 0.5F - a2 / 511.0F));
				g.pose().popMatrix();
			}
		}
		renderAspects(g, time, k, l);
	}

	private static void renderAspects(GuiGraphicsExtractor g, long time, int k, int l) {
		List<AspectNote> temp = new ArrayList<>();
		for (AspectNote a : aspects) {
			if (a.expire >= time) {
				temp.add(a);
			}
		}
		aspects = temp;
		float mainAlpha = 0.0F;
		for (AspectNote li : temp) {
			if (li.created > time) {
				continue;
			}
			int startX = (int) (k * li.startX);
			int startY = (int) (l * li.startY);
			int endY = -8;
			int bezierX = (int) (k * (0.25F + li.startX));
			int bezierY = (int) (l * li.startY);
			double t = (double) (time - li.created) / (li.expire - li.created);
			double x = (1.0 - t) * (1.0 - t) * startX + 2.0 * (1.0 - t) * t * bezierX + t * t * k;
			double y = (1.0 - t) * (1.0 - t) * startY + 2.0 * (1.0 - t) * t * bezierY + t * t * endY;
			float alpha = 1.0F;
			if (t < 0.3F) {
				alpha = (float) (t / 0.3F);
			} else if (t > 0.66F) {
				alpha = (float) (1.0 - (t - 0.66F) / 0.34F);
			}
			mainAlpha = Math.max(mainAlpha, alpha);
			float sz = 256 * 0.075F * alpha;
			GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, li.aspect.getImage(), (float) x, (float) y, sz, sz, GuiUtil.color(li.aspect.getColor(), alpha * 0.66F));
		}
		if (mainAlpha > 0.0F) {
			GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, BOOK, k - 16, 0, 16, 16, GuiUtil.color(0xFFFFFF, mainAlpha));
		}
	}

	private Notifications() {
	}
}
