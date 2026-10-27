package dev.neothaum.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** TileBellows.onVanillaFurnace: bellows pump a furnace's cook timer along. */
@Mixin(AbstractFurnaceBlockEntity.class)
public interface FurnaceAccessor {
	@Accessor("cookingTimer")
	int neothaum$getCookingTimer();

	@Accessor("cookingTimer")
	void neothaum$setCookingTimer(int value);

	@Accessor("cookingTotalTime")
	int neothaum$getCookingTotalTime();
}
