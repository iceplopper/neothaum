package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.blocks.eldritch.AncientLockBlock;
import dev.neothaum.tiles.eldritch.AncientLockBlockEntity;
import net.minecraft.client.Minecraft;
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
 * TileEldritchLockRenderer: four arms of eldritch cubes reach out across the sealed doorway from the lock, breathing in and out; once a
 * tablet is fitted they draw back one cube at a time, and the tablet shows in the keyhole. The doorway itself is a 5x5 window onto the
 * starfield.
 */
public class AncientLockRenderer implements BlockEntityRenderer<AncientLockBlockEntity, AncientLockRenderer.State> {
	private static final Identifier CUBE = NeoThaum.id("textures/models/eldritch_cube.png");
	private static final Identifier TABLET = NeoThaum.id("textures/items/eldritch_object_2.png");

	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		int count = -1;
		float time;
	}

	public AncientLockRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(AncientLockBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.getBlockState().getValue(AncientLockBlock.FACING);
		s.count = be.count;
		var player = Minecraft.getInstance().player;
		s.time = (player == null ? 0 : player.tickCount) + pt;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		Direction dir = s.facing;
		ps.pushPose();
		ps.translate(0.5, 0.5, 0.5);
		for (int u = 0; u < 4; u++) {
			ps.pushPose();
			ps.rotateAround(Axis.of(new org.joml.Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ())).rotationDegrees(90 * u), 0, 0, 0);
			for (int a = 1; a < 5 - (s.count + u * 5) / 20; a++) {
				ps.pushPose();
				ps.translate(0.0, 0.25 + 0.5 * a, 0.0);
				float w = Mth.sin((s.time + a * 10 + u * 20) / 20.0F) * 0.1F;
				if (a == 1 || a == 4) {
					w = w / 2.0F + 0.2F;
				}
				ps.scale(0.5F + w, 0.5F, 0.5F + w);
				int light = s.lightCoords;
				collector.submitCustomGeometry(ps, RenderTypes.entityCutout(CUBE), (pose, vc) -> cube(vc, pose, light));
				ps.popPose();
			}
			ps.popPose();
		}
		ps.popPose();
		if (s.count >= 0 && dir.getAxis().isHorizontal()) {
			// the Runed Tablet sitting in the keyhole
			ps.pushPose();
			ps.translate(0.5 + dir.getStepX() * 0.525, 0.285, 0.5 + dir.getStepZ() * 0.525);
			ps.rotateDegrees(Axis.YP, -dir.toYRot());
			ps.scale(0.5F, 0.5F, 0.5F);
			collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TABLET), (pose, vc) -> {
				// the texture is a 20-frame animation strip; the first frame
				float v1 = 1.0F / 20.0F;
				vertex(vc, pose, -0.5F, 0.0F, 0.0F, 1.0F, v1, 0xF000F0);
				vertex(vc, pose, 0.5F, 0.0F, 0.0F, 0.0F, v1, 0xF000F0);
				vertex(vc, pose, 0.5F, 1.0F, 0.0F, 0.0F, 0.0F, 0xF000F0);
				vertex(vc, pose, -0.5F, 1.0F, 0.0F, 1.0F, 0.0F, 0xF000F0);
			});
			ps.popPose();
		}
		if (dir.getAxis().isHorizontal()) {
			// the doorway: a 5x5 window onto the starfield, on the lock's outer face
			ps.pushPose();
			ps.translate(0.5, 0.5, 0.5);
			ps.rotateDegrees(Axis.YP, -dir.toYRot());
			collector.submitCustomGeometry(ps, RenderTypes.endPortal(), (pose, vc) -> {
				vc.addVertex(pose, 2.5F, -2.5F, 0.45F);
				vc.addVertex(pose, 2.5F, 2.5F, 0.45F);
				vc.addVertex(pose, -2.5F, 2.5F, 0.45F);
				vc.addVertex(pose, -2.5F, -2.5F, 0.45F);
			});
			ps.popPose();
		}
	}

	private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
		vc.addVertex(pose, x, y, z).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
	}

	/** ModelCube(0): a whole-block box with the 64x64 texture's box layout from (0, 0). */
	private static void cube(VertexConsumer vc, PoseStack.Pose pose, int light) {
		float h = 0.5F;
		float s = 16.0F / 64.0F;
		// u, v of each face in the box layout (16x16 faces)
		face(vc, pose, light, new float[][]{{-h, h, -h}, {h, h, -h}, {h, h, h}, {-h, h, h}}, s, 0, 0, 1, 0);
		face(vc, pose, light, new float[][]{{-h, -h, h}, {h, -h, h}, {h, -h, -h}, {-h, -h, -h}}, 2 * s, 0, -1, 0, 0);
		face(vc, pose, light, new float[][]{{-h, h, -h}, {-h, -h, -h}, {h, -h, -h}, {h, h, -h}}, s, s, 0, 0, -1);
		face(vc, pose, light, new float[][]{{h, h, h}, {h, -h, h}, {-h, -h, h}, {-h, h, h}}, 3 * s, s, 0, 0, 1);
		face(vc, pose, light, new float[][]{{-h, h, h}, {-h, -h, h}, {-h, -h, -h}, {-h, h, -h}}, 0, s, -1, 0, 0);
		face(vc, pose, light, new float[][]{{h, h, -h}, {h, -h, -h}, {h, -h, h}, {h, h, h}}, 2 * s, s, 1, 0, 0);
	}

	private static void face(VertexConsumer vc, PoseStack.Pose pose, int light, float[][] p, float u0, float v0, float nx, float ny, float nz) {
		float s = 16.0F / 64.0F;
		float[][] uv = {{u0, v0}, {u0, v0 + s}, {u0 + s, v0 + s}, {u0 + s, v0}};
		for (int i = 0; i < 4; i++) {
			vc.addVertex(pose, p[i][0], p[i][1], p[i][2]).setColor(0xFFFFFFFF).setUv(uv[i][0], uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light).setNormal(pose, nx, ny, nz);
		}
	}
}
