package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.EldritchGuardianEntity;
import dev.neothaum.entity.monster.EldritchWardenEntity;
import net.minecraft.client.Minecraft;
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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Port of RenderEldritchGuardian and ModelEldritchGuardian, shared by the guardian and the warden. The guardian is a translucent shade, at
 * most 60% opaque, that fades out entirely between 16 and 32 blocks away (24 on hard); the warden is solid, half again as large, rises out of
 * the ground when it spawns, and its hood glows.
 */
public class EldritchGuardianRenderer<T extends Mob> extends MobRenderer<T, EldritchGuardianRenderer.State, EldritchGuardianRenderer.GuardianModel> {
	private static final Identifier GUARDIAN = NeoThaum.id("textures/models/eldritch_guardian.png");
	private static final Identifier WARDEN = NeoThaum.id("textures/models/eldritch_warden.png");
	private final boolean warden;

	public static class State extends LivingEntityRenderState {
		public float armLiftL;
		public float armLiftR;
		public float alpha = 1.0F;
		public float sink;
		public boolean warden;
	}

	public static class GuardianModel extends EntityModel<State> {
		final ModelPart beltR;
		final ModelPart mbelt;
		final ModelPart mbeltL;
		final ModelPart mbeltR;
		final ModelPart beltL;
		final ModelPart chestplate;
		final ModelPart hoodEye;
		final ModelPart hood1;
		final ModelPart hood2;
		final ModelPart hood3;
		final ModelPart hood4;
		final ModelPart backplate;
		final ModelPart shoulderplateTopR;
		final ModelPart shoulderplateR1;
		final ModelPart shoulderplateR2;
		final ModelPart shoulderplateR3;
		final ModelPart shoulderR;
		final ModelPart armL1;
		final ModelPart armL2;
		final ModelPart armL3;
		final ModelPart armR1;
		final ModelPart armR2;
		final ModelPart armR3;
		final ModelPart shoulderL;
		final ModelPart shoulderplateLtop;
		final ModelPart shoulderplateL1;
		final ModelPart shoulderplateL2;
		final ModelPart shoulderplateL3;
		final ModelPart legpanelR4;
		final ModelPart legpanelR5;
		final ModelPart legpanelR6;
		final ModelPart backpanelR1;
		final ModelPart backpanelR2;
		final ModelPart backpanelR3;
		final ModelPart backpanelL3;
		final ModelPart legpanelL4;
		final ModelPart legpanelL5;
		final ModelPart legpanelL6;
		final ModelPart backpanelL1;
		final ModelPart backpanelL2;
		final ModelPart sidepanelL1;
		final ModelPart sidepanelR1;
		final ModelPart sidepanelR2;
		final ModelPart sidepanelR3;
		final ModelPart sidepanelR4;
		final ModelPart sidepanelL2;
		final ModelPart sidepanelL3;
		final ModelPart sidepanelL4;
		final ModelPart legpanelC1;
		final ModelPart legpanelC2;
		final ModelPart legpanelC3;
		final ModelPart cloak1;
		final ModelPart cloak2;
		final ModelPart cloak3;

