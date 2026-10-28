package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.tiles.eldritch.EldritchNothingBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.EndPortalRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/** TileEldritchNothingRenderer: a whole block of the starfield, shown on the faces that look out onto open space. */
public class EldritchNothingRenderer extends AbstractEndPortalRenderer<EldritchNothingBlockEntity, EndPortalRenderState> {
	public EldritchNothingRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public EndPortalRenderState createRenderState() {
		return new EndPortalRenderState();
	}

	@Override
	public void submit(EndPortalRenderState state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		submitCube(state.facesToShow, RenderTypes.endPortal(), ps, collector);
	}
}
