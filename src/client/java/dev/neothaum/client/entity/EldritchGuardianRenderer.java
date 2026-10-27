package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.EldritchGuardianEntity;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** RenderEldritchGuardian: exact port of TC4's robed floating Eldritch Guardian model. */
public class EldritchGuardianRenderer extends MobRenderer<EldritchGuardianEntity, EldritchGuardianRenderer.State, EldritchGuardianRenderer.GuardianModel> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/entity/eldritch_guardian.png");

	public static class State extends LivingEntityRenderState {
		public float armLiftL;
		public float armLiftR;
	}

	public static class GuardianModel extends EntityModel<State> {
		public final ModelPart root;
		public final ModelPart hood;
		public final ModelPart body;
		public final ModelPart leftArm;
		public final ModelPart rightArm;
		public final ModelPart cloak;

		public GuardianModel(ModelPart root) {
			super(root);
			this.root = root;
			this.hood = root.getChild("hood");
			this.body = root.getChild("body");
			this.leftArm = root.getChild("left_arm");
			this.rightArm = root.getChild("right_arm");
			this.cloak = root.getChild("cloak");
		}

		public static LayerDefinition createMesh() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition r = mesh.getRoot();

			// Hood (pivot at 0, -6, 0)
			PartDefinition hood = r.addOrReplaceChild("hood", CubeListBuilder.create()
				.texOffs(40, 12).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8)
				.texOffs(36, 28).addBox(-3.5F, -8.7F, 2.0F, 7, 7, 3)
				.texOffs(22, 19).addBox(-3.0F, -9.0F, 2.5F, 6, 6, 3)
				.texOffs(40, 4).addBox(-2.5F, -9.7F, 3.5F, 5, 5, 3), PartPose.offset(0.0F, -6.0F, 0.0F));

			// Body / Chestplate & Backplate
			PartDefinition body = r.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8, 7, 2) // chestplate
				.texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8, 11, 2)  // backplate
				.texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8, 4, 1)  // mbelt
				.texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1, 7, 6)  // beltR
				.texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1, 7, 6),   // beltL
				PartPose.offset(0.0F, -6.0F, 0.0F));

			// Left Arm
			r.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(72, 8).addBox(-1.0F, 2.5F, -1.5F, 4, 10, 5)
				.texOffs(76, 28).addBox(-1.0F, 9.5F, 3.5F, 4, 3, 3)
				.texOffs(76, 23).addBox(-1.0F, 6.5F, 3.5F, 4, 3, 2),
				PartPose.offset(5.0F, -4.0F, 0.0F));

			// Right Arm
			r.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(72, 8).addBox(-3.0F, 2.5F, -1.5F, 4, 10, 5)
				.texOffs(76, 28).addBox(-3.0F, 9.5F, 3.5F, 4, 3, 3)
				.texOffs(76, 23).addBox(-3.0F, 6.5F, 3.5F, 4, 3, 2),
				PartPose.offset(-5.0F, -4.0F, 0.0F));

			// Cloak / Robes (hanging lower body)
			r.addOrReplaceChild("cloak", CubeListBuilder.create()
				.texOffs(0, 32).addBox(-4.5F, 0.0F, -3.0F, 9, 20, 6)
				.texOffs(0, 0).addBox(-5.0F, 2.0F, -3.5F, 10, 18, 7),
				PartPose.offset(0.0F, 4.0F, 0.0F));

			return LayerDefinition.create(mesh, 128, 64);
		}

		@Override
		public void setupAnim(State state) {
			super.setupAnim(state);
			this.hood.yRot = state.yRot * ((float) Math.PI / 180.0F);
			this.hood.xRot = state.xRot * ((float) Math.PI / 180.0F);

			float bob = Mth.sin(state.ageInTicks / 10.0F) * 0.08F;
			this.leftArm.xRot = -1.0F - state.armLiftL + bob;
			this.rightArm.xRot = -1.0F - state.armLiftR + bob;

			// Swaying cloak
			this.cloak.xRot = 0.05F + Mth.sin(state.ageInTicks / 8.0F) * 0.05F;
		}
	}

	public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
		super(context, new GuardianModel(GuardianModel.createMesh().bakeRoot()), 0.5F);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EldritchGuardianEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.armLiftL = entity.armLiftL;
		state.armLiftR = entity.armLiftR;
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return TEXTURE;
	}

	@Override
	protected void scale(State state, PoseStack ps) {
		ps.scale(1.2F, 1.2F, 1.2F);
	}
}
