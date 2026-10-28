package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.client.fx.BoltFX;
import dev.neothaum.items.equipment.ThaumostaticHarnessItem;
import dev.neothaum.registry.TVComponents;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * ModelHoverHarness: the harness's brass backpack (hoverharness.obj) worn over the chest piece, and, while it hovers, a crackling ring of
 * lightning on the back and little bolts snapping from it to the blocks nearby.
 */
public class HarnessLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final Identifier MODEL = NeoThaum.id("textures/models/hoverharness.obj");
	private static final Identifier BACK = NeoThaum.id("textures/models/hoverharness2.png");
	private static final Identifier RING = NeoThaum.id("textures/items/lightningring.png");
	private static final Map<Integer, Long> TIMING_SHOCK = new HashMap<>();
	private static ObjModel model;

	public HarnessLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	private static boolean hovering(net.minecraft.world.item.ItemStack chest) {
		return chest.getItem() instanceof ThaumostaticHarnessItem && Boolean.TRUE.equals(chest.get(TVComponents.HARNESS_ACTIVE));
	}

	@Override
	public void submit(PoseStack ps, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
		if (!(state.chestEquipment.getItem() instanceof ThaumostaticHarnessItem) || state.isInvisible) {
			return;
		}
		if (model == null) {
			model = ObjModel.load(MODEL);
		}
		ps.pushPose();
		this.getParentModel().body.translateAndRotate(ps);
		ps.pushPose();
		ps.scale(0.1F, 0.1F, 0.1F);
		ps.rotateDegrees(Axis.XP, -90.0F);
		ps.translate(0.0F, 0.33F, -3.7F);
		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(BACK), (pose, vc) -> model.render(pose, vc, 0xF000F0, OverlayTexture.NO_OVERLAY, -1));
		ps.popPose();
		if (hovering(state.chestEquipment)) {
			ps.translate(0.0F, 0.2F + (state.isCrouching ? 0.075F : 0.0F), 0.55F - (state.isCrouching ? 0.05F : 0.0F));
			int frame = (int) state.ageInTicks % 16;
			ring(ps, collector, 2.5F, 1.0F, 1.0F, 1.0F, frame);
			ps.rotateDegrees(Axis.YP, 180.0F);
			ps.translate(0.0F, 0.0F, 0.03F);
			ring(ps, collector, 1.5F, 1.0F, 0.5F, 1.0F, frame);
		}
		ps.popPose();
	}

	/** UtilsFX.renderQuadCenteredFromIcon with additive blending, one frame of the animated ring. */
	private static void ring(PoseStack ps, SubmitNodeCollector collector, float size, float r, float g, float b, int frame) {
		float v0 = frame / 16.0F;
		float v1 = (frame + 1) / 16.0F;
		float h = size / 2.0F;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(RING, true, true), (pose, vc) -> {
			v(pose, vc, -h, h, 0.0F, v1, r, g, b);
			v(pose, vc, h, h, 1.0F, v1, r, g, b);
			v(pose, vc, h, -h, 1.0F, v0, r, g, b);
			v(pose, vc, -h, -h, 0.0F, v0, r, g, b);
		});
	}

	private static void v(PoseStack.Pose pose, VertexConsumer vc, float x, float y, float u, float v, float r, float g, float b) {
		vc.addVertex(pose, x, y, 0.0F).setColor(r, g, b, 1.0F).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xE600E6)
			.setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	/** Every 50-100 ms a hovering harness throws a little bolt from the wearer's back to a block within six blocks. */
	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.isPaused()) {
			return;
		}
		long now = System.currentTimeMillis();
		for (Player p : mc.level.players()) {
			if (!hovering(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST))) {
				continue;
			}
			long t = TIMING_SHOCK.getOrDefault(p.getId(), 0L);
			if (t >= now) {
				continue;
			}
			var rand = mc.level.getRandom();
			TIMING_SHOCK.put(p.getId(), now + 50L + rand.nextInt(50));
			float mod = p.isCrouching() ? 0.075F : 0.0F;
			double sx = p.getX() - Mth.cos((p.yBodyRot + 90.0F) / 180.0F * (float) Math.PI) * 0.5F;
			double sy = p.getY() + 1.6 - 0.45F - mod;
			double sz = p.getZ() - Mth.sin((p.yBodyRot + 90.0F) / 180.0F * (float) Math.PI) * 0.5F;
			float yaw = p.yBodyRot - 90.0F - rand.nextInt(180);
			float pitch = -80 + rand.nextInt(160);
			Vec3 from = new Vec3(p.getX(), sy, p.getZ());
			Vec3 dir = Vec3.directionFromRotation(pitch, yaw);
			var hit = mc.level.clip(new ClipContext(from, from.add(dir.scale(6.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
			if (hit.getType() == HitResult.Type.BLOCK) {
				Vec3 h = hit.getLocation();
				BoltFX.bolt(sx, sy, sz, h.x, h.y, h.z, 1, 2.0F, 3, 6, 0.015F);
			}
		}
	}
}
