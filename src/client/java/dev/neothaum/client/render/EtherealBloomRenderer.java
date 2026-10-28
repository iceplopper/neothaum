package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.tiles.EtherealBloomBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Port of TileEtherealBloomRenderer: the bloom has no block model of its own. Over its first hundred ticks a glowing node strip rises, four
 * stalks and two rings of leaves unfold, and a small crystal settles at the top.
 */
public class EtherealBloomRenderer implements BlockEntityRenderer<EtherealBloomBlockEntity, EtherealBloomRenderer.State> {
	private static final Identifier CRYSTAL = NeoThaum.id("textures/models/crystalcapacitor.png");
	private static final Identifier LEAVES = NeoThaum.id("textures/blocks/purifier_leaves.png");
	private static final Identifier STALK = NeoThaum.id("textures/blocks/purifier_stalk.png");
	/** renderQuadCenteredFromIcon's brightness of 200: block light 12.5, no sky light. */
	private static final int BRIGHTNESS = 200;
	private static ModelPart cube;

	public static class State extends BlockEntityRenderState {
		float growth;
		int counter;
	}

	public EtherealBloomRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	/** ModelCube(): a 16-pixel cube on a 64x32 sheet, spanning the block from 0 to 1. */
	private static ModelPart cube() {
		if (cube == null) {
			MeshDefinition mesh = new MeshDefinition();
			mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16),
				PartPose.offset(8.0F, 8.0F, 8.0F));
			cube = LayerDefinition.create(mesh, 64, 32).bakeRoot();
		}
		return cube;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EtherealBloomBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.growth = be.growthCounter + pt;
		s.counter = be.counter;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		float rc1 = Math.min(s.growth, 100.0F);
		float rc2 = Math.min(s.growth, 50.0F);
		float rc3 = Math.max(0.0F, Math.min(s.growth - 33.0F, 33.0F));
		float rc4 = Math.max(0.0F, Math.min(s.growth - 66.0F, 33.0F));
		float scale1 = rc1 / 100.0F;
		float scale2 = rc2 / 60.0F + 0.1666666F;
		float scale3 = rc3 / 33.0F;
		float scale4 = rc4 / 33.0F * 0.7F;

		// the node glow, frame counter % 32 of strip 6, rising with the stalks
		int frame = Math.floorMod(s.counter, 32);
		ps.pushPose();
		ps.translate(0.5F, scale1, 0.5F);
		ps.rotate(new Quaternionf(camera.orientation));
		float f2 = frame / 32.0F, f3 = (frame + 1) / 32.0F, f4 = 6 / 32.0F, f5 = 7 / 32.0F;
		float sc = scale1;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.NODES, true, true), (pose, vc) -> {
			glow(vc, pose, -sc, -sc, f3, f5);
			glow(vc, pose, -sc, sc, f3, f4);
			glow(vc, pose, sc, sc, f2, f4);
			glow(vc, pose, sc, -sc, f2, f5);
		});
		ps.popPose();

		if (scale4 > 0.0F) {
			ps.pushPose();
			ps.translate(0.5F - scale4 / 8.0F, scale1 - scale4 / 6.0F, 0.5F - scale4 / 8.0F);
			ps.scale(scale4 / 4.0F, scale4 / 3.0F, scale4 / 4.0F);
			// still under the strip's additive blending in TC4
			collector.submitCustomGeometry(ps, TVRenderTypes.glow(CRYSTAL, true, true), (pose, vc) -> {
				PoseStack local = new PoseStack();
				local.last().set(pose);
				cube().render(local, vc, BRIGHTNESS, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			});
			ps.popPose();
		}

		// the lower leaves
		ps.pushPose();
		ps.translate(0.5F, 0.25F, 0.5F);
		ps.rotateDegrees(Axis.XP, 180.0F);
		for (int a = 0; a < 4; a++) {
			ps.pushPose();
			ps.scale(scale3, scale1, scale3);
			ps.rotateDegrees(Axis.YP, 90 * a);
			quad(ps, collector, LEAVES);
			ps.popPose();
		}
		ps.popPose();

		// the upper leaves, turned 45 degrees
		ps.pushPose();
		ps.translate(0.5F, 0.6F, 0.5F);
		ps.rotateDegrees(Axis.YP, 45.0F);
		ps.rotateDegrees(Axis.XP, 180.0F);
		for (int a = 0; a < 4; a++) {
			ps.pushPose();
			ps.scale(scale4, scale1 * 0.7F, scale4);
			ps.rotateDegrees(Axis.YP, 90 * a);
			quad(ps, collector, LEAVES);
			ps.popPose();
		}
		ps.popPose();

		// the stalks
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);
		ps.rotateDegrees(Axis.XP, 180.0F);
		for (int a = 0; a < 4; a++) {
			ps.pushPose();
			ps.translate(0.0F, (1.0F - scale1) / 2.0F, 0.0F);
			ps.scale(scale2, scale1, scale2);
			ps.rotateDegrees(Axis.YP, 90 * a);
			quad(ps, collector, STALK);
			ps.popPose();
		}
		ps.popPose();
	}

	/** UtilsFX.renderQuadCenteredFromIcon: a unit quad centred on the origin, drawn from both sides as TC4 had culling off. */
	private static void quad(PoseStack ps, SubmitNodeCollector collector, Identifier texture) {
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(texture), (pose, vc) -> {
			leaf(vc, pose, -0.5F, 0.5F, 0.0F, 1.0F);
			leaf(vc, pose, 0.5F, 0.5F, 1.0F, 1.0F);
			leaf(vc, pose, 0.5F, -0.5F, 1.0F, 0.0F);
			leaf(vc, pose, -0.5F, -0.5F, 0.0F, 0.0F);
			leaf(vc, pose, -0.5F, -0.5F, 0.0F, 0.0F);
			leaf(vc, pose, 0.5F, -0.5F, 1.0F, 0.0F);
			leaf(vc, pose, 0.5F, 0.5F, 1.0F, 1.0F);
			leaf(vc, pose, -0.5F, 0.5F, 0.0F, 1.0F);
		});
	}

	private static void leaf(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v) {
		vc.addVertex(pose, x, y, 0.0F).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(BRIGHTNESS)
			.setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	private static void glow(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v) {
		// colour 11197951 (0xAADDFF) at full alpha
		vc.addVertex(pose, x, y, 0.0F).setColor(0xAA, 0xDD, 0xFF, 255).setUv(u, v).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}
}
