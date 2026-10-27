package dev.neothaum.items.equipment;

import dev.neothaum.ClientBridge;
import dev.neothaum.registry.TVSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** ItemElementalPickaxe, the Pickaxe of the Core: sets what it hits alight; used on a block, it shows the ores around it for five seconds. */
public class ElementalPickaxeItem extends Item {
	public ElementalPickaxeItem(Properties props) {
		super(props);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		// players only burn where they may hurt each other
		if (!(target instanceof Player tp) || attacker instanceof Player ap && ap.canHarmPlayer(tp)) {
			target.igniteForSeconds(2.0F);
		}
		super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		var level = ctx.getLevel();
		var pos = ctx.getClickedPos();
		ctx.getItemInHand().hurtAndBreak(5, player, ctx.getHand());
		if (!level.isClientSide()) {
			level.playSound(null, pos, TVSounds.WANDFAIL, SoundSource.PLAYERS, 0.2F, 0.2F + level.getRandom().nextFloat() * 0.2F);
		} else {
			ClientBridge.oreScan.accept(pos);
		}
		return InteractionResult.SUCCESS;
	}
}
