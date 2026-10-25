package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.blocks.CrystalClusterBlock;
import dev.thaumvibe.tiles.CrystalBlockEntity;
import java.util.Random;
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
import net.minecraft.client.renderer.rendertype.RenderType;
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

/** Port of TileCrystalRenderer + ModelCrystal: six stretched, tilted crystals with a shimmering glow. */
public class CrystalRenderer implements BlockEntityRenderer<CrystalBlockEntity, CrystalRenderer.State> {
	private static final Identifier TEXTURE = Thaumvibe.id("textures/models/crystal.png");
	/** BlockCustomOreItem.colors */
	private static final int[] COLORS = {0xFFFFFF, 0xFFFF7E, 0xFF3C01, 0x0090FF, 0x00A000, 0xEECCFF, 0x555577};
	private static ModelPart model;

	/** ModelCrystal: one 16x16x16 box spun 0.7071 rad about Z and X so it stands on a corner. */
	private static ModelPart model() {
		if (model == null) {
			MeshDefinition mesh = new MeshDefinition();
			mesh.getRoot().addOrReplaceChild("crystal", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-16.0F, -16.0F, 0.0F, 16, 16, 16),
				PartPose.offsetAndRotation(0.0F, 32.0F, 0.0F, 0.7071F, 0.0F, 0.7071F));
			model = LayerDefinition.create(mesh, 64, 32).bakeRoot();
		}
		return model;
	}

	public static class State extends BlockEntityRenderState {
		int type;
		int orientation;
	}

	public CrystalRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CrystalBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.type = be.getBlockState().getBlock() instanceof CrystalClusterBlock c ? c.type : 0;
		s.orientation = be.getBlockState().getValue(CrystalClusterBlock.FACING).get3DDataValue();
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		draw(ps, collector, s.type, s.orientation, s.type + s.blockPos.getX() + s.blockPos.getY() * s.blockPos.getZ(), -1);
	}

	/**
	 * renderTileEntityAt, drawn at the block origin. In the world the crystals set their own glowing lightmap value; items pass the
	 * light they are given ({@code fixedLight}), as 1.7.10 drew inventory items without the lightmap.
	 */
	public static void draw(PoseStack ps, SubmitNodeCollector collector, int md, int orientation, long seed, int fixedLight) {
		int color = md != 6 ? COLORS[md + 1] : COLORS[5];
		Random rand = new Random(seed);
		float a1 = (rand.nextFloat() - rand.nextFloat()) * 5.0F;
		float a2 = (rand.nextFloat() - rand.nextFloat()) * 5.0F;
		drawCrystal(ps, collector, orientation, a1, a2, rand, color, 1.1F, fixedLight);
		for (int a = 1; a < 6; a++) {
			if (md == 6) {
				color = COLORS[a == 5 ? 6 : a];
			}
			int angle1 = rand.nextInt(36) + 72 * a;
			int angle2 = 15 + rand.nextInt(15);
			drawCrystal(ps, collector, orientation, angle1, angle2, rand, color, 0.8F, fixedLight);
		}
	}

	private static void drawCrystal(PoseStack ps, SubmitNodeCollector collector, int ori, float a1, float a2, Random rand, int color, float size, int fixedLight) {
		var player = Minecraft.getInstance().player;
		int ticks = player == null ? 0 : player.tickCount;
		float shade = Mth.sin((ticks + rand.nextInt(10)) / (5.0F + rand.nextFloat())) * 0.075F + 0.925F;
		float r = Math.min(1.0F, (color >> 16 & 255) / 220.0F);
		float g = Math.min(1.0F, (color >> 8 & 255) / 220.0F);
		float b = Math.min(1.0F, (color & 255) / 220.0F);
		ps.pushPose();
		translateFromOrientation(ps, ori);
		ps.rotateDegrees(Axis.YP, a1);
		ps.rotateDegrees(Axis.XP, a2);
		ps.scale((0.15F + rand.nextFloat() * 0.075F) * size, (0.5F + rand.nextFloat() * 0.1F) * size, (0.15F + rand.nextFloat() * 0.05F) * size);
		// setLightmapTextureCoords(210 * shade, 0): block light only, sky light 0
		int light = fixedLight >= 0 ? fixedLight : (int) (210.0F * shade);
		int argb = 0xFF000000 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
		RenderType type = RenderTypes.entityTranslucent(TEXTURE);
		collector.submitCustomGeometry(ps, type, (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			model().render(local, vc, light, OverlayTexture.NO_OVERLAY, argb);
		});
		ps.popPose();
	}

	private static void translateFromOrientation(PoseStack ps, int orientation) {
		switch (orientation) {
			case 0 -> {
				ps.translate(0.5F, 1.3F, 0.5F);
				ps.rotateDegrees(Axis.XP, 180.0F);
			}
			case 1 -> ps.translate(0.5F, -0.3F, 0.5F);
			case 2 -> {
				ps.translate(0.5F, 0.5F, 1.3F);
				ps.rotateDegrees(Axis.XP, -90.0F);
			}
			case 3 -> {
				ps.translate(0.5F, 0.5F, -0.3F);
				ps.rotateDegrees(Axis.XP, 90.0F);
			}
			case 4 -> {
				ps.translate(1.3F, 0.5F, 0.5F);
				ps.rotateDegrees(Axis.ZP, 90.0F);
			}
			case 5 -> {
				ps.translate(-0.3F, 0.5F, 0.5F);
				ps.rotateDegrees(Axis.ZP, -90.0F);
			}
			default -> {
			}
		}
	}

	/** BlockCrystalRenderer.renderInventoryBlock: the tile renderer, turned 90 degrees, pointing up. */
	public record ItemRenderer(int kind) implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			ps.pushPose();
			ps.translate(0.5F, 0.0F, 0.5F);
			ps.rotateDegrees(Axis.YP, 90.0F);
			ps.translate(-0.5F, 0.0F, -0.5F);
			draw(ps, collector, this.kind, Direction.UP.get3DDataValue(), this.kind, light);
			ps.popPose();
		}

		@Override
		public void getExtents(Consumer<Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new Vector3f(i & 1, i >> 1 & 1, i >> 2 & 1));
			}
		}
	}

	public record Unbaked(int kind) implements SpecialModelRenderer.Unbaked<Void> {
		public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			com.mojang.serialization.Codec.INT.fieldOf("kind").forGetter(Unbaked::kind)).apply(i, Unbaked::new));

		@Override
		public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext ctx) {
			return new ItemRenderer(this.kind);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}
	}
}
