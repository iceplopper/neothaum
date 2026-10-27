package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.neothaum.NeoThaum;
import dev.neothaum.tiles.BellowsBlockEntity;
import java.util.function.Consumer;
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

/** Port of TileBellowsRenderer and ModelBellows: a leather bag between two planks that squeeze together and spring apart. */
public class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity, BellowsRenderer.State> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/bellows.png");
	private static ModelPart planks;
	private static ModelPart bag;

	public static class State extends BlockEntityRenderState {
		Direction orientation = Direction.DOWN;
		float inflation = 1.0F;
	}

	public BellowsRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static void init() {
		if (planks == null) {
			// ModelBellows set mirror and the bag's 64x32 texture size only after adding the boxes, which in 1.7 leaves the boxes unmirrored and
			// mapped on the 128x64 sheet like everything else
			MeshDefinition mesh = new MeshDefinition();
			var root = mesh.getRoot();
			root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12, 2, 12),
				PartPose.offset(0.0F, 22.0F, 0.0F));
			root.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -1.0F, -6.0F, 12, 2, 12),
				PartPose.offset(0.0F, 16.0F, 0.0F));
			root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12, 2, 12),
				PartPose.offset(0.0F, 8.0F, 0.0F));
			root.addOrReplaceChild("nozzle", CubeListBuilder.create().texOffs(0, 36).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 2),
				PartPose.offset(0.0F, 16.0F, 6.0F));
			root.addOrReplaceChild("bag", CubeListBuilder.create().texOffs(48, 0).addBox(-10.0F, -12.03333F, -10.0F, 20, 24, 20),
				PartPose.offset(0.0F, 0.5F, 0.0F));
			planks = LayerDefinition.create(mesh, 128, 64).bakeRoot();
			bag = planks.getChild("bag");
		}
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(BellowsBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.orientation = be.orientation;
		s.inflation = be.inflation;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		draw(ps, collector, s.orientation, s.inflation, s.lightCoords, OverlayTexture.NO_OVERLAY);
	}

	static void draw(PoseStack ps, SubmitNodeCollector collector, Direction orientation, float scale, int light, int overlay) {
		init();
		float tscale = 0.125F + scale * 0.875F;
		ps.pushPose();
		// translateFromOrientation
		ps.translate(0.5F, -0.5F, 0.5F);
		switch (orientation) {
			case DOWN -> {
				ps.translate(0.0F, 1.0F, -1.0F);
				ps.rotateDegrees(Axis.XP, 90.0F);
			}
			case UP -> {
				ps.translate(0.0F, 1.0F, 1.0F);
				ps.rotateDegrees(Axis.XP, 270.0F);
			}
			case NORTH -> ps.rotateDegrees(Axis.YP, 180.0F);
			case WEST -> ps.rotateDegrees(Axis.YP, 270.0F);
			case EAST -> ps.rotateDegrees(Axis.YP, 90.0F);
			default -> {
			}
		}
		ps.translate(0.0F, 1.0F, 0.0F);
		ps.pushPose();
		ps.scale(0.5F, (scale + 0.1F) / 2.0F, 0.5F);
		part(ps, collector, bag, light, overlay);
		ps.popPose();
		ps.translate(0.0F, -1.0F, 0.0F);
		ps.pushPose();
		ps.translate(0.0F, -tscale / 2.0F + 0.5F, 0.0F);
		part(ps, collector, planks.getChild("top"), light, overlay);
		ps.popPose();
		ps.pushPose();
		ps.translate(0.0F, tscale / 2.0F - 0.5F, 0.0F);
		part(ps, collector, planks.getChild("bottom"), light, overlay);
		ps.popPose();
		part(ps, collector, planks.getChild("middle"), light, overlay);
		part(ps, collector, planks.getChild("nozzle"), light, overlay);
		ps.popPose();
	}

	private static void part(PoseStack ps, SubmitNodeCollector collector, ModelPart part, int light, int overlay) {
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(TEXTURE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			part.render(local, vc, light, overlay, 0xFFFFFFFF);
		});
	}

	/** In the inventory the bellows breathes on its own and faces north (renderEntityAt with no world). */
	public record ItemRenderer() implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			var p = Minecraft.getInstance().player;
			float scale = p == null ? 1.0F : Mth.sin(p.tickCount / 8.0F) * 0.3F + 0.7F;
			draw(ps, collector, Direction.NORTH, scale, light, overlay);
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
