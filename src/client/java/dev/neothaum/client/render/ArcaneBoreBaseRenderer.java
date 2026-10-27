package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.blocks.ArcaneBoreBaseBlock;
import dev.neothaum.tiles.ArcaneBoreBaseBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TC4 TileArcaneBoreBaseRenderer:
 * Renders the wooden bore base with pillars and an orientation-aligned ejection nozzle.
 */
public class ArcaneBoreBaseRenderer implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity, ArcaneBoreBaseRenderer.State> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/bore.png");

	private static ModelPart basePart;
	private static ModelPart nozzlePart;

	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
	}

	public ArcaneBoreBaseRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static void init() {
		if (basePart == null) {
			MeshDefinition mesh = new MeshDefinition();
			var root = mesh.getRoot();

			// Texture size: 128, 64
			var base = root.addOrReplaceChild("base", CubeListBuilder.create()
				.texOffs(64, 24).addBox(-8.0F, 0.0F, -8.0F, 16, 2, 16)
				.texOffs(64, 24).addBox(-8.0F, 14.0F, -8.0F, 16, 2, 16)
				.texOffs(84, 42).addBox(-2.5F, 2.0F, -2.5F, 5, 12, 5)
				.texOffs(64, 42).addBox(-7.0F, 2.0F, -7.0F, 4, 12, 4)
				.texOffs(64, 42).addBox(-7.0F, 2.0F, 3.0F, 4, 12, 4)
				.texOffs(64, 42).addBox(3.0F, 2.0F, 3.0F, 4, 12, 4)
				.texOffs(64, 42).addBox(3.0F, 2.0F, -7.0F, 4, 12, 4),
				PartPose.ZERO);

			var nozzle = root.addOrReplaceChild("nozzle", CubeListBuilder.create()
				.texOffs(106, 42).addBox(2.5F, -2.0F, -2.0F, 5, 4, 4)
				.texOffs(106, 51).addBox(7.0F, -2.5F, -2.5F, 1, 5, 5),
				PartPose.offset(0.0F, 8.0F, 0.0F));

			ModelPart baked = LayerDefinition.create(mesh, 128, 64).bakeRoot();
			basePart = baked.getChild("base");
			nozzlePart = baked.getChild("nozzle");
		}
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ArcaneBoreBaseBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.getFacing();
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		init();
		ps.pushPose();
		ps.translate(0.5F, 0.0F, 0.5F);

		// Render Base
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			basePart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});

		// Render Nozzle rotated to facing
		ps.pushPose();
		float yaw = switch (s.facing) {
			case NORTH -> 90.0F;
			case SOUTH -> 270.0F;
			case WEST -> 180.0F;
			case EAST -> 0.0F;
			default -> 0.0F;
		};
		ps.rotateDegrees(Axis.YP, yaw);

		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			nozzlePart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();

		ps.popPose();
	}
}
