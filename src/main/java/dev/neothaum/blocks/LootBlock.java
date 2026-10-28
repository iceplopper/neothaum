package dev.neothaum.blocks;

import dev.neothaum.items.LootBagItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockLoot: the urns and crates found in barrow mounds, common (0), uncommon (1) or rare (2). Broken, one gives up 1 + rarity + up to 2
 * things from the loot bag of its rarity (Utils.generateLoot).
 */
public class LootBlock extends Block {
	private static final VoxelShape URN = Block.box(2, 0, 2, 14, 16, 14);
	private static final VoxelShape CRATE = Block.box(1, 0, 1, 15, 14, 15);
	private final boolean urn;
	private final int rarity;

	public LootBlock(boolean urn, int rarity, Properties props) {
		super(props);
		this.urn = urn;
		this.rarity = rarity;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return this.urn ? URN : CRATE;
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		List<ItemStack> ret = new ArrayList<>();
		var level = params.getLevel();
		int q = 1 + this.rarity + level.getRandom().nextInt(3);
		for (int a = 0; a < q; a++) {
			ItemStack is = LootBagItem.generateLoot(this.rarity, level.getRandom(), level);
			if (is != null && !is.isEmpty()) {
				ret.add(is.copy());
			}
		}
		return ret;
	}
}
