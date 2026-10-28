package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.client.render.ObjModel;
import dev.neothaum.client.render.TVRenderTypes;
import dev.neothaum.entity.projectile.Ember;
import dev.neothaum.entity.projectile.FrostShard;
import java.util.Random;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** RenderEmber, RenderElectricOrb, RenderExplosiveOrb, RenderFrostShard and RenderPrimalOrb: the focus projectiles. */
public final class FocusProjectileRenderers {
	public static class State extends EntityRenderState {
		int ticks;
		float partial;
		int id;
		float duration = 20.0F;
		float damage;
		float yRot;
		float xRot;
	}

	private abstract static class Base<T extends Entity> extends EntityRenderer<T, State> {
		Base(EntityRendererProvider.Context ctx, float shadow) {
			super(ctx);
			this.shadowRadius = shadow;
		}

		@Override
		public State createRenderState() {
			return new State();
		}

		@Override
		public void extractRenderState(T e, State s, float pt) {
			super.extractRenderState(e, s, pt);
			s.ticks = e.tickCount;
			s.partial = pt;
			s.id = e.getId();
			s.yRot = Mth.lerp(pt, e.yRotO, e.getYRot());
			s.xRot = Mth.lerp(pt, e.xRotO, e.getXRot());
			if (e instanceof Ember em) {
				s.duration = em.getDuration();
			}
			if (e instanceof FrostShard fs) {
				s.damage = fs.getDamage();
			}
		}
	}

	/** A camera-facing card of one frame of a particle sheet. */
	static void card(PoseStack ps, SubmitNodeCollector c, CameraRenderState cam, Identifier tex, boolean additive, float scale, float u0, float u1,
		float v0, float v1, float alpha) {
		ps.pushPose();
		ps.rotate(cam.orientation);
		ps.scale(scale, scale, scale);
		c.submitCustomGeometry(ps, TVRenderTypes.glow(tex, additive, true), (pose, vc) -> {
			v(pose, vc, -0.5F, -0.5F, u1, v1, alpha);
			v(pose, vc, 0.5F, -0.5F, u0, v1, alpha);
			v(pose, vc, 0.5F, 0.5F, u0, v0, alpha);
			v(pose, vc, -0.5F, 0.5F, u1, v0, alpha);
		});
		ps.popPose();
	}

	private static void v(PoseStack.Pose pose, VertexConsumer vc, float x, float y, float u, float v, float a) {
		vc.addVertex(pose, x, y, 0.0F).setColor(1.0F, 1.0F, 1.0F, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
			.setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	/** RenderEmber: eight frames of flame that swell as the ember burns out. */
	public static class EmberRenderer extends Base<Ember> {
		public EmberRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, 0.0F);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
			float fc = s.ticks / s.duration;
			int p = (int) (8.0F * fc);
			float u0 = (7 + p) / 16.0F;
			card(ps, c, cam, TVRenderTypes.PARTICLES, true, 0.25F + fc, u0, u0 + 0.0625F, 0.5625F, 0.625F, 0.9F);
		}
	}

