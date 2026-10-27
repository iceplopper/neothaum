package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.neothaum.NeoThaum;
import dev.neothaum.tiles.CrystallizerBlockEntity;
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
 * Port of TileEssentiaCrystalizerRenderer:
 * Renders the crystalizer body and four orbiting essentia crystals spinning within.
 */
public class CrystallizerRenderer implements BlockEntityRenderer<CrystallizerBlockEntity, CrystallizerRenderer.State> {
	private static final Identifier MODEL_BODY = NeoThaum.id("textures/models/crystalizer.obj");
	private static final Identifier TEXTURE_BODY = NeoThaum.id("textures/models/crystalizer.png");
	private static final Identifier MODEL_CRYSTAL = NeoThaum.id("textures/models/vis_relay.obj");
	private static final Identifier TEXTURE_CRYSTAL = NeoThaum.id("textures/models/vis_relay.png");

	private static ObjModel modelBody;
	private static ObjModel modelCrystal;

	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.DOWN;
		float spin;
		int color = 0xFFFFFFFF;
	}

	public CrystallizerRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static ObjModel modelBody() {
		if (modelBody == null) {
			modelBody = ObjModel.load(MODEL_BODY);
		}
		return modelBody;
	}

	private static ObjModel modelCrystal() {
		if (modelCrystal == null) {
			modelCrystal = ObjModel.load(MODEL_CRYSTAL);
		}
		return modelCrystal;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CrystallizerBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.getFacing();
		s.spin = Mth.rotLerp(pt, be.prevSpin, be.spin);
		s.color = be.aspect != null ? (0xFF000000 | be.aspect.getColor()) : 0xFFFFFFFF;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		draw(ps, collector, s.facing, s.spin, s.color, s.lightCoords, OverlayTexture.NO_OVERLAY);
	}

	static void draw(PoseStack ps, SubmitNodeCollector collector, Direction facing, float spin, int color, int light, int overlay) {
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

		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE_BODY), (pose, vc) -> {
			modelBody().render(pose, vc, light, overlay, 0xFFFFFFFF);
		});

		for (int q = 0; q < 4; q++) {
			ps.pushPose();
			ps.scale(0.75F, 0.75F, 0.75F);
			ps.rotateDegrees(Axis.ZP, 90.0F * q);
			ps.translate(0.34F, 0.0F, 1.2125F);
			ps.rotateDegrees(Axis.ZP, spin);
			collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(TEXTURE_CRYSTAL), (pose, vc) -> {
				modelCrystal().renderPart("Crystal", pose, vc, light, overlay, color);
			});
			ps.popPose();
		}

		ps.popPose();
	}

	public record ItemRenderer() implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			var p = Minecraft.getInstance().player;
			float spin = p == null ? 0.0F : (p.tickCount * 4.0F) % 360.0F;
			draw(ps, collector, Direction.DOWN, spin, 0xFF44FFCC, light, overlay);
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
