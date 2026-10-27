package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.wands.Architect;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.items.WandItem;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * REHWandHandler.handleArchitectOverlay: while an architect-upgraded focus is aimed at a block, the blocks it would act on glow through walls in
 * shifting blue warded-glass panes, and arrows mark the axes the misc key currently resizes.
 */
public final class ArchitectOverlay {
	private static final Identifier ARROWS = NeoThaum.id("textures/misc/architect_arrows.png");
	/** UtilsFX.connectedTextureRefByID: neighbour bitmask to warded glass pane (filled in from TC4's table). */
	private static final int[] CTM = CtmTable.TABLE;
	private static int lastHash;
	private static List<BlockPos> blocks = List.of();
	private static Set<BlockPos> blockSet = Set.of();

	public static void render(LevelRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult target) || target.getType() != HitResult.Type.BLOCK) {
			return;
		}
		ItemStack wand = player.getMainHandItem();
		boolean isShovel = wand.getItem() instanceof dev.neothaum.items.equipment.ElementalShovelItem;
		ItemStack fs = WandItem.getFocusStack(wand);
		boolean isWand = (wand.getItem() instanceof WandItem) && (fs.getItem() instanceof FocusItem focus) && (focus instanceof Architect af)
			&& focus.isUpgradedWith(fs, FocusUpgradeType.architect);
		if (!isShovel && !isWand) {
			return;
		}
		int ticks = player.tickCount;
		int hc = (target.getBlockPos().toShortString() + target.getDirection() + ticks / 5 + (isShovel ? dev.neothaum.items.equipment.ElementalShovelItem.getOrientation(wand) : 0)).hashCode();
		if (hc != lastHash) {
			lastHash = hc;
			if (isShovel) {
				blocks = dev.neothaum.items.equipment.ElementalShovelItem.architectBlocks(wand, mc.level, target.getBlockPos(), target.getDirection(), player);
			} else {
				FocusItem focus = (FocusItem) fs.getItem();
				Architect af = (Architect) focus;
				blocks = af.getArchitectBlocks(wand, mc.level, target.getBlockPos(), target.getDirection(), player);
			}
			blockSet = new HashSet<>(blocks);
		}
		if (blocks.isEmpty()) {
			return;
		}
		Vec3 cam = mc.gameRenderer.mainCamera().position();
		PoseStack ps = ctx.poseStack();
		BlockPos t = target.getBlockPos();
		if (isWand) {
			FocusItem focus = (FocusItem) fs.getItem();
			Architect af = (Architect) focus;
			drawAxis(ctx, ps, cam, t, ticks, af.showAxis(wand, mc.level, player, target.getDirection(), Architect.Axis.X),
				af.showAxis(wand, mc.level, player, target.getDirection(), Architect.Axis.Y),
				af.showAxis(wand, mc.level, player, target.getDirection(), Architect.Axis.Z));
		}
		var atlas = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
		for (BlockPos p : blocks) {
			float r = Mth.sin(ticks / 2.0F + p.getX()) * 0.2F + 0.3F;
			float g = Mth.sin(ticks / 3.0F + p.getY()) * 0.2F + 0.3F;
			float b = Mth.sin(ticks / 4.0F + p.getZ()) * 0.2F + 0.8F;
			ps.pushPose();
			ps.translate(p.getX() - cam.x, p.getY() - cam.y, p.getZ() - cam.z);
			ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(TextureAtlas.LOCATION_BLOCKS, true, false), (pose, vc) -> {
				for (Direction d : Direction.values()) {
					if (blockSet.contains(p.relative(d))) {
						continue;
					}
					TextureAtlasSprite sp = atlas.getSprite(NeoThaum.id("blocks/warded_glass_" + (CTM[mask(p, d)] + 1)));
					face(pose, vc, d, sp, r, g, b);
				}
			});
			ps.popPose();
		}
	}

	private static boolean in(int x, int y, int z) {
		return blockSet.contains(new BlockPos(x, y, z));
	}

	/** REHWandHandler.getIconOnSide: which of the eight neighbours around this face are also in the area. */
	private static int mask(BlockPos p, Direction d) {
		int x = p.getX();
		int y = p.getY();
		int z = p.getZ();
		int side = d.get3DDataValue();
		boolean[] m = new boolean[8];
		if (side == 0 || side == 1) {
			m[0] = in(x - 1, y, z - 1);
			m[1] = in(x, y, z - 1);
			m[2] = in(x + 1, y, z - 1);
			m[3] = in(x - 1, y, z);
			m[4] = in(x + 1, y, z);
			m[5] = in(x - 1, y, z + 1);
			m[6] = in(x, y, z + 1);
			m[7] = in(x + 1, y, z + 1);
		} else if (side == 2 || side == 3) {
			int a = side == 2 ? 1 : -1;
			int c = side == 3 ? 1 : -1;
			m[0] = in(x + a, y + 1, z);
			m[1] = in(x, y + 1, z);
			m[2] = in(x + c, y + 1, z);
			m[3] = in(x + a, y, z);
			m[4] = in(x + c, y, z);
			m[5] = in(x + a, y - 1, z);
			m[6] = in(x, y - 1, z);
			m[7] = in(x + c, y - 1, z);
		} else {
			int a = side == 5 ? 1 : -1;
			int c = side == 4 ? 1 : -1;
			m[0] = in(x, y + 1, z + a);
			m[1] = in(x, y + 1, z);
			m[2] = in(x, y + 1, z + c);
			m[3] = in(x, y, z + a);
			m[4] = in(x, y, z + c);
			m[5] = in(x, y - 1, z + a);
			m[6] = in(x, y - 1, z);
			m[7] = in(x, y - 1, z + c);
		}
		int id = 0;
		for (int i = 0; i < 8; i++) {
			if (m[i]) {
				id |= 1 << i;
			}
		}
		return id;
	}

	private static void face(PoseStack.Pose pose, VertexConsumer vc, Direction d, TextureAtlasSprite sp, float r, float g, float b) {
		float lo = -0.001F;
		float hi = 1.001F;
		float[][] c = switch (d) {
			case DOWN -> new float[][]{{lo, lo, hi}, {lo, lo, lo}, {hi, lo, lo}, {hi, lo, hi}};
			case UP -> new float[][]{{lo, hi, lo}, {lo, hi, hi}, {hi, hi, hi}, {hi, hi, lo}};
			case NORTH -> new float[][]{{hi, hi, lo}, {hi, lo, lo}, {lo, lo, lo}, {lo, hi, lo}};
			case SOUTH -> new float[][]{{lo, hi, hi}, {lo, lo, hi}, {hi, lo, hi}, {hi, hi, hi}};
			case WEST -> new float[][]{{lo, hi, lo}, {lo, lo, lo}, {lo, lo, hi}, {lo, hi, hi}};
			case EAST -> new float[][]{{hi, hi, hi}, {hi, lo, hi}, {hi, lo, lo}, {hi, hi, lo}};
		};
		float[][] uv = {{sp.getU0(), sp.getV0()}, {sp.getU0(), sp.getV1()}, {sp.getU1(), sp.getV1()}, {sp.getU1(), sp.getV0()}};
		for (int i = 0; i < 4; i++) {
			vc.addVertex(pose, c[i][0], c[i][1], c[i][2]).setColor(r, g, b, 0.2F).setUv(uv[i][0], uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(0xF000F0).setNormal(pose, d.getStepX(), d.getStepY(), d.getStepZ());
		}
	}

	/** REHWandHandler.drawArchitectAxis: crossed arrow cards through the target along each resizable axis. */
	private static void drawAxis(LevelRenderContext ctx, PoseStack ps, Vec3 cam, BlockPos t, int ticks, boolean dx, boolean dy, boolean dz) {
		if (!dx && !dy && !dz) {
			return;
		}
		float r = Mth.sin(ticks / 4.0F + t.getX()) * 0.2F + 0.3F;
		float g = Mth.sin(ticks / 3.0F + t.getY()) * 0.2F + 0.3F;
		float b = Mth.sin(ticks / 2.0F + t.getZ()) * 0.2F + 0.8F;
		ps.pushPose();
		ps.translate(t.getX() + 0.5 - cam.x, t.getY() + 0.5 - cam.y, t.getZ() + 0.5 - cam.z);
		ps.rotateDegrees(Axis.XP, 90.0F);
		if (dx) {
			card(ctx, ps, r, g, b, 0.0F, 0.0F, 0.0F);
			card(ctx, ps, r, g, b, 90.0F, 0.0F, 0.0F);
		}
		if (dz) {
			card(ctx, ps, r, g, b, 0.0F, 0.0F, 90.0F);
			card(ctx, ps, r, g, b, 90.0F, 0.0F, 90.0F);
		}
		if (dy) {
			card(ctx, ps, r, g, b, 0.0F, 90.0F, 0.0F);
			card(ctx, ps, r, g, b, 90.0F, 90.0F, 0.0F);
		}
		ps.popPose();
	}

	private static void card(LevelRenderContext ctx, PoseStack ps, float r, float g, float b, float rx, float ry, float rz) {
		ps.pushPose();
		ps.rotateDegrees(Axis.YP, ry);
		ps.rotateDegrees(Axis.ZP, rz);
		ps.rotateDegrees(Axis.XP, rx);
		ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(ARROWS, true, false), (pose, vc) -> {
			float[][] q = {{-0.5F, 0.5F, 0, 1}, {0.5F, 0.5F, 1, 1}, {0.5F, -0.5F, 1, 0}, {-0.5F, -0.5F, 0, 0}};
			for (float[] v : q) {
				vc.addVertex(pose, v[0], v[1], 0.0F).setColor(r, g, b, 1.0F).setUv(v[2], v[3]).setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
			}
		});
		ps.popPose();
	}

	private ArchitectOverlay() {
	}
}
