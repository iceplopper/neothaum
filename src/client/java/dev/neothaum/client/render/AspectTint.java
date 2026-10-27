package dev.neothaum.client.render;

import com.mojang.serialization.MapCodec;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVComponents;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Item tint "neothaum:aspect": the colour of the stack's essentia (ItemEssence pass 1) or of its label's aspect. */
public record AspectTint() implements ItemTintSource {
	public static final MapCodec<AspectTint> MAP_CODEC = MapCodec.unit(new AspectTint());

	@Override
	public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
		AspectList al = stack.get(TVComponents.ASPECTS);
		Aspect a = al != null && al.size() > 0 ? al.getAspects()[0] : stack.get(TVComponents.ASPECT_FILTER);
		return a == null ? -1 : 0xFF000000 | a.getColor();
	}

	@Override
	public MapCodec<AspectTint> type() {
		return MAP_CODEC;
	}
}
