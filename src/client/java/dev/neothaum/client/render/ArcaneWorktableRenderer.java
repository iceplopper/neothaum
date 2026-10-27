package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.items.WandItem;
import dev.neothaum.tiles.ArcaneWorktableBlockEntity;
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

/** TileArcaneWorkbenchRenderer: the wand in slot 10 lies on the worktable. */
public class ArcaneWorktableRenderer implements BlockEntityRenderer<ArcaneWorktableBlockEntity, ArcaneWorktableRenderer.State> {
	public static class State extends BlockEntityRenderState {
		final ItemStackRenderState wand = new ItemStackRenderState();
	}

	private final ItemModelResolver itemModelResolver;

	public ArcaneWorktableRenderer(BlockEntityRendererProvider.Context ctx) {
		this.itemModelResolver = ctx.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ArcaneWorktableBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		ItemStack wand = be.getItem(ArcaneWorktableBlockEntity.WAND_SLOT);
		if (wand.getItem() instanceof WandItem) {
			this.itemModelResolver.updateForTopItem(s.wand, wand, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
		} else {
			s.wand.clear();
		}
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (s.wand.isEmpty()) {
			return;
		}
		ps.pushPose();
		ps.translate(0.65F, 1.0625F, 0.25F);
		ps.rotateDegrees(Axis.XP, 90.0F);
		ps.rotateDegrees(Axis.ZP, 20.0F);
		ps.scale(0.5F, 0.5F, 0.5F);
		s.wand.submit(ps, collector, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		ps.popPose();
	}
}
