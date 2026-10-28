package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.blocks.eldritch.CrustedOpeningBlock;
import dev.neothaum.tiles.eldritch.CrustedOpeningBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** TileEldritchCrabSpawnerRenderer: the crab vent (crabvent.obj) on the crusted opening's open face. */
public class CrustedOpeningRenderer implements BlockEntityRenderer<CrustedOpeningBlockEntity, CrustedOpeningRenderer.State> {
	private static final Identifier MODEL = NeoThaum.id("textures/models/crabvent.obj");
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/crabvent.png");
	private static ObjModel model;

	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.UP;
	}

	public CrustedOpeningRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CrustedOpeningBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.getBlockState().getValue(CrustedOpeningBlock.FACING);
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (model == null) {
			model = ObjModel.load(MODEL);
		}
		ps.pushPose();
		ps.translate(0.5, 0.5, 0.5);
		switch (s.facing) {
			case DOWN -> ps.rotateDegrees(Axis.XP, 90.0F);
			case UP -> ps.rotateDegrees(Axis.XP, -90.0F);
			case NORTH -> ps.rotateDegrees(Axis.YP, 180.0F);
			case WEST -> ps.rotateDegrees(Axis.YP, -90.0F);
			case EAST -> ps.rotateDegrees(Axis.YP, 90.0F);
			default -> {
			}
		}
		int light = s.lightCoords;
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE),
			(pose, vc) -> model.render(pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
		ps.popPose();
	}
}
