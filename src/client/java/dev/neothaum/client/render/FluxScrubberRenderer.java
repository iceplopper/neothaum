package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.blocks.FluxScrubberBlock;
import dev.neothaum.tiles.FluxScrubberBlockEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TC4 TileFluxScrubberRenderer:
 * Renders the directional obelisk cap OBJ model and bobbing animated tip.
 */
public class FluxScrubberRenderer implements BlockEntityRenderer<FluxScrubberBlockEntity, FluxScrubberRenderer.State> {
	private static final Identifier MODEL = NeoThaum.id("textures/models/obelisk_cap.obj");
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/fluxscrubber.png");
	private static ObjModel model;

	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.DOWN;
		int count = 0;
		long gameTime = 0;
	}

	public FluxScrubberRenderer(BlockEntityRendererProvider.Context ctx) {
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
	public void extractRenderState(FluxScrubberBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.getBlockState().hasProperty(FluxScrubberBlock.FACING)
			? be.getBlockState().getValue(FluxScrubberBlock.FACING)
			: be.facing;
		s.count = be.count;
		if (be.getLevel() != null) {
			s.gameTime = be.getLevel().getGameTime();
		}
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);

		switch (s.facing) {
			case DOWN -> ps.rotateDegrees(Axis.XP, -90.0F);
			case UP -> ps.rotateDegrees(Axis.XP, 90.0F);
			case SOUTH -> ps.rotateDegrees(Axis.YP, 180.0F);
			case WEST -> ps.rotateDegrees(Axis.YP, 90.0F);
			case EAST -> ps.rotateDegrees(Axis.YP, -90.0F);
			default -> {} // NORTH
		}
		ps.translate(0.0F, 0.0F, -0.5F);

		// Render stationary Cap
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			model().renderPart("Cap", pose, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});

		// Render bobbing Tip
		float bob = Mth.sin((s.gameTime + s.count) / 8.0F) * 0.075F + 0.075F;
		ps.pushPose();
		ps.translate(0.0F, 0.0F, -bob);
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			model().renderPart("Tip", pose, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();

		ps.popPose();
	}
}
