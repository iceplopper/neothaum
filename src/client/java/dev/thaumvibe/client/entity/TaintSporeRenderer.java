package dev.thaumvibe.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.entity.monster.TaintSpore;
import dev.thaumvibe.entity.monster.TaintSporeSwarmer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * RenderTaintSpore and RenderTaintSporeSwarmer with ModelTaintSpore / ModelTaintSporeSwarmer: a translucent pod (a cube in a cube) that wobbles,
 * harder when hurt, and swells with the spore. The swarmer's glowing heart sits inside a pod drawn in the world's light.
 */
public class TaintSporeRenderer extends MobRenderer<TaintSpore, TaintSporeRenderer.State, TaintSporeRenderer.SporeModel> {
	private static final Identifier TEXTURE = Thaumvibe.id("textures/models/taint_spore.png");

	public static class State extends LivingEntityRenderState {
		float displaySize;
		int sporeSize;
		boolean swarmer;
		int worldLight;
	}

	public static class SporeModel extends EntityModel<State> {
		private final ModelPart cube;

		SporeModel(ModelPart root) {
			super(root, RenderTypes::entityTranslucent);
			this.cube = root.getChild("cube");
		}

		static LayerDefinition spore() {
			MeshDefinition mesh = new MeshDefinition();
			mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 2.0F, -6.0F, 12, 12, 12)
				.addBox(-8.0F, 0.0F, -8.0F, 16, 16, 16), PartPose.offset(0.0F, 24.0F, 0.0F));
			return LayerDefinition.create(mesh, 64, 64);
		}

		@Override
		public void setupAnim(State state) {
			super.setupAnim(state);
			float intensity = state.hasRedOverlay ? 0.04F : 0.02F;
			this.cube.xRot = intensity * Mth.sin(state.ageInTicks * 0.05F);
			this.cube.zRot = intensity * Mth.sin(state.ageInTicks * 0.1F);
		}
	}

	private final ModelPart swarmerCube;
	private final ModelPart swarmerCube2;

	public TaintSporeRenderer(EntityRendererProvider.Context context) {
		super(context, new SporeModel(SporeModel.spore().bakeRoot()), 0.25F);
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16, 16, 16), PartPose.ZERO);
		mesh.getRoot().addOrReplaceChild("cube2", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16),
			PartPose.offset(0.0F, 16.0F, 0.0F));
		ModelPart root = LayerDefinition.create(mesh, 64, 64).bakeRoot();
		this.swarmerCube = root.getChild("cube");
		this.swarmerCube2 = root.getChild("cube2");
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return TEXTURE;
	}

	@Override
	public void extractRenderState(TaintSpore entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.displaySize = entity.displaySize;
		if (entity.displaySize < entity.getSporeSize()) {
			state.displaySize += 0.02F * partialTicks;
		}
		state.sporeSize = entity.getSporeSize();
		state.swarmer = entity instanceof TaintSporeSwarmer;
		state.worldLight = this.getPackedLightCoords(entity, partialTicks);
	}

	@Override
	protected int getBlockLightLevel(TaintSpore entity, BlockPos pos) {
		return entity instanceof TaintSporeSwarmer ? super.getBlockLightLevel(entity, pos) : 15;
	}

	@Override
	protected float getFlipDegrees() {
		return 0.0F;
	}

	@Override
	protected void scale(State state, PoseStack ps) {
		if (!state.swarmer) {
			float f3 = -0.12F;
			float pulse = 0.025F * Mth.sin(state.ageInTicks * 0.075F);
			ps.scale(f3 * state.displaySize - pulse, f3 * state.displaySize + pulse, f3 * state.displaySize - pulse);
		}
	}

	@Override
	public void submit(State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.swarmer) {
			super.submit(state, ps, collector, camera);
			return;
		}
		// ModelTaintSporeSwarmer: a full-bright core scaled with the pulse, inside the pod
		ps.pushPose();
		ps.scale(-1.0F, -1.0F, 1.0F);
		ps.translate(0.0F, -1.501F, 0.0F);
		float intensity = state.hasRedOverlay ? 0.04F : 0.02F;
		this.swarmerCube.xRot = intensity * Mth.sin(state.ageInTicks * 0.05F);
		this.swarmerCube.zRot = intensity * Mth.sin(state.ageInTicks * 0.1F);
		int overlay = OverlayTexture.pack(0, state.hasRedOverlay ? 3 : 10);
		ps.pushPose();
		float f3 = -0.07F;
		float pulse = 0.025F * Mth.sin(state.ageInTicks * 0.075F);
		ps.translate(0.0F, 1.6F, 0.0F);
		ps.scale(f3 * 10.0F - pulse, f3 * 10.0F + pulse, f3 * 10.0F - pulse);
		ps.translate(0.0F, -(f3 * 10.0F + pulse) / 2.0F, 0.0F);
		collector.submitModelPart(this.swarmerCube, ps, RenderTypes.entityTranslucent(TEXTURE), 0xF000F0, overlay, null);
		ps.popPose();
		collector.submitModelPart(this.swarmerCube2, ps, RenderTypes.entityTranslucent(TEXTURE), state.worldLight, overlay, null);
		ps.popPose();
	}
}
