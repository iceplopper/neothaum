package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.tiles.InfusionPillarBlockEntity;
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
import com.mojang.math.Axis;

/** Port of TileInfusionPillarRenderer: the two-block OBJ pillar, turned to face the altar from its corner. */
public class InfusionPillarRenderer implements BlockEntityRenderer<InfusionPillarBlockEntity, InfusionPillarRenderer.State> {
	private static final Identifier MODEL = NeoThaum.id("textures/models/pillar.obj");
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/pillar.png");
	private static ObjModel model;

	public static class State extends BlockEntityRenderState {
		int orientation;
	}

	public InfusionPillarRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	/** The pillar stands two blocks tall. */
	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(InfusionPillarBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.orientation = be.orientation();
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (model == null) {
			model = ObjModel.load(MODEL);
		}
		int light = s.lightCoords;
		ps.pushPose();
		ps.translate(0.5F, 0.0F, 0.5F);
		ps.rotateDegrees(Axis.XN, 90.0F);
		switch (s.orientation) {
			case 3 -> ps.rotateDegrees(Axis.ZP, 90.0F);
			case 4 -> ps.rotateDegrees(Axis.ZP, 270.0F);
			case 5 -> ps.rotateDegrees(Axis.ZP, 180.0F);
			default -> {
			}
		}
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> model.render(pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
		ps.popPose();
	}
}