	/** RenderElectricOrb: a bobbing, flickering ball of lightning. */
	public static class ShockOrbRenderer extends Base<Entity> {
		public ShockOrbRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, 0.1F);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
			float u0 = (1 + s.ticks % 6) / 8.0F;
			float bob = Mth.sin(s.ticks / 5.0F) * 0.2F + 0.2F;
			card(ps, c, cam, TVRenderTypes.PARTICLES, true, 1.0F + bob, u0, u0 + 0.125F, 0.875F, 1.0F, 0.8F);
		}
	}

	/** RenderExplosiveOrb: four frames of a smouldering fireball from particles2.png, drawn normally rather than additively. */
	public static class ExplosiveOrbRenderer extends Base<Entity> {
		public ExplosiveOrbRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, 0.1F);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
			float u0 = s.ticks % 4 / 16.0F;
			card(ps, c, cam, TVRenderTypes.PARTICLES2, false, 0.7F, u0, u0 + 0.0625F, 0.8125F, 0.875F, 0.8F);
		}
	}

	/** RenderFrostShard: a lumpy orb of ice, sized by its damage and turned along its flight. */
	public static class FrostShardRenderer extends Base<FrostShard> {
		private static final Identifier ORB = NeoThaum.id("textures/models/orb.obj");
		private static final Identifier TEX = NeoThaum.id("textures/blocks/frostshard.png");
		private static ObjModel model;

		public FrostShardRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, 0.0F);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
			if (model == null) {
				model = ObjModel.load(ORB);
			}
			Random rnd = new Random(s.id);
			ps.pushPose();
			ps.rotateDegrees(Axis.YP, s.yRot);
			ps.rotateDegrees(Axis.ZP, s.xRot);
			float base = s.damage * 0.1F;
			ps.scale(base + rnd.nextFloat() * 0.1F, base + rnd.nextFloat() * 0.1F, base + rnd.nextFloat() * 0.1F);
			int light = s.lightCoords;
			c.submitCustomGeometry(ps, RenderTypes.entityTranslucent(TEX), (pose, vc) -> model.render(pose, vc, light, OverlayTexture.NO_OVERLAY,
				0xFFFFFFFF));
			ps.popPose();
		}
	}

	/** RenderPrimalOrb: twelve rays in the six primal colours wheeling about a white core, and a flickering spark on top. */
	public static class PrimalOrbRenderer extends Base<Entity> {
		private static final int[] COLORS = {0xFFFF7E, 0xFF3C01, 0x0090FF, 0x00A000, 0xEECCFF, 0x555577};

		public PrimalOrbRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, 0.1F);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
			float f1 = (s.ticks + s.partial) / 80.0F;
			Random random = new Random(s.id);
			float grow = Math.min(s.ticks, 10) / 10.0F;
			for (int i = 0; i < 12; i++) {
				ps.pushPose();
				ps.rotateDegrees(Axis.XP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.YP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.ZP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.XP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.YP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.ZP, random.nextFloat() * 360.0F + f1 * 360.0F);
				float fa = (random.nextFloat() * 20.0F + 5.0F) / 30.0F * grow;
				float f4 = (random.nextFloat() * 2.0F + 1.0F) / 30.0F * grow;
				int col = COLORS[i / 2];
				float r = (col >> 16 & 255) / 255.0F;
				float g = (col >> 8 & 255) / 255.0F;
				float b = (col & 255) / 255.0F;
				c.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.WHITE, true, true), (pose, vc) -> {
					// the 1.7 triangle fan: white at the centre fading to the colour, transparent, at the rim
					float[][] rim = {{-0.866F * f4, fa, -0.5F * f4}, {0.866F * f4, fa, -0.5F * f4}, {0.0F, fa, f4}};
					for (int k = 0; k < 3; k++) {
						float[] a = rim[k];
						float[] bb = rim[(k + 1) % 3];
						vc.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, a[0], a[1], a[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, bb[0], bb[1], bb[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, bb[0], bb[1], bb[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
					}
				});
				ps.popPose();
			}
			float u0 = s.ticks % 13 / 16.0F;
			card(ps, c, cam, TVRenderTypes.PARTICLES, true, 0.5F, u0, u0 + 0.0624375F, 0.125F, 0.1874375F, 0.8F);
		}
	}

	/** RenderEldritchOrb: the primal orb's rays, all in order's pale violet. */
	public static class EldritchOrbRenderer extends Base<Entity> {
		private static final int[] COLORS = {0xEECCFF, 0xEECCFF, 0xEECCFF, 0xEECCFF, 0xEECCFF, 0xEECCFF};

		public EldritchOrbRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, 0.1F);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
			float f1 = (s.ticks + s.partial) / 80.0F;
			Random random = new Random(s.id);
			float grow = Math.min(s.ticks, 10) / 10.0F;
			for (int i = 0; i < 12; i++) {
				ps.pushPose();
				ps.rotateDegrees(Axis.XP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.YP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.ZP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.XP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.YP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.ZP, random.nextFloat() * 360.0F + f1 * 360.0F);
				float fa = (random.nextFloat() * 20.0F + 5.0F) / 30.0F * grow;
				float f4 = (random.nextFloat() * 2.0F + 1.0F) / 30.0F * grow;
				int col = COLORS[i / 2];
				float r = (col >> 16 & 255) / 255.0F;
				float g = (col >> 8 & 255) / 255.0F;
				float b = (col & 255) / 255.0F;
				c.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.WHITE, true, true), (pose, vc) -> {
					// the 1.7 triangle fan: white at the centre fading to the colour, transparent, at the rim
					float[][] rim = {{-0.866F * f4, fa, -0.5F * f4}, {0.866F * f4, fa, -0.5F * f4}, {0.0F, fa, f4}};
					for (int k = 0; k < 3; k++) {
						float[] a = rim[k];
						float[] bb = rim[(k + 1) % 3];
						vc.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, a[0], a[1], a[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, bb[0], bb[1], bb[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, bb[0], bb[1], bb[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
					}
				});
				ps.popPose();
			}
			float u0 = s.ticks % 13 / 16.0F;
			card(ps, c, cam, TVRenderTypes.PARTICLES, true, 0.5F, u0, u0 + 0.0624375F, 0.125F, 0.1874375F, 0.8F);
		}
	}

	public static class GolemOrbRenderer extends Base<Entity> {
		private static final int[] COLORS = {0xDD0022, 0x990033, 0x880088, 0x550077, 0xAA0044, 0x330055};

		public GolemOrbRenderer(EntityRendererProvider.Context ctx) {
			super(ctx, 0.15F);
		}

		@Override
		public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
			float f1 = (s.ticks + s.partial) / 60.0F;
			Random random = new Random(s.id);
			float grow = Math.min(s.ticks, 10) / 10.0F;
			for (int i = 0; i < 8; i++) {
				ps.pushPose();
				ps.rotateDegrees(Axis.XP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.YP, random.nextFloat() * 360.0F);
				ps.rotateDegrees(Axis.ZP, random.nextFloat() * 360.0F + f1 * 360.0F);
				float fa = (random.nextFloat() * 20.0F + 5.0F) / 30.0F * grow;
				float f4 = (random.nextFloat() * 2.0F + 1.0F) / 30.0F * grow;
				int col = COLORS[i % COLORS.length];
				float r = (col >> 16 & 255) / 255.0F;
				float g = (col >> 8 & 255) / 255.0F;
				float b = (col & 255) / 255.0F;
				c.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.WHITE, true, true), (pose, vc) -> {
					float[][] rim = {{-0.866F * f4, fa, -0.5F * f4}, {0.866F * f4, fa, -0.5F * f4}, {0.0F, fa, f4}};
					for (int k = 0; k < 3; k++) {
						float[] a = rim[k];
						float[] bb = rim[(k + 1) % 3];
						vc.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(1.0F, 0.5F, 0.8F, 1.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, a[0], a[1], a[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, bb[0], bb[1], bb[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
						vc.addVertex(pose, bb[0], bb[1], bb[2]).setColor(r, g, b, 0.0F).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
					}
				});
				ps.popPose();
			}
		}
	}

	private FocusProjectileRenderers() {
	}
}
