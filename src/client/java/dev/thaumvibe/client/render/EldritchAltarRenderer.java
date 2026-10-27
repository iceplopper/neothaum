package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.tiles.eldritch.EldritchAltarBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 1:1 Port of TileEldritchCapRenderer (altar portion).
 * Renders slotted Eldritch Eyes around the 4 sockets of the Eldritch Altar.
 */
public class EldritchAltarRenderer implements BlockEntityRenderer<EldritchAltarBlockEntity, EldritchAltarRenderer.State> {
	public static class State extends BlockEntityRenderState {
		final ItemStackRenderState eyeItem = new ItemStackRenderState();
		byte eyes;
	}

	private final ItemModelResolver itemModelResolver;
	private final ItemStack eyeStack = new ItemStack(TVItems.ELDRITCH_EYE);

	public EldritchAltarRenderer(BlockEntityRendererProvider.Context ctx) {
		this.itemModelResolver = ctx.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EldritchAltarBlockEntity altar, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(altar, s, pt, cam, crumbling);
		s.eyes = altar.getEyes();
		if (s.eyes > 0) {
			this.itemModelResolver.updateForTopItem(s.eyeItem, this.eyeStack, ItemDisplayContext.GROUND, altar.getLevel(), null, 0);
		} else {
			s.eyeItem.clear();
		}
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (s.eyes <= 0 || s.eyeItem.isEmpty()) {
			return;
		}

		for (int a = 0; a < s.eyes; a++) {
			ps.pushPose();
			ps.translate(0.5F, 0.42F, 0.5F);
			ps.rotateDegrees(Axis.YP, a * 90.0F);
			ps.translate(0.44F, 0.0F, 0.0F);
			ps.rotateDegrees(Axis.YP, 90.0F);
			ps.rotateDegrees(Axis.XP, 18.0F);
			ps.scale(0.5F, 0.5F, 0.5F);
			s.eyeItem.submit(ps, collector, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			ps.popPose();
		}
	}
}
