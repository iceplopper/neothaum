package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** Shared pieces of the essentia renderers: aspect labels and the jar's glowing liquid. */
public final class EssentiaRender {
	public static final Identifier LABEL = Thaumvibe.id("textures/models/label.png");
	public static final Identifier LIQUID = Thaumvibe.id("textures/blocks/animatedglow.png");
	/** animatedglow.png.mcmeta: 20 frames played forward then back, two ticks each. */
	private static final int FRAMES = 20;

	private EssentiaRender() {
	}

	/** Turns the pose (centred on the block) so that -Z points out of the given side. */
	public static void faceTowards(PoseStack ps, Direction facing) {
		ps.rotateDegrees(Axis.YP, 180.0F - facing.toYRot());
	}

	/**
	 * UtilsFX.renderQuadCenteredFromTexture + drawTag: a label card of the given size at (0, y, -z) facing -Z, with the aspect's icon on it.
	 */
	public static void label(PoseStack ps, SubmitNodeCollector collector, Aspect aspect, float y, float z, float size, float tilt, int light) {
		ps.pushPose();
		ps.translate(0.0F, y, -z);
		ps.rotateDegrees(Axis.ZP, tilt);
		quad(ps, collector, LABEL, size / 2.0F, 0xFFFFFFFF, light, 0.0F);
		if (aspect != null) {
			// drawTag draws the 16px icon over 16 of the label's ~38px
			quad(ps, collector, aspect.getImage(), size * 0.21F, 0xFF000000 | aspect.getColor(), light, -0.001F);
		}
		ps.popPose();
	}

	private static void quad(PoseStack ps, SubmitNodeCollector collector, Identifier tex, float s, int argb, int light, float dz) {
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(tex), (pose, vc) -> {
			vertex(vc, pose, s, -s, dz, 1, 1, argb, light);
			vertex(vc, pose, s, s, dz, 1, 0, argb, light);
			vertex(vc, pose, -s, s, dz, 0, 0, argb, light);
			vertex(vc, pose, -s, -s, dz, 0, 1, argb, light);
		});
	}

	private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, int argb, int light) {
		vc.addVertex(pose, -x, y, z).setColor(argb).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
	}

	/** TileJarRenderer.renderLiquid: a box of the animated glow texture, tinted by the aspect, filling the jar to its level. */
	public static void liquid(PoseStack ps, SubmitNodeCollector collector, float x0, float y0, float z0, float x1, float y1, float z1, int rgb,
		long gameTime, int light) {
		int step = (int) (gameTime / 2 % (FRAMES * 2 - 2));
		int frame = step < FRAMES ? step : FRAMES * 2 - 2 - step;
		float v0 = frame / (float) FRAMES;
		float v1 = (frame + 1) / (float) FRAMES;
		int argb = 0xFF000000 | rgb;
		int l = Math.max(light & 0xFFFF, 200) | light & 0xFFFF0000;
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(LIQUID), (pose, vc) -> {
			float h = (y1 - y0) * (v1 - v0);
			// top and bottom
			face(vc, pose, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, 0, 1, 0, v0, v1, argb, l);
			face(vc, pose, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, 0, -1, 0, v0, v1, argb, l);
			// sides: the texture is cut to the liquid's height, as RenderBlocks did
			face(vc, pose, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, 0, 0, -1, v1 - h, v1, argb, l);
			face(vc, pose, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1, 0, 0, 1, v1 - h, v1, argb, l);
			face(vc, pose, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0, -1, 0, 0, v1 - h, v1, argb, l);
			face(vc, pose, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, 1, 0, 0, v1 - h, v1, argb, l);
		});
	}

	private static void face(VertexConsumer vc, PoseStack.Pose pose, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy,
		float cz, float dx, float dy, float dz, float nx, float ny, float nz, float vTop, float vBottom, int argb, int light) {
		vc.addVertex(pose, ax, ay, az).setColor(argb).setUv(0, vBottom).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, bx, by, bz).setColor(argb).setUv(0, vTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, cx, cy, cz).setColor(argb).setUv(1, vTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, dx, dy, dz).setColor(argb).setUv(1, vBottom).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
	}
}
