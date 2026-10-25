package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.aspects.ObjectAspects;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * RenderEventHandler.startScan / showScannedBlocks: for five seconds after the Pickaxe of the Core is used, ores within eight blocks glow
 * through the stone (brighter for richer ores), and lava and water show as tinted outlines. It all fades out with distance from the spot.
 */
public final class OreScanOverlay {
	private static final Identifier WARDED_GLASS = Thaumvibe.id("textures/blocks/wardedglass.png");

	private record Scanned(BlockPos pos, int value) {
	}

	private static final List<Scanned> SCANNED = new ArrayList<>();
	private static BlockPos center = BlockPos.ZERO;
	private static long scanCount;

	public static void startScan(BlockPos pos) {
		var level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		SCANNED.clear();
		center = pos;
		scanCount = System.currentTimeMillis() + 5000L;
		int range = 8;
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-range, -range, -range), pos.offset(range, range, range))) {
			BlockState s = level.getBlockState(p);
			if (s.isAir() || s.is(Blocks.BEDROCK)) {
				continue;
			}
			int value = -1;
			if (s.getFluidState().is(FluidTags.LAVA)) {
				value = -10;
			} else if (s.getFluidState().is(FluidTags.WATER)) {
				value = -5;
			} else if (s.is(ConventionalBlockTags.ORES)) {
				var al = ObjectAspects.getAspects(new ItemStack(s.getBlock()));
				value = al == null ? 0 : al.visSize();
			}
			if (value != -1) {
				SCANNED.add(new Scanned(p.immutable(), value));
			}
		}
	}

	public static void render(LevelRenderContext ctx) {
		long time = System.currentTimeMillis();
		if (scanCount <= time || SCANNED.isEmpty()) {
			return;
		}
		long dif = scanCount - time;
		Vec3 cam = Minecraft.getInstance().gameRenderer.mainCamera().position();
		Quaternionf camRot = Minecraft.getInstance().gameRenderer.mainCamera().rotation();
		PoseStack ps = ctx.poseStack();
		var collector = ctx.submitNodeCollector();
		for (Scanned sc : SCANNED) {
			BlockPos d = sc.pos().subtract(center);
			float alpha = 1.0F;
			if (dif > 4750L) {
				alpha = 1.0F - (dif - 4750L) / 5.0F;
			}
			if (dif < 1500L) {
				alpha = dif / 1500.0F;
			}
			alpha *= 1.0F - (d.getX() * d.getX() + d.getY() * d.getY() + d.getZ() * d.getZ()) / 64.0F;
			if (alpha <= 0.0F) {
				continue;
			}
			ps.pushPose();
			ps.translate(sc.pos().getX() - cam.x, sc.pos().getY() - cam.y, sc.pos().getZ() - cam.z);
			if (sc.value() == -5 || sc.value() == -10) {
				blockOverlay(ps, collector, sc.value() == -5 ? 3986684 : 16734721, alpha);
			} else {
				// drawPickScannedObject: two nested glows from the node sheet, sized by how rich the ore is
				int frame = (int) (time / 50L % 32L);
				float size = sc.value() / 7.0F;
				strip(ps, collector, camRot, 0.2F * size, alpha, frame, 11184657);
				strip(ps, collector, camRot, 0.5F * size, alpha, frame, 11145506);
			}
			ps.popPose();
		}
	}

	private static void strip(PoseStack ps, net.minecraft.client.renderer.SubmitNodeCollector collector, Quaternionf camRot, float scale, float alpha,
		int frame, int color) {
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);
		ps.rotate(camRot);
		float f2 = frame / 32.0F, f3 = (frame + 1) / 32.0F, f4 = 0.0F, f5 = 1.0F / 32.0F;
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.NODES, true, false), (pose, vc) -> {
			vertex(vc, pose, -scale, -scale, 0, f3, f5, r, g, b, a);
			vertex(vc, pose, -scale, scale, 0, f3, f4, r, g, b, a);
			vertex(vc, pose, scale, scale, 0, f2, f4, r, g, b, a);
			vertex(vc, pose, scale, -scale, 0, f2, f5, r, g, b, a);
		});
		ps.popPose();
	}

	/** drawSpecialBlockoverlay: the warded-glass frame drawn on all six faces in the fluid's colour. */
	private static void blockOverlay(PoseStack ps, net.minecraft.client.renderer.SubmitNodeCollector collector, int color, float alpha) {
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(WARDED_GLASS, true, false), (pose, vc) -> {
			for (Direction dir : Direction.values()) {
				float[][] q = face(dir);
				for (float[] v : q) {
					vertex(vc, pose, v[0], v[1], v[2], v[3], v[4], r, g, b, a);
				}
			}
		});
	}

	private static float[][] face(Direction d) {
		return switch (d) {
			case DOWN -> new float[][]{{0, 0, 0, 0, 0}, {1, 0, 0, 1, 0}, {1, 0, 1, 1, 1}, {0, 0, 1, 0, 1}};
			case UP -> new float[][]{{0, 1, 0, 0, 0}, {0, 1, 1, 0, 1}, {1, 1, 1, 1, 1}, {1, 1, 0, 1, 0}};
			case NORTH -> new float[][]{{0, 0, 0, 0, 1}, {0, 1, 0, 0, 0}, {1, 1, 0, 1, 0}, {1, 0, 0, 1, 1}};
			case SOUTH -> new float[][]{{0, 0, 1, 0, 1}, {1, 0, 1, 1, 1}, {1, 1, 1, 1, 0}, {0, 1, 1, 0, 0}};
			case WEST -> new float[][]{{0, 0, 0, 0, 1}, {0, 0, 1, 1, 1}, {0, 1, 1, 1, 0}, {0, 1, 0, 0, 0}};
			case EAST -> new float[][]{{1, 0, 0, 0, 1}, {1, 1, 0, 0, 0}, {1, 1, 1, 1, 0}, {1, 0, 1, 1, 1}};
		};
	}

	private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
		vc.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	private OreScanOverlay() {
	}
}
