package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neothaum.golem.GolemBobberEntity;
import dev.neothaum.golem.GolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** RenderGolemBobber: the float, facing the camera, on a line back to the golem's right hand. */
public class GolemBobberRenderer extends EntityRenderer<GolemBobberEntity, GolemBobberRenderer.State> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/fishing/fishing_hook.png");
	private static final RenderType RENDER_TYPE = RenderTypes.entityCutoutCull(TEXTURE);

	public static class State extends EntityRenderState {
		public Vec3 lineOriginOffset = Vec3.ZERO;
	}

	public GolemBobberRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(GolemBobberEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		GolemEntity fisher = entity.getFisher();
		if (fisher == null) {
			state.lineOriginOffset = Vec3.ZERO;
			return;
		}
		float yaw = Mth.lerp(partialTicks, fisher.yBodyRotO, fisher.yBodyRot) * Mth.DEG_TO_RAD;
		double sin = Mth.sin(yaw);
		double cos = Mth.cos(yaw);
		Vec3 hand = fisher.getPosition(partialTicks).add(-cos * 0.3 - sin * 0.4, fisher.getBbHeight() * 0.6, -sin * 0.3 + cos * 0.4);
		state.lineOriginOffset = hand.subtract(entity.getPosition(partialTicks).add(0.0, 0.25, 0.0));
	}

	@Override
	protected boolean affectedByCulling(GolemBobberEntity entity) {
		return false;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.pushPose();
		poseStack.scale(0.5F, 0.5F, 0.5F);
		poseStack.rotate(camera.orientation);
		collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
			vertex(buffer, pose, state.lightCoords, 0.0F, 0, 0, 1);
			vertex(buffer, pose, state.lightCoords, 1.0F, 0, 1, 1);
			vertex(buffer, pose, state.lightCoords, 1.0F, 1, 1, 0);
			vertex(buffer, pose, state.lightCoords, 0.0F, 1, 0, 0);
		});
		poseStack.popPose();
		float xa = (float) state.lineOriginOffset.x;
		float ya = (float) state.lineOriginOffset.y;
		float za = (float) state.lineOriginOffset.z;
		if (xa != 0.0F || ya != 0.0F || za != 0.0F) {
			float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
			collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
				for (int i = 0; i < 16; i++) {
					float a0 = i / 16.0F;
					float a1 = (i + 1) / 16.0F;
					stringVertex(xa, ya, za, buffer, pose, a0, a1, width);
					stringVertex(xa, ya, za, buffer, pose, a1, a0, width);
				}
			});
		}
		poseStack.popPose();
		super.submit(state, poseStack, collector, camera);
	}

	private static void vertex(VertexConsumer b, PoseStack.Pose pose, int light, float x, int y, int u, int v) {
		b.addVertex(pose, x - 0.5F, y - 0.5F, 0.0F).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	private static void stringVertex(float xa, float ya, float za, VertexConsumer b, PoseStack.Pose pose, float aa, float nexta, float width) {
		float x = xa * aa;
		float y = ya * (aa * aa + aa) * 0.5F + 0.25F;
		float z = za * aa;
		float nx = xa * nexta - x;
		float ny = ya * (nexta * nexta + nexta) * 0.5F + 0.25F - y;
		float nz = za * nexta - z;
		float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
		b.addVertex(pose, x, y, z).setColor(-16777216).setNormal(pose, nx / length, ny / length, nz / length).setLineWidth(width);
	}
}
