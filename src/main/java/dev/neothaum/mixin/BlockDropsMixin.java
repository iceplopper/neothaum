package dev.neothaum.mixin;

import dev.neothaum.items.equipment.SpecialMining;
import dev.neothaum.registry.TVItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * EventHandlerWorld.harvestDrops: mining with the pickaxe of the core or the primal crusher turns ore drops into native clusters, one cluster
 * for each raw ore dropped (so Fortune counts). The pickaxe of the core does it every time, now that ores drop raw metal that Fortune
 * multiplies; the primal crusher keeps TC4's chance, 20% plus 7.5% a level of Fortune.
 */
@Mixin(Block.class)
public abstract class BlockDropsMixin {
	@Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;"
		+ "Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
		at = @At("RETURN"), cancellable = true)
	private static void neothaum$nativeClusters(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity be, @Nullable Entity breaker,
		ItemInstance tool, CallbackInfoReturnable<List<ItemStack>> cir) {
		if (!(breaker instanceof Player) || !(tool instanceof ItemStack held)) {
			return;
		}
		boolean core = held.is(TVItems.PICKAXE_OF_THE_CORE);
		if (!core && !held.is(TVItems.PRIMAL_CRUSHER)) {
			return;
		}
		int fortune = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
			.getOrThrow(Enchantments.FORTUNE), held);
		// above 1, every drop SpecialMining knows becomes a cluster (its own chance aside, as for gold and cinnabar)
		float chance = core ? 100.0F : 0.2F + fortune * 0.075F;
		List<ItemStack> out = new ArrayList<>();
		boolean changed = false;
		for (ItemStack is : cir.getReturnValue()) {
			ItemStack r = SpecialMining.find(is, chance, level.getRandom());
			changed |= !ItemStack.isSameItem(is, r);
			out.add(r);
		}
		if (changed) {
			level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.2F, 0.7F + level.getRandom().nextFloat() * 0.2F);
			cir.setReturnValue(out);
		}
	}
}
