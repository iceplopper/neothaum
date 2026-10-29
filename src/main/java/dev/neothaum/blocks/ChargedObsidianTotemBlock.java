package dev.neothaum.blocks;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.items.EtherealEssenceItem;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.NodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockCosmeticSolid metadata 8, the charged obsidian totem: looks and stacks like an obsidian totem but holds an aura node (the tops of
 * the totems TC4 generated). Broken by a player, it gives up an ethereal essence for every ten points of each aspect in its node.
 */
public class ChargedObsidianTotemBlock extends ObsidianTotemBlock implements EntityBlock {
	public ChargedObsidianTotemBlock(Properties props) {
		super(props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new NodeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state,
		net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
		return type == dev.neothaum.registry.TVBlockEntities.NODE ? (l, p, s, be) -> NodeBlockEntity.tick(l, p, s, (NodeBlockEntity) be) : null;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof NodeBlockEntity node && node.getAspects().size() > 0) {
			AspectList al = node.getAspects();
			for (Aspect aspect : al.getAspects()) {
				for (int a = 0; a <= al.getAmount(aspect) / 10; a++) {
					if (al.getAmount(aspect) >= 5) {
						popResource(level, pos, EtherealEssenceItem.of(aspect));
					}
				}
			}
		}
		// addDestroyEffects: a burst and the craft-fail crackle
		level.playSound(null, pos, TVSounds.CRAFTFAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
		return super.playerWillDestroy(level, pos, state, player);
	}
}
