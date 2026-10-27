package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neothaum.NeoThaum;
import dev.neothaum.items.WandItem;
import dev.neothaum.items.foci.WardingFocusItem;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.WardedBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * BlockWardedRenderer and TileWardedRenderer: a warded block looks like the block it holds. To someone holding a warding focus every warded block
 * also wears a pane of warded glass, gold for their own and faint red for anyone else's.
 */
public class WardedBlockRenderer implements BlockEntityRenderer<WardedBlockEntity, WardedBlockRenderer.State> {
	public static class State extends BlockEntityRenderState {
		final MovingBlockRenderState block = new MovingBlockRenderState();
		boolean overlay;
		boolean own;
		int ticks;
		final int[] faceIcons = new int[6];
		final boolean[] faces = new boolean[6];
	}

	public WardedBlockRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(WardedBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(be, s, crumbling);
		Level level = be.getLevel();
		BlockPos pos = be.getBlockPos();
		s.block.randomSeedPos = pos;
		s.block.blockPos = pos;
		s.block.blockState = be.held;
		if (level instanceof ClientLevel cl) {
			s.block.biome = cl.getBiome(pos);
			s.block.cardinalLighting = cl.cardinalLighting();
			s.block.lightEngine = cl.getLightEngine();
		}
		Player player = Minecraft.getInstance().player;
		s.overlay = player != null && player.getMainHandItem().getItem() instanceof WandItem
			&& WandItem.getFocus(player.getMainHandItem()) instanceof WardingFocusItem;
		if (s.overlay && level != null) {
			s.own = be.owner == WardingFocusItem.ownerId(player);
			s.ticks = player.tickCount;
			for (Direction d : Direction.values()) {
				s.faces[d.get3DDataValue()] = !level.getBlockState(pos.relative(d)).is(TVBlocks.WARDED_BLOCK);
				s.faceIcons[d.get3DDataValue()] = icon(level, pos, d, be.owner);
			}
		}
	}

	private static boolean connected(Level level, int x, int y, int z, int owner) {
		return level.getBlockEntity(new BlockPos(x, y, z)) instanceof WardedBlockEntity w && w.owner == owner;
	}

	/** TileWardedRenderer.getIconOnSide: the connected warded glass pane for this face. */
	private static int icon(Level level, BlockPos p, Direction d, int owner) {
		int x = p.getX();
		int y = p.getY();
		int z = p.getZ();
		int side = d.get3DDataValue();
		boolean[] m = new boolean[8];
		if (side == 0 || side == 1) {
			m[0] = connected(level, x - 1, y, z - 1, owner);
			m[1] = connected(level, x, y, z - 1, owner);
			m[2] = connected(level, x + 1, y, z - 1, owner);
			m[3] = connected(level, x - 1, y, z, owner);
			m[4] = connected(level, x + 1, y, z, owner);
			m[5] = connected(level, x - 1, y, z + 1, owner);
			m[6] = connected(level, x, y, z + 1, owner);
			m[7] = connected(level, x + 1, y, z + 1, owner);
		} else if (side == 2 || side == 3) {
			int a = side == 2 ? 1 : -1;
			int c = side == 3 ? 1 : -1;
			m[0] = connected(level, x + a, y + 1, z, owner);
			m[1] = connected(level, x, y + 1, z, owner);
			m[2] = connected(level, x + c, y + 1, z, owner);
			m[3] = connected(level, x + a, y, z, owner);
			m[4] = connected(level, x + c, y, z, owner);
			m[5] = connected(level, x + a, y - 1, z, owner);
			m[6] = connected(level, x, y - 1, z, owner);
			m[7] = connected(level, x + c, y - 1, z, owner);
		} else {
			int a = side == 5 ? 1 : -1;
			int c = side == 4 ? 1 : -1;
			m[0] = connected(level, x, y + 1, z + a, owner);
			m[1] = connected(level, x, y + 1, z, owner);
			m[2] = connected(level, x, y + 1, z + c, owner);
			m[3] = connected(level, x, y, z + a, owner);
			m[4] = connected(level, x, y, z + c, owner);
			m[5] = connected(level, x, y - 1, z + a, owner);
			m[6] = connected(level, x, y - 1, z, owner);
			m[7] = connected(level, x, y - 1, z + c, owner);
		}
		int id = 0;
		for (int i = 0; i < 8; i++) {
			if (m[i]) {
				id |= 1 << i;
			}
		}
		return CtmTable.TABLE[id];
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (s.block.blockState != null && s.block.lightEngine != null) {
			collector.submitMovingBlock(ps, s.block, s.lightCoords);
		}
		if (!s.overlay) {
			return;
		}
		BlockPos p = s.blockPos;
		float r;
		float g;
		float b;
		float a;
		if (s.own) {
			r = Mth.sin(s.ticks / 2.0F + p.getX()) * 0.2F + 0.8F;
			g = Mth.sin(s.ticks / 3.0F + p.getY()) * 0.2F + 0.7F;
			b = Mth.sin(s.ticks / 4.0F + p.getZ()) * 0.2F + 0.28F;
			a = 0.5F;
		} else {
			r = Mth.sin(s.ticks / 2.0F + p.getX()) * 0.2F + 0.8F;
			g = Mth.sin(s.ticks / 3.0F + p.getY()) * 0.2F + 0.28F;
			b = Mth.sin(s.ticks / 4.0F + p.getZ()) * 0.2F + 0.28F;
			a = 0.25F;
		}
		var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(TextureAtlas.LOCATION_BLOCKS, true, true), (pose, vc) -> {
			for (Direction d : Direction.values()) {
				if (s.faces[d.get3DDataValue()]) {
					TextureAtlasSprite sp = atlas.getSprite(NeoThaum.id("blocks/warded_glass_" + (s.faceIcons[d.get3DDataValue()] + 1)));
					face(pose, vc, d, sp, r, g, b, a);
				}
			}
		});
	}

	private static void face(PoseStack.Pose pose, VertexConsumer vc, Direction d, TextureAtlasSprite sp, float r, float g, float b, float a) {
		float lo = -0.0001F;
		float hi = 1.0001F;
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
			vc.addVertex(pose, c[i][0], c[i][1], c[i][2]).setColor(r, g, b, a).setUv(uv[i][0], uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(200).setNormal(pose, d.getStepX(), d.getStepY(), d.getStepZ());
		}
	}
}
