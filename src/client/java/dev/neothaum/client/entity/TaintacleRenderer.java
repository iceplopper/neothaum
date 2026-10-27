package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.Taintacle;
import dev.neothaum.entity.monster.TaintacleGiant;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * RenderTaintacle with ModelTaintacle and ModelRendererTaintacle: a chain of cubes, each one 0.88 the size of the one below, swaying gently or
 * flailing when roused, with a glowing orb and a fat glowing tip at the top. It rises out of the ground over its first few seconds.
 */
public class TaintacleRenderer extends MobRenderer<Taintacle, TaintacleRenderer.State, TaintacleRenderer.EmptyModel> {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/taintacle.png");
	private static final float SHRINK = 0.88F;

	public static class State extends LivingEntityRenderState {
		float flail;
		boolean agitated;
		boolean attacking;
		float height;
		boolean giant;
	}

	/** The renderer draws the chain itself; the model only carries the render type. */
	public static class EmptyModel extends EntityModel<State> {
		EmptyModel() {
			super(new ModelPart(java.util.List.of(), java.util.Map.of()));
		}
	}

	private final int length;
	private final ModelPart base, segment, orb, tip;

	public TaintacleRenderer(EntityRendererProvider.Context context, float shadow, int length) {
		super(context, new EmptyModel(), shadow);
		this.length = length;
		MeshDefinition mesh = new MeshDefinition();
		var r = mesh.getRoot();
		r.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8), PartPose.ZERO);
		r.addOrReplaceChild("segment", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8), PartPose.ZERO);
		r.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, -2.0F, -2.0F, 4, 4, 4), PartPose.ZERO);
		r.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, -6.0F, -6.0F, 12, 12, 12), PartPose.ZERO);
		ModelPart root = LayerDefinition.create(mesh, 64, 64).bakeRoot();
		this.base = root.getChild("base");
		this.segment = root.getChild("segment");
		this.orb = root.getChild("orb");
		this.tip = root.getChild("tip");
		this.addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack ps, SubmitNodeCollector collector, int light, State state, float yRot, float xRot) {
				TaintacleRenderer.this.drawChain(ps, collector, light, state);
			}
		});
	}

	public static TaintacleRenderer normal(EntityRendererProvider.Context ctx) {
		return new TaintacleRenderer(ctx, 0.6F, 10);
	}

	public static TaintacleRenderer small(EntityRendererProvider.Context ctx) {
		return new TaintacleRenderer(ctx, 0.2F, 6);
	}

	public static TaintacleRenderer giant(EntityRendererProvider.Context ctx) {
		return new TaintacleRenderer(ctx, 1.0F, 14);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(Taintacle entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.flail = entity.flailIntensity;
		state.agitated = entity.getAgitationState(entity.getTarget());
		state.attacking = entity.attackTime > 0;
		state.height = entity.getBbHeight();
		state.giant = entity instanceof TaintacleGiant;
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return TEXTURE;
	}

	@Override
	protected void scale(State state, PoseStack ps) {
		if (state.giant) {
			ps.scale(1.33F, 1.33F, 1.33F);
		}
	}

	private void drawChain(PoseStack ps, SubmitNodeCollector collector, int light, State s) {
		// ModelTaintacle.setRotationAngles
		float mod = 0.0625F * 0.2F;
		float fs = s.agitated ? 3.0F : 1.0F - mod;
		float fi = s.flail + (!s.hasRedOverlay && !s.attacking ? -mod : mod);
		float[] rx = new float[this.length - 1];
		float[] rz = new float[this.length - 1];
		for (int k = 0; k < this.length - 1; k++) {
			rx[k] = 0.15F * fi * Mth.sin(s.ageInTicks * 0.1F * fs - k / 2.0F);
			rz[k] = 0.1F / fi * Mth.sin(s.ageInTicks * 0.15F - k / 2.0F);
		}
		// ModelTaintacle.render: rise out of the ground, then scale to the entity's height
		float height = 0.0F;
		float hc = s.height * 10.0F;
		if (s.ageInTicks < hc) {
			height = (hc - s.ageInTicks) / hc * s.height;
		}
		int overlay = LivingEntityRenderer.getOverlayCoords(s, 0.0F);
		var type = RenderTypes.entityTranslucent(TEXTURE);
		ps.pushPose();
		ps.translate(0.0F, (s.height == 3.0F ? 0.6F : 1.2F) + height, 0.0F);
		ps.scale(s.height / 3.0F, s.height / 3.0F, s.height / 3.0F);
		// the root cube at (0, 12, 0)
		ps.translate(0.0F, 12.0F / 16.0F, 0.0F);
		collector.submitModelPart(this.base, ps, type, light, overlay, null);
		for (int k = 0; k < this.length - 1; k++) {
			ps.scale(SHRINK, SHRINK, SHRINK);
			ps.translate(0.0F, -8.0F / 16.0F, 0.0F);
			ps.rotateDegrees(Axis.ZP, rz[k] * Mth.RAD_TO_DEG);
			ps.rotateDegrees(Axis.XP, rx[k] * Mth.RAD_TO_DEG);
			collector.submitModelPart(this.segment, ps, type, light, overlay, null);
		}
		// the last segment carries the glowing orb and the fat tip, both full-bright
		ps.pushPose();
		ps.scale(SHRINK, SHRINK, SHRINK);
		ps.translate(0.0F, -8.0F / 16.0F, 0.0F);
		collector.submitModelPart(this.orb, ps, type, 0xF000F0, overlay, null);
		ps.popPose();
		ps.scale(SHRINK, SHRINK, SHRINK);
		ps.translate(0.0F, -8.0F / 16.0F, 0.0F);
		collector.submitModelPart(this.tip, ps, type, 0xF000F0, overlay, null);
		ps.popPose();
	}
}
