package dev.thaumvibe.devtools;

import dev.thaumvibe.world.TVBiomes;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Dev tool: use in the air to pick a biome; use on a block to paint it over a 9x9 area round that spot (plains puts it back to normal). */
public class BiomePainterItem extends Item {
	private static final List<ResourceKey<Biome>> BIOMES = List.of(TVBiomes.MAGICAL_FOREST, TVBiomes.TAINTED_LAND, TVBiomes.EERIE, Biomes.PLAINS);

	public BiomePainterItem(Item.Properties props) {
		super(props);
	}

	private static int selected(ItemStack stack) {
		return Math.floorMod(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("Biome", 0), BIOMES.size());
	}

	private static String name(ResourceKey<Biome> biome) {
		return Component.translatable("biome." + biome.identifier().getNamespace() + "." + biome.identifier().getPath()).getString();
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isCreative()) {
			return InteractionResult.FAIL;
		}
		ItemStack stack = player.getItemInHand(hand);
		int i = Math.floorMod(selected(stack) + (player.isShiftKeyDown() ? -1 : 1), BIOMES.size());
		CompoundTag tag = new CompoundTag();
		tag.putInt("Biome", i);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		if (!level.isClientSide()) {
			player.sendOverlayMessage(Component.literal("[Dev] Biome: " + name(BIOMES.get(i))).withStyle(ChatFormatting.LIGHT_PURPLE));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null || !player.isCreative()) {
			return InteractionResult.FAIL;
		}
		if (ctx.getLevel() instanceof ServerLevel level) {
			ResourceKey<Biome> biome = BIOMES.get(selected(ctx.getItemInHand()));
			BlockPos c = ctx.getClickedPos();
			for (int dx = -4; dx <= 4; dx += 4) {
				for (int dz = -4; dz <= 4; dz += 4) {
					TVBiomes.setBiomeAt(level, c.getX() + dx, c.getZ() + dz, biome);
				}
			}
			player.sendOverlayMessage(Component.literal("[Dev] Painted " + name(biome)).withStyle(ChatFormatting.LIGHT_PURPLE));
		}
		return InteractionResult.SUCCESS;
	}
}
