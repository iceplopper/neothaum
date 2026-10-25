package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.tiles.AlembicBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Port of TileAlembicRenderer: the OBJ alembic, standing on legs or piped down into the furnace or the alembic below. */
public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity, AlembicRenderer.State> {
	private static final Identifier MODEL = Thaumvibe.id("textures/models/alembic.obj");
	private static final Identifier TEXTURE = Thaumvibe.id("textures/models/alembic.png");
	private static ObjModel model;

	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		boolean aboveFurnace;
		boolean aboveAlembic;
		Aspect filter;
	}

	public AlembicRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	static ObjModel model() {
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
	public void extractRenderState(AlembicBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.facing.getAxis().isHorizontal() ? be.facing : Direction.NORTH;
		s.aboveFurnace = be.aboveFurnace();
		s.aboveAlembic = !s.aboveFurnace && be.aboveAlembic();
		s.filter = be.aspectFilter;
	}

	/** The rotation about the model's up axis for each facing (TC4: 2 → 270, 3 → 90, 4 → 0, 5 → 180). */
	static float modelRotation(Direction facing) {
		return switch (facing) {
			case NORTH -> 270.0F;
			case SOUTH -> 90.0F;
			case EAST -> 180.0F;
			default -> 0.0F;
		};
	}

	/** The alembic as an item: on its legs, shifted as TileAlembicRenderer did with no world. */
	public record ItemRenderer() implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			ps.pushPose();
			ps.translate(0.5F, 0.0F, 0.5F);
			ps.rotateDegrees(Axis.XP, -90.0F);
			collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
				ObjModel m = model();
				m.renderPart("Legs", pose, vc, light, overlay, 0xFFFFFFFF);
				m.renderPart("Pot", pose, vc, light, overlay, 0xFFFFFFFF);
				m.renderPart("Panel", pose, vc, light, overlay, 0xFFFFFFFF);
			});
			ps.popPose();
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

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		int light = s.lightCoords;
		ps.pushPose();
		ps.translate(0.5F, 0.0F, 0.5F);
		ps.pushPose();
		ps.rotateDegrees(Axis.XP, -90.0F);
		ps.rotateDegrees(Axis.ZP, modelRotation(s.facing));
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			ObjModel m = model();
			if (s.aboveFurnace) {
				m.renderPart("TubeMain", pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
				m.renderPart("Legs", pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			} else if (s.aboveAlembic) {
				m.renderPart("TubeMain", pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
				m.renderPart("TubeSmall", pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			} else {
				m.renderPart("Legs", pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			}
			m.renderPart("Pot", pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			m.renderPart("Panel", pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();
		if (s.filter != null) {
			EssentiaRender.faceTowards(ps, s.facing);
			EssentiaRender.label(ps, collector, s.filter, 0.468F, 0.41F, 0.27F, 0.0F, light);
		}
		ps.popPose();
	}
}
