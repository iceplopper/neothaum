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
		//@FIELDS

		public GolemModel(ModelPart root) {
			super(root, RenderTypes::entityTranslucent);
			//@CTOR
		}

		//@LAYER

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
