package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.MindSpiderEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** RenderMindSpider: 0.3 scale tiny spider summoned during deep warp paranoia. */
public class MindSpiderRenderer extends MobRenderer<MindSpiderEntity, LivingEntityRenderState, SpiderModel> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/taint_spider.png");
	private static final RenderType EYES = RenderTypes.eyes(NeoThaum.id("textures/models/taint_spider_eyes.png"));

	public MindSpiderRenderer(EntityRendererProvider.Context context) {
		super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.15F);
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
		ps.scale(0.3F, 0.3F, 0.3F);
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
