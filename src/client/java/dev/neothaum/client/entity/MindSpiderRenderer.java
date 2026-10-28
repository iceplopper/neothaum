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

/**
 * RenderMindSpider: the little spiders of warp hallucinations, at 0.3 scale. Only the player haunted by one sees it; its body is a faint
 * ghost (at most a tenth opaque, fading in over its first ten ticks) under fully lit eyes.
 */
public class MindSpiderRenderer extends MobRenderer<MindSpiderEntity, LivingEntityRenderState, SpiderModel> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/taint_spider.png");
	private static final RenderType EYES = RenderTypes.eyes(NeoThaum.id("textures/models/taint_spider_eyes.png"));

	public MindSpiderRenderer(EntityRendererProvider.Context context) {
		super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.0F);
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
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public boolean shouldRender(MindSpiderEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z,
		float partialTicks) {
		var viewer = net.minecraft.client.Minecraft.getInstance().getCameraEntity();
		String haunted = entity.getViewer();
		return (haunted.isEmpty() || viewer != null && haunted.equals(viewer.getName().getString())) && super.shouldRender(entity, frustum, x, y, z, partialTicks);
	}

	@Override
	protected @org.jspecify.annotations.Nullable RenderType getRenderType(LivingEntityRenderState s, boolean bodyVisible, boolean forceTransparent, boolean glowing) {
		return bodyVisible ? RenderTypes.entityTranslucent(TEXTURE) : super.getRenderType(s, bodyVisible, forceTransparent, glowing);
	}

	@Override
	protected int getModelTint(LivingEntityRenderState s) {
		return net.minecraft.util.ARGB.color((int) (Math.min(0.1F, s.ageInTicks / 100.0F) * 255.0F), 0xFFFFFF);
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return TEXTURE;
	}
}
