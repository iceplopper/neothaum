package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.blocks.ArcaneFurnaceBlock;
import dev.thaumvibe.tiles.ArcaneFurnaceBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ArcaneFurnaceRenderer implements BlockEntityRenderer<ArcaneFurnaceBlockEntity, ArcaneFurnaceRenderer.State> {
	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
	}

	public ArcaneFurnaceRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 64;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ArcaneFurnaceBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(be, s, crumbling);
		BlockState state = be.getBlockState();
		if (state.hasProperty(ArcaneFurnaceBlock.FACING)) {
			s.facing = state.getValue(ArcaneFurnaceBlock.FACING);
		}
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
		int light = s.lightCoords;

		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);

		// Render 4 horizontal sides
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			ps.pushPose();
			ps.rotateDegrees(Axis.YP, -dir.toYRot());
			collector.submitCustomGeometry(ps, RenderTypes.cutoutMovingBlock(), (pose, vc) -> {
				renderHorizontalSide(pose, vc, dir == s.facing, atlas, light);
			});
			ps.popPose();
		}

		// Render Bottom face
		collector.submitCustomGeometry(ps, RenderTypes.cutoutMovingBlock(), (pose, vc) -> {
			renderBottom(pose, vc, atlas, light);
		});

		// Render Top face & Chimney (aligned with facing)
		ps.pushPose();
		ps.rotateDegrees(Axis.YP, -s.facing.toYRot());
		collector.submitCustomGeometry(ps, RenderTypes.cutoutMovingBlock(), (pose, vc) -> {
			renderTopAndChimney(pose, vc, atlas, light);
		});
		ps.popPose();

		ps.popPose();
	}

	private static void renderHorizontalSide(PoseStack.Pose pose, VertexConsumer vc, boolean isFront, TextureAtlas atlas, int light) {
		// Face plane is at z = 1.5, x from -1.5 to 1.5, y from -1.5 to 1.5
		float z = 1.5001F;
		TextureAtlasSprite wall = atlas.getSprite(Thaumvibe.id("blocks/furnace6"));
		for (int row = 0; row < 3; row++) {
			float y0 = -1.5F + row;
			float y1 = y0 + 1.0F;
			for (int col = 0; col < 3; col++) {
				float x0 = -1.5F + col;
				float x1 = x0 + 1.0F;

				if (isFront && row == 1 && col == 1) {
					// Mouth opening
					TextureAtlasSprite bars = atlas.getSprite(Thaumvibe.id("blocks/furnace13"));
					TextureAtlasSprite dark = atlas.getSprite(Thaumvibe.id("blocks/furnace15"));
					TextureAtlasSprite fire = atlas.getSprite(Identifier.parse("minecraft:block/fire_0"));
					TextureAtlasSprite lava = atlas.getSprite(Identifier.parse("minecraft:block/lava_still"));

					// Recessed iron bars
					quad(pose, vc, x0, x1, y0, y1, z - 0.15F, bars, light, 0.0F, 0.0F, 1.0F);
					// Dark interior grin
					quad(pose, vc, x0, x1, y0, y1, z - 0.45F, dark, light, 0.0F, 0.0F, 1.0F);
					// Fire
					quad(pose, vc, x0, x1, y0, y1, z - 0.35F, fire, 0xF000F0, 0.0F, 0.0F, 1.0F);

					// Mouth tunnel (from z = 0.5 to 1.5, y from -0.5 to 0.5)
					// Tunnel left wall (facing +X)
					quadZ(pose, vc, 1.5F, 0.5F, y0, y1, x0, wall, light, 1.0F, 0.0F, 0.0F);
					// Tunnel right wall (facing -X)
					quadZ(pose, vc, 0.5F, 1.5F, y0, y1, x1, wall, light, -1.0F, 0.0F, 0.0F);
					// Tunnel floor (facing +Y)
					quadY(pose, vc, x0, x1, 0.5F, 1.5F, y0, wall, light, 0.0F, 1.0F, 0.0F, false);
					// Tunnel ceiling (facing -Y)
					quadY(pose, vc, x0, x1, 0.5F, 1.5F, y1, wall, light, 0.0F, -1.0F, 0.0F, true);

					// Central chamber (z from -0.5 to 0.5, y from -0.5 to 0.5)
					// Chamber back wall (facing +Z)
					quad(pose, vc, x0, x1, y0, y1, -0.5F, wall, light, 0.0F, 0.0F, 1.0F);
					// Chamber left wall (facing +X)
					quadZ(pose, vc, 0.5F, -0.5F, y0, y1, x0, wall, light, 1.0F, 0.0F, 0.0F);
					// Chamber right wall (facing -X)
					quadZ(pose, vc, -0.5F, 0.5F, y0, y1, x1, wall, light, -1.0F, 0.0F, 0.0F);
					// Chamber floor (facing +Y)
					quadY(pose, vc, x0, x1, -0.5F, 0.5F, y0, wall, light, 0.0F, 1.0F, 0.0F, false);

					// Chamber lava pool at y = -0.25 (facing +Y)
					quadY(pose, vc, x0, x1, -0.5F, 0.5F, -0.25F, lava, 0xF000F0, 0.0F, 1.0F, 0.0F, false);
					// Lava front step at z = 0.5 (facing +Z)
					quad(pose, vc, x0, x1, -0.5F, -0.25F, 0.5F, lava, 0xF000F0, 0.0F, 0.0F, 1.0F);
				} else {
					int iconIdx;
					if (isFront) {
						int[][] frontIcons = {
							{3, 4, 5},
							{12, 13, 14},
							{21, 22, 23}
						};
						iconIdx = frontIcons[row][col];
					} else {
						int[][] sideIcons = {
							{0, 1, 2},
							{9, 10, 11},
							{18, 19, 20}
						};
						iconIdx = sideIcons[row][col];
					}
					TextureAtlasSprite sprite = atlas.getSprite(Thaumvibe.id("blocks/furnace" + iconIdx));
					quad(pose, vc, x0, x1, y0, y1, z, sprite, light, 0.0F, 0.0F, 1.0F);
				}
			}
		}
	}

	private static void renderBottom(PoseStack.Pose pose, VertexConsumer vc, TextureAtlas atlas, int light) {
		float y = -1.5001F;
		int[][] bottomIcons = {
			{0, 1, 2},
			{9, 10, 11},
			{18, 19, 20}
		};
		for (int r = 0; r < 3; r++) {
			float z0 = -1.5F + r;
			float z1 = z0 + 1.0F;
			for (int c = 0; c < 3; c++) {
				float x0 = -1.5F + c;
				float x1 = x0 + 1.0F;
				TextureAtlasSprite sp = atlas.getSprite(Thaumvibe.id("blocks/furnace" + bottomIcons[r][c]));
				quadY(pose, vc, x0, x1, z0, z1, y, sp, light, 0.0F, -1.0F, 0.0F, true);
			}
		}
	}

	private static void renderTopAndChimney(PoseStack.Pose pose, VertexConsumer vc, TextureAtlas atlas, int light) {
		float y = 1.5001F;
		for (int r = 0; r < 3; r++) {
			float z0 = -1.5F + r;
			float z1 = z0 + 1.0F;
			for (int c = 0; c < 3; c++) {
				if (r == 1 && c == 1) {
					// Chimney opening (Air)
					continue;
				}
				float x0 = -1.5F + c;
				float x1 = x0 + 1.0F;
				int iconIdx;
				if (r == 0 && c == 1) iconIdx = 16;
				else if (r == 1 && c == 0) iconIdx = 17;
				else if (r == 1 && c == 2) iconIdx = 26;
				else if (r == 2 && c == 1) iconIdx = 25;
				else {
					int[][] cornerIcons = {
						{0, 1, 2},
						{9, 10, 11},
						{18, 19, 20}
					};
					iconIdx = cornerIcons[r][c];
				}
				TextureAtlasSprite sp = atlas.getSprite(Thaumvibe.id("blocks/furnace" + iconIdx));
				quadY(pose, vc, x0, x1, z0, z1, y, sp, light, 0.0F, 1.0F, 0.0F, false);
			}
		}

		// Inner walls of the chimney
		TextureAtlasSprite wall = atlas.getSprite(Thaumvibe.id("blocks/furnace6"));
		float cy0 = -0.25F;
		float cy1 = 1.5F;
		float hx0 = -0.5F, hx1 = 0.5F;
		float hz0 = -0.5F, hz1 = 0.5F;

		// Back (North) inner wall - facing +Z (inward)
		quad(pose, vc, hx0, hx1, cy0, cy1, hz0, wall, light, 0.0F, 0.0F, 1.0F);
		// Front (South) inner wall - facing -Z (inward) above the mouth opening
		quad(pose, vc, hx1, hx0, 0.5F, cy1, hz1, wall, light, 0.0F, 0.0F, -1.0F);
		// Left (West) inner wall - facing +X (inward)
		quadZ(pose, vc, hz1, hz0, cy0, cy1, hx0, wall, light, 1.0F, 0.0F, 0.0F);
		// Right (East) inner wall - facing -X (inward)
		quadZ(pose, vc, hz0, hz1, cy0, cy1, hx1, wall, light, -1.0F, 0.0F, 0.0F);

		// Lava pool at bottom of chimney
		TextureAtlasSprite lava = atlas.getSprite(Identifier.parse("minecraft:block/lava_still"));
		quadY(pose, vc, hx0, hx1, hz0, hz1, cy0, lava, 0xF000F0, 0.0F, 1.0F, 0.0F, false);
	}

	private static void quad(PoseStack.Pose pose, VertexConsumer vc, float x0, float x1, float y0, float y1, float z,
		TextureAtlasSprite sp, int light, float nx, float ny, float nz) {
		vc.addVertex(pose, x0, y1, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU0(), sp.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x0, y0, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU0(), sp.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x1, y0, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU1(), sp.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x1, y1, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU1(), sp.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
	}

	private static void quadZ(PoseStack.Pose pose, VertexConsumer vc, float z0, float z1, float y0, float y1, float x,
		TextureAtlasSprite sp, int light, float nx, float ny, float nz) {
		vc.addVertex(pose, x, y1, z0).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU0(), sp.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x, y0, z0).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU0(), sp.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x, y0, z1).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU1(), sp.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		vc.addVertex(pose, x, y1, z1).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU1(), sp.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
	}

	private static void quadY(PoseStack.Pose pose, VertexConsumer vc, float x0, float x1, float z0, float z1, float y,
		TextureAtlasSprite sp, int light, float nx, float ny, float nz, boolean down) {
		if (down) {
			vc.addVertex(pose, x0, y, z1).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU0(), sp.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
			vc.addVertex(pose, x0, y, z0).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU0(), sp.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
			vc.addVertex(pose, x1, y, z0).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU1(), sp.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
			vc.addVertex(pose, x1, y, z1).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU1(), sp.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		} else {
			vc.addVertex(pose, x0, y, z0).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU0(), sp.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
			vc.addVertex(pose, x0, y, z1).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU0(), sp.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
			vc.addVertex(pose, x1, y, z1).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU1(), sp.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
			vc.addVertex(pose, x1, y, z0).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(sp.getU1(), sp.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
		}
	}
}
