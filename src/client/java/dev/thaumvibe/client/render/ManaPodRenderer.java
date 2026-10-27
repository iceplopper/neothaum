package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.blocks.ManaPodBlock;
import dev.thaumvibe.tiles.ManaPodBlockEntity;
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

/**
 * Port of TC4 TileManaPodRenderer:
 * Renders the hanging stem, glowing pulsing inner core, and translucent colored outer pod.
 */
public class ManaPodRenderer implements BlockEntityRenderer<ManaPodBlockEntity, ManaPodRenderer.State> {
	private static final Identifier TEX0 = Thaumvibe.id("textures/models/manapod_0.png");
	private static final Identifier TEX2 = Thaumvibe.id("textures/models/manapod_2.png");

	private static ModelPart stemPart;
	private static ModelPart pod0Part;
	private static ModelPart pod2Part;

	public static class State extends BlockEntityRenderState {
		int age = 0;
		int color = 0x259D75;
		int hashCode = 0;
		long gameTime = 0;
	}

	public ManaPodRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static void init() {
		if (stemPart == null) {
			MeshDefinition mesh = new MeshDefinition();
			var root = mesh.getRoot();

			// Texture size: 32, 32
			root.addOrReplaceChild("stem", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
				PartPose.ZERO);

			root.addOrReplaceChild("pod0", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 5, 4),
				PartPose.ZERO);

			root.addOrReplaceChild("pod2", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.5F, 0.0F, -3.5F, 7, 9, 7),
				PartPose.ZERO);

			ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
			stemPart = baked.getChild("stem");
			pod0Part = baked.getChild("pod0");
			pod2Part = baked.getChild("pod2");
		}
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ManaPodBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.age = be.getBlockState().getValue(ManaPodBlock.AGE);
		Aspect a = be.getAspect();
		s.color = a != null ? a.getColor() : 0x259D75;
		s.hashCode = be.getBlockPos().hashCode();
		if (be.getLevel() != null) {
			s.gameTime = be.getLevel().getGameTime();
		}
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		init();
		ps.pushPose();
		ps.translate(0.5F, 1.0F, 0.5F);
		ps.rotateDegrees(Axis.XP, 180.0F);

		// Hanging stem from ceiling
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEX0), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			stemPart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFF4A3525);
		});

		int meta = s.age;
		if (meta > 1) {
			float br = 0.14509805F;
			float bg = 0.6156863F;
			float bb = 0.45882353F;
			float ar = ((s.color >> 16) & 0xFF) / 255.0F;
			float ag = ((s.color >> 8) & 0xFF) / 255.0F;
			float ab = (s.color & 0xFF) / 255.0F;

			float fr, fg, fb;
			if (meta == 7) {
				fr = ar;
				fg = ag;
				fb = ab;
			} else {
				float m = meta - 2;
				fr = (br + ar * m) / (m + 1.0F);
				fg = (bg + ag * m) / (m + 1.0F);
				fb = (bb + ab * m) / (m + 1.0F);
			}

			int podColor = ((int)(0.9F * 255) << 24) | ((int)(fr * 255) << 16) | ((int)(fg * 255) << 8) | (int)(fb * 255);

			// Pulsing inner core at meta > 2
			if (meta > 2) {
				float scale = (float) Math.sin((s.gameTime + s.hashCode % 100) / 8.0) * 0.1F + 0.9F;
				float innerScale = 0.125F * meta * scale;
				ps.pushPose();
				ps.translate(0.0F, 0.1F, 0.0F);
				ps.scale(innerScale, innerScale, innerScale);
				collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEX0), (pose, vc) -> {
					PoseStack local = new PoseStack();
					local.last().set(pose);
					pod0Part.render(local, vc, 0x00F000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
				});
				ps.popPose();
			}

			// Outer translucent shell
			float outerScale = 0.15F * meta;
			ps.pushPose();
			ps.scale(outerScale, outerScale, outerScale);
			collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(TEX2), (pose, vc) -> {
				PoseStack local = new PoseStack();
				local.last().set(pose);
				pod2Part.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, podColor);
			});
			ps.popPose();
		}
		ps.popPose();
	}
}
