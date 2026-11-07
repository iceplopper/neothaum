package dev.neothaum.client.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.config.NeoThaumConfig;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * NeoThaum addition, not TC4: a lens flare from bright nodes. NodeRenderer reports each bright node it draws (so only nodes the viewer can see,
 * through goggles or a thaumometer); this draws, over the world, a halo on the node, a horizontal streak through it and ghost rings along the
 * line from it through the middle of the screen, in the colour of its strongest aspect. It needs a clear line of sight, grows as the node nears
 * the middle of the view and fades in and out. Only while an Iris shader pack is in use, to go with the pack's own bloom; drawn as part of the
 * HUD (also when F1 hides the rest).
 */
public final class BrightNodeFlare {
	private static final Identifier HALO = NeoThaum.id("textures/misc/flare_halo.png");
	private static final Identifier STREAK = NeoThaum.id("textures/misc/flare_streak.png");
	private static final Identifier GHOST = NeoThaum.id("textures/misc/flare_ghost.png");
	/** Ghost rings along the flare line: where (0 the node, 1 the middle of the screen, past 1 beyond it), size and strength. */
	private static final float[][] GHOSTS = {{0.45F, 0.22F, 0.16F}, {0.8F, 0.1F, 0.24F}, {1.3F, 0.32F, 0.1F}, {1.7F, 0.16F, 0.18F}};
	/** Within this the flare is at its strongest close up, weakening to a steady glow at its edge; beyond it, that glow carries on. */
	private static final double NEAR = 48.0;

	/** How far a flare shows: to the end of the render distance (and never less than 64 blocks). */
	public static double range() {
		return Math.max(64.0, Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0 + 16.0);
	}

	private static final class Flare {
		Vec3 pos;
		int color;
		long seenAt;
		float strength;
	}

	private static final Map<BlockPos, Flare> FLARES = new HashMap<>();

	/** From NodeRenderer: a bright node was drawn this frame. */
	public static void seen(BlockPos pos, int color) {
		if (!NeoThaumConfig.current.brightNodeFlare || !dev.neothaum.addon.Additions.enabled("bright_nodes")) {
			return;
		}
		Flare f = FLARES.computeIfAbsent(pos.immutable(), p -> new Flare());
		f.pos = Vec3.atCenterOf(pos);
		f.color = color;
		f.seenAt = System.nanoTime();
	}

	/** HUD element for when the HUD shows: under the hotbar and the rest. */
	public static void renderUnderHud(GuiGraphicsExtractor g, DeltaTracker delta) {
		if (!Minecraft.getInstance().gui.hud.isHidden()) {
			render(g);
		}
	}

	/** HUD element for when F1 hides the HUD: the flare is part of the view, so it stays. */
	public static void renderHudHidden(GuiGraphicsExtractor g, DeltaTracker delta) {
		if (Minecraft.getInstance().gui.hud.isHidden()) {
			render(g);
		}
	}

	private static void render(GuiGraphicsExtractor g) {
		Minecraft mc = Minecraft.getInstance();
		if (FLARES.isEmpty() || mc.level == null || mc.player == null) {
			return;
		}
		if (!IrisCompat.shaderPackInUse()) {
			FLARES.clear();
			return;
		}
		var camera = mc.gameRenderer.mainCamera();
		Vec3 eye = camera.position();
		Matrix4f viewProj = camera.getViewRotationProjectionMatrix(new Matrix4f());
		int w = g.guiWidth();
		int h = g.guiHeight();
		float cx = w / 2.0F;
		float cy = h / 2.0F;
		long now = System.nanoTime();
		double range = range();
		for (Iterator<Map.Entry<BlockPos, Flare>> it = FLARES.entrySet().iterator(); it.hasNext(); ) {
			Flare f = it.next().getValue();
			boolean current = now - f.seenAt < 100_000_000L;
			Vec3 rel = f.pos.subtract(eye);
			double dist = rel.length();
			Vector4f clip = viewProj.transform(new Vector4f((float) rel.x, (float) rel.y, (float) rel.z, 1.0F));
			boolean inFront = clip.w > 0.0F;
			float sx = inFront ? cx + clip.x / clip.w * cx : 0.0F;
			float sy = inFront ? cy - clip.y / clip.w * cy : 0.0F;
			boolean onScreen = inFront && sx > -w * 0.1F && sx < w * 1.1F && sy > -h * 0.1F && sy < h * 1.1F;
			boolean clear = current && onScreen && dist < range && clearSight(mc, eye, f.pos);
			// ease in and out rather than pop
			f.strength = Mth.clamp(f.strength + (clear ? 0.12F : -0.08F), 0.0F, 1.0F);
			if (f.strength <= 0.0F) {
				if (!current) {
					it.remove();
				}
				continue;
			}
			if (!inFront) {
				continue;
			}
			// stronger close up and toward the middle of the view; far off, a steady glow out to the end of the render distance
			float nearness = (float) Math.max(0.0, 1.0 - dist / NEAR);
			float centred = 1.0F - Mth.clamp((float) Math.hypot((sx - cx) / cx, (sy - cy) / cy) / 1.2F, 0.0F, 1.0F);
			float power = f.strength * (0.35F + 0.65F * nearness) * (0.4F + 0.6F * centred);
			draw(g, f.color, sx, sy, cx, cy, h, power, now);
		}
	}

