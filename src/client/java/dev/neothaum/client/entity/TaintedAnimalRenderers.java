package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.TaintChicken;
import dev.neothaum.entity.monster.TaintCow;
import dev.neothaum.entity.monster.TaintCreeper;
import dev.neothaum.entity.monster.TaintPig;
import dev.neothaum.entity.monster.TaintSheep;
import dev.neothaum.entity.monster.TaintVillager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.animal.sheep.SheepFurModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * RenderTaintChicken, Cow, Pig, Sheep, Creeper and Villager: the 1.7 vanilla models in TC4's tainted textures (each a double-resolution copy of
 * the old layout). The cow, pig, chicken and villager are rebuilt to their 1.7 shapes; the sheep and creeper are unchanged since.
 */
public final class TaintedAnimalRenderers {
	private static Identifier tex(String name) {
		return NeoThaum.id("textures/models/" + name + ".png");
	}

	// ------------------------------------------------------------------ 1.7 meshes

	private static PartDefinition legs(PartDefinition r, int legSize, float x, float zBack, float zFront) {
		CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, legSize, 4);
		float y = 24 - legSize;
		r.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-x, y, zBack));
		r.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(x, y, zBack));
		r.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-x, y, zFront));
		r.addOrReplaceChild("left_front_leg", leg, PartPose.offset(x, y, zFront));
		return r;
	}

	/** ModelPig: ModelQuadruped(6) with a snout. */
	static LayerDefinition pigMesh() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition r = mesh.getRoot();
		r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8).texOffs(16, 16)
			.addBox(-2.0F, 0.0F, -9.0F, 4, 3, 1), PartPose.offset(0.0F, 12.0F, -6.0F));
		r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8),
			PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
		legs(r, 6, 3.0F, 7.0F, -5.0F);
		return LayerDefinition.create(mesh, 64, 32);
	}

	/** ModelCow: a deeper head with horns, a big body with an udder, long legs. */
	static LayerDefinition cowMesh() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition r = mesh.getRoot();
		r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8, 8, 6).texOffs(22, 0)
			.addBox(-5.0F, -5.0F, -4.0F, 1, 3, 1).texOffs(22, 0).addBox(4.0F, -5.0F, -4.0F, 1, 3, 1), PartPose.offset(0.0F, 4.0F, -8.0F));
		r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10).texOffs(52, 0)
			.addBox(-2.0F, 2.0F, -8.0F, 4, 6, 1), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
		legs(r, 12, 4.0F, 7.0F, -6.0F);
		return LayerDefinition.create(mesh, 64, 32);
	}

	/** ModelChicken. */
	static LayerDefinition chickenMesh() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition r = mesh.getRoot();
		PartPose headPose = PartPose.offset(0.0F, 15.0F, -4.0F);
		r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4, 6, 3), headPose);
		r.addOrReplaceChild("bill", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4, 2, 2), headPose);
		r.addOrReplaceChild("chin", CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2, 2, 2), headPose);
		r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6, 8, 6),
			PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
		CubeListBuilder leg = CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3, 5, 3);
		r.addOrReplaceChild("right_leg", leg, PartPose.offset(-2.0F, 19.0F, 1.0F));
		r.addOrReplaceChild("left_leg", leg, PartPose.offset(1.0F, 19.0F, 1.0F));
		r.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1, 4, 6), PartPose.offset(-4.0F, 13.0F, 0.0F));
		r.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1, 4, 6), PartPose.offset(4.0F, 13.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 32);
	}

	/** ModelVillager(0): no hat, a robe to the knees, arms folded. */
	static LayerDefinition villagerMesh() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition r = mesh.getRoot();
		PartDefinition head = r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8), PartPose.ZERO);
		head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2), PartPose.offset(0.0F, -2.0F, 0.0F));
		r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6).texOffs(0, 38)
			.addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, new CubeDeformation(0.5F)), PartPose.ZERO);
		r.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4).texOffs(44, 22)
			.addBox(4.0F, -2.0F, -2.0F, 4, 8, 4).texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4),
			PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
		r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4), PartPose.offset(-2.0F, 12.0F, 0.0F));
		r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
			PartPose.offset(2.0F, 12.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	// ------------------------------------------------------------------ models

	public static class Quadruped extends QuadrupedModel<LivingEntityRenderState> {
		Quadruped(ModelPart root) {
			super(root);
		}
	}

	public static class ChickenState extends LivingEntityRenderState {
		float wing;
	}

	public static class Chicken extends EntityModel<ChickenState> {
		private final ModelPart head, bill, chin, rightLeg, leftLeg, rightWing, leftWing;

		Chicken(ModelPart root) {
			super(root);
			this.head = root.getChild("head");
			this.bill = root.getChild("bill");
			this.chin = root.getChild("chin");
			this.rightLeg = root.getChild("right_leg");
			this.leftLeg = root.getChild("left_leg");
			this.rightWing = root.getChild("right_wing");
			this.leftWing = root.getChild("left_wing");
		}

		@Override
		public void setupAnim(ChickenState s) {
			super.setupAnim(s);
			float deg = (float) Math.PI / 180.0F;
			this.head.xRot = s.xRot * deg;
			this.head.yRot = s.yRot * deg;
			this.bill.xRot = this.chin.xRot = this.head.xRot;
			this.bill.yRot = this.chin.yRot = this.head.yRot;
			this.rightLeg.xRot = Mth.cos(s.walkAnimationPos * 0.6662F) * 1.4F * s.walkAnimationSpeed;
			this.leftLeg.xRot = Mth.cos(s.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * s.walkAnimationSpeed;
			this.rightWing.zRot = s.wing;
			this.leftWing.zRot = -s.wing;
		}
	}

	public static class Villager extends EntityModel<LivingEntityRenderState> {
		private final ModelPart head, rightLeg, leftLeg;

		Villager(ModelPart root) {
			super(root);
			this.head = root.getChild("head");
			this.rightLeg = root.getChild("right_leg");
			this.leftLeg = root.getChild("left_leg");
		}

		@Override
		public void setupAnim(LivingEntityRenderState s) {
			super.setupAnim(s);
			float deg = (float) Math.PI / 180.0F;
			this.head.yRot = s.yRot * deg;
			this.head.xRot = s.xRot * deg;
			this.rightLeg.xRot = Mth.cos(s.walkAnimationPos * 0.6662F) * 1.4F * s.walkAnimationSpeed * 0.5F;
			this.leftLeg.xRot = Mth.cos(s.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * s.walkAnimationSpeed * 0.5F;
		}
	}

	// ------------------------------------------------------------------ renderers

	public static class Cow extends MobRenderer<TaintCow, LivingEntityRenderState, Quadruped> {
		public Cow(EntityRendererProvider.Context ctx) {
			super(ctx, new Quadruped(cowMesh().bakeRoot()), 0.7F);
		}

		@Override
		public LivingEntityRenderState createRenderState() {
			return new LivingEntityRenderState();
		}

		@Override
		public Identifier getTextureLocation(LivingEntityRenderState s) {
			return tex("cow");
		}
	}

	public static class Pig extends MobRenderer<TaintPig, LivingEntityRenderState, Quadruped> {
		public Pig(EntityRendererProvider.Context ctx) {
			super(ctx, new Quadruped(pigMesh().bakeRoot()), 0.7F);
		}

		@Override
		public LivingEntityRenderState createRenderState() {
			return new LivingEntityRenderState();
		}

		@Override
		public Identifier getTextureLocation(LivingEntityRenderState s) {
			return tex("pig");
		}
	}

	public static class ChickenRenderer extends MobRenderer<TaintChicken, ChickenState, Chicken> {
		public ChickenRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, new Chicken(chickenMesh().bakeRoot()), 0.3F);
		}

		@Override
		public ChickenState createRenderState() {
			return new ChickenState();
		}

		@Override
		public void extractRenderState(TaintChicken e, ChickenState s, float a) {
			super.extractRenderState(e, s, a);
			float flap = Mth.lerp(a, e.oFlap, e.flap);
			float speed = Mth.lerp(a, e.oFlapSpeed, e.flapSpeed);
			s.wing = (Mth.sin(flap) + 1.0F) * speed;
		}

		@Override
		public Identifier getTextureLocation(ChickenState s) {
			return tex("chicken");
		}
	}

	public static class VillagerRenderer extends MobRenderer<TaintVillager, LivingEntityRenderState, Villager> {
		public VillagerRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, new Villager(villagerMesh().bakeRoot()), 0.5F);
		}

		@Override
		public LivingEntityRenderState createRenderState() {
			return new LivingEntityRenderState();
		}

		@Override
		protected void scale(LivingEntityRenderState s, PoseStack ps) {
			ps.scale(0.9375F, 0.9375F, 0.9375F);
		}

		@Override
		public Identifier getTextureLocation(LivingEntityRenderState s) {
			return tex("villager");
		}
	}

	public static class Sheep extends MobRenderer<TaintSheep, SheepRenderState, SheepModel> {
		public Sheep(EntityRendererProvider.Context ctx) {
			super(ctx, new SheepModel(ctx.bakeLayer(ModelLayers.SHEEP)), 0.7F);
			SheepFurModel fur = new SheepFurModel(ctx.bakeLayer(ModelLayers.SHEEP_WOOL));
			this.addLayer(new RenderLayer<>(this) {
				@Override
				public void submit(PoseStack ps, SubmitNodeCollector collector, int light, SheepRenderState s, float yRot, float xRot) {
					if (!s.isSheared && !s.isInvisible) {
						collector.order(1).submitModel(fur, s, ps, RenderTypes.entityCutout(tex("sheep_fur")), light,
							LivingEntityRenderer.getOverlayCoords(s, 0.0F), s.outlineColor);
					}
				}
			});
		}

		@Override
		public SheepRenderState createRenderState() {
			return new SheepRenderState();
		}

		@Override
		public void extractRenderState(TaintSheep e, SheepRenderState s, float a) {
			super.extractRenderState(e, s, a);
			s.headEatPositionScale = e.getHeadEatPositionScale(a);
			s.headEatAngleScale = e.getHeadEatAngleScale(a);
			s.isSheared = e.isSheared();
		}

		@Override
		public Identifier getTextureLocation(SheepRenderState s) {
			return tex("sheep");
		}
	}

	/** RenderTaintCreeper: swells and flashes white as it is about to burst. */
	public static class Creeper extends MobRenderer<TaintCreeper, CreeperRenderState, CreeperModel> {
		public Creeper(EntityRendererProvider.Context ctx) {
			super(ctx, new CreeperModel(ctx.bakeLayer(ModelLayers.CREEPER)), 0.5F);
		}

		@Override
		public CreeperRenderState createRenderState() {
			return new CreeperRenderState();
		}

		@Override
		public void extractRenderState(TaintCreeper e, CreeperRenderState s, float a) {
			super.extractRenderState(e, s, a);
			s.swelling = e.getSwelling(a);
		}

		@Override
		protected void scale(CreeperRenderState s, PoseStack ps) {
			float g = s.swelling;
			float wobble = 1.0F + Mth.sin(g * 100.0F) * g * 0.01F;
			g = Mth.clamp(g, 0.0F, 1.0F);
			g *= g;
			g *= g;
			float hs = (1.0F + g * 0.4F) * wobble;
			float vs = (1.0F + g * 0.1F) / wobble;
			ps.scale(hs, vs, hs);
		}

		@Override
		protected float getWhiteOverlayProgress(CreeperRenderState s) {
			float step = s.swelling;
			return (int) (step * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(step, 0.5F, 1.0F);
		}

		@Override
		public Identifier getTextureLocation(CreeperRenderState s) {
			return tex("creeper");
		}
	}

	private TaintedAnimalRenderers() {
	}
}
