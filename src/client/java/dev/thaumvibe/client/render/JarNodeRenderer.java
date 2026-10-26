package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.nodes.NodeModifier;
import dev.thaumvibe.api.nodes.NodeType;
import dev.thaumvibe.items.JarNodeItem;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.tiles.JarNodeBlockEntity;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * TileJarRenderer for TileJarNode: the node is drawn as a small (0.7 size) node a little low in the jar, always visible. The glass is the
 * block model.
 */
public class JarNodeRenderer implements BlockEntityRenderer<JarNodeBlockEntity, NodeRenderer.State> {
	public JarNodeRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public NodeRenderer.State createRenderState() {
		return new NodeRenderer.State();
	}

	@Override
	public void extractRenderState(JarNodeBlockEntity jar, NodeRenderer.State s, float partialTicks, Vec3 cam,
		ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(jar, s, partialTicks, cam, breakProgress);
		s.aspects = jar.getAspects().copy();
		s.type = jar.getNodeType();
		s.modifier = jar.getNodeModifier();
		s.x = jar.getBlockPos().getX();
		s.visible = true;
		s.depthIgnore = false;
		s.inJar = true;
		s.size = 0.7F;
		s.viewDistance = 64.0;
		s.distance = cam.distanceTo(Vec3.atCenterOf(jar.getBlockPos()));
		var viewer = Minecraft.getInstance().player;
		s.ticks = viewer == null ? 0 : viewer.tickCount;
		s.drainFrom = null;
	}

	@Override
	public void submit(NodeRenderer.State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		ps.pushPose();
		ps.translate(0.0F, -0.1F, 0.0F);
		NodeRenderer.draw(s, ps, collector, camera.orientation);
		ps.popPose();
	}

	public record NodeData(AspectList aspects, NodeType type, NodeModifier modifier) {
	}

	/** ItemJarNodeRenderer: the node as three crossed flat quads (ItemNodeRenderer.renderItemNode) in the middle of the jar. */
	public record ItemRenderer() implements SpecialModelRenderer<NodeData> {
		@Override
		public NodeData extractArgument(ItemStack stack) {
			AspectList al = stack.get(TVComponents.ASPECTS);
			return al == null || al.size() == 0 ? null : new NodeData(al, JarNodeItem.getNodeType(stack), JarNodeItem.getNodeModifier(stack));
		}

		@Override
		public void submit(NodeData data, PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			if (data == null) {
				return;
			}
			ps.pushPose();
			ps.translate(0.5F, 0.4F, 0.5F);
			itemNode(data, ps, collector);
			ps.rotateDegrees(Axis.YP, 90.0F);
			itemNode(data, ps, collector);
			ps.rotateDegrees(Axis.XP, 90.0F);
			itemNode(data, ps, collector);
			ps.popPose();
		}

		@Override
		public void getExtents(Consumer<Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new Vector3f(i & 1, i >> 1 & 1, i >> 2 & 1));
			}
		}
	}

	/** ItemNodeRenderer.renderItemNode. */
	private static void itemNode(NodeData node, PoseStack ps, SubmitNodeCollector collector) {
		var viewer = Minecraft.getInstance().player;
		int ticks = viewer == null ? 0 : viewer.tickCount;
		float alpha = 0.5F;
		if (node.modifier() != null) {
			switch (node.modifier()) {
				case BRIGHT -> alpha *= 1.5F;
				case PALE -> alpha *= 0.66F;
				case FADING -> alpha *= Mth.sin(ticks / 3.0F) * 0.25F + 0.33F;
			}
		}
		long nt = System.nanoTime();
		float bscale = 0.25F;
		int frames = 32;
		int i = (int) ((nt / 40000000L + 1L) % frames);
		int count = 0;
		float average = 0.0F;
		AspectList al = node.aspects();
		for (Aspect aspect : al.getAspects()) {
			float a = aspect.getBlend() == 771 ? alpha * 1.5F : alpha;
			average += al.getAmount(aspect);
			float scale = Mth.sin(ticks / (14.0F - count)) * bscale + bscale * 2.0F;
			scale = 0.2F + scale * (al.getAmount(aspect) / 50.0F);
			quad(ps, collector, aspect.getBlend() != 771, scale, a / al.size(), frames, 0, i, aspect.getColor());
			count++;
		}
		average /= al.size();
		float scale = 0.1F + average / 150.0F;
		int strip = 1;
		boolean additive = true;
		switch (node.type()) {
			case UNSTABLE -> strip = 6;
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
		quad(ps, collector, additive, scale, alpha, frames, strip, i, 0xFFFFFF);
	}

	/** UtilsFX.renderAnimatedQuadStrip: a flat quad (both faces) using cell (frame, strip) of nodes.png. */
	private static void quad(PoseStack ps, SubmitNodeCollector collector, boolean additive, float scale, float alpha, int frames, int strip, int frame,
		int color) {
		float f2 = (float) frame / frames;
		float f3 = (float) (frame + 1) / frames;
		float f4 = (float) strip / frames;
		float f5 = (strip + 1.0F) / frames;
		float h = 0.5F * scale;
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.NODES, additive, true), (pose, vc) -> {
			vertex(vc, pose, -h, h, f2, f5, r, g, b, a);
			vertex(vc, pose, h, h, f3, f5, r, g, b, a);
			vertex(vc, pose, h, -h, f3, f4, r, g, b, a);
			vertex(vc, pose, -h, -h, f2, f4, r, g, b, a);
		});
	}

	private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v, int r, int g, int b, int a) {
		vc.addVertex(pose, x, y, 0.0F).setColor(r, g, b, a).setUv(u, v).setOverlay(0).setLight(220).setNormal(pose, 0.0F, 0.0F, -1.0F);
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<NodeData> {
		public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

		@Override
		public SpecialModelRenderer<NodeData> bake(SpecialModelRenderer.BakingContext ctx) {
			return new ItemRenderer();
		}

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}
	}
}