		public GuardianModel(ModelPart root) {
			super(root, RenderTypes::entityTranslucent);
			this.beltR = root.getChild("belt_r");
			this.mbelt = root.getChild("mbelt");
			this.mbeltL = root.getChild("mbelt_l");
			this.mbeltR = root.getChild("mbelt_r");
			this.beltL = root.getChild("belt_l");
			this.chestplate = root.getChild("chestplate");
			this.hoodEye = root.getChild("hood_eye");
			this.hood1 = root.getChild("hood1");
			this.hood2 = root.getChild("hood1").getChild("hood2");
			this.hood3 = root.getChild("hood1").getChild("hood3");
			this.hood4 = root.getChild("hood1").getChild("hood4");
			this.backplate = root.getChild("backplate");
			this.shoulderplateTopR = root.getChild("shoulderplate_top_r");
			this.shoulderplateR1 = root.getChild("shoulderplate_r1");
			this.shoulderplateR2 = root.getChild("shoulderplate_r2");
			this.shoulderplateR3 = root.getChild("shoulderplate_r3");
			this.shoulderR = root.getChild("shoulder_r");
			this.armL1 = root.getChild("arm_l1");
			this.armL2 = root.getChild("arm_l1").getChild("arm_l2");
			this.armL3 = root.getChild("arm_l1").getChild("arm_l3");
			this.armR1 = root.getChild("arm_r1");
			this.armR2 = root.getChild("arm_r1").getChild("arm_r2");
			this.armR3 = root.getChild("arm_r1").getChild("arm_r3");
			this.shoulderL = root.getChild("shoulder_l");
			this.shoulderplateLtop = root.getChild("shoulderplate_ltop");
			this.shoulderplateL1 = root.getChild("shoulderplate_l1");
			this.shoulderplateL2 = root.getChild("shoulderplate_l2");
			this.shoulderplateL3 = root.getChild("shoulderplate_l3");
			this.legpanelR4 = root.getChild("legpanel_r4");
			this.legpanelR5 = root.getChild("legpanel_r5");
			this.legpanelR6 = root.getChild("legpanel_r6");
			this.backpanelR1 = root.getChild("backpanel_r1");
			this.backpanelR2 = root.getChild("backpanel_r2");
			this.backpanelR3 = root.getChild("backpanel_r3");
			this.backpanelL3 = root.getChild("backpanel_l3");
			this.legpanelL4 = root.getChild("legpanel_l4");
			this.legpanelL5 = root.getChild("legpanel_l5");
			this.legpanelL6 = root.getChild("legpanel_l6");
			this.backpanelL1 = root.getChild("backpanel_l1");
			this.backpanelL2 = root.getChild("backpanel_l2");
			this.sidepanelL1 = root.getChild("sidepanel_l1");
			this.sidepanelR1 = root.getChild("sidepanel_r1");
			this.sidepanelR2 = root.getChild("sidepanel_r2");
			this.sidepanelR3 = root.getChild("sidepanel_r2").getChild("sidepanel_r3");
			this.sidepanelR4 = root.getChild("sidepanel_r2").getChild("sidepanel_r3").getChild("sidepanel_r4");
			this.sidepanelL2 = root.getChild("sidepanel_l2");
			this.sidepanelL3 = root.getChild("sidepanel_l2").getChild("sidepanel_l3");
			this.sidepanelL4 = root.getChild("sidepanel_l2").getChild("sidepanel_l3").getChild("sidepanel_l4");
			this.legpanelC1 = root.getChild("legpanel_c1");
			this.legpanelC2 = root.getChild("legpanel_c1").getChild("legpanel_c2");
			this.legpanelC3 = root.getChild("legpanel_c1").getChild("legpanel_c2").getChild("legpanel_c3");
			this.cloak1 = root.getChild("cloak1");
			this.cloak2 = root.getChild("cloak1").getChild("cloak2");
			this.cloak3 = root.getChild("cloak1").getChild("cloak2").getChild("cloak3");
		}

