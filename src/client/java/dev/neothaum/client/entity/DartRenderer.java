package dev.neothaum.client.entity;

import dev.neothaum.golem.DartEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

/** RenderDart: an arrow. */
public class DartRenderer extends ArrowRenderer<DartEntity, ArrowRenderState> {
	private static final Identifier ARROW = Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");

	public DartRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public ArrowRenderState createRenderState() {
		return new ArrowRenderState();
	}

	@Override
	protected Identifier getTextureLocation(ArrowRenderState state) {
		return ARROW;
	}
}
