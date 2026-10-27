package dev.neothaum.items;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.AlembicBlockEntity;
import dev.neothaum.tiles.JarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * ItemEssence: the empty glass phial scoops 8 essentia out of a jar or alembic; the filled phial pours its 8 into a jar that will take them.
 */
public class EssentiaPhialItem extends Item {
	public static final int SIZE = 8;
	private final boolean filled;

	public EssentiaPhialItem(boolean filled, Properties props) {
		super(props);
		this.filled = filled;
	}

	public static ItemStack of(Aspect aspect) {
		ItemStack stack = new ItemStack(TVItems.PHIAL_OF_ESSENTIA);
		stack.set(TVComponents.ASPECTS, new AspectList().add(aspect, SIZE));
		return stack;
	}

	public static Aspect aspectOf(ItemStack stack) {
		AspectList al = stack.get(TVComponents.ASPECTS);
		return al == null || al.size() == 0 ? null : al.getAspects()[0];
	}

	@Override
	public Component getName(ItemStack stack) {
		Aspect a = this.filled ? aspectOf(stack) : null;
		return a == null ? super.getName(stack) : Component.translatable("item.neothaum.phial_of_essentia.of", a.getName());
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockEntity be = level.getBlockEntity(pos);
		if (!this.filled) {
			Aspect a = null;
			if (be instanceof JarBlockEntity jar && jar.amount >= SIZE) {
				a = jar.aspect;
				if (!level.isClientSide()) {
					jar.takeFromContainer(a, SIZE);
				}
			} else if (be instanceof AlembicBlockEntity al && al.amount >= SIZE) {
				a = al.aspect;
				if (!level.isClientSide()) {
					al.takeFromContainer(a, SIZE);
				}
			}
			if (a == null) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				give(player, of(a));
			}
			level.playSound(player, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.25F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		Aspect a = aspectOf(stack);
		if (a != null && be instanceof JarBlockEntity jar && jar.amount <= JarBlockEntity.MAX - SIZE && (jar.aspect == null || jar.amount == 0 || jar.aspect == a)
			&& jar.doesContainerAccept(a)) {
			if (!level.isClientSide()) {
				jar.addToContainer(a, SIZE);
				stack.consume(1, player);
				give(player, new ItemStack(TVItems.GLASS_PHIAL));
			}
			level.playSound(player, pos, TVSounds.JAR, SoundSource.BLOCKS, 0.4F, 1.0F);
			level.playSound(player, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.5F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private static void give(Player player, ItemStack stack) {
		if (player != null && !player.getInventory().add(stack)) {
			player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
		}
	}
}
