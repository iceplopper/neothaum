package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.tiles.eldritch.EldritchCapBlockEntity;
import dev.neothaum.tiles.eldritch.EldritchObeliskBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TileEldritchObeliskRenderer: a three-block column of the void (TC4's copy of the end portal effect, here the end portal shader fed
 * TC4's tunnel and particle field textures) behind translucent rune-cut sides, with an obsidian cap at either end, bobbing gently above its
 * base.
 */
public class EldritchObeliskRenderer implements BlockEntityRenderer<EldritchObeliskBlockEntity, EldritchObeliskRenderer.State> {
	static final Identifier CAP_MODEL = NeoThaum.id("textures/models/obelisk_cap.obj");
	private static final Identifier SIDE = NeoThaum.id("textures/models/obelisk_side.png");
	static final Identifier CAP = NeoThaum.id("textures/models/obelisk_cap.png");
	static final Identifier CAP_ALTAR = NeoThaum.id("textures/models/obelisk_cap_altar.png");
	private static final RenderType VOID = RenderType.create("neothaum_obelisk_void",
		RenderSetup.builder(RenderPipelines.END_PORTAL).withTexture("Sampler0", NeoThaum.id("textures/misc/tunnel.png"))
			.withTexture("Sampler1", NeoThaum.id("textures/misc/particlefield.png")).createRenderSetup());
	private static ObjModel model;

	public static class State extends BlockEntityRenderState {
		float bob;
	}

	public EldritchObeliskRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	static ObjModel model() {
		if (model == null) {
			model = ObjModel.load(CAP_MODEL);
		}
		return model;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EldritchObeliskBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		var viewer = Minecraft.getInstance().getCameraEntity();
		float count = (viewer == null ? 0 : viewer.tickCount) + pt;
		s.bob = Mth.sin(count / 10.0F) * 0.1F + 0.1F;
	}

	/** getRenderBoundingBox: the column reaches five blocks up, past the base block's own box. */
	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		draw(ps, collector, s.bob, s.lightCoords);
	}

	static void draw(PoseStack ps, SubmitNodeCollector collector, float bob, int light) {
		// the void, just inside each face
		ps.pushPose();
		ps.translate(0.0F, 1.0F + bob, 0.0F);
		collector.submitCustomGeometry(ps, VOID, (pose, vc) -> {
			float lo = 0.01F, hi = 0.99F, h = 3.0F;
			// north (-z), south (+z), west (-x), east (+x), each facing outwards
			plane(vc, pose, 1, 0, lo, 0, 0, lo, 0, h, lo, 1, h, lo);
			plane(vc, pose, 0, 0, hi, 1, 0, hi, 1, h, hi, 0, h, hi);
			plane(vc, pose, lo, 0, 0, lo, 0, 1, lo, h, 1, lo, h, 0);
			plane(vc, pose, hi, 0, 1, hi, 0, 0, hi, h, 0, hi, h, 1);
		});
		ps.popPose();

		// the four rune-cut sides
		ps.pushPose();
		ps.translate(0.5F, 1.0F + bob, 0.5F);
		for (int a = 0; a < 4; a++) {
			ps.pushPose();
			ps.rotateDegrees(Axis.YP, a * 90.0F);
			ps.translate(0.0F, 0.0F, -0.5F);
			collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(SIDE), (pose, vc) -> {
				side(vc, pose, -0.5F, 3.0F, 0.0F, 1.0F, light);
				side(vc, pose, 0.5F, 3.0F, 1.0F, 1.0F, light);
				side(vc, pose, 0.5F, 0.0F, 1.0F, 0.0F, light);
				side(vc, pose, -0.5F, 0.0F, 0.0F, 0.0F, light);
			});
			ps.popPose();
		}
		ps.popPose();

		// the lower cap points down at the altar, the upper one up at the sky
		ps.pushPose();
		ps.translate(0.5F, 1.0F + bob, 0.5F);
		ps.rotateDegrees(Axis.XP, 90.0F);
		cap(ps, collector, CAP, light);
		ps.popPose();
		ps.pushPose();
		ps.translate(0.5F, 4.0F + bob, 0.5F);
		ps.rotateDegrees(Axis.XP, -90.0F);
		cap(ps, collector, CAP, light);
		ps.popPose();
	}

	/** One "Cap" from obelisk_cap.obj, in whichever texture. */
	static void cap(PoseStack ps, SubmitNodeCollector collector, Identifier texture, int light) {
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(texture),
			(pose, vc) -> model().renderPart("Cap", pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
	}

	private static void plane(VertexConsumer vc, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float... xyz) {
		for (int i = 0; i < 12; i += 3) {
			vc.addVertex(pose, xyz[i], xyz[i + 1], xyz[i + 2]);
		}
	}

	private static void side(VertexConsumer vc, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float x, float y, float u, float v, int light) {
		vc.addVertex(pose, x, y, 0.0F).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
			.setNormal(pose, 0.0F, 0.0F, -1.0F);
	}

	/** TileEldritchCapRenderer for the caps on the ring's pillars: the cap stands upright on its block. */
	public static class CapRenderer implements BlockEntityRenderer<EldritchCapBlockEntity, BlockEntityRenderState> {
		public CapRenderer(BlockEntityRendererProvider.Context ctx) {
		}

		@Override
		public BlockEntityRenderState createRenderState() {
			return new BlockEntityRenderState();
		}

		@Override
		public void submit(BlockEntityRenderState s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
			ps.pushPose();
			ps.translate(0.5F, 0.0F, 0.5F);
			ps.rotateDegrees(Axis.XP, -90.0F);
			cap(ps, collector, CAP, s.lightCoords);
			ps.popPose();
		}
	}
}
