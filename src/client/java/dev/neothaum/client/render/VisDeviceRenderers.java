package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.items.RevealerGear;
import dev.neothaum.tiles.EnergizedNodeBlockEntity;
import dev.neothaum.tiles.FocalManipulatorBlockEntity;
import dev.neothaum.tiles.NodeStabilizerBlockEntity;
import dev.neothaum.tiles.NodeTransducerBlockEntity;
import dev.neothaum.tiles.VisRelayBlockEntity;
import dev.neothaum.tiles.WandPedestalBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/** The vis network's block entity renderers: relays, node stabilizers and transducers, energized nodes, the wand pedestal and focal manipulator. */
public final class VisDeviceRenderers {
	static final Identifier RELAY_OBJ = NeoThaum.id("textures/models/vis_relay.obj");
	static final Identifier RELAY_TEX = NeoThaum.id("textures/models/vis_relay.png");
	static final Identifier STAB_OBJ = NeoThaum.id("textures/models/node_stabilizer.obj");
	static final Identifier STAB_TEX = NeoThaum.id("textures/models/node_stabilizer.png");
	static final Identifier STAB_OVER = NeoThaum.id("textures/models/node_stabilizer_over.png");
	static final Identifier CONV_TEX = NeoThaum.id("textures/models/node_converter.png");
	static final Identifier CONV_OVER = NeoThaum.id("textures/models/node_converter_over.png");
	static final Identifier BUBBLE = NeoThaum.id("textures/misc/node_bubble.png");
	static final Identifier BEAM1 = NeoThaum.id("textures/misc/beam1.png");
	static final Identifier RING = NeoThaum.id("textures/items/lightningringv.png");
	private static ObjModel relay;
	private static ObjModel stabilizer;

	static ObjModel relay() {
		if (relay == null) {
			relay = ObjModel.load(RELAY_OBJ);
		}
		return relay;
	}

	static ObjModel stabilizer() {
		if (stabilizer == null) {
			stabilizer = ObjModel.load(STAB_OBJ);
		}
		return stabilizer;
	}

	static float viewerTicks(float pt) {
		var p = Minecraft.getInstance().player;
		return p == null ? 0.0F : p.tickCount + pt;
	}

	static void part(PoseStack ps, SubmitNodeCollector c, ObjModel m, String part, Identifier tex, int light, int argb, boolean glow) {
		var type = glow ? RenderTypes.entityTranslucentEmissive(tex) : RenderTypes.entityTranslucent(tex);
		c.submitCustomGeometry(ps, type, (pose, vc) -> m.renderPart(part, pose, vc, light, OverlayTexture.NO_OVERLAY, argb));
	}

	/** UtilsFX.renderFacingQuad at the block's centre plus an offset: one frame of a horizontal strip, camera-facing and additive. */
	static void facingQuad(PoseStack ps, SubmitNodeCollector c, Quaternionf cam, Identifier tex, float ox, float oy, float oz, float scale, float alpha,
		int frames, int frame, int color) {
		ps.pushPose();
		ps.translate(ox, oy, oz);
		ps.rotate(cam);
		float f2 = (float) frame / frames;
		float f3 = (float) (frame + 1) / frames;
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		c.submitCustomGeometry(ps, TVRenderTypes.glow(tex, true, true), (pose, vc) -> {
			v(vc, pose, -scale, -scale, f2, 1.0F, r, g, b, a);
			v(vc, pose, scale, -scale, f3, 1.0F, r, g, b, a);
			v(vc, pose, scale, scale, f3, 0.0F, r, g, b, a);
			v(vc, pose, -scale, scale, f2, 0.0F, r, g, b, a);
		});
		ps.popPose();
	}

