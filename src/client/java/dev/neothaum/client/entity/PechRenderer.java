package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.Pech;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/** RenderPech with ModelPech: a stooped little figure with a huge pack, jowls that wobble as it mumbles, and whatever it holds in its right hand. */
public class PechRenderer extends MobRenderer<Pech, PechRenderer.State, PechRenderer.PechModel> {
	private static final Identifier[] SKIN = {NeoThaum.id("textures/models/pech_forage.png"), NeoThaum.id("textures/models/pech_thaum.png"),
		NeoThaum.id("textures/models/pech_stalker.png")};

	public static class State extends ArmedEntityRenderState {
		int type;
		float mumble;
		boolean sneaking;
	}

	public static class PechModel extends EntityModel<State> implements ArmedModel<State> {
		private final ModelPart body, rightLeg, leftLeg, head, jowls, lowerPack, upperPack, rightArm, leftArm;

		PechModel(ModelPart root) {
			super(root);
			this.body = root.getChild("body");
			this.rightLeg = root.getChild("right_leg");
			this.leftLeg = root.getChild("left_leg");
			this.head = root.getChild("head");
			this.jowls = root.getChild("jowls");
			this.lowerPack = root.getChild("lower_pack");
			this.upperPack = root.getChild("upper_pack");
			this.rightArm = root.getChild("right_arm");
			this.leftArm = root.getChild("left_arm");
		}

		/** ModelPech; as in 1.7, only boxes added after "mirror = true" (the right leg and right arm) are mirrored. */
		static LayerDefinition create() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition r = mesh.getRoot();
			r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(34, 12).addBox(-3.0F, 0.0F, 0.0F, 6, 10, 6),
				PartPose.offsetAndRotation(0.0F, 9.0F, -3.0F, 0.3129957F, 0.0F, 0.0F));
			r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(35, 1).mirror().addBox(-2.9F, 0.0F, 0.0F, 3, 6, 3), PartPose.offset(0.0F, 18.0F, 0.0F));
			r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(35, 1).addBox(-0.1F, 0.0F, 0.0F, 3, 6, 3), PartPose.offset(0.0F, 18.0F, 0.0F));
			r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 11).addBox(-3.5F, -5.0F, -5.0F, 7, 5, 5), PartPose.offset(0.0F, 8.0F, 0.0F));
			r.addOrReplaceChild("jowls", CubeListBuilder.create().texOffs(1, 21).addBox(-4.0F, -1.0F, -6.0F, 8, 3, 5), PartPose.offset(0.0F, 8.0F, 0.0F));
			r.addOrReplaceChild("lower_pack", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, 0.0F, 10, 5, 5),
				PartPose.offsetAndRotation(0.0F, 10.0F, 3.5F, 0.3013602F, 0.0F, 0.0F));
			r.addOrReplaceChild("upper_pack", CubeListBuilder.create().texOffs(64, 1).addBox(-7.5F, -14.0F, 0.0F, 15, 14, 11),
				PartPose.offsetAndRotation(0.0F, 10.0F, 3.0F, 0.4537856F, 0.0F, 0.0F));
			r.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 2).mirror().addBox(-2.0F, 0.0F, -1.0F, 2, 6, 2),
				PartPose.offset(-3.0F, 10.0F, -1.0F));
			r.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 2).addBox(0.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(3.0F, 10.0F, -1.0F));
			return LayerDefinition.create(mesh, 128, 64);
		}

		@Override
		public void setupAnim(State s) {
			super.setupAnim(s);
			float deg = (float) Math.PI / 180.0F;
			float limb = s.walkAnimationPos, amount = s.walkAnimationSpeed, age = s.ageInTicks;
			this.head.yRot = s.yRot * deg;
			this.head.xRot = s.xRot * deg;
			this.jowls.yRot = this.head.yRot;
			this.jowls.xRot = this.head.xRot + ((float) (Math.PI / 12) + Mth.cos(limb * 0.6662F) * amount * 0.25F) + 0.34906587F * Math.abs(Mth.sin(s.mumble));
			this.rightArm.xRot = Mth.cos(limb * 0.6662F + (float) Math.PI) * 2.0F * amount * 0.5F;
			this.leftArm.xRot = Mth.cos(limb * 0.6662F) * 2.0F * amount * 0.5F;
			this.rightArm.zRot = 0.0F;
			this.leftArm.zRot = 0.0F;
			this.rightLeg.xRot = Mth.cos(limb * 0.6662F) * 1.4F * amount;
			this.leftLeg.xRot = Mth.cos(limb * 0.6662F + (float) Math.PI) * 1.4F * amount;
			this.rightLeg.yRot = 0.0F;
			this.leftLeg.yRot = 0.0F;
			this.lowerPack.yRot = Mth.cos(limb * 0.6662F) * 2.0F * amount * 0.125F;
			this.lowerPack.zRot = Mth.cos(limb * 0.6662F) * 2.0F * amount * 0.125F;
			this.rightArm.yRot = 0.0F;
			this.leftArm.yRot = 0.0F;
			float swing = s.swingAnimation;
			this.rightArm.yRot += this.body.yRot;
			this.leftArm.yRot += this.body.yRot;
			this.leftArm.xRot += this.body.yRot;
			float f6 = 1.0F - swing;
			f6 *= f6;
			f6 *= f6;
			f6 = 1.0F - f6;
			float f7 = Mth.sin(f6 * (float) Math.PI);
			float f8 = Mth.sin(swing * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
			this.rightArm.xRot = (float) (this.rightArm.xRot - (f7 * 1.2 + f8));
			this.rightArm.yRot += this.body.yRot * 2.0F;
			this.rightArm.zRot = Mth.sin(swing * (float) Math.PI) * -0.4F;
			if (s.sneaking) {
				this.rightArm.xRot += 0.4F;
				this.leftArm.xRot += 0.4F;
			}
			this.rightArm.zRot += Mth.cos(age * 0.09F) * 0.05F + 0.05F;
			this.leftArm.zRot -= Mth.cos(age * 0.09F) * 0.05F + 0.05F;
			this.rightArm.xRot += Mth.sin(age * 0.067F) * 0.05F;
			this.leftArm.xRot -= Mth.sin(age * 0.067F) * 0.05F;
		}

		/** RenderPech: the item sits at the end of the short right arm (5.4 pixels down it, where a player's would be at 10). */
		@Override
		public void translateToHand(State state, HumanoidArm arm, PoseStack ps) {
			(arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm).translateAndRotate(ps);
			ps.translate(0.0F, -0.2875F, 0.0F);
		}
	}

	public PechRenderer(EntityRendererProvider.Context context) {
		super(context, new PechModel(PechModel.create().bakeRoot()), 0.5F);
		this.addLayer(new ItemInHandLayer<>(this));
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(Pech entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
		state.type = entity.getPechType();
		state.mumble = entity.mumble;
		state.sneaking = entity.isCrouching();
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return SKIN[Math.min(2, Math.max(0, state.type))];
	}
}
