package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.EldritchGolemEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/** Port of RenderEldritchGolem and ModelEldritchGolem: the cloaked stone colossus, 2.15 times its model's size, that fights on once beheaded. */
public class EldritchGolemRenderer extends MobRenderer<EldritchGolemEntity, EldritchGolemRenderer.State, EldritchGolemRenderer.GolemModel> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/eldritch_golem.png");

	public static class State extends LivingEntityRenderState {
		public boolean headless;
		public int spawnTimer;
		public float attackTimer;
	}

	public static class GolemModel extends EntityModel<State> {
		final ModelPart cloak1;
		final ModelPart cloak3;
		final ModelPart cloak2;
		final ModelPart cloakCL;
		final ModelPart cloakCR;
		final ModelPart head;
		final ModelPart head2;
		final ModelPart collarL;
		final ModelPart collarR;
		final ModelPart collarB;
		final ModelPart collarF;
		final ModelPart collarBlack;
		final ModelPart frontcloth0;
		final ModelPart frontcloth1;
		final ModelPart frontcloth2;
		final ModelPart torso;
		final ModelPart armR;
		final ModelPart shoulderR1;
		final ModelPart shoulderR;
		final ModelPart shoulderR2;
		final ModelPart shoulderR0;
		final ModelPart armL;
		final ModelPart shoulderL1;
		final ModelPart shoulderL0;
		final ModelPart shoulderL;
		final ModelPart shoulderL2;
		final ModelPart backpanelR1;
		final ModelPart waistR1;
		final ModelPart waistR2;
		final ModelPart waistR3;
		final ModelPart legR;
		final ModelPart waistL1;
		final ModelPart waistL2;
		final ModelPart waistL3;
		final ModelPart backpanelL1;
		final ModelPart legL;

		public GolemModel(ModelPart root) {
			super(root, RenderTypes::entityTranslucent);
			this.cloak1 = root.getChild("cloak1");
			this.cloak3 = root.getChild("cloak3");
			this.cloak2 = root.getChild("cloak2");
			this.cloakCL = root.getChild("cloak_c_l");
			this.cloakCR = root.getChild("cloak_c_r");
			this.head = root.getChild("head");
			this.head2 = root.getChild("head2");
			this.collarL = root.getChild("collar_l");
			this.collarR = root.getChild("collar_r");
			this.collarB = root.getChild("collar_b");
			this.collarF = root.getChild("collar_f");
			this.collarBlack = root.getChild("collar_black");
			this.frontcloth0 = root.getChild("frontcloth0");
			this.frontcloth1 = root.getChild("frontcloth1");
			this.frontcloth2 = root.getChild("frontcloth2");
			this.torso = root.getChild("torso");
			this.armR = root.getChild("arm_r");
			this.shoulderR1 = root.getChild("arm_r").getChild("shoulder_r1");
			this.shoulderR = root.getChild("arm_r").getChild("shoulder_r");
			this.shoulderR2 = root.getChild("arm_r").getChild("shoulder_r2");
			this.shoulderR0 = root.getChild("arm_r").getChild("shoulder_r0");
			this.armL = root.getChild("arm_l");
			this.shoulderL1 = root.getChild("arm_l").getChild("shoulder_l1");
			this.shoulderL0 = root.getChild("arm_l").getChild("shoulder_l0");
			this.shoulderL = root.getChild("arm_l").getChild("shoulder_l");
			this.shoulderL2 = root.getChild("arm_l").getChild("shoulder_l2");
			this.backpanelR1 = root.getChild("backpanel_r1");
			this.waistR1 = root.getChild("waist_r1");
			this.waistR2 = root.getChild("waist_r2");
			this.waistR3 = root.getChild("waist_r3");
			this.legR = root.getChild("leg_r");
			this.waistL1 = root.getChild("waist_l1");
			this.waistL2 = root.getChild("waist_l2");
			this.waistL3 = root.getChild("waist_l3");
			this.backpanelL1 = root.getChild("backpanel_l1");
			this.legL = root.getChild("leg_l");
		}

		public static LayerDefinition createLayer() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition root = mesh.getRoot();
			root.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-5.0F, 1.5F, 4.0F, 10, 12, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F));
			root.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(0, 37).addBox(-5.0F, 17.5F, -0.8F, 10, 4, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.4465716F, 0.0F, 0.0F));
			root.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-5.0F, 13.5F, 1.7F, 10, 4, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.3069452F, 0.0F, 0.0F));
			root.addOrReplaceChild("cloak_c_l", CubeListBuilder.create().texOffs(0, 43).addBox(3.0F, 0.5F, 2.0F, 2, 1, 3), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F));
			root.addOrReplaceChild("cloak_c_r", CubeListBuilder.create().texOffs(0, 43).addBox(-5.0F, 0.5F, 2.0F, 2, 1, 3), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F));
			root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(47, 12).addBox(-3.5F, -6.0F, -2.5F, 7, 7, 5), PartPose.offsetAndRotation(0.0F, 4.5F, -3.8F, -0.1047198F, 0.0F, 0.0F));
			root.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(26, 16).addBox(-2.0F, -2.0F, -2.0F, 4, 4, 4), PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, -0.1047198F, 0.0F, 0.0F));
			root.addOrReplaceChild("collar_l", CubeListBuilder.create().texOffs(75, 50).addBox(3.5F, -0.5F, -7.0F, 1, 4, 10), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
			root.addOrReplaceChild("collar_r", CubeListBuilder.create().texOffs(67, 50).addBox(-4.5F, -0.5F, -7.0F, 1, 4, 10), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
			root.addOrReplaceChild("collar_b", CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, 2.0F, 7, 4, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
			root.addOrReplaceChild("collar_f", CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, -7.0F, 7, 4, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
			root.addOrReplaceChild("collar_black", CubeListBuilder.create().texOffs(22, 0).addBox(-3.5F, 0.0F, -6.0F, 7, 1, 8), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
			root.addOrReplaceChild("frontcloth0", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 3.2F, -3.5F, 6, 10, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745329F, 0.0F, 0.0F));
			root.addOrReplaceChild("frontcloth1", CubeListBuilder.create().texOffs(114, 39).addBox(-1.0F, 1.5F, -3.5F, 6, 6, 1), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, -0.1047198F, 0.0F, 0.0F));
			root.addOrReplaceChild("frontcloth2", CubeListBuilder.create().texOffs(114, 47).addBox(-1.0F, 8.5F, -1.5F, 6, 3, 1), PartPose.offsetAndRotation(-2.0F, 11.0F, 0.0F, -0.3316126F, 0.0F, 0.0F));
			root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(34, 45).addBox(-5.0F, 2.5F, -3.0F, 10, 10, 6), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745329F, 0.0F, 0.0F));
			PartDefinition p_arm_r = root.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(78, 32).addBox(-3.5F, 1.5F, -2.0F, 4, 13, 5), PartPose.offsetAndRotation(-5.0F, 3.0F, -2.0F, 0.0F, 0.0F, 0.1047198F));
			p_arm_r.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.0F, -3.0F, 4, 5, 7), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F));
			p_arm_r.addOrReplaceChild("shoulder_r0", CubeListBuilder.create().texOffs(56, 31).addBox(-4.5F, -1.5F, -2.5F, 5, 6, 6), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			p_arm_r.addOrReplaceChild("shoulder_r1", CubeListBuilder.create().texOffs(0, 23).addBox(-3.3F, 4.0F, -2.5F, 1, 2, 6), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F));
			p_arm_r.addOrReplaceChild("shoulder_r2", CubeListBuilder.create().texOffs(0, 12).addBox(-2.3F, 4.0F, -3.0F, 2, 3, 7), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F));
			PartDefinition p_arm_l = root.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(78, 32).mirror().addBox(-0.5F, 1.5F, -2.0F, 4, 13, 5).mirror(false), PartPose.offsetAndRotation(5.0F, 3.0F, -2.0F, 0.0F, 0.0F, -0.1047198F));
			p_arm_l.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.3F, -1.0F, -3.0F, 4, 5, 7).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F));
			p_arm_l.addOrReplaceChild("shoulder_l0", CubeListBuilder.create().texOffs(56, 31).mirror().addBox(-0.5F, -1.5F, -2.5F, 5, 6, 6).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			p_arm_l.addOrReplaceChild("shoulder_l1", CubeListBuilder.create().texOffs(0, 23).mirror().addBox(2.3F, 4.0F, -2.5F, 1, 2, 6).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F));
			p_arm_l.addOrReplaceChild("shoulder_l2", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(0.3F, 4.0F, -3.0F, 2, 3, 7).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F));
			root.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(96, 7).addBox(0.0F, 2.5F, -2.5F, 2, 2, 5), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
			root.addOrReplaceChild("waist_r1", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5, 3, 5), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
			root.addOrReplaceChild("waist_r2", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1, 4, 5), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
			root.addOrReplaceChild("waist_r3", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2, 3, 5).mirror(false), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
			root.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(79, 19).addBox(-2.5F, 2.5F, -2.0F, 4, 9, 4), PartPose.offsetAndRotation(-2.0F, 12.5F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("waist_l1", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5, 3, 5), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
			root.addOrReplaceChild("waist_l2", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1, 4, 5), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
			root.addOrReplaceChild("waist_l3", CubeListBuilder.create().texOffs(114, 5).addBox(0.0F, 2.5F, -2.5F, 2, 3, 5), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
			root.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(96, 7).addBox(-2.0F, 2.5F, -2.5F, 2, 2, 5), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
			root.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(79, 19).addBox(-1.5F, 2.5F, -2.0F, 4, 9, 4), PartPose.offsetAndRotation(2.0F, 12.5F, 0.0F, 0.0F, 0.0F, 0.0F));
			return LayerDefinition.create(mesh, 128, 64);
		}

		@Override
		public void setupAnim(State s) {
			super.setupAnim(s);
			float f = s.walkAnimationPos, f1 = s.walkAnimationSpeed;
			if (s.spawnTimer > 0) {
				this.head.yRot = 0.0F;
				this.head.xRot = s.spawnTimer / 2 * Mth.DEG_TO_RAD;
			} else {
				this.head.yRot = s.yRot / 4.0F * Mth.DEG_TO_RAD;
				this.head.xRot = s.xRot / 2.0F * Mth.DEG_TO_RAD;
				this.head2.yRot = s.yRot * Mth.DEG_TO_RAD;
				this.head2.xRot = s.xRot * Mth.DEG_TO_RAD;
			}
			this.head.visible = !s.headless;
			this.head2.visible = s.headless;
			this.legR.xRot = Mth.cos(f * 0.4662F) * 1.4F * f1;
			this.legL.xRot = Mth.cos(f * 0.4662F + Mth.PI) * 1.4F * f1;
			// setLivingAnimations: arms swing with the walk, or slam down while the attack timer runs
			if (s.attackTimer > 0.0F) {
				this.armR.xRot = -2.0F + 1.5F * triangle(s.attackTimer, 10.0F);
				this.armL.xRot = -2.0F + 1.5F * triangle(s.attackTimer, 10.0F);
			} else {
				this.armR.xRot = Mth.cos(f * 0.4F + Mth.PI) * 2.0F * f1 * 0.5F;
				this.armL.xRot = Mth.cos(f * 0.4F) * 2.0F * f1 * 0.5F;
			}
			float a = Mth.cos(f * 0.44F) * 1.4F * f1;
			float b = Mth.cos(f * 0.44F + Mth.PI) * 1.4F * f1;
			float c = Math.min(a, b);
			this.frontcloth1.xRot = c - 0.1047198F;
			this.frontcloth2.xRot = c - 0.3316126F;
			this.cloak1.xRot = -c / 3.0F + 0.1396263F;
			this.cloak2.xRot = -c / 3.0F + 0.3069452F;
			this.cloak3.xRot = -c / 3.0F + 0.4465716F;
		}

		private static float triangle(float x, float period) {
			return (Math.abs(x % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
		}
	}

	public EldritchGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new GolemModel(GolemModel.createLayer().bakeRoot()), 1.5F);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EldritchGolemEntity entity, State s, float partialTicks) {
		super.extractRenderState(entity, s, partialTicks);
		s.headless = entity.isHeadless();
		s.spawnTimer = entity.spawnTimer;
		s.attackTimer = entity.getAttackTimer() > 0 ? entity.getAttackTimer() - partialTicks : 0.0F;
	}

	@Override
	protected @Nullable RenderType getRenderType(State s, boolean bodyVisible, boolean forceTransparent, boolean glowing) {
		return bodyVisible ? RenderTypes.entityTranslucent(TEXTURE) : super.getRenderType(s, bodyVisible, forceTransparent, glowing);
	}

	@Override
	public Identifier getTextureLocation(State s) {
		return TEXTURE;
	}

	@Override
	protected void scale(State s, PoseStack ps) {
		ps.scale(2.15F, 2.15F, 2.15F);
	}
}
