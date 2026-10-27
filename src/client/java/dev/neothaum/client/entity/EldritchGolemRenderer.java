package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.EldritchGolemEntity;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;

/**
 * EldritchGolemRenderer: colossal construct boss of the Outer Lands.
 */
public class EldritchGolemRenderer extends MobRenderer<EldritchGolemEntity, EldritchGolemRenderer.State, EldritchGolemRenderer.Model> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/entity/golem/golem_thaumium.png");

	public static class State extends IronGolemRenderState {
		public boolean headless = false;
	}

	public static class Model extends IronGolemModel {
		public final ModelPart headPart;

		public Model(ModelPart root) {
			super(root);
			this.headPart = root.getChild("head");
		}
	}

	public EldritchGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new Model(context.bakeLayer(ModelLayers.IRON_GOLEM)), 1.0F);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EldritchGolemEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.headless = entity.isHeadless();
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return TEXTURE;
	}

	@Override
	protected void scale(State state, PoseStack ps) {
		ps.scale(1.2F, 1.2F, 1.2F);
		this.getModel().headPart.visible = !state.headless;
	}
}
