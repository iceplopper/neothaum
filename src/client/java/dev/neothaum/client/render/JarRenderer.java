package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.tiles.JarBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Port of TileJarRenderer for warded and void jars: the essentia inside and the label on the front. */
public class JarRenderer implements BlockEntityRenderer<JarBlockEntity, JarRenderer.State> {
	public static class State extends BlockEntityRenderState {
		float level;
		int color;
		Aspect filter;
		Direction facing = Direction.NORTH;
		float tilt;
		long time;
	}

	public JarRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(JarBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.level = be.amount > 0 ? (float) be.amount / JarBlockEntity.MAX * 0.625F : 0.0F;
		s.color = be.aspect != null ? be.aspect.getColor() : 0xFFFFFF;
		s.filter = be.aspectFilter;
		s.facing = be.facing.getAxis().isHorizontal() ? be.facing : Direction.NORTH;
		// Config.crooked: labels sit a couple of degrees askew
		s.tilt = be.aspectFilter == null ? 0.0F
			: (be.aspectFilter.getTag().hashCode() + be.getBlockPos().getX() + be.facing.get3DDataValue()) % 4 - 2;
		s.time = be.getLevel() == null ? 0L : be.getLevel().getGameTime();
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		ps.pushPose();
		ps.translate(0.5F, 0.01F, 0.5F);
		if (s.level > 0.0F) {
			EssentiaRender.liquid(ps, collector, -0.25F, 0.0625F, -0.25F, 0.25F, 0.0625F + s.level, 0.25F, s.color, s.time, s.lightCoords);
		}
		if (s.filter != null) {
			EssentiaRender.faceTowards(ps, s.facing);
			EssentiaRender.label(ps, collector, s.filter, 0.4F, 0.316F, 0.5F, s.tilt, s.lightCoords);
		}
		ps.popPose();
	}
}
