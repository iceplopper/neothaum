package dev.neothaum.items.equipment;

import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * ItemElementalSword, the Sword of the Zephyr: each swing also cuts whatever crowds round its target; held up, a whirlwind lifts you and
 * flings everything nearby away.
 */
public class ElementalSwordItem extends Item {
	private static final ThreadLocal<Boolean> SWEEPING = ThreadLocal.withInitial(() -> false);

	public ElementalSwordItem(Properties props) {
		super(props);
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
		int ticks = this.getUseDuration(stack, player) - count;
		Vec3 m = player.getDeltaMovement();
		double my = m.y;
		if (my < 0.0) {
			my /= 1.2F;
			player.fallDistance /= 1.2F;
		}
		my += 0.08F;
		if (my > 0.5) {
			my = 0.2F;
		}
		player.setDeltaMovement(m.x, my, m.z);
		if (player instanceof ServerPlayer sp) {
			// Utils.resetFloatCounter: hovering is allowed
			sp.connection.resetFlyingTicks();
		}
		for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(2.5))) {
			if (!(entity instanceof Player) && entity.isAlive() && player.getVehicle() != entity) {
				Vec3 r = entity.position().subtract(player.position());
				double distance = r.length() + 0.1;
				entity.setDeltaMovement(entity.getDeltaMovement().add(r.x / 2.5 / distance, r.y / 2.5 / distance, r.z / 2.5 / distance));
			}
		}
		if (level.isClientSide()) {
			var rand = level.getRandom();
			int miny = (int) (player.getBoundingBox().minY - 2.0);
			if (player.onGround()) {
				miny = Mth.floor(player.getBoundingBox().minY);
			}
			for (int a = 0; a < 5; a++) {
				FX.smokeSpiral(level, player.getX(), player.getBoundingBox().minY + player.getBbHeight() / 2.0F, player.getZ(), 1.5F, rand.nextInt(360), miny,
					14540253);
			}
			if (player.onGround()) {
				float r1 = rand.nextFloat() * 360.0F;
				level.addParticle(ParticleTypes.SMOKE, player.getX(), player.getBoundingBox().minY + 0.1F, player.getZ(),
					-Mth.sin(r1 / 180.0F * Mth.PI) / 5.0F, 0.0, Mth.cos(r1 / 180.0F * Mth.PI) / 5.0F);
			}
		} else if (ticks == 0 || ticks % 20 == 0) {
			level.playSound(null, player.blockPosition(), TVSounds.WIND, SoundSource.PLAYERS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
		}
		if (ticks % 20 == 0) {
			stack.hurtAndBreak(1, player, player.getUsedItemHand());
		}
	}

	/** onLeftClickEntity: the blow carries to every other mob pressed close to the one you struck (not your own pets). */
	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (SWEEPING.get() || !(attacker instanceof Player player) || player.level().isClientSide()) {
			return;
		}
		int count = 0;
		SWEEPING.set(true);
		try {
			for (Entity e : player.level().getEntities(player, target.getBoundingBox().inflate(1.2, 1.1, 1.2))) {
				if (e != target && e instanceof Mob mob && mob.isAlive() && !(e instanceof OwnableEntity o && player.equals(o.getOwner()))) {
					player.attack(mob);
					count++;
				}
			}
		} finally {
			SWEEPING.set(false);
		}
		if (count > 0) {
			player.level().playSound(null, target.blockPosition(), TVSounds.SWING, SoundSource.PLAYERS, 1.0F,
				0.9F + player.level().getRandom().nextFloat() * 0.2F);
		}
	}
}
