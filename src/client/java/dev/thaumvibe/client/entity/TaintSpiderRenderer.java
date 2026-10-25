package dev.thaumvibe.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.entity.monster.TaintSpider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** RenderTaintSpider: the spider model at 0.4 scale (stretched a quarter taller) in taint_spider.png, with glowing eyes. */
public class TaintSpiderRenderer extends MobRenderer<TaintSpider, LivingEntityRenderState, SpiderModel> {
	private static final Identifier TEXTURE = Thaumvibe.id("textures/models/taint_spider.png");
	private static final RenderType EYES = RenderTypes.eyes(Thaumvibe.id("textures/models/taint_spider_eyes.png"));

	public TaintSpiderRenderer(EntityRendererProvider.Context context) {
		super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.5F);
		this.addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return EYES;
			}
		});
	}

	@Override
	protected float getFlipDegrees() {
		return 180.0F;
	}

	@Override
	protected void scale(LivingEntityRenderState state, PoseStack ps) {
		ps.scale(0.4F, 0.5F, 0.4F);
	}

	@Override
	protected float getShadowRadius(LivingEntityRenderState state) {
		return 0.1F;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return TEXTURE;
	}
}
