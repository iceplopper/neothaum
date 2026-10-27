package dev.neothaum.items;

import dev.neothaum.ClientBridge;
import dev.neothaum.registry.TVSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Port of ItemThaumonomicon: opens the research browser. */
public class ThaumonomiconItem extends Item {
	public ThaumonomiconItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			ClientBridge.openScreen.accept("thaumonomicon");
		} else {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.PAGE, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}
}
