package dev.thaumvibe.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.entity.monster.FireBat;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** RenderFireBat with ModelFireBat: a small, full-bright bat that flaps and bobs in flight and folds up upside down while hanging. */
public class FireBatRenderer extends MobRenderer<FireBat, FireBatRenderer.State, FireBatRenderer.FireBatModel> {
	private static final Identifier FIREBAT = Thaumvibe.id("textures/models/firebat.png");
	private static final Identifier VAMPIRE = Thaumvibe.id("textures/models/vampirebat.png");

	public static class State extends LivingEntityRenderState {
		boolean hanging;
		boolean big;
		boolean vampire;
	}

	public static class FireBatModel extends EntityModel<State> {
		private final ModelPart head, body, rightWing, leftWing, outerRightWing, outerLeftWing;

		FireBatModel(ModelPart root) {
			super(root);
			this.head = root.getChild("head");
			this.body = root.getChild("body");
			this.rightWing = this.body.getChild("right_wing");
			this.leftWing = this.body.getChild("left_wing");
			this.outerRightWing = this.rightWing.getChild("outer_right_wing");
			this.outerLeftWing = this.leftWing.getChild("outer_left_wing");
		}

		static LayerDefinition create() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition root = mesh.getRoot();
			PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6), PartPose.ZERO);
			head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(24, 0).addBox(-4.0F, -6.0F, -2.0F, 3, 4, 1), PartPose.ZERO);
			head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(1.0F, -6.0F, -2.0F, 3, 4, 1), PartPose.ZERO);
			PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 4.0F, -3.0F, 6, 12, 6)
				.texOffs(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10, 6, 1), PartPose.ZERO);
			PartDefinition right = body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(42, 0).addBox(-12.0F, 1.0F, 1.5F, 10, 16, 1),
				PartPose.ZERO);
			right.addOrReplaceChild("outer_right_wing", CubeListBuilder.create().texOffs(24, 16).addBox(-8.0F, 1.0F, 0.0F, 8, 12, 1),
				PartPose.offset(-12.0F, 1.0F, 1.5F));
			PartDefinition left = body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(42, 0).mirror().addBox(2.0F, 1.0F, 1.5F, 10, 16, 1),
				PartPose.ZERO);
			left.addOrReplaceChild("outer_left_wing", CubeListBuilder.create().texOffs(24, 16).mirror().addBox(0.0F, 1.0F, 0.0F, 8, 12, 1),
				PartPose.offset(12.0F, 1.0F, 1.5F));
			return LayerDefinition.create(mesh, 64, 64);
		}

		@Override
		public void setupAnim(State s) {
			super.setupAnim(s);
			float deg = (float) Math.PI / 180.0F;
			if (s.hanging) {
				this.head.xRot = s.xRot * deg;
				this.head.yRot = (float) Math.PI - s.yRot * deg;
				this.head.zRot = (float) Math.PI;
				this.head.setPos(0.0F, -2.0F, 0.0F);
				this.rightWing.setPos(-3.0F, 0.0F, 3.0F);
				this.leftWing.setPos(3.0F, 0.0F, 3.0F);
				this.body.xRot = (float) Math.PI;
				this.rightWing.xRot = (float) (-Math.PI / 20);
				this.rightWing.yRot = (float) (-Math.PI * 2.0 / 5.0);
				this.outerRightWing.yRot = -1.7278761F;
				this.leftWing.xRot = this.rightWing.xRot;
				this.leftWing.yRot = -this.rightWing.yRot;
				this.outerLeftWing.yRot = -this.outerRightWing.yRot;
			} else {
				this.head.xRot = s.xRot * deg;
				this.head.yRot = s.yRot * deg;
				this.head.zRot = 0.0F;
				this.head.setPos(0.0F, 0.0F, 0.0F);
				this.rightWing.setPos(0.0F, 0.0F, 0.0F);
				this.leftWing.setPos(0.0F, 0.0F, 0.0F);
				this.body.xRot = (float) (Math.PI / 4) + Mth.cos(s.ageInTicks * 0.1F) * 0.15F;
				this.body.yRot = 0.0F;
				this.rightWing.yRot = Mth.cos(s.ageInTicks * 1.3F) * (float) Math.PI * 0.25F;
				this.leftWing.yRot = -this.rightWing.yRot;
				this.outerRightWing.yRot = this.rightWing.yRot * 0.5F;
				this.outerLeftWing.yRot = -this.rightWing.yRot * 0.5F;
			}
		}
	}

	public FireBatRenderer(EntityRendererProvider.Context context) {
		super(context, new FireBatModel(FireBatModel.create().bakeRoot()), 0.25F);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(FireBat entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.hanging = entity.getIsBatHanging();
		state.big = entity.getIsDevil() || entity.getIsVampire();
		state.vampire = entity.getIsVampire();
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return state.vampire ? VAMPIRE : FIREBAT;
	}

	@Override
	protected int getBlockLightLevel(FireBat entity, BlockPos pos) {
		return 15;
	}

	@Override
	protected void scale(State state, PoseStack ps) {
		float s = state.big ? 0.6F : 0.35F;
		ps.scale(s, s, s);
	}

	@Override
	protected void setupRotations(State state, PoseStack ps, float bodyRot, float entityScale) {
		if (!state.hanging) {
			ps.translate(0.0F, Mth.cos(state.ageInTicks * 0.3F) * 0.1F, 0.0F);
		} else {
			ps.translate(0.0F, -0.1F, 0.0F);
		}
		super.setupRotations(state, ps, bodyRot, entityScale);
	}
}
