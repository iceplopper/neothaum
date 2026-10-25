package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.tiles.BufferBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Port of TileTubeBufferRenderer: a ring on each connected side that is choked (blue) or shut to suction (red). */
public class BufferRenderer implements BlockEntityRenderer<BufferBlockEntity, BufferRenderer.State> {
	public static class State extends BlockEntityRenderState {
		final byte[] rings = new byte[6];
	}

	public BufferRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(BufferBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		for (Direction d : Direction.values()) {
			int i = d.ordinal();
			s.rings[i] = be.chokedSides[i] != 0 && be.openSides[i] && be.getLevel() != null
				&& EssentiaTransport.connectable(be.getLevel(), be.getBlockPos(), d) != null ? be.chokedSides[i] : 0;
		}
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		for (Direction d : Direction.values()) {
			byte ring = s.rings[d.ordinal()];
			if (ring == 0) {
				continue;
			}
			ps.pushPose();
			ps.translate(0.5F, 0.5F, 0.5F);
			TubeRenderer.orient(ps, d.getOpposite());
			ps.scale(1.2F, 1.0F, 1.2F);
			ps.translate(0.0F, -0.5F, 0.0F);
			int argb = ring == 2 ? 0xFFFF4C4C : 0xFF4C4CFF;
			TubeRenderer.part(ps, collector, s.lightCoords, argb);
			ps.popPose();
		}
	}
}
