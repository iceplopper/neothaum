package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.blocks.ResearchTableBlock;
import dev.neothaum.items.ScribingToolsItem;
import dev.neothaum.research.ResearchNoteData;
import dev.neothaum.tiles.ResearchTableBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TileResearchTableRenderer for the parts that change: the inkwell and quill when scribing tools are in, the stack of parchment,
 * and the rolled research note (ribbon in the note's colour). The table itself stays a block model. The note's scroll is turned by
 * 10 radians in ModelResearchTable, an angle block models can't express, so it has to be drawn here.
 */
public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity, ResearchTableRenderer.State> {
	private static final Identifier TABLE = NeoThaum.id("textures/models/restable.png");
	private static final Identifier SCROLL = NeoThaum.id("textures/models/restable2.png");
	private static final Identifier PARCHMENT = NeoThaum.id("textures/misc/parchment.png");
	private static final Identifier QUILL = NeoThaum.id("textures/blocks/tablequill.png");
	private static ModelPart inkwell, scrollTube, scrollRibbon;

	private static void bake() {
		if (inkwell != null) {
			return;
		}
		MeshDefinition mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("inkwell", CubeListBuilder.create().texOffs(0, 44).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 2, 3),
			PartPose.offset(-6.0F, -2.0F, 3.0F));
		root.addOrReplaceChild("scroll_tube", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-21.0F, -0.5F, -8.0F, 8, 2, 2),
			PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F));
		root.addOrReplaceChild("scroll_ribbon", CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-15.1F, -0.275F, -6.75F, 1, 2, 2),
			PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F));
		ModelPart baked = LayerDefinition.create(mesh, 128, 64).bakeRoot();
		inkwell = baked.getChild("inkwell");
		scrollTube = baked.getChild("scroll_tube");
		scrollRibbon = baked.getChild("scroll_ribbon");
	}

	public static class State extends BlockEntityRenderState {
		boolean main;
		float yaw;
		boolean ink;
		int noteColor = -1;
	}

	public ResearchTableRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ResearchTableBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		var state = be.getBlockState();
		s.main = state.getValue(ResearchTableBlock.MAIN);
		// TC4 metadata 2..5 (north, south, west, east) -> 270, 90, 180, 0 degrees
		Direction facing = state.getValue(ResearchTableBlock.FACING);
		s.yaw = switch (facing) {
			case NORTH -> 270.0F;
			case SOUTH -> 90.0F;
			case WEST -> 180.0F;
			default -> 0.0F;
		};
		s.ink = be.getItem(0).getItem() instanceof ScribingToolsItem;
		ResearchNoteData note = be.getData();
		s.noteColor = note == null ? -1 : note.color;
		if (note == null && !be.getItem(1).isEmpty()) {
			s.noteColor = 10066329;
		}
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!s.main) {
			return;
		}
		bake();
		int light = s.lightCoords;
		ps.pushPose();
		ps.translate(0.5F, 1.0F, 0.5F);
		ps.rotateDegrees(Axis.XP, 180.0F);
		ps.rotateDegrees(Axis.YP, s.yaw);
		if (s.ink) {
			part(ps, collector, inkwell, TABLE, light, 0xFFFFFFFF);
			ps.pushPose();
			ps.rotateDegrees(Axis.YP, -90.0F);
			ps.rotateDegrees(Axis.XP, 180.0F);
			ps.translate(-0.17F, 0.1F, -0.15F);
			ps.rotateDegrees(Axis.YP, 15.0F);
			ps.scale(0.5F, 0.5F, 0.5F);
			// ItemRenderer.renderItemIn2D: the icon mirrored left-right, 0.025 thick
			collector.submitCustomGeometry(ps, RenderTypes.entityCutout(QUILL), (pose, vc) -> {
				for (float z : new float[]{0.0F, -0.025F}) {
					float n = z == 0.0F ? 1.0F : -1.0F;
					float[][] q = {{0, 0, 1, 1}, {1, 0, 0, 1}, {1, 1, 0, 0}, {0, 1, 1, 0}};
					for (int i = 0; i < 4; i++) {
						float[] v = q[z == 0.0F ? i : 3 - i];
						vc.addVertex(pose, v[0], v[1], z).setColor(0xFFFFFFFF).setUv(v[2], v[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
							.setNormal(pose, 0.0F, 0.0F, n);
					}
				}
			});
			ps.popPose();
		}
		for (int a = 0; a < 6; a++) {
			ps.pushPose();
			ps.translate(0.1F, -0.01F - a * 0.015F, 0.35F);
			ps.rotateDegrees(Axis.XP, -90.0F);
			ps.rotateDegrees(Axis.ZP, 15 + a % 3 * 2);
			ps.scale(0.5F, 0.6F, 0.6F);
			collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(PARCHMENT), (pose, vc) -> {
				float[][] q = {{0, 1, 0, 1}, {1, 1, 1, 1}, {1, 0, 1, 0}, {0, 0, 0, 0}};
				for (float[] v : q) {
					vc.addVertex(pose, v[0], v[1], 0.0F).setColor(0xFFFFFFFF).setUv(v[2], v[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
						.setNormal(pose, 0.0F, 0.0F, -1.0F);
				}
			});
			ps.popPose();
		}
		if (s.noteColor != -1) {
			ps.pushPose();
			part(ps, collector, scrollTube, SCROLL, light, 0xFFFFFFFF);
			ps.scale(1.2F, 1.2F, 1.2F);
			part(ps, collector, scrollRibbon, SCROLL, light, 0xFF000000 | s.noteColor);
			ps.popPose();
		}
		ps.popPose();
	}

	private static void part(PoseStack ps, SubmitNodeCollector collector, ModelPart part, Identifier texture, int light, int argb) {
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(texture), (pose, vc) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			part.render(local, vc, light, OverlayTexture.NO_OVERLAY, argb);
		});
	}
}