	private static void v(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float vv, int r, int g, int b, int a) {
		vc.addVertex(pose, x, y, 0.0F).setColor(r, g, b, a).setUv(u, vv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
			.setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	// ------------------------------------------------------------------ vis relay

	/** TileVisRelayRenderer and FXBeamPower: the relay's rings and crystal, and the faint beam back to its parent. */
	public static class Relay<T extends VisRelayBlockEntity> implements BlockEntityRenderer<T, Relay.State> {
		public static class State extends BlockEntityRenderState {
			Direction facing = Direction.UP;
			int color = -1;
			boolean linked;
			boolean charger;
			float ticks;
			Vec3 parent;
			float r = 1.0F;
			float g = 1.0F;
			float b = 1.0F;
			float opacity = 0.3F;
			boolean revealed;
		}

		public Relay(BlockEntityRendererProvider.Context ctx) {
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
		public void extractRenderState(T be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
			BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
			s.charger = be instanceof dev.neothaum.tiles.WorkbenchChargerBlockEntity;
			s.facing = be.orientation();
			s.color = be.color;
			s.linked = be.hasParent();
			s.ticks = viewerTicks(pt);
			s.parent = be.hasParent() ? new Vec3(-be.px, -be.py, -be.pz) : null;
			s.r = be.pRed;
			s.g = be.pGreen;
			s.b = be.pBlue;
			s.opacity = be.opacity;
			var p = Minecraft.getInstance().player;
			ItemStack head = p == null ? ItemStack.EMPTY : p.getItemBySlot(EquipmentSlot.HEAD);
			s.revealed = head.getItem() instanceof RevealerGear;
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState camera) {
			ps.pushPose();
			ps.translate(0.5F, 0.5F, 0.5F);
			if (s.charger) {
				// TileMagicWorkbenchChargerRenderer: the relay hangs from the top of its block, pointing down at the table
				ps.rotateDegrees(Axis.XP, -90.0F);
			} else {
				switch (s.facing) {
					case DOWN -> ps.rotateDegrees(Axis.XP, -90.0F);
					case UP -> ps.rotateDegrees(Axis.XP, 90.0F);
					case SOUTH -> ps.rotateDegrees(Axis.YP, 180.0F);
					case WEST -> ps.rotateDegrees(Axis.YP, 90.0F);
					case EAST -> ps.rotateDegrees(Axis.YP, -90.0F);
					default -> {
					}
				}
			}
			ps.rotateDegrees(Axis.XP, 180.0F);
			ps.rotateDegrees(Axis.ZP, 45.0F);
			if (!s.charger) {
				ps.pushPose();
				ps.scale(0.75F, 0.75F, 0.75F);
				ps.translate(0.0F, 0.0F, -0.16F);
				part(ps, c, relay(), "RingBase", RELAY_TEX, s.lightCoords, 0xFFFFFFFF, false);
				ps.popPose();
			}
			ps.pushPose();
			part(ps, c, relay(), "RingFloat", RELAY_TEX, s.lightCoords, 0xFFFFFFFF, false);
			ps.popPose();
			if (s.charger) {
				// four struts down to the worktable
				for (int a = 0; a < 4; a++) {
					ps.pushPose();
					ps.rotateDegrees(Axis.XP, 180.0F);
					ps.translate(0.0F, 0.0F, 0.5F);
					ps.rotateDegrees(Axis.ZP, 90.0F * a);
					part(ps, c, relay(), "Support", RELAY_TEX, s.lightCoords, 0xFFFFFFFF, false);
					ps.popPose();
				}
			}
			int argb = 0xFFFFFFFF;
			if (s.color >= 0) {
				int col = VisRelayBlockEntity.COLORS[s.color];
				int rr = Math.min(255, (col >> 16 & 255) * 255 / 200);
				int gg = Math.min(255, (col >> 8 & 255) * 255 / 200);
				int bb = Math.min(255, (col & 255) * 255 / 200);
				argb = 0xFF000000 | rr << 16 | gg << 8 | bb;
			}
			float scale = Mth.sin(s.ticks / 2.0F) * 0.05F + 0.95F;
			int j = (s.linked ? 50 : 0) + (int) (150.0F * scale);
			ps.pushPose();
			part(ps, c, relay(), "Crystal", RELAY_TEX, j, argb, false);
			ps.popPose();
			ps.popPose();
			if (s.parent != null) {
				this.beam(s, ps, c, camera);
			}
		}

		/** FXBeamPower: two crossed ribbons of beam1.png from the parent to this relay, plus a flare where it lands. */
		private void beam(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState camera) {
			Direction d2 = s.facing;
			double sx = s.parent.x + 0.5;
			double sy = s.parent.y + 0.5;
			double sz = s.parent.z + 0.5;
			double tx = 0.5 - d2.getStepX() * 0.05;
			double ty = 0.5 - d2.getStepY() * 0.05;
			double tz = 0.5 - d2.getStepZ() * 0.05;
			double xd = sx - tx;
			double yd = sy - ty;
			double zd = sz - tz;
			float length = (float) Math.sqrt(xd * xd + yd * yd + zd * zd);
			float yaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
			float pitch = (float) (Math.atan2(yd, Math.sqrt(xd * xd + zd * zd)) * 180.0 / Math.PI);
			float slide = s.ticks;
			float scroll = -slide * 0.2F - Mth.floor(-slide * 0.1F);
			float opmod = s.revealed ? 1.0F : 0.1F;
			float alpha = s.opacity * opmod;
			float w = 0.15F * 0.7F;
			for (int t = 0; t < 2; t++) {
				ps.pushPose();
				ps.translate(sx, sy, sz);
				ps.rotateDegrees(Axis.XP, 90.0F);
				ps.rotateDegrees(Axis.ZN, 180.0F + yaw);
				ps.rotateDegrees(Axis.XP, pitch);
				ps.rotateDegrees(Axis.YP, 90.0F * (t + 1));
				float v0 = -1.0F + scroll + t / 3.0F;
				c.submitCustomGeometry(ps, TVRenderTypes.glow(BEAM1, true, true), (pose, vc) -> {
					float start = v0;
					float end = v0 + length;
					while (start < end - 1.0E-4F) {
						float next = Math.min(end, Mth.floor(start) + 1.0F);
						float base = Mth.floor(start);
						float y0 = start - v0;
						float y1 = next - v0;
						vq(vc, pose, -w, y1, 1.0F, next - base, s, alpha);
						vq(vc, pose, -w, y0, 1.0F, start - base, s, alpha);
						vq(vc, pose, w, y0, 0.0F, start - base, s, alpha);
						vq(vc, pose, w, y1, 0.0F, next - base, s, alpha);
						start = next;
					}
				});
				ps.popPose();
			}
			int part = (int) s.ticks % 16;
			float u0 = part / 16.0F;
			float sc = 0.66F * s.opacity;
			ps.pushPose();
			ps.translate(tx, ty, tz);
			ps.rotate(camera.orientation);
			int a = Mth.clamp((int) (s.opacity * (s.revealed ? 1.0F : 0.2F) * 255), 0, 255);
			int rr = (int) (s.r * 255);
			int gg = (int) (s.g * 255);
			int bb = (int) (s.b * 255);
			c.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.PARTICLES, true, true), (pose, vc) -> {
				v(vc, pose, -sc, -sc, u0 + 0.0624375F, 0.3749375F, rr, gg, bb, a);
				v(vc, pose, sc, -sc, u0, 0.3749375F, rr, gg, bb, a);
				v(vc, pose, sc, sc, u0, 0.3125F, rr, gg, bb, a);
				v(vc, pose, -sc, sc, u0 + 0.0624375F, 0.3125F, rr, gg, bb, a);
			});
			ps.popPose();
		}

		private static void vq(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float vv, State s, float a) {
			vc.addVertex(pose, x, y, 0.0F).setColor(s.r, s.g, s.b, a).setUv(u, vv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(200)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
		}
	}

	// ------------------------------------------------------------------ stabilizer and transducer

	/** TileNodeStabilizerRenderer: the clamp, four pistons that extend while a node rests on top, and the bubble around the node. */
	public static class Stabilizer implements BlockEntityRenderer<NodeStabilizerBlockEntity, Stabilizer.State> {
		public static class State extends BlockEntityRenderState {
			int count;
			boolean advanced;
			float ticks;
		}

		public Stabilizer(BlockEntityRendererProvider.Context ctx) {
		}

		@Override
		public State createRenderState() {
			return new State();
		}

		@Override
		public void extractRenderState(NodeStabilizerBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
			BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
			s.count = be.count;
			s.advanced = be.advanced();
			s.ticks = viewerTicks(pt);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState camera) {
			draw(ps, c, s.lightCoords, s.count, s.advanced, s.ticks);
			if (s.count > 0) {
				float alpha = Mth.sin(s.ticks / 8.0F) * 0.1F + 0.5F;
				facingQuad(ps, c, camera.orientation, BUBBLE, 0.5F, 1.5F, 0.5F, 0.9F, s.count / 37.0F * alpha, 1, 0, s.advanced ? 16729156 : 16777215);
			}
		}

		static void draw(PoseStack ps, SubmitNodeCollector c, int light, int count, boolean advanced, float ticks) {
			ps.pushPose();
			ps.translate(0.5F, 0.0F, 0.5F);
			ps.rotateDegrees(Axis.XN, 90.0F);
			ps.pushPose();
			part(ps, c, stabilizer(), "lock", STAB_TEX, light, 0xFFFFFFFF, false);
			ps.popPose();
			for (int a = 0; a < 4; a++) {
				ps.pushPose();
				ps.rotateDegrees(Axis.ZP, 90 * a);
				ps.rotateDegrees(Axis.YP, 45.0F);
				ps.translate(0.0F, 0.0F, count / 100.0F);
				part(ps, c, stabilizer(), "piston", STAB_TEX, light, 0xFFFFFFFF, false);
				float scale = Mth.sin((ticks + a * 5) / 3.0F) * 0.1F + 0.9F;
				int j = 50 + (int) (170.0F * (count / 37.0F * scale));
				part(ps, c, stabilizer(), "piston", STAB_OVER, j, advanced ? 0xFFFF3333 : 0xFFFFFFFF, false);
				ps.popPose();
			}
			ps.popPose();
		}
	}

	/** TileNodeConverterRenderer: the stabilizer's clamp hung upside down, its glow green when idle, orange while energizing, red once done. */
	public static class Transducer implements BlockEntityRenderer<NodeTransducerBlockEntity, Transducer.State> {
		public static class State extends BlockEntityRenderState {
			int count;
			int status;
			float ticks;
		}

		public Transducer(BlockEntityRendererProvider.Context ctx) {
		}

		@Override
		public State createRenderState() {
			return new State();
		}

		@Override
		public void extractRenderState(NodeTransducerBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
			BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
			s.count = be.count;
			s.status = be.status;
			s.ticks = viewerTicks(pt);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState camera) {
			draw(ps, c, s.lightCoords, s.count, s.status, s.ticks);
		}

		static void draw(PoseStack ps, SubmitNodeCollector c, int light, int count, int status, float ticks) {
			float v = Math.min(50, Math.max(0, count)) / 137.0F;
			int glow = status == 2 ? 0xFFFF004C : status == 1 ? 0xFFFF9919 : 0xFF7FFF7F;
			ps.pushPose();
			ps.translate(0.5F, 1.0F, 0.5F);
			ps.rotateDegrees(Axis.XP, 90.0F);
			ps.pushPose();
			part(ps, c, stabilizer(), "lock", CONV_TEX, light, 0xFFFFFFFF, false);
			float scale = Mth.sin(ticks / 3.0F) * 0.1F + 0.9F;
			part(ps, c, stabilizer(), "lock", CONV_OVER, 50 + (int) (170.0F * (v * 2.5F * scale)), glow, false);
			ps.popPose();
			for (int a = 0; a < 4; a++) {
				ps.pushPose();
				ps.rotateDegrees(Axis.ZP, 90 * a);
				ps.rotateDegrees(Axis.YP, 45.0F);
				ps.translate(0.0F, 0.0F, v);
				part(ps, c, stabilizer(), "piston", CONV_TEX, light, 0xFFFFFFFF, false);
				float sc = Mth.sin((ticks + a * 5) / 3.0F) * 0.1F + 0.9F;
				part(ps, c, stabilizer(), "piston", CONV_OVER, 50 + (int) (170.0F * (v * 2.5F * sc)), glow, false);
				ps.popPose();
			}
			ps.popPose();
		}
	}

	// ------------------------------------------------------------------ energized node

	/** TileNodeEnergizedRenderer: the node's strips, always shown, wrapped in a crackling ring. */
	public static class EnergizedNode implements BlockEntityRenderer<EnergizedNodeBlockEntity, NodeRenderer.State> {
		private final NodeRenderer node;

		public EnergizedNode(BlockEntityRendererProvider.Context ctx) {
			this.node = new NodeRenderer(ctx);
		}

		@Override
		public NodeRenderer.State createRenderState() {
			return new NodeRenderer.State();
		}

		@Override
		public boolean shouldRenderOffScreen() {
			return true;
		}

		@Override
		public void extractRenderState(EnergizedNodeBlockEntity be, NodeRenderer.State s, float pt, Vec3 cam,
			ModelFeatureRenderer.CrumblingOverlay crumbling) {
			BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
			s.aspects = be.getAuraBase().copy();
			s.type = be.getNodeType();
			s.modifier = be.getNodeModifier();
			s.visible = true;
			s.depthIgnore = false;
			s.viewDistance = 64.0;
			s.x = be.getBlockPos().getX();
			s.distance = cam.distanceTo(Vec3.atCenterOf(be.getBlockPos()));
			var p = Minecraft.getInstance().player;
			s.ticks = p == null ? 0 : p.tickCount;
			s.drainFrom = null;
		}

		@Override
		public void submit(NodeRenderer.State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState camera) {
			this.node.submit(s, ps, c, camera);
			long nt = System.nanoTime();
			int frames = 16;
			int i = (int) ((nt / 40000000L + s.x) % frames);
			facingQuad(ps, c, camera.orientation, RING, 0.5F, 0.5F, 0.5F, 0.33F, 0.9F, frames, i, 0xFFFFFF);
		}
	}

	// ------------------------------------------------------------------ wand pedestal and focal manipulator

	/** TileWandPedestalRenderer: the wand turns and bobs over the pedestal, with a wispy stream from the node it is drinking from. */
	public static class WandPedestal implements BlockEntityRenderer<WandPedestalBlockEntity, WandPedestal.State> {
		public static class State extends BlockEntityRenderState {
			final ItemStackRenderState item = new ItemStackRenderState();
			float ticks;
			boolean draining;
			Vec3 drain;
			int color;
		}

		private final ItemModelResolver resolver;

		public WandPedestal(BlockEntityRendererProvider.Context ctx) {
			this.resolver = ctx.itemModelResolver();
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
		public void extractRenderState(WandPedestalBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
			BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
			ItemStack stack = be.getItem();
			if (stack.isEmpty()) {
				s.item.clear();
			} else {
				this.resolver.updateForTopItem(s.item, stack.copyWithCount(1), ItemDisplayContext.GROUND, be.getLevel(), null, 0);
			}
			s.ticks = viewerTicks(pt);
			s.draining = be.draining && !stack.isEmpty();
			s.drain = Vec3.atLowerCornerOf(be.drain.subtract(be.getBlockPos()));
			s.color = be.drainColor;
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState camera) {
			if (s.item.isEmpty()) {
				return;
			}
			float h = Mth.sin(s.ticks % 32767.0F / 16.0F) * 0.05F;
			ps.pushPose();
			ps.translate(0.5F, 1.15F + h, 0.5F);
			ps.rotateDegrees(Axis.YP, s.ticks % 360.0F);
			s.item.submit(ps, c, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			ps.popPose();
			if (s.draining) {
				FloatyLine.draw(ps, c, new Vec3(0.5, 1.65 - h * 2.0F, 0.5), s.drain.add(0.5, 0.5, 0.5), s.color, -0.02F, Math.min(s.ticks, 10.0F) / 10.0F);
			}
		}
	}

	/** TileFocalManipulatorRenderer: the focus turning above the table. */
	public static class FocalManipulator implements BlockEntityRenderer<FocalManipulatorBlockEntity, FocalManipulator.State> {
		public static class State extends BlockEntityRenderState {
			final ItemStackRenderState item = new ItemStackRenderState();
			float ticks;
		}

		private final ItemModelResolver resolver;

		public FocalManipulator(BlockEntityRendererProvider.Context ctx) {
			this.resolver = ctx.itemModelResolver();
		}

		@Override
		public State createRenderState() {
			return new State();
		}

		@Override
		public void extractRenderState(FocalManipulatorBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
			BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
			ItemStack stack = be.getItem(0);
			if (stack.isEmpty()) {
				s.item.clear();
			} else {
				this.resolver.updateForTopItem(s.item, stack, ItemDisplayContext.GROUND, be.getLevel(), null, 0);
			}
			s.ticks = viewerTicks(pt);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState camera) {
			if (s.item.isEmpty()) {
				return;
			}
			float bob = Mth.sin(s.ticks / 14.0F) * 0.2F + 0.2F;
			ps.pushPose();
			ps.translate(0.5F, 1.0F + bob * 0.5F + 0.1F, 0.5F);
			ps.rotateDegrees(Axis.YP, s.ticks % 360.0F);
			s.item.submit(ps, c, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			ps.popPose();
		}
	}

	/** UtilsFX.drawFloatyLine between two points in block-local space, as the node drain beam uses. */
	static final class FloatyLine {
		static void draw(PoseStack ps, SubmitNodeCollector c, Vec3 from, Vec3 to, int color, float speed, float progress) {
			double dx = from.x - to.x;
			double dy = from.y - to.y;
			double dz = from.z - to.z;
			float time = (float) (System.nanoTime() / 30000000L);
			float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
			float length = Math.round(dist) * 8.0F;
			float width = 0.15F;
			List<double[]> pts = new ArrayList<>();
			for (int i = 0; i <= length * progress; i++) {
				float f2 = i / length;
				float f3 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);
				double ox = dx + Mth.sin((float) ((to.z % 16.0 + dist * (1.0F - f2) * 8.0F - time % 32767.0F / 5.0F) / 4.0)) * 0.5F * f3;
				double oy = dy + Mth.sin((float) ((to.x % 16.0 + dist * (1.0F - f2) * 8.0F - time % 32767.0F / 5.0F) / 3.0)) * 0.5F * f3;
				double oz = dz + Mth.sin((float) ((to.y % 16.0 + dist * (1.0F - f2) * 8.0F - time % 32767.0F / 5.0F) / 2.0)) * 0.5F * f3;
				float f13 = (1.0F - f2) * dist - time * speed;
				pts.add(new double[]{to.x + ox * f2, to.y + oy * f2, to.z + oz * f2, f13, f3});
			}
			if (pts.size() < 2) {
				return;
			}
			int r = color >> 16 & 255;
			int g = color >> 8 & 255;
			int b = color & 255;
			c.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.WISPY, true, true), (pose, vc) -> {
				for (int pass = 0; pass < 2; pass++) {
					for (int i = 0; i < pts.size() - 1; i++) {
						double[] p0 = pts.get(i);
						double[] p1 = pts.get(i + 1);
						float ox = pass == 1 ? width : 0;
						float oy = pass == 0 ? width : 0;
						int a0 = (int) (p0[4] * 255);
						int a1 = (int) (p1[4] * 255);
						q(vc, pose, (float) (p0[0] - ox), (float) (p0[1] - oy), (float) p0[2], (float) p0[3], 1.0F, r, g, b, a0);
						q(vc, pose, (float) (p0[0] + ox), (float) (p0[1] + oy), (float) p0[2], (float) p0[3], 0.0F, r, g, b, a0);
						q(vc, pose, (float) (p1[0] + ox), (float) (p1[1] + oy), (float) p1[2], (float) p1[3], 0.0F, r, g, b, a1);
						q(vc, pose, (float) (p1[0] - ox), (float) (p1[1] - oy), (float) p1[2], (float) p1[3], 1.0F, r, g, b, a1);
					}
				}
			});
		}

		private static void q(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
			vc.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
		}
	}

	/** The relays', stabilizers' and transducer's inventory icons: their block entity renderers, idle, in the item's block space. */
	public record DeviceItem(String kind) implements net.minecraft.client.renderer.special.NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector c, int light, int overlay, boolean glint, int outline) {
			float ticks = viewerTicks(0.0F);
			switch (this.kind) {
				case "relay", "charger" -> {
					Relay.State s = new Relay.State();
					s.charger = this.kind.equals("charger");
					s.facing = Direction.UP;
					s.lightCoords = light;
					s.ticks = ticks;
					new Relay<VisRelayBlockEntity>(null).submit(s, ps, c, null);
				}
				case "stabilizer", "advanced_stabilizer" -> Stabilizer.draw(ps, c, light, 0, this.kind.startsWith("advanced"), ticks);
				default -> Transducer.draw(ps, c, light, 0, 0, ticks);
			}
		}

		@Override
		public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new org.joml.Vector3f(i & 1, i >> 1 & 1, i >> 2 & 1));
			}
		}
	}

	public record DeviceUnbaked(String kind) implements net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<Void> {
		public static final com.mojang.serialization.MapCodec<DeviceUnbaked> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(
			i -> i.group(com.mojang.serialization.Codec.STRING.fieldOf("kind").forGetter(DeviceUnbaked::kind)).apply(i, DeviceUnbaked::new));

		@Override
		public net.minecraft.client.renderer.special.SpecialModelRenderer<Void> bake(
			net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext ctx) {
			return new DeviceItem(this.kind);
		}

		@Override
		public com.mojang.serialization.MapCodec<DeviceUnbaked> type() {
			return CODEC;
		}
	}

	private VisDeviceRenderers() {
	}
}
