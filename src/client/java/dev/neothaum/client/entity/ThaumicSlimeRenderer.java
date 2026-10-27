package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.ThaumicSlime;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * RenderThaumicSlime: ModelSlime(16) inside a translucent ModelSlime(0) shell, both from tslime.png, squashed and stretched as it hops and
 * scaled by the square root of its size.
 */
public class ThaumicSlimeRenderer extends MobRenderer<ThaumicSlime, SlimeRenderState, SlimeModel> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/tslime.png");

	public ThaumicSlimeRenderer(EntityRendererProvider.Context context) {
		super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
		SlimeModel outer = new SlimeModel(context.bakeLayer(ModelLayers.SLIME_OUTER));
		this.addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack ps, SubmitNodeCollector collector, int light, SlimeRenderState state, float yRot, float xRot) {
				if (!state.isInvisible) {
					collector.order(1).submitModel(outer, state, ps, RenderTypes.entityTranslucent(TEXTURE), light,
						LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
				}
			}
		});
	}

	@Override
	public SlimeRenderState createRenderState() {
		return new SlimeRenderState();
	}

	@Override
	public void extractRenderState(ThaumicSlime entity, SlimeRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.size = entity.getSlimeSize();
		state.squish = entity.prevSquishFactor + (entity.squishFactor - entity.prevSquishFactor) * partialTicks;
	}

	@Override
	public Identifier getTextureLocation(SlimeRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(SlimeRenderState state, PoseStack ps) {
		float f1 = (float) Math.sqrt(state.size);
		float f2 = state.squish / (f1 * 0.25F + 1.0F);
		float f3 = 1.0F / (f2 + 1.0F);
		ps.scale(f3 * f1 + 0.1F, 1.0F / f3 * f1 + 0.1F, f3 * f1 + 0.1F);
	}
}
