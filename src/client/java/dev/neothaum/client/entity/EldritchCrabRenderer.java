package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.EldritchCrabEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * EldritchCrabRenderer: skittering crab arthropod that can wear a cultist helm.
 */
public class EldritchCrabRenderer extends MobRenderer<EldritchCrabEntity, EldritchCrabRenderer.State, SpiderModel> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/spider/cave_spider.png");

	public static class State extends LivingEntityRenderState {
		public boolean hasHelm = true;
	}

	public EldritchCrabRenderer(EntityRendererProvider.Context context) {
		super(context, new SpiderModel(context.bakeLayer(ModelLayers.CAVE_SPIDER)), 0.3F);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EldritchCrabEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.hasHelm = entity.hasHelm();
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return TEXTURE;
	}

	@Override
	protected void scale(State state, PoseStack ps) {
		ps.scale(0.7F, 0.7F, 0.7F);
	}
}
