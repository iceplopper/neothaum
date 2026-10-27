package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.tiles.CrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Port of TileCrucibleRenderer: a water surface at the fluid height, tinted towards purple as essentia builds up. */
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderer.State> {
	private static final int WATER_COLOR = 0x3F76E4;

	public static class State extends BlockEntityRenderState {
		float height;
		int water;
		float recolor;
	}

	public CrucibleRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CrucibleBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.water = be.water;
		s.height = be.getFluidHeight();
		float recolor = be.tagAmount() / 100.0F;
		if (recolor > 0.0F) {
			recolor = 0.5F + recolor / 2.0F;
		}
		s.recolor = Math.min(recolor, 1.0F);
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (s.water <= 0) {
			return;
		}
		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
			.getSprite(Identifier.withDefaultNamespace("block/water_still"));
		float r = (WATER_COLOR >> 16 & 255) / 255.0F * (1.0F - s.recolor / 3.0F);
		float g = (WATER_COLOR >> 8 & 255) / 255.0F * (1.0F - s.recolor);
		float b = (WATER_COLOR & 255) / 255.0F * (1.0F - s.recolor / 2.0F);
		int ri = (int) (r * 255), gi = (int) (g * 255), bi = (int) (b * 255);
		int alpha = (int) net.minecraft.util.Mth.lerp(s.recolor, 180.0F, 220.0F);
		float y = s.height;
		int light = s.lightCoords;
		float x0 = 2.0F / 16.0F;
		float x1 = 14.0F / 16.0F;
		float z0 = 2.0F / 16.0F;
		float z1 = 14.0F / 16.0F;
		float u0 = sprite.getU(x0);
		float u1 = sprite.getU(x1);
		float v0 = sprite.getV(z0);
		float v1 = sprite.getV(z1);

		collector.submitCustomGeometry(ps, RenderTypes.translucentMovingBlock(), (pose, vc) -> {
			vc.addVertex(pose, x0, y, z0).setColor(ri, gi, bi, alpha).setUv(u0, v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
			vc.addVertex(pose, x0, y, z1).setColor(ri, gi, bi, alpha).setUv(u0, v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
			vc.addVertex(pose, x1, y, z1).setColor(ri, gi, bi, alpha).setUv(u1, v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
			vc.addVertex(pose, x1, y, z0).setColor(ri, gi, bi, alpha).setUv(u1, v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
		});
	}
}
