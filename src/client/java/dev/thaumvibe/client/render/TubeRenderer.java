package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.tiles.TubeBlockEntity;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** TileTubeValveRenderer (the handwheel, which sinks as it turns shut) and TileTubeOnewayRenderer (three blue rings showing the flow). */
public class TubeRenderer implements BlockEntityRenderer<TubeBlockEntity, TubeRenderer.State> {
	private static final Identifier VALVE = Thaumvibe.id("textures/models/valve.png");
	private static ModelPart rod;

	public static class State extends BlockEntityRenderState {
		TubeBlockEntity.Kind kind = TubeBlockEntity.Kind.NORMAL;
		Direction facing = Direction.NORTH;
		float rotation;
		boolean showArrow;
	}

	public TubeRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	/** ModelTubeValve: a 2x2x2 stub standing 2 pixels off the tube's centre. */
	static ModelPart rod() {
		if (rod == null) {
			MeshDefinition mesh = new MeshDefinition();
			mesh.getRoot().addOrReplaceChild("rod", CubeListBuilder.create().texOffs(0, 10).mirror().addBox(-1.0F, 2.0F, -1.0F, 2, 2, 2), PartPose.ZERO);
			rod = LayerDefinition.create(mesh, 64, 32).bakeRoot();
		}
		return rod;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(TubeBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.kind = be.kind();
		s.facing = be.facing;
		s.rotation = be.rotation;
		s.showArrow = s.kind == TubeBlockEntity.Kind.ONEWAY && be.getLevel() != null
			&& EssentiaTransport.connectable(be.getLevel(), be.getBlockPos(), be.facing.getOpposite()) != null;
	}

	/** The shared turn: local +Y ends up pointing out of the facing side. */
	static void orient(PoseStack ps, Direction fd) {
		if (fd.getStepY() == 0) {
			ps.rotateDegrees(Axis.YP, 90.0F);
		} else {
			ps.rotateDegrees(Axis.XN, 90.0F);
			ps.rotateDegrees(Axis.of(new Vector3f(fd.getStepY(), 0, 0)), 90.0F);
		}
		ps.rotateDegrees(Axis.of(new Vector3f(fd.getStepX(), fd.getStepY(), fd.getStepZ())), 90.0F);
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		int light = s.lightCoords;
		if (s.kind == TubeBlockEntity.Kind.VALVE) {
			ps.pushPose();
			ps.translate(0.5F, 0.5F, 0.5F);
			orient(ps, s.facing);
			ps.rotateDegrees(Axis.YP, -s.rotation * 1.5F);
			ps.translate(0.0F, -(s.rotation / 360.0F) * 0.12F, 0.0F);
			part(ps, collector, light, 0xFFFFFFFF);
			// renderValve: the handwheel sprite as a thin 2D item, flat on top of the stub
			ps.rotateDegrees(Axis.XP, 90.0F);
			ps.translate(-0.25F, -0.25F, -0.25F);
			ps.scale(0.5F, 0.5F, 0.5F);
			TextureAtlasSprite sp = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
				.getSprite(Thaumvibe.id("blocks/pipe_valve"));
			collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), (pose, vc) -> {
				// ItemRenderer.renderItemIn2D: front at z=0, back at z=-0.1, u mirrored as TC4 passed maxU first
				float u0 = sp.getU1(), u1 = sp.getU0(), v0 = sp.getV0(), v1 = sp.getV1();
				quad(vc, pose, 0, 0, 0, 1, 1, 0, u0, v1, u1, v0, 0, 0, 1, light);
				quad(vc, pose, 1, 0, -0.1F, 0, 1, -0.1F, u1, v1, u0, v0, 0, 0, -1, light);
			});
			ps.popPose();
		} else if (s.showArrow) {
			ps.pushPose();
			ps.translate(0.5F, 0.5F, 0.5F);
			orient(ps, s.facing);
			ps.scale(1.1F, 0.5F, 1.1F);
			ps.translate(0.0F, -0.5F, 0.0F);
			int blue = 0xFF000000 | (int) (0.45F * 255) << 16 | (int) (0.5F * 255) << 8 | 255;
			part(ps, collector, light, blue);
			ps.translate(0.0F, -0.25F, 0.0F);
			part(ps, collector, light, blue);
			ps.translate(0.0F, -0.25F, 0.0F);
			part(ps, collector, light, blue);
			ps.popPose();
		}
	}

	static void part(PoseStack ps, SubmitNodeCollector collector, int light, int argb) {
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(VALVE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			rod().render(local, vc, light, OverlayTexture.NO_OVERLAY, argb);
		});
	}

	/** A quad spanning (x0, y0) to (x1, y1) at depth z0/z1 (front and back are drawn with opposite windings). */
	private static void quad(VertexConsumer vc, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1, float ua, float va,
		float ub, float vb, float nx, float ny, float nz, int light) {
		vc.addVertex(pose, x0, y0, z0).setColor(0xFFFFFFFF).setUv(ua, va).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x1, y0, z0).setColor(0xFFFFFFFF).setUv(ub, va).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x1, y1, z1).setColor(0xFFFFFFFF).setUv(ub, vb).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x0, y1, z1).setColor(0xFFFFFFFF).setUv(ua, vb).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
	}
}
