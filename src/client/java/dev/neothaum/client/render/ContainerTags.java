package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectContainer;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.items.RevealerGear;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.tiles.AlchemyFurnaceBlockEntity;
import dev.neothaum.tiles.JarBlockEntity;
import dev.neothaum.tiles.TubeBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * RenderEventHandler.blockHighlight / drawTagsOnContainer: while wearing goggles of revealing, the block you look at shows what essentia it
 * holds, as a row of aspect icons with their amounts floating over it (or out of the face you look at, if something is on top). The tags grow
 * in while you keep looking and shrink away when you look elsewhere.
 */
public final class ContainerTags {
	private static final Identifier UNKNOWN = NeoThaum.id("textures/aspects/_unknown.png");
	/** The dark rounded square TC4 sets aspect icons on (UtilsFX.drawTag), so an icon still reads against essentia of its own colour. */
	private static final Identifier BACK = NeoThaum.id("textures/aspects/_back.png");
	private static float tagscale;

	/** What the block at pos holds: IAspectContainers, jars, and (beyond TC4) tubes and the alchemical furnace. */
	private static AspectList contents(BlockEntity be) {
		if (be instanceof dev.neothaum.tiles.CrucibleBlockEntity crucible) {
			return crucible.aspects;
		}
		if (be instanceof JarBlockEntity jar) {
			return jar.getAspects();
		}
		if (be instanceof AspectContainer c) {
			return c.getAspects();
		}
		if (be instanceof TubeBlockEntity tube) {
			Aspect a = tube.getEssentiaType(null);
			int amount = tube.getEssentiaAmount(null);
			return a != null && amount > 0 ? new AspectList().add(a, amount) : null;
		}
		if (be instanceof AlchemyFurnaceBlockEntity furnace) {
			return furnace.aspects;
		}
		return null;
	}

	public static void render(LevelRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		// RenderEventHandler.renderLast: the tags fade a little every frame
		if (tagscale > 0.0F) {
			tagscale = Math.max(0.0F, tagscale - 0.005F);
		}
		if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		ItemStack head = mc.player.getItemBySlot(EquipmentSlot.HEAD);
		boolean hasGoggles = head.getItem() instanceof RevealerGear r && r.showNodes(head);
		boolean hasThaumometer = mc.player.getMainHandItem().getItem() instanceof dev.neothaum.items.ThaumometerItem
			|| mc.player.getOffhandItem().getItem() instanceof dev.neothaum.items.ThaumometerItem;
		if (!hasGoggles && !hasThaumometer) {
			return;
		}
		BlockPos pos = hit.getBlockPos();
		AspectList tags = contents(mc.level.getBlockEntity(pos));
		if (tags == null || tags.size() == 0) {
			return;
		}
		if (tagscale < 0.3F) {
			tagscale += 0.031F - tagscale / 10.0F;
		}
		boolean spaceAbove = mc.level.isEmptyBlock(pos.above());
		Direction dir = spaceAbove ? Direction.UP : hit.getDirection();
		drawTagsOnContainer(ctx, pos.getX(), pos.getY() + (spaceAbove ? 0.4F : 0.0F), pos.getZ(), tags, 220, dir,
			ResearchManager.knowledge(mc.player));
	}

