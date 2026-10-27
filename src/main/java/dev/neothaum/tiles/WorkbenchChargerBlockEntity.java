package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** TileMagicWorkbenchCharger, the vis charge relay: set on an arcane worktable, it tops up the wand in the table from the vis network. */
public class WorkbenchChargerBlockEntity extends VisRelayBlockEntity {
	public WorkbenchChargerBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.WORKBENCH_CHARGER, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, WorkbenchChargerBlockEntity be) {
		be.relayTick();
		if (level.isClientSide() || !(level.getBlockEntity(pos.below()) instanceof ArcaneWorktableBlockEntity table)) {
			return;
		}
		ItemStack wand = table.getItem(ArcaneWorktableBlockEntity.WAND_SLOT);
		if (!(wand.getItem() instanceof WandItem)) {
			return;
		}
		AspectList room = WandItem.getAspectsWithRoom(wand);
		boolean changed = false;
		for (Aspect aspect : room.getAspects()) {
			int drain = Math.min(5, WandItem.getMaxVis(wand) - WandItem.getVis(wand, aspect));
			if (drain > 0) {
				int got = be.consumeVis(aspect, drain);
				if (got > 0) {
					WandItem.addRealVis(wand, aspect, got, true);
					changed = true;
				}
			}
		}
		if (changed) {
			table.setChanged();
		}
	}
}
