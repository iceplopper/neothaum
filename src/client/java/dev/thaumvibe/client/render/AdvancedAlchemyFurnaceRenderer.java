package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.tiles.AdvancedAlchemyFurnaceBlockEntity;
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

/**
 * Port of TC4 TileAlchemyFurnaceAdvancedRenderer:
 * Renders the 3D OBJ model of the Advanced Alchemical Furnace (adv_alch_furnace.obj).
 * Switches textures dynamically based on heat (> 100 on) and stored essentia (> 0 on).
 */
public class AdvancedAlchemyFurnaceRenderer implements BlockEntityRenderer<AdvancedAlchemyFurnaceBlockEntity, AdvancedAlchemyFurnaceRenderer.State> {
	private static final Identifier MODEL = Thaumvibe.id("textures/models/adv_alch_furnace.obj");
	private static final Identifier TEX_BASE_OFF = Thaumvibe.id("textures/models/alch_furnace.png");
	private static final Identifier TEX_BASE_ON = Thaumvibe.id("textures/models/alch_furnace_on.png");
	private static final Identifier TEX_TANK_OFF = Thaumvibe.id("textures/models/alch_furnace_tank.png");
	private static final Identifier TEX_TANK_ON = Thaumvibe.id("textures/models/alch_furnace_tank_on.png");

	private static ObjModel model;

	public static class State extends BlockEntityRenderState {
		int heat;
		int vis;
	}

	public AdvancedAlchemyFurnaceRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static ObjModel model() {
		if (model == null) {
			model = ObjModel.load(MODEL);
		}
		return model;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(AdvancedAlchemyFurnaceBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.heat = be.heat;
		s.vis = be.vis;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		Identifier baseTex = s.heat > 100 ? TEX_BASE_ON : TEX_BASE_OFF;
		Identifier tankTex = s.vis > 0 ? TEX_TANK_ON : TEX_TANK_OFF;

		ps.pushPose();
		ps.translate(0.5F, 0.0F, 0.5F);
		ps.rotateDegrees(Axis.XP, -90.0F);

		// Render Base
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(baseTex), (pose, vc) -> {
			model().renderPart("Base", pose, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});

		// Render 4 rotated Tanks
		for (int a = 0; a < 4; a++) {
			ps.pushPose();
			ps.rotateDegrees(Axis.ZP, 90.0F * a);
			collector.submitCustomGeometry(ps, RenderTypes.entityCutout(tankTex), (pose, vc) -> {
				model().renderPart("Tank", pose, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			});
			ps.popPose();
		}

		ps.popPose();
	}
}
