package dev.thaumvibe.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.golem.GolemCoreType;
import dev.thaumvibe.golem.GolemEntity;
import dev.thaumvibe.golem.GolemType;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GolemRenderer extends MobRenderer<GolemEntity, GolemRenderer.State, GolemRenderer.GolemModel> {
	private static final Identifier DECORATION_TEXTURE = Thaumvibe.id("textures/entity/golem/golem_decoration.png");
	private static final Identifier DAMAGE_TEXTURE = Thaumvibe.id("textures/entity/golem/golem_damage.png");

	private final GolemDecorationModel decorationModel;

	public static class State extends LivingEntityRenderState {
		public GolemType golemType = GolemType.STRAW;
		public boolean advanced = false;
		public GolemCoreType core = GolemCoreType.BLANK;
		public String decorations = "";
		public List<Integer> upgrades = List.of();
		public float bootup = 0.0F;
		public boolean inactive = false;
		public float healthPercent = 1.0F;
		public final ItemStackRenderState carriedItem = new ItemStackRenderState();
		public boolean hasCarriedItem = false;
		public int carryLimit = 1;
	}

	public static class GolemModel extends EntityModel<State> {
		public final ModelPart head;
		public final ModelPart body;
		public final ModelPart rightArm;
		public final ModelPart leftArm;
		public final ModelPart rightLeg;
		public final ModelPart leftLeg;

		public GolemModel(ModelPart root) {
			super(root);
			this.head = root.getChild("head");
			this.body = root.getChild("body");
			this.rightArm = root.getChild("right_arm");
			this.leftArm = root.getChild("left_arm");
			this.rightLeg = root.getChild("right_leg");
			this.leftLeg = root.getChild("left_leg");
		}

		public static LayerDefinition createMesh() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition r = mesh.getRoot();

			// Pivot offset f2 = -7.0F aligns feet to Y = 24.0F in modern model space
			r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.0F, -11.0F, -5.5F, 8, 9, 8), PartPose.offset(0.0F, -7.0F, -2.0F));

			r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40)
				.addBox(-8.0F, -2.0F, -6.0F, 16, 12, 11)
				.texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9, 5, 6, new CubeDeformation(0.5F)),
				PartPose.offset(0.0F, -7.0F, 0.0F));

			r.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 21)
				.addBox(-12.0F, -2.5F, -3.0F, 4, 25, 6), PartPose.offset(0.0F, -7.0F, 0.0F));

			r.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 21).mirror()
				.addBox(8.0F, -2.5F, -3.0F, 4, 25, 6), PartPose.offset(0.0F, -7.0F, 0.0F));

			r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(37, 0)
				.addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5), PartPose.offset(-4.0F, 11.0F, 0.0F));

			r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(37, 0).mirror()
				.addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5), PartPose.offset(5.0F, 11.0F, 0.0F));

			return LayerDefinition.create(mesh, 128, 128);
		}

		private static float triangleWave(float par1, float par2) {
			return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
		}

		@Override
		public void setupAnim(State s) {
			super.setupAnim(s);
			float bu = s.bootup;
			boolean hasCore = s.core != null && s.core != GolemCoreType.BLANK;

			if (hasCore && bu >= 0.0F) {
				if (s.inactive) {
					this.head.yRot = 0.0F;
					this.head.xRot = 0.57595867F;
				} else if (bu < 1.0F && bu > 0.0F) {
					this.head.yRot = 0.0F;
					this.head.xRot = (1.0F - bu) * 0.57595867F;
				} else {
					this.head.yRot = s.yRot * ((float) Math.PI / 180.0F);
					this.head.xRot = s.xRot * ((float) Math.PI / 180.0F);
				}

				float limb = s.walkAnimationPos;
				float amount = s.walkAnimationSpeed;

				this.rightLeg.xRot = -1.5F * triangleWave(limb, 13.0F) * amount;
				this.leftLeg.xRot = 1.5F * triangleWave(limb, 13.0F) * amount;
				this.rightLeg.yRot = 0.0F;
				this.leftLeg.yRot = 0.0F;
				this.leftArm.zRot = 0.0F;
				this.rightArm.zRot = 0.0F;

				if (s.hasCarriedItem) {
					this.rightArm.xRot = -1.0F;
					this.leftArm.xRot = -1.0F;
				} else {
					this.rightArm.xRot = (-0.2F + 1.5F * triangleWave(limb, 13.0F)) * amount;
					this.leftArm.xRot = (-0.2F - 1.5F * triangleWave(limb, 13.0F)) * amount;
				}
			} else {
				this.head.yRot = 0.0F;
				this.head.xRot = 0.57595867F;
				this.rightLeg.xRot = 0.0F;
				this.leftLeg.xRot = 0.0F;
				this.rightArm.xRot = 0.0F;
				this.leftArm.xRot = 0.0F;
				this.rightLeg.yRot = 0.0F;
				this.leftLeg.yRot = 0.0F;
				this.leftArm.zRot = 0.0F;
				this.rightArm.zRot = 0.0F;
			}
		}
	}

	public static class GolemDecorationModel extends EntityModel<State> {
		public final ModelPart head;
		public final ModelPart body;
		public final ModelPart rightArm;
		public final ModelPart leftArm;

		public final ModelPart fez;
		public final ModelPart hat;
		public final ModelPart hatRim;
		public final ModelPart glasses;
		public final ModelPart visor;
		public final ModelPart jar;
		public final ModelPart brain;
		public final ModelPart evilHead;

		public final ModelPart bowtie;
		public final ModelPart plate;
		public final ModelPart plateLeft;
		public final ModelPart plateRight;

		public final ModelPart mace;
		public final ModelPart dartgun;

		public GolemDecorationModel(ModelPart root) {
			super(root);
			this.head = root.getChild("head");
			this.body = root.getChild("body");
			this.rightArm = root.getChild("right_arm");
			this.leftArm = root.getChild("left_arm");

			this.fez = this.head.getChild("fez");
			this.hat = this.head.getChild("hat");
			this.hatRim = this.head.getChild("hat_rim");
			this.glasses = this.head.getChild("glasses");
			this.visor = this.head.getChild("visor");
			this.jar = this.head.getChild("jar");
			this.brain = this.head.getChild("brain");
			this.evilHead = this.head.getChild("evil_head");

			this.bowtie = this.body.getChild("bowtie");
			this.plate = this.body.getChild("plate");
			this.plateLeft = this.body.getChild("plate_left");
			this.plateRight = this.body.getChild("plate_right");

			this.mace = this.rightArm.getChild("mace");
			this.dartgun = this.leftArm.getChild("dartgun");
		}

		public static LayerDefinition createMesh() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition r = mesh.getRoot();

			PartDefinition head = r.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, -2.0F));
			head.addOrReplaceChild("fez", CubeListBuilder.create().texOffs(0, 94).addBox(-4.5F, -15.0F, -6.0F, 9, 7, 9), PartPose.ZERO);
			head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 110).addBox(-4.5F, -17.0F, -6.0F, 9, 9, 9), PartPose.ZERO);
			head.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(36, 114).addBox(-6.5F, -9.0F, -8.0F, 13, 1, 13), PartPose.ZERO);
			head.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(0, 80).addBox(-4.5F, -8.0F, -6.0F, 9, 4, 9), PartPose.ZERO);
			head.addOrReplaceChild("visor", CubeListBuilder.create().texOffs(0, 70).addBox(-5.0F, -8.0F, -6.0F, 10, 5, 5), PartPose.ZERO);
			head.addOrReplaceChild("jar", CubeListBuilder.create().texOffs(96, 56).addBox(-4.0F, -15.0F, -5.5F, 8, 4, 8), PartPose.ZERO);
			head.addOrReplaceChild("brain", CubeListBuilder.create().texOffs(96, 70).addBox(-3.5F, -14.0F, -5.0F, 7, 3, 7), PartPose.ZERO);
			head.addOrReplaceChild("evil_head", CubeListBuilder.create().texOffs(64, 65).addBox(-4.0F, -9.0F, -5.5F, 8, 7, 8), PartPose.ZERO);

			PartDefinition body = r.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));
			body.addOrReplaceChild("bowtie", CubeListBuilder.create().texOffs(0, 0).addBox(-8.5F, -2.0F, -6.5F, 17, 4, 12), PartPose.ZERO);
			body.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(32, 40).addBox(-6.5F, -1.0F, -7.0F, 13, 12, 13), PartPose.ZERO);
			body.addOrReplaceChild("plate_left", CubeListBuilder.create().texOffs(0, 44).addBox(-8.5F, -4.0F, -6.5F, 3, 6, 12), PartPose.ZERO);
			body.addOrReplaceChild("plate_right", CubeListBuilder.create().texOffs(0, 44).mirror().addBox(5.5F, -4.0F, -6.5F, 3, 6, 12), PartPose.ZERO);

			PartDefinition rightArm = r.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));
			rightArm.addOrReplaceChild("mace", CubeListBuilder.create().texOffs(80, 26).addBox(-13.0F, 15.0F, -5.0F, 6, 8, 10), PartPose.ZERO);

			PartDefinition leftArm = r.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));
			leftArm.addOrReplaceChild("dartgun", CubeListBuilder.create().texOffs(80, 80).addBox(7.9F, 7.5F, -3.5F, 6, 16, 7), PartPose.ZERO);

			return LayerDefinition.create(mesh, 128, 128);
		}

		public void syncAngles(GolemModel base, State s) {
			this.head.xRot = base.head.xRot;
			this.head.yRot = base.head.yRot;
			this.head.zRot = base.head.zRot;

			this.body.xRot = base.body.xRot;
			this.body.yRot = base.body.yRot;
			this.body.zRot = base.body.zRot;

			this.rightArm.xRot = base.rightArm.xRot;
			this.rightArm.yRot = base.rightArm.yRot;
			this.rightArm.zRot = base.rightArm.zRot;

			this.leftArm.xRot = base.leftArm.xRot;
			this.leftArm.yRot = base.leftArm.yRot;
			this.leftArm.zRot = base.leftArm.zRot;

			String deco = s.decorations;
			this.fez.visible = deco.contains("F");
			this.hat.visible = deco.contains("H");
			this.hatRim.visible = deco.contains("H");
			this.glasses.visible = deco.contains("G");
			this.visor.visible = deco.contains("V");
			this.bowtie.visible = deco.contains("B");
			this.plate.visible = deco.contains("P");
			this.plateLeft.visible = deco.contains("P");
			this.plateRight.visible = deco.contains("P");
			this.dartgun.visible = deco.contains("R");
			this.mace.visible = deco.contains("M");

			this.brain.visible = s.advanced;
			this.jar.visible = s.advanced;
			this.evilHead.visible = s.advanced && s.core != null && s.core != GolemCoreType.BLANK;
		}

		@Override
		public void setupAnim(State s) {
			super.setupAnim(s);
		}
	}

	public GolemRenderer(EntityRendererProvider.Context context) {
		super(context, new GolemModel(GolemModel.createMesh().bakeRoot()), 0.25F);
		this.decorationModel = new GolemDecorationModel(GolemDecorationModel.createMesh().bakeRoot());

		// Decoration layer
		this.addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack ps, SubmitNodeCollector collector, int light, State state, float yRot, float xRot) {
				if (!state.isInvisible) {
					GolemRenderer.this.decorationModel.syncAngles(GolemRenderer.this.getModel(), state);
					collector.order(1).submitModel(GolemRenderer.this.decorationModel, state, ps,
						RenderTypes.entityCutout(DECORATION_TEXTURE, false), light,
						LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
				}
			}
		});

		// Damage overlay layer
		this.addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack ps, SubmitNodeCollector collector, int light, State state, float yRot, float xRot) {
				if (!state.isInvisible && state.healthPercent < 1.0F) {
					collector.order(2).submitModel(GolemRenderer.this.getModel(), state, ps,
						RenderTypes.entityTranslucent(DAMAGE_TEXTURE), light,
						LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
				}
			}
		});

		// Carried item layer
		this.addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack ps, SubmitNodeCollector collector, int light, State state, float yRot, float xRot) {
				if (!state.isInvisible && state.hasCarriedItem && !state.carriedItem.isEmpty()) {
					ps.pushPose();
					// Held in front of golem chest
					ps.translate(0.0F, -0.6F, -0.35F);
					ps.scale(0.5F, 0.5F, 0.5F);
					state.carriedItem.submit(ps, collector, light, OverlayTexture.NO_OVERLAY, 0);
					ps.popPose();
				}
			}
		});
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(GolemEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.golemType = entity.getGolemType();
		state.advanced = entity.isAdvanced();
		state.core = GolemCoreType.byId(entity.getCore());
		state.decorations = entity.getDecoration();
		state.upgrades = entity.getUpgrades();
		state.bootup = entity.getBootup();
		state.inactive = entity.isInactive();
		state.healthPercent = Math.max(0.0F, Math.min(1.0F, entity.getHealth() / entity.getMaxHealth()));

		ItemStack carried = entity.getCarried();
		if (!carried.isEmpty()) {
			state.hasCarriedItem = true;
			this.itemModelResolver.updateForTopItem(state.carriedItem, carried, ItemDisplayContext.GROUND, entity.level(), entity, 0);
		} else {
			state.hasCarriedItem = false;
			state.carriedItem.clear();
		}
	}

	@Override
	public Identifier getTextureLocation(State state) {
		String name = state.golemType != null ? state.golemType.name().toLowerCase() : "straw";
		return Thaumvibe.id("textures/entity/golem/golem_" + name + ".png");
	}

	@Override
	protected void scale(State state, PoseStack ps) {
		ps.scale(0.4F, 0.4F, 0.4F);
	}
}
