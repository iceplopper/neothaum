package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.tiles.BannerBlockEntity;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TC4 TileBannerRenderer and ModelBanner:
 * Renders floor or wall banners with animated swaying cloth and custom colors.
 */
public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity, BannerRenderer.State> {
	private static final Identifier TEX_BLANK = Thaumvibe.id("textures/models/banner_blank.png");
	private static final Identifier TEX_CULTIST = Thaumvibe.id("textures/models/banner_cultist.png");

	private static final int[] TC_COLORS = {
		0xFFF0F0F0, 0xFFEB8844, 0xFFC354CD, 0xFF6689D3, 0xFFDECF2A, 0xFF41CD34, 0xFFD88198, 0xFF434343,
		0xFFABABAB, 0xFF287697, 0xFF7B2FBE, 0xFF253192, 0xFF51301A, 0xFF3B511A, 0xFFB3312C, 0xFF1E1B1B
	};

	private static ModelPart polePart;
	private static ModelPart beamPart;
	private static ModelPart tab1Part;
	private static ModelPart tab2Part;
	private static ModelPart bannerClothPart;

	public static class State extends BlockEntityRenderState {
		int color = -1;
		Aspect aspect = null;
		boolean onWall = false;
		int rotation = 0;
		Direction facing = Direction.NORTH;
		BlockPos pos = BlockPos.ZERO;
		long gameTime = 0;
	}

	public BannerRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static void init() {
		if (polePart == null) {
			MeshDefinition mesh = new MeshDefinition();
			var root = mesh.getRoot();

			// Texture size: 128, 64
			root.addOrReplaceChild("b1", CubeListBuilder.create()
				.texOffs(0, 29).addBox(-5.0F, -7.5F, -1.5F, 2, 3, 3),
				PartPose.ZERO);

			root.addOrReplaceChild("b2", CubeListBuilder.create()
				.texOffs(0, 29).addBox(3.0F, -7.5F, -1.5F, 2, 3, 3),
				PartPose.ZERO);

			root.addOrReplaceChild("beam", CubeListBuilder.create()
				.texOffs(30, 0).addBox(-7.0F, -7.0F, -1.0F, 14, 2, 2),
				PartPose.ZERO);

			root.addOrReplaceChild("banner", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-7.0F, 0.0F, -0.5F, 14, 28, 1),
				PartPose.ZERO);

			root.addOrReplaceChild("pole", CubeListBuilder.create()
				.texOffs(62, 0).addBox(0.0F, 0.0F, -1.0F, 2, 31, 2),
				PartPose.offset(-1.0F, -7.0F, -2.0F));

			ModelPart baked = LayerDefinition.create(mesh, 128, 64).bakeRoot();
			tab1Part = baked.getChild("b1");
			tab2Part = baked.getChild("b2");
			beamPart = baked.getChild("beam");
			bannerClothPart = baked.getChild("banner");
			polePart = baked.getChild("pole");
		}
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(BannerBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.color = be.getColor();
		s.aspect = be.getAspect();
		s.onWall = be.isOnWall();
		s.rotation = be.getRotation();
		s.facing = be.getFacing();
		s.pos = be.getBlockPos();
		if (be.getLevel() != null) {
			s.gameTime = be.getLevel().getGameTime();
		}
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		init();
		Identifier tex = (s.color == -1 && s.aspect == null) ? TEX_CULTIST : TEX_BLANK;

		ps.pushPose();
		ps.translate(0.5F, 1.5F, 0.5F);
		ps.rotateDegrees(Axis.XP, 180.0F);
		ps.rotateDegrees(Axis.YP, 180.0F);

		float rot = s.onWall ? s.facing.toYRot() : s.rotation * 360.0F / 16.0F;
		ps.rotateDegrees(Axis.YP, rot);

		if (!s.onWall) {
			collector.submitCustomGeometry(ps, RenderTypes.entityCutout(tex), (pose, vc) -> {
				PoseStack local = new PoseStack();
				local.last().set(pose);
				polePart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			});
		} else {
			ps.translate(0.0F, 0.0F, -0.4125F);
		}

		// Beam
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(tex), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			beamPart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});

		// Tabs with color
		int colorTint = (s.color >= 0 && s.color < 16) ? TC_COLORS[s.color] : 0xFFFFFFFF;
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(tex), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			tab1Part.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, colorTint);
			tab2Part.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, colorTint);
		});

		// Flutter sway
		float f3 = s.pos.getX() * 7 + s.pos.getY() * 9 + s.pos.getZ() * 13 + (float) s.gameTime;
		float rx = (0.005F + 0.005F * Mth.cos(f3 * (float) Math.PI * 0.02F)) * (float) Math.PI;

		ps.pushPose();
		ps.translate(0.0F, -0.3125F, 0.0F);
		ps.rotateDegrees(Axis.XP, rx * (180.0F / (float) Math.PI));

		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(tex), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			bannerClothPart.render(local, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, colorTint);
		});

		ps.popPose();
		ps.popPose();
	}
}
