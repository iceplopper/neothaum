package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.blocks.HungryChestBlock;
import dev.thaumvibe.tiles.HungryChestBlockEntity;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Port of TileChestHungryRenderer: draws a single chest with chesthungry.png (sharp teeth along the seam).
 */
public class HungryChestRenderer implements BlockEntityRenderer<HungryChestBlockEntity, HungryChestRenderer.State> {
	public static final Identifier TEXTURE = Thaumvibe.id("textures/models/chesthungry.png");

	public static class State extends BlockEntityRenderState {
		public Direction facing = Direction.NORTH;
		public float openness;
	}

	private final ChestModel model;

	public HungryChestRenderer(BlockEntityRendererProvider.Context ctx) {
		this.model = new ChestModel(ctx.bakeLayer(ModelLayers.CHEST));
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(HungryChestBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.getBlockState().hasProperty(HungryChestBlock.FACING) ? be.getBlockState().getValue(HungryChestBlock.FACING) : Direction.NORTH;
		s.openness = be.getOpenNess(pt);
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		ps.pushPose();
		ps.mulPose(ChestRenderer.modelTransformation(s.facing));
		float f = 1.0F - s.openness;
		f = 1.0F - f * f * f;
		collector.submitModel(this.model, f, ps, TEXTURE, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		ps.popPose();
	}

	public record ItemRenderer(ChestModel model) implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			ps.pushPose();
			ps.mulPose(ChestRenderer.modelTransformation(Direction.NORTH));
			collector.submitModel(this.model, 0.0F, ps, TEXTURE, light, overlay, outline);
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
			return new ItemRenderer(new ChestModel(ctx.entityModelSet().bakeLayer(ModelLayers.CHEST)));
		}

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}
	}
}