		public static LayerDefinition createLayer() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition root = mesh.getRoot();
			root.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1, 3, 6), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8, 4, 1), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1, 3, 6), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8, 7, 2), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("hood_eye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			PartDefinition p_hood1 = root.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(40, 12).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			p_hood1.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(36, 28).addBox(-3.5F, -8.7F, 2.0F, 7, 7, 3), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2268928F, 0.0F, 0.0F));
			p_hood1.addOrReplaceChild("hood3", CubeListBuilder.create().texOffs(22, 19).addBox(-3.0F, -9.0F, 2.5F, 6, 6, 3), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3490659F, 0.0F, 0.0F));
			p_hood1.addOrReplaceChild("hood4", CubeListBuilder.create().texOffs(40, 4).addBox(-2.5F, -9.7F, 3.5F, 5, 5, 3), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5759587F, 0.0F, 0.0F));
			root.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8, 11, 2), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("shoulderplate_top_r", CubeListBuilder.create().texOffs(110, 37).addBox(-5.5F, -2.5F, -3.5F, 2, 1, 7), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F));
			root.addOrReplaceChild("shoulderplate_r1", CubeListBuilder.create().texOffs(110, 45).addBox(3.5F, -1.5F, -3.5F, 1, 4, 7), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F));
			root.addOrReplaceChild("shoulderplate_r2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5F, 1.5F, -3.5F, 1, 3, 7), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F));
			root.addOrReplaceChild("shoulderplate_r3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5F, 3.5F, -3.5F, 1, 3, 7), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F));
			root.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5, 5, 5), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.122173F, 0.0349066F));
			PartDefinition p_arm_l1 = root.addOrReplaceChild("arm_l1", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, 2.5F, -1.5F, 4, 10, 5), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.9599311F, -0.1047198F, -0.1919862F));
			p_arm_l1.addOrReplaceChild("arm_l2", CubeListBuilder.create().texOffs(76, 28).addBox(-1.0F, 9.5F, 3.5F, 4, 3, 3), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			p_arm_l1.addOrReplaceChild("arm_l3", CubeListBuilder.create().texOffs(76, 23).addBox(-1.0F, 6.5F, 3.5F, 4, 3, 2), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			PartDefinition p_arm_r1 = root.addOrReplaceChild("arm_r1", CubeListBuilder.create().texOffs(72, 8).addBox(-3.0F, 2.5F, -1.5F, 4, 10, 5), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.9599311F, 0.1047198F, 0.1919862F));
			p_arm_r1.addOrReplaceChild("arm_r2", CubeListBuilder.create().texOffs(76, 28).addBox(-3.0F, 9.5F, 3.5F, 4, 3, 3), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			p_arm_r1.addOrReplaceChild("arm_r3", CubeListBuilder.create().texOffs(76, 23).addBox(-3.0F, 6.5F, 3.5F, 4, 3, 2), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5, 5, 5).mirror(false), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.122173F, -0.0349066F));
			root.addOrReplaceChild("shoulderplate_ltop", CubeListBuilder.create().texOffs(110, 37).addBox(3.5F, -2.5F, -3.5F, 2, 1, 7), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F));
			root.addOrReplaceChild("shoulderplate_l1", CubeListBuilder.create().texOffs(110, 45).addBox(-4.5F, -1.5F, -3.5F, 1, 4, 7), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F));
			root.addOrReplaceChild("shoulderplate_l2", CubeListBuilder.create().texOffs(94, 45).addBox(2.5F, 1.5F, -3.5F, 1, 3, 7), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F));
			root.addOrReplaceChild("shoulderplate_l3", CubeListBuilder.create().texOffs(94, 45).addBox(1.5F, 3.5F, -3.5F, 1, 3, 7), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F));
			root.addOrReplaceChild("legpanel_r4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, 0.5F, -3.5F, 2, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("legpanel_r5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0F, 2.5F, -2.5F, 2, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("legpanel_r6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0F, 4.5F, -1.5F, 2, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 0.5F, 2.5F, 5, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("backpanel_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 2.5F, 1.5F, 5, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("backpanel_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 4.5F, 0.5F, 5, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("backpanel_l3", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 4.5F, 0.5F, 5, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("legpanel_l4", CubeListBuilder.create().texOffs(0, 43).addBox(1.0F, 0.5F, -3.5F, 2, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("legpanel_l5", CubeListBuilder.create().texOffs(0, 47).addBox(1.0F, 2.5F, -2.5F, 2, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("legpanel_l6", CubeListBuilder.create().texOffs(6, 43).addBox(1.0F, 4.5F, -1.5F, 2, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 0.5F, 2.5F, 5, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("backpanel_l2", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 2.5F, 1.5F, 5, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
			root.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(0, 22).addBox(1.5F, 0.5F, -2.5F, 1, 4, 5), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
			root.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, 0.5F, -2.5F, 1, 4, 5), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
			PartDefinition p_sidepanel_r2 = root.addOrReplaceChild("sidepanel_r2", CubeListBuilder.create().texOffs(0, 54).addBox(0.0F, 0.0F, -0.5F, 1, 5, 5), PartPose.offsetAndRotation(-4.5F, 9.5F, -2.0F, 0.0F, 0.0F, 0.122173F));
			PartDefinition p_sidepanel_r3 = p_sidepanel_r2.addOrReplaceChild("sidepanel_r3", CubeListBuilder.create().texOffs(0, 35).addBox(0.0F, 0.0F, -0.5F, 1, 3, 5), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.296706F));
			p_sidepanel_r3.addOrReplaceChild("sidepanel_r4", CubeListBuilder.create().texOffs(24, 35).addBox(0.0F, 0.0F, -0.5F, 1, 3, 5), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.5235987755982988F));
			PartDefinition p_sidepanel_l2 = root.addOrReplaceChild("sidepanel_l2", CubeListBuilder.create().texOffs(0, 54).addBox(0.0F, 0.0F, -0.5F, 1, 5, 5), PartPose.offsetAndRotation(4.5F, 9.5F, -2.0F, 0.0F, 0.0F, -0.122173F));
			PartDefinition p_sidepanel_l3 = p_sidepanel_l2.addOrReplaceChild("sidepanel_l3", CubeListBuilder.create().texOffs(0, 35).addBox(0.0F, 0.0F, -0.5F, 1, 3, 5), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.296706F));
			p_sidepanel_l3.addOrReplaceChild("sidepanel_l4", CubeListBuilder.create().texOffs(24, 35).addBox(0.0F, 0.0F, -0.5F, 1, 3, 5), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, -0.5235987755982988F));
			PartDefinition p_legpanel_c1 = root.addOrReplaceChild("legpanel_c1", CubeListBuilder.create().texOffs(16, 45).addBox(-3.0F, 0.0F, -0.5F, 6, 8, 1), PartPose.offsetAndRotation(0.0F, 5.5F, -3.0F, 0.0F, 0.0F, 0.0F));
			PartDefinition p_legpanel_c2 = p_legpanel_c1.addOrReplaceChild("legpanel_c2", CubeListBuilder.create().texOffs(16, 54).addBox(-3.0F, 0.0F, -0.5F, 6, 4, 1), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			p_legpanel_c2.addOrReplaceChild("legpanel_c3", CubeListBuilder.create().texOffs(32, 59).addBox(-3.0F, 0.0F, -0.5F, 6, 4, 1), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			PartDefinition p_cloak1 = root.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(106, 0).addBox(0.0F, 0.0F, -0.5F, 10, 18, 1), PartPose.offsetAndRotation(-5.0F, -6.0F, 4.0F, 0.0F, 0.0F, 0.0F));
			PartDefinition p_cloak2 = p_cloak1.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(106, 19).addBox(0.0F, 0.0F, -0.5F, 10, 4, 1), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			p_cloak2.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(106, 24).addBox(0.0F, 0.0F, -0.5F, 10, 4, 1), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			return LayerDefinition.create(mesh, 128, 64);
		}

		@Override
		public void setupAnim(State s) {
			super.setupAnim(s);
			float t = s.ageInTicks;
			this.hood1.yRot = s.yRot * Mth.DEG_TO_RAD;
			this.hood1.xRot = s.xRot * Mth.DEG_TO_RAD;
			this.hoodEye.xRot = this.hood1.xRot;
			this.hoodEye.yRot = this.hood1.yRot;
			// the eye is drawn only by the warden's glow layer
			this.hoodEye.visible = false;
			this.armL1.xRot = -1.0F - s.armLiftL + Mth.sin((t + 20.0F) / 10.0F) * 0.08F;
			this.armR1.xRot = -1.0F - s.armLiftR + Mth.sin(t / 10.0F) * 0.08F;
			this.legpanelC1.xRot = -0.15F + Mth.sin(t / 8.0F) * 0.12F;
			this.legpanelC2.xRot = Mth.sin((t - 5.0F) / 8.0F) * 0.13F;
			this.legpanelC3.xRot = Mth.sin((t - 10.0F) / 8.0F) * 0.14F;
			this.cloak1.xRot = 0.2F + Mth.sin(t / 7.0F) * 0.08F;
			this.cloak2.xRot = Mth.sin((t - 5.0F) / 7.0F) * 0.1F;
			this.cloak3.xRot = Mth.sin((t - 10.0F) / 7.0F) * 0.12F;
			this.sidepanelL2.zRot = -0.2F + Mth.sin((t + 10.0F) / 8.0F) * 0.12F;
			this.sidepanelL3.zRot = Mth.sin((t + 5.0F) / 8.0F) * 0.13F;
			this.sidepanelL4.zRot = Mth.sin(t / 8.0F) * 0.14F;
			this.sidepanelR2.zRot = 0.2F + Mth.sin((t - 5.0F) / 8.0F) * 0.12F;
			this.sidepanelR3.zRot = Mth.sin((t - 10.0F) / 8.0F) * 0.13F;
			this.sidepanelR4.zRot = Mth.sin((t - 15.0F) / 8.0F) * 0.14F;
		}
	}

	/** The warden's hood eye, drawn again over itself additively and a hair larger, pulsing. */
	static class EyeLayer extends RenderLayer<State, GuardianModel> {
		EyeLayer(RenderLayerParent<State, GuardianModel> parent) {
			super(parent);
		}

		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, State s, float yRot, float xRot) {
			if (!s.warden) {
				return;
			}
			int glow = Math.min(240, (int) (195.0F + Mth.sin(s.ageInTicks / 3.0F) * 15.0F + 15.0F));
			ModelPart eye = this.getParentModel().hoodEye;
			ps.pushPose();
			ps.scale(1.01F, 1.01F, 1.01F);
			collector.submitCustomGeometry(ps, RenderTypes.eyes(WARDEN), (pose, vc) -> {
				PoseStack local = new PoseStack();
				local.last().set(pose);
				eye.visible = true;
				eye.render(local, vc, glow, OverlayTexture.NO_OVERLAY);
				eye.visible = false;
			});
			ps.popPose();
		}
	}

	public EldritchGuardianRenderer(EntityRendererProvider.Context context, boolean warden) {
		super(context, new GuardianModel(GuardianModel.createLayer().bakeRoot()), warden ? 0.75F : 0.5F);
		this.warden = warden;
		this.addLayer(new EyeLayer(this));
	}

	public static EldritchGuardianRenderer<EldritchGuardianEntity> guardian(EntityRendererProvider.Context context) {
		return new EldritchGuardianRenderer<>(context, false);
	}

	public static EldritchGuardianRenderer<EldritchWardenEntity> warden(EntityRendererProvider.Context context) {
		return new EldritchGuardianRenderer<>(context, true);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T entity, State s, float partialTicks) {
		super.extractRenderState(entity, s, partialTicks);
		s.warden = this.warden;
		s.sink = 0.0F;
		s.alpha = 1.0F;
		if (entity instanceof EldritchWardenEntity w) {
			s.armLiftL = w.armLiftL;
			s.armLiftR = w.armLiftR;
			s.sink = entity.getBbHeight() * (w.spawnTimer / 150.0F);
		} else if (entity instanceof EldritchGuardianEntity g) {
			s.armLiftL = g.armLiftL;
			s.armLiftR = g.armLiftR;
			var viewer = Minecraft.getInstance().getCameraEntity();
			// full strength in the outer lands, which this port does not have yet
			if (viewer != null) {
				float far = entity.level().getDifficulty() == Difficulty.HARD ? 576.0F : 1024.0F;
				float near = 256.0F;
				double d = entity.distanceToSqr(viewer);
				s.alpha = d < near ? 0.6F : (float) (1.0 - Math.min(far - near, d - near) / (far - near)) * 0.6F;
			}
		}
	}

	@Override
	public Vec3 getRenderOffset(State s) {
		return s.sink > 0.0F ? new Vec3(0.0, -s.sink, 0.0) : super.getRenderOffset(s);
	}

	@Override
	protected @Nullable RenderType getRenderType(State s, boolean bodyVisible, boolean forceTransparent, boolean glowing) {
		return bodyVisible ? RenderTypes.entityTranslucent(this.getTextureLocation(s)) : super.getRenderType(s, bodyVisible, forceTransparent, glowing);
	}

	@Override
	protected int getModelTint(State s) {
		return ARGB.color(Mth.clamp((int) (s.alpha * 255.0F), 0, 255), 0xFFFFFF);
	}

	@Override
	public Identifier getTextureLocation(State s) {
		return s.warden ? WARDEN : GUARDIAN;
	}

	@Override
	protected void scale(State s, PoseStack ps) {
		if (s.warden) {
			ps.scale(1.5F, 1.5F, 1.5F);
		}
	}
}
