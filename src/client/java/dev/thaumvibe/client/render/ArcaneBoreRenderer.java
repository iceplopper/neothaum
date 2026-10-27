package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.tiles.ArcaneBoreBlockEntity;
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
 * Port of TC4 TileArcaneBoreRenderer:
 * Renders the rotatable turret, nozzle, and focus emitter assembly.
 */
public class ArcaneBoreRenderer implements BlockEntityRenderer<ArcaneBoreBlockEntity, ArcaneBoreRenderer.State> {
	private static final Identifier TEXTURE = Thaumvibe.id("textures/models/bore.png");

	private static ModelPart boreBasePart;
	private static ModelPart boreNozzlePart;
	private static ModelPart boreEmitPart;

	public static class State extends BlockEntityRenderState {
		Direction orientation = Direction.NORTH;
		Direction baseOrientation = Direction.DOWN;
		int rotX;
		int rotZ;
		int topRotation;
		boolean hasFocus;
	}

	public ArcaneBoreRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static void init() {
		if (boreBasePart == null) {
			MeshDefinition mesh = new MeshDefinition();
			var root = mesh.getRoot();

			// Texture size: 128, 64
			var base = root.addOrReplaceChild("base", CubeListBuilder.create()
				.texOffs(0, 32).addBox(-6.0F, 0.0F, -6.0F, 12, 2, 12)
				.texOffs(0, 0).addBox(-2.0F, 2.0F, -5.5F, 4, 8, 1)
				.texOffs(0, 0).addBox(-2.0F, 2.0F, 4.5F, 4, 8, 1)
				.texOffs(0, 48).addBox(-1.0F, 7.0F, -6.0F, 2, 2, 12),
				PartPose.ZERO);

			var nozzle = root.addOrReplaceChild("nozzle", CubeListBuilder.create()
				.texOffs(30, 14).addBox(4.0F, 5.5F, -2.5F, 4, 5, 5)
				.texOffs(0, 14).addBox(-2.0F, 4.0F, -4.0F, 6, 8, 8),
				PartPose.ZERO);

			var emit = root.addOrReplaceChild("emit", CubeListBuilder.create()
				.texOffs(66, 0).addBox(-2.0F, 12.0F, -2.0F, 4, 4, 4)
				.texOffs(56, 16).addBox(-2.0F, 8.0F, -2.0F, 4, 1, 4)
				.texOffs(56, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 1, 4)
				.texOffs(56, 24).addBox(-3.0F, 4.0F, -3.0F, 6, 1, 6)
				.texOffs(56, 0).addBox(-1.0F, 1.0F, -1.0F, 2, 11, 2),
				PartPose.ZERO);

			ModelPart baked = LayerDefinition.create(mesh, 128, 64).bakeRoot();
			boreBasePart = baked.getChild("base");
			boreNozzlePart = baked.getChild("nozzle");
			boreEmitPart = baked.getChild("emit");
		}
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ArcaneBoreBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.orientation = be.orientation;
		s.baseOrientation = be.baseOrientation;
		s.rotX = be.rotX;
		s.rotZ = be.rotZ;
		s.topRotation = be.topRotation;
		s.hasFocus = be.hasFocus;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		init();
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);

		// Turret yaw rotation
		ps.rotateDegrees(Axis.YP, s.rotX);

		// Base mount
		ps.pushPose();
		if (s.baseOrientation == Direction.UP) {
			ps.rotateDegrees(Axis.ZP, 180.0F);
		}
		ps.translate(0.0F, -0.5F, 0.0F);
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			boreBasePart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();

		// Pitch rotation for nozzle
		ps.pushPose();
		ps.rotateDegrees(Axis.ZP, s.rotZ);
		ps.rotateDegrees(Axis.ZP, 90.0F);
		ps.translate(0.0F, -0.5F, 0.0F);
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			boreNozzlePart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();

		// Emitter
		ps.pushPose();
		ps.rotateDegrees(Axis.YP, s.topRotation);
		ps.translate(0.0F, 0.5F, 0.0F);
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			boreEmitPart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();

		ps.popPose();
	}
}
