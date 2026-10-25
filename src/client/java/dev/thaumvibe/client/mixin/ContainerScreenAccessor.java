package dev.thaumvibe.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor {
	@Accessor("hoveredSlot")
	Slot thaumvibe$hoveredSlot();

	@Accessor("leftPos")
	int thaumvibe$leftPos();

	@Accessor("topPos")
	int thaumvibe$topPos();
}
