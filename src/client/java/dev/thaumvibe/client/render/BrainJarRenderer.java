package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.tiles.BrainJarBlockEntity;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** TileJarRenderer.renderBrain: the brain bobs in its brine, turning slowly to watch whoever is near. */
public class BrainJarRenderer implements BlockEntityRenderer<BrainJarBlockEntity, BrainJarRenderer.State> {
	private static final Identifier BRAIN = Thaumvibe.id("textures/models/brain2.png");
	private static final Identifier BRINE = Thaumvibe.id("textures/models/jarbrine.png");
	private static ModelPart brain;
	private static ModelPart brine;

	public static class State extends BlockEntityRenderState {
		float rot;
		float bob;
	}

	public BrainJarRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static void init() {
		if (brain == null) {
			// ModelBrain
			MeshDefinition mesh = new MeshDefinition();
			var root = mesh.getRoot();
			root.addOrReplaceChild("s1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 12, 10, 16),
				PartPose.offset(-6.0F, 8.0F, -8.0F));
			root.addOrReplaceChild("s2", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 8, 3, 7),
				PartPose.offset(-4.0F, 18.0F, 0.0F));
			root.addOrReplaceChild("s3", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(0.0F, 0.0F, 0.0F, 2, 6, 2),
				PartPose.offsetAndRotation(-1.0F, 18.0F, -2.0F, 0.4089647F, 0.0F, 0.0F));
			brain = LayerDefinition.create(mesh, 128, 64).bakeRoot();
			// ModelJar.Brine
			MeshDefinition jar = new MeshDefinition();
			jar.getRoot().addOrReplaceChild("brine", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -11.0F, -4.0F, 8, 10, 8),
				PartPose.ZERO);
			brine = LayerDefinition.create(jar, 64, 32).bakeRoot();
		}
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(BrainJarBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		var p = Minecraft.getInstance().player;
		s.bob = p == null ? 0.0F : Mth.sin(p.tickCount / 14.0F) * 0.03F + 0.03F;
		float f2 = be.rota - be.rotb;
		while (f2 >= Mth.PI) {
			f2 -= Mth.TWO_PI;
		}
		while (f2 < -Mth.PI) {
			f2 += Mth.TWO_PI;
		}
		s.rot = be.rotb + f2 * pt;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		init();
		int light = s.lightCoords;
		ps.pushPose();
		ps.translate(0.5F, 0.01F, 0.5F);
		ps.rotateDegrees(Axis.XP, 180.0F);
		ps.pushPose();
		ps.translate(0.0F, -0.8F + s.bob, 0.0F);
		ps.rotateDegrees(Axis.YP, s.rot * Mth.RAD_TO_DEG);
		ps.rotateDegrees(Axis.YP, -90.0F);
		ps.scale(0.4F, 0.4F, 0.4F);
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(BRAIN), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			brain.render(local, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(BRINE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			brine.render(local, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();
	}
}
