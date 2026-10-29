package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neothaum.NeoThaum;
import dev.neothaum.golem.GolemBellItem;
import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemMarker;
import dev.neothaum.registry.TVComponents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * RenderEventHandler.renderMarkedBlocks: while a golemancer's bell linked to a golem is held, its golem's home shows as a slowly shifting
 * gear on the face it is bound to, and every marked face as a glowing square in its colour (shimmering white when any colour will do), with a
 * wavering line of script from the golem to it (drawMarkerLine; Config.golemLinkQuality sets its detail, and below 4 turns it off).
 */
public final class BellOverlay {
	private static final Identifier HOME = NeoThaum.id("textures/misc/home.png");
	private static final Identifier MARK = NeoThaum.id("textures/misc/mark.png");
	private static final Identifier SCRIPT = NeoThaum.id("textures/misc/script.png");
	/** UtilsFX.colors: the sixteen dye colours. */
	private static final int[] COLORS = {15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131, 10526880, 2651799, 8073150,
		2437522, 5320730, 3887386, 11743532, 1973019};

	private BellOverlay() {
	}

	public static void render(LevelRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return;
		}
		ItemStack held = mc.player.getMainHandItem();
		if (!(held.getItem() instanceof GolemBellItem) || !held.has(TVComponents.GOLEM_MARKERS)) {
			return;
		}
		int gid = GolemBellItem.getGolemId(held);
		if (gid < 0 || !(mc.level.getEntity(gid) instanceof GolemEntity golem)) {
			return;
		}
		int quality = dev.neothaum.config.NeoThaumConfig.current.golemLinkQuality;
		Vec3 golemTop = golem.getPosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false)).add(0.0, golem.getBbHeight(), 0.0);
		Vec3 cam = mc.gameRenderer.mainCamera().position();
		PoseStack ps = ctx.poseStack();
		float time = System.nanoTime() / 30000000L;
		BlockPos home = held.get(TVComponents.GOLEM_BELL_POS);
		Integer face = held.get(TVComponents.GOLEM_BELL_FACE);
		if (home != null && face != null && mc.player.distanceToSqr(Vec3.atCenterOf(home)) < 4096.0) {
			// the gear sits on the face of the block the golem is bound to, toward its home side
			drawFace(ctx, ps, cam, home.relative(Direction.from3DDataValue(face)), Direction.from3DDataValue(face), HOME, 0.65F, shimmer(time, face));
		}
		for (GolemMarker m : GolemBellItem.getMarkers(held)) {
			Direction dir = Direction.from3DDataValue(m.side());
			BlockPos at = m.pos().relative(dir);
			if (m.dim().equals(mc.level.dimension()) && mc.player.distanceToSqr(Vec3.atCenterOf(at)) < 4096.0) {
				drawFace(ctx, ps, cam, at, dir, MARK, 0.4F, m.color() < 0 ? shimmer(time, m.side()) : COLORS[m.color()]);
				if (quality > 3) {
					drawMarkerLine(ctx, ps, cam, golemTop, m.pos(), dir, m.color(), time, quality);
				}
			}
		}
	}

	/**
	 * drawMarkerLine: a ribbon of script from the top of the golem to the middle of the space in front of the marked face, curling in to the face
	 * itself at the end. It wavers most in the middle, fades in from the golem, scrolls toward the mark, and has quality segments a block.
	 */
	private static void drawMarkerLine(LevelRenderContext ctx, PoseStack ps, Vec3 cam, Vec3 golem, BlockPos marked, Direction dir, int color, float time,
		int quality) {
		// TC4's x, y, z: the marked block, half a block out along the face
		double x = marked.getX() + dir.getStepX() * 0.5;
		double y = marked.getY() + dir.getStepY() * 0.5;
		double z = marked.getZ() + dir.getStepZ() * 0.5;
		int side = dir.get3DDataValue();
		double dc1x = x + 0.5 + dir.getStepX() * 0.5 - golem.x;
		double dc1y = y + 0.5 + dir.getStepY() * 0.5 - golem.y;
		double dc1z = z + 0.5 + dir.getStepZ() * 0.5 - golem.z;
		double dc22x = x + 0.5 - golem.x;
		double dc22y = y + 0.5 - golem.y;
		double dc22z = z + 0.5 - golem.z;
		double d3 = x - golem.x;
		double d4 = y - golem.y;
		double d5 = z - golem.z;
		float dist = Mth.sqrt((float) (d3 * d3 + d4 * d4 + d5 * d5));
		float length = Math.round(dist) * quality;
		if (length <= 0.0F) {
			return;
		}
		int fixed = color < 0 ? -1 : COLORS[color];
		// each step: position, scroll, colour and alpha
		java.util.List<float[]> pts = new java.util.ArrayList<>();
		for (int i = 0; i <= length; i++) {
			float f2 = i / length;
			float f2a = Math.min(0.75F, i * 1.5F / length);
			float f3 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);
			float f4 = 0.0F;
			double wobble = side * 20 + dist * (1.0F - f2) * quality - time % 32767.0F / 5.0F;
			double dx = dc1x + Mth.sin((float) ((wobble + z % 16.0) / 4.0)) * 0.5F * f3;
			double dy = dc1y + Mth.sin((float) ((wobble + x % 16.0) / 3.0)) * 0.5F * f3;
			double dz = dc1z + Mth.sin((float) ((wobble + y % 16.0) / 2.0)) * 0.5F * f3;
			if (i > length - quality / 2) {
				double dx2 = dc22x + Mth.sin((float) ((wobble + z % 16.0) / 4.0)) * 0.5F * f3;
				double dy2 = dc22y + Mth.sin((float) ((wobble + x % 16.0) / 3.0)) * 0.5F * f3;
				double dz2 = dc22z + Mth.sin((float) ((wobble + y % 16.0) / 2.0)) * 0.5F * f3;
				f3 = (length - i) / (quality / 2.0F);
				f4 = 1.0F - f3;
				dx = dx * f3 + dx2 * f4;
				dy = dy * f3 + dy2 * f4;
				dz = dz * f3 + dz2 * f4;
			}
			int rgb = fixed;
			if (fixed < 0) {
				int r = (int) ((Mth.sin(time % 32767.0F / 12.0F + side + i) * 0.2F + 0.8F) * 255);
				int g = (int) ((Mth.sin(time % 32767.0F / 14.0F + side + i) * 0.2F + 0.8F) * 255);
				int b = (int) ((Mth.sin(time % 32767.0F / 16.0F + side + i) * 0.2F + 0.8F) * 255);
				rgb = r << 16 | g << 8 | b;
			}
			float u = (1.0F - f2) * dist - time * 0.005F;
			pts.add(new float[]{(float) (dx * f2), (float) (dy * f2), (float) (dz * f2), u, rgb, f2a * (1.0F - f4)});
		}
		ps.pushPose();
		ps.translate(golem.x - cam.x, golem.y - cam.y, golem.z - cam.z);
		ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(SCRIPT, true, true), (pose, vc) -> {
			// TC4's triangle strip, 0.1 tall, as quads between neighbouring steps
			for (int i = 0; i < pts.size() - 1; i++) {
				float[] a = pts.get(i);
				float[] b = pts.get(i + 1);
				lineVertex(vc, pose, a, -0.05F, 1.0F);
				lineVertex(vc, pose, a, 0.05F, 0.0F);
				lineVertex(vc, pose, b, 0.05F, 0.0F);
				lineVertex(vc, pose, b, -0.05F, 1.0F);
			}
		});
		ps.popPose();
	}

	private static void lineVertex(VertexConsumer vc, PoseStack.Pose pose, float[] p, float dy, float v) {
		int rgb = (int) p[4];
		vc.addVertex(pose, p[0], p[1] + dy, p[2]).setColor((rgb >> 16 & 255) / 255.0F, (rgb >> 8 & 255) / 255.0F, (rgb & 255) / 255.0F, p[5])
			.setUv(p[3], v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	/** A colour drifting gently around white. */
	private static int shimmer(float time, int side) {
		int r = (int) ((Mth.sin(time % 32767.0F / 12.0F + side) * 0.2F + 0.8F) * 255);
		int g = (int) ((Mth.sin(time % 32767.0F / 14.0F + side) * 0.2F + 0.8F) * 255);
		int b = (int) ((Mth.sin(time % 32767.0F / 16.0F + side) * 0.2F + 0.8F) * 255);
		return r << 16 | g << 8 | b;
	}

	/** A square of the texture on the face of the block behind `at`, the side facing `dir`. */
	private static void drawFace(LevelRenderContext ctx, PoseStack ps, Vec3 cam, BlockPos at, Direction dir, Identifier tex, float scale, int rgb) {
		ps.pushPose();
		ps.translate(at.getX() + 0.5 - dir.getStepX() * 0.49 - cam.x, at.getY() + 0.5 - dir.getStepY() * 0.49 - cam.y,
			at.getZ() + 0.5 - dir.getStepZ() * 0.49 - cam.z);
		ps.rotate(dir.getRotation());
		ps.scale(scale, scale, scale);
		float r = (rgb >> 16 & 255) / 255.0F, g = (rgb >> 8 & 255) / 255.0F, b = (rgb & 255) / 255.0F;
		ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(tex, true, true), (pose, vc) -> {
			// a quad in the XZ plane, which dir's rotation turns to face along dir
			vertex(vc, pose, -0.5F, -0.5F, 0.0F, 0.0F, r, g, b);
			vertex(vc, pose, -0.5F, 0.5F, 0.0F, 1.0F, r, g, b);
			vertex(vc, pose, 0.5F, 0.5F, 1.0F, 1.0F, r, g, b);
			vertex(vc, pose, 0.5F, -0.5F, 1.0F, 0.0F, r, g, b);
		});
		ps.popPose();
	}

	private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float z, float u, float v, float r, float g, float b) {
		vc.addVertex(pose, x, 0.0F, z).setColor(r, g, b, 1.0F).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}
}
