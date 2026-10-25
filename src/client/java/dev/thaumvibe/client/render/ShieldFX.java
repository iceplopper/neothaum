package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

/**
 * PacketFXShield and FXShieldRunes: a hemisphere of runes flares up on the side of a shielded entity that was struck, burning through its
 * fifteen frames as it fades. Monsters' shields ripple instead.
 */
public final class ShieldFX {
	private static final net.minecraft.resources.Identifier MODEL = Thaumvibe.id("textures/models/hemis.obj");
	private static ObjModel model;

	private static final class Flare {
		final int entity;
		final float yaw;
		final float pitch;
		final int maxAge;
		int age;

		Flare(int entity, float yaw, float pitch, int maxAge) {
			this.entity = entity;
			this.yaw = yaw;
			this.pitch = pitch;
			this.maxAge = maxAge;
		}
	}

	private static final List<Flare> FLARES = new ArrayList<>();

	public static void onPacket(int source, int target) {
		var level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		Entity p = level.getEntity(source);
		if (p == null) {
			return;
		}
		int age = 8 + level.getRandom().nextInt(4);
		if (target >= 0) {
			Entity t = level.getEntity(target);
			float pitch = 90.0F, yaw = 0.0F;
			if (t != null) {
				double d0 = p.getX() - t.getX();
				double d1 = (p.getBoundingBox().minY + p.getBoundingBox().maxY) / 2.0 - (t.getBoundingBox().minY + t.getBoundingBox().maxY) / 2.0;
				double d2 = p.getZ() - t.getZ();
				double d3 = Math.sqrt(d0 * d0 + d2 * d2);
				yaw = (float) (Math.atan2(d2, d0) * 180.0 / Math.PI) - 90.0F;
				pitch = (float) -(Math.atan2(d1, d3) * 180.0 / Math.PI);
			}
			FLARES.add(new Flare(source, yaw, pitch, age));
		} else if (target == -1) {
			FLARES.add(new Flare(source, 0.0F, 90.0F, age));
			FLARES.add(new Flare(source, 0.0F, 270.0F, age));
		} else if (target == -2) {
			FLARES.add(new Flare(source, 0.0F, 270.0F, age));
		} else if (target == -3) {
			FLARES.add(new Flare(source, 0.0F, 90.0F, age));
		}
	}

	public static void tick(Minecraft mc) {
		for (Iterator<Flare> it = FLARES.iterator(); it.hasNext(); ) {
			Flare f = it.next();
			if (f.age++ >= f.maxAge) {
				it.remove();
			}
		}
	}

	public static void render(LevelRenderContext ctx) {
		var mc = Minecraft.getInstance();
		if (FLARES.isEmpty() || mc.level == null) {
			return;
		}
		if (model == null) {
			model = ObjModel.load(MODEL);
		}
		float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		Vec3 cam = mc.gameRenderer.mainCamera().position();
		PoseStack ps = ctx.poseStack();
		for (Flare f : FLARES) {
			Entity target = mc.level.getEntity(f.entity);
			if (target == null) {
				continue;
			}
			float fade = (f.age + pt) / f.maxAge;
			int frame = Math.min(15, (int) (14.0F * fade) + 1);
			boolean mob = target instanceof Monster;
			var tex = Thaumvibe.id("textures/models/" + (mob ? "ripple" : "hemis") + frame + ".png");
			float b = mob ? 0.5F : 1.0F;
			float alpha = Math.min(1.0F, (1.0F - fade) * 3.0F);
			Vec3 pos = target.getPosition(pt);
			double cy = pos.y + target.getBbHeight() / 2.0;
			ps.pushPose();
			ps.translate(pos.x - cam.x, cy - cam.y, pos.z - cam.z);
			ps.rotateDegrees(Axis.YP, 180.0F - f.yaw);
			ps.rotateDegrees(Axis.XP, -f.pitch);
			float s = 0.4F * target.getBbHeight();
			ps.scale(s, s, s);
			int argb = Mth.clamp((int) (alpha * 255), 0, 255) << 24 | (int) (b * 255) << 16 | (int) (b * 255) << 8 | (int) (b * 255);
			ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(tex, true, true),
				(pose, vc) -> model.render(pose, vc, 0xDC00DC, OverlayTexture.NO_OVERLAY, argb));
			ps.popPose();
		}
	}

	private ShieldFX() {
	}
}
