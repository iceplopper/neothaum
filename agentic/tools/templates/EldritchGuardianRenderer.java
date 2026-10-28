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
		//@FIELDS

		public GuardianModel(ModelPart root) {
			super(root, RenderTypes::entityTranslucent);
			//@CTOR
		}

		//@LAYER

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
