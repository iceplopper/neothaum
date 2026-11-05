package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.NodeBlockEntity;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Blocks placed where a node is go inside it rather than destroying it: the node keeps working within the block, shown through a see-through
 * one and hidden by an opaque one (so a node can also be built into a wall as decoration). Breaking the block gives it back and leaves the
 * node. Only plain blocks fit: no block entities, fluids or blocks drawn some other way.
 */
public final class NodeEmbedding {
	public static void init() {
		UseBlockCallback.EVENT.register(NodeEmbedding::place);
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, be) -> !(be instanceof NodeBlockEntity node && node.embedded != null)
			|| takeOut(level, player, pos, node));
	}

	private static InteractionResult place(Player player, Level level, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(stack.getItem() instanceof BlockItem item) || player.isSpectator() || !dev.neothaum.addon.Additions.enabled("node_embedding")) {
			return InteractionResult.PASS;
		}
		BlockPlaceContext ctx = new BlockPlaceContext(player, hand, stack, hit);
		BlockPos pos = ctx.getClickedPos();
		if (!level.getBlockState(pos).is(TVBlocks.AURA_NODE) || !(level.getBlockEntity(pos) instanceof NodeBlockEntity node) || node.embedded != null) {
			return InteractionResult.PASS;
		}
		Block block = item.getBlock();
		BlockState inside = block.getStateForPlacement(ctx);
		if (inside == null || block instanceof EntityBlock || !inside.getFluidState().isEmpty() || inside.getRenderShape() != RenderShape.MODEL
			|| !inside.canSurvive(level, pos)) {
			// nothing else can go in a node's space
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			node.setEmbedded(inside);
			var sound = inside.getSoundType();
			level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
			stack.consume(1, player);
		}
		return InteractionResult.SUCCESS;
	}

	/** Breaking a node with a block inside: the block drops (or not, in creative) and the node stays. Returns false to stop the break. */
	private static boolean takeOut(Level level, Player player, BlockPos pos, NodeBlockEntity node) {
		BlockState inside = node.embedded;
		if (level instanceof ServerLevel sl && !player.isCreative() && player.hasCorrectToolForDrops(inside)) {
			Block.dropResources(inside, sl, pos, null, player, player.getMainHandItem());
		}
		level.levelEvent(2001, pos, Block.getId(inside));
		node.setEmbedded(null);
		return false;
	}

	private NodeEmbedding() {
	}
}
