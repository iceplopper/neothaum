package dev.neothaum.fluids;

import dev.neothaum.NeoThaum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;

/** NeoThaum's flowing fluids. Registered before the blocks that hold them. */
public final class TVFluids {
	public static final FlowingFluid PURIFYING = Registry.register(BuiltInRegistries.FLUID, NeoThaum.id("purifying_fluid"), new PurifyingFluid.Source());
	public static final FlowingFluid FLOWING_PURIFYING = Registry.register(BuiltInRegistries.FLUID, NeoThaum.id("flowing_purifying_fluid"),
		new PurifyingFluid.Flowing());

	private TVFluids() {
	}

	public static void init() {
	}
}
