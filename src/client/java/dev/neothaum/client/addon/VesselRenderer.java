package dev.neothaum.client.addon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.addon.vessels.VesselBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** What a vessel holds, small and slowly turning inside the glass, as a spawner shows its creature. */
public class VesselRenderer implements BlockEntityRenderer<VesselBlockEntity, SpawnerRenderState> {
	private final EntityRenderDispatcher entityRenderer;

	public VesselRenderer(BlockEntityRendererProvider.Context context) {
		this.entityRenderer = context.entityRenderer();
	}

	@Override
	public SpawnerRenderState createRenderState() {
		return new SpawnerRenderState();
	}

	@Override
	public void extractRenderState(VesselBlockEntity be, SpawnerRenderState state, float partialTicks, Vec3 camera,
		ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
		state.displayEntity = null;
		if (be.getLevel() == null) {
			return;
		}
		Entity shown = be.shown(be.getLevel());
		if (shown == null) {
			return;
		}
		state.displayEntity = this.entityRenderer.extractEntity(shown, partialTicks);
		state.displayEntity.lightCoords = state.lightCoords;
		state.spin = (be.getLevel().getGameTime() % 360 + partialTicks) * 2.0F;
		// to fit inside the jar (ten pixels across, twelve high)
		state.scale = 0.36F / Math.max(1.0F, Math.max(shown.getBbWidth(), shown.getBbHeight()));
	}

	@Override
	public void submit(SpawnerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.displayEntity == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.08F, 0.5F);
		poseStack.rotateDegrees(Axis.YP, state.spin);
		poseStack.scale(state.scale, state.scale, state.scale);
		this.entityRenderer.submit(state.displayEntity, camera, 0.0, 0.0, 0.0, poseStack, collector);
		poseStack.popPose();
	}
}
