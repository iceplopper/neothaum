package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.tiles.InfusionMatrixBlockEntity;
import java.util.Random;
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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of TileRunicMatrixRenderer: eight runed cubes that tilt into a spinning diamond as the matrix wakes, trembling with instability while it
 * works, under a pulsing violet glow, and wreathed in a halo of light beams while crafting.
 */
public class RunicMatrixRenderer implements BlockEntityRenderer<InfusionMatrixBlockEntity, RunicMatrixRenderer.State> {
	private static final Identifier TEXTURE = Thaumvibe.id("textures/models/infuser.png");
	private static ModelPart cube;
	private static ModelPart cubeOver;

	public static class State extends BlockEntityRenderState {
		float ticks;
		float startUp;
		float instability;
		boolean active;
		boolean crafting;
		int craftCount;
	}

	public RunicMatrixRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	/** ModelCube(shift): one 16-pixel cube on a 64x64 sheet, the runes at v=0 and their glow at v=32. */
	private static ModelPart cube(int shift) {
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, shift).mirror().addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16),
			PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64).bakeRoot();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	/** The halo reaches well past the block. */
	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public void extractRenderState(InfusionMatrixBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		var viewer = Minecraft.getInstance().getCameraEntity();
		s.ticks = (viewer == null ? 0 : viewer.tickCount) + pt;
		s.startUp = be.startUp;
		s.active = be.active;
		s.crafting = be.crafting;
		s.craftCount = be.craftCount;
		s.instability = Math.min(6.0F, 1.0F + be.instability * 0.66F * (Math.min(be.craftCount, 50) / 50.0F));
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (cube == null) {
			cube = cube(0);
			cubeOver = cube(32);
		}
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);
		ps.rotateDegrees(Axis.YP, s.ticks % 360.0F * s.startUp);
		ps.rotateDegrees(Axis.XP, 35.0F * s.startUp);
		ps.rotateDegrees(Axis.ZP, 45.0F * s.startUp);
		this.cubes(s, ps, collector, false);
		if (s.active) {
			this.cubes(s, ps, collector, true);
		}
		ps.popPose();
		if (s.crafting) {
			this.drawHalo(ps, collector, s.craftCount);
		}
	}

	private void cubes(State s, PoseStack ps, SubmitNodeCollector collector, boolean over) {
		float inst = s.instability;
		for (int a = 0; a < 2; a++) {
			for (int b = 0; b < 2; b++) {
				for (int c = 0; c < 2; c++) {
					float b1 = 0.0F, b2 = 0.0F, b3 = 0.0F;
					if (s.active) {
						b1 = Mth.sin((s.ticks + a * 10) / (15.0F - inst / 2.0F)) * 0.01F * s.startUp * inst;
						b2 = Mth.sin((s.ticks + b * 10) / (14.0F - inst / 2.0F)) * 0.01F * s.startUp * inst;
						b3 = Mth.sin((s.ticks + c * 10) / (13.0F - inst / 2.0F)) * 0.01F * s.startUp * inst;
					}
					ps.pushPose();
					ps.translate(b1 + (a == 0 ? -1 : 1) * 0.25F, b2 + (b == 0 ? -1 : 1) * 0.25F, b3 + (c == 0 ? -1 : 1) * 0.25F);
					if (a > 0) {
						ps.rotateDegrees(Axis.XP, 90.0F);
					}
					if (b > 0) {
						ps.rotateDegrees(Axis.YP, 90.0F);
					}
					if (c > 0) {
						ps.rotateDegrees(Axis.ZP, 90.0F);
					}
					ps.scale(0.45F, 0.45F, 0.45F);
					if (over) {
						// the glow: full-bright, additive, pulsing a little out of step on each cube
						float alpha = (Mth.sin((s.ticks + a * 2 + b * 3 + c * 4) / 4.0F) * 0.1F + 0.2F) * s.startUp;
						int argb = (int) (alpha * 255) << 24 | (int) (0.8F * 255) << 16 | (int) (0.1F * 255) << 8 | 255;
						collector.submitCustomGeometry(ps, TVRenderTypes.glow(TEXTURE, true, true), (pose, vc) -> {
							PoseStack local = new PoseStack();
							local.last().set(pose);
							cubeOver.render(local, vc, 0xF000F0, OverlayTexture.NO_OVERLAY, argb);
						});
					} else {
						int light = s.lightCoords;
						collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
							PoseStack local = new PoseStack();
							local.last().set(pose);
							cube.render(local, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
						});
					}
					ps.popPose();
				}
			}
		}
	}

	/** drawHalo: twenty beams fanning out in fixed random directions, growing with the craft and slowly turning. */
	private void drawHalo(PoseStack ps, SubmitNodeCollector collector, int count) {
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);
		float f1 = count / 500.0F;
		Random random = new Random(245L);
		for (int i = 0; i < 20; i++) {
			ps.rotateDegrees(Axis.XP, random.nextFloat() * 360.0F);
			ps.rotateDegrees(Axis.YP, random.nextFloat() * 360.0F);
			ps.rotateDegrees(Axis.ZP, random.nextFloat() * 360.0F);
			ps.rotateDegrees(Axis.XP, random.nextFloat() * 360.0F);
			ps.rotateDegrees(Axis.YP, random.nextFloat() * 360.0F);
			ps.rotateDegrees(Axis.ZP, random.nextFloat() * 360.0F + f1 * 360.0F);
			float fa = random.nextFloat() * 20.0F + 5.0F;
			float f4 = random.nextFloat() * 2.0F + 1.0F;
			float grow = Math.min(count, 50) / 50.0F;
			fa /= 20.0F / grow;
			f4 /= 20.0F / grow;
			float len = fa, w = f4;
			collector.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, vc) -> {
				Vector3f p1 = new Vector3f(-0.866F * w, len, -0.5F * w);
				Vector3f p2 = new Vector3f(0.866F * w, len, -0.5F * w);
				Vector3f p3 = new Vector3f(0.0F, len, 1.0F * w);
				fan(vc, pose, p1, p2);
				fan(vc, pose, p2, p3);
				fan(vc, pose, p3, p1);
			});
		}
		ps.popPose();
	}

	/** One triangle of the fan, as a quad with its last corner doubled: white at the centre, fading to violet. */
	private static void fan(VertexConsumer vc, PoseStack.Pose pose, Vector3f a, Vector3f b) {
		vc.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255);
		vc.addVertex(pose, a.x, a.y, a.z).setColor(0xCC, 0x00, 0xFF, 0);
		vc.addVertex(pose, b.x, b.y, b.z).setColor(0xCC, 0x00, 0xFF, 0);
		vc.addVertex(pose, b.x, b.y, b.z).setColor(0xCC, 0x00, 0xFF, 0);
	}

	/** renderInventoryBlock: the eight cubes at rest. */
	public record ItemRenderer() implements net.minecraft.client.renderer.special.NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			if (cube == null) {
				cube = cube(0);
				cubeOver = cube(32);
			}
			ps.pushPose();
			ps.translate(0.5F, 0.5F, 0.5F);
			for (int a = 0; a < 2; a++) {
				for (int b = 0; b < 2; b++) {
					for (int c = 0; c < 2; c++) {
						ps.pushPose();
						ps.translate((a == 0 ? -1 : 1) * 0.25F, (b == 0 ? -1 : 1) * 0.25F, (c == 0 ? -1 : 1) * 0.25F);
						if (a > 0) {
							ps.rotateDegrees(Axis.XP, 90.0F);
						}
						if (b > 0) {
							ps.rotateDegrees(Axis.YP, 90.0F);
						}
						if (c > 0) {
							ps.rotateDegrees(Axis.ZP, 90.0F);
						}
						ps.scale(0.45F, 0.45F, 0.45F);
						collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
							PoseStack local = new PoseStack();
							local.last().set(pose);
							cube.render(local, vc, light, overlay, 0xFFFFFFFF);
						});
						ps.popPose();
					}
				}
			}
			ps.popPose();
		}

		@Override
		public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new Vector3f(i & 1, i >> 1 & 1, i >> 2 & 1));
			}
		}
	}

	public record Unbaked() implements net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<Void> {
		public static final com.mojang.serialization.MapCodec<Unbaked> CODEC = com.mojang.serialization.MapCodec.unit(new Unbaked());

		@Override
		public net.minecraft.client.renderer.special.SpecialModelRenderer<Void> bake(net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext ctx) {
			return new ItemRenderer();
		}

		@Override
		public com.mojang.serialization.MapCodec<Unbaked> type() {
			return CODEC;
		}
	}
}
