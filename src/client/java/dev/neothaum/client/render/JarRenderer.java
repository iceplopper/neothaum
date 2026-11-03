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

	public record JarData(float level, int color, Aspect filter) {
	}

	/** ItemJarFilledRenderer: a jar item shows its essentia and label as the placed jar does. */
	public record ItemRenderer() implements net.minecraft.client.renderer.special.SpecialModelRenderer<JarData> {
		@Override
		public JarData extractArgument(net.minecraft.world.item.ItemStack stack) {
			var al = stack.get(dev.neothaum.registry.TVComponents.ASPECTS);
			Aspect filter = stack.get(dev.neothaum.registry.TVComponents.ASPECT_FILTER);
			Aspect held = al == null || al.size() == 0 ? null : al.getAspects()[0];
			int amount = held == null ? 0 : al.getAmount(held);
			if (amount <= 0 && filter == null) {
				return null;
			}
			return new JarData(amount > 0 ? Math.min(1.0F, (float) amount / JarBlockEntity.MAX) * 0.625F : 0.0F,
				held != null ? held.getColor() : 0xFFFFFF, filter);
		}

		@Override
		public void submit(JarData data, PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			if (data == null) {
				return;
			}
			ps.pushPose();
			ps.translate(0.5F, 0.01F, 0.5F);
			if (data.level() > 0.0F) {
				EssentiaRender.liquid(ps, collector, -0.25F, 0.0625F, -0.25F, 0.25F, 0.0625F + data.level(), 0.25F, data.color(),
					net.minecraft.util.Util.getMillis() / 50L, light);
			}
			if (data.filter() != null) {
				EssentiaRender.faceTowards(ps, Direction.NORTH);
				EssentiaRender.label(ps, collector, data.filter(), 0.4F, 0.316F, 0.5F, 0.0F, light);
			}
			ps.popPose();
		}

		@Override
		public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new org.joml.Vector3f(i & 1, i >> 1 & 1, i >> 2 & 1));
			}
		}
	}

	public record Unbaked() implements net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<JarData> {
		public static final com.mojang.serialization.MapCodec<Unbaked> CODEC = com.mojang.serialization.MapCodec.unit(new Unbaked());

		@Override
		public net.minecraft.client.renderer.special.SpecialModelRenderer<JarData> bake(
			net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext ctx) {
			return new ItemRenderer();
		}

		@Override
		public com.mojang.serialization.MapCodec<Unbaked> type() {
			return CODEC;
		}
	}
}
