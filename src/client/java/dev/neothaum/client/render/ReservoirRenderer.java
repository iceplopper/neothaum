package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.neothaum.NeoThaum;
import dev.neothaum.tiles.ReservoirBlockEntity;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Port of TileEssentiaReservoirRenderer:
 * Renders the reservoir OBJ model and the interior dynamic essentia fluid.
 */
public class ReservoirRenderer implements BlockEntityRenderer<ReservoirBlockEntity, ReservoirRenderer.State> {
	private static final Identifier MODEL = NeoThaum.id("textures/models/reservoir.obj");
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/reservoir.png");

	private static ObjModel model;

	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.DOWN;
		float fillLevel;
		int rgb = 0xFFFFFF;
		long gameTime;
	}

	public ReservoirRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static ObjModel model() {
		if (model == null) {
			model = ObjModel.load(MODEL);
		}
		return model;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ReservoirBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.getFacing();
		s.fillLevel = (float) be.aspects.visSize() / ReservoirBlockEntity.MAX_AMOUNT;
		int r = Mth.clamp((int) (be.currentR * 255.0F), 0, 255);
		int g = Mth.clamp((int) (be.currentG * 255.0F), 0, 255);
		int b = Mth.clamp((int) (be.currentB * 255.0F), 0, 255);
		s.rgb = (r << 16) | (g << 8) | b;
		s.gameTime = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		draw(ps, collector, s.facing, s.lightCoords, OverlayTexture.NO_OVERLAY);
		if (s.fillLevel > 0.0F) {
			float y1 = 0.1875F + 0.625F * s.fillLevel;
			EssentiaRender.liquid(ps, collector, 0.1875F, 0.1875F, 0.1875F, 0.8125F, y1, 0.8125F, s.rgb, s.gameTime, s.lightCoords);
		}
	}

	static void draw(PoseStack ps, SubmitNodeCollector collector, Direction facing, int light, int overlay) {
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);
		switch (facing) {
			case DOWN -> ps.rotateDegrees(Axis.XP, -90.0F);
			case UP -> ps.rotateDegrees(Axis.XP, 90.0F);
			case NORTH -> {
			}
			case SOUTH -> ps.rotateDegrees(Axis.YP, 180.0F);
			case WEST -> ps.rotateDegrees(Axis.YP, 90.0F);
			case EAST -> ps.rotateDegrees(Axis.YP, -90.0F);
		}
		ps.translate(0.0F, 0.0F, -0.5F);

		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			model().render(pose, vc, light, overlay, 0xFFFFFFFF);
		});
		ps.popPose();
	}

	public record ItemRenderer() implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			draw(ps, collector, Direction.DOWN, light, overlay);
		}

		@Override
		public void getExtents(Consumer<Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new Vector3f(i & 1, i >> 1 & 1, i >> 2 & 1));
			}
		}
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
		public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

		@Override
		public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext ctx) {
			return new ItemRenderer();
		}

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}
	}
}
