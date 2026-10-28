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
 * gear on the face it is bound to, and every marked face as a glowing square in its colour (shimmering white when any colour will do).
 */
public final class BellOverlay {
	private static final Identifier HOME = NeoThaum.id("textures/misc/home.png");
	private static final Identifier MARK = NeoThaum.id("textures/misc/mark.png");
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
		if (gid < 0 || !(mc.level.getEntity(gid) instanceof GolemEntity)) {
			return;
		}
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
			}
		}
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
