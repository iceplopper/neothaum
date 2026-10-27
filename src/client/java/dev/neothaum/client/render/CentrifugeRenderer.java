package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.neothaum.NeoThaum;
import dev.neothaum.tiles.CentrifugeBlockEntity;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Port of TileCentrifugeRenderer and ModelCentrifuge:
 * Static top and bottom caps with spinning rotor, crossbar and twin counterweight flasks.
 */
public class CentrifugeRenderer implements BlockEntityRenderer<CentrifugeBlockEntity, CentrifugeRenderer.State> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/centrifuge.png");
	private static ModelPart root;
	private static ModelPart top;
	private static ModelPart bottom;
	private static ModelPart rotor;

	public static class State extends BlockEntityRenderState {
		float rotation;
	}

	public CentrifugeRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static void init() {
		if (root == null) {
			MeshDefinition mesh = new MeshDefinition();
			var r = mesh.getRoot();
			// Texture size: 64, 32
			// In ModelCentrifuge:
			// Top: addBox(-4, -8, -4, 8, 4, 8) at (20, 16)
			r.addOrReplaceChild("top", CubeListBuilder.create().texOffs(20, 16).addBox(-4.0F, -8.0F, -4.0F, 8, 4, 8), PartPose.ZERO);
			// Bottom: addBox(-4, 4, -4, 8, 4, 8) at (20, 16)
			r.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(20, 16).addBox(-4.0F, 4.0F, -4.0F, 8, 4, 8), PartPose.ZERO);
			// Rotor:
			r.addOrReplaceChild("rotor", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5F, -4.0F, -1.5F, 3, 8, 3)
				.texOffs(16, 0).addBox(-4.0F, -1.0F, -1.0F, 8, 2, 2)
				.texOffs(0, 16).addBox(4.0F, -3.0F, -2.0F, 4, 6, 4)
				.texOffs(0, 16).addBox(-8.0F, -3.0F, -2.0F, 4, 6, 4),
				PartPose.ZERO);

			root = LayerDefinition.create(mesh, 64, 32).bakeRoot();
			top = root.getChild("top");
			bottom = root.getChild("bottom");
			rotor = root.getChild("rotor");
		}
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CentrifugeBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.rotation = Mth.rotLerp(pt, be.prevRotation, be.rotation);
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		draw(ps, collector, s.rotation, s.lightCoords, OverlayTexture.NO_OVERLAY);
	}

	static void draw(PoseStack ps, SubmitNodeCollector collector, float rotation, int light, int overlay) {
		init();
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);
		ps.scale(1.0F, -1.0F, -1.0F);
		part(ps, collector, top, light, overlay);
		part(ps, collector, bottom, light, overlay);
		ps.pushPose();
		ps.rotateDegrees(Axis.YP, rotation);
		part(ps, collector, rotor, light, overlay);
		ps.popPose();
		ps.popPose();
	}

	private static void part(PoseStack ps, SubmitNodeCollector collector, ModelPart part, int light, int overlay) {
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			part.render(local, vc, light, overlay, 0xFFFFFFFF);
		});
	}

	public record ItemRenderer() implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			var p = Minecraft.getInstance().player;
			float rot = p == null ? 0.0F : (p.tickCount * 4.0F) % 360.0F;
			draw(ps, collector, rot, light, overlay);
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
