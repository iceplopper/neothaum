package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.EldritchCrabEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Port of RenderEldritchCrab and ModelEldritchCrab: the crab, its tail in a cultist helm or bare, with a second pass of craboverlay.png drawn
 * at a fixed brightness of 200 so its markings stay lit in the dark.
 */
public class EldritchCrabRenderer extends MobRenderer<EldritchCrabEntity, EldritchCrabRenderer.State, EldritchCrabRenderer.CrabModel> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/crab.png");
	private static final Identifier OVERLAY = NeoThaum.id("textures/models/craboverlay.png");

	public static class State extends LivingEntityRenderState {
		public boolean hasHelm = true;
	}

	public static class CrabModel extends EntityModel<State> {
		//@FIELDS

		public CrabModel(ModelPart root) {
			super(root);
			//@CTOR
		}

		//@LAYER

		@Override
		public void setupAnim(State s) {
			super.setupAnim(s);
			float par1 = s.walkAnimationPos, par2 = s.walkAnimationSpeed;
			this.tailHelm.visible = s.hasHelm;
			this.tailBare.visible = !s.hasHelm;
			rot(this.rRLeg1, 0.0F, 0.2094395F, 0.4363323F);
			rot(this.rFLeg1, 0.0F, -0.2094395F, 0.4363323F);
			rot(this.lRLeg1, 0.0F, -0.2094395F, -0.4363323F);
			rot(this.lFLeg1, 0.0F, 0.2094395F, -0.4363323F);
			rot(this.rRLeg0, 0.0F, 0.2094395F, 0.4363323F);
			rot(this.rFLeg0, 0.0F, -0.2094395F, 0.4363323F);
			rot(this.lFLeg0, 0.0F, 0.2094395F, -0.4363323F);
			rot(this.lRLeg0, 0.0F, -0.2094395F, -0.4363323F);
			float f9 = -(Mth.cos(par1 * 0.6662F * 2.0F) * 0.4F) * par2;
			float f10 = -(Mth.cos(par1 * 0.6662F * 2.0F + Mth.PI) * 0.4F) * par2;
			this.rRLeg1.yRot += f9;
			this.rRLeg0.yRot += f9;
			this.lRLeg1.yRot -= f9;
			this.lRLeg0.yRot -= f9;
			this.rFLeg1.yRot += f10;
			this.rFLeg0.yRot += f10;
			this.lFLeg1.yRot -= f10;
			this.lFLeg0.yRot -= f10;
			this.rRLeg1.zRot += f9;
			this.rRLeg0.zRot += f9;
			this.lRLeg1.zRot -= f9;
			this.lRLeg0.zRot -= f9;
			this.rFLeg1.zRot += f10;
			this.rFLeg0.zRot += f10;
			this.lFLeg1.zRot -= f10;
			this.lFLeg0.zRot -= f10;
			this.tailBare.yRot = this.tailHelm.yRot = Mth.cos(par1 * 0.6662F) * 2.0F * par2 * 0.125F;
			this.tailBare.zRot = this.tailHelm.zRot = Mth.cos(par1 * 0.6662F) * par2 * 0.125F;
			float t = s.ageInTicks;
			this.rClaw2.xRot = 0.3141593F - Mth.sin(t / 4.0F) * 0.25F;
			this.lClaw2.xRot = 0.3141593F + Mth.sin(t / 4.1F) * 0.25F;
			this.rClaw1.xRot = Mth.sin(t / 4.0F) * 0.125F;
			this.lClaw1.xRot = -Mth.sin(t / 4.1F) * 0.125F;
		}

		private static void rot(ModelPart p, float x, float y, float z) {
			p.xRot = x;
			p.yRot = y;
			p.zRot = z;
		}
	}

	/** shouldRenderPass 0: craboverlay.png over the same pose, translucent, at brightness 200. */
	static class OverlayLayer extends RenderLayer<State, CrabModel> {
		OverlayLayer(RenderLayerParent<State, CrabModel> parent) {
			super(parent);
		}

		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, State s, float yRot, float xRot) {
			if (s.isInvisible) {
				return;
			}
			collector.order(1).submitModel(this.getParentModel(), s, ps, RenderTypes.entityTranslucent(OVERLAY), 200, OverlayTexture.NO_OVERLAY,
				s.outlineColor);
		}
	}

	public EldritchCrabRenderer(EntityRendererProvider.Context context) {
		super(context, new CrabModel(CrabModel.createLayer().bakeRoot()), 1.0F);
		this.addLayer(new OverlayLayer(this));
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EldritchCrabEntity entity, State s, float partialTicks) {
		super.extractRenderState(entity, s, partialTicks);
		s.hasHelm = entity.hasHelm();
	}

	@Override
	public Identifier getTextureLocation(State s) {
		return TEXTURE;
	}
}
