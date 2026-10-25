package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.tiles.PedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Port of TilePedestalRenderer: the item turns slowly and bobs above the pedestal, blocks drawn at double size as dropped items. */
public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderer.State> {
	public static class State extends BlockEntityRenderState {
		final ItemStackRenderState item = new ItemStackRenderState();
		float ticks;
		boolean block;
	}

	private final ItemModelResolver itemModelResolver;

	public PedestalRenderer(BlockEntityRendererProvider.Context ctx) {
		this.itemModelResolver = ctx.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(PedestalBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		ItemStack stack = be.getItem();
		if (stack.isEmpty()) {
			s.item.clear();
			return;
		}
		this.itemModelResolver.updateForTopItem(s.item, stack.copyWithCount(1), ItemDisplayContext.GROUND, be.getLevel(), null, 0);
		var viewer = Minecraft.getInstance().getCameraEntity();
		s.ticks = (viewer == null ? 0 : viewer.tickCount) + pt;
		s.block = stack.getItem() instanceof BlockItem;
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (s.item.isEmpty()) {
			return;
		}
		float h = Mth.sin(s.ticks % 32767.0F / 16.0F) * 0.05F;
		ps.pushPose();
		ps.translate(0.5F, 1.15F + h, 0.5F);
		ps.rotateDegrees(Axis.YP, s.ticks % 360.0F);
		if (s.block) {
			ps.scale(2.0F, 2.0F, 2.0F);
		}
		s.item.submit(ps, collector, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		ps.popPose();
	}
}