	private static void drawTagsOnContainer(LevelRenderContext ctx, double x, double y, double z, AspectList tags, int bright, Direction dir,
		PlayerKnowledge knowledge) {
		Minecraft mc = Minecraft.getInstance();
		Vec3 cam = mc.gameRenderer.mainCamera().position();
		Font font = mc.font;
		PoseStack ps = ctx.poseStack();
		int rowsize = 5;
		int current = 0;
		float shifty = 0.0F;
		int left = tags.size();
		for (Aspect tag : tags.getAspects()) {
			int div = Math.min(left, rowsize);
			if (current >= rowsize) {
				current = 0;
				shifty -= tagscale * 1.05F;
				left -= rowsize;
				if (left < rowsize) {
					div = left % rowsize;
				}
			}
			float shift = (current - div / 2.0F + 0.5F) * tagscale * 4.0F;
			shift *= tagscale;
			int c = tag.getColor();
			float r = (c >> 16 & 255) / 255.0F, g = (c >> 8 & 255) / 255.0F, b = (c & 255) / 255.0F;
			ps.pushPose();
			ps.translate(-cam.x + x + 0.5 + tagscale * 2.0F * dir.getStepX(), -cam.y + y - shifty + 0.5 + tagscale * 2.0F * dir.getStepY(),
				-cam.z + z + 0.5 + tagscale * 2.0F * dir.getStepZ());
			float xd = (float) (cam.x - (x + 0.5));
			float zd = (float) (cam.z - (z + 0.5));
			float rotYaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
			ps.rotateDegrees(Axis.YP, rotYaw + 180.0F);
			ps.translate(shift, 0.0F, 0.0F);
			ps.rotateDegrees(Axis.ZP, 180.0F);
			ps.scale(tagscale, tagscale, tagscale);
			Identifier tex = knowledge.hasDiscoveredAspect(tag) ? tag.getImage() : UNKNOWN;
			// the backing goes in first (order 0) so the icon and number draw over it
			ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(BACK, false, false), (pose, vc) -> back(pose, vc, bright));
			ctx.submitNodeCollector().order(1).submitCustomGeometry(ps, TVRenderTypes.glow(tex, false, false), (pose, vc) -> quad(pose, vc, r, g, b, bright));
			if (tags.getAmount(tag) >= 0) {
				String am = String.valueOf(tags.getAmount(tag));
				// a pose of its own: the icon above keeps a reference to the current one until it is drawn
				ps.pushPose();
				ps.scale(0.04F, 0.04F, 0.04F);
				ps.translate(0.0F, 6.0F, -0.1F);
				int sw = font.width(am);
				var seq = Component.literal(am).getVisualOrderText();
				ctx.submitNodeCollector().order(1).submitText(ps, 14 - sw, 1, seq, false, Font.DisplayMode.SEE_THROUGH, bright, 0xFF111111, 0, 0);
				ps.translate(0.0F, 0.0F, -0.1F);
				ctx.submitNodeCollector().order(1).submitText(ps, 13 - sw, 0, seq, false, Font.DisplayMode.SEE_THROUGH, bright, 0xFFFFFFFF, 0, 0);
				ps.popPose();
			}
			ps.popPose();
			current++;
		}
	}

	/** UtilsFX.renderQuadCenteredFromTexture: a unit square in the aspect's colour, three quarters opaque. */
	private static void quad(PoseStack.Pose pose, VertexConsumer vc, float r, float g, float b, int bright) {
		v(pose, vc, -0.5F, 0.5F, 0.0F, 1.0F, r, g, b, bright);
		v(pose, vc, 0.5F, 0.5F, 1.0F, 1.0F, r, g, b, bright);
		v(pose, vc, 0.5F, -0.5F, 1.0F, 0.0F, r, g, b, bright);
		v(pose, vc, -0.5F, -0.5F, 0.0F, 0.0F, r, g, b, bright);
	}

	/** The backing square, 20 pixels to the icon's 16 as in the GUI. */
	private static void back(PoseStack.Pose pose, VertexConsumer vc, int bright) {
		float s = 0.625F;
		vb(pose, vc, -s, s, 0.0F, 1.0F, bright);
		vb(pose, vc, s, s, 1.0F, 1.0F, bright);
		vb(pose, vc, s, -s, 1.0F, 0.0F, bright);
		vb(pose, vc, -s, -s, 0.0F, 0.0F, bright);
	}

	private static void vb(PoseStack.Pose pose, VertexConsumer vc, float x, float y, float u, float v, int bright) {
		vc.addVertex(pose, x, y, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v).setOverlay(0).setLight(bright).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	private static void v(PoseStack.Pose pose, VertexConsumer vc, float x, float y, float u, float v, float r, float g, float b, int bright) {
		vc.addVertex(pose, x, y, 0.0F).setColor(r, g, b, 0.75F).setUv(u, v).setOverlay(0).setLight(bright).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	private ContainerTags() {
	}
}
