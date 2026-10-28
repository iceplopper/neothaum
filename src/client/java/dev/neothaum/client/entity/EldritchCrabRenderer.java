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
		final ModelPart tailHelm;
		final ModelPart tailBare;
		final ModelPart rClaw1;
		final ModelPart head1;
		final ModelPart rClaw0;
		final ModelPart rClaw2;
		final ModelPart rArm;
		final ModelPart lClaw2;
		final ModelPart lClaw1;
		final ModelPart lClaw0;
		final ModelPart lArm;
		final ModelPart torso;
		final ModelPart head0;
		final ModelPart rRLeg1;
		final ModelPart rFLeg1;
		final ModelPart lRLeg1;
		final ModelPart lFLeg1;
		final ModelPart rRLeg0;
		final ModelPart rFLeg0;
		final ModelPart lFLeg0;
		final ModelPart lRLeg0;

		public CrabModel(ModelPart root) {
			super(root);
			this.tailHelm = root.getChild("tail_helm");
			this.tailBare = root.getChild("tail_bare");
			this.rClaw1 = root.getChild("r_claw1");
			this.head1 = root.getChild("head1");
			this.rClaw0 = root.getChild("r_claw0");
			this.rClaw2 = root.getChild("r_claw2");
			this.rArm = root.getChild("r_arm");
			this.lClaw2 = root.getChild("l_claw2");
			this.lClaw1 = root.getChild("l_claw1");
			this.lClaw0 = root.getChild("l_claw0");
			this.lArm = root.getChild("l_arm");
			this.torso = root.getChild("torso");
			this.head0 = root.getChild("head0");
			this.rRLeg1 = root.getChild("r_r_leg1");
			this.rFLeg1 = root.getChild("r_f_leg1");
			this.lRLeg1 = root.getChild("l_r_leg1");
			this.lFLeg1 = root.getChild("l_f_leg1");
			this.rRLeg0 = root.getChild("r_r_leg0");
			this.rFLeg0 = root.getChild("r_f_leg0");
			this.lFLeg0 = root.getChild("l_f_leg0");
			this.lRLeg0 = root.getChild("l_r_leg0");
		}

		public static LayerDefinition createLayer() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition root = mesh.getRoot();
			root.addOrReplaceChild("tail_helm", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.5F, -0.4F, 9, 9, 9), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
			root.addOrReplaceChild("tail_bare", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, -4.0F, -0.4F, 8, 8, 8), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
			root.addOrReplaceChild("r_claw1", CubeListBuilder.create().texOffs(0, 47).addBox(-2.0F, -1.0F, -5.066667F, 4, 3, 5), PartPose.offsetAndRotation(-6.0F, 15.5F, -10.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("head1", CubeListBuilder.create().texOffs(0, 38).addBox(-2.0F, -1.5F, -9.066667F, 4, 4, 1), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("r_claw0", CubeListBuilder.create().texOffs(0, 55).addBox(-2.0F, -2.5F, -3.066667F, 4, 5, 3), PartPose.offsetAndRotation(-6.0F, 17.0F, -7.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("r_claw2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3, 2, 5), PartPose.offsetAndRotation(-6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F));
			root.addOrReplaceChild("r_arm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -5.066667F, 2, 2, 6), PartPose.offsetAndRotation(-3.0F, 17.0F, -4.0F, 0.0F, 0.7504916F, 0.0F));
			root.addOrReplaceChild("l_claw2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3, 2, 5), PartPose.offsetAndRotation(6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F));
			root.addOrReplaceChild("l_claw1", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-2.0F, -1.0F, -5.066667F, 4, 3, 5).mirror(false), PartPose.offsetAndRotation(6.0F, 15.5F, -10.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("l_claw0", CubeListBuilder.create().texOffs(0, 55).mirror().addBox(-2.0F, -2.5F, -3.066667F, 4, 5, 3).mirror(false), PartPose.offsetAndRotation(6.0F, 17.0F, -7.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -4.066667F, 2, 2, 6), PartPose.offsetAndRotation(4.0F, 17.0F, -5.0F, 0.0F, -0.7504916F, 0.0F));
			root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -3.5F, -6.066667F, 7, 7, 6), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0523599F, 0.0F, 0.0F));
			root.addOrReplaceChild("head0", CubeListBuilder.create().texOffs(0, 31).addBox(-2.5F, -2.0F, -8.066667F, 5, 5, 2), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("r_r_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(-4.5F, 1.0F, -0.9F, 2, 5, 2), PartPose.offsetAndRotation(-4.0F, 20.0F, -1.5F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("r_f_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(-5.0F, 1.0F, -1.066667F, 2, 5, 2), PartPose.offsetAndRotation(-4.0F, 20.0F, -3.5F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("l_r_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(2.5F, 1.0F, -0.9F, 2, 5, 2), PartPose.offsetAndRotation(4.0F, 20.0F, -1.5F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("l_f_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(3.0F, 1.0F, -1.066667F, 2, 5, 2), PartPose.offsetAndRotation(4.0F, 20.0F, -3.5F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("r_r_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-4.5F, -1.0F, -0.9F, 6, 2, 2), PartPose.offsetAndRotation(-4.0F, 20.0F, -1.5F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("r_f_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-5.0F, -1.0F, -1.066667F, 6, 2, 2), PartPose.offsetAndRotation(-4.0F, 20.0F, -3.5F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("l_f_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-1.0F, -1.0F, -1.066667F, 6, 2, 2), PartPose.offsetAndRotation(4.0F, 20.0F, -3.5F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("l_r_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-1.5F, -1.0F, -0.9F, 6, 2, 2), PartPose.offsetAndRotation(4.0F, 20.0F, -1.5F, 0.0F, 0.0F, 0.0F));
			return LayerDefinition.create(mesh, 128, 64);
		}

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
