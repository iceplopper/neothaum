package dev.thaumvibe.client.render;

import dev.thaumvibe.entity.projectile.PrimalArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

/**
 * Port of RenderPrimalArrow:
 * Renders fired primal arrows with aspect-tinted flight particles.
 */
public class PrimalArrowRenderer extends ArrowRenderer<PrimalArrowEntity, PrimalArrowRenderer.PrimalState> {
	private static final Identifier ARROW_LOCATION = Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");

	public static class PrimalState extends ArrowRenderState {
		public int primalType;
	}

	public PrimalArrowRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public PrimalState createRenderState() {
		return new PrimalState();
	}

	@Override
	public void extractRenderState(PrimalArrowEntity entity, PrimalState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.primalType = entity.getPrimalType();
	}

	@Override
	protected Identifier getTextureLocation(PrimalState state) {
		return ARROW_LOCATION;
	}
}
