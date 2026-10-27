package dev.thaumvibe.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.golem.TravelingTrunkEntity;
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

public class TravelingTrunkRenderer extends MobRenderer<TravelingTrunkEntity, TravelingTrunkRenderer.State, TravelingTrunkRenderer.TrunkModel> {
	private static final Identifier NORMAL_TEXTURE = Thaumvibe.id("textures/entity/golem/trunk.png");
	private static final Identifier ANGRY_TEXTURE = Thaumvibe.id("textures/entity/golem/trunkangry.png");

	public static class State extends LivingEntityRenderState {
		public float lidAngle;
		public boolean angry;
		public int upgrade = -1;
	}

	public static class TrunkModel extends EntityModel<State> {
		public final ModelPart chestBelow;
		public final ModelPart chestLid;
		public final ModelPart chestKnob;

		public TrunkModel(ModelPart root) {
			super(root);
			this.chestBelow = root.getChild("chest_below");
			this.chestLid = root.getChild("chest_lid");
			this.chestKnob = this.chestLid.getChild("chest_knob");
		}

		public static LayerDefinition createMesh() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition r = mesh.getRoot();

			// Bottom box: centered 14x10x14, bottom at Y = 24.0F (14 to 24)
			r.addOrReplaceChild("chest_below", CubeListBuilder.create().texOffs(0, 19)
				.addBox(-7.0F, 0.0F, -7.0F, 14, 10, 14), PartPose.offset(0.0F, 14.0F, 0.0F));

			// Lid box: 14x5x14, pivot at back edge (Z = 7.0F, Y = 14.0F)
			PartDefinition lid = r.addOrReplaceChild("chest_lid", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-7.0F, -5.0F, -14.0F, 14, 5, 14), PartPose.offset(0.0F, 14.0F, 7.0F));

			// Knob/latch on front of lid
			lid.addOrReplaceChild("chest_knob", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-1.0F, -2.0F, -15.0F, 2, 4, 1), PartPose.ZERO);

			return LayerDefinition.create(mesh, 64, 64);
		}

		@Override
		public void setupAnim(State s) {
			super.setupAnim(s);
			this.chestLid.xRot = -s.lidAngle * ((float) Math.PI / 2.0F);
		}
	}

	public TravelingTrunkRenderer(EntityRendererProvider.Context context) {
		super(context, new TrunkModel(TrunkModel.createMesh().bakeRoot()), 0.4F);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(TravelingTrunkEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.lidAngle = Mth.lerp(partialTicks, entity.prevLidAngle, entity.lidAngle);
		state.angry = entity.getAnger() > 0;
		state.upgrade = entity.getUpgrade();
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return state.angry ? ANGRY_TEXTURE : NORMAL_TEXTURE;
	}

	@Override
	protected void scale(State state, PoseStack ps) {
		ps.scale(0.8F, 0.8F, 0.8F);
	}
}
