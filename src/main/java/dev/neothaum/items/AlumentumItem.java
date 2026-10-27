package dev.neothaum.items;

import dev.neothaum.entity.projectile.AlumentumEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

/**
 * Port of TC4 Alumentum:
 * Burns for 6400 ticks in furnace / twice as fast in alchemical furnace.
 * Right click throws an explosive projectile (EntityAlumentum) at velocity 0.75F.
 * Also supports dispensers via ProjectileItem.
 */
public class AlumentumItem extends Item implements ProjectileItem {
	public AlumentumItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.3F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

		if (level instanceof ServerLevel sl) {
			Projectile.spawnProjectileFromRotation(AlumentumEntity::new, sl, stack, player, 0.0F, 0.75F, 1.0F);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	@Override
	public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction dir) {
		return new AlumentumEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
	}

	@Override
	public ProjectileItem.DispenseConfig createDispenseConfig() {
		return ProjectileItem.DispenseConfig.builder()
			.uncertainty(1.0F)
			.power(0.75F)
			.build();
	}
}