	/**
	 * Whether the flare's light reaches the eye: through glass, panes and water, and between the parts of thin blocks such as fences, but
	 * not through tinted glass or anything solid.
	 */
	private static boolean clearSight(Minecraft mc, Vec3 eye, Vec3 to) {
		BlockPos target = BlockPos.containing(to);
		Boolean blocked = net.minecraft.world.level.BlockGetter.traverseBlocks(eye, to, mc.level, (level, pos) -> {
			if (pos.equals(target)) {
				return null;
			}
			var state = level.getBlockState(pos);
			if (state.isAir() || seeThrough(state)) {
				return null;
			}
			// anything else stops the light where its visible shape crosses the line
			var shape = state.getVisualShape(level, pos, net.minecraft.world.phys.shapes.CollisionContext.empty());
			return shape.clip(eye, to, pos) != null ? Boolean.TRUE : null;
		}, level -> Boolean.FALSE);
		return !blocked;
	}

	/**
	 * Glass, glass panes and iron bars, and water: see-through all over. Blocks whose texture merely has holes (doors, trapdoors, leaves,
	 * ladders) count as solid; telling a hole from the rest would mean reading the texture where the line crosses it. Fences, gates and
	 * chains are thin enough that their own shape already leaves the gaps open.
	 */
	private static boolean seeThrough(net.minecraft.world.level.block.state.BlockState state) {
		var b = state.getBlock();
		if (b instanceof net.minecraft.world.level.block.TintedGlassBlock) {
			return false;
		}
		return b instanceof net.minecraft.world.level.block.TransparentBlock || b instanceof net.minecraft.world.level.block.IronBarsBlock
			|| !state.getFluidState().isEmpty();
	}

	private static void draw(GuiGraphicsExtractor g, int color, float sx, float sy, float cx, float cy, int h, float power, long now) {
		float pulse = 1.0F + Mth.sin(now / 400_000_000.0F) * 0.06F;
		// a hot white core over a halo in the node's colour
		float halo = h * 0.55F * pulse * (0.5F + 0.5F * power);
		blit(g, HALO, sx, sy, halo, halo, color, power * 0.7F);
		blit(g, HALO, sx, sy, halo * 0.35F, halo * 0.35F, 0xFFFFFF, power);
		// the anamorphic streak, wide and thin
		blit(g, STREAK, sx, sy, h * 1.6F * (0.4F + 0.6F * power), h * 0.05F, mix(color, 0xFFFFFF, 0.5F), power * 0.8F);
		// ghosts along the line from the node through the middle of the screen
		for (float[] gh : GHOSTS) {
			float gx = sx + (cx - sx) * gh[0] * 2.0F;
			float gy = sy + (cy - sy) * gh[0] * 2.0F;
			float size = h * gh[1];
			blit(g, GHOST, gx, gy, size, size, mix(color, 0xFFFFFF, 0.25F), power * gh[2]);
		}
	}

	/** A texture centred on (x, y), added to what is below in the given colour and strength. */
	private static void blit(GuiGraphicsExtractor g, Identifier tex, float x, float y, float w, float h, int rgb, float strength) {
		int a = Mth.clamp((int) (strength * 255.0F), 0, 255);
		if (a <= 0) {
			return;
		}
		// additive: the texture's brightness, scaled by the colour times the strength
		int r = ((rgb >> 16) & 0xFF) * a / 255;
		int gr = ((rgb >> 8) & 0xFF) * a / 255;
		int b = (rgb & 0xFF) * a / 255;
		GuiUtil.blitFull(g, GuiUtil.GUI_ADDITIVE, tex, x - w / 2.0F, y - h / 2.0F, w, h, 0xFF000000 | r << 16 | gr << 8 | b);
	}

	private static int mix(int a, int b, float t) {
		int r = (int) Mth.lerp(t, (a >> 16) & 0xFF, (b >> 16) & 0xFF);
		int g = (int) Mth.lerp(t, (a >> 8) & 0xFF, (b >> 8) & 0xFF);
		int bl = (int) Mth.lerp(t, a & 0xFF, b & 0xFF);
		return r << 16 | g << 8 | bl;
	}

	private BrightNodeFlare() {
	}
}
