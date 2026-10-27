package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.nodes.NodeModifier;
import dev.neothaum.api.nodes.NodeType;
import dev.neothaum.items.RevealerGear;
import dev.neothaum.items.ThaumometerItem;
import dev.neothaum.tiles.NodeBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Port of TileNodeRenderer: animated billboard strips from nodes.png, one per aspect plus a core strip for the node type. */
public class NodeRenderer implements BlockEntityRenderer<NodeBlockEntity, NodeRenderer.State> {
	public static class State extends BlockEntityRenderState {
		AspectList aspects = new AspectList();
		NodeType type = NodeType.NORMAL;
		NodeModifier modifier;
		boolean visible;
		boolean depthIgnore;
		/** In a jar: drawn with the block entities, before the translucent glass, so the glass covers it as TC4's did. */
		boolean inJar;
		double viewDistance = 64.0;
		double distance;
		int ticks;
		float size = 1.0F;
		Vec3 drainFrom;
		int drainColor;
		float drainProgress;
		int x;
	}

	public NodeRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 64;
	}

	@Override
	public void extractRenderState(NodeBlockEntity node, State s, float partialTicks, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(node, s, partialTicks, cam, breakProgress);
		s.aspects = node.getAspects().copy();
		s.type = node.getNodeType();
		s.modifier = node.getNodeModifier();
		s.x = node.getBlockPos().getX();
		Player viewer = Minecraft.getInstance().player;
		s.visible = false;
		s.depthIgnore = false;
		s.viewDistance = 64.0;
		if (viewer != null) {
			ItemStack head = viewer.getItemBySlot(EquipmentSlot.HEAD);
			if (head.getItem() instanceof RevealerGear r && r.showNodes(head)) {
				s.visible = true;
				s.depthIgnore = true;
			} else if (viewer.getMainHandItem().getItem() instanceof ThaumometerItem && isVisibleTo(0.44F, viewer, node)) {
				s.visible = true;
				s.depthIgnore = true;
				s.viewDistance = 48.0;
			}
			s.ticks = viewer.tickCount;
			s.distance = cam.distanceTo(Vec3.atCenterOf(node.getBlockPos()));
		}
		s.drainFrom = null;
		if (node.drainEntity instanceof Player p && p.isUsingItem()) {
			Vec3 hand = new Vec3(-0.1, -0.1, 0.5).xRot(-p.getXRot(partialTicks) * Mth.DEG_TO_RAD).yRot(-p.getYRot(partialTicks) * Mth.DEG_TO_RAD);
			Vec3 pp = p.getPosition(partialTicks).add(hand).add(0, p == viewer ? p.getEyeHeight() - 0.1 : p.getEyeHeight(), 0);
			s.drainFrom = pp;
			s.drainColor = node.color;
			s.drainProgress = Math.min(p.getTicksUsingItem(), 10) / 10.0F;
		} else if (node.drainEntity != null) {
			node.drainEntity = null;
		}
	}

	/** UtilsFX.isVisibleTo: within the player's view cone. */
	private static boolean isVisibleTo(float fov, Player p, NodeBlockEntity n) {
		Vec3 look = p.getViewVector(1.0F);
		Vec3 to = Vec3.atCenterOf(n.getBlockPos()).subtract(p.getEyePosition()).normalize();
		return look.dot(to) > 1.0F - fov;
	}

	@Override
	public void submit(State s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		draw(s, poseStack, collector, camera.orientation);
	}

	/** TileNodeRenderer.renderTileEntityAt, also used for nodes in jars. */
	static void draw(State s, PoseStack poseStack, SubmitNodeCollector collector, Quaternionf camRot) {
		long nt = System.nanoTime();
		int frames = 32;
		if (s.aspects.size() > 0 && s.visible) {
			if (s.distance > s.viewDistance) {
				return;
			}
			float alpha = (float) ((s.viewDistance - s.distance) / s.viewDistance);
			if (s.modifier != null) {
				switch (s.modifier) {
					case BRIGHT -> alpha *= 1.5F;
					case PALE -> alpha *= 0.66F;
					case FADING -> alpha *= Mth.sin(s.ticks / 3.0F) * 0.25F + 0.33F;
				}
			}
			long time = nt / 5000000L;
			float bscale = 0.25F;
			float rad = (float) (Math.PI * 2);
			int i = (int) ((nt / 40000000L + s.x) % frames);
			int count = 0;
			float angle = 0.0F;
			float average = 0.0F;
			for (Aspect aspect : s.aspects.getAspects()) {
				float a = aspect.getBlend() == 771 ? alpha * 1.5F : alpha;
				average += s.aspects.getAmount(aspect);
				float scale = Mth.sin(s.ticks / (14.0F - count)) * bscale + bscale * 2.0F;
				scale = 0.2F + scale * (s.aspects.getAmount(aspect) / 50.0F);
				scale *= s.size;
				angle = (float) (time % (5000 + 500 * count)) / (5000.0F + 500 * count) * rad;
				strip(poseStack, collector, camRot, aspect.getBlend() != 771, s.depthIgnore, s.inJar, angle, scale,
					a / Math.max(1.0F, s.aspects.size() / 2.0F), frames, 0, i, aspect.getColor());
				count++;
			}
			average /= s.aspects.size();
			float scale = (0.1F + average / 150.0F) * s.size;
			int strip = 1;
			boolean additive = true;
			switch (s.type) {
				case UNSTABLE -> {
					strip = 6;
					angle = 0.0F;
				}
				case DARK -> {
					additive = false;
					strip = 2;
				}
				case TAINTED -> {
					additive = false;
					strip = 5;
				}
				case PURE -> strip = 4;
				case HUNGRY -> {
					scale *= 0.75F;
					strip = 3;
				}
				default -> {
				}
			}
			strip(poseStack, collector, camRot, additive, s.depthIgnore, s.inJar, angle, scale, alpha, frames, strip, i, 0xFFFFFF);
		} else {
			int i = (int) ((nt / 40000000L + s.x) % frames);
			strip(poseStack, collector, camRot, true, false, s.inJar, 0.0F, 0.5F, 0.1F, frames, 1, i, 0xFFFFFF);
		}
		if (s.drainFrom != null) {
			drawFloatyLine(poseStack, collector, s);
		}
	}

	/** UtilsFX.renderFacingStrip: a camera-facing quad, rotated by angle around the view axis, using cell (frame, strip) of nodes.png. */
	private static void strip(PoseStack ps, SubmitNodeCollector collector, Quaternionf camRot, boolean additive, boolean depthIgnore, boolean inJar,
		float angle, float scale, float alpha, int frames, int strip, int frame, int color) {
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);
		ps.rotate(camRot);
		if (angle != 0.0F) {
			ps.rotate(new Quaternionf().rotateZ(angle));
		}
		float f2 = (float) frame / frames;
		float f3 = (float) (frame + 1) / frames;
		float f4 = (float) strip / frames;
		float f5 = (strip + 1.0F) / frames;
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		SubmitNodeCollector.CustomGeometryRenderer quad = (pose, vc) -> {
			vertex(vc, pose, -scale, -scale, f3, f5, r, g, b, a);
			vertex(vc, pose, -scale, scale, f3, f4, r, g, b, a);
			vertex(vc, pose, scale, scale, f2, f4, r, g, b, a);
			vertex(vc, pose, scale, -scale, f2, f5, r, g, b, a);
		};
		var type = TVRenderTypes.glow(TVRenderTypes.NODES, additive, !depthIgnore);
		if (inJar) {
			collector.submitCustomGeometry(ps, type, quad);
		} else {
			afterTerrain(collector, ps, type, quad);
		}
		ps.popPose();
	}

	private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v, int r, int g, int b, int a) {
		vc.addVertex(pose, x, y, 0.0F).setColor(r, g, b, a).setUv(u, v).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	/** UtilsFX.drawFloatyLine: the wavy "wispy" stream from the wand to the node while draining. */
	private static void drawFloatyLine(PoseStack ps, SubmitNodeCollector collector, State s) {
		Vec3 node = Vec3.atCenterOf(s.blockPos);
		double dc1x = s.drainFrom.x - node.x;
		double dc1y = s.drainFrom.y - node.y;
		double dc1z = s.drainFrom.z - node.z;
		float time = (float) (System.nanoTime() / 30000000L);
		float rr = (s.drainColor >> 16 & 255) / 255.0F, gg = (s.drainColor >> 8 & 255) / 255.0F, bb = (s.drainColor & 255) / 255.0F;
		float dist = (float) Math.sqrt(dc1x * dc1x + dc1y * dc1y + dc1z * dc1z);
		float blocks = Math.round(dist);
		float length = blocks * (16 / 2.0F);
		float width = 0.15F;
		float speed = -0.02F;
		List<double[]> pts = new ArrayList<>();
		for (int i = 0; i <= length * s.drainProgress; i++) {
			float f2 = i / length;
			float f3 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);
			double dx = dc1x + Mth.sin((float) ((node.z % 16.0 + dist * (1.0F - f2) * 16 / 2.0F - time % 32767.0F / 5.0F) / 4.0)) * 0.5F * f3;
			double dy = dc1y + Mth.sin((float) ((node.x % 16.0 + dist * (1.0F - f2) * 16 / 2.0F - time % 32767.0F / 5.0F) / 3.0)) * 0.5F * f3;
			double dz = dc1z + Mth.sin((float) ((node.y % 16.0 + dist * (1.0F - f2) * 16 / 2.0F - time % 32767.0F / 5.0F) / 2.0)) * 0.5F * f3;
			float f13 = (1.0F - f2) * dist - time * speed;
			pts.add(new double[]{dx * f2 + 0.5, dy * f2 + 0.5, dz * f2 + 0.5, f13, f3});
		}
		if (pts.size() < 2) {
			return;
		}
		afterTerrain(collector, ps, TVRenderTypes.glow(TVRenderTypes.WISPY, true, true), (pose, vc) -> {
			for (int pass = 0; pass < 2; pass++) {
				for (int i = 0; i < pts.size() - 1; i++) {
					double[] p0 = pts.get(i);
					double[] p1 = pts.get(i + 1);
					float ox = pass == 1 ? width : 0, oy = pass == 0 ? width : 0;
					int a0 = (int) (p0[4] * 255), a1 = (int) (p1[4] * 255);
					int r = (int) (rr * 255), g = (int) (gg * 255), b = (int) (bb * 255);
					quadV(vc, pose, (float) (p0[0] - ox), (float) (p0[1] - oy), (float) p0[2], (float) p0[3], 1.0F, r, g, b, a0);
					quadV(vc, pose, (float) (p0[0] + ox), (float) (p0[1] + oy), (float) p0[2], (float) p0[3], 0.0F, r, g, b, a0);
					quadV(vc, pose, (float) (p1[0] + ox), (float) (p1[1] + oy), (float) p1[2], (float) p1[3], 0.0F, r, g, b, a1);
					quadV(vc, pose, (float) (p1[0] - ox), (float) (p1[1] - oy), (float) p1[2], (float) p1[3], 1.0F, r, g, b, a1);
				}
			}
		});
	}

	private static void quadV(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
		vc.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	static Vector3f unused() {
		return new Vector3f();
	}

	/**
	 * Nodes draw in the after-terrain phase, as TC4 drew them at the end of the frame: otherwise water behind a node is drawn over its glow
	 * (which writes no depth) and washes it out.
	 */
	private static void afterTerrain(SubmitNodeCollector collector, PoseStack ps, net.minecraft.client.renderer.rendertype.RenderType type,
		SubmitNodeCollector.CustomGeometryRenderer geometry) {
		((net.fabricmc.fabric.api.client.rendering.v1.FabricOrderedSubmitNodeCollector) collector).submitCustom(
			net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases.AFTER_TERRAIN,
			new net.minecraft.client.renderer.feature.CustomFeatureRenderer.Submit(ps.last().copy(), type, geometry));
	}
}
